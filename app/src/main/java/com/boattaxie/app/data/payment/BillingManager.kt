package com.boattaxie.app.data.payment

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Play Billing Manager
 * Handles In-App Purchases through Google Play
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener {
    
    companion object {
        private const val TAG = "BillingManager"
        
        // One-time subscription passes (must match Google Play Console - In-app products)
        val SUBSCRIPTION_PRODUCTS = listOf(
            "day_pass",
            "three_day_pass",
            "five_day_pass",
            "week_pass",
            "two_week_pass",
            "month_pass"
        )
        
        // Auto-renewing subscriptions (must match Google Play Console - Subscriptions)
        val AUTO_RENEWING_SUBSCRIPTIONS = listOf(
            "month_pass_auto"
        )
        
        // Ad product IDs (one-time purchases)
        val AD_PRODUCTS = listOf(
            "ad_standard_1day",
            "ad_standard_3day",
            "ad_standard_7day",
            "ad_standard_14day",
            "ad_standard_30day",
            "ad_featured_1day",
            "ad_featured_3day",
            "ad_featured_7day",
            "ad_featured_14day",
            "ad_featured_30day"
        )
        
        // Auto-renewing ad subscriptions (monthly recurring)
        val AD_AUTO_RENEWING = listOf(
            "ad_standard_monthly_auto",
            "ad_featured_monthly_auto"
        )
    }
    
    private var billingClient: BillingClient? = null
    
    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState
    
    private val _availableProducts = MutableStateFlow<List<ProductDetails>>(emptyList())
    val availableProducts: StateFlow<List<ProductDetails>> = _availableProducts
    
    sealed class PurchaseState {
        object Idle : PurchaseState()
        object Loading : PurchaseState()
        data class Success(val purchase: Purchase) : PurchaseState()
        data class Error(val message: String) : PurchaseState()
    }
    
    fun initialize() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing client connected")
                    queryProducts()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }
            
            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
            }
        })
    }
    
    private fun queryProducts() {
        // Query one-time in-app products
        val inappProductIds = SUBSCRIPTION_PRODUCTS + AD_PRODUCTS
        val inappProductList = inappProductIds.map { productId ->
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
                val currentProducts = _availableProducts.value.toMutableList()
                currentProducts.addAll(productDetailsList)
                _availableProducts.value = currentProducts
                Log.d(TAG, "Loaded ${productDetailsList.size} in-app products")
            } else {
                Log.e(TAG, "Failed to query in-app products: ${billingResult.debugMessage}")
            }
        }
        
        // Query auto-renewing subscriptions
        val subscriptionIds = AUTO_RENEWING_SUBSCRIPTIONS + AD_AUTO_RENEWING
        val subscriptionList = subscriptionIds.map { productId ->
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
                val currentProducts = _availableProducts.value.toMutableList()
                currentProducts.addAll(productDetailsList)
                _availableProducts.value = currentProducts
                Log.d(TAG, "Loaded ${productDetailsList.size} subscription products")
            } else {
                Log.e(TAG, "Failed to query subscriptions: ${billingResult.debugMessage}")
            }
        }
    }
    
    fun launchPurchase(activity: Activity, productId: String) {
        val productDetails = _availableProducts.value.find { it.productId == productId }
        if (productDetails == null) {
            _purchaseState.value = PurchaseState.Error("Product not found: $productId")
            return
        }
        
        _purchaseState.value = PurchaseState.Loading
        
        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
        
        // For subscriptions, we need to include the offer token
        val isSubscription = productId in AUTO_RENEWING_SUBSCRIPTIONS || productId in AD_AUTO_RENEWING
        if (isSubscription && productDetails.subscriptionOfferDetails?.isNotEmpty() == true) {
            productDetailsParamsBuilder.setOfferToken(
                productDetails.subscriptionOfferDetails!![0].offerToken
            )
        }
        
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build()))
            .build()
        
        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }
    
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        _purchaseState.value = PurchaseState.Success(purchase)
                        acknowledgePurchase(purchase)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseState.Error("Purchase cancelled")
            }
            else -> {
                _purchaseState.value = PurchaseState.Error("Purchase failed: ${billingResult.debugMessage}")
            }
        }
    }
    
    private fun acknowledgePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            
            billingClient?.acknowledgePurchase(params) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Purchase acknowledged")
                }
            }
        }
    }
    
    fun resetState() {
        _purchaseState.value = PurchaseState.Idle
    }
    
    fun destroy() {
        billingClient?.endConnection()
        billingClient = null
    }
}
