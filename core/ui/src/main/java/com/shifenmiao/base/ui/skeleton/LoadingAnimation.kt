package com.shifenmiao.core.ui.skeleton

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.shifenmiao.theme.AppTheme

@Composable
fun Modifier.shimmerLoading(
    isActive: Boolean = true,
    isLightMode: Boolean = !isSystemInDarkTheme(),
    widthOfShadowBrush: Int = 500,
    durationMillis: Int = 1500
): Modifier {
    if (!isActive) {
        return this
    } else {
        return composed {
            val shimmerColors = remember { getColors(isLightMode) }
            val transition = rememberInfiniteTransition(label = "")
            val offsetX by transition.animateFloat(
                initialValue = 0f,
                targetValue = (durationMillis + widthOfShadowBrush).toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = durationMillis),
                    repeatMode = RepeatMode.Restart
                ),
                label = "ShimmerLoadingAnimation"
            )

            this.background(
                brush = Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(x = offsetX - widthOfShadowBrush, y = 0f),
                    end = Offset(x = offsetX, y = 0f)
                )
            )
        }
    }
}

private fun getColors(isLightMode: Boolean): List<Color> {
    return if (isLightMode) {
        val color = AppTheme.colorScheme.surfaceContainer
        listOf(
            color.copy(0.4f),
            color.copy(1f),
            color.copy(0.4f)
        )
    } else {
        val color = AppTheme.colorScheme.surfaceContainerHighest
        listOf(
            color.copy(0.0f),
            color.copy(0.3f),
            color.copy(0.5f),
            color.copy(0.3f),
            color.copy(0.0f),
        )
    }
}