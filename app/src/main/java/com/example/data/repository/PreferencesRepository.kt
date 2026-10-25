package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.BannerStyle
import com.example.data.model.ClockDisplayMode
import com.example.data.model.LauncherSettings
import com.example.data.model.WallpaperMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("personal_tv_home_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<LauncherSettings> = _settings.asStateFlow()

    private fun loadSettings(): LauncherSettings {
        val priorityCount = prefs.getInt("priority_count", 7)
        val scenicGlass = prefs.getBoolean("scenic_glass", true)
        val animations = prefs.getBoolean("animations", true)
        val nightMode = prefs.getBoolean("night_mode", false)
        val wallpaperModeStr = prefs.getString("wallpaper_mode", WallpaperMode.PRESET_SCENIC.name)
        val wallpaperMode = try {
            WallpaperMode.valueOf(wallpaperModeStr ?: WallpaperMode.PRESET_SCENIC.name)
        } catch (_: Exception) {
            WallpaperMode.PRESET_SCENIC
        }
        val customUri = prefs.getString("custom_wallpaper_uri", null)
        val videoUrl = prefs.getString("video_wallpaper_url", "https://assets.mixkit.co/videos/preview/mixkit-serene-fog-in-a-forest-42866-large.mp4") ?: ""
        val accentHex = prefs.getLong("accent_hex", 0xFF38BDF8)
        val clockModeStr = prefs.getString("clock_mode", ClockDisplayMode.HEADER_GLASS.name)
        val clockMode = try {
            ClockDisplayMode.valueOf(clockModeStr ?: ClockDisplayMode.HEADER_GLASS.name)
        } catch (_: Exception) {
            ClockDisplayMode.HEADER_GLASS
        }
        val clockOpacity = prefs.getFloat("clock_opacity", 0.95f)
        val city = prefs.getString("selected_city", "Auto (Local)") ?: "Auto (Local)"
        val tz = prefs.getString("selected_timezone", "Auto") ?: "Auto"
        val tempCelsius = prefs.getBoolean("temp_celsius", false)
        val bannerStyleStr = prefs.getString("banner_style", BannerStyle.ROUNDED_18DP.name)
        val bannerStyle = try {
            BannerStyle.valueOf(bannerStyleStr ?: BannerStyle.ROUNDED_18DP.name)
        } catch (_: Exception) {
            BannerStyle.ROUNDED_18DP
        }
        val glassTransparency = prefs.getFloat("glass_transparency", 0.35f)
        val confirmExit = prefs.getBoolean("confirm_exit", true)
        val showWidgets = prefs.getBoolean("show_widgets", true)

        val priorityListStr = prefs.getString("priority_pkgs", "") ?: ""
        val priorityPkgs = if (priorityListStr.isNotEmpty()) priorityListStr.split(",").filter { it.isNotBlank() } else emptyList()
        val hiddenPkgs = prefs.getStringSet("hidden_pkgs", emptySet())?.toSet() ?: emptySet()
        val favoritePkgs = prefs.getStringSet("favorite_pkgs", emptySet())?.toSet() ?: emptySet()

        return LauncherSettings(
            priorityDockCount = priorityCount,
            scenicGlassEnabled = scenicGlass,
            animationsEnabled = animations,
            nightMode = nightMode,
            wallpaperMode = wallpaperMode,
            customWallpaperUri = customUri,
            videoWallpaperUrl = videoUrl,
            accentColorHex = accentHex,
            clockMode = clockMode,
            clockOpacity = clockOpacity,
            selectedCity = city,
            selectedTimezone = tz,
            tempUnitCelsius = tempCelsius,
            bannerStyle = bannerStyle,
            glassTransparency = glassTransparency,
            confirmExitApp = confirmExit,
            showLiquidWidgets = showWidgets,
            priorityPackageNames = priorityPkgs,
            hiddenPackageNames = hiddenPkgs,
            favoritePackageNames = favoritePkgs
        )
    }

    fun updateSettings(newSettings: LauncherSettings) {
        _settings.value = newSettings
        prefs.edit()
            .putInt("priority_count", newSettings.priorityDockCount)
            .putBoolean("scenic_glass", newSettings.scenicGlassEnabled)
            .putBoolean("animations", newSettings.animationsEnabled)
            .putBoolean("night_mode", newSettings.nightMode)
            .putString("wallpaper_mode", newSettings.wallpaperMode.name)
            .putString("custom_wallpaper_uri", newSettings.customWallpaperUri)
            .putString("video_wallpaper_url", newSettings.videoWallpaperUrl)
            .putLong("accent_hex", newSettings.accentColorHex)
            .putString("clock_mode", newSettings.clockMode.name)
            .putFloat("clock_opacity", newSettings.clockOpacity)
            .putString("selected_city", newSettings.selectedCity)
            .putString("selected_timezone", newSettings.selectedTimezone)
            .putBoolean("temp_celsius", newSettings.tempUnitCelsius)
            .putString("banner_style", newSettings.bannerStyle.name)
            .putFloat("glass_transparency", newSettings.glassTransparency)
            .putBoolean("confirm_exit", newSettings.confirmExitApp)
            .putBoolean("show_widgets", newSettings.showLiquidWidgets)
            .putString("priority_pkgs", newSettings.priorityPackageNames.joinToString(","))
            .putStringSet("hidden_pkgs", newSettings.hiddenPackageNames)
            .putStringSet("favorite_pkgs", newSettings.favoritePackageNames)
            .apply()
    }

    fun toggleScenicGlass() {
        val current = _settings.value
        updateSettings(current.copy(scenicGlassEnabled = !current.scenicGlassEnabled))
    }

    fun toggleAnimations() {
        val current = _settings.value
        updateSettings(current.copy(animationsEnabled = !current.animationsEnabled))
    }

    fun toggleNightMode() {
        val current = _settings.value
        updateSettings(current.copy(nightMode = !current.nightMode))
    }

    fun setWallpaperMode(mode: WallpaperMode, customUri: String? = null) {
        val current = _settings.value
        updateSettings(current.copy(wallpaperMode = mode, customWallpaperUri = customUri ?: current.customWallpaperUri))
    }

    fun setAccentColor(colorHex: Long) {
        val current = _settings.value
        updateSettings(current.copy(accentColorHex = colorHex))
    }

    fun setPriorityDockCount(count: Int) {
        val current = _settings.value
        updateSettings(current.copy(priorityDockCount = count))
    }

    fun toggleAppHidden(packageName: String) {
        val current = _settings.value
        val newHidden = current.hiddenPackageNames.toMutableSet()
        if (newHidden.contains(packageName)) {
            newHidden.remove(packageName)
        } else {
            newHidden.add(packageName)
        }
        updateSettings(current.copy(hiddenPackageNames = newHidden))
    }

    fun toggleAppFavorite(packageName: String) {
        val current = _settings.value
        val newFavorites = current.favoritePackageNames.toMutableSet()
        if (newFavorites.contains(packageName)) {
            newFavorites.remove(packageName)
        } else {
            newFavorites.add(packageName)
        }
        updateSettings(current.copy(favoritePackageNames = newFavorites))
    }

    fun setAppPriority(packageName: String, isPriority: Boolean) {
        val current = _settings.value
        val newPriorityList = current.priorityPackageNames.toMutableList()
        if (isPriority) {
            if (!newPriorityList.contains(packageName)) {
                newPriorityList.add(0, packageName)
            }
        } else {
            newPriorityList.remove(packageName)
        }
        updateSettings(current.copy(priorityPackageNames = newPriorityList))
    }

    fun movePriorityApp(packageName: String, moveLeft: Boolean) {
        val current = _settings.value
        val list = current.priorityPackageNames.toMutableList()
        val index = list.indexOf(packageName)
        if (index == -1) return
        val newIndex = if (moveLeft) (index - 1).coerceAtLeast(0) else (index + 1).coerceAtMost(list.size - 1)
        if (newIndex != index) {
            list.removeAt(index)
            list.add(newIndex, packageName)
            updateSettings(current.copy(priorityPackageNames = list))
        }
    }
}
