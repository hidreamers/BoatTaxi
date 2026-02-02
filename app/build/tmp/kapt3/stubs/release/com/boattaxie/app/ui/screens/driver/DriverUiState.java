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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0006\n\u0002\u0010$\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\bS\b\u0086\b\u0018\u00002\u00020\u0001B\u00cf\u0002\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\u0003\u0012\b\b\u0002\u0010\n\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f\u0012\b\b\u0002\u0010\r\u001a\u00020\u000e\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0010\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u000e\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u0013\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u000e\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0010\u0012\b\b\u0002\u0010\u0016\u001a\u00020\u000e\u0012\b\b\u0002\u0010\u0017\u001a\u00020\u000e\u0012\b\b\u0002\u0010\u0018\u001a\u00020\u000e\u0012\u0014\b\u0002\u0010\u0019\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u000e0\u001a\u0012\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u001c\u0012\n\b\u0002\u0010\u001d\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\u001e\u001a\u0004\u0018\u00010\u001c\u0012\u000e\b\u0002\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u001c0 \u0012\b\b\u0002\u0010!\u001a\u00020\u0003\u0012\b\b\u0002\u0010\"\u001a\u00020\u0010\u0012\b\b\u0002\u0010#\u001a\u00020\u0010\u0012\n\b\u0002\u0010$\u001a\u0004\u0018\u00010\u001c\u0012\b\b\u0002\u0010%\u001a\u00020\u0003\u0012\b\b\u0002\u0010&\u001a\u00020\u0003\u0012\b\b\u0002\u0010\'\u001a\u00020\u0006\u0012\b\b\u0002\u0010(\u001a\u00020\u0006\u0012\n\b\u0002\u0010)\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\u0002\u0010*J\t\u0010P\u001a\u00020\u0003H\u00c6\u0003J\t\u0010Q\u001a\u00020\u000eH\u00c6\u0003J\t\u0010R\u001a\u00020\u0013H\u00c6\u0003J\t\u0010S\u001a\u00020\u000eH\u00c6\u0003J\t\u0010T\u001a\u00020\u0010H\u00c6\u0003J\t\u0010U\u001a\u00020\u000eH\u00c6\u0003J\t\u0010V\u001a\u00020\u000eH\u00c6\u0003J\t\u0010W\u001a\u00020\u000eH\u00c6\u0003J\u0015\u0010X\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u000e0\u001aH\u00c6\u0003J\u000b\u0010Y\u001a\u0004\u0018\u00010\u001cH\u00c6\u0003J\u000b\u0010Z\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\t\u0010[\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\\\u001a\u0004\u0018\u00010\u001cH\u00c6\u0003J\u000f\u0010]\u001a\b\u0012\u0004\u0012\u00020\u001c0 H\u00c6\u0003J\t\u0010^\u001a\u00020\u0003H\u00c6\u0003J\t\u0010_\u001a\u00020\u0010H\u00c6\u0003J\t\u0010`\u001a\u00020\u0010H\u00c6\u0003J\u000b\u0010a\u001a\u0004\u0018\u00010\u001cH\u00c6\u0003J\t\u0010b\u001a\u00020\u0003H\u00c6\u0003J\t\u0010c\u001a\u00020\u0003H\u00c6\u0003J\t\u0010d\u001a\u00020\u0006H\u00c6\u0003J\t\u0010e\u001a\u00020\u0006H\u00c6\u0003J\t\u0010f\u001a\u00020\u0006H\u00c6\u0003J\u000b\u0010g\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\t\u0010h\u001a\u00020\bH\u00c6\u0003J\t\u0010i\u001a\u00020\u0003H\u00c6\u0003J\t\u0010j\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010k\u001a\u0004\u0018\u00010\fH\u00c6\u0003J\t\u0010l\u001a\u00020\u000eH\u00c6\u0003J\t\u0010m\u001a\u00020\u0010H\u00c6\u0003J\u00d3\u0002\u0010n\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\u00032\b\b\u0002\u0010\n\u001a\u00020\u00032\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f2\b\b\u0002\u0010\r\u001a\u00020\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u0010\u0011\u001a\u00020\u000e2\b\b\u0002\u0010\u0012\u001a\u00020\u00132\b\b\u0002\u0010\u0014\u001a\u00020\u000e2\b\b\u0002\u0010\u0015\u001a\u00020\u00102\b\b\u0002\u0010\u0016\u001a\u00020\u000e2\b\b\u0002\u0010\u0017\u001a\u00020\u000e2\b\b\u0002\u0010\u0018\u001a\u00020\u000e2\u0014\b\u0002\u0010\u0019\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u000e0\u001a2\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u001c2\n\b\u0002\u0010\u001d\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u001e\u001a\u0004\u0018\u00010\u001c2\u000e\b\u0002\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u001c0 2\b\b\u0002\u0010!\u001a\u00020\u00032\b\b\u0002\u0010\"\u001a\u00020\u00102\b\b\u0002\u0010#\u001a\u00020\u00102\n\b\u0002\u0010$\u001a\u0004\u0018\u00010\u001c2\b\b\u0002\u0010%\u001a\u00020\u00032\b\b\u0002\u0010&\u001a\u00020\u00032\b\b\u0002\u0010\'\u001a\u00020\u00062\b\b\u0002\u0010(\u001a\u00020\u00062\n\b\u0002\u0010)\u001a\u0004\u0018\u00010\u0006H\u00c6\u0001J\u0013\u0010o\u001a\u00020\u00032\b\u0010p\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010q\u001a\u00020\u0010H\u00d6\u0001J\t\u0010r\u001a\u00020\u0006H\u00d6\u0001R\u0013\u0010\u001d\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010,R\u0013\u0010\u001e\u001a\u0004\u0018\u00010\u001c\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010.R\u0011\u0010\'\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010,R\u0011\u0010(\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u0010,R\u0017\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u001c0 \u00a2\u0006\b\n\u0000\u001a\u0004\b1\u00102R\u0011\u0010\u0016\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b3\u00104R\u0011\u0010\"\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b5\u00106R\u0011\u0010\u0018\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b7\u00104R\u0013\u0010\u000b\u001a\u0004\u0018\u00010\f\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u00109R\u001d\u0010\u0019\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u000e0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b:\u0010;R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b<\u0010,R\u0013\u0010)\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b=\u0010,R\u0011\u0010\t\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b>\u0010?R\u0011\u0010\n\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b@\u0010?R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010?R\u0011\u0010&\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010?R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0004\u0010?R\u0011\u0010\u0011\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\bA\u00104R\u0013\u0010\u001b\u001a\u0004\u0018\u00010\u001c\u00a2\u0006\b\n\u0000\u001a\u0004\bB\u0010.R\u0011\u0010\u0012\u001a\u00020\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\bC\u0010DR\u0013\u0010$\u001a\u0004\u0018\u00010\u001c\u00a2\u0006\b\n\u0000\u001a\u0004\bE\u0010.R\u0011\u0010%\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bF\u0010?R\u0011\u0010!\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bG\u0010?R\u0011\u0010#\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\bH\u00106R\u0011\u0010\u0017\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\bI\u00104R\u0011\u0010\r\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\bJ\u00104R\u0011\u0010\u000f\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\bK\u00106R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\bL\u0010MR\u0011\u0010\u0014\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\bN\u00104R\u0011\u0010\u0015\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\bO\u00106\u00a8\u0006s"}, d2 = {"Lcom/boattaxie/app/ui/screens/driver/DriverUiState;", "", "isLoading", "", "isOnline", "driverName", "", "vehicleType", "Lcom/boattaxie/app/data/model/VehicleType;", "hasBoat", "hasTaxi", "currentLocation", "Lcom/google/android/gms/maps/model/LatLng;", "todayEarnings", "", "todayTrips", "", "onlineHours", "rating", "", "weekEarnings", "weekTrips", "baseFaresEarnings", "tipsEarnings", "bonusEarnings", "dailyEarnings", "", "pendingRequest", "Lcom/boattaxie/app/data/model/Booking;", "acceptedBookingId", "activeRide", "allPendingRequests", "", "showRequestsOnMap", "boatRequestCount", "taxiRequestCount", "selectedRequest", "showFareAdjustmentSheet", "isNightRateTime", "adjustedFare", "adjustmentReason", "errorMessage", "(ZZLjava/lang/String;Lcom/boattaxie/app/data/model/VehicleType;ZZLcom/google/android/gms/maps/model/LatLng;DIDFDIDDDLjava/util/Map;Lcom/boattaxie/app/data/model/Booking;Ljava/lang/String;Lcom/boattaxie/app/data/model/Booking;Ljava/util/List;ZIILcom/boattaxie/app/data/model/Booking;ZZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getAcceptedBookingId", "()Ljava/lang/String;", "getActiveRide", "()Lcom/boattaxie/app/data/model/Booking;", "getAdjustedFare", "getAdjustmentReason", "getAllPendingRequests", "()Ljava/util/List;", "getBaseFaresEarnings", "()D", "getBoatRequestCount", "()I", "getBonusEarnings", "getCurrentLocation", "()Lcom/google/android/gms/maps/model/LatLng;", "getDailyEarnings", "()Ljava/util/Map;", "getDriverName", "getErrorMessage", "getHasBoat", "()Z", "getHasTaxi", "getOnlineHours", "getPendingRequest", "getRating", "()F", "getSelectedRequest", "getShowFareAdjustmentSheet", "getShowRequestsOnMap", "getTaxiRequestCount", "getTipsEarnings", "getTodayEarnings", "getTodayTrips", "getVehicleType", "()Lcom/boattaxie/app/data/model/VehicleType;", "getWeekEarnings", "getWeekTrips", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component20", "component21", "component22", "component23", "component24", "component25", "component26", "component27", "component28", "component29", "component3", "component30", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_release"})
public final class DriverUiState {
    private final boolean isLoading = false;
    private final boolean isOnline = false;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String driverName = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.model.VehicleType vehicleType = null;
    private final boolean hasBoat = false;
    private final boolean hasTaxi = false;
    @org.jetbrains.annotations.Nullable()
    private final com.google.android.gms.maps.model.LatLng currentLocation = null;
    private final double todayEarnings = 0.0;
    private final int todayTrips = 0;
    private final double onlineHours = 0.0;
    private final float rating = 0.0F;
    private final double weekEarnings = 0.0;
    private final int weekTrips = 0;
    private final double baseFaresEarnings = 0.0;
    private final double tipsEarnings = 0.0;
    private final double bonusEarnings = 0.0;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, java.lang.Double> dailyEarnings = null;
    @org.jetbrains.annotations.Nullable()
    private final com.boattaxie.app.data.model.Booking pendingRequest = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String acceptedBookingId = null;
    @org.jetbrains.annotations.Nullable()
    private final com.boattaxie.app.data.model.Booking activeRide = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.boattaxie.app.data.model.Booking> allPendingRequests = null;
    private final boolean showRequestsOnMap = false;
    private final int boatRequestCount = 0;
    private final int taxiRequestCount = 0;
    @org.jetbrains.annotations.Nullable()
    private final com.boattaxie.app.data.model.Booking selectedRequest = null;
    private final boolean showFareAdjustmentSheet = false;
    private final boolean isNightRateTime = false;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String adjustedFare = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String adjustmentReason = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String errorMessage = null;
    
    public DriverUiState(boolean isLoading, boolean isOnline, @org.jetbrains.annotations.NotNull()
    java.lang.String driverName, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VehicleType vehicleType, boolean hasBoat, boolean hasTaxi, @org.jetbrains.annotations.Nullable()
    com.google.android.gms.maps.model.LatLng currentLocation, double todayEarnings, int todayTrips, double onlineHours, float rating, double weekEarnings, int weekTrips, double baseFaresEarnings, double tipsEarnings, double bonusEarnings, @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.Double> dailyEarnings, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.Booking pendingRequest, @org.jetbrains.annotations.Nullable()
    java.lang.String acceptedBookingId, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.Booking activeRide, @org.jetbrains.annotations.NotNull()
    java.util.List<com.boattaxie.app.data.model.Booking> allPendingRequests, boolean showRequestsOnMap, int boatRequestCount, int taxiRequestCount, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.Booking selectedRequest, boolean showFareAdjustmentSheet, boolean isNightRateTime, @org.jetbrains.annotations.NotNull()
    java.lang.String adjustedFare, @org.jetbrains.annotations.NotNull()
    java.lang.String adjustmentReason, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage) {
        super();
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    public final boolean isOnline() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDriverName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.VehicleType getVehicleType() {
        return null;
    }
    
    public final boolean getHasBoat() {
        return false;
    }
    
    public final boolean getHasTaxi() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.android.gms.maps.model.LatLng getCurrentLocation() {
        return null;
    }
    
    public final double getTodayEarnings() {
        return 0.0;
    }
    
    public final int getTodayTrips() {
        return 0;
    }
    
    public final double getOnlineHours() {
        return 0.0;
    }
    
    public final float getRating() {
        return 0.0F;
    }
    
    public final double getWeekEarnings() {
        return 0.0;
    }
    
    public final int getWeekTrips() {
        return 0;
    }
    
    public final double getBaseFaresEarnings() {
        return 0.0;
    }
    
    public final double getTipsEarnings() {
        return 0.0;
    }
    
    public final double getBonusEarnings() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.String, java.lang.Double> getDailyEarnings() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.Booking getPendingRequest() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getAcceptedBookingId() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.Booking getActiveRide() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.Booking> getAllPendingRequests() {
        return null;
    }
    
    public final boolean getShowRequestsOnMap() {
        return false;
    }
    
    public final int getBoatRequestCount() {
        return 0;
    }
    
    public final int getTaxiRequestCount() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.Booking getSelectedRequest() {
        return null;
    }
    
    public final boolean getShowFareAdjustmentSheet() {
        return false;
    }
    
    public final boolean isNightRateTime() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAdjustedFare() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAdjustmentReason() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getErrorMessage() {
        return null;
    }
    
    public DriverUiState() {
        super();
    }
    
    public final boolean component1() {
        return false;
    }
    
    public final double component10() {
        return 0.0;
    }
    
    public final float component11() {
        return 0.0F;
    }
    
    public final double component12() {
        return 0.0;
    }
    
    public final int component13() {
        return 0;
    }
    
    public final double component14() {
        return 0.0;
    }
    
    public final double component15() {
        return 0.0;
    }
    
    public final double component16() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.String, java.lang.Double> component17() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.Booking component18() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component19() {
        return null;
    }
    
    public final boolean component2() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.Booking component20() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.Booking> component21() {
        return null;
    }
    
    public final boolean component22() {
        return false;
    }
    
    public final int component23() {
        return 0;
    }
    
    public final int component24() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.Booking component25() {
        return null;
    }
    
    public final boolean component26() {
        return false;
    }
    
    public final boolean component27() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component28() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component29() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component30() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.VehicleType component4() {
        return null;
    }
    
    public final boolean component5() {
        return false;
    }
    
    public final boolean component6() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.android.gms.maps.model.LatLng component7() {
        return null;
    }
    
    public final double component8() {
        return 0.0;
    }
    
    public final int component9() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.ui.screens.driver.DriverUiState copy(boolean isLoading, boolean isOnline, @org.jetbrains.annotations.NotNull()
    java.lang.String driverName, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VehicleType vehicleType, boolean hasBoat, boolean hasTaxi, @org.jetbrains.annotations.Nullable()
    com.google.android.gms.maps.model.LatLng currentLocation, double todayEarnings, int todayTrips, double onlineHours, float rating, double weekEarnings, int weekTrips, double baseFaresEarnings, double tipsEarnings, double bonusEarnings, @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.Double> dailyEarnings, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.Booking pendingRequest, @org.jetbrains.annotations.Nullable()
    java.lang.String acceptedBookingId, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.Booking activeRide, @org.jetbrains.annotations.NotNull()
    java.util.List<com.boattaxie.app.data.model.Booking> allPendingRequests, boolean showRequestsOnMap, int boatRequestCount, int taxiRequestCount, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.Booking selectedRequest, boolean showFareAdjustmentSheet, boolean isNightRateTime, @org.jetbrains.annotations.NotNull()
    java.lang.String adjustedFare, @org.jetbrains.annotations.NotNull()
    java.lang.String adjustmentReason, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}