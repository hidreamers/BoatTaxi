package com.boattaxie.app.ui.screens.driver

import android.annotation.SuppressLint
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boattaxie.app.data.model.*
import com.boattaxie.app.data.repository.AuthRepository
import com.boattaxie.app.data.repository.BookingRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DriverUiState(
    val isLoading: Boolean = false,
    val isOnline: Boolean = false,
    val driverName: String = "Driver",
    val vehicleType: VehicleType = VehicleType.TAXI,
    
    // Multi-vehicle support
    val hasBoat: Boolean = false,
    val hasTaxi: Boolean = false,
    
    // Location
    val currentLocation: LatLng? = null,
    
    // Today's stats
    val todayEarnings: Double = 0.0,
    val todayTrips: Int = 0,
    val onlineHours: Double = 0.0,
    val rating: Float = 5.0f,
    
    // Week stats
    val weekEarnings: Double = 0.0,
    val weekTrips: Int = 0,
    val baseFaresEarnings: Double = 0.0,
    val tipsEarnings: Double = 0.0,
    val bonusEarnings: Double = 0.0,
    val dailyEarnings: Map<String, Double> = emptyMap(),
    
    // Ride requests
    val pendingRequest: Booking? = null,
    val acceptedBookingId: String? = null,
    val activeRide: Booking? = null,
    
    // All pending requests (for map display)
    val allPendingRequests: List<Booking> = emptyList(),
    val showRequestsOnMap: Boolean = false,
    val boatRequestCount: Int = 0,
    val taxiRequestCount: Int = 0,
    val selectedRequest: Booking? = null,
    
    // Fare adjustment
    val showFareAdjustmentSheet: Boolean = false,
    val isNightRateTime: Boolean = false,
    val adjustedFare: String = "",
    val adjustmentReason: String = "",
    
    val errorMessage: String? = null
)

@HiltViewModel
class DriverViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val bookingRepository: BookingRepository,
    private val fusedLocationClient: FusedLocationProviderClient
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DriverUiState())
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()
    
    // Track recently completed ride to prevent re-loading it due to Firestore delay
    private var justCompletedRideId: String? = null
    
    // Location callback for continuous GPS updates when online
    private var locationCallback: LocationCallback? = null
    
    init {
        loadDriverData()
        getCurrentLocation()
        checkNightRate()
        loadOnlineStatus()
        // Load any active ride the driver has
        loadActiveRideOnInit()
    }
    
    private fun loadOnlineStatus() {
        // Load persisted online status from user record
        viewModelScope.launch {
            authRepository.getCurrentUser()?.let { user ->
                if (user.isOnline) {
                    _uiState.update { it.copy(isOnline = true) }
                    // Start continuous location updates if already online
                    startContinuousLocationUpdates()
                    startListeningForRides()
                }
            }
        }
    }
    
    /**
     * Refresh active ride - call this when screen becomes visible
     */
    fun refreshActiveRide() {
        viewModelScope.launch {
            val booking = bookingRepository.getDriverActiveBooking()
            android.util.Log.d("DriverVM", "refreshActiveRide: found ${booking?.id}, status: ${booking?.status}, justCompleted: $justCompletedRideId")
            
            // Skip if this is a ride we just completed (Firestore may not have synced yet)
            if (booking != null && booking.id != justCompletedRideId) {
                _uiState.update { state ->
                    state.copy(
                        activeRide = booking,
                        isOnline = true
                    ) 
                }
                // Start continuous location updates for active ride
                startContinuousLocationUpdates()
                startLocationUpdates(booking.id)
                startListeningForRides()
            }
        }
    }
    
    private fun loadActiveRideOnInit() {
        // Check if driver has any active rides (ACCEPTED, ARRIVED, IN_PROGRESS)
        viewModelScope.launch {
            val booking = bookingRepository.getDriverActiveBooking()
            android.util.Log.d("DriverVM", "loadActiveRideOnInit: found ${booking?.id}, status: ${booking?.status}")
            booking?.let {
                _uiState.update { state ->
                    state.copy(
                        activeRide = it,
                        isOnline = true
                    ) 
                }
                // Start continuous location updates for active ride
                startContinuousLocationUpdates()
                startLocationUpdates(it.id)
                // Also listen for new rides
                startListeningForRides()
            }
        }
    }
    
    private fun checkNightRate() {
        _uiState.update { it.copy(isNightRateTime = bookingRepository.isNightRateTime()) }
    }
    
    private fun loadDriverData() {
        viewModelScope.launch {
            authRepository.observeCurrentUser().collect { user ->
                user?.let {
                    android.util.Log.d("DriverVM", "loadDriverData: hasBoat=${it.hasBoat}, hasTaxi=${it.hasTaxi}, vehicleType=${it.vehicleType}")
                    _uiState.update { state ->
                        state.copy(
                            driverName = it.fullName.split(" ").firstOrNull() ?: "Driver",
                            vehicleType = it.vehicleType ?: if (it.userType == UserType.CAPTAIN) VehicleType.BOAT else VehicleType.TAXI,
                            hasBoat = it.hasBoat,
                            hasTaxi = it.hasTaxi,
                            rating = it.rating
                        )
                    }
                }
            }
        }
        
        // Initialize with zeros - real earnings will come from completed bookings
        _uiState.update {
            it.copy(
                todayEarnings = 0.0,
                todayTrips = 0,
                onlineHours = 0.0,
                weekEarnings = 0.0,
                weekTrips = 0,
                baseFaresEarnings = 0.0,
                tipsEarnings = 0.0,
                bonusEarnings = 0.0,
                dailyEarnings = emptyMap()
            )
        }
    }
    
    private fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    _uiState.update { state ->
                        state.copy(currentLocation = LatLng(it.latitude, it.longitude))
                    }
                }
            }
        } catch (e: SecurityException) {
            // Handle permission not granted
        }
    }
    
    /**
     * Start continuous GPS location updates when driver goes online
     * Updates location every 3 seconds for smooth map tracking
     */
    @SuppressLint("MissingPermission")
    private fun startContinuousLocationUpdates() {
        if (locationCallback != null) return // Already running
        
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L // Update every 3 seconds
        )
            .setMinUpdateIntervalMillis(2000L)
            .setMaxUpdateDelayMillis(5000L)
            .build()
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val newLatLng = LatLng(location.latitude, location.longitude)
                    
                    // Update local state
                    _uiState.update { state ->
                        state.copy(currentLocation = newLatLng)
                    }
                    
                    // Update Firebase so riders see live location
                    viewModelScope.launch {
                        authRepository.updateUserLocation(
                            GeoLocation(latitude = location.latitude, longitude = location.longitude)
                        )
                    }
                }
            }
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
            android.util.Log.d("DriverVM", "Started continuous location updates")
        } catch (e: Exception) {
            android.util.Log.e("DriverVM", "Failed to start location updates", e)
        }
    }
    
    /**
     * Stop continuous location updates when driver goes offline
     */
    private fun stopContinuousLocationUpdates() {
        locationCallback?.let { callback ->
            fusedLocationClient.removeLocationUpdates(callback)
            android.util.Log.d("DriverVM", "Stopped continuous location updates")
        }
        locationCallback = null
    }
    
    fun toggleOnlineStatus() {
        val newStatus = !_uiState.value.isOnline
        _uiState.update { it.copy(isOnline = newStatus) }
        
        // Persist to Firebase and fix user record
        viewModelScope.launch {
            authRepository.setDriverOnlineStatus(newStatus)
            // Fix user type in Firebase if needed (ensure lowercase)
            authRepository.fixUserTypeCase()
        }
        
        if (newStatus) {
            // Going online - start continuous location updates
            startContinuousLocationUpdates()
            startListeningForRides()
            // Clear any old stale pending requests first
            viewModelScope.launch {
                android.util.Log.d("DriverVM", "Driver going online, listening for rides...")
            }
        } else {
            // Going offline - stop location updates and clear state
            stopContinuousLocationUpdates()
            stopListeningForRides()
            _uiState.update { 
                it.copy(
                    pendingRequest = null,
                    allPendingRequests = emptyList(),
                    boatRequestCount = 0,
                    taxiRequestCount = 0,
                    selectedRequest = null
                )
            }
        }
    }
    
    /**
     * Switch between boat and taxi mode (for multi-vehicle drivers)
     */
    fun switchVehicleType(vehicleType: VehicleType) {
        viewModelScope.launch {
            // Go offline first
            if (_uiState.value.isOnline) {
                authRepository.setDriverOnlineStatus(false)
                stopContinuousLocationUpdates()
                stopListeningForRides()
            }
            
            // Update vehicle type in UI
            _uiState.update { 
                it.copy(
                    vehicleType = vehicleType,
                    isOnline = false,
                    pendingRequest = null,
                    allPendingRequests = emptyList()
                )
            }
            
            // Update vehicle type in Firebase
            authRepository.updateVehicleType(vehicleType)
            
            android.util.Log.d("DriverVM", "Switched to vehicle type: $vehicleType")
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Clean up location updates when ViewModel is destroyed
        stopContinuousLocationUpdates()
    }
    
    private fun startListeningForRides() {
        viewModelScope.launch {
            bookingRepository.observePendingBookings(_uiState.value.vehicleType).collect { bookings ->
                android.util.Log.d("DriverVM", "observePendingBookings: ${bookings.size} for ${_uiState.value.vehicleType}")
                val pendingBooking = bookings.firstOrNull { it.status == BookingStatus.PENDING }
                _uiState.update { it.copy(pendingRequest = pendingBooking) }
            }
        }
        // Also observe ALL pending bookings for the map display
        viewModelScope.launch {
            bookingRepository.observeAllPendingBookings().collect { bookings ->
                android.util.Log.d("DriverVM", "Received ${bookings.size} pending bookings from Firebase")
                bookings.forEach { booking ->
                    android.util.Log.d("DriverVM", "  - Booking ${booking.id}: ${booking.pickupAddress} -> ${booking.destinationAddress}, status: ${booking.status}")
                }
                val boatCount = bookings.count { it.vehicleType == VehicleType.BOAT }
                val taxiCount = bookings.count { it.vehicleType == VehicleType.TAXI }
                _uiState.update { 
                    it.copy(
                        allPendingRequests = bookings,
                        boatRequestCount = boatCount,
                        taxiRequestCount = taxiCount
                    ) 
                }
            }
        }
    }
    
    /**
     * Clear all old test/pending bookings from Firebase (for testing)
     */
    fun clearOldPendingBookings() {
        viewModelScope.launch {
            bookingRepository.clearAllPendingBookings()
        }
    }
    
    fun toggleShowRequestsOnMap() {
        _uiState.update { it.copy(showRequestsOnMap = !it.showRequestsOnMap) }
    }
    
    fun selectRequest(booking: Booking?) {
        _uiState.update { it.copy(selectedRequest = booking) }
    }
    
    fun acceptSelectedRequest() {
        _uiState.value.selectedRequest?.let { booking ->
            acceptBooking(booking.id)
            _uiState.update { it.copy(selectedRequest = null) }
        }
    }
    
    private fun stopListeningForRides() {
        _uiState.update { it.copy(pendingRequest = null) }
    }
    
    fun acceptBooking(bookingId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val result = bookingRepository.acceptBooking(bookingId)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pendingRequest = null,
                            acceptedBookingId = bookingId
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message
                        )
                    }
                }
            )
        }
    }
    
    fun declineBooking(bookingId: String) {
        _uiState.update { it.copy(pendingRequest = null) }
    }
    
    fun clearAcceptedBooking() {
        _uiState.update { it.copy(acceptedBookingId = null) }
    }
    
    fun clearActiveRide() {
        justCompletedRideId = _uiState.value.activeRide?.id
        _uiState.update { it.copy(activeRide = null) }
    }
    
    fun loadActiveRide(bookingId: String) {
        viewModelScope.launch {
            bookingRepository.observeBooking(bookingId).collect { booking ->
                // Don't update if the ride was just completed/cancelled or is in a terminal state
                if (booking?.status in listOf(BookingStatus.COMPLETED, BookingStatus.CANCELLED)) {
                    // Clear the active ride if it reached a terminal state
                    _uiState.update { it.copy(activeRide = null) }
                } else if (booking?.id != justCompletedRideId) {
                    _uiState.update { it.copy(activeRide = booking) }
                }
            }
        }
        
        // Start location updates for this ride
        startLocationUpdates(bookingId)
    }
    
    private fun startLocationUpdates(bookingId: String) {
        // Update location every few seconds while on active ride
        viewModelScope.launch {
            while (_uiState.value.activeRide != null) {
                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            val latLng = LatLng(it.latitude, it.longitude)
                            _uiState.update { state -> state.copy(currentLocation = latLng) }
                            
                            // Send location to Firebase for rider to see
                            viewModelScope.launch {
                                bookingRepository.updateDriverLocation(
                                    bookingId = bookingId,
                                    latitude = it.latitude,
                                    longitude = it.longitude
                                )
                            }
                        }
                    }
                } catch (e: SecurityException) {
                    // Handle permission not granted
                }
                kotlinx.coroutines.delay(5000) // Update every 5 seconds
            }
        }
    }
    
    fun arrivedAtPickup(bookingId: String) {
        viewModelScope.launch {
            bookingRepository.updateBookingStatus(bookingId, BookingStatus.ARRIVED)
        }
    }
    
    fun startRide(bookingId: String) {
        viewModelScope.launch {
            bookingRepository.updateBookingStatus(bookingId, BookingStatus.IN_PROGRESS)
        }
    }
    
    fun completeRide(bookingId: String) {
        // Track this so refreshActiveRide won't reload it due to Firestore delay
        justCompletedRideId = bookingId
        
        // Clear active ride immediately to prevent it showing after navigation
        val completedRide = _uiState.value.activeRide
        _uiState.update { it.copy(activeRide = null) }
        
        viewModelScope.launch {
            bookingRepository.updateBookingStatus(bookingId, BookingStatus.COMPLETED)
            
            // Update today's stats
            completedRide?.let { booking ->
                _uiState.update {
                    it.copy(
                        todayEarnings = it.todayEarnings + (booking.finalFare ?: booking.estimatedFare),
                        todayTrips = it.todayTrips + 1
                    )
                }
            }
            
            // Clear the just-completed flag after a delay (let Firestore sync)
            kotlinx.coroutines.delay(3000)
            justCompletedRideId = null
        }
    }
    
    fun cancelRide(bookingId: String) {
        viewModelScope.launch {
            bookingRepository.cancelBooking(bookingId, "Driver cancelled")
            _uiState.update { it.copy(activeRide = null) }
        }
    }
    
    fun updateLocation(location: LatLng) {
        _uiState.update { it.copy(currentLocation = location) }
    }
    
    // ========== Fare Adjustment Functions ==========
    
    /**
     * Show the fare adjustment sheet
     */
    fun showFareAdjustment() {
        _uiState.update { 
            it.copy(
                showFareAdjustmentSheet = true,
                adjustedFare = "",
                adjustmentReason = ""
            )
        }
    }
    
    /**
     * Hide the fare adjustment sheet
     */
    fun hideFareAdjustment() {
        _uiState.update { it.copy(showFareAdjustmentSheet = false) }
    }
    
    /**
     * Update the adjusted fare amount
     */
    fun updateAdjustedFare(fare: String) {
        _uiState.update { it.copy(adjustedFare = fare) }
    }
    
    /**
     * Update the adjustment reason
     */
    fun updateAdjustmentReason(reason: String) {
        _uiState.update { it.copy(adjustmentReason = reason) }
    }
    
    /**
     * Apply quick night rate adjustment (typically 1.5x the original fare)
     */
    fun applyNightRate() {
        val booking = _uiState.value.activeRide ?: _uiState.value.pendingRequest ?: return
        val nightFare = booking.estimatedFare * 1.5 // 50% extra for night rate
        _uiState.update {
            it.copy(
                adjustedFare = String.format("%.2f", nightFare),
                adjustmentReason = "Night rate (9PM - 6AM)"
            )
        }
    }
    
    /**
     * Apply bad weather rate adjustment
     */
    fun applyBadWeatherRate() {
        val booking = _uiState.value.activeRide ?: _uiState.value.pendingRequest ?: return
        val weatherFare = booking.estimatedFare * 1.25 // 25% extra for bad weather
        _uiState.update {
            it.copy(
                adjustedFare = String.format("%.2f", weatherFare),
                adjustmentReason = "Bad weather conditions"
            )
        }
    }
    
    /**
     * Apply holiday rate adjustment
     */
    fun applyHolidayRate() {
        val booking = _uiState.value.activeRide ?: _uiState.value.pendingRequest ?: return
        val holidayFare = booking.estimatedFare * 1.5 // 50% extra for holidays
        _uiState.update {
            it.copy(
                adjustedFare = String.format("%.2f", holidayFare),
                adjustmentReason = "Holiday rate"
            )
        }
    }
    
    /**
     * Submit the fare adjustment to the rider
     */
    fun submitFareAdjustment() {
        val bookingId = _uiState.value.activeRide?.id ?: _uiState.value.pendingRequest?.id ?: return
        val fareText = _uiState.value.adjustedFare
        val reason = _uiState.value.adjustmentReason
        
        val adjustedFare = fareText.toDoubleOrNull() ?: return
        if (adjustedFare <= 0 || reason.isBlank()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val isNightRate = reason.contains("Night", ignoreCase = true)
            
            val result = bookingRepository.adjustFare(
                bookingId = bookingId,
                adjustedFare = adjustedFare,
                reason = reason,
                isNightRate = isNightRate
            )
            
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showFareAdjustmentSheet = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to adjust fare: ${error.message}"
                        )
                    }
                }
            )
        }
    }
}
