package com.boattaxie.app.ui.screens.subscription

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.*
import com.boattaxie.app.BuildConfig
import com.boattaxie.app.data.model.*
import com.boattaxie.app.data.repository.AuthRepository
import com.boattaxie.app.data.repository.SubscriptionRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.Calendar
import java.util.UUID

data class SubscriptionUiState(
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val hasActiveSubscription: Boolean = false,
    val currentSubscription: Subscription? = null,
    val selectedPlan: SubscriptionPlan? = null,
    val paymentSuccess: Boolean = false,
    val errorMessage: String? = null,
    val paypalUrl: String? = null, // URL to open for PayPal payment
    // Promo code
    val promoCodeMessage: String? = null,
    val promoCodeSuccess: Boolean = false,
    val appliedPromoCode: String? = null,
    // Free bookings from promo
    val hasFreeBookings: Boolean = false,
    val userPromoCode: String? = null
)

class SubscriptionViewModel(
    private val subscriptionRepository: SubscriptionRepository,
    private val authRepository: AuthRepository,
    private val application: Application
) : ViewModel(), PurchasesUpdatedListener {
    
    companion object {
        private const val TAG = "SubscriptionVM"
        
        // Product IDs must match Google Play Console
        val SUBSCRIPTION_PRODUCTS = listOf(
            "day_pass",
            "three_day_pass",
            "five_day_pass",
            "week_pass",
            "two_week_pass",
            "month_pass"
        )
        
        val AUTO_RENEWING_SUBSCRIPTIONS = listOf(
            "month_pass_auto"
        )
    }
    
    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()
    
    private var pendingPlan: SubscriptionPlan? = null
    private var billingClient: BillingClient? = null
    private var availableProducts = mutableListOf<ProductDetails>()
    
    init {
        loadSubscription()
        loadPromoStatus()
        initializeBilling()
    }
    
    private fun initializeBilling() {
        Log.d(TAG, "Initializing billing client")
        billingClient = BillingClient.newBuilder(application)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing client connected successfully")
                    queryProducts()
                    // Consume any existing unconsumed purchases to allow re-purchase
                    consumeExistingPurchases()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                    _uiState.update { it.copy(errorMessage = "Failed to connect to Google Play: ${billingResult.debugMessage}") }
                }
            }
            
            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
            }
        })
    }
    
    /**
     * Query and consume any existing purchases that weren't consumed.
     * This is needed for one-time subscription passes to be re-purchasable.
     */
    private fun consumeExistingPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Found ${purchases.size} existing INAPP purchases")
                for (purchase in purchases) {
                    val productId = purchase.products.firstOrNull() ?: continue
                    // Only consume non-auto-renewing products
                    if (productId !in AUTO_RENEWING_SUBSCRIPTIONS) {
                        Log.d(TAG, "Consuming existing purchase: $productId")
                        val consumeParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        billingClient?.consumeAsync(consumeParams) { result, _ ->
                            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                                Log.d(TAG, "Successfully consumed old purchase: $productId")
                            } else {
                                Log.e(TAG, "Failed to consume: ${result.debugMessage}")
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun queryProducts() {
        // Query one-time in-app products
        val inappProductList = SUBSCRIPTION_PRODUCTS.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        
        val inappParams = QueryProductDetailsParams.newBuilder()
            .setProductList(inappProductList)
            .build()
        
        billingClient?.queryProductDetailsAsync(inappParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                availableProducts.addAll(productDetailsList)
                Log.d(TAG, "Loaded ${productDetailsList.size} in-app subscription products: ${productDetailsList.map { it.productId }}")
            } else {
                Log.e(TAG, "Failed to query in-app products: ${billingResult.debugMessage}")
            }
        }
        
        // Query auto-renewing subscriptions
        val subscriptionList = AUTO_RENEWING_SUBSCRIPTIONS.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        
        val subsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(subscriptionList)
            .build()
        
        billingClient?.queryProductDetailsAsync(subsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                availableProducts.addAll(productDetailsList)
                Log.d(TAG, "Loaded ${productDetailsList.size} auto-renewing subscriptions: ${productDetailsList.map { it.productId }}")
            } else {
                Log.e(TAG, "Failed to query subscriptions: ${billingResult.debugMessage}")
            }
        }
    }
    
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        Log.d(TAG, "onPurchasesUpdated: responseCode=${billingResult.responseCode}")
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    Log.d(TAG, "Purchase received: ${purchase.products}, state=${purchase.purchaseState}")
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        onGooglePlayPurchaseSuccess(purchase.purchaseToken, purchase.orderId)
                        acknowledgePurchase(purchase)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "Purchase cancelled by user")
                _uiState.update { it.copy(isProcessing = false, errorMessage = "Purchase cancelled") }
            }
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
                Log.e(TAG, "Item unavailable - may already own this item")
                _uiState.update { it.copy(isProcessing = false, errorMessage = "This item is unavailable. You may already have an active subscription.") }
            }
            else -> {
                Log.e(TAG, "Purchase failed: ${billingResult.debugMessage}")
                _uiState.update { it.copy(isProcessing = false, errorMessage = "Purchase failed: ${billingResult.debugMessage}") }
            }
        }
    }
    
    /**
     * Consume the purchase so it can be bought again later.
     * This is needed for subscription passes (one-time products that should be re-purchasable).
     */
    private fun consumePurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        
        billingClient?.consumeAsync(consumeParams) { billingResult, purchaseToken ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase consumed successfully - can be purchased again")
            } else {
                Log.e(TAG, "Failed to consume purchase: ${billingResult.debugMessage}")
            }
        }
    }
    
    private fun acknowledgePurchase(purchase: Purchase) {
        // For one-time subscription passes, consume instead of acknowledge
        // This allows users to buy the same pass again after it expires
        val productId = purchase.products.firstOrNull() ?: ""
        val isAutoRenewing = productId in AUTO_RENEWING_SUBSCRIPTIONS
        
        if (isAutoRenewing) {
            // Auto-renewing subscriptions should be acknowledged, not consumed
            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                
                billingClient?.acknowledgePurchase(params) { result ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Auto-renewing subscription acknowledged")
                    } else {
                        Log.e(TAG, "Failed to acknowledge: ${result.debugMessage}")
                    }
                }
            }
        } else {
            // One-time passes should be consumed so they can be purchased again
            consumePurchase(purchase)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        billingClient?.endConnection()
    }
    
    private fun loadSubscription() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            subscriptionRepository.observeSubscription().collect { subscription ->
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        hasActiveSubscription = subscription != null,
                        currentSubscription = subscription
                    )
                }
            }
        }
    }
    
    private fun loadPromoStatus() {
        viewModelScope.launch {
            // Check if user has free bookings from promo code
            val hasFree = authRepository.hasFreeBookingsForLife()
            
            // Get user's applied promo code
            authRepository.observeCurrentUser().collect { user ->
                _uiState.update {
                    it.copy(
                        hasFreeBookings = hasFree,
                        userPromoCode = user?.appliedPromoCode
                    )
                }
            }
        }
    }
    
    fun selectPlan(plan: SubscriptionPlan) {
        _uiState.update { it.copy(selectedPlan = plan) }
    }
    
    /**
     * Start Google Play In-App Purchase for subscription
     */
    fun startGooglePlayPayment(activity: Activity, plan: SubscriptionPlan) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _uiState.update { it.copy(isProcessing = false, errorMessage = "User not logged in") }
            return
        }

        pendingPlan = plan
        _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
        
        // Convert plan name to Google Play product ID
        val productId = getProductIdForPlan(plan)
        Log.d(TAG, "Starting purchase for product: $productId")
        
        // Find product details
        val productDetails = availableProducts.find { it.productId == productId }
        if (productDetails == null) {
            Log.e(TAG, "Product not found: $productId. Available: ${availableProducts.map { it.productId }}")
            _uiState.update { 
                it.copy(
                    isProcessing = false, 
                    errorMessage = "Product '$productId' not configured in Google Play Console. Please try again later."
                ) 
            }
            return
        }
        
        // Build purchase params
        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
        
        // For auto-renewing subscriptions, include offer token
        if (plan.isAutoRenew && productDetails.subscriptionOfferDetails?.isNotEmpty() == true) {
            productDetailsParamsBuilder.setOfferToken(
                productDetails.subscriptionOfferDetails!![0].offerToken
            )
        }
        
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build()))
            .build()
        
        // Launch Google Play purchase dialog
        val result = billingClient?.launchBillingFlow(activity, billingFlowParams)
        Log.d(TAG, "launchBillingFlow result: ${result?.responseCode}")
        
        if (result?.responseCode != BillingClient.BillingResponseCode.OK) {
            _uiState.update { 
                it.copy(
                    isProcessing = false, 
                    errorMessage = "Failed to launch Google Play: ${result?.debugMessage}"
                ) 
            }
        }
    }
    
    private fun getProductIdForPlan(plan: SubscriptionPlan): String {
        return when (plan) {
            SubscriptionPlan.DAY_PASS -> "day_pass"
            SubscriptionPlan.THREE_DAY_PASS -> "three_day_pass"
            SubscriptionPlan.FIVE_DAY_PASS -> "five_day_pass"
            SubscriptionPlan.WEEK_PASS -> "week_pass"
            SubscriptionPlan.TWO_WEEK_PASS -> "two_week_pass"
            SubscriptionPlan.MONTH_PASS -> "month_pass"
            SubscriptionPlan.MONTH_PASS_AUTO -> "month_pass_auto"
        }
    }

    private fun checkForSuccessfulPayment() {
        val prefs = application.getSharedPreferences("payment_prefs", Application.MODE_PRIVATE)
        val planName = prefs.getString("successful_payment_plan", null)
        val timestamp = prefs.getLong("payment_timestamp", 0)
        
        if (planName != null && timestamp > 0) {
            // Check if the payment was recent (within last 5 minutes)
            val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
            if (timestamp > fiveMinutesAgo) {
                // Clear the stored payment
                prefs.edit().clear().apply()
                
                // Find the plan and activate subscription
                val plan = SubscriptionPlan.values().find { it.name == planName }
                if (plan != null) {
                    activateSubscriptionAfterPayment(plan)
                }
            } else {
                // Clear old payment data
                prefs.edit().clear().apply()
            }
        }
    }
    
    private fun activateSubscriptionAfterPayment(plan: SubscriptionPlan) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            
            val subscriptionResult = subscriptionRepository.createSubscription(
                plan = plan,
                paymentMethodId = "google_play_iap",
                paypalOrderId = null
            )
            
            subscriptionResult.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            paymentSuccess = true,
                            selectedPlan = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = "Failed to activate subscription: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    /**
     * Called when Google Play purchase is successful
     */
    fun onGooglePlayPurchaseSuccess(purchaseToken: String, orderId: String?) {
        val plan = pendingPlan ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            
            // Send purchase to backend for verification and activation
            try {
                verifyPurchaseWithBackend(plan, purchaseToken, orderId)
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        paymentSuccess = true,
                        selectedPlan = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Failed to verify purchase: ${e.message}"
                    )
                }
            }
        }
    }
    
    private suspend fun verifyPurchaseWithBackend(plan: SubscriptionPlan, purchaseToken: String, orderId: String?) = withContext(Dispatchers.IO) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: throw Exception("Not logged in")
        val firestore = FirebaseFirestore.getInstance()
        
        // Calculate subscription dates
        val now = Calendar.getInstance()
        val startDate = Timestamp(now.time)
        now.add(Calendar.DAY_OF_YEAR, plan.days)
        val endDate = Timestamp(now.time)
        
        // Generate subscription ID
        val subscriptionId = UUID.randomUUID().toString()
        
        // Create subscription document directly in Firestore
        // Note: Don't include "id" field - @DocumentId annotation handles it from document path
        val subscriptionData = hashMapOf(
            "userId" to userId,
            "plan" to plan.name,
            "status" to "active",
            "startDate" to startDate,
            "endDate" to endDate,
            "autoRenew" to plan.isAutoRenew,
            "purchaseToken" to purchaseToken,
            "orderId" to (orderId ?: ""),
            "price" to plan.price,
            "currency" to "USD",
            "paymentMethodId" to "google_play_iap",
            "createdAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now()
        )
        
        // Write to Firestore
        firestore.collection("subscriptions")
            .document(subscriptionId)
            .set(subscriptionData)
            .await()
        
        // Also update user's subscription status
        firestore.collection("users")
            .document(userId)
            .update(mapOf(
                "hasActiveSubscription" to true,
                "subscriptionEndDate" to endDate,
                "subscriptionPlan" to plan.name
            ))
            .await()
    }



    fun cancelSubscription() {
        val subscriptionId = _uiState.value.currentSubscription?.id ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            
            val result = subscriptionRepository.cancelSubscription(subscriptionId)
            
            result.fold(
                onSuccess = {
                    _uiState.update { 
                        it.copy(
                            isProcessing = false,
                            hasActiveSubscription = false,
                            currentSubscription = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isProcessing = false,
                            errorMessage = error.message ?: "Failed to cancel subscription"
                        )
                    }
                }
            )
        }
    }
    
    fun onAppResume() {
        // Check for successful payment when app resumes
        checkForSuccessfulPayment()
    }
    
    /**
     * Apply a promo code with anti-abuse protection
     * Gets device ID and public IP to prevent abuse across multiple accounts
     */
    fun applyPromoCode(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, promoCodeMessage = null) }
            
            try {
                // Get device ID (Android ID - unique per device/app install)
                val deviceId = try {
                    val id = Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID)
                    android.util.Log.d("SubscriptionVM", "Got device ID: $id")
                    id
                } catch (e: Exception) {
                    android.util.Log.e("SubscriptionVM", "Failed to get device ID: ${e.message}")
                    null
                }
                
                // Get public IP address for anti-abuse tracking
                val ipAddress = withContext(Dispatchers.IO) {
                    try {
                        val ip = URL("https://api.ipify.org").readText().trim()
                        android.util.Log.d("SubscriptionVM", "Got IP address: $ip")
                        ip
                    } catch (e: Exception) {
                        android.util.Log.w("SubscriptionVM", "Could not get IP address: ${e.message}")
                        null
                    }
                }
                
                android.util.Log.d("SubscriptionVM", "=== CALLING PROMO CODE ===")
                android.util.Log.d("SubscriptionVM", "Code: $code")
                android.util.Log.d("SubscriptionVM", "Device ID: $deviceId (length: ${deviceId?.length ?: 0})")
                android.util.Log.d("SubscriptionVM", "IP Address: $ipAddress")
                
                if (deviceId.isNullOrBlank()) {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            promoCodeMessage = "⛔ Unable to verify device. Please restart the app and try again.",
                            promoCodeSuccess = false
                        )
                    }
                    return@launch
                }
                
                if (ipAddress.isNullOrBlank()) {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            promoCodeMessage = "⛔ Unable to verify network. Please check your internet connection.",
                            promoCodeSuccess = false
                        )
                    }
                    return@launch
                }
                
                val result = authRepository.applyPromoCode(code, deviceId, ipAddress)
                result.fold(
                    onSuccess = { message ->
                        android.util.Log.d("SubscriptionVM", "Promo SUCCESS: $message")
                        _uiState.update { 
                            it.copy(
                                isProcessing = false,
                                promoCodeMessage = message,
                                promoCodeSuccess = true,
                                appliedPromoCode = code.uppercase()
                            )
                        }
                    },
                    onFailure = { error ->
                        android.util.Log.e("SubscriptionVM", "Promo FAILED: ${error.message}")
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                promoCodeMessage = error.message ?: "Invalid promo code",
                                promoCodeSuccess = false
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("SubscriptionVM", "Promo EXCEPTION: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        promoCodeMessage = e.message ?: "An error occurred. Please try again.",
                        promoCodeSuccess = false
                    )
                }
            }
        }
    }
    
    fun clearPromoMessage() {
        _uiState.update { it.copy(promoCodeMessage = null) }
    }
}
