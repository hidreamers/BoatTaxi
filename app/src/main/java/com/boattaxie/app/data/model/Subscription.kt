package com.boattaxie.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Subscription model for rider access
 * Riders pay $2.99/day or choose longer plans for discounts
 */
data class Subscription(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val plan: SubscriptionPlan = SubscriptionPlan.DAY_PASS,
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),
    val autoRenew: Boolean = false,
    val paymentMethodId: String? = null,
    val paypalOrderId: String? = null,
    val price: Double = 0.0,
    val currency: String = "USD",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

enum class SubscriptionPlan(
    val displayName: String,
    val days: Int,
    val price: Double,
    val pricePerDay: Double,
    val originalPrice: Double // For showing strikethrough savings
) {
    @PropertyName("day_pass")
    DAY_PASS("1 Day", 1, 1.99, 1.99, 1.99),
    
    @PropertyName("three_day_pass")
    THREE_DAY_PASS("3 Days", 3, 4.99, 1.66, 5.97),  // Save 16%
    
    @PropertyName("five_day_pass")
    FIVE_DAY_PASS("5 Days", 5, 6.99, 1.40, 9.95),   // Save 30%
    
    @PropertyName("week_pass")
    WEEK_PASS("1 Week", 7, 8.99, 1.28, 13.93),      // Save 35%
    
    @PropertyName("two_week_pass")
    TWO_WEEK_PASS("2 Weeks", 14, 14.99, 1.07, 27.86), // Save 46%
    
    @PropertyName("month_pass")
    MONTH_PASS("1 Month", 30, 24.99, 0.83, 59.70);  // Save 58%

    fun getSavingsPercentage(): Int {
        val baseDailyPrice = DAY_PASS.price
        val savings = ((baseDailyPrice - pricePerDay) / baseDailyPrice * 100).toInt()
        return savings
    }
    
    fun getSavingsAmount(): Double {
        return originalPrice - price
    }
}

enum class SubscriptionStatus {
    @PropertyName("active")
    ACTIVE,
    
    @PropertyName("expired")
    EXPIRED,
    
    @PropertyName("cancelled")
    CANCELLED,
    
    @PropertyName("pending")
    PENDING,
    
    @PropertyName("payment_failed")
    PAYMENT_FAILED
}

/**
 * Helper functions for subscription management
 */
object SubscriptionHelper {
    fun isSubscriptionActive(subscription: Subscription?): Boolean {
        if (subscription == null) return false
        if (subscription.status != SubscriptionStatus.ACTIVE) return false
        
        val now = Timestamp.now()
        return subscription.endDate.toDate().after(now.toDate())
    }
    
    fun getRemainingDays(subscription: Subscription): Int {
        val now = System.currentTimeMillis()
        val endTime = subscription.endDate.toDate().time
        val diffMillis = endTime - now
        return (diffMillis / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    }
    
    fun getSubscriptionPlans(): List<SubscriptionPlan> {
        return SubscriptionPlan.values().toList()
    }
    
    fun calculateEndDate(plan: SubscriptionPlan): Timestamp {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, plan.days)
        return Timestamp(calendar.time)
    }
}
