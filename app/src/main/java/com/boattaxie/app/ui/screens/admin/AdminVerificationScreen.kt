package com.boattaxie.app.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.boattaxie.app.data.model.VerificationSubmission
import com.boattaxie.app.data.model.VerificationStatus
import com.boattaxie.app.data.model.VehicleType
import com.boattaxie.app.data.model.DocumentType
import com.boattaxie.app.ui.theme.*

// Tab filter options
enum class VerificationFilter(val label: String) {
    ALL("All"),
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVerificationScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminVerificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedFilter by remember { mutableStateOf(VerificationFilter.ALL) }
    
    // Load submissions on first composition
    LaunchedEffect(Unit) {
        viewModel.loadSubmissions()
    }
    
    // Show messages
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Driver Verifications") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadSubmissions() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Stats header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Pending", uiState.pendingCount, Color(0xFFFF9800))
                    StatItem("Approved", uiState.approvedCount, Success)
                    StatItem("Rejected", uiState.rejectedCount, Color(0xFFD32F2F))
                }
            }
            
            // Filter tabs
            ScrollableTabRow(
                selectedTabIndex = selectedFilter.ordinal,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 16.dp
            ) {
                VerificationFilter.values().forEach { filter ->
                    val count = when (filter) {
                        VerificationFilter.ALL -> uiState.submissions.size
                        VerificationFilter.PENDING -> uiState.pendingCount
                        VerificationFilter.APPROVED -> uiState.approvedCount
                        VerificationFilter.REJECTED -> uiState.rejectedCount
                    }
                    Tab(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        text = { Text("${filter.label} ($count)") }
                    )
                }
            }
            
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Filter submissions based on selected tab
                val filteredSubmissions = when (selectedFilter) {
                    VerificationFilter.ALL -> uiState.submissions
                    VerificationFilter.PENDING -> uiState.submissions.filter { 
                        it.status == "pending" || it.overallStatus == VerificationStatus.PENDING 
                    }
                    VerificationFilter.APPROVED -> uiState.submissions.filter { 
                        it.status == "approved" || it.overallStatus == VerificationStatus.APPROVED 
                    }
                    VerificationFilter.REJECTED -> uiState.submissions.filter { 
                        it.status == "rejected" || it.overallStatus == VerificationStatus.REJECTED 
                    }
                }
                
                if (filteredSubmissions.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val emoji = when (selectedFilter) {
                            VerificationFilter.ALL -> "📋"
                            VerificationFilter.PENDING -> "⏳"
                            VerificationFilter.APPROVED -> "✅"
                            VerificationFilter.REJECTED -> "❌"
                        }
                        Text(emoji, fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No ${selectedFilter.label} Verifications",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = when (selectedFilter) {
                                VerificationFilter.ALL -> "No driver verifications submitted yet"
                                VerificationFilter.PENDING -> "All verifications have been processed"
                                VerificationFilter.APPROVED -> "No approved drivers yet"
                                VerificationFilter.REJECTED -> "No rejected submissions"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredSubmissions) { submission ->
                            VerificationCard(
                                submission = submission,
                                isProcessing = uiState.isLoading,
                                showActions = submission.status == "pending" || submission.overallStatus == VerificationStatus.PENDING,
                                onApprove = { viewModel.approveSubmission(submission.id) },
                                onReject = { reason -> viewModel.rejectSubmission(submission.id, reason) },
                                onDelete = { viewModel.deleteUserAndSubmission(submission.id, submission.userId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, count: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun VerificationCard(
    submission: VerificationSubmission,
    isProcessing: Boolean,
    showActions: Boolean = true,
    onApprove: () -> Unit,
    onReject: (String) -> Unit,
    onDelete: () -> Unit
) {
    var showRejectDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Vehicle type icon
                    Text(
                        text = if (submission.vehicleType == VehicleType.TAXI) "🚕" else "🚤",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = submission.userName.ifBlank { "Driver #${submission.userId.take(8)}" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${submission.vehicleType.name} Driver",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                
                // Status badge
                val statusColor = when (submission.overallStatus) {
                    VerificationStatus.PENDING -> Color(0xFFFF9800)
                    VerificationStatus.APPROVED -> Success
                    VerificationStatus.REJECTED -> Color(0xFFD32F2F)
                    else -> TextSecondary
                }
                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = submission.overallStatus.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Driver contact info
            if (!submission.userEmail.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, null, tint = Primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = submission.userEmail, style = MaterialTheme.typography.bodySmall, color = Primary)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            if (!submission.phoneNumber.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, null, tint = Success, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = submission.phoneNumber, style = MaterialTheme.typography.bodySmall, color = Success)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            Divider()
            Spacer(modifier = Modifier.height(12.dp))
            
            // Document thumbnails - tap to view full size
            Text(
                text = "📄 Documents (tap to view)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Show document images in a horizontal scroll
            val documentUrls = submission.documentUrls ?: submission.documents.map { it.documentUrl }
            if (documentUrls.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(documentUrls.size) { index ->
                        val url = documentUrls[index]
                        val docType = submission.documents.getOrNull(index)?.documentType
                        
                        Card(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable { selectedImageUrl = url },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = docType?.name ?: "Document $index",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                // Document type label
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth(),
                                    color = Color.Black.copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = docType?.name?.replace("_", " ") ?: "Doc ${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "No documents uploaded",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Submitted time
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Schedule,
                    null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Submitted: ${formatTimestamp(submission.submittedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            // Show rejection reason for rejected submissions
            if (submission.overallStatus == VerificationStatus.REJECTED && !submission.adminNotes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFD32F2F).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("❌", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Rejection Reason:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                            Text(
                                text = submission.adminNotes,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
            
            // Show reviewed time for processed submissions
            if (submission.reviewedAt != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = if (submission.overallStatus == VerificationStatus.APPROVED) Success else Color(0xFFD32F2F),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reviewed: ${formatTimestamp(submission.reviewedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            
            // Only show action buttons for pending
            if (showActions && submission.overallStatus == VerificationStatus.PENDING) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Reject button
                    OutlinedButton(
                        onClick = { showRejectDialog = true },
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFD32F2F)
                        )
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject")
                    }
                    
                    // Approve button
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Success
                        )
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve")
                        }
                    }
                }
            }
            
            // Delete button - always show for admin
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF9E9E9E)
                )
            ) {
                Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete User")
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.DeleteForever, null, tint = Color(0xFFD32F2F)) },
            title = { Text("Delete User?") },
            text = {
                Column {
                    Text(
                        "Are you sure you want to permanently delete this user?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "User: ${submission.userName}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Email: ${submission.userEmail}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "⚠️ This will delete:\n• User account\n• Verification documents\n• All bookings\n\nThis action cannot be undone!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD32F2F)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Reject dialog
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            icon = { Icon(Icons.Default.Warning, null, tint = Color(0xFFD32F2F)) },
            title = { Text("Reject Verification") },
            text = {
                Column {
                    Text("Please provide a reason for rejection:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        placeholder = { Text("e.g., Documents unclear, license expired...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReject(rejectReason.ifBlank { "Documents not accepted" })
                        showRejectDialog = false
                        rejectReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Full-screen image viewer dialog
    if (selectedImageUrl != null) {
        Dialog(onDismissRequest = { selectedImageUrl = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { selectedImageUrl = null }
            ) {
                AsyncImage(
                    model = selectedImageUrl,
                    contentDescription = "Document",
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    contentScale = ContentScale.Fit
                )
                // Close button
                IconButton(
                    onClick = { selectedImageUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return "Unknown"
    return try {
        val date = timestamp.toDate()
        java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(date)
    } catch (e: Exception) {
        "Unknown"
    }
}
