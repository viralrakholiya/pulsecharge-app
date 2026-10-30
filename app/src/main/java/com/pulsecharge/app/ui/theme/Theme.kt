package com.pulsecharge.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.pulsecharge.app.data.model.AccentTheme
import com.pulsecharge.app.data.model.AppearanceMode

data class CustomThemeColors(
    val primary: Color,
    val secondary: Color,
    val container: Color,
    val background: Color,
    val surface: Color,
    val cardBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color
)

val LocalCustomColors = staticCompositionLocalOf {
    CustomThemeColors(
        primary = PurplePrimary,
        secondary = PurpleSecondary,
        container = PurpleContainer,
        background = DarkBackground,
        surface = DarkSurface,
        cardBackground = DarkCardBg,
        textPrimary = DarkTextPrimary,
        textSecondary = DarkTextSecondary
    )
}

@Composable
fun PulseChargeTheme(
    accentTheme: AccentTheme = AccentTheme.PURPLE,
    appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appearanceMode) {
        AppearanceMode.SYSTEM -> isSystemInDarkTheme()
        AppearanceMode.DARK -> true
        AppearanceMode.LIGHT -> false
    }

    val (rawPrimary, rawSecondary, rawContainer) = when (accentTheme) {
        AccentTheme.PURPLE -> Triple(PurplePrimary, PurpleSecondary, PurpleContainer)
        AccentTheme.TEAL -> Triple(TealPrimary, TealSecondary, TealContainer)
        AccentTheme.CORAL -> Triple(CoralPrimary, CoralSecondary, CoralContainer)
        AccentTheme.BLUE -> Triple(BluePrimary, BlueSecondary, BlueContainer)
        AccentTheme.AMBER -> Triple(AmberPrimary, AmberSecondary, AmberContainer)
        AccentTheme.ROSE -> Triple(RosePrimary, RoseSecondary, RoseContainer)
    }

    val targetBackground = if (darkTheme) DarkBackground else LightBackground
    val targetSurface = if (darkTheme) DarkSurface else LightSurface
    val targetCardBg = if (darkTheme) DarkCardBg else LightCardBg
    val targetTextPrimary = if (darkTheme) DarkTextPrimary else LightTextPrimary
    val targetTextSecondary = if (darkTheme) DarkTextSecondary else LightTextSecondary

    // Smooth animated color swaps for theme changing
    val animPrimary = animateColorAsState(rawPrimary, tween(400), label = "primary").value
    val animSecondary = animateColorAsState(rawSecondary, tween(400), label = "secondary").value
    val animContainer = animateColorAsState(rawContainer, tween(400), label = "container").value
    val animBg = animateColorAsState(targetBackground, tween(400), label = "bg").value
    val animSurface = animateColorAsState(targetSurface, tween(400), label = "surface").value
    val animCardBg = animateColorAsState(targetCardBg, tween(400), label = "cardBg").value
    val animTextPrimary = animateColorAsState(targetTextPrimary, tween(400), label = "textPrimary").value
    val animTextSecondary = animateColorAsState(targetTextSecondary, tween(400), label = "textSecondary").value

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = animPrimary,
            secondary = animSecondary,
            background = animBg,
            surface = animSurface,
            onBackground = animTextPrimary,
            onSurface = animTextPrimary
        )
    } else {
        lightColorScheme(
            primary = animPrimary,
            secondary = animSecondary,
            background = animBg,
            surface = animSurface,
            onBackground = animTextPrimary,
            onSurface = animTextPrimary
        )
    }

    val customColors = CustomThemeColors(
        primary = animPrimary,
        secondary = animSecondary,
        container = animContainer,
        background = animBg,
        surface = animSurface,
        cardBackground = animCardBg,
        textPrimary = animTextPrimary,
        textSecondary = animTextSecondary
    )

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
