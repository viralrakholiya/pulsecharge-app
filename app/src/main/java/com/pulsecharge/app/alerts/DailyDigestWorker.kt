package com.pulsecharge.app.alerts

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.work.*
import com.pulsecharge.app.data.preferences.SettingsRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class DailyDigestWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val settingsRepo = SettingsRepository(appContext)
        val settings = settingsRepo.settingsFlow.first()

        if (!settings.dailyDigestEnabled) {
            return Result.success()
        }

        val intent = appContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val tempRaw = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 360
        val tempC = tempRaw / 10f

        val notificationHelper = NotificationHelper(appContext)
        notificationHelper.showDailyDigestNotification(tempC)

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "pulse_charge_daily_digest"

        fun scheduleDailyDigest(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<DailyDigestWorker>(24, TimeUnit.HOURS)
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
