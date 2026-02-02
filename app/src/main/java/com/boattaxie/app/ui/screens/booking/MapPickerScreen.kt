package com.boattaxie.app.ui.screens.booking

import android.location.Geocoder
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.boattaxie.app.ui.theme.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    isPickup: Boolean,
    onLocationSelected: (placeId: String, address: String, lat: Double, lng: Double) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Default to Panama City or Bocas del Toro
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedAddress by remember { mutableStateOf<String?>(null) }
    var isLoadingAddress by remember { mutableStateOf(false) }
    
    // Camera starts at Bocas del Toro (common water taxi area)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(9.3403, -82.2419), // Bocas Town
            13f
        )
    }
    
    // Geocoder for reverse geocoding
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }
    
    // Function to get address from coordinates
    fun getAddressFromLocation(latLng: LatLng) {
        isLoadingAddress = true
        scope.launch {
            try {
                val addresses = withContext(Dispatchers.IO) {
                    try {
                        geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                    } catch (e: Exception) {
                        null
                    }
                }
                
                selectedAddress = if (!addresses.isNullOrEmpty()) {
                    addresses[0].getAddressLine(0) ?: "${latLng.latitude}, ${latLng.longitude}"
                } else {
                    "Location: ${String.format("%.4f", latLng.latitude)}, ${String.format("%.4f", latLng.longitude)}"
                }
            } catch (e: Exception) {
                selectedAddress = "Location: ${String.format("%.4f", latLng.latitude)}, ${String.format("%.4f", latLng.longitude)}"
            }
            isLoadingAddress = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (isPickup) "Choose Pickup Location" else "Choose Destination") 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
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
                properties = MapProperties(
                    isMyLocationEnabled = true
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = true,
                    zoomControlsEnabled = true
                ),
                onMapClick = { latLng ->
                    selectedLocation = latLng
                    getAddressFromLocation(latLng)
                }
            ) {
                // Show marker at selected location
                selectedLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = if (isPickup) "Pickup" else "Destination",
                        snippet = selectedAddress
                    )
                }
            }
            
            // Instructions at top
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.TouchApp,
                        null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Tap on the map to select a location",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Bottom card with selected location and confirm button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (selectedLocation != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                null,
                                tint = if (isPickup) Success else Error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isPickup) "Pickup Location" else "Destination",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary
                                )
                                if (isLoadingAddress) {
                                    Text(
                                        text = "Getting address...",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                } else {
                                    Text(
                                        text = selectedAddress ?: "Tap map to select",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                selectedLocation?.let { loc ->
                                    val address = selectedAddress ?: "Selected Location"
                                    val placeId = "map_${loc.latitude}_${loc.longitude}"
                                    onLocationSelected(placeId, address, loc.latitude, loc.longitude)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = selectedLocation != null && !isLoadingAddress
                        ) {
                            Icon(Icons.Default.Check, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirm Location")
                        }
                    } else {
                        Text(
                            text = "Tap anywhere on the map to select your ${if (isPickup) "pickup" else "destination"} location",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = TextSecondary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            // Quick location buttons
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp, end = 16.dp)
            ) {
                // Jump to Bocas del Toro
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(9.3403, -82.2419), 13f
                                )
                            )
                        }
                    },
                    containerColor = BoatColor,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.DirectionsBoat,
                        "Bocas del Toro",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Jump to Panama City
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(8.9824, -79.5199), 12f
                                )
                            )
                        }
                    },
                    containerColor = TaxiColor,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.LocationCity,
                        "Panama City",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
