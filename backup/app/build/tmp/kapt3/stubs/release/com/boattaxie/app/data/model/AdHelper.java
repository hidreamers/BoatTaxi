package com.boattaxie.app.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

/**
 * Helper for ad management
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ\u000e\u0010\u000b\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\bJ\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00060\u000eJ\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00100\u000eJ\u000e\u0010\u0011\u001a\u00020\u00122\u0006\u0010\t\u001a\u00020\nJ\u000e\u0010\u0013\u001a\u00020\u00142\u0006\u0010\t\u001a\u00020\nJ\u000e\u0010\u0015\u001a\u00020\u00142\u0006\u0010\t\u001a\u00020\n\u00a8\u0006\u0016"}, d2 = {"Lcom/boattaxie/app/data/model/AdHelper;", "", "()V", "calculateAdEndDate", "Lcom/google/firebase/Timestamp;", "plan", "Lcom/boattaxie/app/data/model/AdPlan;", "formatCouponDisplay", "", "ad", "Lcom/boattaxie/app/data/model/Advertisement;", "generateCouponCode", "businessName", "getAdPlans", "", "getCategories", "Lcom/boattaxie/app/data/model/AdCategory;", "getRemainingDays", "", "isAdActive", "", "isCouponValid", "app_release"})
public final class AdHelper {
    @org.jetbrains.annotations.NotNull()
    public static final com.boattaxie.app.data.model.AdHelper INSTANCE = null;
    
    private AdHelper() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.Timestamp calculateAdEndDate(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.AdPlan plan) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.AdPlan> getAdPlans() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.AdCategory> getCategories() {
        return null;
    }
    
    public final boolean isAdActive(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.Advertisement ad) {
        return false;
    }
    
    public final int getRemainingDays(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.Advertisement ad) {
        return 0;
    }
    
    /**
     * Generate a unique coupon code
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String generateCouponCode(@org.jetbrains.annotations.NotNull()
    java.lang.String businessName) {
        return null;
    }
    
    /**
     * Check if coupon is valid
     */
    public final boolean isCouponValid(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.Advertisement ad) {
        return false;
    }
    
    /**
     * Format coupon for display
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String formatCouponDisplay(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.Advertisement ad) {
        return null;
    }
}