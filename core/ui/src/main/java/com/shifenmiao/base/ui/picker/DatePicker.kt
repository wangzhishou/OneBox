package com.shifenmiao.base.ui.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.t8rin.imagetoolbox.core.ui.R
import java.time.LocalDate

enum class DateType {
    YEAR,
    MONTH,
    DAY
}

@Composable
fun DatePicker(
    visible: Boolean,
    value: LocalDate? = null,
    type: DateType = DateType.DAY,
    start: LocalDate = LocalDate.now().minusYears(50),
    end: LocalDate = LocalDate.now().plusYears(10),
    onCancel: () -> Unit,
    onChange: (LocalDate) -> Unit
) {
    require(end.isAfter(start)) {
        "Invalid date range: end ($end) must be after start ($start)"
    }

    var rangesSource by rememberRangesSource(start, end, type)
    val currentRanges by rememberCurrentRanges(rangesSource)
    var currentValues by rememberCurrentValues(rangesSource, value, start, end)

    Picker(
        visible,
        currentRanges,
        values = currentValues,
        title = stringResource(id = R.string.date_picker_title),
        onCancel = onCancel,
        onColumnValueChange = { column, _, newValues ->
            handleColumnChange(rangesSource, newValues, column, start, end, type) {
                rangesSource = it
            }
        }
    ) {
        currentValues = it

        val date = LocalDate.of(
            rangesSource[0][it[0]],
            rangesSource.getOrNull(1)?.get(it[1]) ?: 1,
            rangesSource.getOrNull(2)?.get(it[2]) ?: 1
        )
        onChange(date)
    }
}

private fun handleColumnChange(
    ranges: Array<List<Int>>,
    values: Array<Int>,
    column: Int = 0,
    start: LocalDate,
    end: LocalDate,
    type: DateType,
    onRangesChange: (Array<List<Int>>) -> Unit
) {
    // 只要不是选择年份，并且不是最后一列变化，就不做改变
    if (type == DateType.YEAR || column == 2) return

    val yearLastIndex = ranges.first().lastIndex
    // 计算可选月份
    val monthRange = if (values.first() == 0 && values.first() == yearLastIndex) {
        // 1.当前选择的既是第一个，又是最后一个年份，则可选月份就是他们之间的月份
        IntRange(start.monthValue, end.monthValue)
    } else {
        when (values.first()) {
            // 2.当前选择的是第一个年份，则可选月份就是开始时间的月份到12月
            0 -> IntRange(start.monthValue, 12)
            // 3.当前选择的是最后一个年份，则可选月份就是1月到结束时间的月份
            yearLastIndex -> IntRange(1, end.monthValue)
            // 4.其它情况，月份固定都是1-12月
            else -> IntRange(1, 12)
        }
    }
    ranges[1] = monthRange.toList()

    // 如果精确到日，就计算可选的日
    if (type == DateType.DAY) {
        // 获取当前选择的月份的值
        ranges[1].getOrNull(values[1])?.let { month ->
            val dayRange =
                if (values.first() == yearLastIndex && start.monthValue == end.monthValue) {
                    // 如果开始和结束年月都相等，则可选的日就是他们之间的日
                    IntRange(start.dayOfMonth, end.dayOfMonth)
                } else if (values.first() == 0 && month == start.monthValue) {
                    // 如果当前选择的是开始时间的年和月，则可选的日就是开始时间的日到开始时间的月的最后一天
                    val days = LocalDate.now().withMonth(start.monthValue).lengthOfMonth()
                    IntRange(start.dayOfMonth, days)
                } else if (values.first() == yearLastIndex && month == end.monthValue) {
                    // 如果当前选择的是结束结束时间的年和月，则可选的日就是1日到结束时间的日期
                    IntRange(1, end.dayOfMonth)
                } else {
                    // 其它情况，就计算对应年月的可选天数
                    val daysOfMonth =
                        LocalDate.now().withMonth(ranges[1][values[1]])
                            .lengthOfMonth()
                    IntRange(1, daysOfMonth)
                }
            ranges[2] = dayRange.toList()
        }
    }

    // 重新赋值，触发界面更新
    onRangesChange(ranges.copyOf())
}

@Composable
private fun rememberRangesSource(
    start: LocalDate,
    end: LocalDate,
    type: DateType
): MutableState<Array<List<Int>>> {
    return remember(start, end, type) {
        val options = arrayOf(
            IntRange(start.year, end.year).toList(),
            IntRange(1, 12).toList(),
            IntRange(1, 31).toList()
        )

        mutableStateOf(
            when (type) {
                DateType.YEAR -> options.copyOfRange(0, 1)
                DateType.MONTH -> options.copyOfRange(0, 2)
                DateType.DAY -> options
            }
        )
    }
}

@Composable
private fun rememberCurrentRanges(
    ranges: Array<List<Int>>
): State<Array<List<String>>> {
    val units = remember { arrayOf("年", "月", "日") }
    val currentRanges by rememberUpdatedState(newValue = ranges)

    return remember {
        derivedStateOf {
            currentRanges.mapIndexed { index, options ->
                val unit = units[index]
                options.map { it.toString() + unit }
            }.toTypedArray()
        }
    }
}

@Composable
private fun rememberCurrentValues(
    ranges: Array<List<Int>>,
    value: LocalDate?,
    start: LocalDate,
    end: LocalDate
): MutableState<Array<Int>> {
    return remember(ranges, value, start, end) {
        val finalValue = value ?: LocalDate.now()
        val initialValue = if (finalValue.isBefore(start)) {
            start
        } else if (finalValue.isAfter(end)) {
            end
        } else {
            finalValue
        }

        mutableStateOf(
            arrayOf(
                ranges.first().indexOf(initialValue.year),
                ranges.getOrNull(1)?.indexOf(initialValue.monthValue) ?: 0,
                ranges.getOrNull(2)?.indexOf(initialValue.dayOfMonth) ?: 0
            )
        )
    }
}

@Stable
interface DatePickerState {
    val visible: Boolean

    fun show(
        value: LocalDate? = null,
        type: DateType = DateType.DAY,
        start: LocalDate = LocalDate.now().minusYears(50),
        end: LocalDate = LocalDate.now().plusYears(10),
        onCancel: () -> Unit,
        onChange: (LocalDate) -> Unit
    )

    fun hide()
}

@Composable
fun rememberDatePickerState(): DatePickerState {
    val state = remember { DatePickerStateImpl() }

    state.props?.let { props ->
        DatePicker(
            visible = state.visible,
            value = props.value,
            type = props.type,
            start = props.start,
            end = props.end,
            onCancel = {
                state.hide()
                props.onCancel()
            },
            onChange = props.onChange
        )
    }

    return state
}

private class DatePickerStateImpl : DatePickerState {
    override var visible by mutableStateOf(false)
    var props by mutableStateOf<DatePickerProps?>(null)
        private set

    override fun show(
        value: LocalDate?,
        type: DateType,
        start: LocalDate,
        end: LocalDate,
        onCancel: () -> Unit,
        onChange: (LocalDate) -> Unit
    ) {
        props = DatePickerProps(value, type, start, end, onCancel, onChange)
        visible = true
    }

    override fun hide() {
        visible = false
    }
}

private data class DatePickerProps(
    val value: LocalDate?,
    val type: DateType,
    val start: LocalDate,
    val end: LocalDate,
    val onCancel: () -> Unit,
    val onChange: (LocalDate) -> Unit
)