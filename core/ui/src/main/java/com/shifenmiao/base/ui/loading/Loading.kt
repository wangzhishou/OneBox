package com.shifenmiao.core.ui.loading

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme


@Composable
fun DotDanceLoading(color: Color = AppTheme.colors.getPrimaryColor()) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val offsetY = LocalDensity.current.run { 3.dp.toPx() }
    val animations = List(3) { index ->
        val value by infiniteTransition.animateFloat(
            initialValue = -offsetY,
            targetValue = offsetY,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 400,
                    delayMillis = index * 100,
                    easing = FastOutLinearInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "DotDanceLoadingAnimation"
        )
        value
    }

    Canvas(modifier = Modifier.size(width = 44.dp, height = 20.dp)) {
        val dotRadius = 4.dp.toPx()
        val spacing = (size.width - 2 * dotRadius) / 2

        animations.forEachIndexed { index, item ->
            val center = Offset(
                x = dotRadius + spacing * index,
                y = size.height / 2 - item
            )

            drawCircle(
                color,
                radius = dotRadius,
                center = center
            )
        }
    }
}