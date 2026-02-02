package com.boattaxie.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.android.gms.ads.MobileAds
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BoatTaxieApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Google Places API
        try {
            if (!Places.isInitialized()) {
                Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Initialize Mobile Ads SDK safely
        try {
            MobileAds.initialize(this) {}
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Create notification channels
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            
            // Ride notifications channel
            val rideChannel = NotificationChannel(
                RIDE_NOTIFICATION_CHANNEL_ID,
                "Ride Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for ride requests and updates"
                enableVibration(true)
            }
            
            // Promotional notifications channel
            val promoChannel = NotificationChannel(
                PROMO_NOTIFICATION_CHANNEL_ID,
                "Promotions",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Promotional offers and updates"
            }
            
            // Location tracking channel
            val locationChannel = NotificationChannel(
                LOCATION_NOTIFICATION_CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background location tracking for drivers"
            }
            
            notificationManager.createNotificationChannels(
                listOf(rideChannel, promoChannel, locationChannel)
            )
        }
    }
    
    companion object {
        const val RIDE_NOTIFICATION_CHANNEL_ID = "ride_notifications"
        const val PROMO_NOTIFICATION_CHANNEL_ID = "promo_notifications"
        const val LOCATION_NOTIFICATION_CHANNEL_ID = "location_tracking"
    }
}
