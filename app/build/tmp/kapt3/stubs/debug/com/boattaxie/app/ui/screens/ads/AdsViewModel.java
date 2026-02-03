package com.boattaxie.app.ui.screens.ads;

import android.net.Uri;
import androidx.lifecycle.ViewModel;
import com.boattaxie.app.data.model.*;
import com.boattaxie.app.data.payment.PaymentManager;
import com.boattaxie.app.data.repository.AdvertisementRepository;
import com.boattaxie.app.data.repository.PlaceResult;
import com.boattaxie.app.data.repository.PlacesRepository;
import com.google.firebase.auth.FirebaseAuth;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.*;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0088\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\u0010\u0006\n\u0002\b\u000f\b\u0007\u0018\u00002\u00020\u0001B\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u0006\u0010\u001b\u001a\u00020\u001cJ\u0006\u0010\u001d\u001a\u00020\u001cJ\u00cd\u0001\u0010\u001e\u001a\u00020\u001c2\u0006\u0010\u001f\u001a\u00020\u000f2\u0006\u0010 \u001a\u00020\u000f2\u0006\u0010!\u001a\u00020\u000f2\b\u0010\"\u001a\u0004\u0018\u00010#2\n\b\u0002\u0010$\u001a\u0004\u0018\u00010#2\n\b\u0002\u0010%\u001a\u0004\u0018\u00010\u000f2\b\u0010&\u001a\u0004\u0018\u00010\u000f2\b\u0010\'\u001a\u0004\u0018\u00010\u000f2\b\u0010(\u001a\u0004\u0018\u00010\u000f2\u0006\u0010)\u001a\u00020*2\u0006\u0010+\u001a\u00020,2\u0006\u0010-\u001a\u00020.2\n\b\u0002\u0010/\u001a\u0004\u0018\u0001002\n\b\u0002\u00101\u001a\u0004\u0018\u00010\u000f2\b\b\u0002\u00102\u001a\u00020.2\n\b\u0002\u00103\u001a\u0004\u0018\u00010\u000f2\n\b\u0002\u00104\u001a\u0004\u0018\u00010\u000f2\n\b\u0002\u00105\u001a\u0004\u0018\u00010\u000f2\n\b\u0002\u00106\u001a\u0004\u0018\u000107\u00a2\u0006\u0002\u00108J\u001a\u00109\u001a\u00020\u001c2\b\b\u0002\u0010!\u001a\u00020\u000f2\b\b\u0002\u0010:\u001a\u000207J\u000e\u0010;\u001a\u00020\u001c2\u0006\u0010<\u001a\u00020\u000fJ$\u0010=\u001a\u0010\u0012\u0004\u0012\u00020?\u0012\u0004\u0012\u00020?\u0018\u00010>2\u0006\u0010@\u001a\u00020\u000fH\u0086@\u00a2\u0006\u0002\u0010AJ\u000e\u0010B\u001a\u00020\u001c2\u0006\u0010<\u001a\u00020\u000fJ\b\u0010C\u001a\u00020\u001cH\u0002J\u0006\u0010D\u001a\u00020\u001cJ\u000e\u0010E\u001a\u00020\u001c2\u0006\u0010<\u001a\u00020\u000fJ\u0006\u0010F\u001a\u00020\u001cJ\u000e\u0010G\u001a\u00020\u001c2\u0006\u0010<\u001a\u00020\u000fJ\u000e\u0010H\u001a\u00020\u001c2\u0006\u0010I\u001a\u00020\u000fJ\u0006\u0010J\u001a\u00020\u001cJz\u0010K\u001a\u00020\u001c2\u0006\u0010<\u001a\u00020\u000f2\u0006\u0010\u001f\u001a\u00020\u000f2\u0006\u0010 \u001a\u00020\u000f2\u0006\u0010!\u001a\u00020\u000f2\b\u0010L\u001a\u0004\u0018\u00010\u000f2\b\u0010\'\u001a\u0004\u0018\u00010\u000f2\b\u0010M\u001a\u0004\u0018\u00010\u000f2\b\u0010%\u001a\u0004\u0018\u00010\u000f2\b\u00104\u001a\u0004\u0018\u00010\u000f2\b\u00105\u001a\u0004\u0018\u00010\u000f2\n\b\u0002\u0010\"\u001a\u0004\u0018\u00010#2\n\b\u0002\u0010$\u001a\u0004\u0018\u00010#R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0013\u0010\u000e\u001a\u0004\u0018\u00010\u000f8F\u00a2\u0006\u0006\u001a\u0004\b\u0010\u0010\u0011R\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00140\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\r0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001a\u00a8\u0006N"}, d2 = {"Lcom/boattaxie/app/ui/screens/ads/AdsViewModel;", "Landroidx/lifecycle/ViewModel;", "advertisementRepository", "Lcom/boattaxie/app/data/repository/AdvertisementRepository;", "placesRepository", "Lcom/boattaxie/app/data/repository/PlacesRepository;", "paymentManager", "Lcom/boattaxie/app/data/payment/PaymentManager;", "auth", "Lcom/google/firebase/auth/FirebaseAuth;", "(Lcom/boattaxie/app/data/repository/AdvertisementRepository;Lcom/boattaxie/app/data/repository/PlacesRepository;Lcom/boattaxie/app/data/payment/PaymentManager;Lcom/google/firebase/auth/FirebaseAuth;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/boattaxie/app/ui/screens/ads/AdsUiState;", "currentUserId", "", "getCurrentUserId", "()Ljava/lang/String;", "panamaBusinessLocations", "", "Lcom/boattaxie/app/data/repository/PlaceResult;", "getPaymentManager", "()Lcom/boattaxie/app/data/payment/PaymentManager;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "clearError", "", "clearLocationSearch", "createAd", "businessName", "title", "description", "imageUri", "Landroid/net/Uri;", "logoUri", "youtubeUrl", "phone", "email", "website", "category", "Lcom/boattaxie/app/data/model/AdCategory;", "plan", "Lcom/boattaxie/app/data/model/AdPlan;", "isFeatured", "", "location", "Lcom/boattaxie/app/data/model/GeoLocation;", "locationName", "hasCoupon", "couponCode", "couponDiscount", "couponDescription", "couponMaxRedemptions", "", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/net/Uri;Landroid/net/Uri;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/boattaxie/app/data/model/AdCategory;Lcom/boattaxie/app/data/model/AdPlan;ZLcom/boattaxie/app/data/model/GeoLocation;Ljava/lang/String;ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;)V", "createDemoAds", "count", "deleteAd", "adId", "getPlaceCoordinates", "Lkotlin/Pair;", "", "placeId", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "loadAdDetails", "loadAdvertisements", "loadMyAds", "pauseAd", "resetAdCreatedFlag", "resumeAd", "searchLocation", "query", "seedTestAds", "updateAd", "phoneNumber", "websiteUrl", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class AdsViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.repository.AdvertisementRepository advertisementRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.repository.PlacesRepository placesRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.payment.PaymentManager paymentManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.auth.FirebaseAuth auth = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.boattaxie.app.ui.screens.ads.AdsUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.ui.screens.ads.AdsUiState> uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.boattaxie.app.data.repository.PlaceResult> panamaBusinessLocations = null;
    
    @javax.inject.Inject()
    public AdsViewModel(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.repository.AdvertisementRepository advertisementRepository, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.repository.PlacesRepository placesRepository, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.payment.PaymentManager paymentManager, @org.jetbrains.annotations.NotNull()
    com.google.firebase.auth.FirebaseAuth auth) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.payment.PaymentManager getPaymentManager() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.ui.screens.ads.AdsUiState> getUiState() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getCurrentUserId() {
        return null;
    }
    
    private final void loadAdvertisements() {
    }
    
    /**
     * Search for locations using Google Places API + local database
     */
    public final void searchLocation(@org.jetbrains.annotations.NotNull()
    java.lang.String query) {
    }
    
    /**
     * Get coordinates for a selected place
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getPlaceCoordinates(@org.jetbrains.annotations.NotNull()
    java.lang.String placeId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Pair<java.lang.Double, java.lang.Double>> $completion) {
        return null;
    }
    
    /**
     * Clear location search results
     */
    public final void clearLocationSearch() {
    }
    
    public final void loadMyAds() {
    }
    
    public final void loadAdDetails(@org.jetbrains.annotations.NotNull()
    java.lang.String adId) {
    }
    
    public final void createAd(@org.jetbrains.annotations.NotNull()
    java.lang.String businessName, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String description, @org.jetbrains.annotations.Nullable()
    android.net.Uri imageUri, @org.jetbrains.annotations.Nullable()
    android.net.Uri logoUri, @org.jetbrains.annotations.Nullable()
    java.lang.String youtubeUrl, @org.jetbrains.annotations.Nullable()
    java.lang.String phone, @org.jetbrains.annotations.Nullable()
    java.lang.String email, @org.jetbrains.annotations.Nullable()
    java.lang.String website, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.AdCategory category, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.AdPlan plan, boolean isFeatured, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.GeoLocation location, @org.jetbrains.annotations.Nullable()
    java.lang.String locationName, boolean hasCoupon, @org.jetbrains.annotations.Nullable()
    java.lang.String couponCode, @org.jetbrains.annotations.Nullable()
    java.lang.String couponDiscount, @org.jetbrains.annotations.Nullable()
    java.lang.String couponDescription, @org.jetbrains.annotations.Nullable()
    java.lang.Integer couponMaxRedemptions) {
    }
    
    public final void pauseAd(@org.jetbrains.annotations.NotNull()
    java.lang.String adId) {
    }
    
    public final void resumeAd(@org.jetbrains.annotations.NotNull()
    java.lang.String adId) {
    }
    
    public final void deleteAd(@org.jetbrains.annotations.NotNull()
    java.lang.String adId) {
    }
    
    public final void updateAd(@org.jetbrains.annotations.NotNull()
    java.lang.String adId, @org.jetbrains.annotations.NotNull()
    java.lang.String businessName, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String description, @org.jetbrains.annotations.Nullable()
    java.lang.String phoneNumber, @org.jetbrains.annotations.Nullable()
    java.lang.String email, @org.jetbrains.annotations.Nullable()
    java.lang.String websiteUrl, @org.jetbrains.annotations.Nullable()
    java.lang.String youtubeUrl, @org.jetbrains.annotations.Nullable()
    java.lang.String couponDiscount, @org.jetbrains.annotations.Nullable()
    java.lang.String couponDescription, @org.jetbrains.annotations.Nullable()
    android.net.Uri imageUri, @org.jetbrains.annotations.Nullable()
    android.net.Uri logoUri) {
    }
    
    public final void clearError() {
    }
    
    public final void resetAdCreatedFlag() {
    }
    
    /**
     * Seed test advertisements (for development)
     * First deletes ALL other ads (keeps yours), then creates new test ones
     */
    public final void seedTestAds() {
    }
    
    /**
     * Create demo ads with custom description and count
     */
    public final void createDemoAds(@org.jetbrains.annotations.NotNull()
    java.lang.String description, int count) {
    }
}