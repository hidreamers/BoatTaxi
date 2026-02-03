package com.boattaxie.app.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\u0006\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\'\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\tJ\u000e\u0010\u0011\u001a\u00020\u00072\u0006\u0010\u0012\u001a\u00020\u0013R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\b\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000fj\u0002\b\u0014j\u0002\b\u0015j\u0002\b\u0016j\u0002\b\u0017j\u0002\b\u0018\u00a8\u0006\u0019"}, d2 = {"Lcom/boattaxie/app/data/model/AdPlan;", "", "displayName", "", "days", "", "price", "", "featuredPrice", "(Ljava/lang/String;ILjava/lang/String;IDD)V", "getDays", "()I", "getDisplayName", "()Ljava/lang/String;", "getFeaturedPrice", "()D", "getPrice", "getPricePerDay", "isFeatured", "", "ONE_DAY", "THREE_DAYS", "ONE_WEEK", "TWO_WEEKS", "ONE_MONTH", "app_debug"})
public enum AdPlan {
    @com.google.firebase.firestore.PropertyName(value = "one_day")
    /*public static final*/ ONE_DAY /* = new ONE_DAY(null, 0, 0.0, 0.0) */,
    @com.google.firebase.firestore.PropertyName(value = "three_days")
    /*public static final*/ THREE_DAYS /* = new THREE_DAYS(null, 0, 0.0, 0.0) */,
    @com.google.firebase.firestore.PropertyName(value = "one_week")
    /*public static final*/ ONE_WEEK /* = new ONE_WEEK(null, 0, 0.0, 0.0) */,
    @com.google.firebase.firestore.PropertyName(value = "two_weeks")
    /*public static final*/ TWO_WEEKS /* = new TWO_WEEKS(null, 0, 0.0, 0.0) */,
    @com.google.firebase.firestore.PropertyName(value = "one_month")
    /*public static final*/ ONE_MONTH /* = new ONE_MONTH(null, 0, 0.0, 0.0) */;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String displayName = null;
    private final int days = 0;
    private final double price = 0.0;
    private final double featuredPrice = 0.0;
    
    AdPlan(java.lang.String displayName, int days, double price, double featuredPrice) {
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
    
    public final double getFeaturedPrice() {
        return 0.0;
    }
    
    public final double getPricePerDay(boolean isFeatured) {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.boattaxie.app.data.model.AdPlan> getEntries() {
        return null;
    }
}