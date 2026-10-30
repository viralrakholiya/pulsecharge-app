package com.pulsecharge.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsecharge.app.data.model.BatteryTelemetry
import com.pulsecharge.app.ui.components.BannerAdView
import com.pulsecharge.app.ui.theme.LocalCustomColors
import com.pulsecharge.app.ui.theme.StatusChargingGreen
import com.pulsecharge.app.ui.theme.StatusDischargingAmber
import com.pulsecharge.app.ui.theme.StatusWarningRed

private data class BatteryTip(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val priority: TipPriority
)

private enum class TipPriority(val label: String, val color: Color) {
    HIGH("High Impact", StatusChargingGreen),
    MEDIUM("Good Practice", StatusDischargingAmber),
    LOW("Optional", Color(0xFF64B5F6))
}

private val BATTERY_TIPS = listOf(
    BatteryTip(
        icon = Icons.Default.Favorite,
        title = "Keep battery between 20–80%",
        description = "Lithium-ion batteries have the longest lifespan when kept in the 20–80% range. Avoid draining to 0% or charging to 100% regularly.",
        priority = TipPriority.HIGH
    ),
    BatteryTip(
        icon = Icons.Default.Warning,
        title = "Avoid overnight charging",
        description = "Keeping your phone plugged in at 100% for long periods generates heat which degrades battery chemistry over time. Use the charge alert to unplug at 80–90%.",
        priority = TipPriority.HIGH
    ),
    BatteryTip(
        icon = Icons.Default.Info,
        title = "Manage screen brightness",
        description = "The display is typically the biggest battery drain. Use adaptive brightness or set a lower brightness level to significantly extend battery life.",
        priority = TipPriority.MEDIUM
    ),
    BatteryTip(
        icon = Icons.Default.Refresh,
        title = "Limit background app refresh",
        description = "Apps running in the background consume battery even when you're not using them. Restrict background activity for apps you rarely use.",
        priority = TipPriority.MEDIUM
    ),
    BatteryTip(
        icon = Icons.Default.Star,
        title = "Use Wi-Fi over mobile data",
        description = "Wi-Fi consumes significantly less power than mobile data. When available, connect to Wi-Fi to conserve battery life.",
        priority = TipPriority.MEDIUM
    ),
    BatteryTip(
        icon = Icons.Default.Lock,
        title = "Avoid extreme temperatures",
        description = "Heat above 35°C permanently damages battery capacity. Keep your device out of direct sunlight and avoid using phone-intensive apps while charging.",
        priority = TipPriority.HIGH
    ),
    BatteryTip(
        icon = Icons.Default.Notifications,
        title = "Reduce push notifications",
        description = "Every notification wakes your processor and radio. Limiting unnecessary app notifications can meaningfully extend your battery life throughout the day.",
        priority = TipPriority.LOW
    ),
    BatteryTip(
        icon = Icons.Default.Settings,
        title = "Enable battery saver mode",
        description = "Android's built-in battery saver mode reduces background activity and performance to extend battery life when it gets low.",
        priority = TipPriority.MEDIUM
    )
)

@Composable
fun TipsScreen(
    telemetry: BatteryTelemetry,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalCustomColors.current
    val scrollState = rememberScrollState()

    // Battery wellness score derived from telemetry
    val wellnessScore = computeWellnessScore(telemetry)
    val wellnessColor = when {
        wellnessScore >= 80 -> StatusChargingGreen
        wellnessScore >= 55 -> StatusDischargingAmber
        else -> StatusWarningRed
    }
    val wellnessLabel = when {
        wellnessScore >= 80 -> "Excellent"
        wellnessScore >= 55 -> "Fair"
        else -> "Poor"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        Text(
            text = "Battery tips & care",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.textPrimary
        )
        Text(
            text = "Maximize lifespan & daily performance",
            fontSize = 14.sp,
            color = themeColors.textSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Wellness Score Hero Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = wellnessColor.copy(alpha = 0.25f)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.cardBackground)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                wellnessColor.copy(alpha = 0.10f),
                                themeColors.cardBackground
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Score Circle
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(wellnessColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$wellnessScore",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = wellnessColor
                            )
                            Text(
                                text = "/100",
                                fontSize = 11.sp,
                                color = themeColors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Battery Wellness",
                            fontSize = 13.sp,
                            color = themeColors.textSecondary
                        )
                        Text(
                            text = wellnessLabel,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { wellnessScore / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = wellnessColor,
                            trackColor = themeColors.primary.copy(alpha = 0.12f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Current status quick summary
        QuickStatusRow(telemetry, themeColors.primary, themeColors.textPrimary, themeColors.textSecondary, themeColors.cardBackground)

        Spacer(modifier = Modifier.height(24.dp))

        // AdMob Banner Ad (live)
        BannerAdView()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Care tips",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.textPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Tips Cards
        BATTERY_TIPS.forEachIndexed { index, tip ->
            TipCard(tip = tip, cardBg = themeColors.cardBackground, textPrimary = themeColors.textPrimary, textSecondary = themeColors.textSecondary)
            if (index < BATTERY_TIPS.size - 1) {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun QuickStatusRow(
    telemetry: BatteryTelemetry,
    primary: Color,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color
) {
    val tempWarning = telemetry.temperatureC >= 38f
    val levelWarning = telemetry.levelPercent < 20
    val overchargeWarning = telemetry.levelPercent >= 95 && telemetry.isCharging

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatusChip(
            label = "${telemetry.levelPercent}%",
            sublabel = if (levelWarning) "Low battery" else "Level OK",
            color = if (levelWarning) StatusWarningRed else StatusChargingGreen,
            modifier = Modifier.weight(1f),
            cardBg = cardBg,
            textPrimary = textPrimary,
            textSecondary = textSecondary
        )
        StatusChip(
            label = "${String.format("%.1f", telemetry.temperatureC)}°C",
            sublabel = if (tempWarning) "Warm!" else "Normal temp",
            color = if (tempWarning) StatusWarningRed else StatusChargingGreen,
            modifier = Modifier.weight(1f),
            cardBg = cardBg,
            textPrimary = textPrimary,
            textSecondary = textSecondary
        )
        StatusChip(
            label = if (overchargeWarning) "Unplug!" else if (telemetry.isCharging) "Charging" else "On Battery",
            sublabel = if (overchargeWarning) "Near 100%" else telemetry.pluggedState.take(14),
            color = if (overchargeWarning) StatusWarningRed else if (telemetry.isCharging) StatusChargingGreen else StatusDischargingAmber,
            modifier = Modifier.weight(1f),
            cardBg = cardBg,
            textPrimary = textPrimary,
            textSecondary = textSecondary
        )
    }
}

@Composable
private fun StatusChip(
    label: String,
    sublabel: String,
    color: Color,
    modifier: Modifier = Modifier,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = cardBg
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = sublabel,
                fontSize = 10.sp,
                color = textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TipCard(
    tip: BatteryTip,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = tip.priority.color.copy(alpha = 0.12f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(tip.priority.color.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tip.icon,
                        contentDescription = null,
                        tint = tip.priority.color,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tip.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = tip.priority.color.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = tip.priority.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = tip.priority.color,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.Info else Icons.Default.Add,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = tip.priority.color.copy(alpha = 0.10f))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = tip.description,
                    fontSize = 13.sp,
                    color = textSecondary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

private fun computeWellnessScore(telemetry: BatteryTelemetry): Int {
    var score = 100

    // Penalise for very low level
    if (telemetry.levelPercent < 15) score -= 20
    else if (telemetry.levelPercent < 20) score -= 10

    // Penalise for staying near 100% while charging
    if (telemetry.levelPercent >= 95 && telemetry.isCharging) score -= 15

    // Penalise for heat
    if (telemetry.temperatureC >= 42f) score -= 25
    else if (telemetry.temperatureC >= 38f) score -= 12

    // Penalise for degraded health
    when (telemetry.healthStatus) {
        "Good" -> Unit
        "Fair", "Cold" -> score -= 10
        "Overheat" -> score -= 30
        "Dead", "Over Voltage", "Unspecified Failure" -> score -= 40
        else -> score -= 5
    }

    return score.coerceIn(0, 100)
}
