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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowDropDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKeyboardArrowLeft

/**
 * 中文日期选择器对话框（毫秒时间戳版本）
 *
 * 便捷的重载版本，接受毫秒时间戳作为参数
 *
 * @param initialDateMillis 初始选中日期的毫秒时间戳
 * @param onDateSelected 日期选中回调，返回毫秒时间戳
 * @param onDismiss 取消/关闭回调
 * @param minDateMillis 最小可选日期的毫秒时间戳，默认 null（不限制）
 * @param maxDateMillis 最大可选日期的毫秒时间戳，默认 null（不限制）
 * @param title 标题文字，默认"选择日期"
 * @param confirmText 确认按钮文字，默认"确认"
 * @param cancelText 取消按钮文字，默认"取消"
 */
@Composable
fun ChineseDatePickerDialog(
    initialDateMillis: Long?,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
    minDateMillis: Long? = null,
    maxDateMillis: Long? = null,
    title: String = stringResource(R.string.date_picker_title),
    confirmText: String = stringResource(R.string.date_picker_confirm),
    cancelText: String = stringResource(R.string.date_picker_cancel),
    placeAboveAll: Boolean = false
) {
    val initialDate = initialDateMillis?.let {
        Date(it).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val minDate = minDateMillis?.let {
        Date(it).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val maxDate = maxDateMillis?.let {
        Date(it).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    ChineseDatePickerDialog(
        initialDate = initialDate,
        onDateSelected = { localDate ->
            val millis = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            onDateSelected(millis)
        },
        onDismiss = onDismiss,
        minDate = minDate,
        maxDate = maxDate,
        title = title,
        confirmText = confirmText,
        cancelText = cancelText,
        placeAboveAll = placeAboveAll
    )
}

/**
 * 中文日期选择器对话框
 *
 * 完全中文显示的日历式日期选择器，包括：
 * - 中文星期显示（日、一、二、三、四、五、六）
 * - 中文日期格式（2020年5月27日）
 * - 月份切换动画
 * - 年份快速选择
 * - 支持日期范围限制
 *
 * @param initialDate 初始选中日期，默认为今天
 * @param onDateSelected 日期选中回调
 * @param onDismiss 取消/关闭回调
 * @param minDate 最小可选日期，默认 null（不限制）
 * @param maxDate 最大可选日期，默认 null（不限制）
 * @param minYear 最小可选年份，默认1900
 * @param maxYear 最大可选年份，默认2100
 * @param title 标题文字，默认"选择日期"
 * @param confirmText 确认按钮文字，默认"确认"
 * @param cancelText 取消按钮文字，默认"取消"
 */
@Composable
fun ChineseDatePickerDialog(
    initialDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    minYear: Int = minDate?.year ?: 1900,
    maxYear: Int = maxDate?.year ?: 2100,
    title: String = stringResource(R.string.date_picker_title),
    confirmText: String = stringResource(R.string.date_picker_confirm),
    cancelText: String = stringResource(R.string.date_picker_cancel),
    placeAboveAll: Boolean = false
) {
    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(initialDate ?: today) }
    var displayedYearMonth by remember {
        mutableStateOf(YearMonth.of(selectedDate.year, selectedDate.month))
    }
    // 是否处于输入模式
    var isInputMode by remember { mutableStateOf(false) }
    // 输入的年月日
    var inputYear by remember { mutableStateOf(selectedDate.year.toString()) }
    var inputMonth by remember { mutableStateOf(selectedDate.monthValue.toString()) }
    var inputDay by remember { mutableStateOf(selectedDate.dayOfMonth.toString()) }

    EnhancedAlertDialog(
        visible = true,
        containerColor = AppTheme.colors.getContainerSurfaceColor(),
        onDismissRequest = onDismiss,
        enableGlass = true,
        placeAboveAll = placeAboveAll,
        confirmButton = {
            FilledTonalButton(
                colors = AppTheme.colors.getSecondaryContainerButtonColors(),
                onClick = {
                    if (isInputMode) {
                        // 输入模式下，验证并应用输入的日期
                        try {
                            val year = inputYear.toIntOrNull() ?: selectedDate.year
                            val month = inputMonth.toIntOrNull()?.coerceIn(1, 12) ?: selectedDate.monthValue
                            val maxDay = YearMonth.of(year, month).lengthOfMonth()
                            val day = inputDay.toIntOrNull()?.coerceIn(1, maxDay) ?: selectedDate.dayOfMonth
                            val newDate = LocalDate.of(year, month, day)

                            // 检查日期范围
                            val validDate = when {
                                minDate != null && newDate.isBefore(minDate) -> minDate
                                maxDate != null && newDate.isAfter(maxDate) -> maxDate
                                else -> newDate
                            }
                            selectedDate = validDate
                            displayedYearMonth = YearMonth.of(validDate.year, validDate.month)
                            isInputMode = false
                        } catch (_: Exception) {
                            // 输入无效，保持当前日期
                            isInputMode = false
                        }
                    } else {
                        onDateSelected(selectedDate)
                    }
                }) {
                Text(if (isInputMode) "应用" else confirmText)
            }
        },
        dismissButton = {
            FilledTonalButton(
                colors = AppTheme.colors.getSurfaceContainerButtonColors(),
                onClick = {
                    if (isInputMode) {
                        // 取消输入模式，恢复原来的值
                        inputYear = selectedDate.year.toString()
                        inputMonth = selectedDate.monthValue.toString()
                        inputDay = selectedDate.dayOfMonth.toString()
                        isInputMode = false
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(if (isInputMode) "取消" else cancelText)
            }
        },
        title = null,
        text = {
            Column {
                // 标题
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isInputMode) {
                    // 输入模式 - 显示输入框
                    DateInputFields(
                        year = inputYear,
                        month = inputMonth,
                        day = inputDay,
                        onYearChange = { inputYear = it },
                        onMonthChange = { inputMonth = it },
                        onDayChange = { inputDay = it }
                    )
                } else {
                    // 选择模式 - 显示日历
                    // 显示选中的日期
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(
                                R.string.date_picker_date_format,
                                selectedDate.year,
                                selectedDate.monthValue,
                                selectedDate.dayOfMonth
                            ),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = {
                            // 切换到输入模式
                            inputYear = selectedDate.year.toString()
                            inputMonth = selectedDate.monthValue.toString()
                            inputDay = selectedDate.dayOfMonth.toString()
                            isInputMode = true
                        }) {
                            Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit, contentDescription = stringResource(R.string.date_picker_edit))
                        }
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

                    // 星期标题行
                    WeekdayHeader()

                    Spacer(modifier = Modifier.height(4.dp))

                    // 日期网格
                    CalendarGrid(
                        yearMonth = displayedYearMonth,
                        selectedDate = selectedDate,
                        minDate = minDate,
                        maxDate = maxDate,
                        onDateClick = { date ->
                            selectedDate = date
                        }
                    )
                }
            }
        }
    )
}

/**
 * 日期输入框组件
 */
@Composable
private fun DateInputFields(
    year: String,
    month: String,
    day: String,
    onYearChange: (String) -> Unit,
    onMonthChange: (String) -> Unit,
    onDayChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "请输入日期",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 年份输入
            androidx.compose.material3.OutlinedTextField(
                value = year,
                onValueChange = {
                    if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                        onYearChange(it)
                    }
                },
                label = { Text("年") },
                modifier = Modifier.weight(1.2f),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                colors = AppTheme.colors.getOutlinedTextFieldColors(),
                shape = MaterialTheme.shapes.medium
            )

            // 月份输入
            androidx.compose.material3.OutlinedTextField(
                value = month,
                onValueChange = {
                    if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                        onMonthChange(it)
                    }
                },
                label = { Text("月") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                colors = AppTheme.colors.getOutlinedTextFieldColors(),
                shape = MaterialTheme.shapes.medium
            )

            // 日期输入
            androidx.compose.material3.OutlinedTextField(
                value = day,
                onValueChange = {
                    if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                        onDayChange(it)
                    }
                },
                label = { Text("日") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                colors = AppTheme.colors.getOutlinedTextFieldColors(),
                shape = MaterialTheme.shapes.medium
            )
        }

        // 提示文字
        Text(
            text = "提示：输入完成后点击\"应用\"按钮",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

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
        // 年月选择器
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
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowDropDown,
                    contentDescription = stringResource(R.string.date_picker_select_year_month),
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = showYearPicker,
                onDismissRequest = { showYearPicker = false }
            ) {
                // 显示年份列表
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

        // 前后月份按钮
        Row {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineKeyboardArrowLeft,
                    contentDescription = stringResource(R.string.date_picker_previous_month),
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onNextMonth) {
                Icon(
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight,
                    contentDescription = stringResource(R.string.date_picker_next_month),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * 星期标题行（中文）
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
 * 日历网格
 */
@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    minDate: LocalDate?,
    maxDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()

    // 计算第一天是星期几（0 = 周日）
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

    // 构建日期列表（包括前面的空白）
    val days = buildList {
        // 添加空白占位
        repeat(firstDayOfWeek) { add(null) }
        // 添加当月日期
        for (day in 1..daysInMonth) {
            add(yearMonth.atDay(day))
        }
    }

    // 使用 AnimatedContent 添加切换动画
    var previousYearMonth by remember { mutableStateOf(yearMonth) }
    val slideDirection = if (yearMonth > previousYearMonth) 1 else -1

    AnimatedContent(
        targetState = days,
        transitionSpec = {
            (slideInHorizontally { width -> width * slideDirection } + fadeIn())
                .togetherWith(slideOutHorizontally { width -> -width * slideDirection } + fadeOut())
        },
        label = "calendar"
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
                DayCell(
                    date = date,
                    isSelected = date == selectedDate,
                    isToday = date == LocalDate.now(),
                    isDisabled = isDisabled,
                    onClick = { date?.let { if (!isDisabled) onDateClick(it) } }
                )
            }
        }
    }

    // 更新前一个年月
    remember(yearMonth) {
        previousYearMonth = yearMonth
        yearMonth
    }
}

/**
 * 单个日期单元格
 */
@Composable
private fun DayCell(
    date: LocalDate?,
    isSelected: Boolean,
    isToday: Boolean,
    isDisabled: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .then(
                when {
                    isSelected && !isDisabled -> Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                    isToday && !isDisabled -> Modifier.background(MaterialTheme.colorScheme.primary)
                    else -> Modifier
                }
            )
            .clickable(enabled = date != null && !isDisabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isDisabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                    isToday -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if ((isSelected || isToday) && !isDisabled) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

