package com.boattaxie.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Booking/Trip model for ride requests
 */
data class Booking(
    @DocumentId
    val id: String = "",
    val riderId: String = "",
    val driverId: String? = null,
    val vehicleType: VehicleType = VehicleType.BOAT,
    val vehicleId: String? = null,
    val status: BookingStatus = BookingStatus.PENDING,
    val pickupLocation: GeoLocation = GeoLocation(),
    val pickupAddress: String = "",
    val destinationLocation: GeoLocation = GeoLocation(),
    val destinationAddress: String = "",
    val estimatedDistance: Float = 0f, // in km or nautical miles
    val estimatedDuration: Int = 0, // in minutes
    val estimatedFare: Double = 0.0,
    val finalFare: Double? = null,
    
    // Rider info - so driver can see who is requesting
    val riderName: String? = null,
    val riderPhoneNumber: String? = null,
    val riderPhotoUrl: String? = null,
    
    // Driver/Captain info - populated when driver accepts
    val driverName: String? = null,
    val driverPhoneNumber: String? = null,
    val driverPhotoUrl: String? = null,
    val driverRatingValue: Float? = null,
    val driverTotalTrips: Int? = null,
    val driverLicenseNumber: String? = null,   // License # for verification
    val driverLicenseType: String? = null,     // "Taxi License" or "Boat Captain License"
    val vehiclePlate: String? = null,          // Plate or boat registration
    val vehicleModel: String? = null,          // Car model or boat type
    val vehicleColor: String? = null,          // Vehicle color
    val vehiclePhoto: String? = null,          // Photo of vehicle/boat
    
    // Driver fare adjustment - captain can adjust the fare
    val driverAdjustedFare: Double? = null,
    val fareAdjustmentReason: String? = null, // e.g., "Night rate", "Bad weather", "Holiday"
    val riderAcceptedAdjustment: Boolean = false,
    @get:PropertyName("nightRate") @set:PropertyName("nightRate")
    var isNightRate: Boolean = false, // Automatic night rate (9PM - 6AM)
    
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val paymentMethod: String? = null,
    val rating: Float? = null,
    val review: String? = null,
    val driverRating: Float? = null,
    val driverReview: String? = null,
    val cancelledBy: String? = null,
    val cancellationReason: String? = null,
    val route: List<GeoLocation> = emptyList(),
    val requestedAt: Timestamp = Timestamp.now(),
    val acceptedAt: Timestamp? = null,
    val arrivedAt: Timestamp? = null,
    val startedAt: Timestamp? = null,
    val completedAt: Timestamp? = null,
    val cancelledAt: Timestamp? = null
)

enum class BookingStatus {
    @PropertyName("pending")
    PENDING,           // Waiting for driver to accept
    
    @PropertyName("accepted")
    ACCEPTED,          // Driver accepted, en route to pickup
    
    @PropertyName("arrived")
    ARRIVED,           // Driver arrived at pickup location
    
    @PropertyName("in_progress")
    IN_PROGRESS,       // Trip started
    
    @PropertyName("completed")
    COMPLETED,         // Trip completed
    
    @PropertyName("cancelled")
    CANCELLED,         // Trip cancelled
    
    @PropertyName("no_drivers")
    NO_DRIVERS         // No drivers available
}

enum class PaymentStatus {
    @PropertyName("pending")
    PENDING,
    @PropertyName("processing")
    PROCESSING,
    @PropertyName("completed")
    COMPLETED,
    @PropertyName("failed")
    FAILED,
    @PropertyName("refunded")
    REFUNDED
}

/**
 * Fare calculation parameters
 */
data class FareConfig(
    val vehicleType: VehicleType,
    val baseFare: Double,
    val perKmRate: Double,
    val perMinuteRate: Double,
    val minimumFare: Double,
    val bookingFee: Double,
    val surgeMultiplier: Double = 1.0
)

/**
 * Fare Calculator based on Bocas del Toro, Panama pricing:
 * 
 * WATER TAXIS (Boats):
 * - Almirante to Bocas Town: ~$6-$8
 * - Bocas Town to Bastimentos: ~$3-$5/person
 * - Bocas Town to Carenero: ~$2-$3/person
 * - Bocas Town to Solarte (Bambuda): ~$20 for 1-2 people
 * - Island Hopping: ~$5-$10+/person
 * 
 * LAND TAXIS (Isla Colón):
 * - Within Bocas Town: $1-$2/person
 * - Bocas Town to Airport: ~$2/person
 * - Bocas Town to Boca del Drago: ~$2.50/person
 * 
 * TOURIST PRICING: Tourists may be charged ~20-30% more
 */
object FareCalculator {
    // Water Taxi (Boat) pricing - based on Bocas del Toro rates
    private val boatFareConfig = FareConfig(
        vehicleType = VehicleType.BOAT,
        baseFare = 3.00,      // Base fare for short trips
        perKmRate = 1.50,     // Per km rate
        perMinuteRate = 0.15, // Per minute (boats are slower)
        minimumFare = 3.00,   // Minimum like Carenero trip
        bookingFee = 0.50     // Small app fee
    )
    
    // Land Taxi pricing - based on Isla Colón rates
    private val taxiFareConfig = FareConfig(
        vehicleType = VehicleType.TAXI,
        baseFare = 1.00,      // Base fare $1
        perKmRate = 0.50,     // Per km rate
        perMinuteRate = 0.10, // Per minute
        minimumFare = 1.50,   // Minimum within Bocas Town
        bookingFee = 0.25     // Small app fee
    )
    
    // Common routes with fixed pricing (Bocas del Toro)
    private val fixedRoutes = mapOf(
        // Water taxi routes
        "almirante_bocas" to RoutePrice(VehicleType.BOAT, 6.00, 8.00),
        "bocas_bastimentos" to RoutePrice(VehicleType.BOAT, 3.00, 5.00),
        "bocas_carenero" to RoutePrice(VehicleType.BOAT, 2.00, 3.00),
        "bocas_solarte" to RoutePrice(VehicleType.BOAT, 8.00, 12.00),
        "bocas_zapatilla" to RoutePrice(VehicleType.BOAT, 8.00, 15.00),
        "bocas_redfrog" to RoutePrice(VehicleType.BOAT, 5.00, 8.00),
        
        // Land taxi routes (Isla Colón)
        "bocas_airport" to RoutePrice(VehicleType.TAXI, 2.00, 3.00),
        "bocas_drago" to RoutePrice(VehicleType.TAXI, 2.50, 4.00),
        "bocas_bluff" to RoutePrice(VehicleType.TAXI, 2.00, 3.00)
    )
    
    // Tourist markup (20% higher for tourists)
    private const val TOURIST_MARKUP = 1.20
    
    fun calculateFare(
        vehicleType: VehicleType,
        distanceKm: Float,
        durationMinutes: Int,
        surgeMultiplier: Double = 1.0,
        isTourist: Boolean = false
    ): Double {
        val config = when (vehicleType) {
            VehicleType.BOAT -> boatFareConfig
            VehicleType.TAXI -> taxiFareConfig
        }
        
        val distanceFare = distanceKm * config.perKmRate
        val timeFare = durationMinutes * config.perMinuteRate
        var subtotal = (config.baseFare + distanceFare + timeFare) * surgeMultiplier
        
        // Apply tourist markup if applicable
        if (isTourist) {
            subtotal *= TOURIST_MARKUP
        }
        
        val total = subtotal + config.bookingFee
        
        return maxOf(total, config.minimumFare)
    }
    
    /**
     * Get fare estimate with local and tourist prices
     */
    fun getFareEstimate(
        vehicleType: VehicleType,
        distanceKm: Float,
        durationMinutes: Int
    ): FareEstimate {
        val localFare = calculateFare(vehicleType, distanceKm, durationMinutes, isTourist = false)
        val touristFare = calculateFare(vehicleType, distanceKm, durationMinutes, isTourist = true)
        return FareEstimate(localFare, touristFare)
    }
    
    /**
     * Get minimum fare info for display
     */
    fun getMinimumFares(): Map<VehicleType, Double> = mapOf(
        VehicleType.BOAT to boatFareConfig.minimumFare,
        VehicleType.TAXI to taxiFareConfig.minimumFare
    )
}

data class RoutePrice(
    val vehicleType: VehicleType,
    val localPrice: Double,
    val touristPrice: Double
)

data class FareEstimate(
    val localPrice: Double,
    val touristPrice: Double
)
