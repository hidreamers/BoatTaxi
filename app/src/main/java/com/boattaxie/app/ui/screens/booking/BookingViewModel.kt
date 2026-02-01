package com.boattaxie.app.ui.screens.booking

import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boattaxie.app.data.model.*
import com.boattaxie.app.data.repository.AdvertisementRepository
import com.boattaxie.app.data.repository.AuthRepository
import com.boattaxie.app.data.repository.BookingRepository
import com.boattaxie.app.data.repository.SubscriptionRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class BookingUiState(
    val isLoading: Boolean = false,
    val isBooking: Boolean = false,
    val isSubmitting: Boolean = false,
    val vehicleType: VehicleType = VehicleType.BOAT,
    
    // User info
    val isTourist: Boolean = false,
    val canBeDriver: Boolean = false,
    val hasActiveSubscription: Boolean = false, // Subscription status for booking
    val showSubscriptionRequired: Boolean = false, // Show paywall dialog
    
    // Location states
    val currentLocation: LatLng? = null,
    val pickupLocation: LatLng? = null,
    val pickupAddress: String? = null,
    val dropoffLocation: LatLng? = null,
    val dropoffAddress: String? = null,
    
    // Map tap mode: true = setting pickup, false = setting dropoff
    val isSettingPickupOnMap: Boolean = true,
    
    // Location search
    val isSearchingPickup: Boolean = false,
    val isSearchingDropoff: Boolean = false,
    
    // Fare estimation
    val estimatedFare: Double? = null,
    val estimatedDistance: Double? = null,
    val estimatedDuration: Int? = null,
    
    // Driver fare adjustment (when captain proposes different rate)
    val driverAdjustedFare: Double? = null,
    val fareAdjustmentReason: String? = null,
    val showFareAdjustmentDialog: Boolean = false,
    val isNightRate: Boolean = false,
    
    // Booking
    val bookingConfirmed: String? = null,
    val activeBooking: Booking? = null,
    val completedBooking: Booking? = null,
    
    // Tracking
    val driverLocation: LatLng? = null,
    val etaMinutes: Int? = null,
    val remainingMinutes: Int? = null,
    
    // Ads on map
    val adsOnMap: List<Advertisement> = emptyList(),
    val selectedMapAd: Advertisement? = null,
    
    // Live online drivers
    val onlineDrivers: List<User> = emptyList(),
    val boatDriversOnline: Int = 0,
    val taxiDriversOnline: Int = 0,
    val showOnlineDrivers: Boolean = false,
    val selectedDriver: User? = null,
    
    // Specific driver request
    val requestedDriverId: String? = null,
    val requestedDriverName: String? = null,
    
    val errorMessage: String? = null
)

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val advertisementRepository: AdvertisementRepository,
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val geocoder: Geocoder
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()
    
    init {
        loadAdsForMap()
        loadUserResidencyStatus()
        observeOnlineDrivers()
        getCurrentLocation()
        loadActiveBooking()
        checkSubscriptionStatus()
    }
    
    /**
     * Check if user has an active subscription
     */
    private fun checkSubscriptionStatus() {
        viewModelScope.launch {
            subscriptionRepository.observeSubscription().collect { subscription ->
                val isActive = subscription != null && subscription.status == SubscriptionStatus.ACTIVE
                _uiState.update { it.copy(hasActiveSubscription = isActive) }
            }
        }
    }
    
    /**
     * Show subscription required dialog
     */
    fun showSubscriptionRequired() {
        _uiState.update { it.copy(showSubscriptionRequired = true) }
    }
    
    fun dismissSubscriptionRequired() {
        _uiState.update { it.copy(showSubscriptionRequired = false) }
    }
    
    /**
     * Load any active booking the rider has (restored from Firebase)
     */
    private fun loadActiveBooking() {
        viewModelScope.launch {
            val activeBooking = bookingRepository.getActiveRiderBooking()
            android.util.Log.d("BookingVM", "Loaded active booking: ${activeBooking?.id}, status: ${activeBooking?.status}")
            activeBooking?.let { booking ->
                _uiState.update { 
                    it.copy(
                        activeBooking = booking,
                        isBooking = true,
                        pickupLocation = LatLng(booking.pickupLocation.latitude, booking.pickupLocation.longitude),
                        pickupAddress = booking.pickupAddress,
                        dropoffLocation = LatLng(booking.destinationLocation.latitude, booking.destinationLocation.longitude),
                        dropoffAddress = booking.destinationAddress,
                        estimatedFare = booking.estimatedFare
                    )
                }
                // Start observing this booking for updates
                observeBookingUpdates(booking.id)
            }
        }
    }
    
    /**
     * Observe booking updates in real-time
     */
    private fun observeBookingUpdates(bookingId: String) {
        viewModelScope.launch {
            bookingRepository.observeBooking(bookingId).collect { booking ->
                android.util.Log.d("BookingVM", "Booking update: ${booking?.id}, status: ${booking?.status}")
                _uiState.update { it.copy(activeBooking = booking) }
            }
        }
    }
    
    /**
     * Observe online drivers in real-time
     */
    private fun observeOnlineDrivers() {
        viewModelScope.launch {
            authRepository.observeOnlineDrivers().collect { drivers ->
                android.util.Log.d("BookingVM", "Received ${drivers.size} online drivers")
                drivers.forEach { driver ->
                    android.util.Log.d("BookingVM", "  - ${driver.fullName} (${driver.vehicleType}) at ${driver.currentLocation?.latitude}, ${driver.currentLocation?.longitude}")
                }
                val boatCount = drivers.count { it.vehicleType == VehicleType.BOAT }
                val taxiCount = drivers.count { it.vehicleType == VehicleType.TAXI }
                _uiState.update { 
                    it.copy(
                        onlineDrivers = drivers,
                        boatDriversOnline = boatCount,
                        taxiDriversOnline = taxiCount
                    ) 
                }
            }
        }
    }
    
    fun toggleShowOnlineDrivers() {
        _uiState.update { it.copy(showOnlineDrivers = !it.showOnlineDrivers) }
    }
    
    fun selectDriver(driver: User?) {
        _uiState.update { it.copy(selectedDriver = driver) }
    }
    
    fun requestSpecificDriver(driver: User) {
        // Check if pickup and dropoff are set
        val state = _uiState.value
        if (state.pickupLocation == null || state.dropoffLocation == null) {
            // Store the driver they want to request and prompt them to set locations
            android.util.Log.d("BookingVM", "requestSpecificDriver: locations not set, storing driver ${driver.id}")
            _uiState.update { 
                it.copy(
                    requestedDriverId = driver.id,
                    requestedDriverName = driver.fullName,
                    selectedDriver = null,
                    errorMessage = "Please set your pickup and drop-off locations first, then the request will be sent to ${driver.fullName.split(" ").firstOrNull()}"
                )
            }
        } else {
            // Locations are set - directly create the booking and send to this driver!
            android.util.Log.d("BookingVM", "requestSpecificDriver: locations set, sending request directly to driver ${driver.id}")
            _uiState.update { 
                it.copy(
                    requestedDriverId = driver.id,
                    requestedDriverName = driver.fullName,
                    selectedDriver = null
                )
            }
            // Immediately confirm the booking
            confirmBooking()
        }
    }
    
    fun clearRequestedDriver() {
        _uiState.update { it.copy(requestedDriverId = null, requestedDriverName = null) }
    }
    
    /**
     * Clear pickup and dropoff locations - reset for new booking
     */
    fun clearLocations() {
        _uiState.update { 
            it.copy(
                pickupLocation = null,
                pickupAddress = null,
                dropoffLocation = null,
                dropoffAddress = null,
                estimatedFare = null,
                isSettingPickupOnMap = true // Reset to pickup mode
            )
        }
    }
    
    /**
     * Set a pre-selected driver from trip history
     * When coming from trip history, the user just needs to set locations
     * then the request will be sent to this driver
     */
    fun setRequestedDriver(driverId: String, driverName: String) {
        android.util.Log.d("BookingVM", "setRequestedDriver: driverId=$driverId, driverName=$driverName")
        _uiState.update { 
            it.copy(
                requestedDriverId = driverId,
                requestedDriverName = driverName
            )
        }
    }
    
    /**
     * Load user's residency status to determine pricing
     */
    private fun loadUserResidencyStatus() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                val isTourist = user?.residencyType == ResidencyType.TOURIST
                val canBeDriver = user?.canBeDriver ?: false
                _uiState.update { it.copy(isTourist = isTourist, canBeDriver = canBeDriver) }
            } catch (e: Exception) {
                // Default to local pricing if error
            }
        }
    }
    
    /**
     * Load advertisements with locations to show on map
     */
    private fun loadAdsForMap() {
        viewModelScope.launch {
            try {
                android.util.Log.d("BookingVM", "Loading ads for map...")
                val ads = advertisementRepository.getAdsWithLocations(limit = 30)
                android.util.Log.d("BookingVM", "Got ${ads.size} ads for map:")
                ads.forEach { ad ->
                    android.util.Log.d("BookingVM", "  Map ad: ${ad.title} by ${ad.advertiserId}")
                }
                _uiState.update { it.copy(adsOnMap = ads) }
                
                // Track impressions for all ads shown on map
                ads.forEach { ad ->
                    launch {
                        try {
                            advertisementRepository.recordImpression(ad.id)
                        } catch (e: Exception) {
                            // Silently fail
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("BookingVM", "Failed to load map ads: ${e.message}")
            }
        }
    }
    
    /**
     * Public function to refresh ads (called from RideTrackingScreen)
     */
    fun refreshAdsForMap() {
        loadAdsForMap()
    }
    
    /**
     * Select an ad marker on the map (track click)
     */
    fun selectMapAd(ad: Advertisement?) {
        _uiState.update { it.copy(selectedMapAd = ad) }
        
        // Track click when an ad is selected (not when dismissed)
        if (ad != null) {
            viewModelScope.launch {
                try {
                    advertisementRepository.recordClick(ad.id)
                    android.util.Log.d("BookingVM", "Recorded click for ad: ${ad.businessName}")
                } catch (e: Exception) {
                    android.util.Log.e("BookingVM", "Failed to record click: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Set dropoff location from ad marker
     */
    fun setDropoffFromAd(location: LatLng, name: String) {
        _uiState.update { 
            it.copy(
                dropoffLocation = location,
                dropoffAddress = name,
                isSettingPickupOnMap = true // Switch back to pickup mode
            )
        }
        calculateFare()
    }
    
    fun setVehicleType(type: VehicleType) {
        _uiState.update { it.copy(vehicleType = type) }
    }
    
    /**
     * Reset to pickup mode - call this when opening the booking screen
     */
    fun resetToPickupMode() {
        _uiState.update { 
            it.copy(
                isSettingPickupOnMap = true,
                pickupLocation = null,
                pickupAddress = null,
                dropoffLocation = null,
                dropoffAddress = null,
                estimatedFare = null
            ) 
        }
    }
    
    fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    _uiState.update { state ->
                        state.copy(
                            currentLocation = latLng,
                            // Don't set pickup automatically - let user tap the map
                            // Keep isSettingPickupOnMap = true so user sets pickup first
                        )
                    }
                    
                    // Just store current location for reference, don't set as pickup
                }
            }
        } catch (e: SecurityException) {
            _uiState.update { it.copy(errorMessage = "Location permission required") }
        }
    }
    
    fun openLocationSearch(isPickup: Boolean) {
        // Trigger navigation state - this will be handled by the screen
        _uiState.update { 
            it.copy(
                isSearchingPickup = isPickup,
                isSearchingDropoff = !isPickup
            )
        }
    }
    
    fun setPickupLocation(address: String, location: LatLng) {
        _uiState.update { 
            it.copy(
                pickupLocation = location,
                pickupAddress = address,
                isSearchingPickup = false
            )
        }
        calculateFare()
    }
    
    fun setDropoffLocation(address: String, location: LatLng) {
        _uiState.update { 
            it.copy(
                dropoffLocation = location,
                dropoffAddress = address,
                isSearchingDropoff = false
            )
        }
        calculateFare()
    }
    
    fun closeLocationSearch() {
        _uiState.update { 
            it.copy(
                isSearchingPickup = false,
                isSearchingDropoff = false
            )
        }
    }
    
    fun toggleMapTapMode() {
        _uiState.update { it.copy(isSettingPickupOnMap = !it.isSettingPickupOnMap) }
    }
    
    fun setMapTapMode(isPickup: Boolean) {
        _uiState.update { it.copy(isSettingPickupOnMap = isPickup) }
    }
    
    fun onMapTapped(latLng: LatLng) {
        android.util.Log.d("BookingViewModel", "Map tapped at: ${latLng.latitude}, ${latLng.longitude}")
        android.util.Log.d("BookingViewModel", "isSettingPickupOnMap: ${_uiState.value.isSettingPickupOnMap}")
        
        viewModelScope.launch {
            // Reverse geocode to get address
            val address = try {
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    addresses[0].getAddressLine(0) ?: "${latLng.latitude}, ${latLng.longitude}"
                } else {
                    "${latLng.latitude}, ${latLng.longitude}"
                }
            } catch (e: Exception) {
                android.util.Log.e("BookingViewModel", "Geocoding failed: ${e.message}")
                "${latLng.latitude}, ${latLng.longitude}"
            }
            
            android.util.Log.d("BookingViewModel", "Address resolved: $address")
            
            if (_uiState.value.isSettingPickupOnMap) {
                setPickupLocation(address, latLng)
                // Automatically switch to dropoff mode after setting pickup
                _uiState.update { it.copy(isSettingPickupOnMap = false) }
            } else {
                setDropoffLocation(address, latLng)
            }
        }
    }
    
    fun setLocationFromSearch(address: String, placeId: String, isPickup: Boolean) {
        android.util.Log.d("BookingVM", "setLocationFromSearch called: address=$address, placeId=$placeId, isPickup=$isPickup")
        viewModelScope.launch {
            // For now, use geocoding to get coordinates from address
            // In production, you'd use Places API getPlaceDetails
            try {
                android.util.Log.d("BookingVM", "Geocoding address: $address")
                val addresses = geocoder.getFromLocationName(address, 1)
                if (!addresses.isNullOrEmpty()) {
                    val location = LatLng(addresses[0].latitude, addresses[0].longitude)
                    android.util.Log.d("BookingVM", "Geocoded to: ${location.latitude}, ${location.longitude}")
                    if (isPickup) {
                        setPickupLocation(address, location)
                    } else {
                        setDropoffLocation(address, location)
                    }
                } else {
                    android.util.Log.w("BookingVM", "No geocode results for: $address")
                    // Fallback - just set the address without coordinates
                    if (isPickup) {
                        _uiState.update { it.copy(pickupAddress = address) }
                    } else {
                        _uiState.update { it.copy(dropoffAddress = address) }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("BookingVM", "Geocoding failed: ${e.message}", e)
                // If geocoding fails, just set the address
                if (isPickup) {
                    _uiState.update { it.copy(pickupAddress = address) }
                } else {
                    _uiState.update { it.copy(dropoffAddress = address) }
                }
            }
        }
    }
    
    fun calculateFare() {
        val pickup = _uiState.value.pickupLocation ?: return
        val dropoff = _uiState.value.dropoffLocation ?: return
        val vehicleType = _uiState.value.vehicleType
        val isTourist = _uiState.value.isTourist
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Calculate distance (simplified - in real app use Distance Matrix API)
            val distance = calculateDistance(pickup, dropoff)
            val duration = (distance * 3).toInt() // Estimate 3 min per mile
            
            val fare = FareCalculator.calculateFare(
                vehicleType = vehicleType,
                distanceKm = distance.toFloat(),
                durationMinutes = duration,
                isTourist = isTourist
            )
            
            _uiState.update {
                it.copy(
                    isLoading = false,
                    estimatedFare = fare,
                    estimatedDistance = distance,
                    estimatedDuration = duration
                )
            }
        }
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
    
    fun confirmBooking() {
        val pickup = _uiState.value.pickupLocation ?: return
        val dropoff = _uiState.value.dropoffLocation ?: return
        val fare = _uiState.value.estimatedFare ?: return
        val distance = _uiState.value.estimatedDistance ?: return
        val duration = _uiState.value.estimatedDuration ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isBooking = true, errorMessage = null) }
            
            // Get current user info for rider details
            val currentUser = authRepository.getCurrentUser()
            
            android.util.Log.d("BookingVM", "confirmBooking: creating booking for requestedDriver=${_uiState.value.requestedDriverId}, rider=${currentUser?.fullName}")
            
            val result = bookingRepository.createBooking(
                vehicleType = _uiState.value.vehicleType,
                pickupLocation = GeoLocation(pickup.latitude, pickup.longitude, _uiState.value.pickupAddress),
                pickupAddress = _uiState.value.pickupAddress ?: "Unknown pickup",
                destinationLocation = GeoLocation(dropoff.latitude, dropoff.longitude, _uiState.value.dropoffAddress),
                destinationAddress = _uiState.value.dropoffAddress ?: "Unknown destination",
                estimatedDistance = distance.toFloat(),
                estimatedDuration = duration,
                requestedDriverId = _uiState.value.requestedDriverId,
                riderName = currentUser?.fullName,
                riderPhoneNumber = currentUser?.phoneNumber,
                riderPhotoUrl = currentUser?.profilePhotoUrl
            )
            
            result.fold(
                onSuccess = { booking ->
                    _uiState.update {
                        it.copy(
                            isBooking = false,
                            bookingConfirmed = booking.id,
                            requestedDriverId = null,
                            requestedDriverName = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isBooking = false,
                            errorMessage = error.message ?: "Failed to create booking"
                        )
                    }
                }
            )
        }
    }
    
    fun observeBooking(bookingId: String) {
        viewModelScope.launch {
            // Clear any stale booking that doesn't match the requested bookingId
            // This prevents showing wrong booking data during initial load
            if (_uiState.value.activeBooking?.id != bookingId) {
                _uiState.update { it.copy(isLoading = true, activeBooking = null) }
            } else {
                _uiState.update { it.copy(isLoading = true) }
            }
            
            // Observe booking status
            bookingRepository.observeBooking(bookingId).collect { booking ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        activeBooking = booking
                    )
                }
                
                // Handle completed booking
                if (booking?.status == BookingStatus.COMPLETED) {
                    _uiState.update { it.copy(completedBooking = booking) }
                }
                
                // Handle driver fare adjustment - show dialog if driver adjusted fare
                booking?.let { b ->
                    if (b.driverAdjustedFare != null && !b.riderAcceptedAdjustment) {
                        _uiState.update { 
                            it.copy(
                                driverAdjustedFare = b.driverAdjustedFare,
                                fareAdjustmentReason = b.fareAdjustmentReason,
                                isNightRate = b.isNightRate,
                                showFareAdjustmentDialog = true
                            )
                        }
                    }
                }
            }
        }
        
        // Also observe driver location separately for real-time updates
        viewModelScope.launch {
            bookingRepository.observeDriverLocation(bookingId).collect { location ->
                _uiState.update { it.copy(driverLocation = location) }
                
                // Calculate ETA if we have pickup location
                location?.let { driverLoc ->
                    _uiState.value.activeBooking?.let { booking ->
                        if (booking.status == BookingStatus.ACCEPTED) {
                            val pickup = LatLng(booking.pickupLocation.latitude, booking.pickupLocation.longitude)
                            val distanceToPickup = calculateDistance(driverLoc, pickup)
                            val etaMinutes = (distanceToPickup * 2).toInt().coerceAtLeast(1) // ~2 min per mile
                            _uiState.update { it.copy(etaMinutes = etaMinutes) }
                        } else if (booking.status == BookingStatus.IN_PROGRESS) {
                            val dropoff = LatLng(booking.destinationLocation.latitude, booking.destinationLocation.longitude)
                            val distanceToDropoff = calculateDistance(driverLoc, dropoff)
                            val remainingMinutes = (distanceToDropoff * 2).toInt().coerceAtLeast(1)
                            _uiState.update { it.copy(remainingMinutes = remainingMinutes) }
                        }
                    }
                }
            }
        }
    }
    
    fun loadCompletedBooking(bookingId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val booking = bookingRepository.getBooking(bookingId)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    completedBooking = booking
                )
            }
        }
    }
    
    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            android.util.Log.d("BookingVM", "Cancelling booking: $bookingId")
            bookingRepository.cancelBooking(bookingId, "User requested cancellation")
            // Clear the active booking from UI
            _uiState.update { 
                it.copy(
                    activeBooking = null,
                    isBooking = false
                )
            }
        }
    }
    
    /**
     * Clear any stale active booking that might be stuck
     */
    fun clearActiveBooking() {
        // Add stack trace to find who is calling this
        android.util.Log.d("BookingVM", "clearActiveBooking called from:", Exception("Stack trace"))
        
        viewModelScope.launch {
            _uiState.value.activeBooking?.let { booking ->
                android.util.Log.d("BookingVM", "Clearing stale booking: ${booking.id}")
                bookingRepository.cancelBooking(booking.id, "Stale booking cleared")
            }
            _uiState.update { 
                it.copy(
                    activeBooking = null,
                    isBooking = false,
                    pickupLocation = null,
                    pickupAddress = null,
                    dropoffLocation = null,
                    dropoffAddress = null,
                    estimatedFare = null
                )
            }
        }
    }
    
    fun submitRating(bookingId: String, rating: Int, review: String, tip: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            
            // Rate the trip using the existing rateTrip method
            bookingRepository.rateTrip(bookingId, rating.toFloat(), review, isDriverRating = false)
            
            // TODO: Implement tip functionality when backend supports it
            
            _uiState.update { it.copy(isSubmitting = false) }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * Accept the driver's adjusted fare
     */
    fun acceptAdjustedFare() {
        val bookingId = _uiState.value.activeBooking?.id ?: return
        viewModelScope.launch {
            bookingRepository.acceptFareAdjustment(bookingId)
            _uiState.update { 
                it.copy(
                    showFareAdjustmentDialog = false,
                    estimatedFare = it.driverAdjustedFare
                )
            }
        }
    }
    
    /**
     * Decline the driver's adjusted fare - cancels the booking
     */
    fun declineAdjustedFare() {
        val bookingId = _uiState.value.activeBooking?.id ?: return
        _uiState.update { it.copy(showFareAdjustmentDialog = false) }
        cancelBooking(bookingId)
    }
    
    /**
     * Dismiss the fare adjustment dialog (user can still see it in booking details)
     */
    fun dismissFareAdjustmentDialog() {
        _uiState.update { it.copy(showFareAdjustmentDialog = false) }
    }
}
