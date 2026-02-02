package com.boattaxie.app.data.repository;

import com.boattaxie.app.data.model.*;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import kotlinx.coroutines.flow.Flow;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000~\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0007\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\f\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\b\n\u0002\b\u000b\n\u0002\u0010 \n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\b\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J$\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u000e\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u000f\u0010\u0010J$\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u000e\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0012\u0010\u0010J>\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u000e\u001a\u00020\b2\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\b2\b\b\u0002\u0010\u0017\u001a\u00020\u0018H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0019\u0010\u001aJ0\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u000e\u001a\u00020\b2\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\bH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u001c\u0010\u001dJ\u000e\u0010\u001e\u001a\u00020\rH\u0086@\u00a2\u0006\u0002\u0010\u001fJ,\u0010 \u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u000e\u001a\u00020\b2\u0006\u0010!\u001a\u00020\u0015H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\"\u0010#J\u0084\u0001\u0010$\u001a\b\u0012\u0004\u0012\u00020%0\f2\u0006\u0010&\u001a\u00020\'2\u0006\u0010(\u001a\u00020)2\u0006\u0010*\u001a\u00020\b2\u0006\u0010+\u001a\u00020)2\u0006\u0010,\u001a\u00020\b2\u0006\u0010-\u001a\u00020.2\u0006\u0010/\u001a\u0002002\n\b\u0002\u00101\u001a\u0004\u0018\u00010\b2\n\b\u0002\u00102\u001a\u0004\u0018\u00010\b2\n\b\u0002\u00103\u001a\u0004\u0018\u00010\b2\n\b\u0002\u00104\u001a\u0004\u0018\u00010\bH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b5\u00106J\u0010\u00107\u001a\u0004\u0018\u00010%H\u0086@\u00a2\u0006\u0002\u0010\u001fJ\u0010\u00108\u001a\u0004\u0018\u00010%H\u0086@\u00a2\u0006\u0002\u0010\u001fJ\u0018\u00109\u001a\u0004\u0018\u00010%2\u0006\u0010\u000e\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\u0010J\u0010\u0010:\u001a\u0004\u0018\u00010%H\u0086@\u00a2\u0006\u0002\u0010\u001fJ\u001e\u0010;\u001a\b\u0012\u0004\u0012\u00020%0<2\b\b\u0002\u0010=\u001a\u000200H\u0086@\u00a2\u0006\u0002\u0010>J\u001e\u0010?\u001a\b\u0012\u0004\u0012\u00020%0<2\b\b\u0002\u0010=\u001a\u000200H\u0086@\u00a2\u0006\u0002\u0010>J\u0006\u0010@\u001a\u00020\u0018J\u0012\u0010A\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020%0<0BJ\u0016\u0010C\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010%0B2\u0006\u0010\u000e\u001a\u00020\bJ\u0016\u0010D\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010E0B2\u0006\u0010\u000e\u001a\u00020\bJ\u001a\u0010F\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020%0<0B2\u0006\u0010&\u001a\u00020\'JB\u0010G\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u000e\u001a\u00020\b2\u0006\u0010H\u001a\u00020.2\n\b\u0002\u0010I\u001a\u0004\u0018\u00010\b2\b\b\u0002\u0010J\u001a\u00020\u0018H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\bK\u0010LJ,\u0010M\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u000e\u001a\u00020\b2\u0006\u0010N\u001a\u00020OH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\bP\u0010QJ4\u0010R\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u000e\u001a\u00020\b2\u0006\u0010S\u001a\u00020\u00152\u0006\u0010T\u001a\u00020\u0015H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\bU\u0010VR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0007\u001a\u0004\u0018\u00010\b8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\t\u0010\n\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006W"}, d2 = {"Lcom/boattaxie/app/data/repository/BookingRepository;", "", "auth", "Lcom/google/firebase/auth/FirebaseAuth;", "firestore", "Lcom/google/firebase/firestore/FirebaseFirestore;", "(Lcom/google/firebase/auth/FirebaseAuth;Lcom/google/firebase/firestore/FirebaseFirestore;)V", "userId", "", "getUserId", "()Ljava/lang/String;", "acceptBooking", "Lkotlin/Result;", "", "bookingId", "acceptBooking-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "acceptFareAdjustment", "acceptFareAdjustment-gIAlu-s", "adjustFare", "adjustedFare", "", "reason", "isNightRate", "", "adjustFare-yxL6bBk", "(Ljava/lang/String;DLjava/lang/String;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "cancelBooking", "cancelBooking-0E7RQCE", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "clearAllPendingBookings", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "completeBooking", "finalFare", "completeBooking-0E7RQCE", "(Ljava/lang/String;DLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createBooking", "Lcom/boattaxie/app/data/model/Booking;", "vehicleType", "Lcom/boattaxie/app/data/model/VehicleType;", "pickupLocation", "Lcom/boattaxie/app/data/model/GeoLocation;", "pickupAddress", "destinationLocation", "destinationAddress", "estimatedDistance", "", "estimatedDuration", "", "requestedDriverId", "riderName", "riderPhoneNumber", "riderPhotoUrl", "createBooking-jLovISM", "(Lcom/boattaxie/app/data/model/VehicleType;Lcom/boattaxie/app/data/model/GeoLocation;Ljava/lang/String;Lcom/boattaxie/app/data/model/GeoLocation;Ljava/lang/String;FILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getActiveDriverBooking", "getActiveRiderBooking", "getBooking", "getDriverActiveBooking", "getDriverBookings", "", "limit", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getUserBookings", "isNightRateTime", "observeAllPendingBookings", "Lkotlinx/coroutines/flow/Flow;", "observeBooking", "observeDriverLocation", "Lcom/google/android/gms/maps/model/LatLng;", "observePendingBookings", "rateTrip", "rating", "review", "isDriverRating", "rateTrip-yxL6bBk", "(Ljava/lang/String;FLjava/lang/String;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateBookingStatus", "status", "Lcom/boattaxie/app/data/model/BookingStatus;", "updateBookingStatus-0E7RQCE", "(Ljava/lang/String;Lcom/boattaxie/app/data/model/BookingStatus;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateDriverLocation", "latitude", "longitude", "updateDriverLocation-BWLJW6A", "(Ljava/lang/String;DDLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
public final class BookingRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.auth.FirebaseAuth auth = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.firestore.FirebaseFirestore firestore = null;
    
    @javax.inject.Inject()
    public BookingRepository(@org.jetbrains.annotations.NotNull()
    com.google.firebase.auth.FirebaseAuth auth, @org.jetbrains.annotations.NotNull()
    com.google.firebase.firestore.FirebaseFirestore firestore) {
        super();
    }
    
    private final java.lang.String getUserId() {
        return null;
    }
    
    /**
     * Get a specific booking
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getBooking(@org.jetbrains.annotations.NotNull()
    java.lang.String bookingId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.boattaxie.app.data.model.Booking> $completion) {
        return null;
    }
    
    /**
     * Observe a booking in real-time
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.boattaxie.app.data.model.Booking> observeBooking(@org.jetbrains.annotations.NotNull()
    java.lang.String bookingId) {
        return null;
    }
    
    /**
     * Get user's booking history
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getUserBookings(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.boattaxie.app.data.model.Booking>> $completion) {
        return null;
    }
    
    /**
     * Get driver's booking history
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getDriverBookings(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.boattaxie.app.data.model.Booking>> $completion) {
        return null;
    }
    
    /**
     * Get driver's active booking (ACCEPTED, ARRIVED, or IN_PROGRESS)
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getDriverActiveBooking(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.boattaxie.app.data.model.Booking> $completion) {
        return null;
    }
    
    /**
     * Observe pending booking requests for drivers
     * This includes:
     * 1. General bookings (no specific driver requested) for the vehicle type
     * 2. Bookings specifically requested for this driver
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.boattaxie.app.data.model.Booking>> observePendingBookings(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VehicleType vehicleType) {
        return null;
    }
    
    /**
     * Observe ALL pending booking requests (for showing live counts on driver map)
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.boattaxie.app.data.model.Booking>> observeAllPendingBookings() {
        return null;
    }
    
    /**
     * Clear ALL bookings from Firebase (for cleaning up test/fake data)
     * This deletes everything - use with caution!
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object clearAllPendingBookings(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Get active booking for rider
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getActiveRiderBooking(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.boattaxie.app.data.model.Booking> $completion) {
        return null;
    }
    
    /**
     * Get active booking for driver
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getActiveDriverBooking(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.boattaxie.app.data.model.Booking> $completion) {
        return null;
    }
    
    /**
     * Observe driver location for a booking in real-time
     * Riders use this to see the driver moving on the map
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.google.android.gms.maps.model.LatLng> observeDriverLocation(@org.jetbrains.annotations.NotNull()
    java.lang.String bookingId) {
        return null;
    }
    
    /**
     * Check if current time is night rate hours (9PM - 6AM)
     */
    public final boolean isNightRateTime() {
        return false;
    }
}