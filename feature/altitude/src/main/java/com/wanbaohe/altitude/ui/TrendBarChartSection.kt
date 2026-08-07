package com.wanbaohe.altitude.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.core.R as CoreR

/**
 * 趋势柱状图区域 — 2H AGO → CURRENT PEAK
 * 用柱状图显示最近趋势数据点
 */
@Composable
internal fun TrendBarChartSection(
    points: List<Float>,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) return

    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    val minVal = remember(points) { points.min() }
    val maxVal = remember(points) { points.max() }
    val range = remember(minVal, maxVal) { (maxVal - minVal).coerceAtLeast(1f) }

    // 取最近 8 个点做柱状展示
    val barData = remember(points) {
        val step = (points.size / 8).coerceAtLeast(1)
        points.filterIndexed { i, _ -> i % step == 0 }.takeLast(8)
    }

    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(CoreR.string.altitude_2h_ago),
                style = MaterialTheme.typography.labelSmall,
                color = onSurface.copy(alpha = 0.4f),
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .padding(horizontal = 12.dp)
            ) {
                val barCount = barData.size
                if (barCount == 0) return@Canvas
                val barWidth = (size.width / barCount) * 0.6f
                val gap = (size.width / barCount) * 0.4f

                barData.forEachIndexed { index, value ->
                    val normalizedHeight = ((value - minVal) / range).coerceIn(0.15f, 1f)
                    val barHeight = normalizedHeight * size.height
                    val x = index * (barWidth + gap) + gap / 2

                    // 柱体颜色：最后一个高亮
                    val color = if (index == barData.lastIndex) primary
                    else primary.copy(alpha = 0.25f)

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                }
            }

            Text(
                text = stringResource(CoreR.string.altitude_current_peak),
                style = MaterialTheme.typography.labelSmall,
                color = onSurface.copy(alpha = 0.4f),
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }
    }
}

