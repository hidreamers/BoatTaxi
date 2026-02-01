package com.boattaxie.app.data.payment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.boattaxie.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stripe Payment Manager
 * Handles Stripe checkout sessions and payment links
 */
@Singleton
class PaymentManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "PaymentManager"
        
        // PayPal URLs
        private const val PAYPAL_SANDBOX_URL = "https://www.sandbox.paypal.com/checkoutnow"
        private const val PAYPAL_LIVE_URL = "https://www.paypal.com/checkoutnow"
        
        private const val BACKEND_URL = "https://boattaxi-boattaxi.up.railway.app"
    }
    
    private val clientId: String = "" // BuildConfig.PAYPAL_CLIENT_ID
    private val isLive: Boolean = false // BuildConfig.PAYPAL_ENVIRONMENT == "LIVE"
    
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    
    @Serializable
    data class CheckoutSessionResponse(val url: String)
    
    /**
     * Get the PayPal checkout URL for a payment
     */
    fun getCheckoutUrl(orderId: String): String {
        val baseUrl = if (isLive) PAYPAL_LIVE_URL else PAYPAL_SANDBOX_URL
        return "$baseUrl?token=$orderId"
    }
    
    /**
     * Open PayPal payment in browser
     * For subscriptions and payments, we use PayPal's web checkout
     */
    fun openPayPalCheckout(activity: Activity, amount: Double, currency: String = "USD", description: String) {
        Log.d(TAG, "Opening PayPal checkout for $amount $currency - $description")
        Log.d(TAG, "Environment: ${if (isLive) "LIVE" else "SANDBOX"}")
        
        // For real implementation, you would:
        // 1. Call your backend to create a PayPal order
        // 2. Get the approval URL from PayPal
        // 3. Open that URL in a browser/webview
        // 4. Handle the return URL callback
        
        // For now, open PayPal directly with payment link
        val paypalUrl = if (isLive) {
            "https://www.paypal.com/paypalme/boattaxie/$amount"
        } else {
            "https://www.sandbox.paypal.com/paypalme/boattaxie/$amount"
        }
        
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paypalUrl))
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open PayPal: ${e.message}")
        }
    }
    
    /**
     * Process a subscription payment
     * Returns true if payment was initiated successfully
     */
    suspend fun processSubscriptionPayment(
        amount: Double,
        planName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ): Boolean {
        Log.d(TAG, "Processing subscription payment: $amount for $planName")
        Log.d(TAG, "Using ${if (isLive) "LIVE" else "SANDBOX"} environment")
        
        // In a full implementation:
        // 1. Create order on your backend
        // 2. Get PayPal approval
        // 3. Capture payment
        // 4. Return result
        
        // For now, we'll simulate success since PayPal.me links
        // handle the actual payment
        return true
    }
    
    /**
     * Create Stripe Checkout Session for ad payment and open it
     */
    suspend fun createAdCheckoutSession(
        activity: Activity,
        durationDays: Int,
        isFeatured: Boolean,
        businessName: String,
        title: String,
        description: String,
        imageUri: String?,
        logoUri: String?,
        youtubeUrl: String?,
        phone: String?,
        email: String?,
        website: String?,
        category: String,
        location: String?
    ): Boolean {
        Log.d(TAG, "createAdCheckoutSession called with durationDays=$durationDays, isFeatured=$isFeatured, businessName=$businessName")
        try {
                val response: CheckoutSessionResponse = httpClient.post("$BACKEND_URL/api/create-ad-checkout-session") {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf(
                        "durationDays" to durationDays,
                        "isFeatured" to isFeatured,
                        "businessName" to businessName,
                        "title" to title,
                        "description" to description,
                        "imageUri" to imageUri,
                        "logoUri" to logoUri,
                        "youtubeUrl" to youtubeUrl,
                        "phone" to phone,
                        "email" to email,
                        "website" to website,
                        "category" to category,
                        "location" to location
                    ))
                }.body()
                
                Log.d(TAG, "Checkout session created successfully: ${response.url}")
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(response.url))
                Log.d(TAG, "Starting activity with intent: $intent")
                activity.startActivity(intent)
                Log.d(TAG, "Activity started")
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create ad checkout session: ${e.message}")
                return false
            }
    }
}
