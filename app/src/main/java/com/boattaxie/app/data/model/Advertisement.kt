package com.boattaxie.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Advertisement model for local business ads
 * Note: id is stored as a field in the document, not using @DocumentId
 */
data class Advertisement(
    val id: String = "",
    val advertiserId: String = "",
    val businessName: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null, // Can be local file path or remote URL
    val logoUrl: String? = null, // Business logo for map markers
    val youtubeUrl: String? = null, // YouTube video URL
    val websiteUrl: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val location: GeoLocation? = null,
    val locationName: String? = null, // Name of the location for display
    val category: AdCategory = AdCategory.GENERAL,
    val targetAudience: AdTargetAudience = AdTargetAudience.ALL,
    val plan: AdPlan = AdPlan.ONE_DAY,
    val status: AdStatus = AdStatus.PENDING,
    val isFeatured: Boolean = false,
    @JvmField  // Prevents getter generation to avoid conflict
    val featured: Boolean = false,
    val impressions: Int = 0,
    val clicks: Int = 0,
    val price: Double = 0.0,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    // Coupon fields
    val hasCoupon: Boolean = false,
    val couponCode: String? = null,
    val couponDiscount: String? = null, // e.g., "10% OFF", "$5 OFF", "FREE DRINK"
    val couponDescription: String? = null,
    val couponExpiresAt: Timestamp? = null,
    val couponRedemptions: Int = 0,
    val couponMaxRedemptions: Int? = null // null = unlimited
)

/**
 * Coupon redemption record
 */
data class CouponRedemption(
    @DocumentId
    val id: String = "",
    val couponCode: String = "",
    val advertisementId: String = "",
    val userId: String = "",
    val redeemedAt: Timestamp = Timestamp.now()
)

/**
 * Billing history record - persists after ad deletion
 */
data class AdBillingRecord(
    val id: String = "",
    val advertiserId: String = "",
    val adId: String = "",
    val businessName: String = "",
    val title: String = "",
    val plan: String = "",
    val price: Double = 0.0,
    val paymentStatus: String = "",
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val impressions: Int = 0,
    val clicks: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
    val deletedAt: Timestamp? = null
)

enum class AdCategory {
    @PropertyName("restaurant")
    RESTAURANT,
    
    @PropertyName("hotel")
    HOTEL,
    
    @PropertyName("marina")
    MARINA,
    
    @PropertyName("fuel_station")
    FUEL_STATION,
    
    @PropertyName("repair_service")
    REPAIR_SERVICE,
    
    @PropertyName("tourism")
    TOURISM,
    
    @PropertyName("shopping")
    SHOPPING,
    
    @PropertyName("entertainment")
    ENTERTAINMENT,
    
    @PropertyName("health")
    HEALTH,
    
    @PropertyName("general")
    GENERAL;
    
    fun getDisplayName(): String {
        return name.replace("_", " ").lowercase()
            .replaceFirstChar { it.uppercase() }
    }
    
    fun getIcon(): String {
        return when (this) {
            RESTAURANT -> "🍽️"
            HOTEL -> "🏨"
            MARINA -> "⚓"
            FUEL_STATION -> "⛽"
            REPAIR_SERVICE -> "🔧"
            TOURISM -> "🗺️"
            SHOPPING -> "🛍️"
            ENTERTAINMENT -> "🎭"
            HEALTH -> "🏥"
            GENERAL -> "📢"
        }
    }
}

enum class AdTargetAudience {
    @PropertyName("all")
    ALL,
    
    @PropertyName("boat_riders")
    BOAT_RIDERS,
    
    @PropertyName("taxi_riders")
    TAXI_RIDERS,
    
    @PropertyName("captains")
    CAPTAINS,
    
    @PropertyName("drivers")
    DRIVERS
}

enum class AdPlan(
    val displayName: String,
    val days: Int,
    val price: Double,
    val featuredPrice: Double
) {
    @PropertyName("one_day")
    ONE_DAY("1 Day", 1, 4.99, 9.99),
    
    @PropertyName("three_days")
    THREE_DAYS("3 Days", 3, 9.99, 19.99),
    
    @PropertyName("one_week")
    ONE_WEEK("1 Week", 7, 19.99, 34.99),
    
    @PropertyName("two_weeks")
    TWO_WEEKS("2 Weeks", 14, 29.99, 54.99),
    
    @PropertyName("one_month")
    ONE_MONTH("1 Month", 30, 49.99, 89.99);
    
    fun getPricePerDay(isFeatured: Boolean): Double {
        val totalPrice = if (isFeatured) featuredPrice else price
        return totalPrice / days
    }
}

enum class AdStatus {
    @PropertyName("pending")
    PENDING,        // Awaiting payment
    
    @PropertyName("review")
    REVIEW,         // Under review after payment
    
    @PropertyName("active")
    ACTIVE,         // Currently running
    
    @PropertyName("paused")
    PAUSED,         // Temporarily paused
    
    @PropertyName("expired")
    EXPIRED,        // Campaign ended
    
    @PropertyName("rejected")
    REJECTED        // Rejected by admin
}

/**
 * Advertiser account information
 */
data class Advertiser(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val businessName: String = "",
    val businessEmail: String = "",
    val businessPhone: String = "",
    val businessAddress: String = "",
    val website: String? = null,
    val taxId: String? = null,
    val isVerified: Boolean = false,
    val totalSpent: Double = 0.0,
    val activeAds: Int = 0,
    val createdAt: Timestamp = Timestamp.now()
)

/**
 * Helper for ad management
 */
object AdHelper {
    fun calculateAdEndDate(plan: AdPlan): Timestamp {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, plan.days)
        return Timestamp(calendar.time)
    }
    
    fun getAdPlans(): List<AdPlan> = AdPlan.values().toList()
    
    fun getCategories(): List<AdCategory> = AdCategory.values().toList()
    
    fun isAdActive(ad: Advertisement): Boolean {
        if (ad.status != AdStatus.ACTIVE) return false
        val now = Timestamp.now()
        val endDate = ad.endDate ?: return false
        return endDate.toDate().after(now.toDate())
    }
    
    fun getRemainingDays(ad: Advertisement): Int {
        val now = System.currentTimeMillis()
        val endTime = ad.endDate?.toDate()?.time ?: return 0
        val diffMillis = endTime - now
        return (diffMillis / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    }
    
    /**
     * Generate a unique coupon code
     */
    fun generateCouponCode(businessName: String): String {
        val prefix = businessName
            .uppercase()
            .replace(" ", "")
            .take(4)
        val randomPart = (10000..99999).random()
        return "$prefix$randomPart"
    }
    
    /**
     * Check if coupon is valid
     */
    fun isCouponValid(ad: Advertisement): Boolean {
        if (!ad.hasCoupon || ad.couponCode == null) return false
        if (ad.status != AdStatus.ACTIVE) return false
        
        // Check expiration
        val expiresAt = ad.couponExpiresAt
        if (expiresAt != null && expiresAt.toDate().before(java.util.Date())) {
            return false
        }
        
        // Check max redemptions
        val maxRedemptions = ad.couponMaxRedemptions
        if (maxRedemptions != null && ad.couponRedemptions >= maxRedemptions) {
            return false
        }
        
        return true
    }
    
    /**
     * Format coupon for display
     */
    fun formatCouponDisplay(ad: Advertisement): String {
        return buildString {
            append("🎟️ ${ad.couponDiscount}")
            if (ad.couponDescription != null) {
                append("\n${ad.couponDescription}")
            }
            append("\nCode: ${ad.couponCode}")
        }
    }
}
