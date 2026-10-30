package com.pulsecharge.app.data.model

data class BatteryTelemetry(
    val levelPercent: Int = 0,
    val isCharging: Boolean = false,
    val isFull: Boolean = false,
    val pluggedState: String = "Discharging",
    val currentMa: Float = 0f,
    val voltageV: Float = 0f,
    val temperatureC: Float = 0f,
    val healthStatus: String = "Good",
    val cycleCount: String = "Unavailable",
    val technology: String = "Li-ion",
    val estimatedCapacityMah: Int = 4000,
    val remainingTimeMinutes: Int = 0,
    val chargingSessionSummary: String? = null
)
