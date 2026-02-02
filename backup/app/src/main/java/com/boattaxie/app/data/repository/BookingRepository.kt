package com.boattaxie.app.data.repository

import com.boattaxie.app.data.model.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val userId: String?
        get() = auth.currentUser?.uid
    
    /**
     * Create a new booking request
     */
    suspend fun createBooking(
        vehicleType: VehicleType,
        pickupLocation: GeoLocation,
        pickupAddress: String,
        destinationLocation: GeoLocation,
        destinationAddress: String,
        estimatedDistance: Float,
        estimatedDuration: Int,
        requestedDriverId: String? = null,
        riderName: String? = null,
        riderPhoneNumber: String? = null,
        riderPhotoUrl: String? = null
    ): Result<Booking> = runCatching {
        val uid = userId ?: throw Exception("User not logged in")
        
        val estimatedFare = FareCalculator.calculateFare(
            vehicleType = vehicleType,
            distanceKm = estimatedDistance,
            durationMinutes = estimatedDuration
        )
        
        val booking = Booking(
            id = UUID.randomUUID().toString(),
            riderId = uid,
            driverId = requestedDriverId, // Set if requesting specific driver
            vehicleType = vehicleType,
            pickupLocation = pickupLocation,
            pickupAddress = pickupAddress,
            destinationLocation = destinationLocation,
            destinationAddress = destinationAddress,
            estimatedDistance = estimatedDistance,
            estimatedDuration = estimatedDuration,
            estimatedFare = estimatedFare,
            riderName = riderName,
            riderPhoneNumber = riderPhoneNumber,
            riderPhotoUrl = riderPhotoUrl,
            status = BookingStatus.PENDING
        )
        
        // Use explicit map to ensure lowercase values for Firestore queries
        // DO NOT include "id" - @DocumentId will read from document ID
        val bookingMap = mutableMapOf(
            "riderId" to booking.riderId,
            "vehicleType" to vehicleType.name.lowercase(), // "boat" or "taxi"
            "status" to "pending", // lowercase for queries
            "pickupLocation" to mapOf(
                "latitude" to pickupLocation.latitude,
                "longitude" to pickupLocation.longitude
            ),
            "pickupAddress" to booking.pickupAddress,
            "destinationLocation" to mapOf(
                "latitude" to destinationLocation.latitude,
                "longitude" to destinationLocation.longitude
            ),
            "destinationAddress" to booking.destinationAddress,
            "estimatedDistance" to booking.estimatedDistance,
            "estimatedDuration" to booking.estimatedDuration,
            "estimatedFare" to booking.estimatedFare,
            "paymentStatus" to "pending",
            "requestedAt" to com.google.firebase.Timestamp.now(),
            "isNightRate" to false,
            "route" to emptyList<Map<String, Double>>()
        )
        
        // Add rider info if available
        riderName?.let { bookingMap["riderName"] = it }
        riderPhoneNumber?.let { bookingMap["riderPhoneNumber"] = it }
        riderPhotoUrl?.let { bookingMap["riderPhotoUrl"] = it }
        
        // Add specific driver if requested
        requestedDriverId?.let { bookingMap["driverId"] = it }
        
        android.util.Log.d("BookingRepo", "Creating booking: id=${booking.id}, vehicleType=${vehicleType.name.lowercase()}, status=pending, requestedDriver=$requestedDriverId")
        android.util.Log.d("BookingRepo", "  pickup: ${booking.pickupAddress}, dropoff: ${booking.destinationAddress}")
        
        firestore.collection("bookings")
            .document(booking.id)
            .set(bookingMap)
            .await()
        
        android.util.Log.d("BookingRepo", "Booking created successfully!")
        
        booking
    }
    
    /**
     * Accept a booking (for drivers/captains)
     * Includes driver info so rider can see who is picking them up
     */
    suspend fun acceptBooking(bookingId: String): Result<Unit> = runCatching {
        val uid = userId ?: throw Exception("User not logged in")
        
        // Get driver details to include in booking
        val driverDoc = firestore.collection("users")
            .document(uid)
            .get()
            .await()
        
        val driverName = driverDoc.getString("fullName") ?: "Driver"
        val driverPhone = driverDoc.getString("phoneNumber") ?: ""
        val driverPhoto = driverDoc.getString("profilePhotoUrl")
        val driverRating = driverDoc.getDouble("rating")?.toFloat() ?: 5.0f
        val driverTrips = driverDoc.getLong("totalTrips")?.toInt() ?: 0
        val licenseNumber = driverDoc.getString("licenseNumber")
        val licenseType = driverDoc.getString("licenseType")
        val vehiclePlate = driverDoc.getString("vehiclePlate")
        val vehicleModel = driverDoc.getString("vehicleModel")
        val vehicleColor = driverDoc.getString("vehicleColor")
        val vehiclePhoto = driverDoc.getString("vehiclePhoto")
        
        firestore.collection("bookings")
            .document(bookingId)
            .update(
                mapOf(
                    "driverId" to uid,
                    "status" to "accepted", // Use lowercase string for Firestore queries
                    "acceptedAt" to Timestamp.now(),
                    // Driver info for rider to see
                    "driverName" to driverName,
                    "driverPhoneNumber" to driverPhone,
                    "driverPhotoUrl" to driverPhoto,
                    "driverRatingValue" to driverRating,
                    "driverTotalTrips" to driverTrips,
                    "driverLicenseNumber" to licenseNumber,
                    "driverLicenseType" to licenseType,
                    "vehiclePlate" to vehiclePlate,
                    "vehicleModel" to vehicleModel,
                    "vehicleColor" to vehicleColor,
                    "vehiclePhoto" to vehiclePhoto
                )
            )
            .await()
    }

    /**
     * Update booking status
     */
    suspend fun updateBookingStatus(
        bookingId: String,
        status: BookingStatus
    ): Result<Unit> = runCatching {
        // Convert enum to lowercase string for Firestore queries
        val statusString = when (status) {
            BookingStatus.PENDING -> "pending"
            BookingStatus.ACCEPTED -> "accepted"
            BookingStatus.ARRIVED -> "arrived"
            BookingStatus.IN_PROGRESS -> "in_progress"
            BookingStatus.COMPLETED -> "completed"
            BookingStatus.CANCELLED -> "cancelled"
            BookingStatus.NO_DRIVERS -> "no_drivers"
        }
        
        val updates = mutableMapOf<String, Any>(
            "status" to statusString
        )
        
        when (status) {
            BookingStatus.ARRIVED -> updates["arrivedAt"] = Timestamp.now()
            BookingStatus.IN_PROGRESS -> updates["startedAt"] = Timestamp.now()
            BookingStatus.COMPLETED -> updates["completedAt"] = Timestamp.now()
            BookingStatus.CANCELLED -> updates["cancelledAt"] = Timestamp.now()
            else -> {}
        }
        
        firestore.collection("bookings")
            .document(bookingId)
            .update(updates)
            .await()
    }
    
    /**
     * Cancel a booking
     */
    suspend fun cancelBooking(
        bookingId: String,
        reason: String? = null
    ): Result<Unit> = runCatching {
        val uid = userId ?: throw Exception("User not logged in")
        
        firestore.collection("bookings")
            .document(bookingId)
            .update(
                mapOf(
                    "status" to "cancelled", // Use lowercase string
                    "cancelledBy" to uid,
                    "cancellationReason" to reason,
                    "cancelledAt" to Timestamp.now()
                )
            )
            .await()
    }
    
    /**
     * Complete a booking with final fare
     */
    suspend fun completeBooking(
        bookingId: String,
        finalFare: Double
    ): Result<Unit> = runCatching {
        firestore.collection("bookings")
            .document(bookingId)
            .update(
                mapOf(
                    "status" to "completed", // Use lowercase string
                    "finalFare" to finalFare,
                    "completedAt" to Timestamp.now()
                )
            )
            .await()
    }
    
    /**
     * Rate a completed trip
     */
    suspend fun rateTrip(
        bookingId: String,
        rating: Float,
        review: String? = null,
        isDriverRating: Boolean = false
    ): Result<Unit> = runCatching {
        val updates = if (isDriverRating) {
            mapOf(
                "driverRating" to rating,
                "driverReview" to review
            )
        } else {
            mapOf(
                "rating" to rating,
                "review" to review
            )
        }
        
        firestore.collection("bookings")
            .document(bookingId)
            .update(updates)
            .await()
    }
    
    /**
     * Get a specific booking
     */
    suspend fun getBooking(bookingId: String): Booking? {
        return firestore.collection("bookings")
            .document(bookingId)
            .get()
            .await()
            .toObject(Booking::class.java)
    }
    
    /**
     * Observe a booking in real-time
     */
    fun observeBooking(bookingId: String): Flow<Booking?> = callbackFlow {
        val listener = firestore.collection("bookings")
            .document(bookingId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(Booking::class.java))
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get user's booking history
     */
    suspend fun getUserBookings(limit: Int = 20): List<Booking> {
        val uid = userId ?: return emptyList()
        
        return firestore.collection("bookings")
            .whereEqualTo("riderId", uid)
            .get()
            .await()
            .toObjects(Booking::class.java)
            .sortedByDescending { it.requestedAt }
            .take(limit)
    }
    
    /**
     * Get driver's booking history
     */
    suspend fun getDriverBookings(limit: Int = 20): List<Booking> {
        val uid = userId ?: return emptyList()
        
        return firestore.collection("bookings")
            .whereEqualTo("driverId", uid)
            .get()
            .await()
            .toObjects(Booking::class.java)
            .sortedByDescending { it.requestedAt }
            .take(limit)
    }
    
    /**
     * Get driver's active booking (ACCEPTED, ARRIVED, or IN_PROGRESS)
     */
    suspend fun getDriverActiveBooking(): Booking? {
        val uid = userId ?: return null
        
        // Use lowercase to match @PropertyName annotations
        val activeStatuses = listOf("accepted", "arrived", "in_progress")
        
        android.util.Log.d("BookingRepo", "getDriverActiveBooking: looking for driverId=$uid, statuses=$activeStatuses")
        
        val result = firestore.collection("bookings")
            .whereEqualTo("driverId", uid)
            .whereIn("status", activeStatuses)
            .get()
            .await()
        
        android.util.Log.d("BookingRepo", "getDriverActiveBooking: found ${result.documents.size} documents")
        result.documents.forEach { doc ->
            android.util.Log.d("BookingRepo", "  - ${doc.id}: driverId=${doc.getString("driverId")}, status=${doc.getString("status")}")
        }
        
        return result.toObjects(Booking::class.java).firstOrNull()
    }
    
    /**
     * Observe pending booking requests for drivers
     * This includes:
     * 1. General bookings (no specific driver requested) for the vehicle type
     * 2. Bookings specifically requested for this driver
     */
    fun observePendingBookings(vehicleType: VehicleType): Flow<List<Booking>> = callbackFlow {
        val currentDriverId = userId
        android.util.Log.d("BookingRepo", "Starting observePendingBookings for vehicleType=${vehicleType.name.lowercase()}, driverId=$currentDriverId")
        
        // Query for pending bookings of specific vehicle type
        // Note: We use lowercase values since that's what we save
        val listener = firestore.collection("bookings")
            .whereEqualTo("vehicleType", vehicleType.name.lowercase()) // "boat" or "taxi"
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("BookingRepo", "Error in observePendingBookings", error)
                    close(error)
                    return@addSnapshotListener
                }
                // Parse each document individually to skip bad ones
                val allBookings = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Booking::class.java)
                    } catch (e: Exception) {
                        android.util.Log.e("BookingRepo", "Error parsing booking ${doc.id}, deleting it", e)
                        // Delete the corrupted booking
                        firestore.collection("bookings").document(doc.id).delete()
                        null
                    }
                } ?: emptyList()
                
                // Filter bookings:
                // - Show if driverId is null (general request, any driver can take)
                // - Show if driverId matches current driver (specifically requested)
                // - Hide if driverId is set but doesn't match (requested someone else)
                val filteredBookings = allBookings.filter { booking ->
                    val requestedDriverId = booking.driverId
                    when {
                        requestedDriverId == null -> true // General request
                        requestedDriverId == currentDriverId -> true // Requested this driver
                        else -> false // Requested someone else
                    }
                }
                
                android.util.Log.d("BookingRepo", "observePendingBookings: ${allBookings.size} total, ${filteredBookings.size} for this driver")
                filteredBookings.forEach { b ->
                    android.util.Log.d("BookingRepo", "  - ${b.id}: vehicleType=${b.vehicleType}, driverId=${b.driverId}, ${b.pickupAddress} -> ${b.destinationAddress}")
                }
                trySend(filteredBookings)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Observe ALL pending booking requests (for showing live counts on driver map)
     */
    fun observeAllPendingBookings(): Flow<List<Booking>> = callbackFlow {
        android.util.Log.d("BookingRepo", "Starting observeAllPendingBookings")
        
        val listener = firestore.collection("bookings")
            .whereIn("status", listOf("pending", "PENDING")) // handle both cases
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("BookingRepo", "Error observing pending bookings", error)
                    close(error)
                    return@addSnapshotListener
                }
                // Parse each document individually to skip bad ones
                val bookings = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Booking::class.java)
                    } catch (e: Exception) {
                        android.util.Log.e("BookingRepo", "Error parsing booking ${doc.id}, deleting it", e)
                        // Delete the corrupted booking
                        firestore.collection("bookings").document(doc.id).delete()
                        null
                    }
                } ?: emptyList()
                android.util.Log.d("BookingRepo", "observeAllPendingBookings: ${bookings.size} bookings")
                bookings.forEach { b ->
                    android.util.Log.d("BookingRepo", "  - ${b.id}: type=${b.vehicleType}, status=${b.status}, pickup=${b.pickupAddress}")
                }
                trySend(bookings)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Clear ALL bookings from Firebase (for cleaning up test/fake data)
     * This deletes everything - use with caution!
     */
    suspend fun clearAllPendingBookings() {
        try {
            // Get ALL bookings (not just pending) to clean up any fake data
            val allBookings = firestore.collection("bookings")
                .get()
                .await()
            
            android.util.Log.d("BookingRepo", "Found ${allBookings.documents.size} total bookings to clear")
            
            // Delete each one
            allBookings.documents.forEach { doc ->
                val status = doc.getString("status") ?: "unknown"
                val pickup = doc.getString("pickupAddress") ?: "no address"
                android.util.Log.d("BookingRepo", "Deleting booking ${doc.id}: status=$status, pickup=$pickup")
                firestore.collection("bookings").document(doc.id).delete().await()
            }
            
            android.util.Log.d("BookingRepo", "Cleared ALL ${allBookings.documents.size} bookings from Firebase")
        } catch (e: Exception) {
            android.util.Log.e("BookingRepo", "Error clearing bookings", e)
        }
    }
    
    /**
     * Get active booking for rider
     */
    suspend fun getActiveRiderBooking(): Booking? {
        val uid = userId ?: return null
        
        // Check for both lowercase and uppercase status values to catch any format
        val statuses = listOf("pending", "accepted", "arrived", "in_progress", "PENDING", "ACCEPTED", "ARRIVED", "IN_PROGRESS")
        
        return firestore.collection("bookings")
            .whereEqualTo("riderId", uid)
            .whereIn("status", statuses)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toObject(Booking::class.java)
    }
    
    /**
     * Get active booking for driver
     */
    suspend fun getActiveDriverBooking(): Booking? {
        val uid = userId ?: return null
        
        return firestore.collection("bookings")
            .whereEqualTo("driverId", uid)
            .whereIn("status", listOf("accepted", "arrived", "in_progress"))
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toObject(Booking::class.java)
    }
    
    /**
     * Update driver's current location for a booking
     * This allows riders to see the driver moving on the map
     */
    suspend fun updateDriverLocation(
        bookingId: String,
        latitude: Double,
        longitude: Double
    ): Result<Unit> = runCatching {
        firestore.collection("bookings")
            .document(bookingId)
            .update(
                mapOf(
                    "driverLatitude" to latitude,
                    "driverLongitude" to longitude,
                    "driverLocationUpdatedAt" to Timestamp.now()
                )
            )
            .await()
    }
    
    /**
     * Observe driver location for a booking in real-time
     * Riders use this to see the driver moving on the map
     */
    fun observeDriverLocation(bookingId: String): Flow<com.google.android.gms.maps.model.LatLng?> = callbackFlow {
        val listener = firestore.collection("bookings")
            .document(bookingId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val lat = snapshot?.getDouble("driverLatitude")
                val lng = snapshot?.getDouble("driverLongitude")
                
                if (lat != null && lng != null) {
                    trySend(com.google.android.gms.maps.model.LatLng(lat, lng))
                } else {
                    trySend(null)
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Driver/Captain adjusts the fare (e.g., night rate, bad weather, holiday)
     * This notifies the rider who can accept or decline
     */
    suspend fun adjustFare(
        bookingId: String,
        adjustedFare: Double,
        reason: String,
        isNightRate: Boolean = false
    ): Result<Unit> = runCatching {
        firestore.collection("bookings")
            .document(bookingId)
            .update(
                mapOf(
                    "driverAdjustedFare" to adjustedFare,
                    "fareAdjustmentReason" to reason,
                    "isNightRate" to isNightRate,
                    "riderAcceptedAdjustment" to false
                )
            )
            .await()
    }
    
    /**
     * Rider accepts the driver's adjusted fare
     */
    suspend fun acceptFareAdjustment(bookingId: String): Result<Unit> = runCatching {
        firestore.collection("bookings")
            .document(bookingId)
            .update(
                mapOf(
                    "riderAcceptedAdjustment" to true
                )
            )
            .await()
    }
    
    /**
     * Check if current time is night rate hours (9PM - 6AM)
     */
    fun isNightRateTime(): Boolean {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        return hour >= 21 || hour < 6
    }
}
