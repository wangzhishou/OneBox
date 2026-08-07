package com.shifenmiao.base.ui.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.R
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog

/**
 * 中文时间段选择器对话框
 *
 * 完全中文显示的时间段选择器，避免系统 TimePicker 在中文环境下显示异常的问题
 *
 * @param initialStartHour 初始开始小时（0-23）
 * @param initialStartMinute 初始开始分钟（0-59）
 * @param initialEndHour 初始结束小时（0-23）
 * @param initialEndMinute 初始结束分钟（0-59）
 * @param title 标题文字，默认"选择时间段"
 * @param onTimeRangeSelected 时间段选中回调
 * @param onDismiss 取消/关闭回调
 * @param confirmText 确认按钮文字，默认"确认"
 * @param cancelText 取消按钮文字，默认"取消"
 */
@Composable
fun ChineseTimeRangePickerDialog(
    initialStartHour: Int = 9,
    initialStartMinute: Int = 0,
    initialEndHour: Int = 18,
    initialEndMinute: Int = 0,
    title: String? = null,
    onTimeRangeSelected: (startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = stringResource(R.string.date_picker_confirm),
    cancelText: String = stringResource(R.string.date_picker_cancel),
    @Suppress("UNUSED_PARAMETER") placeAboveAll: Boolean = false
) {
    var startHour by remember { mutableStateOf(initialStartHour.coerceIn(0, 23).toString()) }
    var startMinute by remember { mutableStateOf(initialStartMinute.coerceIn(0, 59).toString()) }
    var endHour by remember { mutableStateOf(initialEndHour.coerceIn(0, 23).toString()) }
    var endMinute by remember { mutableStateOf(initialEndMinute.coerceIn(0, 59).toString()) }

    EnhancedAlertDialog(
        visible = true,
        containerColor = AppTheme.colors.getContainerSurfaceColor(),
        onDismissRequest = onDismiss,
        placeAboveAll = placeAboveAll,
        enableGlass = true,
        confirmButton = {
            FilledTonalButton(
                colors = AppTheme.colors.getSecondaryContainerButtonColors(),
                onClick = {
                    val sh = startHour.toIntOrNull()?.coerceIn(0, 23) ?: 0
                    val sm = startMinute.toIntOrNull()?.coerceIn(0, 59) ?: 0
                    val eh = endHour.toIntOrNull()?.coerceIn(0, 23) ?: 0
                    val em = endMinute.toIntOrNull()?.coerceIn(0, 59) ?: 0
                    onTimeRangeSelected(sh, sm, eh, em)
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            FilledTonalButton(
                colors = AppTheme.colors.getSurfaceContainerButtonColors(),
                onClick = onDismiss
            ) {
                Text(cancelText)
            }
        },
        title = null,
        text = {
            Column {
                // 标题
                Text(
                    text = title ?: stringResource(R.string.time_range_picker_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 开始时间
                Text(
                    text = stringResource(R.string.time_range_picker_start_time),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                TimeInputRow(
                    hour = startHour,
                    minute = startMinute,
                    onHourChange = { startHour = it },
                    onMinuteChange = { startMinute = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 结束时间
                Text(
                    text = stringResource(R.string.time_range_picker_end_time),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                TimeInputRow(
                    hour = endHour,
                    minute = endMinute,
                    onHourChange = { endHour = it },
                    onMinuteChange = { endMinute = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 提示文字
                Text(
                    text = stringResource(R.string.time_range_picker_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

/**
 * 时间输入行组件
 */
@Composable
internal fun TimeInputRow(
    hour: String,
    minute: String,
    onHourChange: (String) -> Unit,
    onMinuteChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 小时输入
        OutlinedTextField(
            value = hour,
            onValueChange = {
                if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                    onHourChange(it)
                }
            },
            label = { Text(stringResource(R.string.time_range_picker_hour)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = AppTheme.colors.getOutlinedTextFieldColors(),
            shape = MaterialTheme.shapes.medium
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // 分钟输入
        OutlinedTextField(
            value = minute,
            onValueChange = {
                if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                    onMinuteChange(it)
                }
            },
            label = { Text(stringResource(R.string.time_range_picker_minute)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = AppTheme.colors.getOutlinedTextFieldColors(),
            shape = MaterialTheme.shapes.medium
        )
    }
}
