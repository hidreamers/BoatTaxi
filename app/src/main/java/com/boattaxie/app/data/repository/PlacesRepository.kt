package com.boattaxie.app.data.repository

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
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
