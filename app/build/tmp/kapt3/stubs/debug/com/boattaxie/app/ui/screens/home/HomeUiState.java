package com.boattaxie.app.ui.screens.home;

import androidx.lifecycle.ViewModel;
import com.boattaxie.app.data.model.*;
import com.boattaxie.app.data.repository.*;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.*;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b \n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u0083\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\t\u001a\u00020\u0003\u0012\b\b\u0002\u0010\n\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f\u0012\u000e\b\u0002\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e\u0012\u000e\b\u0002\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00110\u000e\u0012\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0014J\t\u0010#\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010$\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010%\u001a\u00020\u0003H\u00c6\u0003J\t\u0010&\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\'\u001a\u00020\u0007H\u00c6\u0003J\t\u0010(\u001a\u00020\u0003H\u00c6\u0003J\t\u0010)\u001a\u00020\u0003H\u00c6\u0003J\t\u0010*\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010+\u001a\u0004\u0018\u00010\fH\u00c6\u0003J\u000f\u0010,\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eH\u00c6\u0003J\u000f\u0010-\u001a\b\u0012\u0004\u0012\u00020\u00110\u000eH\u00c6\u0003J\u0087\u0001\u0010.\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\u00032\b\b\u0002\u0010\n\u001a\u00020\u00032\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f2\u000e\b\u0002\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e2\u000e\b\u0002\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00110\u000e2\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u0013\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010/\u001a\u00020\u00032\b\u00100\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00101\u001a\u000202H\u00d6\u0001J\t\u00103\u001a\u00020\u0005H\u00d6\u0001R\u0011\u0010\b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0013\u0010\u0012\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0017\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00110\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\n\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0016R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u0016R\u0011\u0010\t\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\u0016R\u0017\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001aR\u0013\u0010\u000b\u001a\u0004\u0018\u00010\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001eR\u0011\u0010\u0013\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u0016R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u0018R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\"\u00a8\u00064"}, d2 = {"Lcom/boattaxie/app/ui/screens/home/HomeUiState;", "", "isLoading", "", "userName", "", "userType", "Lcom/boattaxie/app/data/model/UserType;", "canBeDriver", "isVerifiedDriver", "hasActiveSubscription", "subscription", "Lcom/boattaxie/app/data/model/Subscription;", "recentTrips", "", "Lcom/boattaxie/app/data/model/Booking;", "featuredAds", "Lcom/boattaxie/app/data/model/Advertisement;", "errorMessage", "switchToDriverMode", "(ZLjava/lang/String;Lcom/boattaxie/app/data/model/UserType;ZZZLcom/boattaxie/app/data/model/Subscription;Ljava/util/List;Ljava/util/List;Ljava/lang/String;Z)V", "getCanBeDriver", "()Z", "getErrorMessage", "()Ljava/lang/String;", "getFeaturedAds", "()Ljava/util/List;", "getHasActiveSubscription", "getRecentTrips", "getSubscription", "()Lcom/boattaxie/app/data/model/Subscription;", "getSwitchToDriverMode", "getUserName", "getUserType", "()Lcom/boattaxie/app/data/model/UserType;", "component1", "component10", "component11", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "", "toString", "app_debug"})
public final class HomeUiState {
    private final boolean isLoading = false;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String userName = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.model.UserType userType = null;
    private final boolean canBeDriver = false;
    private final boolean isVerifiedDriver = false;
    private final boolean hasActiveSubscription = false;
    @org.jetbrains.annotations.Nullable()
    private final com.boattaxie.app.data.model.Subscription subscription = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.boattaxie.app.data.model.Booking> recentTrips = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.boattaxie.app.data.model.Advertisement> featuredAds = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String errorMessage = null;
    private final boolean switchToDriverMode = false;
    
    public HomeUiState(boolean isLoading, @org.jetbrains.annotations.NotNull()
    java.lang.String userName, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.UserType userType, boolean canBeDriver, boolean isVerifiedDriver, boolean hasActiveSubscription, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.Subscription subscription, @org.jetbrains.annotations.NotNull()
    java.util.List<com.boattaxie.app.data.model.Booking> recentTrips, @org.jetbrains.annotations.NotNull()
    java.util.List<com.boattaxie.app.data.model.Advertisement> featuredAds, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage, boolean switchToDriverMode) {
        super();
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getUserName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.UserType getUserType() {
        return null;
    }
    
    public final boolean getCanBeDriver() {
        return false;
    }
    
    public final boolean isVerifiedDriver() {
        return false;
    }
    
    public final boolean getHasActiveSubscription() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.Subscription getSubscription() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.Booking> getRecentTrips() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.Advertisement> getFeaturedAds() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getErrorMessage() {
        return null;
    }
    
    public final boolean getSwitchToDriverMode() {
        return false;
    }
    
    public HomeUiState() {
        super();
    }
    
    public final boolean component1() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component10() {
        return null;
    }
    
    public final boolean component11() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.UserType component3() {
        return null;
    }
    
    public final boolean component4() {
        return false;
    }
    
    public final boolean component5() {
        return false;
    }
    
    public final boolean component6() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.Subscription component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.Booking> component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.Advertisement> component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.ui.screens.home.HomeUiState copy(boolean isLoading, @org.jetbrains.annotations.NotNull()
    java.lang.String userName, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.UserType userType, boolean canBeDriver, boolean isVerifiedDriver, boolean hasActiveSubscription, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.Subscription subscription, @org.jetbrains.annotations.NotNull()
    java.util.List<com.boattaxie.app.data.model.Booking> recentTrips, @org.jetbrains.annotations.NotNull()
    java.util.List<com.boattaxie.app.data.model.Advertisement> featuredAds, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage, boolean switchToDriverMode) {
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