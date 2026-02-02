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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\bb\b\u0086\b\u0018\u00002\u00020\u0001B\u00c1\u0003\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\t\u001a\u00020\u0003\u0012\b\b\u0002\u0010\n\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u000b\u001a\u00020\u0003\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\r\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\r\u0012\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u0010\u0012\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\r\u0012\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u0010\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u0017\u0012\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u0017\u0012\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u001a\u0012\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u0017\u0012\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u0010\u0012\b\b\u0002\u0010\u001d\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u001e\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u001f\u001a\u0004\u0018\u00010\u0010\u0012\n\b\u0002\u0010 \u001a\u0004\u0018\u00010!\u0012\n\b\u0002\u0010\"\u001a\u0004\u0018\u00010!\u0012\n\b\u0002\u0010#\u001a\u0004\u0018\u00010\r\u0012\n\b\u0002\u0010$\u001a\u0004\u0018\u00010\u001a\u0012\n\b\u0002\u0010%\u001a\u0004\u0018\u00010\u001a\u0012\u000e\b\u0002\u0010&\u001a\b\u0012\u0004\u0012\u00020(0\'\u0012\n\b\u0002\u0010)\u001a\u0004\u0018\u00010(\u0012\u000e\b\u0002\u0010*\u001a\b\u0012\u0004\u0012\u00020+0\'\u0012\b\b\u0002\u0010,\u001a\u00020\u001a\u0012\b\b\u0002\u0010-\u001a\u00020\u001a\u0012\b\b\u0002\u0010.\u001a\u00020\u0003\u0012\n\b\u0002\u0010/\u001a\u0004\u0018\u00010+\u0012\n\b\u0002\u00100\u001a\u0004\u0018\u00010\u0010\u0012\n\b\u0002\u00101\u001a\u0004\u0018\u00010\u0010\u0012\n\b\u0002\u00102\u001a\u0004\u0018\u00010\u0010\u00a2\u0006\u0002\u00103J\t\u0010`\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010a\u001a\u0004\u0018\u00010\rH\u00c6\u0003J\u000b\u0010b\u001a\u0004\u0018\u00010\u0010H\u00c6\u0003J\u000b\u0010c\u001a\u0004\u0018\u00010\rH\u00c6\u0003J\u000b\u0010d\u001a\u0004\u0018\u00010\u0010H\u00c6\u0003J\t\u0010e\u001a\u00020\u0003H\u00c6\u0003J\t\u0010f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010g\u001a\u00020\u0003H\u00c6\u0003J\u0010\u0010h\u001a\u0004\u0018\u00010\u0017H\u00c6\u0003\u00a2\u0006\u0002\u0010BJ\u0010\u0010i\u001a\u0004\u0018\u00010\u0017H\u00c6\u0003\u00a2\u0006\u0002\u0010BJ\u0010\u0010j\u001a\u0004\u0018\u00010\u001aH\u00c6\u0003\u00a2\u0006\u0002\u0010JJ\t\u0010k\u001a\u00020\u0003H\u00c6\u0003J\u0010\u0010l\u001a\u0004\u0018\u00010\u0017H\u00c6\u0003\u00a2\u0006\u0002\u0010BJ\u000b\u0010m\u001a\u0004\u0018\u00010\u0010H\u00c6\u0003J\t\u0010n\u001a\u00020\u0003H\u00c6\u0003J\t\u0010o\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010p\u001a\u0004\u0018\u00010\u0010H\u00c6\u0003J\u000b\u0010q\u001a\u0004\u0018\u00010!H\u00c6\u0003J\u000b\u0010r\u001a\u0004\u0018\u00010!H\u00c6\u0003J\u000b\u0010s\u001a\u0004\u0018\u00010\rH\u00c6\u0003J\u0010\u0010t\u001a\u0004\u0018\u00010\u001aH\u00c6\u0003\u00a2\u0006\u0002\u0010JJ\u0010\u0010u\u001a\u0004\u0018\u00010\u001aH\u00c6\u0003\u00a2\u0006\u0002\u0010JJ\t\u0010v\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010w\u001a\b\u0012\u0004\u0012\u00020(0\'H\u00c6\u0003J\u000b\u0010x\u001a\u0004\u0018\u00010(H\u00c6\u0003J\u000f\u0010y\u001a\b\u0012\u0004\u0012\u00020+0\'H\u00c6\u0003J\t\u0010z\u001a\u00020\u001aH\u00c6\u0003J\t\u0010{\u001a\u00020\u001aH\u00c6\u0003J\t\u0010|\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010}\u001a\u0004\u0018\u00010+H\u00c6\u0003J\u000b\u0010~\u001a\u0004\u0018\u00010\u0010H\u00c6\u0003J\u000b\u0010\u007f\u001a\u0004\u0018\u00010\u0010H\u00c6\u0003J\f\u0010\u0080\u0001\u001a\u0004\u0018\u00010\u0010H\u00c6\u0003J\n\u0010\u0081\u0001\u001a\u00020\u0007H\u00c6\u0003J\n\u0010\u0082\u0001\u001a\u00020\u0003H\u00c6\u0003J\n\u0010\u0083\u0001\u001a\u00020\u0003H\u00c6\u0003J\n\u0010\u0084\u0001\u001a\u00020\u0003H\u00c6\u0003J\n\u0010\u0085\u0001\u001a\u00020\u0003H\u00c6\u0003J\f\u0010\u0086\u0001\u001a\u0004\u0018\u00010\rH\u00c6\u0003J\u00cc\u0003\u0010\u0087\u0001\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\u00032\b\b\u0002\u0010\n\u001a\u00020\u00032\b\b\u0002\u0010\u000b\u001a\u00020\u00032\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\r2\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\r2\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u00102\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\r2\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u00102\b\b\u0002\u0010\u0013\u001a\u00020\u00032\b\b\u0002\u0010\u0014\u001a\u00020\u00032\b\b\u0002\u0010\u0015\u001a\u00020\u00032\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u00172\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u00172\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u001a2\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u00172\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u00102\b\b\u0002\u0010\u001d\u001a\u00020\u00032\b\b\u0002\u0010\u001e\u001a\u00020\u00032\n\b\u0002\u0010\u001f\u001a\u0004\u0018\u00010\u00102\n\b\u0002\u0010 \u001a\u0004\u0018\u00010!2\n\b\u0002\u0010\"\u001a\u0004\u0018\u00010!2\n\b\u0002\u0010#\u001a\u0004\u0018\u00010\r2\n\b\u0002\u0010$\u001a\u0004\u0018\u00010\u001a2\n\b\u0002\u0010%\u001a\u0004\u0018\u00010\u001a2\u000e\b\u0002\u0010&\u001a\b\u0012\u0004\u0012\u00020(0\'2\n\b\u0002\u0010)\u001a\u0004\u0018\u00010(2\u000e\b\u0002\u0010*\u001a\b\u0012\u0004\u0012\u00020+0\'2\b\b\u0002\u0010,\u001a\u00020\u001a2\b\b\u0002\u0010-\u001a\u00020\u001a2\b\b\u0002\u0010.\u001a\u00020\u00032\n\b\u0002\u0010/\u001a\u0004\u0018\u00010+2\n\b\u0002\u00100\u001a\u0004\u0018\u00010\u00102\n\b\u0002\u00101\u001a\u0004\u0018\u00010\u00102\n\b\u0002\u00102\u001a\u0004\u0018\u00010\u0010H\u00c6\u0001\u00a2\u0006\u0003\u0010\u0088\u0001J\u0015\u0010\u0089\u0001\u001a\u00020\u00032\t\u0010\u008a\u0001\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\n\u0010\u008b\u0001\u001a\u00020\u001aH\u00d6\u0001J\n\u0010\u008c\u0001\u001a\u00020\u0010H\u00d6\u0001R\u0013\u0010 \u001a\u0004\u0018\u00010!\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u00105R\u0017\u0010&\u001a\b\u0012\u0004\u0012\u00020(0\'\u00a2\u0006\b\n\u0000\u001a\u0004\b6\u00107R\u0011\u0010,\u001a\u00020\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u00109R\u0013\u0010\u001f\u001a\u0004\u0018\u00010\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b:\u0010;R\u0011\u0010\t\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b<\u0010=R\u0013\u0010\"\u001a\u0004\u0018\u00010!\u00a2\u0006\b\n\u0000\u001a\u0004\b>\u00105R\u0013\u0010\f\u001a\u0004\u0018\u00010\r\u00a2\u0006\b\n\u0000\u001a\u0004\b?\u0010@R\u0015\u0010\u001b\u001a\u0004\u0018\u00010\u0017\u00a2\u0006\n\n\u0002\u0010C\u001a\u0004\bA\u0010BR\u0013\u0010#\u001a\u0004\u0018\u00010\r\u00a2\u0006\b\n\u0000\u001a\u0004\bD\u0010@R\u0013\u0010\u0012\u001a\u0004\u0018\u00010\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\bE\u0010;R\u0013\u0010\u0011\u001a\u0004\u0018\u00010\r\u00a2\u0006\b\n\u0000\u001a\u0004\bF\u0010@R\u0013\u00102\u001a\u0004\u0018\u00010\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\bG\u0010;R\u0015\u0010\u0018\u001a\u0004\u0018\u00010\u0017\u00a2\u0006\n\n\u0002\u0010C\u001a\u0004\bH\u0010BR\u0015\u0010\u0019\u001a\u0004\u0018\u00010\u001a\u00a2\u0006\n\n\u0002\u0010K\u001a\u0004\bI\u0010JR\u0015\u0010\u0016\u001a\u0004\u0018\u00010\u0017\u00a2\u0006\n\n\u0002\u0010C\u001a\u0004\bL\u0010BR\u0015\u0010$\u001a\u0004\u0018\u00010\u001a\u00a2\u0006\n\n\u0002\u0010K\u001a\u0004\bM\u0010JR\u0013\u0010\u001c\u001a\u0004\u0018\u00010\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\bN\u0010;R\u0011\u0010\n\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bO\u0010=R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0004\u0010=R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010=R\u0011\u0010\u001e\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010=R\u0011\u0010\u0015\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010=R\u0011\u0010\u0014\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010=R\u0011\u0010\u0013\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010=R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010=R\u0011\u0010\b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010=R\u0017\u0010*\u001a\b\u0012\u0004\u0012\u00020+0\'\u00a2\u0006\b\n\u0000\u001a\u0004\bP\u00107R\u0013\u0010\u000f\u001a\u0004\u0018\u00010\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\bQ\u0010;R\u0013\u0010\u000e\u001a\u0004\u0018\u00010\r\u00a2\u0006\b\n\u0000\u001a\u0004\bR\u0010@R\u0015\u0010%\u001a\u0004\u0018\u00010\u001a\u00a2\u0006\n\n\u0002\u0010K\u001a\u0004\bS\u0010JR\u0013\u00100\u001a\u0004\u0018\u00010\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\bT\u0010;R\u0013\u00101\u001a\u0004\u0018\u00010\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\bU\u0010;R\u0013\u0010/\u001a\u0004\u0018\u00010+\u00a2\u0006\b\n\u0000\u001a\u0004\bV\u0010WR\u0013\u0010)\u001a\u0004\u0018\u00010(\u00a2\u0006\b\n\u0000\u001a\u0004\bX\u0010YR\u0011\u0010\u001d\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bZ\u0010=R\u0011\u0010.\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b[\u0010=R\u0011\u0010\u000b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\\\u0010=R\u0011\u0010-\u001a\u00020\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b]\u00109R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b^\u0010_\u00a8\u0006\u008d\u0001"}, d2 = {"Lcom/boattaxie/app/ui/screens/booking/BookingUiState;", "", "isLoading", "", "isBooking", "isSubmitting", "vehicleType", "Lcom/boattaxie/app/data/model/VehicleType;", "isTourist", "canBeDriver", "hasActiveSubscription", "showSubscriptionRequired", "currentLocation", "Lcom/google/android/gms/maps/model/LatLng;", "pickupLocation", "pickupAddress", "", "dropoffLocation", "dropoffAddress", "isSettingPickupOnMap", "isSearchingPickup", "isSearchingDropoff", "estimatedFare", "", "estimatedDistance", "estimatedDuration", "", "driverAdjustedFare", "fareAdjustmentReason", "showFareAdjustmentDialog", "isNightRate", "bookingConfirmed", "activeBooking", "Lcom/boattaxie/app/data/model/Booking;", "completedBooking", "driverLocation", "etaMinutes", "remainingMinutes", "adsOnMap", "", "Lcom/boattaxie/app/data/model/Advertisement;", "selectedMapAd", "onlineDrivers", "Lcom/boattaxie/app/data/model/User;", "boatDriversOnline", "taxiDriversOnline", "showOnlineDrivers", "selectedDriver", "requestedDriverId", "requestedDriverName", "errorMessage", "(ZZZLcom/boattaxie/app/data/model/VehicleType;ZZZZLcom/google/android/gms/maps/model/LatLng;Lcom/google/android/gms/maps/model/LatLng;Ljava/lang/String;Lcom/google/android/gms/maps/model/LatLng;Ljava/lang/String;ZZZLjava/lang/Double;Ljava/lang/Double;Ljava/lang/Integer;Ljava/lang/Double;Ljava/lang/String;ZZLjava/lang/String;Lcom/boattaxie/app/data/model/Booking;Lcom/boattaxie/app/data/model/Booking;Lcom/google/android/gms/maps/model/LatLng;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/util/List;Lcom/boattaxie/app/data/model/Advertisement;Ljava/util/List;IIZLcom/boattaxie/app/data/model/User;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getActiveBooking", "()Lcom/boattaxie/app/data/model/Booking;", "getAdsOnMap", "()Ljava/util/List;", "getBoatDriversOnline", "()I", "getBookingConfirmed", "()Ljava/lang/String;", "getCanBeDriver", "()Z", "getCompletedBooking", "getCurrentLocation", "()Lcom/google/android/gms/maps/model/LatLng;", "getDriverAdjustedFare", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getDriverLocation", "getDropoffAddress", "getDropoffLocation", "getErrorMessage", "getEstimatedDistance", "getEstimatedDuration", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getEstimatedFare", "getEtaMinutes", "getFareAdjustmentReason", "getHasActiveSubscription", "getOnlineDrivers", "getPickupAddress", "getPickupLocation", "getRemainingMinutes", "getRequestedDriverId", "getRequestedDriverName", "getSelectedDriver", "()Lcom/boattaxie/app/data/model/User;", "getSelectedMapAd", "()Lcom/boattaxie/app/data/model/Advertisement;", "getShowFareAdjustmentDialog", "getShowOnlineDrivers", "getShowSubscriptionRequired", "getTaxiDriversOnline", "getVehicleType", "()Lcom/boattaxie/app/data/model/VehicleType;", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component20", "component21", "component22", "component23", "component24", "component25", "component26", "component27", "component28", "component29", "component3", "component30", "component31", "component32", "component33", "component34", "component35", "component36", "component37", "component38", "component39", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(ZZZLcom/boattaxie/app/data/model/VehicleType;ZZZZLcom/google/android/gms/maps/model/LatLng;Lcom/google/android/gms/maps/model/LatLng;Ljava/lang/String;Lcom/google/android/gms/maps/model/LatLng;Ljava/lang/String;ZZZLjava/lang/Double;Ljava/lang/Double;Ljava/lang/Integer;Ljava/lang/Double;Ljava/lang/String;ZZLjava/lang/String;Lcom/boattaxie/app/data/model/Booking;Lcom/boattaxie/app/data/model/Booking;Lcom/google/android/gms/maps/model/LatLng;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/util/List;Lcom/boattaxie/app/data/model/Advertisement;Ljava/util/List;IIZLcom/boattaxie/app/data/model/User;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcom/boattaxie/app/ui/screens/booking/BookingUiState;", "equals", "other", "hashCode", "toString", "app_release"})
public final class BookingUiState {
    private final boolean isLoading = false;
    private final boolean isBooking = false;
    private final boolean isSubmitting = false;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.model.VehicleType vehicleType = null;
    private final boolean isTourist = false;
    private final boolean canBeDriver = false;
    private final boolean hasActiveSubscription = false;
    private final boolean showSubscriptionRequired = false;
    @org.jetbrains.annotations.Nullable()
    private final com.google.android.gms.maps.model.LatLng currentLocation = null;
    @org.jetbrains.annotations.Nullable()
    private final com.google.android.gms.maps.model.LatLng pickupLocation = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String pickupAddress = null;
    @org.jetbrains.annotations.Nullable()
    private final com.google.android.gms.maps.model.LatLng dropoffLocation = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String dropoffAddress = null;
    private final boolean isSettingPickupOnMap = false;
    private final boolean isSearchingPickup = false;
    private final boolean isSearchingDropoff = false;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double estimatedFare = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double estimatedDistance = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer estimatedDuration = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double driverAdjustedFare = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String fareAdjustmentReason = null;
    private final boolean showFareAdjustmentDialog = false;
    private final boolean isNightRate = false;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String bookingConfirmed = null;
    @org.jetbrains.annotations.Nullable()
    private final com.boattaxie.app.data.model.Booking activeBooking = null;
    @org.jetbrains.annotations.Nullable()
    private final com.boattaxie.app.data.model.Booking completedBooking = null;
    @org.jetbrains.annotations.Nullable()
    private final com.google.android.gms.maps.model.LatLng driverLocation = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer etaMinutes = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer remainingMinutes = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.boattaxie.app.data.model.Advertisement> adsOnMap = null;
    @org.jetbrains.annotations.Nullable()
    private final com.boattaxie.app.data.model.Advertisement selectedMapAd = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.boattaxie.app.data.model.User> onlineDrivers = null;
    private final int boatDriversOnline = 0;
    private final int taxiDriversOnline = 0;
    private final boolean showOnlineDrivers = false;
    @org.jetbrains.annotations.Nullable()
    private final com.boattaxie.app.data.model.User selectedDriver = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String requestedDriverId = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String requestedDriverName = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String errorMessage = null;
    
    public BookingUiState(boolean isLoading, boolean isBooking, boolean isSubmitting, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VehicleType vehicleType, boolean isTourist, boolean canBeDriver, boolean hasActiveSubscription, boolean showSubscriptionRequired, @org.jetbrains.annotations.Nullable()
    com.google.android.gms.maps.model.LatLng currentLocation, @org.jetbrains.annotations.Nullable()
    com.google.android.gms.maps.model.LatLng pickupLocation, @org.jetbrains.annotations.Nullable()
    java.lang.String pickupAddress, @org.jetbrains.annotations.Nullable()
    com.google.android.gms.maps.model.LatLng dropoffLocation, @org.jetbrains.annotations.Nullable()
    java.lang.String dropoffAddress, boolean isSettingPickupOnMap, boolean isSearchingPickup, boolean isSearchingDropoff, @org.jetbrains.annotations.Nullable()
    java.lang.Double estimatedFare, @org.jetbrains.annotations.Nullable()
    java.lang.Double estimatedDistance, @org.jetbrains.annotations.Nullable()
    java.lang.Integer estimatedDuration, @org.jetbrains.annotations.Nullable()
    java.lang.Double driverAdjustedFare, @org.jetbrains.annotations.Nullable()
    java.lang.String fareAdjustmentReason, boolean showFareAdjustmentDialog, boolean isNightRate, @org.jetbrains.annotations.Nullable()
    java.lang.String bookingConfirmed, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.Booking activeBooking, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.Booking completedBooking, @org.jetbrains.annotations.Nullable()
    com.google.android.gms.maps.model.LatLng driverLocation, @org.jetbrains.annotations.Nullable()
    java.lang.Integer etaMinutes, @org.jetbrains.annotations.Nullable()
    java.lang.Integer remainingMinutes, @org.jetbrains.annotations.NotNull()
    java.util.List<com.boattaxie.app.data.model.Advertisement> adsOnMap, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.Advertisement selectedMapAd, @org.jetbrains.annotations.NotNull()
    java.util.List<com.boattaxie.app.data.model.User> onlineDrivers, int boatDriversOnline, int taxiDriversOnline, boolean showOnlineDrivers, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.User selectedDriver, @org.jetbrains.annotations.Nullable()
    java.lang.String requestedDriverId, @org.jetbrains.annotations.Nullable()
    java.lang.String requestedDriverName, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage) {
        super();
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    public final boolean isBooking() {
        return false;
    }
    
    public final boolean isSubmitting() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.VehicleType getVehicleType() {
        return null;
    }
    
    public final boolean isTourist() {
        return false;
    }
    
    public final boolean getCanBeDriver() {
        return false;
    }
    
    public final boolean getHasActiveSubscription() {
        return false;
    }
    
    public final boolean getShowSubscriptionRequired() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.android.gms.maps.model.LatLng getCurrentLocation() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.android.gms.maps.model.LatLng getPickupLocation() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getPickupAddress() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.android.gms.maps.model.LatLng getDropoffLocation() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getDropoffAddress() {
        return null;
    }
    
    public final boolean isSettingPickupOnMap() {
        return false;
    }
    
    public final boolean isSearchingPickup() {
        return false;
    }
    
    public final boolean isSearchingDropoff() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getEstimatedFare() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getEstimatedDistance() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getEstimatedDuration() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getDriverAdjustedFare() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getFareAdjustmentReason() {
        return null;
    }
    
    public final boolean getShowFareAdjustmentDialog() {
        return false;
    }
    
    public final boolean isNightRate() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getBookingConfirmed() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.Booking getActiveBooking() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.Booking getCompletedBooking() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.android.gms.maps.model.LatLng getDriverLocation() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getEtaMinutes() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getRemainingMinutes() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.Advertisement> getAdsOnMap() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.Advertisement getSelectedMapAd() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.User> getOnlineDrivers() {
        return null;
    }
    
    public final int getBoatDriversOnline() {
        return 0;
    }
    
    public final int getTaxiDriversOnline() {
        return 0;
    }
    
    public final boolean getShowOnlineDrivers() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.User getSelectedDriver() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getRequestedDriverId() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getRequestedDriverName() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getErrorMessage() {
        return null;
    }
    
    public BookingUiState() {
        super();
    }
    
    public final boolean component1() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.android.gms.maps.model.LatLng component10() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component11() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.android.gms.maps.model.LatLng component12() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component13() {
        return null;
    }
    
    public final boolean component14() {
        return false;
    }
    
    public final boolean component15() {
        return false;
    }
    
    public final boolean component16() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component17() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component18() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component19() {
        return null;
    }
    
    public final boolean component2() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component20() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component21() {
        return null;
    }
    
    public final boolean component22() {
        return false;
    }
    
    public final boolean component23() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component24() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.Booking component25() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.Booking component26() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.android.gms.maps.model.LatLng component27() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component28() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component29() {
        return null;
    }
    
    public final boolean component3() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.Advertisement> component30() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.Advertisement component31() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.User> component32() {
        return null;
    }
    
    public final int component33() {
        return 0;
    }
    
    public final int component34() {
        return 0;
    }
    
    public final boolean component35() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.User component36() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component37() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component38() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component39() {
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
    
    public final boolean component7() {
        return false;
    }
    
    public final boolean component8() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.android.gms.maps.model.LatLng component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.ui.screens.booking.BookingUiState copy(boolean isLoading, boolean isBooking, boolean isSubmitting, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VehicleType vehicleType, boolean isTourist, boolean canBeDriver, boolean hasActiveSubscription, boolean showSubscriptionRequired, @org.jetbrains.annotations.Nullable()
    com.google.android.gms.maps.model.LatLng currentLocation, @org.jetbrains.annotations.Nullable()
    com.google.android.gms.maps.model.LatLng pickupLocation, @org.jetbrains.annotations.Nullable()
    java.lang.String pickupAddress, @org.jetbrains.annotations.Nullable()
    com.google.android.gms.maps.model.LatLng dropoffLocation, @org.jetbrains.annotations.Nullable()
    java.lang.String dropoffAddress, boolean isSettingPickupOnMap, boolean isSearchingPickup, boolean isSearchingDropoff, @org.jetbrains.annotations.Nullable()
    java.lang.Double estimatedFare, @org.jetbrains.annotations.Nullable()
    java.lang.Double estimatedDistance, @org.jetbrains.annotations.Nullable()
    java.lang.Integer estimatedDuration, @org.jetbrains.annotations.Nullable()
    java.lang.Double driverAdjustedFare, @org.jetbrains.annotations.Nullable()
    java.lang.String fareAdjustmentReason, boolean showFareAdjustmentDialog, boolean isNightRate, @org.jetbrains.annotations.Nullable()
    java.lang.String bookingConfirmed, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.Booking activeBooking, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.Booking completedBooking, @org.jetbrains.annotations.Nullable()
    com.google.android.gms.maps.model.LatLng driverLocation, @org.jetbrains.annotations.Nullable()
    java.lang.Integer etaMinutes, @org.jetbrains.annotations.Nullable()
    java.lang.Integer remainingMinutes, @org.jetbrains.annotations.NotNull()
    java.util.List<com.boattaxie.app.data.model.Advertisement> adsOnMap, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.Advertisement selectedMapAd, @org.jetbrains.annotations.NotNull()
    java.util.List<com.boattaxie.app.data.model.User> onlineDrivers, int boatDriversOnline, int taxiDriversOnline, boolean showOnlineDrivers, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.User selectedDriver, @org.jetbrains.annotations.Nullable()
    java.lang.String requestedDriverId, @org.jetbrains.annotations.Nullable()
    java.lang.String requestedDriverName, @org.jetbrains.annotations.Nullable()
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