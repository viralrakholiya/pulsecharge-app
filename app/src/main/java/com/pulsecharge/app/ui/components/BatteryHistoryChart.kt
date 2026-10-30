package com.pulsecharge.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsecharge.app.data.model.HistoryPoint
import com.pulsecharge.app.ui.theme.LocalCustomColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BatteryHistoryChart(
    historyPoints: List<HistoryPoint>,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalCustomColors.current
    var selectedPoint by remember { mutableStateOf<HistoryPoint?>(null) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val primaryColor = themeColors.primary
    val gridColor = themeColors.textSecondary.copy(alpha = 0.2f)

    if (historyPoints.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Recording telemetry history...",
                fontSize = 14.sp,
                color = themeColors.textSecondary
            )
        }
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Optional Selected Data Point Tooltip Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Battery Level History",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.textPrimary
            )
            selectedPoint?.let { point ->
                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(point.timestamp))
                Text(
                    text = "${point.levelPercent}% at $timeStr",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            } ?: run {
                Text(
                    text = "Tap graph to inspect",
                    fontSize = 12.sp,
                    color = themeColors.textSecondary
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .pointerInput(historyPoints) {
                    detectTapGestures { tapOffset ->
                        val width = size.width
                        val stepX = width / (historyPoints.size - 1).coerceAtLeast(1)
                        val closestIdx = ((tapOffset.x + (stepX / 2f)) / stepX)
                            .toInt()
                            .coerceIn(0, historyPoints.size - 1)
                        selectedIndex = closestIdx
                        selectedPoint = historyPoints[closestIdx]
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            val paddingLeft = 10.dp.toPx()
            val paddingRight = 10.dp.toPx()
            val paddingTop = 10.dp.toPx()
            val paddingBottom = 20.dp.toPx()

            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom

            // Draw horizontal reference lines (20%, 50%, 80%, 100%)
            val levels = listOf(1.0f, 0.8f, 0.5f, 0.2f)
            levels.forEach { levelFrac ->
                val y = paddingTop + (chartHeight * (1f - levelFrac))
                drawLine(
                    color = gridColor,
                    start = Offset(paddingLeft, y),
                    end = Offset(width - paddingRight, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            val pointsCount = historyPoints.size
            val stepX = if (pointsCount > 1) chartWidth / (pointsCount - 1) else chartWidth

            val path = Path()
            val fillPath = Path()

            val mappedOffsets = historyPoints.mapIndexed { idx, point ->
                val x = paddingLeft + (idx * stepX)
                val y = paddingTop + (chartHeight * (1f - (point.levelPercent / 100f)))
                Offset(x, y)
            }

            if (mappedOffsets.isNotEmpty()) {
                path.moveTo(mappedOffsets.first().x, mappedOffsets.first().y)
                fillPath.moveTo(mappedOffsets.first().x, height - paddingBottom)
                fillPath.lineTo(mappedOffsets.first().x, mappedOffsets.first().y)

                for (i in 0 until mappedOffsets.size - 1) {
                    val p1 = mappedOffsets[i]
                    val p2 = mappedOffsets[i + 1]
                    val control1 = Offset((p1.x + p2.x) / 2f, p1.y)
                    val control2 = Offset((p1.x + p2.x) / 2f, p2.y)
                    path.cubicTo(control1.x, control1.y, control2.x, control2.y, p2.x, p2.y)
                    fillPath.cubicTo(control1.x, control1.y, control2.x, control2.y, p2.x, p2.y)
                }

                fillPath.lineTo(mappedOffsets.last().x, height - paddingBottom)
                fillPath.close()

                // 1. Draw Gradient Area under Line
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.35f),
                            primaryColor.copy(alpha = 0.02f)
                        ),
                        startY = paddingTop,
                        endY = height - paddingBottom
                    )
                )

                // 2. Draw Smooth Bezier Line Path
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // 3. Highlight Selected Point or Last Point
                val activeIdx = selectedIndex ?: (pointsCount - 1)
                val activeOffset = mappedOffsets[activeIdx]

                drawCircle(
                    color = primaryColor.copy(alpha = 0.3f),
                    radius = 10.dp.toPx(),
                    center = activeOffset
                )
                drawCircle(
                    color = primaryColor,
                    radius = 5.dp.toPx(),
                    center = activeOffset
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5.dp.toPx(),
                    center = activeOffset
                )
            }
        }
    }
}
