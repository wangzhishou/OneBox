package com.wanbaohe.householditems.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAddCircleOutline
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBarChart
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendarAlert
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLocationOn
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePieChart
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStorage
import com.t8rin.imagetoolbox.core.ui.widget.charts.DonutChartSlice
import com.t8rin.imagetoolbox.core.ui.widget.charts.DonutExpenseChart
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.wanbaohe.householditems.R
import com.wanbaohe.householditems.component.HouseholdItemsComponent
import com.wanbaohe.householditems.model.CategoryCountUi
import com.wanbaohe.householditems.model.HouseholdItemUi
import com.wanbaohe.householditems.model.LocationCountUi
import com.wanbaohe.householditems.model.categoryIconOrNull
import com.wanbaohe.householditems.model.locationIcon
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// 统计 tab:三统计卡 + 分类占比环形图 + 位置分布条形图 + 动态摘要
// 图表绘制直接复用 core/ui charts(DonutExpenseChart,与记账统计页同源),
// 区块卡片/图例/条形行样式照搬 bookkeeping StatsTab 的实现
// ─────────────────────────────────────────────────────────────────────────────

/** 图表调色板:全部派生自 MaterialTheme,深浅色/动态色自适应(同记账 StatsTab) */
@Composable
private fun householdChartPalette(): List<Color> {
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

/** 图例最多展示的分类数,其余并入"其他" */
private const val MAX_LEGEND_ENTRIES = 5

@Composable
fun HouseholdStatsTab(
    component: HouseholdItemsComponent,
    modifier: Modifier = Modifier,
) {
    val uiState by component.uiState.collectAsState()
    val stats = uiState.stats
    val palette = householdChartPalette()

    // 分类占比:top N + 其他合并
    val otherLabel = stringResource(R.string.household_stats_other)
    val uncategorizedLabel = stringResource(R.string.household_uncategorized)
    val legendEntries = remember(stats.categorySlices, otherLabel, uncategorizedLabel) {
        val named = stats.categorySlices.map {
            it.copy(name = it.name.ifBlank { uncategorizedLabel })
        }
        if (named.size <= MAX_LEGEND_ENTRIES) {
            named
        } else {
            val top = named.take(MAX_LEGEND_ENTRIES)
            val restCount = named.drop(MAX_LEGEND_ENTRIES).sumOf { it.count }
            top + CategoryCountUi(
                name = otherLabel,
                count = restCount,
                colorIndex = MAX_LEGEND_ENTRIES,
            )
        }
    }
    val donutSlices = remember(legendEntries, palette) {
        legendEntries.map { entry ->
            DonutChartSlice(
                value = entry.count.toFloat(),
                color = palette[entry.colorIndex % palette.size],
                label = entry.name,
            )
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── 顶部三统计卡 ──
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatsMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.LineStorage,
                    label = stringResource(R.string.household_stats_total_items),
                    value = stats.totalItems.toString(),
                    unit = stringResource(R.string.household_stats_unit_item),
                    // "较上月"只展示能真实计算的:本月新增(按 createdAt)
                    trend = if (stats.addedThisMonth > 0) {
                        stringResource(R.string.household_stats_added_this_month, stats.addedThisMonth)
                    } else {
                        null
                    },
                )
                StatsMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.LineLocationOn,
                    label = stringResource(R.string.household_stats_location_count),
                    value = stats.locationCount.toString(),
                    unit = stringResource(R.string.household_stats_unit_location),
                    trend = null,
                )
                StatsMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.LineCalendarAlert,
                    label = stringResource(R.string.household_stats_expiring_soon),
                    value = stats.expiringSoonCount.toString(),
                    unit = stringResource(R.string.household_stats_unit_item),
                    trend = null,
                    highlight = stats.expiringSoonCount > 0,
                )
            }
        }

        // ── 按物品分类占比(环形图 + 图例) ──
        item {
            StatsSectionCard(
                title = stringResource(R.string.household_stats_category_share),
                icon = Icons.Outlined.LinePieChart,
            ) {
                if (donutSlices.isEmpty()) {
                    StatsEmptyHint()
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        DonutExpenseChart(
                            slices = donutSlices,
                            centerText = stringResource(
                                R.string.household_stats_total_count,
                                stats.totalItems,
                            ),
                            chartSize = 180.dp,
                            strokeWidth = 32.dp,
                        )
                    }
                    Column(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        legendEntries.forEach { entry ->
                            CategoryLegendRow(
                                entry = entry,
                                total = stats.totalItems,
                                color = palette[entry.colorIndex % palette.size],
                            )
                        }
                    }
                }
            }
        }

        // ── 按位置分布(横向条形) ──
        item {
            StatsSectionCard(
                title = stringResource(R.string.household_stats_location_distribution),
                icon = Icons.Outlined.LineBarChart,
            ) {
                if (stats.locationBars.isEmpty() || stats.totalItems == 0) {
                    StatsEmptyHint()
                } else {
                    val maxCount = stats.locationBars.maxOf { it.count }.coerceAtLeast(1)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        stats.locationBars.forEachIndexed { index, bar ->
                            LocationBarRow(
                                bar = bar,
                                maxCount = maxCount,
                                color = palette[index % palette.size],
                            )
                        }
                    }
                }
            }
        }

        // ── 动态摘要(最近新增 + 即将过期) ──
        item {
            StatsSectionCard(
                title = stringResource(R.string.household_stats_activity),
                icon = Icons.Outlined.LineAddCircleOutline,
            ) {
                if (stats.recentAdded.isEmpty() && stats.upcomingExpiry.isEmpty()) {
                    StatsEmptyHint()
                } else {
                    ActivityGroup(
                        title = stringResource(R.string.household_stats_recent_added),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    ) {
                        stats.recentAdded.forEach { item ->
                            RecentAddedRow(item = item, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    if (stats.upcomingExpiry.isNotEmpty()) {
                        ActivityGroup(
                            title = stringResource(R.string.household_stats_upcoming_expiry),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(top = 16.dp),
                        ) {
                            stats.upcomingExpiry.forEach { item ->
                                UpcomingExpiryRow(item = item, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                        }
                    }
                }
            }
        }

        item { Box(modifier = Modifier.height(80.dp)) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 统计卡
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatsMetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    trend: String?,
    highlight: Boolean = false,
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        borderWidth = 0.dp,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        style = GlassStyle.None,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .glassBackground(
                        style = if (highlight) GlassStyle.Medium else GlassStyle.Regular,
                        shape = CircleShape,
                        color = if (highlight) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        borderWidth = 0.dp,
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (highlight) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    },
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = " $unit",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp),
                )
            }
            Text(
                text = trend.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                minLines = 1,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 通用区块卡片容器(同记账 StatsSectionCard)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        borderWidth = 0.dp,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        style = GlassStyle.None,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp),
                )
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 分类图例行 / 位置条形行
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CategoryLegendRow(
    entry: CategoryCountUi,
    total: Int,
    color: Color,
) {
    val percent = if (total > 0) entry.count * 100 / total else 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(
            text = entry.name,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(R.string.household_stats_items_count, entry.count),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "$percent%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(34.dp),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun LocationBarRow(
    bar: LocationCountUi,
    maxCount: Int,
    color: Color,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (bar.count.toFloat() / maxCount).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "location_bar_${bar.locationId}",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = bar.locationId?.let { locationIcon(bar.iconKey, it) }
                ?: Icons.Outlined.LineLocationOn,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = bar.name.ifBlank { stringResource(R.string.household_no_location) },
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(64.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(99.dp))
                    .background(color),
            )
        }
        Text(
            text = bar.count.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(30.dp),
            textAlign = TextAlign.End,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 动态摘要:左侧竖直时间线(分组圆形描边图标 + 条目圆点串在线上)
// 最近新增 = primary 系,即将过期 = tertiary 系;不硬编码 hex
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ActivityGroup(
    title: String,
    tint: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        // 分组标题:纯文字,与时间线条目内容(26dp gutter 之后)左对齐,只用空白分隔
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = tint,
            modifier = Modifier.padding(start = 26.dp),
        )
        Column(
            modifier = Modifier.padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun RecentAddedRow(item: HouseholdItemUi, tint: Color) {
    val formatter = remember {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withLocale(Locale.getDefault())
    }
    val timeText = remember(item.createdAt) {
        Instant.ofEpochMilli(item.createdAt).atZone(ZoneId.systemDefault()).format(formatter)
    }
    TimelineEntryRow(
        item = item,
        tint = tint,
        subtitle = AnnotatedString(
            listOf(item.locationPath, timeText)
                .filter { it.isNotBlank() }
                .joinToString("  |  "),
        ),
    )
}

@Composable
private fun UpcomingExpiryRow(item: HouseholdItemUi, tint: Color) {
    val days = item.daysToExpire ?: return
    val dateFormatter = remember {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
    }
    val dateText = item.expireDate?.format(dateFormatter).orEmpty()
    val prefix = stringResource(R.string.household_stats_expire_prefix, dateText)
    val suffix = stringResource(R.string.household_stats_expire_suffix)
    // 倒计时文案(剩余 N 天)整段彩色加粗高亮
    val daysText = if (days == 0L) {
        stringResource(R.string.household_expires_today)
    } else {
        stringResource(R.string.household_days_left, days)
    }
    TimelineEntryRow(
        item = item,
        tint = tint,
        subtitle = buildAnnotatedString {
            if (item.locationPath.isNotBlank()) {
                append(item.locationPath)
                append("  |  ")
            }
            append(prefix)
            withStyle(SpanStyle(color = tint, fontWeight = FontWeight.Bold)) {
                append(daysText)
            }
            append(suffix)
        },
    )
}

/** 时间线条目:gutter(竖线 + 圆点)+ 圆角方形图标底 + 名称(加粗)+ 副标题 */
@Composable
private fun TimelineEntryRow(
    item: HouseholdItemUi,
    tint: Color,
    subtitle: AnnotatedString,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 时间线 gutter:竖线贯通行高,圆点居中,相邻行的线自然连成一条
        Box(
            modifier = Modifier
                .width(26.dp)
                .fillMaxHeight(),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(tint),
            )
        }
        // 圆角方形图标底:分类图标(回退首字),颜色跟随分组
        Box(
            modifier = Modifier
                .size(38.dp)
                .glassBackground(
                    style = GlassStyle.Regular,
                    shape = RoundedCornerShape(11.dp),
                    color = tint,
                    borderWidth = 0.dp,
                ),
            contentAlignment = Alignment.Center,
        ) {
            val icon = categoryIconOrNull(item.category)
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                    tint = tint,
                )
            } else {
                Text(
                    text = (item.category.ifBlank { item.name }).take(1),
                    style = MaterialTheme.typography.labelLarge,
                    color = tint,
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun StatsEmptyHint() {
    Box(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.household_stats_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
