package com.boattaxie.app.ui.screens.subscription

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boattaxie.app.data.model.*
import com.boattaxie.app.data.repository.SubscriptionRepository
import com.boattaxie.app.ui.components.*
import com.boattaxie.app.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
@Composable
fun subscriptionViewModel(): SubscriptionViewModel {
    val context = LocalContext.current
    return viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            // For now, create a simple mock repository
            // In a real app, you'd inject this properly
            val repository = SubscriptionRepository(
                FirebaseAuth.getInstance(),
                FirebaseFirestore.getInstance()
            )
            val application = context.applicationContext as android.app.Application
            return SubscriptionViewModel(repository, application) as T
        }
    })
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onNavigateToPlans: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SubscriptionViewModel = subscriptionViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subscription") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.hasActiveSubscription && uiState.currentSubscription != null) {
                ActiveSubscriptionView(
                    subscription = uiState.currentSubscription!!,
                    onRenew = onNavigateToPlans,
                    onCancel = { viewModel.cancelSubscription() }
                )
            } else {
                NoSubscriptionView(onSubscribe = onNavigateToPlans)
            }
        }
    }
}

@Composable
private fun ActiveSubscriptionView(
    subscription: Subscription,
    onRenew: () -> Unit,
    onCancel: () -> Unit
) {
    val remainingDays = SubscriptionHelper.getRemainingDays(subscription)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CheckCircle,
            "Active",
            modifier = Modifier.size(80.dp),
            tint = Success
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Subscription Active",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "${subscription.plan.displayName} Plan",
            style = MaterialTheme.typography.titleLarge,
            color = Primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Stats card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Days Remaining",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "$remainingDays",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingDays <= 3) Warning else Success
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Price Paid",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "$${String.format("%.2f", subscription.price)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress bar
                LinearProgressIndicator(
                    progress = remainingDays.toFloat() / subscription.plan.days,
                    modifier = Modifier.fillMaxWidth(),
                    color = if (remainingDays <= 3) Warning else Success,
                    trackColor = if (remainingDays <= 3) Warning.copy(alpha = 0.2f) else Success.copy(alpha = 0.2f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Benefits
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Your Benefits",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                BenefitItem("Unlimited boat bookings")
                BenefitItem("Unlimited taxi bookings")
                BenefitItem("Priority support")
                BenefitItem("No booking fees")
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Actions
        if (remainingDays <= 7) {
            PrimaryButton(
                text = "Renew Subscription",
                onClick = onRenew
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        TextButton(onClick = onCancel) {
            Text("Cancel Subscription", color = Error)
        }
    }
}

@Composable
private fun NoSubscriptionView(onSubscribe: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Icon(
            Icons.Default.CardMembership,
            "Subscribe",
            modifier = Modifier.size(100.dp),
            tint = Primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Unlock Unlimited Rides",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Subscribe to book boats and taxis anytime",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Pricing highlight
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Starting at",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "$2.99",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Text(
                        text = "/day",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Benefits
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "What You Get",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                BenefitItem("Unlimited boat bookings")
                BenefitItem("Unlimited taxi bookings")
                BenefitItem("Flexible plans (1 day to 1 month)")
                BenefitItem("Save up to 45% with longer plans")
                BenefitItem("Cancel anytime")
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        PrimaryButton(
            text = "View Plans",
            onClick = onSubscribe
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun BenefitItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.CheckCircle,
            null,
            tint = Success,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionPlansScreen(
    onNavigateToPayment: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SubscriptionViewModel = subscriptionViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Your Plan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Header with benefits
                Surface(
                    color = Primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🚤",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Unlimited Rides in Bocas del Toro",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Benefits grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🚕", fontSize = 24.sp)
                                Text("Taxi", style = MaterialTheme.typography.labelSmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🚤", fontSize = 24.sp)
                                Text("Boat", style = MaterialTheme.typography.labelSmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📍", fontSize = 24.sp)
                                Text("Tracking", style = MaterialTheme.typography.labelSmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⭐", fontSize = 24.sp)
                                Text("Support", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Select a plan that works for you",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Pricing info
                Text(
                    text = "💡 Longer plans = bigger savings!",
                    style = MaterialTheme.typography.labelMedium,
                    color = Success,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            items(SubscriptionPlan.values().toList()) { plan ->
                SubscriptionPlanCard(
                    plan = plan,
                    isSelected = uiState.selectedPlan == plan,
                    onClick = { viewModel.selectPlan(plan) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Full pricing breakdown
                Surface(
                    color = Surface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📊 Full Pricing Breakdown",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Price table
                        SubscriptionPlan.values().forEach { plan ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = plan.displayName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Row {
                                    if (plan.getSavingsPercentage() > 0) {
                                        Text(
                                            text = "$${String.format("%.2f", plan.originalPrice)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(
                                        text = "$${String.format("%.2f", plan.price)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (plan.getSavingsPercentage() > 0) Success else TextPrimary
                                    )
                                    if (plan.getSavingsPercentage() > 0) {
                                        Text(
                                            text = " (-${plan.getSavingsPercentage()}%)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Success
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "✅ All plans include unlimited taxi & boat rides",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "✅ Real-time driver/captain tracking",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "✅ Priority customer support",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "✅ No hidden fees",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                PrimaryButton(
                    text = "Continue to Payment",
                    onClick = {
                        uiState.selectedPlan?.let { plan ->
                            onNavigateToPayment(plan.name)
                        }
                    },
                    enabled = uiState.selectedPlan != null
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    planId: String,
    onPaymentSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SubscriptionViewModel = subscriptionViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val plan = remember(planId) {
        SubscriptionPlan.values().find { it.name == planId } ?: SubscriptionPlan.DAY_PASS
    }
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    // Stripe is now initialized in ViewModel init block
    
    LaunchedEffect(uiState.paymentSuccess) {
        if (uiState.paymentSuccess) {
            onPaymentSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complete Your Subscription") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            // Order summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎉", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Order Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${plan.displayName} Subscription",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Unlimited rides for ${plan.days} days",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            if (plan.getSavingsPercentage() > 0) {
                                Text(
                                    text = "$${String.format("%.2f", plan.originalPrice)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                            Text(
                                text = "$${String.format("%.2f", plan.price)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }
                    
                    if (plan.getSavingsPercentage() > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Success.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "🎊 You save $${String.format("%.2f", plan.getSavingsAmount())} (${plan.getSavingsPercentage()}% off)!",
                                style = MaterialTheme.typography.labelMedium,
                                color = Success,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // What you get
            Text(
                text = "✅ What's Included",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("• Unlimited taxi rides", style = MaterialTheme.typography.bodyMedium)
                Text("• Unlimited boat rides", style = MaterialTheme.typography.bodyMedium)
                Text("• Real-time driver tracking", style = MaterialTheme.typography.bodyMedium)
                Text("• Priority customer support", style = MaterialTheme.typography.bodyMedium)
                Text("• Valid for ${plan.days} days from activation", style = MaterialTheme.typography.bodyMedium)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Payment methods
            Text(
                text = "💳 Payment Method",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stripe payment option
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f)),
                border = BorderStroke(2.dp, Primary)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💳", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Credit Card",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Text(
                            text = "Fast & secure payment via Stripe",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Error message
            if (uiState.errorMessage != null) {
                Surface(
                    color = Error.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, "Error", tint = Error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.errorMessage!!,
                            color = Error
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Pay button
            Button(
                onClick = { 
                    activity?.let { 
                        viewModel.startStripePayment(it, plan)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = Success),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Processing Payment...")
                } else {
                    Icon(Icons.Default.CreditCard, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pay with Stripe $${String.format("%.2f", plan.price)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Trust badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔒", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Secure payment powered by Stripe",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun PaymentSuccessScreen(
    onNavigateToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            "Success",
            modifier = Modifier.size(120.dp),
            tint = Success
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Payment Successful!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Your subscription is now active. Enjoy unlimited rides!",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        PrimaryButton(
            text = "Start Booking",
            onClick = onNavigateToHome
        )
    }
}
