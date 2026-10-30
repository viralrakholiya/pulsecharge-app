package com.pulsecharge.app.data.model

data class HistoryPoint(
    val timestamp: Long,
    val levelPercent: Int,
    val isCharging: Boolean
)
