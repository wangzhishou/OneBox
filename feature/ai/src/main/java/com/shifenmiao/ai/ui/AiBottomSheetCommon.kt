package com.shifenmiao.ai.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.widget.enhanced.EnhancedSheetHeader
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.resources.icons.Search
import com.t8rin.imagetoolbox.core.resources.icons.Close

internal object AiBottomSheetDefaults {
    val HorizontalPadding = 20.dp
    val ChipShape = RoundedCornerShape(14.dp)
    val ContentMinHeight = 420.dp
    val ContentMaxHeight = 720.dp
}

@Composable
internal fun AiBottomSheetHeader(
    title: String,
    onClose: () -> Unit
) {
    // 统一头部:DragHandle + 居中标题 + 右上角关闭(对齐「编辑文字」面板)
    EnhancedSheetHeader(
        title = title,
        onClose = onClose
    )
}

@Composable
internal fun AiBottomSheetSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    GlassOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Search,
                contentDescription = null,
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        label = {
            Text(text = placeholder)
        },
    )
}

@Composable
internal fun AiBottomSheetFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    GlassFilterChip(
        selected = selected,
        onClick = onClick,
        shape = AiBottomSheetDefaults.ChipShape,
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium
            )
        }
    )
}
