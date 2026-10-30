package com.pulsecharge.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsecharge.app.data.model.AccentTheme
import com.pulsecharge.app.data.model.AppearanceMode
import com.pulsecharge.app.data.model.AppSettings
import com.pulsecharge.app.data.model.TempUnit
import com.pulsecharge.app.ui.theme.*

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onUpdateAccentTheme: (AccentTheme) -> Unit,
    onUpdateAppearanceMode: (AppearanceMode) -> Unit,
    onUpdateTempUnit: (TempUnit) -> Unit,
    onUpdateFullChargeAlert: (Boolean, Int) -> Unit,
    onUpdateLowBatteryAlert: (Boolean, Int) -> Unit,
    onUpdateOverheatAlert: (Boolean, Int) -> Unit,
    onUpdateDailyDigest: (Boolean) -> Unit,
    onUpdateAppUsageAccess: (Boolean) -> Unit,
    onOpenUsageSettings: () -> Unit,
    onTestNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalCustomColors.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Settings & alerts",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.textPrimary
        )
        Text(
            text = "Configure real system alerts, units, and appearance",
            fontSize = 14.sp,
            color = themeColors.textSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. SECTION: Functional notifications Header
        Text(
            text = "Functional notifications",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.textPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        // SUB-SECTION A: Threshold Alerts Group
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
                // Group Sub-Header Label
                Text(
                    text = "THRESHOLD ALERTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Item 1: Full Charge Alert
                AlertItemRow(
                    title = "Full charge alert",
                    subtitle = "Alert when charged to ${settings.fullChargeThresholdPercent}%",
                    enabled = settings.fullChargeAlertEnabled,
                    icon = Icons.Default.Notifications,
                    onEnabledChange = { onUpdateFullChargeAlert(it, settings.fullChargeThresholdPercent) }
                )

                if (settings.fullChargeAlertEnabled) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Target: ${settings.fullChargeThresholdPercent}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = themeColors.textSecondary
                        )
                        Slider(
                            value = settings.fullChargeThresholdPercent.toFloat(),
                            onValueChange = { onUpdateFullChargeAlert(true, it.toInt()) },
                            valueRange = 60f..100f,
                            modifier = Modifier.fillMaxWidth(0.85f),
                            colors = SliderDefaults.colors(
                                thumbColor = themeColors.primary,
                                activeTrackColor = themeColors.primary,
                                inactiveTrackColor = themeColors.primary.copy(alpha = 0.15f)
                            )
                        )
                    }
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = themeColors.textSecondary.copy(alpha = 0.12f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Item 2: Low Battery Warning
                AlertItemRow(
                    title = "Low battery warning",
                    subtitle = "Alert when battery drops below ${settings.lowBatteryThresholdPercent}%",
                    enabled = settings.lowBatteryAlertEnabled,
                    icon = Icons.Default.Warning,
                    onEnabledChange = { onUpdateLowBatteryAlert(it, settings.lowBatteryThresholdPercent) }
                )

                if (settings.lowBatteryAlertEnabled) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Target: ${settings.lowBatteryThresholdPercent}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = themeColors.textSecondary
                        )
                        Slider(
                            value = settings.lowBatteryThresholdPercent.toFloat(),
                            onValueChange = { onUpdateLowBatteryAlert(true, it.toInt()) },
                            valueRange = 5f..30f,
                            modifier = Modifier.fillMaxWidth(0.85f),
                            colors = SliderDefaults.colors(
                                thumbColor = themeColors.primary,
                                activeTrackColor = themeColors.primary,
                                inactiveTrackColor = themeColors.primary.copy(alpha = 0.15f)
                            )
                        )
                    }
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = themeColors.textSecondary.copy(alpha = 0.12f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Item 3: Overheat Alert
                AlertItemRow(
                    title = "Overheat thermal alert",
                    subtitle = "Alert when temperature exceeds ${settings.overheatThresholdTempC}°C",
                    enabled = settings.overheatAlertEnabled,
                    icon = Icons.Default.Warning,
                    onEnabledChange = { onUpdateOverheatAlert(it, settings.overheatThresholdTempC) }
                )

                if (settings.overheatAlertEnabled) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Target: ${settings.overheatThresholdTempC}°C",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = themeColors.textSecondary
                        )
                        Slider(
                            value = settings.overheatThresholdTempC.toFloat(),
                            onValueChange = { onUpdateOverheatAlert(true, it.toInt()) },
                            valueRange = 35f..50f,
                            modifier = Modifier.fillMaxWidth(0.85f),
                            colors = SliderDefaults.colors(
                                thumbColor = themeColors.primary,
                                activeTrackColor = themeColors.primary,
                                inactiveTrackColor = themeColors.primary.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SUB-SECTION B: Summary & Insights Group
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
                // Group Sub-Header Label
                Text(
                    text = "SUMMARY & INSIGHTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Item 4: Daily Digest Notification Toggle
                AlertItemRow(
                    title = "Daily digest notification",
                    subtitle = "Daily 24h battery summary notification",
                    enabled = settings.dailyDigestEnabled,
                    icon = Icons.Default.Notifications,
                    onEnabledChange = onUpdateDailyDigest
                )

                HorizontalDivider(
                    thickness = 1.dp,
                    color = themeColors.textSecondary.copy(alpha = 0.12f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Item 5: App Usage Access Permission Toggle
                AlertItemRow(
                    title = "App usage access",
                    subtitle = "Show top battery usage by app",
                    enabled = settings.appUsageAccessEnabled,
                    icon = Icons.Default.Info,
                    onEnabledChange = { enabled ->
                        onUpdateAppUsageAccess(enabled)
                        if (enabled) onOpenUsageSettings()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Test Notification Action Button
        OutlinedButton(
            onClick = onTestNotificationClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            border = BorderStroke(1.2.dp, themeColors.primary),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = themeColors.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = themeColors.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Test alert notification",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. SECTION: Appearance & theme
        Text(
            text = "Appearance & theme",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.textPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

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
                Text(
                    text = "Theme Mode",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.textPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = themeColors.background
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AppearanceMode.values().forEach { mode ->
                            val selected = settings.appearanceMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) themeColors.primary else Color.Transparent)
                                    .clickable { onUpdateAppearanceMode(mode) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.name.lowercase().capitalize(),
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) Color.White else themeColors.textSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Temperature Unit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.textPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = themeColors.background
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TempUnit.values().forEach { unit ->
                            val selected = settings.tempUnit == unit
                            val label = if (unit == TempUnit.CELSIUS) "Celsius (°C)" else "Fahrenheit (°F)"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) themeColors.primary else Color.Transparent)
                                    .clickable { onUpdateTempUnit(unit) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) Color.White else themeColors.textSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = themeColors.background)
                Spacer(modifier = Modifier.height(18.dp))

                // Accent Theme Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(themeColors.primary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = themeColors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Accent color theme",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.textPrimary
                        )
                        Text(
                            text = "Select dynamic accent palette",
                            fontSize = 12.sp,
                            color = themeColors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AccentTheme.values().forEach { theme ->
                        val themePrimaryColor = when (theme) {
                            AccentTheme.PURPLE -> PurplePrimary
                            AccentTheme.TEAL -> TealPrimary
                            AccentTheme.CORAL -> CoralPrimary
                            AccentTheme.BLUE -> BluePrimary
                            AccentTheme.AMBER -> AmberPrimary
                            AccentTheme.ROSE -> RosePrimary
                        }
                        val isSelected = settings.accentTheme == theme

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(themePrimaryColor)
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(3.dp, themeColors.textPrimary, CircleShape)
                                        } else Modifier
                                    )
                                    .clickable { onUpdateAccentTheme(theme) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = theme.displayName.split(" ").first(),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) themeColors.textPrimary else themeColors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertItemRow(
    title: String,
    subtitle: String,
    enabled: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onEnabledChange: (Boolean) -> Unit
) {
    val themeColors = LocalCustomColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(themeColors.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = themeColors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                Text(text = subtitle, fontSize = 12.sp, color = themeColors.textSecondary)
            }
        }

        Switch(
            checked = enabled,
            onCheckedChange = onEnabledChange,
            modifier = Modifier.scale(0.78f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = themeColors.primary,
                uncheckedTrackColor = themeColors.background
            )
        )
    }
}

private fun String.capitalize(): String = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
