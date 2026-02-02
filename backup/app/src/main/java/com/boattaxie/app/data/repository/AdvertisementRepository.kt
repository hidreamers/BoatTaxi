package com.boattaxie.app.data.repository

import android.content.Context
import android.net.Uri
import com.boattaxie.app.data.model.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdvertisementRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) {
    private val userId: String?
        get() = auth.currentUser?.uid
    
    /**
     * Upload image to Firebase Storage and return the download URL
     */
    private suspend fun uploadImageToStorage(imageUri: Uri, path: String): String? {
        return try {
            android.util.Log.d("AdRepo", "Uploading image to Firebase Storage: $path")
            val storageRef = storage.reference.child(path)
            
            // Upload the file
            storageRef.putFile(imageUri).await()
            
            // Get the download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()
            android.util.Log.d("AdRepo", "Image uploaded successfully: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            android.util.Log.e("AdRepo", "Failed to upload image to Firebase Storage: ${e.message}", e)
            null
        }
    }
    
    /**
     * Create a new advertisement
     */
    suspend fun createAdvertisement(
        businessName: String,
        title: String,
        description: String,
        imageUri: Uri?,
        logoUri: Uri? = null,
        youtubeUrl: String? = null,
        websiteUrl: String?,
        phoneNumber: String?,
        email: String?,
        location: GeoLocation?,
        locationName: String? = null,
        category: AdCategory,
        targetAudience: AdTargetAudience,
        plan: AdPlan,
        isFeatured: Boolean,
        hasCoupon: Boolean = false,
        couponCode: String? = null,
        couponDiscount: String? = null,
        couponDescription: String? = null,
        couponMaxRedemptions: Int? = null
    ): Result<Advertisement> {
        return try {
            val uid = userId ?: throw Exception("User not logged in")
            val adId = UUID.randomUUID().toString()
            android.util.Log.d("AdRepo", "Creating ad for user: $uid, id: $adId")
            
            // Upload image to Firebase Storage
            var imageUrl: String? = null
            if (imageUri != null) {
                android.util.Log.d("AdRepo", "Uploading image to Firebase Storage...")
                imageUrl = uploadImageToStorage(imageUri, "ads/${adId}/image.jpg")
                if (imageUrl != null) {
                    android.util.Log.d("AdRepo", "Image uploaded: $imageUrl")
                } else {
                    android.util.Log.w("AdRepo", "Failed to upload image, continuing without image")
                }
            }
            
            // Upload logo to Firebase Storage
            var logoUrl: String? = null
            if (logoUri != null) {
                android.util.Log.d("AdRepo", "Uploading logo to Firebase Storage...")
                logoUrl = uploadImageToStorage(logoUri, "ads/${adId}/logo.jpg")
                if (logoUrl != null) {
                    android.util.Log.d("AdRepo", "Logo uploaded: $logoUrl")
                } else {
                    android.util.Log.w("AdRepo", "Failed to upload logo, continuing without logo")
                }
            }
            
            val price = if (isFeatured) plan.featuredPrice else plan.price
            
            // Calculate coupon expiration (same as ad end date)
            val couponExpiresAt = if (hasCoupon) AdHelper.calculateAdEndDate(plan) else null
            
            val ad = Advertisement(
                id = adId,
                advertiserId = uid,
                businessName = businessName,
                title = title,
                description = description,
                imageUrl = imageUrl,
                logoUrl = logoUrl,
                youtubeUrl = youtubeUrl,
                websiteUrl = websiteUrl,
                phoneNumber = phoneNumber,
                email = email,
                location = location,
                locationName = locationName,
                category = category,
                targetAudience = targetAudience,
                plan = plan,
                status = AdStatus.PENDING,
                isFeatured = isFeatured,
                price = price,
                hasCoupon = hasCoupon,
                couponCode = couponCode,
                couponDiscount = couponDiscount,
                couponDescription = couponDescription,
                couponExpiresAt = couponExpiresAt,
                couponMaxRedemptions = couponMaxRedemptions
            )
            
            android.util.Log.d("AdRepo", "Saving ad to Firestore: ${ad.id}")
            android.util.Log.d("AdRepo", "Ad location: $location, locationName: $locationName")
            
            // Convert ad to a map to handle GeoLocation serialization properly
            val adMap = hashMapOf<String, Any?>(
                "id" to ad.id,
                "advertiserId" to ad.advertiserId,
                "businessName" to ad.businessName,
                "title" to ad.title,
                "description" to ad.description,
                "imageUrl" to ad.imageUrl,
                "logoUrl" to ad.logoUrl,
                "youtubeUrl" to ad.youtubeUrl,
                "websiteUrl" to ad.websiteUrl,
                "phoneNumber" to ad.phoneNumber,
                "email" to ad.email,
                "location" to if (ad.location != null) hashMapOf(
                    "latitude" to ad.location.latitude,
                    "longitude" to ad.location.longitude,
                    "address" to ad.location.address
                ) else null,
                "locationName" to ad.locationName,
                "category" to ad.category.name,
                "targetAudience" to ad.targetAudience.name,
                "plan" to ad.plan.name,
                "status" to ad.status.name,
                "isFeatured" to ad.isFeatured,
                "impressions" to ad.impressions,
                "clicks" to ad.clicks,
                "price" to ad.price,
                "paymentStatus" to ad.paymentStatus.name,
                "startDate" to ad.startDate,
                "endDate" to ad.endDate,
                "createdAt" to ad.createdAt,
                "updatedAt" to ad.updatedAt,
                "hasCoupon" to ad.hasCoupon,
                "couponCode" to ad.couponCode,
                "couponDiscount" to ad.couponDiscount,
                "couponDescription" to ad.couponDescription,
                "couponExpiresAt" to ad.couponExpiresAt,
                "couponRedemptions" to ad.couponRedemptions,
                "couponMaxRedemptions" to ad.couponMaxRedemptions
            )
            
            firestore.collection("advertisements")
                .document(ad.id)
                .set(adMap)
                .await()
            
            android.util.Log.d("AdRepo", "Ad saved successfully!")
            Result.success(ad)
        } catch (e: Exception) {
            android.util.Log.e("AdRepo", "Failed to create ad: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get a single advertisement by ID
     */
    suspend fun getAdById(adId: String): Advertisement? {
        return try {
            android.util.Log.d("AdRepo", "Fetching ad by ID: $adId")
            val doc = firestore.collection("advertisements")
                .document(adId)
                .get()
                .await()
            
            if (doc.exists()) {
                val ad = doc.toObject(Advertisement::class.java)
                android.util.Log.d("AdRepo", "Found ad: ${ad?.title}, imageUrl=${ad?.imageUrl}, youtubeUrl=${ad?.youtubeUrl}")
                ad
            } else {
                android.util.Log.d("AdRepo", "Ad not found: $adId")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("AdRepo", "Error fetching ad: ${e.message}", e)
            null
        }
    }
    
    /**
     * Activate advertisement after payment
     */
    suspend fun activateAdvertisement(adId: String): Result<Unit> = runCatching {
        android.util.Log.d("AdRepo", "Activating ad: $adId")
        
        val ad = firestore.collection("advertisements")
            .document(adId)
            .get()
            .await()
            .toObject(Advertisement::class.java)
            ?: throw Exception("Advertisement not found")
        
        val startDate = Timestamp.now()
        val endDate = AdHelper.calculateAdEndDate(ad.plan)
        
        // Save status as STRING to match how we query it
        firestore.collection("advertisements")
            .document(adId)
            .update(
                mapOf(
                    "status" to AdStatus.ACTIVE.name,
                    "paymentStatus" to PaymentStatus.COMPLETED.name,
                    "startDate" to startDate,
                    "endDate" to endDate,
                    "updatedAt" to Timestamp.now()
                )
            )
            .await()
            
        android.util.Log.d("AdRepo", "Ad activated successfully!")
    }
    
    /**
     * Get active advertisements for display
     * Note: Sorting in memory to avoid requiring Firestore composite indexes
     */
    suspend fun getActiveAdvertisements(
        targetAudience: AdTargetAudience? = null,
        category: AdCategory? = null,
        limit: Int = 10
    ): List<Advertisement> {
        android.util.Log.d("AdRepo", "getActiveAdvertisements called")
        
        try {
            // Get ALL ads first to debug
            val snapshot = firestore.collection("advertisements")
                .get()
                .await()
                
            android.util.Log.d("AdRepo", "Got ${snapshot.documents.size} documents from Firestore")
            
            val allAds = mutableListOf<Advertisement>()
            for (doc in snapshot.documents) {
                try {
                    val ad = doc.toObject(Advertisement::class.java)
                    if (ad != null) {
                        allAds.add(ad)
                        android.util.Log.d("AdRepo", "  Parsed ad: ${ad.businessName}, status=${ad.status}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AdRepo", "  Failed to parse doc ${doc.id}: ${e.message}")
                }
            }
                
            android.util.Log.d("AdRepo", "Total parsed ads: ${allAds.size}")
            
            // Filter by ACTIVE status
            var results = allAds.filter { it.status == AdStatus.ACTIVE }
            android.util.Log.d("AdRepo", "Found ${results.size} ACTIVE ads after enum filter")
            
            // Temporarily skip date filter for debugging
            // results = results.filter { AdHelper.isAdActive(it) }
            android.util.Log.d("AdRepo", "After date filter: ${results.size} ads")
            
            // Filter by target audience if specified
            if (targetAudience != null && targetAudience != AdTargetAudience.ALL) {
                results = results.filter { 
                    it.targetAudience == AdTargetAudience.ALL || it.targetAudience == targetAudience
                }
            }
            
            // Filter by category if specified
            if (category != null) {
                results = results.filter { it.category == category }
            }
            
            // Sort in memory: featured first, then by creation date
            return results
                .sortedWith(compareByDescending<Advertisement> { it.isFeatured }.thenByDescending { it.createdAt })
                .take(limit)
        } catch (e: Exception) {
            android.util.Log.e("AdRepo", "getActiveAdvertisements FAILED: ${e.message}", e)
            return emptyList()
        }
    }
    
    /**
     * Get featured advertisements (falls back to all active ads if no featured ones)
     * Excludes test/seed ads (those with empty IDs)
     * Fetches up to 100 ads and randomly selects from them
     */
    suspend fun getFeaturedAdvertisements(limit: Int = 20): List<Advertisement> {
        android.util.Log.d("AdRepo", "getFeaturedAdvertisements called")
        
        // Get all active ads and filter out test ads (empty IDs)
        val allAds = firestore.collection("advertisements")
            .whereEqualTo("status", "ACTIVE")
            .get()
            .await()
            .toObjects(Advertisement::class.java)
            .filter { 
                // Must have a valid ID (not empty) - excludes test/seed ads
                it.id.isNotBlank() && AdHelper.isAdActive(it) 
            }
        
        android.util.Log.d("AdRepo", "Found ${allAds.size} real ads (excluding test ads)")
        
        // Shuffle randomly and take the limit
        val randomAds = allAds.shuffled().take(limit)
        
        android.util.Log.d("AdRepo", "Returning ${randomAds.size} randomly selected ads for home screen")
        return randomAds
    }
    
    /**
     * Get advertisements with locations (for map display)
     */
    suspend fun getAdsWithLocations(limit: Int = 50): List<Advertisement> {
        android.util.Log.d("AdRepo", "getAdsWithLocations called")
        
        // Query ALL docs and filter locally to avoid cache issues
        val documents = firestore.collection("advertisements")
            .get(com.google.firebase.firestore.Source.SERVER)
            .await()
            .documents
        
        android.util.Log.d("AdRepo", "Got ${documents.size} docs from SERVER for map (unfiltered)")
        
        val ads = mutableListOf<Advertisement>()
        for (doc in documents) {
            try {
                val ad = doc.toObject(Advertisement::class.java)
                if (ad != null && 
                    ad.status == AdStatus.ACTIVE &&
                    // Temporarily skip date filter for debugging
                    // AdHelper.isAdActive(ad) && 
                    ad.location != null && 
                    ad.location.latitude != 0.0 && 
                    ad.location.longitude != 0.0) {
                    ads.add(ad)
                    android.util.Log.d("AdRepo", "  Map ad: ${ad.title}, logoUrl=${ad.logoUrl}, imageUrl=${ad.imageUrl}")
                }
            } catch (e: Exception) {
                android.util.Log.e("AdRepo", "  Failed to parse map ad: ${e.message}")
            }
        }
        
        android.util.Log.d("AdRepo", "Found ${ads.size} ads with locations")
        return ads
            .sortedWith(compareByDescending<Advertisement> { it.isFeatured }.thenByDescending { it.createdAt })
            .take(limit)
    }
    
    /**
     * Get advertisements with coupons
     */
    suspend fun getAdsWithCoupons(limit: Int = 20): List<Advertisement> {
        val documents = firestore.collection("advertisements")
            .whereEqualTo("status", "ACTIVE")
            .whereEqualTo("hasCoupon", true)
            .get()
            .await()
            .documents
        
        val ads = mutableListOf<Advertisement>()
        for (doc in documents) {
            try {
                val ad = doc.toObject(Advertisement::class.java)
                if (ad != null && AdHelper.isAdActive(ad) && AdHelper.isCouponValid(ad)) {
                    ads.add(ad)
                }
            } catch (e: Exception) {
                android.util.Log.e("AdRepo", "  Failed to parse coupon ad: ${e.message}")
            }
        }
        
        return ads
            .sortedWith(compareByDescending<Advertisement> { it.isFeatured }.thenByDescending { it.createdAt })
            .take(limit)
    }
    
    /**
     * Redeem a coupon
     */
    suspend fun redeemCoupon(adId: String): Result<Boolean> = runCatching {
        val uid = userId ?: throw Exception("User not logged in")
        
        // Check if already redeemed by this user
        val existing = firestore.collection("coupon_redemptions")
            .whereEqualTo("advertisementId", adId)
            .whereEqualTo("userId", uid)
            .get()
            .await()
        
        if (!existing.isEmpty) {
            throw Exception("You have already redeemed this coupon")
        }
        
        // Get the ad
        val ad = firestore.collection("advertisements")
            .document(adId)
            .get()
            .await()
            .toObject(Advertisement::class.java)
            ?: throw Exception("Advertisement not found")
        
        if (!AdHelper.isCouponValid(ad)) {
            throw Exception("Coupon is no longer valid")
        }
        
        // Create redemption record
        val redemption = CouponRedemption(
            couponCode = ad.couponCode ?: "",
            advertisementId = adId,
            userId = uid
        )
        
        firestore.collection("coupon_redemptions")
            .add(redemption)
            .await()
        
        // Increment redemption count
        firestore.collection("advertisements")
            .document(adId)
            .update("couponRedemptions", ad.couponRedemptions + 1)
            .await()
        
        true
    }
    
    /**
     * Get advertisements by location (nearby)
     */
    suspend fun getNearbyAdvertisements(
        location: GeoLocation,
        radiusKm: Double = 10.0,
        limit: Int = 10
    ): List<Advertisement> {
        // Note: For production, use GeoFire or similar for proper geo queries
        // This is a simplified version
        return getActiveAdvertisements(limit = limit)
    }
    
    /**
     * Get user's advertisements from BOTH collections
     */
    suspend fun getUserAdvertisements(): List<Advertisement> {
        val uid = userId ?: run {
            android.util.Log.w("AdRepo", "getUserAdvertisements: No user ID!")
            return emptyList()
        }
        
        android.util.Log.d("AdRepo", "getUserAdvertisements for userId: $uid")
        
        val allAds = mutableListOf<Advertisement>()
        
        // Query 1: 'ads' collection (Stripe-paid ads from backend) with 'userId' field
        try {
            val adsSnapshot = firestore.collection("ads")
                .whereEqualTo("userId", uid)
                .get()
                .await()
            
            // Manually parse documents to handle type mismatches from backend
            adsSnapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    Advertisement(
                        id = doc.id,
                        advertiserId = data["userId"] as? String ?: "",
                        businessName = data["businessName"] as? String ?: "",
                        title = data["title"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        imageUrl = data["imageUri"] as? String,
                        logoUrl = data["logoUri"] as? String,
                        youtubeUrl = data["youtubeUrl"] as? String,
                        websiteUrl = data["website"] as? String,
                        phoneNumber = data["phone"] as? String,
                        email = data["email"] as? String,
                        locationName = data["location"] as? String,
                        category = try { AdCategory.valueOf(data["category"] as? String ?: "GENERAL") } catch (e: Exception) { AdCategory.GENERAL },
                        status = try { AdStatus.valueOf((data["status"] as? String ?: "PENDING").uppercase()) } catch (e: Exception) { AdStatus.PENDING },
                        isFeatured = data["isFeatured"] as? Boolean ?: false,
                        startDate = data["startDate"] as? com.google.firebase.Timestamp,
                        endDate = data["endDate"] as? com.google.firebase.Timestamp,
                        createdAt = data["createdAt"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now()
                    )
                } catch (e: Exception) {
                    android.util.Log.e("AdRepo", "Error parsing ad from 'ads' ${doc.id}: ${e.message}")
                    null
                }
            }.also { allAds.addAll(it) }
        } catch (e: Exception) {
            android.util.Log.e("AdRepo", "Error querying 'ads' collection: ${e.message}")
        }
        
        // Query 2: 'advertisements' collection (free/app-created ads) with 'advertiserId' field
        try {
            val advertisementsSnapshot = firestore.collection("advertisements")
                .whereEqualTo("advertiserId", uid)
                .get()
                .await()
                .toObjects(Advertisement::class.java)
            allAds.addAll(advertisementsSnapshot)
        } catch (e: Exception) {
            android.util.Log.e("AdRepo", "Error querying 'advertisements' collection: ${e.message}")
        }
        
        // Sort all ads by createdAt descending
        val sortedAds = allAds.sortedByDescending { it.createdAt }
            
        android.util.Log.d("AdRepo", "Found ${sortedAds.size} total ads for user")
        sortedAds.forEach { ad ->
            android.util.Log.d("AdRepo", "  Ad: ${ad.businessName}, status=${ad.status}, id=${ad.id}")
        }
        
        return sortedAds
    }
    
    /**
     * Record ad impression
     */
    suspend fun recordImpression(adId: String) {
        try {
            firestore.collection("advertisements")
                .document(adId)
                .update("impressions", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
        } catch (e: Exception) {
            // Silently fail for impressions
        }
    }
    
    /**
     * Record ad click
     */
    suspend fun recordClick(adId: String) {
        try {
            firestore.collection("advertisements")
                .document(adId)
                .update("clicks", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
        } catch (e: Exception) {
            // Silently fail for clicks
        }
    }
    
    /**
     * Pause an advertisement
     */
    suspend fun pauseAdvertisement(adId: String): Result<Unit> = runCatching {
        firestore.collection("advertisements")
            .document(adId)
            .update(
                mapOf(
                    "status" to AdStatus.PAUSED.name,
                    "updatedAt" to Timestamp.now()
                )
            )
            .await()
    }
    
    /**
     * Resume a paused advertisement
     */
    suspend fun resumeAdvertisement(adId: String): Result<Unit> = runCatching {
        firestore.collection("advertisements")
            .document(adId)
            .update(
                mapOf(
                    "status" to AdStatus.ACTIVE.name,
                    "updatedAt" to Timestamp.now()
                )
            )
            .await()
    }
    
    /**
     * Update an advertisement's basic info
     */
    suspend fun updateAdvertisement(
        adId: String,
        businessName: String,
        title: String,
        description: String,
        phoneNumber: String?,
        email: String?,
        websiteUrl: String?,
        youtubeUrl: String?,
        couponDiscount: String?,
        couponDescription: String?,
        imageUri: Uri? = null,
        logoUri: Uri? = null
    ): Result<Unit> = runCatching {
        val updates = mutableMapOf<String, Any?>(
            "businessName" to businessName,
            "title" to title,
            "description" to description,
            "phoneNumber" to phoneNumber,
            "email" to email,
            "websiteUrl" to websiteUrl,
            "youtubeUrl" to youtubeUrl,
            "updatedAt" to Timestamp.now()
        )
        
        // Update coupon info if provided
        if (couponDiscount != null) {
            updates["couponDiscount"] = couponDiscount
        }
        if (couponDescription != null) {
            updates["couponDescription"] = couponDescription
        }
        
        // Upload new image if provided
        if (imageUri != null) {
            val imageUrl = uploadImageToStorage(imageUri, "ads/${adId}/image.jpg")
            if (imageUrl != null) {
                updates["imageUrl"] = imageUrl
                android.util.Log.d("AdRepo", "Updated image: $imageUrl")
            }
        }
        
        // Upload new logo if provided
        if (logoUri != null) {
            val logoUrl = uploadImageToStorage(logoUri, "ads/${adId}/logo.jpg")
            if (logoUrl != null) {
                updates["logoUrl"] = logoUrl
                android.util.Log.d("AdRepo", "Updated logo: $logoUrl")
            }
        }
        
        firestore.collection("advertisements")
            .document(adId)
            .update(updates.filterValues { it != null } as Map<String, Any>)
            .await()
        
        android.util.Log.d("AdRepo", "Ad updated: $adId")
    }
    
    /**
     * Delete an advertisement (keeps billing history)
     */
    suspend fun deleteAdvertisement(adId: String): Result<Unit> = runCatching {
        // Get ad to save billing history and delete images
        val ad = firestore.collection("advertisements")
            .document(adId)
            .get()
            .await()
            .toObject(Advertisement::class.java)
        
        // Save billing history before deleting
        if (ad != null && ad.paymentStatus == PaymentStatus.COMPLETED) {
            val billingRecord = hashMapOf(
                "id" to adId,
                "advertiserId" to ad.advertiserId,
                "adId" to ad.id,
                "businessName" to ad.businessName,
                "title" to ad.title,
                "plan" to ad.plan.name,
                "price" to ad.price,
                "paymentStatus" to ad.paymentStatus.name,
                "startDate" to ad.startDate,
                "endDate" to ad.endDate,
                "impressions" to ad.impressions,
                "clicks" to ad.clicks,
                "createdAt" to ad.createdAt,
                "deletedAt" to Timestamp.now()
            )
            
            firestore.collection("ad_billing_history")
                .document(adId)
                .set(billingRecord)
                .await()
            
            android.util.Log.d("AdRepo", "Saved billing history for ad: $adId")
        }
        
        // Delete image from storage
        ad?.imageUrl?.let { url ->
            if (url.startsWith("https://firebasestorage")) {
                try {
                    storage.getReferenceFromUrl(url).delete().await()
                } catch (e: Exception) {
                    android.util.Log.w("AdRepo", "Failed to delete image: ${e.message}")
                }
            }
        }
        
        // Delete logo from storage
        ad?.logoUrl?.let { url ->
            if (url.startsWith("https://firebasestorage")) {
                try {
                    storage.getReferenceFromUrl(url).delete().await()
                } catch (e: Exception) {
                    android.util.Log.w("AdRepo", "Failed to delete logo: ${e.message}")
                }
            }
        }
        
        // Delete from Firestore
        firestore.collection("advertisements")
            .document(adId)
            .delete()
            .await()
        
        android.util.Log.d("AdRepo", "Ad deleted successfully: $adId")
    }
    
    /**
     * Observe ads for real-time updates
     * Note: No orderBy to avoid requiring composite index
     */
    fun observeActiveAds(): Flow<List<Advertisement>> = callbackFlow {
        val listener = firestore.collection("advertisements")
            .whereEqualTo("status", "ACTIVE")
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val ads = snapshot?.toObjects(Advertisement::class.java)
                    ?.filter { AdHelper.isAdActive(it) }
                    ?.sortedWith(compareByDescending<Advertisement> { it.isFeatured }.thenByDescending { it.createdAt })
                    ?.take(20)
                    ?: emptyList()
                
                trySend(ads)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get ad categories
     */
    fun getCategories(): List<AdCategory> = AdHelper.getCategories()
    
    /**
     * Get ad plans
     */
    fun getAdPlans(): List<AdPlan> = AdHelper.getAdPlans()
    
    /**
     * Get billing history for the current user
     */
    suspend fun getBillingHistory(): List<AdBillingRecord> {
        val uid = userId ?: return emptyList()
        return try {
            firestore.collection("ad_billing_history")
                .whereEqualTo("advertiserId", uid)
                .get()
                .await()
                .toObjects(AdBillingRecord::class.java)
                .sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            android.util.Log.e("AdRepo", "Error getting billing history: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Seed test advertisements for development
     */
    suspend fun seedTestAds(): Result<Int> = runCatching {
        android.util.Log.d("AdRepo", "Seeding test ads...")
        
        val currentUserId = userId ?: "test_user"
        android.util.Log.d("AdRepo", "Using advertiserId: $currentUserId")
        
        val testAds = listOf(
            mapOf(
                
                "advertiserId" to currentUserId,
                "businessName" to "Bocas Marina & Fuel",
                "title" to "Full Service Marina",
                "description" to "Gas, diesel, boat repairs, and docking services. Located in Bocas Town with easy access from the sea.",
                "phoneNumber" to "+507 757-9800",
                "category" to AdCategory.MARINA.name,
                "targetAudience" to AdTargetAudience.ALL.name,
                "plan" to AdPlan.ONE_MONTH.name,
                "status" to AdStatus.ACTIVE.name,
                "isFeatured" to true,
                
                "impressions" to 0,
                "clicks" to 0,
                "price" to 50.0,
                "paymentStatus" to PaymentStatus.COMPLETED.name,
                "location" to hashMapOf(
                    "latitude" to 9.3403,
                    "longitude" to -82.2419,
                    "address" to "Bocas Town Marina"
                ),
                "locationName" to "Bocas Town",
                "startDate" to Timestamp.now(),
                "endDate" to Timestamp(Timestamp.now().seconds + 30 * 24 * 60 * 60, 0),
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now(),
                "hasCoupon" to true,
                "couponCode" to "FUEL10",
                "couponDiscount" to "10% OFF",
                "couponDescription" to "10% off fuel purchase over $50"
            ),
            mapOf(
                
                "advertiserId" to currentUserId,
                "businessName" to "Restaurante El Pirata",
                "title" to "Best Seafood in Bocas!",
                "description" to "Fresh ceviche, grilled fish, and cold beers with ocean views. Live music on weekends!",
                "phoneNumber" to "+507 757-9123",
                "category" to AdCategory.RESTAURANT.name,
                "targetAudience" to AdTargetAudience.ALL.name,
                "plan" to AdPlan.ONE_WEEK.name,
                "status" to AdStatus.ACTIVE.name,
                "isFeatured" to false,
                
                "impressions" to 0,
                "clicks" to 0,
                "price" to 15.0,
                "paymentStatus" to PaymentStatus.COMPLETED.name,
                "location" to hashMapOf(
                    "latitude" to 9.3398,
                    "longitude" to -82.2415,
                    "address" to "Calle 3, Bocas Town"
                ),
                "locationName" to "Bocas Town",
                "startDate" to Timestamp.now(),
                "endDate" to Timestamp(Timestamp.now().seconds + 7 * 24 * 60 * 60, 0),
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now(),
                "hasCoupon" to true,
                "couponCode" to "CEVICHE",
                "couponDiscount" to "FREE DRINK",
                "couponDescription" to "Free drink with any main course"
            ),
            mapOf(
                
                "advertiserId" to currentUserId,
                "businessName" to "Hotel Bocas Paradise",
                "title" to "Waterfront Rooms Available",
                "description" to "Comfortable rooms with A/C, WiFi, and stunning water views. Walking distance to restaurants and nightlife.",
                "phoneNumber" to "+507 757-9456",
                "websiteUrl" to "https://hotelbocasparadise.com",
                "category" to AdCategory.HOTEL.name,
                "targetAudience" to AdTargetAudience.BOAT_RIDERS.name,
                "plan" to AdPlan.ONE_MONTH.name,
                "status" to AdStatus.ACTIVE.name,
                "isFeatured" to true,
                
                "impressions" to 0,
                "clicks" to 0,
                "price" to 50.0,
                "paymentStatus" to PaymentStatus.COMPLETED.name,
                "location" to hashMapOf(
                    "latitude" to 9.3410,
                    "longitude" to -82.2425,
                    "address" to "Waterfront, Bocas Town"
                ),
                "locationName" to "Bocas Town",
                "startDate" to Timestamp.now(),
                "endDate" to Timestamp(Timestamp.now().seconds + 30 * 24 * 60 * 60, 0),
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now(),
                "hasCoupon" to false
            ),
            mapOf(
                
                "advertiserId" to currentUserId,
                "businessName" to "Starfish Beach Tours",
                "title" to "Island Hopping Adventures",
                "description" to "Visit Starfish Beach, Red Frog Beach, and snorkeling spots. Full day tours with lunch included!",
                "phoneNumber" to "+507 6789-1234",
                "category" to AdCategory.TOURISM.name,
                "targetAudience" to AdTargetAudience.ALL.name,
                "plan" to AdPlan.ONE_WEEK.name,
                "status" to AdStatus.ACTIVE.name,
                "isFeatured" to false,
                
                "impressions" to 0,
                "clicks" to 0,
                "price" to 15.0,
                "paymentStatus" to PaymentStatus.COMPLETED.name,
                "location" to hashMapOf(
                    "latitude" to 9.2500,
                    "longitude" to -82.1300,
                    "address" to "Starfish Beach, Isla Colon"
                ),
                "locationName" to "Starfish Beach",
                "startDate" to Timestamp.now(),
                "endDate" to Timestamp(Timestamp.now().seconds + 7 * 24 * 60 * 60, 0),
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now(),
                "hasCoupon" to true,
                "couponCode" to "STARFISH20",
                "couponDiscount" to "$20 OFF",
                "couponDescription" to "$20 off group bookings of 4+"
            ),
            mapOf(
                
                "advertiserId" to currentUserId,
                "businessName" to "Aqua Lounge",
                "title" to "Bar & Hostel on the Water",
                "description" to "Famous overwater bar with rope swings, trampolines, and the best sunset views. Dorm beds available!",
                "phoneNumber" to "+507 757-9000",
                "category" to AdCategory.ENTERTAINMENT.name,
                "targetAudience" to AdTargetAudience.ALL.name,
                "plan" to AdPlan.ONE_MONTH.name,
                "status" to AdStatus.ACTIVE.name,
                "isFeatured" to true,
                
                "impressions" to 0,
                "clicks" to 0,
                "price" to 50.0,
                "paymentStatus" to PaymentStatus.COMPLETED.name,
                "location" to hashMapOf(
                    "latitude" to 9.3380,
                    "longitude" to -82.2400,
                    "address" to "Over the water, Bocas Town"
                ),
                "locationName" to "Bocas Town",
                "startDate" to Timestamp.now(),
                "endDate" to Timestamp(Timestamp.now().seconds + 30 * 24 * 60 * 60, 0),
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now(),
                "hasCoupon" to false
            )
        )
        
        var count = 0
        for (ad in testAds) {
            val id = UUID.randomUUID().toString()
            // Remove 'id' from the map before saving - Firestore will use document ID
            val adData = ad.toMutableMap()
            adData.remove("id")
            
            firestore.collection("advertisements")
                .document(id)
                .set(adData)
                .await()
            count++
            android.util.Log.d("AdRepo", "Created test ad: ${ad["businessName"]}")
        }
        
        android.util.Log.d("AdRepo", "Seeded $count test ads")
        count
    }
    
    /**
     * Delete all test ads (ads created by test_user)
     */
    suspend fun deleteTestAds(): Result<Int> = runCatching {
        android.util.Log.d("AdRepo", "Deleting test ads...")
        
        val testAds = firestore.collection("advertisements")
            .whereEqualTo("advertiserId", "test_user")
            .get()
            .await()
            
        var count = 0
        for (doc in testAds.documents) {
            doc.reference.delete().await()
            count++
        }
        
        android.util.Log.d("AdRepo", "Deleted $count test ads")
        count
    }
    
    /**
     * Delete all ads except those belonging to the current user
     */
    suspend fun deleteAllAdsExceptMine(): Result<Int> = runCatching {
        val uid = userId ?: throw Exception("Not logged in")
        android.util.Log.d("AdRepo", "Deleting all ads except for user: $uid")
        
        // Delete ALL documents in the collection except user's
        val allAds = firestore.collection("advertisements")
            .get()
            .await()
        
        android.util.Log.d("AdRepo", "Found ${allAds.documents.size} total ads in Firestore")
            
        var count = 0
        for (doc in allAds.documents) {
            val advertiserId = doc.getString("advertiserId")
            val title = doc.getString("title") ?: ""
            android.util.Log.d("AdRepo", "  Checking: $title by $advertiserId")
            if (advertiserId != uid) {
                android.util.Log.d("AdRepo", "  -> Deleting: $title")
                doc.reference.delete().await()
                count++
            }
        }
        
        android.util.Log.d("AdRepo", "Deleted $count ads (kept yours)")
        count
    }
    
    /**
     * Create demo/AI generated ads based on description and count
     * These can be deleted later from the Ads screen
     */
    suspend fun createDemoAds(description: String = "", count: Int = 10): Result<Int> = runCatching {
        val uid = userId ?: throw Exception("Not logged in")
        android.util.Log.d("AdRepo", "Creating $count AI ads with description: $description")
        
        // Parse description to determine category and type
        val descLower = description.lowercase()
        val category = when {
            descLower.contains("restaurant") || descLower.contains("food") || descLower.contains("cafe") || descLower.contains("dining") -> AdCategory.RESTAURANT
            descLower.contains("hotel") || descLower.contains("hostel") || descLower.contains("accommodation") || descLower.contains("stay") -> AdCategory.HOTEL
            descLower.contains("tour") || descLower.contains("snorkel") || descLower.contains("dive") || descLower.contains("trip") -> AdCategory.TOURISM
            descLower.contains("bar") || descLower.contains("club") || descLower.contains("party") || descLower.contains("night") -> AdCategory.ENTERTAINMENT
            descLower.contains("boat") || descLower.contains("marina") || descLower.contains("rental") -> AdCategory.MARINA
            descLower.contains("shop") || descLower.contains("store") || descLower.contains("grocery") -> AdCategory.SHOPPING
            descLower.contains("spa") || descLower.contains("yoga") || descLower.contains("massage") || descLower.contains("wellness") -> AdCategory.HEALTH
            descLower.contains("surf") || descLower.contains("paddle") || descLower.contains("sport") -> AdCategory.ENTERTAINMENT
            else -> AdCategory.GENERAL
        }
        
        // Generate business names based on description
        val businessNames = when (category) {
            AdCategory.RESTAURANT -> listOf(
                "Bocas Fresh Seafood", "El Pescador", "Caribbean Kitchen", "The Sunset Grill", 
                "Sabor del Mar", "Tropical Bites", "Ocean View Café", "Isla Dining", 
                "Coconut Cove", "Beach House Restaurant", "La Buena Vida", "Palm Tree Café"
            )
            AdCategory.HOTEL -> listOf(
                "Tropical Paradise Hotel", "Bocas Bay Resort", "Island Hideaway", "Seaside Hostel",
                "Ocean Breeze Inn", "Palm Beach Suites", "Sunset Cabañas", "Caribbean Dreams Hotel",
                "Beachfront Bungalows", "La Luna Hotel", "Casa del Mar", "Vista Hermosa"
            )
            AdCategory.TOURISM -> listOf(
                "Island Adventures", "Bocas Explorer Tours", "Sea Life Snorkeling", "Dolphin Watch Tours",
                "Paradise Dive Center", "Starfish Tours", "Crystal Waters Diving", "Bocas Discovery",
                "Caribbean Adventures", "Ocean Quest Tours", "Island Hopper", "Aqua Expeditions"
            )
            AdCategory.ENTERTAINMENT -> listOf(
                "Sunset Beach Bar", "The Wave Club", "Isla Vibes", "Caribbean Nights",
                "Ocean Side Bar", "Party Pier", "Tropical Beats", "Beach Break Bar",
                "Moonlight Lounge", "Salty Dog Bar", "Coco Loco", "Paradise Club"
            )
            AdCategory.MARINA -> listOf(
                "Bocas Boat Rentals", "Island Charters", "Sea Breeze Marina", "Captain's Choice",
                "Paradise Pontoons", "Caribbean Cruises", "Ocean Explorer Rentals", "Isla Boats",
                "Tropical Waters Rental", "Marina del Sol", "Boat Life Bocas", "Anchor Point"
            )
            AdCategory.SHOPPING -> listOf(
                "Island Market", "Super Bocas", "Beach Boutique", "Tropical Goods",
                "Souvenir Paradise", "Caribbean Crafts", "Local Treasures", "Bocas Bazaar",
                "Island Essentials", "Artisan Market", "Coconut Shop", "Palm Prints"
            )
            AdCategory.HEALTH -> listOf(
                "Ocean Yoga", "Island Wellness Spa", "Tropical Zen", "Caribbean Healing",
                "Beach Body Fitness", "Namaste Bocas", "Serenity Spa", "Island Massage",
                "Paradise Wellness", "Sunset Yoga Studio", "Holistic Haven", "Pure Balance"
            )
            else -> listOf(
                "Bocas Local Business", "Island Services", "Caribbean Solutions", "Tropical Helpers",
                "Bocas Professional", "Island Works", "Paradise Services", "Local Expert",
                "Community Service", "Bocas Best", "Island Choice", "Caribbean Care"
            )
        }
        
        // Generate descriptions based on category
        val descriptions = when (category) {
            AdCategory.RESTAURANT -> listOf(
                "Fresh seafood and Caribbean cuisine with ocean views. Daily specials and live music on weekends!",
                "Authentic local dishes and international favorites. Best ceviche in Bocas!",
                "Beachfront dining with spectacular sunsets. Fresh catch of the day every day!",
                "Tropical drinks, amazing tacos, and waterfront vibes. Happy hour 4-6pm daily!"
            )
            AdCategory.HOTEL -> listOf(
                "Oceanfront accommodations with stunning views. Free kayaks and snorkeling gear!",
                "Boutique hotel with pool, yoga studio, and coworking space. Perfect for digital nomads!",
                "Private cabins on the water. Wake up to dolphins! Breakfast included.",
                "Budget-friendly with great vibes. Tours, surf lessons, and island life await!"
            )
            AdCategory.TOURISM -> listOf(
                "Daily snorkeling tours to Zapatilla Islands! See dolphins, starfish, and coral reefs.",
                "PADI certified dive center. Courses for beginners to advanced. Night dives available!",
                "Island hopping adventures! Visit 5 islands, snorkel 3 spots, includes lunch.",
                "Private boat tours, sunset cruises, and fishing trips. Create your own adventure!"
            )
            AdCategory.ENTERTAINMENT -> listOf(
                "The best nightlife in Bocas! Live DJs, cocktails, and ocean views. Free entry!",
                "Swim-up bar with pool, games, and great music. Party every night!",
                "Chill beach vibes by day, epic parties by night. Happy hour 5-7pm!",
                "Live reggae, cold beers, and perfect sunsets. The place to be in Bocas!"
            )
            else -> listOf(
                "Your go-to spot in Bocas del Toro! Quality service and local expertise.",
                "Serving the Bocas community with pride. Come visit us today!",
                "Local business with international standards. We're here to help!",
                "Experience the best of Bocas! Friendly staff and great service."
            )
        }
        
        // Bocas locations for random placement
        val locations = listOf(
            mapOf("latitude" to 9.3405, "longitude" to -82.2410, "address" to "Calle 3, Bocas Town"),
            mapOf("latitude" to 9.3398, "longitude" to -82.2418, "address" to "Av G, Bocas Town"),
            mapOf("latitude" to 9.3412, "longitude" to -82.2425, "address" to "Main Street, Bocas Town"),
            mapOf("latitude" to 9.3380, "longitude" to -82.2445, "address" to "Calle 6, Bocas Town"),
            mapOf("latitude" to 9.3365, "longitude" to -82.2380, "address" to "Isla Carenero"),
            mapOf("latitude" to 9.2820, "longitude" to -82.1320, "address" to "Red Frog Beach"),
            mapOf("latitude" to 9.4100, "longitude" to -82.3250, "address" to "Starfish Beach"),
            mapOf("latitude" to 9.2957, "longitude" to -82.1350, "address" to "Bastimentos Town")
        )
        
        val couponCodes = listOf("SAVE10", "FIRST15", "WELCOME20", "BOATTAXIE", "SPECIAL", "DEAL25", "VIP10", "FRIEND15")
        val discounts = listOf("10% Off", "15% Off First Visit", "\$5 Off", "Free Drink", "2-for-1", "Free Dessert", "20% Off", "Free Appetizer")
        
        var createdCount = 0
        val now = Timestamp.now()
        val actualCount = minOf(count, 50) // Max 50 at a time
        
        for (i in 0 until actualCount) {
            val adId = UUID.randomUUID().toString()
            val businessName = businessNames[i % businessNames.size]
            val location = locations[i % locations.size]
            val desc = if (description.isNotBlank()) description else descriptions[i % descriptions.size]
            
            val adMap = hashMapOf<String, Any?>(
                "id" to adId,
                "advertiserId" to uid,
                "businessName" to "$businessName ${if (i > 0 && i < businessNames.size) "" else "#${i + 1}"}".trim(),
                "title" to "Welcome to $businessName",
                "description" to desc,
                "imageUrl" to null,
                "logoUrl" to null,
                "youtubeUrl" to null,
                "websiteUrl" to null,
                "phoneNumber" to "+507 6${(100..999).random()}-${(1000..9999).random()}",
                "email" to null,
                "location" to location,
                "locationName" to (location["address"] as String),
                "category" to category.name,
                "targetAudience" to AdTargetAudience.ALL.name,
                "plan" to AdPlan.TWO_WEEKS.name,
                "status" to AdStatus.DRAFT.name, // AI generated = draft, needs payment to activate
                "isFeatured" to (i < 3), // First 3 will be featured when activated
                "impressions" to 0,
                "clicks" to 0,
                "price" to 19.99, // Standard 2-week price
                "paymentStatus" to PaymentStatus.PENDING.name, // Needs payment
                "startDate" to null, // Will be set when payment is made
                "endDate" to null, // Will be set when payment is made
                "createdAt" to now,
                "updatedAt" to now,
                "hasCoupon" to true,
                "couponCode" to couponCodes[i % couponCodes.size],
                "couponDiscount" to discounts[i % discounts.size],
                "couponDescription" to "Show this code at $businessName to redeem!",
                "couponExpiresAt" to null, // Will be set when payment is made
                "couponRedemptions" to 0,
                "couponMaxRedemptions" to 100
            )
            
            firestore.collection("advertisements")
                .document(adId)
                .set(adMap)
                .await()
            
            createdCount++
            android.util.Log.d("AdRepo", "Created AI ad: $businessName")
        }
        
        android.util.Log.d("AdRepo", "Created $createdCount AI-generated ads")
        createdCount
    }
    
    /**
     * Delete all demo ads (ads created by current user for demo purposes)
     */
    suspend fun deleteDemoAds(): Result<Int> = runCatching {
        val uid = userId ?: throw Exception("Not logged in")
        
        // Get all ads by current user
        val myAds = firestore.collection("advertisements")
            .whereEqualTo("advertiserId", uid)
            .get()
            .await()
        
        var count = 0
        for (doc in myAds.documents) {
            doc.reference.delete().await()
            count++
        }
        
        android.util.Log.d("AdRepo", "Deleted $count demo ads")
        count
    }
}
