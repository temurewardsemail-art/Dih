package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppItem
import com.example.data.model.LauncherSettings
import com.example.data.model.WallpaperMode
import com.example.data.repository.AppManagerRepository
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.WeatherData
import com.example.data.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class LauncherUiState(
    val priorityApps: List<AppItem> = emptyList(),
    val remainingApps: List<AppItem> = emptyList(),
    val settings: LauncherSettings = LauncherSettings(),
    val weather: WeatherData = WeatherData(),
    val isAppsDrawerExpanded: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val selectedAppForMenu: AppItem? = null,
    val showExitConfirm: Boolean = false,
    val exitConfirmApp: AppItem? = null,
    val exitConfirmSeconds: Int = 5,
    val timeDisplay: String = "12:00",
    val timeHour: String = "12",
    val timeMinute: String = "00",
    val dateDisplay: String = "Sun, Oct 25",
    val timezoneDisplay: String = TimeZone.getDefault().getDisplayName(TimeZone.getDefault().inDaylightTime(Date()), TimeZone.SHORT, Locale.US)
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val prefsRepo = PreferencesRepository(application)
    private val appManagerRepo = AppManagerRepository(application)
    private val weatherRepo = WeatherRepository()

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    private var clockJob: Job? = null
    private var exitConfirmJob: Job? = null

    init {
        // Collect preferences
        viewModelScope.launch {
            prefsRepo.settings.collect { newSettings ->
                _uiState.value = _uiState.value.copy(settings = newSettings)
                // If city is default Auto, align weather and clock with the device's actual location
                if (newSettings.selectedCity == "Auto (Local)") {
                    val localCity = detectCityFromDeviceTimezone()
                    weatherRepo.setCity(localCity)
                } else {
                    weatherRepo.setCity(newSettings.selectedCity)
                }
                updateTime()
                refreshApps(newSettings)
            }
        }

        // Collect weather
        viewModelScope.launch {
            weatherRepo.currentWeather.collect { newWeather ->
                _uiState.value = _uiState.value.copy(weather = newWeather)
            }
        }

        startClockUpdates()
    }

    private fun startClockUpdates() {
        clockJob?.cancel()
        clockJob = viewModelScope.launch {
            while (isActive) {
                updateTime()
                delay(1000)
            }
        }
    }

    private fun updateTime() {
        val now = Date()
        val settings = _uiState.value.settings

        // Dynamically resolve timezone based on the city the user is in
        val tz: TimeZone = if (settings.selectedCity.isNotBlank() && !settings.selectedCity.equals("Auto (Local)", ignoreCase = true)) {
            resolveTimezoneForCity(settings.selectedCity)
        } else if (settings.selectedTimezone.isNotBlank() && settings.selectedTimezone != "Auto") {
            try {
                TimeZone.getTimeZone(settings.selectedTimezone)
            } catch (_: Exception) {
                TimeZone.getDefault()
            }
        } else {
            TimeZone.getDefault()
        }

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault()).apply { timeZone = tz }
        val hourFormat = SimpleDateFormat("hh", Locale.getDefault()).apply { timeZone = tz }
        val minFormat = SimpleDateFormat("mm", Locale.getDefault()).apply { timeZone = tz }
        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).apply { timeZone = tz }

        val timeStr = timeFormat.format(now)
        val hourStr = hourFormat.format(now)
        val minStr = minFormat.format(now)
        val dateStr = dateFormat.format(now)

        // Automatically format daylight saving vs standard time correctly (e.g. EDT vs EST, PDT vs PST, BST vs GMT)
        val tzStr = tz.getDisplayName(tz.inDaylightTime(now), TimeZone.SHORT, Locale.US)

        _uiState.value = _uiState.value.copy(
            timeDisplay = timeStr,
            timeHour = hourStr,
            timeMinute = minStr,
            dateDisplay = dateStr,
            timezoneDisplay = tzStr
        )
    }

    fun resolveTimezoneForCity(cityName: String): TimeZone {
        if (cityName.equals("Auto (Local)", ignoreCase = true) || cityName.isBlank()) {
            return TimeZone.getDefault()
        }
        val lower = cityName.lowercase(Locale.ROOT).trim()
        val tzId = when {
            lower.contains("new york") || lower.contains("miami") || lower.contains("boston") ||
            lower.contains("atlanta") || lower.contains("orlando") || lower.contains("philadelphia") ||
            lower.contains("washington") || lower.contains("toronto") || lower.contains("montreal") ||
            lower.contains("detroit") || lower.contains("cleveland") || lower.contains("baltimore") ||
            lower.contains("pittsburgh") || lower.contains("charlotte") || lower.contains("tampa") -> "America/New_York"

            lower.contains("chicago") || lower.contains("dallas") || lower.contains("houston") ||
            lower.contains("austin") || lower.contains("minneapolis") || lower.contains("st. louis") ||
            lower.contains("kansas") || lower.contains("san antonio") || lower.contains("nashville") ||
            lower.contains("memphis") || lower.contains("milwaukee") || lower.contains("oklahoma") ||
            lower.contains("new orleans") || lower.contains("winnipeg") || lower.contains("mexico city") -> "America/Chicago"

            lower.contains("denver") || lower.contains("salt lake") || lower.contains("albuquerque") ||
            lower.contains("calgary") || lower.contains("edmonton") || lower.contains("boise") ||
            lower.contains("el paso") -> "America/Denver"

            lower.contains("phoenix") || lower.contains("tucson") || lower.contains("arizona") -> "America/Phoenix"

            lower.contains("los angeles") || lower.contains("san francisco") || lower.contains("seattle") ||
            lower.contains("san diego") || lower.contains("portland") || lower.contains("las vegas") ||
            lower.contains("vancouver") || lower.contains("sacramento") || lower.contains("san jose") ||
            lower.contains("oakland") || lower.contains("fresno") -> "America/Los_Angeles"

            lower.contains("honolulu") || lower.contains("hawaii") -> "Pacific/Honolulu"
            lower.contains("anchorage") || lower.contains("alaska") || lower.contains("juneau") -> "America/Anchorage"

            lower.contains("london") || lower.contains("dublin") || lower.contains("edinburgh") ||
            lower.contains("belfast") || lower.contains("manchester") || lower.contains("glasgow") ||
            lower.contains("cardiff") || lower.contains("lisbon") -> "Europe/London"

            lower.contains("paris") || lower.contains("berlin") || lower.contains("rome") ||
            lower.contains("madrid") || lower.contains("amsterdam") || lower.contains("brussels") ||
            lower.contains("vienna") || lower.contains("stockholm") || lower.contains("warsaw") ||
            lower.contains("zurich") || lower.contains("oslo") || lower.contains("copenhagen") ||
            lower.contains("prague") || lower.contains("budapest") || lower.contains("frankfurt") ||
            lower.contains("munich") || lower.contains("barcelona") || lower.contains("milan") -> "Europe/Paris"

            lower.contains("athens") || lower.contains("cairo") || lower.contains("helsinki") ||
            lower.contains("istanbul") || lower.contains("jerusalem") || lower.contains("bucharest") ||
            lower.contains("kyiv") || lower.contains("beirut") -> "Europe/Athens"

            lower.contains("tokyo") || lower.contains("osaka") || lower.contains("kyoto") ||
            lower.contains("sapporo") || lower.contains("yokohama") || lower.contains("fukuoka") -> "Asia/Tokyo"

            lower.contains("seoul") || lower.contains("busan") || lower.contains("incheon") -> "Asia/Seoul"

            lower.contains("beijing") || lower.contains("shanghai") || lower.contains("hong kong") ||
            lower.contains("singapore") || lower.contains("taipei") || lower.contains("guangzhou") ||
            lower.contains("shenzhen") || lower.contains("manila") || lower.contains("kuala lumpur") -> "Asia/Shanghai"

            lower.contains("sydney") || lower.contains("melbourne") || lower.contains("canberra") ||
            lower.contains("hobart") -> "Australia/Sydney"
            lower.contains("brisbane") || lower.contains("gold coast") -> "Australia/Brisbane"
            lower.contains("adelaide") -> "Australia/Adelaide"
            lower.contains("perth") -> "Australia/Perth"

            lower.contains("auckland") || lower.contains("wellington") || lower.contains("christchurch") -> "Pacific/Auckland"

            lower.contains("dubai") || lower.contains("abu dhabi") || lower.contains("doha") ||
            lower.contains("muscat") -> "Asia/Dubai"

            lower.contains("mumbai") || lower.contains("delhi") || lower.contains("bangalore") ||
            lower.contains("kolkata") || lower.contains("hyderabad") || lower.contains("chennai") -> "Asia/Kolkata"

            lower.contains("sao paulo") || lower.contains("rio") || lower.contains("buenos aires") -> "America/Sao_Paulo"

            else -> null
        }

        if (tzId != null) {
            return TimeZone.getTimeZone(tzId)
        }

        try {
            val candidate = TimeZone.getTimeZone(cityName)
            if (candidate.id != "GMT" || cityName.equals("GMT", ignoreCase = true)) {
                return candidate
            }
        } catch (_: Exception) {}

        return TimeZone.getDefault()
    }

    fun detectCityFromDeviceTimezone(): String {
        val defaultId = TimeZone.getDefault().id
        return when {
            defaultId.contains("New_York") || defaultId.contains("Toronto") || defaultId.contains("Montreal") || defaultId.contains("Indianapolis") -> "New York"
            defaultId.contains("Chicago") || defaultId.contains("Winnipeg") || defaultId.contains("Mexico_City") -> "Chicago"
            defaultId.contains("Denver") || defaultId.contains("Edmonton") || defaultId.contains("Salt_Lake") -> "Denver"
            defaultId.contains("Phoenix") -> "Phoenix"
            defaultId.contains("Los_Angeles") || defaultId.contains("Vancouver") || defaultId.contains("Tijuana") -> "Los Angeles"
            defaultId.contains("London") || defaultId.contains("Dublin") -> "London"
            defaultId.contains("Paris") || defaultId.contains("Berlin") || defaultId.contains("Rome") || defaultId.contains("Madrid") -> "Paris"
            defaultId.contains("Tokyo") -> "Tokyo"
            defaultId.contains("Seoul") -> "Seoul"
            defaultId.contains("Shanghai") || defaultId.contains("Hong_Kong") || defaultId.contains("Singapore") -> "Singapore"
            defaultId.contains("Sydney") || defaultId.contains("Melbourne") || defaultId.contains("Canberra") -> "Sydney"
            else -> "San Francisco"
        }
    }

    fun refreshApps(settings: LauncherSettings = _uiState.value.settings) {
        viewModelScope.launch {
            val (priority, remaining) = appManagerRepo.loadInstalledApps(settings)
            _uiState.value = _uiState.value.copy(
                priorityApps = priority,
                remainingApps = remaining
            )
        }
    }

    fun launchApp(app: AppItem) {
        val success = appManagerRepo.launchApp(app)
        if (success && _uiState.value.settings.confirmExitApp) {
            triggerExitConfirmation(app)
        }
    }

    private fun triggerExitConfirmation(app: AppItem) {
        exitConfirmJob?.cancel()
        _uiState.value = _uiState.value.copy(
            showExitConfirm = true,
            exitConfirmApp = app,
            exitConfirmSeconds = 5
        )
        exitConfirmJob = viewModelScope.launch {
            for (sec in 5 downTo 1) {
                _uiState.value = _uiState.value.copy(exitConfirmSeconds = sec)
                delay(1000)
            }
            _uiState.value = _uiState.value.copy(showExitConfirm = false, exitConfirmApp = null)
        }
    }

    fun confirmReturnToApp() {
        val app = _uiState.value.exitConfirmApp
        dismissExitConfirm()
        if (app != null) {
            appManagerRepo.launchApp(app)
        }
    }

    fun dismissExitConfirm() {
        exitConfirmJob?.cancel()
        _uiState.value = _uiState.value.copy(showExitConfirm = false, exitConfirmApp = null)
    }

    fun toggleAppsDrawer(expand: Boolean? = null) {
        val newExpanded = expand ?: !_uiState.value.isAppsDrawerExpanded
        _uiState.value = _uiState.value.copy(
            isAppsDrawerExpanded = newExpanded,
            isSettingsOpen = if (newExpanded) false else _uiState.value.isSettingsOpen
        )
    }

    fun toggleSettings(open: Boolean? = null) {
        val newOpen = open ?: !_uiState.value.isSettingsOpen
        _uiState.value = _uiState.value.copy(
            isSettingsOpen = newOpen,
            isAppsDrawerExpanded = if (newOpen) false else _uiState.value.isAppsDrawerExpanded
        )
    }

    fun openAppMenu(app: AppItem) {
        _uiState.value = _uiState.value.copy(selectedAppForMenu = app)
    }

    fun closeAppMenu() {
        _uiState.value = _uiState.value.copy(selectedAppForMenu = null)
    }

    fun moveAppToPriority(app: AppItem, isPriority: Boolean) {
        prefsRepo.setAppPriority(app.packageName, isPriority)
        closeAppMenu()
        refreshApps()
    }

    fun movePriorityApp(app: AppItem, moveLeft: Boolean) {
        prefsRepo.movePriorityApp(app.packageName, moveLeft)
        refreshApps()
    }

    fun toggleAppFavorite(app: AppItem) {
        prefsRepo.toggleAppFavorite(app.packageName)
        refreshApps()
    }

    fun toggleAppHidden(app: AppItem) {
        prefsRepo.toggleAppHidden(app.packageName)
        closeAppMenu()
        refreshApps()
    }

    fun openAppInfo(app: AppItem) {
        closeAppMenu()
        appManagerRepo.openAppInfo(app.packageName)
    }

    fun openSystemSettings() {
        appManagerRepo.openSystemSettings()
    }

    fun updateSettings(newSettings: LauncherSettings) {
        prefsRepo.updateSettings(newSettings)
    }

    fun setWallpaperMode(mode: WallpaperMode, customUri: String? = null) {
        prefsRepo.setWallpaperMode(mode, customUri)
    }

    fun setCity(city: String) {
        val finalCity = if (city.equals("Auto (Local)", ignoreCase = true)) {
            detectCityFromDeviceTimezone()
        } else {
            city
        }
        weatherRepo.setCity(finalCity)
        val tz = resolveTimezoneForCity(finalCity)
        val newWeather = weatherRepo.currentWeather.value
        val updatedSettings = _uiState.value.settings.copy(
            selectedCity = finalCity,
            selectedTimezone = tz.id
        )
        prefsRepo.updateSettings(updatedSettings)
        _uiState.value = _uiState.value.copy(
            settings = updatedSettings,
            weather = newWeather
        )
        updateTime()
    }

    fun getAvailableCities(): List<String> = weatherRepo.getAvailableCities()

    fun toggleScenicGlass() = prefsRepo.toggleScenicGlass()
    fun toggleAnimations() = prefsRepo.toggleAnimations()
    fun toggleNightMode() = prefsRepo.toggleNightMode()
    fun setAccentColor(hex: Long) = prefsRepo.setAccentColor(hex)
    fun setPriorityDockCount(count: Int) = prefsRepo.setPriorityDockCount(count)
}
