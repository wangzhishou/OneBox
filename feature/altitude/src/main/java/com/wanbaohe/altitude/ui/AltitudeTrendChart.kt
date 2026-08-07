package com.wanbaohe.altitude.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R as CoreR

/**
 * 海拔趋势折线图（Canvas 自绘，零第三方依赖）
 * 显示最近一批历史记录的海拔变化曲线
 */
@Composable
internal fun AltitudeTrendChart(
    /** 时间正序的海拔值列表（米） */
    points: List<Float>,
    modifier: Modifier = Modifier
) {
    // 至少 2 个点才能绘线
    if (points.size < 2) return

    val primary = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    val minVal = remember(points) { points.min() }
    val maxVal = remember(points) { points.max() }
    val range = remember(minVal, maxVal) { (maxVal - minVal).coerceAtLeast(1f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(CoreR.string.altitude_trend),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            val w = size.width
            val h = size.height
            val step = w / (points.size - 1).coerceAtLeast(1)

            // 计算折线各顶点坐标
            val coords = points.mapIndexed { index, value ->
                Offset(
                    x = index * step,
                    y = h - ((value - minVal) / range) * h
                )
            }

            // ── 填充区域 ──────────────────────────────────────────
            val fillPath = Path().apply {
                moveTo(coords.first().x, h)
                coords.forEach { lineTo(it.x, it.y) }
                lineTo(coords.last().x, h)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primary.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h
                )
            )

            // ── 折线 ──────────────────────────────────────────────
            val linePath = Path().apply {
                moveTo(coords.first().x, coords.first().y)
                coords.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(
                path = linePath,
                color = primary,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // ── 末端高亮点 ────────────────────────────────────────
            drawCircle(
                color = primary,
                radius = 5.dp.toPx(),
                center = coords.last()
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = coords.last()
            )
        }
    }
}
