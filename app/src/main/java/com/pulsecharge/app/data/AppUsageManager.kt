package com.pulsecharge.app.data

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings

data class AppUsageInfo(
    val appName: String,
    val packageName: String,
    val usageTimeMinutes: Int,
    val estimatedDrainPercent: Float
)

class AppUsageManager(private val context: Context) {

    fun hasUsagePermission(): Boolean {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return false
        val now = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            now - (24 * 3600 * 1000),
            now
        )
        return !stats.isNullOrEmpty()
    }

    fun openUsageAccessSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }

    fun getTopAppUsage(limit: Int = 5): List<AppUsageInfo> {
        if (!hasUsagePermission()) return emptyList()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return emptyList()
        val pm = context.packageManager
        val now = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            now - (24 * 3600 * 1000),
            now
        ) ?: return emptyList()

        val totalTime = stats.sumOf { it.totalTimeInForeground }.coerceAtLeast(1L)

        return stats.filter { it.totalTimeInForeground > 60_000 }
            .sortedByDescending { it.totalTimeInForeground }
            .take(limit)
            .map { usage ->
                val appName = runCatching {
                    val appInfo = pm.getApplicationInfo(usage.packageName, 0)
                    pm.getApplicationLabel(appInfo).toString()
                }.getOrNull() ?: usage.packageName.substringAfterLast('.')

                val mins = (usage.totalTimeInForeground / 60000).toInt()
                val drainPercent = ((usage.totalTimeInForeground.toFloat() / totalTime) * 15f).coerceIn(0.5f, 25f)

                AppUsageInfo(
                    appName = appName,
                    packageName = usage.packageName,
                    usageTimeMinutes = mins,
                    estimatedDrainPercent = drainPercent
                )
            }
    }
}
