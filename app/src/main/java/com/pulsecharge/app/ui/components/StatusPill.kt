package com.pulsecharge.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsecharge.app.ui.theme.LocalCustomColors
import com.pulsecharge.app.ui.theme.StatusChargingGreen
import com.pulsecharge.app.ui.theme.StatusDischargingAmber
import com.pulsecharge.app.ui.theme.StatusFullBlue

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun StatusPill(
    isCharging: Boolean,
    isFull: Boolean,
    pluggedState: String,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalCustomColors.current

    val (statusText, dotColor) = when {
        isFull -> "Fully Charged" to StatusFullBlue
        isCharging -> "Charging" to StatusChargingGreen
        else -> "Discharging" to StatusDischargingAmber
    }

    Row(
        modifier = modifier
            .background(
                color = themeColors.surface,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color = dotColor, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))

        AnimatedContent(
            targetState = statusText to pluggedState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "statusPillText"
        ) { (text, detail) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
                if (detail.isNotBlank() && detail != text) {
                    Text(
                        text = " • $detail",
                        fontSize = 12.sp,
                        color = themeColors.textSecondary
                    )
                }
            }
        }
    }
}
