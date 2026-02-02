package com.boattaxie.app.ui.screens.profile

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.boattaxie.app.R
import com.boattaxie.app.data.model.*
import com.boattaxie.app.ui.components.*
import com.boattaxie.app.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToTripHistory: () -> Unit,
    onNavigateToPaymentMethods: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToAdminVerifications: (() -> Unit)? = null,
    onSignOut: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile)) },
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
                .verticalScroll(rememberScrollState())
        ) {
            // Profile header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(50),
                    color = Primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val photoUrl = uiState.user?.profilePhotoUrl
                        val isValidPhoto = photoUrl != null && (
                            photoUrl.startsWith("https://") || 
                            (photoUrl.startsWith("/") && File(photoUrl).exists())
                        )
                        if (isValidPhoto && photoUrl != null) {
                            AsyncImage(
                                model = if (photoUrl.startsWith("/")) File(photoUrl) else photoUrl,
                                contentDescription = "Profile photo",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(50)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                null,
                                tint = Primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = uiState.user?.fullName?.ifEmpty { stringResource(R.string.user) } ?: stringResource(R.string.user),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = uiState.user?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        null,
                        tint = Warning,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f", uiState.user?.rating ?: 5.0f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = " • ${uiState.user?.totalTrips ?: 0} ${stringResource(R.string.trips).lowercase()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(onClick = onNavigateToEditProfile) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.edit_profile))
                }
            }
            
            Divider()
            
            // Menu items
            ProfileMenuItem(
                icon = Icons.Default.History,
                title = stringResource(R.string.trip_history),
                subtitle = stringResource(R.string.view_past_rides),
                onClick = onNavigateToTripHistory
            )
            
            ProfileMenuItem(
                icon = Icons.Default.Payment,
                title = stringResource(R.string.payment_methods),
                subtitle = stringResource(R.string.manage_payment_options),
                onClick = onNavigateToPaymentMethods
            )
            
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            
            ProfileMenuItem(
                icon = Icons.Default.Settings,
                title = stringResource(R.string.settings),
                subtitle = stringResource(R.string.notifications_privacy_more),
                onClick = onNavigateToSettings
            )
            
            ProfileMenuItem(
                icon = Icons.Default.Help,
                title = stringResource(R.string.help_support),
                subtitle = stringResource(R.string.get_help_account),
                onClick = onNavigateToHelp
            )
            
            ProfileMenuItem(
                icon = Icons.Default.Info,
                title = stringResource(R.string.about_boattaxie),
                subtitle = stringResource(R.string.version_terms_policies),
                onClick = onNavigateToAbout
            )
            
            // Admin section - only show for admin email
            val isAdmin = uiState.user?.email == "jerimiah@lacunabotanicals.com"
            if (isAdmin && onNavigateToAdminVerifications != null) {
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                Text(
                    text = "Admin",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                ProfileMenuItem(
                    icon = Icons.Default.AdminPanelSettings,
                    title = "Driver Verifications",
                    subtitle = "Approve or reject driver signups",
                    onClick = onNavigateToAdminVerifications,
                    iconTint = Primary,
                    titleColor = Primary
                )
            }
            
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            
            ProfileMenuItem(
                icon = Icons.Default.Logout,
                title = stringResource(R.string.sign_out),
                subtitle = stringResource(R.string.sign_out_account),
                onClick = {
                    viewModel.signOut { onSignOut() }
                },
                iconTint = Error,
                titleColor = Error
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            var showDeleteDialog by remember { mutableStateOf(false) }
            
            ProfileMenuItem(
                icon = Icons.Default.DeleteForever,
                title = stringResource(R.string.delete_account),
                subtitle = stringResource(R.string.permanently_delete_account),
                onClick = { showDeleteDialog = true },
                iconTint = Error,
                titleColor = Error
            )
            
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text(stringResource(R.string.delete_account_question)) },
                    text = { Text(stringResource(R.string.delete_account_warning)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                viewModel.deleteAccount { onSignOut() }
                            }
                        ) {
                            Text(stringResource(R.string.delete), color = Error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconTint: androidx.compose.ui.graphics.Color = Primary,
    titleColor: androidx.compose.ui.graphics.Color = TextPrimary
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = titleColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = TextSecondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var fullName by remember { mutableStateOf(uiState.user?.fullName ?: "") }
    var phone by remember { mutableStateOf(uiState.user?.phoneNumber ?: "") }
    
    // Driver/Captain specific fields
    var licenseNumber by remember { mutableStateOf(uiState.user?.licenseNumber ?: "") }
    var vehiclePlate by remember { mutableStateOf(uiState.user?.vehiclePlate ?: "") }
    var vehicleModel by remember { mutableStateOf(uiState.user?.vehicleModel ?: "") }
    var vehicleColor by remember { mutableStateOf(uiState.user?.vehicleColor ?: "") }
    
    val isDriver = uiState.user?.userType in listOf(UserType.DRIVER, UserType.CAPTAIN)
    val isBoatCaptain = uiState.user?.userType == UserType.CAPTAIN
    
    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadProfilePhoto(it) }
    }
    
    LaunchedEffect(uiState.user) {
        uiState.user?.let {
            fullName = it.fullName
            phone = it.phoneNumber
            licenseNumber = it.licenseNumber ?: ""
            vehiclePlate = it.vehiclePlate ?: ""
            vehicleModel = it.vehicleModel ?: ""
            vehicleColor = it.vehicleColor ?: ""
        }
    }
    
    // Snackbar for showing errors/success
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_profile)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, stringResource(R.string.close))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.updateProfile(
                                fullName = fullName, 
                                phone = phone,
                                licenseNumber = if (isDriver) licenseNumber else null,
                                vehiclePlate = if (isDriver) vehiclePlate else null,
                                vehicleModel = if (isDriver) vehicleModel else null,
                                vehicleColor = if (isDriver) vehicleColor else null
                            )
                            onNavigateBack()
                        },
                        enabled = fullName.isNotBlank() && !uiState.isUploadingPhoto
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Profile photo
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val photoUrl = uiState.user?.profilePhotoUrl
                    val isValidPhoto = photoUrl != null && (
                        photoUrl.startsWith("https://") || 
                        (photoUrl.startsWith("/") && File(photoUrl).exists())
                    )
                    if (isValidPhoto && photoUrl != null) {
                        AsyncImage(
                            model = if (photoUrl.startsWith("/")) File(photoUrl) else photoUrl,
                            contentDescription = "Profile photo",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(50)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = RoundedCornerShape(50),
                            color = Primary.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    null,
                                    tint = Primary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                    
                    // Uploading indicator
                    if (uiState.isUploadingPhoto) {
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = RoundedCornerShape(50),
                            color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    color = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    enabled = !uiState.isUploadingPhoto
                ) {
                    Icon(Icons.Default.CameraAlt, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (uiState.isUploadingPhoto) stringResource(R.string.uploading) else stringResource(R.string.change_photo))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Personal Information Section
            Text(
                text = stringResource(R.string.personal_information),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text(stringResource(R.string.full_name)) },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = uiState.user?.email ?: "",
                onValueChange = { },
                label = { Text(stringResource(R.string.email)) },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = false
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(stringResource(R.string.phone_contact_riders)) },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                placeholder = { Text("+507 6XXX-XXXX") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Driver/Captain specific fields
            if (isDriver) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Divider()
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // License Section
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isBoatCaptain) Icons.Default.Sailing else Icons.Default.LocalTaxi,
                        null,
                        tint = Primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBoatCaptain) stringResource(R.string.boat_captain_license) else stringResource(R.string.taxi_driver_license),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = stringResource(R.string.info_shown_riders),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = licenseNumber,
                    onValueChange = { licenseNumber = it },
                    label = { Text(if (isBoatCaptain) stringResource(R.string.boat_captain_license_number) else stringResource(R.string.taxi_license_number)) },
                    leadingIcon = { Icon(Icons.Default.Badge, null) },
                    placeholder = { Text(stringResource(R.string.license_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Vehicle Section
                Text(
                    text = if (isBoatCaptain) stringResource(R.string.boat_information) else stringResource(R.string.vehicle_information),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = vehicleModel,
                    onValueChange = { vehicleModel = it },
                    label = { Text(if (isBoatCaptain) stringResource(R.string.boat_model_type) else stringResource(R.string.vehicle_model)) },
                    leadingIcon = { 
                        Text(
                            if (isBoatCaptain) "🚤" else "🚕",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    placeholder = { Text(if (isBoatCaptain) stringResource(R.string.boat_model_placeholder) else stringResource(R.string.vehicle_model_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = vehicleColor,
                    onValueChange = { vehicleColor = it },
                    label = { Text(if (isBoatCaptain) stringResource(R.string.boat_color) else stringResource(R.string.vehicle_color)) },
                    leadingIcon = { Icon(Icons.Default.ColorLens, null) },
                    placeholder = { Text(stringResource(R.string.color_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = vehiclePlate,
                    onValueChange = { vehiclePlate = it },
                    label = { Text(if (isBoatCaptain) stringResource(R.string.boat_registration) else stringResource(R.string.license_plate)) },
                    leadingIcon = { Icon(Icons.Default.DirectionsCar, null) },
                    placeholder = { Text(if (isBoatCaptain) stringResource(R.string.boat_reg_placeholder) else stringResource(R.string.plate_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Info card
                Surface(
                    color = Info.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            null,
                            tint = Info,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.rider_info_message, if (isBoatCaptain) stringResource(R.string.boat_word) else stringResource(R.string.vehicle_word)),
                            style = MaterialTheme.typography.bodySmall,
                            color = Info
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripHistoryScreen(
    onNavigateBack: () -> Unit,
    onRequestDriver: (driverId: String, driverName: String, vehicleType: VehicleType) -> Unit = { _, _, _ -> },
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadTripHistory()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.trip_history)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.tripHistory.isEmpty() && !uiState.isLoading) {
            EmptyState(
                icon = Icons.Default.History,
                title = stringResource(R.string.no_trips_yet),
                message = stringResource(R.string.trip_history_empty),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.tripHistory) { trip ->
                    TripHistoryCard(
                        trip = trip,
                        onRequestDriver = {
                            trip.driverId?.let { driverId ->
                                onRequestDriver(
                                    driverId,
                                    trip.driverName ?: "Driver",
                                    trip.vehicleType
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TripHistoryCard(
    trip: Booking,
    onRequestDriver: () -> Unit = {}
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (trip.vehicleType == VehicleType.BOAT) Icons.Default.DirectionsBoat
                        else Icons.Default.LocalTaxi,
                        null,
                        tint = if (trip.vehicleType == VehicleType.BOAT) Color(0xFF2196F3) else Color(0xFFFFEB3B)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (trip.vehicleType == VehicleType.BOAT) stringResource(R.string.boat_ride) else stringResource(R.string.taxi_ride),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "$${String.format("%.2f", trip.finalFare ?: trip.estimatedFare)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = trip.requestedAt.toDate().toString().take(10),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            
            // Driver info section
            if (trip.driverId != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Driver photo
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(50),
                        color = Primary.copy(alpha = 0.1f)
                    ) {
                        if (!trip.driverPhotoUrl.isNullOrBlank()) {
                            val context = LocalContext.current
                            val imageModel = remember(trip.driverPhotoUrl) {
                                val imageData = if (trip.driverPhotoUrl!!.startsWith("/")) {
                                    java.io.File(trip.driverPhotoUrl)
                                } else {
                                    trip.driverPhotoUrl
                                }
                                ImageRequest.Builder(context)
                                    .data(imageData)
                                    .crossfade(true)
                                    .allowHardware(false)
                                    .build()
                            }
                            AsyncImage(
                                model = imageModel,
                                contentDescription = "Driver photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(50)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    Icons.Default.Person,
                                    null,
                                    tint = Primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = trip.driverName ?: stringResource(R.string.driver),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (trip.driverRatingValue != null) {
                                Icon(
                                    Icons.Default.Star,
                                    null,
                                    tint = Warning,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = String.format("%.1f", trip.driverRatingValue),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (trip.vehiclePlate != null) {
                                Text(
                                    text = trip.vehiclePlate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                    
                    // Request Again button
                    Button(
                        onClick = onRequestDriver,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            if (trip.vehicleType == VehicleType.BOAT) Icons.Default.DirectionsBoat else Icons.Default.LocalTaxi,
                            null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.request), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.Top) {
                Column {
                    Icon(
                        Icons.Default.MyLocation,
                        null,
                        tint = Success,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(20.dp)
                            .padding(start = 7.dp)
                    )
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        tint = Error,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = trip.pickupLocation.address ?: stringResource(R.string.pickup),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = trip.destinationLocation.address ?: stringResource(R.string.dropoff),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddCard by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.payment_methods)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddCard = true }) {
                Icon(Icons.Default.Add, stringResource(R.string.add_payment_method))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Default payment methods
            Text(
                text = stringResource(R.string.saved_cards),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (uiState.paymentMethods.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CreditCard,
                            null,
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.no_payment_methods),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = { showAddCard = true }) {
                            Text(stringResource(R.string.add_card))
                        }
                    }
                }
            } else {
                uiState.paymentMethods.forEach { method ->
                    PaymentMethodCard(
                        method = method,
                        onDelete = { viewModel.deletePaymentMethod(method.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
    
    if (showAddCard) {
        AddCardDialog(
            onDismiss = { showAddCard = false },
            onAdd = { cardNumber, expiry, cvv ->
                viewModel.addPaymentMethod(cardNumber, expiry, cvv)
                showAddCard = false
            }
        )
    }
}

@Composable
private fun PaymentMethodCard(
    method: PaymentMethod,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CreditCard,
                null,
                tint = Primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "•••• •••• •••• ${method.lastFour}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${stringResource(R.string.expires)} ${method.expiryMonth}/${method.expiryYear}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            if (method.isDefault) {
                Surface(
                    color = Primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.default_label),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, stringResource(R.string.delete), tint = Error)
            }
        }
    }
}

@Composable
private fun AddCardDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_card)) },
        text = {
            Column {
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it.take(16) },
                    label = { Text(stringResource(R.string.card_number)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    OutlinedTextField(
                        value = expiry,
                        onValueChange = { expiry = it.take(5) },
                        label = { Text("MM/YY") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { cvv = it.take(4) },
                        label = { Text("CVV") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(cardNumber, expiry, cvv) },
                enabled = cardNumber.length >= 15 && expiry.length >= 4 && cvv.length >= 3
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

data class PaymentMethod(
    val id: String,
    val lastFour: String,
    val brand: String,
    val expiryMonth: String,
    val expiryYear: String,
    val isDefault: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
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
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.notifications),
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            SettingsToggleItem(
                title = stringResource(R.string.push_notifications),
                subtitle = stringResource(R.string.push_notifications_desc),
                checked = uiState.pushNotificationsEnabled,
                onCheckedChange = { viewModel.togglePushNotifications() }
            )
            
            SettingsToggleItem(
                title = stringResource(R.string.email_notifications),
                subtitle = stringResource(R.string.email_notifications_desc),
                checked = uiState.emailNotificationsEnabled,
                onCheckedChange = { viewModel.toggleEmailNotifications() }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                text = stringResource(R.string.privacy),
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            SettingsToggleItem(
                title = stringResource(R.string.share_location),
                subtitle = stringResource(R.string.share_location_desc),
                checked = uiState.locationSharingEnabled,
                onCheckedChange = { viewModel.toggleLocationSharing() }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                text = stringResource(R.string.account),
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            SettingsItem(
                title = stringResource(R.string.change_password),
                onClick = { /* Navigate to change password */ }
            )
            
            SettingsItem(
                title = stringResource(R.string.delete_account),
                titleColor = Error,
                onClick = { /* Show delete confirmation */ }
            )
        }
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsItem(
    title: String,
    titleColor: androidx.compose.ui.graphics.Color = TextPrimary,
    onClick: () -> Unit
) {
    Surface(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor
            )
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = TextSecondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showFAQ by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (showFAQ) stringResource(R.string.faqs) else stringResource(R.string.help_support)) },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (showFAQ) showFAQ = false else onNavigateBack() 
                    }) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        if (showFAQ) {
            // FAQ Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    FAQSection(
                        title = stringResource(R.string.getting_started),
                        items = listOf(
                            FAQItem(
                                question = stringResource(R.string.faq_how_book_q),
                                answer = stringResource(R.string.faq_how_book_a)
                            ),
                            FAQItem(
                                question = stringResource(R.string.faq_switch_vehicle_q),
                                answer = stringResource(R.string.faq_switch_vehicle_a)
                            ),
                            FAQItem(
                                question = stringResource(R.string.faq_see_drivers_q),
                                answer = stringResource(R.string.faq_see_drivers_a)
                            )
                        )
                    )
                }
                
                item {
                    FAQSection(
                        title = stringResource(R.string.for_drivers_captains),
                        items = listOf(
                            FAQItem(
                                question = stringResource(R.string.faq_become_driver_q),
                                answer = stringResource(R.string.faq_become_driver_a)
                            ),
                            FAQItem(
                                question = stringResource(R.string.faq_go_online_q),
                                answer = stringResource(R.string.faq_go_online_a)
                            ),
                            FAQItem(
                                question = stringResource(R.string.faq_both_vehicles_q),
                                answer = stringResource(R.string.faq_both_vehicles_a)
                            ),
                            FAQItem(
                                question = stringResource(R.string.faq_contact_rider_q),
                                answer = stringResource(R.string.faq_contact_rider_a)
                            )
                        )
                    )
                }
                
                item {
                    FAQSection(
                        title = stringResource(R.string.during_ride),
                        items = listOf(
                            FAQItem(
                                question = stringResource(R.string.faq_how_ride_works_q),
                                answer = stringResource(R.string.faq_how_ride_works_a)
                            ),
                            FAQItem(
                                question = stringResource(R.string.faq_contact_driver_q),
                                answer = stringResource(R.string.faq_contact_driver_a)
                            ),
                            FAQItem(
                                question = stringResource(R.string.faq_driver_not_showing_q),
                                answer = stringResource(R.string.faq_driver_not_showing_a)
                            )
                        )
                    )
                }
                
                item {
                    FAQSection(
                        title = stringResource(R.string.payments_pricing),
                        items = listOf(
                            FAQItem(
                                question = stringResource(R.string.faq_fare_calculated_q),
                                answer = stringResource(R.string.faq_fare_calculated_a)
                            ),
                            FAQItem(
                                question = stringResource(R.string.faq_how_pay_q),
                                answer = stringResource(R.string.faq_how_pay_a)
                            ),
                            FAQItem(
                                question = stringResource(R.string.faq_tip_q),
                                answer = stringResource(R.string.faq_tip_a)
                            )
                        )
                    )
                }
                
                item {
                    FAQSection(
                        title = stringResource(R.string.business_advertising),
                        items = listOf(
                            FAQItem(
                                question = stringResource(R.string.faq_advertise_q),
                                answer = stringResource(R.string.faq_advertise_a)
                            ),
                            FAQItem(
                                question = stringResource(R.string.faq_coupons_q),
                                answer = stringResource(R.string.faq_coupons_a)
                            )
                        )
                    )
                }
                
                item {
                    FAQSection(
                        title = stringResource(R.string.account_safety),
                        items = listOf(
                            FAQItem(
                                question = stringResource(R.string.faq_update_profile_q),
                                answer = stringResource(R.string.faq_update_profile_a)
                            ),
                            FAQItem(
                                question = stringResource(R.string.faq_info_safe_q),
                                answer = stringResource(R.string.faq_info_safe_a)
                            ),
                            FAQItem(
                                question = stringResource(R.string.faq_safety_concern_q),
                                answer = stringResource(R.string.faq_safety_concern_a)
                            )
                        )
                    )
                }
            }
        } else {
            // Main Help Menu
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                HelpItem(
                    icon = Icons.Default.QuestionAnswer,
                    title = stringResource(R.string.faqs),
                    subtitle = stringResource(R.string.find_answers),
                    onClick = { showFAQ = true }
                )
                
                HelpItem(
                    icon = Icons.Default.Chat,
                    title = stringResource(R.string.chat_support),
                    subtitle = stringResource(R.string.whatsapp_number),
                    onClick = {
                        // Open WhatsApp chat
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://wa.me/50766686294")
                        }
                        context.startActivity(intent)
                    }
                )
                
                HelpItem(
                    icon = Icons.Default.Email,
                    title = stringResource(R.string.email_support),
                    subtitle = stringResource(R.string.email_address),
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:jerimiah@lacunabotanicals.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Boat Taxie Support Request")
                        }
                        context.startActivity(intent)
                    }
                )
                
                HelpItem(
                    icon = Icons.Default.Phone,
                    title = stringResource(R.string.call_support),
                    subtitle = stringResource(R.string.phone_support),
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:+50766686294")
                        }
                        context.startActivity(intent)
                    }
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                HelpItem(
                    icon = Icons.Default.Report,
                    title = stringResource(R.string.report_safety),
                    subtitle = stringResource(R.string.report_safety_desc),
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:jerimiah@lacunabotanicals.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Boat Taxie Safety Report")
                            putExtra(Intent.EXTRA_TEXT, "Please describe the safety issue:\n\n")
                        }
                        context.startActivity(intent)
                    }
                )
                
                HelpItem(
                    icon = Icons.Default.Feedback,
                    title = stringResource(R.string.send_feedback),
                    subtitle = stringResource(R.string.help_improve),
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:jerimiah@lacunabotanicals.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Boat Taxie App Feedback")
                            putExtra(Intent.EXTRA_TEXT, "I'd like to share some feedback:\n\n")
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

// FAQ data class
data class FAQItem(
    val question: String,
    val answer: String
)

@Composable
private fun FAQSection(
    title: String,
    items: List<FAQItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            items.forEachIndexed { index, item ->
                var expanded by remember { mutableStateOf(false) }
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.question,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = Primary
                        )
                    }
                    if (expanded) {
                        Text(
                            text = item.answer,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                        )
                    }
                    if (index < items.size - 1) {
                        Divider(color = Divider.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HelpItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Surface(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = TextSecondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about)) },
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // App logo
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(24.dp),
                color = Primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.DirectionsBoat,
                        null,
                        tint = TextOnPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = stringResource(R.string.version),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = stringResource(R.string.tagline),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Divider()
            
            // Terms of Service
            var showTermsDialog by remember { mutableStateOf(false) }
            var showPrivacyDialog by remember { mutableStateOf(false) }
            
            AboutItem(
                title = stringResource(R.string.terms_of_service),
                onClick = { showTermsDialog = true }
            )
            AboutItem(
                title = stringResource(R.string.privacy_policy),
                onClick = { showPrivacyDialog = true }
            )
            AboutItem(
                title = stringResource(R.string.open_source_licenses),
                onClick = { }
            )
            
            // Terms of Service Dialog
            if (showTermsDialog) {
                AlertDialog(
                    onDismissRequest = { showTermsDialog = false },
                    title = {
                        Text(
                            text = "📜 Terms & Conditions",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = """
BOATTAXIE TERMS OF SERVICE

Last Updated: January 2026

1. ACCEPTANCE OF TERMS
By downloading, installing, or using the BoatTaxie application, you agree to be bound by these Terms of Service. If you do not agree, do not use the app.

2. SERVICE DESCRIPTION
BoatTaxie provides a platform connecting passengers with boat and taxi operators in Bocas del Toro, Panama. We are a technology platform and do not provide transportation services directly.

3. USER ACCOUNTS
• You must be 18+ to create an account
• You are responsible for maintaining account security
• Provide accurate, current information
• One account per person

4. SUBSCRIPTION SERVICES
• Subscriptions are required to book rides
• Prices are displayed in USD
• Subscriptions auto-renew unless cancelled
• No refunds for partial periods

5. BOOKING & RIDES
• Fares are estimates and may vary
• Drivers/Captains are independent operators
• We do not guarantee availability
• Weather may affect boat services

6. PAYMENT
• Payment is processed securely via PayPal
• You authorize charges for services used
• Drivers may adjust fares with your approval
• Tips are optional but appreciated

7. CONDUCT
You agree NOT to:
• Use the service for illegal purposes
• Harass drivers or other users
• Damage vehicles or boats
• Carry prohibited items

8. ADVERTISING
• Businesses can advertise on our platform
• Ad pricing is clearly displayed
• We reserve the right to reject ads
• Advertisers are responsible for content accuracy

9. LIMITATION OF LIABILITY
BoatTaxie is not liable for:
• Actions of drivers/captains
• Personal injury or property damage
• Weather-related cancellations
• Third-party service failures

10. CONTACT
Email: support@boattaxie.com
Location: Bocas del Toro, Panama

By using BoatTaxie, you acknowledge reading and accepting these terms.
                                """.trimIndent(),
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showTermsDialog = false }) {
                            Text("I Accept")
                        }
                    }
                )
            }
            
            // Privacy Policy Dialog
            if (showPrivacyDialog) {
                AlertDialog(
                    onDismissRequest = { showPrivacyDialog = false },
                    title = {
                        Text(
                            text = "🔒 Privacy Policy",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = """
BOATTAXIE PRIVACY POLICY

Last Updated: January 2026

1. INFORMATION WE COLLECT

Personal Information:
• Name, email, phone number
• Profile photo (optional)
• Payment information (via PayPal)
• Driver license info (for drivers)

Location Data:
• GPS location for ride matching
• Pickup and dropoff locations
• Route tracking during rides

Usage Data:
• App usage patterns
• Search history
• Booking history

2. HOW WE USE YOUR DATA

• To provide transportation services
• To process payments
• To improve our services
• To send important notifications
• To display relevant ads
• To ensure safety and security

3. DATA SHARING

We share data with:
• Drivers (to complete rides)
• Payment processors (PayPal)
• Analytics providers (anonymized)
• Law enforcement (when required)

We DO NOT sell your personal data.

4. DATA STORAGE

• Data is stored on Firebase (Google Cloud)
• We use industry-standard encryption
• Servers located in secure facilities

5. YOUR RIGHTS

You can:
• Access your personal data
• Request data correction
• Delete your account
• Opt out of marketing emails
• Export your data

6. COOKIES & TRACKING

We use minimal tracking for:
• App functionality
• Performance monitoring
• Crash reporting

7. CHILDREN'S PRIVACY

BoatTaxie is not for users under 18. We do not knowingly collect data from minors.

8. DATA RETENTION

• Active accounts: Data retained
• Deleted accounts: 30 days retention
• Ride history: 3 years for legal purposes

9. SECURITY

We implement:
• SSL/TLS encryption
• Secure authentication
• Regular security audits
• Access controls

10. CHANGES

We may update this policy. Continued use after changes means acceptance.

11. CONTACT

Privacy questions:
Email: privacy@boattaxie.com

Data requests:
Email: data@boattaxie.com

Location: Bocas del Toro, Panama
                                """.trimIndent(),
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showPrivacyDialog = false }) {
                            Text("I Understand")
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = stringResource(R.string.copyright),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutItem(
    title: String,
    onClick: () -> Unit = {}
) {
    Surface(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = TextSecondary
            )
        }
    }
}
