package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import android.widget.ImageView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.onKeyEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.AppItem
import com.example.data.model.BannerStyle
import com.example.data.model.LauncherSettings

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppBannerTile(
    app: AppItem,
    settings: LauncherSettings,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    surfaceModifier: Modifier = Modifier,
    tileWidth: Dp = 144.dp,
    tileHeight: Dp = 84.dp,
    showLabelBeneath: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.09f else 1.0f,
        animationSpec = tween(durationMillis = if (settings.animationsEnabled) 220 else 0),
        label = "tile_scale"
    )

    val cornerRadius = when (settings.bannerStyle) {
        BannerStyle.ROUNDED_18DP -> 18.dp
        BannerStyle.PILL_ROUNDED -> 24.dp
        BannerStyle.SUBTLE_EDGES -> 10.dp
        BannerStyle.EXPANDED_HERO -> 20.dp
    }

    val shape = RoundedCornerShape(cornerRadius)
    val accentColor = Color(settings.accentColorHex)

    val border = if (isFocused) {
        BorderStroke(2.5.dp, accentColor)
    } else {
        BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
    }

    val coroutineScope = rememberCoroutineScope()
    var isLongPressTriggered by remember { mutableStateOf(false) }
    var pressJob by remember { mutableStateOf<Job?>(null) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Surface(
            shape = shape,
            border = border,
            color = Color(app.accentColor ?: 0xFF1E293B),
            shadowElevation = if (isFocused) 12.dp else 4.dp,
            modifier = Modifier
                .width(tileWidth)
                .height(tileHeight)
                .scale(scale)
                .testTag("app_tile_${app.packageName.replace('.', '_')}")
                .then(surfaceModifier)
                .focusable(interactionSource = interactionSource)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        // Physically tapped
                        onClick()
                    },
                    onLongClick = {
                        // Held down
                        onLongClick()
                    }
                )
                .onKeyEvent { keyEvent ->
                    val nativeEvent = keyEvent.nativeKeyEvent
                    when (nativeEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            if (nativeEvent.action == KeyEvent.ACTION_DOWN) {
                                if (nativeEvent.repeatCount == 0) {
                                    isLongPressTriggered = false
                                    pressJob?.cancel()
                                    pressJob = coroutineScope.launch {
                                        delay(450L)
                                        isLongPressTriggered = true
                                        onLongClick()
                                    }
                                } else if (!isLongPressTriggered && (nativeEvent.isLongPress || nativeEvent.repeatCount >= 1)) {
                                    isLongPressTriggered = true
                                    pressJob?.cancel()
                                    onLongClick()
                                }
                                true
                            } else if (nativeEvent.action == KeyEvent.ACTION_UP) {
                                pressJob?.cancel()
                                val wasLongPress = isLongPressTriggered
                                isLongPressTriggered = false
                                if (!wasLongPress) {
                                    // Only physical short tap opens the app
                                    onClick()
                                }
                                true
                            } else {
                                false
                            }
                        }
                        KeyEvent.KEYCODE_MENU -> {
                            if (nativeEvent.action == KeyEvent.ACTION_DOWN) {
                                onLongClick()
                                true
                            } else {
                                false
                            }
                        }
                        else -> false
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
            ) {
                // If official TV banner drawable is present
                if (app.bannerDrawable != null) {
                    AndroidView(
                        factory = { ctx ->
                            ImageView(ctx).apply {
                                scaleType = ImageView.ScaleType.CENTER_CROP
                                setImageDrawable(app.bannerDrawable)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (app.iconDrawable != null) {
                    // Full artwork banner presentation with app icon and brand background
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(app.accentColor ?: 0xFF1E293B),
                                        Color(0xFF0F172A)
                                    )
                                )
                            )
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                ImageView(ctx).apply {
                                    scaleType = ImageView.ScaleType.FIT_CENTER
                                    setImageDrawable(app.iconDrawable)
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.Center)
                        )

                        // Subtle bottom title strip for non-banner apps
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.75f)
                                        ),
                                        startY = 40f
                                    )
                                )
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Fallback artwork design with rich typography & brand icon styling
                    BrandedFallbackArtwork(app = app)
                }

                // Favorite badge
                if (app.isFavorite) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(18.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(9.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorite",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // Focused inner glass reflection highlight
                if (isFocused) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.45f),
                                        Color.Transparent
                                    )
                                ),
                                shape = shape
                            )
                    )
                }
            }
        }

        // Optional label beneath tile (used in the expanded All Apps drawer)
        if (showLabelBeneath) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal,
                    color = if (isFocused) accentColor else Color(0xFFE2E8F0)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(tileWidth)
            )
        }
    }
}

@Composable
private fun BrandedFallbackArtwork(app: AppItem) {
    val brandColors = when (app.fallbackTag) {
        "Netflix" -> Pair(Color(0xFFE50914), Color.Black)
        "YouTube TV", "YouTube" -> Pair(Color(0xFFFF0000), Color(0xFF1E1E1E))
        "Prime Video" -> Pair(Color(0xFF00A8E1), Color(0xFF001E36))
        "Twitch" -> Pair(Color(0xFF9146FF), Color(0xFF261245))
        "Plex" -> Pair(Color(0xFFE5A00D), Color(0xFF2B2100))
        "Spotify" -> Pair(Color(0xFF1ED760), Color(0xFF0D2818))
        "File Manager" -> Pair(Color(0xFF0284C7), Color(0xFF0C243B))
        "TV Settings" -> Pair(Color(0xFF475569), Color(0xFF0F172A))
        else -> Pair(Color(0xFF3B82F6), Color(0xFF1E293B))
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(brandColors.second, brandColors.first.copy(alpha = 0.35f))
                )
            )
            .padding(8.dp)
    ) {
        Text(
            text = app.label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            ),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
