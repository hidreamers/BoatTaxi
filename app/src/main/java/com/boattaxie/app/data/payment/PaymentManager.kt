package com.boattaxie.app.data.payment

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import com.android.billingclient.api.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Play Billing Payment Manager for Ads
 * Handles In-App Purchases through Google Play for advertisements
 * Flow: 
 * 1. User selects ad plan and fills in details
 * 2. User pays via Google Play Billing
 * 3. App creates ad directly in Firestore after successful purchase
 * 4. Ad is immediately active with proper start/end dates
 */
@Singleton
class PaymentManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener {
    companion object {
        private const val TAG = "PaymentManager"
    }
    
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var billingClient: BillingClient? = null
    private var pendingAdData: PendingAdData? = null
    private var pendingActivity: Activity? = null
    
    private val _purchaseState = MutableStateFlow<AdPurchaseState>(AdPurchaseState.Idle)
    val purchaseState: StateFlow<AdPurchaseState> = _purchaseState
    
    private val _availableProducts = MutableStateFlow<List<ProductDetails>>(emptyList())
    val availableProducts: StateFlow<List<ProductDetails>> = _availableProducts
    
    sealed class AdPurchaseState {
        object Idle : AdPurchaseState()
        object Loading : AdPurchaseState()
        data class Success(val adId: String) : AdPurchaseState()
        data class Error(val message: String) : AdPurchaseState()
    }
    
    data class PendingAdData(
        val durationDays: Int,
        val isFeatured: Boolean,
        val isAutoRenew: Boolean,
        val businessName: String,
        val title: String,
        val description: String,
        val imageUri: String?,
        val logoUri: String?,
        val youtubeUrl: String?,
        val phone: String?,
        val email: String?,
        val website: String?,
        val category: String,
        val location: String?,
        val userId: String,
        val existingAdId: String?
    )
    
    fun initialize() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing client connected")
                    queryAdProducts()
                    // Consume any existing unconsumed ad purchases
                    consumeExistingAdPurchases()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }
            
            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
            }
        })
    }
    
    // Store existing unconsumed purchases for recovery
    private var existingPurchases: List<Purchase> = emptyList()
    
    /**
     * Query existing purchases - DON'T consume them automatically.
     * They will be consumed only after ad creation succeeds.
     * This allows recovery if app crashed during ad creation.
     */
    private fun consumeExistingAdPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Found ${purchases.size} existing INAPP purchases (NOT consuming - waiting for ad creation)")
                existingPurchases = purchases.filter { purchase ->
                    val productId = purchase.products.firstOrNull() ?: ""
                    // Keep track of ad purchases (not auto-renewing)
                    !productId.contains("monthly_auto") && !productId.contains("_auto") &&
                    productId.contains("ad_") || productId.contains("adfeatured")
                }
                if (existingPurchases.isNotEmpty()) {
                    Log.d(TAG, "Recoverable ad purchases: ${existingPurchases.map { it.products.firstOrNull() }}")
                }
            } else {
                Log.e(TAG, "Failed to query purchases: ${billingResult.debugMessage}")
            }
        }
    }
    
    /**
     * Check if there's an existing purchase that can be used instead of a new purchase.
     * This recovers promo code purchases that didn't create an ad.
     */
    fun hasRecoverablePurchase(productId: String): Purchase? {
        return existingPurchases.find { it.products.contains(productId) }
    }
    
    /**
     * Recover an existing purchase and create the ad from it.
     */
    fun recoverPurchase(purchase: Purchase) {
        Log.d(TAG, "Recovering purchase: ${purchase.products.firstOrNull()}")
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            verifyPurchaseAndCreateAd(purchase)
        }
    }
    
    private fun queryAdProducts() {
        // Ad product IDs (one-time purchases) - must match Play Console exactly
        val adProductIds = listOf(
            "ad_standard_1day",
            "ad_standard_3day",
            "ad_standard_7day",
            "ad_standard_14day",
            "ad_standard_30day",
            "ad_featured_1day",
            "adfeatured3day",      // Play Console has no underscores
            "ad_featured_7day",
            "adfeatured14day",     // Play Console has no underscores
            "ad_featured_30day"
        )
        
        val inappProductList = adProductIds.map { productId ->
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
                Log.d(TAG, "Loaded ${productDetailsList.size} ad in-app products: ${productDetailsList.map { it.productId }}")
            } else {
                Log.e(TAG, "Failed to query ad products: ${billingResult.debugMessage}")
            }
        }
        
        // Query auto-renewing ad subscriptions
        val adSubscriptionIds = listOf(
            "ad_standard_monthly_auto",
            "ad_featured_monthly_auto"
        )
        
        val subscriptionList = adSubscriptionIds.map { productId ->
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
                Log.d(TAG, "Loaded ${productDetailsList.size} ad subscription products")
            } else {
                Log.e(TAG, "Failed to query ad subscriptions: ${billingResult.debugMessage}")
            }
        }
    }
    
    /**
     * Create ad checkout session using Google Play Billing
     * 1. First checks for recoverable purchases (promo codes that didn't create ads)
     * 2. If found, creates ad directly without new purchase
     * 3. Otherwise launches Google Play purchase flow
     * 4. On success, verifies purchase with backend to activate ad
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
        location: String?,
        userId: String = "",
        existingAdId: String? = null,
        isAutoRenew: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "createAdCheckoutSession: durationDays=$durationDays, isFeatured=$isFeatured, isAutoRenew=$isAutoRenew, businessName=$businessName")
        
        _purchaseState.value = AdPurchaseState.Loading
        
        // Store ad data for after purchase completes
        pendingAdData = PendingAdData(
            durationDays = durationDays,
            isFeatured = isFeatured,
            isAutoRenew = isAutoRenew,
            businessName = businessName,
            title = title,
            description = description,
            imageUri = imageUri,
            logoUri = logoUri,
            youtubeUrl = youtubeUrl,
            phone = phone,
            email = email,
            website = website,
            category = category,
            location = location,
            userId = userId,
            existingAdId = existingAdId
        )
        pendingActivity = activity
        
        // Determine product ID based on duration and featured status
        val productId = getAdProductId(durationDays, isFeatured, isAutoRenew)
        Log.d(TAG, "Using product ID: $productId")
        
        // Check for existing recoverable purchase (e.g., promo code that didn't create ad)
        val recoverablePurchase = hasRecoverablePurchase(productId)
        if (recoverablePurchase != null) {
            Log.d(TAG, "Found recoverable purchase for $productId - creating ad without new purchase")
            recoverPurchase(recoverablePurchase)
            return@withContext true
        }
        
        // Also check for ANY ad purchase that hasn't been consumed (different product IDs)
        if (existingPurchases.isNotEmpty()) {
            val anyAdPurchase = existingPurchases.firstOrNull { 
                it.purchaseState == Purchase.PurchaseState.PURCHASED 
            }
            if (anyAdPurchase != null) {
                Log.d(TAG, "Found existing ad purchase (${anyAdPurchase.products.firstOrNull()}) - using it for this ad")
                recoverPurchase(anyAdPurchase)
                return@withContext true
            }
        }
        
        // Launch Google Play purchase
        withContext(Dispatchers.Main) {
            launchAdPurchase(activity, productId)
        }
        
        return@withContext true
    }
    
    private fun getAdProductId(durationDays: Int, isFeatured: Boolean, isAutoRenew: Boolean = false): String {
        if (isAutoRenew) {
            return if (isFeatured) "ad_featured_monthly_auto" else "ad_standard_monthly_auto"
        }
        // Product IDs must match Play Console exactly
        // Note: Some featured ad IDs don't have underscores in Play Console
        return when {
            isFeatured && durationDays == 3 -> "adfeatured3day"
            isFeatured && durationDays == 14 -> "adfeatured14day"
            isFeatured -> "ad_featured_${durationDays}day"
            else -> "ad_standard_${durationDays}day"
        }
    }
    
    private fun launchAdPurchase(activity: Activity, productId: String) {
        val productDetails = _availableProducts.value.find { it.productId == productId }
        if (productDetails == null) {
            _purchaseState.value = AdPurchaseState.Error("Product not found: $productId. Make sure products are configured in Google Play Console.")
            return
        }
        
        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
        
        // For subscriptions, include offer token
        val isSubscription = productId.contains("monthly_auto")
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
        Log.d(TAG, "onPurchasesUpdated: responseCode=${billingResult.responseCode}, message=${billingResult.debugMessage}")
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.d(TAG, "Purchase successful, processing ${purchases?.size ?: 0} purchases")
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        // Verify and create ad on backend
                        verifyPurchaseAndCreateAd(purchase)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = AdPurchaseState.Error("Purchase cancelled")
                clearPendingData()
            }
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
                Log.e(TAG, "ITEM_UNAVAILABLE - product may already be owned or not found")
                _purchaseState.value = AdPurchaseState.Error("Item not available - you may already own this product")
                clearPendingData()
            }
            else -> {
                Log.e(TAG, "Purchase failed: ${billingResult.debugMessage}")
                _purchaseState.value = AdPurchaseState.Error("Purchase failed: ${billingResult.debugMessage}")
                clearPendingData()
            }
        }
    }
    
    private fun verifyPurchaseAndCreateAd(purchase: Purchase) {
        val adData = pendingAdData ?: run {
            _purchaseState.value = AdPurchaseState.Error("No pending ad data")
            return
        }
        
        // Consume the purchase so user can buy ads again
        // One-time ad products need to be consumed to allow re-purchase
        val productId = purchase.products.firstOrNull() ?: ""
        val isAutoRenewing = productId.contains("monthly_auto")
        
        if (isAutoRenewing) {
            // Auto-renewing ad subscriptions should be acknowledged
            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                
                billingClient?.acknowledgePurchase(params) { result ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Auto-renewing ad subscription acknowledged")
                        CoroutineScope(Dispatchers.IO).launch {
                            createAdInFirestore(purchase, adData)
                        }
                    } else {
                        Log.e(TAG, "Failed to acknowledge: ${result.debugMessage}")
                        _purchaseState.value = AdPurchaseState.Error("Failed to acknowledge purchase")
                    }
                }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    createAdInFirestore(purchase, adData)
                }
            }
        } else {
            // One-time ad products should be consumed so they can be purchased again
            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            
            billingClient?.consumeAsync(consumeParams) { result, _ ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Ad purchase consumed - can buy again")
                    CoroutineScope(Dispatchers.IO).launch {
                        createAdInFirestore(purchase, adData)
                    }
                } else {
                    Log.e(TAG, "Failed to consume: ${result.debugMessage}")
                    _purchaseState.value = AdPurchaseState.Error("Failed to process purchase")
                }
            }
        }
    }
    
    private suspend fun createAdInFirestore(purchase: Purchase, adData: PendingAdData) = withContext(Dispatchers.IO) {
        try {
            val now = Calendar.getInstance()
            val startDate = Timestamp(now.time)
            
            // Calculate end date based on duration
            now.add(Calendar.DAY_OF_YEAR, adData.durationDays)
            val endDate = Timestamp(now.time)
            
            // Generate ad ID or use existing
            val adId = adData.existingAdId ?: UUID.randomUUID().toString()
            
            // Upload images to Firebase Storage
            val imageUrl = uploadImageToStorage(adData.imageUri, adId, "image")
            val logoUrl = uploadImageToStorage(adData.logoUri, adId, "logo")
            
            Log.d(TAG, "Image uploaded: $imageUrl, Logo uploaded: $logoUrl")
            
            val adDocument = hashMapOf(
                "id" to adId,
                "advertiserId" to adData.userId, // Match AdvertisementRepository field name
                "userId" to adData.userId,
                "businessName" to adData.businessName,
                "title" to adData.title,
                "description" to adData.description,
                "imageUrl" to imageUrl,
                "logoUrl" to logoUrl,
                "youtubeUrl" to (adData.youtubeUrl ?: ""),
                "phoneNumber" to (adData.phone ?: ""),
                "email" to (adData.email ?: ""),
                "websiteUrl" to (adData.website ?: ""),
                "category" to adData.category,
                "locationName" to (adData.location ?: ""),
                "isFeatured" to adData.isFeatured,
                "isAutoRenew" to adData.isAutoRenew,
                "status" to "ACTIVE", // Use uppercase to match AdvertisementRepository queries
                "startDate" to startDate,
                "endDate" to endDate,
                "durationDays" to adData.durationDays,
                "purchaseToken" to purchase.purchaseToken,
                "productId" to (purchase.products.firstOrNull() ?: ""),
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now(),
                "impressions" to 0, // Match field names used in AdvertisementRepository
                "clicks" to 0
            )

            Log.d(TAG, "Creating ad in Firestore: $adId")

            // Write to 'advertisements' collection to match AdvertisementRepository
            firestore.collection("advertisements")
                .document(adId)
                .set(adDocument)
                .await()

            Log.d(TAG, "Ad created successfully: $adId")
            _purchaseState.value = AdPurchaseState.Success(adId)
            clearPendingData()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create ad in Firestore: ${e.message}", e)
            _purchaseState.value = AdPurchaseState.Error("Failed to create ad: ${e.message}")
            clearPendingData()
        }
    }
    
    /**
     * Upload an image to Firebase Storage and return the download URL
     */
    private suspend fun uploadImageToStorage(localUri: String?, adId: String, type: String): String {
        if (localUri.isNullOrBlank()) return ""
        
        try {
            val uri = when {
                localUri.startsWith("content://") -> Uri.parse(localUri)
                localUri.startsWith("/") -> Uri.fromFile(File(localUri))
                localUri.startsWith("file://") -> Uri.parse(localUri)
                else -> return localUri // Already a URL, return as-is
            }
            
            val storageRef = storage.reference
                .child("ads")
                .child(adId)
                .child("${type}_${System.currentTimeMillis()}.jpg")
            
            Log.d(TAG, "Uploading $type to Storage: $uri")
            
            storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            
            Log.d(TAG, "$type uploaded successfully: $downloadUrl")
            return downloadUrl
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload $type: ${e.message}", e)
            return "" // Return empty on failure, ad will still be created without image
        }
    }
    
    private fun clearPendingData() {
        pendingAdData = null
        pendingActivity = null
    }
    
    fun resetState() {
        _purchaseState.value = AdPurchaseState.Idle
    }
}
