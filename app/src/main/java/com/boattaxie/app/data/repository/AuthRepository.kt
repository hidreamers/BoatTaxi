package com.boattaxie.app.data.repository

import com.boattaxie.app.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser
    
    val isLoggedIn: Boolean
        get() = currentUser != null
    
    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }
    
    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        phoneNumber: String,
        userType: UserType,
        vehicleType: VehicleType? = null,
        residencyType: ResidencyType = ResidencyType.LOCAL,
        hasBoat: Boolean = false,
        hasTaxi: Boolean = false
    ): Result<User> = runCatching {
        android.util.Log.d("AuthRepo", "signUp - userType: $userType, vehicleType: $vehicleType, hasBoat: $hasBoat, hasTaxi: $hasTaxi")
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user ?: throw Exception("Failed to create user")
        android.util.Log.d("AuthRepo", "Firebase Auth user created: ${firebaseUser.uid}")
        
        val user = User(
            id = firebaseUser.uid,
            email = email,
            fullName = fullName,
            phoneNumber = phoneNumber,
            userType = userType,
            vehicleType = vehicleType,
            canBeDriver = userType != UserType.RIDER, // True for CAPTAIN/DRIVER, false for RIDER
            hasBoat = hasBoat,
            hasTaxi = hasTaxi,
            isVerified = userType == UserType.RIDER, // Riders are auto-verified
            verificationStatus = if (userType == UserType.RIDER) 
                VerificationStatus.APPROVED else VerificationStatus.NONE,
            isLocalResident = residencyType == ResidencyType.LOCAL,
            residencyType = residencyType
        )
        
        android.util.Log.d("AuthRepo", "Creating Firestore doc for user: ${user.id}, type: ${user.userType}, vehicle: ${user.vehicleType}")
        firestore.collection("users")
            .document(firebaseUser.uid)
            .set(user)
            .await()
        android.util.Log.d("AuthRepo", "Firestore doc created successfully!")
        
        user
    }
    
    suspend fun signIn(email: String, password: String): Result<User> = runCatching {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user ?: throw Exception("Failed to sign in")
        
        getUser(firebaseUser.uid) ?: throw Exception("User data not found")
    }
    
    suspend fun signOut() {
        // Set user offline before signing out
        try {
            currentUser?.uid?.let { userId ->
                firestore.collection("users")
                    .document(userId)
                    .update("isOnline", false)
                    .await()
                android.util.Log.d("AuthRepo", "Set user $userId offline before signout")
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepo", "Failed to set offline on signout: ${e.message}")
        }
        auth.signOut()
    }
    
    suspend fun deleteAccount(): Result<Unit> = runCatching {
        val userId = currentUser?.uid ?: throw Exception("User not logged in")
        
        // Delete user document from Firestore
        firestore.collection("users")
            .document(userId)
            .delete()
            .await()
        
        // Delete Firebase Auth account
        auth.currentUser?.delete()?.await()
            ?: throw Exception("Failed to delete auth account")
    }
    
    suspend fun resetPassword(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }
    
    suspend fun getUser(userId: String): User? {
        return firestore.collection("users")
            .document(userId)
            .get()
            .await()
            .toObject(User::class.java)
    }
    
    suspend fun getCurrentUser(): User? {
        val userId = currentUser?.uid ?: return null
        return getUser(userId)
    }
    
    suspend fun updateUser(user: User): Result<Unit> = runCatching {
        firestore.collection("users")
            .document(user.id)
            .set(user, SetOptions.merge())
            .await()
    }
    
    suspend fun updateUserLocation(location: GeoLocation): Result<Unit> = runCatching {
        val userId = currentUser?.uid ?: throw Exception("User not logged in")
        firestore.collection("users")
            .document(userId)
            .update("currentLocation", location)
            .await()
    }
    
    suspend fun updateOnlineStatus(isOnline: Boolean): Result<Unit> = runCatching {
        val userId = currentUser?.uid ?: throw Exception("User not logged in")
        firestore.collection("users")
            .document(userId)
            .update("isOnline", isOnline)
            .await()
    }
    
    suspend fun updateUserType(userType: UserType, vehicleType: VehicleType? = null): Result<User> = runCatching {
        val userId = currentUser?.uid ?: throw Exception("User not logged in")
        android.util.Log.d("AuthRepo", "Updating user type to: $userType, vehicleType: $vehicleType")
        
        // Use lowercase to match @PropertyName annotations
        val updates = mutableMapOf<String, Any?>(
            "userType" to userType.name.lowercase()
        )
        
        if (vehicleType != null) {
            updates["vehicleType"] = vehicleType.name.lowercase()
        }
        
        // For riders, auto-approve verification
        if (userType == UserType.RIDER) {
            updates["isVerified"] = true
            updates["verificationStatus"] = "approved"
        }
        
        firestore.collection("users")
            .document(userId)
            .update(updates as Map<String, Any>)
            .await()
        
        getUser(userId) ?: throw Exception("Failed to get updated user")
    }
    
    /**
     * Update just the vehicle type (for multi-vehicle drivers switching between boat/taxi)
     */
    suspend fun updateVehicleType(vehicleType: VehicleType): Result<Unit> = runCatching {
        val userId = currentUser?.uid ?: throw Exception("User not logged in")
        android.util.Log.d("AuthRepo", "Updating vehicle type to: $vehicleType")
        
        // Update vehicleType and userType (BOAT -> CAPTAIN, TAXI -> DRIVER)
        val userType = when (vehicleType) {
            VehicleType.BOAT -> UserType.CAPTAIN
            VehicleType.TAXI -> UserType.DRIVER
        }
        
        firestore.collection("users")
            .document(userId)
            .update(
                mapOf(
                    "vehicleType" to vehicleType.name.lowercase(),
                    "userType" to userType.name.lowercase()
                )
            )
            .await()
    }
    
    suspend fun updateFcmToken(token: String): Result<Unit> = runCatching {
        val userId = currentUser?.uid ?: throw Exception("User not logged in")
        firestore.collection("users")
            .document(userId)
            .update("fcmToken", token)
            .await()
    }
    
    fun observeCurrentUser(): Flow<User?> = callbackFlow {
        val userId = currentUser?.uid
        if (userId == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        
        val listener = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(User::class.java))
            }
        
        awaitClose { listener.remove() }
    }
    
    suspend fun setDriverOnlineStatus(isOnline: Boolean): Result<Unit> = runCatching {
        val userId = currentUser?.uid ?: throw Exception("User not logged in")
        android.util.Log.d("AuthRepo", "Setting driver online status to: $isOnline for user: $userId")
        firestore.collection("users")
            .document(userId)
            .update("isOnline", isOnline)
            .await()
    }
    
    /**
     * Fix user type and vehicle type case to lowercase if needed (for Firebase queries to work)
     * Also sets vehicleType based on userType if missing
     */
    suspend fun fixUserTypeCase(): Result<Unit> = runCatching {
        val userId = currentUser?.uid ?: return@runCatching
        
        val userDoc = firestore.collection("users").document(userId).get().await()
        val userType = userDoc.getString("userType")
        val vehicleType = userDoc.getString("vehicleType")
        
        android.util.Log.d("AuthRepo", "fixUserTypeCase - Current userType: $userType, vehicleType: $vehicleType")
        
        val updates = mutableMapOf<String, Any>()
        
        // Fix userType if uppercase
        if (userType == "CAPTAIN" || userType == "DRIVER" || userType == "RIDER") {
            updates["userType"] = userType.lowercase()
            android.util.Log.d("AuthRepo", "Fixing userType from $userType to ${userType.lowercase()}")
        }
        
        // Fix vehicleType if uppercase
        if (vehicleType == "BOAT" || vehicleType == "TAXI") {
            updates["vehicleType"] = vehicleType.lowercase()
            android.util.Log.d("AuthRepo", "Fixing vehicleType from $vehicleType to ${vehicleType.lowercase()}")
        }
        
        // Set vehicleType if missing (based on userType)
        if (vehicleType == null || vehicleType.isEmpty()) {
            val inferredVehicleType = when (userType?.lowercase()) {
                "captain" -> "boat"
                "driver" -> "taxi"
                else -> null
            }
            if (inferredVehicleType != null) {
                updates["vehicleType"] = inferredVehicleType
                android.util.Log.d("AuthRepo", "Setting missing vehicleType to $inferredVehicleType based on userType")
            }
        }
        
        if (updates.isNotEmpty()) {
            firestore.collection("users").document(userId).update(updates).await()
            android.util.Log.d("AuthRepo", "Fixed user record: $updates")
        } else {
            android.util.Log.d("AuthRepo", "No fixes needed for user record")
        }
    }
    
    /**
     * Observe all online drivers in real-time
     */
    fun observeOnlineDrivers(): Flow<List<User>> = callbackFlow {
        android.util.Log.d("AuthRepo", "Starting to observe online drivers")
        
        // Query for ALL drivers/captains first, then filter by online status
        // This avoids issues with the isOnline field name mismatch
        val listener = firestore.collection("users")
            .whereIn("userType", listOf("driver", "captain"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("AuthRepo", "Error observing online drivers", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                android.util.Log.d("AuthRepo", "Raw drivers/captains: ${snapshot?.documents?.size ?: 0}")
                
                // Log raw data from each document
                snapshot?.documents?.forEach { doc ->
                    val isOnline = doc.getBoolean("isOnline") ?: doc.getBoolean("online") ?: false
                    android.util.Log.d("AuthRepo", "  Raw doc ${doc.id}: userType=${doc.getString("userType")}, vehicleType=${doc.getString("vehicleType")}, isOnline=$isOnline")
                }
                
                // Filter to only ONLINE drivers/captains
                val drivers = snapshot?.documents?.mapNotNull { doc ->
                    val isOnline = doc.getBoolean("isOnline") ?: doc.getBoolean("online") ?: false
                    if (isOnline) {
                        doc.toObject(User::class.java)?.copy(isOnline = true)
                    } else {
                        null
                    }
                } ?: emptyList()
                
                android.util.Log.d("AuthRepo", "Filtered to ${drivers.size} ONLINE drivers/captains")
                drivers.forEach { driver ->
                    android.util.Log.d("AuthRepo", "  - ${driver.fullName} (${driver.userType}, ${driver.vehicleType}) loc: ${driver.currentLocation?.latitude}, ${driver.currentLocation?.longitude}")
                }
                trySend(drivers)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Observe ALL online users (passengers + drivers) in real-time
     */
    fun observeAllOnlineUsers(): Flow<Int> = callbackFlow {
        android.util.Log.d("AuthRepo", "Starting to observe ALL online users")
        
        val listener = firestore.collection("users")
            .whereEqualTo("isOnline", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("AuthRepo", "Error observing online users", error)
                    trySend(0)
                    return@addSnapshotListener
                }
                
                val count = snapshot?.documents?.size ?: 0
                android.util.Log.d("AuthRepo", "Total online users: $count")
                trySend(count)
            }
        
        awaitClose { listener.remove() }
    }
    
    // ==================== PROMO CODES ====================
    
    companion object {
        // VIP Promo Codes - these give free bookings for life
        private val VIP_PROMO_CODES = mapOf(
            "BOATTAXIVIP" to "Free rides for life - VIP",
            "FOUNDER2026" to "Founder's special - Free rides forever",
            "LIFETIMEFREE" to "Lifetime free booking perk"
        )
        
        // Advertising Promo Codes - these give free ads for life
        private val ADS_PROMO_CODES = mapOf(
            "FREEADS" to "Lifetime free advertising perk",
            "ADSVIP" to "VIP free advertising forever",
            "ADVERTISER2026" to "Founder's special - Free ads forever"
        )
    }
    
    /**
     * Apply a promo code to the current user
     * Returns success message if valid, or error if invalid/already used
     * 
     * ANTI-ABUSE: Multi-layer protection to prevent the same person from
     * creating multiple accounts with different emails/phones to reuse promo codes:
     * 1. Phone number tracking
     * 2. Device ID tracking (Android ID - unique per device)
     * 3. IP address tracking (catches VPN users who switch)
     */
    suspend fun applyPromoCode(
        promoCode: String,
        deviceId: String? = null,
        ipAddress: String? = null
    ): Result<String> = runCatching {
        val uid = currentUser?.uid ?: throw Exception("Not logged in")
        val code = promoCode.trim().uppercase()
        
        android.util.Log.d("AuthRepo", "╔════════════════════════════════════════")
        android.util.Log.d("AuthRepo", "║ PROMO CODE ATTEMPT")
        android.util.Log.d("AuthRepo", "║ Code: $code")
        android.util.Log.d("AuthRepo", "║ User: $uid")
        android.util.Log.d("AuthRepo", "║ Device ID: $deviceId")
        android.util.Log.d("AuthRepo", "║ IP Address: $ipAddress")
        android.util.Log.d("AuthRepo", "╚════════════════════════════════════════")
        
        // ANTI-ABUSE: Require device ID
        if (deviceId.isNullOrBlank() || deviceId == "unknown") {
            android.util.Log.e("AuthRepo", "BLOCKED: No device ID")
            throw Exception("⛔ Unable to verify device. Please restart the app and try again.")
        }
        
        // ANTI-ABUSE: Require IP address
        if (ipAddress.isNullOrBlank() || ipAddress == "unknown") {
            android.util.Log.e("AuthRepo", "BLOCKED: No IP address")
            throw Exception("⛔ Unable to verify network. Please check your internet connection.")
        }
        
        // ═══════════════════════════════════════════════════════════════
        // LAYER 1: Check promo_redemptions for this Device ID
        // ═══════════════════════════════════════════════════════════════
        android.util.Log.d("AuthRepo", "→ Checking promo_redemptions for device: $deviceId")
        try {
            val deviceRedemptions = firestore.collection("promo_redemptions")
                .whereEqualTo("deviceId", deviceId)
                .get()
                .await()
            
            android.util.Log.d("AuthRepo", "  Found ${deviceRedemptions.documents.size} device redemptions")
            
            if (!deviceRedemptions.isEmpty) {
                val doc = deviceRedemptions.documents.first()
                val previousCode = doc.getString("promoCode") ?: "unknown"
                val previousUser = doc.getString("userId") ?: "unknown"
                val previousDate = doc.getTimestamp("redeemedAt")?.toDate()?.toString() ?: "unknown"
                android.util.Log.e("AuthRepo", "BLOCKED: Device already used promo!")
                android.util.Log.e("AuthRepo", "  Previous code: $previousCode")
                android.util.Log.e("AuthRepo", "  Previous user: $previousUser")
                android.util.Log.e("AuthRepo", "  Date: $previousDate")
                throw Exception("⛔ This device already used promo code: $previousCode. Promo codes are one-time only per device. Please purchase your subscription.")
            }
        } catch (e: Exception) {
            if (e.message?.startsWith("⛔") == true) throw e
            android.util.Log.e("AuthRepo", "Error checking device redemptions: ${e.message}")
        }
        
        // ═══════════════════════════════════════════════════════════════
        // LAYER 2: Check promo_redemptions for this IP Address
        // ═══════════════════════════════════════════════════════════════
        android.util.Log.d("AuthRepo", "→ Checking promo_redemptions for IP: $ipAddress")
        try {
            val ipRedemptions = firestore.collection("promo_redemptions")
                .whereEqualTo("ipAddress", ipAddress)
                .get()
                .await()
            
            android.util.Log.d("AuthRepo", "  Found ${ipRedemptions.documents.size} IP redemptions")
            
            if (!ipRedemptions.isEmpty) {
                val doc = ipRedemptions.documents.first()
                val previousCode = doc.getString("promoCode") ?: "unknown"
                android.util.Log.e("AuthRepo", "BLOCKED: IP already used promo!")
                throw Exception("⛔ A promo code was already used from this network: $previousCode. One promo per household. Please purchase your subscription.")
            }
        } catch (e: Exception) {
            if (e.message?.startsWith("⛔") == true) throw e
            android.util.Log.e("AuthRepo", "Error checking IP redemptions: ${e.message}")
        }
        
        // Check if user already has a promo applied on THIS account
        val userDoc = firestore.collection("users")
            .document(uid)
            .get()
            .await()
        
        val existingPromo = userDoc.getString("appliedPromoCode")
        if (!existingPromo.isNullOrBlank()) {
            android.util.Log.e("AuthRepo", "BLOCKED: User already has promo: $existingPromo")
            throw Exception("⛔ You already have promo code: $existingPromo applied. Please purchase if you need more.")
        }
        
        // ═══════════════════════════════════════════════════════════════
        // LAYER 3: Check ALL users with this device ID who have a promo
        // ═══════════════════════════════════════════════════════════════
        android.util.Log.d("AuthRepo", "→ Checking users collection for device ID: $deviceId")
        try {
            val usersWithSameDevice = firestore.collection("users")
                .whereEqualTo("lastDeviceId", deviceId)
                .get()
                .await()
            
            // Check if any of those users have a promo code applied
            val usersWithPromo = usersWithSameDevice.documents.filter { 
                !it.getString("appliedPromoCode").isNullOrBlank() 
            }
            
            if (usersWithPromo.isNotEmpty()) {
                val otherUser = usersWithPromo.first()
                val otherPromo = otherUser.getString("appliedPromoCode") ?: "a promo"
                android.util.Log.w("AuthRepo", "BLOCKED: Another account on this device has promo: $otherPromo")
                throw Exception("⛔ Another account on this device already used promo code: $otherPromo. Please purchase your subscription or ad package.")
            }
        } catch (e: Exception) {
            if (e.message?.contains("⛔") == true) throw e
            android.util.Log.w("AuthRepo", "Device user check error (continuing): ${e.message}")
        }
        
        // Check if it's a valid promo code (rides or ads)
        val isRidePromo = VIP_PROMO_CODES.containsKey(code)
        val isAdsPromo = ADS_PROMO_CODES.containsKey(code)
        
        val promoDescription = when {
            isRidePromo -> VIP_PROMO_CODES[code]
            isAdsPromo -> ADS_PROMO_CODES[code]
            else -> throw Exception("Invalid promo code. Please check and try again.")
        }
        
        // Get phone number - REQUIRED for promo codes
        val phoneNumber = userDoc.getString("phoneNumber")
        if (phoneNumber.isNullOrBlank()) {
            throw Exception("Please add your phone number in your profile before using a promo code")
        }
        val normalizedPhone = phoneNumber.replace(Regex("[^0-9+]"), "")
        
        // ANTI-ABUSE LAYER 4: Check Phone Number in promo_redemptions
        android.util.Log.d("AuthRepo", "Checking phone number: $normalizedPhone")
        val existingPhoneRedemption = firestore.collection("promo_redemptions")
            .whereEqualTo("phoneNumber", normalizedPhone)
            .get()
            .await()
        
        android.util.Log.d("AuthRepo", "Phone redemptions found: ${existingPhoneRedemption.documents.size}")
        
        if (!existingPhoneRedemption.isEmpty) {
            val previousCode = existingPhoneRedemption.documents.first().getString("promoCode") ?: "a promo"
            throw Exception("⛔ This phone number already used promo code: $previousCode. Please purchase your subscription or ad package.")
        }
        
        // ANTI-ABUSE LAYER 5: Check ALL users with this phone who have a promo
        try {
            val usersWithSamePhone = firestore.collection("users")
                .whereEqualTo("phoneNumber", phoneNumber)
                .get()
                .await()
            
            // Filter out current user and check if any have promo
            val otherUsersWithPromo = usersWithSamePhone.documents.filter { 
                it.id != uid && !it.getString("appliedPromoCode").isNullOrBlank()
            }
            if (otherUsersWithPromo.isNotEmpty()) {
                val otherPromo = otherUsersWithPromo.first().getString("appliedPromoCode") ?: "a promo"
                android.util.Log.w("AuthRepo", "BLOCKED: Another account with this phone has promo: $otherPromo")
                throw Exception("⛔ Another account with this phone number already used promo code: $otherPromo. Please purchase your subscription or ad package.")
            }
        } catch (e: Exception) {
            if (e.message?.contains("⛔") == true) throw e
            android.util.Log.w("AuthRepo", "Phone user check error (continuing): ${e.message}")
        }
        
        android.util.Log.d("AuthRepo", "✓ All anti-abuse checks passed! Recording redemption...")
        
        // Record this redemption with ALL identifiers BEFORE applying to prevent race conditions
        val redemptionData = mapOf(
            "phoneNumber" to normalizedPhone,
            "deviceId" to deviceId,
            "ipAddress" to ipAddress,
            "promoCode" to code,
            "userId" to uid,
            "userEmail" to (currentUser?.email ?: ""),
            "redeemedAt" to com.google.firebase.Timestamp.now()
        )
        
        android.util.Log.d("AuthRepo", "Recording to promo_redemptions: $redemptionData")
        
        // CRITICAL: Write redemption record FIRST with specific ID for verification
        // Use device ID as document ID so we can easily check if it exists
        val redemptionDocId = "device_${deviceId}"
        
        try {
            firestore.collection("promo_redemptions")
                .document(redemptionDocId)
                .set(redemptionData)
                .await()
            
            android.util.Log.d("AuthRepo", "✓ Recorded redemption with ID: $redemptionDocId")
            
            // Verify the write actually worked by reading it back
            val verifyDoc = firestore.collection("promo_redemptions")
                .document(redemptionDocId)
                .get()
                .await()
            
            if (!verifyDoc.exists()) {
                android.util.Log.e("AuthRepo", "VERIFICATION FAILED: Document doesn't exist after write!")
                throw Exception("⛔ Failed to verify promo usage record. Please try again.")
            }
            android.util.Log.d("AuthRepo", "✓ Verified redemption document exists")
            
        } catch (e: Exception) {
            android.util.Log.e("AuthRepo", "FAILED to record redemption: ${e.message}", e)
            throw Exception("⛔ Failed to record promo usage. Please try again. Error: ${e.message}")
        }
        
        // Update user document with promo AND store device ID for future checks
        val updateFields = mutableMapOf<String, Any>(
            "appliedPromoCode" to code,
            "promoAppliedAt" to com.google.firebase.Timestamp.now(),
            "lastDeviceId" to deviceId,
            "lastIpAddress" to ipAddress
        )
        
        if (isRidePromo) {
            updateFields["hasFreeBookingsForLife"] = true
        }
        if (isAdsPromo) {
            updateFields["hasFreeAdsForLife"] = true
        }
        
        firestore.collection("users")
            .document(uid)
            .update(updateFields)
            .await()
        
        android.util.Log.d("AuthRepo", "SUCCESS: Applied promo code $code to user $uid")
        
        when {
            isRidePromo -> "🎉 $promoDescription! You now have FREE bookings for life!"
            isAdsPromo -> "🎉 $promoDescription! You now have FREE advertising for life!"
            else -> "🎉 Promo code applied successfully!"
        }
    }
    
    /**
     * Check if current user has free bookings for life
     */
    suspend fun hasFreeBookingsForLife(): Boolean {
        val uid = currentUser?.uid ?: return false
        return try {
            val userDoc = firestore.collection("users")
                .document(uid)
                .get()
                .await()
            userDoc.getBoolean("hasFreeBookingsForLife") ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if current user has free ads for life
     */
    suspend fun hasFreeAdsForLife(): Boolean {
        val uid = currentUser?.uid ?: return false
        return try {
            val userDoc = firestore.collection("users")
                .document(uid)
                .get()
                .await()
            userDoc.getBoolean("hasFreeAdsForLife") ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if current user has already used the one-time free featured ad offer
     */
    suspend fun hasUsedFreeFeaturedOffer(): Boolean {
        val uid = currentUser?.uid ?: return true // Return true (already used) if not logged in
        return try {
            val userDoc = firestore.collection("users")
                .document(uid)
                .get()
                .await()
            userDoc.getBoolean("usedFreeFeaturedOffer") ?: false
        } catch (e: Exception) {
            // On error, assume used to prevent abuse
            true
        }
    }
    
    /**
     * Atomically claim the one-time free featured ad offer
     * Returns true if successfully claimed, false if already used or error
     * Uses a transaction to prevent race conditions
     */
    suspend fun claimFreeFeaturedOffer(): Boolean {
        val uid = currentUser?.uid ?: return false
        android.util.Log.d("AuthRepo", "Attempting to claim free featured offer for user: $uid")
        return try {
            val userRef = firestore.collection("users").document(uid)
            val result = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val alreadyUsed = snapshot.getBoolean("usedFreeFeaturedOffer") ?: false
                android.util.Log.d("AuthRepo", "Already used free offer: $alreadyUsed")
                if (alreadyUsed) {
                    false // Already used, cannot claim
                } else {
                    // Mark as used atomically
                    transaction.set(
                        userRef, 
                        mapOf("usedFreeFeaturedOffer" to true), 
                        com.google.firebase.firestore.SetOptions.merge()
                    )
                    true // Successfully claimed
                }
            }.await()
            android.util.Log.d("AuthRepo", "Claim transaction result: $result")
            result
        } catch (e: Exception) {
            android.util.Log.e("AuthRepo", "Failed to claim free offer: ${e.message}", e)
            false
        }
    }
    
    /**
     * Mark that user has used the one-time free featured ad offer
     */
    suspend fun markFreeFeaturedOfferUsed(): Boolean {
        val uid = currentUser?.uid ?: return false
        return try {
            firestore.collection("users")
                .document(uid)
                .set(mapOf("usedFreeFeaturedOffer" to true), com.google.firebase.firestore.SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
