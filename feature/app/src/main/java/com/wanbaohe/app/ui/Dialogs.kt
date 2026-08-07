package com.wanbaohe.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import com.shifenmiao.theme.AppTheme
import com.t8rin.dynamic.theme.ColorTuple
import com.t8rin.imagetoolbox.core.resources.icons.Check


@Composable
fun ColorCircle(
    colorTuple: ColorTuple,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    showBackground: Boolean = false,
    icon: ImageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
    onClick: (ColorTuple) -> Unit = {},
    onLongClick: (ColorTuple) -> Unit = {}
) {
    val colors = listOfNotNull(
        colorTuple.primary,
        colorTuple.secondary,
        colorTuple.tertiary,
        colorTuple.surface
    )
    val segmentAngle = 360f / colors.size

    BoxWithConstraints(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongClick(colorTuple) },
                    onTap = { onClick(colorTuple) }
                )
            }
    ) {
        val iconSize = maxWidth * 0.4f

        // 优化 Canvas 绘制逻辑
        Canvas(modifier = Modifier.matchParentSize()) {
            colors.forEachIndexed { index, color ->
                rotate(degrees = index * segmentAngle) {
                    drawArc(
                        color = color,
                        startAngle = 0f,
                        sweepAngle = segmentAngle,
                        useCenter = true
                    )
                }
            }
        }

        // 优化选中状态和背景逻辑
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .align(Alignment.Center)
                    .background(
                        if (showBackground) {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        } else {
                            Color.Transparent
                        },
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppTheme.colors.getPrimaryColor(),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
