package com.boattaxie.app.ui.screens.auth

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
import com.boattaxie.app.data.model.UserType
import com.boattaxie.app.ui.components.PrimaryButton
import com.boattaxie.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserTypeSelectionScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToVehicleTypeSelection: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val selectedUserType by viewModel.selectedUserType.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // Navigate after successful signup
    LaunchedEffect(authState) {
        if (authState is AuthState.LoggedIn) {
            val user = (authState as AuthState.LoggedIn).user
            if (user.userType == UserType.RIDER) {
                onNavigateToHome()
            } else {
                onNavigateToVehicleTypeSelection()
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "How will you use BoatTaxie?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Choose your account type to get started",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        // Show error message if any
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorMessage!!,
                    color = Error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Driver & Rider option (can do both)
        UserTypeCard(
            icon = Icons.Default.DirectionsBoat,
            title = "Driver & Rider",
            description = "Drive for income AND book rides as a passenger",
            price = "FREE - Drive & Ride free!",
            isSelected = selectedUserType == UserType.CAPTAIN,
            onClick = { viewModel.updateSelectedUserType(UserType.CAPTAIN) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Rider only option
        UserTypeCard(
            icon = Icons.Default.Person,
            title = "Rider Only",
            description = "Just book boats and taxis for your trips",
            price = "$2.99/day subscription",
            isSelected = selectedUserType == UserType.RIDER,
            onClick = { viewModel.updateSelectedUserType(UserType.RIDER) }
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        PrimaryButton(
            text = "Continue",
            onClick = {
                viewModel.clearError()
                if (selectedUserType != null) {
                    if (selectedUserType == UserType.RIDER) {
                        // Riders sign up immediately
                        viewModel.signUp()
                    } else {
                        // Captains/Drivers go to vehicle type selection first, signup happens there
                        onNavigateToVehicleTypeSelection()
                    }
                }
            },
            enabled = selectedUserType != null,
            isLoading = isLoading
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserTypeCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    price: String,
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
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (title.contains("Captain")) Success.copy(alpha = 0.1f) 
                           else Accent.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = price,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (title.contains("Captain")) Success else Accent,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    "Selected",
                    tint = Primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
