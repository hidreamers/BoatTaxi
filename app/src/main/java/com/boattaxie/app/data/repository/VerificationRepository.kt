package com.boattaxie.app.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.boattaxie.app.data.model.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerificationRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) {
    private val userId: String?
        get() = auth.currentUser?.uid
    
    /**
     * Save document image locally
     */
    private fun saveDocumentLocally(imageUri: Uri, documentType: DocumentType, uid: String): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return null
            val docsDir = File(context.filesDir, "verification_docs")
            if (!docsDir.exists()) docsDir.mkdirs()
            
            val imageFile = File(docsDir, "${uid}_${documentType.name}_${System.currentTimeMillis()}.jpg")
            imageFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            
            imageFile.absolutePath
        } catch (e: Exception) {
            android.util.Log.e("VerificationRepo", "Failed to save document locally: ${e.message}")
            null
        }
    }
    
    /**
     * Upload a verification document image - saves locally instead of Firebase Storage
     */
    suspend fun uploadDocument(
        imageUri: Uri,
        documentType: DocumentType,
        vehicleType: VehicleType
    ): Result<VerificationDocument> = runCatching {
        val uid = userId ?: throw Exception("User not logged in")
        
        // Save image locally instead of Firebase Storage
        val localPath = saveDocumentLocally(imageUri, documentType, uid)
            ?: throw Exception("Failed to save document locally")
        
        // Create verification document record
        val document = VerificationDocument(
            id = UUID.randomUUID().toString(),
            userId = uid,
            vehicleType = vehicleType,
            documentType = documentType,
            documentUrl = localPath, // Store local file path
            status = VerificationStatus.PENDING
        )
        
        // Save to Firestore
        firestore.collection("verification_documents")
            .document(document.id)
            .set(document)
            .await()
        
        document
    }
    
    /**
     * Send email with verification documents attached
     */
    fun sendVerificationEmail(documents: List<VerificationDocument>, vehicleType: VehicleType, userName: String): Intent {
        val uid = userId ?: "unknown"
        val userEmail = auth.currentUser?.email ?: "No email"
        
        // Collect document files
        val attachmentUris = mutableListOf<Uri>()
        val documentInfo = StringBuilder()
        
        documents.forEach { doc ->
            val file = File(doc.documentUrl)
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                attachmentUris.add(uri)
                documentInfo.append("- ${doc.documentType.name}: ${file.name}\n")
            }
        }
        
        val emailBody = """
            |New Driver Verification Request
            |================================
            |
            |User ID: $uid
            |User Name: $userName
            |User Email: $userEmail
            |Vehicle Type: ${vehicleType.name}
            |Submitted: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}
            |
            |Documents Attached:
            |$documentInfo
            |
            |Please review and approve/reject this driver verification request.
        """.trimMargin()
        
        val emailIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("jerimiah@lacunabotanicals.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Driver Verification - ${vehicleType.name} - $userName")
            putExtra(Intent.EXTRA_TEXT, emailBody)
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(attachmentUris))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        return emailIntent
    }
    
    /**
     * Submit all verification documents for review
     * Sets status to PENDING - admin must approve via push notification
     */
    suspend fun submitVerification(
        vehicleType: VehicleType,
        documents: List<VerificationDocument>
    ): Result<VerificationSubmission> = runCatching {
        val uid = userId ?: throw Exception("User not logged in")
        val userName = auth.currentUser?.displayName ?: auth.currentUser?.email ?: "Unknown"
        
        val submission = VerificationSubmission(
            id = UUID.randomUUID().toString(),
            userId = uid,
            vehicleType = vehicleType,
            documents = documents,
            overallStatus = VerificationStatus.PENDING // Requires admin approval
        )
        
        // Save submission
        firestore.collection("verification_submissions")
            .document(submission.id)
            .set(submission)
            .await()
        
        // Determine userType based on vehicleType
        // TAXI drivers = DRIVER userType, BOAT drivers = CAPTAIN userType
        val userType = if (vehicleType == VehicleType.TAXI) UserType.DRIVER else UserType.CAPTAIN
        
        // Update user verification status to PENDING - admin will approve
        firestore.collection("users")
            .document(uid)
            .update(
                mapOf(
                    "verificationStatus" to VerificationStatus.PENDING,
                    "isVerified" to false,
                    "vehicleType" to vehicleType,
                    "userType" to userType
                )
            )
            .await()
        
        // Notify admin about new verification request
        notifyAdminNewVerification(submission.id, userName, vehicleType)
        
        submission
    }
    
    /**
     * Send push notification to admin about new verification request
     */
    private suspend fun notifyAdminNewVerification(
        submissionId: String,
        userName: String,
        vehicleType: VehicleType
    ) {
        try {
            // Call backend to send push notification to admin
            val url = "https://boattaxi-boattaxi.up.railway.app/api/notify-admin-verification"
            val client = java.net.HttpURLConnection::class.java
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            
            val json = """
                {
                    "submissionId": "$submissionId",
                    "userName": "$userName",
                    "vehicleType": "${vehicleType.name}"
                }
            """.trimIndent()
            
            connection.outputStream.bufferedWriter().use { it.write(json) }
            val responseCode = connection.responseCode
            android.util.Log.d("VerificationRepo", "Admin notification sent, response: $responseCode")
        } catch (e: Exception) {
            android.util.Log.e("VerificationRepo", "Failed to notify admin: ${e.message}")
            // Don't fail the submission if notification fails
        }
    }
    
    /**
     * Get all verification documents for current user
     */
    suspend fun getUserDocuments(): List<VerificationDocument> {
        val uid = userId ?: return emptyList()
        
        return firestore.collection("verification_documents")
            .whereEqualTo("userId", uid)
            .get()
            .await()
            .toObjects(VerificationDocument::class.java)
    }
    
    /**
     * Get the latest verification submission for current user
     */
    suspend fun getLatestSubmission(): VerificationSubmission? {
        val uid = userId ?: return null
        
        return firestore.collection("verification_submissions")
            .whereEqualTo("userId", uid)
            .get()
            .await()
            .toObjects(VerificationSubmission::class.java)
            .sortedByDescending { it.submittedAt }
            .firstOrNull()
    }
    
    /**
     * Observe verification status changes
     */
    fun observeVerificationStatus(): Flow<VerificationStatus> = callbackFlow {
        val uid = userId
        if (uid == null) {
            trySend(VerificationStatus.NONE)
            close()
            return@callbackFlow
        }
        
        val listener = firestore.collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val user = snapshot?.toObject(User::class.java)
                trySend(user?.verificationStatus ?: VerificationStatus.NONE)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Delete a verification document
     */
    suspend fun deleteDocument(documentId: String): Result<Unit> = runCatching {
        // Get document first to delete local file
        val doc = firestore.collection("verification_documents")
            .document(documentId)
            .get()
            .await()
            .toObject(VerificationDocument::class.java)
        
        // Delete local file if exists
        doc?.documentUrl?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                // Ignore deletion errors
            }
        }
        
        // Delete from Firestore
        firestore.collection("verification_documents")
            .document(documentId)
            .delete()
            .await()
    }
    
    /**
     * Get required documents for a vehicle type
     */
    fun getRequiredDocuments(vehicleType: VehicleType): List<DocumentType> {
        return VerificationRequirements.getRequirements(vehicleType)
    }
    
    /**
     * Check if all required documents are uploaded
     */
    suspend fun hasAllRequiredDocuments(vehicleType: VehicleType): Boolean {
        val uploadedDocs = getUserDocuments()
        val required = getRequiredDocuments(vehicleType)
        
        val uploadedTypes = uploadedDocs.map { it.documentType }.toSet()
        return required.all { it in uploadedTypes }
    }
    
    /**
     * Check if current user is verified local driver (for free subscription)
     */
    suspend fun isVerifiedLocalDriver(): Boolean {
        val uid = userId ?: return false
        return try {
            val userDoc = firestore.collection("users").document(uid).get().await()
            val verificationStatus = userDoc.getString("verificationStatus")
            val isLocalResident = userDoc.getBoolean("isLocalResident") ?: false
            val canBeDriver = userDoc.getBoolean("canBeDriver") ?: false
            val hasBoat = userDoc.getBoolean("hasBoat") ?: false
            val hasTaxi = userDoc.getBoolean("hasTaxi") ?: false
            
            // Must be verified, local, and a driver with boat or taxi
            verificationStatus == "approved" && isLocalResident && canBeDriver && (hasBoat || hasTaxi)
        } catch (e: Exception) {
            android.util.Log.e("VerificationRepo", "Error checking verified status: ${e.message}")
            false
        }
    }
    
    // ============ ADMIN FUNCTIONS ============
    
    /**
     * Get all pending verification submissions (Admin only)
     */
    suspend fun getPendingSubmissions(): List<VerificationSubmission> {
        return try {
            firestore.collection("verification_submissions")
                .whereEqualTo("status", "pending")
                .get()
                .await()
                .toObjects(VerificationSubmission::class.java)
                .sortedBy { it.submittedAt }
        } catch (e: Exception) {
            android.util.Log.e("VerificationRepo", "Error getting pending submissions: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Get all verification submissions (Admin only)
     */
    suspend fun getAllSubmissions(): List<VerificationSubmission> {
        return try {
            firestore.collection("verification_submissions")
                .get()
                .await()
                .toObjects(VerificationSubmission::class.java)
                .sortedByDescending { it.submittedAt }
        } catch (e: Exception) {
            android.util.Log.e("VerificationRepo", "Error getting all submissions: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Approve a verification submission (Admin only)
     * This grants the user verified local driver status = FREE subscription
     */
    suspend fun approveSubmission(submissionId: String, adminNotes: String? = null): Result<Unit> {
        return try {
            // Get submission
            val submissionDoc = firestore.collection("verification_submissions")
                .document(submissionId)
                .get()
                .await()
            
            val submission = submissionDoc.toObject(VerificationSubmission::class.java)
                ?: return Result.failure(Exception("Submission not found"))
            
            val adminId = userId ?: "admin"
            
            // Update submission status
            firestore.collection("verification_submissions")
                .document(submissionId)
                .update(
                    mapOf(
                        "status" to "approved",
                        "overallStatus" to VerificationStatus.APPROVED,
                        "reviewedBy" to adminId,
                        "reviewedAt" to Timestamp.now(),
                        "adminNotes" to adminNotes
                    )
                ).await()
            
            // Update user - mark as verified local driver
            firestore.collection("users")
                .document(submission.userId)
                .update(
                    mapOf(
                        "verificationStatus" to "approved",
                        "isVerified" to true,
                        "isLocalResident" to true,
                        "canBeDriver" to true,
                        "updatedAt" to Timestamp.now()
                    )
                ).await()
            
            android.util.Log.d("VerificationRepo", "Approved verification for user: ${submission.userId}")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("VerificationRepo", "Error approving submission: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Reject a verification submission (Admin only)
     */
    suspend fun rejectSubmission(submissionId: String, reason: String): Result<Unit> {
        return try {
            val submissionDoc = firestore.collection("verification_submissions")
                .document(submissionId)
                .get()
                .await()
            
            val submission = submissionDoc.toObject(VerificationSubmission::class.java)
                ?: return Result.failure(Exception("Submission not found"))
            
            val adminId = userId ?: "admin"
            
            // Update submission status
            firestore.collection("verification_submissions")
                .document(submissionId)
                .update(
                    mapOf(
                        "status" to "rejected",
                        "overallStatus" to VerificationStatus.REJECTED,
                        "reviewedBy" to adminId,
                        "reviewedAt" to Timestamp.now(),
                        "adminNotes" to reason
                    )
                ).await()
            
            // Update user
            firestore.collection("users")
                .document(submission.userId)
                .update(
                    mapOf(
                        "verificationStatus" to "rejected",
                        "isVerified" to false,
                        "updatedAt" to Timestamp.now()
                    )
                ).await()
            
            android.util.Log.d("VerificationRepo", "Rejected verification for user: ${submission.userId}")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("VerificationRepo", "Error rejecting submission: ${e.message}", e)
            Result.failure(e)
        }
    }
}
