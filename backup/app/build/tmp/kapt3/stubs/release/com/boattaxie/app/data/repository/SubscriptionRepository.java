package com.boattaxie.app.data.repository;

import com.boattaxie.app.data.model.*;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import kotlinx.coroutines.flow.Flow;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J$\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u000e\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u000f\u0010\u0010J<\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\f2\u0006\u0010\u0013\u001a\u00020\u00142\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\b2\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\bH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0017\u0010\u0018J\u0016\u0010\u0019\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\bH\u0082@\u00a2\u0006\u0002\u0010\u0010J,\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u000e\u001a\u00020\b2\u0006\u0010\u001b\u001a\u00020\u001cH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u001d\u0010\u001eJ\u0010\u0010\u001f\u001a\u0004\u0018\u00010\u0012H\u0086@\u00a2\u0006\u0002\u0010 J\f\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00140\"J\u001e\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00120\"2\b\b\u0002\u0010$\u001a\u00020\u001cH\u0086@\u00a2\u0006\u0002\u0010%J\u000e\u0010&\u001a\u00020\'H\u0086@\u00a2\u0006\u0002\u0010 J\u000e\u0010(\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00120)J,\u0010*\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u000e\u001a\u00020\b2\u0006\u0010+\u001a\u00020\'H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b,\u0010-R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0007\u001a\u0004\u0018\u00010\b8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\t\u0010\n\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006."}, d2 = {"Lcom/boattaxie/app/data/repository/SubscriptionRepository;", "", "auth", "Lcom/google/firebase/auth/FirebaseAuth;", "firestore", "Lcom/google/firebase/firestore/FirebaseFirestore;", "(Lcom/google/firebase/auth/FirebaseAuth;Lcom/google/firebase/firestore/FirebaseFirestore;)V", "userId", "", "getUserId", "()Ljava/lang/String;", "cancelSubscription", "Lkotlin/Result;", "", "subscriptionId", "cancelSubscription-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createSubscription", "Lcom/boattaxie/app/data/model/Subscription;", "plan", "Lcom/boattaxie/app/data/model/SubscriptionPlan;", "paymentMethodId", "paypalOrderId", "createSubscription-BWLJW6A", "(Lcom/boattaxie/app/data/model/SubscriptionPlan;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "expireSubscription", "extendSubscription", "additionalDays", "", "extendSubscription-0E7RQCE", "(Ljava/lang/String;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getActiveSubscription", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAvailablePlans", "", "getSubscriptionHistory", "limit", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "hasActiveSubscription", "", "observeSubscription", "Lkotlinx/coroutines/flow/Flow;", "setAutoRenew", "autoRenew", "setAutoRenew-0E7RQCE", "(Ljava/lang/String;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
public final class SubscriptionRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.auth.FirebaseAuth auth = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.firestore.FirebaseFirestore firestore = null;
    
    @javax.inject.Inject()
    public SubscriptionRepository(@org.jetbrains.annotations.NotNull()
    com.google.firebase.auth.FirebaseAuth auth, @org.jetbrains.annotations.NotNull()
    com.google.firebase.firestore.FirebaseFirestore firestore) {
        super();
    }
    
    private final java.lang.String getUserId() {
        return null;
    }
    
    /**
     * Get current active subscription
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getActiveSubscription(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.boattaxie.app.data.model.Subscription> $completion) {
        return null;
    }
    
    /**
     * Check if user has active subscription
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object hasActiveSubscription(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Observe subscription status changes
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.boattaxie.app.data.model.Subscription> observeSubscription() {
        return null;
    }
    
    /**
     * Get subscription history
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getSubscriptionHistory(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.boattaxie.app.data.model.Subscription>> $completion) {
        return null;
    }
    
    /**
     * Mark subscription as expired
     */
    private final java.lang.Object expireSubscription(java.lang.String subscriptionId, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Get available subscription plans
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.SubscriptionPlan> getAvailablePlans() {
        return null;
    }
}