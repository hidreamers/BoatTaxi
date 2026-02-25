package com.boattaxie.app.ui.screens.explore

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.boattaxie.app.R
import com.boattaxie.app.data.model.Advertisement
import com.boattaxie.app.data.model.NearbyPlace
import com.boattaxie.app.data.model.PlaceCategory
import com.boattaxie.app.data.model.TimeFilter
import com.boattaxie.app.data.model.NewsArticle
import com.boattaxie.app.data.model.WeatherData
import com.boattaxie.app.data.model.WeatherForecast
import com.boattaxie.app.ui.theme.*
import com.boattaxie.app.ui.screens.booking.AdMapPopup
import com.boattaxie.app.ui.components.LiveUsersBadge
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBookBoat: () -> Unit = {},
    onNavigateToBookTaxi: () -> Unit = {},
    onNavigateToBookRide: (placeId: String, placeName: String, lat: Double, lng: Double) -> Unit,
    onNavigateToAdDetails: (String) -> Unit = {},
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Check location permission
    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    // Map state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(uiState.currentLocation, 16f)
    }
    
    // Bottom sheet state
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val adSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Search state
    var showSearchBar by remember { mutableStateOf(false) }
    
    // Fullscreen mode - hides controls for more map space
    var fullscreenMode by remember { mutableStateOf(false) }
    
    // Controls expanded state
    var showControls by remember { mutableStateOf(true) }
    
    // Map type state
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var showMapTypeMenu by remember { mutableStateOf(false) }
    
    // Show error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }
    
    // Move camera when target location changes
    LaunchedEffect(uiState.targetLocation) {
        uiState.targetLocation?.let { target ->
            // Zoom out more for larger search areas
            val zoom = when {
                uiState.searchRadiusKm >= 1000f -> 5f
                uiState.searchRadiusKm >= 500f -> 7f
                uiState.searchRadiusKm >= 250f -> 8f
                uiState.searchRadiusKm >= 100f -> 10f
                else -> 12f
            }
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(target, zoom)
            )
        }
    }
    
    // Place Details Bottom Sheet
    if (uiState.showPlaceDetails && uiState.selectedPlace != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closePlaceDetails() },
            sheetState = sheetState
        ) {
            PlaceDetailsSheet(
                place = uiState.selectedPlace!!,
                onShowOnMap = { place ->
                    viewModel.closePlaceDetails()
                    viewModel.clearSearch()
                    showSearchBar = false
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(place.latLng, 19f)
                        )
                    }
                },
                onCall = { phone ->
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    context.startActivity(intent)
                },
                onWebsite = { url ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                },
                onDirections = { place ->
                    val uri = Uri.parse("google.navigation:q=${place.latLng.latitude},${place.latLng.longitude}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.setPackage("com.google.android.apps.maps")
                    context.startActivity(intent)
                },
                getPhotoUrl = { viewModel.getPhotoUrl(it) }
            )
        }
    }
    
    // Deal Details Bottom Sheet
    if (uiState.selectedAd != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeAdDetails() },
            sheetState = adSheetState
        ) {
            DealDetailsSheet(
                ad = uiState.selectedAd!!,
                onNavigateTo = {
                    viewModel.closeAdDetails()
                    uiState.selectedAd?.location?.let { loc ->
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 17f)
                            )
                        }
                    }
                },
                onCall = { phone ->
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    context.startActivity(intent)
                },
                onWebsite = { url ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                },
                onDismiss = { viewModel.closeAdDetails() }
            )
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                // Hide top bar in fullscreen mode
                AnimatedVisibility(
                    visible = !fullscreenMode,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                ) {
                    TopAppBar(
                        title = { 
                            Text(stringResource(R.string.explore_title))
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.ArrowBack, "Back")
                            }
                        },
                        actions = {
                            // Live users badge
                            LiveUsersBadge(compact = true)
                            
                            // My location button - center map on user's actual GPS location
                            IconButton(onClick = {
                                viewModel.refreshLocation()
                                coroutineScope.launch {
                                    // Small delay to let location update
                                    kotlinx.coroutines.delay(300)
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(uiState.currentLocation, 19f)
                                    )
                                }
                            }) {
                                Icon(Icons.Default.MyLocation, "My Location")
                            }
                            IconButton(onClick = { showSearchBar = !showSearchBar }) {
                                Icon(
                                    if (showSearchBar) Icons.Default.Close else Icons.Default.Search,
                                    "Search"
                                )
                            }
                            IconButton(onClick = { viewModel.refresh() }) {
                                Icon(Icons.Default.Refresh, "Refresh")
                            }
                            // Fullscreen toggle
                            IconButton(onClick = { fullscreenMode = true }) {
                                Icon(Icons.Default.Fullscreen, "Fullscreen")
                            }
                        }
                    )
                }
                
                // Search bar - full width below app bar
                AnimatedVisibility(
                    visible = showSearchBar && !fullscreenMode,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 4.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.searchPlaces(it) },
                            placeholder = { Text("Search places, cities, countries...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            leadingIcon = {
                                Icon(Icons.Default.Search, "Search", tint = Primary)
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.clearSearch() }) {
                                        Icon(Icons.Default.Clear, "Clear")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // All controls - hidden in fullscreen mode
                AnimatedVisibility(
                    visible = !fullscreenMode,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        // Compact header row with Book buttons and toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Book Boat button - compact
                            Button(
                                onClick = onNavigateToBookBoat,
                                colors = ButtonDefaults.buttonColors(containerColor = BoatColor),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text("🚤 Book Boat", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            
                            // Book Taxi button - compact
                            Button(
                                onClick = onNavigateToBookTaxi,
                                colors = ButtonDefaults.buttonColors(containerColor = TaxiColor),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text("🚕 Book Taxi", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            
                            // Expand/Collapse controls toggle
                            IconButton(
                                onClick = { showControls = !showControls },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    if (showControls) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (showControls) "Hide filters" else "Show filters",
                                    tint = Primary
                                )
                            }
                        }
                        
                        // Collapsible filters section
                        AnimatedVisibility(
                            visible = showControls,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                // Category chips
            CategoryChips(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.setCategory(it) }
            )
            
            // Discovery toggle and Radius selector row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Real-time discovery toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.toggleDiscovery(!uiState.isDiscoveryEnabled) }
                ) {
                    Icon(
                        if (uiState.isDiscoveryEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = if (uiState.isDiscoveryEnabled) Color(0xFF4CAF50) else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = if (uiState.isDiscoveryEnabled) "Live" else "Off",
                            fontSize = 11.sp,
                            fontWeight = if (uiState.isDiscoveryEnabled) FontWeight.Bold else FontWeight.Normal,
                            color = if (uiState.isDiscoveryEnabled) Color(0xFF4CAF50) else TextSecondary
                        )
                        Text(
                            text = if (uiState.isDiscoveryEnabled) "Within 45m (~150 ft)" else "Auto-shows nearby places",
                            fontSize = 9.sp,
                            color = TextSecondary
                        )
                    }
                    if (uiState.isDiscoveryEnabled) {
                        Spacer(modifier = Modifier.width(4.dp))
                        // Pulsing dot indicator
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 0.3f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulseAlpha"
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50).copy(alpha = alpha))
                        )
                    }
                }
                
                // Auto-show places on map toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.toggleAutoShowPlaces(!uiState.autoShowPlacesOnMap) }
                ) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = null,
                        tint = if (uiState.autoShowPlacesOnMap) Primary else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = if (uiState.autoShowPlacesOnMap) "Auto" else "Manual",
                        fontSize = 10.sp,
                        fontWeight = if (uiState.autoShowPlacesOnMap) FontWeight.Bold else FontWeight.Normal,
                        color = if (uiState.autoShowPlacesOnMap) Primary else TextSecondary
                    )
                }
                
                // Radius selector
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Radius:",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    var expanded by remember { mutableStateOf(false) }
                    val radiusOptions = listOf(1f, 2f, 5f, 10f, 25f, 50f, 100f, 250f, 500f, 1000f, 5000f)
                    
                    Box {
                        Surface(
                            onClick = { expanded = true },
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val displayValue = when {
                                    uiState.searchRadiusKm >= 5000f -> "🌍"
                                    uiState.searchRadiusKm >= 1000f -> "🌎"
                                    uiState.searchRadiusKm >= 500f -> "🗺️"
                                    uiState.searchRadiusKm >= 250f -> "📍"
                                    else -> if (uiState.useKilometers) "${uiState.searchRadiusKm.toInt()} km" else "${(uiState.searchRadiusKm * 0.621371f).toInt()} mi"
                                }
                                Text(
                                    text = displayValue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            radiusOptions.forEach { radius ->
                                DropdownMenuItem(
                                    text = {
                                        val displayText = when {
                                            radius >= 5000f -> "🌍 Worldwide"
                                            radius >= 1000f -> "🌎 Country"
                                            radius >= 500f -> "🗺️ Region"
                                            radius >= 250f -> "📍 State/Province"
                                            radius >= 100f -> "🏙️ ${if (uiState.useKilometers) "${radius.toInt()} km" else "${(radius * 0.621371f).toInt()} mi"}"
                                            else -> if (uiState.useKilometers) "${radius.toInt()} km" else "${(radius * 0.621371f).toInt()} mi"
                                        }
                                        Text(displayText)
                                    },
                                    onClick = {
                                        viewModel.setSearchRadius(radius)
                                        expanded = false
                                    }
                                )
                            }
                            Divider()
                            DropdownMenuItem(
                                text = { 
                                    Text(if (uiState.useKilometers) "Switch to Miles" else "Switch to Kilometers")
                                },
                                onClick = {
                                    viewModel.toggleUnits()
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Location search for larger areas
            if (uiState.searchRadiusKm >= 100f || uiState.targetLocation != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.TravelExplore,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    if (uiState.targetLocationName != null) {
                        // Show current target location
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "📍 ${uiState.targetLocationName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Showing places within 50km",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.clearTargetLocation() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Location search input - more prominent prompt
                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = uiState.locationSearchQuery,
                                onValueChange = { viewModel.searchLocations(it) },
                                placeholder = { 
                                    Text(
                                        "🌍 Search city, country to explore...",
                                        fontSize = 13.sp
                                    ) 
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodySmall,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = Primary.copy(alpha = 0.5f)
                                ),
                                trailingIcon = {
                                    if (uiState.locationSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.clearLocationSearch() }) {
                                            Icon(Icons.Default.Clear, "Clear")
                                        }
                                    } else {
                                        Icon(
                                            Icons.Default.Search,
                                            "Search",
                                            tint = Primary
                                        )
                                    }
                                }
                            )
                            Text(
                                text = "ℹ️ Enter a location to explore places there",
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }
                    }
                }
                
                // Location search results
                if (uiState.locationSearchResults.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shadowElevation = 4.dp,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column {
                            uiState.locationSearchResults.take(5).forEach { suggestion ->
                                Surface(
                                    onClick = { viewModel.selectTargetLocation(suggestion) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = suggestion.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = suggestion.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                if (suggestion != uiState.locationSearchResults.take(5).last()) {
                                    Divider()
                                }
                            }
                        }
                    }
                }
            }
                            } // End collapsible filters AnimatedVisibility Column
                        } // End collapsible filters AnimatedVisibility
                    } // End controls Column
                } // End fullscreen AnimatedVisibility
            
            // Search results or map
            if (showSearchBar && uiState.searchQuery.isNotEmpty()) {
                SearchResultsList(
                    results = uiState.searchResults,
                    isLoading = uiState.isSearching,
                    onPlaceClick = { viewModel.selectPlace(it) },
                    getPhotoUrl = { viewModel.getPhotoUrl(it) }
                )
            } else {
                // Log places count for debugging
                LaunchedEffect(uiState.nearbyPlaces.size, uiState.selectedCategory) {
                    android.util.Log.d("ExploreScreen", "Places count: ${uiState.nearbyPlaces.size}, category: ${uiState.selectedCategory}")
                }
                
                // Auto-load places when map camera stops moving
                LaunchedEffect(cameraPositionState.isMoving) {
                    if (!cameraPositionState.isMoving && uiState.autoShowPlacesOnMap) {
                        // Camera stopped moving - load places at this location
                        val center = cameraPositionState.position.target
                        val zoom = cameraPositionState.position.zoom
                        viewModel.loadPlacesAtMapCenter(center, zoom)
                    }
                }
                
                // Map with places
                Box(modifier = Modifier.weight(1f)) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(
                            isMyLocationEnabled = hasLocationPermission,
                            mapType = mapType
                        ),
                        uiSettings = MapUiSettings(
                            myLocationButtonEnabled = hasLocationPermission,
                            zoomControlsEnabled = false
                        )
                    ) {
                        // "You are here" marker at user location - updates in real-time
                        val userMarkerState = rememberMarkerState(position = uiState.currentLocation)
                        LaunchedEffect(uiState.currentLocation) {
                            userMarkerState.position = uiState.currentLocation
                        }
                        MarkerComposable(
                            state = userMarkerState,
                            zIndex = 200f
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    color = Color(0xFF1976D2),
                                    shape = RoundedCornerShape(8.dp),
                                    shadowElevation = 6.dp
                                ) {
                                    Text(
                                        text = "📍 You are here",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF1976D2),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        // Place markers - hide when Island Deals is selected
                        if (uiState.selectedCategory != PlaceCategory.ISLAND_DEALS) {
                            uiState.nearbyPlaces.forEach { place ->
                                // Only show markers with valid coordinates
                                if (place.latLng.latitude != 0.0 || place.latLng.longitude != 0.0) {
                                    val categoryEmoji = getCategoryEmoji(place)
                                    
                                    key(place.placeId) {
                                        MarkerComposable(
                                            state = rememberMarkerState(position = place.latLng),
                                            zIndex = 50f,
                                            onClick = {
                                                viewModel.selectPlace(place)
                                                true
                                            }
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Surface(
                                                    color = Primary,
                                                    shape = RoundedCornerShape(6.dp),
                                                    shadowElevation = 4.dp
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = categoryEmoji,
                                                            fontSize = 10.sp
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = place.name.take(15) + if (place.name.length > 15) "…" else "",
                                                            color = Color.White,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                                // Small triangle pointer
                                                Icon(
                                                    Icons.Default.Place,
                                                    contentDescription = null,
                                                    tint = Primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Advertisement markers on map - only show for ALL or ISLAND_DEALS categories
                        if (uiState.selectedCategory == PlaceCategory.ALL || uiState.selectedCategory == PlaceCategory.ISLAND_DEALS) {
                            uiState.advertisements.forEach { ad ->
                                ad.location?.let { loc ->
                                    val adPosition = LatLng(loc.latitude, loc.longitude)
                                    
                                    // Get logo URL
                                    val logoUrl = ad.logoUrl ?: ad.imageUrl
                                    val hasLogo = !logoUrl.isNullOrBlank()
                                    
                                    // Preload image using painter
                                    val imageModel = remember(logoUrl) {
                                        if (logoUrl != null && logoUrl.startsWith("/")) java.io.File(logoUrl) else logoUrl
                                    }
                                    
                                    val painter = rememberAsyncImagePainter(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(imageModel)
                                            .size(Size.ORIGINAL)
                                            .crossfade(false)
                                            .allowHardware(false)
                                            .build()
                                    )
                                    val painterState = painter.state
                                    val isImageLoaded = painterState is AsyncImagePainter.State.Success
                                    
                                    val markerKey = remember(ad.id, isImageLoaded) { "${ad.id}_$isImageLoaded" }
                                    
                                    key(markerKey) {
                                        MarkerComposable(
                                            state = rememberMarkerState(position = adPosition),
                                            zIndex = 100f,
                                            onClick = {
                                            viewModel.selectAd(ad)
                                            true
                                        }
                                    ) {
                                        // Custom marker with logo - compact version
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            // Marker card with logo and info
                                            Surface(
                                                color = if (ad.hasCoupon) Color(0xFF4CAF50) else Primary,
                                                shape = RoundedCornerShape(8.dp),
                                                shadowElevation = 4.dp
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Logo image - smaller
                                                    Surface(
                                                        modifier = Modifier.size(24.dp),
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color.White
                                                    ) {
                                                        Box(
                                                            contentAlignment = Alignment.Center,
                                                            modifier = Modifier.fillMaxSize()
                                                        ) {
                                                            if (hasLogo && isImageLoaded) {
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
                                                                    fontSize = 14.sp
                                                                )
                                                            }
                                                        }
                                                    }
                                                    
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    
                                                    Column {
                                                        // Business name - smaller
                                                        Text(
                                                            text = ad.businessName,
                                                            color = Color.White,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            maxLines = 1
                                                        )
                                                        // Deal/Coupon below name
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
                                                                fontSize = 7.sp,
                                                                maxLines = 1
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            // Arrow pointing down - smaller
                                            Icon(
                                                Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = if (ad.hasCoupon) Color(0xFF4CAF50) else Primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        }
                    }
                    
                    // Loading indicator
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(48.dp)
                        )
                    }
                    
                    // Map type selector button
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 8.dp, top = 8.dp)
                    ) {
                        Surface(
                            onClick = { showMapTypeMenu = !showMapTypeMenu },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Layers,
                                    contentDescription = "Map type",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (mapType) {
                                        MapType.NORMAL -> "Map"
                                        MapType.SATELLITE -> "Satellite"
                                        MapType.TERRAIN -> "Terrain"
                                        MapType.HYBRID -> "Hybrid"
                                        else -> "Map"
                                    },
                                    fontSize = 12.sp
                                )
                            }
                        }
                        
                        DropdownMenu(
                            expanded = showMapTypeMenu,
                            onDismissRequest = { showMapTypeMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("🗺️ Map") },
                                onClick = {
                                    mapType = MapType.NORMAL
                                    showMapTypeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🛰️ Satellite") },
                                onClick = {
                                    mapType = MapType.SATELLITE
                                    showMapTypeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🏔️ Terrain") },
                                onClick = {
                                    mapType = MapType.TERRAIN
                                    showMapTypeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("📡 Hybrid") },
                                onClick = {
                                    mapType = MapType.HYBRID
                                    showMapTypeMenu = false
                                }
                            )
                        }
                    }
                }
                
                // Bottom places list (horizontal scrollable) - or deals list if Island Deals selected
                // Or news panel if News category selected
                if (uiState.selectedCategory == PlaceCategory.NEWS) {
                    ExploreNewsPanel(
                        weather = uiState.weather,
                        forecast = uiState.weatherForecast,
                        articles = uiState.newsArticles,
                        isLoading = uiState.isLoadingNews,
                        onRefresh = { viewModel.refreshNews() }
                    )
                } else if (uiState.selectedCategory == PlaceCategory.ISLAND_DEALS && uiState.advertisements.isNotEmpty()) {
                    DealsHorizontalList(
                        ads = uiState.advertisements,
                        currentLocation = uiState.currentLocation,
                        onAdClick = { ad ->
                            viewModel.selectAd(ad)
                            // Move camera to deal location
                            ad.location?.let { loc ->
                                coroutineScope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 17f)
                                    )
                                }
                            }
                        }
                    )
                } else if (uiState.nearbyPlaces.isNotEmpty()) {
                    PlacesHorizontalList(
                        places = uiState.nearbyPlaces, // Show all places, no limit
                        currentLocation = uiState.currentLocation,
                        onPlaceClick = { place ->
                            viewModel.selectPlace(place)
                            // Move camera to place
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(place.latLng, 15f)
                                )
                            }
                        },
                        getPhotoUrl = { viewModel.getPhotoUrl(it) }
                    )
                } else if (uiState.isLoading) {
                    // Loading indicator
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Loading places...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else if (uiState.selectedCategory != PlaceCategory.ISLAND_DEALS) {
                    // No places found message
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (uiState.searchRadiusKm >= 100f && uiState.targetLocation == null) {
                                Text(
                                    text = "🌍 Search for a location above",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Enter a city or country to explore places there",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    text = "No places found nearby",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try a different category or location",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
            } // End Column
            
            // Fullscreen mode overlay controls
            AnimatedVisibility(
                visible = fullscreenMode,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                // Floating buttons in fullscreen mode
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Exit fullscreen
                    IconButton(onClick = { fullscreenMode = false }) {
                        Icon(Icons.Default.FullscreenExit, "Exit fullscreen")
                    }
                    // Back button
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                    // My location
                    IconButton(onClick = {
                        viewModel.refreshLocation()
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(300)
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(uiState.currentLocation, 19f)
                            )
                        }
                    }) {
                        Icon(Icons.Default.MyLocation, "My Location")
                    }
                }
            }
            
            // Category dropdown in fullscreen mode (compact)
            AnimatedVisibility(
                visible = fullscreenMode,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                var categoryExpanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Surface(
                        onClick = { categoryExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${uiState.selectedCategory.icon} ${uiState.selectedCategory.displayName}",
                                fontWeight = FontWeight.Medium
                            )
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                    }
                    
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        PlaceCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = { Text("${category.icon} ${category.displayName}") },
                                onClick = {
                                    viewModel.setCategory(category)
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Real-time Nearby Discovery Popup
            AnimatedVisibility(
                visible = uiState.nearbyDiscovery != null && uiState.isDiscoveryEnabled,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                uiState.nearbyDiscovery?.let { discovery ->
                    NearbyDiscoveryPopup(
                        discovery = discovery,
                        onDismiss = { viewModel.dismissDiscovery() },
                        onViewDetails = { 
                            when (discovery) {
                                is NearbyDiscovery.DealDiscovery -> {
                                    viewModel.selectAd(discovery.ad)
                                }
                                is NearbyDiscovery.PlaceDiscovery -> {
                                    viewModel.selectPlace(discovery.place)
                                }
                            }
                        },
                        getPhotoUrl = { photoRef -> viewModel.getPhotoUrl(photoRef) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryChips(
    selectedCategory: PlaceCategory,
    onCategorySelected: (PlaceCategory) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        items(PlaceCategory.values().toList()) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = { Text("${category.icon} ${category.displayName}", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeFilterChips(
    selectedFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TimeFilter.values().forEach { filter ->
            FilterChip(
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) },
                label = { 
                    Text(
                        when (filter) {
                            TimeFilter.ALL -> "🕐 All"
                            TimeFilter.DAY -> "☀️ Day"
                            TimeFilter.NIGHT -> "🌙 Night"
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SearchResultsList(
    results: List<NearbyPlace>,
    isLoading: Boolean,
    onPlaceClick: (NearbyPlace) -> Unit,
    getPhotoUrl: (String) -> String
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (results.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.SearchOff,
                    null,
                    modifier = Modifier.size(64.dp),
                    tint = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("No results found", color = TextSecondary)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(results) { place ->
                PlaceListItem(
                    place = place,
                    onClick = { onPlaceClick(place) },
                    getPhotoUrl = getPhotoUrl
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceListItem(
    place: NearbyPlace,
    onClick: () -> Unit,
    getPhotoUrl: (String) -> String
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo
            if (place.photoReference != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(getPhotoUrl(place.photoReference))
                        .crossfade(true)
                        .build(),
                    contentDescription = place.name,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Place, null, tint = TextSecondary)
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.name,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (place.address.isNotBlank()) {
                    Text(
                        text = place.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Rating
                    if (place.rating != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                null,
                                tint = Warning,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = String.format("%.1f", place.rating),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            if (place.userRatingsTotal != null) {
                                Text(
                                    text = "(${place.userRatingsTotal})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                    
                    // Open/Closed
                    place.isOpenNow?.let { isOpen ->
                        Text(
                            text = if (isOpen) "Open" else "Closed",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isOpen) Success else Error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Price level
                    if (place.priceLevel != null && place.priceLevel > 0) {
                        Text(
                            text = "$".repeat(place.priceLevel),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = TextSecondary
            )
        }
    }
}

@Composable
private fun PlacesHorizontalList(
    places: List<NearbyPlace>,
    currentLocation: LatLng,
    onPlaceClick: (NearbyPlace) -> Unit,
    getPhotoUrl: (String) -> String
) {
    Column {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(places) { place ->
                PlaceCard(
                    place = place,
                    currentLocation = currentLocation,
                    onClick = { onPlaceClick(place) },
                    getPhotoUrl = getPhotoUrl
                )
            }
        }
    }
}

@Composable
private fun DealsHorizontalList(
    ads: List<Advertisement>,
    currentLocation: LatLng,
    onAdClick: (Advertisement) -> Unit
) {
    Column {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ads) { ad ->
                DealCard(
                    ad = ad,
                    currentLocation = currentLocation,
                    onClick = { onAdClick(ad) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DealCard(
    ad: Advertisement,
    currentLocation: LatLng,
    onClick: () -> Unit
) {
    // Calculate distance if location available
    val distanceInfo = remember(ad.location, currentLocation) {
        ad.location?.let { loc ->
            val results = FloatArray(1)
            Location.distanceBetween(
                currentLocation.latitude, currentLocation.longitude,
                loc.latitude, loc.longitude,
                results
            )
            val distanceMeters = results[0]
            val distanceKm = distanceMeters / 1000f
            val distanceMiles = distanceMeters / 1609.34f
            Triple(distanceMeters, distanceKm, distanceMiles)
        }
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (ad.hasCoupon) Color(0xFF4CAF50).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Logo/Image - show full image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp, max = 120.dp)
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                val logoUrl = ad.logoUrl ?: ad.imageUrl
                if (!logoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(if (logoUrl.startsWith("/")) java.io.File(logoUrl) else logoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = ad.businessName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = "🎟️",
                        fontSize = 28.sp
                    )
                }
            }
            
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = ad.businessName ?: "Island Deal",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (ad.hasCoupon && ad.couponCode != null) {
                    Surface(
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = ad.couponCode,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                // Distance display
                distanceInfo?.let { (distanceMeters, distanceKm, distanceMiles) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.NearMe,
                            null,
                            tint = Primary,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = if (distanceKm < 1) {
                                "${distanceMeters.toInt()}m"
                            } else if (distanceKm < 10) {
                                String.format("%.1f km \u2022 %.1f mi", distanceKm, distanceMiles)
                            } else {
                                String.format("%.0f km \u2022 %.0f mi", distanceKm, distanceMiles)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceCard(
    place: NearbyPlace,
    currentLocation: LatLng,
    onClick: () -> Unit,
    getPhotoUrl: (String) -> String
) {
    // Calculate distance
    val distanceMeters = remember(place.latLng, currentLocation) {
        val results = FloatArray(1)
        Location.distanceBetween(
            currentLocation.latitude, currentLocation.longitude,
            place.latLng.latitude, place.latLng.longitude,
            results
        )
        results[0]
    }
    val distanceKm = distanceMeters / 1000f
    val distanceMiles = distanceMeters / 1609.34f
    
    Card(
        onClick = onClick,
        modifier = Modifier.width(140.dp)
    ) {
        Column {
            // Photo
            if (place.photoReference != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(getPhotoUrl(place.photoReference))
                        .crossfade(true)
                        .build(),
                    contentDescription = place.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Place,
                        null,
                        modifier = Modifier.size(32.dp),
                        tint = TextSecondary
                    )
                }
            }
            
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Show description/address if available (especially for docks)
                val description = place.vicinity ?: place.address
                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 9.sp
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (place.rating != null) {
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = Warning,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = String.format("%.1f", place.rating),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    
                    place.isOpenNow?.let { isOpen ->
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (isOpen) Success else Error)
                        )
                    }
                }
                
                // Distance display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        Icons.Default.NearMe,
                        null,
                        tint = Primary,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = if (distanceKm < 1) {
                            "${(distanceMeters).toInt()}m"
                        } else if (distanceKm < 10) {
                            String.format("%.1f km \u2022 %.1f mi", distanceKm, distanceMiles)
                        } else {
                            String.format("%.0f km \u2022 %.0f mi", distanceKm, distanceMiles)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceDetailsSheet(
    place: NearbyPlace,
    onShowOnMap: (NearbyPlace) -> Unit,
    onCall: (String) -> Unit,
    onWebsite: (String) -> Unit,
    onDirections: (NearbyPlace) -> Unit,
    getPhotoUrl: (String) -> String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Photo
        if (place.photoReference != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(getPhotoUrl(place.photoReference))
                    .crossfade(true)
                    .build(),
                contentDescription = place.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Name
        Text(
            text = place.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        // Description/Address (especially for docks)
        val description = place.vicinity ?: place.address
        if (description.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Rating and status row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (place.rating != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Warning, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f", place.rating),
                        fontWeight = FontWeight.SemiBold
                    )
                    if (place.userRatingsTotal != null) {
                        Text(
                            text = " (${place.userRatingsTotal} reviews)",
                            color = TextSecondary
                        )
                    }
                }
            }
            
            place.isOpenNow?.let { isOpen ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isOpen) Success else Error)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isOpen) "Open Now" else "Closed",
                        color = if (isOpen) Success else Error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            if (place.priceLevel != null && place.priceLevel > 0) {
                Text(
                    text = "$".repeat(place.priceLevel),
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Address
        if (place.address.isNotBlank()) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.LocationOn,
                    null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = place.address, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Phone
        if (!place.phoneNumber.isNullOrBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onCall(place.phoneNumber) }
            ) {
                Icon(
                    Icons.Default.Phone,
                    null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = place.phoneNumber, color = Primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Website
        if (!place.website.isNullOrBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onWebsite(place.website) }
            ) {
                Icon(
                    Icons.Default.Language,
                    null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Visit Website",
                    color = Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Opening hours
        if (!place.openingHours.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Opening Hours",
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            place.openingHours.forEach { hours ->
                Text(
                    text = hours,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { onDirections(place) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Directions, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Directions")
            }
            
            Button(
                onClick = { onShowOnMap(place) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.NearMe, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Show on Map")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DealDetailsSheet(
    ad: Advertisement,
    onNavigateTo: () -> Unit,
    onCall: (String) -> Unit,
    onWebsite: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Image - show full image without cropping
        val imageUrl = ad.imageUrl ?: ad.logoUrl
        if (!imageUrl.isNullOrBlank()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                color = Color(0xFFF5F5F5)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(if (imageUrl.startsWith("/")) java.io.File(imageUrl) else imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = ad.businessName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp, max = 300.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Business name
        Text(
            text = ad.businessName ?: "Island Deal",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Deal/Coupon badge
        if (ad.hasCoupon && ad.couponCode != null) {
            Surface(
                color = Color(0xFF4CAF50),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎟️",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "COUPON CODE",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = ad.couponCode,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // Description
        if (ad.description.isNotBlank()) {
            Text(
                text = ad.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // Contact info
        if (!ad.phoneNumber.isNullOrBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onCall(ad.phoneNumber) }
            ) {
                Icon(
                    Icons.Default.Phone,
                    null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = ad.phoneNumber, color = Primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        if (!ad.websiteUrl.isNullOrBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onWebsite(ad.websiteUrl) }
            ) {
                Icon(
                    Icons.Default.Language,
                    null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Visit Website",
                    color = Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (ad.location != null) {
                OutlinedButton(
                    onClick = onNavigateTo,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.NearMe, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View on Map")
                }
            }
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Close")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Featured Ads Carousel
 */
@Composable
private fun AdsCarousel(
    ads: List<Advertisement>,
    onAdClick: (Advertisement) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Featured",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Sponsored",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(ads) { ad -> // Show all ads, no limit
                AdCard(
                    ad = ad,
                    onClick = { onAdClick(ad) }
                )
            }
        }
    }
}

/**
 * Individual Ad Card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdCard(
    ad: Advertisement,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(150.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            // Ad Image with proper loading/error states
            if (ad.imageUrl != null) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(ad.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = ad.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Primary
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ad.category.getIcon(),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    },
                    success = { SubcomposeAsyncImageContent() }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = TextSecondary
                    )
                }
            }
            
            // Gradient overlay with text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 50f
                        )
                    )
            )
            
            // Ad info at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = ad.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (ad.businessName != null) {
                    Text(
                        text = ad.businessName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1
                    )
                }
            }
            
            // "Ad" label
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                shape = RoundedCornerShape(4.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "AD",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Get marker color based on place category
 */
private fun getMarkerColor(place: NearbyPlace): Float {
    return when {
        place.types.any { it in listOf("restaurant", "food", "cafe") } -> BitmapDescriptorFactory.HUE_ORANGE
        place.types.any { it in listOf("bar", "night_club") } -> BitmapDescriptorFactory.HUE_VIOLET
        place.types.any { it in listOf("lodging", "hotel") } -> BitmapDescriptorFactory.HUE_BLUE
        place.types.any { it in listOf("tourist_attraction", "museum") } -> BitmapDescriptorFactory.HUE_GREEN
        place.types.any { it in listOf("shopping_mall", "store") } -> BitmapDescriptorFactory.HUE_YELLOW
        else -> BitmapDescriptorFactory.HUE_RED
    }
}

/**
 * Get category emoji for place marker
 */
private fun getCategoryEmoji(place: NearbyPlace): String {
    return when {
        place.types.any { it in listOf("dock", "marina", "ferry", "port") } -> "⚓"
        place.types.any { it in listOf("restaurant", "food", "cafe", "bakery") } -> "🍽️"
        place.types.any { it in listOf("bar", "night_club", "casino") } -> "🍸"
        place.types.any { it in listOf("lodging", "hotel", "resort") } -> "🏨"
        place.types.any { it in listOf("tourist_attraction", "museum", "amusement_park") } -> "🎢"
        place.types.any { it in listOf("shopping_mall", "store", "market") } -> "🛍️"
        place.types.any { it in listOf("travel_agency") } -> "🚤"
        place.types.any { it in listOf("park", "campground") } -> "🌴"
        place.types.any { it in listOf("spa", "gym", "beauty_salon", "hair_care") } -> "💆"
        place.types.any { it in listOf("natural_feature", "beach") } -> "🏖️"
        else -> "📍"
    }
}

/**
 * Build snippet text for marker
 */
private fun buildMarkerSnippet(place: NearbyPlace): String {
    val parts = mutableListOf<String>()
    
    place.rating?.let { parts.add("★ ${String.format("%.1f", it)}") }
    place.isOpenNow?.let { parts.add(if (it) "Open" else "Closed") }
    place.priceLevel?.let { if (it > 0) parts.add("$".repeat(it)) }
    
    return parts.joinToString(" • ")
}

/**
 * Island Deals List - shows all advertisements as a vertical list
 */
@Composable
private fun IslandDealsList(
    ads: List<Advertisement>,
    onAdClick: (Advertisement) -> Unit
) {
    if (ads.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎟️", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Island Deals Available",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Check back later for local deals and offers!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ads) { ad ->
                IslandDealCard(
                    ad = ad,
                    onClick = { onAdClick(ad) }
                )
            }
        }
    }
}

/**
 * Individual Island Deal Card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IslandDealCard(
    ad: Advertisement,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (ad.hasCoupon) Color(0xFFE8F5E9) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
            ) {
                val logoUrl = ad.logoUrl ?: ad.imageUrl
                if (!logoUrl.isNullOrBlank()) {
                    val imageModel = remember(logoUrl) {
                        if (logoUrl.startsWith("/")) java.io.File(logoUrl) else logoUrl
                    }
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageModel)
                            .crossfade(true)
                            .build(),
                        contentDescription = ad.businessName,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        contentScale = ContentScale.Fit
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
            
            // Deal info
            Column(modifier = Modifier.weight(1f)) {
                // Business name
                Text(
                    text = ad.businessName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Deal/Coupon text
                val dealText = when {
                    !ad.couponDiscount.isNullOrBlank() -> "🎟️ ${ad.couponDiscount}"
                    ad.hasCoupon && !ad.title.isBlank() -> "🎟️ ${ad.title}"
                    !ad.title.isBlank() && ad.title != ad.businessName -> ad.title
                    else -> ad.description.take(50)
                }
                if (dealText.isNotBlank()) {
                    Text(
                        text = dealText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (ad.hasCoupon) Color(0xFF2E7D32) else TextSecondary,
                        fontWeight = if (ad.hasCoupon) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Location if available
                ad.locationName?.let { location ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = location,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
            
            // Coupon badge
            if (ad.hasCoupon) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF4CAF50)
                ) {
                    Text(
                        text = "DEAL",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
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

/**
 * Real-time Nearby Discovery Popup
 */
@Composable
private fun NearbyDiscoveryPopup(
    discovery: NearbyDiscovery,
    onDismiss: () -> Unit,
    onViewDetails: () -> Unit,
    getPhotoUrl: (String) -> String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (discovery) {
                is NearbyDiscovery.DealDiscovery -> Color(0xFF4CAF50).copy(alpha = 0.95f)
                is NearbyDiscovery.PlaceDiscovery -> Primary.copy(alpha = 0.95f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with distance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (discovery) {
                            is NearbyDiscovery.DealDiscovery -> "🎁 Deal Right Here!"
                            is NearbyDiscovery.PlaceDiscovery -> "📍 Right Next To You!"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = when (discovery) {
                                is NearbyDiscovery.DealDiscovery -> "${discovery.distanceMeters}m"
                                is NearbyDiscovery.PlaceDiscovery -> "${discovery.distanceMeters}m"
                            },
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Content based on type
            when (discovery) {
                is NearbyDiscovery.DealDiscovery -> {
                    val ad = discovery.ad
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Logo
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White
                        ) {
                            val logoUrl = ad.logoUrl ?: ad.imageUrl
                            if (!logoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(if (logoUrl.startsWith("/")) java.io.File(logoUrl) else logoUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text("🎟️", fontSize = 24.sp)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ad.businessName ?: "Island Deal",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (ad.hasCoupon && ad.couponCode != null) {
                                Text(
                                    text = "Use code: ${ad.couponCode}",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
                
                is NearbyDiscovery.PlaceDiscovery -> {
                    val place = discovery.place
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Photo or icon
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            if (place.photoReference != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(getPhotoUrl(place.photoReference))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(getCategoryEmoji(place), fontSize = 24.sp)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = place.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (place.rating != null) {
                                    Icon(
                                        Icons.Default.Star,
                                        null,
                                        tint = Warning,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = " ${String.format("%.1f", place.rating)}",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                place.isOpenNow?.let { isOpen ->
                                    Text(
                                        text = if (isOpen) "• Open" else "• Closed",
                                        color = if (isOpen) Color(0xFF81C784) else Color(0xFFE57373),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action button
            Button(
                onClick = onViewDetails,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = when (discovery) {
                        is NearbyDiscovery.DealDiscovery -> Color(0xFF4CAF50)
                        is NearbyDiscovery.PlaceDiscovery -> Primary
                    }
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Default.NearMe,
                    null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "View Details",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ExploreNewsPanel(
    weather: WeatherData?,
    forecast: List<WeatherForecast>,
    articles: List<NewsArticle>,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 350.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            // Header with refresh button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📰 News & Weather",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Compact Weather Card
            weather?.let { w ->
                CompactWeatherCard(weather = w, forecast = forecast)
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Loading indicator
            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Primary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Loading news...", style = MaterialTheme.typography.bodySmall)
                }
            }
            
            // News Articles
            if (articles.isNotEmpty()) {
                Text(
                    text = "Latest Headlines",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                articles.take(5).forEach { article ->
                    CompactNewsItem(
                        article = article,
                        onClick = {
                            try {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse(article.link)
                                )
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Handle error
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun CompactWeatherCard(
    weather: WeatherData,
    forecast: List<WeatherForecast>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E88E5)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Current weather row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bocas del Toro",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = weather.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = weather.condition.icon,
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${weather.temperature.toInt()}°C",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            // Mini forecast row
            if (forecast.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    forecast.take(4).forEachIndexed { index, day ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (index == 0) "Now" else {
                                    val sdf = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
                                    sdf.format(java.util.Date(day.date))
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(text = day.condition.icon, fontSize = 18.sp)
                            Text(
                                text = "${day.tempMax.toInt()}°",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactNewsItem(
    article: NewsArticle,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Source icon
            Text(
                text = article.source.getIconEmoji(),
                fontSize = 24.sp
            )
            
            Column(modifier = Modifier.weight(1f)) {
                // Source name
                Text(
                    text = article.source.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    fontWeight = FontWeight.SemiBold
                )
                // Title
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
