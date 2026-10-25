package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ClockDisplayMode
import com.example.data.model.WallpaperMode
import com.example.ui.MainViewModel
import com.example.ui.components.AppSettingsDialog
import com.example.ui.components.CitySelectorDialog
import com.example.ui.components.DynamicSamsungClock
import com.example.ui.components.ExpandedAppsDrawer
import com.example.ui.components.LauncherSettingsDrawer
import com.example.ui.components.ReturnToAppOverlay
import com.example.ui.components.ScenicGlassBackground
import com.example.ui.components.ScenicGlassDock
import com.example.ui.components.SettingsGlassButton
import com.example.ui.components.TopStatusBar
import com.example.ui.theme.PersonalTvHomeTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var viewModelRef: MainViewModel? = null

    private fun syncDeviceLocationAndCity() {
        try {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val hasPerm = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

            if (hasPerm && locationManager != null) {
                val loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

                if (loc != null) {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(loc.latitude, loc.longitude, 1) { addresses ->
                            val detected = addresses.firstOrNull()?.locality
                                ?: addresses.firstOrNull()?.subAdminArea
                                ?: addresses.firstOrNull()?.adminArea
                            if (!detected.isNullOrBlank()) {
                                runOnUiThread {
                                    viewModelRef?.setCity(detected)
                                    Toast.makeText(this@MainActivity, "City set to $detected", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        return
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                        val detected = addresses?.firstOrNull()?.locality
                            ?: addresses?.firstOrNull()?.subAdminArea
                            ?: addresses?.firstOrNull()?.adminArea
                        if (!detected.isNullOrBlank()) {
                            viewModelRef?.setCity(detected)
                            Toast.makeText(this, "City set to $detected", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        // Fallback to device timezone city
        val fallbackCity = viewModelRef?.detectCityFromDeviceTimezone() ?: "Atlanta"
        viewModelRef?.setCity(fallbackCity)
        Toast.makeText(this, "City synced: $fallbackCity", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainViewModel = viewModel()
            viewModelRef = viewModel
            val uiState by viewModel.uiState.collectAsState()

            var isCityPickerOpen by remember { mutableStateOf(false) }

            // File picker for custom scenic wallpaper
            val wallpaperPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri ->
                if (uri != null) {
                    viewModel.setWallpaperMode(WallpaperMode.CUSTOM_FILE, uri.toString())
                    Toast.makeText(this, "Custom wallpaper applied", Toast.LENGTH_SHORT).show()
                }
            }

            // Location permission launcher for weather
            val locationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                syncDeviceLocationAndCity()
            }

            PersonalTvHomeTheme(accentColor = Color(uiState.settings.accentColorHex)) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        val canFocusBackground = !uiState.isSettingsOpen && !uiState.isAppsDrawerExpanded && uiState.selectedAppForMenu == null && !isCityPickerOpen

                        // 1. Scenic Glass Background
                        ScenicGlassBackground(settings = uiState.settings)

                        // 2. Clock & Weather Header
                        if (uiState.settings.clockMode == ClockDisplayMode.HEADER_GLASS) {
                            TopStatusBar(
                                weather = uiState.weather,
                                settings = uiState.settings,
                                timeDisplay = uiState.timeDisplay,
                                timezoneDisplay = uiState.timezoneDisplay,
                                onWeatherClick = { isCityPickerOpen = true },
                                onSettingsClick = { viewModel.toggleSettings(true) },
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .focusProperties { canFocus = canFocusBackground }
                            )
                        } else {
                            // Dynamic Samsung Lockscreen Style (Screenshot 2)
                            DynamicSamsungClock(
                                timeHour = uiState.timeHour,
                                timeMinute = uiState.timeMinute,
                                dateDisplay = uiState.dateDisplay,
                                timezoneDisplay = uiState.timezoneDisplay,
                                weather = uiState.weather,
                                settings = uiState.settings,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .focusProperties { canFocus = canFocusBackground }
                            )

                            // Quick settings button in dynamic clock mode
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 28.dp, end = 36.dp)
                                    .focusProperties { canFocus = canFocusBackground }
                            ) {
                                SettingsGlassButton(
                                    accentColor = Color(uiState.settings.accentColorHex),
                                    onClick = { viewModel.toggleSettings(true) }
                                )
                            }
                        }

                        // 3. Floating Priority Scenic Glass Dock at the Bottom
                        ScenicGlassDock(
                            priorityApps = uiState.priorityApps,
                            settings = uiState.settings,
                            onAppClick = { app -> viewModel.launchApp(app) },
                            onAppLongClick = { app -> viewModel.openAppMenu(app) },
                            onExpandAppsDrawer = { viewModel.toggleAppsDrawer(true) },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .focusProperties { canFocus = canFocusBackground }
                        )

                        // 4. Slide-up All Apps Drawer
                        ExpandedAppsDrawer(
                            isExpanded = uiState.isAppsDrawerExpanded,
                            appsList = uiState.remainingApps.ifEmpty { uiState.priorityApps },
                            settings = uiState.settings,
                            onAppClick = { app ->
                                viewModel.toggleAppsDrawer(false)
                                viewModel.launchApp(app)
                            },
                            onAppLongClick = { app ->
                                viewModel.openAppMenu(app)
                            },
                            onCloseDrawer = { viewModel.toggleAppsDrawer(false) },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )

                        // 5. Slide-out Launcher Settings Drawer on Right
                        LauncherSettingsDrawer(
                            isOpen = uiState.isSettingsOpen,
                            settings = uiState.settings,
                            availableCities = viewModel.getAvailableCities(),
                            onClose = { viewModel.toggleSettings(false) },
                            onUpdateSettings = { newSettings -> viewModel.updateSettings(newSettings) },
                            onOpenSystemSettings = { viewModel.openSystemSettings() },
                            onPickCustomWallpaper = { wallpaperPickerLauncher.launch("image/*") },
                            onSetCity = { city ->
                                viewModel.setCity(city)
                                Toast.makeText(this@MainActivity, "City changed to $city", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )

                        // 6. Long-Press App Options Context Dialog
                        if (uiState.selectedAppForMenu != null) {
                            val targetApp = uiState.selectedAppForMenu!!
                            AppSettingsDialog(
                                app = targetApp,
                                settings = uiState.settings,
                                onMoveToPriority = { isPriority ->
                                    viewModel.moveAppToPriority(targetApp, isPriority)
                                },
                                onMoveLeft = {
                                    viewModel.movePriorityApp(targetApp, true)
                                },
                                onMoveRight = {
                                    viewModel.movePriorityApp(targetApp, false)
                                },
                                onToggleFavorite = {
                                    viewModel.toggleAppFavorite(targetApp)
                                },
                                onToggleHide = {
                                    viewModel.toggleAppHidden(targetApp)
                                },
                                onOpenAppInfo = {
                                    viewModel.openAppInfo(targetApp)
                                },
                                onDismiss = {
                                    viewModel.closeAppMenu()
                                }
                            )
                        }

                        // 7. Accidental Exit 5-Second Confirmation Overlay
                        ReturnToAppOverlay(
                            visible = uiState.showExitConfirm,
                            app = uiState.exitConfirmApp,
                            secondsLeft = uiState.exitConfirmSeconds,
                            settings = uiState.settings,
                            onConfirm = { viewModel.confirmReturnToApp() },
                            onDismiss = { viewModel.dismissExitConfirm() },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 140.dp)
                        )

                        // 8. City & Location Selector Dialog
                        CitySelectorDialog(
                            isOpen = isCityPickerOpen,
                            currentCity = uiState.settings.selectedCity,
                            availableCities = viewModel.getAvailableCities(),
                            settings = uiState.settings,
                            onRequestLocation = {
                                val hasPerm = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                if (hasPerm) {
                                    syncDeviceLocationAndCity()
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            onSelectCity = { city ->
                                viewModel.setCity(city)
                                Toast.makeText(this@MainActivity, "City changed to $city", Toast.LENGTH_SHORT).show()
                            },
                            onDismiss = { isCityPickerOpen = false }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModelRef?.refreshApps()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val vm = viewModelRef
            if (vm != null) {
                if (vm.uiState.value.isSettingsOpen) {
                    vm.toggleSettings(false)
                    return true
                }
                if (vm.uiState.value.isAppsDrawerExpanded) {
                    vm.toggleAppsDrawer(false)
                    return true
                }
                if (vm.uiState.value.selectedAppForMenu != null) {
                    vm.closeAppMenu()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
