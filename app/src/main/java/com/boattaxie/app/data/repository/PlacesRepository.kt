package com.boattaxie.app.data.repository

import android.content.Context
import com.boattaxie.app.BuildConfig
import com.boattaxie.app.data.model.BusynessLevel
import com.boattaxie.app.data.model.NearbyPlace
import com.boattaxie.app.data.model.PlaceCategory
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

data class PlaceResult(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String,
    val fullAddress: String
)

data class PlaceDetails(
    val placeId: String,
    val name: String,
    val address: String,
    val latLng: LatLng
)

// API Response models for Nearby Search
@Serializable
data class NearbySearchResponse(
    val results: List<NearbyPlaceResult> = emptyList(),
    val status: String = "",
    @SerialName("next_page_token") val nextPageToken: String? = null
)

@Serializable
data class NearbyPlaceResult(
    @SerialName("place_id") val placeId: String = "",
    val name: String = "",
    val vicinity: String? = null,
    @SerialName("formatted_address") val formattedAddress: String? = null,
    val geometry: PlaceGeometry? = null,
    val rating: Float? = null,
    @SerialName("user_ratings_total") val userRatingsTotal: Int? = null,
    @SerialName("price_level") val priceLevel: Int? = null,
    val types: List<String> = emptyList(),
    @SerialName("opening_hours") val openingHours: PlaceOpeningHours? = null,
    val photos: List<PlacePhoto>? = null,
    val icon: String? = null,
    @SerialName("business_status") val businessStatus: String? = null
)

@Serializable
data class PlaceGeometry(
    val location: PlaceLocation? = null
)

@Serializable
data class PlaceLocation(
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

@Serializable
data class PlaceOpeningHours(
    @SerialName("open_now") val openNow: Boolean? = null
)

@Serializable
data class PlacePhoto(
    @SerialName("photo_reference") val photoReference: String = "",
    val height: Int = 0,
    val width: Int = 0
)

// Place Details API Response
@Serializable
data class PlaceDetailsResponse(
    val result: PlaceDetailResult? = null,
    val status: String = ""
)

@Serializable
data class PlaceDetailResult(
    @SerialName("place_id") val placeId: String = "",
    val name: String = "",
    @SerialName("formatted_address") val formattedAddress: String? = null,
    @SerialName("formatted_phone_number") val phoneNumber: String? = null,
    val website: String? = null,
    @SerialName("opening_hours") val openingHours: PlaceDetailOpeningHours? = null,
    val rating: Float? = null,
    @SerialName("user_ratings_total") val userRatingsTotal: Int? = null,
    val reviews: List<PlaceReview>? = null,
    @SerialName("current_opening_hours") val currentOpeningHours: PlaceDetailOpeningHours? = null,
    val geometry: PlaceGeometry? = null,
    val photos: List<PlacePhoto>? = null,
    val types: List<String> = emptyList(),
    @SerialName("price_level") val priceLevel: Int? = null,
    @SerialName("business_status") val businessStatus: String? = null
)

@Serializable
data class PlaceDetailOpeningHours(
    @SerialName("open_now") val openNow: Boolean? = null,
    @SerialName("weekday_text") val weekdayText: List<String>? = null
)

@Serializable
data class PlaceReview(
    @SerialName("author_name") val authorName: String = "",
    val rating: Int = 0,
    @SerialName("relative_time_description") val relativeTime: String = "",
    val text: String = ""
)

// Autocomplete API response models
@Serializable
data class AutocompleteApiResponse(
    val status: String = "",
    val predictions: List<AutocompletePredictionResult> = emptyList()
)

@Serializable
data class AutocompletePredictionResult(
    @SerialName("place_id") val placeId: String = "",
    val description: String = "",
    @SerialName("structured_formatting") val structuredFormatting: StructuredFormatting? = null
)

@Serializable
data class StructuredFormatting(
    @SerialName("main_text") val mainText: String = "",
    @SerialName("secondary_text") val secondaryText: String? = null
)

// Location suggestion for UI
data class LocationSuggestion(
    val placeId: String,
    val name: String,
    val description: String,
    val latLng: LatLng? = null
)

@Singleton
class PlacesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val placesClient: PlacesClient by lazy {
        if (!Places.isInitialized()) {
            // Fallback initialization - should already be done in Application
            try {
                val apiKey = context.packageManager
                    .getApplicationInfo(context.packageName, android.content.pm.PackageManager.GET_META_DATA)
                    .metaData
                    ?.getString("com.google.android.geo.API_KEY") ?: ""
                Places.initialize(context, apiKey)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        Places.createClient(context)
    }
    
    private var isInitialized = false
    
    fun initialize(apiKey: String) {
        if (!Places.isInitialized()) {
            Places.initialize(context, apiKey)
        }
        isInitialized = true
    }
    
    /**
     * Search for places by query text
     * Biased towards Panama location - includes islands, restaurants, hotels, homes, businesses
     */
    suspend fun searchPlaces(
        query: String,
        currentLocation: LatLng? = null
    ): List<PlaceResult> {
        if (query.isBlank()) return emptyList()
        
        return try {
            val requestBuilder = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
            
            // Bias results to Panama area
            val panamaBounds = RectangularBounds.newInstance(
                LatLng(7.0, -83.0),  // Southwest corner
                LatLng(10.0, -77.0)  // Northeast corner
            )
            
            // If we have current location, use nearby bounds, otherwise use Panama-wide
            if (currentLocation != null) {
                val bounds = RectangularBounds.newInstance(
                    LatLng(currentLocation.latitude - 0.5, currentLocation.longitude - 0.5),
                    LatLng(currentLocation.latitude + 0.5, currentLocation.longitude + 0.5)
                )
                requestBuilder.setLocationBias(bounds)
            } else {
                requestBuilder.setLocationBias(panamaBounds)
            }
            
            android.util.Log.d("PlacesRepository", "Calling Places API for query: $query")
            val response = placesClient.findAutocompletePredictions(requestBuilder.build()).await()
            android.util.Log.d("PlacesRepository", "Got ${response.autocompletePredictions.size} predictions")
            response.autocompletePredictions.map { prediction ->
                PlaceResult(
                    placeId = prediction.placeId,
                    primaryText = prediction.getPrimaryText(null).toString(),
                    secondaryText = prediction.getSecondaryText(null).toString(),
                    fullAddress = prediction.getFullText(null).toString()
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("PlacesRepository", "Search failed: ${e.message}", e)
            android.util.Log.e("PlacesRepository", "Exception type: ${e.javaClass.simpleName}")
            emptyList()
        }
    }
    
    /**
     * Get place details including coordinates
     */
    suspend fun getPlaceDetails(placeId: String): PlaceDetails? {
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )
        
        val request = FetchPlaceRequest.builder(placeId, placeFields).build()
        
        return try {
            val response = placesClient.fetchPlace(request).await()
            val place = response.place
            
            PlaceDetails(
                placeId = place.id ?: placeId,
                name = place.name ?: "",
                address = place.address ?: "",
                latLng = place.latLng ?: LatLng(0.0, 0.0)
            )
        } catch (e: Exception) {
            null
        }
    }
    
    // HTTP client for Places API calls
    private val httpClient by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }
    
    private val apiKey: String by lazy {
        BuildConfig.MAPS_API_KEY
    }
    
    /**
     * Search for nearby places using Google Places API (Nearby Search)
     * Returns real-time data including ratings, open status, and busyness
     */
    suspend fun searchNearbyPlaces(
        location: LatLng,
        radiusMeters: Int = 5000,
        category: PlaceCategory = PlaceCategory.ALL,
        keyword: String? = null
    ): List<NearbyPlace> = withContext(Dispatchers.IO) {
        try {
            val baseUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
            
            // Use larger radius for emergency/medical services which are sparse in Bocas
            val effectiveRadius = when (category) {
                PlaceCategory.HOSPITALS, PlaceCategory.DOCTORS, PlaceCategory.EMERGENCY -> 50000 // 50km for medical/emergency
                else -> radiusMeters
            }
            
            android.util.Log.d("PlacesRepository", "Searching nearby places at ${location.latitude},${location.longitude}, radius: $effectiveRadius, category: $category")
            
            // For ALL category, search for popular place types
            val typeParams = when (category) {
                PlaceCategory.NONE -> emptyList() // Show nothing
                PlaceCategory.ALL -> listOf("restaurant", "tourist_attraction", "lodging", "bar", "park", "spa", "gym", "cafe", "store") // Multiple searches for more coverage
                PlaceCategory.DOCKS -> emptyList() // Handled separately - loads from Firestore
                PlaceCategory.ISLAND_DEALS -> emptyList() // Handled separately - shows ads, not places
                PlaceCategory.RESTAURANTS -> listOf("restaurant", "cafe", "bakery")
                PlaceCategory.BARS -> listOf("bar", "night_club")
                PlaceCategory.TOURS -> listOf("travel_agency", "tourist_attraction")
                PlaceCategory.BEACHES -> listOf("natural_feature")
                PlaceCategory.PARKS -> listOf("park", "campground")
                PlaceCategory.WELLNESS -> listOf("spa", "gym", "beauty_salon")
                PlaceCategory.HOTELS -> listOf("lodging")
                PlaceCategory.ATTRACTIONS -> listOf("tourist_attraction", "museum", "amusement_park")
                PlaceCategory.SHOPPING -> listOf("shopping_mall", "store")
                PlaceCategory.HOSPITALS -> listOf("hospital", "health")
                PlaceCategory.DOCTORS -> listOf("doctor", "dentist", "physiotherapist", "pharmacy", "health")
                PlaceCategory.EMERGENCY -> listOf("police", "fire_station", "local_government_office")
                PlaceCategory.NEWS -> emptyList() // Handled separately - shows news, not places
            }
            
            if (typeParams.isEmpty()) {
                return@withContext emptyList()
            }
            
            // Make search for each type and combine unique results
            val allResults = mutableListOf<NearbyPlace>()
            val seenPlaceIds = mutableSetOf<String>()
            
            for (type in typeParams.take(9)) { // Search up to 9 types for maximum coverage
                try {
                    val response: NearbySearchResponse = httpClient.get(baseUrl) {
                        parameter("location", "${location.latitude},${location.longitude}")
                        parameter("radius", effectiveRadius)
                        parameter("key", apiKey)
                        parameter("type", type)
                        keyword?.let { parameter("keyword", it) }
                    }.body()
                    
                    android.util.Log.d("PlacesRepository", "Search for type '$type': status=${response.status}, results=${response.results.size}")
                    
                    if (response.status == "OK") {
                        response.results.forEach { result ->
                            if (result.placeId !in seenPlaceIds) {
                                seenPlaceIds.add(result.placeId)
                                allResults.add(
                                    NearbyPlace(
                                        placeId = result.placeId,
                                        name = result.name,
                                        address = result.vicinity ?: "",
                                        latLng = LatLng(
                                            result.geometry?.location?.lat ?: 0.0,
                                            result.geometry?.location?.lng ?: 0.0
                                        ),
                                        rating = result.rating,
                                        userRatingsTotal = result.userRatingsTotal,
                                        priceLevel = result.priceLevel,
                                        types = result.types,
                                        isOpenNow = result.openingHours?.openNow,
                                        photoReference = result.photos?.firstOrNull()?.photoReference,
                                        iconUrl = result.icon,
                                        businessStatus = result.businessStatus,
                                        vicinity = result.vicinity,
                                        currentBusyness = null
                                    )
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PlacesRepository", "Error searching type $type: ${e.message}")
                }
            }
            
            // Additional keyword searches for emergency/medical in Spanish (for Panama)
            val keywordSearches = when (category) {
                PlaceCategory.EMERGENCY -> listOf("policia", "bomberos", "emergencia", "police station")
                PlaceCategory.DOCTORS -> listOf("clinica", "medico", "doctor", "dentista", "farmacia")
                PlaceCategory.HOSPITALS -> listOf("hospital", "clinica", "centro medico", "emergencias")
                else -> emptyList()
            }
            
            for (searchKeyword in keywordSearches) {
                try {
                    val textSearchUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json"
                    val response: NearbySearchResponse = httpClient.get(textSearchUrl) {
                        parameter("query", "$searchKeyword Bocas del Toro Panama")
                        parameter("key", apiKey)
                    }.body()
                    
                    android.util.Log.d("PlacesRepository", "Keyword search '$searchKeyword': status=${response.status}, results=${response.results.size}")
                    
                    if (response.status == "OK") {
                        response.results.forEach { result ->
                            if (result.placeId !in seenPlaceIds) {
                                seenPlaceIds.add(result.placeId)
                                allResults.add(
                                    NearbyPlace(
                                        placeId = result.placeId,
                                        name = result.name,
                                        address = result.formattedAddress ?: result.vicinity ?: "",
                                        latLng = LatLng(
                                            result.geometry?.location?.lat ?: 0.0,
                                            result.geometry?.location?.lng ?: 0.0
                                        ),
                                        rating = result.rating,
                                        userRatingsTotal = result.userRatingsTotal,
                                        priceLevel = result.priceLevel,
                                        types = result.types,
                                        isOpenNow = result.openingHours?.openNow,
                                        photoReference = result.photos?.firstOrNull()?.photoReference,
                                        iconUrl = result.icon,
                                        businessStatus = result.businessStatus,
                                        vicinity = result.vicinity,
                                        currentBusyness = null
                                    )
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PlacesRepository", "Keyword search error for '$searchKeyword': ${e.message}")
                }
            }
            
            android.util.Log.d("PlacesRepository", "Total unique places found: ${allResults.size}")
            
            // Sort by rating and return all results (no limit)
            allResults.sortedByDescending { it.rating ?: 0f }
        } catch (e: Exception) {
            android.util.Log.e("PlacesRepository", "Nearby search error: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Get detailed place information including opening hours and reviews
     */
    suspend fun getPlaceFullDetails(placeId: String): NearbyPlace? = withContext(Dispatchers.IO) {
        try {
            val baseUrl = "https://maps.googleapis.com/maps/api/place/details/json"
            
            val response: PlaceDetailsResponse = httpClient.get(baseUrl) {
                parameter("place_id", placeId)
                parameter("fields", "place_id,name,formatted_address,formatted_phone_number,website,opening_hours,rating,user_ratings_total,reviews,geometry,photos,types,price_level,business_status")
                parameter("key", apiKey)
            }.body()
            
            if (response.status != "OK") {
                android.util.Log.e("PlacesRepository", "Place details failed: ${response.status}")
                return@withContext null
            }
            
            val result = response.result ?: return@withContext null
            
            NearbyPlace(
                placeId = result.placeId,
                name = result.name,
                address = result.formattedAddress ?: "",
                latLng = LatLng(
                    result.geometry?.location?.lat ?: 0.0,
                    result.geometry?.location?.lng ?: 0.0
                ),
                rating = result.rating,
                userRatingsTotal = result.userRatingsTotal,
                priceLevel = result.priceLevel,
                types = result.types,
                isOpenNow = result.openingHours?.openNow ?: result.currentOpeningHours?.openNow,
                photoReference = result.photos?.firstOrNull()?.photoReference,
                businessStatus = result.businessStatus,
                phoneNumber = result.phoneNumber,
                website = result.website,
                openingHours = result.openingHours?.weekdayText ?: result.currentOpeningHours?.weekdayText
            )
        } catch (e: Exception) {
            android.util.Log.e("PlacesRepository", "Place details error: ${e.message}", e)
            null
        }
    }
    
    /**
     * Get photo URL for a place
     */
    fun getPhotoUrl(photoReference: String, maxWidth: Int = 400): String {
        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=$maxWidth&photo_reference=$photoReference&key=$apiKey"
    }
    
    /**
     * Text search for places (more flexible than nearby search)
     */
    suspend fun textSearchPlaces(
        query: String,
        location: LatLng? = null,
        radiusMeters: Int = 10000
    ): List<NearbyPlace> = withContext(Dispatchers.IO) {
        try {
            val baseUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json"
            
            // Add "Bocas del Toro" to query for local results
            val searchQuery = if (location == null) "$query Bocas del Toro Panama" else query
            
            val response: NearbySearchResponse = httpClient.get(baseUrl) {
                parameter("query", searchQuery)
                parameter("key", apiKey)
                location?.let {
                    parameter("location", "${it.latitude},${it.longitude}")
                    parameter("radius", radiusMeters)
                }
            }.body()
            
            if (response.status != "OK" && response.status != "ZERO_RESULTS") {
                android.util.Log.e("PlacesRepository", "Text search failed: ${response.status}")
                return@withContext emptyList()
            }
            
            response.results.map { result ->
                NearbyPlace(
                    placeId = result.placeId,
                    name = result.name,
                    address = result.vicinity ?: "",
                    latLng = LatLng(
                        result.geometry?.location?.lat ?: 0.0,
                        result.geometry?.location?.lng ?: 0.0
                    ),
                    rating = result.rating,
                    userRatingsTotal = result.userRatingsTotal,
                    priceLevel = result.priceLevel,
                    types = result.types,
                    isOpenNow = result.openingHours?.openNow,
                    photoReference = result.photos?.firstOrNull()?.photoReference,
                    iconUrl = result.icon,
                    businessStatus = result.businessStatus,
                    vicinity = result.vicinity
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("PlacesRepository", "Text search error: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Search for location suggestions (cities, countries, regions) using Places Autocomplete
     */
    suspend fun searchLocationSuggestions(query: String): List<LocationSuggestion> = withContext(Dispatchers.IO) {
        try {
            val baseUrl = "https://maps.googleapis.com/maps/api/place/autocomplete/json"
            
            val response: AutocompleteApiResponse = httpClient.get(baseUrl) {
                parameter("input", query)
                parameter("types", "(regions)") // Cities, countries, regions
                parameter("key", apiKey)
            }.body()
            
            if (response.status != "OK" && response.status != "ZERO_RESULTS") {
                return@withContext emptyList()
            }
            
            response.predictions.map { prediction ->
                LocationSuggestion(
                    placeId = prediction.placeId,
                    name = prediction.structuredFormatting?.mainText ?: prediction.description,
                    description = prediction.description
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("PlacesRepository", "Location autocomplete error: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Get the coordinates of a place by its placeId
     */
    suspend fun getPlaceLocation(placeId: String): LatLng? = withContext(Dispatchers.IO) {
        try {
            val baseUrl = "https://maps.googleapis.com/maps/api/place/details/json"
            
            val response: PlaceDetailsResponse = httpClient.get(baseUrl) {
                parameter("place_id", placeId)
                parameter("fields", "geometry")
                parameter("key", apiKey)
            }.body()
            
            if (response.status != "OK") {
                return@withContext null
            }
            
            response.result?.geometry?.location?.let {
                LatLng(it.lat, it.lng)
            }
        } catch (e: Exception) {
            android.util.Log.e("PlacesRepository", "Place details error: ${e.message}", e)
            null
        }
    }
    
    /**
     * Load dock locations from Firestore (same docks as booking map)
     */
    suspend fun loadDocksFromFirestore(): List<NearbyPlace> = withContext(Dispatchers.IO) {
        try {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("dock_locations").get().await()
            
            android.util.Log.d("PlacesRepository", "Loading docks from Firestore, found ${snapshot.documents.size} documents")
            
            snapshot.documents.mapNotNull { doc ->
                try {
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val lat = doc.getDouble("latitude") ?: return@mapNotNull null
                    val lng = doc.getDouble("longitude") ?: return@mapNotNull null
                    val description = doc.getString("description") ?: ""
                    val category = doc.getString("category") ?: "Docks"
                    
                    NearbyPlace(
                        placeId = doc.id,
                        name = name,
                        address = description,
                        latLng = LatLng(lat, lng),
                        types = listOf("dock", category.lowercase()),
                        vicinity = description
                    )
                } catch (e: Exception) {
                    android.util.Log.e("PlacesRepository", "Error parsing dock: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PlacesRepository", "Error loading docks from Firestore: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Calculate route between two points using Directions API
     * Returns distance in miles and duration in minutes
     */
    suspend fun getRoute(
        origin: LatLng,
        destination: LatLng
    ): RouteResult? {
        // For a full implementation, you would call the Directions API here
        // For now, calculate straight-line distance
        val distance = calculateDistance(origin, destination)
        val duration = (distance * 3).toInt() // Estimate 3 min per mile
        
        return RouteResult(
            distanceMiles = distance,
            durationMinutes = duration,
            polylinePoints = listOf(origin, destination)
        )
    }
    
    private fun calculateDistance(from: LatLng, to: LatLng): Double {
        val earthRadius = 3958.8 // miles
        
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLng = Math.toRadians(to.longitude - from.longitude)
        
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return earthRadius * c
    }
}

data class RouteResult(
    val distanceMiles: Double,
    val durationMinutes: Int,
    val polylinePoints: List<LatLng>
)
