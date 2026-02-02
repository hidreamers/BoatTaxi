package com.boattaxie.app.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0014\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B/\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\u0007\u0012\u0006\u0010\t\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\nJ\u0006\u0010\u0013\u001a\u00020\u0007J\u0006\u0010\u0014\u001a\u00020\u0005R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\t\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0010R\u0011\u0010\b\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0010j\u0002\b\u0015j\u0002\b\u0016j\u0002\b\u0017j\u0002\b\u0018j\u0002\b\u0019j\u0002\b\u001a\u00a8\u0006\u001b"}, d2 = {"Lcom/boattaxie/app/data/model/SubscriptionPlan;", "", "displayName", "", "days", "", "price", "", "pricePerDay", "originalPrice", "(Ljava/lang/String;ILjava/lang/String;IDDD)V", "getDays", "()I", "getDisplayName", "()Ljava/lang/String;", "getOriginalPrice", "()D", "getPrice", "getPricePerDay", "getSavingsAmount", "getSavingsPercentage", "DAY_PASS", "THREE_DAY_PASS", "FIVE_DAY_PASS", "WEEK_PASS", "TWO_WEEK_PASS", "MONTH_PASS", "app_release"})
public enum SubscriptionPlan {
    @com.google.firebase.firestore.PropertyName(value = "day_pass")
    /*public static final*/ DAY_PASS /* = new DAY_PASS(null, 0, 0.0, 0.0, 0.0) */,
    @com.google.firebase.firestore.PropertyName(value = "three_day_pass")
    /*public static final*/ THREE_DAY_PASS /* = new THREE_DAY_PASS(null, 0, 0.0, 0.0, 0.0) */,
    @com.google.firebase.firestore.PropertyName(value = "five_day_pass")
    /*public static final*/ FIVE_DAY_PASS /* = new FIVE_DAY_PASS(null, 0, 0.0, 0.0, 0.0) */,
    @com.google.firebase.firestore.PropertyName(value = "week_pass")
    /*public static final*/ WEEK_PASS /* = new WEEK_PASS(null, 0, 0.0, 0.0, 0.0) */,
    @com.google.firebase.firestore.PropertyName(value = "two_week_pass")
    /*public static final*/ TWO_WEEK_PASS /* = new TWO_WEEK_PASS(null, 0, 0.0, 0.0, 0.0) */,
    @com.google.firebase.firestore.PropertyName(value = "month_pass")
    /*public static final*/ MONTH_PASS /* = new MONTH_PASS(null, 0, 0.0, 0.0, 0.0) */;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String displayName = null;
    private final int days = 0;
    private final double price = 0.0;
    private final double pricePerDay = 0.0;
    private final double originalPrice = 0.0;
    
    SubscriptionPlan(java.lang.String displayName, int days, double price, double pricePerDay, double originalPrice) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDisplayName() {
        return null;
    }
    
    public final int getDays() {
        return 0;
    }
    
    public final double getPrice() {
        return 0.0;
    }
    
    public final double getPricePerDay() {
        return 0.0;
    }
    
    public final double getOriginalPrice() {
        return 0.0;
    }
    
    public final int getSavingsPercentage() {
        return 0;
    }
    
    public final double getSavingsAmount() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.boattaxie.app.data.model.SubscriptionPlan> getEntries() {
        return null;
    }
}