package com.wanbaohe.recordcenter.screen.tab

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.components.sectionGradient
import com.shifenmiao.common.components.sectionIconColor
import com.shifenmiao.common.components.sectionIconContainerColor
import com.shifenmiao.common.components.sectionThemeForIndex
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAreaChart
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTrendingDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTrendingUp
import com.wanbaohe.core.ui.widget.charts.BarChartEntry
import com.wanbaohe.core.ui.widget.charts.CompareBarChart
import com.wanbaohe.core.ui.widget.charts.LineTrendChart
import com.wanbaohe.core.ui.widget.charts.TrendSeries
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.tintedGlassContentColor
import com.wanbaohe.recordcenter.R
import com.wanbaohe.recordcenter.model.RecordTrendSummary
import com.wanbaohe.recordcenter.registry.TrendChartKind
import com.wanbaohe.recordcenter.screen.util.formatChartDate
import com.wanbaohe.recordcenter.screen.util.formatRecordValueParts
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 趋势 tab:每种记录类型一张趋势卡片 —— 最新值 + 较上周增量 + 近 7 天图表。
 * 图表复用 core/ui 的 LineTrendChart / CompareBarChart,卡片配色与记录 tab 一致。
 */
@Composable
fun TrendsTab(
    summaries: List<RecordTrendSummary>,
    gridState: LazyGridState,
    onTypeClick: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(
            items = summaries,
            key = { _, summary -> summary.definition.key },
        ) { index, summary ->
            TrendCard(
                themeIndex = index,
                summary = summary,
                onClick = { onTypeClick(summary.definition.key) },
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun TrendCard(
    themeIndex: Int,
    summary: RecordTrendSummary,
    onClick: () -> Unit,
) {
    val definition = summary.definition
    val theme = sectionThemeForIndex(themeIndex)
    val containerColor = sectionGradient(theme)
    val contentColor = tintedGlassContentColor(containerColor)
    val iconBgColor = sectionIconContainerColor(theme)
    val accentColor = sectionIconColor(theme)

    val chartUnit = remember(definition) {
        definition.chartFieldKeys.firstOrNull()
            ?.let { key -> definition.fields.firstOrNull { it.key == key } }
            ?.unit
            ?.takeIf { it.isNotEmpty() }
    }
    val valueParts = remember(summary.latest) {
        summary.latest?.let { formatRecordValueParts(definition, it.fieldsJson) }
    }

    GlassCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(TREND_CARD_HEIGHT),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
        ) {
            // 头部:图标 + 名称/单位 | 最新值 + 较上周
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = definition.icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = stringResource(definition.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (chartUnit != null) {
                        Text(
                            text = chartUnit,
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.7f),
                            maxLines = 1,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (valueParts != null && valueParts.first.isNotEmpty()) {
                Text(
                    text = valueParts.first,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                WeekDeltaRow(
                    summary = summary,
                    contentColor = contentColor,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (summary.hasRecentData) {
                TrendCardChart(
                    summary = summary,
                    accentColor = accentColor,
                )
            } else {
                // 空状态:近 7 天无记录
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TREND_CHART_HEIGHT),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAreaChart,
                        contentDescription = null,
                        tint = contentColor.copy(alpha = 0.4f),
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.record_center_trend_empty),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}

/** 较上周增量行:多图表字段按定义顺序拼接(如血压 "-3/-2") */
@Composable
private fun WeekDeltaRow(
    summary: RecordTrendSummary,
    contentColor: Color,
) {
    val deltas = summary.definition.chartFieldKeys
        .map { summary.weekDeltas[it] }
    if (deltas.all { it == null }) return
    val first = deltas.filterNotNull().first()
    val decreased = first < 0f
    val deltaColor = if (decreased) TrendDownColor else MaterialTheme.colorScheme.onErrorContainer

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector = if (decreased) {
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTrendingDown
            } else {
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTrendingUp
            },
            contentDescription = null,
            tint = deltaColor,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = deltas.joinToString("/") { it?.let(::formatSignedDelta) ?: "--" },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = deltaColor,
            maxLines = 1,
        )
        Text(
            text = stringResource(R.string.record_center_vs_last_week),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.7f),
            maxLines = 1,
        )
    }
}

@Composable
private fun TrendCardChart(
    summary: RecordTrendSummary,
    accentColor: Color,
) {
    val definition = summary.definition
    val xLabels = remember(summary) { summary.dayStarts.map(::formatChartDate) }
    val seriesColors = listOf(
        accentColor,
        MaterialTheme.colorScheme.onTertiaryContainer,
        MaterialTheme.colorScheme.onErrorContainer,
    )

    when (definition.trendChartKind) {
        TrendChartKind.LINE -> {
            val series = definition.chartFieldKeys.mapIndexed { index, fieldKey ->
                TrendSeries(
                    label = stringResource(
                        definition.fields.first { it.key == fieldKey }.labelRes
                    ),
                    color = seriesColors[index % seriesColors.size],
                    points = summary.dailyPoints[fieldKey].orEmpty(),
                )
            }
            LineTrendChart(
                series = series,
                xLabels = xLabels,
                modifier = Modifier.fillMaxWidth(),
                chartHeight = TREND_CHART_HEIGHT,
                showFill = false,
                maxXLabels = 4,
            )
        }
        TrendChartKind.BAR -> {
            val fieldKey = definition.chartFieldKeys.first()
            val entries = remember(summary) {
                summary.dailyPoints[fieldKey].orEmpty().mapIndexed { index, value ->
                    BarChartEntry(
                        // 半宽卡片上 7 个日期标签会挤在一起,隔一个显示
                        label = if (index % 2 == 0) xLabels.getOrElse(index) { "" } else "",
                        value = value ?: 0f,
                    )
                }
            }
            CompareBarChart(
                entries = entries,
                modifier = Modifier.fillMaxWidth(),
                chartHeight = TREND_CHART_HEIGHT,
                barColor = accentColor,
            )
        }
    }
}

/** 增量格式化:保留正负号,按 1 位小数舍入(+0.5 / -3 / 0) */
private fun formatSignedDelta(delta: Float): String =
    (if (delta > 0f) "+" else "") +
        BigDecimal(delta.toString()).setScale(1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()

/** 下降(好转/回落)语义色:设计稿的绿色箭头 */
private val TrendDownColor = Color(0xFF34A853)

private val TREND_CARD_HEIGHT = 218.dp
private val TREND_CHART_HEIGHT = 88.dp
