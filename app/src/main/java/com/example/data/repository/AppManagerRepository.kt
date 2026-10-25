package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.data.model.AppItem
import com.example.data.model.LauncherSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppManagerRepository(private val context: Context) {
    private val pm: PackageManager = context.packageManager

    suspend fun loadInstalledApps(settings: LauncherSettings): Pair<List<AppItem>, List<AppItem>> =
        withContext(Dispatchers.IO) {
            val ownPackage = context.packageName
            val discoveredMap = mutableMapOf<String, AppItem>()

            // 1. Query Leanback TV Launcher activities first
            val leanbackIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
            }
            val leanbackApps: List<ResolveInfo> = try {
                pm.queryIntentActivities(leanbackIntent, 0)
            } catch (e: Exception) {
                emptyList()
            }

            for (info in leanbackApps) {
                val pkg = info.activityInfo.packageName
                if (pkg == ownPackage) continue
                val label = try {
                    info.loadLabel(pm).toString()
                } catch (_: Exception) {
                    info.activityInfo.name
                }
                val banner = try {
                    info.activityInfo.loadBanner(pm) ?: pm.getApplicationBanner(pkg)
                } catch (_: Exception) {
                    null
                }
                val icon = try {
                    info.activityInfo.loadIcon(pm) ?: info.loadIcon(pm)
                } catch (_: Exception) {
                    null
                }
                discoveredMap[pkg] = AppItem(
                    packageName = pkg,
                    activityName = info.activityInfo.name,
                    label = label,
                    iconDrawable = icon,
                    bannerDrawable = banner,
                    isSystemApp = (info.activityInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }

            // 2. Query standard Android launcher activities (for any non-leanback apps or tools)
            val standardIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val standardApps: List<ResolveInfo> = try {
                pm.queryIntentActivities(standardIntent, 0)
            } catch (e: Exception) {
                emptyList()
            }

            for (info in standardApps) {
                val pkg = info.activityInfo.packageName
                if (pkg == ownPackage || discoveredMap.containsKey(pkg)) continue
                val label = try {
                    info.loadLabel(pm).toString()
                } catch (_: Exception) {
                    info.activityInfo.name
                }
                val banner = try {
                    info.activityInfo.loadBanner(pm) ?: pm.getApplicationBanner(pkg)
                } catch (_: Exception) {
                    null
                }
                val icon = try {
                    info.activityInfo.loadIcon(pm) ?: info.loadIcon(pm)
                } catch (_: Exception) {
                    null
                }
                discoveredMap[pkg] = AppItem(
                    packageName = pkg,
                    activityName = info.activityInfo.name,
                    label = label,
                    iconDrawable = icon,
                    bannerDrawable = banner,
                    isSystemApp = (info.activityInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }

            // Real discovered items
            val allInstalledList = discoveredMap.values.toMutableList()

            // If the environment is a minimal sandbox or emulator with few apps, add essential TV system services/apps
            ensureEssentialTvApps(allInstalledList)

            // Filter out hidden packages
            val visibleApps = allInstalledList.filter { !settings.hiddenPackageNames.contains(it.packageName) }

            // Priority mapping
            val priorityPkgs = settings.priorityPackageNames
            val priorityApps = mutableListOf<AppItem>()
            val remainingApps = mutableListOf<AppItem>()

            if (priorityPkgs.isNotEmpty()) {
                // User has customized priority list
                val visibleMap = visibleApps.associateBy { it.packageName }.toMutableMap()
                for (pkg in priorityPkgs) {
                    val app = visibleMap.remove(pkg)
                    if (app != null) {
                        priorityApps.add(app.copy(isPriority = true, isFavorite = settings.favoritePackageNames.contains(pkg)))
                    }
                }
                // If there is room and priorityDockCount > priorityApps.size, add from visibleMap
                for (app in visibleMap.values) {
                    if (priorityApps.size < settings.priorityDockCount) {
                        priorityApps.add(app.copy(isPriority = true, isFavorite = settings.favoritePackageNames.contains(app.packageName)))
                    } else {
                        remainingApps.add(app.copy(isPriority = false, isFavorite = settings.favoritePackageNames.contains(app.packageName)))
                    }
                }
            } else {
                // Default: take first `priorityDockCount` apps for priority bar
                val limit = settings.priorityDockCount.coerceAtLeast(3)
                visibleApps.forEachIndexed { index, app ->
                    val isFav = settings.favoritePackageNames.contains(app.packageName)
                    if (index < limit) {
                        priorityApps.add(app.copy(isPriority = true, isFavorite = isFav))
                    } else {
                        remainingApps.add(app.copy(isPriority = false, isFavorite = isFav))
                    }
                }
            }

            Pair(priorityApps, remainingApps)
        }

    private fun ensureEssentialTvApps(list: MutableList<AppItem>) {
        val existingPkgs = list.map { it.packageName }.toSet()

        // Real TV apps that can either be launched if installed or directly accessed
        val essentials = listOf(
            Triple("com.google.android.youtube.tv", "YouTube TV", 0xFFE50914),
            Triple("com.netflix.ninja", "Netflix", 0xFFE50914),
            Triple("com.amazon.amazonvideo.livingroom", "Prime Video", 0xFF00A8E1),
            Triple("com.plexapp.android", "Plex", 0xFFE5A00D),
            Triple("tv.twitch.android.app", "Twitch", 0xFF9146FF),
            Triple("com.spotify.tv.android", "Spotify", 0xFF1ED760),
            Triple("com.android.tv.settings", "TV Settings", 0xFF374151),
            Triple("com.google.android.videos", "Google TV", 0xFF4285F4),
            Triple("com.android.documentsui", "File Manager", 0xFF0284C7)
        )

        for ((pkg, label, color) in essentials) {
            if (!existingPkgs.contains(pkg)) {
                // Check if launch intent exists under common aliases
                val intent = pm.getLaunchIntentForPackage(pkg)
                list.add(
                    AppItem(
                        packageName = pkg,
                        label = label,
                        iconDrawable = if (intent != null) try { pm.getApplicationIcon(pkg) } catch (_: Exception) { null } else null,
                        bannerDrawable = if (intent != null) try { pm.getApplicationBanner(pkg) } catch (_: Exception) { null } else null,
                        accentColor = color,
                        fallbackTag = label
                    )
                )
            }
        }
    }

    fun launchApp(app: AppItem): Boolean {
        return try {
            val leanbackIntent = pm.getLeanbackLaunchIntentForPackage(app.packageName)
            val launchIntent = leanbackIntent ?: pm.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                // If not installed on device, open Play Store or web
                val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${app.packageName}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(playStoreIntent)
                    true
                } catch (_: Exception) {
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${app.packageName}")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(webIntent)
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("AppManagerRepository", "Failed to launch ${app.packageName}", e)
            Toast.makeText(context, "Cannot launch ${app.label}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun openAppInfo(packageName: String) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("AppManagerRepository", "Failed to open app info", e)
        }
    }

    fun openSystemSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("AppManagerRepository", "Failed to open settings", e)
        }
    }
}
