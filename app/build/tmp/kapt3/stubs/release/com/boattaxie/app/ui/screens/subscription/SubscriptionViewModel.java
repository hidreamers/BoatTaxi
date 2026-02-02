package com.boattaxie.app.ui.screens.subscription;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import androidx.lifecycle.ViewModel;
import com.boattaxie.app.BuildConfig;
import com.boattaxie.app.data.model.*;
import com.boattaxie.app.data.repository.SubscriptionRepository;
import com.google.firebase.auth.FirebaseAuth;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.*;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0010\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u000bH\u0002J\u0006\u0010\u0013\u001a\u00020\u0011J\b\u0010\u0014\u001a\u00020\u0011H\u0002J\u001e\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0012\u001a\u00020\u000b2\u0006\u0010\u0017\u001a\u00020\u0016H\u0082@\u00a2\u0006\u0002\u0010\u0018J\b\u0010\u0019\u001a\u00020\u0011H\u0002J\u0006\u0010\u001a\u001a\u00020\u0011J\b\u0010\u001b\u001a\u00020\u0011H\u0014J\u000e\u0010\u001c\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u000bJ\u0016\u0010\u001d\u001a\u00020\u00112\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010\u0012\u001a\u00020\u000bR\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\f\u001a\b\u0012\u0004\u0012\u00020\t0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000f\u00a8\u0006 "}, d2 = {"Lcom/boattaxie/app/ui/screens/subscription/SubscriptionViewModel;", "Landroidx/lifecycle/ViewModel;", "subscriptionRepository", "Lcom/boattaxie/app/data/repository/SubscriptionRepository;", "application", "Landroid/app/Application;", "(Lcom/boattaxie/app/data/repository/SubscriptionRepository;Landroid/app/Application;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/boattaxie/app/ui/screens/subscription/SubscriptionUiState;", "pendingPlan", "Lcom/boattaxie/app/data/model/SubscriptionPlan;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "activateSubscriptionAfterPayment", "", "plan", "cancelSubscription", "checkForSuccessfulPayment", "createCheckoutSession", "", "userId", "(Lcom/boattaxie/app/data/model/SubscriptionPlan;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "loadSubscription", "onAppResume", "onCleared", "selectPlan", "startStripePayment", "activity", "Landroid/app/Activity;", "app_release"})
public final class SubscriptionViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.repository.SubscriptionRepository subscriptionRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final android.app.Application application = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.boattaxie.app.ui.screens.subscription.SubscriptionUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.ui.screens.subscription.SubscriptionUiState> uiState = null;
    @org.jetbrains.annotations.Nullable()
    private com.boattaxie.app.data.model.SubscriptionPlan pendingPlan;
    
    public SubscriptionViewModel(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.repository.SubscriptionRepository subscriptionRepository, @org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.ui.screens.subscription.SubscriptionUiState> getUiState() {
        return null;
    }
    
    private final void loadSubscription() {
    }
    
    public final void selectPlan(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.SubscriptionPlan plan) {
    }
    
    /**
     * Start Stripe payment process using checkout session from backend
     */
    public final void startStripePayment(@org.jetbrains.annotations.NotNull()
    android.app.Activity activity, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.SubscriptionPlan plan) {
    }
    
    private final void checkForSuccessfulPayment() {
    }
    
    private final void activateSubscriptionAfterPayment(com.boattaxie.app.data.model.SubscriptionPlan plan) {
    }
    
    public final void cancelSubscription() {
    }
    
    private final java.lang.Object createCheckoutSession(com.boattaxie.app.data.model.SubscriptionPlan plan, java.lang.String userId, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @java.lang.Override()
    protected void onCleared() {
    }
    
    public final void onAppResume() {
    }
}