package com.boattaxie.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * User model representing app users (riders and drivers/captains)
 */
data class User(
    @DocumentId
    val id: String = "",
    val email: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val profilePhotoUrl: String? = null,
    val userType: UserType = UserType.RIDER,
    val vehicleType: VehicleType? = null,  // Current active vehicle type for drivers
    // True if user signed up as driver (can switch between rider/driver modes)
    // False if user signed up as rider-only (no driver mode toggle)
    val canBeDriver: Boolean = false,
    // Which vehicles the driver has (for multi-vehicle drivers)
    val hasBoat: Boolean = false,
    val hasTaxi: Boolean = false,
    @get:PropertyName("verified") @set:PropertyName("verified")
    var isVerified: Boolean = false,
    val verificationStatus: VerificationStatus = VerificationStatus.NONE,
    val rating: Float = 5.0f,
    val totalTrips: Int = 0,
    @get:PropertyName("isOnline") @set:PropertyName("isOnline")
    var isOnline: Boolean = false,
    val currentLocation: GeoLocation? = null,
    val fcmToken: String? = null,
    // Local resident or foreign tourist - affects pricing
    val isLocalResident: Boolean = true,
    val residencyType: ResidencyType = ResidencyType.LOCAL,
    
    // Driver/Captain license info
    val licenseNumber: String? = null,      // Taxi or Boat license number
    val licenseType: String? = null,        // "Taxi License" or "Boat Captain License"
    val licenseExpiry: Timestamp? = null,   // License expiration date
    val vehiclePlate: String? = null,       // Vehicle plate number (taxi) or boat registration
    val vehicleModel: String? = null,       // e.g., "Toyota Corolla" or "Panga 25ft"
    val vehicleColor: String? = null,       // e.g., "Yellow" or "White/Blue"
    val vehiclePhoto: String? = null,       // Photo URL of vehicle/boat
    
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

enum class ResidencyType {
    @PropertyName("local")
    LOCAL,          // Panamanian resident - local pricing
    @PropertyName("tourist")
    TOURIST         // Foreign tourist - tourist pricing (slightly higher)
}

enum class UserType {
    @PropertyName("rider")
    RIDER,
    @PropertyName("captain")
    CAPTAIN,
    @PropertyName("driver")
    DRIVER
}

enum class VehicleType {
    @PropertyName("boat")
    BOAT,
    @PropertyName("taxi")
    TAXI
}

enum class VerificationStatus {
    @PropertyName("none")
    NONE,
    @PropertyName("pending")
    PENDING,
    @PropertyName("approved")
    APPROVED,
    @PropertyName("rejected")
    REJECTED
}

/**
 * Geographic location data - used for user location, ads, etc.
 * All fields have defaults for Firestore deserialization
 */
data class GeoLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String? = null,
    val timestamp: Long? = null  // For backward compatibility with existing data
) {
    // No-arg constructor for Firestore
    constructor() : this(0.0, 0.0, null, null)
}
