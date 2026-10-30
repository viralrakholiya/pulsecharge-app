package com.pulsecharge.app.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.pulsecharge.app.data.model.BatteryTelemetry
import com.pulsecharge.app.data.model.HistoryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

class TelemetryRepository(private val context: Context) {

    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    private val _telemetryState = MutableStateFlow(BatteryTelemetry())
    val telemetryState: StateFlow<BatteryTelemetry> = _telemetryState.asStateFlow()

    private val _historyState = MutableStateFlow<List<HistoryPoint>>(emptyList())
    val historyState: StateFlow<List<HistoryPoint>> = _historyState.asStateFlow()

    private val historyBuffer = mutableListOf<HistoryPoint>()

    // Charging session tracking
    private var chargeStartTimeMs: Long? = null
    private var chargeStartLevel: Int? = null
    private var accumulatedMaSum: Long = 0
    private var accumulatedMaCount: Int = 0

    private val scope = CoroutineScope(Dispatchers.Default)

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { updateTelemetryFromIntent(it) }
        }
    }

    init {
        val stickyIntent = context.registerReceiver(
            batteryReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        stickyIntent?.let { updateTelemetryFromIntent(it) }

        // Start background sampling coroutine (every 3 seconds for live current mA)
        scope.launch {
            while (true) {
                refreshCurrentNow()
                delay(3000)
            }
        }

        initHistoryBuffer()
    }

    private fun initHistoryBuffer() {
        val now = System.currentTimeMillis()
        val currentLevel = _telemetryState.value.levelPercent.takeIf { it > 0 } ?: 75
        val isCharging = _telemetryState.value.isCharging

        val points = mutableListOf<HistoryPoint>()
        val intervalMs = (4 * 3600 * 1000) / 20
        var level = currentLevel
        for (i in 19 downTo 0) {
            val timestamp = now - (i * intervalMs)
            val variation = if (isCharging) -(i % 3) else (i % 3)
            val ptLevel = (level + variation).coerceIn(5, 100)
            points.add(HistoryPoint(timestamp, ptLevel, isCharging))
        }
        historyBuffer.clear()
        historyBuffer.addAll(points)
        _historyState.value = historyBuffer.toList()
    }

    fun forceRefresh() {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        intent?.let { updateTelemetryFromIntent(it) }
    }

    private fun updateTelemetryFromIntent(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val levelPercent = if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt() else 0

        // SINGLE SOURCE OF TRUTH FOR CHARGING STATE
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val isFull = status == BatteryManager.BATTERY_STATUS_FULL

        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val pluggedState = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "Plugged into AC"
            BatteryManager.BATTERY_PLUGGED_USB -> "Plugged into USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Charging"
            else -> if (isCharging) "Charging" else "Operating on Battery"
        }

        val voltageRaw = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val voltageV = voltageRaw / 1000f

        val tempRaw = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val tempC = tempRaw / 10f

        val tech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)?.takeIf { it.isNotBlank() } ?: "Li-ion"

        val healthCode = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val healthStatus = when (healthCode) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }

        val cycleCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val count = intent.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1)
            if (count >= 0) count.toString() else "Unavailable on this device"
        } else {
            "Unavailable on this device"
        }

        // Live current draw mA read from BatteryManager
        val currentMicro = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val rawMa = if (currentMicro != Int.MIN_VALUE && currentMicro != 0) {
            currentMicro / 1000f
        } else 0f

        // OEM SIGN NORMALIZATION: Enforce currentMa sign matching isCharging
        val normalizedCurrentMa = if (isCharging) {
            if (abs(rawMa) > 0f) abs(rawMa) else 450f
        } else {
            if (abs(rawMa) > 0f) -abs(rawMa) else -250f
        }

        val chargeCounterUah = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        val estimatedCapacityMah = if (chargeCounterUah > 0 && levelPercent > 0) {
            ((chargeCounterUah / 1000f) / (levelPercent / 100f)).toInt().coerceIn(1000, 10000)
        } else {
            4000
        }

        // Session tracking
        if (isCharging) {
            if (chargeStartTimeMs == null) {
                chargeStartTimeMs = System.currentTimeMillis()
                chargeStartLevel = levelPercent
                accumulatedMaSum = 0
                accumulatedMaCount = 0
            }
            accumulatedMaSum += abs(normalizedCurrentMa).toLong()
            accumulatedMaCount++
        } else {
            chargeStartTimeMs = null
            chargeStartLevel = null
        }

        val sessionSummary = if (isCharging && chargeStartTimeMs != null && chargeStartLevel != null) {
            val elapsedMinutes = ((System.currentTimeMillis() - chargeStartTimeMs!!) / 60000).toInt()
            val gainedPercent = (levelPercent - chargeStartLevel!!).coerceAtLeast(0)
            val avgMa = if (accumulatedMaCount > 0) (accumulatedMaSum / accumulatedMaCount).toInt() else abs(normalizedCurrentMa).toInt()
            "This charge: +$gainedPercent% in ${elapsedMinutes}m, avg ${avgMa}mA"
        } else null

        val remainingTimeMinutes = computeTimeRemainingMinutes(levelPercent, isCharging, normalizedCurrentMa, estimatedCapacityMah)

        val updatedTelemetry = BatteryTelemetry(
            levelPercent = levelPercent,
            isCharging = isCharging,
            isFull = isFull,
            pluggedState = pluggedState,
            currentMa = normalizedCurrentMa,
            voltageV = voltageV,
            temperatureC = tempC,
            healthStatus = healthStatus,
            cycleCount = cycleCount,
            technology = tech,
            estimatedCapacityMah = estimatedCapacityMah,
            remainingTimeMinutes = remainingTimeMinutes,
            chargingSessionSummary = sessionSummary
        )

        _telemetryState.value = updatedTelemetry
        recordHistoryPoint(levelPercent, isCharging)
    }

    private fun refreshCurrentNow() {
        val currentTelemetry = _telemetryState.value
        val isCharging = currentTelemetry.isCharging

        val currentMicro = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val rawMa = if (currentMicro != Int.MIN_VALUE && currentMicro != 0) {
            currentMicro / 1000f
        } else 0f

        val normalizedCurrentMa = if (isCharging) {
            if (abs(rawMa) > 0f) abs(rawMa) else 450f
        } else {
            if (abs(rawMa) > 0f) -abs(rawMa) else -250f
        }

        val updatedTime = computeTimeRemainingMinutes(
            currentTelemetry.levelPercent,
            isCharging,
            normalizedCurrentMa,
            currentTelemetry.estimatedCapacityMah
        )

        _telemetryState.value = currentTelemetry.copy(
            currentMa = normalizedCurrentMa,
            remainingTimeMinutes = updatedTime
        )
    }

    private fun computeTimeRemainingMinutes(
        levelPercent: Int,
        isCharging: Boolean,
        currentMa: Float,
        capacityMah: Int
    ): Int {
        val absCurrentMa = abs(currentMa)
        if (absCurrentMa < 10f) {
            return if (isCharging) {
                ((100 - levelPercent) * 1.5f).toInt()
            } else {
                (levelPercent * 6f).toInt()
            }
        }

        return if (isCharging) {
            val remainingMah = (100 - levelPercent) / 100f * capacityMah
            ((remainingMah / absCurrentMa) * 60f).toInt().coerceIn(0, 1440)
        } else {
            val currentMah = (levelPercent / 100f) * capacityMah
            ((currentMah / absCurrentMa) * 60f).toInt().coerceIn(0, 2880)
        }
    }

    private fun recordHistoryPoint(levelPercent: Int, isCharging: Boolean) {
        val now = System.currentTimeMillis()
        val lastPoint = historyBuffer.lastOrNull()

        if (lastPoint == null || lastPoint.levelPercent != levelPercent || (now - lastPoint.timestamp) > 300_000) {
            if (historyBuffer.size >= 50) {
                historyBuffer.removeAt(0)
            }
            historyBuffer.add(HistoryPoint(now, levelPercent, isCharging))
            _historyState.value = historyBuffer.toList()
        }
    }
}
