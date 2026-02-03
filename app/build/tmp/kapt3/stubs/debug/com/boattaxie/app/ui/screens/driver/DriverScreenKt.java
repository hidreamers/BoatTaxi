package com.boattaxie.app.ui.screens.driver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.compose.animation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.core.content.ContextCompat;
import com.boattaxie.app.data.model.*;
import com.boattaxie.app.ui.components.*;
import com.boattaxie.app.ui.theme.*;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.compose.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000N\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0006\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u001aV\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u000e\b\u0002\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u000e\b\u0002\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\t\u001a\u00020\nH\u0007\u001a \u0010\u000b\u001a\u00020\u00012\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\t\u001a\u00020\nH\u0007\u001aR\u0010\f\u001a\u00020\u00012\u0012\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u000e2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u000e\b\u0002\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\t\u001a\u00020\nH\u0007\u001a\u0018\u0010\u0011\u001a\u00020\u00012\u0006\u0010\u0012\u001a\u00020\u00032\u0006\u0010\u0013\u001a\u00020\u0014H\u0003\u001a\u009e\u0001\u0010\u0015\u001a\u00020\u00012\u0006\u0010\u0016\u001a\u00020\u00142\u0006\u0010\u0017\u001a\u00020\u00032\u0006\u0010\u0018\u001a\u00020\u00032\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001a2\u0012\u0010\u001c\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u000e2\u0012\u0010\u001d\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u000e2\f\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a6\u0010#\u001a\u00020\u00012\u0006\u0010$\u001a\u00020%2\f\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010(\u001a\u00020)H\u0003\u001a \u0010*\u001a\u00020\u00012\u0006\u0010+\u001a\u00020,2\u0006\u0010-\u001a\u00020\u00032\u0006\u0010\u0012\u001a\u00020\u0003H\u0003\u00a8\u0006."}, d2 = {"ActiveRideScreen", "", "bookingId", "", "onRideComplete", "Lkotlin/Function0;", "onNavigateBack", "onNavigateToHome", "onNavigateToRiderMode", "viewModel", "Lcom/boattaxie/app/ui/screens/driver/DriverViewModel;", "DriverEarningsScreen", "DriverHomeScreen", "onNavigateToActiveRide", "Lkotlin/Function1;", "onNavigateToEarnings", "onNavigateToProfile", "EarningsRow", "label", "amount", "", "FareAdjustmentSheet", "originalFare", "adjustedFare", "adjustmentReason", "isNightRateTime", "", "isLoading", "onAdjustedFareChange", "onReasonChange", "onApplyNightRate", "onApplyBadWeather", "onApplyHoliday", "onSubmit", "onDismiss", "RideRequestPopup", "booking", "Lcom/boattaxie/app/data/model/Booking;", "onAccept", "onDecline", "modifier", "Landroidx/compose/ui/Modifier;", "StatBox", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "value", "app_debug"})
public final class DriverScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void DriverHomeScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavigateToActiveRide, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToEarnings, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToProfile, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToRiderMode, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.ui.screens.driver.DriverViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void StatBox(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String value, java.lang.String label) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void RideRequestPopup(com.boattaxie.app.data.model.Booking booking, kotlin.jvm.functions.Function0<kotlin.Unit> onAccept, kotlin.jvm.functions.Function0<kotlin.Unit> onDecline, androidx.compose.ui.Modifier modifier) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void DriverEarningsScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.ui.screens.driver.DriverViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void EarningsRow(java.lang.String label, double amount) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void ActiveRideScreen(@org.jetbrains.annotations.NotNull()
    java.lang.String bookingId, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onRideComplete, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToHome, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToRiderMode, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.ui.screens.driver.DriverViewModel viewModel) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    private static final void FareAdjustmentSheet(double originalFare, java.lang.String adjustedFare, java.lang.String adjustmentReason, boolean isNightRateTime, boolean isLoading, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onAdjustedFareChange, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onReasonChange, kotlin.jvm.functions.Function0<kotlin.Unit> onApplyNightRate, kotlin.jvm.functions.Function0<kotlin.Unit> onApplyBadWeather, kotlin.jvm.functions.Function0<kotlin.Unit> onApplyHoliday, kotlin.jvm.functions.Function0<kotlin.Unit> onSubmit, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
}