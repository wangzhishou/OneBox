package com.wanbaohe.period.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanbaohe.base.ui.empty.EmptyStateGuide
import com.wanbaohe.base.ui.empty.EmptyStateGuideAction
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAiChat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoFix
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBarChart
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronLeft
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGpsFixed
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKeyboardArrowDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKeyboardArrowUp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWaterDrop
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWoman
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedButton
import com.wanbaohe.core.ui.widget.glass.GlassSearchTextField
import com.wanbaohe.core.ui.widget.glass.GlassSegmentedButtonRow
import com.wanbaohe.core.ui.widget.navigation.BottomNavItem
import com.wanbaohe.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.period.R
import com.wanbaohe.period.component.PeriodComponent
import com.wanbaohe.period.model.PeriodColors
import com.wanbaohe.period.model.PeriodFlow
import com.wanbaohe.period.model.PeriodMood
import com.wanbaohe.period.model.PeriodMonthGroup
import com.wanbaohe.period.model.PeriodRecordUi
import com.wanbaohe.period.model.PeriodStatsRange
import com.wanbaohe.period.model.PeriodStatsUi
import com.wanbaohe.period.model.PeriodTab
import com.wanbaohe.period.model.periodColors
import com.wanbaohe.period.screen.component.PeriodMoodFace
import com.wanbaohe.period.screen.component.PeriodStatCard
import com.wanbaohe.period.screen.component.PeriodStatusBadge
import com.wanbaohe.period.screen.component.periodMonthTitle
import com.wanbaohe.period.screen.component.periodShortDate
import com.wanbaohe.period.screen.component.periodWeekday
import com.wanbaohe.period.screen.component.symptomIcon
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
fun PeriodMainScreen(
    component: PeriodComponent,
) {
    val uiState by component.uiState.collectAsState()
    val colors = periodColors()

    BaseScreen(
        title = stringResource(
            if (uiState.selectedTab == PeriodTab.STATS) {
                R.string.period_stats_title
            } else {
                R.string.period_title
            }
        ),
        onGoBack = component.onGoBack,
        actions = {
            IconButton(onClick = component::openAddRecord) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.period_add),
                    tint = colors.accent,
                )
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (uiState.selectedTab == PeriodTab.RECORDS && uiState.records.isNotEmpty()) {
                GlassSearchTextField(
                    value = uiState.searchQuery,
                    onValueChange = component::onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = stringResource(R.string.period_search_hint),
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Box(modifier = Modifier.weight(1f)) {
                when (uiState.selectedTab) {
                    PeriodTab.RECORDS -> {
                        if (uiState.records.isEmpty()) {
                            PeriodEmptyState(
                                colors = colors,
                                onAiAssist = component::navigateToAiAssist,
                                onManual = component::openAddRecord,
                            )
                        } else {
                            PeriodRecordList(
                                colors = colors,
                                groups = uiState.monthGroups,
                                today = uiState.today,
                                calendarMonth = uiState.calendarMonth,
                                calendarPeriodDays = uiState.calendarPeriodDays,
                                calendarRecordIds = uiState.calendarRecordIds,
                                onCalendarMonthChange = component::onCalendarMonthChange,
                                onCalendarDayClick = component::openCalendarDay,
                                onRecordClick = component::openDetail,
                                onToggleCollapse = component::toggleMonthCollapse,
                            )
                        }
                    }

                    PeriodTab.STATS -> {
                        PeriodStatsContent(
                            colors = colors,
                            stats = uiState.stats,
                            range = uiState.statsRange,
                            onRangeChange = component::setStatsRange,
                        )
                    }
                }
            }
            PeriodBottomTabs(
                selected = uiState.selectedTab,
                onSelect = component::switchTab,
            )
        }
    }
}

// ── 空状态 ─────────────────────────────────────────

@Composable
private fun PeriodEmptyState(
    colors: PeriodColors,
    onAiAssist: () -> Unit,
    onManual: () -> Unit,
) {
    EmptyStateGuide(
        icon = Icons.Outlined.LineWoman,
        title = stringResource(R.string.period_empty_title),
        description = stringResource(R.string.period_empty_subtitle),
        actions = listOf(
            EmptyStateGuideAction(
                icon = Icons.Outlined.LineAutoFix,
                title = stringResource(R.string.period_ai_quick_record),
                description = stringResource(R.string.period_empty_example),
                onClick = onAiAssist,
                emphasized = true,
            ),
            EmptyStateGuideAction(
                icon = Icons.Outlined.Add,
                title = stringResource(R.string.period_manual_record),
                description = stringResource(R.string.period_manual_record),
                onClick = onManual,
            ),
        ),
        footerHint = stringResource(R.string.period_empty_example),
    )
}

// ── 记录列表 ───────────────────────────────────────

@Composable
private fun PeriodRecordList(
    colors: PeriodColors,
    groups: List<PeriodMonthGroup>,
    today: LocalDate,
    calendarMonth: YearMonth,
    calendarPeriodDays: Set<LocalDate>,
    calendarRecordIds: Map<LocalDate, String>,
    onCalendarMonthChange: (Long) -> Unit,
    onCalendarDayClick: (LocalDate) -> Unit,
    onRecordClick: (String) -> Unit,
    onToggleCollapse: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "calendar") {
            PeriodCalendarCard(
                colors = colors,
                month = calendarMonth,
                periodDays = calendarPeriodDays,
                recordIds = calendarRecordIds,
                today = today,
                onMonthChange = onCalendarMonthChange,
                onDayClick = onCalendarDayClick,
            )
        }
        groups.forEach { group ->
            item(key = "header_${group.yearMonth}") {
                MonthGroupHeader(
                    colors = colors,
                    group = group,
                    onClick = { onToggleCollapse(group.yearMonth.toString()) },
                )
            }
            if (!group.isCollapsed) {
                items(group.records, key = { it.id }) { record ->
                    PeriodRecordCard(
                        colors = colors,
                        record = record,
                        isToday = record.recordDate == today,
                        onClick = { onRecordClick(record.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthGroupHeader(
    colors: PeriodColors,
    group: PeriodMonthGroup,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = group.yearMonth.periodMonthTitle(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.period_record_count, group.records.size),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
        )
        Icon(
            imageVector = if (group.isCollapsed) {
                Icons.Outlined.LineKeyboardArrowDown
            } else {
                Icons.Outlined.LineKeyboardArrowUp
            },
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier
                .padding(start = 4.dp)
                .size(26.dp),
        )
    }
}

// ── 月历 ─────────────────────────────────────────

@Composable
private fun PeriodCalendarCard(
    colors: PeriodColors,
    month: YearMonth,
    periodDays: Set<LocalDate>,
    recordIds: Map<LocalDate, String>,
    today: LocalDate,
    onMonthChange: (Long) -> Unit,
    onDayClick: (LocalDate) -> Unit,
) {
    val locale = Locale.getDefault()
    val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onMonthChange(-1) }) {
                    Icon(
                        imageVector = Icons.Outlined.LineChevronLeft,
                        contentDescription = stringResource(R.string.period_calendar_prev),
                        tint = colors.onSurfaceVariant,
                    )
                }
                Text(
                    text = month.periodMonthTitle(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { onMonthChange(1) }) {
                    Icon(
                        imageVector = Icons.Outlined.LineChevronRight,
                        contentDescription = stringResource(R.string.period_calendar_next),
                        tint = colors.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                (0..6).forEach { offset ->
                    Text(
                        text = firstDayOfWeek.plus(offset.toLong())
                            .getDisplayName(TextStyle.SHORT, locale),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            val leadingBlanks =
                (month.atDay(1).dayOfWeek.value - firstDayOfWeek.value + 7) % 7
            val daysInMonth = month.lengthOfMonth()
            val rowCount = (leadingBlanks + daysInMonth + 6) / 7
            Column(modifier = Modifier.fillMaxWidth()) {
                for (row in 0 until rowCount) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0..6) {
                            val dayNumber = row * 7 + col - leadingBlanks + 1
                            if (dayNumber in 1..daysInMonth) {
                                val date = month.atDay(dayNumber)
                                CalendarDayCell(
                                    date = date,
                                    isPeriodDay = date in periodDays,
                                    isToday = date == today,
                                    hasRecord = recordIds.containsKey(date),
                                    colors = colors,
                                    onClick = { onDayClick(date) },
                                )
                            } else {
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.CalendarDayCell(
    date: LocalDate,
    isPeriodDay: Boolean,
    isToday: Boolean,
    hasRecord: Boolean,
    colors: PeriodColors,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(if (isPeriodDay) colors.accentSoft else Color.Transparent)
            .then(
                if (isToday) Modifier.border(1.dp, colors.accent, CircleShape) else Modifier
            )
            .clickable(enabled = hasRecord, onClick = onClick),
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isPeriodDay || isToday) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isPeriodDay || isToday) colors.accent else colors.onSurface,
        )
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(if (hasRecord) colors.accent else Color.Transparent),
        )
    }
}

@Composable
private fun PeriodRecordCard(
    colors: PeriodColors,
    record: PeriodRecordUi,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isToday) colors.accent else colors.accentSoft),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = record.recordDate.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isToday) colors.onAccent else colors.onSurface,
                    )
                    Text(
                        text = record.recordDate.periodWeekday(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isToday) colors.onAccent else colors.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.cycleDay?.let {
                        stringResource(R.string.period_cycle_day, it)
                    } ?: stringResource(R.string.period_cycle_day_unknown),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                )
                Text(
                    text = record.recordDate.periodShortDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                PeriodStatusBadge(status = record.status, colors = colors)
                Spacer(modifier = Modifier.height(6.dp))
                RecordIconRow(record = record, colors = colors)
            }
        }
    }
}

@Composable
private fun RecordIconRow(
    record: PeriodRecordUi,
    colors: PeriodColors,
) {
    val tint = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (record.flowIntensity != PeriodFlow.NONE) {
            Icon(
                imageVector = Icons.Outlined.LineWaterDrop,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(16.dp),
            )
        }
        record.symptoms.take(3).forEach { symptom ->
            Icon(
                imageVector = symptomIcon(symptom),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
        }
        if (record.mood != PeriodMood.NORMAL) {
            PeriodMoodFace(
                mood = record.mood,
                color = tint,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

// ── 周期统计 ───────────────────────────────────────

@Composable
private fun PeriodStatsContent(
    colors: PeriodColors,
    stats: PeriodStatsUi,
    range: PeriodStatsRange,
    onRangeChange: (PeriodStatsRange) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            val rangeOptions = listOf(
                PeriodStatsRange.SIX_MONTHS to stringResource(R.string.period_stats_range_6m),
                PeriodStatsRange.DAY to stringResource(R.string.period_stats_range_day),
                PeriodStatsRange.WEEK to stringResource(R.string.period_stats_range_week),
                PeriodStatsRange.MONTH to stringResource(R.string.period_stats_range_month),
            )
            val rangeLabelStyle = MaterialTheme.typography.labelLarge
            GlassSegmentedButtonRow(
                options = rangeOptions,
                selectedOption = rangeOptions.first { it.first == range },
                onOptionSelected = { onRangeChange(it.first) },
                modifier = Modifier.fillMaxWidth(),
                label = { option ->
                    // 四段等宽,长文案折行会把整行撑高且左对齐。单行居中,放不下再缩小字号。
                    Text(
                        text = option.second,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        style = rangeLabelStyle,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = 11.sp,
                            maxFontSize = rangeLabelStyle.fontSize,
                            stepSize = 0.5.sp,
                        ),
                    )
                },
                selectedColor = colors.accent,
                selectedContentColor = colors.onAccent,
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PeriodStatCard(
                    icon = Icons.Outlined.LineCalendar,
                    title = stringResource(R.string.period_stats_avg_cycle),
                    value = "${stats.averageCycleDays}",
                    unit = stringResource(R.string.period_day_unit),
                    subtitle = stats.cycleDelta?.let {
                        stringResource(R.string.period_stats_vs_prev, it)
                    } ?: stringResource(R.string.period_stats_vs_prev_na),
                    colors = colors,
                    modifier = Modifier.weight(1f),
                )
                PeriodStatCard(
                    icon = Icons.Outlined.LineWaterDrop,
                    title = stringResource(R.string.period_stats_avg_period),
                    value = "${stats.averagePeriodDays}",
                    unit = stringResource(R.string.period_day_unit),
                    subtitle = stats.periodDelta?.let {
                        stringResource(R.string.period_stats_vs_prev, it)
                    } ?: stringResource(R.string.period_stats_vs_prev_na),
                    colors = colors,
                    modifier = Modifier.weight(1f),
                )
                PeriodStatCard(
                    icon = Icons.Outlined.LineGpsFixed,
                    title = stringResource(R.string.period_stats_next),
                    value = stats.nextPeriodStart?.periodShortDate()
                        ?: stringResource(R.string.period_stats_vs_prev_na),
                    unit = "",
                    subtitle = stats.nextPeriodStart?.periodWeekday(TextStyle.FULL)
                        ?: stringResource(R.string.period_stats_vs_prev_na),
                    colors = colors,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.period_stats_trend_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CycleTrendChart(
                        points = stats.cycleTrend,
                        averageDays = stats.averageCycleDays,
                        colors = colors,
                    )
                }
            }
        }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.period_stats_symptom_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SymptomDonutChart(
                        counts = stats.symptomCounts,
                        total = stats.symptomTotal,
                        colors = colors,
                    )
                }
            }
        }
    }
}

// ── 底部 tab ───────────────────────────────────────

@Composable
private fun PeriodBottomTabs(
    selected: PeriodTab,
    onSelect: (PeriodTab) -> Unit,
) {
    val recordsId = "records"
    val statsId = "stats"
    BottomNavigationBar(
        items = listOf(
            BottomNavItem(
                id = recordsId,
                label = stringResource(R.string.period_tab_records),
                icon = Icons.Outlined.LineCalendar,
            ),
            BottomNavItem(
                id = statsId,
                label = stringResource(R.string.period_tab_stats),
                icon = Icons.Outlined.LineBarChart,
            ),
        ),
        selectedItemId = if (selected == PeriodTab.RECORDS) recordsId else statsId,
        onItemClick = { item ->
            onSelect(if (item.id == recordsId) PeriodTab.RECORDS else PeriodTab.STATS)
        },
    )
}
