package com.pulsecharge.app.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.pulsecharge.app.data.model.AppSettings
import com.pulsecharge.app.data.model.BatteryTelemetry
import com.pulsecharge.app.data.model.HistoryPoint
import com.pulsecharge.app.ui.screens.DashboardScreen
import com.pulsecharge.app.ui.screens.HealthScreen
import com.pulsecharge.app.ui.screens.SettingsScreen
import com.pulsecharge.app.ui.screens.TipsScreen
import com.pulsecharge.app.ui.theme.LocalCustomColors

enum class AppTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Home),
    HEALTH("Health", Icons.Default.Favorite),
    TIPS("Tips", Icons.Default.Star),
    SETTINGS("Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PulseChargeApp(
    telemetry: BatteryTelemetry,
    historyPoints: List<HistoryPoint>,
    settings: AppSettings,
    onRefreshTelemetry: () -> Unit,
    onUpdateAccentTheme: (com.pulsecharge.app.data.model.AccentTheme) -> Unit,
    onUpdateAppearanceMode: (com.pulsecharge.app.data.model.AppearanceMode) -> Unit,
    onUpdateTempUnit: (com.pulsecharge.app.data.model.TempUnit) -> Unit,
    onUpdateFullChargeAlert: (Boolean, Int) -> Unit,
    onUpdateLowBatteryAlert: (Boolean, Int) -> Unit,
    onUpdateOverheatAlert: (Boolean, Int) -> Unit,
    onUpdateDailyDigest: (Boolean) -> Unit,
    onUpdateAppUsageAccess: (Boolean) -> Unit,
    onOpenUsageSettings: () -> Unit,
    onTestNotificationClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    val themeColors = LocalCustomColors.current

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = themeColors.surface,
                tonalElevation = 8.dp
            ) {
                AppTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(text = tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = themeColors.primary,
                            selectedTextColor = themeColors.primary,
                            indicatorColor = themeColors.primary.copy(alpha = 0.18f),
                            unselectedIconColor = themeColors.textSecondary,
                            unselectedTextColor = themeColors.textSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            modifier = Modifier.padding(innerPadding),
            label = "tabTransition"
        ) { tab ->
            when (tab) {
                AppTab.DASHBOARD -> DashboardScreen(
                    telemetry = telemetry,
                    tempUnit = settings.tempUnit,
                    onRefresh = onRefreshTelemetry
                )
                AppTab.HEALTH -> HealthScreen(
                    telemetry = telemetry,
                    historyPoints = historyPoints,
                    appUsageAccessEnabled = settings.appUsageAccessEnabled,
                    onOpenUsageSettings = onOpenUsageSettings
                )
                AppTab.TIPS -> TipsScreen(
                    telemetry = telemetry
                )
                AppTab.SETTINGS -> SettingsScreen(
                    settings = settings,
                    onUpdateAccentTheme = onUpdateAccentTheme,
                    onUpdateAppearanceMode = onUpdateAppearanceMode,
                    onUpdateTempUnit = onUpdateTempUnit,
                    onUpdateFullChargeAlert = onUpdateFullChargeAlert,
                    onUpdateLowBatteryAlert = onUpdateLowBatteryAlert,
                    onUpdateOverheatAlert = onUpdateOverheatAlert,
                    onUpdateDailyDigest = onUpdateDailyDigest,
                    onUpdateAppUsageAccess = onUpdateAppUsageAccess,
                    onOpenUsageSettings = onOpenUsageSettings,
                    onTestNotificationClick = onTestNotificationClick
                )
            }
        }
    }
}
