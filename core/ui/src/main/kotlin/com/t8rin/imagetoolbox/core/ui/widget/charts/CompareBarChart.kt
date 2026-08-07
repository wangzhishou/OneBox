package com.t8rin.imagetoolbox.core.ui.widget.charts

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class BarChartEntry(
    val label: String,
    val value: Float,
)

@Composable
fun CompareBarChart(
    entries: List<BarChartEntry>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 180.dp,
    barColor: Color,
) {
    val max = entries.maxOfOrNull { it.value }?.takeIf { it > 0f } ?: 1f
    val axisColor = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            val widthPerItem = size.width / entries.size.coerceAtLeast(1)
            val barWidth = widthPerItem * 0.56f
            val bottom = size.height

            drawLine(
                color = axisColor,
                start = Offset(0f, bottom),
                end = Offset(size.width, bottom),
                strokeWidth = 1.dp.toPx()
            )

            entries.forEachIndexed { index, entry ->
                val xCenter = widthPerItem * (index + 0.5f)
                val barHeight = (entry.value / max) * size.height * 0.9f
                drawLine(
                    color = barColor,
                    start = Offset(xCenter, bottom),
                    end = Offset(xCenter, bottom - barHeight),
                    strokeWidth = barWidth,
                    cap = StrokeCap.Butt
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            entries.forEach { entry ->
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}


