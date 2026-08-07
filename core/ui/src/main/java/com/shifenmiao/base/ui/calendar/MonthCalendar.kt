package com.shifenmiao.base.ui.calendar

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.t8rin.imagetoolbox.core.ui.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Immutable
data class MonthCalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val isSelected: Boolean,
    val isToday: Boolean,
)

@Composable
fun ChineseWeekdayHeader(
    firstDayOfWeek: DayOfWeek,
    modifier: Modifier = Modifier,
) {
    val weekdays = remember(firstDayOfWeek) {
        generateSequence(firstDayOfWeek) { day ->
            DayOfWeek.of(day.value % DAYS_PER_WEEK + 1)
        }.take(DAYS_PER_WEEK).toList()
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        weekdays.forEach { day ->
            Text(
                text = stringResource(day.labelRes()),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun MonthCalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    firstDayOfWeek: DayOfWeek,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    isDateEnabled: (LocalDate) -> Boolean = { true },
    dayContent: @Composable BoxScope.(MonthCalendarDay) -> Unit,
) {
    val days = remember(yearMonth, firstDayOfWeek) {
        buildCalendarDays(yearMonth, firstDayOfWeek)
    }
    Column(modifier = modifier.fillMaxWidth()) {
        days.chunked(DAYS_PER_WEEK).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable(
                                enabled = isDateEnabled(date),
                                onClick = { onDateClick(date) },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        dayContent(
                            MonthCalendarDay(
                                date = date,
                                isCurrentMonth = YearMonth.from(date) == yearMonth,
                                isSelected = date == selectedDate,
                                isToday = date == LocalDate.now(),
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun buildCalendarDays(
    yearMonth: YearMonth,
    firstDayOfWeek: DayOfWeek,
): List<LocalDate> {
    val firstDay = yearMonth.atDay(1)
    val leadingDays = (firstDay.dayOfWeek.value - firstDayOfWeek.value + 7) % 7
    val gridStart = firstDay.minusDays(leadingDays.toLong())
    val visibleDayCount = ((leadingDays + yearMonth.lengthOfMonth() + 6) / 7) * 7
    return List(visibleDayCount) { offset -> gridStart.plusDays(offset.toLong()) }
}

private const val DAYS_PER_WEEK = 7

@StringRes
private fun DayOfWeek.labelRes(): Int = when (this) {
    DayOfWeek.MONDAY -> R.string.date_picker_weekday_mon
    DayOfWeek.TUESDAY -> R.string.date_picker_weekday_tue
    DayOfWeek.WEDNESDAY -> R.string.date_picker_weekday_wed
    DayOfWeek.THURSDAY -> R.string.date_picker_weekday_thu
    DayOfWeek.FRIDAY -> R.string.date_picker_weekday_fri
    DayOfWeek.SATURDAY -> R.string.date_picker_weekday_sat
    DayOfWeek.SUNDAY -> R.string.date_picker_weekday_sun
}
