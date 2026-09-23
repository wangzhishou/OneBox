package com.wanbaohe.boardgame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.boardgame.model.AiPickerItem
import com.wanbaohe.boardgame.model.AiSourceTag

/**
 * 走棋 AI 来源选择面板,棋种无关。
 * 来源项(工作模型/引擎等)由调用方组装成 [AiPickerItem] 列表,
 * 标签文案(需登录/扣积分/免费)与颜色也由调用方决定。
 */
@Composable
fun AiPickerBottomSheet(
    visible: Boolean,
    title: String,
    description: String,
    items: List<AiPickerItem>,
    selectedItem: AiPickerItem?,
    onSelected: (AiPickerItem) -> Unit,
    onDismiss: () -> Unit,
) {
    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss() },
        dragHandle = {},
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = AppTheme.dimens.spaceLarge, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))

            items.forEach { item ->
                SourceRow(
                    item = item,
                    selected = item == selectedItem,
                    onClick = {
                        onSelected(item)
                        onDismiss()
                    },
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SourceRow(
    item: AiPickerItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (item.subtitle.isNotBlank()) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SourceTags(tags = item.tags)
        }
        if (item.trailingIcon != null) {
            Icon(
                imageVector = item.trailingIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
    }
}

@Composable
private fun SourceTags(tags: List<AiSourceTag>) {
    if (tags.isEmpty()) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp),
    ) {
        tags.forEach { tag ->
            SourceTag(tag = tag)
        }
    }
}

@Composable
private fun SourceTag(tag: AiSourceTag) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = tag.color.copy(alpha = 0.14f),
        shadowElevation = 0.dp,
    ) {
        Text(
            text = tag.text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
            ),
            color = tag.color.copy(alpha = 0.92f),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}
