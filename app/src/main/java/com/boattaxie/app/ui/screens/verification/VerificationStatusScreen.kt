package com.boattaxie.app.ui.screens.verification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.boattaxie.app.data.model.VerificationStatus
import com.boattaxie.app.ui.components.PrimaryButton
import com.boattaxie.app.ui.components.SecondaryButton
import com.boattaxie.app.ui.components.VerificationStatusBadge
import com.boattaxie.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationStatusScreen(
    onNavigateToDriverHome: () -> Unit,
    onNavigateToVerification: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: VerificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Navigate when approved
    LaunchedEffect(uiState.verificationStatus) {
        if (uiState.verificationStatus == VerificationStatus.APPROVED) {
            onNavigateToDriverHome()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verification Status") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState.verificationStatus) {
                VerificationStatus.PENDING -> PendingVerificationContent(
                    onGoToHome = onNavigateToHome
                )
                VerificationStatus.REJECTED -> RejectedVerificationContent(
                    onResubmit = { onNavigateToVerification("boat") }
                )
                VerificationStatus.APPROVED -> ApprovedVerificationContent(
                    onContinue = onNavigateToDriverHome
                )
                else -> NoVerificationContent(
                    onStartVerification = { onNavigateToVerification("boat") }
                )
            }
        }
    }
}

@Composable
private fun PendingVerificationContent(
    onGoToHome: () -> Unit
) {
    // Success header card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CheckCircle,
                "Success",
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF4CAF50)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Documents Submitted!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF2E7D32)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Your verification is being reviewed",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF388E3C),
                textAlign = TextAlign.Center
            )
        }
    }
    
    Spacer(modifier = Modifier.height(20.dp))
    
    // Main info card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "What happens next?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Once your documents are verified, you'll automatically have access to the driver side of the app and can start earning money!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF616161),
                lineHeight = 22.sp
            )
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // What you'll get as a verified driver
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DirectionsBoat,
                    "Driver",
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "After Verification You Can:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Driver feature list
            FeatureItem(icon = "📲", text = "Receive ride requests from passengers")
            FeatureItem(icon = "🗺️", text = "Your boat shows on the map when online")
            FeatureItem(icon = "👀", text = "Riders see your location in real-time")
            FeatureItem(icon = "💰", text = "Accept rides and start earning money")
            FeatureItem(icon = "⭐", text = "Build your reputation with ratings")
            FeatureItem(icon = "📊", text = "Track your earnings and trip history")
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Info about verification timeline
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Schedule,
                "Time",
                tint = Color(0xFFF57C00),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Review Time: 24-48 hours",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "You'll get a notification when approved",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF795548)
                )
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Enjoy the app in the meantime
    Text(
        text = "In the meantime, enjoy using the app as a passenger!",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF757575),
        textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    // Enter App Button
    Button(
        onClick = onGoToHome,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Primary),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            Icons.Default.Home,
            null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Enter the App",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
    
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun FeatureItem(icon: String, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF424242)
        )
    }
}

@Composable
private fun RejectedVerificationContent(
    onResubmit: () -> Unit
) {
    Icon(
        Icons.Default.Cancel,
        "Rejected",
        modifier = Modifier.size(100.dp),
        tint = VerificationRejected
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    VerificationStatusBadge(status = VerificationStatus.REJECTED)
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Text(
        text = "Verification Rejected",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(12.dp))
    
    Text(
        text = "Unfortunately, we couldn't verify your documents. Please check your email for details and resubmit with correct documents.",
        style = MaterialTheme.typography.bodyLarge,
        color = TextSecondary,
        textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    // Common reasons card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Common reasons for rejection:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• Blurry or unclear photos\n" +
                       "• Expired documents\n" +
                       "• Documents don't match vehicle info\n" +
                       "• Missing required documents",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    PrimaryButton(
        text = "Resubmit Documents",
        onClick = onResubmit
    )
}

@Composable
private fun ApprovedVerificationContent(
    onContinue: () -> Unit
) {
    Icon(
        Icons.Default.Verified,
        "Approved",
        modifier = Modifier.size(100.dp),
        tint = VerificationApproved
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    VerificationStatusBadge(status = VerificationStatus.APPROVED)
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Text(
        text = "You're Verified! 🎉",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(12.dp))
    
    Text(
        text = "Congratulations! Your account has been verified. You can now start accepting ride requests and earning money.",
        style = MaterialTheme.typography.bodyLarge,
        color = TextSecondary,
        textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    PrimaryButton(
        text = "Start Driving",
        onClick = onContinue
    )
}

@Composable
private fun NoVerificationContent(
    onStartVerification: () -> Unit
) {
    Icon(
        Icons.Default.VerifiedUser,
        "Verify",
        modifier = Modifier.size(100.dp),
        tint = Primary
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Text(
        text = "Get Verified",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(12.dp))
    
    Text(
        text = "To start accepting rides, you need to verify your identity and vehicle. It's quick, easy, and completely FREE!",
        style = MaterialTheme.typography.bodyLarge,
        color = TextSecondary,
        textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    PrimaryButton(
        text = "Start Verification",
        onClick = onStartVerification
    )
}
