package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LauncherSettings
import com.example.data.repository.WeatherData

@Composable
fun TopStatusBar(
    weather: WeatherData,
    settings: LauncherSettings,
    timeDisplay: String,
    timezoneDisplay: String,
    onWeatherClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(settings.accentColorHex)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp, vertical = 24.dp)
            .testTag("top_status_bar")
    ) {
        // Upper Left: Compact Unframed Weather
        WeatherIndicator(
            weather = weather,
            isCelsius = settings.tempUnitCelsius,
            accentColor = accentColor,
            onClick = onWeatherClick
        )

        // Upper Right: Scenic Glass Status Pill (WiFi, Timezone, Time, Settings)
        GlassStatusPill(
            timeDisplay = timeDisplay,
            timezoneDisplay = timezoneDisplay,
            settings = settings,
            accentColor = accentColor,
            onSettingsClick = onSettingsClick
        )
    }
}

@Composable
private fun WeatherIndicator(
    weather: WeatherData,
    isCelsius: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .onKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN &&
                    (keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER || keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_ENTER)) {
                    onClick()
                    true
                } else false
            }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("weather_indicator")
    ) {
        val weatherIcon = when (weather.conditionCode) {
            "sun" -> Icons.Default.WbSunny
            "rain" -> Icons.Outlined.WaterDrop
            else -> Icons.Default.Cloud
        }

        val iconTint = if (weather.conditionCode == "sun") Color(0xFFFBBF24) else Color(0xFF93C5FD)

        Icon(
            imageVector = weatherIcon,
            contentDescription = weather.condition,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = weather.getTempDisplay(isCelsius),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${weather.condition} · ${weather.cityName}",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (isFocused) accentColor else Color.White.copy(alpha = 0.75f),
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}

@Composable
private fun GlassStatusPill(
    timeDisplay: String,
    timezoneDisplay: String,
    settings: LauncherSettings,
    accentColor: Color,
    onSettingsClick: () -> Unit
) {
    val pillShape = RoundedCornerShape(24.dp)

    Surface(
        shape = pillShape,
        color = if (settings.scenicGlassEnabled) {
            Color(0xFF0F172A).copy(alpha = (settings.glassTransparency * 1.1f).coerceAtMost(0.85f))
        } else {
            Color(0xFF131A26)
        },
        border = BorderStroke(
            1.dp,
            Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.05f))
            )
        ),
        shadowElevation = 8.dp,
        modifier = Modifier
            .alpha(settings.clockOpacity)
            .testTag("status_glass_pill")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp)
        ) {
            // Wi-Fi icon
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "Wi-Fi Connected",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Time & Timezone with tabular font figures
            Text(
                text = "$timeDisplay ($timezoneDisplay)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    letterSpacing = 0.2.sp
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Circular Glass Settings Button
            SettingsGlassButton(
                accentColor = accentColor,
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
fun SettingsGlassButton(
    accentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Surface(
        shape = CircleShape,
        color = if (isFocused) accentColor else Color.White.copy(alpha = 0.12f),
        border = BorderStroke(
            1.dp,
            if (isFocused) accentColor else Color.White.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .size(34.dp)
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .onKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN &&
                    (keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER || keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_ENTER)) {
                    onClick()
                    true
                } else false
            }
            .testTag("launcher_settings_button")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Launcher Settings",
                tint = if (isFocused) Color.Black else Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
