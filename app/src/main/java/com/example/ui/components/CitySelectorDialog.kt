package com.example.ui.components

import android.view.KeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LauncherSettings

@Composable
fun CitySelectorDialog(
    isOpen: Boolean,
    currentCity: String,
    availableCities: List<String>,
    settings: LauncherSettings,
    onRequestLocation: () -> Unit,
    onSelectCity: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    var customCityInput by remember { mutableStateOf("") }
    val accentColor = Color(settings.accentColorHex)

    val allCities = remember(availableCities) {
        val base = availableCities.toMutableList()
        if (!base.contains("Atlanta")) base.add(0, "Atlanta")
        base
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.96f),
            border = BorderStroke(
                1.2.dp,
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f))
                )
            ),
            shadowElevation = 24.dp,
            modifier = Modifier
                .width(460.dp)
                .testTag("city_selector_dialog")
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Weather Location & City",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    val closeInteractionSource = remember { MutableInteractionSource() }
                    val isCloseFocused by closeInteractionSource.collectIsFocusedAsState()

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isCloseFocused) accentColor else Color.White.copy(alpha = 0.1f),
                        modifier = Modifier
                            .size(32.dp)
                            .focusable(interactionSource = closeInteractionSource)
                            .clickable(interactionSource = closeInteractionSource, indication = null) { onDismiss() }
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                                    (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) {
                                    onDismiss()
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

                Spacer(modifier = Modifier.height(14.dp))

                // GPS / Auto Detect Button
                val gpsInteractionSource = remember { MutableInteractionSource() }
                val isGpsFocused by gpsInteractionSource.collectIsFocusedAsState()

                Button(
                    onClick = {
                        onRequestLocation()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isGpsFocused) accentColor else accentColor.copy(alpha = 0.25f),
                        contentColor = if (isGpsFocused) Color.Black else accentColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusable(interactionSource = gpsInteractionSource)
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                                when (keyEvent.nativeKeyEvent.keyCode) {
                                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                                        onRequestLocation()
                                        onDismiss()
                                        true
                                    }
                                    else -> false
                                }
                            } else false
                        }
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Auto Detect / Use Device Location", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom City Input
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = customCityInput,
                        onValueChange = { customCityInput = it },
                        placeholder = { Text("Enter city (e.g. Atlanta, Dallas, Miami)...", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (customCityInput.isNotBlank()) {
                                    onSelectCity(customCityInput.trim())
                                    onDismiss()
                                }
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    val setInteractionSource = remember { MutableInteractionSource() }
                    val isSetFocused by setInteractionSource.collectIsFocusedAsState()

                    Button(
                        onClick = {
                            if (customCityInput.isNotBlank()) {
                                onSelectCity(customCityInput.trim())
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSetFocused) Color.White else accentColor,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = customCityInput.isNotBlank(),
                        modifier = Modifier
                            .height(52.dp)
                            .focusable(interactionSource = setInteractionSource)
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                                    when (keyEvent.nativeKeyEvent.keyCode) {
                                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                                            if (customCityInput.isNotBlank()) {
                                                onSelectCity(customCityInput.trim())
                                                onDismiss()
                                                true
                                            } else false
                                        }
                                        else -> false
                                    }
                                } else false
                            }
                    ) {
                        Text("Set", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Preset Cities (Select with remote):",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(allCities) { city ->
                        val isSelected = city.equals(currentCity, ignoreCase = true)
                        PresetCityItem(
                            city = city,
                            isSelected = isSelected,
                            accentColor = accentColor,
                            onSelect = {
                                onSelectCity(city)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetCityItem(
    city: String,
    isSelected: Boolean,
    accentColor: Color,
    onSelect: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isFocused) {
            accentColor.copy(alpha = 0.40f)
        } else if (isSelected) {
            accentColor.copy(alpha = 0.20f)
        } else {
            Color.White.copy(alpha = 0.06f)
        },
        border = BorderStroke(
            if (isFocused) 2.dp else 1.dp,
            if (isFocused) accentColor else if (isSelected) accentColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onSelect
            )
            .onKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            onSelect()
                            true
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 14.dp)
        ) {
            Text(
                text = city,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isFocused || isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isFocused) Color.White else if (isSelected) accentColor else Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            )
            if (isSelected) {
                Text(
                    text = "Active",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
