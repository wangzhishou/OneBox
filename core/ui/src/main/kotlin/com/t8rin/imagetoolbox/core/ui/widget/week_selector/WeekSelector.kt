package com.t8rin.imagetoolbox.core.ui.widget.week_selector

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.picker.ChineseDatePickerDialog
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowDropDown
import com.t8rin.imagetoolbox.core.ui.R
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * 周筛选公共组件 — 头部 "年月 · 第N周" chip + 周一~周日 7 格周条。
 *
 * - 点 chip 弹日期选择器,任选一天跳到对应周
 * - 选中日:圆角高亮块 + 下方小圆点;今天:日号块描边区分
 * - [maxDate] 之后的日期置灰不可选(如打卡页传今天,禁止选未来)
 */
@Composable
fun WeekSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    maxDate: LocalDate? = null,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val weekStart = selectedDate.with(DayOfWeek.MONDAY)
    val today = LocalDate.now()
    val weekdayLabels = stringArrayResource(R.array.week_selector_weekdays_short)

    Column(modifier = modifier.fillMaxWidth()) {
        // ── 头部 chip:点击任选日期跳周(玻璃底) ──
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .glassBackground(
                    style = GlassStyle.Thick,
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
                .clickable { showDatePicker = true }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    R.string.week_selector_week_title,
                    selectedDate.year,
                    selectedDate.monthValue,
                    weekOfMonth(selectedDate),
                ),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Outlined.LineArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(14.dp),
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── 周一~周日 7 格 ──
        Row(modifier = Modifier.fillMaxWidth()) {
            (0L..6L).forEach { offset ->
                val date = weekStart.plusDays(offset)
                val isEnabled = maxDate == null || !date.isAfter(maxDate)
                WeekDayCell(
                    date = date,
                    weekdayLabel = weekdayLabels[offset.toInt()],
                    isSelected = date == selectedDate,
                    isToday = date == today,
                    isEnabled = isEnabled,
                    onSelect = { onDateSelected(date) },
                )
            }
        }
    }

    if (showDatePicker) {
        ChineseDatePickerDialog(
            initialDate = selectedDate,
            maxDate = maxDate,
            onDateSelected = { date ->
                onDateSelected(date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 单日格子:星期标签 + 日号块 + 选中圆点
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RowScope.WeekDayCell(
    date: LocalDate,
    weekdayLabel: String,
    isSelected: Boolean,
    isToday: Boolean,
    isEnabled: Boolean,
    onSelect: () -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = isEnabled, onClick = onSelect)
            .padding(vertical = 4.dp)
            // 超过 maxDate 的日期整体降透明度置灰
            .alpha(if (isEnabled) 1f else 0.38f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = weekdayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else Color.Transparent
                )
                // 今天:描边区分(选中态下高亮块已足够醒目,不重复描边)
                .then(
                    if (isToday && !isSelected) {
                        Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(10.dp),
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
        // 选中下带小圆点
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else Color.Transparent
                ),
        )
    }
}

/**
 * 所选日期是当月第几周(周一起算):
 * ((dayOfMonth + 当月1日的 dayOfWeek.value - 2) / 7) + 1
 * 例:2024-06(1 日周六)→ 6/3-6/9 为第 2 周。
 */
private fun weekOfMonth(date: LocalDate): Int {
    val firstDayOfWeekValue = date.withDayOfMonth(1).dayOfWeek.value
    return ((date.dayOfMonth + firstDayOfWeekValue - 2) / 7) + 1
}
