package com.pulsecharge.app

import android.app.Application
import android.content.Context
import com.pulsecharge.app.alerts.BatteryAlertWorker
import com.pulsecharge.app.alerts.NotificationHelper
import com.pulsecharge.app.data.TelemetryRepository
import com.pulsecharge.app.data.preferences.SettingsRepository

import android.util.Log
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppContainer(val context: Context) {
    val settingsRepository by lazy { SettingsRepository(context) }
    val telemetryRepository by lazy { TelemetryRepository(context) }
    val notificationHelper by lazy { NotificationHelper(context) }
}

class PulseChargeApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Initialize Google Mobile Ads SDK asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@PulseChargeApplication) { status ->
                Log.d("PulseChargeAds", "MobileAds initialization completed: ${status.adapterStatusMap}")
            }
        }

        // Schedule periodic alert background worker
        BatteryAlertWorker.schedulePeriodicCheck(this)
    }
}
