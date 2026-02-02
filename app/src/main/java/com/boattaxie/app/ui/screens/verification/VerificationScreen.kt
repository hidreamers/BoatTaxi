package com.boattaxie.app.ui.screens.verification

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.boattaxie.app.data.model.*
import com.boattaxie.app.ui.components.PrimaryButton
import com.boattaxie.app.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreen(
    vehicleType: String,
    onNavigateToVerificationStatus: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: VerificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var selectedDocumentType by remember { mutableStateOf<DocumentType?>(null) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var phoneNumber by remember { mutableStateOf("") }
    
    // Initialize vehicle type
    LaunchedEffect(vehicleType) {
        viewModel.setVehicleType(vehicleType)
    }
    
    // Navigate on successful submission
    LaunchedEffect(uiState.verificationStatus) {
        if (uiState.verificationStatus in listOf(VerificationStatus.PENDING, VerificationStatus.APPROVED)) {
            onNavigateToVerificationStatus()
        }
    }
    
    // Launch email when intent is available
    LaunchedEffect(uiState.emailIntent) {
        uiState.emailIntent?.let { intent ->
            try {
                context.startActivity(android.content.Intent.createChooser(intent, "Send verification documents via email"))
            } catch (e: Exception) {
                android.util.Log.e("VerificationScreen", "Failed to open email app: ${e.message}")
            }
            viewModel.clearEmailIntent()
        }
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null && selectedDocumentType != null) {
            viewModel.uploadDocument(selectedDocumentType!!, tempImageUri!!)
        }
    }
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && selectedDocumentType != null) {
            viewModel.uploadDocument(selectedDocumentType!!, uri)
        }
    }
    
    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && tempImageUri != null) {
            cameraLauncher.launch(tempImageUri!!)
        }
    }
    
    fun createTempImageUri(): Uri {
        val tempFile = File.createTempFile("verification_", ".jpg", context.cacheDir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (vehicleType == "taxi") "Taxi Verification" 
                        else "Boat Verification"
                    ) 
                },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Upload Your Documents",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Please upload clear photos of the following documents to verify your ${if (vehicleType == "taxi") "taxi" else "boat"}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Free badge
                Surface(
                    color = Success.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            "Free",
                            tint = Success,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Verification is FREE for all captains and drivers",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Success,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Document upload cards
            val requiredDocs = viewModel.getRequiredDocuments()
            items(requiredDocs) { documentType ->
                val document = uiState.documents[documentType]
                val isUploading = uiState.uploadingDocument == documentType
                
                DocumentUploadCard(
                    documentType = documentType,
                    vehicleType = uiState.vehicleType,
                    document = document,
                    isUploading = isUploading,
                    onUploadClick = {
                        selectedDocumentType = documentType
                        showImageSourceDialog = true
                    },
                    onDeleteClick = {
                        viewModel.deleteDocument(documentType)
                    }
                )
            }
            
            // Error message
            if (uiState.errorMessage != null) {
                item {
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
                            Icon(
                                Icons.Default.Error,
                                "Error",
                                tint = Error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Error
                            )
                        }
                    }
                }
            }
            
            // Submit button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Phone number field for contact
                Text(
                    text = "Contact Phone Number",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "We'll use this to notify you about your verification status",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("+507 6XXX-XXXX") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                PrimaryButton(
                    text = "Submit for Verification",
                    onClick = { viewModel.submitVerification(phoneNumber.ifBlank { null }) },
                    enabled = uiState.canSubmit && !uiState.isLoading,
                    isLoading = uiState.isLoading
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // Image source dialog
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Upload Document") },
            text = { Text("Choose how you want to upload your document") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImageSourceDialog = false
                        tempImageUri = createTempImageUri()
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Icon(Icons.Default.CameraAlt, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Take Photo")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImageSourceDialog = false
                        galleryLauncher.launch("image/*")
                    }
                ) {
                    Icon(Icons.Default.PhotoLibrary, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery")
                }
            }
        )
    }
}

@Composable
private fun DocumentUploadCard(
    documentType: DocumentType,
    vehicleType: VehicleType,
    document: VerificationDocument?,
    isUploading: Boolean,
    onUploadClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val title = VerificationRequirements.getDocumentTitle(documentType)
    val description = VerificationRequirements.getDocumentDescription(documentType, vehicleType)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (document != null) Success.copy(alpha = 0.05f) else Surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (document != null) Success else Divider
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (documentType) {
                            DocumentType.VEHICLE_PHOTO -> Icons.Default.DirectionsBoat
                            DocumentType.REGISTRATION -> Icons.Default.Description
                            DocumentType.LICENSE -> Icons.Default.Badge
                            DocumentType.INSURANCE -> Icons.Default.HealthAndSafety
                            else -> Icons.Default.InsertDriveFile
                        },
                        contentDescription = null,
                        tint = if (document != null) Success else TextSecondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                
                if (document != null) {
                    Icon(
                        Icons.Default.CheckCircle,
                        "Uploaded",
                        tint = Success,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (document != null) {
                // Show uploaded image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = document.documentUrl,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Delete button
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(32.dp)
                            .background(
                                color = Error.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            "Delete",
                            tint = TextOnPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                // Upload button
                OutlinedButton(
                    onClick = onUploadClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Uploading...")
                    } else {
                        Icon(Icons.Default.CloudUpload, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload Document")
                    }
                }
            }
        }
    }
}
