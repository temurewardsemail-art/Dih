package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.LauncherSettings
import com.example.data.model.WallpaperMode

@Composable
fun ScenicGlassBackground(
    settings: LauncherSettings,
    modifier: Modifier = Modifier
) {
    val nightModeColor by animateColorAsState(
        targetValue = if (settings.nightMode) Color(0xFF000000) else Color.Transparent,
        animationSpec = tween(durationMillis = if (settings.animationsEnabled) 600 else 0),
        label = "night_mode_color"
    )

    Box(modifier = modifier.fillMaxSize()) {
        if (!settings.nightMode) {
            when (settings.wallpaperMode) {
                WallpaperMode.PRESET_SCENIC -> {
                    Image(
                        painter = painterResource(id = R.drawable.scenic_default),
                        contentDescription = "Scenic Wallpaper",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                WallpaperMode.PRESET_ANIME -> {
                    // Cute aesthetic warm pastel gradient canvas with gentle glowing highlights
                    AnimeCuteWallpaper()
                }

                WallpaperMode.PRESET_COSMOS -> {
                    CosmosNebulaWallpaper()
                }

                WallpaperMode.PRESET_ALPINE -> {
                    AlpineDawnWallpaper()
                }

                WallpaperMode.CUSTOM_FILE -> {
                    if (!settings.customWallpaperUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = Uri.parse(settings.customWallpaperUri),
                            contentDescription = "Custom Wallpaper",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Fallback to default scenic
                        Image(
                            painter = painterResource(id = R.drawable.scenic_default),
                            contentDescription = "Scenic Wallpaper",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                WallpaperMode.VIDEO_LOOP -> {
                    // Cinematic animated living wallpaper loop
                    LivingScenicVideoLoop(animationsEnabled = settings.animationsEnabled)
                }
            }

            // Subtle, clear top vignette for high readability of header time and weather
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.40f),
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.35f)
                                ),
                                startY = 0f,
                                endY = size.height
                            )
                        )
                    }
            )
        } else {
            // Night Mode: true OLED pitch black
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }

        // Night mode transition overlay
        if (settings.nightMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(nightModeColor)
            )
        }
    }
}

@Composable
private fun AnimeCuteWallpaper() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFEDF0),
                        Color(0xFFFFCCD5),
                        Color(0xFFFFAAA6)
                    ),
                    center = Offset(800f, 400f),
                    radius = 1400f
                )
            )
    ) {
        // Soft diagonal stripes and glowing stars inspired by user's screenshot
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 40f
            for (i in -size.width.toInt()..size.width.toInt() * 2 step 120) {
                drawLine(
                    color = Color.White.copy(alpha = 0.18f),
                    start = Offset(i.toFloat(), 0f),
                    end = Offset(i.toFloat() + size.height * 0.7f, size.height),
                    strokeWidth = 14f
                )
            }
        }
    }
}

@Composable
private fun CosmosNebulaWallpaper() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF030712),
                        Color(0xFF1E1B4B),
                        Color(0xFF2E1065),
                        Color(0xFF0F172A)
                    ),
                    start = Offset.Zero,
                    end = Offset(1920f, 1080f)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Ambient stars
            val points = listOf(
                Offset(300f, 180f), Offset(750f, 240f), Offset(1200f, 140f),
                Offset(1600f, 320f), Offset(450f, 600f), Offset(980f, 750f),
                Offset(1420f, 620f), Offset(200f, 850f), Offset(850f, 420f)
            )
            for (p in points) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.65f),
                    radius = 3.5f,
                    center = p
                )
            }
        }
    }
}

@Composable
private fun AlpineDawnWallpaper() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF334155),
                        Color(0xFF38BDF8).copy(alpha = 0.4f),
                        Color(0xFF0F172A)
                    )
                )
            )
    )
}

@Composable
private fun LivingScenicVideoLoop(animationsEnabled: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "scenic_loop")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animationsEnabled) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0B192C),
                        Color(0xFF1E3E62),
                        Color(0xFF000000)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1920f * (0.8f + waveOffset * 0.4f), 1080f)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width * (0.3f + waveOffset * 0.4f)
            val cy = size.height * 0.45f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF38BDF8).copy(alpha = 0.25f),
                        Color(0xFFA855F7).copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = size.width * 0.45f
                ),
                radius = size.width * 0.45f,
                center = Offset(cx, cy)
            )
        }
    }
}
