package com.boattaxie.app.ui.screens.verification

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
import com.boattaxie.app.data.model.VerificationStatus
import com.boattaxie.app.ui.components.PrimaryButton
import com.boattaxie.app.ui.components.SecondaryButton
import com.boattaxie.app.ui.components.VerificationStatusBadge
import com.boattaxie.app.ui.theme.*

@Composable
fun VerificationStatusScreen(
    onNavigateToDriverHome: () -> Unit,
    onNavigateToVerification: (String) -> Unit,
    viewModel: VerificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Navigate when approved
    LaunchedEffect(uiState.verificationStatus) {
        if (uiState.verificationStatus == VerificationStatus.APPROVED) {
            onNavigateToDriverHome()
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState.verificationStatus) {
                VerificationStatus.PENDING -> PendingVerificationContent()
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
private fun PendingVerificationContent() {
    Icon(
        Icons.Default.HourglassTop,
        "Pending",
        modifier = Modifier.size(100.dp),
        tint = VerificationPending
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    VerificationStatusBadge(status = VerificationStatus.PENDING)
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Text(
        text = "Verification in Progress",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(12.dp))
    
    Text(
        text = "We're reviewing your documents. This usually takes 24-48 hours. You'll receive a notification once your account is verified.",
        style = MaterialTheme.typography.bodyLarge,
        color = TextSecondary,
        textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    // Info card
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
                    text = "What happens next?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Our team will review your documents\n" +
                           "• You'll receive an email notification\n" +
                           "• Once approved, you can start accepting rides",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
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
