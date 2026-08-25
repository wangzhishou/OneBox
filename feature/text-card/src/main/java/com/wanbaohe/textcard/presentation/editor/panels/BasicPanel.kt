package com.wanbaohe.textcard.presentation.editor.panels

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.container
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EmojiEmotions

/**
 * 「基础」操作面板(对标图片创作侧栏动作):
 * 添加文字 / 添加装饰(打开装饰选择 Sheet)/ 删除选中元素(未选中禁用)。
 */
@Composable
fun BasicPanel(
    component: TextCardComponent,
    onAddDecoration: () -> Unit,
) {
    PanelTitle(R.string.textcard_tab_basic)

    BasicActionRow(
        icon = MaterialIcons.Outlined.Add,
        label = stringResource(R.string.textcard_add_text),
        enabled = true,
        onClick = component::addTextBlock
    )
    BasicActionRow(
        icon = MaterialIcons.Outlined.EmojiEmotions,
        label = stringResource(R.string.textcard_add_decoration),
        enabled = true,
        onClick = onAddDecoration
    )
    BasicActionRow(
        icon = MaterialIcons.Outlined.Delete,
        label = stringResource(R.string.textcard_delete_selected),
        enabled = component.selectedElementId != null,
        onClick = {
            component.selectedElementId?.let(component::removeElement)
        }
    )
}

@Composable
private fun BasicActionRow(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .container(shape = ShapeDefaults.large)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
