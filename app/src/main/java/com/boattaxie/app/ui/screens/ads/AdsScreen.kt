package com.boattaxie.app.ui.screens.ads

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.boattaxie.app.BuildConfig
import com.boattaxie.app.data.model.*
import com.boattaxie.app.ui.components.*
import com.boattaxie.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Extract YouTube video ID from various YouTube URL formats
 */
private fun extractYouTubeVideoId(url: String): String? {
    val patterns = listOf(
        // Standard watch URL: https://www.youtube.com/watch?v=VIDEO_ID
        """(?:youtube\.com/watch\?v=|youtube\.com/watch\?.*&v=)([^&]+)""",
        // Short URL: https://youtu.be/VIDEO_ID
        """youtu\.be/([^?&]+)""",
        // Embed URL: https://www.youtube.com/embed/VIDEO_ID
        """youtube\.com/embed/([^?&]+)""",
        // Shorts URL: https://www.youtube.com/shorts/VIDEO_ID
        """youtube\.com/shorts/([^?&]+)"""
    )
    
    for (pattern in patterns) {
        val regex = Regex(pattern)
        val match = regex.find(url)
        if (match != null && match.groupValues.size > 1) {
            return match.groupValues[1]
        }
    }
    return null
}

/**
 * Get YouTube video thumbnail URL from video ID
 */
private fun getYouTubeThumbnailUrl(videoId: String): String {
    // Use maxresdefault for high quality, falls back to hqdefault
    return "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvertisementsScreen(
    onNavigateToAdDetails: (String) -> Unit,
    onNavigateToCreateAd: () -> Unit,
    onNavigateToMyAds: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AdsViewModel = hiltViewModel()
) {
    // Redirect straight to My Ads screen
    LaunchedEffect(Unit) {
        onNavigateToMyAds()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdDetailsScreen(
    adId: String,
    onNavigateBack: () -> Unit,
    viewModel: AdsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(adId) {
        viewModel.loadAdDetails(adId)
    }
    
    val ad = uiState.selectedAd
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ad Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (ad != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image - handle local files or YouTube thumbnail fallback
                val hasLocalImage = remember(ad.imageUrl) {
                    if (ad.imageUrl.isNullOrBlank()) {
                        false
                    } else if (ad.imageUrl.startsWith("/")) {
                        val file = File(ad.imageUrl)
                        val exists = file.exists()
                        android.util.Log.d("AdDetails", "Checking file ${ad.imageUrl}: exists=$exists")
                        exists
                    } else {
                        true
                    }
                }
                val youtubeVideoId = ad.youtubeUrl?.let { extractYouTubeVideoId(it) }
                
                if (hasLocalImage && !ad.imageUrl.isNullOrBlank()) {
                    val imageModel = remember(ad.imageUrl) {
                        if (ad.imageUrl.startsWith("/")) {
                            File(ad.imageUrl)
                        } else {
                            ad.imageUrl
                        }
                    }
                    AsyncImage(
                        model = imageModel,
                        contentDescription = ad.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop,
                        onError = { android.util.Log.e("AdDetails", "Image load error: ${it.result.throwable}") }
                    )
                } else if (youtubeVideoId != null) {
                    // Show YouTube thumbnail as fallback
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        AsyncImage(
                            model = getYouTubeThumbnailUrl(youtubeVideoId),
                            contentDescription = "YouTube Video Thumbnail",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Play button overlay
                        Surface(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(64.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = Color(0xFFFF0000).copy(alpha = 0.9f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("▶", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    }
                } else {
                    // No image and no YouTube - show large attractive placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        Primary.copy(alpha = 0.8f),
                                        Primary.copy(alpha = 0.95f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = ad.category.getIcon(),
                                fontSize = 72.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = ad.category.getDisplayName(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                
                // YouTube link
                if (!ad.youtubeUrl.isNullOrBlank()) {
                    val context = LocalContext.current
                    android.util.Log.d("AdsScreen", "YouTube URL: ${ad.youtubeUrl}")
                    Card(
                        onClick = {
                            try {
                                val youtubeUrl = ad.youtubeUrl!!
                                android.util.Log.d("AdsScreen", "Opening YouTube: $youtubeUrl")
                                // Try opening in YouTube app first, then fall back to browser
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.util.Log.e("AdsScreen", "Failed to open YouTube: ${e.message}", e)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF0000).copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("▶", color = Color(0xFFFF0000), style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Watch Video",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF0000)
                                )
                                Text(
                                    text = "Tap to open YouTube",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
                
                Column(modifier = Modifier.padding(16.dp)) {
                    // Category and featured badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${ad.category.getIcon()} ${ad.category.getDisplayName()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        if (ad.isFeatured) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = SponsoredBadge,
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                Text(
                                    text = "FEATURED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextOnPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = ad.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = ad.businessName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = ad.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Contact info
                    Text(
                        text = "Contact Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (ad.phoneNumber != null) {
                        ContactItem(
                            icon = Icons.Default.Phone,
                            label = "Phone",
                            value = ad.phoneNumber
                        )
                    }
                    
                    if (ad.email != null) {
                        ContactItem(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = ad.email
                        )
                    }
                    
                    if (ad.websiteUrl != null) {
                        ContactItem(
                            icon = Icons.Default.Language,
                            label = "Website",
                            value = ad.websiteUrl
                        )
                    }
                    
                    if (ad.location != null && ad.location.address != null) {
                        ContactItem(
                            icon = Icons.Default.LocationOn,
                            label = "Location",
                            value = ad.location.address
                        )
                    }
                    
                    // Location name if set
                    if (ad.locationName != null) {
                        ContactItem(
                            icon = Icons.Default.Place,
                            label = "Area",
                            value = ad.locationName
                        )
                    }
                    
                    // Coupon section
                    if (ad.hasCoupon && ad.couponCode != null && AdHelper.isCouponValid(ad)) {
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.1f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🎟️ SPECIAL OFFER",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Success
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = ad.couponDiscount ?: "",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Success
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Coupon code card
                                Surface(
                                    color = Surface,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Code: ",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = ad.couponCode,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Primary
                                        )
                                    }
                                }
                                
                                if (ad.couponDescription != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = ad.couponDescription,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Show this code at ${ad.businessName} to redeem",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        } else if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun ContactItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAdScreen(
    onAdCreated: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AdsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var businessName by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var youtubeUrl by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCategory by remember { mutableStateOf(AdCategory.GENERAL) }
    var selectedPlan by remember { mutableStateOf(AdPlan.ONE_WEEK) }
    var isFeatured by remember { mutableStateOf(false) }
    
    // Location fields
    var locationName by remember { mutableStateOf("") }
    var locationLat by remember { mutableStateOf("") }
    var locationLng by remember { mutableStateOf("") }
    var showLocationPicker by remember { mutableStateOf(false) }
    
    // Coupon fields
    var hasCoupon by remember { mutableStateOf(false) }
    var couponCode by remember { mutableStateOf("") }
    var couponDiscount by remember { mutableStateOf("") }
    var couponDescription by remember { mutableStateOf("") }
    var couponMaxRedemptions by remember { mutableStateOf("") }
    
    // Promo code for advertisers
    var promoCode by remember { mutableStateOf("") }
    var promoApplied by remember { mutableStateOf(false) }
    var promoMessage by remember { mutableStateOf("") }
    
    // Card payment flow
    var showPaymentConfirmDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    
    // Valid promo codes
    val validPromoCodes = mapOf(
        "FREE2WEEKS" to "First 2 weeks FREE on monthly plan!",
        "BOATTAXIE50" to "50% off your first ad!",
        "WELCOME" to "First 2 weeks FREE on monthly plan!",
        "ADFREE" to "2 Weeks Advertising 100% FREE!",
        "BOCAS2025" to "2 Weeks Advertising 100% FREE!"
    )
    
    // Calculate discounted price (moved outside for dialog access)
    val basePrice = if (isFeatured) selectedPlan.featuredPrice else selectedPlan.price
    val isTotallyFree = promoApplied && (promoCode.uppercase() == "ADFREE" || promoCode.uppercase() == "BOCAS2025") && selectedPlan == AdPlan.TWO_WEEKS
    val discountedPrice = when {
        isTotallyFree -> 0.0 // Completely free for 2 weeks with ADFREE or BOCAS2025 code
        promoApplied && (promoCode.uppercase() == "FREE2WEEKS" || promoCode.uppercase() == "WELCOME") && selectedPlan == AdPlan.ONE_MONTH -> basePrice / 2
        promoApplied && promoCode.uppercase() == "BOATTAXIE50" -> basePrice / 2
        else -> basePrice
    }
    val hasDiscount = discountedPrice < basePrice
    
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }
    
    val logoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> logoUri = uri }
    
    // Reset the adCreated flag when screen is opened
    LaunchedEffect(Unit) {
        viewModel.resetAdCreatedFlag()
    }
    
    LaunchedEffect(uiState.adCreated) {
        if (uiState.adCreated) {
            android.util.Log.d("AdsScreen", "Ad created successfully! Navigating...")
            onAdCreated()
        }
    }
    
    // Show error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            android.util.Log.e("AdsScreen", "Error message: $error")
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }
    
    // Auto-generate coupon code when business name changes and coupon is enabled
    LaunchedEffect(businessName, hasCoupon) {
        if (hasCoupon && businessName.isNotBlank() && couponCode.isBlank()) {
            couponCode = AdHelper.generateCouponCode(businessName)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Ad") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // How Ads Work - Instructions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "How Your Ad Will Be Shown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Map marker info
                    Row(verticalAlignment = Alignment.Top) {
                        Text("📍", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Your ad appears as a marker on the map for all riders in your area",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1976D2)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Featured info
                    Row(verticalAlignment = Alignment.Top) {
                        Text("⭐", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Featured ads get a highlighted marker and priority placement",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1976D2)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Coupon info
                    Row(verticalAlignment = Alignment.Top) {
                        Text("🎟️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add a coupon to attract more customers - they can tap to redeem!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1976D2)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Logo info
                    Row(verticalAlignment = Alignment.Top) {
                        Text("🖼️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Upload your logo - it shows directly on the map marker!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1976D2)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Image upload
            Card(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Ad Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            null,
                            modifier = Modifier.size(48.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add Image",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Logo upload - shows on map markers
            Text(
                text = "Business Logo (shows on map)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    onClick = { logoLauncher.launch("image/*") },
                    modifier = Modifier.size(80.dp)
                ) {
                    if (logoUri != null) {
                        AsyncImage(
                            model = logoUri,
                            contentDescription = "Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Business,
                                null,
                                modifier = Modifier.size(32.dp),
                                tint = TextSecondary
                            )
                            Text(
                                text = "Logo",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Upload a square logo for your business.\nThis will appear on the map marker.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // YouTube Video URL
            OutlinedTextField(
                value = youtubeUrl,
                onValueChange = { youtubeUrl = it },
                label = { Text("YouTube Video URL (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { 
                    Text("▶", color = Color(0xFFFF0000), style = MaterialTheme.typography.titleMedium) 
                },
                placeholder = { Text("https://youtube.com/watch?v=...") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Business info
            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("Business Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Ad Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Category dropdown
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdCategory.values().take(4).forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category.getIcon()) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ============ LOCATION SECTION ============
            Text(
                text = "📍 Business Location",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Choose your business location to show on the map",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // State for map picker
            var showMapPicker by remember { mutableStateOf(false) }
            
            // Choose on Map button - prominent option
            Card(
                onClick = { showMapPicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Map,
                        null,
                        tint = Primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Choose on Map",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Text(
                            text = if (locationLat.isNotBlank()) "Location selected ✓" else "Tap on map to select",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (locationLat.isNotBlank()) Success else TextSecondary
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        null,
                        tint = Primary
                    )
                }
            }
            
            // Show selected location if any
            if (locationName.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint = Success,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = locationName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            if (locationLat.isNotBlank() && locationLng.isNotBlank()) {
                                Text(
                                    text = "📍 ${locationLat.take(8)}, ${locationLng.take(8)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Map Picker Dialog
            if (showMapPicker) {
                AdLocationMapPicker(
                    initialLat = locationLat.toDoubleOrNull(),
                    initialLng = locationLng.toDoubleOrNull(),
                    onLocationSelected = { address, lat, lng ->
                        locationName = address
                        locationLat = lat.toString()
                        locationLng = lng.toString()
                        showMapPicker = false
                    },
                    onDismiss = { showMapPicker = false }
                )
            }
            
            // Location search field with debouncing
            var locationQuery by remember { mutableStateOf("") }
            val coroutineScope = rememberCoroutineScope()
            
            Text(
                text = "Or search by name:",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            // Debounced search - wait 300ms after user stops typing
            LaunchedEffect(locationQuery) {
                if (locationQuery.isNotBlank() && locationQuery.length >= 2) {
                    kotlinx.coroutines.delay(300)
                    viewModel.searchLocation(locationQuery)
                }
            }
            
            OutlinedTextField(
                value = locationQuery,
                onValueChange = { query ->
                    locationQuery = query
                    if (query.isBlank()) {
                        viewModel.clearLocationSearch()
                    }
                    // Search is triggered by LaunchedEffect with debouncing
                },
                label = { Text("Search location...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (locationQuery.isNotBlank()) {
                        IconButton(onClick = { 
                            locationQuery = ""
                            viewModel.clearLocationSearch()
                        }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                },
                placeholder = { Text("e.g., Casco Viejo, Panama City") }
            )
            
            // Show search results
            if (uiState.locationSearchResults.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        uiState.locationSearchResults.take(5).forEach { place ->
                            Card(
                                onClick = {
                                    locationQuery = place.primaryText
                                    locationName = place.primaryText
                                    viewModel.clearLocationSearch()
                                    // Get coordinates for the selected place
                                    coroutineScope.launch {
                                        val coords = viewModel.getPlaceCoordinates(place.placeId)
                                        if (coords != null) {
                                            locationLat = coords.first.toString()
                                            locationLng = coords.second.toString()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Place,
                                        null,
                                        tint = Primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = place.primaryText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = place.secondaryText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                            Divider()
                        }
                    }
                }
            }
            
            // Show searching indicator
            if (uiState.isSearchingLocation) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Searching...", style = MaterialTheme.typography.bodySmall)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Show selected location with coordinates
            if (locationName.isNotBlank() && locationLat.isNotBlank() && locationLng.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint = Success,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = locationName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "📍 $locationLat, $locationLng",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        IconButton(onClick = {
                            locationName = ""
                            locationLat = ""
                            locationLng = ""
                            locationQuery = ""
                        }) {
                            Icon(Icons.Default.Close, "Remove", tint = TextSecondary)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Quick location buttons for popular Panama areas
            Text(
                text = "Or select popular area:",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Triple("Casco Viejo", "8.9516", "-79.5341"),
                    Triple("Panama City", "8.9824", "-79.5199"),
                    Triple("Bocas del Toro", "9.3403", "-82.2418")
                ).forEach { (name, lat, lng) ->
                    AssistChip(
                        onClick = {
                            locationName = name
                            locationLat = lat
                            locationLng = lng
                            locationQuery = name
                        },
                        label = { Text(name, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Triple("San Blas", "9.5720", "-78.9825"),
                    Triple("Taboga Island", "8.7853", "-79.5536"),
                    Triple("Contadora", "8.6256", "-79.0364")
                ).forEach { (name, lat, lng) ->
                    AssistChip(
                        onClick = {
                            locationName = name
                            locationLat = lat
                            locationLng = lng
                            locationQuery = name
                        },
                        label = { Text(name, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ============ COUPON SECTION ============
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (hasCoupon) Success.copy(alpha = 0.1f) else Surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🎟️ Add Coupon",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Attract more customers with a special offer",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = hasCoupon,
                            onCheckedChange = { 
                                hasCoupon = it
                                if (it && businessName.isNotBlank()) {
                                    couponCode = AdHelper.generateCouponCode(businessName)
                                }
                            }
                        )
                    }
                    
                    if (hasCoupon) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Coupon code with regenerate button
                        OutlinedTextField(
                            value = couponCode,
                            onValueChange = { couponCode = it.uppercase() },
                            label = { Text("Coupon Code") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.ConfirmationNumber, 
                                    null,
                                    tint = Success
                                ) 
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { 
                                        couponCode = AdHelper.generateCouponCode(
                                            businessName.ifBlank { "DEAL" }
                                        )
                                    }
                                ) {
                                    Icon(Icons.Default.Refresh, "Generate New Code")
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Discount type - quick options
                        Text(
                            text = "Discount:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("10% OFF", "15% OFF", "20% OFF", "$5 OFF").forEach { discount ->
                                FilterChip(
                                    selected = couponDiscount == discount,
                                    onClick = { couponDiscount = discount },
                                    label = { Text(discount, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("$10 OFF", "FREE DRINK", "2 FOR 1", "FREE DESSERT").forEach { discount ->
                                FilterChip(
                                    selected = couponDiscount == discount,
                                    onClick = { couponDiscount = discount },
                                    label = { Text(discount, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Or custom discount
                        OutlinedTextField(
                            value = couponDiscount,
                            onValueChange = { couponDiscount = it },
                            label = { Text("Or custom discount") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("e.g., FREE APPETIZER") }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = couponDescription,
                            onValueChange = { couponDescription = it },
                            label = { Text("Coupon Details (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2,
                            placeholder = { Text("e.g., Valid for dine-in only. Min. purchase $20") }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = couponMaxRedemptions,
                            onValueChange = { couponMaxRedemptions = it.filter { c -> c.isDigit() } },
                            label = { Text("Max Redemptions (leave empty for unlimited)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("e.g., 100") }
                        )
                        
                        // Preview
                        if (couponCode.isNotBlank() && couponDiscount.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.2f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🎟️ COUPON PREVIEW",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = couponDiscount,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Success
                                    )
                                    Text(
                                        text = "Code: $couponCode",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (couponDescription.isNotBlank()) {
                                        Text(
                                            text = couponDescription,
                                            style = MaterialTheme.typography.bodySmall,
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Contact info
            Text(
                text = "Contact Info",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Phone, null) }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Email, null) }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = website,
                onValueChange = { website = it },
                label = { Text("Website (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Language, null) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Ad plan selection
            Text(
                text = "Ad Duration",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            AdPlan.values().forEach { plan ->
                val price = if (isFeatured) plan.featuredPrice else plan.price
                val isSelected = selectedPlan == plan
                Card(
                    onClick = { selectedPlan = plan },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Primary else Color(0xFFF5F5F5)
                    ),
                    border = if (!isSelected) BorderStroke(1.dp, Color(0xFFE0E0E0)) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = plan.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFF212121)
                        )
                        Text(
                            text = "$${String.format("%.2f", price)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color(0xFF212121)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Featured toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Featured Ad",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Get more visibility",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = isFeatured,
                    onCheckedChange = { isFeatured = it }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Promo Code Section
            Text(
                text = "Have a Promo Code?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                OutlinedTextField(
                    value = promoCode,
                    onValueChange = { 
                        promoCode = it.uppercase()
                        promoApplied = false
                        promoMessage = ""
                    },
                    label = { Text("Promo Code") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !promoApplied
                )
                Button(
                    onClick = {
                        val upperCode = promoCode.uppercase().trim()
                        if (validPromoCodes.containsKey(upperCode)) {
                            promoApplied = true
                            promoMessage = validPromoCodes[upperCode] ?: ""
                            // Force monthly plan for FREE2WEEKS promo
                            if (upperCode == "FREE2WEEKS" || upperCode == "WELCOME") {
                                selectedPlan = AdPlan.ONE_MONTH
                            }
                        } else if (upperCode.isNotBlank()) {
                            promoMessage = "Invalid promo code"
                        }
                    },
                    enabled = promoCode.isNotBlank() && !promoApplied,
                    modifier = Modifier.height(56.dp)
                ) {
                    Text(if (promoApplied) "Applied ✓" else "Apply")
                }
            }
            
            if (promoMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (promoApplied) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (promoApplied) "🎉" else "❌",
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = promoMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (promoApplied) Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Show promo details for FREE2WEEKS
                if (promoApplied && (promoCode.uppercase() == "FREE2WEEKS" || promoCode.uppercase() == "WELCOME")) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📅 How it works:",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF8F00)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Your ad runs for the full month (30 days)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5D4037)
                            )
                            Text(
                                text = "• First 2 weeks are completely FREE",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5D4037)
                            )
                            Text(
                                text = "• You only pay for weeks 3 & 4",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5D4037)
                            )
                            Text(
                                text = "• Cancel anytime during free period!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5D4037)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Total price
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (hasDiscount) Color(0xFFE8F5E9) else Primary.copy(alpha = 0.1f)
                )
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
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            if (hasDiscount) {
                                // Show original price with strikethrough
                                Text(
                                    text = "$${String.format("%.2f", basePrice)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textDecoration = TextDecoration.LineThrough,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = "$${String.format("%.2f", discountedPrice)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (hasDiscount) Color(0xFF2E7D32) else Primary
                            )
                        }
                    }
                    if (hasDiscount) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🎉 You save $${String.format("%.2f", basePrice - discountedPrice)} with promo code!",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Check if completely free - skip PayPal
            if (isTotallyFree) {
                // Free ad - no payment needed
                Surface(
                    color = Success.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎉", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your ad is FREE!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Success
                        )
                        Text(
                            text = "No payment required with code: ${promoCode.uppercase()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        val location = if (locationLat.isNotBlank() && locationLng.isNotBlank()) {
                            try {
                                GeoLocation(
                                    latitude = locationLat.toDouble(),
                                    longitude = locationLng.toDouble(),
                                    address = locationName.ifBlank { null }
                                )
                            } catch (e: Exception) {
                                null
                            }
                        } else null
                        
                        viewModel.createAd(
                            businessName = businessName,
                            title = title,
                            description = description,
                            imageUri = imageUri,
                            logoUri = logoUri,
                            youtubeUrl = youtubeUrl.ifBlank { null },
                            phone = phone.ifBlank { null },
                            email = email.ifBlank { null },
                            website = website.ifBlank { null },
                            category = selectedCategory,
                            plan = selectedPlan,
                            isFeatured = isFeatured,
                            location = location,
                            locationName = locationName.ifBlank { null },
                            hasCoupon = hasCoupon,
                            couponCode = if (hasCoupon) couponCode.ifBlank { null } else null,
                            couponDiscount = if (hasCoupon) couponDiscount.ifBlank { null } else null,
                            couponDescription = if (hasCoupon) couponDescription.ifBlank { null } else null,
                            couponMaxRedemptions = if (hasCoupon && couponMaxRedemptions.isNotBlank()) {
                                couponMaxRedemptions.toIntOrNull()
                            } else null
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = businessName.isNotBlank() && title.isNotBlank() && description.isNotBlank() && !uiState.isCreating,
                    colors = ButtonDefaults.buttonColors(containerColor = Success),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Submit FREE Ad",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // Stripe Payment Flow
                Button(
                    onClick = {
                        Toast.makeText(activity, "Pay button clicked", Toast.LENGTH_SHORT).show()
                        Log.d("AdsScreen", "Pay button clicked")
                        Log.d("AdsScreen", "Activity: $activity")
                        Log.d("AdsScreen", "Fields: businessName='$businessName', title='$title', description='$description'")
                        Log.d("AdsScreen", "Enabled: ${businessName.isNotBlank() && title.isNotBlank() && description.isNotBlank()}")
                        scope.launch {
                            activity?.let {
                                Log.d("AdsScreen", "Launching createAdCheckoutSession")
                                val success = viewModel.paymentManager.createAdCheckoutSession(
                                    activity = it,
                                    durationDays = selectedPlan.displayName.split(" ")[0].toIntOrNull() ?: 1,
                                    isFeatured = isFeatured,
                                    businessName = businessName,
                                    title = title,
                                    description = description,
                                    imageUri = imageUri?.toString(),
                                    logoUri = logoUri?.toString(),
                                    youtubeUrl = youtubeUrl.ifBlank { null },
                                    phone = phone.ifBlank { null },
                                    email = email.ifBlank { null },
                                    website = website.ifBlank { null },
                                    category = selectedCategory.name,
                                    location = if (locationName.isNotBlank()) locationName else null
                                )
                                Log.d("AdsScreen", "createAdCheckoutSession result: $success")
                                if (!success) {
                                    Log.d("AdsScreen", "Payment session creation failed")
                                }
                            } ?: Log.d("AdsScreen", "Activity is null")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = true, // Temporarily always enabled for testing
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💳", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pay with Stripe - $${String.format("%.2f", discountedPrice)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Payment required to submit your ad",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Payment confirmation dialog
    if (showPaymentConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentConfirmDialog = false },
            icon = {
                Text("💳", fontSize = 48.sp)
            },
            title = {
                Text(
                    "Confirm Payment",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Did you complete the credit card payment of $${String.format("%.2f", discountedPrice)}?",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "By confirming, your ${selectedPlan.displayName} ad for \"$businessName\" will be submitted for review.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPaymentConfirmDialog = false
                        
                        val location = if (locationLat.isNotBlank() && locationLng.isNotBlank()) {
                            try {
                                GeoLocation(
                                    latitude = locationLat.toDouble(),
                                    longitude = locationLng.toDouble(),
                                    address = locationName.ifBlank { null }
                                )
                            } catch (e: Exception) {
                                null
                            }
                        } else null
                        
                        viewModel.createAd(
                            businessName = businessName,
                            title = title,
                            description = description,
                            imageUri = imageUri,
                            logoUri = logoUri,
                            youtubeUrl = youtubeUrl.ifBlank { null },
                            phone = phone.ifBlank { null },
                            email = email.ifBlank { null },
                            website = website.ifBlank { null },
                            category = selectedCategory,
                            plan = selectedPlan,
                            isFeatured = isFeatured,
                            location = location,
                            locationName = locationName.ifBlank { null },
                            hasCoupon = hasCoupon,
                            couponCode = if (hasCoupon) couponCode.ifBlank { null } else null,
                            couponDiscount = if (hasCoupon) couponDiscount.ifBlank { null } else null,
                            couponDescription = if (hasCoupon) couponDescription.ifBlank { null } else null,
                            couponMaxRedemptions = if (hasCoupon && couponMaxRedemptions.isNotBlank()) {
                                couponMaxRedemptions.toIntOrNull()
                            } else null
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Text("Yes, Submit My Ad")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentConfirmDialog = false }) {
                    Text("Not Yet")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAdsScreen(
    onNavigateToAdDetails: (String) -> Unit,
    onNavigateToCreateAd: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AdsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // AI Generator dialog state
    var showAiDialog by remember { mutableStateOf(false) }
    var aiDescription by remember { mutableStateOf("") }
    var aiCount by remember { mutableStateOf("10") }
    
    LaunchedEffect(Unit) {
        viewModel.loadMyAds()
    }
    
    // AI Ad Generator Dialog
    if (showAiDialog) {
        AlertDialog(
            onDismissRequest = { showAiDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("\uD83E\uDD16", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Ad Generator", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Describe what ads you want to create:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = aiDescription,
                        onValueChange = { aiDescription = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("What are the ads about?") },
                        placeholder = { Text("e.g., Restaurants in Bocas Town") },
                        minLines = 2,
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = aiCount,
                        onValueChange = { 
                            if (it.isEmpty() || it.toIntOrNull() != null) {
                                aiCount = it.take(2) // Max 99
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("How many ads?") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val count = aiCount.toIntOrNull() ?: 10
                        viewModel.createDemoAds(aiDescription, count)
                        showAiDialog = false
                        aiDescription = ""
                        aiCount = "10"
                    },
                    enabled = aiDescription.isNotBlank()
                ) {
                    Text("Generate Ads")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAiDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Ads") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // AI Ad Generator button
                    IconButton(onClick = { showAiDialog = true }) {
                        Icon(Icons.Default.AutoAwesome, "AI Ad Generator")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateAd) {
                Icon(Icons.Default.Add, "Create Ad")
            }
        }
    ) { paddingValues ->
        if (uiState.myAds.isEmpty() && !uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                EmptyState(
                    icon = Icons.Default.Campaign,
                    title = "No Ads Yet",
                    message = "You haven't created any ads. Start advertising your business!",
                    actionText = "Create Ad",
                    onAction = onNavigateToCreateAd
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { showAiDialog = true }
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Ad Generator")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.myAds) { ad ->
                    MyAdCard(
                        ad = ad,
                        onClick = { onNavigateToAdDetails(ad.id) },
                        onPause = { viewModel.pauseAd(ad.id) },
                        onResume = { viewModel.resumeAd(ad.id) },
                        onDelete = { viewModel.deleteAd(ad.id) },
                        onEdit = { businessName, title, description, phone, email, website, youtubeUrl, couponDiscount, couponDesc, imageUri, logoUri ->
                            viewModel.updateAd(
                                adId = ad.id,
                                businessName = businessName,
                                title = title,
                                description = description,
                                phoneNumber = phone,
                                email = email,
                                websiteUrl = website,
                                youtubeUrl = youtubeUrl,
                                couponDiscount = couponDiscount,
                                couponDescription = couponDesc,
                                imageUri = imageUri,
                                logoUri = logoUri
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyAdCard(
    ad: Advertisement,
    onClick: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit,
    onEdit: (String, String, String, String?, String?, String?, String?, String?, String?, Uri?, Uri?) -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    
    // Edit dialog state
    var editBusinessName by remember(ad) { mutableStateOf(ad.businessName) }
    var editTitle by remember(ad) { mutableStateOf(ad.title) }
    var editDescription by remember(ad) { mutableStateOf(ad.description) }
    var editPhone by remember(ad) { mutableStateOf(ad.phoneNumber ?: "") }
    var editEmail by remember(ad) { mutableStateOf(ad.email ?: "") }
    var editWebsite by remember(ad) { mutableStateOf(ad.websiteUrl ?: "") }
    var editYoutubeUrl by remember(ad) { mutableStateOf(ad.youtubeUrl ?: "") }
    var editCouponDiscount by remember(ad) { mutableStateOf(ad.couponDiscount ?: "") }
    var editCouponDescription by remember(ad) { mutableStateOf(ad.couponDescription ?: "") }
    var editImageUri by remember { mutableStateOf<Uri?>(null) }
    var editLogoUri by remember { mutableStateOf<Uri?>(null) }
    
    // Image picker launchers
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        editImageUri = uri
    }
    
    val logoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        editLogoUri = uri
    }
    
    // Edit dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Advertisement") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Image upload section
                    Text("Images", fontWeight = FontWeight.SemiBold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Main Image
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Card(
                                onClick = { imageLauncher.launch("image/*") },
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (editImageUri != null) {
                                        AsyncImage(
                                            model = editImageUri,
                                            contentDescription = "New image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else if (!ad.imageUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = if (ad.imageUrl.startsWith("/")) File(ad.imageUrl) else ad.imageUrl,
                                            contentDescription = "Current image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Default.Image, null, modifier = Modifier.size(32.dp))
                                    }
                                }
                            }
                            Text("Image", style = MaterialTheme.typography.labelSmall)
                        }
                        
                        // Logo
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Card(
                                onClick = { logoLauncher.launch("image/*") },
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (editLogoUri != null) {
                                        AsyncImage(
                                            model = editLogoUri,
                                            contentDescription = "New logo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else if (!ad.logoUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = if (ad.logoUrl.startsWith("/")) File(ad.logoUrl) else ad.logoUrl,
                                            contentDescription = "Current logo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(ad.category.getIcon(), fontSize = 28.sp)
                                    }
                                }
                            }
                            Text("Logo", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Text("Tap to change", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    
                    Divider()
                    
                    OutlinedTextField(
                        value = editBusinessName,
                        onValueChange = { editBusinessName = it },
                        label = { Text("Business Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Ad Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editWebsite,
                        onValueChange = { editWebsite = it },
                        label = { Text("Website URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editYoutubeUrl,
                        onValueChange = { editYoutubeUrl = it },
                        label = { Text("YouTube Video URL") },
                        placeholder = { Text("https://youtube.com/watch?v=...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Text("▶️", fontSize = 18.sp)
                        }
                    )
                    if (ad.hasCoupon) {
                        Divider()
                        Text("Coupon Details", fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = editCouponDiscount,
                            onValueChange = { editCouponDiscount = it },
                            label = { Text("Discount (e.g., 10% OFF)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editCouponDescription,
                            onValueChange = { editCouponDescription = it },
                            label = { Text("Coupon Description") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEditDialog = false
                        onEdit(
                            editBusinessName,
                            editTitle,
                            editDescription,
                            editPhone.ifBlank { null },
                            editEmail.ifBlank { null },
                            editWebsite.ifBlank { null },
                            editYoutubeUrl.ifBlank { null },
                            editCouponDiscount.ifBlank { null },
                            editCouponDescription.ifBlank { null },
                            editImageUri,
                            editLogoUri
                        )
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            icon = { Icon(Icons.Default.Delete, null, tint = Error) },
            title = { Text("Delete Advertisement?") },
            text = {
                Column {
                    Text("Are you sure you want to delete \"${ad.title}\"?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Your billing history will be preserved for your records.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Check if local image exists - use remember to cache the file check
            val hasLocalImage = remember(ad.imageUrl) {
                if (ad.imageUrl.isNullOrBlank()) {
                    false
                } else if (ad.imageUrl.startsWith("/")) {
                    // Local file path - check if it actually exists
                    val file = File(ad.imageUrl)
                    val exists = file.exists()
                    android.util.Log.d("MyAdCard", "Checking file ${ad.imageUrl}: exists=$exists")
                    exists
                } else {
                    // Remote URL - assume it's valid
                    true
                }
            }
            val youtubeVideoId = ad.youtubeUrl?.let { extractYouTubeVideoId(it) }
            
            // Show image if available
            if (hasLocalImage && !ad.imageUrl.isNullOrBlank()) {
                val imageModel = remember(ad.imageUrl) {
                    if (ad.imageUrl.startsWith("/")) {
                        // Local file path - use File object
                        File(ad.imageUrl)
                    } else {
                        // Remote URL
                        ad.imageUrl
                    }
                }
                AsyncImage(
                    model = imageModel,
                    contentDescription = ad.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop,
                    onError = { android.util.Log.e("MyAdCard", "Image load error for ${ad.title}: ${it.result.throwable}") }
                )
            } else if (youtubeVideoId != null) {
                // Show YouTube thumbnail if no image but has YouTube URL
                Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                    AsyncImage(
                        model = getYouTubeThumbnailUrl(youtubeVideoId),
                        contentDescription = "YouTube Video Thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Play button overlay
                    Surface(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(48.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color(0xFFFF0000).copy(alpha = 0.9f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("▶", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            } else {
                // No image and no YouTube - show large attractive placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Primary.copy(alpha = 0.8f),
                                    Primary.copy(alpha = 0.95f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = ad.category.getIcon(),
                            fontSize = 56.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ad.category.getDisplayName(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Show YouTube indicator if available (only if image was shown above)
            if (!ad.youtubeUrl.isNullOrBlank() && hasLocalImage) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFF0000).copy(alpha = 0.1f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("▶", color = Color(0xFFFF0000), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "YouTube Video",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFF0000)
                    )
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ad.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                
                Surface(
                    color = when (ad.status) {
                        AdStatus.ACTIVE -> Success
                        AdStatus.PAUSED -> Warning
                        AdStatus.PENDING -> Info
                        AdStatus.EXPIRED -> TextSecondary
                        else -> Error
                    }.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = ad.status.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = when (ad.status) {
                            AdStatus.ACTIVE -> Success
                            AdStatus.PAUSED -> Warning
                            AdStatus.PENDING -> Info
                            AdStatus.EXPIRED -> TextSecondary
                            else -> Error
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Views/Impressions highlight
            if (ad.impressions > 0) {
                Surface(
                    color = Primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👁️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Your ad is being viewed!",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                            Text(
                                text = "${ad.impressions} drivers and riders have seen it",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatItem("Impressions", ad.impressions.toString())
                StatItem("Clicks", ad.clicks.toString())
                if (ad.impressions > 0) {
                    val ctr = (ad.clicks.toFloat() / ad.impressions * 100)
                    StatItem("CTR", "${String.format("%.1f", ctr)}%")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Actions - Edit, Pause/Resume, Delete
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Edit button
                OutlinedButton(
                    onClick = { showEditDialog = true }
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                
                if (ad.status == AdStatus.ACTIVE) {
                    OutlinedButton(
                        onClick = onPause
                    ) {
                        Icon(Icons.Default.Pause, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pause")
                    }
                } else if (ad.status == AdStatus.PAUSED) {
                    Button(
                        onClick = onResume
                    ) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resume")
                    }
                }
                
                OutlinedButton(
                    onClick = { showDeleteConfirmation = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                }
            }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
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

/**
 * Full-screen map picker for selecting ad business location
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdLocationMapPicker(
    initialLat: Double? = null,
    initialLng: Double? = null,
    onLocationSelected: (address: String, lat: Double, lng: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedLocation by remember { 
        mutableStateOf(
            if (initialLat != null && initialLng != null) 
                com.google.android.gms.maps.model.LatLng(initialLat, initialLng) 
            else null
        ) 
    }
    var selectedAddress by remember { mutableStateOf<String?>(null) }
    var isLoadingAddress by remember { mutableStateOf(false) }
    
    val geocoder = remember { android.location.Geocoder(context, java.util.Locale.getDefault()) }
    
    // Camera starts at Bocas del Toro or Panama City
    val cameraPositionState = com.google.maps.android.compose.rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
            if (initialLat != null && initialLng != null)
                com.google.android.gms.maps.model.LatLng(initialLat, initialLng)
            else
                com.google.android.gms.maps.model.LatLng(9.3403, -82.2419), // Bocas Town
            13f
        )
    }
    
    // Function to get address from coordinates
    fun getAddressFromLocation(latLng: com.google.android.gms.maps.model.LatLng) {
        isLoadingAddress = true
        scope.launch {
            try {
                val addresses = withContext(Dispatchers.IO) {
                    try {
                        @Suppress("DEPRECATION")
                        geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                    } catch (e: Exception) {
                        null
                    }
                }
                
                selectedAddress = if (!addresses.isNullOrEmpty()) {
                    addresses[0].getAddressLine(0) ?: "Selected Location"
                } else {
                    "Location: ${String.format("%.4f", latLng.latitude)}, ${String.format("%.4f", latLng.longitude)}"
                }
            } catch (e: Exception) {
                selectedAddress = "Location: ${String.format("%.4f", latLng.latitude)}, ${String.format("%.4f", latLng.longitude)}"
            }
            isLoadingAddress = false
        }
    }
    
    // Full screen dialog
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
    ) {
        Card(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top bar
                TopAppBar(
                    title = { Text("Choose Business Location") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    }
                )
                
                // Map
                Box(modifier = Modifier.weight(1f)) {
                    com.google.maps.android.compose.GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = com.google.maps.android.compose.MapUiSettings(
                            zoomControlsEnabled = true
                        ),
                        onMapClick = { latLng ->
                            selectedLocation = latLng
                            getAddressFromLocation(latLng)
                        }
                    ) {
                        selectedLocation?.let { location ->
                            com.google.maps.android.compose.Marker(
                                state = com.google.maps.android.compose.MarkerState(position = location),
                                title = "Business Location",
                                snippet = selectedAddress
                            )
                        }
                    }
                    
                    // Instructions
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.TouchApp, null, tint = Primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tap on map to select location", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    
                    // Quick jump buttons
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 80.dp, end = 16.dp)
                    ) {
                        FloatingActionButton(
                            onClick = {
                                scope.launch {
                                    cameraPositionState.animate(
                                        com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                                            com.google.android.gms.maps.model.LatLng(9.3403, -82.2419), 13f
                                        )
                                    )
                                }
                            },
                            containerColor = BoatColor,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.DirectionsBoat, "Bocas", tint = Color.White)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        FloatingActionButton(
                            onClick = {
                                scope.launch {
                                    cameraPositionState.animate(
                                        com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                                            com.google.android.gms.maps.model.LatLng(8.9824, -79.5199), 12f
                                        )
                                    )
                                }
                            },
                            containerColor = TaxiColor,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.LocationCity, "Panama City", tint = Color.White)
                        }
                    }
                }
                
                // Bottom section with selected location
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large.copy(
                        bottomStart = androidx.compose.foundation.shape.CornerSize(0.dp),
                        bottomEnd = androidx.compose.foundation.shape.CornerSize(0.dp)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        if (selectedLocation != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    null,
                                    tint = Primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Selected Location",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondary
                                    )
                                    if (isLoadingAddress) {
                                        Text("Getting address...")
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
                                        onLocationSelected(address, loc.latitude, loc.longitude)
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
                                text = "Tap anywhere on the map to select your business location",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = TextSecondary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
