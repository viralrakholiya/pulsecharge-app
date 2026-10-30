package com.pulsecharge.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.pulsecharge.app.data.model.AccentTheme
import com.pulsecharge.app.data.model.AppearanceMode
import com.pulsecharge.app.data.model.AppSettings
import com.pulsecharge.app.data.model.TempUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pulse_charge_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val ACCENT_THEME = stringPreferencesKey("accent_theme")
        val APPEARANCE_MODE = stringPreferencesKey("appearance_mode")
        val TEMP_UNIT = stringPreferencesKey("temp_unit")
        val FULL_CHARGE_ALERT_ENABLED = booleanPreferencesKey("full_charge_alert_enabled")
        val FULL_CHARGE_THRESHOLD = intPreferencesKey("full_charge_threshold")
        val LOW_BATTERY_ALERT_ENABLED = booleanPreferencesKey("low_battery_alert_enabled")
        val LOW_BATTERY_THRESHOLD = intPreferencesKey("low_battery_threshold")
        val OVERHEAT_ALERT_ENABLED = booleanPreferencesKey("overheat_alert_enabled")
        val OVERHEAT_THRESHOLD = intPreferencesKey("overheat_threshold")
        val DAILY_DIGEST_ENABLED = booleanPreferencesKey("daily_digest_enabled")
        val APP_USAGE_ACCESS_ENABLED = booleanPreferencesKey("app_usage_access_enabled")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            accentTheme = prefs[Keys.ACCENT_THEME]?.let { runCatching { AccentTheme.valueOf(it) }.getOrNull() } ?: AccentTheme.PURPLE,
            appearanceMode = prefs[Keys.APPEARANCE_MODE]?.let { runCatching { AppearanceMode.valueOf(it) }.getOrNull() } ?: AppearanceMode.SYSTEM,
            tempUnit = prefs[Keys.TEMP_UNIT]?.let { runCatching { TempUnit.valueOf(it) }.getOrNull() } ?: TempUnit.CELSIUS,
            fullChargeAlertEnabled = prefs[Keys.FULL_CHARGE_ALERT_ENABLED] ?: true,
            fullChargeThresholdPercent = prefs[Keys.FULL_CHARGE_THRESHOLD] ?: 80,
            lowBatteryAlertEnabled = prefs[Keys.LOW_BATTERY_ALERT_ENABLED] ?: true,
            lowBatteryThresholdPercent = prefs[Keys.LOW_BATTERY_THRESHOLD] ?: 20,
            overheatAlertEnabled = prefs[Keys.OVERHEAT_ALERT_ENABLED] ?: true,
            overheatThresholdTempC = prefs[Keys.OVERHEAT_THRESHOLD] ?: 40,
            dailyDigestEnabled = prefs[Keys.DAILY_DIGEST_ENABLED] ?: false,
            appUsageAccessEnabled = prefs[Keys.APP_USAGE_ACCESS_ENABLED] ?: false
        )
    }

    suspend fun updateAccentTheme(theme: AccentTheme) {
        context.dataStore.edit { prefs -> prefs[Keys.ACCENT_THEME] = theme.name }
    }

    suspend fun updateAppearanceMode(mode: AppearanceMode) {
        context.dataStore.edit { prefs -> prefs[Keys.APPEARANCE_MODE] = mode.name }
    }

    suspend fun updateTempUnit(unit: TempUnit) {
        context.dataStore.edit { prefs -> prefs[Keys.TEMP_UNIT] = unit.name }
    }

    suspend fun updateFullChargeAlert(enabled: Boolean, threshold: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FULL_CHARGE_ALERT_ENABLED] = enabled
            prefs[Keys.FULL_CHARGE_THRESHOLD] = threshold
        }
    }

    suspend fun updateLowBatteryAlert(enabled: Boolean, threshold: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LOW_BATTERY_ALERT_ENABLED] = enabled
            prefs[Keys.LOW_BATTERY_THRESHOLD] = threshold
        }
    }

    suspend fun updateOverheatAlert(enabled: Boolean, thresholdTempC: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.OVERHEAT_ALERT_ENABLED] = enabled
            prefs[Keys.OVERHEAT_THRESHOLD] = thresholdTempC
        }
    }

    suspend fun updateDailyDigest(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.DAILY_DIGEST_ENABLED] = enabled }
    }

    suspend fun updateAppUsageAccess(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.APP_USAGE_ACCESS_ENABLED] = enabled }
    }
}
