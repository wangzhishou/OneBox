package com.shifenmiao.base.ui.picker

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.R
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowDropDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKeyboardArrowLeft

/**
 * 中文日期范围选择器对话框（毫秒时间戳版本）
 *
 * @param initialStartDateMillis 初始开始日期的毫秒时间戳
 * @param initialEndDateMillis 初始结束日期的毫秒时间戳
 * @param onDateRangeSelected 日期范围选中回调，返回开始和结束日期的毫秒时间戳
 * @param onDismiss 取消/关闭回调
 * @param minDateMillis 最小可选日期的毫秒时间戳，默认 null（不限制）
 * @param maxDateMillis 最大可选日期的毫秒时间戳，默认 null（不限制）
 */
@Composable
fun ChineseDateRangePickerDialog(
    initialStartDateMillis: Long?,
    initialEndDateMillis: Long?,
    onDateRangeSelected: (start: Long, end: Long) -> Unit,
    onDismiss: () -> Unit,
    minDateMillis: Long? = null,
    maxDateMillis: Long? = null,
    placeAboveAll: Boolean = false
) {
    val startDate = initialStartDateMillis?.let {
        Date(it).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val endDate = initialEndDateMillis?.let {
        Date(it).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val minDate = minDateMillis?.let {
        Date(it).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val maxDate = maxDateMillis?.let {
        Date(it).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    ChineseDateRangePickerDialog(
        initialStartDate = startDate,
        initialEndDate = endDate,
        onDateRangeSelected = { s, e ->
            val sMillis = s.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val eMillis = e.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            onDateRangeSelected(sMillis, eMillis)
        },
        onDismiss = onDismiss,
        minDate = minDate,
        maxDate = maxDate,
        placeAboveAll = placeAboveAll,
    )
}

/**
 * 中文日期范围选择器对话框
 *
 * 特性：
 * - 中文星期显示（日、一、二、三、四、五、六）
 * - 先选开始日期，再选结束日期
 * - 范围内的中间日期高亮显示
 * - 支持日期范围限制
 * - 支持国际化
 *
 * @param initialStartDate 初始开始日期，默认为今天
 * @param initialEndDate 初始结束日期，默认为 null
 * @param onDateRangeSelected 日期范围选中回调
 * @param onDismiss 取消/关闭回调
 * @param minDate 最小可选日期，默认 null（不限制）
 * @param maxDate 最大可选日期，默认 null（不限制）
 * @param minYear 最小可选年份
 * @param maxYear 最大可选年份
 */
@Composable
fun ChineseDateRangePickerDialog(
    initialStartDate: LocalDate?,
    initialEndDate: LocalDate?,
    onDateRangeSelected: (start: LocalDate, end: LocalDate) -> Unit,
    onDismiss: () -> Unit,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    minYear: Int = minDate?.year ?: 1900,
    maxYear: Int = maxDate?.year ?: 2100,
    placeAboveAll: Boolean = false
) {
    val today = LocalDate.now()
    var startDate by remember { mutableStateOf(initialStartDate ?: today) }
    var endDate by remember { mutableStateOf(initialEndDate) }

    // true = 正在选开始日期, false = 正在选结束日期
    var selectingStart by remember {
        mutableStateOf(initialStartDate == null || initialEndDate == null)
    }

    var displayedYearMonth by remember {
        mutableStateOf(
            YearMonth.from(initialEndDate ?: initialStartDate ?: today)
        )
    }

    val currentEnd = endDate
    val isValidRange = currentEnd != null && !currentEnd.isBefore(startDate)

    EnhancedAlertDialog(
        visible = true,
        containerColor = AppTheme.colors.getContainerSurfaceColor(),
        onDismissRequest = onDismiss,
        enableGlass = true,
        placeAboveAll = placeAboveAll,
        confirmButton = {
            FilledTonalButton(
                colors = AppTheme.colors.getSecondaryContainerButtonColors(),
                enabled = isValidRange,
                onClick = {
                    endDate?.let { onDateRangeSelected(startDate, it) }
                }
            ) {
                Text(stringResource(R.string.date_picker_confirm))
            }
        },
        dismissButton = {
            FilledTonalButton(
                colors = AppTheme.colors.getSurfaceContainerButtonColors(),
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.date_picker_cancel))
            }
        },
        title = null,
        text = {
            Column {
                Text(
                    text = stringResource(R.string.date_range_picker_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 开始/结束日期选择标签
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DateRangeSelectorChip(
                        label = stringResource(R.string.date_range_start),
                        date = startDate,
                        isSelected = selectingStart,
                        onClick = { selectingStart = true }
                    )
                    DateRangeSelectorChip(
                        label = stringResource(R.string.date_range_end),
                        date = endDate,
                        isSelected = !selectingStart,
                        onClick = { selectingStart = false }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 月份导航
                MonthNavigator(
                    yearMonth = displayedYearMonth,
                    onPreviousMonth = {
                        displayedYearMonth = displayedYearMonth.minusMonths(1)
                    },
                    onNextMonth = {
                        displayedYearMonth = displayedYearMonth.plusMonths(1)
                    },
                    onYearMonthSelected = { displayedYearMonth = it },
                    minYear = minYear,
                    maxYear = maxYear
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 星期标题
                WeekdayHeader()

                Spacer(modifier = Modifier.height(4.dp))

                // 日期网格（范围版）
                RangeCalendarGrid(
                    yearMonth = displayedYearMonth,
                    startDate = startDate,
                    endDate = endDate,
                    minDate = minDate,
                    maxDate = maxDate,
                    onDateClick = { date ->
                        if (selectingStart) {
                            startDate = date
                            if (endDate != null && date.isAfter(endDate)) {
                                endDate = null
                            }
                            selectingStart = false
                        } else {
                            if (date.isBefore(startDate)) {
                                startDate = date
                                endDate = null
                            } else {
                                endDate = date
                            }
                        }
                    }
                )
            }
        }
    )
}

/**
 * 日期范围选择标签 chip
 */
@Composable
private fun RowScope.DateRangeSelectorChip(
    label: String,
    date: LocalDate?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = date?.let {
                stringResource(R.string.date_picker_date_format, it.year, it.monthValue, it.dayOfMonth)
            } ?: "--",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

// ============================================
// 以下组件与 ChineseDatePickerDialog.kt 共享模式
// ============================================

/**
 * 月份导航器
 */
@Composable
private fun MonthNavigator(
    yearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onYearMonthSelected: (YearMonth) -> Unit,
    minYear: Int,
    maxYear: Int
) {
    var showYearPicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showYearPicker = true }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.date_picker_year_month_format,
                        yearMonth.year,
                        yearMonth.monthValue
                    ),
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowDropDown,
                    contentDescription = stringResource(R.string.date_picker_select_year_month),
                    modifier = Modifier.size(24.dp)
                )
            }

            DropdownMenu(
                expanded = showYearPicker,
                onDismissRequest = { showYearPicker = false }
            ) {
                (minYear..maxYear).reversed().forEach { year ->
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.date_picker_year_format, year)) },
                        onClick = {
                            onYearMonthSelected(YearMonth.of(year, yearMonth.monthValue))
                            showYearPicker = false
                        }
                    )
                }
            }
        }

        Row {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineKeyboardArrowLeft,
                    contentDescription = stringResource(R.string.date_picker_previous_month)
                )
            }
            IconButton(onClick = onNextMonth) {
                Icon(
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight,
                    contentDescription = stringResource(R.string.date_picker_next_month)
                )
            }
        }
    }
}

/**
 * 星期标题行（支持国际化）
 */
@Composable
private fun WeekdayHeader() {
    val weekdays = listOf(
        R.string.date_picker_weekday_sun,
        R.string.date_picker_weekday_mon,
        R.string.date_picker_weekday_tue,
        R.string.date_picker_weekday_wed,
        R.string.date_picker_weekday_thu,
        R.string.date_picker_weekday_fri,
        R.string.date_picker_weekday_sat
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weekdays.forEach { dayRes ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(dayRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 日期范围网格
 */
@Composable
private fun RangeCalendarGrid(
    yearMonth: YearMonth,
    startDate: LocalDate,
    endDate: LocalDate?,
    minDate: LocalDate?,
    maxDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

    val days = buildList {
        repeat(firstDayOfWeek) { add(null) }
        for (day in 1..daysInMonth) {
            add(yearMonth.atDay(day))
        }
    }

    var previousYearMonth by remember { mutableStateOf(yearMonth) }
    val slideDirection = if (yearMonth > previousYearMonth) 1 else -1

    AnimatedContent(
        targetState = days,
        transitionSpec = {
            (slideInHorizontally { width -> width * slideDirection } + fadeIn())
                .togetherWith(slideOutHorizontally { width -> -width * slideDirection } + fadeOut())
        },
        label = "range_calendar"
    ) { currentDays ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(currentDays) { date ->
                val isDisabled = date != null && (
                    (minDate != null && date.isBefore(minDate)) ||
                    (maxDate != null && date.isAfter(maxDate))
                )

                val isStart = date != null && date == startDate
                val isEnd = date != null && endDate != null && date == endDate
                val isInRange = date != null && endDate != null &&
                    date.isAfter(startDate) && date.isBefore(endDate)

                RangeDayCell(
                    date = date,
                    isStart = isStart,
                    isEnd = isEnd,
                    isInRange = isInRange,
                    isToday = date == LocalDate.now(),
                    isDisabled = isDisabled,
                    onClick = { date?.let { if (!isDisabled) onDateClick(it) } }
                )
            }
        }
    }

    remember(yearMonth) {
        previousYearMonth = yearMonth
        yearMonth
    }
}

/**
 * 范围日期单元格
 */
@Composable
private fun RangeDayCell(
    date: LocalDate?,
    isStart: Boolean,
    isEnd: Boolean,
    isInRange: Boolean,
    isToday: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundModifier = when {
        isStart || isEnd -> Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
        isInRange -> Modifier.background(MaterialTheme.colorScheme.primaryContainer)
        else -> Modifier
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(horizontal = if (isInRange) 0.dp else 2.dp, vertical = 2.dp)
            .then(backgroundModifier)
            .clip(if (isStart || isEnd) CircleShape else RoundedCornerShape(4.dp))
            .clickable(enabled = date != null && !isDisabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isDisabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    isStart || isEnd -> MaterialTheme.colorScheme.onPrimary
                    isInRange -> MaterialTheme.colorScheme.onPrimaryContainer
                    isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = when {
                    isStart || isEnd || isToday -> FontWeight.Bold
                    else -> FontWeight.Normal
                }
            )
        }
    }
}
