package com.boattaxie.app.ui.screens.driver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.boattaxie.app.R
import com.boattaxie.app.data.model.*
import com.boattaxie.app.ui.components.*
import com.boattaxie.app.ui.theme.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreen(
    onNavigateToActiveRide: (String) -> Unit,
    onNavigateToEarnings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToRiderMode: () -> Unit = {},
    viewModel: DriverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            uiState.currentLocation ?: LatLng(9.4073, -82.3421), // Default to Bocas del Toro, Panama
            14f
        )
    }
    
    // Navigate to active ride ONLY when driver explicitly accepts a new booking
    LaunchedEffect(uiState.acceptedBookingId) {
        uiState.acceptedBookingId?.let { bookingId ->
            onNavigateToActiveRide(bookingId)
            viewModel.clearAcceptedBooking()
        }
    }
    
    // Track if we've shown the ride accepted notification
    var notifiedRideAccepted by remember { mutableStateOf<String?>(null) }
    
    // Detect when rider accepts our fare proposal (activeRide becomes non-null)
    // This also plays a notification sound and navigates to the ride
    LaunchedEffect(uiState.activeRide?.id) {
        uiState.activeRide?.let { ride ->
            // Only notify if we haven't already for this ride
            if (ride.id != notifiedRideAccepted && ride.status == BookingStatus.ACCEPTED) {
                android.util.Log.d("DriverScreen", "Rider accepted! Ride ID: ${ride.id}")
                notifiedRideAccepted = ride.id
                
                // Play success notification sound
                try {
                    val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val mediaPlayer = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .build()
                        )
                        setDataSource(context, notificationUri)
                        prepare()
                        start()
                    }
                    mediaPlayer.setOnCompletionListener { mp -> mp.release() }
                    
                    // Vibrate to alert driver
                    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                        vibratorManager.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
                    }
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                } catch (e: Exception) {
                    android.util.Log.e("DriverScreen", "Error playing ride accepted notification: ${e.message}")
                }
                
                // Navigate to active ride
                onNavigateToActiveRide(ride.id)
            }
        }
    }
    
    // Refresh active ride when screen becomes visible (on tab switch)
    LaunchedEffect(Unit) {
        viewModel.refreshActiveRide()
    }
    
    // Track previous request count to detect new requests
    var previousRequestCount by remember { mutableStateOf(0) }
    
    // Play sound and vibrate when new ride request comes in
    LaunchedEffect(uiState.allPendingRequests.size) {
        val currentCount = uiState.allPendingRequests.size
        // Only play sound if there are new requests (count increased) and driver is online
        if (currentCount > previousRequestCount && currentCount > 0 && uiState.isOnline) {
            try {
                // Play notification sound
                val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .build()
                    )
                    setDataSource(context, notificationUri)
                    prepare()
                    start()
                }
                
                // Release after playing
                mediaPlayer.setOnCompletionListener { mp ->
                    mp.release()
                }
                
                // Vibrate to alert driver
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Vibrate pattern: wait 0ms, vibrate 500ms, wait 200ms, vibrate 500ms
                    val pattern = longArrayOf(0, 500, 200, 500)
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 500, 200, 500), -1)
                }
                
                android.util.Log.d("DriverScreen", "Playing ride request notification sound for ${currentCount - previousRequestCount} new requests")
            } catch (e: Exception) {
                android.util.Log.e("DriverScreen", "Error playing notification sound: ${e.message}")
            }
        }
        previousRequestCount = currentCount
    }
    
    // State for summary popup
    var showSummary by remember { mutableStateOf(false) }
    
    // State for vehicle type switch dialog
    var showVehicleSwitchDialog by remember { mutableStateOf(false) }
    
    // State for pending requests dropdown
    var showPendingRequestsDropdown by remember { mutableStateOf(false) }
    
    // State for selected request to show on map (not as full popup)
    var selectedRequestForMap by remember { mutableStateOf<Booking?>(null) }
    
    // Keep selectedRequestForMap in sync with allPendingRequests
    // This updates the local state when Firebase data changes (e.g., after proposeFareChange)
    LaunchedEffect(uiState.allPendingRequests) {
        selectedRequestForMap?.let { selected ->
            val updatedBooking = uiState.allPendingRequests.find { it.id == selected.id }
            if (updatedBooking != null && updatedBooking != selected) {
                android.util.Log.d("DriverScreen", "Updating selectedRequestForMap: driverAdjustedFare=${updatedBooking.driverAdjustedFare}")
                selectedRequestForMap = updatedBooking
            } else if (updatedBooking == null) {
                // Booking was removed (e.g., cancelled or accepted)
                selectedRequestForMap = null
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Hello, ${uiState.driverName}!",
                                style = MaterialTheme.typography.titleMedium
                            )
                            // Show vehicle type badge
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = if (uiState.vehicleType == VehicleType.BOAT) Primary.copy(alpha = 0.2f) else Warning.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (uiState.vehicleType == VehicleType.BOAT) "🚤" else "🚕",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            if (uiState.isOnline) "You're online" else "You're offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.isOnline) Success else TextSecondary
                        )
                    }
                },
                actions = {
                    // Live users badge
                    LiveUsersBadge(compact = true, showDriverCount = true)
                    
                    // Show vehicle switcher button if driver has both vehicles
                    if (uiState.hasBoat && uiState.hasTaxi) {
                        Surface(
                            onClick = { showVehicleSwitchDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            color = Primary.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (uiState.vehicleType == VehicleType.BOAT) Icons.Default.DirectionsBoat else Icons.Default.LocalTaxi,
                                    "Switch Vehicle",
                                    tint = Primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (uiState.vehicleType == VehicleType.BOAT) "Boat" else "Taxi",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Primary,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    Icons.Default.SwapHoriz,
                                    "Switch",
                                    tint = Primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, "Profile")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "Rider Mode") },
                    label = { Text("Rider Mode") },
                    selected = false,
                    onClick = onNavigateToRiderMode
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DirectionsBoat, contentDescription = "Driver") },
                    label = { Text("Driver") },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Summary") },
                    label = { Text("Summary") },
                    selected = showSummary,
                    onClick = { showSummary = !showSummary }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Payments, contentDescription = "Earnings") },
                    label = { Text("Earnings") },
                    selected = false,
                    onClick = onNavigateToEarnings
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Map background
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
            ) {
                // Show pending ride requests as markers on the map
                if (uiState.showRequestsOnMap) {
                    uiState.allPendingRequests.forEach { booking ->
                        val pickupLoc = LatLng(booking.pickupLocation.latitude, booking.pickupLocation.longitude)
                        val dropoffLoc = LatLng(booking.destinationLocation.latitude, booking.destinationLocation.longitude)
                        val isSelected = selectedRequestForMap?.id == booking.id
                        
                        // Pickup marker
                        MarkerComposable(
                            state = rememberMarkerState(position = pickupLoc),
                            onClick = {
                                selectedRequestForMap = if (isSelected) null else booking
                                true
                            }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    color = if (isSelected) Success else (if (booking.vehicleType == VehicleType.BOAT) Primary else Warning),
                                    shape = RoundedCornerShape(8.dp),
                                    shadowElevation = if (isSelected) 8.dp else 4.dp
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "📍 PICKUP",
                                            color = Color.White.copy(alpha = 0.9f),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        val pickupText = if (booking.pickupAddress.isNotEmpty()) {
                                            booking.pickupAddress.take(15) + if (booking.pickupAddress.length > 15) "..." else ""
                                        } else {
                                            String.format("%.4f, %.4f", booking.pickupLocation.latitude, booking.pickupLocation.longitude)
                                        }
                                        Text(
                                            text = pickupText,
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (booking.vehicleType == VehicleType.BOAT) "🚤 Boat" else "🚕 Taxi",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Icon(
                                    Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = if (isSelected) Success else (if (booking.vehicleType == VehicleType.BOAT) Primary else Warning),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        // Show dropoff marker when this request is selected
                        if (isSelected) {
                            MarkerComposable(
                                state = rememberMarkerState(position = dropoffLoc),
                                onClick = { true }
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        color = Error,
                                        shape = RoundedCornerShape(8.dp),
                                        shadowElevation = 8.dp
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "DROPOFF",
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            val dropoffText = if (booking.destinationAddress.isNotEmpty()) {
                                                booking.destinationAddress.take(15) + if (booking.destinationAddress.length > 15) "..." else ""
                                            } else {
                                                "📍 ${String.format("%.3f", booking.destinationLocation.latitude)}"
                                            }
                                            Text(
                                                text = dropoffText,
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    Icon(
                                        Icons.Default.Flag,
                                        contentDescription = null,
                                        tint = Error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            // Draw line between pickup and dropoff
                            Polyline(
                                points = listOf(pickupLoc, dropoffLoc),
                                color = Primary,
                                width = 8f
                            )
                        }
                    }
                }
                
                // ALWAYS show selected request markers even if showRequestsOnMap is false
                if (!uiState.showRequestsOnMap) {
                    selectedRequestForMap?.let { booking ->
                        val pickupLoc = LatLng(booking.pickupLocation.latitude, booking.pickupLocation.longitude)
                        val dropoffLoc = LatLng(booking.destinationLocation.latitude, booking.destinationLocation.longitude)
                        
                        // Pickup marker (green)
                        MarkerComposable(
                            state = rememberMarkerState(position = pickupLoc),
                            onClick = { true }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    color = Success,
                                    shape = RoundedCornerShape(8.dp),
                                    shadowElevation = 8.dp
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "📍 PICKUP",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = booking.pickupAddress.take(20) + if (booking.pickupAddress.length > 20) "..." else "",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1
                                        )
                                        // No fare shown - driver will set their price
                                    }
                                }
                                Icon(
                                    Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = Success,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        // Dropoff marker (red)
                        MarkerComposable(
                            state = rememberMarkerState(position = dropoffLoc),
                            onClick = { true }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    color = Error,
                                    shape = RoundedCornerShape(8.dp),
                                    shadowElevation = 8.dp
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "🏁 DROPOFF",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = booking.destinationAddress.take(20) + if (booking.destinationAddress.length > 20) "..." else "",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1
                                        )
                                    }
                                }
                                Icon(
                                    Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = Error,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        // Route line
                        Polyline(
                            points = listOf(pickupLoc, dropoffLoc),
                            color = Primary,
                            width = 8f
                        )
                    }
                }
            }
            
            // Pending Ride Requests Dropdown at the top
            if (uiState.allPendingRequests.isNotEmpty() && uiState.isOnline) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 70.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                ) {
                    // Dropdown header button
                    Surface(
                        onClick = { showPendingRequestsDropdown = !showPendingRequestsDropdown },
                        color = Primary,
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Notifications,
                                    "Requests",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.pending_ride_requests),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "${uiState.allPendingRequests.size}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    if (showPendingRequestsDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    "Toggle",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    
                    // Dropdown list
                    AnimatedVisibility(
                        visible = showPendingRequestsDropdown,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Surface)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 250.dp)
                            ) {
                                items(uiState.allPendingRequests, key = { it.id }) { booking ->
                                    val isSelected = selectedRequestForMap?.id == booking.id
                                    Surface(
                                        onClick = {
                                            selectedRequestForMap = if (isSelected) null else booking
                                            showPendingRequestsDropdown = false
                                            // Move camera to show the route
                                            if (!isSelected) {
                                                val pickupLoc = LatLng(booking.pickupLocation.latitude, booking.pickupLocation.longitude)
                                                cameraPositionState.position = CameraPosition.fromLatLngZoom(pickupLoc, 13f)
                                            }
                                        },
                                        color = if (isSelected) Primary.copy(alpha = 0.1f) else Color.Transparent
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Vehicle type icon
                                            Surface(
                                                color = if (booking.vehicleType == VehicleType.BOAT) Primary.copy(alpha = 0.2f) else Warning.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = if (booking.vehicleType == VehicleType.BOAT) "🚤" else "🚕",
                                                    modifier = Modifier.padding(8.dp),
                                                    style = MaterialTheme.typography.titleLarge
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.width(12.dp))
                                            
                                            // Details
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = if (booking.pickupAddress.isNotEmpty()) booking.pickupAddress.take(20) + if (booking.pickupAddress.length > 20) "..." else "" else "Location pickup",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Medium,
                                                        maxLines = 1,
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = Warning,
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            text = "👥 ${booking.passengerCount} people",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "→ ${if (booking.destinationAddress.isNotEmpty()) booking.destinationAddress.take(20) + if (booking.destinationAddress.length > 20) "..." else "" else "Destination"}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextSecondary,
                                                    maxLines = 1
                                                )
                                            }
                                            
                                            // Status - no fare shown, driver sets price
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = if (booking.vehicleType == VehicleType.BOAT) "🚤" else "🚕",
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                if (booking.driverId != null) {
                                                    Text(
                                                        text = "🎯 Requested",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Success
                                                    )
                                                } else {
                                                    Text(
                                                        text = "Set price →",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (booking != uiState.allPendingRequests.last()) {
                                        Divider(color = Color.Gray.copy(alpha = 0.2f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Accept/Decline card at the bottom when a request is selected
            // NEW FLOW: Driver sets their price and submits an offer
            selectedRequestForMap?.let { booking ->
                // Check if driver already submitted an offer for this booking
                var myExistingOffer by remember { mutableStateOf<DriverOffer?>(null) }
                var isCheckingOffer by remember { mutableStateOf(true) }
                
                LaunchedEffect(booking.id) {
                    isCheckingOffer = true
                    myExistingOffer = viewModel.getMyOffer(booking.id)
                    isCheckingOffer = false
                }
                
                var priceInputText by remember(booking.id) { 
                    mutableStateOf("")  // Start empty - driver must enter their price
                }
                val enteredPrice = priceInputText.toDoubleOrNull()
                val hasEnteredPrice = enteredPrice != null && enteredPrice > 0
                
                // Compact floating price input bar - lets driver see map
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    if (isCheckingOffer) {
                        // Loading
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Checking...", style = MaterialTheme.typography.bodySmall)
                        }
                    } else if (myExistingOffer != null) {
                        // Already submitted - check if rejected or waiting
                        if (myExistingOffer!!.isRejected) {
                            // Offer was rejected - show message to lower price
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = stringResource(R.string.price_rejected, String.format("%.2f", myExistingOffer!!.price)),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Red
                                            )
                                            Text(
                                                text = stringResource(R.string.too_high_submit_lower),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Red.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                    IconButton(onClick = { 
                                        // Clear existing offer so driver can submit new one
                                        myExistingOffer = null 
                                    }) {
                                        Icon(Icons.Default.Refresh, "Submit new price", tint = Primary)
                                    }
                                }
                            }
                        } else {
                            // Waiting for rider response
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccessTime, null, tint = Warning, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "Offer: $${String.format("%.2f", myExistingOffer!!.price)}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Primary
                                        )
                                        Text(
                                            text = stringResource(R.string.waiting_for_rider),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                IconButton(onClick = { selectedRequestForMap = null }) {
                                    Icon(Icons.Default.Close, "Close", tint = TextSecondary)
                                }
                            }
                        }
                    } else {
                        // Compact price input row
                        Column(modifier = Modifier.padding(10.dp)) {
                            // Top row: Rider info + Close button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Small rider photo
                                    Surface(
                                        modifier = Modifier.size(32.dp),
                                        shape = RoundedCornerShape(50),
                                        color = Primary.copy(alpha = 0.2f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (booking.riderPhotoUrl != null) {
                                                AsyncImage(
                                                    model = booking.riderPhotoUrl,
                                                    contentDescription = "Rider",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Icon(Icons.Default.Person, null, tint = Primary, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = booking.riderName ?: "Rider",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = if (booking.riderIsLocalResident) "🏠 Local" else "✈️ Visitor",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (booking.riderIsLocalResident) Success else Warning
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Passenger count badge - prominent
                                    Surface(
                                        color = Warning,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "👥",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${booking.passengerCount} people",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    // Distance badge
                                    Surface(
                                        color = Primary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "${String.format("%.1f", booking.estimatedDistance)} mi",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { selectedRequestForMap = null },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Close, "Close", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Bottom row: Price input + Submit button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Compact price input
                                OutlinedTextField(
                                    value = priceInputText,
                                    onValueChange = { newValue ->
                                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                            priceInputText = newValue
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(52.dp),
                                    prefix = { Text("$", fontWeight = FontWeight.Bold) },
                                    placeholder = { Text("Price", color = TextSecondary) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                
                                // Submit button
                                Button(
                                    onClick = { 
                                        enteredPrice?.let { price ->
                                            viewModel.submitPriceOffer(booking.id, price)
                                            myExistingOffer = DriverOffer(
                                                price = price,
                                                driverId = "",
                                                driverName = ""
                                            )
                                        }
                                    },
                                    enabled = hasEnteredPrice,
                                    modifier = Modifier.height(52.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Send, null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Send", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            
            // Dock markers
            
            // Online/Offline toggle - prominent button at top center
            Surface(
                shape = RoundedCornerShape(50),
                color = if (uiState.isOnline) Success else Surface,
                shadowElevation = 8.dp,
                onClick = { viewModel.toggleOnlineStatus() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Show vehicle type icon
                    Text(
                        if (uiState.vehicleType == VehicleType.BOAT) "🚤" else "🚕",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        if (uiState.isOnline) Icons.Default.WifiTetheringOff else Icons.Default.Wifi,
                        null,
                        tint = if (uiState.isOnline) TextOnPrimary else TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (uiState.isOnline) stringResource(R.string.go_offline) else stringResource(R.string.go_online),
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isOnline) TextOnPrimary else TextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            // Stats popup - shown when Summary tab is tapped
            AnimatedVisibility(
                visible = showSummary,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today's Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(onClick = { showSummary = false }) {
                                Icon(Icons.Default.Close, "Close")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatBox(
                                icon = Icons.Default.AttachMoney,
                                value = "$${String.format("%.2f", uiState.todayEarnings)}",
                                label = "Earned"
                            )
                            StatBox(
                                icon = Icons.Default.DirectionsCar,
                                value = uiState.todayTrips.toString(),
                                label = "Trips"
                            )
                            StatBox(
                                icon = Icons.Default.AccessTime,
                                value = "${uiState.onlineHours}h",
                                label = "Online"
                            )
                            StatBox(
                                icon = Icons.Default.Star,
                                value = String.format("%.1f", uiState.rating),
                                label = "Rating"
                            )
                        }
                    }
                }
            }
            
            // Show active ride banner if driver has an ongoing ride
            uiState.activeRide?.let { ride ->
                Surface(
                    color = Success,
                    shape = RoundedCornerShape(12.dp),
                    onClick = { onNavigateToActiveRide(ride.id) },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DirectionsBoat,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Active Ride",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${ride.status.name} • Tap to view",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }
        
        // Vehicle switch dialog
        if (showVehicleSwitchDialog) {
            AlertDialog(
                onDismissRequest = { showVehicleSwitchDialog = false },
                title = { Text("Switch Vehicle") },
                text = {
                    Column {
                        Text(
                            "Select which vehicle you want to drive:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Boat option
                        Card(
                            onClick = {
                                viewModel.switchVehicleType(VehicleType.BOAT)
                                showVehicleSwitchDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (uiState.vehicleType == VehicleType.BOAT) Primary.copy(alpha = 0.1f) else Surface
                            ),
                            border = if (uiState.vehicleType == VehicleType.BOAT) 
                                androidx.compose.foundation.BorderStroke(2.dp, Primary) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.DirectionsBoat, "Boat", tint = Primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Boat Captain", fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.weight(1f))
                                if (uiState.vehicleType == VehicleType.BOAT) {
                                    Icon(Icons.Default.CheckCircle, "Selected", tint = Primary)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Taxi option
                        Card(
                            onClick = {
                                viewModel.switchVehicleType(VehicleType.TAXI)
                                showVehicleSwitchDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (uiState.vehicleType == VehicleType.TAXI) Warning.copy(alpha = 0.1f) else Surface
                            ),
                            border = if (uiState.vehicleType == VehicleType.TAXI) 
                                androidx.compose.foundation.BorderStroke(2.dp, Warning) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocalTaxi, "Taxi", tint = Warning)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Taxi Driver", fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.weight(1f))
                                if (uiState.vehicleType == VehicleType.TAXI) {
                                    Icon(Icons.Default.CheckCircle, "Selected", tint = Warning)
                                }
                            }
                        }
                        
                        if (uiState.isOnline) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                stringResource(R.string.go_offline_warning),
                                style = MaterialTheme.typography.bodySmall,
                                color = Warning
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showVehicleSwitchDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun StatBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverEarningsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DriverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Earnings") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Total earnings card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Primary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "This Week",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextOnPrimary.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "$${String.format("%.2f", uiState.weekEarnings)}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = TextOnPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${uiState.weekTrips} trips completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnPrimary.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Earnings breakdown
            Text(
                text = "Earnings Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    EarningsRow("Base Fares", uiState.baseFaresEarnings)
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    EarningsRow("Tips", uiState.tipsEarnings)
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    EarningsRow("Bonuses", uiState.bonusEarnings)
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$${String.format("%.2f", uiState.weekEarnings)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Success
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Daily breakdown
            Text(
                text = "Daily Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            uiState.dailyEarnings.forEach { (day, amount) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "$${String.format("%.2f", amount)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Cash payment info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Payments,
                        null,
                        tint = Success
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Cash Payments",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "You collect payment directly from riders at the end of each trip",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EarningsRow(label: String, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )
        Text(
            text = "$${String.format("%.2f", amount)}",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveRideScreen(
    bookingId: String,
    onRideComplete: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToRiderMode: () -> Unit = {},
    viewModel: DriverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    LaunchedEffect(bookingId) {
        viewModel.loadActiveRide(bookingId)
    }
    
    val booking = uiState.activeRide
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            uiState.currentLocation ?: LatLng(9.4073, -82.3421), // Default to Bocas del Toro, Panama
            15f
        )
    }
    
    // Move camera to follow driver location
    LaunchedEffect(uiState.currentLocation) {
        uiState.currentLocation?.let { driverLoc ->
            cameraPositionState.animate(
                update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(driverLoc, 15f),
                durationMs = 1000
            )
        }
    }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "Rider Mode") },
                    label = { Text("Rider Mode") },
                    selected = false,
                    onClick = onNavigateToRiderMode
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DirectionsBoat, contentDescription = "Active Ride") },
                    label = { Text("Active Ride") },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Navigation, contentDescription = "Driver Home") },
                    label = { Text("Driver") },
                    selected = false,
                    onClick = onNavigateToHome
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Map
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
            ) {
                booking?.let { b ->
                    // Pickup marker - GREEN with always-visible label
                    val pickupPosition = LatLng(b.pickupLocation.latitude, b.pickupLocation.longitude)
                    MarkerComposable(
                        state = rememberMarkerState(key = "pickup_${b.id}", position = pickupPosition)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                color = Success,
                                shape = RoundedCornerShape(8.dp),
                                shadowElevation = 4.dp
                            ) {
                                Text(
                                    text = "📍 PICKUP",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    color = androidx.compose.ui.graphics.Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            // Triangle pointer
                            Surface(
                                modifier = Modifier.size(12.dp),
                                color = Success,
                                shape = androidx.compose.foundation.shape.CircleShape
                            ) {}
                        }
                    }
                    
                    // Dropoff marker - YELLOW with always-visible label
                    val dropoffPosition = LatLng(b.destinationLocation.latitude, b.destinationLocation.longitude)
                    MarkerComposable(
                        state = rememberMarkerState(key = "dropoff_${b.id}", position = dropoffPosition)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                color = Warning,
                                shape = RoundedCornerShape(8.dp),
                                shadowElevation = 4.dp
                            ) {
                                Text(
                                    text = "🏁 DROP OFF",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    color = androidx.compose.ui.graphics.Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            // Triangle pointer
                            Surface(
                                modifier = Modifier.size(12.dp),
                                color = Warning,
                                shape = androidx.compose.foundation.shape.CircleShape
                            ) {}
                        }
                    }
                    
                    // Driver's current location marker - CYAN with always-visible label
                    uiState.currentLocation?.let { driverLoc ->
                        MarkerComposable(
                            state = rememberMarkerState(key = "driver_loc", position = driverLoc)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    color = androidx.compose.ui.graphics.Color(0xFF00BCD4),
                                    shape = RoundedCornerShape(8.dp),
                                    shadowElevation = 4.dp
                                ) {
                                    Text(
                                        text = "🚤 YOU",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        color = androidx.compose.ui.graphics.Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                                // Pointer
                                Surface(
                                    modifier = Modifier.size(12.dp),
                                    color = androidx.compose.ui.graphics.Color(0xFF00BCD4),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                ) {}
                            }
                        }
                        
                        // Route line from driver to next destination
                        val nextDestination = if (b.status == BookingStatus.IN_PROGRESS) {
                            dropoffPosition
                        } else {
                            pickupPosition
                        }
                        Polyline(
                            points = listOf(driverLoc, nextDestination),
                            color = Primary,
                            width = 8f
                        )
                    }
                    
                    // Route line from pickup to dropoff (dashed/lighter)
                    Polyline(
                        points = listOf(pickupPosition, dropoffPosition),
                        color = Primary.copy(alpha = 0.4f),
                        width = 6f
                    )
                }
            }
            
            // Compact navigation bar at top
            if (booking != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = when (booking.status) {
                        BookingStatus.ACCEPTED -> Success
                        BookingStatus.ARRIVED -> Warning
                        BookingStatus.IN_PROGRESS -> Primary
                        else -> Primary
                    },
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (booking.status) {
                                BookingStatus.ACCEPTED -> Icons.Default.Navigation
                                BookingStatus.ARRIVED -> Icons.Default.AccessTime
                                BookingStatus.IN_PROGRESS -> Icons.Default.DirectionsBoat
                                else -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = if (booking.status == BookingStatus.ARRIVED) 
                                androidx.compose.ui.graphics.Color.Black 
                            else 
                                androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (booking.status) {
                                    BookingStatus.ACCEPTED -> "Navigate to Pickup"
                                    BookingStatus.ARRIVED -> stringResource(R.string.waiting_for_passenger)
                                    BookingStatus.IN_PROGRESS -> "Navigate to Drop-off"
                                    else -> "Ride"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (booking.status == BookingStatus.ARRIVED) 
                                    androidx.compose.ui.graphics.Color.Black 
                                else 
                                    androidx.compose.ui.graphics.Color.White
                            )
                            Text(
                                text = when (booking.status) {
                                    BookingStatus.ACCEPTED -> booking.pickupLocation.address ?: ""
                                    BookingStatus.IN_PROGRESS -> booking.destinationLocation.address ?: ""
                                    else -> ""
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (booking.status == BookingStatus.ARRIVED) 
                                    androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f) 
                                else 
                                    androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
                                maxLines = 1
                            )
                        }
                        // Fare badge - show accepted price (the driver's offer that was accepted)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "$${String.format("%.2f", booking.acceptedPrice ?: booking.driverAdjustedFare ?: booking.estimatedFare)}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (booking.status == BookingStatus.ARRIVED) 
                                    androidx.compose.ui.graphics.Color.Black 
                                else 
                                    androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }
                }
                
                // Compact bottom action bar
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(12.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Passenger row - compact
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Passenger photo
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(50),
                                color = Primary.copy(alpha = 0.1f)
                            ) {
                                if (booking.riderPhotoUrl != null) {
                                    AsyncImage(
                                        model = booking.riderPhotoUrl,
                                        contentDescription = "Passenger photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(Icons.Default.Person, null, tint = Primary, modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = booking.riderName ?: "Passenger",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, null, tint = Warning, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("4.9", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                            // Fare adjustment button
                            IconButton(onClick = { viewModel.showFareAdjustment() }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Edit, "Adjust Fare", tint = Primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        
                        // Connect to passenger section
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.connect_to_passenger),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Contact buttons row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Phone button
                            OutlinedButton(
                                onClick = {
                                    booking.riderPhoneNumber?.let { phone ->
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:$phone")
                                        }
                                        context.startActivity(intent)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !booking.riderPhoneNumber.isNullOrBlank()
                            ) {
                                Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call")
                            }
                            
                            // SMS button
                            OutlinedButton(
                                onClick = {
                                    booking.riderPhoneNumber?.let { phone ->
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse("sms:$phone")
                                        }
                                        context.startActivity(intent)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !booking.riderPhoneNumber.isNullOrBlank()
                            ) {
                                Icon(Icons.Default.Message, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Text")
                            }
                            
                            // WhatsApp button
                            OutlinedButton(
                                onClick = {
                                    booking.riderPhoneNumber?.let { phone ->
                                        // Remove non-digits and ensure proper format
                                        val cleanPhone = phone.replace(Regex("[^0-9+]"), "").removePrefix("+")
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse("https://wa.me/$cleanPhone")
                                        }
                                        context.startActivity(intent)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !booking.riderPhoneNumber.isNullOrBlank(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF25D366))
                            ) {
                                Text("💬", style = MaterialTheme.typography.bodyLarge)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp")
                            }
                        }
                        
                        // Night rate indicator - compact
                        if (uiState.isNightRateTime) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Warning.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🌙 Night hours", style = MaterialTheme.typography.bodySmall, color = Warning)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Action button
                        when (booking.status) {
                            BookingStatus.ACCEPTED -> {
                                // Only riders can cancel bookings - drivers just proceed with the ride
                                Button(
                                    onClick = { viewModel.arrivedAtPickup(bookingId) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                                ) { Text(stringResource(R.string.arrived_at_pickup)) }
                            }
                            BookingStatus.ARRIVED -> {
                                Button(
                                    onClick = { viewModel.startRide(bookingId) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                ) { Text(stringResource(R.string.start_ride)) }
                            }
                            BookingStatus.IN_PROGRESS -> {
                                Button(
                                    onClick = { viewModel.completeRide(bookingId); onRideComplete() },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                                ) { Text(stringResource(R.string.complete_ride)) }
                            }
                            BookingStatus.COMPLETED -> {
                                // Ride is done - show clear button
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "✅ Ride Completed!",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Success
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.clearActiveRide(); onRideComplete() },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                    ) { Text("Back to Home") }
                                }
                            }
                            BookingStatus.CANCELLED -> {
                                // Ride was cancelled
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "❌ Ride Cancelled",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Error
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.clearActiveRide(); onNavigateBack() },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Back to Home") }
                                }
                            }
                            else -> {
                                // Fallback clear button for any stuck state
                                Button(
                                    onClick = { viewModel.clearActiveRide(); onNavigateBack() },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Clear & Go Back") }
                            }
                        }
                    }
                }
            }
            
            // Fare Adjustment Bottom Sheet
            if (uiState.showFareAdjustmentSheet) {
                FareAdjustmentSheet(
                    originalFare = booking?.estimatedFare ?: 0.0,
                    adjustedFare = uiState.adjustedFare,
                    adjustmentReason = uiState.adjustmentReason,
                    isNightRateTime = uiState.isNightRateTime,
                    isLoading = uiState.isLoading,
                    onAdjustedFareChange = { viewModel.updateAdjustedFare(it) },
                    onReasonChange = { viewModel.updateAdjustmentReason(it) },
                    onApplyNightRate = { viewModel.applyNightRate() },
                    onApplyBadWeather = { viewModel.applyBadWeatherRate() },
                    onApplyHoliday = { viewModel.applyHolidayRate() },
                    onSubmit = { viewModel.submitFareAdjustment() },
                    onDismiss = { viewModel.hideFareAdjustment() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FareAdjustmentSheet(
    originalFare: Double,
    adjustedFare: String,
    adjustmentReason: String,
    isNightRateTime: Boolean,
    isLoading: Boolean,
    onAdjustedFareChange: (String) -> Unit,
    onReasonChange: (String) -> Unit,
    onApplyNightRate: () -> Unit,
    onApplyBadWeather: () -> Unit,
    onApplyHoliday: () -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(TextPrimary.copy(alpha = 0.5f)),
        color = TextPrimary.copy(alpha = 0.5f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Click outside to dismiss
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TextPrimary.copy(alpha = 0.3f))
            ) {}
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Adjust Fare",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "Notify the rider of a fare adjustment. They can accept or decline.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Original fare display
                    Surface(
                        color = Background,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Original Fare", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "$${String.format("%.2f", originalFare)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Quick adjustment buttons
                    Text(
                        "Quick Adjustments",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Night rate button
                        Surface(
                            onClick = onApplyNightRate,
                            color = if (isNightRateTime) Warning.copy(alpha = 0.2f) else Background,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🌙", style = MaterialTheme.typography.titleLarge)
                                Text(
                                    "Night",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "+50%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        
                        // Bad weather button
                        Surface(
                            onClick = onApplyBadWeather,
                            color = Background,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🌧️", style = MaterialTheme.typography.titleLarge)
                                Text(
                                    "Weather",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "+25%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        
                        // Holiday button
                        Surface(
                            onClick = onApplyHoliday,
                            color = Background,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🎉", style = MaterialTheme.typography.titleLarge)
                                Text(
                                    "Holiday",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "+50%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Custom fare input
                    OutlinedTextField(
                        value = adjustedFare,
                        onValueChange = onAdjustedFareChange,
                        label = { Text("New Fare Amount ($)") },
                        placeholder = { Text("Enter amount") },
                        leadingIcon = { Text("$") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Reason input
                    OutlinedTextField(
                        value = adjustmentReason,
                        onValueChange = onReasonChange,
                        label = { Text("Reason for Adjustment") },
                        placeholder = { Text("e.g., Night rate, bad weather, holiday") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Submit button
                    Button(
                        onClick = onSubmit,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = adjustedFare.isNotBlank() && adjustmentReason.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = TextOnPrimary
                            )
                        } else {
                            Text("Notify Rider of New Fare")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "💵 The rider will be notified and can accept or decline the adjusted fare.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
