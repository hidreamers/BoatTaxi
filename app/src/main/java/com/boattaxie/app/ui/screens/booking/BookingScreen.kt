package com.boattaxie.app.ui.screens.booking

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
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
import kotlinx.coroutines.tasks.await

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
    onNavigateToRateRide: (String) -> Unit = {},
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
    
    // Places Search Dialog state
    var showPlacesSearchDialog by remember { mutableStateOf(false) }
    var placesSearchQuery by remember { mutableStateOf("") }
    var selectedPlaceCategory by remember { mutableStateOf("All") }
    
    // Place categories for search
    val placeCategories = listOf("All", "Docks", "Restaurants", "Hotels", "Resorts", "Islands", "Beaches", "Dolphin Sites", "Island Hopping", "Tours")
    
    // Dock locations state
    var showDockLocationsDialog by remember { mutableStateOf(false) }
    var showDocksOnMap by remember { mutableStateOf(true) } // Always show docks by default
    var showLocationError by remember { mutableStateOf<String?>(null) }
    
    // Island Deals (ads) visibility - hidden by default, with flashing animation to attract attention
    var showIslandDeals by remember { mutableStateOf(false) }
    
    // Boat dock locations in Bocas del Toro - Verified with Google Maps
    val dockLocations = listOf(
        // Bocas Town - Main docks (verified)
        "Main Water Taxi Dock" to LatLng(9.3387625, -82.2402344),
        "Bocas Docks" to LatLng(9.3433664, -82.2424937),
        "Water Taxi Dock (Lanchas)" to LatLng(9.3367882, -82.2429174),
        "Watertaxi Stop" to LatLng(9.3399387, -82.239166),
        "Isla Colon Water Taxi" to LatLng(9.3388934, -82.2396447),
        "Transporte Maritimo Valencia" to LatLng(9.3384523, -82.2402878),
        "Palanga Ferry Terminal" to LatLng(9.3353841, -82.2410291),
        "Bocas Yacht Club & Marina" to LatLng(9.3349824, -82.2468556),
        "Bambuda Bocas Town" to LatLng(9.3433, -82.2429),
        "Bocas Airport (BOC)" to LatLng(9.34083, -82.25),
        "Dolphin Bay" to LatLng(9.34326, -82.25188),
        
        // Bocas Town - Hotels with Docks
        "Hotel Palma Royale" to LatLng(9.33504, -82.24169),
        "Gran Hotel Bahía" to LatLng(9.33913, -82.23797),
        "Hotel CalaLuna" to LatLng(9.34276, -82.23871),
        "Hotel Bocas del Toro" to LatLng(9.33938, -82.24258),
        "Hostel on the Sea" to LatLng(9.3437, -82.2437),
        "Lil Spa Shop / United Reality" to LatLng(9.3432, -82.2443),
        
        // Carenero Island docks (verified)
        "Carenero Dock" to LatLng(9.3424, -82.2348),
        "Marina Carenero" to LatLng(9.3424036, -82.235247),
        "Bibi's on the Beach" to LatLng(9.3406077, -82.2313467),
        "Coquitos Pizza" to LatLng(9.3401557, -82.2319386),
        "Hotel Tierra Verde" to LatLng(9.3392473, -82.234033),
        "Faro del Colibri" to LatLng(9.33776, -82.23524),
        "Buccaneer Resort" to LatLng(9.3401283, -82.2320573),
        "Cosmic Crab Resort" to LatLng(9.3436494, -82.2355459),
        
        // Bastimentos Island docks (verified)
        "Bastimentos Boat Dock (Bocas)" to LatLng(9.3372843, -82.240895),
        "Old Bank Dock (Bastimentos)" to LatLng(9.34782, -82.2111463),
        "Red Frog Marina" to LatLng(9.3365993, -82.1774826),
        "Casa Cayuco Lodge" to LatLng(9.2918901, -82.0878826),
        "Palmar Beach Lodge" to LatLng(9.3436695, -82.1787023),
        
        // Almirante ferries (verified)
        "Ferry Bocas (Almirante)" to LatLng(9.2893377, -82.3932207),
        "Ferry Palanga (Almirante)" to LatLng(9.2892706, -82.3930836),
        
        // Resorts with docks (verified)
        "Punta Caracol Dock" to LatLng(9.3424986, -82.2518131),
        "Azul Paradise" to LatLng(9.2886696, -82.1152572),
        "Tranquilo Bay" to LatLng(9.2554756, -82.1453889),
        "Dolphin Bay Hideaway" to LatLng(9.2413134, -82.2581001),
        
        // Isla Solarte - Verified with Google Maps Feb 2026
        "Solarte EcoLodge" to LatLng(9.315224, -82.1816718),
        "Akwaba Lodge" to LatLng(9.320453, -82.188827),
        "Los Secretos Guesthouse" to LatLng(9.3101702, -82.171318),
        "Roam Yoga & Wellness" to LatLng(9.3263929, -82.2074944),
        "Las Casitas del Perezoso" to LatLng(9.3224699, -82.1980979),
        "La Purita Ecolodge" to LatLng(9.3128596, -82.1793595),
        "Red Hill Villa" to LatLng(9.320611, -82.18816),
        "Hotel Villa F&B" to LatLng(9.3056464, -82.1866724),
        "Casa Marlin" to LatLng(9.3050428, -82.1751478),
        "Marina Solarte" to LatLng(9.3095, -82.1870),
        "Isla Vista Tranquila" to LatLng(9.299679, -82.17088),
        
        // Isla Colon - North
        "Boca del Drago" to LatLng(9.41972, -82.33417),
        
        // Tierra Oscura - Waterfront locations
        "El Toucan Loco" to LatLng(9.1852931, -82.2708503),
        "Finca Tranquila" to LatLng(9.19082, -82.24794),
        
        // San Cristóbal area
        "Eco Lodge La Escapada" to LatLng(9.19141, -82.32903),
        
        // Waterfront Hotels with Docks - Isla Colón
        "Selina Bocas del Toro" to LatLng(9.3395, -82.2397),
        "Tropical Suites Hotel" to LatLng(9.3388, -82.2400),
        "Divers Paradise Boutique Hotel" to LatLng(9.3355, -82.2412),
        
        // Waterfront Restaurants with Docks - Isla Colón
        "JJ's at Bocas Blended" to LatLng(9.3432, -82.2426),
        "Restaurant Pier 19" to LatLng(9.3353, -82.2412),
        
        // Carenero Restaurants with Docks
        "Leaf Eaters Cafe" to LatLng(9.33867, -82.23639),
        
        // Isla Solarte - Waterfront (verified Feb 2026)
        "Sol Bungalows" to LatLng(9.328641, -82.2197046),
        "Aqui Hoy Bocas" to LatLng(9.3189252, -82.2109504),
        "Caribbean Coral Restoration Center" to LatLng(9.3264465, -82.2062221),
        "Solarte Breeze Lodges" to LatLng(9.3315227, -82.2199765),
        
        // Bastimentos - Waterfront
        "Blue Coconut Restaurant" to LatLng(9.3108, -82.2109),
        
        // Bastimentos - Remote Resorts (boat-only access)
        "Al Natural Resort" to LatLng(9.2909113, -82.0850771),
        "La Loma Jungle Lodge" to LatLng(9.316995, -82.1577651),
        "La Vida Resort" to LatLng(9.29300, -82.09000),
        "Eclypse de Mar Acqua Lodge" to LatLng(9.342692, -82.20653),
        
        // Private Island Resorts
        "Urraca Monkey Island Eco Resort" to LatLng(9.1824447, -82.0844067)
    )
    
    // All places with categories for search
    data class Place(val name: String, val position: LatLng, val category: String, val emoji: String, val description: String = "")

    fun normalizeCategory(input: String): String {
        val c = input.trim()
        // First check for exact matches (categories from Firestore)
        return when (c) {
            "Docks" -> "Docks"
            "Restaurants" -> "Restaurants"
            "Hotels" -> "Hotels"
            "Resorts" -> "Resorts"
            "Islands" -> "Islands"
            "Beaches" -> "Beaches"
            "Dolphin Sites" -> "Dolphin Sites"
            "Island Hopping" -> "Island Hopping"
            "Tours" -> "Tours"
            else -> {
                // Fallback to pattern matching for old/custom categories
                val lower = c.lowercase()
                when {
                    lower.contains("beach") || lower.contains("playa") -> "Beaches"
                    lower.contains("dolphin") || lower.contains("delfin") -> "Dolphin Sites"
                    lower.contains("island") || lower.contains("isla") || lower.contains("cayo") -> "Islands"
                    lower.contains("hopping") -> "Island Hopping"
                    lower.contains("tour") || lower.contains("snorkel") || lower.contains("dive") || lower.contains("trip") -> "Tours"
                    lower.contains("restaurant") || lower.contains("cafe") || lower.contains("bar") || lower.contains("pizza") || lower.contains("food") || lower.contains("grill") -> "Restaurants"
                    lower.contains("hotel") || lower.contains("hostel") || lower.contains("guesthouse") -> "Hotels"
                    lower.contains("resort") || lower.contains("lodge") || lower.contains("eco") || lower.contains("bungalow") || lower.contains("villa") || lower.contains("cabin") -> "Resorts"
                    lower.contains("dock") || lower.contains("marina") || lower.contains("ferry") || lower.contains("taxi") || lower.contains("port") -> "Docks"
                    else -> "Docks"
                }
            }
        }
    }
    
    val allPlaces = remember {
        listOf(
            // DOCKS
            Place("Main Water Taxi Dock", LatLng(9.3387625, -82.2402344), "Docks", "⚓"),
            Place("Bocas Docks", LatLng(9.3433664, -82.2424937), "Docks", "⚓"),
            Place("Water Taxi Dock (Lanchas)", LatLng(9.3367882, -82.2429174), "Docks", "⚓"),
            Place("Watertaxi Stop", LatLng(9.3399387, -82.239166), "Docks", "⚓"),
            Place("Palanga Ferry Terminal", LatLng(9.3353841, -82.2410291), "Docks", "⚓"),
            Place("Bocas Yacht Club & Marina", LatLng(9.3349824, -82.2468556), "Docks", "⚓"),
            Place("Marina Carenero", LatLng(9.3424036, -82.235247), "Docks", "⚓"),
            Place("Red Frog Marina", LatLng(9.3365993, -82.1774826), "Docks", "⚓"),
            Place("Ferry Bocas (Almirante)", LatLng(9.2893377, -82.3932207), "Docks", "⚓"),
            Place("Ferry Palanga (Almirante)", LatLng(9.2892706, -82.3930836), "Docks", "⚓"),
            Place("Bastimentos Boat Dock (Bocas)", LatLng(9.3372843, -82.240895), "Docks", "⚓"),
            Place("Carenero Dock", LatLng(9.3424, -82.2348), "Docks", "⚓"),
            Place("Old Bank Dock (Bastimentos)", LatLng(9.34782, -82.2111463), "Docks", "⚓"),
            
            // RESTAURANTS
            Place("JJ's at Bocas Blended", LatLng(9.3432, -82.2426), "Restaurants", "🍽️"),
            Place("Restaurant Pier 19", LatLng(9.3353, -82.2412), "Restaurants", "🍽️"),
            Place("Bibi's on the Beach", LatLng(9.3406077, -82.2313467), "Restaurants", "🍽️"),
            Place("Coquitos Pizza", LatLng(9.3401557, -82.2319386), "Restaurants", "🍽️"),
            Place("Leaf Eaters Cafe", LatLng(9.33867, -82.23639), "Restaurants", "🍽️"),
            Place("Blue Coconut Restaurant", LatLng(9.3108, -82.2109), "Restaurants", "🍽️"),
            Place("El Toucan Loco", LatLng(9.1852931, -82.2708503), "Restaurants", "🍽️"),
            Place("Aqui Hoy Bocas", LatLng(9.3189252, -82.2109504), "Restaurants", "🍽️"),
            Place("Bambuda Bocas Town", LatLng(9.3433, -82.2429), "Restaurants", "🍽️"),
            
            // HOTELS
            Place("Hotel Palma Royale", LatLng(9.33504, -82.24169), "Hotels", "🏨"),
            Place("Gran Hotel Bahía", LatLng(9.33913, -82.23797), "Hotels", "🏨"),
            Place("Hotel CalaLuna", LatLng(9.34276, -82.23871), "Hotels", "🏨"),
            Place("Hotel Bocas del Toro", LatLng(9.33938, -82.24258), "Hotels", "🏨"),
            Place("Hotel Tierra Verde", LatLng(9.3392473, -82.234033), "Hotels", "🏨"),
            Place("Selina Bocas del Toro", LatLng(9.3395, -82.2397), "Hotels", "🏨"),
            Place("Tropical Suites Hotel", LatLng(9.3388, -82.2400), "Hotels", "🏨"),
            Place("Divers Paradise Boutique Hotel", LatLng(9.3355, -82.2412), "Hotels", "🏨"),
            Place("Hostel on the Sea", LatLng(9.3437, -82.2437), "Hotels", "🏨"),
            Place("Faro del Colibri", LatLng(9.33776, -82.23524), "Hotels", "🏨"),
            Place("Buccaneer Resort", LatLng(9.3401283, -82.2320573), "Hotels", "🏨"),
            Place("Cosmic Crab Resort", LatLng(9.3436494, -82.2355459), "Hotels", "🏨"),
            Place("Solarte EcoLodge", LatLng(9.315224, -82.1816718), "Hotels", "🏨"),
            Place("Akwaba Lodge", LatLng(9.320453, -82.188827), "Hotels", "🏨"),
            Place("Los Secretos Guesthouse", LatLng(9.3101702, -82.171318), "Hotels", "🏨"),
            Place("Casa Cayuco Lodge", LatLng(9.2918901, -82.0878826), "Hotels", "🏨"),
            Place("Hotel Villa F&B", LatLng(9.3056464, -82.1866724), "Hotels", "🏨"),
            
            // RESORTS
            Place("Punta Caracol Acqua Lodge", LatLng(9.3424986, -82.2518131), "Resorts", "🏝️"),
            Place("Azul Paradise Resort", LatLng(9.2886696, -82.1152572), "Resorts", "🏝️"),
            Place("Tranquilo Bay Eco Resort", LatLng(9.2554756, -82.1453889), "Resorts", "🏝️"),
            Place("Dolphin Bay Hideaway", LatLng(9.2413134, -82.2581001), "Resorts", "🏝️"),
            Place("Al Natural Resort", LatLng(9.2909113, -82.0850771), "Resorts", "🏝️"),
            Place("La Loma Jungle Lodge", LatLng(9.316995, -82.1577651), "Resorts", "🏝️"),
            Place("Eclypse de Mar Acqua Lodge", LatLng(9.342692, -82.20653), "Resorts", "🏝️"),
            Place("Urraca Monkey Island Eco Resort", LatLng(9.1824447, -82.0844067), "Resorts", "🏝️"),
            Place("Palmar Beach Lodge", LatLng(9.3436695, -82.1787023), "Resorts", "🏝️"),
            Place("La Purita Ecolodge", LatLng(9.3128596, -82.1793595), "Resorts", "🏝️"),
            Place("Roam Yoga & Wellness", LatLng(9.3263929, -82.2074944), "Resorts", "🏝️"),
            Place("Sol Bungalows", LatLng(9.328641, -82.2197046), "Resorts", "🏝️"),
            Place("Solarte Breeze Lodges", LatLng(9.3315227, -82.2199765), "Resorts", "🏝️"),
            
            // ISLANDS
            Place("Isla Colón (Bocas Town)", LatLng(9.3400, -82.2425), "Islands", "🏝️"),
            Place("Isla Carenero", LatLng(9.3405, -82.2340), "Islands", "🏝️"),
            Place("Isla Bastimentos", LatLng(9.3000, -82.1700), "Islands", "🏝️"),
            Place("Isla Solarte", LatLng(9.3150, -82.1850), "Islands", "🏝️"),
            Place("Isla Popa", LatLng(9.2100, -82.1200), "Islands", "🏝️"),
            Place("Isla Cristóbal", LatLng(9.2700, -82.2650), "Islands", "🏝️"),
            Place("Cayo Zapatilla 1", LatLng(9.2450, -82.0550), "Islands", "🏝️"),
            Place("Cayo Zapatilla 2", LatLng(9.2350, -82.0450), "Islands", "🏝️"),
            Place("Bird Island", LatLng(9.3560, -82.2310), "Islands", "🏝️"),
            Place("Swan Cay", LatLng(9.4500, -82.2900), "Islands", "🏝️"),
            
            // BEACHES
            Place("Starfish Beach (Playa Estrella)", LatLng(9.4120, -82.3250), "Beaches", "🏖️"),
            Place("Boca del Drago Beach", LatLng(9.4190, -82.3340), "Beaches", "🏖️"),
            Place("Red Frog Beach", LatLng(9.3360, -82.1770), "Beaches", "🏖️"),
            Place("Wizard Beach", LatLng(9.3510, -82.1650), "Beaches", "🏖️"),
            Place("Bluff Beach", LatLng(9.3800, -82.2150), "Beaches", "🏖️"),
            Place("Playa Polo", LatLng(9.3200, -82.1550), "Beaches", "🏖️"),
            Place("Playa Larga", LatLng(9.3100, -82.1400), "Beaches", "🏖️"),
            Place("Big Creek Beach", LatLng(9.3600, -82.2080), "Beaches", "🏖️"),
            Place("Paunch Beach", LatLng(9.3680, -82.2200), "Beaches", "🏖️"),
            Place("Playa Istmito", LatLng(9.3450, -82.2480), "Beaches", "🏖️"),
            
            // DOLPHIN SITES
            Place("Dolphin Bay", LatLng(9.2413, -82.2581), "Dolphin Sites", "🐬"),
            Place("Bocas del Drago Dolphins", LatLng(9.4197, -82.3345), "Dolphin Sites", "🐬"),
            Place("Laguna Bocatorito", LatLng(9.2200, -82.2400), "Dolphin Sites", "🐬"),
            Place("Bahia de los Delfines", LatLng(9.2500, -82.2300), "Dolphin Sites", "🐬"),
            
            // ISLAND HOPPING TOURS
            Place("Zapatilla Islands Tour", LatLng(9.2400, -82.0500), "Island Hopping", "🚤"),
            Place("3 Islands Tour Start", LatLng(9.3387, -82.2402), "Island Hopping", "🚤"),
            Place("Bastimentos Day Trip", LatLng(9.3000, -82.1700), "Island Hopping", "🚤"),
            Place("Solarte Snorkel Tour", LatLng(9.3150, -82.1850), "Island Hopping", "🚤"),
            Place("Bird Island & Starfish", LatLng(9.3560, -82.2310), "Island Hopping", "🚤"),
            Place("Full Day Island Tour", LatLng(9.3400, -82.2425), "Island Hopping", "🚤"),
            
            // TOURS
            Place("Chocolate Farm Tour", LatLng(9.2800, -82.2100), "Tours", "🎫"),
            Place("Bat Cave Tour", LatLng(9.3100, -82.1600), "Tours", "🎫"),
            Place("Bioluminescent Bay Tour", LatLng(9.2700, -82.2000), "Tours", "🎫"),
            Place("Mangrove Tour", LatLng(9.3200, -82.2200), "Tours", "🎫"),
            Place("Snorkeling Tour Coral Gardens", LatLng(9.3300, -82.2000), "Tours", "🎫"),
            Place("Sloth Sanctuary Visit", LatLng(9.3400, -82.2300), "Tours", "🎫")
        )
    }
    
    // Load additional places from Firestore
    var firestorePlaces by remember { mutableStateOf<List<Place>>(emptyList()) }
    var placesLoaded by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        try {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            // Load from dock_locations collection (where manager adds the 85 places)
            val snapshot = firestore.collection("dock_locations").get().await()
            
            firestorePlaces = snapshot.documents.mapNotNull { doc ->
                try {
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val lat = doc.getDouble("latitude") ?: return@mapNotNull null
                    val lng = doc.getDouble("longitude") ?: return@mapNotNull null
                    val rawCategory = doc.getString("category") ?: "Docks"
                    val category = normalizeCategory(rawCategory)
                    val description = doc.getString("description") ?: ""
                    val emoji = when(category) {
                        "Docks" -> "⚓"
                        "Restaurants" -> "🍽️"
                        "Hotels" -> "🏨"
                        "Resorts" -> "🏝️"
                        "Islands" -> "🏝️"
                        "Beaches" -> "🏖️"
                        "Dolphin Sites" -> "🐬"
                        "Island Hopping" -> "🚤"
                        "Tours" -> "🎫"
                        else -> "📍"
                    }
                    Place(name, LatLng(lat, lng), category, emoji, description)
                } catch (e: Exception) {
                    null
                }
            }
            placesLoaded = true
            android.util.Log.d("BookingScreen", "Loaded ${firestorePlaces.size} places from Firestore")
        } catch (e: Exception) {
            android.util.Log.e("BookingScreen", "Failed to load places: ${e.message}")
            placesLoaded = true
        }
    }
    
    // Use ONLY Firestore places from manager (85 locations)
    val combinedPlaces = remember(firestorePlaces) {
        firestorePlaces
    }
    
    // Filter places based on search and category
    val filteredPlaces = remember(placesSearchQuery, selectedPlaceCategory, combinedPlaces) {
        combinedPlaces.filter { place ->
            val matchesCategory = selectedPlaceCategory == "All" || place.category == selectedPlaceCategory
            val matchesSearch = placesSearchQuery.isEmpty() || 
                place.name.contains(placesSearchQuery, ignoreCase = true) ||
                place.description.contains(placesSearchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }
    
    // Load dock locations from Firestore (primary source, managed by admin)
    var firestoreDocks by remember { mutableStateOf<List<Pair<String, LatLng>>>(emptyList()) }
    var docksLoaded by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        try {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("dock_locations").get().await()
            
            firestoreDocks = snapshot.documents.mapNotNull { doc ->
                try {
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val lat = doc.getDouble("latitude") ?: return@mapNotNull null
                    val lng = doc.getDouble("longitude") ?: return@mapNotNull null
                    name to LatLng(lat, lng)
                } catch (e: Exception) {
                    null
                }
            }
            docksLoaded = true
        } catch (e: Exception) {
            docksLoaded = true
            // Silently fail - will use hardcoded docks as fallback
        }
    }
    
    // Use Firestore docks if available, otherwise fallback to hardcoded
    val allDockLocations = remember(dockLocations, firestoreDocks, docksLoaded) {
        if (firestoreDocks.isNotEmpty()) {
            firestoreDocks
        } else {
            dockLocations
        }
    }
    
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
    
    // Start observing the booking when we have an active PENDING booking
    // This enables the fare adjustment notification to work
    LaunchedEffect(uiState.activeBooking?.id) {
        uiState.activeBooking?.let { booking ->
            if (booking.status == BookingStatus.PENDING) {
                android.util.Log.d("BookRide", "Starting to observe booking ${booking.id} for driver offers")
                viewModel.observeBooking(booking.id)
                // Also observe driver offers for the new flow
                viewModel.observeDriverOffers(booking.id)
            }
        }
    }
    
    // Check for unrated completed bookings and show rating popup
    LaunchedEffect(Unit) {
        viewModel.checkForUnratedBooking()
    }
    
    LaunchedEffect(uiState.unratedBookingId) {
        uiState.unratedBookingId?.let { bookingId ->
            android.util.Log.d("BookRide", "Found unrated booking, navigating to rating: $bookingId")
            viewModel.clearUnratedBooking()
            onNavigateToRateRide(bookingId)
        }
    }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            uiState.currentLocation ?: LatLng(9.4073, -82.3421), // Default to Bocas del Toro
            17f
        )
    }
    
    // Move camera when location is obtained
    LaunchedEffect(uiState.currentLocation) {
        uiState.currentLocation?.let { location ->
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(location, 18f)
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
                myLocationButtonEnabled = false, // Custom button instead for better zoom
                zoomControlsEnabled = false, // Disabled - custom controls on left to avoid booking panel overlap
                compassEnabled = true
            ),
            onMapClick = { latLng ->
                // TAXI can select any location, BOAT must use approved docks
                if (vehicleType == VehicleType.TAXI) {
                    // Taxi - allow any location
                    val locationName = "Selected Location"
                    if (uiState.isSettingPickupOnMap) {
                        viewModel.setPickupLocation(locationName, latLng)
                    } else {
                        viewModel.setDropoffLocation(locationName, latLng)
                    }
                } else {
                    // Boat - must be near a dock (within ~100 meters)
                    val nearestDock = dockLocations.minByOrNull { (_, dockPos) ->
                        val latDiff = kotlin.math.abs(latLng.latitude - dockPos.latitude)
                        val lngDiff = kotlin.math.abs(latLng.longitude - dockPos.longitude)
                        latDiff + lngDiff
                    }
                    
                    nearestDock?.let { (dockName, dockPos) ->
                        val latDiff = kotlin.math.abs(latLng.latitude - dockPos.latitude)
                        val lngDiff = kotlin.math.abs(latLng.longitude - dockPos.longitude)
                        val distance = latDiff + lngDiff
                        
                        // ~0.001 degrees is roughly 100 meters
                        if (distance < 0.001) {
                            // Close enough to a dock - allow it
                            if (uiState.isSettingPickupOnMap) {
                                viewModel.setPickupLocation(dockName, dockPos)
                            } else {
                                viewModel.setDropoffLocation(dockName, dockPos)
                            }
                        } else {
                            // Not near a dock - show error
                            showLocationError = if (uiState.isSettingPickupOnMap) {
                                "Can't pick up at that location. Please select an approved dock."
                            } else {
                                "Can't drop off at that location. Please select an approved dock."
                            }
                        }
                    }
                }
            }
        ) {
            // "You are here" marker at user location - updates in real-time
            uiState.currentLocation?.let { userLoc ->
                val userMarkerState = rememberMarkerState(position = userLoc)
                LaunchedEffect(userLoc) {
                    userMarkerState.position = userLoc
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
            }
            
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
            if (showIslandDeals) {
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
                            // Blinking animation for deals
                            val infiniteTransition = rememberInfiniteTransition(label = "blink")
                            val alpha by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 0.4f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(500),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "blinkAlpha"
                            )
                            
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
                                            modifier = Modifier.size(28.dp),
                                            shape = RoundedCornerShape(6.dp),
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
                                                        fontSize = 16.sp
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
                                        // Deal/Coupon below name - blinking when has deal
                                        val dealText = when {
                                            !ad.couponDiscount.isNullOrBlank() -> "🎟️ ${ad.couponDiscount}"
                                            ad.hasCoupon && !ad.title.isBlank() -> "🎟️ ${ad.title}"
                                            !ad.title.isBlank() && ad.title != ad.businessName -> ad.title
                                            else -> null
                                        }
                                        if (dealText != null) {
                                            Text(
                                                text = dealText,
                                                color = (if (ad.hasCoupon) Color.Yellow else Color.White.copy(alpha = 0.9f)).copy(alpha = alpha),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 8.sp,
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
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    }
                }
            }
            } // End of showIslandDeals
            
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
                        
                        // Use key that includes location to force recomposition when position changes
                        key(driver.id, vehicleType, driver.currentLocation?.latitude, driver.currentLocation?.longitude) {
                            // Create marker state that updates with driver position
                            val markerState = rememberMarkerState(position = driverPosition)
                            
                            // Update marker position when driver moves
                            LaunchedEffect(driverPosition) {
                                markerState.position = driverPosition
                            }
                            
                            MarkerComposable(
                                keys = arrayOf(driver.id, vehicleType, driverPosition.latitude, driverPosition.longitude),
                                state = markerState,
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
            
            // Dock location markers - only show for BOAT mode
            if (vehicleType == VehicleType.BOAT && showDocksOnMap) {
                allDockLocations.forEach { (dockName, dockPosition) ->
                    key(dockName) {
                        MarkerComposable(
                            keys = arrayOf(dockName),
                            state = rememberMarkerState(position = dockPosition),
                            onClick = {
                                // Set as pickup or dropoff based on current mode
                                if (uiState.isSettingPickupOnMap) {
                                    viewModel.setPickupLocation(dockName, dockPosition)
                                } else {
                                    viewModel.setDropoffLocation(dockName, dockPosition)
                                }
                                true
                            }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    color = Color(0xFF0288D1),
                                    shape = RoundedCornerShape(8.dp),
                                    shadowElevation = 4.dp
                                ) {
                                    Text(
                                        text = dockName,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                        maxLines = 2
                                    )
                                }
                                Text(
                                    text = "⚓",
                                    fontSize = 24.sp
                                )
                            }
                        }
                    }
                }
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
        
        // Live users badge - top left
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 8.dp, start = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = Surface,
            shadowElevation = 4.dp
        ) {
            LiveUsersBadge(compact = true)
        }
        
        // Warning banner when no drivers online for this vehicle type
        val driversOnlineForType = when (vehicleType) {
            VehicleType.BOAT -> uiState.boatDriversOnline
            VehicleType.TAXI -> uiState.taxiDriversOnline
        }
        if (driversOnlineForType == 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(100f)
                    .padding(top = 50.dp, start = 60.dp, end = 60.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Warning,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (vehicleType == VehicleType.BOAT) 
                            "No boats online - try again later" 
                        else 
                            "No taxis online - try again later",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Current mode indicator - shows if setting pickup or dropoff
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 70.dp, start = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = if (uiState.isSettingPickupOnMap) Color(0xFF4CAF50) else Color(0xFFFF5722),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    if (uiState.isSettingPickupOnMap) Icons.Default.MyLocation else Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (vehicleType == VehicleType.BOAT) {
                        if (uiState.isSettingPickupOnMap) stringResource(R.string.tap_dock_pickup) else stringResource(R.string.tap_dock_dropoff)
                    } else {
                        if (uiState.isSettingPickupOnMap) stringResource(R.string.tap_map_pickup) else stringResource(R.string.tap_map_dropoff)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
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
                // Total users online badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF4CAF50), shape = RoundedCornerShape(50))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${uiState.totalOnlineUsers} Online",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (uiState.showOnlineDrivers) Color.White else Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
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
        
        // Search Places button - opens category search dialog
        Surface(
            onClick = { showPlacesSearchDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 130.dp, end = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = Primary,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.search_dock_places),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                // Show total places count dynamically (hardcoded + firestore)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "${combinedPlaces.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        
        // Toggle Show/Hide Places on Map button - below "tap dock for pickup"
        Surface(
            onClick = { showDocksOnMap = !showDocksOnMap },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 108.dp, start = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = if (showDocksOnMap) Color(0xFF4CAF50) else Color.White,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    if (showDocksOnMap) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = if (showDocksOnMap) Color.White else Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (showDocksOnMap) stringResource(R.string.hide_docks) else stringResource(R.string.show_docks),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (showDocksOnMap) Color.White else Color.Gray,
                    fontWeight = FontWeight.Medium
                )
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
            
            // My Location button - zooms in to current location
            Surface(
                onClick = { 
                    coroutineScope.launch {
                        uiState.currentLocation?.let { location ->
                            cameraPositionState.animate(
                                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(location, 19f)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = "My Location",
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(24.dp)
                    )
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
        
        // Island Deals toggle button - next to search
        // Flashing animation when deals are hidden to attract attention
        val infiniteTransition = rememberInfiniteTransition(label = "dealsFlash")
        val flashAlpha by infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "flashAlpha"
        )
        val flashScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "flashScale"
        )
        
        Surface(
            onClick = { showIslandDeals = !showIslandDeals },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 162.dp, top = 8.dp)
                .then(
                    if (!showIslandDeals && uiState.adsOnMap.isNotEmpty()) 
                        Modifier.graphicsLayer { 
                            scaleX = flashScale
                            scaleY = flashScale
                        }
                    else Modifier
                ),
            shape = RoundedCornerShape(12.dp),
            color = if (showIslandDeals) Color(0xFF4CAF50) 
                   else if (uiState.adsOnMap.isNotEmpty()) Color(0xFFFF9800).copy(alpha = flashAlpha)
                   else Color.White,
            shadowElevation = if (!showIslandDeals && uiState.adsOnMap.isNotEmpty()) 8.dp else 4.dp,
            border = if (!showIslandDeals && uiState.adsOnMap.isNotEmpty()) 
                        BorderStroke(2.dp, Color(0xFFFF5722).copy(alpha = flashAlpha))
                     else null
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Animated gift icon when hidden
                if (!showIslandDeals && uiState.adsOnMap.isNotEmpty()) {
                    Text(
                        text = "🎁",
                        fontSize = 18.sp,
                        modifier = Modifier.graphicsLayer { rotationZ = (flashAlpha * 10f) - 5f }
                    )
                } else {
                    Text(
                        text = "🏝️",
                        fontSize = 16.sp
                    )
                }
                Text(
                    text = if (showIslandDeals) stringResource(R.string.hide_deals) 
                           else if (uiState.adsOnMap.isNotEmpty()) "${uiState.adsOnMap.size} Deals!"
                           else stringResource(R.string.show_deals),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (showIslandDeals) Color.White 
                           else if (uiState.adsOnMap.isNotEmpty()) Color.White
                           else TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                if (showIslandDeals && uiState.adsOnMap.isNotEmpty()) {
                    Text(
                        text = "${uiState.adsOnMap.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
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
                                BookingStatus.ACCEPTED -> stringResource(R.string.on_the_way)
                                BookingStatus.ARRIVED -> stringResource(R.string.arrived)
                                BookingStatus.IN_PROGRESS -> "En Route"
                                else -> if (vehicleType == VehicleType.BOAT) "Boat" else "Taxi"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextOnPrimary
                        )
                    }
                    Icon(
                        if (isSheetExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = if (isSheetExpanded) "Collapse" else "Expand",
                        tint = TextOnPrimary,
                        modifier = Modifier.size(18.dp)
                    )
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
                    
                    // Passenger count selector
                    Surface(
                        color = Primary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Group,
                                    null,
                                    tint = Primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.how_many_people),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary,
                                    fontSize = 10.sp
                                )
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Minus button
                                Surface(
                                    onClick = { viewModel.decrementPassengers() },
                                    color = if (uiState.passengerCount > 1) Primary else Color.Gray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "−",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                
                                // Count display
                                Text(
                                    text = "${uiState.passengerCount}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary,
                                    modifier = Modifier.width(28.dp),
                                    textAlign = TextAlign.Center
                                )
                                
                                // Plus button
                                Surface(
                                    onClick = { viewModel.incrementPassengers() },
                                    color = if (uiState.passengerCount < 10) Primary else Color.Gray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "+",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
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
                        } else {
                            // Book button and Reset button side by side - no price needed, drivers bid
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
                                
                                // Book button - drivers will set the price
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
                                                "Request ${uiState.requestedDriverName}"
                                            else
                                                "Find Ride",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Show booking status - compact
                        when (uiState.activeBooking?.status) {
                            BookingStatus.PENDING -> {
                                // NEW FLOW: Show driver offers or waiting status
                                val offers = uiState.driverOffers
                                
                                if (offers.isEmpty()) {
                                    // No offers yet - show waiting status
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
                                                    text = "Waiting for Drivers...",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = "Drivers will send you their price offers",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = Warning
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Show driver offers - rider can pick one
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "${offers.size} Driver Offer${if (offers.size > 1) "s" else ""} Available",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Success
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        // List offers (max 5 visible, scrollable)
                                        offers.filter { !it.isRejected }.take(5).forEach { offer ->
                                            Surface(
                                                color = Primary.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Driver photo or icon
                                                    Surface(
                                                        modifier = Modifier.size(40.dp),
                                                        shape = RoundedCornerShape(50),
                                                        color = Primary.copy(alpha = 0.2f)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            if (offer.driverPhotoUrl != null) {
                                                                AsyncImage(
                                                                    model = offer.driverPhotoUrl,
                                                                    contentDescription = null,
                                                                    modifier = Modifier.fillMaxSize(),
                                                                    contentScale = ContentScale.Crop
                                                                )
                                                            } else {
                                                                Text(
                                                                    text = if (vehicleType == VehicleType.BOAT) "🚤" else "🚕",
                                                                    style = MaterialTheme.typography.titleMedium
                                                                )
                                                            }
                                                        }
                                                    }
                                                    
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    
                                                    // Driver info and offer
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = offer.driverName,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.Black
                                                        )
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                Icons.Default.Star,
                                                                null,
                                                                modifier = Modifier.size(14.dp),
                                                                tint = Warning
                                                            )
                                                            Text(
                                                                text = " ${String.format("%.1f", offer.driverRating)} • ${offer.driverTotalTrips} trips",
                                                                style = MaterialTheme.typography.labelMedium,
                                                                color = Color.DarkGray
                                                            )
                                                        }
                                                    }
                                                    
                                                    // Price
                                                    Text(
                                                        text = "$${String.format("%.2f", offer.price)}",
                                                        style = MaterialTheme.typography.headlineSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Primary,
                                                        modifier = Modifier.padding(horizontal = 8.dp)
                                                    )
                                                    
                                                    // Accept button (checkmark)
                                                    Surface(
                                                        color = Success,
                                                        shape = RoundedCornerShape(50),
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clickable { viewModel.acceptDriverOffer(offer) }
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(
                                                                Icons.Default.Check,
                                                                contentDescription = "Accept offer",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        }
                                                    }
                                                    
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    
                                                    // Reject button (X)
                                                    Surface(
                                                        color = Color.Red.copy(alpha = 0.9f),
                                                        shape = RoundedCornerShape(50),
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clickable { viewModel.rejectDriverOffer(offer) }
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(
                                                                Icons.Default.Close,
                                                                contentDescription = "Reject - price too high",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
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
                                                    (if (vehicleType == VehicleType.BOAT) stringResource(R.string.boat_on_the_way) else stringResource(R.string.car_on_the_way))
                                                else 
                                                    (if (vehicleType == VehicleType.BOAT) stringResource(R.string.captain_arrived) else stringResource(R.string.driver_arrived_icon)),
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
                                
                            }
                            else -> {}
                        }
                    }
                    
                    if (uiState.errorMessage != null) {
                        Surface(
                            color = Error.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiState.errorMessage!!,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Error
                                )
                            }
                        }
                        
                        // Auto-clear the error after 5 seconds
                        LaunchedEffect(uiState.errorMessage) {
                            kotlinx.coroutines.delay(5000)
                            viewModel.clearError()
                        }
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
                        
                        // Clear/Reset button - allows clearing pickup and dropoff selections
                        if (uiState.pickupLocation != null || uiState.dropoffLocation != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.clearLocations() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                                border = BorderStroke(1.dp, Divider)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.reset), fontWeight = FontWeight.SemiBold)
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
                                            BookingStatus.PENDING -> "Waiting for driver price offers..."
                                            BookingStatus.ACCEPTED -> stringResource(R.string.driver_on_the_way)
                                            BookingStatus.ARRIVED -> stringResource(R.string.driver_arrived_rider)
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
            Dialog(
                onDismissRequest = { viewModel.dismissSubscriptionRequired() }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Big boat emoji
                        Text(
                            text = "🚤",
                            fontSize = 64.sp
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Big bold title
                        Text(
                            text = "Unlock Unlimited Rides!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Primary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Clear subtitle
                        Text(
                            text = stringResource(R.string.subscribe_to_book_desc),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Price highlight
                        Surface(
                            color = Success.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.starting_at),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "$1.99" + stringResource(R.string.per_day),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Success
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // BIG Primary button - See Plans
                        Button(
                            onClick = {
                                viewModel.dismissSubscriptionRequired()
                                onNavigateToSubscription()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                stringResource(R.string.see_plans_subscribe),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Secondary button - Maybe Later
                        TextButton(
                            onClick = { viewModel.dismissSubscriptionRequired() }
                        ) {
                            Text(
                                "Maybe Later",
                                color = TextSecondary,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
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
                    
                    // DOCKS - ALL dock locations from the map (boat taxi stops)
                    val dockSearchLocations = listOf(
                        // Bocas Town - Main docks
                        Triple("Main Water Taxi Dock", LatLng(9.3387625, -82.2402344), "Bocas Town - Main taxi stand"),
                        Triple("Bocas Docks", LatLng(9.3433664, -82.2424937), "Bocas Town - Central docks"),
                        Triple("Water Taxi Dock (Lanchas)", LatLng(9.3367882, -82.2429174), "Bocas Town - Public lanchas"),
                        Triple("Watertaxi Stop", LatLng(9.3399387, -82.239166), "Bocas Town"),
                        Triple("Isla Colon Water Taxi", LatLng(9.3388934, -82.2396447), "Bocas Town"),
                        Triple("Transporte Maritimo Valencia", LatLng(9.3384523, -82.2402878), "Bocas Town - Ferry service"),
                        Triple("Palanga Ferry Terminal", LatLng(9.3353841, -82.2410291), "Bocas Town - Almirante ferry"),
                        Triple("Bocas Yacht Club & Marina", LatLng(9.3349824, -82.2468556), "Bocas Town - Marina"),
                        Triple("Bambuda Bocas Town", LatLng(9.3433, -82.2429), "Bocas Town"),
                        Triple("Bocas Airport (BOC)", LatLng(9.34083, -82.25), "Bocas del Toro Airport"),
                        Triple("Dolphin Bay", LatLng(9.34326, -82.25188), "Bocas Town - Tours"),
                        
                        // Bocas Town - Hotels with Docks
                        Triple("Hotel Palma Royale", LatLng(9.33504, -82.24169), "Bocas Town - Hotel dock"),
                        Triple("Gran Hotel Bahía", LatLng(9.33913, -82.23797), "Bocas Town - Hotel dock"),
                        Triple("Hotel CalaLuna", LatLng(9.34276, -82.23871), "Bocas Town - Hotel dock"),
                        Triple("Hotel Bocas del Toro", LatLng(9.33938, -82.24258), "Bocas Town - Hotel dock"),
                        Triple("Hostel on the Sea", LatLng(9.3437, -82.2437), "Bocas Town - Hostel dock"),
                        Triple("Lil Spa Shop / United Reality", LatLng(9.3432, -82.2443), "Bocas Town"),
                        Triple("Selina Bocas del Toro", LatLng(9.3395, -82.2397), "Bocas Town - Hotel dock"),
                        Triple("Tropical Suites Hotel", LatLng(9.3388, -82.2400), "Bocas Town - Hotel dock"),
                        Triple("Divers Paradise Boutique Hotel", LatLng(9.3355, -82.2412), "Bocas Town - Hotel dock"),
                        
                        // Bocas Town - Restaurants with Docks
                        Triple("JJ's at Bocas Blended", LatLng(9.3432, -82.2426), "Bocas Town - Restaurant dock"),
                        Triple("Restaurant Pier 19", LatLng(9.3353, -82.2412), "Bocas Town - Restaurant dock"),
                        
                        // Carenero Island
                        Triple("Carenero Dock", LatLng(9.3424, -82.2348), "Carenero - Main dock"),
                        Triple("Marina Carenero", LatLng(9.3424036, -82.235247), "Carenero - Marina"),
                        Triple("Bibi's on the Beach", LatLng(9.3406077, -82.2313467), "Carenero - Restaurant dock"),
                        Triple("Coquitos Pizza", LatLng(9.3401557, -82.2319386), "Carenero - Restaurant dock"),
                        Triple("Hotel Tierra Verde", LatLng(9.3392473, -82.234033), "Carenero - Hotel dock"),
                        Triple("Faro del Colibri", LatLng(9.33776, -82.23524), "Carenero - Hotel dock"),
                        Triple("Buccaneer Resort", LatLng(9.3401283, -82.2320573), "Carenero - Resort dock"),
                        Triple("Cosmic Crab Resort", LatLng(9.3436494, -82.2355459), "Carenero - Resort dock"),
                        Triple("Leaf Eaters Cafe", LatLng(9.33867, -82.23639), "Carenero - Restaurant dock"),
                        
                        // Bastimentos Island
                        Triple("Bastimentos Boat Dock (Bocas)", LatLng(9.3372843, -82.240895), "Bocas - Boats to Bastimentos"),
                        Triple("Old Bank Tender Dock", LatLng(9.34782, -82.2111463), "Bastimentos - Old Bank"),
                        Triple("Red Frog Marina", LatLng(9.3365993, -82.1774826), "Bastimentos - Red Frog Beach"),
                        Triple("Casa Cayuco Lodge", LatLng(9.2918901, -82.0878826), "Bastimentos - Remote lodge"),
                        Triple("Palmar Beach Lodge", LatLng(9.3436695, -82.1787023), "Bastimentos - Beach lodge"),
                        Triple("Blue Coconut Restaurant", LatLng(9.3108, -82.2109), "Bastimentos - Restaurant dock"),
                        Triple("Al Natural Resort", LatLng(9.2909113, -82.0850771), "Bastimentos - Eco resort"),
                        Triple("La Loma Jungle Lodge", LatLng(9.316995, -82.1577651), "Bastimentos - Jungle lodge"),
                        Triple("La Vida Resort", LatLng(9.29300, -82.09000), "Bastimentos - Resort"),
                        Triple("Eclypse de Mar Acqua Lodge", LatLng(9.342692, -82.20653), "Bastimentos - Overwater lodge"),
                        
                        // Almirante Ferries
                        Triple("Ferry Bocas (Almirante)", LatLng(9.2893377, -82.3932207), "Almirante - Ferry to Bocas"),
                        Triple("Ferry Palanga (Almirante)", LatLng(9.2892706, -82.3930836), "Almirante - Ferry dock"),
                        
                        // Resorts with Docks
                        Triple("Punta Caracol Dock", LatLng(9.3424986, -82.2518131), "Overwater bungalows"),
                        Triple("Azul Paradise", LatLng(9.2886696, -82.1152572), "Red Frog - Resort dock"),
                        Triple("Tranquilo Bay", LatLng(9.2554756, -82.1453889), "Remote - Eco resort"),

                        Triple("Dolphin Bay Hideaway", LatLng(9.2413134, -82.2581001), "Remote - Dolphin tours"),
                        
                        // Isla Solarte (verified Feb 2026)
                        Triple("Solarte EcoLodge", LatLng(9.315224, -82.1816718), "Solarte - Eco lodge"),
                        Triple("Akwaba Lodge", LatLng(9.320453, -82.188827), "Solarte - Lodge"),
                        Triple("Los Secretos Guesthouse", LatLng(9.3101702, -82.171318), "Solarte - Guesthouse"),
                        Triple("Roam Yoga & Wellness", LatLng(9.3263929, -82.2074944), "Solarte - Yoga retreat"),
                        Triple("Las Casitas del Perezoso", LatLng(9.3224699, -82.1980979), "Solarte - Cabins"),
                        Triple("La Purita Ecolodge", LatLng(9.3128596, -82.1793595), "Solarte - Eco lodge"),
                        Triple("Red Hill Villa", LatLng(9.320611, -82.18816), "Solarte - Villa"),
                        Triple("Hotel Villa F&B", LatLng(9.3056464, -82.1866724), "Solarte - Hotel"),
                        Triple("Casa Marlin", LatLng(9.3050428, -82.1751478), "Solarte - House"),
                        Triple("Marina Solarte", LatLng(9.3095, -82.1870), "Solarte - Marina"),
                        Triple("Isla Vista Tranquila", LatLng(9.299679, -82.17088), "Solarte area"),
                        Triple("Sol Bungalows", LatLng(9.328641, -82.2197046), "Solarte - Bungalows"),
                        Triple("Aqui Hoy Bocas", LatLng(9.3189252, -82.2109504), "Solarte - Restaurant"),
                        Triple("Caribbean Coral Restoration Center", LatLng(9.3264465, -82.2062221), "Solarte - Conservation"),
                        Triple("Solarte Breeze Lodges", LatLng(9.3315227, -82.2199765), "Solarte - Lodges"),
                        
                        // Isla Colon - North
                        Triple("Boca del Drago", LatLng(9.41972, -82.33417), "Isla Colon - North beach"),
                        
                        // Tierra Oscura
                        Triple("El Toucan Loco", LatLng(9.1852931, -82.2708503), "Tierra Oscura - Lodge"),
                        Triple("Finca Tranquila", LatLng(9.19082, -82.24794), "Tierra Oscura - Farm stay"),
                        
                        // San Cristóbal
                        Triple("Eco Lodge La Escapada", LatLng(9.19141, -82.32903), "San Cristóbal - Eco lodge"),
                        
                        // Private Island
                        Triple("Urraca Monkey Island Eco Resort", LatLng(9.1824447, -82.0844067), "Private island resort")
                    )
                    
                    // Add Firestore places to search (these include all places managed by admin with descriptions)
                    val firestorePlaceSearchLocations = firestorePlaces.map { place ->
                        Triple(place.name, place.position, place.description.ifEmpty { place.category })
                    }
                    
                    // If we have Firestore places, use them; otherwise use the hardcoded search locations
                    val allDockSearchLocations = if (firestorePlaces.isNotEmpty()) {
                        firestorePlaceSearchLocations
                    } else if (firestoreDocks.isNotEmpty()) {
                        firestoreDocks.map { (name, latLng) -> Triple(name, latLng, "Dock") }
                    } else {
                        dockSearchLocations
                    }
                    
                    // Add dock locations to search results
                    // Keywords that should show ALL docks/locations
                    val isDockKeywordSearch = query.contains("dock", ignoreCase = true) ||
                        query.contains("marina", ignoreCase = true) ||
                        query.contains("ferry", ignoreCase = true) ||
                        query.contains("boat", ignoreCase = true) ||
                        query.contains("muelle", ignoreCase = true) ||
                        query.contains("taxi", ignoreCase = true)
                    
                    allDockSearchLocations.forEach { (name, latLng, description) ->
                        val matchesDirectly = name.contains(query, ignoreCase = true) ||
                            description.contains(query, ignoreCase = true)
                        
                        // Show ALL docks if searching for dock-related keywords, otherwise match name/description
                        if (matchesDirectly || isDockKeywordSearch) {
                            allResults.add(SearchResult(
                                id = name,
                                title = name,
                                subtitle = "🚤 $description",
                                type = SearchResultType.LOCATION,
                                icon = "⚓",
                                location = latLng
                            ))
                        }
                    }
                    
                    // Additional location-based suggestions - comprehensive Bocas del Toro directory
                    val locationSuggestions = listOf(
                        // Main Areas
                        "Bocas Town" to LatLng(9.340556, -82.241944),
                        "Isla Colón" to LatLng(9.381944, -82.265556),
                        "Bastimentos" to LatLng(9.300833, -82.143056),
                        "Carenero Island" to LatLng(9.336111, -82.228889),
                        "Almirante" to LatLng(9.297778, -82.404722),
                        "Isla Solarte" to LatLng(9.317778, -82.208889),
                        "Isla Cristóbal" to LatLng(9.283333, -82.250000),
                        
                        // Beaches
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
                        
                        // Restaurants & Bars
                        "Bocas Brewery" to LatLng(9.340556, -82.241944),
                        "El Ultimo Refugio" to LatLng(9.358333, -82.265278),
                        "Toro Loco" to LatLng(9.340278, -82.241667),
                        "Om Cafe" to LatLng(9.340556, -82.242222),
                        "Capitan Caribe" to LatLng(9.340278, -82.241389),
                        "Restaurante Alberto" to LatLng(9.340000, -82.241111),
                        "Hungry Monkey" to LatLng(9.340833, -82.242500),
                        "Raw Fusion" to LatLng(9.340278, -82.241667),
                        "La Coralina" to LatLng(9.339722, -82.241389),
                        "Super Gourmet" to LatLng(9.339722, -82.241111),
                        "Skully's" to LatLng(9.339444, -82.240833),
                        "Aqua Lounge Bar" to LatLng(9.340000, -82.240833),
                        "Mondo Taitu" to LatLng(9.339167, -82.240556),
                        "Pickled Parrot" to LatLng(9.338889, -82.240278),
                        
                        // Tours & Activities
                        "Dolphin Bay Tours" to LatLng(9.260278, -82.270000),
                        "Bocas Water Sports" to LatLng(9.340833, -82.242778),
                        "Mono Loco Surf School" to LatLng(9.365278, -82.275000),
                        "Bastimentos National Park" to LatLng(9.285000, -82.135000),
                        "Finca Los Monos" to LatLng(9.370278, -82.260000),
                        "Bat Cave" to LatLng(9.320000, -82.205000),
                        "Nivida Bat Cave" to LatLng(9.314722, -82.199722),
                        "Snorkeling Point" to LatLng(9.300000, -82.150000),
                        "Coral Cay" to LatLng(9.305000, -82.160000),
                        
                        // Wellness
                        "Bocas Yoga" to LatLng(9.341111, -82.243056),
                        "Punta Vieja Retreat" to LatLng(9.345000, -82.250000),
                        
                        // Services
                        "Bocas Marina" to LatLng(9.340278, -82.241111),
                        "Public Market" to LatLng(9.339167, -82.240000),
                        "Town Square" to LatLng(9.339722, -82.240556),
                        "Bocas Airport (Isla Colón)" to LatLng(9.340556, -82.250833),
                        
                        // Nature
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
                    
                    // Sort by relevance (exact matches first, then dock results)
                    searchResults = allResults
                        .distinctBy { it.id }
                        .sortedByDescending { 
                            when {
                                it.title.equals(query, ignoreCase = true) -> 100
                                it.title.startsWith(query, ignoreCase = true) -> 80
                                it.icon == "⚓" -> 60  // Prioritize docks
                                else -> 40
                            }
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
        
        // Dock Locations Dialog
        if (showDockLocationsDialog) {
            Dialog(onDismissRequest = { showDockLocationsDialog = false }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
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
                                Text(
                                    text = "⚓",
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Dock Locations",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                            IconButton(onClick = { showDockLocationsDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Toggle show on map
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDocksOnMap = !showDocksOnMap }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Show docks on map",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black
                            )
                            Switch(
                                checked = showDocksOnMap,
                                onCheckedChange = { showDocksOnMap = it }
                            )
                        }
                        
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Dock list
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 400.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(dockLocations) { (dockName, dockPosition) ->
                                Surface(
                                    onClick = {
                                        // Set as pickup or dropoff based on current mode
                                        if (uiState.isSettingPickupOnMap) {
                                            viewModel.setPickupLocation(dockName, dockPosition)
                                        } else {
                                            viewModel.setDropoffLocation(dockName, dockPosition)
                                        }
                                        showDockLocationsDialog = false
                                        // Move camera to dock
                                        coroutineScope.launch {
                                            cameraPositionState.animate(
                                                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(dockPosition, 16f)
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF5F5F5)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "⚓",
                                            fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = dockName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.Black
                                            )
                                            Text(
                                                text = if (uiState.isSettingPickupOnMap) "Tap to set as pickup" else "Tap to set as dropoff",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (uiState.isSettingPickupOnMap) Color(0xFF4CAF50) else Color(0xFFFF5722)
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
        
        // Places Search Dialog with Categories
        if (showPlacesSearchDialog) {
            Dialog(onDismissRequest = { 
                showPlacesSearchDialog = false
                placesSearchQuery = ""
            }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .padding(8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
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
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.search_dock_places),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                            IconButton(onClick = { 
                                showPlacesSearchDialog = false 
                                placesSearchQuery = ""
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Search input
                        OutlinedTextField(
                            value = placesSearchQuery,
                            onValueChange = { placesSearchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.search_by_name), color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                            trailingIcon = {
                                if (placesSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { placesSearchQuery = "" }) {
                                        Icon(Icons.Default.Close, "Clear", tint = Color.Gray)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Category chips - Row 1 (first 5)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val firstRow = placeCategories.take(5)
                            items(firstRow.size) { index ->
                                val category = firstRow[index]
                                val isSelected = selectedPlaceCategory == category
                                val emoji = when(category) {
                                    "Docks" -> "⚓"
                                    "Restaurants" -> "🍽️"
                                    "Hotels" -> "🏨"
                                    "Islands" -> "🏝️"
                                    "Beaches" -> "🏖️"
                                    "Resorts" -> "🌴"
                                    "Dolphin Sites" -> "🐬"
                                    "Island Hopping" -> "🚤"
                                    "Tours" -> "🎫"
                                    else -> "🔍"
                                }
                                Surface(
                                    onClick = { selectedPlaceCategory = category },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) Primary else Color(0xFFF0F0F0),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = emoji, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = category,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else Color.Black
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Category chips - Row 2 (remaining 5)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val secondRow = placeCategories.drop(5)
                            items(secondRow.size) { index ->
                                val category = secondRow[index]
                                val isSelected = selectedPlaceCategory == category
                                val emoji = when(category) {
                                    "Docks" -> "⚓"
                                    "Restaurants" -> "🍽️"
                                    "Hotels" -> "🏨"
                                    "Islands" -> "🏝️"
                                    "Beaches" -> "🏖️"
                                    "Resorts" -> "🌴"
                                    "Dolphin Sites" -> "🐬"
                                    "Island Hopping" -> "🚤"
                                    "Tours" -> "🎫"
                                    else -> "🔍"
                                }
                                Surface(
                                    onClick = { selectedPlaceCategory = category },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) Primary else Color(0xFFF0F0F0),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = emoji, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = category,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else Color.Black
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Results count
                        Text(
                            text = "${filteredPlaces.size} places found",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Places list
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filteredPlaces) { place ->
                                Surface(
                                    onClick = {
                                        // Always set as dropoff - this is where rider wants to go
                                        viewModel.setDropoffLocation(place.name, place.position)
                                        showPlacesSearchDialog = false
                                        placesSearchQuery = ""
                                        // Move camera to place
                                        coroutineScope.launch {
                                            cameraPositionState.animate(
                                                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(place.position, 16f)
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8F8F8)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // Category emoji
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = when(place.category) {
                                                "Docks" -> Color(0xFF0288D1).copy(alpha = 0.1f)
                                                "Restaurants" -> Color(0xFFE91E63).copy(alpha = 0.1f)
                                                "Hotels" -> Color(0xFF9C27B0).copy(alpha = 0.1f)
                                                "Islands" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                                "Beaches" -> Color(0xFFFF9800).copy(alpha = 0.1f)
                                                "Resorts" -> Color(0xFF00BCD4).copy(alpha = 0.1f)
                                                "Dolphin Sites" -> Color(0xFF03A9F4).copy(alpha = 0.1f)
                                                "Island Hopping" -> Color(0xFF2196F3).copy(alpha = 0.1f)
                                                "Tours" -> Color(0xFF673AB7).copy(alpha = 0.1f)
                                                else -> Color.Gray.copy(alpha = 0.1f)
                                            },
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = place.emoji,
                                                    fontSize = 22.sp
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = place.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.Black,
                                                maxLines = 2
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            // Show description - allow multiple lines
                                            if (place.description.isNotEmpty()) {
                                                Text(
                                                    text = place.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.DarkGray,
                                                    maxLines = 3,
                                                    lineHeight = 16.sp
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = when(place.category) {
                                                        "Docks" -> Color(0xFF0288D1).copy(alpha = 0.15f)
                                                        "Restaurants" -> Color(0xFFE91E63).copy(alpha = 0.15f)
                                                        "Hotels" -> Color(0xFF9C27B0).copy(alpha = 0.15f)
                                                        "Islands" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                                        "Beaches" -> Color(0xFFFF9800).copy(alpha = 0.15f)
                                                        "Resorts" -> Color(0xFF00BCD4).copy(alpha = 0.15f)
                                                        "Dolphin Sites" -> Color(0xFF03A9F4).copy(alpha = 0.15f)
                                                        "Island Hopping" -> Color(0xFF2196F3).copy(alpha = 0.15f)
                                                        "Tours" -> Color(0xFF673AB7).copy(alpha = 0.15f)
                                                        else -> Color.Gray.copy(alpha = 0.15f)
                                                    }
                                                ) {
                                                    Text(
                                                        text = place.category,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = when(place.category) {
                                                            "Docks" -> Color(0xFF0288D1)
                                                            "Restaurants" -> Color(0xFFE91E63)
                                                            "Hotels" -> Color(0xFF9C27B0)
                                                            "Islands" -> Color(0xFF4CAF50)
                                                            "Beaches" -> Color(0xFFFF9800)
                                                            "Resorts" -> Color(0xFF00BCD4)
                                                            "Dolphin Sites" -> Color(0xFF03A9F4)
                                                            "Island Hopping" -> Color(0xFF2196F3)
                                                            "Tours" -> Color(0xFF673AB7)
                                                            else -> Color.Gray
                                                        },
                                                        fontWeight = FontWeight.Medium,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Tap to set dropoff →",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFFFF5722),
                                                    fontWeight = FontWeight.Medium
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
        }
        
        // Location error message overlay
        showLocationError?.let { errorMessage ->
            LaunchedEffect(errorMessage) {
                kotlinx.coroutines.delay(3000)
                showLocationError = null
            }
            
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 120.dp)
            ) {
                Surface(
                    color = Color(0xFFD32F2F),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = errorMessage,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        // Driver Fare Adjustment Dialog - for when driver proposes price change while finding driver
        if (uiState.showFareAdjustmentDialog && uiState.activeBooking?.status == BookingStatus.PENDING) {
            // Play sound and vibrate
            LaunchedEffect(Unit) {
                try {
                    val mediaPlayer = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .build()
                        )
                        setDataSource(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        prepare()
                        start()
                    }
                    
                    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                        vibratorManager.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
                    }
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
                    }
                    android.util.Log.d("BookRide", "Price change notification played")
                } catch (e: Exception) {
                    android.util.Log.e("BookRide", "Error playing notification", e)
                }
            }
            
            FareAdjustmentNotificationDialog(
                originalFare = uiState.activeBooking?.estimatedFare ?: 0.0,
                adjustedFare = uiState.driverAdjustedFare ?: 0.0,
                reason = uiState.fareAdjustmentReason ?: "Driver adjustment",
                isNightRate = uiState.isNightRate,
                driverName = uiState.fareAdjustmentDriverName,
                onAccept = { viewModel.acceptAdjustedFare() },
                onDecline = { viewModel.declineAdjustedFare() },
                onDismiss = { viewModel.dismissFareAdjustmentDialog() }
            )
        }
        
        // Selected Ad Bottom Sheet - same style as Explore/Home screens
        uiState.selectedMapAd?.let { ad ->
            val adSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { viewModel.selectMapAd(null) },
                sheetState = adSheetState
            ) {
                DealDetailsSheetBooking(
                    ad = ad,
                    onNavigateTo = {
                        // Set as dropoff location
                        ad.location?.let { loc ->
                            viewModel.setDropoffFromAd(LatLng(loc.latitude, loc.longitude), ad.locationName ?: ad.businessName)
                        }
                        viewModel.selectMapAd(null)
                    },
                    onCall = { phone ->
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        context.startActivity(intent)
                    },
                    onWebsite = { url ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    },
                    onDismiss = { viewModel.selectMapAd(null) }
                )
            }
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
    
    // Navigate to rating screen when ride is completed
    LaunchedEffect(booking?.status) {
        if (booking?.status == BookingStatus.COMPLETED) {
            android.util.Log.d("RideTracking", "Ride completed! Navigating to rating screen")
            onRideComplete()
        }
    }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            uiState.currentLocation ?: LatLng(9.4073, -82.3421), // Default to Bocas del Toro, Panama
            17f
        )
    }
    
    // Move camera to driver location when it updates
    LaunchedEffect(uiState.driverLocation) {
        uiState.driverLocation?.let { driverLoc ->
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(driverLoc, 18f)
            )
        }
    }
    
    // Play sound and vibrate when driver proposes fare change
    LaunchedEffect(uiState.showFareAdjustmentDialog) {
        if (uiState.showFareAdjustmentDialog) {
            try {
                // Play notification sound
                val mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .build()
                    )
                    setDataSource(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    prepare()
                    start()
                }
                
                // Vibrate
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 500, 200, 500), -1)
                }
                
                android.util.Log.d("RideTracking", "Price change notification sound played")
            } catch (e: Exception) {
                android.util.Log.e("RideTracking", "Error playing notification", e)
            }
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
                myLocationButtonEnabled = false, // Custom button instead for better zoom
                    zoomControlsEnabled = false // Disabled - using custom controls
                )
            ) {
                // "You are here" marker at user location - updates in real-time
                uiState.currentLocation?.let { userLoc ->
                    val userMarkerState = rememberMarkerState(position = userLoc)
                    LaunchedEffect(userLoc) {
                        userMarkerState.position = userLoc
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
                }
                
                // Driver location marker - shows boat or car icon, updates in real-time
                uiState.driverLocation?.let { driverLoc ->
                    val driverMarkerState = rememberMarkerState(position = driverLoc)
                    
                    // Update marker position when driver moves
                    LaunchedEffect(driverLoc) {
                        driverMarkerState.position = driverLoc
                    }
                    
                    Marker(
                        state = driverMarkerState,
                        title = if (booking?.vehicleType == VehicleType.BOAT) "🚤 Captain" else "🚕 Driver",
                        snippet = when (booking?.status) {
                            BookingStatus.ACCEPTED -> stringResource(R.string.on_the_way_to_you)
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
                // My Location button - zooms in to current location
                Surface(
                    onClick = { 
                        coroutineScope.launch { 
                            uiState.currentLocation?.let { location ->
                                cameraPositionState.animate(
                                    com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(location, 19f)
                                )
                            }
                        }
                    },
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.MyLocation, "My Location", tint = Color(0xFF1976D2))
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
                                            BookingStatus.ARRIVED -> if (booking.vehicleType == VehicleType.BOAT) stringResource(R.string.captain_has_arrived) else stringResource(R.string.driver_has_arrived)
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
                                // Show price only if driver has accepted/set price
                                if (booking.acceptedPrice != null && booking.acceptedPrice > 0) {
                                    Text(
                                        text = "$${String.format("%.2f", booking.acceptedPrice)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                } else if (booking.status == BookingStatus.PENDING) {
                                    Text(
                                        text = "Awaiting offers",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
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
                        
                        // Price - only show if driver has set a price through offers
                        if (booking.acceptedPrice != null && booking.acceptedPrice > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Agreed Price",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "$${String.format("%.2f", booking.acceptedPrice)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }
                        } else if (booking.status == BookingStatus.PENDING) {
                            // Waiting for offers
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Price",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Drivers will offer",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Primary
                                )
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
                    driverName = uiState.fareAdjustmentDriverName,
                    onAccept = { viewModel.acceptAdjustedFare() },
                    onDecline = { viewModel.declineAdjustedFare() },
                    onDismiss = { viewModel.dismissFareAdjustmentDialog() }
                )
            }
        }
    }
}

/**
 * Simple rating popup screen - just 5 stars to rate the driver/captain
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideCompleteScreen(
    bookingId: String,
    onNavigateHome: () -> Unit,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var rating by remember { mutableStateOf(5) }
    
    LaunchedEffect(bookingId) {
        viewModel.loadCompletedBooking(bookingId)
    }
    
    val booking = uiState.completedBooking
    val isBoat = booking?.vehicleType == VehicleType.BOAT
    val driverName = booking?.driverName ?: if (isBoat) "Captain" else "Driver"
    val driverPhoto = booking?.driverPhotoUrl
    
    // Full screen with centered dialog-style card
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success checkmark
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(50),
                    color = Success.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Success,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Title
                Text(
                    text = stringResource(R.string.ride_complete_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Driver profile photo
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(50),
                    color = Primary.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(3.dp, Primary)
                ) {
                    if (driverPhoto != null) {
                        AsyncImage(
                            model = driverPhoto,
                            contentDescription = stringResource(R.string.driver_photo),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (isBoat) Icons.Default.Sailing else Icons.Default.LocalTaxi,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Driver name
                Text(
                    text = driverName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                // Rate your driver/captain subtitle
                Text(
                    text = if (isBoat) stringResource(R.string.your_captain) else stringResource(R.string.your_driver),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Simple 5-star rating
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (1..5).forEach { star ->
                        IconButton(
                            onClick = { rating = star },
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(
                                if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Rate $star stars",
                                tint = if (star <= rating) Warning else TextSecondary.copy(alpha = 0.4f),
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                }
                
                // Star count text
                Text(
                    text = "$rating star${if (rating != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Submit button
                Button(
                    onClick = {
                        viewModel.submitRating(bookingId, rating, "", 0.0)
                        onNavigateHome()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isSubmitting
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.submit_rating),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Skip option
                TextButton(onClick = onNavigateHome) {
                    Text(
                        text = stringResource(R.string.skip_rating),
                        color = TextSecondary
                    )
                }
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
    val context = LocalContext.current
    
    // Check if local image exists
    val hasLocalImage = remember(ad.imageUrl) {
        if (ad.imageUrl.isNullOrBlank()) false
        else if (ad.imageUrl.startsWith("/")) File(ad.imageUrl).exists()
        else true
    }
    
    // Check if it's a direct video URL (MP4, etc)
    val isDirectVideo = remember(ad.videoUrl, ad.youtubeUrl) {
        val url = ad.videoUrl ?: ad.youtubeUrl ?: ""
        url.endsWith(".mp4", ignoreCase = true) ||
        url.endsWith(".webm", ignoreCase = true) ||
        url.endsWith(".m3u8", ignoreCase = true) ||
        url.contains(".mp4", ignoreCase = true)
    }
    
    val directVideoUrl = remember(ad.videoUrl, ad.youtubeUrl) {
        when {
            !ad.videoUrl.isNullOrBlank() -> ad.videoUrl
            ad.youtubeUrl?.contains(".mp4", ignoreCase = true) == true -> ad.youtubeUrl
            else -> null
        }
    }
    
    // Extract YouTube video ID (only for actual YouTube URLs)
    val youtubeVideoId = remember(ad.youtubeUrl) {
        if (isDirectVideo) null // Not YouTube if it's a direct video
        else ad.youtubeUrl?.let { url ->
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
    
    // ExoPlayer for direct video playback
    val exoPlayer = remember(directVideoUrl) {
        if (directVideoUrl != null) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(directVideoUrl))
                repeatMode = Player.REPEAT_MODE_ALL
                prepare()
            }
        } else null
    }
    
    // Cleanup ExoPlayer on dispose
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer?.release()
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .heightIn(max = 380.dp)
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, Primary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            // Close button at top right
            Box(modifier = Modifier.fillMaxWidth()) {
                // Business name header with gradient background
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Logo
                            val logoPath = ad.logoUrl ?: ad.imageUrl
                            val logoExists = remember(logoPath) {
                                if (logoPath.isNullOrBlank()) false
                                else if (logoPath.startsWith("/")) File(logoPath).exists()
                                else true
                            }
                            
                            Surface(
                                modifier = Modifier.size(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
                            ) {
                                if (logoExists && !logoPath.isNullOrBlank()) {
                                    AsyncImage(
                                        model = if (logoPath!!.startsWith("/")) File(logoPath) else logoPath,
                                        contentDescription = "Logo",
                                        modifier = Modifier.fillMaxSize().padding(4.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = ad.category.getIcon(),
                                            fontSize = 24.sp
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = ad.businessName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        // Close button
                        Surface(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp),
                            shape = RoundedCornerShape(50),
                            color = Color.White,
                            shadowElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Close, 
                                    "Close",
                                    modifier = Modifier.size(18.dp),
                                    tint = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
            
            // Image section - full width with border, no cut-off
            if (hasLocalImage && !ad.imageUrl.isNullOrBlank() && directVideoUrl == null) {
                val imageModel = remember(ad.imageUrl) {
                    if (ad.imageUrl.startsWith("/")) File(ad.imageUrl) else ad.imageUrl
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .border(
                            width = 2.dp,
                            color = Primary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8F8F8)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = ad.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 160.dp)
                            .padding(4.dp),
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.Center
                    )
                }
            } else if (directVideoUrl != null && exoPlayer != null) {
                // Direct MP4/video playback with ExoPlayer
                var isPlaying by remember { mutableStateOf(false) }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color.Black)
                        .clip(RoundedCornerShape(0.dp))
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = true
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Play/Pause overlay when not playing
                    if (!isPlaying) {
                        Surface(
                            onClick = { 
                                exoPlayer.play()
                                isPlaying = true
                            },
                            modifier = Modifier.align(Alignment.Center).size(64.dp),
                            shape = RoundedCornerShape(32.dp),
                            color = Primary.copy(alpha = 0.9f),
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("▶", color = Color.White, fontSize = 28.sp)
                            }
                        }
                    }
                }
                
                // Update playing state
                LaunchedEffect(exoPlayer) {
                    exoPlayer.addListener(object : Player.Listener {
                        override fun onIsPlayingChanged(playing: Boolean) {
                            isPlaying = playing
                        }
                    })
                }
            } else if (youtubeVideoId != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color.Black)
                        .clickable {
                            try {
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
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    // Play button overlay
                    Surface(
                        modifier = Modifier.align(Alignment.Center).size(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = Color(0xFFFF0000).copy(alpha = 0.95f),
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("▶", color = Color.White, fontSize = 24.sp)
                        }
                    }
                    // Tap to play label
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Black.copy(alpha = 0.75f)
                    ) {
                        Text(
                            text = "▶ Tap to watch video",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            
            // Content section
            Column(modifier = Modifier.padding(12.dp)) {
                // Description
                if (ad.description.isNotBlank()) {
                    Text(
                        text = ad.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Location with icon
                if (ad.locationName != null) {
                    Surface(
                        color = Surface,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.Place,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = Primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = ad.locationName,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                // Coupon section
                if (ad.hasCoupon && ad.couponCode != null && AdHelper.isCouponValid(ad)) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, Success.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎟️", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                if (!ad.couponDiscount.isNullOrBlank()) {
                                    Text(
                                        text = ad.couponDiscount,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Success
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, Success.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "CODE: ${ad.couponCode}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Success,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                            if (!ad.couponDescription.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
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
    
    // Auto-scroll every 5 seconds
    LaunchedEffect(ads) {
        if (ads.isNotEmpty()) {
            while (true) {
                kotlinx.coroutines.delay(5000)
                currentAdIndex = (currentAdIndex + 1) % ads.size
            }
        }
    }
    
    val currentAd = ads.getOrNull(currentAdIndex) ?: return
    
    // Professional card design with full image display
    Card(
        onClick = { onAdClick(currentAd) },
        modifier = modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Image section - bordered, full display, no cut-off
            val hasLocalImage = remember(currentAd.imageUrl) {
                if (currentAd.imageUrl.isNullOrBlank()) false
                else if (currentAd.imageUrl.startsWith("/")) File(currentAd.imageUrl).exists()
                else true
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(
                        width = 2.dp,
                        color = Primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5))
                    .heightIn(min = 120.dp, max = 180.dp),
                contentAlignment = Alignment.Center
            ) {
                if (hasLocalImage && !currentAd.imageUrl.isNullOrBlank()) {
                    val imageModel = remember(currentAd.imageUrl) {
                        if (currentAd.imageUrl.startsWith("/")) File(currentAd.imageUrl) else currentAd.imageUrl
                    }
                    AsyncImage(
                        model = imageModel,
                        contentDescription = currentAd.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 180.dp),
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.Center
                    )
                } else {
                    // Placeholder with business initial
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Surface(
                            modifier = Modifier.size(60.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Primary.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = currentAd.businessName.take(2).uppercase(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }
                        }
                    }
                }
            }
            
            // Business info section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentAd.businessName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (currentAd.hasCoupon && currentAd.couponDiscount != null) {
                        Text(
                            text = "🎟️ ${currentAd.couponDiscount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Success,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (currentAd.description.isNotBlank()) {
                        Text(
                            text = currentAd.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Counter badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Primary.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${currentAdIndex + 1}/${ads.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "View",
                            modifier = Modifier.size(16.dp),
                            tint = Primary
                        )
                    }
                }
            }
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
    driverName: String? = null,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onDismiss: () -> Unit
) {
    val displayDriverName = driverName ?: if (isNightRate) "Captain" else "Driver"
    
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
                text = "$displayDriverName accepted your ride!",
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
                    text = "$displayDriverName has proposed a different price:",
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
                            stringResource(R.string.search_docks_places_businesses),
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
                    textStyle = LocalTextStyle.current.copy(color = TextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = Primary,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Divider,
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface
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
                        text = stringResource(R.string.quick_search),
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
                        QuickSearchChip("⚓ Docks", onClick = { onSearchQueryChange("dock") })
                        QuickSearchChip("🚤 Marinas", onClick = { onSearchQueryChange("marina") })
                        QuickSearchChip("⛴️ Ferry", onClick = { onSearchQueryChange("ferry") })
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Row 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickSearchChip("🏨 Hotels", onClick = { onSearchQueryChange("hotel") })
                        QuickSearchChip("🍽️ Food", onClick = { onSearchQueryChange("restaurant") })
                        QuickSearchChip("🏖️ Beach", onClick = { onSearchQueryChange("beach") })
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Row 3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickSearchChip("🍺 Bars", onClick = { onSearchQueryChange("bar") })
                        QuickSearchChip("🎉 Tours", onClick = { onSearchQueryChange("tour") })
                        QuickSearchChip("🌴 Island", onClick = { onSearchQueryChange("isla") })
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Row 4
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickSearchChip("🏄 Surf", onClick = { onSearchQueryChange("surf") })
                        QuickSearchChip("🤿 Snorkel", onClick = { onSearchQueryChange("snorkel") })
                        QuickSearchChip("🧘 Yoga", onClick = { onSearchQueryChange("yoga") })
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "Popular Dock Locations",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // Popular dock locations - expanded
                    listOf(
                        "⚓ Main Water Taxi Dock" to "Bocas Town - Primary taxi stand",
                        "⚓ Bocas Docks" to "Bocas Town - Central boat docks",
                        "⚓ Carenero Dock" to "Carenero Island - Main dock",
                        "⚓ Bastimentos Dock" to "Bastimentos - Main dock",
                        "⛴️ Palanga Ferry" to "Bocas - Almirante ferry",
                        "⛴️ Ferry Almirante" to "Mainland ferry to Bocas",
                        "🏝️ Red Frog Marina" to "Bastimentos - Beach dock",
                        "🏨 Hotel Palma Royale" to "Bocas Town - Hotel dock",
                        "🏨 Selina Bocas" to "Bocas Town - Hostel dock",
                        "🍽️ Bibi's on the Beach" to "Carenero - Restaurant dock"
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
            verticalAlignment = Alignment.Top
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
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = result.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 4,
                    lineHeight = 16.sp,
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

@Composable
private fun HelpDialog(onDismiss: () -> Unit) {
    var selectedSection by remember { mutableStateOf(0) }
    
    val sections = listOf(
        "📱 Getting Started",
        "🚤 How to Book",
        "🔍 Search",
        "📍 Map Features",
        "🏝️ Island Deals",
        "💳 Subscription",
        "👤 Driver Mode",
        "🗺️ Explore Map",
        "⚙️ Tips & FAQ"
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Surface(
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "❓",
                                fontSize = 28.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "How to Use OmniMap",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Complete Guide",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }
                
                // Section tabs - horizontal scroll
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(sections) { index, section ->
                        Surface(
                            onClick = { selectedSection = index },
                            shape = RoundedCornerShape(20.dp),
                            color = if (selectedSection == index) Color(0xFF9C27B0) else Color.White,
                            border = if (selectedSection == index) null else BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Text(
                                text = section,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selectedSection == index) Color.White else Color.DarkGray,
                                fontWeight = if (selectedSection == index) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
                
                // Content based on selected section
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    when (selectedSection) {
                        0 -> GettingStartedSection()
                        1 -> HowToBookSection()
                        2 -> SearchNavigationSection()
                        3 -> MapFeaturesSection()
                        4 -> IslandDealsSection()
                        5 -> SubscriptionSection()
                        6 -> DriverModeSection()
                        7 -> ExploreMapSection()
                        8 -> SettingsTipsSection()
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF6A1B9A),
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun HelpStep(number: Int, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFF6A1B9A),
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "$number",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF424242),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun HelpTip(emoji: String, text: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFEDE7F6)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF37474F),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun GettingStartedSection() {
    HelpSectionTitle("📱 Getting Started with OmniMap")
    
    Text(
        text = "Welcome to OmniMap - Everything. Everywhere. Live. Explore the world and book rides in Bocas del Toro!",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = Color.Black,
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    HelpStep(
        number = 1,
        title = "Open the App",
        description = "When you open the app, you'll see a map of Bocas del Toro with your current location (blue dot) and all available docks, hotels, restaurants, and points of interest marked on the map."
    )
    
    HelpStep(
        number = 2,
        title = "Allow Location Access",
        description = "Make sure to allow location access so the app can show where you are and help drivers find you. Your blue dot shows your exact position on the map."
    )
    
    HelpStep(
        number = 3,
        title = "Explore the Map",
        description = "Use two fingers to zoom in/out, or use the +/- buttons on the left side. Tap on any marker to see details about that location."
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    HelpTip("💡", "The app shows real-time boat and taxi drivers online. Look for the boat 🚤 and taxi 🚕 icons moving on the map!")
    
    HelpTip("🌐", "Tap the flag button (🇺🇸/🇪🇸) to switch between English and Spanish.")
}

@Composable
private fun HowToBookSection() {
    HelpSectionTitle("🚤 How to Book a Boat Taxi")
    
    Text(
        text = "Follow these steps to book your water taxi ride:",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF424242),
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    HelpStep(
        number = 1,
        title = "Set Your Pickup Location",
        description = "Tap on the map where you want to be picked up, OR tap the green 'Pickup' button at the bottom and search for a dock/location. Your pickup point will show as a GREEN marker."
    )
    
    HelpStep(
        number = 2,
        title = "Set Your Dropoff Location",
        description = "Tap on another location on the map for your destination, OR tap the red 'Dropoff' button and search. Your dropoff point shows as a RED marker."
    )
    
    HelpStep(
        number = 3,
        title = "See the Route",
        description = "Once both locations are set, you'll see a BLUE line showing the boat route across the water."
    )
    
    HelpStep(
        number = 4,
        title = "Choose Number of Passengers",
        description = "Use the +/- buttons to set how many passengers (1-10). The price adjusts per person for shared rides."
    )
    
    HelpStep(
        number = 5,
        title = "Tap 'Confirm Booking'",
        description = "Press the big button at the bottom to send your booking request. Your request goes to ALL online boat drivers in the area."
    )
    
    HelpStep(
        number = 6,
        title = "Wait for a Driver to Accept",
        description = "The panel turns YELLOW showing 'Finding driver...'. Drivers see your request and can accept. This usually takes 1-5 minutes."
    )
    
    HelpStep(
        number = 7,
        title = "Driver Accepts with Price - You're Booked!",
        description = "When a driver accepts, they will confirm the ride price. The panel turns GREEN showing the driver's name, photo, rating, boat info, phone number, and the confirmed price. They're on their way!"
    )
    
    HelpStep(
        number = 8,
        title = "Track Your Driver",
        description = "Watch the driver's boat icon move toward your pickup location in real-time. You'll see 'Driver on the way' or 'Driver arrived' status."
    )
    
    HelpStep(
        number = 9,
        title = "Call Your Driver (if needed)",
        description = "Tap the phone icon to call your driver directly if you need to communicate pickup details."
    )
    
    HelpStep(
        number = 10,
        title = "Enjoy Your Ride & Pay",
        description = "Board the boat, enjoy the ride! Payment is made directly to the driver in CASH (USD) at the end of your trip."
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    HelpTip("⚠️", "Once you send a booking request, please wait for a driver to accept. NO CANCELLATIONS are allowed after booking.")
    
    HelpTip("⭐", "After your ride, you can rate your driver 1-5 stars. Good ratings help other riders choose great drivers!")
    
    HelpTip("💰", "The driver will confirm the final price when they accept your ride. Pay in CASH (USD) at the end of your trip.")
}

@Composable
private fun SearchNavigationSection() {
    HelpSectionTitle("🔍 Search & Navigation")
    
    Text(
        text = "Two ways to search for places:",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFE3F2FD)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔍 Magnifying Glass Button (Top Left)",
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "• Searches EVERYTHING: docks, businesses, drivers, hotels, restaurants\n• Shows quick search chips: Docks, Marinas, Ferry, Hotels, Food, Beach, Bars, Tours, Island, Surf, Snorkel, Yoga\n• Type any name to find it instantly\n• Results show location on map when tapped",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF424242),
                lineHeight = 22.sp
            )
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFE8F5E9)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔍 'Search Dock & Places' Button (Top Right)",
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "• Shows ALL 85+ locations organized by category\n• Filter by: All, Docks, Restaurants, Hotels, Resorts, Islands, Beaches, Dolphin Sites, Island Hopping, Tours\n• Tap any result to SET IT AS YOUR DROPOFF\n• Shows full descriptions of each place\n• Great for exploring what's available in Bocas!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF424242),
                lineHeight = 22.sp
            )
        }
    }
    
    Spacer(modifier = Modifier.height(12.dp))
    
    HelpTip("🎯", "Tap 'Search Dock & Places' then browse categories to discover beaches, restaurants, and hotels you might not know about!")
    
    HelpTip("📍", "When you tap a search result in 'Search Dock & Places', it automatically sets that location as your DROPOFF destination.")
}

@Composable
private fun MapFeaturesSection() {
    HelpSectionTitle("📍 Map Features")
    
    Text(
        text = "Understanding the map buttons and markers:",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF424242),
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    // Top Left Buttons
    Text(
        text = "🔲 Top Left Buttons:",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    
    HelpTip("🏠", "HOME button - Returns to the main booking screen from anywhere")
    HelpTip("🇺🇸", "LANGUAGE button - Tap to switch between English and Spanish")
    HelpTip("🔍", "SEARCH button - Opens the main search dialog")
    HelpTip("🏝️", "ISLAND DEALS - Show/Hide business ads on the map (green = showing)")
    
    Spacer(modifier = Modifier.height(12.dp))
    
    // Top Right Buttons
    Text(
        text = "🔲 Top Right Buttons:",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    
    HelpTip("❓", "HELP button - Opens this guide you're reading now!")
    HelpTip("🚤/🚕", "DRIVERS ONLINE - Shows count of boat and taxi drivers online. Tap to show/hide driver icons on map")
    HelpTip("�", "USERS ONLINE - Shows total users with the app open (passengers + drivers). Updates in real-time!")
    HelpTip("�🔍", "SEARCH DOCK & PLACES - Opens category-based search with 85+ locations")
    HelpTip("👁️", "SHOW/HIDE DOCKS - Toggle all dock/location markers on the map")
    
    Spacer(modifier = Modifier.height(12.dp))
    
    // Map Markers
    Text(
        text = "📍 Map Markers:",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    
    HelpTip("🔵", "BLUE DOT - Your current location (updates in real-time as you move!)")
    HelpTip("📍", "YOU ARE HERE - Blue marker showing your exact position, moves with you in real-time")
    HelpTip("🟢", "GREEN MARKER - Your pickup location")
    HelpTip("🔴", "RED MARKER - Your dropoff location")
    HelpTip("⚓", "ANCHOR ICONS - Dock/marina locations")
    HelpTip("🚤", "BOAT ICONS - Online boat drivers (moving in real-time)")
    HelpTip("🚕", "TAXI ICONS - Online taxi drivers")
    HelpTip("🏷️", "AD MARKERS - Business advertisements (Island Deals) - tap to see details")
    
    Spacer(modifier = Modifier.height(12.dp))
    
    // Zoom Controls
    Text(
        text = "🔎 Zoom Controls:",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    
    HelpTip("➕", "+ button (left side) - Zoom in for closer view")
    HelpTip("➖", "- button (left side) - Zoom out for wider view")
    HelpTip("👆", "Pinch with two fingers to zoom in/out")
    HelpTip("👆👆", "Double-tap to zoom in quickly")
}

@Composable
private fun IslandDealsSection() {
    HelpSectionTitle("🏝️ Island Deals & Business Ads")
    
    Text(
        text = "Discover local businesses and special deals!",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF424242),
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    HelpStep(
        number = 1,
        title = "See Ads on the Map",
        description = "Business ads appear as colorful markers on the map. Tap any ad marker to see the full business details, photos, description, and contact info."
    )
    
    HelpStep(
        number = 2,
        title = "Toggle Ads On/Off",
        description = "Tap the '🏝️ Show Deals' / 'Hide Deals' button at the top to show or hide all business ads on the map. Green means ads are visible."
    )
    
    HelpStep(
        number = 3,
        title = "View Business Details",
        description = "When you tap an ad, a popup shows: Business name, category, description, photo, phone number (tap to call), and exact location."
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = "🏪 Want to Advertise YOUR Business?",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    
    Text(
        text = "Business owners can create ads to appear on the map for all OmniMap users!",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF424242),
        modifier = Modifier.padding(bottom = 12.dp)
    )
    
    HelpStep(
        number = 1,
        title = "Go to Home Screen",
        description = "Tap the HOME button (🏠) to go back to the main menu."
    )
    
    HelpStep(
        number = 2,
        title = "Tap 'Island Deals'",
        description = "Find the 'Island Deals' card on the home screen and tap it."
    )
    
    HelpStep(
        number = 3,
        title = "Create Your Ad",
        description = "Fill in your business name, select a category (Restaurant, Hotel, Tour, etc.), add a description, upload a photo, and set your location on the map."
    )
    
    HelpStep(
        number = 4,
        title = "Add Contact Info",
        description = "Enter your phone number and any other contact details so customers can reach you."
    )
    
    HelpStep(
        number = 5,
        title = "Choose Ad Duration",
        description = "Select how long you want your ad to run (1 week, 1 month, 3 months, etc.). Longer durations offer better value!"
    )
    
    HelpStep(
        number = 6,
        title = "Submit & Pay",
        description = "Review your ad and submit. Payment is required to activate your ad on the map."
    )
    
    HelpTip("💰", "Ad pricing varies by duration. Contact support for current rates and special promotions!")
}

@Composable
private fun SubscriptionSection() {
    HelpSectionTitle("💳 Subscription Plans")
    
    Text(
        text = "Unlock premium features with a subscription!",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF424242),
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    Text(
        text = "🚤 For Drivers:",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFF3E0),
        border = BorderStroke(1.dp, Color(0xFFFF9800))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Driver Subscription Benefits:",
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "✅ Appear on the map for riders to see\n✅ Receive booking requests from riders\n✅ Set your own prices per route\n✅ Track your earnings and trips\n✅ Build your rating and reputation\n✅ Accept unlimited bookings",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF424242),
                lineHeight = 24.sp
            )
        }
    }
    
    Text(
        text = "How to Subscribe as a Driver:",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
    )
    
    HelpStep(
        number = 1,
        title = "Go to Home Screen",
        description = "Tap HOME (🏠) to go to the main menu."
    )
    
    HelpStep(
        number = 2,
        title = "Tap 'Driver Mode'",
        description = "Find and tap the 'Driver Mode' option."
    )
    
    HelpStep(
        number = 3,
        title = "Register as Driver",
        description = "Fill in your details: full name, phone number, vehicle type (boat or taxi), license plate, and upload a photo."
    )
    
    HelpStep(
        number = 4,
        title = "Choose Subscription Plan",
        description = "Select your subscription duration: Weekly, Monthly, or Yearly. Longer plans offer better savings!"
    )
    
    HelpStep(
        number = 5,
        title = "Complete Payment",
        description = "Pay via the available payment methods. Your subscription activates immediately."
    )
    
    HelpStep(
        number = 6,
        title = "Go Online!",
        description = "Once subscribed, toggle 'Online' to start appearing on the map and receiving booking requests!"
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    HelpTip("💡", "Free trial may be available for new drivers! Check the Driver Mode screen for current promotions.")
    
    HelpTip("📱", "Your subscription renews automatically. You can cancel anytime from your profile settings.")
}

@Composable
private fun DriverModeSection() {
    HelpSectionTitle("👤 Driver Mode")
    
    Text(
        text = "Become a boat taxi driver and earn money!",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF424242),
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    HelpStep(
        number = 1,
        title = "Access Driver Mode",
        description = "From the Home screen, tap 'Driver Mode' or 'Become a Driver' to switch to the driver interface."
    )
    
    HelpStep(
        number = 2,
        title = "Register Your Vehicle",
        description = "Enter your boat/taxi information: type, capacity, license plate, and a photo of your vehicle."
    )
    
    HelpStep(
        number = 3,
        title = "Set Your Prices",
        description = "Go to 'My Prices' to set your rates for different routes. You control how much you charge per trip!"
    )
    
    HelpStep(
        number = 4,
        title = "Go Online",
        description = "Toggle the 'Online' switch to start appearing on the rider's map. Your location updates in real-time."
    )
    
    HelpStep(
        number = 5,
        title = "Receive Booking Requests",
        description = "When a rider books, you'll hear a sound and see the request with pickup/dropoff locations and passenger count. Tap 'Accept' or 'Decline'."
    )
    
    HelpStep(
        number = 6,
        title = "Navigate to Pickup",
        description = "After accepting, navigate to the rider's pickup location. The app shows their exact position. Tap 'Arrived' when you get there."
    )
    
    HelpStep(
        number = 7,
        title = "Start the Trip",
        description = "Once the rider boards, tap 'Start Trip' to begin the ride."
    )
    
    HelpStep(
        number = 8,
        title = "Complete the Trip",
        description = "When you arrive at the dropoff, tap 'Complete Trip'. Collect payment in CASH from the rider."
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = "📊 Driver Features:",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    
    HelpTip("💵", "MY PRICES - Set custom prices for each route (e.g., Bocas to Carenero = $3)")
    HelpTip("📈", "EARNINGS - View your daily, weekly, and monthly earnings")
    HelpTip("⭐", "RATINGS - See your average rating from riders")
    HelpTip("📋", "TRIP HISTORY - Review all your completed trips")
    HelpTip("🔔", "NOTIFICATIONS - Get alerts for new booking requests")
}

@Composable
private fun ExploreMapSection() {
    HelpSectionTitle("🗺️ Explore Map Features")
    
    Text(
        text = "Discover places as you walk or drive around Bocas!",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF424242),
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    HelpStep(
        number = 1,
        title = "Real-Time Location Tracking",
        description = "Your 'You Are Here' marker updates in real-time as you move. Watch it follow you on the map!"
    )
    
    HelpStep(
        number = 2,
        title = "Live Place Discovery",
        description = "When you get within 50 feet (~15 meters) of a registered place, deal, or business, the app automatically shows you info about it!"
    )
    
    HelpStep(
        number = 3,
        title = "See Who's Online",
        description = "The 'X Online' badge shows total users with the app open right now - both passengers and drivers. Great for gauging activity!"
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    HelpTip("🚶", "WALK AROUND - The app detects when you're near a business and shows you deals automatically")
    HelpTip("📍", "REAL-TIME - Your location marker moves with you as you explore the area")
    HelpTip("👥", "ONLINE COUNT - See how many people are using OmniMap right now in Bocas")
    HelpTip("🔎", "SEARCH ALL - Search for 'dock' to see ALL 85+ dock locations at once")
}

@Composable
private fun SettingsTipsSection() {
    HelpSectionTitle("⚙️ Settings & Pro Tips")
    
    Text(
        text = "Get the most out of OmniMap!",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF424242),
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    Text(
        text = "💡 Pro Tips:",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    
    HelpTip("⏰", "BEST TIMES: Book early morning or late afternoon when more drivers are available")
    HelpTip("📍", "BE PRECISE: Set your pickup at an exact dock for faster pickups")
    HelpTip("💬", "COMMUNICATE: Call your driver if the pickup spot is tricky to find")
    HelpTip("💵", "CASH READY: Have small bills ready - drivers may not have change for large bills")
    HelpTip("👥", "GROUP RIDES: Traveling with friends? Set passenger count to get accurate shared pricing")
    HelpTip("⭐", "RATE DRIVERS: Good ratings help great drivers stand out!")
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = "❓ Frequently Asked Questions:",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Q: How do I pay?", fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = "A: Payment is CASH directly to the driver at the end of your trip. USD is accepted everywhere in Bocas.", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF424242))
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Q: What if no driver accepts?", fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = "A: If no driver accepts within 5-10 minutes, try rebooking. Drivers may be busy or offline. You can also try during busier times.", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF424242))
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Q: Can I schedule a ride in advance?", fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = "A: Currently, OmniMap rides are available in Bocas del Toro. For scheduled trips, contact a driver directly after your first ride.", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF424242))
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Q: Is my location shared?", fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = "A: Your location is only shared with your driver AFTER they accept your booking. It's used to help them find you.", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF424242))
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Q: What about bad weather?", fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = "A: Boat drivers may decline trips in dangerous weather conditions for safety. Taxi drivers may still be available on Isla Colon.", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF424242))
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = "📞 Need Help?",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    
    HelpTip("📧", "Email: support@omnimap.app")
    HelpTip("📱", "WhatsApp: Contact us for quick support")
    HelpTip("🌐", "Website: www.omnimap.app")
}

/**
 * Deal Details Sheet for Booking Screen - Shows ad details in a bottom sheet
 */
@Composable
private fun DealDetailsSheetBooking(
    ad: Advertisement,
    onNavigateTo: () -> Unit,
    onCall: (String) -> Unit,
    onWebsite: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
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
                    model = ImageRequest.Builder(context)
                        .data(if (imageUrl.startsWith("/")) File(imageUrl) else imageUrl)
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
                    Text("Set as Destination")
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