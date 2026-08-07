package com.wanbaohe.loancalculator.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight

/**
 * 贷款计算器通用输入行（卡片样式，极致扁平）
 *
 * @param label        左侧标签文字
 * @param value        当前输入值（供 TextField 使用）
 * @param onValueChange 值变化回调（null 表示只读/选择行）
 * @param hint         右侧占位提示
 * @param isSelector   是否为选择行（右侧显示 chevron，整行可点击）
 * @param onClick      选择行点击回调
 * @param errorMessage 校验错误提示，非空时行底部显示红色文字
 * @param leadingContent 标签右侧的额外 Composable（如年/月切换 DropdownMenu）
 * @param keyboardType 键盘类型
 */
@Composable
fun LoanInputItem(
    label: String,
    modifier: Modifier = Modifier,
    value: String = "",
    onValueChange: ((String) -> Unit)? = null,
    hint: String = "",
    isSelector: Boolean = false,
    onClick: (() -> Unit)? = null,
    errorMessage: String? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Number,
) {
    val containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerColor)
            .then(
                if (isSelector && onClick != null)
                    Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 标签区（可附加子控件如下拉菜单）
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(0.4f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            leadingContent?.invoke()
        }

        if (isSelector) {
            // 选择行：右侧显示当前值或提示 + chevron
            Text(
                text = value.ifEmpty { hint },
                style = MaterialTheme.typography.bodyMedium,
                color = if (value.isEmpty())
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(0.5f),
            )
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        } else {
            // 输入行：右侧 TextField（透明背景，无分割线）
            TextField(
                value = value,
                onValueChange = { onValueChange?.invoke(it) },
                placeholder = {
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = AppTheme.colors.getOutlinedTextFieldColors().let {
                    // 完全透明容器 + 隐藏指示线，实现极致扁平
                    androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary,
                    )
                },
                modifier = Modifier.weight(0.6f),
            )
        }
    }

    // 校验错误提示
    if (errorMessage != null) {
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(start = 20.dp, top = 4.dp, bottom = 2.dp),
        )
    }
}
