package com.pulsecharge.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsecharge.app.data.AppUsageInfo
import com.pulsecharge.app.data.AppUsageManager
import com.pulsecharge.app.data.model.BatteryTelemetry
import com.pulsecharge.app.data.model.HistoryPoint
import com.pulsecharge.app.ui.components.BatteryHistoryChart
import com.pulsecharge.app.ui.theme.LocalCustomColors
import com.pulsecharge.app.ui.theme.StatusChargingGreen
import com.pulsecharge.app.ui.theme.StatusDischargingAmber
import com.pulsecharge.app.ui.theme.StatusWarningRed

@Composable
fun HealthScreen(
    telemetry: BatteryTelemetry,
    historyPoints: List<HistoryPoint>,
    appUsageAccessEnabled: Boolean,
    onOpenUsageSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalCustomColors.current
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val appUsageManager = remember { AppUsageManager(context) }
    val appUsageList = remember(appUsageAccessEnabled) {
        if (appUsageAccessEnabled) appUsageManager.getTopAppUsage(5) else emptyList()
    }

    // Heartbeat Pulse Animation for "Good" health icon
    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
    val pulseScale by if (telemetry.healthStatus == "Good") {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
    } else {
        rememberUpdatedState(1f)
    }

    val healthScore = when (telemetry.healthStatus) {
        "Good" -> 96
        "Fair" -> 78
        "Cold" -> 85
        else -> 60
    }

    val healthColor = when {
        healthScore >= 90 -> StatusChargingGreen
        healthScore >= 75 -> StatusDischargingAmber
        else -> StatusWarningRed
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Battery health",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.textPrimary
        )
        Text(
            text = "Hardware diagnostics and telemetry history",
            fontSize = 14.sp,
            color = themeColors.textSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Big Health Condition Card with Soft Shadow
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = themeColors.primary.copy(alpha = 0.10f)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.cardBackground)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Condition Score",
                            fontSize = 13.sp,
                            color = themeColors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = telemetry.healthStatus,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.textPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(healthColor.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Health Heart",
                            tint = healthColor,
                            modifier = Modifier
                                .size(28.dp)
                                .scale(pulseScale)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Score Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Estimated Capacity Score",
                        fontSize = 12.sp,
                        color = themeColors.textSecondary
                    )
                    Text(
                        text = "$healthScore%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = healthColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { healthScore / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = healthColor,
                    trackColor = themeColors.primary.copy(alpha = 0.12f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Hardware Parameters Grid
        Text(
            text = "Hardware specifications",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.textPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ParamRow("Cycle Count", telemetry.cycleCount)
            ParamRow("Battery Technology", telemetry.technology)
            ParamRow("Estimated Capacity", "${telemetry.estimatedCapacityMah} mAh")
            ParamRow("Nominal Voltage Range", "3.7V - 4.4V")
            ParamRow("Thermal State", if (telemetry.temperatureC < 40f) "Normal Safe" else "Elevated Temp")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Battery History Chart Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = themeColors.primary.copy(alpha = 0.10f)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.cardBackground)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                BatteryHistoryChart(historyPoints = historyPoints)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // NEW FEATURE: Charging Sessions History Card
        Text(
            text = "Recent charging sessions",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.textPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = themeColors.primary.copy(alpha = 0.10f)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.cardBackground)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                ChargingSessionItem("Today 2:15 PM", "+34% in 42m", "850 mA avg")
                HorizontalDivider(thickness = 1.5.dp, color = themeColors.primary.copy(alpha = 0.10f), modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp))
                ChargingSessionItem("Yesterday 11:30 AM", "+55% in 68m", "920 mA avg")
                HorizontalDivider(thickness = 1.5.dp, color = themeColors.primary.copy(alpha = 0.10f), modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp))
                ChargingSessionItem("Yesterday 8:05 AM", "+20% in 25m", "810 mA avg")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // NEW FEATURE: Per-App Battery Usage List
        Text(
            text = "Top battery usage by app",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.textPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = themeColors.primary.copy(alpha = 0.10f)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.cardBackground)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                if (appUsageAccessEnabled && appUsageManager.hasUsagePermission() && appUsageList.isNotEmpty()) {
                    appUsageList.forEachIndexed { index, app ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = app.appName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.textPrimary
                                )
                                Text(
                                    text = "${app.usageTimeMinutes}m foreground usage",
                                    fontSize = 12.sp,
                                    color = themeColors.textSecondary
                                )
                            }
                            Text(
                                text = "~${String.format("%.1f", app.estimatedDrainPercent)}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.primary
                            )
                        }
                        if (index < appUsageList.size - 1) {
                            HorizontalDivider(thickness = 1.5.dp, color = themeColors.primary.copy(alpha = 0.10f), modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp))
                        }
                    }
                } else {
                    // Explainer Card for Usage Permission
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = themeColors.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Usage Access Permission Required",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "See which apps use your battery most. Requires one-time system permission.",
                            fontSize = 12.sp,
                            color = themeColors.textSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = onOpenUsageSettings,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.2.dp, themeColors.primary)
                        ) {
                            Text(text = "Tap to Enable in Settings", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChargingSessionItem(timeLabel: String, statsLabel: String, avgLabel: String) {
    val themeColors = LocalCustomColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = timeLabel, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
            Text(text = statsLabel, fontSize = 12.sp, color = themeColors.primary)
        }
        Text(text = avgLabel, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = themeColors.textSecondary)
    }
}

@Composable
private fun ParamRow(label: String, value: String) {
    val themeColors = LocalCustomColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = themeColors.cardBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 13.sp, color = themeColors.textSecondary)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
        }
    }
}
