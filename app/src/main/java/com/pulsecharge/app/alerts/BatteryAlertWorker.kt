package com.pulsecharge.app.alerts

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.work.*
import com.pulsecharge.app.data.preferences.SettingsRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class BatteryAlertWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val settingsRepo = SettingsRepository(appContext)
        val settings = settingsRepo.settingsFlow.first()
        val notificationHelper = NotificationHelper(appContext)

        val intent = appContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        if (intent != null) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val levelPercent = if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt() else 0

            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

            val tempRaw = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            val tempC = tempRaw / 10f

            // Full Charge Alert
            if (settings.fullChargeAlertEnabled && isCharging && levelPercent >= settings.fullChargeThresholdPercent) {
                notificationHelper.showFullChargeNotification(levelPercent)
            }

            // Low Battery Alert
            if (settings.lowBatteryAlertEnabled && !isCharging && levelPercent <= settings.lowBatteryThresholdPercent) {
                notificationHelper.showLowBatteryNotification(levelPercent)
            }

            // Overheat Alert
            if (settings.overheatAlertEnabled && tempC >= settings.overheatThresholdTempC) {
                notificationHelper.showOverheatNotification(tempC)
            }
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "pulse_charge_alert_check"

        fun schedulePeriodicCheck(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<BatteryAlertWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
