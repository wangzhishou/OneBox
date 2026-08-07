package com.shifenmiao.base.ui.picker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.R

/**
 * 时间选择器对话框
 *
 * 使用系统 [TimePicker]，保持与项目整体风格一致的对话框包装。
 *
 * @param initialHour 初始小时（0-23）
 * @param initialMinute 初始分钟（0-59）
 * @param title 标题文字，默认"选择时间"
 * @param onTimeSelected 时间选中回调
 * @param onDismiss 取消/关闭回调
 * @param confirmText 确认按钮文字，默认"确认"
 * @param cancelText 取消按钮文字，默认"取消"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChineseTimePickerDialog(
    initialHour: Int = 0,
    initialMinute: Int = 0,
    title: String? = null,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = stringResource(R.string.date_picker_confirm),
    cancelText: String = stringResource(R.string.date_picker_cancel),
    @Suppress("UNUSED_PARAMETER") placeAboveAll: Boolean = false,
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour.coerceIn(0, 23),
        initialMinute = initialMinute.coerceIn(0, 59),
        is24Hour = true,
    )

    AlertDialog(
        containerColor = AppTheme.colors.getContainerSurfaceColor(),
        onDismissRequest = onDismiss,
        confirmButton = {
            FilledTonalButton(
                colors = AppTheme.colors.getSecondaryContainerButtonColors(),
                onClick = {
                    onTimeSelected(timePickerState.hour, timePickerState.minute)
                },
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            FilledTonalButton(
                colors = AppTheme.colors.getSurfaceContainerButtonColors(),
                onClick = onDismiss,
            ) {
                Text(cancelText)
            }
        },
        title = null,
        text = {
            Column {
                Text(
                    text = title ?: stringResource(R.string.time_picker_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                TimePicker(state = timePickerState)
            }
        },
    )
}
