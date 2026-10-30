package com.shifenmiao.marktodo.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.marktodo.R
import com.wanbaohe.core.ui.widget.system.OneBoxOutlinedTextField
import java.text.SimpleDateFormat
import java.util.Locale
import com.t8rin.imagetoolbox.core.resources.icons.line.LineClear
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendar

/**
 * 统一的任务日期选择输入框组件。
 *
 * 特性：
 * - 点击整个字段可打开日期选择器
 * - 日期值不为空时可单独清除
 * - 只读，禁止键盘输入
 * - 统一日期格式化
 */
@Composable
fun TaskDateField(
    dueDateMillis: Long?,
    onClearDate: () -> Unit,
    onSelectDate: () -> Unit,
    placeholder: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dateFormatter = rememberDateFormatter()

    Box(modifier = modifier) {
        OneBoxOutlinedTextField(
            value = dueDateMillis?.let { dateFormatter.format(it) } ?: "",
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            placeholder = placeholder,
            trailingIcon = {
                Row {
                    IconButton(
                        modifier = Modifier.size(48.dp),
                        onClick = onSelectDate
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar,
                            contentDescription = stringResource(R.string.dialog_add_task_due_date),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (dueDateMillis != null) {
                        IconButton(
                            modifier = Modifier.size(48.dp),
                            onClick = onClearDate
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineClear,
                                contentDescription = stringResource(R.string.dialog_add_task_clear),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(end = if (dueDateMillis != null) 96.dp else 48.dp)
                .clickable(onClick = onSelectDate)
        )
    }
}

@Composable
private fun rememberDateFormatter(): SimpleDateFormat {
    return androidx.compose.runtime.remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }
}
