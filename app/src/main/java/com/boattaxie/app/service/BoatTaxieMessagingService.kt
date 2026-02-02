package com.boattaxie.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.boattaxie.app.R
import com.boattaxie.app.data.repository.AuthRepository
import com.boattaxie.app.ui.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BoatTaxieMessagingService : FirebaseMessagingService() {
    
    @Inject
    lateinit var authRepository: AuthRepository
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    companion object {
        const val CHANNEL_ID_RIDE = "ride_notifications"
        const val CHANNEL_ID_PROMO = "promo_notifications"
        const val CHANNEL_ID_GENERAL = "general_notifications"
        const val CHANNEL_ID_VERIFICATION = "verification"
        
        // Notification types
        const val TYPE_RIDE_REQUEST = "ride_request"
        const val TYPE_RIDE_ACCEPTED = "ride_accepted"
        const val TYPE_DRIVER_ARRIVED = "driver_arrived"
        const val TYPE_RIDE_STARTED = "ride_started"
        const val TYPE_RIDE_COMPLETED = "ride_completed"
        const val TYPE_RIDE_CANCELLED = "ride_cancelled"
        const val TYPE_PROMOTION = "promotion"
        const val TYPE_GENERAL = "general"
        const val TYPE_DRIVER_VERIFICATION = "driver_verification"
        const val TYPE_VERIFICATION_APPROVED = "verification_approved"
        const val TYPE_VERIFICATION_REJECTED = "verification_rejected"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        // Update the FCM token in Firestore
        serviceScope.launch {
            authRepository.updateFcmToken(token)
        }
    }
    
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        val data = message.data
        val type = data["type"] ?: TYPE_GENERAL
        
        when (type) {
            TYPE_RIDE_REQUEST -> handleRideRequest(data)
            TYPE_RIDE_ACCEPTED -> handleRideAccepted(data)
            TYPE_DRIVER_ARRIVED -> handleDriverArrived(data)
            TYPE_RIDE_STARTED -> handleRideStarted(data)
            TYPE_RIDE_COMPLETED -> handleRideCompleted(data)
            TYPE_RIDE_CANCELLED -> handleRideCancelled(data)
            TYPE_PROMOTION -> handlePromotion(data)
            else -> handleGeneral(message.notification, data)
        }
    }
    
    private fun handleRideRequest(data: Map<String, String>) {
        val bookingId = data["bookingId"] ?: return
        val pickup = data["pickup"] ?: "New location"
        val fare = data["fare"] ?: "0.00"
        
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("notification_type", TYPE_RIDE_REQUEST)
            putExtra("booking_id", bookingId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        showNotification(
            channelId = CHANNEL_ID_RIDE,
            title = "New Ride Request!",
            message = "Pickup: $pickup • $$fare",
            intent = intent,
            notificationId = bookingId.hashCode(),
            priority = NotificationCompat.PRIORITY_HIGH,
            autoCancel = true
        )
    }
    
    private fun handleRideAccepted(data: Map<String, String>) {
        val driverName = data["driverName"] ?: "Your driver"
        val eta = data["eta"] ?: "a few"
        
        showNotification(
            channelId = CHANNEL_ID_RIDE,
            title = "Ride Accepted!",
            message = "$driverName is on the way. Arriving in $eta minutes.",
            notificationId = 2001,
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }
    
    private fun handleDriverArrived(data: Map<String, String>) {
        showNotification(
            channelId = CHANNEL_ID_RIDE,
            title = "Driver Has Arrived",
            message = "Your ride is waiting for you at the pickup location.",
            notificationId = 2002,
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }
    
    private fun handleRideStarted(data: Map<String, String>) {
        val destination = data["destination"] ?: "your destination"
        
        showNotification(
            channelId = CHANNEL_ID_RIDE,
            title = "Ride Started",
            message = "On the way to $destination. Enjoy your ride!",
            notificationId = 2003
        )
    }
    
    private fun handleRideCompleted(data: Map<String, String>) {
        val fare = data["fare"] ?: "0.00"
        val bookingId = data["bookingId"] ?: return
        
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("notification_type", TYPE_RIDE_COMPLETED)
            putExtra("booking_id", bookingId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        showNotification(
            channelId = CHANNEL_ID_RIDE,
            title = "Ride Completed!",
            message = "Total fare: $$fare. Rate your experience!",
            intent = intent,
            notificationId = 2004,
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }
    
    private fun handleRideCancelled(data: Map<String, String>) {
        val reason = data["reason"] ?: "The ride has been cancelled"
        
        showNotification(
            channelId = CHANNEL_ID_RIDE,
            title = "Ride Cancelled",
            message = reason,
            notificationId = 2005
        )
    }
    
    private fun handlePromotion(data: Map<String, String>) {
        val title = data["title"] ?: "Special Offer!"
        val message = data["message"] ?: "Check out our latest promotion."
        
        showNotification(
            channelId = CHANNEL_ID_PROMO,
            title = title,
            message = message,
            notificationId = 3001
        )
    }
    
    private fun handleGeneral(notification: RemoteMessage.Notification?, data: Map<String, String>) {
        val title = notification?.title ?: data["title"] ?: "BoatTaxie"
        val message = notification?.body ?: data["message"] ?: ""
        
        if (message.isNotEmpty()) {
            showNotification(
                channelId = CHANNEL_ID_GENERAL,
                title = title,
                message = message,
                notificationId = System.currentTimeMillis().toInt()
            )
        }
    }
    
    private fun showNotification(
        channelId: String,
        title: String,
        message: String,
        intent: Intent? = null,
        notificationId: Int = System.currentTimeMillis().toInt(),
        priority: Int = NotificationCompat.PRIORITY_DEFAULT,
        autoCancel: Boolean = true
    ) {
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent ?: Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(autoCancel)
            .setSound(soundUri)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationId, notification)
    }
    
    private fun createNotificationChannels() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        
        // Ride notifications channel
        val rideChannel = NotificationChannel(
            CHANNEL_ID_RIDE,
            "Ride Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications about your rides"
            enableVibration(true)
            enableLights(true)
        }
        
        // Promotional notifications channel
        val promoChannel = NotificationChannel(
            CHANNEL_ID_PROMO,
            "Promotions",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Special offers and promotions"
        }
        
        // General notifications channel
        val generalChannel = NotificationChannel(
            CHANNEL_ID_GENERAL,
            "General",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "General notifications"
        }
        
        // Verification notifications channel (for admin and driver updates)
        val verificationChannel = NotificationChannel(
            CHANNEL_ID_VERIFICATION,
            "Driver Verifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "New driver verification requests and approvals"
            enableVibration(true)
            enableLights(true)
        }
        
        notificationManager.createNotificationChannels(
            listOf(rideChannel, promoChannel, generalChannel, verificationChannel)
        )
    }
}
