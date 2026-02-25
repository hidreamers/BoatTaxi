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
        estimatedFare: Double,
        passengerCount: Int = 1,
        requestedDriverId: String? = null,
        riderName: String? = null,
        riderPhoneNumber: String? = null,
        riderPhotoUrl: String? = null
    ): Result<Booking> = runCatching {
        val uid = userId ?: throw Exception("User not logged in")
        
        // The estimatedFare is ALWAYS the actual fare - paid in cash to driver
        // Free bookings/subscriptions only affect app fees, not ride fares
        android.util.Log.d("BookingRepo", "createBooking: estimatedFare=$estimatedFare")
        
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
            passengerCount = passengerCount,
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
            "passengerCount" to passengerCount,
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
        
        // Get rider's residency status to show driver if local or visitor
        val currentUser = firestore.collection("users").document(uid).get().await()
        val isLocalResident = currentUser.getBoolean("isLocalResident") ?: true
        bookingMap["riderIsLocalResident"] = isLocalResident
        
        // Add specific driver if requested
        requestedDriverId?.let { bookingMap["driverId"] = it }
        
        android.util.Log.d("BookingRepo", "Creating booking: id=${booking.id}, vehicleType=${vehicleType.name.lowercase()}, fare=${booking.estimatedFare}")
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
     * Driver proposes a fare change to the rider
     * Rider will be notified and can accept or decline
     * Also includes driver info so rider knows who is offering
     */
    suspend fun proposeFareChange(bookingId: String, newFare: Double, reason: String): Result<Unit> = runCatching {
        val uid = userId ?: throw Exception("User not logged in")
        
        android.util.Log.d("BookingRepo", "proposeFareChange: bookingId=$bookingId, newFare=$newFare, reason=$reason")
        
        // Get driver details to include
        val driverDoc = firestore.collection("users")
            .document(uid)
            .get()
            .await()
        
        val driverName = driverDoc.getString("fullName") ?: "Driver"
        val driverPhone = driverDoc.getString("phoneNumber") ?: ""
        val driverPhoto = driverDoc.getString("profilePhotoUrl")
        val driverRating = driverDoc.getDouble("rating")?.toFloat() ?: 5.0f
        val driverTrips = driverDoc.getLong("totalTrips")?.toInt() ?: 0
        
        firestore.collection("bookings")
            .document(bookingId)
            .update(
                mapOf(
                    "driverAdjustedFare" to newFare,
                    "fareAdjustmentReason" to reason,
                    "riderAcceptedAdjustment" to false,
                    "fareProposedAt" to Timestamp.now(),
                    "fareProposedByDriverId" to uid,
                    // Include driver info so rider sees who is proposing
                    "driverId" to uid,
                    "driverName" to driverName,
                    "driverPhoneNumber" to driverPhone,
                    "driverPhotoUrl" to driverPhoto,
                    "driverRatingValue" to driverRating,
                    "driverTotalTrips" to driverTrips
                )
            )
            .await()
        
        android.util.Log.d("BookingRepo", "Fare change proposed successfully by $driverName")
    }
    
    /**
     * Driver submits a price offer for a ride request
     * Multiple drivers can submit offers, rider picks one
     */
    suspend fun submitDriverOffer(bookingId: String, price: Double, message: String? = null): Result<DriverOffer> = runCatching {
        val uid = userId ?: throw Exception("User not logged in")
        
        android.util.Log.d("BookingRepo", "submitDriverOffer: bookingId=$bookingId, price=$price")
        
        // Get driver details
        val driverDoc = firestore.collection("users")
            .document(uid)
            .get()
            .await()
        
        val driverName = driverDoc.getString("fullName") ?: "Driver"
        val driverPhone = driverDoc.getString("phoneNumber") ?: ""
        val driverPhoto = driverDoc.getString("profilePhotoUrl")
        val driverRating = driverDoc.getDouble("rating")?.toFloat() ?: 5.0f
        val driverTrips = driverDoc.getLong("totalTrips")?.toInt() ?: 0
        val vehiclePlate = driverDoc.getString("vehiclePlate")
        val vehicleModel = driverDoc.getString("vehicleModel")
        val vehicleColor = driverDoc.getString("vehicleColor")
        val vehiclePhoto = driverDoc.getString("vehiclePhoto")
        
        // Get booking to know vehicle type
        val bookingDoc = firestore.collection("bookings")
            .document(bookingId)
            .get()
            .await()
        val vehicleTypeStr = bookingDoc.getString("vehicleType") ?: "boat"
        val vehicleType = if (vehicleTypeStr == "taxi") VehicleType.TAXI else VehicleType.BOAT
        
        val offer = DriverOffer(
            id = UUID.randomUUID().toString(),
            driverId = uid,
            driverName = driverName,
            driverPhotoUrl = driverPhoto,
            driverRating = driverRating,
            driverTotalTrips = driverTrips,
            driverPhoneNumber = driverPhone,
            price = price,
            vehicleType = vehicleType,
            vehiclePlate = vehiclePlate,
            vehicleModel = vehicleModel,
            vehicleColor = vehicleColor,
            vehiclePhoto = vehiclePhoto,
            message = message,
            submittedAt = System.currentTimeMillis(),
            isAccepted = false
        )
        
        // Store offer in subcollection
        val offerMap = mapOf(
            "driverId" to offer.driverId,
            "driverName" to offer.driverName,
            "driverPhotoUrl" to offer.driverPhotoUrl,
            "driverRating" to offer.driverRating,
            "driverTotalTrips" to offer.driverTotalTrips,
            "driverPhoneNumber" to offer.driverPhoneNumber,
            "price" to offer.price,
            "vehicleType" to vehicleTypeStr,
            "vehiclePlate" to offer.vehiclePlate,
            "vehicleModel" to offer.vehicleModel,
            "vehicleColor" to offer.vehicleColor,
            "vehiclePhoto" to offer.vehiclePhoto,
            "message" to offer.message,
            "submittedAt" to offer.submittedAt,
            "isAccepted" to false
        )
        
        firestore.collection("bookings")
            .document(bookingId)
            .collection("offers")
            .document(offer.id)
            .set(offerMap)
            .await()
        
        android.util.Log.d("BookingRepo", "Driver offer submitted: $driverName for $$price")
        offer
    }
    
    /**
     * Check if driver already submitted an offer for this booking
     */
    suspend fun getDriverOfferForBooking(bookingId: String): DriverOffer? {
        val uid = userId ?: return null
        
        val offers = firestore.collection("bookings")
            .document(bookingId)
            .collection("offers")
            .whereEqualTo("driverId", uid)
            .get()
            .await()
        
        return offers.documents.firstOrNull()?.let { doc ->
            DriverOffer(
                id = doc.id,
                driverId = doc.getString("driverId") ?: "",
                driverName = doc.getString("driverName") ?: "",
                driverPhotoUrl = doc.getString("driverPhotoUrl"),
                driverRating = doc.getDouble("driverRating")?.toFloat() ?: 5.0f,
                driverTotalTrips = doc.getLong("driverTotalTrips")?.toInt() ?: 0,
                driverPhoneNumber = doc.getString("driverPhoneNumber"),
                price = doc.getDouble("price") ?: 0.0,
                message = doc.getString("message"),
                submittedAt = doc.getLong("submittedAt") ?: 0L,
                isAccepted = doc.getBoolean("isAccepted") ?: false,
                isRejected = doc.getBoolean("isRejected") ?: false,
                rejectedAt = doc.getLong("rejectedAt")
            )
        }
    }
    
    /**
     * Observe all driver offers for a booking (for rider to see)
     */
    fun observeDriverOffers(bookingId: String): Flow<List<DriverOffer>> = callbackFlow {
        val listener = firestore.collection("bookings")
            .document(bookingId)
            .collection("offers")
            .orderBy("price", Query.Direction.ASCENDING) // Cheapest first
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("BookingRepo", "Error observing offers: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val offers = snapshot?.documents?.map { doc ->
                    DriverOffer(
                        id = doc.id,
                        driverId = doc.getString("driverId") ?: "",
                        driverName = doc.getString("driverName") ?: "",
                        driverPhotoUrl = doc.getString("driverPhotoUrl"),
                        driverRating = doc.getDouble("driverRating")?.toFloat() ?: 5.0f,
                        driverTotalTrips = doc.getLong("driverTotalTrips")?.toInt() ?: 0,
                        driverPhoneNumber = doc.getString("driverPhoneNumber"),
                        price = doc.getDouble("price") ?: 0.0,
                        vehiclePlate = doc.getString("vehiclePlate"),
                        vehicleModel = doc.getString("vehicleModel"),
                        vehicleColor = doc.getString("vehicleColor"),
                        vehiclePhoto = doc.getString("vehiclePhoto"),
                        message = doc.getString("message"),
                        submittedAt = doc.getLong("submittedAt") ?: 0L,
                        isAccepted = doc.getBoolean("isAccepted") ?: false,
                        isRejected = doc.getBoolean("isRejected") ?: false,
                        rejectedAt = doc.getLong("rejectedAt")
                    )
                } ?: emptyList()
                
                android.util.Log.d("BookingRepo", "Received ${offers.size} driver offers")
                trySend(offers)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Rider accepts a driver's offer - this confirms the ride
     */
    suspend fun acceptDriverOffer(bookingId: String, offer: DriverOffer): Result<Unit> = runCatching {
        android.util.Log.d("BookingRepo", "acceptDriverOffer: bookingId=$bookingId, offerId=${offer.id}, price=${offer.price}")
        
        // Get driver's license info for the booking
        val driverDoc = firestore.collection("users")
            .document(offer.driverId)
            .get()
            .await()
        
        val licenseNumber = driverDoc.getString("licenseNumber")
        val licenseType = driverDoc.getString("licenseType")
        
        // Update booking with accepted offer details
        firestore.collection("bookings")
            .document(bookingId)
            .update(
                mapOf(
                    "status" to "accepted",
                    "driverId" to offer.driverId,
                    "driverName" to offer.driverName,
                    "driverPhoneNumber" to offer.driverPhoneNumber,
                    "driverPhotoUrl" to offer.driverPhotoUrl,
                    "driverRatingValue" to offer.driverRating,
                    "driverTotalTrips" to offer.driverTotalTrips,
                    "driverLicenseNumber" to licenseNumber,
                    "driverLicenseType" to licenseType,
                    "vehiclePlate" to offer.vehiclePlate,
                    "vehicleModel" to offer.vehicleModel,
                    "vehicleColor" to offer.vehicleColor,
                    "vehiclePhoto" to offer.vehiclePhoto,
                    "acceptedOfferId" to offer.id,
                    "acceptedPrice" to offer.price,
                    "finalFare" to offer.price,  // Set the fare from the offer
                    "acceptedAt" to Timestamp.now()
                )
            )
            .await()
        
        // Mark the offer as accepted
        firestore.collection("bookings")
            .document(bookingId)
            .collection("offers")
            .document(offer.id)
            .update("isAccepted", true)
            .await()
        
        android.util.Log.d("BookingRepo", "Offer accepted from ${offer.driverName} for $${offer.price}")
    }
    
    /**
     * Rider rejects a driver's offer - price too high
     * Driver will see this and can submit a new lower offer
     */
    suspend fun rejectDriverOffer(bookingId: String, offer: DriverOffer): Result<Unit> = runCatching {
        android.util.Log.d("BookingRepo", "rejectDriverOffer: bookingId=$bookingId, offerId=${offer.id}, price=${offer.price}")
        
        // Mark the offer as rejected
        firestore.collection("bookings")
            .document(bookingId)
            .collection("offers")
            .document(offer.id)
            .update(
                mapOf(
                    "isRejected" to true,
                    "rejectedAt" to System.currentTimeMillis()
                )
            )
            .await()
        
        android.util.Log.d("BookingRepo", "Offer rejected from ${offer.driverName} - $${offer.price} was too high")
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
     * Rate a completed trip and update driver's average rating
     */
    suspend fun rateTrip(
        bookingId: String,
        rating: Float,
        review: String? = null,
        isDriverRating: Boolean = false
    ): Result<Unit> = runCatching {
        // First, save the rating to the booking
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
        
        // Now update the driver's average rating
        if (!isDriverRating) {
            // Get the booking to find the driver ID
            val booking = firestore.collection("bookings")
                .document(bookingId)
                .get()
                .await()
                .toObject(Booking::class.java)
            
            booking?.driverId?.let { driverId ->
                // Get all completed bookings for this driver that have a rating
                val driverBookings = firestore.collection("bookings")
                    .whereEqualTo("driverId", driverId)
                    .whereEqualTo("status", "completed")
                    .get()
                    .await()
                    .toObjects(Booking::class.java)
                
                // Calculate average rating from bookings with ratings
                val ratingsWithValues = driverBookings.filter { it.rating != null && it.rating > 0 }
                if (ratingsWithValues.isNotEmpty()) {
                    val averageRating = ratingsWithValues.mapNotNull { it.rating }.average().toFloat()
                    val totalRatings = ratingsWithValues.size
                    
                    // Update driver's profile with new average rating and total trips
                    firestore.collection("users")
                        .document(driverId)
                        .update(
                            mapOf(
                                "rating" to averageRating,
                                "totalTrips" to driverBookings.size
                            )
                        )
                        .await()
                    
                    android.util.Log.d("BookingRepo", "Updated driver $driverId rating: $averageRating (from $totalRatings ratings)")
                }
            }
        }
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
     * Get the most recent completed booking that hasn't been rated yet (for showing rating popup)
     */
    suspend fun getUnratedCompletedBooking(): Booking? {
        val uid = userId ?: return null
        
        return firestore.collection("bookings")
            .whereEqualTo("riderId", uid)
            .whereEqualTo("status", "completed")
            .get()
            .await()
            .toObjects(Booking::class.java)
            .filter { it.rating == null || it.rating == 0f }
            .maxByOrNull { it.completedAt ?: it.requestedAt }
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
                    android.util.Log.d("BookingRepo", "  - ${b.id}: type=${b.vehicleType}, status=${b.status}, pickup=${b.pickupAddress}, FARE=${b.estimatedFare}")
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
     * This also changes status to ACCEPTED so the ride can start
     */
    suspend fun acceptFareAdjustment(bookingId: String): Result<Unit> = runCatching {
        android.util.Log.d("BookingRepo", "acceptFareAdjustment: bookingId=$bookingId - starting ride")
        
        firestore.collection("bookings")
            .document(bookingId)
            .update(
                mapOf(
                    "riderAcceptedAdjustment" to true,
                    "status" to "accepted",
                    "acceptedAt" to Timestamp.now()
                )
            )
            .await()
        
        android.util.Log.d("BookingRepo", "Fare accepted and ride started!")
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
