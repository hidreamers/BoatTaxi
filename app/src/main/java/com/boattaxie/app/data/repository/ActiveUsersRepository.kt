package com.boattaxie.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

data class ActiveUsersCount(
    val totalUsers: Int = 0,
    val riders: Int = 0,
    val drivers: Int = 0
)

/**
 * Repository for tracking active users in real-time using Firestore
 */
@Singleton
class ActiveUsersRepository @Inject constructor(
    private val auth: FirebaseAuth
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val activeUsersCollection = firestore.collection("active_users")
    
    private var currentUserDocId: String? = null
    
    /**
     * Mark current user as active (online)
     * @param isDriver true if user is a driver, false if rider
     */
    fun setUserActive(isDriver: Boolean = false) {
        val userId = auth.currentUser?.uid ?: return
        val userType = if (isDriver) "driver" else "rider"
        
        currentUserDocId = userId
        
        activeUsersCollection.document(userId).set(mapOf(
            "userId" to userId,
            "type" to userType,
            "timestamp" to FieldValue.serverTimestamp()
        )).addOnSuccessListener {
            android.util.Log.d("ActiveUsers", "User marked as active: $userId ($userType)")
        }.addOnFailureListener { e ->
            android.util.Log.e("ActiveUsers", "Failed to mark user active: ${e.message}")
        }
    }
    
    /**
     * Mark current user as inactive (offline)
     */
    fun setUserInactive() {
        currentUserDocId?.let { docId ->
            activeUsersCollection.document(docId).delete()
                .addOnSuccessListener {
                    android.util.Log.d("ActiveUsers", "User marked as inactive: $docId")
                }
        }
        currentUserDocId = null
    }
    
    /**
     * Get real-time count of active users
     */
    fun getActiveUsersCount(): Flow<ActiveUsersCount> = callbackFlow {
        val listener: ListenerRegistration = activeUsersCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("ActiveUsers", "Firestore error: ${error.message}")
                    return@addSnapshotListener
                }
                
                snapshot?.let { docs ->
                    var riders = 0
                    var drivers = 0
                    
                    // Count by type and filter old entries (last 5 minutes)
                    val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
                    
                    for (doc in docs.documents) {
                        val timestamp = doc.getTimestamp("timestamp")
                        val timestampMillis = timestamp?.toDate()?.time ?: 0
                        
                        // Only count users active in last 5 minutes
                        if (timestampMillis > fiveMinutesAgo || timestamp == null) {
                            val type = doc.getString("type") ?: "rider"
                            if (type == "driver") {
                                drivers++
                            } else {
                                riders++
                            }
                        }
                    }
                    
                    trySend(ActiveUsersCount(
                        totalUsers = riders + drivers,
                        riders = riders,
                        drivers = drivers
                    ))
                }
            }
        
        awaitClose {
            listener.remove()
        }
    }
}
