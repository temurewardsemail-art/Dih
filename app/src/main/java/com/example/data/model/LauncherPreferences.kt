package com.example.data.model

enum class WallpaperMode {
    PRESET_SCENIC,
    PRESET_ANIME,
    PRESET_COSMOS,
    PRESET_ALPINE,
    CUSTOM_FILE,
    VIDEO_LOOP
}

enum class ClockDisplayMode {
    HEADER_GLASS,
    DYNAMIC_LOCKSCREEN
}

enum class BannerStyle {
    ROUNDED_18DP,
    PILL_ROUNDED,
    SUBTLE_EDGES,
    EXPANDED_HERO
}

data class LauncherSettings(
    val priorityDockCount: Int = 7,
    val scenicGlassEnabled: Boolean = true,
    val animationsEnabled: Boolean = true,
    val nightMode: Boolean = false,
    val wallpaperMode: WallpaperMode = WallpaperMode.PRESET_SCENIC,
    val customWallpaperUri: String? = null,
    val videoWallpaperUrl: String = "https://assets.mixkit.co/videos/preview/mixkit-serene-fog-in-a-forest-42866-large.mp4",
    val accentColorHex: Long = 0xFF38BDF8, // Scenic Cyan
    val clockMode: ClockDisplayMode = ClockDisplayMode.HEADER_GLASS,
    val clockOpacity: Float = 0.95f,
    val selectedCity: String = "Auto (Local)",
    val selectedTimezone: String = "Auto",
    val tempUnitCelsius: Boolean = false,
    val bannerStyle: BannerStyle = BannerStyle.ROUNDED_18DP,
    val glassTransparency: Float = 0.35f,
    val confirmExitApp: Boolean = true,
    val showLiquidWidgets: Boolean = true,
    val priorityPackageNames: List<String> = emptyList(),
    val hiddenPackageNames: Set<String> = emptySet(),
    val favoritePackageNames: Set<String> = emptySet()
)
