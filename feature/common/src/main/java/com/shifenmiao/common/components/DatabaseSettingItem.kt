package com.shifenmiao.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCloudUpload
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCloudDownload
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassMedium

/**
 * 简单的两列「备份 / 恢复」按钮行，不包含任何业务逻辑。
 *
 * 背景与「我的」页设置卡片、胶囊按钮共用 glassMedium + surfaceContainerLow。
 *
 * @param onClicked 0 = 备份, 1 = 恢复
 */
@Composable
fun DatabaseSettingItem(
    modifier: Modifier = Modifier,
    onClicked: (Int) -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DatabaseActionButton(
            text = stringResource(R.string.data_backup),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudUpload,
            onClick = { onClicked(0) },
            modifier = Modifier.weight(1f),
        )
        DatabaseActionButton(
            text = stringResource(R.string.data_restore),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudDownload,
            onClick = { onClicked(1) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DatabaseActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(AppTheme.dimens.cornerRadiusSmall)
    Box(
        modifier = modifier
            .clip(shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .glassMedium(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = shape,
            )
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(18.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
