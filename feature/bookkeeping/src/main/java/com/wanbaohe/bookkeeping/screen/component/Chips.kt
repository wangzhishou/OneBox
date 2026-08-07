package com.wanbaohe.bookkeeping.screen.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassFilterChip
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQuickTiles

/**
 * 通用状态筛选 chip，用于浅色背景（如底部弹出表单、设置页）。
 * selected 时以 primaryContainer 填充，使用玻璃质感组件。
 */
@Composable
internal fun TypeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    GlassFilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            )
        },
        shape = RoundedCornerShape(12.dp),
        glassSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        glassContainerColor = MaterialTheme.colorScheme.surface,
    )
}

/**
 * 用于筛选的 chip，显示当前激活的分类过滤条件。
 * 右侧带 Apps 图标提示可点击切换，使用玻璃质感组件。
 */
@Composable
internal fun PrimaryTypeChip(
    text: String,
    onClick: () -> Unit,
) {
    GlassSurface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        borderWidth = 0.dp,
        style = GlassStyle.Thin,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
            )
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQuickTiles,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
