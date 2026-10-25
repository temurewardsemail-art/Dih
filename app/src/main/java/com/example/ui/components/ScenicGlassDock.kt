package com.example.ui.components

import android.view.KeyEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.data.model.LauncherSettings

@Composable
fun ScenicGlassDock(
    priorityApps: List<AppItem>,
    settings: LauncherSettings,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    onExpandAppsDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val dockCornerShape = RoundedCornerShape(26.dp)
    val accentColor = Color(settings.accentColorHex)

    val dockBackground = if (settings.scenicGlassEnabled) {
        Color(0xFF0F172A).copy(alpha = settings.glassTransparency)
    } else {
        Color(0xFF131A26)
    }

    val dockBorder = if (settings.scenicGlassEnabled) {
        BorderStroke(
            1.2.dp,
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.28f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        )
    } else {
        BorderStroke(1.dp, Color(0xFF2E384D))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 20.dp)
            .onKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    if (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        onExpandAppsDrawer()
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
    ) {
        // Floating glass dock container
        Surface(
            shape = dockCornerShape,
            border = dockBorder,
            color = dockBackground,
            shadowElevation = 16.dp,
            modifier = Modifier
                .height(116.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = dockCornerShape,
                    spotColor = Color.Black.copy(alpha = 0.45f)
                )
                .testTag("priority_glass_dock")
        ) {
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxHeight()
            ) {
                items(
                    items = priorityApps,
                    key = { it.packageName }
                ) { app ->
                    AppBannerTile(
                        app = app,
                        settings = settings,
                        onClick = { onAppClick(app) },
                        onLongClick = { onAppLongClick(app) },
                        tileWidth = 144.dp,
                        tileHeight = 84.dp,
                        showLabelBeneath = false
                    )
                }

                // All Apps button control at the end of dock
                item(key = "dock_all_apps_button") {
                    AllAppsDockControl(
                        settings = settings,
                        onClick = onExpandAppsDrawer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Subtle down chevron indicating more apps below
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand All Apps",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Press Down for More Apps",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.65f),
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun AllAppsDockControl(
    settings: LauncherSettings,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.09f else 1.0f,
        animationSpec = tween(durationMillis = if (settings.animationsEnabled) 220 else 0),
        label = "all_apps_scale"
    )

    val accentColor = Color(settings.accentColorHex)
    val shape = RoundedCornerShape(18.dp)

    Surface(
        shape = shape,
        border = if (isFocused) {
            BorderStroke(2.5.dp, accentColor)
        } else {
            BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
        },
        color = if (isFocused) {
            accentColor.copy(alpha = 0.25f)
        } else {
            Color.White.copy(alpha = 0.08f)
        },
        modifier = Modifier
            .width(84.dp)
            .height(84.dp)
            .scale(scale)
            .clip(shape)
            .testTag("dock_all_apps_button")
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .onKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            onClick()
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = "All Apps",
                tint = if (isFocused) accentColor else Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "All Apps",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isFocused) accentColor else Color.White.copy(alpha = 0.9f)
                )
            )
        }
    }
}
