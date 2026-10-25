package com.example.ui.components

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.data.model.LauncherSettings
import kotlinx.coroutines.delay

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun ExpandedAppsDrawer(
    isExpanded: Boolean,
    appsList: List<AppItem>,
    settings: LauncherSettings,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isExpanded) {
        BackHandler {
            onCloseDrawer()
        }
    }

    val firstAppFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            delay(150L)
            try {
                firstAppFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    AnimatedVisibility(
        visible = isExpanded,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        var selectedFilter by remember { mutableStateOf("All") }
        val accentColor = Color(settings.accentColorHex)

        val filteredApps = remember(selectedFilter, appsList) {
            when (selectedFilter) {
                "Favorites" -> appsList.filter { it.isFavorite }
                "Tools" -> appsList.filter { it.isSystemApp }
                else -> appsList
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f))
                .pointerInput(Unit) {
                    detectTapGestures { onCloseDrawer() }
                }
        ) {
            Surface(
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = if (settings.scenicGlassEnabled) {
                    Color(0xFF0F172A).copy(alpha = 0.92f)
                } else {
                    Color(0xFF131A26)
                },
                border = BorderStroke(
                    1.2.dp,
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.28f), Color.Transparent)
                    )
                ),
                shadowElevation = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(460.dp)
                    .align(Alignment.BottomCenter)
                    .clickable(enabled = false) {}
                    .testTag("expanded_apps_drawer")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 36.dp, vertical = 20.dp)
                ) {
                    // Header Bar with Remote-Navigable controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Focusable Collapse Button
                            val collapseInteractionSource = remember { MutableInteractionSource() }
                            val isCollapseFocused by collapseInteractionSource.collectIsFocusedAsState()

                            Surface(
                                shape = CircleShape,
                                color = if (isCollapseFocused) accentColor else Color.White.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, if (isCollapseFocused) Color.White else Color.Transparent),
                                modifier = Modifier
                                    .size(36.dp)
                                    .focusable(interactionSource = collapseInteractionSource)
                                    .clickable(interactionSource = collapseInteractionSource, indication = null) { onCloseDrawer() }
                                    .onKeyEvent { keyEvent ->
                                        if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                                            (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) {
                                            onCloseDrawer()
                                            true
                                        } else false
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Collapse Apps",
                                        tint = if (isCollapseFocused) Color.Black else accentColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "All Applications",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${filteredApps.size} apps",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            )
                        }

                        // Filter Chips (Focusable with remote)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("All", "Favorites", "Tools").forEach { filter ->
                                val isSelected = selectedFilter == filter
                                val chipInteractionSource = remember { MutableInteractionSource() }
                                val isChipFocused by chipInteractionSource.collectIsFocusedAsState()

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isChipFocused) {
                                        accentColor
                                    } else if (isSelected) {
                                        accentColor.copy(alpha = 0.35f)
                                    } else {
                                        Color.White.copy(alpha = 0.08f)
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        if (isChipFocused) Color.White else if (isSelected) accentColor else Color.White.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .focusable(interactionSource = chipInteractionSource)
                                        .clickable(interactionSource = chipInteractionSource, indication = null) {
                                            selectedFilter = filter
                                        }
                                        .onKeyEvent { keyEvent ->
                                            if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                                                (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) {
                                                selectedFilter = filter
                                                true
                                            } else false
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 14.dp)) {
                                        Text(
                                            text = filter,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected || isChipFocused) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isChipFocused) Color.Black else Color.White
                                            )
                                        )
                                    }
                                }
                            }

                            // Close Button
                            val closeInteractionSource = remember { MutableInteractionSource() }
                            val isCloseFocused by closeInteractionSource.collectIsFocusedAsState()

                            Surface(
                                shape = CircleShape,
                                color = if (isCloseFocused) accentColor else Color.White.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, if (isCloseFocused) Color.White else Color.Transparent),
                                modifier = Modifier
                                    .size(34.dp)
                                    .focusable(interactionSource = closeInteractionSource)
                                    .clickable(interactionSource = closeInteractionSource, indication = null) { onCloseDrawer() }
                                    .onKeyEvent { keyEvent ->
                                        if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                                            (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) {
                                            onCloseDrawer()
                                            true
                                        } else false
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = if (isCloseFocused) Color.Black else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (filteredApps.isEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "No apps found in this category",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 148.dp),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(
                                items = filteredApps,
                                key = { _, it -> it.packageName }
                            ) { index, app ->
                                val tileModifier = if (index == 0) {
                                    Modifier.focusRequester(firstAppFocusRequester)
                                } else {
                                    Modifier
                                }

                                AppBannerTile(
                                    app = app,
                                    settings = settings,
                                    onClick = {
                                        onAppClick(app)
                                    },
                                    onLongClick = {
                                        onAppLongClick(app)
                                    },
                                    tileWidth = 144.dp,
                                    tileHeight = 84.dp,
                                    showLabelBeneath = true,
                                    surfaceModifier = tileModifier
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
