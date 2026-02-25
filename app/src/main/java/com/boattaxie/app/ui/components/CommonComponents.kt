package com.boattaxie.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.boattaxie.app.data.model.*
import com.boattaxie.app.ui.theme.*
import java.io.File

/**
 * Primary button with BoatTaxie styling
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            contentColor = TextOnPrimary,
            disabledContainerColor = Primary.copy(alpha = 0.5f),
            disabledContentColor = TextOnPrimary.copy(alpha = 0.5f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = TextOnPrimary,
                strokeWidth = 2.dp
            )
        } else {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Secondary/Outlined button
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, if (enabled) Primary else Primary.copy(alpha = 0.5f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Primary,
            disabledContentColor = Primary.copy(alpha = 0.5f)
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Vehicle type selection card (Boat or Taxi)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleTypeCard(
    vehicleType: VehicleType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, title, color) = when (vehicleType) {
        VehicleType.BOAT -> Triple(Icons.Default.DirectionsBoat, "Boat", BoatColor)
        VehicleType.TAXI -> Triple(Icons.Default.LocalTaxi, "Taxi", TaxiColor)
    }
    
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.1f) else Surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) color else Divider
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(64.dp),
                tint = if (isSelected) color else TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = if (isSelected) color else TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Subscription plan card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionPlanCard(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val savings = plan.getSavingsPercentage()
    val savingsAmount = plan.getSavingsAmount()
    val isBestValue = plan == SubscriptionPlan.MONTH_PASS || plan == SubscriptionPlan.MONTH_PASS_AUTO
    val isPopular = plan == SubscriptionPlan.WEEK_PASS
    val isAutoRenew = plan.isAutoRenew
    
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Primary.copy(alpha = 0.1f) else Surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = when {
                isSelected -> Primary
                isBestValue -> Success
                isPopular -> Warning
                else -> Divider
            }
        )
    ) {
        Column {
            // Badge for best value, popular, or auto-renew
            if (isBestValue || isPopular || isAutoRenew) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(when {
                            isAutoRenew -> Primary
                            isBestValue -> Success
                            else -> Warning
                        })
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            isAutoRenew -> "🔄 AUTO-RENEW SUBSCRIPTION"
                            isBestValue -> "🏆 BEST VALUE - Save 58%"
                            else -> "⭐ POPULAR"
                        },
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (savings > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Save $savings%",
                                style = MaterialTheme.typography.bodySmall,
                                color = Success,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = " ($${String.format("%.2f", savingsAmount)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = Success
                            )
                        }
                    }
                    Text(
                        text = "$${String.format("%.2f", plan.pricePerDay)}/day",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    // Show original price with strikethrough if there's savings
                    if (savings > 0) {
                        Text(
                            text = "$${String.format("%.2f", plan.originalPrice)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                    Text(
                        text = "$${String.format("%.2f", plan.price)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Primary else if (isBestValue) Success else TextPrimary
                    )
                }
            }
        }
    }
}

/**
 * Advertisement card for displaying local business ads
 * Now with YouTube video playback support
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvertisementCard(
    ad: Advertisement,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Check if local image exists
    val hasLocalImage = remember(ad.imageUrl) {
        if (ad.imageUrl.isNullOrBlank()) {
            false
        } else if (ad.imageUrl.startsWith("/")) {
            File(ad.imageUrl).exists()
        } else {
            true // Remote URL
        }
    }
    
    // Extract YouTube video ID if available
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
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (ad.isFeatured) AdBackground else Surface
        ),
        border = if (ad.isFeatured) BorderStroke(2.dp, AdBorder) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Image section - handle local files, YouTube thumbnails, or placeholder
            if (hasLocalImage && !ad.imageUrl.isNullOrBlank()) {
                val imageModel = remember(ad.imageUrl) {
                    if (ad.imageUrl.startsWith("/")) File(ad.imageUrl) else ad.imageUrl
                }
                // Bordered image container - no cut-off
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .border(
                            width = 2.dp,
                            color = Primary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = imageModel,
                        contentDescription = ad.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 150.dp, max = 220.dp)
                            .padding(4.dp),
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.Center,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = Primary
                                )
                            }
                        },
                        error = {
                            // Show category placeholder on error
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(
                                                Primary.copy(alpha = 0.6f),
                                                Primary.copy(alpha = 0.8f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = ad.category.getIcon(),
                                        fontSize = 48.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ad.category.getDisplayName(),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        success = { SubcomposeAsyncImageContent() }
                    )
                }
            } else if (youtubeVideoId != null) {
                // Show YouTube thumbnail with play button - clickable to open video
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable {
                            // Open YouTube video
                            val youtubeUrl = ad.youtubeUrl ?: "https://www.youtube.com/watch?v=$youtubeVideoId"
                            try {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse(youtubeUrl)
                                )
                                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.util.Log.e("AdCard", "Failed to open YouTube: ${e.message}")
                            }
                        }
                ) {
                    SubcomposeAsyncImage(
                        model = "https://img.youtube.com/vi/$youtubeVideoId/hqdefault.jpg",
                        contentDescription = "YouTube Video",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFFF0000).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = Color(0xFFFF0000)
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFFF0000).copy(alpha = 0.8f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "YouTube Video",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        success = { SubcomposeAsyncImageContent() }
                    )
                    // Play button overlay - more prominent
                    Surface(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(72.dp),
                        shape = RoundedCornerShape(36.dp),
                        color = Color(0xFFFF0000),
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Video",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    // YouTube branding
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFF0000)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "YouTube",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // Large attractive placeholder with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
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
                            fontSize = 64.sp
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
            
            Column(modifier = Modifier.padding(20.dp)) {
                // COUPON/DEAL - Show prominently at top
                if (ad.hasCoupon && ad.couponDiscount != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Success,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎟️",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = ad.couponDiscount,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Sponsored badge
                if (ad.isFeatured) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SponsoredBadge
                    ) {
                        Text(
                            text = "FEATURED",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextOnPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Category
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = ad.category.getIcon(),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = ad.category.getDisplayName(),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Title - with proper width to prevent cutoff
                Text(
                    text = ad.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Business name with logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Logo - check if file exists
                    val logoImage = ad.logoUrl ?: ad.imageUrl
                    val logoExists = remember(logoImage) {
                        if (logoImage.isNullOrBlank()) false
                        else if (logoImage.startsWith("/")) File(logoImage).exists()
                        else true
                    }
                    
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Surface
                    ) {
                        if (logoExists && !logoImage.isNullOrBlank()) {
                            SubcomposeAsyncImage(
                                model = if (logoImage.startsWith("/")) File(logoImage) else logoImage,
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                loading = {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = Primary
                                        )
                                    }
                                },
                                error = {
                                    // Category emoji fallback on error
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = ad.category.getIcon(),
                                            fontSize = 18.sp
                                        )
                                    }
                                },
                                success = { SubcomposeAsyncImageContent() }
                            )
                        } else {
                            // Category emoji fallback
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = ad.category.getIcon(),
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ad.businessName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Primary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Description
                if (ad.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = ad.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                }
                
                // Tap for more hint
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap for more details →",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    fontWeight = FontWeight.Medium
                )
                
                // Location if available
                if (ad.locationName != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = ad.locationName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Verification status badge
 */
@Composable
fun VerificationStatusBadge(
    status: VerificationStatus,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (status) {
        VerificationStatus.NONE -> "Not Verified" to TextSecondary
        VerificationStatus.PENDING -> "Pending Review" to VerificationPending
        VerificationStatus.APPROVED -> "Verified" to VerificationApproved
        VerificationStatus.REJECTED -> "Rejected" to VerificationRejected
    }
    
    val icon = when (status) {
        VerificationStatus.APPROVED -> Icons.Default.Verified
        VerificationStatus.PENDING -> Icons.Default.HourglassTop
        VerificationStatus.REJECTED -> Icons.Default.Cancel
        else -> Icons.Default.Info
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Loading overlay
 */
@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    message: String = "Loading..."
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(16.dp),
                color = Surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Empty state placeholder
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = TextHint
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryButton(
                text = actionText,
                onClick = onAction,
                modifier = Modifier.width(200.dp)
            )
        }
    }
}

/**
 * Booking status indicator
 */
@Composable
fun BookingStatusIndicator(
    status: BookingStatus,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (status) {
        BookingStatus.PENDING -> "Finding driver..." to Warning
        BookingStatus.ACCEPTED -> "Driver accepted" to Info
        BookingStatus.ARRIVED -> "Driver arrived" to Primary
        BookingStatus.IN_PROGRESS -> "Trip in progress" to Success
        BookingStatus.COMPLETED -> "Trip completed" to Success
        BookingStatus.CANCELLED -> "Cancelled" to Error
        BookingStatus.NO_DRIVERS -> "No drivers available" to Error
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (status == BookingStatus.PENDING || status == BookingStatus.IN_PROGRESS) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = color,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * AdMob Banner Ad component - shows ads while waiting
 */
@Composable
fun AdBannerView(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111" // Test banner ID
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    androidx.compose.ui.viewinterop.AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { ctx ->
            com.google.android.gms.ads.AdView(ctx).apply {
                setAdSize(com.google.android.gms.ads.AdSize.BANNER)
                setAdUnitId(adUnitId)
                loadAd(com.google.android.gms.ads.AdRequest.Builder().build())
            }
        }
    )
}

/**
 * Local business ad card - shows sponsored content from local businesses
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalAdCard(
    businessName: String,
    description: String,
    imageUrl: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column {
            // Ad image
            if (imageUrl != null) {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = businessName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Primary.copy(alpha = 0.1f)),
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
                                .background(Primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = businessName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Primary,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    },
                    success = { SubcomposeAsyncImageContent() }
                )
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = businessName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Surface(
                        color = Primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Sponsored",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
