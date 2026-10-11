package com.wanbaohe.sudoku.screen

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBarChart
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCheckCircleOutline
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGrid4x4
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMedal
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTimer
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionHeader
import com.wanbaohe.sudoku.R
import com.wanbaohe.sudoku.component.SudokuComponent
import com.wanbaohe.sudoku.logic.SudokuDifficulty
import com.wanbaohe.sudoku.logic.SudokuRecord
import com.wanbaohe.sudoku.logic.SudokuStats
import kotlin.math.roundToInt

/**
 * 沉淀层: 统计页。4 数据卡 + 胜率环形图 + 最佳时间 + 难度分布柱状图 + 完成趋势折线图。
 * 图表全部 Canvas 自绘; 无数据时整页空态, 不进图表分支。
 */
@Composable
fun SudokuStatsContent(
    component: SudokuComponent,
    bottomInset: Dp = 0.dp
) {
    val state by component.uiState.collectAsState()
    val records = state.records

    BaseScreen(
        modifier = Modifier.padding(bottom = bottomInset),
        title = stringResource(R.string.sudoku_stats_title),
        onGoBack = component::onTabBack
    ) {
        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(OneBoxDesignSystem.screenPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.sudoku_stats_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            return@BaseScreen
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = OneBoxDesignSystem.screenPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing)
        ) {
            Spacer(modifier = Modifier.height(OneBoxDesignSystem.screenTopSpacing))

            OverviewCard(records = records)

            Row(horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing)) {
                WinRateCard(
                    records = records,
                    modifier = Modifier.weight(1f)
                )
                BestTimeCard(
                    records = records,
                    modifier = Modifier.weight(1f)
                )
            }

            DistributionCard(records = records)

            TrendCard(records = records)

            Spacer(modifier = Modifier.height(OneBoxDesignSystem.blockSpacing))
        }
    }
}

// ─── 4 数据卡 ──────────────────────────────────────────────────────────────

@Composable
private fun OverviewCard(records: List<SudokuRecord>) {
    OneBoxSectionCard {
        Row(modifier = Modifier.fillMaxWidth()) {
            OverviewItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.LineGrid4x4,
                iconAccent = MaterialTheme.colorScheme.onPrimaryContainer,
                value = SudokuStats.totalGames(records).toString(),
                label = stringResource(R.string.sudoku_stats_total)
            )
            OverviewItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.LineCheckCircleOutline,
                iconAccent = Color(0xFF43A047),
                value = SudokuStats.completedGames(records).toString(),
                label = stringResource(R.string.sudoku_stats_completed)
            )
            OverviewItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.LineTimer,
                iconAccent = MaterialTheme.colorScheme.tertiary,
                value = SudokuStats.unfinishedGames(records).toString(),
                label = stringResource(R.string.sudoku_stats_unfinished)
            )
            OverviewItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.LineBarChart,
                iconAccent = MaterialTheme.colorScheme.error,
                value = SudokuStats.averageTimeSeconds(records)?.let(::formatSudokuTime)
                    ?: stringResource(R.string.sudoku_time_none),
                label = stringResource(R.string.sudoku_stats_avg_time)
            )
        }
    }
}

@Composable
private fun OverviewItem(
    icon: ImageVector,
    iconAccent: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconAccent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconAccent,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// ─── 胜率环形图 + 最佳时间 ─────────────────────────────────────────────────

@Composable
private fun WinRateCard(
    records: List<SudokuRecord>,
    modifier: Modifier = Modifier
) {
    val rate = SudokuStats.winRate(records)
    val ringColor = MaterialTheme.colorScheme.onPrimaryContainer
    val trackColor = MaterialTheme.colorScheme.surfaceContainer

    OneBoxSectionCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing)
        ) {
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 10.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(
                        (size.width - diameter) / 2,
                        (size.height - diameter) / 2
                    )
                    val arcSize = Size(diameter, diameter)
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth)
                    )
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = 360f * rate,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = stringResource(R.string.sudoku_stats_percent, (rate * 100).roundToInt()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.sudoku_stats_win_rate),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.sudoku_stats_win_rate_sub),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BestTimeCard(
    records: List<SudokuRecord>,
    modifier: Modifier = Modifier
) {
    OneBoxSectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LineMedal,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = stringResource(R.string.sudoku_stats_best_time),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = SudokuStats.bestTimeSeconds(records)?.let(::formatSudokuTime)
                    ?: stringResource(R.string.sudoku_time_none),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.sudoku_stats_best_time_sub),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── 难度分布柱状图 ────────────────────────────────────────────────────────

@Composable
private fun DistributionCard(records: List<SudokuRecord>) {
    val distribution = SudokuStats.completedByDifficulty(records)
    val difficulties = SudokuDifficulty.entries
    val barColor = MaterialTheme.colorScheme.onPrimaryContainer
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    OneBoxSectionCard {
        OneBoxSectionHeader(
            title = stringResource(R.string.sudoku_stats_distribution),
            supporting = stringResource(R.string.sudoku_stats_distribution_sub)
        )
        Spacer(modifier = Modifier.height(OneBoxDesignSystem.itemSpacing))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val maxCount = maxOf(difficulties.maxOf { distribution[it] ?: 0 }, 1)
            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = labelColor.toArgb()
                textSize = 11.sp.toPx()
                textAlign = Paint.Align.CENTER
            }
            val slotWidth = size.width / difficulties.size
            val barWidth = slotWidth * 0.45f
            val valueLabelHeight = 16.sp.toPx()
            val maxBarHeight = size.height - valueLabelHeight

            difficulties.forEachIndexed { index, difficulty ->
                val count = distribution[difficulty] ?: 0
                // 0 也画一条小底条, 不然该难度整根消失像漏画了
                val barHeight = maxBarHeight * (count.toFloat() / maxCount)
                val left = slotWidth * index + (slotWidth - barWidth) / 2
                val top = size.height - barHeight.coerceAtLeast(4.dp.toPx())
                drawRoundRect(
                    color = barColor.copy(alpha = if (count == 0) 0.25f else 0.85f),
                    topLeft = Offset(left, top),
                    size = Size(barWidth, size.height - top),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
                drawContext.canvas.nativeCanvas.drawText(
                    count.toString(),
                    left + barWidth / 2,
                    top - 6.dp.toPx(),
                    labelPaint
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            difficulties.forEach { difficulty ->
                Text(
                    text = difficultyLabel(difficulty),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ─── 完成趋势折线图 ────────────────────────────────────────────────────────

private val TREND_DAY_OPTIONS = listOf(7, 30, 90)

@Composable
private fun TrendCard(records: List<SudokuRecord>) {
    var selectedDays by remember { mutableIntStateOf(TREND_DAY_OPTIONS.first()) }
    val daily = remember(records, selectedDays) {
        SudokuStats.dailyCompletions(records, selectedDays, System.currentTimeMillis())
    }
    val lineColor = MaterialTheme.colorScheme.onPrimaryContainer

    OneBoxSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                OneBoxSectionHeader(
                    title = stringResource(R.string.sudoku_stats_trend),
                    supporting = stringResource(R.string.sudoku_stats_trend_sub, selectedDays)
                )
            }
            TrendSegmented(
                options = TREND_DAY_OPTIONS,
                selected = selectedDays,
                onSelect = { selectedDays = it }
            )
        }
        Spacer(modifier = Modifier.height(OneBoxDesignSystem.itemSpacing))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val maxCount = maxOf(daily.max(), 1)
            val verticalPadding = 12.dp.toPx()
            val usableHeight = size.height - verticalPadding * 2
            val stepX = if (daily.size > 1) size.width / (daily.size - 1) else 0f
            val points = daily.mapIndexed { index, count ->
                Offset(
                    x = stepX * index,
                    y = verticalPadding + usableHeight * (1f - count.toFloat() / maxCount)
                )
            }
            if (points.isEmpty()) return@Canvas

            val linePath = Path().apply {
                points.forEachIndexed { index, point ->
                    if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
                }
            }
            // 线下面积淡淡铺一层渐变, 全 0 时就是贴底的一条平线
            val areaPath = Path().apply {
                addPath(linePath)
                lineTo(points.last().x, size.height)
                lineTo(points.first().x, size.height)
                close()
            }
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.25f),
                        lineColor.copy(alpha = 0.02f)
                    )
                )
            )
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
            // 点太密(>31 天)就只画线不画点
            if (points.size <= 31) {
                points.forEach { point ->
                    drawCircle(
                        color = lineColor,
                        radius = 3.dp.toPx(),
                        center = point
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendSegmented(
    options: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(OneBoxDesignSystem.pillRadius))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.forEach { days ->
            val isSelected = days == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(OneBoxDesignSystem.pillRadius))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    )
                    .clickable { onSelect(days) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(R.string.sudoku_stats_days, days),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
