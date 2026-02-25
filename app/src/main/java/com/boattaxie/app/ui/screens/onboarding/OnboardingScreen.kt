package com.boattaxie.app.ui.screens.onboarding

import android.media.MediaPlayer
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boattaxie.app.R
import com.boattaxie.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class OnboardingPage(
    val headline: String,
    val tagline: String,
    val emoji: String,
    val backgroundColor: Color,
    val accentColor: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Audio player
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentAudioPosition by remember { mutableIntStateOf(0) }
    
    // Page timings for 67 second audio (6 pages)
    val pageTimings = remember {
        listOf(
            0,      // Page 0: Welcome (0:00)
            11000,  // Page 1: Booking (0:11)
            22000,  // Page 2: Live Mode (0:22)
            33000,  // Page 3: Deals (0:33)
            44000,  // Page 4: Explore (0:44)
            55000   // Page 5: Ready (0:55)
        )
    }
    
    // Punchy call-to-action pages
    val pages = remember {
        listOf(
            OnboardingPage(
                headline = "Everything.\nEverywhere.",
                tagline = "The only map you need",
                emoji = "🗺️",
                backgroundColor = Color(0xFF0D47A1),
                accentColor = Color(0xFF64B5F6)
            ),
            OnboardingPage(
                headline = "Book.\nRide.\nArrive.",
                tagline = "Boats & taxis in seconds",
                emoji = "🚤",
                backgroundColor = Color(0xFF1565C0),
                accentColor = Color(0xFF42A5F5)
            ),
            OnboardingPage(
                headline = "Walk.\nDiscover.\nExplore.",
                tagline = "Live mode finds everything",
                emoji = "🔍",
                backgroundColor = Color(0xFF2E7D32),
                accentColor = Color(0xFF66BB6A)
            ),
            OnboardingPage(
                headline = "Save.\nMore.\nMoney.",
                tagline = "Exclusive local deals",
                emoji = "💰",
                backgroundColor = Color(0xFFE65100),
                accentColor = Color(0xFFFFB74D)
            ),
            OnboardingPage(
                headline = "Go.\nAnywhere.\nAnytime.",
                tagline = "Explore the whole world",
                emoji = "🌍",
                backgroundColor = Color(0xFF7B1FA2),
                accentColor = Color(0xFFBA68C8)
            ),
            OnboardingPage(
                headline = "You're\nReady.",
                tagline = "Let's go!",
                emoji = "🚀",
                backgroundColor = Color(0xFFC2185B),
                accentColor = Color(0xFFF48FB1)
            )
        )
    }
    
    val pagerState = rememberPagerState(pageCount = { pages.size })
    
    // Initialize audio and start playing
    LaunchedEffect(Unit) {
        try {
            mediaPlayer = MediaPlayer.create(context, R.raw.omnimap_intro)
            mediaPlayer?.setOnCompletionListener {
                isPlaying = false
                // Auto-complete when audio finishes
                onComplete()
            }
            mediaPlayer?.start()
            isPlaying = true
        } catch (e: Exception) {
            android.util.Log.e("Onboarding", "Error playing audio", e)
        }
    }
    
    // Track audio position and auto-advance pages
    LaunchedEffect(isPlaying) {
        while (isPlaying && mediaPlayer != null) {
            try {
                currentAudioPosition = mediaPlayer?.currentPosition ?: 0
                
                // Find which page we should be on based on audio position
                var targetPage = 0
                for (i in pageTimings.indices) {
                    if (currentAudioPosition >= pageTimings[i]) {
                        targetPage = i
                    }
                }
                
                // Auto-advance if needed
                if (targetPage != pagerState.currentPage && targetPage < pages.size) {
                    pagerState.animateScrollToPage(targetPage)
                }
            } catch (e: Exception) {
                // Player might be released
            }
            delay(200)
        }
    }
    
    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
    
    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "onboarding")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotateAngle"
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Dynamic gradient background
        val currentColor = pages[pagerState.currentPage].backgroundColor
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            currentColor,
                            currentColor.copy(alpha = 0.8f),
                            Color.Black
                        ),
                        radius = 1500f
                    )
                )
        )
        
        // Animated background circles
        repeat(3) { i ->
            val circleScale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000 + i * 500, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "circle$i"
            )
            
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-100 + i * 50).dp)
                    .size((200 + i * 100).dp)
                    .scale(circleScale)
                    .clip(CircleShape)
                    .background(pages[pagerState.currentPage].accentColor.copy(alpha = 0.1f - i * 0.02f))
                    .blur((10 + i * 5).dp)
            )
        }
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        mediaPlayer?.stop()
                        mediaPlayer?.release()
                        mediaPlayer = null
                        onComplete()
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Text("Skip", color = Color.White, fontWeight = FontWeight.Medium)
                    Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
            
            // Main pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false // Disable manual scroll - audio controls it
            ) { page ->
                val pageData = pages[page]
                val isCurrentPage = pagerState.currentPage == page
                
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        // Animated emoji with glow
                        Box(contentAlignment = Alignment.Center) {
                            // Glow rings
                            if (isCurrentPage) {
                                Box(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .scale(pulseScale * 1.1f)
                                        .clip(CircleShape)
                                        .background(pageData.accentColor.copy(alpha = glowAlpha * 0.2f))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .scale(pulseScale)
                                        .clip(CircleShape)
                                        .background(pageData.accentColor.copy(alpha = glowAlpha * 0.3f))
                                )
                            }
                            
                            // Emoji or Logo
                            if (page == 0) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo),
                                    contentDescription = "OmniMap",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .scale(if (isCurrentPage) pulseScale else 1f)
                                        .rotate(if (isCurrentPage) rotateAngle else 0f)
                                        .clip(RoundedCornerShape(24.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = pageData.emoji,
                                    fontSize = 100.sp,
                                    modifier = Modifier
                                        .scale(if (isCurrentPage) pulseScale else 1f)
                                        .rotate(if (isCurrentPage) rotateAngle else 0f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(48.dp))
                        
                        // Big headline with typewriter animation
                        AnimatedVisibility(
                            visible = isCurrentPage,
                            enter = fadeIn(tween(400)) + scaleIn(initialScale = 0.8f, animationSpec = tween(500)),
                            exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.8f)
                        ) {
                            Text(
                                text = pageData.headline,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                lineHeight = 52.sp,
                                letterSpacing = (-1).sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Tagline with slide animation
                        AnimatedVisibility(
                            visible = isCurrentPage,
                            enter = fadeIn(tween(600, delayMillis = 300)) + slideInVertically { 30 },
                            exit = fadeOut(tween(200))
                        ) {
                            Surface(
                                color = pageData.accentColor.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text(
                                    text = pageData.tagline,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Progress indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                pages.forEachIndexed { index, pageData ->
                    val isActive = index <= pagerState.currentPage
                    val isCurrent = index == pagerState.currentPage
                    
                    val width by animateDpAsState(
                        targetValue = if (isCurrent) 32.dp else 12.dp,
                        animationSpec = spring(dampingRatio = 0.7f),
                        label = "width"
                    )
                    
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(12.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (isActive) pageData.accentColor
                                else Color.White.copy(alpha = 0.3f)
                            )
                    )
                }
            }
            
            // Bottom action - only show Get Started on last page
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = pagerState.currentPage == pages.size - 1,
                    enter = fadeIn(tween(500)) + scaleIn(initialScale = 0.8f),
                    exit = fadeOut(tween(200))
                ) {
                    Button(
                        onClick = {
                            mediaPlayer?.stop()
                            mediaPlayer?.release()
                            mediaPlayer = null
                            onComplete()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = pages[pagerState.currentPage].accentColor,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .scale(pulseScale * 0.95f)
                    ) {
                        Text(
                            "LET'S GO!",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            Icons.Default.RocketLaunch,
                            null,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                // Audio visualizer when not on last page
                androidx.compose.animation.AnimatedVisibility(
                    visible = pagerState.currentPage < pages.size - 1 && isPlaying,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(7) { i ->
                            val barHeight by infiniteTransition.animateFloat(
                                initialValue = 8f,
                                targetValue = 28f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(150 + i * 50, easing = EaseInOutSine),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "bar$i"
                            )
                            
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .width(6.dp)
                                    .height(barHeight.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                pages[pagerState.currentPage].accentColor,
                                                Color.White
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
