package com.boattaxie.app.data.payment

import android.content.Context
import android.util.Log
import com.boattaxie.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stripe Payment Manager
 * Handles direct card payments through Stripe
 */
@Singleton
class StripeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "StripeManager"
        
        // Stripe Checkout Links - Pre-configured payment pages
        // These are created in Stripe Dashboard > Products > Payment Links
        private const val STRIPE_CHECKOUT_BASE = "https://buy.stripe.com/"
        
        // Payment link IDs (create these in Stripe Dashboard)
        // For subscriptions
        val SUBSCRIPTION_LINKS = mapOf(
            "1_day" to "test_subscription_1day",      // $2.99
            "3_days" to "test_subscription_3days",    // $6.99
            "1_week" to "test_subscription_1week",    // $9.99
            "2_weeks" to "test_subscription_2weeks",  // $17.99
            "1_month" to "test_subscription_1month"   // $29.99
        )
        
        // For ads
        val AD_PLAN_LINKS = mapOf(
            "1_day" to "test_ad_1day",        // $4.99
            "3_days" to "test_ad_3days",      // $9.99
            "1_week" to "test_ad_1week",      // $14.99
            "2_weeks" to "test_ad_2weeks",    // $24.99
            "1_month" to "test_ad_1month"     // $39.99
        )
    }
    
    /**
     * Get Stripe payment link URL for subscription
     */
    fun getSubscriptionPaymentUrl(planKey: String, userEmail: String? = null): String {
        val linkId = SUBSCRIPTION_LINKS[planKey] ?: SUBSCRIPTION_LINKS["1_week"]!!
        var url = "$STRIPE_CHECKOUT_BASE$linkId"
        
        // Pre-fill email if available
        if (!userEmail.isNullOrBlank()) {
            url += "?prefilled_email=${java.net.URLEncoder.encode(userEmail, "UTF-8")}"
        }
        
        Log.d(TAG, "Subscription payment URL: $url")
        return url
    }
    
    /**
     * Get Stripe payment link URL for ad plan
     */
    fun getAdPaymentUrl(planKey: String, userEmail: String? = null): String {
        val linkId = AD_PLAN_LINKS[planKey] ?: AD_PLAN_LINKS["1_week"]!!
        var url = "$STRIPE_CHECKOUT_BASE$linkId"
        
        // Pre-fill email if available
        if (!userEmail.isNullOrBlank()) {
            url += "?prefilled_email=${java.net.URLEncoder.encode(userEmail, "UTF-8")}"
        }
        
        Log.d(TAG, "Ad payment URL: $url")
        return url
    }
    
    /**
     * Generate a dynamic payment URL with amount
     * This uses Stripe Payment Links with dynamic pricing
     */
    fun getPaymentUrl(amount: Double, description: String, userEmail: String? = null): String {
        // For dynamic amounts, we create a generic payment link
        // In production, this would call your backend to create a Checkout Session
        
        // For now, use a manual payment approach
        val formattedAmount = String.format("%.2f", amount)
        Log.d(TAG, "Payment request: $formattedAmount USD - $description")
        
        // Return a placeholder - in production, this would be a real Stripe Checkout Session URL
        return "https://checkout.stripe.com/pay/placeholder"
    }
}
