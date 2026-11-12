/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.period.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.wanbaohe.period.R
import com.wanbaohe.period.model.PeriodColors
import com.wanbaohe.period.model.PeriodSymptom
import com.wanbaohe.period.screen.component.symptomLabel
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/** 周期长度趋势折线图 — Compose Canvas 自绘 */
@Composable
fun CycleTrendChart(
    points: List<Pair<YearMonth, Int>>,
    averageDays: Int,
    colors: PeriodColors,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (points.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.period_stats_trend_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }
            return@Column
        }
        val minY = (points.minOf { it.second } - 2).coerceAtLeast(1)
        val maxY = (points.maxOf { it.second } + 2).coerceAtLeast(minY + 4)
        val range = (maxY - minY).coerceAtLeast(1)
        val gridValues = listOf(maxY, (minY + maxY) / 2, minY)
        val textMeasurer = rememberTextMeasurer()
        val bubbleTextStyle = MaterialTheme.typography.labelSmall.copy(
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
        val lineColor = colors.chartLine
        val bubbleColor = colors.chartBubble
        val gridColor = colors.onSurfaceVariant.copy(alpha = 0.2f)
        val avgColor = colors.chartLine.copy(alpha = 0.45f)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Column(
                modifier = Modifier.height(180.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                gridValues.forEach { value ->
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp),
            ) {
                val w = size.width
                val h = size.height
                val stepX = if (points.size > 1) w / (points.size - 1) else 0f
                fun xOf(index: Int): Float =
                    if (points.size > 1) (index * stepX).coerceIn(0f, w) else w / 2f
                fun yOf(value: Int): Float = h - (value - minY).toFloat() / range * h

                gridValues.forEach { v ->
                    val y = yOf(v)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f,
                    )
                }
                drawLine(
                    color = avgColor,
                    start = Offset(0f, yOf(averageDays)),
                    end = Offset(w, yOf(averageDays)),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
                )
                val path = Path()
                points.forEachIndexed { index, (_, value) ->
                    val px = xOf(index)
                    val py = yOf(value)
                    if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3f, cap = StrokeCap.Round),
                )
                points.forEachIndexed { index, (_, value) ->
                    val px = xOf(index)
                    val py = yOf(value)
                    drawCircle(color = Color.White, radius = 6f, center = Offset(px, py))
                    drawCircle(color = lineColor, radius = 4f, center = Offset(px, py))
                }
                val last = points.last()
                val lastX = xOf(points.size - 1)
                val lastY = yOf(last.second)
                val bubbleLayout = textMeasurer.measure(
                    text = last.second.toString(),
                    style = bubbleTextStyle,
                )
                val bubbleWidth = bubbleLayout.size.width + 28f
                val bubbleHeight = bubbleLayout.size.height + 14f
                val bubbleLeft = (lastX - bubbleWidth / 2f).coerceIn(0f, w - bubbleWidth)
                val bubbleTop = (lastY - bubbleHeight - 14f).coerceAtLeast(0f)
                drawRoundRect(
                    color = bubbleColor,
                    topLeft = Offset(bubbleLeft, bubbleTop),
                    size = Size(bubbleWidth, bubbleHeight),
                    cornerRadius = CornerRadius(12f),
                )
                drawText(
                    textLayoutResult = bubbleLayout,
                    topLeft = Offset(
                        bubbleLeft + (bubbleWidth - bubbleLayout.size.width) / 2f,
                        bubbleTop + (bubbleHeight - bubbleLayout.size.height) / 2f,
                    ),
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            points.forEach { (yearMonth, _) ->
                Text(
                    text = yearMonth.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

/** 症状分布环形图 — Compose Canvas 自绘 */
@Composable
fun SymptomDonutChart(
    counts: Map<PeriodSymptom, Int>,
    total: Int,
    colors: PeriodColors,
    modifier: Modifier = Modifier,
) {
    if (total <= 0 || counts.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.period_stats_symptom_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
        return
    }
    val palette = listOf(
        colors.accent,
        colors.accent.copy(alpha = 0.75f),
        colors.accent.copy(alpha = 0.55f),
        colors.accent.copy(alpha = 0.40f),
        colors.accent.copy(alpha = 0.28f),
        colors.accent.copy(alpha = 0.18f),
    )
    val slices = counts.entries.take(palette.size)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(140.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(140.dp)) {
                val stroke = 26.dp.toPx()
                val gapDegrees = if (slices.size > 1) 2f else 0f
                var startAngle = -90f
                slices.forEachIndexed { index, (_, value) ->
                    val sweep = value.toFloat() / total * 360f - gapDegrees
                    drawArc(
                        color = palette[index % palette.size],
                        startAngle = startAngle,
                        sweepAngle = sweep.coerceAtLeast(0f),
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Butt),
                    )
                    startAngle += sweep + gapDegrees
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.period_stats_symptom_center),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
                Text(
                    text = total.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent,
                )
            }
        }
        Spacer(modifier = Modifier.size(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            slices.forEachIndexed { index, (symptom, value) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawCircle(palette[index % palette.size])
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = symptomLabel(symptom),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${(value * 100 / total)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.accent,
                    )
                }
            }
        }
    }
}
