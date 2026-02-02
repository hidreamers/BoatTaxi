package com.boattaxie.app.ui.screens.subscription

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boattaxie.app.BuildConfig
import com.boattaxie.app.data.model.*
import com.boattaxie.app.data.repository.SubscriptionRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class SubscriptionUiState(
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val hasActiveSubscription: Boolean = false,
    val currentSubscription: Subscription? = null,
    val selectedPlan: SubscriptionPlan? = null,
    val paymentSuccess: Boolean = false,
    val errorMessage: String? = null,
    val paypalUrl: String? = null // URL to open for PayPal payment
)

class SubscriptionViewModel(
    private val subscriptionRepository: SubscriptionRepository,
    private val application: Application
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()
    
    private var pendingPlan: SubscriptionPlan? = null
    
    init {
        loadSubscription()
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
    
    fun selectPlan(plan: SubscriptionPlan) {
        _uiState.update { it.copy(selectedPlan = plan) }
    }
    
    /**
     * Start Stripe payment process using checkout session from backend
     */
    fun startStripePayment(activity: Activity, plan: SubscriptionPlan) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _uiState.update { it.copy(isProcessing = false, errorMessage = "User not logged in") }
            return
        }

        pendingPlan = plan
        _uiState.update { it.copy(isProcessing = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val checkoutUrl = createCheckoutSession(plan, userId)
                _uiState.update { it.copy(isProcessing = false) }
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl))
                activity.startActivity(intent)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Failed to create checkout session: ${e.message}"
                    )
                }
            }
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
                paymentMethodId = "stripe_payment_link",
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
    
    private suspend fun createCheckoutSession(plan: SubscriptionPlan, userId: String): String = withContext(Dispatchers.IO) {
        val url = URL("https://boattaxi-boattaxi.up.railway.app/api/create-checkout-session")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val jsonInputString = JSONObject().apply {
            put("planId", plan.name) // Assuming plan.name matches the backend plan IDs
            put("userId", userId)
        }.toString()

        try {
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonInputString)
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    val response = reader.readText()
                    val jsonResponse = JSONObject(response)
                    return@withContext jsonResponse.getString("url")
                }
            } else {
                throw Exception("HTTP error code: $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
    }
    
    fun onAppResume() {
        // Check for successful payment when app resumes
        checkForSuccessfulPayment()
    }
}
