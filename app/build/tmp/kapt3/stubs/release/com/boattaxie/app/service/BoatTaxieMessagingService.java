package com.boattaxie.app.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import androidx.core.app.NotificationCompat;
import com.boattaxie.app.R;
import com.boattaxie.app.data.repository.AuthRepository;
import com.boattaxie.app.ui.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import dagger.hilt.android.AndroidEntryPoint;
import kotlinx.coroutines.Dispatchers;
import javax.inject.Inject;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\b\u0007\u0018\u0000 *2\u00020\u0001:\u0001*B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u000b\u001a\u00020\fH\u0002J\u001c\u0010\r\u001a\u00020\f2\u0012\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00100\u000fH\u0002J&\u0010\u0011\u001a\u00020\f2\b\u0010\u0012\u001a\u0004\u0018\u00010\u00132\u0012\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00100\u000fH\u0002J\u001c\u0010\u0014\u001a\u00020\f2\u0012\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00100\u000fH\u0002J\u001c\u0010\u0015\u001a\u00020\f2\u0012\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00100\u000fH\u0002J\u001c\u0010\u0016\u001a\u00020\f2\u0012\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00100\u000fH\u0002J\u001c\u0010\u0017\u001a\u00020\f2\u0012\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00100\u000fH\u0002J\u001c\u0010\u0018\u001a\u00020\f2\u0012\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00100\u000fH\u0002J\u001c\u0010\u0019\u001a\u00020\f2\u0012\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00100\u000fH\u0002J\b\u0010\u001a\u001a\u00020\fH\u0016J\u0010\u0010\u001b\u001a\u00020\f2\u0006\u0010\u001c\u001a\u00020\u001dH\u0016J\u0010\u0010\u001e\u001a\u00020\f2\u0006\u0010\u001f\u001a\u00020\u0010H\u0016JJ\u0010 \u001a\u00020\f2\u0006\u0010!\u001a\u00020\u00102\u0006\u0010\"\u001a\u00020\u00102\u0006\u0010\u001c\u001a\u00020\u00102\n\b\u0002\u0010#\u001a\u0004\u0018\u00010$2\b\b\u0002\u0010%\u001a\u00020&2\b\b\u0002\u0010\'\u001a\u00020&2\b\b\u0002\u0010(\u001a\u00020)H\u0002R\u001e\u0010\u0003\u001a\u00020\u00048\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006+"}, d2 = {"Lcom/boattaxie/app/service/BoatTaxieMessagingService;", "Lcom/google/firebase/messaging/FirebaseMessagingService;", "()V", "authRepository", "Lcom/boattaxie/app/data/repository/AuthRepository;", "getAuthRepository", "()Lcom/boattaxie/app/data/repository/AuthRepository;", "setAuthRepository", "(Lcom/boattaxie/app/data/repository/AuthRepository;)V", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "createNotificationChannels", "", "handleDriverArrived", "data", "", "", "handleGeneral", "notification", "Lcom/google/firebase/messaging/RemoteMessage$Notification;", "handlePromotion", "handleRideAccepted", "handleRideCancelled", "handleRideCompleted", "handleRideRequest", "handleRideStarted", "onCreate", "onMessageReceived", "message", "Lcom/google/firebase/messaging/RemoteMessage;", "onNewToken", "token", "showNotification", "channelId", "title", "intent", "Landroid/content/Intent;", "notificationId", "", "priority", "autoCancel", "", "Companion", "app_release"})
public final class BoatTaxieMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @javax.inject.Inject()
    public com.boattaxie.app.data.repository.AuthRepository authRepository;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CHANNEL_ID_RIDE = "ride_notifications";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CHANNEL_ID_PROMO = "promo_notifications";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CHANNEL_ID_GENERAL = "general_notifications";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TYPE_RIDE_REQUEST = "ride_request";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TYPE_RIDE_ACCEPTED = "ride_accepted";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TYPE_DRIVER_ARRIVED = "driver_arrived";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TYPE_RIDE_STARTED = "ride_started";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TYPE_RIDE_COMPLETED = "ride_completed";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TYPE_RIDE_CANCELLED = "ride_cancelled";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TYPE_PROMOTION = "promotion";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TYPE_GENERAL = "general";
    @org.jetbrains.annotations.NotNull()
    public static final com.boattaxie.app.service.BoatTaxieMessagingService.Companion Companion = null;
    
    public BoatTaxieMessagingService() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.repository.AuthRepository getAuthRepository() {
        return null;
    }
    
    public final void setAuthRepository(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.repository.AuthRepository p0) {
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    @java.lang.Override()
    public void onNewToken(@org.jetbrains.annotations.NotNull()
    java.lang.String token) {
    }
    
    @java.lang.Override()
    public void onMessageReceived(@org.jetbrains.annotations.NotNull()
    com.google.firebase.messaging.RemoteMessage message) {
    }
    
    private final void handleRideRequest(java.util.Map<java.lang.String, java.lang.String> data) {
    }
    
    private final void handleRideAccepted(java.util.Map<java.lang.String, java.lang.String> data) {
    }
    
    private final void handleDriverArrived(java.util.Map<java.lang.String, java.lang.String> data) {
    }
    
    private final void handleRideStarted(java.util.Map<java.lang.String, java.lang.String> data) {
    }
    
    private final void handleRideCompleted(java.util.Map<java.lang.String, java.lang.String> data) {
    }
    
    private final void handleRideCancelled(java.util.Map<java.lang.String, java.lang.String> data) {
    }
    
    private final void handlePromotion(java.util.Map<java.lang.String, java.lang.String> data) {
    }
    
    private final void handleGeneral(com.google.firebase.messaging.RemoteMessage.Notification notification, java.util.Map<java.lang.String, java.lang.String> data) {
    }
    
    private final void showNotification(java.lang.String channelId, java.lang.String title, java.lang.String message, android.content.Intent intent, int notificationId, int priority, boolean autoCancel) {
    }
    
    private final void createNotificationChannels() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u000b\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/boattaxie/app/service/BoatTaxieMessagingService$Companion;", "", "()V", "CHANNEL_ID_GENERAL", "", "CHANNEL_ID_PROMO", "CHANNEL_ID_RIDE", "TYPE_DRIVER_ARRIVED", "TYPE_GENERAL", "TYPE_PROMOTION", "TYPE_RIDE_ACCEPTED", "TYPE_RIDE_CANCELLED", "TYPE_RIDE_COMPLETED", "TYPE_RIDE_REQUEST", "TYPE_RIDE_STARTED", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}