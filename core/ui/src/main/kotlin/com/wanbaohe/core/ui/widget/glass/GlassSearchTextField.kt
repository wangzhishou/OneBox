package com.wanbaohe.core.ui.widget.glass

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.Search

/**
 * 毛玻璃风格搜索输入框。
 *
 * - 默认单行 + 搜索图标 + 清除图标（仅在 [value] 非空时显示）
 * - placeholder 通过字符串资源传入，由调用方控制 i18n
 * - 圆角、底色、内边距走 [GlassOutlinedTextField] 的统一规格
 */
@Composable
fun GlassSearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = "Search",
    enabled: Boolean = true,
    onSubmit: (() -> Unit)? = null,
    style: GlassStyle = GlassStyle.Medium,
) {
    val resolvedPlaceholder = placeholder ?: "Search"
    GlassOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        placeholder = { Text(text = resolvedPlaceholder) },
        leadingIcon = {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Search,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = if (onSubmit != null) {
            KeyboardActions(onSearch = { onSubmit() })
        } else {
            KeyboardActions.Default
        },
        style = style,
    )
}
