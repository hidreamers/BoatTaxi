package com.boattaxie.app.data.repository;

import android.content.Context;
import android.net.Uri;
import com.boattaxie.app.data.model.*;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.flow.Flow;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0092\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0010\n\u0002\u0010 \n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0006\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0011\b\u0007\u0018\u00002\u00020\u0001B)\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0001\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ$\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00110\u00102\u0006\u0010\u0012\u001a\u00020\fH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0013\u0010\u0014J\u00e4\u0001\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00160\u00102\u0006\u0010\u0017\u001a\u00020\f2\u0006\u0010\u0018\u001a\u00020\f2\u0006\u0010\u0019\u001a\u00020\f2\b\u0010\u001a\u001a\u0004\u0018\u00010\u001b2\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u001b2\n\b\u0002\u0010\u001d\u001a\u0004\u0018\u00010\f2\b\u0010\u001e\u001a\u0004\u0018\u00010\f2\b\u0010\u001f\u001a\u0004\u0018\u00010\f2\b\u0010 \u001a\u0004\u0018\u00010\f2\b\u0010!\u001a\u0004\u0018\u00010\"2\n\b\u0002\u0010#\u001a\u0004\u0018\u00010\f2\u0006\u0010$\u001a\u00020%2\u0006\u0010&\u001a\u00020\'2\u0006\u0010(\u001a\u00020)2\u0006\u0010*\u001a\u00020+2\b\b\u0002\u0010,\u001a\u00020+2\n\b\u0002\u0010-\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010.\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010/\u001a\u0004\u0018\u00010\f2\n\b\u0002\u00100\u001a\u0004\u0018\u000101H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b2\u00103J0\u00104\u001a\b\u0012\u0004\u0012\u0002010\u00102\b\b\u0002\u0010\u0019\u001a\u00020\f2\b\b\u0002\u00105\u001a\u000201H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b6\u00107J$\u00108\u001a\b\u0012\u0004\u0012\u00020\u00110\u00102\u0006\u0010\u0012\u001a\u00020\fH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b9\u0010\u0014J\u001c\u0010:\u001a\b\u0012\u0004\u0012\u0002010\u0010H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b;\u0010<J\u001c\u0010=\u001a\b\u0012\u0004\u0012\u0002010\u0010H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b>\u0010<J\u001c\u0010?\u001a\b\u0012\u0004\u0012\u0002010\u0010H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b@\u0010<J6\u0010A\u001a\b\u0012\u0004\u0012\u00020\u00160B2\n\b\u0002\u0010&\u001a\u0004\u0018\u00010\'2\n\b\u0002\u0010$\u001a\u0004\u0018\u00010%2\b\b\u0002\u0010C\u001a\u000201H\u0086@\u00a2\u0006\u0002\u0010DJ\u0018\u0010E\u001a\u0004\u0018\u00010\u00162\u0006\u0010\u0012\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\u0014J\f\u0010F\u001a\b\u0012\u0004\u0012\u00020)0BJ\u001e\u0010G\u001a\b\u0012\u0004\u0012\u00020\u00160B2\b\b\u0002\u0010C\u001a\u000201H\u0086@\u00a2\u0006\u0002\u0010HJ\u001e\u0010I\u001a\b\u0012\u0004\u0012\u00020\u00160B2\b\b\u0002\u0010C\u001a\u000201H\u0086@\u00a2\u0006\u0002\u0010HJ\u0014\u0010J\u001a\b\u0012\u0004\u0012\u00020K0BH\u0086@\u00a2\u0006\u0002\u0010<J\f\u0010L\u001a\b\u0012\u0004\u0012\u00020%0BJ\u001e\u0010M\u001a\b\u0012\u0004\u0012\u00020\u00160B2\b\b\u0002\u0010C\u001a\u000201H\u0086@\u00a2\u0006\u0002\u0010HJ0\u0010N\u001a\b\u0012\u0004\u0012\u00020\u00160B2\u0006\u0010!\u001a\u00020\"2\b\b\u0002\u0010O\u001a\u00020P2\b\b\u0002\u0010C\u001a\u000201H\u0086@\u00a2\u0006\u0002\u0010QJ\u0014\u0010R\u001a\b\u0012\u0004\u0012\u00020\u00160BH\u0086@\u00a2\u0006\u0002\u0010<J\u0012\u0010S\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00160B0TJ$\u0010U\u001a\b\u0012\u0004\u0012\u00020\u00110\u00102\u0006\u0010\u0012\u001a\u00020\fH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\bV\u0010\u0014J\u0016\u0010W\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\u0014J\u0016\u0010X\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\u0014J$\u0010Y\u001a\b\u0012\u0004\u0012\u00020+0\u00102\u0006\u0010\u0012\u001a\u00020\fH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\bZ\u0010\u0014J$\u0010[\u001a\b\u0012\u0004\u0012\u00020\u00110\u00102\u0006\u0010\u0012\u001a\u00020\fH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\\\u0010\u0014J\u001c\u0010]\u001a\b\u0012\u0004\u0012\u0002010\u0010H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b^\u0010<J\u0090\u0001\u0010_\u001a\b\u0012\u0004\u0012\u00020\u00110\u00102\u0006\u0010\u0012\u001a\u00020\f2\u0006\u0010\u0017\u001a\u00020\f2\u0006\u0010\u0018\u001a\u00020\f2\u0006\u0010\u0019\u001a\u00020\f2\b\u0010\u001f\u001a\u0004\u0018\u00010\f2\b\u0010 \u001a\u0004\u0018\u00010\f2\b\u0010\u001e\u001a\u0004\u0018\u00010\f2\b\u0010\u001d\u001a\u0004\u0018\u00010\f2\b\u0010.\u001a\u0004\u0018\u00010\f2\b\u0010/\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\u001b2\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u001bH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b`\u0010aJ \u0010b\u001a\u0004\u0018\u00010\f2\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010c\u001a\u00020\fH\u0082@\u00a2\u0006\u0002\u0010dR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000b\u001a\u0004\u0018\u00010\f8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\r\u0010\u000e\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006e"}, d2 = {"Lcom/boattaxie/app/data/repository/AdvertisementRepository;", "", "auth", "Lcom/google/firebase/auth/FirebaseAuth;", "firestore", "Lcom/google/firebase/firestore/FirebaseFirestore;", "storage", "Lcom/google/firebase/storage/FirebaseStorage;", "context", "Landroid/content/Context;", "(Lcom/google/firebase/auth/FirebaseAuth;Lcom/google/firebase/firestore/FirebaseFirestore;Lcom/google/firebase/storage/FirebaseStorage;Landroid/content/Context;)V", "userId", "", "getUserId", "()Ljava/lang/String;", "activateAdvertisement", "Lkotlin/Result;", "", "adId", "activateAdvertisement-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createAdvertisement", "Lcom/boattaxie/app/data/model/Advertisement;", "businessName", "title", "description", "imageUri", "Landroid/net/Uri;", "logoUri", "youtubeUrl", "websiteUrl", "phoneNumber", "email", "location", "Lcom/boattaxie/app/data/model/GeoLocation;", "locationName", "category", "Lcom/boattaxie/app/data/model/AdCategory;", "targetAudience", "Lcom/boattaxie/app/data/model/AdTargetAudience;", "plan", "Lcom/boattaxie/app/data/model/AdPlan;", "isFeatured", "", "hasCoupon", "couponCode", "couponDiscount", "couponDescription", "couponMaxRedemptions", "", "createAdvertisement-XeoeeGA", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/net/Uri;Landroid/net/Uri;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/boattaxie/app/data/model/GeoLocation;Ljava/lang/String;Lcom/boattaxie/app/data/model/AdCategory;Lcom/boattaxie/app/data/model/AdTargetAudience;Lcom/boattaxie/app/data/model/AdPlan;ZZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createDemoAds", "count", "createDemoAds-0E7RQCE", "(Ljava/lang/String;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAdvertisement", "deleteAdvertisement-gIAlu-s", "deleteAllAdsExceptMine", "deleteAllAdsExceptMine-IoAF18A", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteDemoAds", "deleteDemoAds-IoAF18A", "deleteTestAds", "deleteTestAds-IoAF18A", "getActiveAdvertisements", "", "limit", "(Lcom/boattaxie/app/data/model/AdTargetAudience;Lcom/boattaxie/app/data/model/AdCategory;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAdById", "getAdPlans", "getAdsWithCoupons", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAdsWithLocations", "getBillingHistory", "Lcom/boattaxie/app/data/model/AdBillingRecord;", "getCategories", "getFeaturedAdvertisements", "getNearbyAdvertisements", "radiusKm", "", "(Lcom/boattaxie/app/data/model/GeoLocation;DILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getUserAdvertisements", "observeActiveAds", "Lkotlinx/coroutines/flow/Flow;", "pauseAdvertisement", "pauseAdvertisement-gIAlu-s", "recordClick", "recordImpression", "redeemCoupon", "redeemCoupon-gIAlu-s", "resumeAdvertisement", "resumeAdvertisement-gIAlu-s", "seedTestAds", "seedTestAds-IoAF18A", "updateAdvertisement", "updateAdvertisement-1iavgos", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/net/Uri;Landroid/net/Uri;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadImageToStorage", "path", "(Landroid/net/Uri;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
public final class AdvertisementRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.auth.FirebaseAuth auth = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.firestore.FirebaseFirestore firestore = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.storage.FirebaseStorage storage = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    
    @javax.inject.Inject()
    public AdvertisementRepository(@org.jetbrains.annotations.NotNull()
    com.google.firebase.auth.FirebaseAuth auth, @org.jetbrains.annotations.NotNull()
    com.google.firebase.firestore.FirebaseFirestore firestore, @org.jetbrains.annotations.NotNull()
    com.google.firebase.storage.FirebaseStorage storage, @dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    private final java.lang.String getUserId() {
        return null;
    }
    
    /**
     * Upload image to Firebase Storage and return the download URL
     */
    private final java.lang.Object uploadImageToStorage(android.net.Uri imageUri, java.lang.String path, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    /**
     * Get a single advertisement by ID
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getAdById(@org.jetbrains.annotations.NotNull()
    java.lang.String adId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.boattaxie.app.data.model.Advertisement> $completion) {
        return null;
    }
    
    /**
     * Get active advertisements for display
     * Note: Sorting in memory to avoid requiring Firestore composite indexes
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getActiveAdvertisements(@org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.AdTargetAudience targetAudience, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.AdCategory category, int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.boattaxie.app.data.model.Advertisement>> $completion) {
        return null;
    }
    
    /**
     * Get featured advertisements (falls back to all active ads if no featured ones)
     * Excludes test/seed ads (those with empty IDs)
     * Fetches up to 100 ads and randomly selects from them
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getFeaturedAdvertisements(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.boattaxie.app.data.model.Advertisement>> $completion) {
        return null;
    }
    
    /**
     * Get advertisements with locations (for map display)
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getAdsWithLocations(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.boattaxie.app.data.model.Advertisement>> $completion) {
        return null;
    }
    
    /**
     * Get advertisements with coupons
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getAdsWithCoupons(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.boattaxie.app.data.model.Advertisement>> $completion) {
        return null;
    }
    
    /**
     * Get advertisements by location (nearby)
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getNearbyAdvertisements(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.GeoLocation location, double radiusKm, int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.boattaxie.app.data.model.Advertisement>> $completion) {
        return null;
    }
    
    /**
     * Get user's advertisements from BOTH collections
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getUserAdvertisements(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.boattaxie.app.data.model.Advertisement>> $completion) {
        return null;
    }
    
    /**
     * Record ad impression
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object recordImpression(@org.jetbrains.annotations.NotNull()
    java.lang.String adId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Record ad click
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object recordClick(@org.jetbrains.annotations.NotNull()
    java.lang.String adId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Observe ads for real-time updates
     * Note: No orderBy to avoid requiring composite index
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.boattaxie.app.data.model.Advertisement>> observeActiveAds() {
        return null;
    }
    
    /**
     * Get ad categories
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.AdCategory> getCategories() {
        return null;
    }
    
    /**
     * Get ad plans
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.AdPlan> getAdPlans() {
        return null;
    }
    
    /**
     * Get billing history for the current user
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getBillingHistory(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.boattaxie.app.data.model.AdBillingRecord>> $completion) {
        return null;
    }
}