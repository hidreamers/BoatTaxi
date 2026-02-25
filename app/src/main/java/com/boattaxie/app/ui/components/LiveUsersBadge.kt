package com.boattaxie.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * A composable that shows real-time active user count using Firestore
 * Can be placed on any screen to show live users
 */
@Composable
fun LiveUsersBadge(
    modifier: Modifier = Modifier,
    showDriverCount: Boolean = true,
    compact: Boolean = false
) {
    // Listen to active users from Firestore
    var activeRiders by remember { mutableStateOf(0) }
    var activeDrivers by remember { mutableStateOf(0) }
    
    // Set up Firestore listener
    DisposableEffect(Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        
        android.util.Log.d("LiveUsersBadge", "Setting up Firestore listeners, userId: $userId")
        
        // Mark current user as active in Firestore
        var userDocRef: com.google.firebase.firestore.DocumentReference? = null
        if (userId != null) {
            userDocRef = firestore.collection("active_users").document(userId)
            userDocRef.set(mapOf(
                "userId" to userId,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "type" to "rider"
            )).addOnSuccessListener {
                android.util.Log.d("LiveUsersBadge", "User marked as active")
            }.addOnFailureListener { e ->
                android.util.Log.e("LiveUsersBadge", "Failed to mark user active: ${e.message}")
            }
        }
        
        // Listen for active users count
        val listener: ListenerRegistration = firestore.collection("active_users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("LiveUsersBadge", "Firestore error: ${error.message}")
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
                    
                    activeRiders = riders
                    activeDrivers = drivers
                    android.util.Log.d("LiveUsersBadge", "Active users: riders=$riders, drivers=$drivers, total=${riders + drivers}")
                }
            }
        
        onDispose {
            listener.remove()
            // Don't delete user on screen change - let timestamp filtering handle stale users
            // User will be removed when app is closed via ActiveUsersRepository or after 5 min timeout
        }
    }
    
    val totalUsers = activeRiders + activeDrivers
    
    // Always show the badge (even when 0 to indicate connectivity)
    if (compact) {
        // Compact version - count with "online" label
        Surface(
            modifier = modifier,
            color = Color(0xFF4CAF50).copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pulsing green dot
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseAlpha"
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50).copy(alpha = alpha))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$totalUsers online",
                    fontSize = 10.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        // Full version with badge
        Surface(
            modifier = modifier,
            color = Color(0xFF4CAF50).copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pulsing green dot
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseAlpha"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50).copy(alpha = alpha))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$totalUsers live",
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
                if (showDriverCount && activeDrivers > 0) {
                    Text(
                        text = " • ${activeDrivers}🚗",
                        fontSize = 11.sp,
                        color = Color(0xFF4CAF50).copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
