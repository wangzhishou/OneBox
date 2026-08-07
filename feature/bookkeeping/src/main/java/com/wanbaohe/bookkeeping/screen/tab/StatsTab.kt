package com.wanbaohe.bookkeeping.screen.tab

import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.shifenmiao.theme.AppTheme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.ui.widget.charts.CompareBarChart
import com.t8rin.imagetoolbox.core.ui.widget.charts.DonutChartSlice
import com.t8rin.imagetoolbox.core.ui.widget.charts.DonutExpenseChart
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTimeFilterBar
import com.wanbaohe.bookkeeping.R
import com.wanbaohe.bookkeeping.component.BookkeepingComponent
import com.wanbaohe.bookkeeping.model.BookkeepingRecentPreset
import com.wanbaohe.bookkeeping.model.BookkeepingTimeFilter
import com.wanbaohe.bookkeeping.model.CategoryBreakdownUi
import com.wanbaohe.bookkeeping.model.dateRange
import com.wanbaohe.bookkeeping.model.displayLabel
import com.wanbaohe.bookkeeping.model.rememberBookkeepingTimePresets
import com.wanbaohe.bookkeeping.model.selectedPresetKey
import com.wanbaohe.bookkeeping.screen.util.centsText
import com.wanbaohe.bookkeeping.screen.util.centsValueText
import com.shifenmiao.base.ui.picker.ChineseDateRangePickerDialog
import java.time.LocalDate
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAccountWallet
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBarChart
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePieChart
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTrendingDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTrendingUp

private enum class SummaryMetricTone {
    EXPENSE,
    INCOME,
    NEUTRAL,
}

// ─────────────────────────────────────────────────────────────────────────────
// 主题调色板（全部来自 MaterialTheme，随系统 / 动态色自动变化）
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 返回 12 种区分度良好的图表颜色，全部派生自 MaterialTheme.colorScheme，
 * 深色/浅色模式、动态色（Material You）下均能自适应。
 */
@Composable
private fun chartPalette(): List<Color> {
    val s = MaterialTheme.colorScheme
    return remember(s) {
        listOf(
            s.primary,
            s.secondary,
            s.tertiary,
            s.error,
            s.primaryContainer,
            s.secondaryContainer,
            s.tertiaryContainer,
            s.errorContainer,
            s.inversePrimary,
            s.outline,
            s.onPrimaryContainer,
            s.onSecondaryContainer,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 统计 Tab 入口
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun StatsTab(
    component: BookkeepingComponent,
    modifier: Modifier = Modifier,
) {
    val uiState   by component.uiState.collectAsState()
    val palette    = chartPalette()
    val breakdowns = uiState.breakdowns
    val incomeBreakdowns = uiState.incomeBreakdowns
    val timeFilter = uiState.timeFilter
    var showDateRangePicker by remember { mutableStateOf(false) }

    // Expense donut slices
    val donutSlices = remember(breakdowns, palette) {
        breakdowns.map { item ->
            DonutChartSlice(
                value = item.amountCents.toFloat().coerceAtLeast(0f),
                color = palette[item.colorIndex % palette.size],
                label = item.name,
            )
        }
    }

    // Income donut slices
    val incomeDonutSlices = remember(incomeBreakdowns, palette) {
        incomeBreakdowns.map { item ->
            DonutChartSlice(
                value = item.amountCents.toFloat().coerceAtLeast(0f),
                color = palette[item.colorIndex % palette.size],
                label = item.name,
            )
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── 0. 时间筛选条 ──────────────────────────────────────────────
        item {
            EnhancedTimeFilterBar(
                customRangeLabel = timeFilter.dateRange().displayLabel(),
                isCustomRangeSelected = timeFilter is BookkeepingTimeFilter.Custom,
                onCustomRangeClick = { showDateRangePicker = true },
                presets = rememberBookkeepingTimePresets(),
                selectedPresetKey = timeFilter.selectedPresetKey,
                onPresetSelected = { preset ->
                    component.setTimeFilter(
                        BookkeepingTimeFilter.Recent(
                            BookkeepingRecentPreset.valueOf(preset.key)
                        )
                    )
                },
            )
        }

        // ── 1. 区间收支总览 ───────────────────────────────────────────
        item {
            MonthlySummaryCard(
                expenseCents = uiState.summary.expenseCents,
                incomeCents  = uiState.summary.incomeCents,
            )
        }

        // ── 2. 支出构成（甜甜圈 + 图例） ─────────────────────────────────
        item {
            StatsSectionCard(
                title = stringResource(R.string.bookkeeping_stats_category),
                icon  = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePieChart,
            ) {
                if (donutSlices.isEmpty()) {
                    EmptyChartHint()
                } else {
                    Box(
                        modifier         = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        DonutExpenseChart(
                            slices      = donutSlices,
                            centerText  = centsText(uiState.summary.expenseCents),
                            chartSize   = 200.dp,
                            strokeWidth = 36.dp,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    DonutLegend(breakdowns = breakdowns, palette = palette)
                }
            }
        }

        // ── 3. 收入构成（甜甜圈 + 图例） ─────────────────────────────────
        item {
            StatsSectionCard(
                title = stringResource(R.string.bookkeeping_income_composition),
                icon  = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePieChart,
            ) {
                if (incomeDonutSlices.isEmpty()) {
                    EmptyChartHint(text = stringResource(R.string.bookkeeping_empty_income_chart))
                } else {
                    Box(
                        modifier         = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        DonutExpenseChart(
                            slices      = incomeDonutSlices,
                            centerText  = centsText(uiState.summary.incomeCents),
                            chartSize   = 200.dp,
                            strokeWidth = 36.dp,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    DonutLegend(breakdowns = incomeBreakdowns, palette = palette)
                }
            }
        }

        // ── 4. 分类排行 ───────────────────────────────────────────────────
        if (breakdowns.isNotEmpty()) {
            item {
                StatsSectionCard(
                    title = stringResource(R.string.bookkeeping_category_rank),
                    icon  = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTrendingDown,
                ) {
                    breakdowns.forEachIndexed { index, item ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 6.dp),
                                color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            )
                        }
                        RankRow(
                            rank    = index + 1,
                            item    = item,
                            color   = palette[item.colorIndex % palette.size],
                        )
                    }
                }
            }
        }

        // ── 5. 每日支出 ───────────────────────────────────────────────────
        if (uiState.dailyBars.isNotEmpty()) {
            item {
                StatsSectionCard(
                    title = stringResource(R.string.bookkeeping_stats_daily),
                    icon  = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBarChart,
                ) {
                    CompareBarChart(
                        entries  = uiState.dailyBars,
                        barColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    if (showDateRangePicker) {
        val currentRange = timeFilter.dateRange()
        ChineseDateRangePickerDialog(
            initialStartDate = currentRange.startDate,
            initialEndDate = currentRange.endDate,
            maxDate = LocalDate.now(),
            onDateRangeSelected = { start, end ->
                component.setTimeFilter(BookkeepingTimeFilter.Custom(startDate = start, endDate = end))
                showDateRangePicker = false
            },
            onDismiss = { showDateRangePicker = false },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 区间收支总览卡片
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MonthlySummaryCard(
    expenseCents: Long,
    incomeCents: Long,
) {
    val netCents = incomeCents - expenseCents

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryMetricCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.bookkeeping_total_expense),
                value = centsText(expenseCents),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTrendingDown,
                tone = SummaryMetricTone.EXPENSE,
            )
            SummaryMetricCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.bookkeeping_total_income),
                value = centsText(incomeCents),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTrendingUp,
                tone = SummaryMetricTone.INCOME,
            )
        }

        val balanceStr = (if (netCents < 0) "-" else "") + centsValueText(kotlin.math.abs(netCents))
        SummaryMetricCard(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.bookkeeping_current_balance),
            value = "¥$balanceStr",
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAccountWallet,
            tone = SummaryMetricTone.NEUTRAL,
            compact = true,
        )
    }
}

@Composable
private fun SummaryMetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon:  ImageVector,
    tone: SummaryMetricTone,
    compact: Boolean = false,
) {
    val containerColor = when (tone) {
        SummaryMetricTone.EXPENSE -> AppTheme.colors.getPrimaryColor()
        SummaryMetricTone.INCOME -> MaterialTheme.colorScheme.primaryContainer
        SummaryMetricTone.NEUTRAL -> MaterialTheme.colorScheme.surface
    }
    val contentColor = when (tone) {
        SummaryMetricTone.EXPENSE -> AppTheme.colors.getOnPrimaryColor()
        SummaryMetricTone.INCOME -> MaterialTheme.colorScheme.onPrimaryContainer
        SummaryMetricTone.NEUTRAL -> MaterialTheme.colorScheme.onSurface
    }
    val secondaryColor = when (tone) {
        SummaryMetricTone.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> contentColor.copy(alpha = 0.92f)
    }
    val iconContainerColor = when (tone) {
        SummaryMetricTone.EXPENSE -> AppTheme.colors.getOnPrimaryColor().copy(alpha = 0.18f)
        SummaryMetricTone.INCOME -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.14f)
        SummaryMetricTone.NEUTRAL -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
    }
    val iconTint = when (tone) {
        SummaryMetricTone.EXPENSE -> AppTheme.colors.getOnPrimaryColor()
        SummaryMetricTone.INCOME -> MaterialTheme.colorScheme.onPrimaryContainer
        SummaryMetricTone.NEUTRAL -> AppTheme.colors.getPrimaryColor()
    }

    GlassCard(
        modifier = modifier.height(if (compact) 108.dp else 132.dp),
        shape = RoundedCornerShape(24.dp),
        borderWidth = 0.dp,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        containerAlpha = when (tone) {
            SummaryMetricTone.NEUTRAL -> 0.94f
            SummaryMetricTone.INCOME -> 0.98f
            SummaryMetricTone.EXPENSE -> 0.96f
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = secondaryColor,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = value,
                    style = if (compact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor,
                )
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(iconContainerColor)
                    .padding(10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 通用区块卡片容器
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatsSectionCard(
    title:   String,
    icon:    ImageVector,
    content: @Composable () -> Unit,
) {
    GlassCard(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        borderWidth = 0.dp,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        containerAlpha = 0.92f,
    ) {
        Column(
            modifier              = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement   = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 甜甜圈图例
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DonutLegend(
    breakdowns: List<CategoryBreakdownUi>,
    palette:    List<Color>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        breakdowns.forEach { item ->
            val color = palette[item.colorIndex % palette.size]
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                Text(item.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Text(
                    text       = centsText(item.amountCents),
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text      = "${(item.progress * 100).toInt()}%",
                    style     = MaterialTheme.typography.labelSmall,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier  = Modifier.width(34.dp),
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 分类排行行
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RankRow(
    rank:  Int,
    item:  CategoryBreakdownUi,
    color: Color,
) {
    val animatedProgress by animateFloatAsState(
        targetValue   = item.progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label         = "progress_$rank",
    )

    // 奖牌颜色全部来自主题，不再写死
    val (medalBg, medalFg) = when (rank) {
        1    -> MaterialTheme.colorScheme.primary        to MaterialTheme.colorScheme.onPrimary
        2    -> MaterialTheme.colorScheme.secondary      to MaterialTheme.colorScheme.onSecondary
        3    -> MaterialTheme.colorScheme.tertiary       to MaterialTheme.colorScheme.onTertiary
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // 排名气泡
            Box(
                modifier         = Modifier.size(24.dp).clip(CircleShape).background(medalBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = "$rank",
                    style      = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color      = medalFg,
                    fontWeight = FontWeight.Bold,
                )
            }
            // 颜色点
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            // 分类名
            Text(item.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            // 金额
            Text(centsText(item.amountCents), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            // 百分比
            Text(
                text      = "${(item.progress * 100).toInt()}%",
                style     = MaterialTheme.typography.labelSmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier  = Modifier.width(34.dp),
                textAlign = TextAlign.End,
            )
        }
        // 进度条（主题色 + 动画）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(99.dp))
                    .background(color),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 空态提示
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyChartHint(text: String = stringResource(R.string.bookkeeping_empty_expense_chart)) {
    Box(
        modifier         = Modifier.fillMaxWidth().height(120.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text  = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
