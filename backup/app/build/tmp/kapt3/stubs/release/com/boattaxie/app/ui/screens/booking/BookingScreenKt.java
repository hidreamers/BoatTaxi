package com.boattaxie.app.ui.screens.booking;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.compose.animation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import android.content.Intent;
import android.net.Uri;
import androidx.compose.ui.text.style.TextDecoration;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.compose.ui.layout.ContentScale;
import androidx.core.content.ContextCompat;
import coil.compose.AsyncImagePainter;
import coil.request.ImageRequest;
import androidx.compose.ui.graphics.painter.Painter;
import com.boattaxie.app.R;
import com.boattaxie.app.data.model.*;
import com.boattaxie.app.ui.components.*;
import com.boattaxie.app.ui.theme.*;
import com.boattaxie.app.util.LanguageManager;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import java.io.File;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.compose.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000f\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0006\n\u0002\b\f\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000f\n\u0002\u0010 \n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\u001a6\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\u0007\u001a\u00020\bH\u0007\u001a\u00d1\u0001\u0010\t\u001a\u00020\u00012\u0006\u0010\n\u001a\u00020\u000b2\u0012\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00010\r2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u0012\u0010\u0010\u001a\u000e\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\u00010\r2\u000e\b\u0002\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u000e\b\u0002\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u000e\b\u0002\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u000e2\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u000e2\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u00112\u000e\b\u0002\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u000e2\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\u000e2\b\b\u0002\u0010\u001b\u001a\u00020\u001cH\u0007\u00a2\u0006\u0002\u0010\u001d\u001aR\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020 2\u0006\u0010\"\u001a\u00020\u000e2\u0006\u0010#\u001a\u00020\u00112\f\u0010$\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\"\u0010&\u001a\u00020\u00012\u0006\u0010\'\u001a\u00020\u000e2\u0006\u0010(\u001a\u00020 2\b\b\u0002\u0010)\u001a\u00020\u000eH\u0003\u001aZ\u0010*\u001a\u00020\u00012\u0006\u0010\'\u001a\u00020\u000e2\u0006\u0010+\u001a\u00020\u000e2\u0006\u0010,\u001a\u00020-2\u0006\u0010.\u001a\u00020/2\b\b\u0002\u00100\u001a\u00020\u00112\f\u00101\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u000e\b\u0002\u00102\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b3\u00104\u001a\u001e\u00105\u001a\u00020\u00012\u0006\u00106\u001a\u00020\u000e2\f\u00101\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a(\u00107\u001a\u00020\u00012\u0006\u00108\u001a\u00020\u000e2\f\u00109\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\u001b\u001a\u00020\u001cH\u0007\u001ad\u0010:\u001a\u00020\u00012\u0006\u00108\u001a\u00020\u000e2\f\u0010;\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010<\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u000e\b\u0002\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u000e\b\u0002\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\u001b\u001a\u00020\u001cH\u0007\u001a4\u0010=\u001a\u00020\u00012\f\u0010>\u001a\b\u0012\u0004\u0012\u00020\u00030?2\u0012\u0010@\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\r2\b\b\u0002\u0010\u0007\u001a\u00020\bH\u0007\u001aT\u0010A\u001a\u00020\u00012\u0006\u0010B\u001a\u00020\u000e2\u0012\u0010C\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00010\r2\f\u0010D\u001a\b\u0012\u0004\u0012\u00020E0?2\u0012\u0010F\u001a\u000e\u0012\u0004\u0012\u00020E\u0012\u0004\u0012\u00020\u00010\r2\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\u001e\u0010G\u001a\u00020\u00012\u0006\u0010H\u001a\u00020E2\f\u00101\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006I"}, d2 = {"AdMapPopup", "", "ad", "Lcom/boattaxie/app/data/model/Advertisement;", "onDismiss", "Lkotlin/Function0;", "onNavigateTo", "modifier", "Landroidx/compose/ui/Modifier;", "BookRideScreen", "vehicleType", "Lcom/boattaxie/app/data/model/VehicleType;", "onBookingConfirmed", "Lkotlin/Function1;", "", "onNavigateBack", "onNavigateToLocationSearch", "", "onNavigateToHome", "onNavigateToDriverMode", "onNavigateToSubscription", "selectedAddress", "selectedPlaceId", "isPickupResult", "onClearLocationResult", "requestDriverId", "requestDriverName", "viewModel", "Lcom/boattaxie/app/ui/screens/booking/BookingViewModel;", "(Lcom/boattaxie/app/data/model/VehicleType;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Boolean;Lkotlin/jvm/functions/Function0;Ljava/lang/String;Ljava/lang/String;Lcom/boattaxie/app/ui/screens/booking/BookingViewModel;)V", "FareAdjustmentNotificationDialog", "originalFare", "", "adjustedFare", "reason", "isNightRate", "onAccept", "onDecline", "FareRow", "label", "amount", "suffix", "LocationInputField", "address", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "iconColor", "Landroidx/compose/ui/graphics/Color;", "isActive", "onClick", "onModeSelect", "LocationInputField-ww6aTOc", "(Ljava/lang/String;Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;JZLkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;)V", "QuickSearchChip", "text", "RideCompleteScreen", "bookingId", "onNavigateHome", "RideTrackingScreen", "onRideComplete", "onCancelRide", "ScrollingAdsWhileWaiting", "ads", "", "onAdClick", "SearchDialog", "searchQuery", "onSearchQueryChange", "searchResults", "Lcom/boattaxie/app/ui/screens/booking/SearchResult;", "onResultClick", "SearchResultItem", "result", "app_release"})
public final class BookingScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void BookRideScreen(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VehicleType vehicleType, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onBookingConfirmed, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onNavigateToLocationSearch, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToHome, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToDriverMode, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToSubscription, @org.jetbrains.annotations.Nullable()
    java.lang.String selectedAddress, @org.jetbrains.annotations.Nullable()
    java.lang.String selectedPlaceId, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean isPickupResult, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClearLocationResult, @org.jetbrains.annotations.Nullable()
    java.lang.String requestDriverId, @org.jetbrains.annotations.Nullable()
    java.lang.String requestDriverName, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.ui.screens.booking.BookingViewModel viewModel) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void RideTrackingScreen(@org.jetbrains.annotations.NotNull()
    java.lang.String bookingId, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onRideComplete, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onCancelRide, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToHome, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToDriverMode, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.ui.screens.booking.BookingViewModel viewModel) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void RideCompleteScreen(@org.jetbrains.annotations.NotNull()
    java.lang.String bookingId, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateHome, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.ui.screens.booking.BookingViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void FareRow(java.lang.String label, double amount, java.lang.String suffix) {
    }
    
    /**
     * Popup showing ad details when user taps on a business marker
     */
    @androidx.compose.runtime.Composable()
    public static final void AdMapPopup(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.Advertisement ad, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateTo, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Horizontal scrolling ads bar at bottom of map screen
     * Shows ads in a compact strip that doesn't cover the map
     */
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void ScrollingAdsWhileWaiting(@org.jetbrains.annotations.NotNull()
    java.util.List<com.boattaxie.app.data.model.Advertisement> ads, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.boattaxie.app.data.model.Advertisement, kotlin.Unit> onAdClick, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Dialog shown to rider when driver proposes a fare adjustment
     */
    @androidx.compose.runtime.Composable()
    private static final void FareAdjustmentNotificationDialog(double originalFare, double adjustedFare, java.lang.String reason, boolean isNightRate, kotlin.jvm.functions.Function0<kotlin.Unit> onAccept, kotlin.jvm.functions.Function0<kotlin.Unit> onDecline, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    /**
     * Full-screen search dialog with real-time filtering
     */
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    private static final void SearchDialog(java.lang.String searchQuery, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onSearchQueryChange, java.util.List<com.boattaxie.app.ui.screens.booking.SearchResult> searchResults, kotlin.jvm.functions.Function1<? super com.boattaxie.app.ui.screens.booking.SearchResult, kotlin.Unit> onResultClick, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void QuickSearchChip(java.lang.String text, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void SearchResultItem(com.boattaxie.app.ui.screens.booking.SearchResult result, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
}