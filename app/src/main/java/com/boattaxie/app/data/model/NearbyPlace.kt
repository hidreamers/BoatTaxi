package com.boattaxie.app.data.model

import com.google.android.gms.maps.model.LatLng

/**
 * Represents a nearby place from Google Places API
 */
data class NearbyPlace(
    val placeId: String,
    val name: String,
    val address: String,
    val latLng: LatLng,
    val rating: Float? = null,
    val userRatingsTotal: Int? = null,
    val priceLevel: Int? = null, // 0-4 (Free to Very Expensive)
    val types: List<String> = emptyList(),
    val isOpenNow: Boolean? = null,
    val photoReference: String? = null,
    val iconUrl: String? = null,
    val businessStatus: String? = null, // OPERATIONAL, CLOSED_TEMPORARILY, CLOSED_PERMANENTLY
    val vicinity: String? = null,
    // Real-time data from Places API (New)
    val currentBusyness: BusynessLevel? = null,
    val phoneNumber: String? = null,
    val website: String? = null,
    val openingHours: List<String>? = null
)

/**
 * Busyness level for real-time crowd data
 */
enum class BusynessLevel {
    NOT_BUSY,      // 0-25% capacity
    SOMEWHAT_BUSY, // 25-50% capacity
    BUSY,          // 50-75% capacity
    VERY_BUSY;     // 75-100% capacity
    
    companion object {
        fun fromPercentage(percentage: Int): BusynessLevel {
            return when {
                percentage < 25 -> NOT_BUSY
                percentage < 50 -> SOMEWHAT_BUSY
                percentage < 75 -> BUSY
                else -> VERY_BUSY
            }
        }
    }
}

/**
 * Category filters for exploring places
 */
enum class PlaceCategory(
    val displayName: String,
    val icon: String,
    val placeTypes: List<String>
) {
    NONE("None", "🚫", emptyList()),
    ALL("All", "🗺️", emptyList()),
    DOCKS("Docks", "⚓", listOf("dock", "marina", "ferry", "port")),
    ISLAND_DEALS("Island Deals", "🎟️", emptyList()),
    RESTAURANTS("Food", "🍽️", listOf("restaurant", "food", "cafe", "bakery")),
    BARS("Nightlife", "🍸", listOf("bar", "night_club", "casino")),
    TOURS("Tours", "🚤", listOf("travel_agency", "tourist_attraction")),
    BEACHES("Beaches", "🏖️", listOf("natural_feature")),
    PARKS("Parks", "🌴", listOf("park", "campground", "natural_feature")),
    WELLNESS("Wellness", "💆", listOf("spa", "gym", "beauty_salon", "hair_care")),
    HOTELS("Hotels", "🏨", listOf("lodging", "hotel", "resort")),
    ATTRACTIONS("Attractions", "🎢", listOf("amusement_park", "aquarium", "museum", "zoo")),
    SHOPPING("Shopping", "🛍️", listOf("shopping_mall", "store", "market")),
    HOSPITALS("Hospitals", "🏥", listOf("hospital", "health")),
    DOCTORS("Doctors", "👨‍⚕️", listOf("doctor", "dentist", "physiotherapist", "health")),
    EMERGENCY("Emergency", "🚨", listOf("police", "fire_station", "local_government_office")),
    NEWS("News & Weather", "📰", emptyList());
    
    companion object {
        fun fromPlaceTypes(types: List<String>): PlaceCategory {
            for (category in values()) {
                if (category == ALL) continue
                if (types.any { it in category.placeTypes }) {
                    return category
                }
            }
            return ALL
        }
    }
}

/**
 * Time of day filter
 */
enum class TimeFilter(val displayName: String) {
    ALL("All Day"),
    DAY("Daytime"),
    NIGHT("Nightlife")
}
