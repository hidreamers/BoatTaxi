package com.boattaxie.app.ui.screens.driver;

import android.annotation.SuppressLint;
import android.os.Looper;
import androidx.lifecycle.ViewModel;
import com.boattaxie.app.data.model.*;
import com.boattaxie.app.data.repository.AuthRepository;
import com.boattaxie.app.data.repository.BookingRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.*;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0017\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u000e\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\rJ\u0006\u0010\u0017\u001a\u00020\u0015J\u0006\u0010\u0018\u001a\u00020\u0015J\u0006\u0010\u0019\u001a\u00020\u0015J\u0006\u0010\u001a\u001a\u00020\u0015J\u000e\u0010\u001b\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\rJ\u000e\u0010\u001c\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\rJ\b\u0010\u001d\u001a\u00020\u0015H\u0002J\u0006\u0010\u001e\u001a\u00020\u0015J\u0006\u0010\u001f\u001a\u00020\u0015J\u0006\u0010 \u001a\u00020\u0015J\u000e\u0010!\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\rJ\u000e\u0010\"\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\rJ\b\u0010#\u001a\u00020\u0015H\u0002J\u0006\u0010$\u001a\u00020\u0015J\u000e\u0010%\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\rJ\b\u0010&\u001a\u00020\u0015H\u0002J\b\u0010\'\u001a\u00020\u0015H\u0002J\b\u0010(\u001a\u00020\u0015H\u0002J\b\u0010)\u001a\u00020\u0015H\u0014J\u0006\u0010*\u001a\u00020\u0015J\u0010\u0010+\u001a\u00020\u00152\b\u0010,\u001a\u0004\u0018\u00010-J\u0006\u0010.\u001a\u00020\u0015J\b\u0010/\u001a\u00020\u0015H\u0003J\b\u00100\u001a\u00020\u0015H\u0002J\u0010\u00101\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\rH\u0002J\u000e\u00102\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\rJ\b\u00103\u001a\u00020\u0015H\u0002J\b\u00104\u001a\u00020\u0015H\u0002J\u0006\u00105\u001a\u00020\u0015J\u000e\u00106\u001a\u00020\u00152\u0006\u00107\u001a\u000208J\u0006\u00109\u001a\u00020\u0015J\u0006\u0010:\u001a\u00020\u0015J\u000e\u0010;\u001a\u00020\u00152\u0006\u0010<\u001a\u00020\rJ\u000e\u0010=\u001a\u00020\u00152\u0006\u0010>\u001a\u00020\rJ\u000e\u0010?\u001a\u00020\u00152\u0006\u0010@\u001a\u00020AR\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013\u00a8\u0006B"}, d2 = {"Lcom/boattaxie/app/ui/screens/driver/DriverViewModel;", "Landroidx/lifecycle/ViewModel;", "authRepository", "Lcom/boattaxie/app/data/repository/AuthRepository;", "bookingRepository", "Lcom/boattaxie/app/data/repository/BookingRepository;", "fusedLocationClient", "Lcom/google/android/gms/location/FusedLocationProviderClient;", "(Lcom/boattaxie/app/data/repository/AuthRepository;Lcom/boattaxie/app/data/repository/BookingRepository;Lcom/google/android/gms/location/FusedLocationProviderClient;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/boattaxie/app/ui/screens/driver/DriverUiState;", "justCompletedRideId", "", "locationCallback", "Lcom/google/android/gms/location/LocationCallback;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "acceptBooking", "", "bookingId", "acceptSelectedRequest", "applyBadWeatherRate", "applyHolidayRate", "applyNightRate", "arrivedAtPickup", "cancelRide", "checkNightRate", "clearAcceptedBooking", "clearActiveRide", "clearOldPendingBookings", "completeRide", "declineBooking", "getCurrentLocation", "hideFareAdjustment", "loadActiveRide", "loadActiveRideOnInit", "loadDriverData", "loadOnlineStatus", "onCleared", "refreshActiveRide", "selectRequest", "booking", "Lcom/boattaxie/app/data/model/Booking;", "showFareAdjustment", "startContinuousLocationUpdates", "startListeningForRides", "startLocationUpdates", "startRide", "stopContinuousLocationUpdates", "stopListeningForRides", "submitFareAdjustment", "switchVehicleType", "vehicleType", "Lcom/boattaxie/app/data/model/VehicleType;", "toggleOnlineStatus", "toggleShowRequestsOnMap", "updateAdjustedFare", "fare", "updateAdjustmentReason", "reason", "updateLocation", "location", "Lcom/google/android/gms/maps/model/LatLng;", "app_release"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class DriverViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.repository.AuthRepository authRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.repository.BookingRepository bookingRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.boattaxie.app.ui.screens.driver.DriverUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.ui.screens.driver.DriverUiState> uiState = null;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String justCompletedRideId;
    @org.jetbrains.annotations.Nullable()
    private com.google.android.gms.location.LocationCallback locationCallback;
    
    @javax.inject.Inject()
    public DriverViewModel(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.repository.AuthRepository authRepository, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.repository.BookingRepository bookingRepository, @org.jetbrains.annotations.NotNull()
    com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.ui.screens.driver.DriverUiState> getUiState() {
        return null;
    }
    
    private final void loadOnlineStatus() {
    }
    
    /**
     * Refresh active ride - call this when screen becomes visible
     */
    public final void refreshActiveRide() {
    }
    
    private final void loadActiveRideOnInit() {
    }
    
    private final void checkNightRate() {
    }
    
    private final void loadDriverData() {
    }
    
    private final void getCurrentLocation() {
    }
    
    /**
     * Start continuous GPS location updates when driver goes online
     * Updates location every 3 seconds for smooth map tracking
     */
    @android.annotation.SuppressLint(value = {"MissingPermission"})
    private final void startContinuousLocationUpdates() {
    }
    
    /**
     * Stop continuous location updates when driver goes offline
     */
    private final void stopContinuousLocationUpdates() {
    }
    
    public final void toggleOnlineStatus() {
    }
    
    /**
     * Switch between boat and taxi mode (for multi-vehicle drivers)
     */
    public final void switchVehicleType(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VehicleType vehicleType) {
    }
    
    @java.lang.Override()
    protected void onCleared() {
    }
    
    private final void startListeningForRides() {
    }
    
    /**
     * Clear all old test/pending bookings from Firebase (for testing)
     */
    public final void clearOldPendingBookings() {
    }
    
    public final void toggleShowRequestsOnMap() {
    }
    
    public final void selectRequest(@org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.Booking booking) {
    }
    
    public final void acceptSelectedRequest() {
    }
    
    private final void stopListeningForRides() {
    }
    
    public final void acceptBooking(@org.jetbrains.annotations.NotNull()
    java.lang.String bookingId) {
    }
    
    public final void declineBooking(@org.jetbrains.annotations.NotNull()
    java.lang.String bookingId) {
    }
    
    public final void clearAcceptedBooking() {
    }
    
    public final void clearActiveRide() {
    }
    
    public final void loadActiveRide(@org.jetbrains.annotations.NotNull()
    java.lang.String bookingId) {
    }
    
    private final void startLocationUpdates(java.lang.String bookingId) {
    }
    
    public final void arrivedAtPickup(@org.jetbrains.annotations.NotNull()
    java.lang.String bookingId) {
    }
    
    public final void startRide(@org.jetbrains.annotations.NotNull()
    java.lang.String bookingId) {
    }
    
    public final void completeRide(@org.jetbrains.annotations.NotNull()
    java.lang.String bookingId) {
    }
    
    public final void cancelRide(@org.jetbrains.annotations.NotNull()
    java.lang.String bookingId) {
    }
    
    public final void updateLocation(@org.jetbrains.annotations.NotNull()
    com.google.android.gms.maps.model.LatLng location) {
    }
    
    /**
     * Show the fare adjustment sheet
     */
    public final void showFareAdjustment() {
    }
    
    /**
     * Hide the fare adjustment sheet
     */
    public final void hideFareAdjustment() {
    }
    
    /**
     * Update the adjusted fare amount
     */
    public final void updateAdjustedFare(@org.jetbrains.annotations.NotNull()
    java.lang.String fare) {
    }
    
    /**
     * Update the adjustment reason
     */
    public final void updateAdjustmentReason(@org.jetbrains.annotations.NotNull()
    java.lang.String reason) {
    }
    
    /**
     * Apply quick night rate adjustment (typically 1.5x the original fare)
     */
    public final void applyNightRate() {
    }
    
    /**
     * Apply bad weather rate adjustment
     */
    public final void applyBadWeatherRate() {
    }
    
    /**
     * Apply holiday rate adjustment
     */
    public final void applyHolidayRate() {
    }
    
    /**
     * Submit the fare adjustment to the rider
     */
    public final void submitFareAdjustment() {
    }
}