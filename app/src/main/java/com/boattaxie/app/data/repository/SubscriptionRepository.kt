package com.boattaxie.app.data.repository

import com.boattaxie.app.data.model.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val userId: String?
        get() = auth.currentUser?.uid
    
    /**
     * Create a new subscription after successful payment
     */
    suspend fun createSubscription(
        plan: SubscriptionPlan,
        paymentMethodId: String? = null,
        paypalOrderId: String? = null
    ): Result<Subscription> = runCatching {
        val uid = userId ?: throw Exception("User not logged in")
        
        // Calculate end date based on plan
        val endDate = SubscriptionHelper.calculateEndDate(plan)
        
        val subscription = Subscription(
            id = UUID.randomUUID().toString(),
            userId = uid,
            plan = plan,
            status = SubscriptionStatus.ACTIVE,
            startDate = Timestamp.now(),
            endDate = endDate,
            paymentMethodId = paymentMethodId,
            paypalOrderId = paypalOrderId,
            price = plan.price,
            currency = "USD"
        )
        
        // Save subscription
        firestore.collection("subscriptions")
            .document(subscription.id)
            .set(subscription)
            .await()
        
        // Update user's subscription status
        firestore.collection("users")
            .document(uid)
            .update("hasActiveSubscription", true)
            .await()
        
        subscription
    }
    
    /**
     * Get current active subscription
     */
    suspend fun getActiveSubscription(): Subscription? {
        val uid = userId ?: return null
        
        val subscription = firestore.collection("subscriptions")
            .whereEqualTo("userId", uid)
            .whereEqualTo("status", SubscriptionStatus.ACTIVE)
            .get()
            .await()
            .toObjects(Subscription::class.java)
            .sortedByDescending { it.endDate }
            .firstOrNull()
        
        // Check if subscription is still valid
        if (subscription != null && !SubscriptionHelper.isSubscriptionActive(subscription)) {
            // Mark as expired
            expireSubscription(subscription.id)
            return null
        }
        
        return subscription
    }
    
    /**
     * Check if user has active subscription
     */
    suspend fun hasActiveSubscription(): Boolean {
        return getActiveSubscription() != null
    }
    
    /**
     * Observe subscription status changes
     */
    fun observeSubscription(): Flow<Subscription?> = callbackFlow {
        val uid = userId
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        
        val listener = firestore.collection("subscriptions")
            .whereEqualTo("userId", uid)
            .whereEqualTo("status", "active")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                
                // Manually deserialize to handle documents with "id" field
                val subscription = try {
                    snapshot?.documents?.mapNotNull { doc ->
                        try {
                            // Create subscription manually to avoid @DocumentId conflict
                            val planName = doc.getString("plan") ?: return@mapNotNull null
                            val plan = try {
                                SubscriptionPlan.valueOf(planName)
                            } catch (e: Exception) {
                                SubscriptionPlan.DAY_PASS
                            }
                            
                            Subscription(
                                id = doc.id,
                                userId = doc.getString("userId") ?: "",
                                plan = plan,
                                status = SubscriptionStatus.ACTIVE,
                                startDate = doc.getTimestamp("startDate") ?: Timestamp.now(),
                                endDate = doc.getTimestamp("endDate") ?: Timestamp.now(),
                                autoRenew = doc.getBoolean("autoRenew") ?: false,
                                paymentMethodId = doc.getString("paymentMethodId"),
                                paypalOrderId = doc.getString("paypalOrderId"),
                                price = doc.getDouble("price") ?: 0.0,
                                currency = doc.getString("currency") ?: "USD",
                                createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                                updatedAt = doc.getTimestamp("updatedAt") ?: Timestamp.now()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }?.sortedByDescending { it.endDate }?.firstOrNull()
                } catch (e: Exception) {
                    null
                }
                
                // Validate subscription - if expired, mark it in database
                if (subscription != null && !SubscriptionHelper.isSubscriptionActive(subscription)) {
                    // Expire in database asynchronously
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            expireSubscriptionById(subscription.id)
                        } catch (e: Exception) {
                            android.util.Log.e("SubscriptionRepo", "Failed to expire subscription: ${e.message}")
                        }
                    }
                    trySend(null)
                } else {
                    trySend(subscription)
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Expire a subscription by ID - called when subscription end date has passed
     */
    private suspend fun expireSubscriptionById(subscriptionId: String) {
        android.util.Log.d("SubscriptionRepo", "Expiring subscription: $subscriptionId")
        firestore.collection("subscriptions")
            .document(subscriptionId)
            .update(
                mapOf(
                    "status" to "expired",
                    "updatedAt" to Timestamp.now()
                )
            )
            .await()
        
        // Update user's subscription status
        userId?.let { uid ->
            firestore.collection("users")
                .document(uid)
                .update("hasActiveSubscription", false)
                .await()
        }
        android.util.Log.d("SubscriptionRepo", "Subscription expired successfully")
    }
    
    /**
     * Get subscription history
     */
    suspend fun getSubscriptionHistory(limit: Int = 20): List<Subscription> {
        val uid = userId ?: return emptyList()
        
        return firestore.collection("subscriptions")
            .whereEqualTo("userId", uid)
            .get()
            .await()
            .toObjects(Subscription::class.java)
            .sortedByDescending { it.createdAt }
            .take(limit)
    }
    
    /**
     * Cancel a subscription
     */
    suspend fun cancelSubscription(subscriptionId: String): Result<Unit> = runCatching {
        firestore.collection("subscriptions")
            .document(subscriptionId)
            .update(
                mapOf(
                    "status" to SubscriptionStatus.CANCELLED,
                    "autoRenew" to false,
                    "updatedAt" to Timestamp.now()
                )
            )
            .await()
    }
    
    /**
     * Mark subscription as expired
     */
    private suspend fun expireSubscription(subscriptionId: String) {
        try {
            firestore.collection("subscriptions")
                .document(subscriptionId)
                .update(
                    mapOf(
                        "status" to SubscriptionStatus.EXPIRED,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()
            
            // Update user
            userId?.let { uid ->
                firestore.collection("users")
                    .document(uid)
                    .update("hasActiveSubscription", false)
                    .await()
            }
        } catch (e: Exception) {
            // Log error but don't throw
        }
    }
    
    /**
     * Toggle auto-renew
     */
    suspend fun setAutoRenew(subscriptionId: String, autoRenew: Boolean): Result<Unit> = runCatching {
        firestore.collection("subscriptions")
            .document(subscriptionId)
            .update("autoRenew", autoRenew)
            .await()
    }
    
    /**
     * Extend subscription (add more days)
     */
    suspend fun extendSubscription(
        subscriptionId: String,
        additionalDays: Int
    ): Result<Unit> = runCatching {
        val subscription = firestore.collection("subscriptions")
            .document(subscriptionId)
            .get()
            .await()
            .toObject(Subscription::class.java)
            ?: throw Exception("Subscription not found")
        
        // Calculate new end date
        val calendar = java.util.Calendar.getInstance()
        calendar.time = subscription.endDate.toDate()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, additionalDays)
        
        firestore.collection("subscriptions")
            .document(subscriptionId)
            .update(
                mapOf(
                    "endDate" to Timestamp(calendar.time),
                    "updatedAt" to Timestamp.now()
                )
            )
            .await()
    }
    
    /**
     * Get available subscription plans
     */
    fun getAvailablePlans(): List<SubscriptionPlan> {
        return SubscriptionHelper.getSubscriptionPlans()
    }
}
