package com.boattaxie.app.ui.screens.booking;

import android.location.Geocoder;
import androidx.lifecycle.ViewModel;
import com.boattaxie.app.data.model.*;
import com.boattaxie.app.data.repository.AdvertisementRepository;
import com.boattaxie.app.data.repository.AuthRepository;
import com.boattaxie.app.data.repository.BookingRepository;
import com.boattaxie.app.data.repository.SubscriptionRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.*;
import java.util.*;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0086\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u0006\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0016\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B7\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u00a2\u0006\u0002\u0010\u000eJ\u0006\u0010\u0016\u001a\u00020\u0017J\u0018\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001bH\u0002J\u0006\u0010\u001d\u001a\u00020\u0017J\u000e\u0010\u001e\u001a\u00020\u00172\u0006\u0010\u001f\u001a\u00020 J\b\u0010!\u001a\u00020\u0017H\u0002J\u0006\u0010\"\u001a\u00020\u0017J\u0006\u0010#\u001a\u00020\u0017J\u0006\u0010$\u001a\u00020\u0017J\u0006\u0010%\u001a\u00020\u0017J\u0006\u0010&\u001a\u00020\u0017J\u0006\u0010\'\u001a\u00020\u0017J\u0006\u0010(\u001a\u00020\u0017J\u0006\u0010)\u001a\u00020\u0017J\u0006\u0010*\u001a\u00020\u0017J\u0006\u0010+\u001a\u00020\u0017J\b\u0010,\u001a\u00020\u0017H\u0002J\b\u0010-\u001a\u00020\u0017H\u0002J\u000e\u0010.\u001a\u00020\u00172\u0006\u0010\u001f\u001a\u00020 J\b\u0010/\u001a\u00020\u0017H\u0002J\u000e\u00100\u001a\u00020\u00172\u0006\u0010\u001f\u001a\u00020 J\u0010\u00101\u001a\u00020\u00172\u0006\u0010\u001f\u001a\u00020 H\u0002J\b\u00102\u001a\u00020\u0017H\u0002J\u000e\u00103\u001a\u00020\u00172\u0006\u00104\u001a\u00020\u001bJ\u000e\u00105\u001a\u00020\u00172\u0006\u00106\u001a\u000207J\u0006\u00108\u001a\u00020\u0017J\u000e\u00109\u001a\u00020\u00172\u0006\u0010:\u001a\u00020;J\u0006\u0010<\u001a\u00020\u0017J\u0010\u0010=\u001a\u00020\u00172\b\u0010:\u001a\u0004\u0018\u00010;J\u0010\u0010>\u001a\u00020\u00172\b\u0010?\u001a\u0004\u0018\u00010@J\u0016\u0010A\u001a\u00020\u00172\u0006\u0010B\u001a\u00020\u001b2\u0006\u0010C\u001a\u00020 J\u0016\u0010D\u001a\u00020\u00172\u0006\u0010E\u001a\u00020 2\u0006\u0010B\u001a\u00020\u001bJ\u001e\u0010F\u001a\u00020\u00172\u0006\u0010E\u001a\u00020 2\u0006\u0010G\u001a\u00020 2\u0006\u00106\u001a\u000207J\u000e\u0010H\u001a\u00020\u00172\u0006\u00106\u001a\u000207J\u0016\u0010I\u001a\u00020\u00172\u0006\u0010E\u001a\u00020 2\u0006\u0010B\u001a\u00020\u001bJ\u0016\u0010J\u001a\u00020\u00172\u0006\u0010K\u001a\u00020 2\u0006\u0010L\u001a\u00020 J\u000e\u0010M\u001a\u00020\u00172\u0006\u0010N\u001a\u00020OJ\u0006\u0010P\u001a\u00020\u0017J&\u0010Q\u001a\u00020\u00172\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010R\u001a\u00020S2\u0006\u0010T\u001a\u00020 2\u0006\u0010U\u001a\u00020\u0019J\u0006\u0010V\u001a\u00020\u0017J\u0006\u0010W\u001a\u00020\u0017R\u0014\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00110\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00110\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015\u00a8\u0006X"}, d2 = {"Lcom/boattaxie/app/ui/screens/booking/BookingViewModel;", "Landroidx/lifecycle/ViewModel;", "bookingRepository", "Lcom/boattaxie/app/data/repository/BookingRepository;", "advertisementRepository", "Lcom/boattaxie/app/data/repository/AdvertisementRepository;", "authRepository", "Lcom/boattaxie/app/data/repository/AuthRepository;", "subscriptionRepository", "Lcom/boattaxie/app/data/repository/SubscriptionRepository;", "fusedLocationClient", "Lcom/google/android/gms/location/FusedLocationProviderClient;", "geocoder", "Landroid/location/Geocoder;", "(Lcom/boattaxie/app/data/repository/BookingRepository;Lcom/boattaxie/app/data/repository/AdvertisementRepository;Lcom/boattaxie/app/data/repository/AuthRepository;Lcom/boattaxie/app/data/repository/SubscriptionRepository;Lcom/google/android/gms/location/FusedLocationProviderClient;Landroid/location/Geocoder;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/boattaxie/app/ui/screens/booking/BookingUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "acceptAdjustedFare", "", "calculateDistance", "", "from", "Lcom/google/android/gms/maps/model/LatLng;", "to", "calculateFare", "cancelBooking", "bookingId", "", "checkSubscriptionStatus", "clearActiveBooking", "clearError", "clearLocations", "clearRequestedDriver", "closeLocationSearch", "confirmBooking", "declineAdjustedFare", "dismissFareAdjustmentDialog", "dismissSubscriptionRequired", "getCurrentLocation", "loadActiveBooking", "loadAdsForMap", "loadCompletedBooking", "loadUserResidencyStatus", "observeBooking", "observeBookingUpdates", "observeOnlineDrivers", "onMapTapped", "latLng", "openLocationSearch", "isPickup", "", "refreshAdsForMap", "requestSpecificDriver", "driver", "Lcom/boattaxie/app/data/model/User;", "resetToPickupMode", "selectDriver", "selectMapAd", "ad", "Lcom/boattaxie/app/data/model/Advertisement;", "setDropoffFromAd", "location", "name", "setDropoffLocation", "address", "setLocationFromSearch", "placeId", "setMapTapMode", "setPickupLocation", "setRequestedDriver", "driverId", "driverName", "setVehicleType", "type", "Lcom/boattaxie/app/data/model/VehicleType;", "showSubscriptionRequired", "submitRating", "rating", "", "review", "tip", "toggleMapTapMode", "toggleShowOnlineDrivers", "app_release"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class BookingViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.repository.BookingRepository bookingRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.repository.AdvertisementRepository advertisementRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.repository.AuthRepository authRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.repository.SubscriptionRepository subscriptionRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient = null;
    @org.jetbrains.annotations.NotNull()
    private final android.location.Geocoder geocoder = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.boattaxie.app.ui.screens.booking.BookingUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.ui.screens.booking.BookingUiState> uiState = null;
    
    @javax.inject.Inject()
    public BookingViewModel(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.repository.BookingRepository bookingRepository, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.repository.AdvertisementRepository advertisementRepository, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.repository.AuthRepository authRepository, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.repository.SubscriptionRepository subscriptionRepository, @org.jetbrains.annotations.NotNull()
    com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient, @org.jetbrains.annotations.NotNull()
    android.location.Geocoder geocoder) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.ui.screens.booking.BookingUiState> getUiState() {
        return null;
    }
    
    /**
     * Check if user has an active subscription
     */
    private final void checkSubscriptionStatus() {
    }
    
    /**
     * Show subscription required dialog
     */
    public final void showSubscriptionRequired() {
    }
    
    public final void dismissSubscriptionRequired() {
    }
    
    /**
     * Load any active booking the rider has (restored from Firebase)
     */
    private final void loadActiveBooking() {
    }
    
    /**
     * Observe booking updates in real-time
     */
    private final void observeBookingUpdates(java.lang.String bookingId) {
    }
    
    /**
     * Observe online drivers in real-time
     */
    private final void observeOnlineDrivers() {
    }
    
    public final void toggleShowOnlineDrivers() {
    }
    
    public final void selectDriver(@org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.User driver) {
    }
    
    public final void requestSpecificDriver(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.User driver) {
    }
    
    public final void clearRequestedDriver() {
    }
    
    /**
     * Clear pickup and dropoff locations - reset for new booking
     */
    public final void clearLocations() {
    }
    
    /**
     * Set a pre-selected driver from trip history
     * When coming from trip history, the user just needs to set locations
     * then the request will be sent to this driver
     */
    public final void setRequestedDriver(@org.jetbrains.annotations.NotNull()
    java.lang.String driverId, @org.jetbrains.annotations.NotNull()
    java.lang.String driverName) {
    }
    
    /**
     * Load user's residency status to determine pricing
     */
    private final void loadUserResidencyStatus() {
    }
    
    /**
     * Load advertisements with locations to show on map
     */
    private final void loadAdsForMap() {
    }
    
    /**
     * Public function to refresh ads (called from RideTrackingScreen)
     */
    public final void refreshAdsForMap() {
    }
    
    /**
     * Select an ad marker on the map (track click)
     */
    public final void selectMapAd(@org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.Advertisement ad) {
    }
    
    /**
     * Set dropoff location from ad marker
     */
    public final void setDropoffFromAd(@org.jetbrains.annotations.NotNull()
    com.google.android.gms.maps.model.LatLng location, @org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
    
    public final void setVehicleType(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VehicleType type) {
    }
    
    /**
     * Reset to pickup mode - call this when opening the booking screen
     */
    public final void resetToPickupMode() {
    }
    
    public final void getCurrentLocation() {
    }
    
    public final void openLocationSearch(boolean isPickup) {
    }
    
    public final void setPickupLocation(@org.jetbrains.annotations.NotNull()
    java.lang.String address, @org.jetbrains.annotations.NotNull()
    com.google.android.gms.maps.model.LatLng location) {
    }
    
    public final void setDropoffLocation(@org.jetbrains.annotations.NotNull()
    java.lang.String address, @org.jetbrains.annotations.NotNull()
    com.google.android.gms.maps.model.LatLng location) {
    }
    
    public final void closeLocationSearch() {
    }
    
    public final void toggleMapTapMode() {
    }
    
    public final void setMapTapMode(boolean isPickup) {
    }
    
    public final void onMapTapped(@org.jetbrains.annotations.NotNull()
    com.google.android.gms.maps.model.LatLng latLng) {
    }
    
    public final void setLocationFromSearch(@org.jetbrains.annotations.NotNull()
    java.lang.String address, @org.jetbrains.annotations.NotNull()
    java.lang.String placeId, boolean isPickup) {
    }
    
    public final void calculateFare() {
    }
    
    private final double calculateDistance(com.google.android.gms.maps.model.LatLng from, com.google.android.gms.maps.model.LatLng to) {
        return 0.0;
    }
    
    public final void confirmBooking() {
    }
    
    public final void observeBooking(@org.jetbrains.annotations.NotNull()
    java.lang.String bookingId) {
    }
    
    public final void loadCompletedBooking(@org.jetbrains.annotations.NotNull()
    java.lang.String bookingId) {
    }
    
    public final void cancelBooking(@org.jetbrains.annotations.NotNull()
    java.lang.String bookingId) {
    }
    
    /**
     * Clear any stale active booking that might be stuck
     */
    public final void clearActiveBooking() {
    }
    
    public final void submitRating(@org.jetbrains.annotations.NotNull()
    java.lang.String bookingId, int rating, @org.jetbrains.annotations.NotNull()
    java.lang.String review, double tip) {
    }
    
    public final void clearError() {
    }
    
    /**
     * Accept the driver's adjusted fare
     */
    public final void acceptAdjustedFare() {
    }
    
    /**
     * Decline the driver's adjusted fare - cancels the booking
     */
    public final void declineAdjustedFare() {
    }
    
    /**
     * Dismiss the fare adjustment dialog (user can still see it in booking details)
     */
    public final void dismissFareAdjustmentDialog() {
    }
}