package com.shifenmiao.common.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.R
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCloudUpload
import com.wanbaohe.core.ui.widget.system.OneBoxListItem
import com.wanbaohe.core.ui.widget.system.OneBoxThemedIconBadge

/**
 * 「备份与恢复」设置行 —— 只是行，不带卡片：由「数据」分组统一包卡片。
 *
 * 样式对齐 [BaseSettingItem]（图标徽标 + 标题 + 副标题 + 尾部箭头），
 * 这样它和「关于」分组里的行看起来是同一套东西。
 */
@Composable
fun BackupCenterSettingRow(
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OneBoxListItem(
        modifier = modifier,
        onClick = onOpen,
        headlineContent = {
            Text(
                text = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.backup_center),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        subtitle = {
            Text(
                text = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.backup_center_sub),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.64f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leadingContent = {
            OneBoxThemedIconBadge(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudUpload,
                themeIndex = 0,
            )
        },
        trailingContent = {
            Icon(
                modifier = Modifier.size(14.dp),
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.56f),
            )
        },
    )
}
