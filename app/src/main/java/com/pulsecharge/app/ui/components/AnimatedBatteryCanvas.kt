package com.pulsecharge.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsecharge.app.ui.theme.LocalCustomColors
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AnimatedBatteryCanvas(
    levelPercent: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalCustomColors.current

    // 1. Spring-based percentage level fill animation
    val animatedLevelFraction by animateFloatAsState(
        targetValue = (levelPercent.coerceIn(0, 100)) / 100f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "batteryFillAnimation"
    )

    // 2. Continuous liquid wave phase loop (faster & energetic when charging ~1.8s, smooth fluid ~3.2s when idle/discharging)
    val infiniteTransition = rememberInfiniteTransition(label = "waveTransition")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isCharging) 1800 else 3200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    // 3. Charging shimmer sweep animation
    val shimmerOffset by if (isCharging) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerOffset"
        )
    } else {
        rememberUpdatedState(0f)
    }

    val primaryFill = themeColors.primary
    val secondaryFill = themeColors.secondary
    val outlineColor = themeColors.primary.copy(alpha = 0.95f)
    val trackBgColor = themeColors.surface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val batteryWidth = canvasWidth * 0.78f
            val batteryHeight = canvasHeight * 0.58f
            val capWidth = batteryWidth * 0.05f
            val capHeight = batteryHeight * 0.38f

            val left = (canvasWidth - batteryWidth - capWidth) / 2f
            val top = (canvasHeight - batteryHeight) / 2f

            val strokeWidth = 5.dp.toPx()
            val cornerRadius = 24.dp.toPx()

            // 1. Outer Battery Housing Track Background
            drawRoundRect(
                color = trackBgColor,
                topLeft = Offset(left, top),
                size = Size(batteryWidth, batteryHeight),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )

            // 2. Battery Terminal Cap
            val capLeft = left + batteryWidth + 2.dp.toPx()
            val capTop = top + (batteryHeight - capHeight) / 2f
            drawRoundRect(
                color = outlineColor,
                topLeft = Offset(capLeft, capTop),
                size = Size(capWidth, capHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            // 3. Inner Liquid Fill Bounds & Clipping
            val padding = 5.dp.toPx()
            val fillLeft = left + padding
            val fillTop = top + padding
            val fillHeight = batteryHeight - (padding * 2)
            val fillBottom = fillTop + fillHeight
            val maxFillWidth = batteryWidth - (padding * 2)
            val innerCornerRadius = (cornerRadius - padding).coerceAtLeast(4.dp.toPx())

            // Clip path for inner battery body to prevent wave overflow at 0% or 100%
            val innerClipPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(fillLeft, fillTop, fillLeft + maxFillWidth, fillBottom),
                        cornerRadius = CornerRadius(innerCornerRadius, innerCornerRadius)
                    )
                )
            }

            // GUARANTEED Wave Amplitude (8dp normally, 10dp when charging — NEVER 0 or flat!)
            val waveAmplitude = if (isCharging) 10.dp.toPx() else 8.dp.toPx()
            val targetFillWidth = (maxFillWidth * animatedLevelFraction).coerceIn(4.dp.toPx(), maxFillWidth)

            // Construct Liquid Wave Path (Top Edge & Wave Front ALWAYS Curved)
            val liquidPath = Path().apply {
                val waveXCenter = fillLeft + targetFillWidth

                // Start at top-left inside battery
                moveTo(fillLeft, fillTop)

                // Generate smooth animated sine wave front from top to bottom
                val steps = 30
                for (i in 0..steps) {
                    val progress = i / steps.toFloat()
                    val y = fillTop + (progress * fillHeight)

                    // Dual-frequency sine wave for realistic fluid sloshing curve (with explicit Float math)
                    val mainSine = sin((progress * 2.5f * PI.toFloat()) + wavePhase)
                    val secondarySine = sin((progress * 4.0f * PI.toFloat()) - (wavePhase * 0.7f)) * 0.3f
                    val waveOffset = (mainSine + secondarySine) * waveAmplitude

                    val currentX = waveXCenter + waveOffset

                    if (i == 0) {
                        lineTo(currentX, y)
                    } else {
                        val prevProgress = (i - 1) / steps.toFloat()
                        val prevY = fillTop + (prevProgress * fillHeight)
                        val prevMain = sin((prevProgress * 2.5f * PI.toFloat()) + wavePhase)
                        val prevSec = sin((prevProgress * 4.0f * PI.toFloat()) - (wavePhase * 0.7f)) * 0.3f
                        val prevX = waveXCenter + ((prevMain + prevSec) * waveAmplitude)

                        val controlY = (prevY + y) / 2f
                        quadraticTo(prevX, controlY, currentX, y)
                    }
                }

                // Close liquid path around bottom-left
                lineTo(fillLeft, fillBottom)
                close()
            }

            val fillBrush = if (isCharging) {
                // Energetic theme-colored shimmer sweep gradient when charging
                Brush.linearGradient(
                    colors = listOf(
                        primaryFill.copy(alpha = 0.85f),
                        primaryFill,
                        secondaryFill,
                        Color.White.copy(alpha = 0.90f),
                        primaryFill,
                        primaryFill.copy(alpha = 0.85f)
                    ),
                    start = Offset(shimmerOffset - 350f, fillTop),
                    end = Offset(shimmerOffset, fillBottom)
                )
            } else {
                // Rich dynamic theme gradient when discharging/idle
                Brush.horizontalGradient(
                    colors = listOf(
                        primaryFill.copy(alpha = 0.90f),
                        secondaryFill,
                        primaryFill
                    )
                )
            }

            // Draw Liquid Fill Wave clipped neatly inside inner battery body
            clipPath(innerClipPath) {
                drawPath(
                    path = liquidPath,
                    brush = fillBrush
                )
            }

            // 4. Outer Battery Outline Housing Stroke
            drawRoundRect(
                color = outlineColor,
                topLeft = Offset(left, top),
                size = Size(batteryWidth, batteryHeight),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = strokeWidth)
            )

            // 5. Active Charging Bolt Icon
            if (isCharging) {
                val boltPath = Path().apply {
                    val centerX = left + (batteryWidth * 0.15f)
                    val centerY = top + (batteryHeight / 2f)
                    moveTo(centerX + 6.dp.toPx(), centerY - 18.dp.toPx())
                    lineTo(centerX - 10.dp.toPx(), centerY + 2.dp.toPx())
                    lineTo(centerX, centerY + 2.dp.toPx())
                    lineTo(centerX - 6.dp.toPx(), centerY + 18.dp.toPx())
                    lineTo(centerX + 10.dp.toPx(), centerY - 2.dp.toPx())
                    lineTo(centerX, centerY - 2.dp.toPx())
                    close()
                }
                drawPath(
                    path = boltPath,
                    color = Color.White
                )
            }
        }

        // Centered High-Contrast Percentage Text Display
        // Uses crisp WHITE text with subtle dark drop-shadow for 100% legibility across all themes & levels!
        val textDropShadow = Shadow(
            color = Color.Black.copy(alpha = 0.65f),
            offset = Offset(1.5f, 2.5f),
            blurRadius = 6f
        )

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$levelPercent",
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                style = TextStyle(
                    color = Color.White,
                    shadow = textDropShadow
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "%",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                style = TextStyle(
                    color = Color.White,
                    shadow = textDropShadow
                ),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
    }
}
