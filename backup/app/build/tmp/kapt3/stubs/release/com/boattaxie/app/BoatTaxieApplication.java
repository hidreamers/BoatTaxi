package com.boattaxie.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import com.google.android.gms.ads.MobileAds;
import com.google.android.libraries.places.api.Places;
import dagger.hilt.android.HiltAndroidApp;

@dagger.hilt.android.HiltAndroidApp()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u0007\u0018\u0000 \u00062\u00020\u0001:\u0001\u0006B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0002J\b\u0010\u0005\u001a\u00020\u0004H\u0016\u00a8\u0006\u0007"}, d2 = {"Lcom/boattaxie/app/BoatTaxieApplication;", "Landroid/app/Application;", "()V", "createNotificationChannels", "", "onCreate", "Companion", "app_release"})
public final class BoatTaxieApplication extends android.app.Application {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String RIDE_NOTIFICATION_CHANNEL_ID = "ride_notifications";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PROMO_NOTIFICATION_CHANNEL_ID = "promo_notifications";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String LOCATION_NOTIFICATION_CHANNEL_ID = "location_tracking";
    @org.jetbrains.annotations.NotNull()
    public static final com.boattaxie.app.BoatTaxieApplication.Companion Companion = null;
    
    public BoatTaxieApplication() {
        super();
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    private final void createNotificationChannels() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/boattaxie/app/BoatTaxieApplication$Companion;", "", "()V", "LOCATION_NOTIFICATION_CHANNEL_ID", "", "PROMO_NOTIFICATION_CHANNEL_ID", "RIDE_NOTIFICATION_CHANNEL_ID", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}