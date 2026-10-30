package com.pulsecharge.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
import com.pulsecharge.app.data.AppUsageManager
import com.pulsecharge.app.ui.PulseChargeApp
import com.pulsecharge.app.ui.theme.PulseChargeTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notifications disabled in settings", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as PulseChargeApplication
        val container = app.container
        val appUsageManager = AppUsageManager(this)

        checkAndRequestNotificationPermission()

        setContent {
            val telemetry by container.telemetryRepository.telemetryState.collectAsState()
            val historyPoints by container.telemetryRepository.historyState.collectAsState()
            val settings by container.settingsRepository.settingsFlow.collectAsState(
                initial = com.pulsecharge.app.data.model.AppSettings()
            )

            val scope = rememberCoroutineScope()

            PulseChargeTheme(
                accentTheme = settings.accentTheme,
                appearanceMode = settings.appearanceMode
            ) {
                PulseChargeApp(
                    telemetry = telemetry,
                    historyPoints = historyPoints,
                    settings = settings,
                    onRefreshTelemetry = {
                        container.telemetryRepository.forceRefresh()
                    },
                    onUpdateAccentTheme = { newTheme ->
                        scope.launch { container.settingsRepository.updateAccentTheme(newTheme) }
                    },
                    onUpdateAppearanceMode = { newMode ->
                        scope.launch { container.settingsRepository.updateAppearanceMode(newMode) }
                    },
                    onUpdateTempUnit = { newUnit ->
                        scope.launch { container.settingsRepository.updateTempUnit(newUnit) }
                    },
                    onUpdateFullChargeAlert = { enabled, threshold ->
                        scope.launch { container.settingsRepository.updateFullChargeAlert(enabled, threshold) }
                    },
                    onUpdateLowBatteryAlert = { enabled, threshold ->
                        scope.launch { container.settingsRepository.updateLowBatteryAlert(enabled, threshold) }
                    },
                    onUpdateOverheatAlert = { enabled, threshold ->
                        scope.launch { container.settingsRepository.updateOverheatAlert(enabled, threshold) }
                    },
                    onUpdateDailyDigest = { enabled ->
                        scope.launch { container.settingsRepository.updateDailyDigest(enabled) }
                    },
                    onUpdateAppUsageAccess = { enabled ->
                        scope.launch { container.settingsRepository.updateAppUsageAccess(enabled) }
                    },
                    onOpenUsageSettings = {
                        appUsageManager.openUsageAccessSettings()
                    },
                    onTestNotificationClick = {
                        checkAndRequestNotificationPermission()
                        container.notificationHelper.showTestNotification()
                    }
                )
            }
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
