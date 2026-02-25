package com.boattaxie.app.ui.screens.explore

import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boattaxie.app.data.model.Advertisement
import com.boattaxie.app.data.model.NearbyPlace
import com.boattaxie.app.data.model.PlaceCategory
import com.boattaxie.app.data.model.TimeFilter
import com.boattaxie.app.data.model.NewsArticle
import com.boattaxie.app.data.model.WeatherData
import com.boattaxie.app.data.model.WeatherForecast
import com.boattaxie.app.data.repository.AdvertisementRepository
import com.boattaxie.app.data.repository.LocationSuggestion
import com.boattaxie.app.data.repository.PlacesRepository
import com.boattaxie.app.data.repository.NewsRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.*

/**
 * Nearby discovery item - pops up when user gets close
 */
sealed class NearbyDiscovery {
    data class PlaceDiscovery(
        val place: NearbyPlace,
        val distanceMeters: Int
    ) : NearbyDiscovery()
    
    data class DealDiscovery(
        val ad: Advertisement,
        val distanceMeters: Int
    ) : NearbyDiscovery()
}

data class ExploreUiState(
    val isLoading: Boolean = false,
    val nearbyPlaces: List<NearbyPlace> = emptyList(),
    val advertisements: List<Advertisement> = emptyList(),
    val selectedCategory: PlaceCategory = PlaceCategory.ALL,
    val selectedTimeFilter: TimeFilter = TimeFilter.ALL,
    val selectedPlace: NearbyPlace? = null,
    val selectedAd: Advertisement? = null,
    val showPlaceDetails: Boolean = false,
    val showAdDetails: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<NearbyPlace> = emptyList(),
    val isSearching: Boolean = false,
    val currentLocation: LatLng = LatLng(9.3403, -82.2419), // Bocas del Toro default
    val errorMessage: String? = null,
    val lastRefreshTime: Long = 0,
    // Real-time discovery
    val nearbyDiscovery: NearbyDiscovery? = null,
    val isDiscoveryEnabled: Boolean = true,
    // Search radius in km
    val searchRadiusKm: Float = 5f,
    val useKilometers: Boolean = true, // true = km, false = miles
    // Target location for searching (different from current location)
    val targetLocation: LatLng? = null, // null means use currentLocation
    val targetLocationName: String? = null, // "Paris, France", "New York, USA" etc.
    val locationSearchQuery: String = "",
    val locationSearchResults: List<LocationSuggestion> = emptyList(),
    val isSearchingLocation: Boolean = false,
    // Auto-show places on map - loads places when map camera moves
    val autoShowPlacesOnMap: Boolean = true,
    val lastMapCameraPosition: LatLng? = null,
    // News and Weather
    val newsArticles: List<NewsArticle> = emptyList(),
    val weather: WeatherData? = null,
    val weatherForecast: List<WeatherForecast> = emptyList(),
    val isLoadingNews: Boolean = false
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val placesRepository: PlacesRepository,
    private val advertisementRepository: AdvertisementRepository,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val newsRepository: NewsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()
    
    private var searchJob: Job? = null
    
    // Track already-shown discoveries to avoid repeating
    private val shownDiscoveries = mutableSetOf<String>()
    
    // Discovery radius in meters - 45m = ~150 feet
    private val discoveryRadiusMeters = 45
    
    // Location tracking callback
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                val latLng = LatLng(location.latitude, location.longitude)
                _uiState.update { it.copy(currentLocation = latLng) }
                checkNearbyDiscoveries(latLng)
            }
        }
    }
    
    // Bocas del Toro area coordinates
    private val bocasDelToro = LatLng(9.3403, -82.2419)
    
    init {
        getCurrentLocation()
        loadNearbyPlaces()
        loadAdvertisements()
        // Start location tracking for live updates (discovery popup is disabled separately)
        startLocationTracking()
        loadNewsAndWeather()
    }
    
    /**
     * Start continuous location tracking for real-time discovery
     */
    private fun startLocationTracking() {
        try {
            // Fast updates for real-time live mode - updates every 2 seconds when moving
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
                .setMinUpdateIntervalMillis(1000L)
                .setMinUpdateDistanceMeters(5f) // Only update if moved at least 5 meters
                .build()
            
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            android.util.Log.e("ExploreVM", "Location permission denied for tracking", e)
        }
    }
    
    /**
     * Check for nearby places/deals and show discovery popup
     * Updates in real-time to show the closest item as you move
     */
    private fun checkNearbyDiscoveries(userLocation: LatLng) {
        if (!_uiState.value.isDiscoveryEnabled) return
        
        // Find the closest deal within radius
        var closestDeal: Advertisement? = null
        var closestDealDistance = Double.MAX_VALUE
        
        for (ad in _uiState.value.advertisements) {
            ad.location?.let { loc ->
                val distance = calculateDistanceMeters(
                    userLocation.latitude, userLocation.longitude,
                    loc.latitude, loc.longitude
                )
                
                if (distance <= discoveryRadiusMeters && distance < closestDealDistance) {
                    closestDeal = ad
                    closestDealDistance = distance
                }
            }
        }
        
        // If there's a close deal, show it (priority for advertisers)
        if (closestDeal != null) {
            val currentDiscovery = _uiState.value.nearbyDiscovery
            // Only update if it's a different deal or distance changed significantly
            if (currentDiscovery !is NearbyDiscovery.DealDiscovery || 
                currentDiscovery.ad.id != closestDeal!!.id ||
                kotlin.math.abs(currentDiscovery.distanceMeters - closestDealDistance.toInt()) > 10) {
                _uiState.update {
                    it.copy(nearbyDiscovery = NearbyDiscovery.DealDiscovery(closestDeal!!, closestDealDistance.toInt()))
                }
            }
            return
        }
        
        // Find the closest place within radius
        var closestPlace: NearbyPlace? = null
        var closestPlaceDistance = Double.MAX_VALUE
        
        for (place in _uiState.value.nearbyPlaces) {
            val distance = calculateDistanceMeters(
                userLocation.latitude, userLocation.longitude,
                place.latLng.latitude, place.latLng.longitude
            )
            
            if (distance <= discoveryRadiusMeters && distance < closestPlaceDistance) {
                closestPlace = place
                closestPlaceDistance = distance
            }
        }
        
        // Show the closest place
        if (closestPlace != null) {
            val currentDiscovery = _uiState.value.nearbyDiscovery
            // Only update if it's a different place or distance changed significantly
            if (currentDiscovery !is NearbyDiscovery.PlaceDiscovery || 
                currentDiscovery.place.placeId != closestPlace.placeId ||
                kotlin.math.abs(currentDiscovery.distanceMeters - closestPlaceDistance.toInt()) > 10) {
                _uiState.update {
                    it.copy(nearbyDiscovery = NearbyDiscovery.PlaceDiscovery(closestPlace, closestPlaceDistance.toInt()))
                }
            }
        } else {
            // No places within radius, clear the discovery popup
            if (_uiState.value.nearbyDiscovery != null) {
                _uiState.update { it.copy(nearbyDiscovery = null) }
            }
        }
    }
    
    /**
     * Calculate distance in meters using Haversine formula
     */
    private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusMeters = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + 
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusMeters * c
    }
    
    /**
     * Dismiss the current discovery popup
     */
    fun dismissDiscovery() {
        _uiState.update { it.copy(nearbyDiscovery = null) }
    }
    
    /**
     * Toggle real-time discovery on/off
     */
    fun toggleDiscovery(enabled: Boolean) {
        _uiState.update { it.copy(isDiscoveryEnabled = enabled) }
        if (!enabled) {
            _uiState.update { it.copy(nearbyDiscovery = null) }
        }
    }
    
    /**
     * Set search radius
     */
    fun setSearchRadius(radiusKm: Float) {
        _uiState.update { it.copy(searchRadiusKm = radiusKm) }
        loadNearbyPlaces(forceRefresh = true)
    }
    
    /**
     * Toggle between kilometers and miles
     */
    fun toggleUnits() {
        _uiState.update { it.copy(useKilometers = !it.useKilometers) }
    }
    
    /**
     * Toggle auto-show places on map
     */
    fun toggleAutoShowPlaces(enabled: Boolean) {
        _uiState.update { it.copy(autoShowPlacesOnMap = enabled) }
    }
    
    /**
     * Load places at the map's current camera center position
     * Called when user moves the map and camera stops
     */
    fun loadPlacesAtMapCenter(centerLatLng: LatLng, zoomLevel: Float) {
        // Only load if auto-show is enabled
        if (!_uiState.value.autoShowPlacesOnMap) return
        
        // Check if we've moved significantly from last position (at least 500m)
        val lastPos = _uiState.value.lastMapCameraPosition
        if (lastPos != null) {
            val distance = calculateDistanceMeters(
                lastPos.latitude, lastPos.longitude,
                centerLatLng.latitude, centerLatLng.longitude
            )
            if (distance < 500) return // Don't reload if moved less than 500m
        }
        
        _uiState.update { it.copy(lastMapCameraPosition = centerLatLng) }
        
        // Calculate radius based on zoom level
        // Higher zoom = smaller area, lower zoom = larger area
        val radiusKm = when {
            zoomLevel >= 18 -> 0.5f   // Very close zoom - 500m radius
            zoomLevel >= 16 -> 1f     // Close zoom - 1km radius
            zoomLevel >= 14 -> 2f     // Medium zoom - 2km radius
            zoomLevel >= 12 -> 5f     // Wide zoom - 5km radius
            zoomLevel >= 10 -> 10f    // Wider zoom - 10km radius
            else -> 20f               // Very wide zoom - 20km radius
        }
        
        viewModelScope.launch {
            try {
                val category = _uiState.value.selectedCategory
                val radiusMeters = (radiusKm * 1000).toInt()
                
                android.util.Log.d("ExploreVM", "Auto-loading places at map center: ${centerLatLng.latitude},${centerLatLng.longitude}, zoom: $zoomLevel, radius: ${radiusMeters}m")
                
                // Handle special categories
                val places = when (category) {
                    PlaceCategory.NONE -> emptyList()
                    PlaceCategory.DOCKS -> placesRepository.loadDocksFromFirestore()
                    else -> placesRepository.searchNearbyPlaces(
                        location = centerLatLng,
                        radiusMeters = radiusMeters,
                        category = category
                    )
                }
                
                android.util.Log.d("ExploreVM", "Auto-loaded ${places.size} places at map center")
                
                _uiState.update { 
                    it.copy(
                        nearbyPlaces = places,
                        lastRefreshTime = System.currentTimeMillis()
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ExploreVM", "Error auto-loading places at map center", e)
            }
        }
    }
    
    /**
     * Search for locations (cities, countries, regions)
     */
    fun searchLocations(query: String) {
        _uiState.update { it.copy(locationSearchQuery = query) }
        
        if (query.length < 2) {
            _uiState.update { it.copy(locationSearchResults = emptyList()) }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingLocation = true) }
            try {
                val results = placesRepository.searchLocationSuggestions(query)
                _uiState.update { 
                    it.copy(
                        locationSearchResults = results,
                        isSearchingLocation = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSearchingLocation = false) }
            }
        }
    }
    
    /**
     * Select a target location to explore
     */
    fun selectTargetLocation(suggestion: LocationSuggestion) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Get the full location details
                val location = placesRepository.getPlaceLocation(suggestion.placeId)
                android.util.Log.d("ExploreVM", "Got location for ${suggestion.name}: $location")
                if (location != null) {
                    _uiState.update { 
                        it.copy(
                            targetLocation = location,
                            targetLocationName = suggestion.name,
                            locationSearchQuery = "",
                            locationSearchResults = emptyList(),
                            lastRefreshTime = 0 // Force refresh
                        )
                    }
                    // Load places at the new location
                    loadNearbyPlacesAtLocation(location)
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Could not find location coordinates"
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ExploreVM", "Error selecting location", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Could not find location"
                    )
                }
            }
        }
    }
    
    /**
     * Load places at a specific location (bypasses state timing issues)
     */
    private fun loadNearbyPlacesAtLocation(location: LatLng) {
        viewModelScope.launch {
            try {
                val category = _uiState.value.selectedCategory
                val radiusMeters = minOf((_uiState.value.searchRadiusKm * 1000).toInt(), 50000)
                
                android.util.Log.d("ExploreVM", "Loading places at target: ${location.latitude},${location.longitude}, radius: ${radiusMeters}m")
                
                // Handle special categories
                val places = when (category) {
                    PlaceCategory.NONE -> emptyList()
                    PlaceCategory.DOCKS -> placesRepository.loadDocksFromFirestore()
                    else -> placesRepository.searchNearbyPlaces(
                        location = location,
                        radiusMeters = radiusMeters,
                        category = category
                    )
                }
                
                // Sort by distance (closest first)
                val sortedPlaces = places.sortedBy { place ->
                    val results = FloatArray(1)
                    android.location.Location.distanceBetween(
                        location.latitude, location.longitude,
                        place.latLng.latitude, place.latLng.longitude,
                        results
                    )
                    results[0]
                }
                
                android.util.Log.d("ExploreVM", "Loaded ${sortedPlaces.size} places at target location (sorted by distance)")
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        nearbyPlaces = sortedPlaces,
                        lastRefreshTime = System.currentTimeMillis()
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ExploreVM", "Error loading places at location", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load places: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Clear target location and use current GPS location
     */
    fun clearTargetLocation() {
        _uiState.update { 
            it.copy(
                targetLocation = null,
                targetLocationName = null,
                locationSearchQuery = "",
                locationSearchResults = emptyList()
            )
        }
        loadNearbyPlaces(forceRefresh = true)
    }
    
    /**
     * Clear location search
     */
    fun clearLocationSearch() {
        _uiState.update { 
            it.copy(
                locationSearchQuery = "",
                locationSearchResults = emptyList()
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    
    /**
     * Get current GPS location
     */
    private fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    _uiState.update { state ->
                        state.copy(currentLocation = latLng)
                    }
                    // Reload places for new location
                    loadNearbyPlaces(forceRefresh = true)
                }
            }
        } catch (e: SecurityException) {
            android.util.Log.e("ExploreVM", "Location permission denied", e)
        }
    }
    
    /**
     * Load featured advertisements
     */
    private fun loadAdvertisements() {
        viewModelScope.launch {
            try {
                val ads = advertisementRepository.getFeaturedAdvertisements(limit = 10)
                _uiState.update { it.copy(advertisements = ads) }
            } catch (e: Exception) {
                android.util.Log.e("ExploreVM", "Error loading ads", e)
            }
        }
    }
    
    /**
     * Select an ad to show details
     */
    fun selectAd(ad: Advertisement) {
        _uiState.update { it.copy(selectedAd = ad, showAdDetails = true) }
        // Record impression
        viewModelScope.launch {
            advertisementRepository.recordImpression(ad.id)
        }
    }
    
    /**
     * Close ad details
     */
    fun closeAdDetails() {
        _uiState.update { it.copy(showAdDetails = false, selectedAd = null) }
    }
    
    /**
     * Record ad click
     */
    fun onAdClick(adId: String) {
        viewModelScope.launch {
            advertisementRepository.recordClick(adId)
        }
    }
    
    /**
     * Load nearby places based on current filters
     */
    fun loadNearbyPlaces(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            // Don't refresh if recently loaded (within 5 minutes) unless forced
            val now = System.currentTimeMillis()
            if (!forceRefresh && now - _uiState.value.lastRefreshTime < 5 * 60 * 1000 && _uiState.value.nearbyPlaces.isNotEmpty()) {
                return@launch
            }
            
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                // Use target location if set, otherwise current GPS location
                val location = _uiState.value.targetLocation ?: _uiState.value.currentLocation
                val category = _uiState.value.selectedCategory
                // Google Places API has a max radius of 50km - cap it
                val radiusMeters = minOf((_uiState.value.searchRadiusKm * 1000).toInt(), 50000)
                
                android.util.Log.d("ExploreVM", "Loading nearby places for category: $category, radius: ${radiusMeters}m, location: ${location.latitude},${location.longitude}")
                
                // Handle special categories
                val places = when (category) {
                    PlaceCategory.NONE -> {
                        // Show nothing
                        emptyList()
                    }
                    PlaceCategory.DOCKS -> {
                        // Load docks from Firestore (same as booking map)
                        placesRepository.loadDocksFromFirestore()
                    }
                    else -> {
                        placesRepository.searchNearbyPlaces(
                            location = location,
                            radiusMeters = radiusMeters,
                            category = category
                        )
                    }
                }
                
                // Apply time filter (skip for DOCKS and NONE categories)
                val filteredPlaces = if (category == PlaceCategory.DOCKS || category == PlaceCategory.NONE) {
                    places
                } else {
                    applyTimeFilter(places, _uiState.value.selectedTimeFilter)
                }
                
                // Sort by distance from current/target location (closest first)
                val sortedPlaces = filteredPlaces.sortedBy { place ->
                    val results = FloatArray(1)
                    android.location.Location.distanceBetween(
                        location.latitude, location.longitude,
                        place.latLng.latitude, place.latLng.longitude,
                        results
                    )
                    results[0] // Distance in meters
                }
                
                android.util.Log.d("ExploreVM", "Loaded ${sortedPlaces.size} places (sorted by distance)")
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        nearbyPlaces = sortedPlaces,
                        lastRefreshTime = now
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ExploreVM", "Error loading places", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load places: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Filter places based on time of day
     */
    private fun applyTimeFilter(places: List<NearbyPlace>, timeFilter: TimeFilter): List<NearbyPlace> {
        return when (timeFilter) {
            TimeFilter.ALL -> places
            TimeFilter.DAY -> {
                // Show places typically open during day (exclude bars/nightclubs unless open)
                places.filter { place ->
                    val isNightlifeType = place.types.any { it in listOf("bar", "night_club", "casino") }
                    if (isNightlifeType) {
                        place.isOpenNow == true
                    } else {
                        true
                    }
                }
            }
            TimeFilter.NIGHT -> {
                // Prioritize nightlife but include open restaurants/venues
                val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val isNightTime = currentHour >= 18 || currentHour < 6
                
                places.filter { place ->
                    val isNightlifeType = place.types.any { it in listOf("bar", "night_club", "casino", "restaurant") }
                    isNightlifeType || (place.isOpenNow == true && isNightTime)
                }.sortedByDescending { place ->
                    // Prioritize bars/clubs
                    if (place.types.any { it in listOf("bar", "night_club") }) 1 else 0
                }
            }
        }
    }
    
    /**
     * Change category filter
     */
    fun setCategory(category: PlaceCategory) {
        if (category != _uiState.value.selectedCategory) {
            _uiState.update { it.copy(selectedCategory = category, nearbyPlaces = emptyList()) }
            loadNearbyPlaces(forceRefresh = true)
        }
    }
    
    /**
     * Change time filter
     */
    fun setTimeFilter(timeFilter: TimeFilter) {
        if (timeFilter != _uiState.value.selectedTimeFilter) {
            _uiState.update { it.copy(selectedTimeFilter = timeFilter) }
            // Re-apply filter to existing places (skip for DOCKS and NONE)
            val category = _uiState.value.selectedCategory
            if (category != PlaceCategory.DOCKS && category != PlaceCategory.NONE) {
                viewModelScope.launch {
                    val location = _uiState.value.targetLocation ?: _uiState.value.currentLocation
                    val filtered = applyTimeFilter(_uiState.value.nearbyPlaces, timeFilter)
                    // Re-sort by distance after filtering
                    val sorted = filtered.sortedBy { place ->
                        val results = FloatArray(1)
                        android.location.Location.distanceBetween(
                            location.latitude, location.longitude,
                            place.latLng.latitude, place.latLng.longitude,
                            results
                        )
                        results[0]
                    }
                    _uiState.update { it.copy(nearbyPlaces = sorted) }
                }
            }
        }
    }
    
    /**
     * Search places by text
     */
    fun searchPlaces(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }
        
        // Debounce search
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // Wait for user to stop typing
            
            _uiState.update { it.copy(isSearching = true) }
            
            try {
                val results = placesRepository.textSearchPlaces(
                    query = query,
                    location = _uiState.value.currentLocation
                )
                
                _uiState.update { 
                    it.copy(
                        searchResults = results,
                        isSearching = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSearching = false,
                        errorMessage = "Search failed: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Clear search
     */
    fun clearSearch() {
        searchJob?.cancel()
        _uiState.update { 
            it.copy(
                searchQuery = "",
                searchResults = emptyList(),
                isSearching = false
            )
        }
    }
    
    /**
     * Select a place to show details
     */
    fun selectPlace(place: NearbyPlace) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedPlace = place, showPlaceDetails = true) }
            
            // Load full details if we don't have them
            if (place.phoneNumber == null && place.website == null) {
                try {
                    val fullDetails = placesRepository.getPlaceFullDetails(place.placeId)
                    if (fullDetails != null) {
                        _uiState.update { it.copy(selectedPlace = fullDetails) }
                    }
                } catch (e: Exception) {
                    // Keep showing basic details
                }
            }
        }
    }
    
    /**
     * Close place details
     */
    fun closePlaceDetails() {
        _uiState.update { it.copy(showPlaceDetails = false, selectedPlace = null) }
    }
    
    /**
     * Update current location
     */
    fun updateLocation(location: LatLng) {
        _uiState.update { it.copy(currentLocation = location) }
        loadNearbyPlaces(forceRefresh = true)
    }
    
    /**
     * Get photo URL for a place
     */
    fun getPhotoUrl(photoReference: String): String {
        return placesRepository.getPhotoUrl(photoReference)
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * Refresh places (pull to refresh)
     */
    fun refresh() {
        loadNearbyPlaces(forceRefresh = true)
    }
    
    /**
     * Refresh GPS location (for target button)
     */
    fun refreshLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    _uiState.update { state ->
                        state.copy(currentLocation = latLng)
                    }
                }
            }
        } catch (e: SecurityException) {
            android.util.Log.e("ExploreVM", "Location permission denied", e)
        }
    }
    
    /**
     * Load news articles and weather data
     */
    private fun loadNewsAndWeather() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingNews = true) }
            
            // Load news articles
            newsRepository.fetchAllNews().onSuccess { articles ->
                _uiState.update { it.copy(newsArticles = articles.take(10)) }
            }
            
            // Load current weather
            newsRepository.fetchWeather().onSuccess { weather ->
                _uiState.update { it.copy(weather = weather) }
            }
            
            // Load forecast
            newsRepository.fetchForecast().onSuccess { forecast ->
                _uiState.update { it.copy(weatherForecast = forecast) }
            }
            
            _uiState.update { it.copy(isLoadingNews = false) }
        }
    }
    
    /**
     * Refresh news and weather
     */
    fun refreshNews() {
        loadNewsAndWeather()
    }
}
