package com.boattaxie.app.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

/**
 * Helper functions for subscription management
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0010\u000b\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00060\fJ\u0010\u0010\r\u001a\u00020\u000e2\b\u0010\t\u001a\u0004\u0018\u00010\n\u00a8\u0006\u000f"}, d2 = {"Lcom/boattaxie/app/data/model/SubscriptionHelper;", "", "()V", "calculateEndDate", "Lcom/google/firebase/Timestamp;", "plan", "Lcom/boattaxie/app/data/model/SubscriptionPlan;", "getRemainingDays", "", "subscription", "Lcom/boattaxie/app/data/model/Subscription;", "getSubscriptionPlans", "", "isSubscriptionActive", "", "app_release"})
public final class SubscriptionHelper {
    @org.jetbrains.annotations.NotNull()
    public static final com.boattaxie.app.data.model.SubscriptionHelper INSTANCE = null;
    
    private SubscriptionHelper() {
        super();
    }
    
    public final boolean isSubscriptionActive(@org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.Subscription subscription) {
        return false;
    }
    
    public final int getRemainingDays(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.Subscription subscription) {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.SubscriptionPlan> getSubscriptionPlans() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.Timestamp calculateEndDate(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.SubscriptionPlan plan) {
        return null;
    }
}