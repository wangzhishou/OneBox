package com.wanbaohe.blessingwall.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.base.ui.calendar.ChineseWeekdayHeader
import com.shifenmiao.base.ui.calendar.MonthCalendarGrid
import com.shifenmiao.base.ui.picker.ChineseYearMonthPickerDialog
import com.shifenmiao.model.remote.BlessingWallTabText
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.blessingwall.R
import com.wanbaohe.blessingwall.component.BlessingRecordComponent
import com.wanbaohe.blessingwall.model.BlessingCalendarDayLabel
import com.wanbaohe.blessingwall.model.BlessingTabCustomization
import com.wanbaohe.blessingwall.model.DailyBlessingRecord
import com.wanbaohe.blessingwall.model.BlessingType
import com.wanbaohe.blessingwall.model.resolveTabText
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowDropDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronLeft

@Composable
fun BlessingRecordScreen(component: BlessingRecordComponent) {
    val uiState by component.uiState.collectAsState()

    BaseScreen(
        title = stringResource(R.string.blessing_record_title),
        onGoBack = component.onGoBack,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CalendarCard(
                    currentYearMonth = uiState.currentYearMonth,
                    selectedDate = uiState.selectedDate,
                    recordDates = uiState.monthRecords.map { it.date }.toSet(),
                    calendarDayLabels = uiState.calendarDayLabels,
                    onPreviousMonth = component::previousMonth,
                    onNextMonth = component::nextMonth,
                    onMonthSelected = component::selectMonth,
                    onDateSelected = { component.onDateSelected(it) },
                )
            }

            item {
                Text(
                    text = stringResource(R.string.blessing_daily_stats_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (uiState.selectedDayRecords.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.blessing_empty_state),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                items(uiState.selectedDayRecords) { record ->
                    DailyRecordItem(
                        record = record,
                        customizations = uiState.tabCustomizationsByDate[record.date].orEmpty(),
                        remoteTexts = uiState.remoteTabTexts,
                        onTypeClick = { type -> component.openWall(record.date, type) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarCard(
    currentYearMonth: YearMonth,
    selectedDate: String?,
    recordDates: Set<String>,
    calendarDayLabels: Map<LocalDate, BlessingCalendarDayLabel>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthSelected: (YearMonth) -> Unit,
    onDateSelected: (String?) -> Unit,
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    var showYearMonthPicker by remember { mutableStateOf(false) }
    val selectedLocalDate = remember(selectedDate) {
        selectedDate?.let { runCatching { LocalDate.parse(it, dateFormatter) }.getOrNull() }
    }
    if (showYearMonthPicker) {
        ChineseYearMonthPickerDialog(
            initialYearMonth = currentYearMonth,
            onYearMonthSelected = onMonthSelected,
            onDismiss = { showYearMonthPicker = false },
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onPreviousMonth, colors = AppTheme.colors.iconButtonColors()) {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronLeft, contentDescription = null)
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showYearMonthPicker = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(
                            R.string.blessing_calendar_year_month,
                            currentYearMonth.year,
                            currentYearMonth.monthValue
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                }
                IconButton(onClick = onNextMonth, colors = AppTheme.colors.iconButtonColors()) {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ChineseWeekdayHeader(firstDayOfWeek = DayOfWeek.MONDAY)

            Spacer(modifier = Modifier.height(4.dp))

            MonthCalendarGrid(
                yearMonth = currentYearMonth,
                selectedDate = selectedLocalDate,
                firstDayOfWeek = DayOfWeek.MONDAY,
                onDateClick = { date ->
                    onDateSelected(
                        if (date == selectedLocalDate) null else date.format(dateFormatter)
                    )
                },
                isDateEnabled = { YearMonth.from(it) == currentYearMonth },
            ) { day ->
                val hasRecord = day.date.format(dateFormatter) in recordDates
                val dayLabel = calendarDayLabels[day.date]
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(
                            if (day.isSelected) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = day.date.dayOfMonth.toString(),
                            fontSize = 13.sp,
                            color = when {
                                day.isSelected -> MaterialTheme.colorScheme.onPrimary
                                day.isToday -> MaterialTheme.colorScheme.primary
                                !day.isCurrentMonth -> {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                }
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                        )
                        Text(
                            text = dayLabel?.text.orEmpty(),
                            fontSize = 9.sp,
                            color = when {
                                day.isSelected -> MaterialTheme.colorScheme.onPrimary
                                !day.isCurrentMonth -> {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                }
                                dayLabel?.isRestDay == true -> MaterialTheme.colorScheme.error
                                dayLabel?.isHighlighted == true -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (hasRecord && !day.isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyRecordItem(
    record: DailyBlessingRecord,
    customizations: Map<BlessingType, BlessingTabCustomization>,
    remoteTexts: Map<BlessingType, BlessingWallTabText>,
    onTypeClick: (BlessingType) -> Unit,
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val localDate = remember(record.date) {
        try {
            LocalDate.parse(record.date, dateFormatter)
        } catch (_: Exception) {
            null
        }
    }
    val dateDisplay = localDate?.let {
        String.format(
            Locale.getDefault(),
            stringResource(R.string.blessing_date_display),
            it.monthValue,
            it.dayOfMonth
        )
    } ?: record.date
    val activeTypes = BlessingType.entries.filter { (record.counts[it] ?: 0) > 0 }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = dateDisplay,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(72.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = stringResource(R.string.blessing_total_count, record.total),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            activeTypes.forEach { type ->
                val customization = customizations[type]
                val remoteText = remoteTexts[type]
                val wish = record.wishes[type].orEmpty()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTypeClick(type) }
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = blessingTypeEmoji(type), fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = resolveTabText(
                                    customization?.title,
                                    remoteText?.title,
                                    tabTitleFallback(type),
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = resolveTabText(
                                    customization?.subtitle,
                                    remoteText?.subtitle,
                                    tabSubtitleFallback(type),
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = stringResource(
                                R.string.blessing_total_count,
                                record.counts[type] ?: 0,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    if (wish.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.blessing_record_note_prefix, wish),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 26.dp, top = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun tabTitleFallback(type: BlessingType): String = stringResource(
    when (type) {
        BlessingType.WOODEN_FISH -> R.string.blessing_tab_wooden_fish
        BlessingType.WEALTH_GOD -> R.string.blessing_tab_wealth_god
        BlessingType.GUANYIN -> R.string.blessing_tab_guanyin
        BlessingType.INCENSE -> R.string.blessing_tab_incense
    }
)

@Composable
private fun tabSubtitleFallback(type: BlessingType): String = stringResource(
    when (type) {
        BlessingType.WOODEN_FISH -> R.string.blessing_subtitle_wooden_fish
        BlessingType.WEALTH_GOD -> R.string.blessing_subtitle_wealth_god
        BlessingType.GUANYIN -> R.string.blessing_subtitle_guanyin
        BlessingType.INCENSE -> R.string.blessing_subtitle_incense
    }
)

private fun blessingTypeEmoji(type: BlessingType): String = when (type) {
    BlessingType.WOODEN_FISH -> "🪘"
    BlessingType.WEALTH_GOD -> "💰"
    BlessingType.GUANYIN -> "🌸"
    BlessingType.INCENSE -> "🕯"
}
