package com.wanbaohe.habittracker.screen.tab

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBarChart
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePieChart
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTrendingUp
import com.t8rin.imagetoolbox.core.ui.widget.charts.BarChartEntry
import com.t8rin.imagetoolbox.core.ui.widget.charts.CompareBarChart
import com.t8rin.imagetoolbox.core.ui.widget.charts.DonutChartSlice
import com.t8rin.imagetoolbox.core.ui.widget.charts.DonutExpenseChart
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedChip
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.wanbaohe.habittracker.R
import com.wanbaohe.habittracker.component.HabitTrackerComponent
import com.wanbaohe.habittracker.model.HabitTrendPoint
import com.wanbaohe.habittracker.model.habitPalette
import java.time.LocalDate

// ─────────────────────────────────────────────────────────────────────────────
// 数据 tab:本周趋势柱状图 + 打卡分布环形图 + 打卡率趋势折线
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun StatsTab(
    component: HabitTrackerComponent,
    modifier: Modifier = Modifier,
) {
    val stats by component.statsState.collectAsState()
    val palette = habitPalette()
    val weekdayLabels = stringArrayResource(R.array.habit_weekdays_short)
    val hasHabits = stats.totalHabits > 0

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── 本周趋势(周一~周日每日打卡率) ──────────────
        item {
            StatsSectionCard(
                title = stringResource(R.string.habit_stats_week_trend),
                icon = Icons.Outlined.LineBarChart,
            ) {
                if (!hasHabits) {
                    EmptyChartHint()
                } else {
                    val entries = remember(stats.weekRates, weekdayLabels) {
                        stats.weekRates.mapIndexed { index, rate ->
                            BarChartEntry(
                                label = weekdayLabels.getOrElse(index) { "" },
                                value = rate * 100f,
                            )
                        }
                    }
                    CompareBarChart(
                        entries = entries,
                        barColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        // ── 打卡分布(已打卡 / 未打卡 / 未开始) ─────────
        item {
            StatsSectionCard(
                title = stringResource(R.string.habit_stats_distribution),
                icon = Icons.Outlined.LinePieChart,
            ) {
                if (!hasHabits) {
                    EmptyChartHint()
                } else {
                    val slices = remember(
                        stats.distributionDone,
                        stats.distributionMissed,
                        stats.distributionNotStarted,
                        palette,
                    ) {
                        listOf(
                            DonutChartSlice(stats.distributionDone.toFloat(), palette[0]),
                            DonutChartSlice(stats.distributionMissed.toFloat(), palette[1]),
                            DonutChartSlice(stats.distributionNotStarted.toFloat(), palette[2]),
                        )
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        DonutExpenseChart(
                            slices = slices,
                            centerText = stats.totalHabits.toString(),
                            chartSize = 180.dp,
                            strokeWidth = 30.dp,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    DistributionLegend(
                        entries = listOf(
                            stringResource(R.string.habit_summary_done) to stats.distributionDone,
                            stringResource(R.string.habit_summary_missed) to stats.distributionMissed,
                            stringResource(R.string.habit_stats_not_started) to stats.distributionNotStarted,
                        ),
                        palette = palette,
                    )
                }
            }
        }

        // ── 打卡率趋势(近 7 天 / 近 30 天) ─────────────
        item {
            StatsSectionCard(
                title = stringResource(R.string.habit_stats_rate_trend),
                icon = Icons.Outlined.LineTrendingUp,
                trailing = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EnhancedChip(
                            selected = stats.trendDays == 7,
                            onClick = { component.setTrendDays(7) },
                            selectedColor = MaterialTheme.colorScheme.primary,
                        ) {
                            Text(stringResource(R.string.habit_last_7_days))
                        }
                        EnhancedChip(
                            selected = stats.trendDays == 30,
                            onClick = { component.setTrendDays(30) },
                            selectedColor = MaterialTheme.colorScheme.primary,
                        ) {
                            Text(stringResource(R.string.habit_last_30_days))
                        }
                    }
                },
            ) {
                if (!hasHabits || stats.trendPoints.size < 2) {
                    EmptyChartHint()
                } else {
                    RateTrendChart(
                        points = stats.trendPoints,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 区块卡片容器(标题 + 图标 + 可选右侧操作区)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatsSectionCard(
    title: String,
    icon: ImageVector,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        borderWidth = 0.dp,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        containerAlpha = 0.92f,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                trailing?.invoke()
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 分布图例(色点 + 标签 + 数量)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DistributionLegend(
    entries: List<Pair<String, Int>>,
    palette: List<Color>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        entries.forEachIndexed { index, (label, count) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(palette[index % palette.size]),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 打卡率趋势折线图(仿 AltitudeTrendChart 自绘 Canvas,纵轴固定 0~100%)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RateTrendChart(
    points: List<HabitTrendPoint>,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val labelEvery = if (points.size > 10) 5 else 1

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
        ) {
            val w = size.width
            val h = size.height
            val step = if (points.size > 1) w / (points.size - 1) else w

            val coords = points.mapIndexed { index, point ->
                Offset(
                    x = index * step,
                    y = h - point.rate.coerceIn(0f, 1f) * h,
                )
            }

            // 渐变填充区域
            val fillPath = Path().apply {
                moveTo(coords.first().x, h)
                coords.forEach { lineTo(it.x, it.y) }
                lineTo(coords.last().x, h)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primary.copy(alpha = 0.25f), Color.Transparent),
                    startY = 0f,
                    endY = h,
                ),
            )

            // 折线
            val linePath = Path().apply {
                moveTo(coords.first().x, coords.first().y)
                coords.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(
                path = linePath,
                color = primary,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )

            // 数据点
            coords.forEach { center ->
                drawCircle(color = primary, radius = 4.dp.toPx(), center = center)
                drawCircle(color = Color.White, radius = 2.dp.toPx(), center = center)
            }
        }

        // X 轴日期标签(近 30 天每隔 5 个取一个)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            points.forEachIndexed { index, point ->
                val date = LocalDate.ofEpochDay(point.epochDay)
                Text(
                    text = if (index % labelEvery == 0) "${date.monthValue}/${date.dayOfMonth}" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 空态提示
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyChartHint() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.habit_stats_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
