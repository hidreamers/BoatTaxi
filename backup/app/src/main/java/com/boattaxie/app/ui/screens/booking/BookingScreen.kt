package com.boattaxie.app.ui.screens.booking

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.ui.graphics.painter.Painter
import coil.size.Size
import com.boattaxie.app.R
import com.boattaxie.app.data.model.*
import com.boattaxie.app.ui.components.*
import com.boattaxie.app.ui.theme.*
import com.boattaxie.app.util.LanguageManager
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.io.File
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookRideScreen(
    vehicleType: VehicleType,
    onBookingConfirmed: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLocationSearch: (Boolean) -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToDriverMode: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    selectedAddress: String? = null,
    selectedPlaceId: String? = null,
    isPickupResult: Boolean? = null,
    onClearLocationResult: () -> Unit = {},
    requestDriverId: String? = null,
    requestDriverName: String? = null,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentLanguage by remember { mutableStateOf(LanguageManager.getCurrentLanguage(context)) }
    
    // Search state
    var showSearchDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    
    // Initialize with requested driver if passed from trip history
    LaunchedEffect(requestDriverId, requestDriverName) {
        if (requestDriverId != null && requestDriverName != null) {
            viewModel.setRequestedDriver(requestDriverId, requestDriverName)
        }
    }
    
    // Handle location selection result from LocationSearchScreen
    LaunchedEffect(selectedAddress, selectedPlaceId, isPickupResult) {
        if (selectedAddress != null && selectedPlaceId != null && isPickupResult != null) {
            viewModel.setLocationFromSearch(
                address = selectedAddress,
                placeId = selectedPlaceId,
                isPickup = isPickupResult
            )
            onClearLocationResult()
        }
    }
    
    // Handle navigation to location search
    LaunchedEffect(uiState.isSearchingPickup, uiState.isSearchingDropoff) {
        if (uiState.isSearchingPickup) {
            onNavigateToLocationSearch(true)
            viewModel.closeLocationSearch()
        } else if (uiState.isSearchingDropoff) {
            onNavigateToLocationSearch(false)
            viewModel.closeLocationSearch()
        }
    }
    
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }
    
    // Reset to pickup mode when screen opens
    LaunchedEffect(Unit) {
        viewModel.resetToPickupMode()
    }
    
    LaunchedEffect(vehicleType) {
        viewModel.setVehicleType(vehicleType)
    }
    
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            viewModel.getCurrentLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    
    LaunchedEffect(uiState.bookingConfirmed) {
        if (uiState.bookingConfirmed != null) {
            onBookingConfirmed(uiState.bookingConfirmed!!)
        }
    }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            uiState.currentLocation ?: LatLng(9.4073, -82.3421), // Default to Bocas del Toro
            14f
        )
    }
    
    // Move camera when location is obtained
    LaunchedEffect(uiState.currentLocation) {
        uiState.currentLocation?.let { location ->
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(location, 15f)
            )
        }
    }
    
    // Compact bottom nav - minimal height
    Box(modifier = Modifier.fillMaxSize()) {
        // Map - TAP to set pickup or dropoff (more sensitive than long press)
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = true,
                zoomControlsEnabled = false, // Disabled - custom controls on left to avoid booking panel overlap
                compassEnabled = true
            ),
            onMapClick = { latLng ->
                // User tapped the map - set pickup or dropoff
                viewModel.onMapTapped(latLng)
            }
        ) {
            // Pickup marker - GREEN with info window always shown
            uiState.pickupLocation?.let { pickup ->
                val pickupState = rememberMarkerState(position = pickup)
                MarkerInfoWindow(
                    state = pickupState,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                ) {
                    Surface(
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.pickup_marker),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                LaunchedEffect(pickup) { pickupState.showInfoWindow() }
            }
            
            // Dropoff marker - YELLOW with info window always shown
            uiState.dropoffLocation?.let { dropoff ->
                val dropoffState = rememberMarkerState(position = dropoff)
                MarkerInfoWindow(
                    state = dropoffState,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                ) {
                    Surface(
                        color = Color(0xFFFF9800),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.dropoff_marker),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                LaunchedEffect(dropoff) { dropoffState.showInfoWindow() }
            }
            
            // Route polyline
            if (uiState.pickupLocation != null && uiState.dropoffLocation != null) {
                Polyline(
                    points = listOf(uiState.pickupLocation!!, uiState.dropoffLocation!!),
                    color = Primary,
                    width = 8f
                )
            }
            
            // Ad markers - show businesses with coupons on map (custom composable markers)
            uiState.adsOnMap.forEach { ad ->
                ad.location?.let { location ->
                    val adPosition = LatLng(location.latitude, location.longitude)
                    
                    // Get logo URL
                    val logoUrl = ad.logoUrl ?: ad.imageUrl
                    val hasLogo = !logoUrl.isNullOrBlank()
                    
                    // Preload image using painter - track loading state
                    val imageModel = remember(logoUrl) {
                        if (logoUrl != null && logoUrl.startsWith("/")) java.io.File(logoUrl) else logoUrl
                    }
                    
                    val painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageModel)
                            .size(Size.ORIGINAL)
                            .crossfade(false)
                            .allowHardware(false) // Required for software rendering in MarkerComposable
                            .build()
                    )
                    val painterState = painter.state
                    val isImageLoaded = painterState is AsyncImagePainter.State.Success
                    
                    // Use a key that changes when image loads so marker re-renders
                    val markerKey = remember(ad.id, isImageLoaded) { "${ad.id}_$isImageLoaded" }
                    
                    key(markerKey) {
                        MarkerComposable(
                            state = rememberMarkerState(position = adPosition),
                            zIndex = 100f, // High z-index to show in front of drivers
                            onClick = {
                                viewModel.selectMapAd(ad)
                                true
                            }
                        ) {
                            // Custom marker with logo
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Marker card with logo and info
                                Surface(
                                    color = if (ad.hasCoupon) Color(0xFF4CAF50) else Primary,
                                    shape = RoundedCornerShape(12.dp),
                                    shadowElevation = 8.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Logo image - larger and more visible
                                        Surface(
                                            modifier = Modifier.size(44.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.White
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                if (hasLogo && isImageLoaded) {
                                                    // Image is loaded - show it
                                                    androidx.compose.foundation.Image(
                                                        painter = painter,
                                                        contentDescription = ad.businessName,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                } else {
                                                    // Show category emoji as fallback
                                                    Text(
                                                        text = ad.category.getIcon(),
                                                        fontSize = 24.sp
                                                    )
                                                }
                                            }
                                        }
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Column {
                                        // Business name first
                                        Text(
                                            text = ad.businessName,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        // Deal/Coupon below name - show couponDiscount, or title as deal
                                        val dealText = when {
                                            !ad.couponDiscount.isNullOrBlank() -> "🎟️ ${ad.couponDiscount}"
                                            ad.hasCoupon && !ad.title.isBlank() -> "🎟️ ${ad.title}"
                                            !ad.title.isBlank() && ad.title != ad.businessName -> ad.title
                                            else -> null
                                        }
                                        if (dealText != null) {
                                            Text(
                                                text = dealText,
                                                color = if (ad.hasCoupon) Color.Yellow else Color.White.copy(alpha = 0.9f),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                            // Arrow pointing down
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (ad.hasCoupon) Color(0xFF4CAF50) else Primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    }
                }
            }
            
            // Online driver markers - show when toggled on
            if (uiState.showOnlineDrivers) {
                uiState.onlineDrivers
                    .filter { driver -> 
                        // Check if driver can serve this vehicle type
                        // Use hasBoat/hasTaxi flags for multi-vehicle drivers, fall back to vehicleType
                        when (vehicleType) {
                            VehicleType.BOAT -> driver.hasBoat || driver.vehicleType == VehicleType.BOAT
                            VehicleType.TAXI -> driver.hasTaxi || driver.vehicleType == VehicleType.TAXI
                        }
                    }
                    .forEach { driver ->
                        // Use driver's location if available, otherwise use a position near the camera
                        val driverPosition = driver.currentLocation?.let { 
                            LatLng(it.latitude, it.longitude) 
                        } ?: uiState.currentLocation ?: LatLng(9.4073, -82.3421) // Default to Bocas
                        
                        key(driver.id, vehicleType) {
                            MarkerComposable(
                                keys = arrayOf(driver.id, vehicleType),
                                state = rememberMarkerState(position = driverPosition),
                                onClick = {
                                    viewModel.selectDriver(driver)
                                    true
                                }
                            ) {
                            // Show driver with the currently selected vehicle type styling
                            val isBoatMode = vehicleType == VehicleType.BOAT
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    color = if (isBoatMode) Color(0xFF2196F3) else Color(0xFFFFEB3B),
                                    shape = RoundedCornerShape(8.dp),
                                    shadowElevation = 4.dp
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = driver.fullName.split(" ").firstOrNull() ?: "Driver",
                                            color = if (isBoatMode) Color.White else Color.Black,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (isBoatMode) Color.Yellow else Color(0xFFFF9800),
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Text(
                                                text = String.format("%.1f", driver.rating),
                                                color = if (isBoatMode) Color.White else Color.Black,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }

                                Icon(
                                    if (isBoatMode) Icons.Default.DirectionsBoat else Icons.Default.LocalTaxi,
                                    contentDescription = null,
                                    tint = if (isBoatMode) Color(0xFF2196F3) else Color(0xFFFFEB3B),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        }
                    }
                }
        }
        
        // Selected Ad Popup - FULL SCREEN OVERLAY in center, on top of everything
        uiState.selectedMapAd?.let { ad ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { viewModel.selectMapAd(null) },
                contentAlignment = Alignment.Center
            ) {
                AdMapPopup(
                    ad = ad,
                    onDismiss = { viewModel.selectMapAd(null) },
                    onNavigateTo = {
                        // Set as dropoff location
                        ad.location?.let { loc ->
                            viewModel.setDropoffFromAd(LatLng(loc.latitude, loc.longitude), ad.locationName ?: ad.businessName)
                        }
                        viewModel.selectMapAd(null)
                    },
                    modifier = Modifier
                        .padding(24.dp)
                        .clickable { /* prevent dismiss on card tap */ }
                )
            }
        }
        
        // Show "No drivers online" message when toggled on but none available
        if (uiState.showOnlineDrivers) {
            val driversForVehicleType = when (vehicleType) {
                VehicleType.BOAT -> uiState.boatDriversOnline
                VehicleType.TAXI -> uiState.taxiDriversOnline
            }
            if (driversForVehicleType == 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            if (vehicleType == VehicleType.BOAT) Icons.Default.DirectionsBoat else Icons.Default.LocalTaxi,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (vehicleType == VehicleType.BOAT) stringResource(R.string.no_boats_online) else stringResource(R.string.no_taxis_online),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (vehicleType == VehicleType.BOAT) 
                                stringResource(R.string.no_boats_available_message) 
                            else 
                                stringResource(R.string.no_taxis_available_message),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { viewModel.toggleShowOnlineDrivers() },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(stringResource(R.string.hide_drivers))
                        }
                    }
                }
            }
        }
        
        // Show indicator when a specific driver is selected from trip history - now just a small chip
        // Main instructions moved to bottom panel
        
        // Live drivers count button - below my location button
        Surface(
            onClick = { viewModel.toggleShowOnlineDrivers() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 70.dp, end = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = if (uiState.showOnlineDrivers) Primary else Color.White,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title text
                Text(
                    text = if (uiState.showOnlineDrivers) "Drivers on Map" else "See Nearby Drivers",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (uiState.showOnlineDrivers) Color.White else TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.DirectionsBoat,
                                null,
                                tint = if (uiState.showOnlineDrivers) Color.White else Color(0xFF2196F3),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${uiState.boatDriversOnline}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.showOnlineDrivers) Color.White else Color(0xFF2196F3)
                            )
                        }
                    }
                    Text(
                        text = "|",
                        color = if (uiState.showOnlineDrivers) Color.White.copy(alpha = 0.5f) else Color.Gray
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocalTaxi,
                                null,
                                tint = if (uiState.showOnlineDrivers) Color.White else Color(0xFFFF9800),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${uiState.taxiDriversOnline}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.showOnlineDrivers) Color.White else Color(0xFFFF9800)
                            )
                        }
                    }
                    Icon(
                        if (uiState.showOnlineDrivers) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (uiState.showOnlineDrivers) "Hide drivers" else "Show drivers",
                        tint = if (uiState.showOnlineDrivers) Color.White else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        // Custom zoom controls - left side (to avoid booking panel overlap)
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Zoom In button
            Surface(
                onClick = { 
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            com.google.android.gms.maps.CameraUpdateFactory.zoomIn()
                        )
                    }
                },
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("+", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
            // Zoom Out button
            Surface(
                onClick = { 
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            com.google.android.gms.maps.CameraUpdateFactory.zoomOut()
                        )
                    }
                },
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("−", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
        
        // Home button - top left corner - always goes to rider home
        Surface(
            onClick = onNavigateToHome,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp, top = 8.dp)
                .size(44.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Home",
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Language toggle button - next to home button
        Surface(
            onClick = { currentLanguage = LanguageManager.toggleLanguage(context) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 60.dp, top = 8.dp)
                .size(44.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = currentLanguage.flag,
                    fontSize = 22.sp
                )
            }
        }
        
        // Search button - next to language flag
        Surface(
            onClick = { showSearchDialog = true },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 112.dp, top = 8.dp)
                .size(44.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Collapsible booking state
        var isSheetExpanded by remember { mutableStateOf(false) }
        
        // Bottom sheet - collapsible with always-visible confirm button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            // Compact header - tap to expand/collapse
            Surface(
                onClick = { isSheetExpanded = !isSheetExpanded },
                color = when (uiState.activeBooking?.status) {
                    BookingStatus.PENDING -> Warning // Yellow for pending
                    BookingStatus.ACCEPTED, BookingStatus.ARRIVED, BookingStatus.IN_PROGRESS -> Success // Green for active
                    else -> Primary // Blue for no booking
                },
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Back button
                        Icon(
                            Icons.Default.ArrowBack,
                            "Back",
                            tint = Color.White,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onNavigateBack() }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            if (vehicleType == VehicleType.BOAT) Icons.Default.DirectionsBoat
                            else Icons.Default.LocalTaxi,
                            contentDescription = null,
                            tint = TextOnPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (uiState.activeBooking?.status) {
                                BookingStatus.PENDING -> "Finding..."
                                BookingStatus.ACCEPTED -> "On the way!"
                                BookingStatus.ARRIVED -> "Arrived!"
                                BookingStatus.IN_PROGRESS -> "En Route"
                                else -> if (vehicleType == VehicleType.BOAT) "Boat" else "Taxi"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextOnPrimary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (uiState.estimatedFare != null) {
                            Text(
                                text = "$${String.format("%.2f", uiState.estimatedFare)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextOnPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Icon(
                            if (isSheetExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = if (isSheetExpanded) "Collapse" else "Expand",
                            tint = TextOnPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            // ALWAYS VISIBLE: Compact booking controls
            Surface(
                color = Surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    // Show requesting driver info if selected - very compact single line
                    if (uiState.requestedDriverName != null || uiState.selectedDriver != null) {
                        val driverName = uiState.requestedDriverName ?: uiState.selectedDriver?.fullName ?: ""
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "→ ${driverName.split(" ").firstOrNull()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Success
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                Icons.Default.Close, 
                                "Clear", 
                                tint = TextSecondary, 
                                modifier = Modifier.size(12.dp).clickable { 
                                    viewModel.clearRequestedDriver()
                                    viewModel.selectDriver(null) 
                                }
                            )
                        }
                    }
                    
                    // Super compact location row - single line
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pickup
                        Surface(
                            onClick = { viewModel.setMapTapMode(true) },
                            color = if (uiState.isSettingPickupOnMap) Success.copy(alpha = 0.15f) else Color.Transparent,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Icon(
                                    if (uiState.pickupLocation != null) Icons.Default.CheckCircle else Icons.Default.MyLocation,
                                    null,
                                    tint = Success,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = uiState.pickupAddress?.take(10)?.let { "$it.." } ?: stringResource(R.string.pickup),
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    color = if (uiState.pickupAddress != null) TextPrimary else TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        
                        Text("→", color = TextSecondary, fontSize = 10.sp)
                        
                        // Dropoff
                        Surface(
                            onClick = { viewModel.setMapTapMode(false) },
                            color = if (!uiState.isSettingPickupOnMap) Error.copy(alpha = 0.15f) else Color.Transparent,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Icon(
                                    if (uiState.dropoffLocation != null) Icons.Default.CheckCircle else Icons.Default.LocationOn,
                                    null,
                                    tint = Error,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = uiState.dropoffAddress?.take(10)?.let { "$it.." } ?: stringResource(R.string.dropoff),
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    color = if (uiState.dropoffAddress != null) TextPrimary else TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Check if drivers are online for this vehicle type
                    val driversOnlineForType = when (vehicleType) {
                        VehicleType.BOAT -> uiState.boatDriversOnline
                        VehicleType.TAXI -> uiState.taxiDriversOnline
                    }
                    
                    // Compact button
                    if (uiState.activeBooking == null) {
                        // Show warning if no drivers online
                        if (driversOnlineForType == 0) {
                            Surface(
                                color = Warning.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        null,
                                        tint = Warning,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (vehicleType == VehicleType.BOAT) 
                                            stringResource(R.string.no_boats_online) 
                                        else 
                                            stringResource(R.string.no_taxis_online),
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        } else if (uiState.estimatedFare != null) {
                            // Book button and Reset button side by side
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Reset button
                                OutlinedButton(
                                    onClick = { viewModel.clearLocations() },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                                ) {
                                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.reset), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                
                                // Book button
                                Button(
                                    onClick = { 
                                        if (uiState.hasActiveSubscription) {
                                            viewModel.confirmBooking() 
                                        } else {
                                            viewModel.showSubscriptionRequired()
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    enabled = uiState.pickupLocation != null && uiState.dropoffLocation != null && !uiState.isBooking,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (uiState.requestedDriverName != null) Success else Primary
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    if (uiState.isBooking) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = if (uiState.requestedDriverName != null)
                                                "${stringResource(R.string.send)} $${ String.format("%.2f", uiState.estimatedFare)}"
                                            else
                                                "${stringResource(R.string.book)} $${ String.format("%.2f", uiState.estimatedFare)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            // Get Fare button and Reset button side by side
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Reset button
                                OutlinedButton(
                                    onClick = { viewModel.clearLocations() },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                                ) {
                                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.reset), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                
                                // Get Fare button
                                Button(
                                    onClick = { viewModel.calculateFare() },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    enabled = uiState.pickupLocation != null && uiState.dropoffLocation != null,
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(stringResource(R.string.get_fare), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Show booking status - compact
                        when (uiState.activeBooking?.status) {
                            BookingStatus.PENDING -> {
                                // Finding your ride - styled card
                                Surface(
                                    color = Warning.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Searching icon with animation
                                        Surface(
                                            modifier = Modifier.size(44.dp),
                                            shape = RoundedCornerShape(50),
                                            color = Warning.copy(alpha = 0.3f)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(24.dp),
                                                    color = Warning,
                                                    strokeWidth = 2.dp
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.width(10.dp))
                                        
                                        // Status text
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (vehicleType == VehicleType.BOAT) "Finding Captain..." else "Finding Driver...",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "⏳ Please wait",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Warning,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        
                                        // Price
                                        Text(
                                            text = "$${String.format("%.2f", uiState.activeBooking?.estimatedFare ?: 0.0)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Cancel button
                                Button(
                                    onClick = { viewModel.clearActiveBooking() },
                                    modifier = Modifier.fillMaxWidth().height(32.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Error),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(stringResource(R.string.cancel), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            BookingStatus.ACCEPTED, BookingStatus.ARRIVED -> {
                                // Driver contact section - ALWAYS VISIBLE
                                val activeBooking = uiState.activeBooking!!
                                val contactDriverPhone = activeBooking.driverPhoneNumber ?: ""
                                val contactDriverName = activeBooking.driverName ?: if (vehicleType == VehicleType.BOAT) "Captain" else "Driver"
                                val contactContext = LocalContext.current
                                
                                // Driver info + contact row
                                Surface(
                                    color = Success.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Driver photo
                                        Surface(
                                            modifier = Modifier.size(44.dp),
                                            shape = RoundedCornerShape(50),
                                            color = Success.copy(alpha = 0.3f)
                                        ) {
                                            if (activeBooking.driverPhotoUrl != null) {
                                                AsyncImage(
                                                    model = activeBooking.driverPhotoUrl,
                                                    contentDescription = "Driver",
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        if (vehicleType == VehicleType.BOAT) Icons.Default.Sailing else Icons.Default.LocalTaxi,
                                                        null,
                                                        tint = Success,
                                                        modifier = Modifier.size(26.dp)
                                                    )
                                                }
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.width(10.dp))
                                        
                                        // Name and status
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = contactDriverName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = if (activeBooking.status == BookingStatus.ACCEPTED) 
                                                    (if (vehicleType == VehicleType.BOAT) "🚤 On the way!" else "🚗 On the way!")
                                                else 
                                                    (if (vehicleType == VehicleType.BOAT) "📍 Captain arrived!" else "📍 Driver arrived!"),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Success,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        
                                        // Contact buttons
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            // WhatsApp
                                            Surface(
                                                onClick = {
                                                    if (contactDriverPhone.isNotEmpty()) {
                                                        val phone = contactDriverPhone.replace("+", "").replace(" ", "")
                                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                                            data = Uri.parse("https://wa.me/$phone")
                                                        }
                                                        contactContext.startActivity(intent)
                                                    }
                                                },
                                                color = Color(0xFF25D366),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text("💬", fontSize = 18.sp)
                                                }
                                            }
                                            
                                            // Call
                                            Surface(
                                                onClick = {
                                                    if (contactDriverPhone.isNotEmpty()) {
                                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                                            data = Uri.parse("tel:$contactDriverPhone")
                                                        }
                                                        contactContext.startActivity(intent)
                                                    }
                                                },
                                                color = Primary,
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Phone, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                            
                                            // Text/SMS
                                            Surface(
                                                onClick = {
                                                    if (contactDriverPhone.isNotEmpty()) {
                                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                                            data = Uri.parse("sms:$contactDriverPhone")
                                                        }
                                                        contactContext.startActivity(intent)
                                                    }
                                                },
                                                color = Info,
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Sms, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Cancel button
                                OutlinedButton(
                                    onClick = { viewModel.clearActiveBooking() },
                                    modifier = Modifier.fillMaxWidth().height(32.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(stringResource(R.string.cancel), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            else -> {}
                        }
                    }
                    
                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage!!,
                            fontSize = 10.sp,
                            color = Error
                        )
                    }
                }
            }
            
            // Expandable content - fare details only
            AnimatedVisibility(
                visible = isSheetExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color = Surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Instructions for tap-and-hold
                        Surface(
                            color = Primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = stringResource(R.string.tap_on_map_instructions),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Pickup location display (tap to switch mode)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = Success,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.pickup),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = uiState.pickupAddress ?: stringResource(R.string.tap_on_map_instructions),
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                            }
                            if (uiState.isSettingPickupOnMap) {
                                Surface(
                                    color = Success.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.active),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Success,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        
                        // Connector line
                        Box(
                            modifier = Modifier
                                .padding(start = 9.dp)
                                .width(2.dp)
                                .height(12.dp)
                                .background(Divider)
                        )
                        
                        // Dropoff location display (tap to switch mode)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.dropoff),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = uiState.dropoffAddress ?: stringResource(R.string.tap_on_map_instructions),
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                            }
                            if (!uiState.isSettingPickupOnMap) {
                                Surface(
                                    color = Error.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.active),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Error,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        
                        // Toggle buttons to switch between pickup/dropoff mode
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.setMapTapMode(true) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (uiState.isSettingPickupOnMap) Success.copy(alpha = 0.1f) else Color.Transparent
                                ),
                                border = BorderStroke(1.dp, if (uiState.isSettingPickupOnMap) Success else Divider)
                            ) {
                                Text(stringResource(R.string.set_pickup), color = if (uiState.isSettingPickupOnMap) Success else TextSecondary)
                            }
                            OutlinedButton(
                                onClick = { viewModel.setMapTapMode(false) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (!uiState.isSettingPickupOnMap) Error.copy(alpha = 0.1f) else Color.Transparent
                                ),
                                border = BorderStroke(1.dp, if (!uiState.isSettingPickupOnMap) Error else Divider)
                            ) {
                                Text(stringResource(R.string.set_dropoff), color = if (!uiState.isSettingPickupOnMap) Error else TextSecondary)
                            }
                        }
                        
                        if (uiState.estimatedFare != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Fare estimate
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = stringResource(R.string.estimated_fare),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextSecondary
                                            )
                                            Text(
                                                text = "${uiState.estimatedDistance ?: "-"} mi • ${uiState.estimatedDuration ?: "-"} min",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "$${String.format("%.2f", uiState.estimatedFare)}",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Primary
                                            )
                                            // Show if tourist pricing
                                            if (uiState.isTourist) {
                                                Text(
                                                    text = stringResource(R.string.tourist_rate),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    // Cash payment indicator
                                    Surface(
                                        color = Success.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                Icons.Default.AttachMoney,
                                                null,
                                                tint = Success,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "💵 PAY IN CASH to driver/captain",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Success
                                            )
                                        }
                                    }
                                    
                                    // Pricing tips
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "💡 Tip: Confirm price with captain before trip",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                        
                        // Show ride status info in expanded view
                        if (uiState.activeBooking != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = when (uiState.activeBooking?.status) {
                                    BookingStatus.PENDING -> Warning.copy(alpha = 0.1f)
                                    else -> Success.copy(alpha = 0.1f)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (uiState.activeBooking?.status == BookingStatus.PENDING) Icons.Default.Search else Icons.Default.CheckCircle,
                                        null,
                                        tint = if (uiState.activeBooking?.status == BookingStatus.PENDING) Warning else Success,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (uiState.activeBooking?.status) {
                                            BookingStatus.PENDING -> "Searching for a driver..."
                                            BookingStatus.ACCEPTED -> "Driver accepted! On the way..."
                                            BookingStatus.ARRIVED -> "Driver arrived!"
                                            BookingStatus.IN_PROGRESS -> "Trip in progress..."
                                            else -> "Ride ${uiState.activeBooking?.status?.name}"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (uiState.activeBooking?.status == BookingStatus.PENDING) Warning else Success
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // FLOATING DRIVER POPUP - on top of everything when tapping driver marker
        uiState.selectedDriver?.let { driver ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { viewModel.selectDriver(null) },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .padding(24.dp)
                        .widthIn(max = 300.dp)
                        .clickable { /* prevent dismiss on card tap */ },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Driver photo
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Primary.copy(alpha = 0.1f))
                        ) {
                            if (!driver.profilePhotoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = driver.profilePhotoUrl,
                                    contentDescription = "Driver photo",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Driver name
                        Text(
                            text = driver.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        
                        // Rating
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB800),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1f", driver.rating),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = " • ${driver.totalTrips} rides",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        
                        // Vehicle info
                        if (!driver.vehiclePlate.isNullOrBlank()) {
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text(
                                    text = driver.vehiclePlate!!,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Cancel button
                            OutlinedButton(
                                onClick = { viewModel.selectDriver(null) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(stringResource(R.string.cancel), fontSize = 12.sp)
                            }
                            
                            // Select button
                            Button(
                                onClick = {
                                    viewModel.setRequestedDriver(driver.id, driver.fullName)
                                    viewModel.selectDriver(null)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Success)
                            ) {
                                Text(stringResource(R.string.select), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        
        // Subscription Required Dialog
        if (uiState.showSubscriptionRequired) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissSubscriptionRequired() },
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🚤",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Subscription Required",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "To book rides on BoatTaxie, you need an active subscription.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Benefits list - more compact
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Primary.copy(alpha = 0.1f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "✨ What you get:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                            Text("🚕 Unlimited taxi bookings", style = MaterialTheme.typography.bodySmall)
                            Text("🚤 Unlimited boat rides", style = MaterialTheme.typography.bodySmall)
                            Text("📍 Real-time driver tracking", style = MaterialTheme.typography.bodySmall)
                            Text("⭐ Priority support", style = MaterialTheme.typography.bodySmall)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Browse the map and explore ads for free!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Buttons inside the scrollable content
                        Button(
                            onClick = {
                                viewModel.dismissSubscriptionRequired()
                                onNavigateToSubscription()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Choose Your Plan", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        TextButton(
                            onClick = { viewModel.dismissSubscriptionRequired() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Maybe Later", color = TextSecondary)
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
        }
        
        // Search Dialog
        if (showSearchDialog) {
            SearchDialog(
                searchQuery = searchQuery,
                onSearchQueryChange = { query ->
                    searchQuery = query
                    // Build search results from available data
                    val allResults = mutableListOf<SearchResult>()
                    
                    // Search in ads/businesses
                    uiState.adsOnMap.forEach { ad ->
                        if (ad.businessName.contains(query, ignoreCase = true) ||
                            ad.description.contains(query, ignoreCase = true) ||
                            ad.category.name.contains(query, ignoreCase = true)) {
                            allResults.add(SearchResult(
                                id = ad.id,
                                title = ad.businessName,
                                subtitle = ad.category.name.replace("_", " "),
                                type = SearchResultType.BUSINESS,
                                icon = "🏪",
                                location = ad.location?.let { LatLng(it.latitude, it.longitude) },
                                ad = ad
                            ))
                        }
                    }
                    
                    // Search in online drivers
                    uiState.onlineDrivers.forEach { driver ->
                        if (driver.fullName.contains(query, ignoreCase = true) ||
                            driver.vehiclePlate?.contains(query, ignoreCase = true) == true) {
                            allResults.add(SearchResult(
                                id = driver.id,
                                title = driver.fullName,
                                subtitle = "⭐ ${String.format("%.1f", driver.rating)} • ${driver.totalTrips} rides",
                                type = SearchResultType.DRIVER,
                                icon = if (vehicleType == VehicleType.BOAT) "🚤" else "🚕",
                                location = driver.currentLocation?.let { LatLng(it.latitude, it.longitude) },
                                driver = driver
                            ))
                        }
                    }
                    
                    // Add location-based suggestions - comprehensive Bocas del Toro directory
                    // Coordinates verified with Google Maps for accuracy
                    val locationSuggestions = listOf(
                        // Main Areas - Verified coordinates
                        "Bocas Town" to LatLng(9.340556, -82.241944),
                        "Isla Colón" to LatLng(9.381944, -82.265556),
                        "Bastimentos" to LatLng(9.300833, -82.143056),
                        "Carenero Island" to LatLng(9.336111, -82.228889),
                        "Almirante" to LatLng(9.297778, -82.404722),
                        "Isla Solarte" to LatLng(9.317778, -82.208889),
                        "Isla Cristóbal" to LatLng(9.283333, -82.250000),
                        
                        // Beaches - Verified coordinates
                        "Red Frog Beach" to LatLng(9.280556, -82.132778),
                        "Starfish Beach" to LatLng(9.410278, -82.326944),
                        "Bluff Beach" to LatLng(9.421667, -82.284167),
                        "Playa Paunch" to LatLng(9.365278, -82.276389),
                        "Wizard Beach" to LatLng(9.290833, -82.140556),
                        "Playa Polo" to LatLng(9.285278, -82.138889),
                        "First Beach" to LatLng(9.291944, -82.142778),
                        "Second Beach" to LatLng(9.288333, -82.136667),
                        "Long Beach" to LatLng(9.275833, -82.128889),
                        "Playa Larga" to LatLng(9.270556, -82.125278),
                        
                        // Hotels & Resorts - Verified coordinates
                        "Selina Bocas del Toro" to LatLng(9.340833, -82.242222),
                        "Hotel Bocas del Toro" to LatLng(9.340000, -82.241389),
                        "Playa Tortuga Hotel" to LatLng(9.405278, -82.320000),
                        "Azul Paradise Resort" to LatLng(9.295278, -82.138056),
                        "Red Frog Beach Resort" to LatLng(9.282778, -82.133333),
                        "Punta Caracol" to LatLng(9.349722, -82.280556),
                        "Bocas Paradise Hotel" to LatLng(9.339722, -82.243056),
                        "Hotel Palma Royale" to LatLng(9.340833, -82.241944),
                        "Gran Hotel Bahia" to LatLng(9.340278, -82.241667),
                        "Tropical Suites" to LatLng(9.341111, -82.242500),
                        "Aqua Lounge Hostel" to LatLng(9.340000, -82.240833),
                        "Casa Cayuco" to LatLng(9.310278, -82.205000),
                        "Tranquilo Bay" to LatLng(9.265278, -82.120000),
                        "Al Natural Resort" to LatLng(9.270278, -82.130000),
                        
                        // Restaurants & Bars - Verified coordinates
                        "Bocas Brewery" to LatLng(9.340556, -82.241944),
                        "El Ultimo Refugio" to LatLng(9.358333, -82.265278),
                        "Bibi's on the Beach" to LatLng(9.362778, -82.272222),
                        "Toro Loco" to LatLng(9.340278, -82.241667),
                        "Om Cafe" to LatLng(9.340556, -82.242222),
                        "Capitan Caribe" to LatLng(9.340278, -82.241389),
                        "Restaurante Alberto" to LatLng(9.340000, -82.241111),
                        "Hungry Monkey" to LatLng(9.340833, -82.242500),
                        "Leaf Eaters Cafe" to LatLng(9.340556, -82.241944),
                        "Raw Fusion" to LatLng(9.340278, -82.241667),
                        "La Coralina" to LatLng(9.339722, -82.241389),
                        "Super Gourmet" to LatLng(9.339722, -82.241111),
                        "Skully's" to LatLng(9.339444, -82.240833),
                        "Aqua Lounge Bar" to LatLng(9.340000, -82.240833),
                        "Mondo Taitu" to LatLng(9.339167, -82.240556),
                        "Pickled Parrot" to LatLng(9.338889, -82.240278),
                        
                        // Tours & Activities - Verified coordinates
                        "Dolphin Bay" to LatLng(9.260278, -82.270000),
                        "Bocas Water Sports" to LatLng(9.340833, -82.242778),
                        "Mono Loco Surf School" to LatLng(9.365278, -82.275000),
                        "Bastimentos National Park" to LatLng(9.285000, -82.135000),
                        "Finca Los Monos" to LatLng(9.370278, -82.260000),
                        "Bat Cave" to LatLng(9.320000, -82.205000),
                        "Nivida Bat Cave" to LatLng(9.314722, -82.199722),
                        "Snorkeling Point" to LatLng(9.300000, -82.150000),
                        "Coral Cay" to LatLng(9.305000, -82.160000),
                        
                        // Wellness & Spa - Verified coordinates
                        "Bocas Yoga" to LatLng(9.341111, -82.243056),
                        "Punta Vieja Retreat" to LatLng(9.345000, -82.250000),
                        
                        // Shopping & Services - Verified coordinates
                        "Bocas Marina" to LatLng(9.340278, -82.241111),
                        "Public Market" to LatLng(9.339167, -82.240000),
                        "Town Square" to LatLng(9.339722, -82.240556),
                        "Ferry Terminal Almirante" to LatLng(9.297778, -82.405556),
                        "Almirante Port" to LatLng(9.297500, -82.405278),
                        "Bocas Airport (Isla Colón)" to LatLng(9.340556, -82.250833),
                        
                        // Nature & Wildlife - Verified coordinates
                        "Isla Pájaros (Bird Island)" to LatLng(9.360000, -82.290000),
                        "Coral Gardens" to LatLng(9.310000, -82.155000),
                        "Mangrove Forest" to LatLng(9.320000, -82.220000),
                        "Hospital Point" to LatLng(9.310000, -82.165000),
                        "Cayo Zapatilla" to LatLng(9.250000, -82.080000),
                        "Cayo Crawl" to LatLng(9.305000, -82.170000)
                    )
                    
                    locationSuggestions.forEach { (name, latLng) ->
                        if (name.contains(query, ignoreCase = true)) {
                            allResults.add(SearchResult(
                                id = name,
                                title = name,
                                subtitle = "📍 Popular location",
                                type = SearchResultType.LOCATION,
                                icon = "📍",
                                location = latLng
                            ))
                        }
                    }
                    
                    // Sort by relevance (exact matches first)
                    searchResults = allResults.sortedByDescending { 
                        it.title.startsWith(query, ignoreCase = true) 
                    }
                },
                searchResults = searchResults,
                onResultClick = { result ->
                    coroutineScope.launch {
                        result.location?.let { loc ->
                            // Use higher zoom for precise location targeting
                            // Zoom 17f gives ~150m view for precise location
                            val zoomLevel = when (result.type) {
                                SearchResultType.BUSINESS -> 18f  // Very close for businesses
                                SearchResultType.DRIVER -> 17f    // Close for drivers
                                SearchResultType.LOCATION -> 16f  // Standard for locations
                            }
                            cameraPositionState.animate(
                                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(loc, zoomLevel),
                                durationMs = 800 // Smooth animation
                            )
                            android.util.Log.d("BookingScreen", "Navigating to: ${result.title} at ${loc.latitude}, ${loc.longitude}")
                        }
                        // If it's an ad, show the popup
                        result.ad?.let { viewModel.selectMapAd(it) }
                        // If it's a driver, show driver info
                        result.driver?.let { viewModel.selectDriver(it) }
                    }
                    showSearchDialog = false
                    searchQuery = ""
                    searchResults = emptyList()
                },
                onDismiss = {
                    showSearchDialog = false
                    searchQuery = ""
                    searchResults = emptyList()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationInputField(
    label: String,
    address: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    isActive: Boolean = false,
    onClick: () -> Unit,
    onModeSelect: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        border = if (isActive) BorderStroke(2.dp, iconColor) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon is clickable to select mode without opening search
            IconButton(
                onClick = onModeSelect,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1
                )
            }
            if (isActive) {
                Text(
                    text = stringResource(R.string.tap_map),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = iconColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideTrackingScreen(
    bookingId: String,
    onRideComplete: () -> Unit,
    onCancelRide: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToDriverMode: () -> Unit = {},
    viewModel: BookingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var currentLanguage by remember { mutableStateOf(LanguageManager.getCurrentLanguage(context)) }
    
    // Load ads for map when screen is shown
    LaunchedEffect(Unit) {
        viewModel.refreshAdsForMap()
    }
    
    LaunchedEffect(bookingId) {
        viewModel.observeBooking(bookingId)
    }
    
    val booking = uiState.activeBooking
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            uiState.currentLocation ?: LatLng(9.4073, -82.3421), // Default to Bocas del Toro, Panama
            14f
        )
    }
    
    // Move camera to driver location when it updates
    LaunchedEffect(uiState.driverLocation) {
        uiState.driverLocation?.let { driverLoc ->
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(driverLoc, 15f)
            )
        }
    }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.home)) },
                    label = { Text(stringResource(R.string.home)) },
                    selected = false,
                    onClick = onNavigateToHome
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DirectionsBoat, contentDescription = stringResource(R.string.trips)) },
                    label = { Text(stringResource(R.string.trips)) },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = stringResource(R.string.driver)) },
                    label = { Text(stringResource(R.string.driver)) },
                    selected = false,
                    onClick = onNavigateToDriverMode
                )
                // Language toggle
                NavigationBarItem(
                    icon = { Text(currentLanguage.flag, fontSize = 20.sp) },
                    label = { Text(LanguageManager.getNextLanguage(context).code.uppercase()) },
                    selected = false,
                    onClick = { currentLanguage = LanguageManager.toggleLanguage(context) }
                )
            }
        }
    ) { paddingValues ->
    Box(
        modifier = Modifier.fillMaxSize().padding(paddingValues)
    ) {
        // Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = true,
                    zoomControlsEnabled = false // Disabled - using custom controls
                )
            ) {
                // Driver location marker - shows boat or car icon
                uiState.driverLocation?.let { driver ->
                    Marker(
                        state = MarkerState(position = driver),
                        title = if (booking?.vehicleType == VehicleType.BOAT) "🚤 Captain" else "🚕 Driver",
                        snippet = when (booking?.status) {
                            BookingStatus.ACCEPTED -> "On the way to you"
                            BookingStatus.IN_PROGRESS -> "Taking you to destination"
                            else -> ""
                        }
                    )
                }
                
                // Pickup marker - GREEN with visible label
                booking?.let {
                    val pickup = LatLng(it.pickupLocation.latitude, it.pickupLocation.longitude)
                    val pickupState = rememberMarkerState(position = pickup)
                    MarkerInfoWindow(
                        state = pickupState,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    ) {
                        Surface(
                            color = Color(0xFF4CAF50),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.pickup_marker),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    LaunchedEffect(pickup) { pickupState.showInfoWindow() }
                }
                
                // Dropoff marker - YELLOW with visible label
                booking?.let {
                    val dropoff = LatLng(it.destinationLocation.latitude, it.destinationLocation.longitude)
                    val dropoffState = rememberMarkerState(position = dropoff)
                    MarkerInfoWindow(
                        state = dropoffState,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                    ) {
                        Surface(
                            color = Color(0xFFFF9800),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.dropoff_marker),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    LaunchedEffect(dropoff) { dropoffState.showInfoWindow() }
                }
                
                // Draw route line from driver to pickup (if driver is on the way)
                if (booking?.status == BookingStatus.ACCEPTED && uiState.driverLocation != null) {
                    val pickup = LatLng(booking.pickupLocation.latitude, booking.pickupLocation.longitude)
                    Polyline(
                        points = listOf(uiState.driverLocation!!, pickup),
                        color = Primary,
                        width = 8f
                    )
                }
                
                // Draw route line from pickup to dropoff (if ride in progress)
                if (booking?.status == BookingStatus.IN_PROGRESS) {
                    val dropoff = LatLng(booking.destinationLocation.latitude, booking.destinationLocation.longitude)
                    uiState.driverLocation?.let { driverLoc ->
                        Polyline(
                            points = listOf(driverLoc, dropoff),
                            color = Success,
                            width = 8f
                        )
                    }
                }
                
            }
            
            // Back button - prominent and always visible
            Surface(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(48.dp),
                shape = RoundedCornerShape(50),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.ArrowBack, 
                        "Go Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Custom zoom controls on left side (not covered by panels)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = { 
                        coroutineScope.launch { 
                            cameraPositionState.animate(
                                com.google.android.gms.maps.CameraUpdateFactory.zoomIn()
                            )
                        }
                    },
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, "Zoom In", tint = TextPrimary)
                    }
                }
                Surface(
                    onClick = { 
                        coroutineScope.launch { 
                            cameraPositionState.animate(
                                com.google.android.gms.maps.CameraUpdateFactory.zoomOut()
                            )
                        }
                    },
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Remove, "Zoom Out", tint = TextPrimary)
                    }
                }
            }
            
            // Collapsible ride info
            var isRideInfoExpanded by remember { mutableStateOf(false) }
            
            // Loading state - show when booking is loading
            if (booking == null && uiState.isLoading) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    color = Warning,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Loading your ride...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Bottom popup - collapsible
            if (booking != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    // Compact header - tap to expand/collapse
                    Surface(
                        onClick = { isRideInfoExpanded = !isRideInfoExpanded },
                        color = when (booking.status) {
                            BookingStatus.PENDING -> Info
                            BookingStatus.ACCEPTED -> Primary
                            BookingStatus.ARRIVED -> Success
                            BookingStatus.IN_PROGRESS -> Success
                            else -> Primary
                        },
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Status icon with animation for pending
                                if (booking.status == BookingStatus.PENDING) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // Cancel button for pending rides - BIG and visible
                                    Surface(
                                        onClick = { viewModel.clearActiveBooking() },
                                        color = Error,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        Text(
                                            text = "CANCEL",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        when (booking.status) {
                                            BookingStatus.ACCEPTED -> Icons.Default.DirectionsCar
                                            BookingStatus.ARRIVED -> Icons.Default.Place
                                            BookingStatus.IN_PROGRESS -> Icons.Default.Navigation
                                            else -> Icons.Default.DirectionsBoat
                                        },
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = when (booking.status) {
                                            BookingStatus.PENDING -> "Finding your ride..."
                                            BookingStatus.ACCEPTED -> if (booking.vehicleType == VehicleType.BOAT) "Captain is on their way!" else "Driver is on their way!"
                                            BookingStatus.ARRIVED -> if (booking.vehicleType == VehicleType.BOAT) "Captain has arrived!" else "Driver has arrived!"
                                            BookingStatus.IN_PROGRESS -> "Ride in progress"
                                            else -> booking.status.name
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    // Show ETA when driver accepted
                                    if (booking.status == BookingStatus.ACCEPTED) {
                                        Text(
                                            text = if (uiState.etaMinutes != null) "${uiState.etaMinutes} min away" else "Contact below ↓",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                    if (booking.status == BookingStatus.ARRIVED) {
                                        Text(
                                            text = "Contact below ↓",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Show fare in header
                                Text(
                                    text = "$${String.format("%.2f", booking.driverAdjustedFare ?: booking.estimatedFare)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    if (isRideInfoExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                    contentDescription = if (isRideInfoExpanded) "Collapse" else "Expand",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    
                    // ALWAYS VISIBLE: Driver photo + compact contact buttons - shows after driver accepts until ride starts
                    if (booking.status in listOf(BookingStatus.ACCEPTED, BookingStatus.ARRIVED)) {
                        val contactContext = LocalContext.current
                        val contactDriverPhone = booking.driverPhoneNumber ?: ""
                        val contactDriverName = booking.driverName ?: if (booking.vehicleType == VehicleType.BOAT) "Captain" else "Driver"
                        
                        Surface(
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Driver photo - compact
                                Surface(
                                    modifier = Modifier.size(50.dp),
                                    shape = RoundedCornerShape(50),
                                    color = Primary.copy(alpha = 0.1f)
                                ) {
                                    if (booking.driverPhotoUrl != null) {
                                        AsyncImage(
                                            model = booking.driverPhotoUrl,
                                            contentDescription = "Driver photo",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                if (booking.vehicleType == VehicleType.BOAT) Icons.Default.Sailing else Icons.Default.LocalTaxi,
                                                null,
                                                tint = Primary,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(10.dp))
                                
                                // Driver name
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = contactDriverName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = if (booking.vehicleType == VehicleType.BOAT) "Your Captain" else "Your Driver",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                                
                                // Compact contact buttons
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // WhatsApp
                                    Surface(
                                        onClick = {
                                            if (contactDriverPhone.isNotEmpty()) {
                                                val phone = contactDriverPhone.replace("+", "").replace(" ", "")
                                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                                    data = Uri.parse("https://wa.me/$phone?text=Hi, I'm waiting for my ride!")
                                                }
                                                contactContext.startActivity(intent)
                                            }
                                        },
                                        color = Color(0xFF25D366),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("💬", style = MaterialTheme.typography.titleMedium)
                                        }
                                    }
                                    
                                    // Call
                                    Surface(
                                        onClick = {
                                            if (contactDriverPhone.isNotEmpty()) {
                                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                                    data = Uri.parse("tel:$contactDriverPhone")
                                                }
                                                contactContext.startActivity(intent)
                                            }
                                        },
                                        color = Primary,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Phone, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                        }
                                    }
                                    
                                    // Text
                                    Surface(
                                        onClick = {
                                            if (contactDriverPhone.isNotEmpty()) {
                                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                                    data = Uri.parse("sms:$contactDriverPhone?body=Hi, I'm waiting for my ride!")
                                                }
                                                contactContext.startActivity(intent)
                                            }
                                        },
                                        color = Info,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Sms, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Expandable content
                    AnimatedVisibility(
                        visible = isRideInfoExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Surface(
                            color = Surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Context and driver phone for contact buttons
                                val context = LocalContext.current
                                val driverPhone = booking.driverPhoneNumber ?: ""
                                val driverName = booking.driverName ?: if (booking.vehicleType == VehicleType.BOAT) "Captain" else "Driver"
                                
                                // For PENDING - show searching message, no driver info yet
                                if (booking.status == BookingStatus.PENDING) {
                                    Surface(
                                        color = Warning.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(36.dp),
                                                color = Warning,
                                                strokeWidth = 3.dp
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    text = if (booking.vehicleType == VehicleType.BOAT) "🚤 Finding Captain..." else "🚗 Finding Driver...",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = "Your request has been sent. Please wait...",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                // Only show driver info if driver assigned
                                if (booking.status in listOf(BookingStatus.ACCEPTED, BookingStatus.ARRIVED, BookingStatus.IN_PROGRESS)) {
                                    // Driver/Captain info - Uber style
                            
                                    // Driver photo, name, rating
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Driver photo
                                        Surface(
                                            modifier = Modifier.size(64.dp),
                                            shape = RoundedCornerShape(50),
                                            color = Primary.copy(alpha = 0.1f)
                                        ) {
                                            if (booking.driverPhotoUrl != null) {
                                                AsyncImage(
                                                    model = booking.driverPhotoUrl,
                                                    contentDescription = "Driver photo",
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        if (booking.vehicleType == VehicleType.BOAT) Icons.Default.Sailing else Icons.Default.LocalTaxi,
                                                        null,
                                                        tint = Primary,
                                                        modifier = Modifier.size(36.dp)
                                                    )
                                                }
                                            }
                                        }
                                    
                                        Spacer(modifier = Modifier.width(12.dp))
                                    
                                        Column(modifier = Modifier.weight(1f)) {
                                            // Driver name
                                            Text(
                                                text = driverName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            // Rating and trips
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Star,
                                                    null,
                                                    tint = Warning,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${String.format("%.1f", booking.driverRatingValue ?: 5.0f)} • ${booking.driverTotalTrips ?: 0} trips",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextSecondary
                                                )
                                            }
                                            // License info
                                            if (booking.driverLicenseNumber != null) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Default.Badge,
                                                        null,
                                                        tint = Success,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "${booking.driverLicenseType ?: "License"}: ${booking.driverLicenseNumber}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Success
                                                    )
                                                }
                                            }
                                        }
                                    }
                                
                                    Spacer(modifier = Modifier.height(12.dp))
                                
                                    // Vehicle info card
                                    if (booking.vehicleModel != null || booking.vehiclePlate != null) {
                                        Surface(
                                            color = Background,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Vehicle icon or photo
                                                Surface(
                                                    modifier = Modifier.size(48.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Primary.copy(alpha = 0.1f)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = if (booking.vehicleType == VehicleType.BOAT) "🚤" else "🚕",
                                                            style = MaterialTheme.typography.headlineSmall
                                                        )
                                                    }
                                                }
                                            
                                                Spacer(modifier = Modifier.width(12.dp))
                                            
                                                Column(modifier = Modifier.weight(1f)) {
                                                    // Vehicle model & color
                                                    Text(
                                                        text = "${booking.vehicleColor ?: ""} ${booking.vehicleModel ?: "Vehicle"}".trim(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    // Plate number
                                                    if (booking.vehiclePlate != null) {
                                                        Surface(
                                                            color = TextPrimary,
                                                            shape = RoundedCornerShape(4.dp)
                                                        ) {
                                                            Text(
                                                                text = booking.vehiclePlate!!,
                                                                style = MaterialTheme.typography.labelMedium,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Price
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.estimated_fare),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                // Show adjusted fare if driver proposed one
                                if (booking.driverAdjustedFare != null) {
                                    Text(
                                        text = "$${String.format("%.2f", booking.estimatedFare)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                    Text(
                                        text = "$${String.format("%.2f", booking.driverAdjustedFare)}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Warning
                                    )
                                    Text(
                                        text = booking.fareAdjustmentReason ?: "Adjusted",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Warning
                                    )
                                } else {
                                    Text(
                                        text = "$${String.format("%.2f", booking.estimatedFare)}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
                                    )
                                }
                            }
                        }
                        
                        // Cash payment reminder (only show for pending/accepted)
                        if (booking.status in listOf(BookingStatus.PENDING, BookingStatus.ACCEPTED)) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Cash payment reminder
                            Surface(
                                color = Success.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "💵",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.pay_cash),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Success
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (uiState.isLoading) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            // Driver Fare Adjustment Dialog
            if (uiState.showFareAdjustmentDialog) {
                FareAdjustmentNotificationDialog(
                    originalFare = booking?.estimatedFare ?: 0.0,
                    adjustedFare = uiState.driverAdjustedFare ?: 0.0,
                    reason = uiState.fareAdjustmentReason ?: "",
                    isNightRate = uiState.isNightRate,
                    onAccept = { viewModel.acceptAdjustedFare() },
                    onDecline = { viewModel.declineAdjustedFare() },
                    onDismiss = { viewModel.dismissFareAdjustmentDialog() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideCompleteScreen(
    bookingId: String,
    onNavigateHome: () -> Unit,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var rating by remember { mutableStateOf(5) }
    var tip by remember { mutableStateOf(0.0) }
    var review by remember { mutableStateOf("") }
    
    LaunchedEffect(bookingId) {
        viewModel.loadCompletedBooking(bookingId)
    }
    
    val booking = uiState.completedBooking
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ride_complete)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Success icon
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(50),
                color = Success.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = Success,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.thanks_for_riding),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = stringResource(R.string.arrived_destination),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Fare breakdown
            if (booking != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.fare_summary),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        FareRow("Estimated Fare", booking.estimatedFare)
                        FareRow("Distance", booking.estimatedDistance.toDouble(), suffix = " km")
                        FareRow("Duration", booking.estimatedDuration.toDouble(), suffix = " min")
                        
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.total),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$${String.format("%.2f", booking.finalFare ?: booking.estimatedFare)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Rating
            Text(
                text = if (booking?.vehicleType == VehicleType.BOAT) 
                    stringResource(R.string.rate_captain) 
                else 
                    stringResource(R.string.rate_driver),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..5).forEach { star ->
                    IconButton(onClick = { rating = star }) {
                        Icon(
                            if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Rate $star",
                            tint = if (star <= rating) Warning else TextSecondary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Review
            OutlinedTextField(
                value = review,
                onValueChange = { review = it },
                label = { Text(stringResource(R.string.leave_comment)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Tip
            Text(
                text = stringResource(R.string.add_tip),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(0.0, 2.0, 5.0, 10.0).forEach { amount ->
                    FilterChip(
                        selected = tip == amount,
                        onClick = { tip = amount },
                        label = {
                            Text(
                                if (amount == 0.0) stringResource(R.string.no_tip) else "$${amount.toInt()}"
                            )
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            PrimaryButton(
                text = if (tip > 0) "Submit & Pay \$${String.format("%.2f", tip)} Tip" else "Submit Rating",
                onClick = {
                    viewModel.submitRating(bookingId, rating, review, tip)
                    onNavigateHome()
                },
                isLoading = uiState.isSubmitting
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TextButton(onClick = onNavigateHome) {
                Text(stringResource(R.string.skip))
            }
        }
    }
}

@Composable
private fun FareRow(label: String, amount: Double, suffix: String = "") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = if (suffix.isNotEmpty()) "${String.format("%.1f", amount)}$suffix" else "$${String.format("%.2f", amount)}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Popup showing ad details when user taps on a business marker
 */
@Composable
fun AdMapPopup(
    ad: Advertisement,
    onDismiss: () -> Unit,
    onNavigateTo: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Check if local image exists
    val hasLocalImage = remember(ad.imageUrl) {
        if (ad.imageUrl.isNullOrBlank()) false
        else if (ad.imageUrl.startsWith("/")) File(ad.imageUrl).exists()
        else true
    }
    
    // Extract YouTube video ID
    val youtubeVideoId = remember(ad.youtubeUrl) {
        ad.youtubeUrl?.let { url ->
            val patterns = listOf(
                """(?:youtube\.com/watch\?v=|youtube\.com/watch\?.*&v=)([^&]+)""",
                """youtu\.be/([^?&]+)""",
                """youtube\.com/embed/([^?&]+)""",
                """youtube\.com/shorts/([^?&]+)"""
            )
            for (pattern in patterns) {
                val match = Regex(pattern).find(url)
                if (match != null) return@let match.groupValues[1]
            }
            null
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column {
            // Image section at top
            if (hasLocalImage && !ad.imageUrl.isNullOrBlank()) {
                val imageModel = remember(ad.imageUrl) {
                    if (ad.imageUrl.startsWith("/")) File(ad.imageUrl) else ad.imageUrl
                }
                AsyncImage(
                    model = imageModel,
                    contentDescription = ad.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else if (youtubeVideoId != null) {
                val context = LocalContext.current
                android.util.Log.d("BookingScreen", "YouTube video ID: $youtubeVideoId, URL: ${ad.youtubeUrl}")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clickable {
                            try {
                                android.util.Log.d("BookingScreen", "Tapped YouTube, opening: ${ad.youtubeUrl}")
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ad.youtubeUrl))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.util.Log.e("BookingScreen", "Failed to open YouTube: ${e.message}", e)
                            }
                        }
                ) {
                    AsyncImage(
                        model = "https://img.youtube.com/vi/$youtubeVideoId/hqdefault.jpg",
                        contentDescription = "YouTube Video",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        modifier = Modifier.align(Alignment.Center).size(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFFFF0000).copy(alpha = 0.9f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("▶", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                    // Tap to play label
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Black.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "Tap to watch video",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            } else {
                // Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Primary.copy(alpha = 0.8f), Primary.copy(alpha = 0.95f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = ad.category.getIcon(), fontSize = 48.sp)
                        Text(
                            text = ad.category.getDisplayName(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                // Header row with logo and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo - check if exists
                        val logoPath = ad.logoUrl ?: ad.imageUrl
                        val logoExists = remember(logoPath) {
                            if (logoPath.isNullOrBlank()) false
                            else if (logoPath.startsWith("/")) File(logoPath).exists()
                            else true
                        }
                        
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Surface
                        ) {
                            if (logoExists && !logoPath.isNullOrBlank()) {
                                AsyncImage(
                                    model = if (logoPath.startsWith("/")) File(logoPath) else logoPath,
                                    contentDescription = "Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = ad.category.getIcon(),
                                        fontSize = 28.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = ad.businessName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${ad.category.getIcon()} ${ad.category.getDisplayName()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
                
                // Description
                if (ad.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = ad.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Location name
            if (ad.locationName != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Place,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = ad.locationName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            
            // Coupon section
            if (ad.hasCoupon && ad.couponCode != null && AdHelper.isCouponValid(ad)) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!ad.couponDiscount.isNullOrBlank()) {
                            Text(
                                text = "🎟️ ${ad.couponDiscount}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Success
                            )
                        }
                        Text(
                            text = "Code: ${ad.couponCode}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!ad.couponDescription.isNullOrBlank()) {
                            Text(
                                text = ad.couponDescription,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Navigate button
            Button(
                onClick = onNavigateTo,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Navigation, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.set_as_destination))
            }
        }
    }
}

/**
 * Horizontal scrolling ads bar at bottom of map screen
 * Shows ads in a compact strip that doesn't cover the map
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrollingAdsWhileWaiting(
    ads: List<Advertisement>,
    onAdClick: (Advertisement) -> Unit,
    modifier: Modifier = Modifier
) {
    // Don't show anything if no ads
    if (ads.isEmpty()) {
        return
    }
    
    var currentAdIndex by remember { mutableStateOf(0) }
    
    // Auto-scroll every 4 seconds
    LaunchedEffect(ads) {
        if (ads.isNotEmpty()) {
            while (true) {
                kotlinx.coroutines.delay(4000)
                currentAdIndex = (currentAdIndex + 1) % ads.size
            }
        }
    }
    
    val currentAd = ads.getOrNull(currentAdIndex) ?: return
    
    // Horizontal compact bar
    Surface(
        onClick = { onAdClick(currentAd) },
        modifier = modifier
            .padding(horizontal = 8.dp)
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        color = Surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon / thumbnail
            val hasLocalImage = remember(currentAd.imageUrl) {
                if (currentAd.imageUrl.isNullOrBlank()) false
                else if (currentAd.imageUrl.startsWith("/")) File(currentAd.imageUrl).exists()
                else true
            }
            
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = Primary.copy(alpha = 0.1f)
            ) {
                if (hasLocalImage && !currentAd.imageUrl.isNullOrBlank()) {
                    val imageModel = remember(currentAd.imageUrl) {
                        if (currentAd.imageUrl.startsWith("/")) File(currentAd.imageUrl) else currentAd.imageUrl
                    }
                    AsyncImage(
                        model = imageModel,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(text = currentAd.category.getIcon(), fontSize = 20.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Ad info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentAd.businessName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (currentAd.hasCoupon && currentAd.couponDiscount != null) {
                        Text(
                            text = "🎟️ ${currentAd.couponDiscount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Success,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = "${currentAd.category.getIcon()} ${currentAd.category.getDisplayName()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
            
            // Counter and arrow
            Text(
                text = "${currentAdIndex + 1}/${ads.size}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View",
                modifier = Modifier.size(20.dp),
                tint = Primary
            )
        }
    }
}

/**
 * Dialog shown to rider when driver proposes a fare adjustment
 */
@Composable
private fun FareAdjustmentNotificationDialog(
    originalFare: Double,
    adjustedFare: Double,
    reason: String,
    isNightRate: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text(
                text = if (isNightRate) "🌙" else "💰",
                style = MaterialTheme.typography.displaySmall
            )
        },
        title = {
            Text(
                text = stringResource(R.string.fare_adjustment),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isNightRate) 
                        stringResource(R.string.captain_proposed_fare) 
                    else 
                        stringResource(R.string.driver_proposed_fare),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Fare comparison
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.original),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "$${String.format("%.2f", originalFare)}",
                            style = MaterialTheme.typography.titleMedium,
                            textDecoration = TextDecoration.LineThrough,
                            color = TextSecondary
                        )
                    }
                    
                    Icon(
                        Icons.Default.ArrowForward,
                        null,
                        tint = TextSecondary,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.new_fare),
                            style = MaterialTheme.typography.labelMedium,
                            color = Primary
                        )
                        Text(
                            text = "$${String.format("%.2f", adjustedFare)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Reason
                Surface(
                    color = if (isNightRate) Warning.copy(alpha = 0.1f) else Info.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isNightRate) "🌙" else "ℹ️",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = if (isNightRate) 
                        stringResource(R.string.cash_payment_captain) 
                    else 
                        stringResource(R.string.cash_payment_driver),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.accept_new_fare))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDecline,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)
            ) {
                Text(stringResource(R.string.decline_cancel_ride))
            }
        }
    )
}

// Search result types
enum class SearchResultType {
    BUSINESS,
    DRIVER,
    LOCATION
}

// Search result data class
data class SearchResult(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: SearchResultType,
    val icon: String,
    val location: LatLng? = null,
    val ad: Advertisement? = null,
    val driver: User? = null
)

/**
 * Full-screen search dialog with real-time filtering
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchDialog(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchResults: List<SearchResult>,
    onResultClick: (SearchResult) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f),
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔍 Search",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Search input field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { 
                        Text(
                            "Search places, businesses, drivers...",
                            color = TextSecondary
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = TextSecondary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Divider,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (searchQuery.isEmpty()) {
                    // Show quick search suggestions when empty
                    Text(
                        text = "Quick Search",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // Category chips - Row 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickSearchChip("🍽️ Food", onClick = { onSearchQueryChange("restaurant") })
                        QuickSearchChip("🚤 Boats", onClick = { onSearchQueryChange("boat") })
                        QuickSearchChip("🏨 Hotels", onClick = { onSearchQueryChange("hotel") })
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Row 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickSearchChip("🎉 Tours", onClick = { onSearchQueryChange("tour") })
                        QuickSearchChip("🏖️ Beach", onClick = { onSearchQueryChange("beach") })
                        QuickSearchChip("🌴 Island", onClick = { onSearchQueryChange("isla") })
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Row 3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickSearchChip("🍺 Bars", onClick = { onSearchQueryChange("bar") })
                        QuickSearchChip("🧘 Spa", onClick = { onSearchQueryChange("yoga") })
                        QuickSearchChip("🏄 Surf", onClick = { onSearchQueryChange("surf") })
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Row 4
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickSearchChip("🐬 Wildlife", onClick = { onSearchQueryChange("dolphin") })
                        QuickSearchChip("🤿 Snorkel", onClick = { onSearchQueryChange("snorkel") })
                        QuickSearchChip("☕ Cafe", onClick = { onSearchQueryChange("cafe") })
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "Popular Locations",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // Popular locations - expanded
                    listOf(
                        "📍 Bocas Town" to "Main town, restaurants & nightlife",
                        "🏝️ Starfish Beach" to "Crystal clear water, starfish",
                        "🐸 Red Frog Beach" to "Beautiful beach, resorts",
                        "🏄 Bluff Beach" to "Great surfing waves",
                        "🐬 Dolphin Bay" to "Dolphin watching tours",
                        "🏖️ Playa Paunch" to "Surf spot, local vibe",
                        "🦇 Bat Cave" to "Nivida bat cave adventure",
                        "🌊 Carenero Island" to "Quick boat ride from town",
                        "🏨 Selina Hostel" to "Popular hostel & coworking",
                        "🍺 Aqua Lounge" to "Famous overwater bar"
                    ).forEach { (name, desc) ->
                        Surface(
                            onClick = { 
                                val cleanName = name.substringAfter(" ")
                                onSearchQueryChange(cleanName) 
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = name.substringBefore(" "),
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = name.substringAfter(" "),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                } else if (searchResults.isEmpty()) {
                    // No results
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🔍",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No results found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Try a different search term",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    // Show search results grouped by type
                    val businesses = searchResults.filter { it.type == SearchResultType.BUSINESS }
                    val drivers = searchResults.filter { it.type == SearchResultType.DRIVER }
                    val locations = searchResults.filter { it.type == SearchResultType.LOCATION }
                    
                    if (locations.isNotEmpty()) {
                        Text(
                            text = "📍 Locations (${locations.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        locations.forEach { result ->
                            SearchResultItem(result = result, onClick = { onResultClick(result) })
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    if (businesses.isNotEmpty()) {
                        Text(
                            text = "🏪 Businesses (${businesses.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        businesses.forEach { result ->
                            SearchResultItem(result = result, onClick = { onResultClick(result) })
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    if (drivers.isNotEmpty()) {
                        Text(
                            text = "🚗 Drivers (${drivers.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        drivers.forEach { result ->
                            SearchResultItem(result = result, onClick = { onResultClick(result) })
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun QuickSearchChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Primary.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun SearchResultItem(
    result: SearchResult,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Divider)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        when (result.type) {
                            SearchResultType.BUSINESS -> Primary.copy(alpha = 0.1f)
                            SearchResultType.DRIVER -> Success.copy(alpha = 0.1f)
                            SearchResultType.LOCATION -> Warning.copy(alpha = 0.1f)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = result.icon,
                    fontSize = 22.sp
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = result.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Arrow
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}