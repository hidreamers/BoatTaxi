package com.boattaxie.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Verification request for local residents who are also drivers/captains
 * to get free subscription access
 */
data class VerificationRequest(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val phoneNumber: String = "",
    
    // What they're verifying
    val isBoatCaptain: Boolean = false,
    val isTaxiDriver: Boolean = false,
    val isLocalResident: Boolean = false,
    
    // Proof documents (Firebase Storage URLs)
    val cedulaPhotoUrl: String? = null,           // Panama ID card photo
    val boatLicensePhotoUrl: String? = null,      // Boat captain license
    val taxiLicensePhotoUrl: String? = null,      // Taxi driver license
    val selfieWithIdPhotoUrl: String? = null,     // Selfie holding ID for verification
    
    // Additional info
    val boatRegistrationNumber: String? = null,
    val taxiPlateNumber: String? = null,
    val notes: String? = null,                    // User can add notes
    
    // Status
    val status: VerificationRequestStatus = VerificationRequestStatus.PENDING,
    val adminNotes: String? = null,               // Admin can add rejection reason
    val reviewedBy: String? = null,               // Admin who reviewed
    val reviewedAt: Timestamp? = null,
    
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

enum class VerificationRequestStatus {
    @PropertyName("pending")
    PENDING,
    @PropertyName("under_review")
    UNDER_REVIEW,
    @PropertyName("approved")
    APPROVED,
    @PropertyName("rejected")
    REJECTED,
    @PropertyName("more_info_needed")
    MORE_INFO_NEEDED
}
