package com.boattaxie.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Verification document model for driver/captain verification
 */
data class VerificationDocument(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val vehicleType: VehicleType = VehicleType.BOAT,
    val documentType: DocumentType = DocumentType.VEHICLE_PHOTO,
    val documentUrl: String = "",
    val status: VerificationStatus = VerificationStatus.PENDING,
    val rejectionReason: String? = null,
    val reviewedBy: String? = null,
    val reviewedAt: Timestamp? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

enum class DocumentType {
    @PropertyName("vehicle_photo")
    VEHICLE_PHOTO,          // Photo of the boat or taxi
    
    @PropertyName("registration")
    REGISTRATION,           // Vehicle registration document
    
    @PropertyName("license")
    LICENSE,                // Driver's license or captain's license
    
    @PropertyName("insurance")
    INSURANCE,              // Insurance document
    
    @PropertyName("permit")
    PERMIT,                 // Operating permit
    
    @PropertyName("safety_certificate")
    SAFETY_CERTIFICATE,     // Safety inspection certificate
    
    @PropertyName("coast_guard_doc")
    COAST_GUARD_DOC,        // Coast Guard documentation (for boats)
    
    @PropertyName("taxi_medallion")
    TAXI_MEDALLION          // Taxi medallion/license
}

/**
 * Complete verification submission containing all documents
 * For local drivers/captains requesting free subscription access
 */
data class VerificationSubmission(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val phoneNumber: String? = null,
    val vehicleType: VehicleType = VehicleType.BOAT,
    val vehicleId: String? = null,
    val documents: List<VerificationDocument> = emptyList(),
    val documentUrls: List<String>? = null,  // Direct URLs for easy viewing
    val documentsCount: Int? = null,
    val overallStatus: VerificationStatus = VerificationStatus.PENDING,
    val status: String = "pending",  // String version for easier Firestore queries
    val hasBoat: Boolean = false,
    val hasTaxi: Boolean = false,
    val isLocalResident: Boolean = true,
    val notes: String? = null,
    val adminNotes: String? = null,
    val reviewedBy: String? = null,
    val reviewedAt: Timestamp? = null,
    val submittedAt: Timestamp = Timestamp.now(),
    val processedAt: Timestamp? = null
)

/**
 * Required documents based on vehicle type
 */
object VerificationRequirements {
    val boatRequirements = listOf(
        DocumentType.VEHICLE_PHOTO,
        DocumentType.REGISTRATION,
        DocumentType.LICENSE,
        DocumentType.INSURANCE
    )
    
    val taxiRequirements = listOf(
        DocumentType.VEHICLE_PHOTO,
        DocumentType.REGISTRATION,
        DocumentType.LICENSE,
        DocumentType.INSURANCE
    )
    
    fun getRequirements(vehicleType: VehicleType): List<DocumentType> {
        return when (vehicleType) {
            VehicleType.BOAT -> boatRequirements
            VehicleType.TAXI -> taxiRequirements
        }
    }
    
    fun getDocumentTitle(documentType: DocumentType): String {
        return when (documentType) {
            DocumentType.VEHICLE_PHOTO -> "Vehicle Photo"
            DocumentType.REGISTRATION -> "Registration Document"
            DocumentType.LICENSE -> "License/Permit"
            DocumentType.INSURANCE -> "Insurance Document"
            DocumentType.PERMIT -> "Operating Permit"
            DocumentType.SAFETY_CERTIFICATE -> "Safety Certificate"
            DocumentType.COAST_GUARD_DOC -> "Coast Guard Documentation"
            DocumentType.TAXI_MEDALLION -> "Taxi Medallion"
        }
    }
    
    fun getDocumentDescription(documentType: DocumentType, vehicleType: VehicleType): String {
        return when (documentType) {
            DocumentType.VEHICLE_PHOTO -> "Clear photo of your ${if (vehicleType == VehicleType.BOAT) "boat" else "taxi"}"
            DocumentType.REGISTRATION -> "Official registration document"
            DocumentType.LICENSE -> if (vehicleType == VehicleType.BOAT) "Captain's license" else "Driver's license"
            DocumentType.INSURANCE -> "Valid insurance document"
            DocumentType.PERMIT -> "Operating permit from local authority"
            DocumentType.SAFETY_CERTIFICATE -> "Safety inspection certificate"
            DocumentType.COAST_GUARD_DOC -> "Coast Guard documentation"
            DocumentType.TAXI_MEDALLION -> "Taxi medallion or operating license"
        }
    }
}
