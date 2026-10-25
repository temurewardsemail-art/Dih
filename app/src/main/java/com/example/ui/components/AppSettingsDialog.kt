package com.example.ui.components

import android.view.KeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppItem
import com.example.data.model.LauncherSettings
import kotlinx.coroutines.delay

@Composable
fun AppSettingsDialog(
    app: AppItem,
    settings: LauncherSettings,
    onMoveToPriority: (Boolean) -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleHide: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onDismiss: () -> Unit
) {
    val accentColor = Color(settings.accentColorHex)
    val shape = RoundedCornerShape(24.dp)
    var isReadyForInput by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Debounce to absorb the key-release from the long press
        delay(250L)
        isReadyForInput = true
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = shape,
            color = Color(0xFF0F172A).copy(alpha = 0.94f),
            border = BorderStroke(
                1.2.dp,
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f))
                )
            ),
            shadowElevation = 24.dp,
            modifier = Modifier
                .width(420.dp)
                .testTag("app_context_menu_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header with App Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.label,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = app.packageName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.5f)
                            ),
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions List - strictly customization options, never auto-launches
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Priority Bar Toggle
                    DialogMenuActionItem(
                        icon = Icons.Default.PushPin,
                        title = if (app.isPriority) "Remove from Priority Dock" else "Move to Priority Dock",
                        accentColor = accentColor,
                        enabled = isReadyForInput,
                        onClick = {
                            if (isReadyForInput) onMoveToPriority(!app.isPriority)
                        }
                    )

                    // Reorder Left / Right if on priority dock
                    if (app.isPriority) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                DialogMenuActionItem(
                                    icon = Icons.Default.ArrowBack,
                                    title = "Move Left",
                                    accentColor = accentColor,
                                    enabled = isReadyForInput,
                                    onClick = {
                                        if (isReadyForInput) onMoveLeft()
                                    }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DialogMenuActionItem(
                                    icon = Icons.Default.ArrowForward,
                                    title = "Move Right",
                                    accentColor = accentColor,
                                    enabled = isReadyForInput,
                                    onClick = {
                                        if (isReadyForInput) onMoveRight()
                                    }
                                )
                            }
                        }
                    }

                    // Favorite Toggle
                    DialogMenuActionItem(
                        icon = Icons.Default.Star,
                        title = if (app.isFavorite) "Remove from Favorites" else "Pin to Favorites",
                        accentColor = accentColor,
                        enabled = isReadyForInput,
                        onClick = {
                            if (isReadyForInput) onToggleFavorite()
                        }
                    )

                    // Hide App
                    DialogMenuActionItem(
                        icon = Icons.Default.VisibilityOff,
                        title = if (app.isHidden) "Unhide Application" else "Hide Application",
                        accentColor = accentColor,
                        enabled = isReadyForInput,
                        onClick = {
                            if (isReadyForInput) onToggleHide()
                        }
                    )

                    // System App Details
                    DialogMenuActionItem(
                        icon = Icons.Default.Info,
                        title = "System App Info",
                        accentColor = accentColor,
                        enabled = isReadyForInput,
                        onClick = {
                            if (isReadyForInput) onOpenAppInfo()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogMenuActionItem(
    icon: ImageVector,
    title: String,
    accentColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState().value

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isFocused) accentColor else Color.White.copy(alpha = 0.08f),
        border = BorderStroke(
            1.dp,
            if (isFocused) accentColor else Color.White.copy(alpha = 0.12f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .focusable(interactionSource = interactionSource, enabled = enabled)
            .clickable(interactionSource = interactionSource, indication = null, enabled = enabled, onClick = onClick)
            .onKeyEvent { keyEvent ->
                if (enabled && keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                    (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) {
                    onClick()
                    true
                } else false
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused) Color.Black else Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (isFocused) Color.Black else Color.White,
                    fontSize = 14.sp
                )
            )
        }
    }
}
