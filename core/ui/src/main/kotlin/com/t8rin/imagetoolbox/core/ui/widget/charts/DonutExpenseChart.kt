package com.t8rin.imagetoolbox.core.ui.widget.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class DonutChartSlice(
    val value: Float,
    val color: Color,
    val label: String = ""
)

@Composable
fun DonutExpenseChart(
    slices: List<DonutChartSlice>,
    modifier: Modifier = Modifier,
    chartSize: Dp = 180.dp,
    strokeWidth: Dp = 30.dp,
    centerText: String = ""
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat().takeIf { it > 0f } ?: 1f

    Box(
        modifier = modifier.size(chartSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
            val arcSize = size.minDimension
            val topLeft = Offset(
                x = (this.size.width - arcSize) / 2f,
                y = (this.size.height - arcSize) / 2f
            )
            val rect = Rect(topLeft, androidx.compose.ui.geometry.Size(arcSize, arcSize))

            var startAngle = -90f
            slices.forEach { slice ->
                val sweep = (slice.value / total) * 360f
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = stroke,
                    topLeft = rect.topLeft,
                    size = rect.size
                )
                startAngle += sweep
            }
        }

        if (centerText.isNotBlank()) {
            Text(
                text = centerText,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}


