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
    val switchToDriverMode: Boolean = false,
    val unratedBookingId: String? = null,
    // Real-time active users count
    val activeRiders: Int = 0,
    val activeDrivers: Int = 0,
    val totalActiveUsers: Int = 0,
    // News and Weather
    val newsArticles: List<NewsArticle> = emptyList(),
    val weather: WeatherData? = null,
    val weatherForecast: List<WeatherForecast> = emptyList(),
    val isLoadingNews: Boolean = false,
    val newsError: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val bookingRepository: BookingRepository,
    private val advertisementRepository: AdvertisementRepository,
    private val activeUsersRepository: ActiveUsersRepository,
    private val newsRepository: NewsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadUserData()
        observeSubscription()
        loadRecentTrips()
        loadFeaturedAds()
        trackActiveUser()
        observeActiveUsers()
        loadNewsAndWeather()
    }
    
    /**
     * Mark current user as active and track presence
     */
    private fun trackActiveUser() {
        viewModelScope.launch {
            authRepository.observeCurrentUser().collect { user ->
                if (user != null) {
                    val isDriver = user.userType == UserType.DRIVER
                    activeUsersRepository.setUserActive(isDriver)
                }
            }
        }
    }
    
    /**
     * Observe real-time active user count
     */
    private fun observeActiveUsers() {
        viewModelScope.launch {
            activeUsersRepository.getActiveUsersCount().collect { count ->
                _uiState.update {
                    it.copy(
                        activeRiders = count.riders,
                        activeDrivers = count.drivers,
                        totalActiveUsers = count.totalUsers
                    )
                }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        activeUsersRepository.setUserInactive()
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
    
    /**
     * Check if there's a completed booking that hasn't been rated yet
     */
    fun checkForUnratedBooking() {
        viewModelScope.launch {
            try {
                val unratedBooking = bookingRepository.getUnratedCompletedBooking()
                if (unratedBooking != null) {
                    android.util.Log.d("HomeVM", "Found unrated booking: ${unratedBooking.id}")
                    _uiState.update { it.copy(unratedBookingId = unratedBooking.id) }
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeVM", "Error checking for unrated booking", e)
            }
        }
    }
    
    fun clearUnratedBooking() {
        _uiState.update { it.copy(unratedBookingId = null) }
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
    
    // Track which ads have already had impressions recorded this session
    private val recordedImpressions = mutableSetOf<String>()
    
    fun recordAdImpression(adId: String) {
        // Only record one impression per ad per session to avoid over-counting
        if (adId !in recordedImpressions) {
            recordedImpressions.add(adId)
            viewModelScope.launch {
                advertisementRepository.recordImpression(adId)
            }
        }
    }
    
    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadRecentTrips()
        loadFeaturedAds()
        loadNewsAndWeather()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    /**
     * Load news articles and weather data
     */
    private fun loadNewsAndWeather() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingNews = true, newsError = null) }
            
            // Load news articles
            newsRepository.fetchAllNews().onSuccess { articles ->
                _uiState.update { it.copy(newsArticles = articles.take(10)) }
            }.onFailure { error ->
                _uiState.update { it.copy(newsError = error.message) }
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
