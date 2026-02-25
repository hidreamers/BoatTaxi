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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boattaxie.app.R
import com.boattaxie.app.data.model.*
import com.boattaxie.app.data.repository.AuthRepository
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
            val firestore = FirebaseFirestore.getInstance()
            val firebaseAuth = FirebaseAuth.getInstance()
            val repository = SubscriptionRepository(
                firebaseAuth,
                firestore
            )
            val authRepository = AuthRepository(firebaseAuth, firestore)
            val application = context.applicationContext as android.app.Application
            return SubscriptionViewModel(repository, authRepository, application) as T
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
                title = { Text(stringResource(R.string.subscription)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
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
            } else if (uiState.hasFreeBookings && uiState.userPromoCode != null) {
                // User has free bookings from promo code
                PromoActiveView(
                    promoCode = uiState.userPromoCode!!
                )
            } else {
                NoSubscriptionView(
                    onSubscribe = onNavigateToPlans
                )
            }
        }
    }
}

@Composable
private fun PromoActiveView(
    promoCode: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Large success badge
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Success),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.White
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "🎉 " + stringResource(R.string.free_subscription_active),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.lifetime_access),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Promo code badge
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f)),
            border = BorderStroke(2.dp, Primary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocalOffer,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.promo_code_applied),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = promoCode,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Benefits card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.your_benefits),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                BenefitItem(stringResource(R.string.benefit_free_rides_forever))
                Spacer(modifier = Modifier.height(8.dp))
                BenefitItem(stringResource(R.string.benefit_unlimited_boats))
                Spacer(modifier = Modifier.height(8.dp))
                BenefitItem(stringResource(R.string.benefit_unlimited_taxis))
                Spacer(modifier = Modifier.height(8.dp))
                BenefitItem(stringResource(R.string.benefit_no_payment_needed))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.enjoy_free_rides),
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ActiveSubscriptionView(
    subscription: Subscription,
    onRenew: () -> Unit,
    onCancel: () -> Unit
) {
    val remainingDays = SubscriptionHelper.getRemainingDays(subscription)
    
    // Format expiration date
    val expirationDate = remember(subscription.endDate) {
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", java.util.Locale.getDefault())
        sdf.format(subscription.endDate.toDate())
    }
    
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
            text = stringResource(R.string.subscription_active_title),
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
                // Expiration date - prominent display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (remainingDays <= 3) Warning else Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Expires",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = expirationDate,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingDays <= 3) Warning else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.days_remaining_label),
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
                            text = stringResource(R.string.price_paid),
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
                    text = stringResource(R.string.your_benefits),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                BenefitItem(stringResource(R.string.unlimited_boat_bookings))
                BenefitItem(stringResource(R.string.unlimited_taxi_bookings))
                BenefitItem(stringResource(R.string.priority_support))
                BenefitItem(stringResource(R.string.no_booking_fees))
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Actions
        if (remainingDays <= 7) {
            PrimaryButton(
                text = stringResource(R.string.renew_subscription_btn),
                onClick = onRenew
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        TextButton(onClick = onCancel) {
            Text(stringResource(R.string.cancel_subscription), color = Error)
        }
    }
}

@Composable
private fun NoSubscriptionView(
    onSubscribe: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Icon(
            Icons.Default.CardMembership,
            stringResource(R.string.subscribe),
            modifier = Modifier.size(80.dp),
            tint = Primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.unlock_unlimited_rides),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.subscribe_anytime),
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
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
                    text = stringResource(R.string.starting_at),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "$1.99",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Text(
                        text = stringResource(R.string.per_day),
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
                    text = stringResource(R.string.what_you_get),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                BenefitItem(stringResource(R.string.benefit_unlimited_boats))
                BenefitItem(stringResource(R.string.benefit_unlimited_taxis))
                BenefitItem(stringResource(R.string.benefit_flexible_plans))
                BenefitItem(stringResource(R.string.benefit_save_percent))
                BenefitItem(stringResource(R.string.benefit_cancel_anytime))
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        PrimaryButton(
            text = stringResource(R.string.view_plans),
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
                title = { Text(stringResource(R.string.choose_your_plan)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        },
        // Fixed bottom bar for continue button - always visible
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    PrimaryButton(
                        text = if (uiState.selectedPlan != null) 
                            stringResource(R.string.continue_price, String.format("%.2f", uiState.selectedPlan!!.price))
                        else 
                            stringResource(R.string.select_a_plan),
                        onClick = {
                            uiState.selectedPlan?.let { plan ->
                                onNavigateToPayment(plan.name)
                            }
                        },
                        enabled = uiState.selectedPlan != null
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Compact header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.unlimited_rides),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.taxi_boat_cancel),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Surface(
                        color = Success.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.save_up_to),
                            style = MaterialTheme.typography.labelSmall,
                            color = Success,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = stringResource(R.string.select_your_plan),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Show ALL plans directly - always visible
            items(SubscriptionPlan.values().toList()) { plan ->
                SubscriptionPlanCard(
                    plan = plan,
                    isSelected = uiState.selectedPlan == plan,
                    onClick = { viewModel.selectPlan(plan) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(12.dp))
                
                // Compact benefits list
                Surface(
                    color = Surface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = stringResource(R.string.all_plans_include),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.plans_benefits),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
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

    // Google Play Billing is initialized in ViewModel init block
    
    LaunchedEffect(uiState.paymentSuccess) {
        if (uiState.paymentSuccess) {
            onPaymentSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.complete_subscription)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
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
                            text = stringResource(R.string.order_summary),
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
                text = stringResource(R.string.whats_included),
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
            
            // Google Play payment option
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f)),
                border = BorderStroke(2.dp, Primary)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🛒", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Google Play",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Text(
                            text = "Fast & secure payment via Google Play",
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
                        viewModel.startGooglePlayPayment(it, plan)
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
                    Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pay with Google Play $${String.format("%.2f", plan.price)}",
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
                    text = "Secure payment powered by Google Play",
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
