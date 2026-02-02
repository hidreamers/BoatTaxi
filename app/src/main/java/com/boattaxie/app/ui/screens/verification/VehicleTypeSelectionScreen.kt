package com.boattaxie.app.ui.screens.verification

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.boattaxie.app.data.model.VehicleType
import com.boattaxie.app.ui.components.PrimaryButton
import com.boattaxie.app.ui.screens.auth.AuthState
import com.boattaxie.app.ui.screens.auth.AuthViewModel
import com.boattaxie.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleTypeSelectionScreen(
    onNavigateToVerification: (String) -> Unit,
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isLoading by authViewModel.isLoading.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    
    // Track which vehicles are selected (multi-select)
    var hasBoat by remember { mutableStateOf(false) }
    var hasTaxi by remember { mutableStateOf(false) }
    
    // Track if we've triggered a signup from this screen
    var signupTriggered by remember { mutableStateOf(false) }
    
    // Navigate to verification only after successful signup triggered from this screen
    LaunchedEffect(authState, signupTriggered) {
        if (signupTriggered && authState is AuthState.LoggedIn) {
            // Navigate to verification for the first vehicle type
            val vehicleTypeName = if (hasBoat) "boat" else "taxi"
            onNavigateToVerification(vehicleTypeName)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Your Vehicles") },
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
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "What vehicles do you have?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Select all that apply - you can drive both!",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Boat option (checkbox style)
            VehicleOptionCard(
                icon = Icons.Default.DirectionsBoat,
                title = "Boat Captain",
                description = "I have a boat and want to offer water taxi services",
                isSelected = hasBoat,
                onClick = { hasBoat = !hasBoat }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Taxi option (checkbox style)
            VehicleOptionCard(
                icon = Icons.Default.LocalTaxi,
                title = "Taxi Driver",
                description = "I have a taxi/car and want to offer land rides",
                isSelected = hasTaxi,
                onClick = { hasTaxi = !hasTaxi }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Info card
            if (hasBoat && hasTaxi) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            "Both",
                            tint = Success,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Multi-Vehicle Driver",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "You'll be able to switch between boat and taxi mode in the app to accept different types of rides!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Info.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            "Info",
                            tint = Info,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Free Verification",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Verification is completely free for all captains and drivers. Upload your documents and start earning once approved.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Show error if any
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            PrimaryButton(
                text = "Continue to Verification",
                onClick = {
                    // Set the vehicle selections in the ViewModel
                    authViewModel.setVehicleSelections(hasBoat = hasBoat, hasTaxi = hasTaxi)
                    signupTriggered = true
                    authViewModel.signUp()
                },
                enabled = (hasBoat || hasTaxi) && !isLoading,
                isLoading = isLoading
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Primary.copy(alpha = 0.1f) else Surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Primary else Divider
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (isSelected) Primary else TextSecondary.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .padding(12.dp),
                    tint = if (isSelected) TextOnPrimary else TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            
            // Checkbox indicator
            if (isSelected) {
                Icon(
                    Icons.Default.CheckBox,
                    "Selected",
                    tint = Primary,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Icon(
                    Icons.Default.CheckBoxOutlineBlank,
                    "Not Selected",
                    tint = TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
