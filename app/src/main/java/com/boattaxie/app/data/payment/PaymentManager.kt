package com.boattaxie.app.data.payment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stripe Payment Manager
 * Handles Stripe checkout sessions for ads
 * Flow: 
 * 1. App calls backend to create ad (saved as draft) and get Stripe checkout URL
 * 2. User pays on Stripe
 * 3. Stripe webhook notifies backend
 * 4. Backend activates ad with start/end dates
 * 5. App reads ad status from Firestore
 */
@Singleton
class PaymentManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "PaymentManager"
        private const val BACKEND_URL = "https://boattaxi-boattaxi.up.railway.app"
    }
    
    /**
     * Create Stripe Checkout Session for ad payment via backend
     * The backend:
     * 1. Creates the ad in Firestore as "draft"
     * 2. Creates a Stripe checkout session with the ad ID
     * 3. Returns the checkout URL
     * 
     * After payment, Stripe webhook activates the ad with proper dates
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
    ): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "createAdCheckoutSession: durationDays=$durationDays, isFeatured=$isFeatured, businessName=$businessName")
        
        try {
            val url = URL("$BACKEND_URL/api/create-ad-checkout-session")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 30000

            val jsonBody = JSONObject().apply {
                put("durationDays", durationDays)
                put("isFeatured", isFeatured)
                put("businessName", businessName)
                put("title", title)
                put("description", description)
                put("imageUri", imageUri ?: JSONObject.NULL)
                put("logoUri", logoUri ?: JSONObject.NULL)
                put("youtubeUrl", youtubeUrl ?: JSONObject.NULL)
                put("phone", phone ?: JSONObject.NULL)
                put("email", email ?: JSONObject.NULL)
                put("website", website ?: JSONObject.NULL)
                put("category", category)
                put("location", location ?: JSONObject.NULL)
            }.toString()

            Log.d(TAG, "Sending request to backend: $jsonBody")

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonBody)
                writer.flush()
            }

            val responseCode = connection.responseCode
            Log.d(TAG, "Response code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    val response = reader.readText()
                    Log.d(TAG, "Response: $response")
                    val jsonResponse = JSONObject(response)
                    val checkoutUrl = jsonResponse.getString("url")
                    
                    Log.d(TAG, "Opening Stripe checkout: $checkoutUrl")
                    
                    // Open Stripe checkout in browser
                    withContext(Dispatchers.Main) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl))
                        activity.startActivity(intent)
                    }
                    
                    connection.disconnect()
                    return@withContext true
                }
            } else {
                // Read error response
                val errorStream = connection.errorStream
                val errorResponse = if (errorStream != null) {
                    BufferedReader(InputStreamReader(errorStream)).use { it.readText() }
                } else {
                    "No error details"
                }
                Log.e(TAG, "HTTP error $responseCode: $errorResponse")
                connection.disconnect()
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create ad checkout session: ${e.message}", e)
            return@withContext false
        }
    }
}
