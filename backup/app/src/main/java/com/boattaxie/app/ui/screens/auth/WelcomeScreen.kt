package com.boattaxie.app.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.boattaxie.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToWelcome: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToDriverHome: () -> Unit,
    onNavigateToVerification: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    
    LaunchedEffect(authState) {
        delay(1500) // Show splash for at least 1.5 seconds
        
        when (authState) {
            is AuthState.LoggedIn -> {
                // Always go to Home screen first - users can switch to driver mode from there
                onNavigateToHome()
            }
            is AuthState.LoggedOut -> onNavigateToWelcome()
            else -> {} // Still loading
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBoat,
                    contentDescription = "Boat",
                    modifier = Modifier.size(64.dp),
                    tint = BoatColor
                )
                Icon(
                    imageVector = Icons.Default.LocalTaxi,
                    contentDescription = "Taxi",
                    modifier = Modifier.size(64.dp),
                    tint = TaxiColor
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "BoatTaxie",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Your ride on water and land",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            LoadingDots()
        }
    }
}

@Composable
fun LoadingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 200)
                ),
                label = "dot_$index"
            )
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(scale)
                    .background(Primary, CircleShape)
            )
        }
    }
}

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var showAccountTypeSelection by remember { mutableStateOf(false) }
    var selectedAction by remember { mutableStateOf("") } // "login" or "signup"
    
    if (showAccountTypeSelection) {
        // Show Rider vs Driver selection
        AccountTypeSelectionScreen(
            actionType = selectedAction,
            onRiderSelected = {
                // Set user type to RIDER
                viewModel.updateSelectedUserType(com.boattaxie.app.data.model.UserType.RIDER)
                // For riders, go to login/signup
                if (selectedAction == "login") {
                    onNavigateToLogin()
                } else {
                    onNavigateToSignUp()
                }
            },
            onDriverSelected = {
                // Set user type to CAPTAIN (will select specific type later)
                viewModel.updateSelectedUserType(com.boattaxie.app.data.model.UserType.CAPTAIN)
                // For drivers, go to login/signup (they'll select vehicle type later)
                if (selectedAction == "login") {
                    onNavigateToLogin()
                } else {
                    onNavigateToSignUp()
                }
            },
            onBack = { showAccountTypeSelection = false }
        )
    } else {
        // Main welcome screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Logo and tagline
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBoat,
                        contentDescription = "Boat",
                        modifier = Modifier.size(80.dp),
                        tint = BoatColor
                    )
                    Icon(
                        imageVector = Icons.Default.LocalTaxi,
                        contentDescription = "Taxi",
                        modifier = Modifier.size(80.dp),
                        tint = TaxiColor
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Welcome to BoatTaxie",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Book boats and taxis with ease.\nWhether on water or land, we've got you covered.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
            
            // Features list
            Column(
                modifier = Modifier.padding(vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureItem(
                    icon = "🚤",
                    title = "Book Boats",
                    description = "Water taxis, speedboats, and more"
                )
                FeatureItem(
                    icon = "🚕",
                    title = "Book Taxis",
                    description = "Quick and reliable taxi service"
                )
                FeatureItem(
                    icon = "💰",
                    title = "Affordable Pricing",
                    description = "Starting at just \$2.99/day"
                )
            }
            
            // Buttons
            Column(
                modifier = Modifier.padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { 
                        selectedAction = "signup"
                        showAccountTypeSelection = true 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(
                        text = "Get Started",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                OutlinedButton(
                    onClick = { 
                        selectedAction = "login"
                        showAccountTypeSelection = true 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "I already have an account",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Account type selection screen - Are you a Rider or Driver?
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountTypeSelectionScreen(
    actionType: String,
    onRiderSelected: () -> Unit,
    onDriverSelected: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (actionType == "login") "Login as..." else "Sign up as..."
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "How will you use BoatTaxie?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "You can have separate accounts for riding and driving",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Rider option
            Card(
                onClick = onRiderSelected,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = Primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "🙋",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "I need a ride",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Book boats and taxis to get around",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    
                    Icon(
                        Icons.Default.ChevronRight,
                        null,
                        tint = Primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Driver/Captain option
            Card(
                onClick = onDriverSelected,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = Success
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🚕",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "🚤",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "I want to drive",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Become a taxi driver or boat captain",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    
                    Icon(
                        Icons.Default.ChevronRight,
                        null,
                        tint = Success
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Info text
            Surface(
                color = Info.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "💡",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "You can create multiple accounts - one for riding and one for driving. Just use different email addresses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Info
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeatureItem(
    icon: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
