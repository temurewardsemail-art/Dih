package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BannerStyle
import com.example.data.model.ClockDisplayMode
import com.example.data.model.LauncherSettings
import com.example.data.model.WallpaperMode
import kotlinx.coroutines.delay

@Composable
fun LauncherSettingsDrawer(
    isOpen: Boolean,
    settings: LauncherSettings,
    availableCities: List<String>,
    onClose: () -> Unit,
    onUpdateSettings: (LauncherSettings) -> Unit,
    onOpenSystemSettings: () -> Unit,
    onPickCustomWallpaper: () -> Unit,
    onSetCity: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isOpen) {
        BackHandler {
            onClose()
        }
    }

    val firstSettingFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isOpen) {
        if (isOpen) {
            delay(150L)
            try {
                firstSettingFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
        modifier = modifier
    ) {
        val context = LocalContext.current
        val accentColor = Color(settings.accentColorHex)
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .pointerInput(Unit) {
                    detectTapGestures { onClose() }
                }
        ) {
            Surface(
                shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp),
                color = if (settings.scenicGlassEnabled) {
                    Color(0xFF0F172A).copy(alpha = 0.96f)
                } else {
                    Color(0xFF131A26)
                },
                border = BorderStroke(
                    1.2.dp,
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.28f), Color.White.copy(alpha = 0.05f))
                    )
                ),
                shadowElevation = 32.dp,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(460.dp)
                    .align(Alignment.CenterEnd)
                    .clickable(enabled = false) {}
                    .testTag("launcher_settings_panel")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp, vertical = 20.dp)
                ) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Launcher Settings",
                                tint = accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Launcher Settings",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        // Focusable Close Button
                        val closeInteractionSource = remember { MutableInteractionSource() }
                        val isCloseFocused by closeInteractionSource.collectIsFocusedAsState()

                        Surface(
                            shape = CircleShape,
                            color = if (isCloseFocused) accentColor else Color.White.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, if (isCloseFocused) Color.White else Color.Transparent),
                            modifier = Modifier
                                .size(34.dp)
                                .focusable(interactionSource = closeInteractionSource)
                                .clickable(interactionSource = closeInteractionSource, indication = null) { onClose() }
                                .onKeyEvent { keyEvent ->
                                    if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                                        (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) {
                                        onClose()
                                        true
                                    } else false
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Settings",
                                    tint = if (isCloseFocused) Color.Black else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Color.White.copy(alpha = 0.12f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Scrollable Settings Sections
                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(bottom = 32.dp)
                    ) {
                        // Section 1: Android TV System Settings
                        SettingsSectionHeader("TV & System", Icons.Default.Tv, accentColor)
                        TvSettingsButton(
                            text = "Open Android TV Settings",
                            icon = Icons.Default.Settings,
                            accentColor = accentColor,
                            onClick = onOpenSystemSettings,
                            modifier = Modifier.focusRequester(firstSettingFocusRequester)
                        )

                        // Section 2: Priority Bar (Scenic Glass Dock)
                        SettingsSectionHeader("Priority Bar (Scenic Dock)", Icons.Default.DisplaySettings, accentColor)
                        Text(
                            text = "Number of apps displayed on priority dock:",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.75f))
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(5, 7, 9, 11).forEach { count ->
                                val isSelected = settings.priorityDockCount == count
                                TvPillOption(
                                    label = "$count",
                                    isSelected = isSelected,
                                    accentColor = accentColor,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onUpdateSettings(settings.copy(priorityDockCount = count)) }
                                )
                            }
                        }

                        // Section 3: Wallpaper & Dynamic Video
                        SettingsSectionHeader("Wallpapers & Video Loops", Icons.Default.Palette, accentColor)
                        Text(
                            text = "Preset Wallpapers:",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.75f))
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                Pair(WallpaperMode.PRESET_SCENIC, "Twilight"),
                                Pair(WallpaperMode.PRESET_ANIME, "Anime Cute"),
                                Pair(WallpaperMode.PRESET_COSMOS, "Cosmos"),
                                Pair(WallpaperMode.PRESET_ALPINE, "Alpine")
                            ).forEach { (mode, label) ->
                                val isSelected = settings.wallpaperMode == mode && !settings.nightMode
                                TvPillOption(
                                    label = label,
                                    isSelected = isSelected,
                                    accentColor = accentColor,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        onUpdateSettings(settings.copy(wallpaperMode = mode, nightMode = false))
                                    }
                                )
                            }
                        }

                        // Custom Wallpaper from Files
                        TvSettingsButton(
                            text = "Choose Picture from Files",
                            icon = Icons.Default.FolderOpen,
                            accentColor = accentColor,
                            onClick = onPickCustomWallpaper
                        )

                        // Dynamic Scenic Video Loop
                        TvToggleRow(
                            title = "Looping Scenic Video Wallpaper",
                            subtitle = "Cinematic animated living background",
                            isChecked = settings.wallpaperMode == WallpaperMode.VIDEO_LOOP && !settings.nightMode,
                            accentColor = accentColor,
                            onToggle = { enabled ->
                                onUpdateSettings(
                                    settings.copy(
                                        wallpaperMode = if (enabled) WallpaperMode.VIDEO_LOOP else WallpaperMode.PRESET_SCENIC,
                                        nightMode = false
                                    )
                                )
                            }
                        )

                        // Night Mode
                        TvToggleRow(
                            title = "Night Mode (Pitch Black OLED)",
                            subtitle = "Turns wallpaper solid true black for dark rooms",
                            isChecked = settings.nightMode,
                            accentColor = accentColor,
                            onToggle = { enabled ->
                                onUpdateSettings(settings.copy(nightMode = enabled))
                            }
                        )

                        // Section 4: Visuals & Scenic Glass
                        SettingsSectionHeader("Visuals & Performance", Icons.Default.DisplaySettings, accentColor)

                        // Scenic Glass Toggle
                        TvToggleRow(
                            title = "Scenic Glass",
                            subtitle = "Turn off for maximum battery efficiency / lightweight speed",
                            isChecked = settings.scenicGlassEnabled,
                            accentColor = accentColor,
                            onToggle = { enabled ->
                                onUpdateSettings(settings.copy(scenicGlassEnabled = enabled))
                            }
                        )

                        // Animations Toggle
                        TvToggleRow(
                            title = "iOS-Style Fluid Animations",
                            subtitle = "Turn off for instant snappy focus transitions",
                            isChecked = settings.animationsEnabled,
                            accentColor = accentColor,
                            onToggle = { enabled ->
                                onUpdateSettings(settings.copy(animationsEnabled = enabled))
                            }
                        )

                        // Remote-Friendly Color Wheel / Palette
                        Text(
                            text = "Accent Color (Select with remote):",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.75f))
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                0xFF38BDF8 to "Cyan",
                                0xFF60A5FA to "Ice Blue",
                                0xFF34D399 to "Emerald",
                                0xFFA855F7 to "Purple",
                                0xFFF59E0B to "Amber",
                                0xFFF43F5E to "Rose",
                                0xFFF8FAFC to "White"
                            ).forEach { (colorHex, name) ->
                                val isSelected = settings.accentColorHex == colorHex
                                val colorInteractionSource = remember { MutableInteractionSource() }
                                val isColorFocused by colorInteractionSource.collectIsFocusedAsState()

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(colorHex))
                                        .border(
                                            width = if (isColorFocused) 3.dp else if (isSelected) 2.dp else 1.dp,
                                            color = if (isColorFocused) Color.White else if (isSelected) Color.White.copy(alpha = 0.9f) else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .focusable(interactionSource = colorInteractionSource)
                                        .clickable(interactionSource = colorInteractionSource, indication = null) {
                                            onUpdateSettings(settings.copy(accentColorHex = colorHex))
                                        }
                                        .onKeyEvent { keyEvent ->
                                            if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                                                (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) {
                                                onUpdateSettings(settings.copy(accentColorHex = colorHex))
                                                true
                                            } else false
                                        }
                                )
                            }
                        }

                        // Section 5: Clock & Widgets
                        SettingsSectionHeader("Clock & Time Display", Icons.Default.WbSunny, accentColor)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                Pair(ClockDisplayMode.HEADER_GLASS, "Header Glass"),
                                Pair(ClockDisplayMode.DYNAMIC_LOCKSCREEN, "Samsung Lockscreen")
                            ).forEach { (mode, label) ->
                                val isSelected = settings.clockMode == mode
                                TvPillOption(
                                    label = label,
                                    isSelected = isSelected,
                                    accentColor = accentColor,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onUpdateSettings(settings.copy(clockMode = mode)) }
                                )
                            }
                        }

                        // Clock Opacity Adjustment
                        val opacityInteractionSource = remember { MutableInteractionSource() }
                        val isOpacityFocused by opacityInteractionSource.collectIsFocusedAsState()

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, if (isOpacityFocused) accentColor else Color.Transparent, RoundedCornerShape(10.dp))
                                .padding(4.dp)
                                .focusable(interactionSource = opacityInteractionSource)
                                .onKeyEvent { keyEvent ->
                                    if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                                        when (keyEvent.nativeKeyEvent.keyCode) {
                                            KeyEvent.KEYCODE_DPAD_LEFT -> {
                                                val newOpacity = (settings.clockOpacity - 0.1f).coerceAtLeast(0.3f)
                                                onUpdateSettings(settings.copy(clockOpacity = newOpacity))
                                                true
                                            }
                                            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                                val newOpacity = (settings.clockOpacity + 0.1f).coerceAtMost(1.0f)
                                                onUpdateSettings(settings.copy(clockOpacity = newOpacity))
                                                true
                                            }
                                            else -> false
                                        }
                                    } else false
                                }
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Clock Opacity (Use Remote Left/Right):",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.75f))
                                )
                                Text(
                                    text = "${(settings.clockOpacity * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                                )
                            }
                            Slider(
                                value = settings.clockOpacity,
                                onValueChange = { onUpdateSettings(settings.copy(clockOpacity = it)) },
                                valueRange = 0.3f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = accentColor,
                                    activeTrackColor = accentColor
                                ),
                                modifier = Modifier.focusProperties { canFocus = false }
                            )
                        }

                        // Liquid Glass Widgets Toggle
                        TvToggleRow(
                            title = "Liquid Glass Widgets",
                            subtitle = "Show weather, screentime & remote cards",
                            isChecked = settings.showLiquidWidgets,
                            accentColor = accentColor,
                            onToggle = { enabled ->
                                onUpdateSettings(settings.copy(showLiquidWidgets = enabled))
                            }
                        )

                        // Section 6: Weather & City Timezone
                        SettingsSectionHeader("Weather & City Timezone", Icons.Default.WbSunny, accentColor)
                        Text(
                            text = "Current City: ${settings.selectedCity}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = accentColor, fontWeight = FontWeight.Bold)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Atlanta", "New York", "Chicago", "Los Angeles").forEach { city ->
                                val isSelected = settings.selectedCity.equals(city, ignoreCase = true)
                                TvPillOption(
                                    label = city,
                                    isSelected = isSelected,
                                    accentColor = accentColor,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onSetCity(city) }
                                )
                            }
                        }

                        // Temperature Unit Toggle
                        TvToggleRow(
                            title = "Display Celsius (°C)",
                            subtitle = if (settings.tempUnitCelsius) "Currently Celsius" else "Currently Fahrenheit (°F)",
                            isChecked = settings.tempUnitCelsius,
                            accentColor = accentColor,
                            onToggle = { enabled ->
                                onUpdateSettings(settings.copy(tempUnitCelsius = enabled))
                            }
                        )

                        // Section 7: Experimental Mode
                        SettingsSectionHeader("Experimental Mode", Icons.Default.Science, accentColor)
                        Text(
                            text = "App Banner Style:",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.75f))
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                BannerStyle.ROUNDED_18DP to "18dp",
                                BannerStyle.PILL_ROUNDED to "Pill",
                                BannerStyle.SUBTLE_EDGES to "Subtle"
                            ).forEach { (style, label) ->
                                val isSelected = settings.bannerStyle == style
                                TvPillOption(
                                    label = label,
                                    isSelected = isSelected,
                                    accentColor = accentColor,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onUpdateSettings(settings.copy(bannerStyle = style)) }
                                )
                            }
                        }

                        // Return to App Prompt Toggle
                        TvToggleRow(
                            title = "Accidental Exit Prompt",
                            subtitle = "5-second quick return overlay after leaving an app",
                            isChecked = settings.confirmExitApp,
                            accentColor = accentColor,
                            onToggle = { enabled ->
                                onUpdateSettings(settings.copy(confirmExitApp = enabled))
                            }
                        )

                        // Section 8: About Section
                        SettingsSectionHeader("About Personal TV Home", Icons.Default.Info, accentColor)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.05f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Personal TV Home",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "100% made by AI",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = accentColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Native Android TV launcher with Scenic Glass floating dock, fluid remote navigation, dynamic lockscreen time, and zero mockup apps.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                TvSettingsButton(
                                    text = "Project Repository",
                                    icon = Icons.Default.Info,
                                    accentColor = accentColor,
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/google/ai-studio")).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector, accentColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 15.sp
            )
        )
    }
}

@Composable
private fun TvPillOption(
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isFocused) {
            accentColor
        } else if (isSelected) {
            accentColor.copy(alpha = 0.35f)
        } else {
            Color.White.copy(alpha = 0.08f)
        },
        border = BorderStroke(
            1.5.dp,
            if (isFocused) Color.White else if (isSelected) accentColor else Color.Transparent
        ),
        modifier = modifier
            .height(38.dp)
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .onKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                    (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) {
                    onClick()
                    true
                } else false
            }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Normal,
                color = if (isFocused) Color.Black else Color.White
            )
        }
    }
}

@Composable
private fun TvSettingsButton(
    text: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isFocused) accentColor else Color.White.copy(alpha = 0.10f),
        border = BorderStroke(
            1.5.dp,
            if (isFocused) Color.White else Color.Transparent
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .onKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                    (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) {
                    onClick()
                    true
                } else false
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused) Color.Black else Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isFocused) Color.Black else Color.White
                )
            )
        }
    }
}

@Composable
private fun TvToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    accentColor: Color,
    onToggle: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isFocused) accentColor.copy(alpha = 0.20f) else Color.Transparent,
        border = BorderStroke(
            1.5.dp,
            if (isFocused) accentColor else Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { onToggle(!isChecked) }
            .onKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                    (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) {
                    onToggle(!isChecked)
                    true
                } else false
            }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold,
                        color = Color.White
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 11.sp
                    )
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = isChecked,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor,
                    uncheckedTrackColor = Color.White.copy(alpha = 0.15f)
                ),
                modifier = Modifier.focusProperties { canFocus = false }
            )
        }
    }
}
