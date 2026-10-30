package com.pulsecharge.app.data.model

enum class AccentTheme(val displayName: String) {
    PURPLE("Purple Indigo"),
    TEAL("Cyber Teal"),
    CORAL("Vibrant Coral"),
    BLUE("Electric Blue"),
    AMBER("Sunset Amber"),
    ROSE("Rose Quartz")
}

enum class AppearanceMode {
    SYSTEM, DARK, LIGHT
}

enum class TempUnit {
    CELSIUS, FAHRENHEIT
}

data class AppSettings(
    val accentTheme: AccentTheme = AccentTheme.PURPLE,
    val appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    val tempUnit: TempUnit = TempUnit.CELSIUS,
    val fullChargeAlertEnabled: Boolean = true,
    val fullChargeThresholdPercent: Int = 80,
    val lowBatteryAlertEnabled: Boolean = true,
    val lowBatteryThresholdPercent: Int = 20,
    val overheatAlertEnabled: Boolean = true,
    val overheatThresholdTempC: Int = 40,
    val dailyDigestEnabled: Boolean = false,
    val appUsageAccessEnabled: Boolean = false
)
