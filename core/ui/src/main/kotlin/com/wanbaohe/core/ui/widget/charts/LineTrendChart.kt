package com.wanbaohe.core.ui.widget.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

data class TrendSeries(
    val label: String,
    val color: Color,
    /** null = 断点；长度可短于 xLabels（右对齐）或等长 */
    val points: List<Float?>
)

/**
 * 多序列趋势折线图（Canvas 自绘，零第三方依赖）
 * 每条序列一条彩色折线 + 末端高亮点；渐变填充仅作用于第一条序列
 */
@Composable
fun LineTrendChart(
    series: List<TrendSeries>,
    xLabels: List<String>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 160.dp,
    showFill: Boolean = true,
    maxXLabels: Int = 5,
    yValueFormatter: (Float) -> String = { value ->
        val rounded = (value * 10f).roundToInt() / 10f
        if (rounded % 1f == 0f) rounded.toInt().toString() else rounded.toString()
    }
) {
    val axisColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val yLabelStyle = MaterialTheme.typography.labelSmall.copy(color = labelColor)
    val textMeasurer = rememberTextMeasurer()

    val allValues = series.flatMap { it.points }.filterNotNull()
    var minValue = allValues.minOrNull() ?: 0f
    var maxValue = allValues.maxOrNull() ?: 1f
    if (maxValue - minValue < 1e-6f) {
        minValue -= 0.5f
        maxValue += 0.5f
    } else {
        val padding = (maxValue - minValue) * 0.1f
        minValue -= padding
        maxValue += padding
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            val guideFractions = listOf(0f, 0.5f, 1f)
            val guideLabels = guideFractions.map { fraction ->
                textMeasurer.measure(
                    text = yValueFormatter(minValue + (maxValue - minValue) * fraction),
                    style = yLabelStyle
                )
            }
            val leftPadding = guideLabels.maxOf { it.size.width } + 6.dp.toPx()
            val topPadding = 8.dp.toPx()
            val rightPadding = 8.dp.toPx()
            val bottomPadding = 4.dp.toPx()

            val chartLeft = leftPadding
            val chartRight = size.width - rightPadding
            val chartTop = topPadding
            val chartBottom = size.height - bottomPadding
            val chartWidth = chartRight - chartLeft
            val chartHeightPx = chartBottom - chartTop

            fun xAt(index: Int, count: Int) =
                if (count > 1) chartLeft + index * (chartWidth / (count - 1))
                else chartLeft + chartWidth / 2f

            fun yAt(value: Float) =
                chartBottom - ((value - minValue) / (maxValue - minValue)) * chartHeightPx

            // ── Y 轴参考线与数值标签 ──────────────────────────────
            guideFractions.forEachIndexed { index, fraction ->
                val y = chartBottom - fraction * chartHeightPx
                drawLine(
                    color = axisColor.copy(alpha = 0.4f),
                    start = Offset(chartLeft, y),
                    end = Offset(chartRight, y),
                    strokeWidth = 0.5.dp.toPx()
                )
                val label = guideLabels[index]
                drawText(
                    textLayoutResult = label,
                    topLeft = Offset(0f, y - label.size.height / 2f)
                )
            }

            // ── 各序列折线 ────────────────────────────────────────
            val pointCount = xLabels.size
            if (pointCount > 0 && chartWidth > 0f && chartHeightPx > 0f) {
                series.forEachIndexed { seriesIndex, item ->
                    // points 短于 xLabels 时右对齐
                    val indexOffset = (pointCount - item.points.size).coerceAtLeast(0)
                    val coords = item.points.mapIndexed { index, value ->
                        value?.let { Offset(xAt(indexOffset + index, pointCount), yAt(it)) }
                    }

                    // 按 null 断点切分为连续线段
                    val segments = mutableListOf<List<Offset>>()
                    var current = mutableListOf<Offset>()
                    coords.forEach { offset ->
                        if (offset != null) {
                            current.add(offset)
                        } else if (current.isNotEmpty()) {
                            segments.add(current)
                            current = mutableListOf()
                        }
                    }
                    if (current.isNotEmpty()) segments.add(current)

                    // 渐变填充仅第一条序列
                    if (showFill && seriesIndex == 0) {
                        segments.forEach { segment ->
                            val fillPath = Path().apply {
                                moveTo(segment.first().x, chartBottom)
                                segment.forEach { lineTo(it.x, it.y) }
                                lineTo(segment.last().x, chartBottom)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        item.color.copy(alpha = 0.25f),
                                        Color.Transparent
                                    ),
                                    startY = chartTop,
                                    endY = chartBottom
                                )
                            )
                        }
                    }

                    segments.forEach { segment ->
                        if (segment.size > 1) {
                            val linePath = Path().apply {
                                moveTo(segment.first().x, segment.first().y)
                                segment.drop(1).forEach { lineTo(it.x, it.y) }
                            }
                            drawPath(
                                path = linePath,
                                color = item.color,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }

                    // 末端高亮点（单点序列也仅画此点）
                    coords.lastOrNull { it != null }?.let { last ->
                        drawCircle(color = item.color, radius = 5.dp.toPx(), center = last)
                        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = last)
                    }
                }
            }
        }

        // ── X 轴标签（均匀抽样至多 maxXLabels 个）──────────────────
        if (xLabels.isNotEmpty()) {
            val sampleCount = minOf(maxXLabels, xLabels.size)
            val sampledIndices = if (sampleCount <= 1) {
                setOf(0)
            } else {
                (0 until sampleCount).map {
                    (it * (xLabels.size - 1).toFloat() / (sampleCount - 1)).roundToInt()
                }.toSet()
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                xLabels.forEachIndexed { index, label ->
                    Text(
                        text = if (index in sampledIndices) label else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
