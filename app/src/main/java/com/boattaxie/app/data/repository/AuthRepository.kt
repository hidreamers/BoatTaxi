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
}
