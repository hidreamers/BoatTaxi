package com.boattaxie.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Booking/Trip model for ride requests
 * 
 * NEW FLOW (Driver-set pricing):
 * 1. Rider creates booking (no price set) → status=PENDING
 * 2. All drivers see request and can submit price offers
 * 3. Rider sees list of driver offers with prices
 * 4. Rider picks an offer → status=ACCEPTED, driver/price set
 * 5. Ride proceeds normally
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
    val estimatedFare: Double = 0.0, // App's estimate (for reference only)
    val finalFare: Double? = null,   // Actual price from accepted driver offer
    val passengerCount: Int = 1,     // Number of passengers for the ride
    
    // Rider info - so driver can see who is requesting
    val riderName: String? = null,
    val riderPhoneNumber: String? = null,
    val riderPhotoUrl: String? = null,
    val riderIsLocalResident: Boolean = true, // Is rider a local or visitor?
    
    // Driver/Captain info - populated when rider accepts an offer
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
    
    // Driver offers - multiple drivers can submit price offers
    // Stored in subcollection "offers" for real-time updates
    val acceptedOfferId: String? = null,       // ID of the offer rider accepted
    val acceptedPrice: Double? = null,         // Price from accepted offer
    
    // Legacy fare adjustment fields (keeping for backwards compatibility)
    val driverAdjustedFare: Double? = null,
    val fareAdjustmentReason: String? = null,
    val riderAcceptedAdjustment: Boolean = false,
    @get:PropertyName("nightRate") @set:PropertyName("nightRate")
    var isNightRate: Boolean = false,
    
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
    PENDING,           // Waiting for driver offers (no driver assigned yet)
    
    @PropertyName("accepted")
    ACCEPTED,          // Rider accepted an offer, driver en route to pickup
    
    @PropertyName("arrived")
    ARRIVED,           // Driver arrived at pickup location
    
    @PropertyName("in_progress")
    IN_PROGRESS,       // Trip started
    
    @PropertyName("completed")
    COMPLETED,         // Trip completed
    
    @PropertyName("cancelled")
    CANCELLED,         // Trip cancelled
    
    @PropertyName("no_drivers")
    NO_DRIVERS         // No drivers available/no offers received
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
 * Driver offer for a ride - multiple drivers can submit offers with their prices
 * Rider sees all offers and picks the one they want
 */
data class DriverOffer(
    val id: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val driverPhotoUrl: String? = null,
    val driverRating: Float = 5.0f,
    val driverTotalTrips: Int = 0,
    val driverPhoneNumber: String? = null,
    val price: Double = 0.0,
    val vehicleType: VehicleType = VehicleType.BOAT,
    val vehiclePlate: String? = null,
    val vehicleModel: String? = null,
    val vehicleColor: String? = null,
    val vehiclePhoto: String? = null,
    val message: String? = null,  // Optional message from driver
    val submittedAt: Long = System.currentTimeMillis(),
    val isAccepted: Boolean = false,
    val isRejected: Boolean = false,  // Rider rejected this price
    val rejectedAt: Long? = null  // When the offer was rejected
)

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
 * WATER TAXIS (Boats) - Based on Feb 2026 local prices:
 * - Bocas Town to Carenero: $1-2 (very short, ~0.5km)
 * - Bocas Town to Bastimentos (Old Bank): $3-5 (~2km)
 * - Bocas Town to Red Frog: $5-8 (~4km)
 * - Bocas Town to Solarte/Bambuda: $8-12 (~5km)
 * - Bocas Town to Zapatillas: $40-50 private (~15-20km, far)
 * - Almirante to Bocas Town: $5-6 (~10km, 30 min)
 * - Island Hopping Day Tour: $25-30 (6+ hours, multiple stops)
 * 
 * Key insight: Short trips ($1-5) are cheap, but longer private
 * water taxi trips scale up significantly due to fuel costs
 * 
 * LAND TAXIS (Isla Colón) - Updated Feb 2026:
 * - Bocas Town short hop: $0.60-$2.00 (~0.8 mi)
 * - Bocas Town ↔ Airport: $1.00-$2.00 (~0.9 mi)
 * - Bocas Town → Paunch: $3.00-$8.00 (~2.9 mi)
 * - Bocas Town → Bluff Beach: $15.00-$20.00 (~4.2 mi)
 * - Bocas Town → Boca del Drago: $26.00-$31.00 (~12.2 mi)
 */
object FareCalculator {
    // Water Taxi (Boat) pricing - based on Bocas del Toro rates
    // Short trips cheap (Carenero $2-3), longer trips expensive (Zapatilla $15+)
    // Fuel costs add up significantly on water for longer distances
    private val boatFareConfig = FareConfig(
        vehicleType = VehicleType.BOAT,
        baseFare = 2.00,      // Base fare (Carenero-level short trip)
        perKmRate = 3.50,     // Higher per km - fuel expensive on water
        perMinuteRate = 0.15, // Per minute (boats are slower)
        minimumFare = 2.50,   // Minimum like Carenero trip
        bookingFee = 0.50     // Small app fee
    )
    
    // Land Taxi pricing - based on Isla Colón rates (Feb 2026)
    // In town: cheap ($0.60-$2), Bluff Beach: $15 (bumpy road, ~6.8km)
    // Boca del Drago: $26-31 (~12km to far end of island)
    private val taxiFareConfig = FareConfig(
        vehicleType = VehicleType.TAXI,
        baseFare = 0.60,      // Base fare (short hop minimum)
        perKmRate = 2.10,     // Higher rate - rough roads, fuel cost
        perMinuteRate = 0.05, // Per minute (short rides)
        minimumFare = 1.00,   // Minimum within Bocas Town
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
        
        // Land taxi routes (Isla Colón) - Updated Feb 2026
        "bocas_short" to RoutePrice(VehicleType.TAXI, 0.60, 2.00),      // Short hop in town
        "bocas_airport" to RoutePrice(VehicleType.TAXI, 1.00, 2.00),    // ~0.9 mi
        "bocas_paunch" to RoutePrice(VehicleType.TAXI, 3.00, 8.00),     // ~2.9 mi
        "bocas_bluff" to RoutePrice(VehicleType.TAXI, 15.00, 20.00),    // ~4.2 mi
        "bocas_drago" to RoutePrice(VehicleType.TAXI, 26.00, 31.00)     // ~12.2 mi
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
