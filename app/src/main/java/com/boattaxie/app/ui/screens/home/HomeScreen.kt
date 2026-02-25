package com.boattaxie.app.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import com.boattaxie.app.R
import com.boattaxie.app.data.model.*
import com.boattaxie.app.ui.components.*
import com.boattaxie.app.ui.theme.*
import com.boattaxie.app.util.LanguageManager
import com.boattaxie.app.data.model.Advertisement
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToBookRide: (String) -> Unit,
    onNavigateToSubscription: () -> Unit,
    onNavigateToAds: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToTripHistory: () -> Unit,
    onNavigateToExplore: () -> Unit = {},
    onNavigateToDriverMode: () -> Unit = {},
    onNavigateToDriverVerification: () -> Unit = {},
    onNavigateToRateRide: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var currentLanguage by remember { mutableStateOf(LanguageManager.getCurrentLanguage(context)) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var selectedAd by remember { mutableStateOf<Advertisement?>(null) }
    
    // Check for unrated completed bookings and navigate to rating screen
    LaunchedEffect(Unit) {
        viewModel.checkForUnratedBooking()
    }
    
    LaunchedEffect(uiState.unratedBookingId) {
        uiState.unratedBookingId?.let { bookingId ->
            viewModel.clearUnratedBooking()
            onNavigateToRateRide(bookingId)
        }
    }
    
    // Handle switch to driver mode
    LaunchedEffect(uiState.switchToDriverMode) {
        if (uiState.switchToDriverMode) {
            viewModel.clearSwitchToDriverMode()
            onNavigateToDriverMode()
        }
    }
    
    // Show error message
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }
    
    // Help Dialog
    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }
    
    // Ad Detail Bottom Sheet - same as Explore screen
    val adSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    selectedAd?.let { ad ->
        ModalBottomSheet(
            onDismissRequest = { selectedAd = null },
            sheetState = adSheetState
        ) {
            DealDetailsSheet(
                ad = ad,
                onNavigateTo = { selectedAd = null },
                onCall = { phone ->
                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:$phone"))
                    context.startActivity(intent)
                },
                onWebsite = { url ->
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                    context.startActivity(intent)
                },
                onDismiss = { selectedAd = null }
            )
        }
    }
    
    Scaffold(
        topBar = {
            // Compact header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "OmniMap Logo",
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                "OmniMap",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            // Live user count badge
                            LiveUsersBadge(compact = true)
                        }
                    }
                    IconButton(
                        onClick = onNavigateToProfile,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.AccountCircle, "Profile", modifier = Modifier.size(24.dp))
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.home)) },
                    label = { Text(stringResource(R.string.home)) },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Explore, contentDescription = stringResource(R.string.explore)) },
                    label = { Text(stringResource(R.string.explore)) },
                    selected = false,
                    onClick = onNavigateToExplore
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, contentDescription = stringResource(R.string.trips)) },
                    label = { Text(stringResource(R.string.trips)) },
                    selected = false,
                    onClick = onNavigateToTripHistory
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.LocalOffer, contentDescription = stringResource(R.string.ads)) },
                    label = { Text(stringResource(R.string.ads)) },
                    selected = false,
                    onClick = onNavigateToAds
                )
                // Language toggle button
                NavigationBarItem(
                    icon = { Text(currentLanguage.flag, fontSize = 20.sp) },
                    label = { Text(LanguageManager.getNextLanguage(context).code.uppercase()) },
                    selected = false,
                    onClick = {
                        currentLanguage = LanguageManager.toggleLanguage(context)
                    }
                )
                // Only show Driver Mode button if user signed up as driver
                if (uiState.canBeDriver) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.SwapHoriz, contentDescription = stringResource(R.string.driver)) },
                        label = { Text(stringResource(R.string.driver)) },
                        selected = false,
                        onClick = {
                            if (uiState.isVerifiedDriver) {
                                onNavigateToDriverMode()
                            } else {
                                onNavigateToDriverVerification()
                            }
                        }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Greeting with subscription status
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hello, ${uiState.userName}! 👋",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        // Subscription status badge
                        if (uiState.hasActiveSubscription && uiState.subscription != null) {
                            val remainingDays = SubscriptionHelper.getRemainingDays(uiState.subscription!!)
                            Surface(
                                color = Success,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${remainingDays}d active",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    // Show subscription status message
                    if (uiState.hasActiveSubscription && uiState.subscription != null) {
                        val remainingDays = SubscriptionHelper.getRemainingDays(uiState.subscription!!)
                        Text(
                            text = "✅ You have $remainingDays day${if (remainingDays != 1) "s" else ""} of active rides remaining!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Success,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = "Where would you like to go today?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // How to book instructions
                    Surface(
                        color = Primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = stringResource(R.string.how_to_book),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.how_to_book_steps),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
            
            // Book ride section - compact
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CompactRideButton(
                        icon = Icons.Default.DirectionsBoat,
                        title = stringResource(R.string.book_boat),
                        color = BoatColor,
                        onClick = { onNavigateToBookRide("boat") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    CompactRideButton(
                        icon = Icons.Default.LocalTaxi,
                        title = stringResource(R.string.book_taxi),
                        color = TaxiColor,
                        onClick = { onNavigateToBookRide("taxi") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Quick action buttons - Subscribe and Map
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Map button - opens Explore screen
                    Surface(
                        onClick = onNavigateToExplore,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, Color(0xFF4CAF50))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Map,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.explore_map),
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4CAF50),
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    // Subscribe button
                    Surface(
                        onClick = onNavigateToSubscription,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Primary
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.subscribe_star),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            
            // Help button row
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    onClick = { showHelpDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF9C27B0)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "❓",
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.how_to_use_app),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            // Featured ads - auto-scrolling carousel
            if (uiState.featuredAds.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏝️ Islands Deals & Coupons",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                item {
                    val pagerState = rememberPagerState(pageCount = { uiState.featuredAds.size })
                    
                    // Track impressions when page changes
                    LaunchedEffect(pagerState.currentPage, uiState.featuredAds) {
                        if (uiState.featuredAds.isNotEmpty() && pagerState.currentPage < uiState.featuredAds.size) {
                            val currentAd = uiState.featuredAds[pagerState.currentPage]
                            viewModel.recordAdImpression(currentAd.id)
                        }
                    }
                    
                    // Auto-scroll every 3 seconds
                    LaunchedEffect(pagerState, uiState.featuredAds.size) {
                        if (uiState.featuredAds.size > 1) {
                            while (true) {
                                delay(3000)
                                val nextPage = (pagerState.currentPage + 1) % uiState.featuredAds.size
                                pagerState.animateScrollToPage(nextPage)
                            }
                        }
                    }
                    
                    Column {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            pageSpacing = 12.dp
                        ) { page ->
                            val ad = uiState.featuredAds[page]
                            AdvertisementCard(
                                ad = ad,
                                onClick = {
                                    viewModel.onAdClick(ad.id)
                                    selectedAd = ad
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        // Simple page counter instead of dots (for many ads)
                        if (uiState.featuredAds.size > 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${pagerState.currentPage + 1} / ${uiState.featuredAds.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
            
            // News and Weather Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                NewsWeatherSection(
                    weather = uiState.weather,
                    forecast = uiState.weatherForecast,
                    articles = uiState.newsArticles,
                    isLoading = uiState.isLoadingNews,
                    onRefresh = { viewModel.refreshNews() }
                )
            }
            
            // Recent trips
            if (uiState.recentTrips.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.recent_trips),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = onNavigateToTripHistory) {
                            Text(stringResource(R.string.see_all))
                        }
                    }
                }
                
                items(uiState.recentTrips.take(3)) { trip ->
                    RecentTripCard(
                        booking = trip,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SubscriptionBanner(
    onSubscribe: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Primary)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.subscribe_to_book),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextOnPrimary
                )
                Text(
                    text = stringResource(R.string.starting_at_price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextOnPrimary.copy(alpha = 0.9f)
                )
            }
            Button(
                onClick = onSubscribe,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Surface,
                    contentColor = Primary
                )
            ) {
                Text(stringResource(R.string.subscribe))
            }
        }
    }
}

@Composable
private fun ActiveSubscriptionCard(
    subscription: Subscription,
    onManage: () -> Unit
) {
    val remainingDays = SubscriptionHelper.getRemainingDays(subscription)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                stringResource(R.string.active),
                tint = Success,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.subscription_active),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Success
                )
                Text(
                    text = stringResource(R.string.days_remaining, remainingDays),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            TextButton(onClick = onManage) {
                Text(stringResource(R.string.manage))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RideTypeCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(140.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactRideButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RecentTripCard(
    booking: Booking,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (booking.vehicleType == VehicleType.BOAT) 
                    Icons.Default.DirectionsBoat else Icons.Default.LocalTaxi,
                contentDescription = null,
                tint = if (booking.vehicleType == VehicleType.BOAT) BoatColor else TaxiColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = booking.destinationLocation.address ?: stringResource(R.string.trip_word),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = booking.pickupLocation.address ?: stringResource(R.string.pickup_location_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Text(
                text = "$${String.format("%.2f", booking.finalFare ?: booking.estimatedFare)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun HelpDialog(onDismiss: () -> Unit) {
    var selectedSection by remember { mutableStateOf(0) }
    
    val sections = listOf(
        stringResource(R.string.help_getting_started),
        stringResource(R.string.help_how_to_book),
        stringResource(R.string.help_search),
        stringResource(R.string.help_map_features),
        stringResource(R.string.help_explore),
        stringResource(R.string.help_island_deals),
        stringResource(R.string.help_subscription),
        stringResource(R.string.help_driver_mode),
        stringResource(R.string.help_tips_faq)
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
                                    text = stringResource(R.string.help_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = stringResource(R.string.help_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.close),
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
                        4 -> ExploreMapSection()
                        5 -> IslandDealsSection()
                        6 -> SubscriptionSection()
                        7 -> DriverModeSection()
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
    HelpSectionTitle(stringResource(R.string.help_getting_started_title))
    
    Text(
        text = stringResource(R.string.help_getting_started_intro),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = Color.Black,
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    HelpStep(
        number = 1,
        title = stringResource(R.string.help_step_open_app),
        description = stringResource(R.string.help_step_open_app_desc)
    )
    
    HelpStep(
        number = 2,
        title = stringResource(R.string.help_step_allow_location),
        description = stringResource(R.string.help_step_allow_location_desc)
    )
    
    HelpStep(
        number = 3,
        title = stringResource(R.string.help_step_explore_map),
        description = stringResource(R.string.help_step_explore_map_desc)
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    HelpTip("💡", stringResource(R.string.help_tip_realtime_drivers))
    
    HelpTip("📍", stringResource(R.string.help_tip_discovery))
    
    HelpTip("🌐", stringResource(R.string.help_tip_language))
}

@Composable
private fun HowToBookSection() {
    HelpSectionTitle(stringResource(R.string.help_how_to_book_title))
    
    Text(
        text = stringResource(R.string.help_how_to_book_intro),
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF424242),
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    HelpStep(
        number = 1,
        title = stringResource(R.string.help_step_set_pickup),
        description = stringResource(R.string.help_step_set_pickup_desc)
    )
    
    HelpStep(
        number = 2,
        title = stringResource(R.string.help_step_set_dropoff),
        description = stringResource(R.string.help_step_set_dropoff_desc)
    )
    
    HelpStep(
        number = 3,
        title = stringResource(R.string.help_step_see_route),
        description = stringResource(R.string.help_step_see_route_desc)
    )
    
    HelpStep(
        number = 4,
        title = stringResource(R.string.help_step_passengers),
        description = stringResource(R.string.help_step_passengers_desc)
    )
    
    HelpStep(
        number = 5,
        title = stringResource(R.string.help_step_confirm),
        description = stringResource(R.string.help_step_confirm_desc)
    )
    
    HelpStep(
        number = 6,
        title = stringResource(R.string.help_step_wait),
        description = stringResource(R.string.help_step_wait_desc)
    )
    
    HelpStep(
        number = 7,
        title = stringResource(R.string.help_step_driver_accepts),
        description = stringResource(R.string.help_step_driver_accepts_desc)
    )
    
    HelpStep(
        number = 8,
        title = stringResource(R.string.help_step_track),
        description = stringResource(R.string.help_step_track_desc)
    )
    
    HelpStep(
        number = 9,
        title = stringResource(R.string.help_step_call),
        description = stringResource(R.string.help_step_call_desc)
    )
    
    HelpStep(
        number = 10,
        title = stringResource(R.string.help_step_enjoy),
        description = stringResource(R.string.help_step_enjoy_desc)
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    HelpTip("⚠️", stringResource(R.string.help_tip_no_cancel))
    
    HelpTip("⭐", stringResource(R.string.help_tip_rate))
    
    HelpTip("💰", stringResource(R.string.help_tip_cash))
}

@Composable
private fun SearchNavigationSection() {
    HelpSectionTitle(stringResource(R.string.help_search_title))
    
    Text(
        text = stringResource(R.string.help_search_intro),
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
                text = stringResource(R.string.help_search_magnifying),
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.help_search_magnifying_desc),
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
                text = stringResource(R.string.help_search_dock),
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.help_search_dock_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF424242),
                lineHeight = 22.sp
            )
        }
    }
    
    Spacer(modifier = Modifier.height(12.dp))
    
    HelpTip("🎯", stringResource(R.string.help_tip_discover))
    
    HelpTip("📍", stringResource(R.string.help_tip_dropoff_set))
}

@Composable
private fun MapFeaturesSection() {
    HelpSectionTitle(stringResource(R.string.help_map_features_title))
    
    Text(
        text = stringResource(R.string.help_map_features_intro),
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF424242),
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    // Top Left Buttons
    Text(
        text = stringResource(R.string.help_top_left_buttons),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    
    HelpTip("🏠", stringResource(R.string.help_btn_home))
    HelpTip("🇺🇸", stringResource(R.string.help_btn_language))
    HelpTip("🔍", stringResource(R.string.help_btn_search))
    HelpTip("🏝️", stringResource(R.string.help_btn_deals))
    
    Spacer(modifier = Modifier.height(12.dp))
    
    // Top Right Buttons
    Text(
        text = stringResource(R.string.help_top_right_buttons),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    
    HelpTip("🚤/🚕", stringResource(R.string.help_btn_drivers_online))
    HelpTip("🔍", stringResource(R.string.help_btn_search_dock))
    HelpTip("👁️", stringResource(R.string.help_btn_show_docks))
    
    Spacer(modifier = Modifier.height(12.dp))
    
    // Map Markers
    Text(
        text = stringResource(R.string.help_map_markers),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    
    HelpTip("🔵", stringResource(R.string.help_marker_blue))
    HelpTip("🟢", stringResource(R.string.help_marker_green))
    HelpTip("🔴", stringResource(R.string.help_marker_red))
    HelpTip("⚓", stringResource(R.string.help_marker_anchor))
    HelpTip("🚤", stringResource(R.string.help_marker_boat))
    HelpTip("🚕", stringResource(R.string.help_marker_taxi))
    HelpTip("🏷️", stringResource(R.string.help_marker_ad))
    
    Spacer(modifier = Modifier.height(12.dp))
    
    // Zoom Controls
    Text(
        text = stringResource(R.string.help_zoom_controls),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    
    HelpTip("➕", stringResource(R.string.help_zoom_in))
    HelpTip("➖", stringResource(R.string.help_zoom_out))
    HelpTip("👆", stringResource(R.string.help_zoom_pinch))
    HelpTip("👆👆", stringResource(R.string.help_zoom_double))
}

@Composable
private fun ExploreMapSection() {
    HelpSectionTitle(stringResource(R.string.help_explore_map_title))
    
    Text(
        text = stringResource(R.string.help_explore_map_intro),
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF424242),
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    HelpStep(
        number = 1,
        title = stringResource(R.string.help_explore_realtime),
        description = stringResource(R.string.help_explore_realtime_desc)
    )
    
    HelpStep(
        number = 2,
        title = stringResource(R.string.help_explore_discovery),
        description = stringResource(R.string.help_explore_discovery_desc)
    )
    
    HelpStep(
        number = 3,
        title = stringResource(R.string.help_explore_online),
        description = stringResource(R.string.help_explore_online_desc)
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    HelpTip("🚶", stringResource(R.string.help_explore_tip_walk))
    HelpTip("📍", stringResource(R.string.help_explore_tip_realtime))
    HelpTip("👥", stringResource(R.string.help_explore_tip_online))
    HelpTip("🔍", stringResource(R.string.help_explore_tip_search))
}

@Composable
private fun IslandDealsSection() {
    HelpSectionTitle(stringResource(R.string.help_island_deals_title))
    
    Text(
        text = stringResource(R.string.help_island_deals_intro),
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF424242),
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    HelpStep(
        number = 1,
        title = stringResource(R.string.help_step_see_ads),
        description = stringResource(R.string.help_step_see_ads_desc)
    )
    
    HelpStep(
        number = 2,
        title = stringResource(R.string.help_step_toggle_ads),
        description = stringResource(R.string.help_step_toggle_ads_desc)
    )
    
    HelpStep(
        number = 3,
        title = stringResource(R.string.help_step_view_details),
        description = stringResource(R.string.help_step_view_details_desc)
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = stringResource(R.string.help_advertise_title),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    
    Text(
        text = stringResource(R.string.help_advertise_intro),
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF424242),
        modifier = Modifier.padding(bottom = 12.dp)
    )
    
    HelpStep(
        number = 1,
        title = stringResource(R.string.help_step_go_home),
        description = stringResource(R.string.help_step_go_home_desc)
    )
    
    HelpStep(
        number = 2,
        title = stringResource(R.string.help_step_tap_deals),
        description = stringResource(R.string.help_step_tap_deals_desc)
    )
    
    HelpStep(
        number = 3,
        title = stringResource(R.string.help_step_create_ad),
        description = stringResource(R.string.help_step_create_ad_desc)
    )
    
    HelpStep(
        number = 4,
        title = stringResource(R.string.help_step_add_contact),
        description = stringResource(R.string.help_step_add_contact_desc)
    )
    
    HelpStep(
        number = 5,
        title = stringResource(R.string.help_step_choose_duration),
        description = stringResource(R.string.help_step_choose_duration_desc)
    )
    
    HelpStep(
        number = 6,
        title = stringResource(R.string.help_step_submit_pay),
        description = stringResource(R.string.help_step_submit_pay_desc)
    )
    
    HelpTip("💰", stringResource(R.string.help_tip_ad_pricing))
}

@Composable
private fun SubscriptionSection() {
    HelpSectionTitle(stringResource(R.string.help_subscription_title))
    
    Text(
        text = stringResource(R.string.help_subscription_intro),
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF424242),
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    Text(
        text = stringResource(R.string.help_for_drivers),
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
                text = stringResource(R.string.help_driver_benefits_title),
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.help_driver_benefits),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF424242),
                lineHeight = 24.sp
            )
        }
    }
    
    Text(
        text = stringResource(R.string.help_how_subscribe_driver),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
    )
    
    HelpStep(
        number = 1,
        title = stringResource(R.string.help_step_driver_home),
        description = stringResource(R.string.help_step_driver_home_desc)
    )
    
    HelpStep(
        number = 2,
        title = stringResource(R.string.help_step_driver_mode),
        description = stringResource(R.string.help_step_driver_mode_desc)
    )
    
    HelpStep(
        number = 3,
        title = stringResource(R.string.help_step_register_driver),
        description = stringResource(R.string.help_step_register_driver_desc)
    )
    
    HelpStep(
        number = 4,
        title = stringResource(R.string.help_step_choose_plan),
        description = stringResource(R.string.help_step_choose_plan_desc)
    )
    
    HelpStep(
        number = 5,
        title = stringResource(R.string.help_step_complete_payment),
        description = stringResource(R.string.help_step_complete_payment_desc)
    )
    
    HelpStep(
        number = 6,
        title = stringResource(R.string.help_step_go_online),
        description = stringResource(R.string.help_step_go_online_desc)
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    HelpTip("💡", stringResource(R.string.help_tip_free_trial))
    
    HelpTip("📱", stringResource(R.string.help_tip_auto_renew))
}

@Composable
private fun DriverModeSection() {
    HelpSectionTitle(stringResource(R.string.help_driver_mode_title))
    
    Text(
        text = stringResource(R.string.help_driver_mode_intro),
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF424242),
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    HelpStep(
        number = 1,
        title = stringResource(R.string.help_step_access_driver),
        description = stringResource(R.string.help_step_access_driver_desc)
    )
    
    HelpStep(
        number = 2,
        title = stringResource(R.string.help_step_register_vehicle),
        description = stringResource(R.string.help_step_register_vehicle_desc)
    )
    
    HelpStep(
        number = 3,
        title = stringResource(R.string.help_step_set_prices),
        description = stringResource(R.string.help_step_set_prices_desc)
    )
    
    HelpStep(
        number = 4,
        title = stringResource(R.string.help_step_go_online),
        description = stringResource(R.string.help_step_go_online_desc2)
    )
    
    HelpStep(
        number = 5,
        title = stringResource(R.string.help_step_receive_requests),
        description = stringResource(R.string.help_step_receive_requests_desc)
    )
    
    HelpStep(
        number = 6,
        title = stringResource(R.string.help_step_navigate_pickup),
        description = stringResource(R.string.help_step_navigate_pickup_desc)
    )
    
    HelpStep(
        number = 7,
        title = stringResource(R.string.help_step_start_trip),
        description = stringResource(R.string.help_step_start_trip_desc)
    )
    
    HelpStep(
        number = 8,
        title = stringResource(R.string.help_step_complete_trip),
        description = stringResource(R.string.help_step_complete_trip_desc)
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = stringResource(R.string.help_driver_features),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    
    HelpTip("💵", stringResource(R.string.help_tip_my_prices))
    HelpTip("📈", stringResource(R.string.help_tip_earnings))
    HelpTip("⭐", stringResource(R.string.help_tip_ratings))
    HelpTip("📋", stringResource(R.string.help_tip_trip_history))
    HelpTip("🔔", stringResource(R.string.help_tip_notifications))
}

@Composable
private fun SettingsTipsSection() {
    HelpSectionTitle(stringResource(R.string.help_settings_title))
    
    Text(
        text = stringResource(R.string.help_settings_intro),
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF424242),
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    Text(
        text = stringResource(R.string.help_pro_tips_title),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    
    HelpTip("⏰", stringResource(R.string.help_tip_best_times))
    HelpTip("📍", stringResource(R.string.help_tip_be_precise))
    HelpTip("💬", stringResource(R.string.help_tip_communicate))
    HelpTip("💵", stringResource(R.string.help_tip_cash_ready))
    HelpTip("👥", stringResource(R.string.help_tip_group_rides))
    HelpTip("⭐", stringResource(R.string.help_tip_rate_drivers))
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = stringResource(R.string.help_faq_title),
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
            Text(text = stringResource(R.string.help_faq_q1), fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = stringResource(R.string.help_faq_a1), style = MaterialTheme.typography.bodyMedium, color = Color(0xFF424242))
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = stringResource(R.string.help_faq_q2), fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = stringResource(R.string.help_faq_a2), style = MaterialTheme.typography.bodyMedium, color = Color(0xFF424242))
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = stringResource(R.string.help_faq_q3), fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = stringResource(R.string.help_faq_a3), style = MaterialTheme.typography.bodyMedium, color = Color(0xFF424242))
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = stringResource(R.string.help_faq_q4), fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = stringResource(R.string.help_faq_a4), style = MaterialTheme.typography.bodyMedium, color = Color(0xFF424242))
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = stringResource(R.string.help_faq_q5), fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = stringResource(R.string.help_faq_a5), style = MaterialTheme.typography.bodyMedium, color = Color(0xFF424242))
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

@Composable
private fun NewsWeatherSection(
    weather: WeatherData?,
    forecast: List<WeatherForecast>,
    articles: List<NewsArticle>,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.local_news_weather),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onRefresh) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.refresh_news),
                    tint = Primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Weather Card
        weather?.let { w ->
            WeatherCard(weather = w, forecast = forecast)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Loading indicator
        if (isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.loading_news),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
        
        // News Articles
        if (articles.isNotEmpty()) {
            Text(
                text = stringResource(R.string.latest_news),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            articles.take(5).forEach { article ->
                NewsArticleCard(
                    article = article,
                    onClick = {
                        // Open article in browser
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
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else if (!isLoading) {
            Text(
                text = stringResource(R.string.no_news),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WeatherCard(
    weather: WeatherData,
    forecast: List<WeatherForecast>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E88E5)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Current Weather
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.bocas_weather),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = weather.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = weather.condition.icon,
                        fontSize = 40.sp
                    )
                    Text(
                        text = "${weather.temperature.toInt()}°C",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Weather details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherDetail(
                    icon = "🌡️",
                    label = stringResource(R.string.feels_like),
                    value = "${weather.feelsLike.toInt()}°C"
                )
                WeatherDetail(
                    icon = "💧",
                    label = stringResource(R.string.humidity),
                    value = "${weather.humidity}%"
                )
                WeatherDetail(
                    icon = "💨",
                    label = stringResource(R.string.wind),
                    value = "${weather.windSpeed.toInt()} km/h"
                )
            }
            
            // Forecast
            if (forecast.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = stringResource(R.string.forecast),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    forecast.take(5).forEachIndexed { index, day ->
                        ForecastDay(
                            dayName = when (index) {
                                0 -> stringResource(R.string.today)
                                1 -> stringResource(R.string.tomorrow)
                                else -> {
                                    val sdf = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
                                    sdf.format(java.util.Date(day.date))
                                }
                            },
                            icon = day.condition.icon,
                            tempHigh = "${day.tempMax.toInt()}°",
                            tempLow = "${day.tempMin.toInt()}°"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherDetail(
    icon: String,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 20.sp)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
private fun ForecastDay(
    dayName: String,
    icon: String,
    tempHigh: String,
    tempLow: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f)
        )
        Text(text = icon, fontSize = 24.sp)
        Text(
            text = tempHigh,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = tempLow,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun NewsArticleCard(
    article: NewsArticle,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Article image placeholder or actual image
            Surface(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(8.dp),
                color = Primary.copy(alpha = 0.2f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = article.source.getIconEmoji(),
                        fontSize = 28.sp
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                // Source badge
                Text(
                    text = article.source.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Title
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Description
                if (article.description.isNotEmpty()) {
                    Text(
                        text = article.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Time ago
                Text(
                    text = getTimeAgo(article.publishedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.read_more),
                tint = TextSecondary
            )
        }
    }
}

private fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        days > 0 -> "${days}d ago"
        hours > 0 -> "${hours}h ago"
        minutes > 0 -> "${minutes}m ago"
        else -> "Just now"
    }
}

/**
 * Deal Details Sheet - Shows full ad details in a bottom sheet
 */
@Composable
private fun DealDetailsSheet(
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