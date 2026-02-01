package com.boattaxie.app.ui.screens.ads

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boattaxie.app.data.model.*
import com.boattaxie.app.data.payment.PaymentManager
import com.boattaxie.app.data.repository.AdvertisementRepository
import com.boattaxie.app.data.repository.PlaceResult
import com.boattaxie.app.data.repository.PlacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdsUiState(
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val advertisements: List<Advertisement> = emptyList(),
    val myAds: List<Advertisement> = emptyList(),
    val selectedAd: Advertisement? = null,
    val adCreated: Boolean = false,
    val errorMessage: String? = null,
    // Location search state
    val locationSearchResults: List<PlaceResult> = emptyList(),
    val isSearchingLocation: Boolean = false
)

@HiltViewModel
class AdsViewModel @Inject constructor(
    private val advertisementRepository: AdvertisementRepository,
    private val placesRepository: PlacesRepository,
    val paymentManager: PaymentManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AdsUiState())
    val uiState: StateFlow<AdsUiState> = _uiState.asStateFlow()
    
    init {
        loadAdvertisements()
    }
    
    private fun loadAdvertisements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val ads = advertisementRepository.getActiveAdvertisements(limit = 50)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        advertisements = ads
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }
    
    // Local database of popular Panama business locations
    private val panamaBusinessLocations = listOf(
        // Panama City Neighborhoods
        PlaceResult("local_1", "Casco Viejo", "Panama City", "Casco Viejo, Panama City, Panama"),
        PlaceResult("local_2", "Panama City Centro", "Panama City", "Downtown Panama City, Panama"),
        PlaceResult("local_4", "Punta Pacifica", "Panama City", "Punta Pacifica, Panama City, Panama"),
        PlaceResult("local_5", "Costa del Este", "Panama City", "Costa del Este, Panama City, Panama"),
        PlaceResult("local_6", "Calle 50", "Panama City", "Calle 50, Panama City, Panama"),
        PlaceResult("local_7", "Via España", "Panama City", "Via España, Panama City, Panama"),
        PlaceResult("local_8", "Obarrio", "Panama City", "Obarrio, Panama City, Panama"),
        PlaceResult("local_9", "El Cangrejo", "Panama City", "El Cangrejo, Panama City, Panama"),
        PlaceResult("local_17", "Clayton", "Panama City", "Clayton, Panama City, Panama"),
        PlaceResult("local_18", "Amador Causeway", "Panama City", "Amador Causeway, Panama City, Panama"),
        PlaceResult("local_19", "Marbella", "Panama City", "Marbella, Panama City, Panama"),
        PlaceResult("local_20", "San Francisco", "Panama City", "San Francisco, Panama City, Panama"),
        PlaceResult("local_21", "Bella Vista", "Panama City", "Bella Vista, Panama City, Panama"),
        PlaceResult("local_22", "Balboa", "Panama City", "Balboa, Panama City, Panama"),
        PlaceResult("local_23", "Albrook", "Panama City", "Albrook, Panama City, Panama"),
        PlaceResult("local_24", "Avenida Central", "Panama City", "Avenida Central, Panama City, Panama"),
        
        // Bocas del Toro - All Islands
        PlaceResult("local_3", "Bocas Town", "Bocas del Toro", "Bocas Town, Isla Colón, Bocas del Toro, Panama"),
        PlaceResult("local_10", "Almirante", "Bocas del Toro", "Almirante Port, Bocas del Toro, Panama"),
        PlaceResult("local_11", "Red Frog Beach", "Bocas del Toro", "Red Frog Beach, Isla Bastimentos, Bocas del Toro, Panama"),
        PlaceResult("local_12", "Starfish Beach", "Bocas del Toro", "Starfish Beach, Isla Colón, Bocas del Toro, Panama"),
        PlaceResult("local_25", "Isla Bastimentos", "Bocas del Toro", "Isla Bastimentos, Bocas del Toro, Panama"),
        PlaceResult("local_26", "Bluff Beach", "Bocas del Toro", "Bluff Beach, Bocas del Toro, Panama"),
        PlaceResult("local_27", "Zapatilla Islands", "Bocas del Toro", "Cayos Zapatilla, Bocas del Toro, Panama"),
        PlaceResult("local_28", "Dolphin Bay", "Bocas del Toro", "Dolphin Bay, Bocas del Toro, Panama"),
        PlaceResult("local_44", "Isla Colón", "Bocas del Toro", "Isla Colón, Bocas del Toro, Panama"),
        PlaceResult("local_45", "Isla Carenero", "Bocas del Toro", "Isla Carenero, Bocas del Toro, Panama"),
        PlaceResult("local_46", "Isla Solarte", "Bocas del Toro", "Isla Solarte, Bocas del Toro, Panama"),
        PlaceResult("local_47", "Wizard Beach", "Bocas del Toro", "Wizard Beach, Isla Bastimentos, Bocas del Toro, Panama"),
        PlaceResult("local_48", "Isla Popa", "Bocas del Toro", "Isla Popa, Bocas del Toro, Panama"),
        PlaceResult("local_49", "Cayo Coral", "Bocas del Toro", "Cayo Coral, Bocas del Toro, Panama"),
        PlaceResult("local_50", "Boca del Drago", "Bocas del Toro", "Boca del Drago, Bocas del Toro, Panama"),
        
        // San Blas / Guna Yala Islands
        PlaceResult("local_15", "San Blas Islands", "Guna Yala", "San Blas Islands, Guna Yala, Panama"),
        PlaceResult("local_51", "Isla Perro", "Guna Yala", "Isla Perro (Dog Island), San Blas, Panama"),
        PlaceResult("local_52", "Isla Pelícano", "Guna Yala", "Isla Pelícano, San Blas, Panama"),
        PlaceResult("local_53", "Isla Iguana", "Guna Yala", "Isla Iguana, San Blas, Panama"),
        PlaceResult("local_54", "Isla Diablo", "Guna Yala", "Isla Diablo, San Blas, Panama"),
        PlaceResult("local_55", "Isla Aguja", "Guna Yala", "Isla Aguja, San Blas, Panama"),
        PlaceResult("local_56", "Cayos Holandeses", "Guna Yala", "Cayos Holandeses, San Blas, Panama"),
        PlaceResult("local_57", "El Porvenir", "Guna Yala", "El Porvenir, San Blas, Panama"),
        
        // Pearl Islands
        PlaceResult("local_14", "Contadora Island", "Pearl Islands", "Isla Contadora, Pearl Islands, Panama"),
        PlaceResult("local_58", "Isla del Rey", "Pearl Islands", "Isla del Rey, Pearl Islands, Panama"),
        PlaceResult("local_59", "Isla San José", "Pearl Islands", "Isla San José, Pearl Islands, Panama"),
        PlaceResult("local_60", "Isla Saboga", "Pearl Islands", "Isla Saboga, Pearl Islands, Panama"),
        PlaceResult("local_61", "Isla Viveros", "Pearl Islands", "Isla Viveros, Pearl Islands, Panama"),
        PlaceResult("local_62", "Pearl Islands", "Panama", "Archipiélago de las Perlas, Panama"),
        
        // Other Islands
        PlaceResult("local_13", "Isla Taboga", "Panama", "Isla Taboga, Panama"),
        PlaceResult("local_31", "Isla Grande", "Colón", "Isla Grande, Colón, Panama"),
        PlaceResult("local_63", "Isla Mamey", "Colón", "Isla Mamey, Colón, Panama"),
        PlaceResult("local_64", "Isla Coiba", "Veraguas", "Isla Coiba National Park, Panama"),
        PlaceResult("local_65", "Isla Cébaco", "Veraguas", "Isla Cébaco, Veraguas, Panama"),
        PlaceResult("local_66", "Islas Secas", "Chiriquí", "Islas Secas, Chiriquí, Panama"),
        PlaceResult("local_67", "Isla Boca Brava", "Chiriquí", "Isla Boca Brava, Chiriquí, Panama"),
        PlaceResult("local_68", "Isla Parida", "Chiriquí", "Isla Parida, Chiriquí, Panama"),
        PlaceResult("local_69", "Isla Palenque", "Chiriquí", "Isla Palenque, Chiriquí, Panama"),
        PlaceResult("local_70", "Isla Gamez", "Chiriquí", "Isla Gamez, Chiriquí, Panama"),
        
        // Beaches
        PlaceResult("local_16", "Coronado", "Panama Oeste", "Coronado Beach, Panama Oeste, Panama"),
        PlaceResult("local_29", "Playa Blanca", "Panama", "Playa Blanca, Farallón, Panama"),
        PlaceResult("local_30", "Santa Catalina", "Veraguas", "Santa Catalina, Veraguas, Panama"),
        PlaceResult("local_32", "Portobelo", "Colón", "Portobelo, Colón, Panama"),
        PlaceResult("local_71", "Playa Venao", "Los Santos", "Playa Venao, Los Santos, Panama"),
        PlaceResult("local_72", "Pedasi", "Los Santos", "Pedasi, Los Santos, Panama"),
        PlaceResult("local_73", "Playa Las Lajas", "Chiriquí", "Playa Las Lajas, Chiriquí, Panama"),
        PlaceResult("local_74", "Playa Santa Clara", "Coclé", "Playa Santa Clara, Coclé, Panama"),
        PlaceResult("local_75", "Rio Mar", "Panama Oeste", "Rio Mar, San Carlos, Panama"),
        PlaceResult("local_76", "Gorgona", "Panama Oeste", "Playa Gorgona, Panama"),
        
        // Major Cities
        PlaceResult("local_33", "David", "Chiriquí", "David, Chiriquí, Panama"),
        PlaceResult("local_34", "Boquete", "Chiriquí", "Boquete, Chiriquí, Panama"),
        PlaceResult("local_35", "Colón", "Colón", "Colón City, Colón, Panama"),
        PlaceResult("local_36", "Santiago", "Veraguas", "Santiago, Veraguas, Panama"),
        PlaceResult("local_37", "Chitré", "Herrera", "Chitré, Herrera, Panama"),
        PlaceResult("local_38", "Las Tablas", "Los Santos", "Las Tablas, Los Santos, Panama"),
        PlaceResult("local_39", "Penonomé", "Coclé", "Penonomé, Coclé, Panama"),
        
        // Popular Areas
        PlaceResult("local_40", "Tocumen Airport", "Panama City", "Tocumen International Airport, Panama"),
        PlaceResult("local_41", "Gamboa", "Panama", "Gamboa, Panama Canal Zone, Panama"),
        PlaceResult("local_42", "El Valle de Antón", "Coclé", "El Valle de Antón, Coclé, Panama"),
        PlaceResult("local_43", "Volcán", "Chiriquí", "Volcán, Chiriquí, Panama")
    )
    
    /**
     * Search for locations using Google Places API + local database
     */
    fun searchLocation(query: String) {
        if (query.isBlank() || query.length < 2) {
            _uiState.update { it.copy(locationSearchResults = emptyList()) }
            return
        }
        
        android.util.Log.d("AdsVM", "Searching location: $query")
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingLocation = true) }
            try {
                // First search local database (fast) - show up to 10 local results
                val lowerQuery = query.lowercase()
                val localResults = panamaBusinessLocations.filter { place ->
                    place.primaryText.lowercase().contains(lowerQuery) ||
                    place.secondaryText.lowercase().contains(lowerQuery) ||
                    place.fullAddress.lowercase().contains(lowerQuery)
                }.take(10)
                
                android.util.Log.d("AdsVM", "Found ${localResults.size} local results")
                
                // Show local results immediately
                if (localResults.isNotEmpty()) {
                    _uiState.update { 
                        it.copy(locationSearchResults = localResults)
                    }
                }
                
                // Then try Google Places API for additional results
                val apiResults = try {
                    placesRepository.searchPlaces(query)
                } catch (e: Exception) {
                    android.util.Log.e("AdsVM", "Google Places API error: ${e.message}")
                    emptyList()
                }
                
                // Combine results - local first, then API, up to 12 total
                val combinedResults = (localResults + apiResults).distinctBy { it.placeId }.take(12)
                
                android.util.Log.d("AdsVM", "Total: ${combinedResults.size} results (${localResults.size} local, ${apiResults.size} API)")
                
                _uiState.update { 
                    it.copy(
                        locationSearchResults = combinedResults,
                        isSearchingLocation = false
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("AdsVM", "Search failed: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        locationSearchResults = emptyList(),
                        isSearchingLocation = false
                    )
                }
            }
        }
    }
    
    /**
     * Get coordinates for a selected place
     */
    suspend fun getPlaceCoordinates(placeId: String): Pair<Double, Double>? {
        return try {
            val details = placesRepository.getPlaceDetails(placeId)
            details?.let { Pair(it.latLng.latitude, it.latLng.longitude) }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Clear location search results
     */
    fun clearLocationSearch() {
        _uiState.update { it.copy(locationSearchResults = emptyList()) }
    }
    
    fun loadMyAds() {
        android.util.Log.d("AdsVM", "loadMyAds() called")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                android.util.Log.d("AdsVM", "Calling getUserAdvertisements...")
                val ads = advertisementRepository.getUserAdvertisements()
                android.util.Log.d("AdsVM", "Got ${ads.size} user ads")
                ads.forEach { ad ->
                    android.util.Log.d("AdsVM", "  - ${ad.businessName}, status=${ad.status}, id=${ad.id}")
                }
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        myAds = ads
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("AdsVM", "Error loading ads: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }
    
    fun loadAdDetails(adId: String) {
        android.util.Log.d("AdsVM", "Loading ad details for: $adId")
        
        // First check in cached lists
        var ad = _uiState.value.advertisements.find { it.id == adId }
            ?: _uiState.value.myAds.find { it.id == adId }
        
        if (ad != null) {
            android.util.Log.d("AdsVM", "Found ad in cache: ${ad.title}")
            _uiState.update { it.copy(selectedAd = ad) }
            
            // Record impression
            viewModelScope.launch {
                advertisementRepository.recordImpression(adId)
            }
        } else {
            // Fetch from Firestore if not in cache
            android.util.Log.d("AdsVM", "Ad not in cache, fetching from Firestore...")
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                val fetchedAd = advertisementRepository.getAdById(adId)
                if (fetchedAd != null) {
                    android.util.Log.d("AdsVM", "Fetched ad from Firestore: ${fetchedAd.title}")
                    _uiState.update { it.copy(selectedAd = fetchedAd, isLoading = false) }
                    advertisementRepository.recordImpression(adId)
                } else {
                    android.util.Log.e("AdsVM", "Ad not found: $adId")
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Ad not found") }
                }
            }
        }
    }
    
    fun createAd(
        businessName: String,
        title: String,
        description: String,
        imageUri: Uri?,
        logoUri: Uri? = null,
        youtubeUrl: String? = null,
        phone: String?,
        email: String?,
        website: String?,
        category: AdCategory,
        plan: AdPlan,
        isFeatured: Boolean,
        location: GeoLocation? = null,
        locationName: String? = null,
        hasCoupon: Boolean = false,
        couponCode: String? = null,
        couponDiscount: String? = null,
        couponDescription: String? = null,
        couponMaxRedemptions: Int? = null
    ) {
        android.util.Log.d("AdsVM", "Creating ad: $businessName - $title")
        android.util.Log.d("AdsVM", "Location: $locationName, Category: $category, Plan: $plan")
        
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, errorMessage = null) }
            
            try {
                val result = advertisementRepository.createAdvertisement(
                    businessName = businessName,
                    title = title,
                    description = description,
                    imageUri = imageUri,
                    logoUri = logoUri,
                    youtubeUrl = youtubeUrl,
                    websiteUrl = website,
                    phoneNumber = phone,
                    email = email,
                    location = location,
                    locationName = locationName,
                    category = category,
                    targetAudience = AdTargetAudience.ALL,
                    plan = plan,
                    isFeatured = isFeatured,
                    hasCoupon = hasCoupon,
                    couponCode = couponCode,
                    couponDiscount = couponDiscount,
                    couponDescription = couponDescription,
                    couponMaxRedemptions = couponMaxRedemptions
                )
                
                result.fold(
                    onSuccess = { ad ->
                        android.util.Log.d("AdsVM", "Ad created successfully: ${ad.id}")
                        
                        // Simulate payment and activate
                        val activateResult = advertisementRepository.activateAdvertisement(ad.id)
                        android.util.Log.d("AdsVM", "Activation result: ${activateResult.isSuccess}")
                        
                        // Refresh the ads list so new ad appears
                        val updatedAds = advertisementRepository.getUserAdvertisements()
                        android.util.Log.d("AdsVM", "User has ${updatedAds.size} ads")
                        
                        _uiState.update { 
                            it.copy(
                                isCreating = false,
                                adCreated = true,
                                myAds = updatedAds
                            )
                        }
                    },
                    onFailure = { error ->
                        android.util.Log.e("AdsVM", "Failed to create ad: ${error.message}", error)
                        _uiState.update { 
                            it.copy(
                                isCreating = false,
                                errorMessage = error.message ?: "Failed to create ad"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("AdsVM", "Exception creating ad: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isCreating = false,
                        errorMessage = e.message ?: "Failed to create ad"
                    )
                }
            }
        }
    }
    
    fun pauseAd(adId: String) {
        viewModelScope.launch {
            advertisementRepository.pauseAdvertisement(adId)
            loadMyAds()
        }
    }
    
    fun resumeAd(adId: String) {
        viewModelScope.launch {
            advertisementRepository.resumeAdvertisement(adId)
            loadMyAds()
        }
    }
    
    fun deleteAd(adId: String) {
        viewModelScope.launch {
            advertisementRepository.deleteAdvertisement(adId)
            loadMyAds()
        }
    }
    
    fun updateAd(
        adId: String,
        businessName: String,
        title: String,
        description: String,
        phoneNumber: String?,
        email: String?,
        websiteUrl: String?,
        youtubeUrl: String?,
        couponDiscount: String?,
        couponDescription: String?,
        imageUri: Uri? = null,
        logoUri: Uri? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = advertisementRepository.updateAdvertisement(
                adId = adId,
                businessName = businessName,
                title = title,
                description = description,
                phoneNumber = phoneNumber,
                email = email,
                websiteUrl = websiteUrl,
                youtubeUrl = youtubeUrl,
                couponDiscount = couponDiscount,
                couponDescription = couponDescription,
                imageUri = imageUri,
                logoUri = logoUri
            )
            result.fold(
                onSuccess = {
                    loadMyAds()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = "Failed to update: ${e.message}") }
                }
            )
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    fun resetAdCreatedFlag() {
        _uiState.update { it.copy(adCreated = false) }
    }

    /**
     * Seed test advertisements (for development)
     * First deletes ALL other ads (keeps yours), then creates new test ones
     */
    fun seedTestAds() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Delete ALL ads except the current user's
                android.util.Log.d("AdsVM", "Deleting all ads except yours...")
                advertisementRepository.deleteAllAdsExceptMine()
                
                // Then create new test ads
                val result = advertisementRepository.seedTestAds()
                result.onSuccess { count ->
                    android.util.Log.d("AdsVM", "Seeded $count test ads")
                    loadAdvertisements()
                }.onFailure { e ->
                    android.util.Log.e("AdsVM", "Failed to seed: ${e.message}", e)
                    _uiState.update { it.copy(errorMessage = "Failed to seed: ${e.message}") }
                }
            } catch (e: Exception) {
                android.util.Log.e("AdsVM", "Seed error: ${e.message}", e)
                _uiState.update { it.copy(errorMessage = "Error: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Create demo ads with custom description and count
     */
    fun createDemoAds(description: String = "", count: Int = 10) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = advertisementRepository.createDemoAds(description, count)
                result.onSuccess { createdCount ->
                    android.util.Log.d("AdsVM", "Created $createdCount demo ads")
                    loadMyAds() // Refresh my ads
                }.onFailure { e ->
                    android.util.Log.e("AdsVM", "Failed to create demo ads: ${e.message}", e)
                    _uiState.update { it.copy(errorMessage = "Failed to create demo ads: ${e.message}") }
                }
            } catch (e: Exception) {
                android.util.Log.e("AdsVM", "Demo ads error: ${e.message}", e)
                _uiState.update { it.copy(errorMessage = "Error: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
