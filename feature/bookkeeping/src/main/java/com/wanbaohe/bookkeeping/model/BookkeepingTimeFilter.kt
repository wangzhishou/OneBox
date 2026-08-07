package com.wanbaohe.bookkeeping.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.TimeFilterPreset
import com.wanbaohe.bookkeeping.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

sealed interface BookkeepingTimeFilter {
    data class Custom(val startDate: LocalDate, val endDate: LocalDate) : BookkeepingTimeFilter
    data class Recent(val preset: BookkeepingRecentPreset) : BookkeepingTimeFilter
}

enum class BookkeepingRecentPreset {
    LAST_7_DAYS,
    LAST_1_MONTH,
    LAST_1_YEAR,
}

data class BookkeepingDateRange(
    val startDate: LocalDate,
    val endDate: LocalDate,
)

fun BookkeepingTimeFilter.dateRange(today: LocalDate = LocalDate.now()): BookkeepingDateRange = when (this) {
    is BookkeepingTimeFilter.Custom -> BookkeepingDateRange(startDate = startDate, endDate = endDate)
    is BookkeepingTimeFilter.Recent -> {
        val startDate = when (preset) {
            BookkeepingRecentPreset.LAST_7_DAYS -> today.minusDays(6)
            BookkeepingRecentPreset.LAST_1_MONTH -> today.minusMonths(1).plusDays(1)
            BookkeepingRecentPreset.LAST_1_YEAR -> today.minusYears(1).plusDays(1)
        }
        BookkeepingDateRange(startDate = startDate, endDate = today)
    }
}

/** 当前选中的预设 key（用于 [EnhancedTimeFilterBar]）；未选预设（即自定义区间生效）时返回 null。 */
val BookkeepingTimeFilter.selectedPresetKey: String?
    get() = (this as? BookkeepingTimeFilter.Recent)?.preset?.name

/** 把 [BookkeepingRecentPreset] 转成 [EnhancedTimeFilterBar] 所需的 [TimeFilterPreset] 列表。 */
@Composable
fun rememberBookkeepingTimePresets(): List<TimeFilterPreset> {
    val last7 = stringResource(R.string.bookkeeping_time_filter_last_7_days)
    val last1Month = stringResource(R.string.bookkeeping_time_filter_last_month)
    val last1Year = stringResource(R.string.bookkeeping_time_filter_last_year)
    return remember(last7, last1Month, last1Year) {
        listOf(
            TimeFilterPreset(
                key = BookkeepingRecentPreset.LAST_7_DAYS.name,
                label = last7,
            ),
            TimeFilterPreset(
                key = BookkeepingRecentPreset.LAST_1_MONTH.name,
                label = last1Month,
            ),
            TimeFilterPreset(
                key = BookkeepingRecentPreset.LAST_1_YEAR.name,
                label = last1Year,
            ),
        )
    }
}

private val DATE_RANGE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

fun BookkeepingDateRange.displayLabel(): String {
    val startLabel = startDate.format(DATE_RANGE_FORMATTER)
    val endLabel = endDate.format(DATE_RANGE_FORMATTER)
    return if (startDate == endDate) startLabel else "$startLabel - $endLabel"
}

fun BookkeepingDateRange.toEpochMilliRange(zone: java.time.ZoneId = java.time.ZoneId.systemDefault()): Pair<Long, Long> {
    val start = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
    val end = endDate.atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
    return start to end
}

