package com.boattaxie.app.ui.screens.booking

import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boattaxie.app.data.repository.PlaceResult
import com.boattaxie.app.data.repository.PlacesRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LocationSearchUiState(
    val isSearching: Boolean = false,
    val searchResults: List<PlaceResult> = emptyList(),
    val recentPlaces: List<PlaceResult> = emptyList(),
    val currentLocation: LatLng? = null,
    val currentAddress: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class LocationSearchViewModel @Inject constructor(
    private val placesRepository: PlacesRepository,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val geocoder: Geocoder
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LocationSearchUiState())
    val uiState: StateFlow<LocationSearchUiState> = _uiState.asStateFlow()
    
    init {
        getCurrentLocation()
        loadRecentPlaces()
    }
    
    private fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    _uiState.update { state -> state.copy(currentLocation = latLng) }
                    
                    // Reverse geocode to get address
                    try {
                        val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0].getAddressLine(0)
                            _uiState.update { state -> state.copy(currentAddress = address) }
                        }
                    } catch (e: Exception) {
                        _uiState.update { state -> 
                            state.copy(currentAddress = "${it.latitude}, ${it.longitude}") 
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            _uiState.update { it.copy(errorMessage = "Location permission required") }
        }
    }
    
    // Local database of Panama places - islands, restaurants, hotels, landmarks
    // Including Bocas del Toro water taxi routes and destinations
    private val panamaPlaces = listOf(
        // ========= BOCAS DEL TORO WATER TAXI ROUTES =========
        PlaceResult("bocas_1", "Bocas Town (Main Dock)", "Isla Colón, Bocas del Toro", "Bocas Town, Isla Colón, Bocas del Toro, Panama"),
        PlaceResult("bocas_2", "Almirante Port", "Mainland, Bocas del Toro", "Almirante Port, Bocas del Toro, Panama"),
        PlaceResult("bocas_3", "Old Bank (Bastimentos)", "Isla Bastimentos, Bocas del Toro", "Old Bank, Isla Bastimentos, Bocas del Toro, Panama"),
        PlaceResult("bocas_4", "Isla Carenero", "Bocas del Toro", "Isla Carenero, Bocas del Toro, Panama"),
        PlaceResult("bocas_5", "Isla Solarte", "Bocas del Toro", "Isla Solarte, Bocas del Toro, Panama"),
        PlaceResult("bocas_6", "Red Frog Beach", "Isla Bastimentos, Bocas del Toro", "Red Frog Beach, Isla Bastimentos, Bocas del Toro, Panama"),
        PlaceResult("bocas_7", "Starfish Beach", "Isla Colón, Bocas del Toro", "Starfish Beach, Isla Colón, Bocas del Toro, Panama"),
        PlaceResult("bocas_8", "Zapatilla Islands", "Bocas del Toro", "Zapatilla Islands, Bocas del Toro Marine Park, Panama"),
        PlaceResult("bocas_9", "Bocas del Drago", "Isla Colón, Bocas del Toro", "Boca del Drago Beach, Isla Colón, Bocas del Toro, Panama"),
        PlaceResult("bocas_10", "Bluff Beach", "Isla Colón, Bocas del Toro", "Bluff Beach, Isla Colón, Bocas del Toro, Panama"),
        PlaceResult("bocas_11", "Bocas Airport", "Isla Colón, Bocas del Toro", "Bocas del Toro International Airport, Isla Colón, Panama"),
        PlaceResult("bocas_12", "Bambuda Lodge", "Isla Solarte, Bocas del Toro", "Bambuda Lodge, Isla Solarte, Bocas del Toro, Panama"),
        PlaceResult("bocas_13", "Dolphin Bay", "Isla Cristóbal, Bocas del Toro", "Dolphin Bay, Isla Cristóbal, Bocas del Toro, Panama"),
        PlaceResult("bocas_14", "Hospital Point", "Isla Solarte, Bocas del Toro", "Hospital Point, Isla Solarte, Bocas del Toro, Panama"),
        PlaceResult("bocas_15", "Crawl Cay", "Bocas del Toro", "Crawl Cay, Bocas del Toro, Panama"),
        PlaceResult("bocas_16", "Playa Wizard", "Isla Bastimentos", "Wizard Beach, Isla Bastimentos, Bocas del Toro, Panama"),
        PlaceResult("bocas_17", "Punta Vieja", "Isla Bastimentos", "Punta Vieja, Isla Bastimentos, Bocas del Toro, Panama"),
        PlaceResult("bocas_18", "Salt Creek", "Isla Bastimentos", "Salt Creek Village, Isla Bastimentos, Bocas del Toro, Panama"),
        
        // Bocas Hotels & Resorts
        PlaceResult("bocas_h1", "Playa Tortuga Hotel", "Bocas Town, Bocas del Toro", "Playa Tortuga Hotel & Beach Resort, Bocas Town, Panama"),
        PlaceResult("bocas_h2", "Hotel Bocas del Toro", "Bocas Town", "Hotel & Restaurant Bocas del Toro, Bocas Town, Panama"),
        PlaceResult("bocas_h3", "Tropical Suites", "Bocas Town", "Tropical Suites Hotel, Bocas Town, Bocas del Toro, Panama"),
        PlaceResult("bocas_h4", "Red Frog Beach Resort", "Isla Bastimentos", "Red Frog Beach Island Resort, Isla Bastimentos, Panama"),
        PlaceResult("bocas_h5", "Punta Caracol Acqua Lodge", "Isla Colón", "Punta Caracol Acqua-Lodge, Isla Colón, Bocas del Toro, Panama"),
        
        // Bocas Restaurants
        PlaceResult("bocas_r1", "Bibi's on the Beach", "Bocas Town", "Bibi's on the Beach, Bocas Town, Bocas del Toro, Panama"),
        PlaceResult("bocas_r2", "El Ultimo Refugio", "Bocas Town", "El Ultimo Refugio, Bocas Town, Bocas del Toro, Panama"),
        PlaceResult("bocas_r3", "Capitan Caribe", "Bocas Town", "Capitan Caribe, Bocas Town, Bocas del Toro, Panama"),
        PlaceResult("bocas_r4", "Om Cafe", "Bocas Town", "Om Cafe, Bocas Town, Bocas del Toro, Panama"),
        
        // ========= PANAMA CITY ISLANDS =========
        PlaceResult("island_1", "Isla Taboga", "Taboga Island, Panama", "Isla Taboga, Panama"),
        PlaceResult("island_2", "Isla Contadora", "Pearl Islands, Panama", "Isla Contadora, Pearl Islands, Panama"),
        PlaceResult("island_3", "Isla del Rey", "Pearl Islands, Panama", "Isla del Rey, Pearl Islands, Panama"),
        PlaceResult("island_4", "San Blas Islands", "Guna Yala, Panama", "San Blas Islands, Guna Yala, Panama"),
        
        // Panama City Restaurants
        PlaceResult("rest_1", "Maito Restaurant", "Calle 50, Panama City", "Maito Restaurant, Calle 50, Panama City, Panama"),
        PlaceResult("rest_2", "Donde José", "Casco Viejo, Panama City", "Donde José, Casco Viejo, Panama City, Panama"),
        PlaceResult("rest_3", "Intimo", "Calle Uruguay, Panama City", "Intimo, Calle Uruguay, Panama City, Panama"),
        PlaceResult("rest_4", "La Casa del Marisco", "Panama City", "La Casa del Marisco, Panama City, Panama"),
        PlaceResult("rest_5", "Tomillo Restaurant", "Costa del Este, Panama City", "Tomillo, Costa del Este, Panama City, Panama"),
        
        // Panama City Hotels
        PlaceResult("hotel_1", "The Bristol Panama", "Via España, Panama City", "The Bristol Panama, Via España, Panama City"),
        PlaceResult("hotel_2", "Waldorf Astoria Panama", "Panama City", "Waldorf Astoria Panama, Panama City"),
        PlaceResult("hotel_3", "American Trade Hotel", "Casco Viejo, Panama City", "American Trade Hotel, Casco Viejo, Panama City"),
        PlaceResult("hotel_4", "JW Marriott Panama", "Punta Pacifica, Panama City", "JW Marriott Panama, Punta Pacifica, Panama City"),
        
        // Panama City Landmarks
        PlaceResult("land_1", "Panama Canal", "Miraflores, Panama City", "Panama Canal, Miraflores Locks, Panama City"),
        PlaceResult("land_2", "Miraflores Locks", "Panama Canal, Panama City", "Miraflores Locks Visitor Center, Panama City"),
        PlaceResult("land_3", "Casco Viejo", "Old Town, Panama City", "Casco Viejo, Panama City, Panama"),
        PlaceResult("land_4", "Biomuseo", "Amador Causeway, Panama City", "Biomuseo, Amador Causeway, Panama City"),
        PlaceResult("land_5", "Amador Causeway", "Panama City", "Amador Causeway, Panama City, Panama"),
        
        // Airports & Transport
        PlaceResult("trans_1", "Tocumen International Airport", "Panama City", "Tocumen International Airport (PTY), Panama City"),
        PlaceResult("trans_2", "Albrook Mall", "Albrook, Panama City", "Albrook Mall, Panama City, Panama"),
        PlaceResult("trans_3", "Flamenco Marina", "Amador, Panama City", "Flamenco Marina, Amador, Panama City"),
        
        // Beaches
        PlaceResult("beach_1", "Playa Blanca", "Farallon, Panama", "Playa Blanca, Farallon, Panama"),
        PlaceResult("beach_2", "Playa Coronado", "Coronado, Panama", "Playa Coronado, Panama"),
        PlaceResult("beach_3", "Santa Clara Beach", "Santa Clara, Panama", "Santa Clara Beach, Panama"),
        
        // Neighborhoods
        PlaceResult("neigh_1", "Punta Pacifica", "Panama City", "Punta Pacifica, Panama City, Panama"),
        PlaceResult("neigh_2", "Costa del Este", "Panama City", "Costa del Este, Panama City, Panama"),
        PlaceResult("neigh_3", "El Cangrejo", "Panama City", "El Cangrejo, Panama City, Panama")
    )
    
    private fun loadRecentPlaces() {
        // Load from local storage - popular Panama places
        _uiState.update {
            it.copy(
                recentPlaces = listOf(
                    PlaceResult(
                        placeId = "recent_1",
                        primaryText = "Casco Viejo",
                        secondaryText = "Panama City, Panama",
                        fullAddress = "Casco Viejo, Panama City, Panama"
                    ),
                    PlaceResult(
                        placeId = "recent_2",
                        primaryText = "Tocumen International Airport",
                        secondaryText = "Panama City, Panama",
                        fullAddress = "Tocumen International Airport, Panama City, Panama"
                    ),
                    PlaceResult(
                        placeId = "recent_3",
                        primaryText = "Panama Canal - Miraflores",
                        secondaryText = "Panama City, Panama",
                        fullAddress = "Miraflores Locks, Panama Canal, Panama City"
                    ),
                    PlaceResult(
                        placeId = "recent_4",
                        primaryText = "Bocas del Toro",
                        secondaryText = "Bocas del Toro Province, Panama",
                        fullAddress = "Bocas del Toro, Panama"
                    )
                )
            )
        }
    }
    
    fun searchPlaces(query: String) {
        android.util.Log.d("LocationSearchVM", "searchPlaces called with: '$query'")
        if (query.isBlank() || query.length < 2) {
            android.util.Log.d("LocationSearchVM", "Query too short, skipping")
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            
            try {
                // First search local database (fast)
                val localResults = searchLocalPlaces(query)
                android.util.Log.d("LocationSearchVM", "Local results: ${localResults.size}")
                
                // Show local results immediately
                if (localResults.isNotEmpty()) {
                    _uiState.update { it.copy(searchResults = localResults, isSearching = true) }
                }
                
                // Then try Google Places API
                val apiResults = try {
                    placesRepository.searchPlaces(
                        query = query,
                        currentLocation = _uiState.value.currentLocation
                    )
                } catch (e: Exception) {
                    android.util.Log.e("LocationSearchVM", "Places API error: ${e.message}")
                    emptyList()
                }
                android.util.Log.d("LocationSearchVM", "API results: ${apiResults.size}")
                
                // Combine results - local first, then API
                val combinedResults = (localResults + apiResults).distinctBy { it.placeId }.take(15)
                
                android.util.Log.d("LocationSearchVM", "Total combined: ${combinedResults.size}")
                
                _uiState.update { 
                    it.copy(
                        isSearching = false,
                        searchResults = combinedResults
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("LocationSearchVM", "Search error: ${e.message}", e)
                _uiState.update { it.copy(isSearching = false) }
            }
        }
    }
    
    private fun searchLocalPlaces(query: String): List<PlaceResult> {
        if (query.length < 2) return emptyList()
        
        val lowerQuery = query.lowercase()
        return panamaPlaces.filter { place ->
            place.primaryText.lowercase().contains(lowerQuery) ||
            place.secondaryText.lowercase().contains(lowerQuery) ||
            place.fullAddress.lowercase().contains(lowerQuery)
        }.take(10)
    }
    
    fun useCurrentLocation() {
        // Current location is already available in uiState
    }
    
    fun clearSearch() {
        _uiState.update { it.copy(searchResults = emptyList()) }
    }
}
