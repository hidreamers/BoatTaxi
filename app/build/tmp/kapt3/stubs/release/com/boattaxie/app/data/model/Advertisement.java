package com.boattaxie.app.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

/**
 * Advertisement model for local business ads
 * Note: id is stored as a field in the document, not using @DocumentId
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\ba\b\u0086\b\u0018\u00002\u00020\u0001B\u00f7\u0002\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0003\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u000f\u0012\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u0012\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u0014\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0016\u0012\b\b\u0002\u0010\u0017\u001a\u00020\u0018\u0012\b\b\u0002\u0010\u0019\u001a\u00020\u001a\u0012\b\b\u0002\u0010\u001b\u001a\u00020\u001a\u0012\b\b\u0002\u0010\u001c\u001a\u00020\u001d\u0012\b\b\u0002\u0010\u001e\u001a\u00020\u001d\u0012\b\b\u0002\u0010\u001f\u001a\u00020 \u0012\b\b\u0002\u0010!\u001a\u00020\"\u0012\n\b\u0002\u0010#\u001a\u0004\u0018\u00010$\u0012\n\b\u0002\u0010%\u001a\u0004\u0018\u00010$\u0012\b\b\u0002\u0010&\u001a\u00020$\u0012\b\b\u0002\u0010\'\u001a\u00020$\u0012\b\b\u0002\u0010(\u001a\u00020\u001a\u0012\n\b\u0002\u0010)\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010*\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010+\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010,\u001a\u0004\u0018\u00010$\u0012\b\b\u0002\u0010-\u001a\u00020\u001d\u0012\n\b\u0002\u0010.\u001a\u0004\u0018\u00010\u001d\u00a2\u0006\u0002\u0010/J\t\u0010]\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010^\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010_\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010`\u001a\u0004\u0018\u00010\u000fH\u00c6\u0003J\u000b\u0010a\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010b\u001a\u00020\u0012H\u00c6\u0003J\t\u0010c\u001a\u00020\u0014H\u00c6\u0003J\t\u0010d\u001a\u00020\u0016H\u00c6\u0003J\t\u0010e\u001a\u00020\u0018H\u00c6\u0003J\t\u0010f\u001a\u00020\u001aH\u00c6\u0003J\t\u0010g\u001a\u00020\u001aH\u00c6\u0003J\t\u0010h\u001a\u00020\u0003H\u00c6\u0003J\t\u0010i\u001a\u00020\u001dH\u00c6\u0003J\t\u0010j\u001a\u00020\u001dH\u00c6\u0003J\t\u0010k\u001a\u00020 H\u00c6\u0003J\t\u0010l\u001a\u00020\"H\u00c6\u0003J\u000b\u0010m\u001a\u0004\u0018\u00010$H\u00c6\u0003J\u000b\u0010n\u001a\u0004\u0018\u00010$H\u00c6\u0003J\t\u0010o\u001a\u00020$H\u00c6\u0003J\t\u0010p\u001a\u00020$H\u00c6\u0003J\t\u0010q\u001a\u00020\u001aH\u00c6\u0003J\u000b\u0010r\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010s\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010t\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010u\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010v\u001a\u0004\u0018\u00010$H\u00c6\u0003J\t\u0010w\u001a\u00020\u001dH\u00c6\u0003J\u0010\u0010x\u001a\u0004\u0018\u00010\u001dH\u00c6\u0003\u00a2\u0006\u0002\u0010=J\t\u0010y\u001a\u00020\u0003H\u00c6\u0003J\t\u0010z\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010{\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010|\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010}\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010~\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0081\u0003\u0010\u007f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00032\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u000f2\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0011\u001a\u00020\u00122\b\b\u0002\u0010\u0013\u001a\u00020\u00142\b\b\u0002\u0010\u0015\u001a\u00020\u00162\b\b\u0002\u0010\u0017\u001a\u00020\u00182\b\b\u0002\u0010\u0019\u001a\u00020\u001a2\b\b\u0002\u0010\u001b\u001a\u00020\u001a2\b\b\u0002\u0010\u001c\u001a\u00020\u001d2\b\b\u0002\u0010\u001e\u001a\u00020\u001d2\b\b\u0002\u0010\u001f\u001a\u00020 2\b\b\u0002\u0010!\u001a\u00020\"2\n\b\u0002\u0010#\u001a\u0004\u0018\u00010$2\n\b\u0002\u0010%\u001a\u0004\u0018\u00010$2\b\b\u0002\u0010&\u001a\u00020$2\b\b\u0002\u0010\'\u001a\u00020$2\b\b\u0002\u0010(\u001a\u00020\u001a2\n\b\u0002\u0010)\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010*\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010+\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010,\u001a\u0004\u0018\u00010$2\b\b\u0002\u0010-\u001a\u00020\u001d2\n\b\u0002\u0010.\u001a\u0004\u0018\u00010\u001dH\u00c6\u0001\u00a2\u0006\u0003\u0010\u0080\u0001J\u0015\u0010\u0081\u0001\u001a\u00020\u001a2\t\u0010\u0082\u0001\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\n\u0010\u0083\u0001\u001a\u00020\u001dH\u00d6\u0001J\n\u0010\u0084\u0001\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u00101R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u00101R\u0011\u0010\u0011\u001a\u00020\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b3\u00104R\u0011\u0010\u001e\u001a\u00020\u001d\u00a2\u0006\b\n\u0000\u001a\u0004\b5\u00106R\u0013\u0010)\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b7\u00101R\u0013\u0010+\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u00101R\u0013\u0010*\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b9\u00101R\u0013\u0010,\u001a\u0004\u0018\u00010$\u00a2\u0006\b\n\u0000\u001a\u0004\b:\u0010;R\u0015\u0010.\u001a\u0004\u0018\u00010\u001d\u00a2\u0006\n\n\u0002\u0010>\u001a\u0004\b<\u0010=R\u0011\u0010-\u001a\u00020\u001d\u00a2\u0006\b\n\u0000\u001a\u0004\b?\u00106R\u0011\u0010&\u001a\u00020$\u00a2\u0006\b\n\u0000\u001a\u0004\b@\u0010;R\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bA\u00101R\u0013\u0010\r\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bB\u00101R\u0013\u0010%\u001a\u0004\u0018\u00010$\u00a2\u0006\b\n\u0000\u001a\u0004\bC\u0010;R\u0010\u0010\u001b\u001a\u00020\u001a8\u0006X\u0087\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010(\u001a\u00020\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\bD\u0010ER\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bF\u00101R\u0013\u0010\b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bG\u00101R\u0011\u0010\u001c\u001a\u00020\u001d\u00a2\u0006\b\n\u0000\u001a\u0004\bH\u00106R\u0011\u0010\u0019\u001a\u00020\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010ER\u0013\u0010\u000e\u001a\u0004\u0018\u00010\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\bI\u0010JR\u0013\u0010\u0010\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bK\u00101R\u0013\u0010\t\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bL\u00101R\u0011\u0010!\u001a\u00020\"\u00a2\u0006\b\n\u0000\u001a\u0004\bM\u0010NR\u0013\u0010\f\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bO\u00101R\u0011\u0010\u0015\u001a\u00020\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\bP\u0010QR\u0011\u0010\u001f\u001a\u00020 \u00a2\u0006\b\n\u0000\u001a\u0004\bR\u0010SR\u0013\u0010#\u001a\u0004\u0018\u00010$\u00a2\u0006\b\n\u0000\u001a\u0004\bT\u0010;R\u0011\u0010\u0017\u001a\u00020\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\bU\u0010VR\u0011\u0010\u0013\u001a\u00020\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\bW\u0010XR\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bY\u00101R\u0011\u0010\'\u001a\u00020$\u00a2\u0006\b\n\u0000\u001a\u0004\bZ\u0010;R\u0013\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b[\u00101R\u0013\u0010\n\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\\\u00101\u00a8\u0006\u0085\u0001"}, d2 = {"Lcom/boattaxie/app/data/model/Advertisement;", "", "id", "", "advertiserId", "businessName", "title", "description", "imageUrl", "logoUrl", "youtubeUrl", "websiteUrl", "phoneNumber", "email", "location", "Lcom/boattaxie/app/data/model/GeoLocation;", "locationName", "category", "Lcom/boattaxie/app/data/model/AdCategory;", "targetAudience", "Lcom/boattaxie/app/data/model/AdTargetAudience;", "plan", "Lcom/boattaxie/app/data/model/AdPlan;", "status", "Lcom/boattaxie/app/data/model/AdStatus;", "isFeatured", "", "featured", "impressions", "", "clicks", "price", "", "paymentStatus", "Lcom/boattaxie/app/data/model/PaymentStatus;", "startDate", "Lcom/google/firebase/Timestamp;", "endDate", "createdAt", "updatedAt", "hasCoupon", "couponCode", "couponDiscount", "couponDescription", "couponExpiresAt", "couponRedemptions", "couponMaxRedemptions", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/boattaxie/app/data/model/GeoLocation;Ljava/lang/String;Lcom/boattaxie/app/data/model/AdCategory;Lcom/boattaxie/app/data/model/AdTargetAudience;Lcom/boattaxie/app/data/model/AdPlan;Lcom/boattaxie/app/data/model/AdStatus;ZZIIDLcom/boattaxie/app/data/model/PaymentStatus;Lcom/google/firebase/Timestamp;Lcom/google/firebase/Timestamp;Lcom/google/firebase/Timestamp;Lcom/google/firebase/Timestamp;ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/google/firebase/Timestamp;ILjava/lang/Integer;)V", "getAdvertiserId", "()Ljava/lang/String;", "getBusinessName", "getCategory", "()Lcom/boattaxie/app/data/model/AdCategory;", "getClicks", "()I", "getCouponCode", "getCouponDescription", "getCouponDiscount", "getCouponExpiresAt", "()Lcom/google/firebase/Timestamp;", "getCouponMaxRedemptions", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getCouponRedemptions", "getCreatedAt", "getDescription", "getEmail", "getEndDate", "getHasCoupon", "()Z", "getId", "getImageUrl", "getImpressions", "getLocation", "()Lcom/boattaxie/app/data/model/GeoLocation;", "getLocationName", "getLogoUrl", "getPaymentStatus", "()Lcom/boattaxie/app/data/model/PaymentStatus;", "getPhoneNumber", "getPlan", "()Lcom/boattaxie/app/data/model/AdPlan;", "getPrice", "()D", "getStartDate", "getStatus", "()Lcom/boattaxie/app/data/model/AdStatus;", "getTargetAudience", "()Lcom/boattaxie/app/data/model/AdTargetAudience;", "getTitle", "getUpdatedAt", "getWebsiteUrl", "getYoutubeUrl", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component20", "component21", "component22", "component23", "component24", "component25", "component26", "component27", "component28", "component29", "component3", "component30", "component31", "component32", "component33", "component34", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/boattaxie/app/data/model/GeoLocation;Ljava/lang/String;Lcom/boattaxie/app/data/model/AdCategory;Lcom/boattaxie/app/data/model/AdTargetAudience;Lcom/boattaxie/app/data/model/AdPlan;Lcom/boattaxie/app/data/model/AdStatus;ZZIIDLcom/boattaxie/app/data/model/PaymentStatus;Lcom/google/firebase/Timestamp;Lcom/google/firebase/Timestamp;Lcom/google/firebase/Timestamp;Lcom/google/firebase/Timestamp;ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/google/firebase/Timestamp;ILjava/lang/Integer;)Lcom/boattaxie/app/data/model/Advertisement;", "equals", "other", "hashCode", "toString", "app_release"})
public final class Advertisement {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String advertiserId = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String businessName = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String title = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String description = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String imageUrl = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String logoUrl = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String youtubeUrl = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String websiteUrl = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String phoneNumber = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String email = null;
    @org.jetbrains.annotations.Nullable()
    private final com.boattaxie.app.data.model.GeoLocation location = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String locationName = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.model.AdCategory category = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.model.AdTargetAudience targetAudience = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.model.AdPlan plan = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.model.AdStatus status = null;
    private final boolean isFeatured = false;
    @kotlin.jvm.JvmField()
    public final boolean featured = false;
    private final int impressions = 0;
    private final int clicks = 0;
    private final double price = 0.0;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.model.PaymentStatus paymentStatus = null;
    @org.jetbrains.annotations.Nullable()
    private final com.google.firebase.Timestamp startDate = null;
    @org.jetbrains.annotations.Nullable()
    private final com.google.firebase.Timestamp endDate = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.Timestamp createdAt = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.Timestamp updatedAt = null;
    private final boolean hasCoupon = false;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String couponCode = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String couponDiscount = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String couponDescription = null;
    @org.jetbrains.annotations.Nullable()
    private final com.google.firebase.Timestamp couponExpiresAt = null;
    private final int couponRedemptions = 0;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer couponMaxRedemptions = null;
    
    public Advertisement(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String advertiserId, @org.jetbrains.annotations.NotNull()
    java.lang.String businessName, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String description, @org.jetbrains.annotations.Nullable()
    java.lang.String imageUrl, @org.jetbrains.annotations.Nullable()
    java.lang.String logoUrl, @org.jetbrains.annotations.Nullable()
    java.lang.String youtubeUrl, @org.jetbrains.annotations.Nullable()
    java.lang.String websiteUrl, @org.jetbrains.annotations.Nullable()
    java.lang.String phoneNumber, @org.jetbrains.annotations.Nullable()
    java.lang.String email, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.GeoLocation location, @org.jetbrains.annotations.Nullable()
    java.lang.String locationName, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.AdCategory category, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.AdTargetAudience targetAudience, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.AdPlan plan, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.AdStatus status, boolean isFeatured, boolean featured, int impressions, int clicks, double price, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.PaymentStatus paymentStatus, @org.jetbrains.annotations.Nullable()
    com.google.firebase.Timestamp startDate, @org.jetbrains.annotations.Nullable()
    com.google.firebase.Timestamp endDate, @org.jetbrains.annotations.NotNull()
    com.google.firebase.Timestamp createdAt, @org.jetbrains.annotations.NotNull()
    com.google.firebase.Timestamp updatedAt, boolean hasCoupon, @org.jetbrains.annotations.Nullable()
    java.lang.String couponCode, @org.jetbrains.annotations.Nullable()
    java.lang.String couponDiscount, @org.jetbrains.annotations.Nullable()
    java.lang.String couponDescription, @org.jetbrains.annotations.Nullable()
    com.google.firebase.Timestamp couponExpiresAt, int couponRedemptions, @org.jetbrains.annotations.Nullable()
    java.lang.Integer couponMaxRedemptions) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAdvertiserId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getBusinessName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTitle() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDescription() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getImageUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getLogoUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getYoutubeUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getWebsiteUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getPhoneNumber() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getEmail() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.GeoLocation getLocation() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getLocationName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.AdCategory getCategory() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.AdTargetAudience getTargetAudience() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.AdPlan getPlan() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.AdStatus getStatus() {
        return null;
    }
    
    public final boolean isFeatured() {
        return false;
    }
    
    public final int getImpressions() {
        return 0;
    }
    
    public final int getClicks() {
        return 0;
    }
    
    public final double getPrice() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.PaymentStatus getPaymentStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.firebase.Timestamp getStartDate() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.firebase.Timestamp getEndDate() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.Timestamp getCreatedAt() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.Timestamp getUpdatedAt() {
        return null;
    }
    
    public final boolean getHasCoupon() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getCouponCode() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getCouponDiscount() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getCouponDescription() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.firebase.Timestamp getCouponExpiresAt() {
        return null;
    }
    
    public final int getCouponRedemptions() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getCouponMaxRedemptions() {
        return null;
    }
    
    public Advertisement() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component10() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component11() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.GeoLocation component12() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component13() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.AdCategory component14() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.AdTargetAudience component15() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.AdPlan component16() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.AdStatus component17() {
        return null;
    }
    
    public final boolean component18() {
        return false;
    }
    
    public final boolean component19() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    public final int component20() {
        return 0;
    }
    
    public final int component21() {
        return 0;
    }
    
    public final double component22() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.PaymentStatus component23() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.firebase.Timestamp component24() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.firebase.Timestamp component25() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.Timestamp component26() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.Timestamp component27() {
        return null;
    }
    
    public final boolean component28() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
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
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component31() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.firebase.Timestamp component32() {
        return null;
    }
    
    public final int component33() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component34() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.Advertisement copy(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String advertiserId, @org.jetbrains.annotations.NotNull()
    java.lang.String businessName, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String description, @org.jetbrains.annotations.Nullable()
    java.lang.String imageUrl, @org.jetbrains.annotations.Nullable()
    java.lang.String logoUrl, @org.jetbrains.annotations.Nullable()
    java.lang.String youtubeUrl, @org.jetbrains.annotations.Nullable()
    java.lang.String websiteUrl, @org.jetbrains.annotations.Nullable()
    java.lang.String phoneNumber, @org.jetbrains.annotations.Nullable()
    java.lang.String email, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.GeoLocation location, @org.jetbrains.annotations.Nullable()
    java.lang.String locationName, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.AdCategory category, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.AdTargetAudience targetAudience, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.AdPlan plan, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.AdStatus status, boolean isFeatured, boolean featured, int impressions, int clicks, double price, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.PaymentStatus paymentStatus, @org.jetbrains.annotations.Nullable()
    com.google.firebase.Timestamp startDate, @org.jetbrains.annotations.Nullable()
    com.google.firebase.Timestamp endDate, @org.jetbrains.annotations.NotNull()
    com.google.firebase.Timestamp createdAt, @org.jetbrains.annotations.NotNull()
    com.google.firebase.Timestamp updatedAt, boolean hasCoupon, @org.jetbrains.annotations.Nullable()
    java.lang.String couponCode, @org.jetbrains.annotations.Nullable()
    java.lang.String couponDiscount, @org.jetbrains.annotations.Nullable()
    java.lang.String couponDescription, @org.jetbrains.annotations.Nullable()
    com.google.firebase.Timestamp couponExpiresAt, int couponRedemptions, @org.jetbrains.annotations.Nullable()
    java.lang.Integer couponMaxRedemptions) {
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