package com.boattaxie.app.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.boattaxie.app.ui.theme.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class DockLocation(
    val id: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val description: String = "",
    val category: String = "Custom",
    val createdAt: Long = System.currentTimeMillis()
)

private fun normalizeDockName(name: String): String {
    return name
        .trim()
        .lowercase()
        .replace(Regex("[^a-z0-9\\s]"), "")
        .replace(Regex("\\s+"), " ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDockLocationsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val firestore = remember { FirebaseFirestore.getInstance() }
    
    var dockLocations by remember { mutableStateOf<List<DockLocation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<DockLocation?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSeedingDocks by remember { mutableStateOf(false) }
    var isUpdatingCategories by remember { mutableStateOf(false) }
    var showUpdateCategoriesDialog by remember { mutableStateOf(false) }
    
    // Hardcoded dock locations to seed into Firestore
    val defaultDockLocations = remember {
        listOf(
            // DOCKS - Main Water Taxi Docks
            DockLocation(name = "Main Water Taxi Dock", latitude = 9.3387625, longitude = -82.2402344, category = "Docks", description = "Primary public dock in Bocas Town where most water taxis depart for beaches and islands."),
            DockLocation(name = "Bocas Docks", latitude = 9.3433664, longitude = -82.2424937, category = "Docks", description = "Cluster of docks along Bocas Town waterfront used by water taxis and tour boats."),
            DockLocation(name = "Water Taxi Dock (Lanchas)", latitude = 9.3367882, longitude = -82.2429174, category = "Docks", description = "Lancha/water-taxi pickup area in Bocas Town for island transfers."),
            DockLocation(name = "Watertaxi Stop", latitude = 9.3399387, longitude = -82.239166, category = "Docks", description = "Water taxi pickup point in Bocas Town."),
            DockLocation(name = "Isla Colon Water Taxi", latitude = 9.3388934, longitude = -82.2396447, category = "Docks", description = "Water taxi service/stand on Isla Colón."),
            DockLocation(name = "Transporte Maritimo Valencia", latitude = 9.3384523, longitude = -82.2402878, category = "Docks", description = "Local maritime transport operator/landing in Bocas."),
            DockLocation(name = "Palanga Ferry Terminal", latitude = 9.3353841, longitude = -82.2410291, category = "Docks", description = "Almirante-side terminal for ferry/boat transfers to/from Isla Colón (Bocas Town)."),
            DockLocation(name = "Bocas Yacht Club & Marina", latitude = 9.3349824, longitude = -82.2468556, category = "Docks", description = "Marina/yacht club facility for moorage and boating services in Bocas Town."),
            DockLocation(name = "Carenero Dock", latitude = 9.3424, longitude = -82.2348, category = "Docks", description = "Common pickup/drop-off point for Isla Carenero, a quick boat ride from Bocas Town."),
            DockLocation(name = "Marina Carenero", latitude = 9.3424036, longitude = -82.235247, category = "Docks", description = "Small marina/boat-access facility on Carenero."),
            DockLocation(name = "Bastimentos Boat Dock (Bocas)", latitude = 9.3372843, longitude = -82.240895, category = "Docks", description = "Bocas Town-side dock for boats heading to Bastimentos."),
            DockLocation(name = "Old Bank Dock (Bastimentos)", latitude = 9.34782, longitude = -82.2111463, category = "Docks", description = "Dock for Old Bank village on Isla Bastimentos, used for water taxis and local transfers."),
            DockLocation(name = "Red Frog Marina", latitude = 9.3365993, longitude = -82.1774826, category = "Docks", description = "Marina/landing serving Red Frog Beach area access on Bastimentos."),
            DockLocation(name = "Ferry Bocas (Almirante)", latitude = 9.2893377, longitude = -82.3932207, category = "Docks", description = "Scheduled ferry service between Almirante and Isla Colón (Bocas Town)."),
            DockLocation(name = "Ferry Palanga (Almirante)", latitude = 9.2892706, longitude = -82.3930836, category = "Docks", description = "Almirante ferry/boat departure point."),
            DockLocation(name = "Marina Solarte", latitude = 9.3095, longitude = -82.1870, category = "Docks", description = "Marina/landing on Isla Solarte."),
            DockLocation(name = "Punta Caracol Dock", latitude = 9.3424986, longitude = -82.2518131, category = "Docks", description = "Private dock/arrival point for Punta Caracol Acqua Lodge (overwater eco-lodge)."),
            DockLocation(name = "Bocas Airport (BOC)", latitude = 9.34083, longitude = -82.25, category = "Docks", description = "The archipelago's main airport near Bocas Town, close to town by short taxi/walk."),
            
            // HOTELS
            DockLocation(name = "Hotel Palma Royale", latitude = 9.33504, longitude = -82.24169, category = "Hotels", description = "Boutique-style hotel on Main Street, close to restaurants/bars, with garden/sea-view rooms."),
            DockLocation(name = "Gran Hotel Bahía", latitude = 9.33913, longitude = -82.23797, category = "Hotels", description = "Historic landmark hotel - the oldest hotel/building on the islands (former United Fruit Co. HQ), central and waterfront-facing."),
            DockLocation(name = "Hotel CalaLuna", latitude = 9.34276, longitude = -82.23871, category = "Hotels", description = "Small, family-run lodging with simple, clean rooms near town/airport area."),
            DockLocation(name = "Hotel Bocas del Toro", latitude = 9.33938, longitude = -82.24258, category = "Hotels", description = "Hotel accommodation in Bocas Town with convenient central location."),
            DockLocation(name = "Hostel on the Sea", latitude = 9.3437, longitude = -82.2437, category = "Hotels", description = "Overwater hostel in Bocas Town offering budget-friendly accommodation."),
            DockLocation(name = "Hotel Tierra Verde", latitude = 9.3392473, longitude = -82.234033, category = "Hotels", description = "Hotel on Carenero Island, short boat ride from Bocas Town."),
            DockLocation(name = "Faro del Colibri", latitude = 9.33776, longitude = -82.23524, category = "Hotels", description = "Guesthouse on Carenero Island with quiet island setting."),
            DockLocation(name = "Selina Bocas del Toro", latitude = 9.3395, longitude = -82.2397, category = "Hotels", description = "Social, activity-heavy hostel in Bocas Town featuring events/yoga/music and shared + private rooms."),
            DockLocation(name = "Tropical Suites Hotel", latitude = 9.3388, longitude = -82.2400, category = "Hotels", description = "Oceanfront boutique hotel in the heart of town with modern suites and easy access to docks/tours."),
            DockLocation(name = "Divers Paradise Boutique Hotel", latitude = 9.3355, longitude = -82.2412, category = "Hotels", description = "Boutique hotel inspired by diving, built over the water, linked to the local dive scene. Pier 19 restaurant associated."),
            DockLocation(name = "Solarte EcoLodge", latitude = 9.315224, longitude = -82.1816718, category = "Hotels", description = "Eco-lodge style stay on Isla Solarte."),
            DockLocation(name = "Akwaba Lodge", latitude = 9.320453, longitude = -82.188827, category = "Hotels", description = "Lodge accommodation on Isla Solarte with island retreat atmosphere."),
            DockLocation(name = "Los Secretos Guesthouse", latitude = 9.3101702, longitude = -82.171318, category = "Hotels", description = "Guesthouse on Isla Solarte offering a quiet island getaway."),
            DockLocation(name = "Red Hill Villa", latitude = 9.320611, longitude = -82.18816, category = "Hotels", description = "Private villa rental on Isla Solarte with scenic views."),
            DockLocation(name = "Hotel Villa F&B", latitude = 9.3056464, longitude = -82.1866724, category = "Hotels", description = "Hotel on Isla Solarte with waterfront location."),
            DockLocation(name = "Casa Marlin", latitude = 9.3050428, longitude = -82.1751478, category = "Hotels", description = "Waterfront accommodation on Isla Solarte."),
            DockLocation(name = "Isla Vista Tranquila", latitude = 9.299679, longitude = -82.17088, category = "Hotels", description = "Peaceful lodging with island views on Isla Solarte."),
            
            // RESORTS
            DockLocation(name = "Buccaneer Resort", latitude = 9.3401283, longitude = -82.2320573, category = "Resorts", description = "Resort on Carenero Island with overwater bungalows and Caribbean atmosphere."),
            DockLocation(name = "Cosmic Crab Resort", latitude = 9.3436494, longitude = -82.2355459, category = "Resorts", description = "Unique overwater cabins on stilts at Carenero Island."),
            DockLocation(name = "Casa Cayuco Lodge", latitude = 9.2918901, longitude = -82.0878826, category = "Resorts", description = "Remote eco-adventure lodge on Bastimentos with beachfront/jungle setting; boat access only."),
            DockLocation(name = "Palmar Beach Lodge", latitude = 9.3436695, longitude = -82.1787023, category = "Resorts", description = "Beachfront lodge near Red Frog Beach on Bastimentos."),
            DockLocation(name = "Azul Paradise", latitude = 9.2886696, longitude = -82.1152572, category = "Resorts", description = "Resort with Caribbean bungalows/overwater style and off-grid island vibe."),
            DockLocation(name = "Tranquilo Bay", latitude = 9.2554756, longitude = -82.1453889, category = "Resorts", description = "Eco-adventure lodge on Isla Bastimentos emphasizing wildlife/birding, snorkeling/kayaking, and comfortable cabanas."),
            DockLocation(name = "Dolphin Bay Hideaway", latitude = 9.2413134, longitude = -82.2581001, category = "Resorts", description = "Secluded resort in Dolphin Bay area with overwater cabins."),
            DockLocation(name = "Roam Yoga & Wellness", latitude = 9.3263929, longitude = -82.2074944, category = "Resorts", description = "Yoga and wellness retreat/lodge on Isla Solarte offering classes and retreat programming."),
            DockLocation(name = "Las Casitas del Perezoso", latitude = 9.3224699, longitude = -82.1980979, category = "Resorts", description = "Eco-cabins on Isla Solarte surrounded by nature."),
            DockLocation(name = "La Purita Ecolodge", latitude = 9.3128596, longitude = -82.1793595, category = "Resorts", description = "Eco-lodge on Isla Solarte with natural surroundings."),
            DockLocation(name = "Sol Bungalows", latitude = 9.328641, longitude = -82.2197046, category = "Resorts", description = "Bungalow accommodation on Isla Solarte with ocean views."),
            DockLocation(name = "Solarte Breeze Lodges", latitude = 9.3315227, longitude = -82.2199765, category = "Resorts", description = "Waterfront lodges on Isla Solarte."),
            DockLocation(name = "Al Natural Resort", latitude = 9.2909113, longitude = -82.0850771, category = "Resorts", description = "Adults-only eco-resort on remote Bastimentos."),
            DockLocation(name = "La Loma Jungle Lodge", latitude = 9.316995, longitude = -82.1577651, category = "Resorts", description = "Eco-friendly jungle lodge on Isla Bastimentos with wellness/retreat positioning, known for chocolate/permaculture farm experiences."),
            DockLocation(name = "La Vida Resort", latitude = 9.29300, longitude = -82.09000, category = "Resorts", description = "Island resort on remote Bastimentos."),
            DockLocation(name = "Eclypse de Mar Acqua Lodge", latitude = 9.342692, longitude = -82.20653, category = "Resorts", description = "Over-the-water/jungle-meets-sea lodge on Bastimentos with nature reserve access and water-taxi arrival to its dock."),
            DockLocation(name = "Urraca Monkey Island Eco Resort", latitude = 9.1824447, longitude = -82.0844067, category = "Resorts", description = "Eco-resort on a private island in the Bocas archipelago."),
            DockLocation(name = "Eco Lodge La Escapada", latitude = 9.19141, longitude = -82.32903, category = "Resorts", description = "Remote eco-lodge offering an off-grid nature experience."),
            DockLocation(name = "Finca Tranquila", latitude = 9.19082, longitude = -82.24794, category = "Resorts", description = "Farm-style retreat in a peaceful rural setting."),
            
            // RESTAURANTS
            DockLocation(name = "Bambuda Bocas Town", latitude = 9.3433, longitude = -82.2429, category = "Restaurants", description = "Popular bar and restaurant in Bocas Town with waterfront setting."),
            DockLocation(name = "Bibi's on the Beach", latitude = 9.3406077, longitude = -82.2313467, category = "Restaurants", description = "Over-the-sea restaurant on Isla Carenero famous for Caribbean/seafood, cocktails, and sunset vibe."),
            DockLocation(name = "Coquitos Pizza", latitude = 9.3401557, longitude = -82.2319386, category = "Restaurants", description = "Pizza restaurant on Carenero Island."),
            DockLocation(name = "JJ's at Bocas Blended", latitude = 9.3432, longitude = -82.2426, category = "Restaurants", description = "Cafe in Bocas Town serving smoothies, juices and light meals."),
            DockLocation(name = "Restaurant Pier 19", latitude = 9.3353, longitude = -82.2412, category = "Restaurants", description = "Restaurant associated with Divers Paradise / dive scene in Bocas Town."),
            DockLocation(name = "Leaf Eaters Cafe", latitude = 9.33867, longitude = -82.23639, category = "Restaurants", description = "Waterfront cafe on Isla Carenero, daytime hours, known for whole-food style meals."),
            DockLocation(name = "Aqui Hoy Bocas", latitude = 9.3189252, longitude = -82.2109504, category = "Restaurants", description = "Restaurant on Isla Solarte serving local Caribbean cuisine."),
            DockLocation(name = "Blue Coconut Restaurant", latitude = 9.3108, longitude = -82.2109, category = "Restaurants", description = "Waterfront restaurant on Bastimentos with Caribbean food."),
            DockLocation(name = "El Toucan Loco", latitude = 9.1852931, longitude = -82.2708503, category = "Restaurants", description = "Bar and restaurant in the Bocas area."),
            
            // BEACHES
            DockLocation(name = "Boca del Drago", latitude = 9.41972, longitude = -82.33417, category = "Beaches", description = "North-west side beach area on Isla Colón; jumping-off point to nearby Starfish Beach."),
            DockLocation(name = "Starfish Beach", latitude = 9.4120, longitude = -82.3250, category = "Beaches", description = "Calm, shallow water beach famous for starfish viewing (observe responsibly - don't handle them)."),
            DockLocation(name = "Red Frog Beach", latitude = 9.3360, longitude = -82.1770, category = "Beaches", description = "Iconic beach on Isla Bastimentos accessed by boat + paid path; known for facilities and tiny red frogs in nearby jungle trails."),
            DockLocation(name = "Wizard Beach", latitude = 9.3510, longitude = -82.1650, category = "Beaches", description = "Wilder, more natural beach stretch on Bastimentos alongside Red Frog (fewer amenities)."),
            DockLocation(name = "Bluff Beach", latitude = 9.3800, longitude = -82.2150, category = "Beaches", description = "Atlantic-facing beach on Isla Colón known for stronger surf/rougher water compared to calmer north-west beaches."),
            
            // DOLPHIN SITES
            DockLocation(name = "Dolphin Bay", latitude = 9.34326, longitude = -82.25188, category = "Dolphin Sites", description = "Well-known lagoon area where boat tours commonly stop to spot bottlenose dolphins (sightings vary)."),
            DockLocation(name = "Laguna Bocatorito", latitude = 9.2200, longitude = -82.2400, category = "Dolphin Sites", description = "Lagoon system in the archipelago used on wildlife/dolphin tour routes."),
            DockLocation(name = "Bahia de los Delfines", latitude = 9.2500, longitude = -82.2300, category = "Dolphin Sites", description = "Dolphin Bay area in Bocas; tours frequently list it as a stop for dolphin watching."),
            
            // ISLANDS
            DockLocation(name = "Cayo Zapatilla 1", latitude = 9.2450, longitude = -82.0550, category = "Islands", description = "One of the two famous Zapatilla Cays - pristine beach day-trip destination visited by boat tour."),
            DockLocation(name = "Cayo Zapatilla 2", latitude = 9.2350, longitude = -82.0450, category = "Islands", description = "The second Zapatilla Cay; tours often specify Zapatilla I/II as separate stops."),
            DockLocation(name = "Bird Island", latitude = 9.3560, longitude = -82.2310, category = "Islands", description = "Common tour stop for seabird viewing (often a quick photo/wildlife stop)."),
            DockLocation(name = "Hospital Point", latitude = 9.3300, longitude = -82.2000, category = "Islands", description = "Snorkel/reef area on Bocas tour circuits - popular snorkeling point."),
            DockLocation(name = "Swan Cay", latitude = 9.4500, longitude = -82.2900, category = "Islands", description = "Small island/cay visited on certain wildlife/snorkel circuits."),
            
            // TOURS
            DockLocation(name = "Caribbean Coral Restoration", latitude = 9.3264465, longitude = -82.2062221, category = "Tours", description = "Coral restoration organization in Bocas del Toro focused on reef restoration and conservation; offers visit/snorkel experiences."),
            DockLocation(name = "Lil Spa Shop", latitude = 9.3432, longitude = -82.2443, category = "Tours", description = "Spa and wellness services in Bocas Town.")
        )
    }
    
    // Load dock locations from Firestore and seed if needed
    LaunchedEffect(Unit) {
        try {
            val snapshot = firestore.collection("dock_locations")
                .orderBy("name")
                .get()
                .await()
            
            val existingDocks = snapshot.documents.mapNotNull { doc ->
                try {
                    DockLocation(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0,
                        description = doc.getString("description") ?: "",
                        category = doc.getString("category") ?: "Custom",
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    null
                }
            }
            
            // If Firestore is empty, seed with default docks
            if (existingDocks.isEmpty()) {
                isSeedingDocks = true
                val batch = firestore.batch()
                val seededDocks = mutableListOf<DockLocation>()
                
                defaultDockLocations.forEach { dock ->
                    val docRef = firestore.collection("dock_locations").document()
                    batch.set(docRef, hashMapOf(
                        "name" to dock.name,
                        "latitude" to dock.latitude,
                        "longitude" to dock.longitude,
                        "description" to dock.description,
                        "category" to dock.category,
                        "createdAt" to System.currentTimeMillis()
                    ))
                    seededDocks.add(dock.copy(id = docRef.id))
                }
                
                batch.commit().await()
                dockLocations = seededDocks.sortedBy { it.name }
                Toast.makeText(context, "Loaded ${seededDocks.size} dock locations", Toast.LENGTH_SHORT).show()
                isSeedingDocks = false
            } else {
                dockLocations = existingDocks
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading docks: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }
    
    // Filter docks based on search
    val filteredDocks = remember(dockLocations, searchQuery) {
        if (searchQuery.isBlank()) {
            dockLocations
        } else {
            dockLocations.filter { 
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Dock Locations") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Update Categories button
                    IconButton(onClick = { showUpdateCategoriesDialog = true }) {
                        Icon(Icons.Default.Refresh, "Update Categories", tint = Color(0xFFFF9800))
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, "Add Dock", tint = Primary)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.AddLocation, "Add") },
                text = { Text("Add Dock") },
                containerColor = Primary,
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search docks...") },
                leadingIcon = { Icon(Icons.Default.Search, "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            // Stats
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = Primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${dockLocations.size}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Text(
                            text = "Total Docks",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Divider(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp),
                        color = TextSecondary.copy(alpha = 0.3f)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${filteredDocks.size}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Success
                        )
                        Text(
                            text = "Showing",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isLoading || isSeedingDocks) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Primary)
                        if (isSeedingDocks) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading dock locations...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else if (filteredDocks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.LocationOff,
                            "No docks",
                            modifier = Modifier.size(64.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No docks match your search" else "No custom docks added yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                        if (searchQuery.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap + to add a new dock location",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredDocks, key = { it.id }) { dock ->
                        DockLocationItem(
                            dock = dock,
                            existingDocks = dockLocations,
                            onEdit = { editedDock ->
                                coroutineScope.launch {
                                    try {
                                        firestore.collection("dock_locations")
                                            .document(editedDock.id)
                                            .update(
                                                mapOf(
                                                    "name" to editedDock.name,
                                                    "description" to editedDock.description,
                                                    "category" to editedDock.category,
                                                    "latitude" to editedDock.latitude,
                                                    "longitude" to editedDock.longitude
                                                )
                                            )
                                            .await()
                                        
                                        dockLocations = dockLocations.map { 
                                            if (it.id == editedDock.id) editedDock else it 
                                        }
                                        Toast.makeText(context, "Dock updated", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error updating: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onDelete = { showDeleteDialog = dock }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                    }
                }
            }
        }
    }
    
    // Add dock dialog
    if (showAddDialog) {
        AddDockDialog(
            existingDocks = dockLocations,
            onDismiss = { showAddDialog = false },
            onSave = { name, description, category, latLng ->
                coroutineScope.launch {
                    try {
                        val dockData = hashMapOf(
                            "name" to name,
                            "description" to description,
                            "category" to category,
                            "latitude" to latLng.latitude,
                            "longitude" to latLng.longitude,
                            "createdAt" to System.currentTimeMillis()
                        )
                        
                        val docRef = firestore.collection("dock_locations").add(dockData).await()
                        
                        val newDock = DockLocation(
                            id = docRef.id,
                            name = name,
                            description = description,
                            category = category,
                            latitude = latLng.latitude,
                            longitude = latLng.longitude
                        )
                        
                        dockLocations = dockLocations + newDock
                        showAddDialog = false
                        Toast.makeText(context, "Dock '$name' added successfully!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error adding dock: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
    
    // Delete confirmation dialog
    showDeleteDialog?.let { dock ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Default.Delete, "Delete", tint = Error) },
            title = { Text("Delete Dock?") },
            text = { 
                Text("Are you sure you want to delete '${dock.name}'? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                firestore.collection("dock_locations")
                                    .document(dock.id)
                                    .delete()
                                    .await()
                                
                                dockLocations = dockLocations.filter { it.id != dock.id }
                                showDeleteDialog = null
                                Toast.makeText(context, "Dock deleted", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error deleting: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Delete", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Update Categories confirmation dialog
    if (showUpdateCategoriesDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateCategoriesDialog = false },
            icon = { Icon(Icons.Default.Refresh, "Update", tint = Color(0xFFFF9800)) },
            title = { Text("Update Categories?") },
            text = { 
                Column {
                    Text("This will update all ${dockLocations.size} locations with proper categories and descriptions.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Categories: Docks, Hotels, Resorts, Restaurants, Beaches, Dolphin Sites, Islands, Tours", 
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Unknown locations will be auto-categorized by name.", 
                        fontWeight = FontWeight.Bold,
                        color = Primary)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUpdateCategoriesDialog = false
                        isUpdatingCategories = true
                        coroutineScope.launch {
                            try {
                                // Create a map of normalized name -> (category, description) from defaults
                                val categoryMap = defaultDockLocations.associate {
                                    normalizeDockName(it.name) to Pair(it.category, it.description)
                                }
                                
                                // Function to auto-categorize by name
                                fun autoCategorize(name: String): Pair<String, String> {
                                    val n = name.lowercase()
                                    return when {
                                        n.contains("beach") || n.contains("playa") -> Pair("Beaches", "Beach destination")
                                        n.contains("dolphin") || n.contains("delfin") -> Pair("Dolphin Sites", "Dolphin watching area")
                                        n.contains("island") || n.contains("isla") || n.contains("cayo") || n.contains("cay") -> Pair("Islands", "Island destination")
                                        n.contains("restaurant") || n.contains("cafe") || n.contains("pizza") || n.contains("bar") || n.contains("grill") || n.contains("food") || n.contains("kitchen") || n.contains("bakery") || n.contains("taco") || n.contains("sushi") -> Pair("Restaurants", "Restaurant")
                                        n.contains("hotel") || n.contains("hostel") || n.contains("guesthouse") || n.contains("guest house") || n.contains("inn") || n.contains("b&b") -> Pair("Hotels", "Accommodation")
                                        n.contains("resort") || n.contains("lodge") || n.contains("eco") || n.contains("bungalow") || n.contains("cabin") || n.contains("villa") || n.contains("retreat") || n.contains("casa") || n.contains("finca") -> Pair("Resorts", "Resort accommodation")
                                        n.contains("tour") || n.contains("snorkel") || n.contains("dive") || n.contains("trip") || n.contains("adventure") || n.contains("excursion") -> Pair("Tours", "Tour activity")
                                        n.contains("marina") || n.contains("dock") || n.contains("ferry") || n.contains("taxi") || n.contains("port") || n.contains("terminal") || n.contains("muelle") -> Pair("Docks", "Dock/Marina")
                                        else -> Pair("Docks", "Water taxi location")
                                    }
                                }
                                
                                var updatedCount = 0
                                var autoCount = 0
                                for (dock in dockLocations) {
                                    val update = categoryMap[normalizeDockName(dock.name)]
                                    if (update != null) {
                                        // Always update with the detailed description from defaults
                                        firestore.collection("dock_locations")
                                            .document(dock.id)
                                            .update(mapOf(
                                                "category" to update.first,
                                                "description" to update.second
                                            ))
                                            .await()
                                        updatedCount++
                                    } else {
                                        // Auto-categorize unknown locations with better descriptions
                                        val auto = autoCategorize(dock.name)
                                        val betterDesc = when {
                                            dock.description.length > 30 -> dock.description // Keep if already detailed
                                            else -> "${auto.second} in Bocas del Toro. Water taxi service available."
                                        }
                                        firestore.collection("dock_locations")
                                            .document(dock.id)
                                            .update(mapOf(
                                                "category" to auto.first,
                                                "description" to betterDesc
                                            ))
                                            .await()
                                        autoCount++
                                    }
                                }
                                
                                // Reload the dock locations
                                val snapshot = firestore.collection("dock_locations")
                                    .orderBy("name")
                                    .get()
                                    .await()
                                
                                dockLocations = snapshot.documents.mapNotNull { doc ->
                                    try {
                                        DockLocation(
                                            id = doc.id,
                                            name = doc.getString("name") ?: "",
                                            latitude = doc.getDouble("latitude") ?: 0.0,
                                            longitude = doc.getDouble("longitude") ?: 0.0,
                                            description = doc.getString("description") ?: "",
                                            category = doc.getString("category") ?: "Docks",
                                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                        )
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                
                                Toast.makeText(context, "Updated $updatedCount + auto $autoCount locations!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isUpdatingCategories = false
                            }
                        }
                    }
                ) {
                    Text("Update", color = Color(0xFFFF9800))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateCategoriesDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Loading overlay for updating categories
    if (isUpdatingCategories) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Updating categories...")
                }
            }
        }
    }
}

@Composable
private fun DockLocationItem(
    dock: DockLocation,
    existingDocks: List<DockLocation>,
    onEdit: (DockLocation) -> Unit,
    onDelete: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    
    if (showEditDialog) {
        EditDockDialog(
            dock = dock,
            existingDocks = existingDocks,
            onDismiss = { showEditDialog = false },
            onSave = { editedDock ->
                onEdit(editedDock)
                showEditDialog = false
            }
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = Primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Anchor,
                        "Dock",
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dock.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (dock.description.isNotEmpty()) {
                    Text(
                        text = dock.description,
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
                    Surface(
                        color = Success.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = dock.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = Success,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "📍 ${String.format("%.5f", dock.latitude)}, ${String.format("%.5f", dock.longitude)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
            
            // Edit button
            IconButton(onClick = { showEditDialog = true }) {
                Icon(
                    Icons.Default.Edit,
                    "Edit",
                    tint = Primary.copy(alpha = 0.7f)
                )
            }
            
            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    "Delete",
                    tint = Error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDockDialog(
    existingDocks: List<DockLocation>,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String, category: String, latLng: LatLng) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Custom") }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var showMapPicker by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    
    val categories = listOf(
        "Custom",
        "Bocas Town",
        "Carenero",
        "Bastimentos",
        "Solarte",
        "Remote Resort",
        "Ferry Terminal",
        "Marina",
        "Restaurant",
        "Hotel"
    )
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add New Dock",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Dock Name *") },
                    placeholder = { Text("e.g., My Resort Dock") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Anchor, "Name") }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("e.g., Main dock at resort entrance") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Description, "Description") }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        leadingIcon = { Icon(Icons.Default.Category, "Category") }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Location section
                Text(
                    text = "Location *",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (selectedLocation != null) {
                    // Show selected location
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Success.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                "Selected",
                                tint = Success
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Location Selected",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Success
                                )
                                Text(
                                    text = "${String.format("%.6f", selectedLocation!!.latitude)}, ${String.format("%.6f", selectedLocation!!.longitude)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            TextButton(onClick = { showMapPicker = true }) {
                                Text("Change")
                            }
                        }
                    }
                } else {
                    // Button to open map picker
                    OutlinedButton(
                        onClick = { showMapPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Map, "Map")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose Location on Map")
                    }
                }
                
                // Map preview (when location selected)
                if (selectedLocation != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        val cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(selectedLocation!!, 15f)
                        }
                        
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            uiSettings = MapUiSettings(
                                zoomControlsEnabled = false,
                                scrollGesturesEnabled = false,
                                zoomGesturesEnabled = false,
                                tiltGesturesEnabled = false,
                                rotationGesturesEnabled = false
                            )
                        ) {
                            Marker(
                                state = MarkerState(position = selectedLocation!!),
                                title = name.ifEmpty { "New Dock" }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            selectedLocation?.let { loc ->
                                onSave(name.trim(), description.trim(), category, loc)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank() && selectedLocation != null,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(Icons.Default.Save, "Save")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Dock")
                    }
                }
            }
        }
    }
    
    // Map picker dialog
    if (showMapPicker) {
        MapLocationPickerDialog(
            initialLocation = selectedLocation,
            existingDocks = existingDocks,
            onDismiss = { showMapPicker = false },
            onLocationSelected = { latLng ->
                selectedLocation = latLng
                showMapPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapLocationPickerDialog(
    initialLocation: LatLng?,
    existingDocks: List<DockLocation>,
    currentDockId: String? = null,
    onDismiss: () -> Unit,
    onLocationSelected: (LatLng) -> Unit
) {
    // Default to Bocas Town center
    val defaultLocation = LatLng(9.3401, -82.2408)
    var selectedLocation by remember { mutableStateOf(initialLocation ?: defaultLocation) }
    
    // Use higher zoom when editing an existing location
    val zoomLevel = if (initialLocation != null) 17f else 14f
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation ?: defaultLocation, zoomLevel)
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .fillMaxHeight(0.95f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                TopAppBar(
                    title = { Text("Tap to Select Location") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = { onLocationSelected(selectedLocation) }
                        ) {
                            Text("Confirm", color = Primary, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                
                // Instructions
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Primary.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.TouchApp,
                            "Tap",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tap on the map to place the dock marker",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Primary
                        )
                    }
                }
                
                // Map
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = true,
                            myLocationButtonEnabled = false
                        ),
                        properties = MapProperties(),
                        onMapClick = { latLng ->
                            selectedLocation = latLng
                        }
                    ) {
                        // Existing dock markers (greyed out)
                        existingDocks.forEach { dock ->
                            // Skip the dock being edited
                            if (dock.id != currentDockId) {
                                Marker(
                                    state = MarkerState(position = LatLng(dock.latitude, dock.longitude)),
                                    title = dock.name,
                                    snippet = dock.category,
                                    alpha = 0.6f
                                )
                            }
                        }
                        
                        // Selected location marker (highlighted)
                        Marker(
                            state = MarkerState(position = selectedLocation),
                            title = "New Dock Location",
                            snippet = "${String.format("%.6f", selectedLocation.latitude)}, ${String.format("%.6f", selectedLocation.longitude)}"
                        )
                    }
                    
                    // Coordinates display overlay
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "📍 ${String.format("%.6f", selectedLocation.latitude)}, ${String.format("%.6f", selectedLocation.longitude)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
                
                // Confirm button at bottom
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Button(
                        onClick = { onLocationSelected(selectedLocation) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(Icons.Default.Check, "Confirm")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Use This Location")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDockDialog(
    dock: DockLocation,
    existingDocks: List<DockLocation>,
    onDismiss: () -> Unit,
    onSave: (DockLocation) -> Unit
) {
    var name by remember { mutableStateOf(dock.name) }
    var description by remember { mutableStateOf(dock.description) }
    var category by remember { mutableStateOf(dock.category) }
    var selectedLocation by remember { mutableStateOf(LatLng(dock.latitude, dock.longitude)) }
    var showMapPicker by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    
    val categories = listOf(
        "Custom",
        "Bocas Town",
        "Carenero",
        "Bastimentos",
        "Solarte",
        "Remote Resort",
        "Ferry Terminal",
        "Marina",
        "Restaurant",
        "Hotel"
    )
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Dock",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Name field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Dock Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Description field
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Category dropdown
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categories.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        category = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Location section
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Current coordinates display
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                "Location",
                                tint = Primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Lat: ${String.format("%.6f", selectedLocation.latitude)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Lng: ${String.format("%.6f", selectedLocation.longitude)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            TextButton(onClick = { showMapPicker = true }) {
                                Icon(
                                    Icons.Default.Edit,
                                    "Change location",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Change")
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Save button
                Button(
                    onClick = {
                        onSave(
                            dock.copy(
                                name = name,
                                description = description,
                                category = category,
                                latitude = selectedLocation.latitude,
                                longitude = selectedLocation.longitude
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Default.Save, "Save")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Changes")
                }
            }
        }
    }
    
    // Map picker dialog
    if (showMapPicker) {
        MapLocationPickerDialog(
            initialLocation = selectedLocation,
            existingDocks = existingDocks,
            currentDockId = dock.id,
            onDismiss = { showMapPicker = false },
            onLocationSelected = { latLng ->
                selectedLocation = latLng
                showMapPicker = false
            }
        )
    }
}
