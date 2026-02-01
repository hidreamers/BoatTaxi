package com.boattaxie.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Vehicle model for boats and taxis
 */
data class Vehicle(
    @DocumentId
    val id: String = "",
    val ownerId: String = "",
    val type: VehicleType = VehicleType.BOAT,
    val make: String = "",
    val model: String = "",
    val year: Int = 0,
    val color: String = "",
    val licensePlate: String = "",
    val registrationNumber: String = "",
    val capacity: Int = 4,
    val photoUrls: List<String> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

/**
 * Boat-specific details
 */
data class BoatDetails(
    val boatType: BoatType = BoatType.SPEEDBOAT,
    val length: Float = 0f, // in feet
    val hasLifeJackets: Boolean = true,
    val hasGps: Boolean = true,
    val hasSafetyEquipment: Boolean = true
)

enum class BoatType {
    @PropertyName("speedboat")
    SPEEDBOAT,
    @PropertyName("pontoon")
    PONTOON,
    @PropertyName("yacht")
    YACHT,
    @PropertyName("fishing_boat")
    FISHING_BOAT,
    @PropertyName("sailboat")
    SAILBOAT,
    @PropertyName("ferry")
    FERRY,
    @PropertyName("water_taxi")
    WATER_TAXI
}

/**
 * Taxi-specific details
 */
data class TaxiDetails(
    val taxiType: TaxiType = TaxiType.STANDARD,
    val isWheelchairAccessible: Boolean = false,
    val hasAirConditioning: Boolean = true,
    val hasWifi: Boolean = false
)

enum class TaxiType {
    @PropertyName("standard")
    STANDARD,
    @PropertyName("suv")
    SUV,
    @PropertyName("luxury")
    LUXURY,
    @PropertyName("van")
    VAN,
    @PropertyName("eco")
    ECO
}
