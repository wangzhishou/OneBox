/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.base.ui.picker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.R
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog

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

    EnhancedAlertDialog(

        visible = true,
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
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title ?: stringResource(R.string.time_picker_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 对话框正文按内容宽度靠左排, 表盘会被挤到左侧。强制竖排并在整行里居中。
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    TimePicker(
                        state = timePickerState,
                        layoutType = TimePickerLayoutType.Vertical,
                    )
                }
            }
        },
    )
}
