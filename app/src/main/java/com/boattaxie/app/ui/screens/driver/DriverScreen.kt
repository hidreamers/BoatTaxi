package com.boattaxie.app.ui.screens.driver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
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
    
    // Refresh active ride when screen becomes visible (on tab switch)
    LaunchedEffect(Unit) {
        viewModel.refreshActiveRide()
    }
    
    // State for summary popup
    var showSummary by remember { mutableStateOf(false) }
    
    // State for vehicle type switch dialog
    var showVehicleSwitchDialog by remember { mutableStateOf(false) }
    
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
                        MarkerComposable(
                            state = rememberMarkerState(position = pickupLoc),
                            onClick = {
                                viewModel.selectRequest(booking)
                                true
                            }
                        ) {
                            // Custom marker showing pickup address and fare
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    color = if (booking.vehicleType == VehicleType.BOAT) Primary else Warning,
                                    shape = RoundedCornerShape(8.dp),
                                    shadowElevation = 4.dp
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // Pickup short address or coordinates
                                        val pickupText = if (booking.pickupAddress.isNotEmpty()) {
                                            booking.pickupAddress.take(15) + if (booking.pickupAddress.length > 15) "..." else ""
                                        } else {
                                            "📍 ${String.format("%.3f", booking.pickupLocation.latitude)}"
                                        }
                                        Text(
                                            text = pickupText,
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (booking.vehicleType == VehicleType.BOAT) "🚤" else "🚕",
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "$${String.format("%.2f", booking.estimatedFare)}",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    }
                                }
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (booking.vehicleType == VehicleType.BOAT) Primary else Warning,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Selected Request Popup - shows full details
            uiState.selectedRequest?.let { booking ->
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Header with vehicle type
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (booking.vehicleType == VehicleType.BOAT) "🚤" else "🚕",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ride Request",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = { viewModel.selectRequest(null) }) {
                                Icon(Icons.Default.Close, "Close")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Pickup
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Default.MyLocation,
                                null,
                                tint = Success,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "PICKUP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Success,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (booking.pickupAddress.isNotEmpty()) booking.pickupAddress 
                                           else "📍 %.4f, %.4f".format(booking.pickupLocation.latitude, booking.pickupLocation.longitude),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        // Line connector
                        Box(
                            modifier = Modifier
                                .padding(start = 9.dp, top = 4.dp, bottom = 4.dp)
                                .width(2.dp)
                                .height(20.dp)
                                .background(Color.Gray)
                        )
                        
                        // Drop-off
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Default.LocationOn,
                                null,
                                tint = Error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "DROP-OFF",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Error,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (booking.destinationAddress.isNotEmpty()) booking.destinationAddress 
                                           else "📍 %.4f, %.4f".format(booking.destinationLocation.latitude, booking.destinationLocation.longitude),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Fare and distance
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$${String.format("%.2f", booking.estimatedFare)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                                Text(
                                    text = "Fare",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${String.format("%.1f", booking.estimatedDistance)} mi",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Distance",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Accept button
                        Button(
                            onClick = { viewModel.acceptSelectedRequest() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Success)
                        ) {
                            Icon(Icons.Default.Check, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Accept This Ride", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
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
                        if (uiState.isOnline) "GO OFFLINE" else "GO ONLINE",
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isOnline) TextOnPrimary else TextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            // Live Ride Requests Button - shows count and toggles map markers
            if (uiState.isOnline) {
                val totalRequests = uiState.boatRequestCount + uiState.taxiRequestCount
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (uiState.showRequestsOnMap) Primary else Surface,
                    shadowElevation = 6.dp,
                    onClick = { viewModel.toggleShowRequestsOnMap() },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 85.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pulsing indicator when there are requests
                        if (totalRequests > 0) {
                            Surface(
                                modifier = Modifier.size(10.dp),
                                shape = RoundedCornerShape(50),
                                color = if (uiState.showRequestsOnMap) Color.White else Error
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = "$totalRequests Ride Requests",
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.showRequestsOnMap) TextOnPrimary else TextPrimary
                        )
                        if (totalRequests > 0) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "🚤${uiState.boatRequestCount} 🚕${uiState.taxiRequestCount}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (uiState.showRequestsOnMap) TextOnPrimary.copy(alpha = 0.9f) else TextSecondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.clearOldPendingBookings() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Clear old requests",
                                    tint = if (uiState.showRequestsOnMap) TextOnPrimary else Error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
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
            
            // New ride request popup
            uiState.pendingRequest?.let { booking ->
                RideRequestPopup(
                    booking = booking,
                    onAccept = { viewModel.acceptBooking(booking.id) },
                    onDecline = { viewModel.declineBooking(booking.id) },
                    modifier = Modifier.align(Alignment.Center)
                )
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
                                "⚠️ You'll go offline when switching vehicles",
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

@Composable
private fun RideRequestPopup(
    booking: Booking,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (booking.driverId != null) "🎯 You've been requested!" else "New Ride Request!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = Primary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$${String.format("%.2f", booking.estimatedFare)}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextOnPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Rider info - show photo and name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Rider photo
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = RoundedCornerShape(50),
                    color = Primary.copy(alpha = 0.1f)
                ) {
                    if (booking.riderPhotoUrl != null) {
                        AsyncImage(
                            model = booking.riderPhotoUrl,
                            contentDescription = "Rider photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                Icons.Default.Person,
                                null,
                                tint = Primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.riderName ?: "Rider",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (booking.driverId != null) {
                        Text(
                            text = "Requested you specifically!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Pickup
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MyLocation,
                    null,
                    tint = Success,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Pickup",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = booking.pickupLocation.address ?: "Location",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Dropoff
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    null,
                    tint = Error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Drop-off",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = booking.destinationLocation.address ?: "Destination",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Trip details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${String.format("%.1f", booking.estimatedDistance)} mi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Distance",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${booking.estimatedDuration} min",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Duration",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)
                ) {
                    Text("Decline")
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Accept")
                }
            }
        }
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
                                    BookingStatus.ARRIVED -> "Waiting for Passenger"
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
                        // Fare badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "$${String.format("%.2f", booking.driverAdjustedFare ?: booking.estimatedFare)}",
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
                            text = "Connect to your passenger",
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
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    TextButton(
                                        onClick = { viewModel.cancelRide(bookingId); onNavigateBack() },
                                        modifier = Modifier.weight(0.3f)
                                    ) { Text("Cancel", color = Error) }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { viewModel.arrivedAtPickup(bookingId) },
                                        modifier = Modifier.weight(0.7f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Success)
                                    ) { Text("Arrived at Pickup") }
                                }
                            }
                            BookingStatus.ARRIVED -> {
                                Button(
                                    onClick = { viewModel.startRide(bookingId) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                ) { Text("Start Ride") }
                            }
                            BookingStatus.IN_PROGRESS -> {
                                Button(
                                    onClick = { viewModel.completeRide(bookingId); onRideComplete() },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                                ) { Text("Complete Ride") }
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
