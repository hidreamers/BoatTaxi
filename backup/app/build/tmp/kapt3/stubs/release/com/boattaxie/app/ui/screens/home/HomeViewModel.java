package com.boattaxie.app.ui.screens.home;

import androidx.lifecycle.ViewModel;
import com.boattaxie.app.data.model.*;
import com.boattaxie.app.data.repository.*;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.*;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0007\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u0006\u0010\u0012\u001a\u00020\u0013J\u0006\u0010\u0014\u001a\u00020\u0013J\b\u0010\u0015\u001a\u00020\u0013H\u0002J\b\u0010\u0016\u001a\u00020\u0013H\u0002J\b\u0010\u0017\u001a\u00020\u0013H\u0002J\b\u0010\u0018\u001a\u00020\u0013H\u0002J\u000e\u0010\u0019\u001a\u00020\u00132\u0006\u0010\u001a\u001a\u00020\u001bJ\u0006\u0010\u001c\u001a\u00020\u0013J\u0006\u0010\u001d\u001a\u00020\u0013R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\r0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011\u00a8\u0006\u001e"}, d2 = {"Lcom/boattaxie/app/ui/screens/home/HomeViewModel;", "Landroidx/lifecycle/ViewModel;", "authRepository", "Lcom/boattaxie/app/data/repository/AuthRepository;", "subscriptionRepository", "Lcom/boattaxie/app/data/repository/SubscriptionRepository;", "bookingRepository", "Lcom/boattaxie/app/data/repository/BookingRepository;", "advertisementRepository", "Lcom/boattaxie/app/data/repository/AdvertisementRepository;", "(Lcom/boattaxie/app/data/repository/AuthRepository;Lcom/boattaxie/app/data/repository/SubscriptionRepository;Lcom/boattaxie/app/data/repository/BookingRepository;Lcom/boattaxie/app/data/repository/AdvertisementRepository;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/boattaxie/app/ui/screens/home/HomeUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "clearError", "", "clearSwitchToDriverMode", "loadFeaturedAds", "loadRecentTrips", "loadUserData", "observeSubscription", "onAdClick", "adId", "", "refresh", "switchToDriverMode", "app_release"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class HomeViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.repository.AuthRepository authRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.repository.SubscriptionRepository subscriptionRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.repository.BookingRepository bookingRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.repository.AdvertisementRepository advertisementRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.boattaxie.app.ui.screens.home.HomeUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.ui.screens.home.HomeUiState> uiState = null;
    
    @javax.inject.Inject()
    public HomeViewModel(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.repository.AuthRepository authRepository, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.repository.SubscriptionRepository subscriptionRepository, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.repository.BookingRepository bookingRepository, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.repository.AdvertisementRepository advertisementRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.ui.screens.home.HomeUiState> getUiState() {
        return null;
    }
    
    private final void loadUserData() {
    }
    
    /**
     * Switch to driver mode - user can toggle between rider and driver
     */
    public final void switchToDriverMode() {
    }
    
    public final void clearSwitchToDriverMode() {
    }
    
    public final void clearError() {
    }
    
    private final void observeSubscription() {
    }
    
    private final void loadRecentTrips() {
    }
    
    private final void loadFeaturedAds() {
    }
    
    public final void onAdClick(@org.jetbrains.annotations.NotNull()
    java.lang.String adId) {
    }
    
    public final void refresh() {
    }
}