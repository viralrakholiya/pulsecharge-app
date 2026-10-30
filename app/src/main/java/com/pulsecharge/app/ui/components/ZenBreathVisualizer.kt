package com.pulsecharge.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun ZenBreathVisualizer(
    percentage: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "zenBreathTransition")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val baseRadius = size.minDimension / 4f

        val ringCount = 4
        for (i in 1..ringCount) {
            val ringRadius = baseRadius * (i * 0.35f + breathScale * 0.4f)
            val alpha = (1f - (i / (ringCount + 1f))).coerceIn(0.1f, 0.8f)

            drawCircle(
                color = accentColor.copy(alpha = alpha),
                radius = ringRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = (6f - i).coerceAtLeast(2f))
            )
        }

        drawCircle(
            color = accentColor.copy(alpha = 0.9f),
            radius = baseRadius * 0.5f,
            center = Offset(centerX, centerY)
        )
    }
}
