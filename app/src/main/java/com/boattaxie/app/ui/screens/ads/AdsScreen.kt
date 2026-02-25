package com.boattaxie.app.ui.screens.ads

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.boattaxie.app.BuildConfig
import com.boattaxie.app.R
import com.boattaxie.app.data.model.*
import com.boattaxie.app.ui.components.*
import com.boattaxie.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import android.content.Context

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
                // Check for direct video URL (MP4, WebM, etc.)
                val hasDirectVideo = !ad.videoUrl.isNullOrBlank()
                
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
                
                // Priority: Direct Video > Local Image > YouTube Thumbnail > Placeholder
                if (hasDirectVideo) {
                    // Inline video player for MP4/WebM/HLS videos
                    val context = LocalContext.current
                    var isPlaying by remember { mutableStateOf(false) }
                    
                    val exoPlayer = remember(ad.videoUrl) {
                        ExoPlayer.Builder(context).build().apply {
                            val mediaItem = MediaItem.fromUri(ad.videoUrl!!)
                            setMediaItem(mediaItem)
                            repeatMode = Player.REPEAT_MODE_ONE
                            prepare()
                        }
                    }
                    
                    DisposableEffect(exoPlayer) {
                        onDispose {
                            exoPlayer.release()
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
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
                        
                        // Play button overlay when not playing
                        if (!isPlaying) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(64.dp),
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = Primary.copy(alpha = 0.9f),
                                onClick = {
                                    isPlaying = true
                                    exoPlayer.play()
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("▶", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                                }
                            }
                        }
                    }
                } else if (hasLocalImage && !ad.imageUrl.isNullOrBlank()) {
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
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.Center,
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
                                text = ad.businessName.take(2).uppercase(),
                                fontSize = 48.sp,
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
                    // Featured badge only (category removed)
                    if (ad.isFeatured) {
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
    
    // Card payment flow
    var showPaymentConfirmDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    
    // Calculate price (only free for lifetime codes)
    val basePrice = if (isFeatured) selectedPlan.featuredPrice else selectedPlan.price
    val userHasFreeAdsForLife = uiState.hasFreeAdsForLife
    val isTotallyFree = userHasFreeAdsForLife
    val discountedPrice = if (isTotallyFree) 0.0 else basePrice
    val hasDiscount = discountedPrice < basePrice
    
    // Helper function to copy image to app's internal storage for persistence
    fun copyImageToInternalStorage(context: Context, sourceUri: Uri, prefix: String): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            if (inputStream != null) {
                val fileName = "${prefix}_${UUID.randomUUID()}.jpg"
                val outputFile = File(context.cacheDir, fileName)
                outputFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                inputStream.close()
                android.util.Log.d("AdsScreen", "Copied image to: ${outputFile.absolutePath}")
                Uri.fromFile(outputFile)
            } else {
                android.util.Log.e("AdsScreen", "Could not open input stream for URI: $sourceUri")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("AdsScreen", "Failed to copy image: ${e.message}", e)
            null
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            // Copy to internal storage immediately to preserve access
            scope.launch(Dispatchers.IO) {
                val localUri = copyImageToInternalStorage(context, uri, "ad_image")
                withContext(Dispatchers.Main) {
                    imageUri = localUri ?: uri  // Fall back to original if copy fails
                }
            }
        }
    }
    
    val logoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            // Copy to internal storage immediately to preserve access
            scope.launch(Dispatchers.IO) {
                val localUri = copyImageToInternalStorage(context, uri, "ad_logo")
                withContext(Dispatchers.Main) {
                    logoUri = localUri ?: uri  // Fall back to original if copy fails
                }
            }
        }
    }
    
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
                title = { Text(stringResource(R.string.create_ad_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, stringResource(R.string.close))
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
                            text = stringResource(R.string.how_ad_shown),
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
                            text = stringResource(R.string.ad_map_info),
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
                            text = stringResource(R.string.ad_featured_info),
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
                            text = stringResource(R.string.ad_coupon_info),
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
                            text = stringResource(R.string.ad_logo_info),
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
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.Center
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
                            text = stringResource(R.string.add_image),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Logo upload - shows on map markers
            Text(
                text = stringResource(R.string.business_logo),
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
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.Center
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
                                text = stringResource(R.string.logo),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.upload_logo_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Video URL (YouTube or direct MP4)
            OutlinedTextField(
                value = youtubeUrl,
                onValueChange = { youtubeUrl = it },
                label = { Text("Video URL (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { 
                    Text("▶", color = Primary, style = MaterialTheme.typography.titleMedium) 
                },
                placeholder = { Text("YouTube or MP4 link (e.g., example.com/video.mp4)") },
                supportingText = { 
                    Text(
                        "Supports YouTube links or direct video URLs (.mp4)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Business info
            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text(stringResource(R.string.business_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.ad_title_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Category dropdown
            Text(
                text = stringResource(R.string.category),
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
                text = stringResource(R.string.business_location),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.location_desc),
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
                            text = stringResource(R.string.choose_on_map),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Text(
                            text = if (locationLat.isNotBlank()) stringResource(R.string.location_selected) else stringResource(R.string.tap_to_select_location),
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
                text = stringResource(R.string.or_search_by_name),
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
                label = { Text(stringResource(R.string.search_location)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (locationQuery.isNotBlank()) {
                        IconButton(onClick = { 
                            locationQuery = ""
                            viewModel.clearLocationSearch()
                        }) {
                            Icon(Icons.Default.Clear, stringResource(R.string.close))
                        }
                    }
                },
                placeholder = { Text(stringResource(R.string.search_example)) }
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
                            Icon(Icons.Default.Close, stringResource(R.string.remove), tint = TextSecondary)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Quick location buttons for popular Panama areas
            Text(
                text = stringResource(R.string.or_select_area),
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
                                text = stringResource(R.string.add_coupon),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.coupon_attract),
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
                            label = { Text(stringResource(R.string.coupon_code_label)) },
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
                                    Icon(Icons.Default.Refresh, stringResource(R.string.generate_new_code))
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Discount type - quick options
                        Text(
                            text = stringResource(R.string.discount_label),
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
                            label = { Text(stringResource(R.string.or_custom_discount)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text(stringResource(R.string.custom_discount_placeholder)) }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = couponDescription,
                            onValueChange = { couponDescription = it },
                            label = { Text(stringResource(R.string.coupon_details_optional)) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2,
                            placeholder = { Text(stringResource(R.string.coupon_details_placeholder)) }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = couponMaxRedemptions,
                            onValueChange = { couponMaxRedemptions = it.filter { c -> c.isDigit() } },
                            label = { Text(stringResource(R.string.max_redemptions)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text(stringResource(R.string.max_redemptions_placeholder)) }
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
                                        text = stringResource(R.string.coupon_preview),
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
                text = stringResource(R.string.contact_info),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(stringResource(R.string.phone_optional)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Phone, null) }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email_optional)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Email, null) }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = website,
                onValueChange = { website = it },
                label = { Text(stringResource(R.string.website_optional)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Language, null) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Ad plan selection
            Text(
                text = stringResource(R.string.ad_duration),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            AdPlan.values().forEach { plan ->
                val price = if (isFeatured) plan.featuredPrice else plan.price
                val isSelected = selectedPlan == plan
                val isAutoRenew = plan.isAutoRenew
                Card(
                    onClick = { selectedPlan = plan },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Primary else if (isAutoRenew) Primary.copy(alpha = 0.1f) else Color(0xFFF5F5F5)
                    ),
                    border = when {
                        isSelected -> null
                        isAutoRenew -> BorderStroke(2.dp, Primary)
                        else -> BorderStroke(1.dp, Color(0xFFE0E0E0))
                    }
                ) {
                    Column {
                        // Badge for auto-renew
                        if (isAutoRenew) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Primary)
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🔄 AUTO-RENEW SUBSCRIPTION",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = plan.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color(0xFF212121)
                                )
                                if (isAutoRenew) {
                                    Text(
                                        text = "Cancel anytime",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) Color.White.copy(alpha = 0.7f) else TextSecondary
                                    )
                                }
                            }
                            Text(
                                text = "$${String.format("%.2f", price)}" + if (isAutoRenew) "/mo" else "",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF212121)
                            )
                        }
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
                        text = stringResource(R.string.featured_ad_label),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(R.string.get_more_visibility),
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
            
            // Free Featured Offer Section (One-Time Use)
            if (!uiState.usedFreeFeaturedOffer) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !uiState.isClaimingFreeOffer) {
                            // Validate required fields first
                            if (title.isBlank()) {
                                // Show error - need title
                                return@clickable
                            }
                            if (description.isBlank()) {
                                // Show error - need description
                                return@clickable
                            }
                            
                            // Claim the free featured offer
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
                            
                            viewModel.claimFreeFeaturedOffer(
                                businessName = businessName,
                                title = title,
                                description = description,
                                imageUri = imageUri,
                                logoUri = logoUri,
                                websiteUrl = website,
                                phoneNumber = phone,
                                email = email,
                                location = location,
                                locationName = locationName,
                                category = selectedCategory,
                                hasCoupon = hasCoupon,
                                couponCode = couponCode.takeIf { hasCoupon && it.isNotBlank() },
                                couponDiscount = couponDiscount.takeIf { hasCoupon && it.isNotBlank() },
                                couponDescription = couponDescription.takeIf { hasCoupon && it.isNotBlank() },
                                couponMaxRedemptions = couponMaxRedemptions.toIntOrNull()?.takeIf { hasCoupon },
                                onSuccess = { onNavigateBack() }
                            )
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🎁 Click here to get your 2 weeks FREE featured advertising now!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "1 time use only",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5D4037)
                            )
                        }
                        if (uiState.isClaimingFreeOffer) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFF2E7D32),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Claim",
                                tint = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
                
                // Show result message for free offer
                if (uiState.freeOfferMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.freeOfferClaimSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (uiState.freeOfferClaimSuccess) "🎉" else "❌",
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.freeOfferMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (uiState.freeOfferClaimSuccess) Color(0xFF2E7D32) else Color(0xFFC62828),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
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
                            text = stringResource(R.string.total_label),
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
                            text = stringResource(R.string.you_save_promo, String.format("%.2f", basePrice - discountedPrice)),
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
                            text = stringResource(R.string.ad_is_free),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Success
                        )
                        Text(
                            text = "Lifetime Free Advertiser - No payment required!",
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
                        
                        // Determine if video URL is direct (MP4) or YouTube
                        val videoUrlInput = youtubeUrl.trim()
                        val isDirectVideoUrl = videoUrlInput.endsWith(".mp4", ignoreCase = true) ||
                            videoUrlInput.endsWith(".webm", ignoreCase = true) ||
                            videoUrlInput.endsWith(".m3u8", ignoreCase = true) ||
                            videoUrlInput.contains(".mp4", ignoreCase = true)
                        
                        viewModel.createAd(
                            businessName = businessName,
                            title = title,
                            description = description,
                            imageUri = imageUri,
                            logoUri = logoUri,
                            youtubeUrl = if (!isDirectVideoUrl) videoUrlInput.ifBlank { null } else null,
                            videoUrl = if (isDirectVideoUrl) videoUrlInput.ifBlank { null } else null,
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
                                text = stringResource(R.string.submit_free_ad),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // Google Play Payment Flow
                Button(
                    onClick = {
                        Toast.makeText(activity, "Pay button clicked", Toast.LENGTH_SHORT).show()
                        Log.d("AdsScreen", "Pay button clicked")
                        Log.d("AdsScreen", "Activity: $activity")
                        Log.d("AdsScreen", "Fields: businessName='$businessName', title='$title', description='$description'")
                        Log.d("AdsScreen", "Enabled: ${businessName.isNotBlank() && title.isNotBlank() && description.isNotBlank()}")
                        scope.launch {
                            activity?.let {
                                Log.d("AdsScreen", "Launching createAdCheckoutSession with days=${selectedPlan.days}")
                                val userId = viewModel.currentUserId ?: ""
                                val success = viewModel.paymentManager.createAdCheckoutSession(
                                    activity = it,
                                    durationDays = selectedPlan.days,
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
                                    location = if (locationName.isNotBlank()) locationName else null,
                                    userId = userId,
                                    isAutoRenew = selectedPlan.isAutoRenew
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
                            text = stringResource(R.string.pay_with_google_play, String.format("%.2f", discountedPrice)),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.payment_required),
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
                    stringResource(R.string.confirm_payment_title),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.payment_confirm_question, String.format("%.2f", discountedPrice)),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.payment_confirm_desc, selectedPlan.displayName, businessName),
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
                    Text(stringResource(R.string.yes_submit_ad))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentConfirmDialog = false }) {
                    Text(stringResource(R.string.not_yet))
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity
    val scope = rememberCoroutineScope()
    
    // AI Generator dialog state
    var showAiDialog by remember { mutableStateOf(false) }
    var aiDescription by remember { mutableStateOf("") }
    var aiCount by remember { mutableStateOf("10") }
    
    // Activation dialog state
    var showActivateDialog by remember { mutableStateOf(false) }
    var adToActivate by remember { mutableStateOf<Advertisement?>(null) }
    var selectedActivationPlan by remember { mutableStateOf(AdPlan.TWO_WEEKS) }
    var activationIsFeatured by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadMyAds()
    }
    
    // Activate Draft Ad Dialog with plan selection
    if (showActivateDialog && adToActivate != null) {
        val ad = adToActivate!!
        val currentPrice = if (activationIsFeatured) selectedActivationPlan.featuredPrice else selectedActivationPlan.price
        
        AlertDialog(
            onDismissRequest = { 
                showActivateDialog = false
                adToActivate = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💳", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Activate Ad", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Activate \"${ad.title}\"",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Duration Selection
                    Text(
                        text = "Select Duration",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    AdPlan.values().forEach { plan ->
                        val isSelected = selectedActivationPlan == plan
                        val planPrice = if (activationIsFeatured) plan.featuredPrice else plan.price
                        
                        Surface(
                            onClick = { selectedActivationPlan = plan },
                            color = if (isSelected) Primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            border = if (isSelected) 
                                androidx.compose.foundation.BorderStroke(2.dp, Primary) 
                            else 
                                androidx.compose.foundation.BorderStroke(1.dp, TextSecondary.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = plan.displayName,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = "$${String.format("%.2f", plan.getPricePerDay(activationIsFeatured))}/day",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                Text(
                                    text = "$${String.format("%.2f", planPrice)}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Primary else TextSecondary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Featured Toggle
                    Surface(
                        onClick = { activationIsFeatured = !activationIsFeatured },
                        color = if (activationIsFeatured) Warning.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        border = if (activationIsFeatured) 
                            androidx.compose.foundation.BorderStroke(2.dp, Warning) 
                        else 
                            androidx.compose.foundation.BorderStroke(1.dp, TextSecondary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("⭐", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Featured Ad",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = "Get top placement & more visibility",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Switch(
                                checked = activationIsFeatured,
                                onCheckedChange = { activationIsFeatured = it }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Total Price
                    Surface(
                        color = Success.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total", fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "${selectedActivationPlan.displayName}${if (activationIsFeatured) " Featured" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = "$${String.format("%.2f", currentPrice)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Success
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            activity?.let {
                                val userId = viewModel.currentUserId ?: ""
                                // Pass existing ad ID so webhook can activate it
                                viewModel.paymentManager.createAdCheckoutSession(
                                    activity = it,
                                    durationDays = selectedActivationPlan.days,
                                    isFeatured = activationIsFeatured,
                                    businessName = ad.businessName,
                                    title = ad.title,
                                    description = ad.description,
                                    imageUri = ad.imageUrl,
                                    logoUri = ad.logoUrl,
                                    youtubeUrl = ad.youtubeUrl,
                                    phone = ad.phoneNumber,
                                    email = ad.email,
                                    website = ad.websiteUrl,
                                    category = ad.category.name,
                                    location = ad.locationName,
                                    userId = userId,
                                    existingAdId = ad.id, // Pass existing ad ID for activation
                                    isAutoRenew = selectedActivationPlan.isAutoRenew
                                )
                            }
                        }
                        showActivateDialog = false
                        adToActivate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Text("Pay $${String.format("%.2f", currentPrice)}")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showActivateDialog = false
                    adToActivate = null
                }) {
                    Text("Cancel")
                }
            }
        )
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
                        text = "Generate ad drafts with AI. You can review and pay to activate each ad.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ Ads will be created as drafts and require payment to go live.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
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
                        label = { Text(stringResource(R.string.how_many_ads)) },
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
                    Text(stringResource(R.string.generate_drafts))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAiDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_ads)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    // Refresh button to get updated stats
                    IconButton(onClick = { viewModel.loadMyAds() }) {
                        Icon(Icons.Default.Refresh, stringResource(R.string.refresh_ads))
                    }
                    // AI Ad Generator button
                    IconButton(onClick = { showAiDialog = true }) {
                        Icon(Icons.Default.AutoAwesome, stringResource(R.string.ai_ad_generator))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateAd) {
                Icon(Icons.Default.Add, stringResource(R.string.create_ad))
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
                    title = stringResource(R.string.no_ads_yet),
                    message = stringResource(R.string.no_ads_message),
                    actionText = stringResource(R.string.create_ad),
                    onAction = onNavigateToCreateAd
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { showAiDialog = true }
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.ai_ad_generator))
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
                // Subscription Status Card
                item {
                    if (uiState.hasActiveSubscription && uiState.subscription != null) {
                        val remainingDays = SubscriptionHelper.getRemainingDays(uiState.subscription!!)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Success,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🚀 Ride Subscription Active",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Success
                                    )
                                    Text(
                                        text = "$remainingDays day${if (remainingDays != 1) "s" else ""} remaining",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                Surface(
                                    color = Success,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "${remainingDays}d",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    // Free Ads for Life banner
                    if (uiState.hasFreeAdsForLife) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎁", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "FREE Ads for Life!",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
                                    )
                                    Text(
                                        text = "Your promo code is active",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
                
                items(uiState.myAds) { ad ->
                    MyAdCard(
                        ad = ad,
                        onClick = { onNavigateToAdDetails(ad.id) },
                        onPause = { viewModel.pauseAd(ad.id) },
                        onResume = { viewModel.resumeAd(ad.id) },
                        onDelete = { viewModel.deleteAd(ad.id) },
                        onActivate = {
                            adToActivate = ad
                            showActivateDialog = true
                        },
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
    onActivate: () -> Unit,
    onEdit: (String, String, String, String?, String?, String?, String?, String?, String?, Uri?, Uri?) -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Helper function to copy image to internal storage for persistence
    fun copyImageToInternalStorage(sourceUri: Uri, prefix: String): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            if (inputStream != null) {
                val fileName = "${prefix}_${UUID.randomUUID()}.jpg"
                val outputFile = File(context.cacheDir, fileName)
                outputFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                inputStream.close()
                android.util.Log.d("AdsScreen", "Copied edit image to: ${outputFile.absolutePath}")
                Uri.fromFile(outputFile)
            } else {
                android.util.Log.e("AdsScreen", "Could not open input stream for URI: $sourceUri")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("AdsScreen", "Failed to copy edit image: ${e.message}", e)
            null
        }
    }
    
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
    
    // Image picker launchers - copy to internal storage immediately for persistence
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val localUri = copyImageToInternalStorage(uri, "edit_image")
                withContext(Dispatchers.Main) {
                    editImageUri = localUri ?: uri
                }
            }
        }
    }
    
    val logoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val localUri = copyImageToInternalStorage(uri, "edit_logo")
                withContext(Dispatchers.Main) {
                    editLogoUri = localUri ?: uri
                }
            }
        }
    }
    
    // Edit dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(stringResource(R.string.edit_advertisement)) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Image upload section
                    Text(stringResource(R.string.images), fontWeight = FontWeight.SemiBold)
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
                                            contentScale = ContentScale.Fit
                                        )
                                    } else if (!ad.imageUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = if (ad.imageUrl.startsWith("/")) File(ad.imageUrl) else ad.imageUrl,
                                            contentDescription = "Current image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Icon(Icons.Default.Image, null, modifier = Modifier.size(32.dp))
                                    }
                                }
                            }
                            Text(stringResource(R.string.image), style = MaterialTheme.typography.labelSmall)
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
                                            contentScale = ContentScale.Fit
                                        )
                                    } else if (!ad.logoUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = if (ad.logoUrl.startsWith("/")) File(ad.logoUrl) else ad.logoUrl,
                                            contentDescription = "Current logo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Text(ad.category.getIcon(), fontSize = 28.sp)
                                    }
                                }
                            }
                            Text(stringResource(R.string.logo), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Text(stringResource(R.string.tap_to_change), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    
                    Divider()
                    
                    OutlinedTextField(
                        value = editBusinessName,
                        onValueChange = { editBusinessName = it },
                        label = { Text(stringResource(R.string.business_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text(stringResource(R.string.ad_title_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text(stringResource(R.string.description)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text(stringResource(R.string.phone_number)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text(stringResource(R.string.email)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editWebsite,
                        onValueChange = { editWebsite = it },
                        label = { Text(stringResource(R.string.website_url)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editYoutubeUrl,
                        onValueChange = { editYoutubeUrl = it },
                        label = { Text(stringResource(R.string.youtube_video_url)) },
                        placeholder = { Text("https://youtube.com/watch?v=...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Text("▶️", fontSize = 18.sp)
                        }
                    )
                    if (ad.hasCoupon) {
                        Divider()
                        Text(stringResource(R.string.coupon_details), fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = editCouponDiscount,
                            onValueChange = { editCouponDiscount = it },
                            label = { Text(stringResource(R.string.discount_example)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editCouponDescription,
                            onValueChange = { editCouponDescription = it },
                            label = { Text(stringResource(R.string.coupon_description)) },
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
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            icon = { Icon(Icons.Default.Delete, null, tint = Error) },
            title = { Text(stringResource(R.string.delete_advertisement)) },
            text = {
                Column {
                    Text(stringResource(R.string.delete_ad_confirm, ad.title))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.billing_preserved),
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
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
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
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.Center,
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
                            text = ad.businessName.take(2).uppercase(),
                            fontSize = 36.sp,
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
                        text = stringResource(R.string.youtube_video),
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
                        AdStatus.DRAFT -> Warning
                        AdStatus.EXPIRED -> TextSecondary
                        else -> Error
                    }.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (ad.status == AdStatus.DRAFT) "DRAFT" else ad.status.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = when (ad.status) {
                            AdStatus.ACTIVE -> Success
                            AdStatus.PAUSED -> Warning
                            AdStatus.PENDING -> Info
                            AdStatus.DRAFT -> Warning
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
                                text = stringResource(R.string.ad_being_viewed_title),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                            Text(
                                text = stringResource(R.string.ad_views_count, ad.impressions),
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
            
            // Show "needs payment" banner for draft ads
            if (ad.status == AdStatus.DRAFT) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Warning.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏳", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.draft_payment_required),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Warning
                            )
                            Text(
                                text = stringResource(R.string.pay_to_activate),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Actions - Edit, Pause/Resume, Activate (for drafts), Delete
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Edit button
                OutlinedButton(
                    onClick = { showEditDialog = true }
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.edit))
                }
                
                if (ad.status == AdStatus.DRAFT) {
                    Button(
                        onClick = onActivate,
                        colors = ButtonDefaults.buttonColors(containerColor = Success)
                    ) {
                        Icon(Icons.Default.Payment, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.activate))
                    }
                } else if (ad.status == AdStatus.ACTIVE) {
                    OutlinedButton(
                        onClick = onPause
                    ) {
                        Icon(Icons.Default.Pause, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.pause))
                    }
                } else if (ad.status == AdStatus.PAUSED) {
                    Button(
                        onClick = onResume
                    ) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.resume))
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
