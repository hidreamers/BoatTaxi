package com.boattaxie.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boattaxie.app.data.model.*
import com.boattaxie.app.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val userName: String = "User",
    val userType: UserType = UserType.RIDER,
    val canBeDriver: Boolean = false,
    val isVerifiedDriver: Boolean = false,
    val hasActiveSubscription: Boolean = false,
    val subscription: Subscription? = null,
    val recentTrips: List<Booking> = emptyList(),
    val featuredAds: List<Advertisement> = emptyList(),
    val errorMessage: String? = null,
    val switchToDriverMode: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val bookingRepository: BookingRepository,
    private val advertisementRepository: AdvertisementRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadUserData()
        observeSubscription()
        loadRecentTrips()
        loadFeaturedAds()
    }
    
    private fun loadUserData() {
        viewModelScope.launch {
            authRepository.observeCurrentUser().collect { user ->
                if (user != null) {
                    _uiState.update { 
                        it.copy(
                            userName = user.fullName.split(" ").firstOrNull() ?: "User",
                            userType = user.userType,
                            canBeDriver = user.canBeDriver,
                            isVerifiedDriver = user.verificationStatus == VerificationStatus.APPROVED
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Switch to driver mode - user can toggle between rider and driver
     */
    fun switchToDriverMode() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                // Check if user is verified as driver
                if (currentUser.verificationStatus == VerificationStatus.APPROVED) {
                    _uiState.update { it.copy(switchToDriverMode = true) }
                } else {
                    // User needs to complete driver verification first
                    _uiState.update { it.copy(errorMessage = "Complete driver verification first") }
                }
            }
        }
    }
    
    fun clearSwitchToDriverMode() {
        _uiState.update { it.copy(switchToDriverMode = false) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    private fun observeSubscription() {
        viewModelScope.launch {
            subscriptionRepository.observeSubscription().collect { subscription ->
                _uiState.update { 
                    it.copy(
                        hasActiveSubscription = subscription != null && 
                            SubscriptionHelper.isSubscriptionActive(subscription),
                        subscription = subscription
                    )
                }
            }
        }
    }
    
    private fun loadRecentTrips() {
        viewModelScope.launch {
            try {
                val trips = bookingRepository.getUserBookings(limit = 5)
                _uiState.update { it.copy(recentTrips = trips) }
            } catch (e: Exception) {
                // Silently fail for recent trips
            }
        }
    }
    
    private fun loadFeaturedAds() {
        viewModelScope.launch {
            try {
                // Load up to 20 real ads from all users
                val ads = advertisementRepository.getFeaturedAdvertisements(limit = 20)
                _uiState.update { it.copy(featuredAds = ads) }
            } catch (e: Exception) {
                // Silently fail for ads
            }
        }
    }
    
    fun onAdClick(adId: String) {
        viewModelScope.launch {
            advertisementRepository.recordClick(adId)
        }
    }
    
    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadRecentTrips()
        loadFeaturedAds()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
