package com.pulsecharge.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsecharge.app.data.model.BatteryTelemetry
import com.pulsecharge.app.data.model.TempUnit
import com.pulsecharge.app.ui.components.AnimatedBatteryCanvas
import com.pulsecharge.app.ui.components.BannerAdView
import com.pulsecharge.app.ui.components.MetricCard
import com.pulsecharge.app.ui.components.StatusPill
import com.pulsecharge.app.ui.theme.LocalCustomColors
import com.pulsecharge.app.ui.theme.StatusChargingGreen
import com.pulsecharge.app.ui.theme.StatusDischargingAmber
import com.pulsecharge.app.ui.theme.StatusWarningRed
import kotlin.math.abs

@Composable
fun DashboardScreen(
    telemetry: BatteryTelemetry,
    tempUnit: TempUnit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalCustomColors.current
    val scrollState = rememberScrollState()

    val displayTemp = if (tempUnit == TempUnit.FAHRENHEIT) {
        (telemetry.temperatureC * 9 / 5f) + 32f
    } else {
        telemetry.temperatureC
    }
    val tempUnitStr = if (tempUnit == TempUnit.FAHRENHEIT) "°F" else "°C"

    // SINGLE SOURCE OF TRUTH FOR CHARGING STATE & SUBTITLE
    val isCharging = telemetry.isCharging
    val currentStr = if (isCharging) "+${abs(telemetry.currentMa.toInt())}" else "${-abs(telemetry.currentMa.toInt())}"
    val currentSubtitle = if (isCharging) "Charging rate" else "Discharge rate"
    val voltageMv = (telemetry.voltageV * 1000).toInt()

    // Battery wellness score (same logic as TipsScreen)
    val wellnessScore = run {
        var score = 100
        if (telemetry.levelPercent < 15) score -= 20 else if (telemetry.levelPercent < 20) score -= 10
        if (telemetry.levelPercent >= 95 && isCharging) score -= 15
        if (telemetry.temperatureC >= 42f) score -= 25 else if (telemetry.temperatureC >= 38f) score -= 12
        when (telemetry.healthStatus) {
            "Good" -> Unit
            "Fair", "Cold" -> score -= 10
            "Overheat" -> score -= 30
            "Dead", "Over Voltage", "Unspecified Failure" -> score -= 40
            else -> score -= 5
        }
        score.coerceIn(0, 100)
    }
    val wellnessColor = when {
        wellnessScore >= 80 -> StatusChargingGreen
        wellnessScore >= 55 -> StatusDischargingAmber
        else -> StatusWarningRed
    }

    // Smart contextual tip
    val contextualTip: String? = when {
        telemetry.levelPercent >= 95 && isCharging -> "💡 Tip: Unplug now to protect battery health"
        telemetry.temperatureC >= 38f -> "🌡️ Battery is warm — avoid heavy usage while charging"
        telemetry.levelPercent < 15 && !isCharging -> "⚠️ Low battery — plug in soon to avoid sudden shutdown"
        telemetry.levelPercent in 79..81 && isCharging -> "✅ 80% reached — great time to unplug for longevity"
        else -> null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PulseCharge",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
                Text(
                    text = if (isCharging) "Charging active" else "Operating on battery power",
                    fontSize = 14.sp,
                    color = themeColors.textSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Wellness score badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = wellnessColor.copy(alpha = 0.14f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$wellnessScore",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = wellnessColor
                        )
                        Text(
                            text = "score",
                            fontSize = 9.sp,
                            color = wellnessColor.copy(alpha = 0.8f)
                        )
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .background(themeColors.cardBackground, RoundedCornerShape(12.dp))
                        .size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = themeColors.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Animated Battery Canvas Graphic with Liquid Wave Front
        AnimatedBatteryCanvas(
            levelPercent = telemetry.levelPercent,
            isCharging = isCharging
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Status Pill Badge
        StatusPill(
            isCharging = isCharging,
            isFull = telemetry.isFull,
            pluggedState = telemetry.pluggedState
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Time Remaining Estimate Text
        val remainingHours = telemetry.remainingTimeMinutes / 60
        val remainingMins = telemetry.remainingTimeMinutes % 60
        val timeLabel = if (isCharging) {
            "${remainingHours} hr ${remainingMins} min remaining until full"
        } else {
            "${remainingHours} hr ${remainingMins} min remaining"
        }

        Text(
            text = timeLabel,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = themeColors.textSecondary
        )

        // Contextual Smart Tip Banner
        contextualTip?.let { tip ->
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = wellnessColor.copy(alpha = 0.10f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tip,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = wellnessColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Live Charging Session Summary (if active)
        telemetry.chargingSessionSummary?.let { summary ->
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = StatusChargingGreen.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = summary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusChargingGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section Title: Live telemetry metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Live telemetry metrics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2x2 Telemetry Grid Cards
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MetricCard(
                    title = "Live current",
                    valueStr = currentStr,
                    unit = "mA",
                    subtitle = currentSubtitle,
                    icon = Icons.Default.Star,
                    iconTint = themeColors.primary,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Voltage",
                    valueStr = String.format("%.2f", telemetry.voltageV),
                    unit = "V",
                    subtitle = "$voltageMv mV potential",
                    icon = Icons.Default.Refresh,
                    iconTint = themeColors.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MetricCard(
                    title = "Temperature",
                    valueStr = String.format("%.1f", displayTemp),
                    unit = tempUnitStr,
                    subtitle = if (telemetry.temperatureC >= 40f) "Thermal Warning!" else "Optimal range",
                    icon = Icons.Default.Info,
                    iconTint = if (telemetry.temperatureC >= 40f) StatusWarningRed else themeColors.primary,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Health status",
                    valueStr = telemetry.healthStatus,
                    unit = "",
                    subtitle = "${telemetry.estimatedCapacityMah} mAh capacity",
                    icon = Icons.Default.Favorite,
                    iconTint = themeColors.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Google AdMob Banner Ad — LIVE production ad unit
            BannerAdView()
        }
    }
}
