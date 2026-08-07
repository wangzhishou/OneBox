package com.wanbaohe.speedtest.screen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.shifenmiao.theme.AppTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wanbaohe.speedtest.R
import com.wanbaohe.speedtest.data.SpeedTestConfig

import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Edit

@Composable
fun SpeedTestConfigSheet(
    visible: Boolean,
    configs: List<SpeedTestConfig>,
    activeConfigId: Long,
    onSelect: (Long) -> Unit,
    onEdit: (SpeedTestConfig) -> Unit,
    onDelete: (Long) -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    var deletingId by remember { mutableStateOf<Long?>(null) }

    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // ── 标题行（无下划线） ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 8.dp, top = 20.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.speed_test_config_sheet_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.getPrimaryTextColor(),
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onAdd,
                    colors = AppTheme.colors.buttonColors()
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(text = stringResource(R.string.speed_test_config_add_btn))
                }
            }

            // ── 配置列表（无分割线） ──────────────────────────────────────
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp)
            ) {
                items(configs, key = { it.id }) { config ->
                    ConfigItem(
                        config = config,
                        isActive = config.isActive,
                        onSelect = { onSelect(config.id) },
                        onEdit = { onEdit(config) },
                        onDeleteRequest = { deletingId = config.id }
                    )
                }
            }
        }
    }

    SpeedTestDeleteConfirmDialog(
        visible = deletingId != null,
        configName = configs.find { it.id == deletingId }?.name ?: "",
        onConfirm = {
            deletingId?.let(onDelete)
            deletingId = null
        },
        onDismiss = { deletingId = null }
    )
}

@Composable
private fun ConfigItem(
    config: SpeedTestConfig,
    isActive: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    val primaryColor = AppTheme.colors.getPrimaryColor()
    val bgColor = if (isActive)
        AppTheme.colors.getSecondaryContainerButtonColors().containerColor.copy(alpha = 0.5f)
    else
        AppTheme.colors.getContainerSurfaceColor()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable(onClick = onSelect)
            .padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 激活指示圆点（无边框，全填充）
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) primaryColor
                    else AppTheme.colors.getGrayColor()
                )
        )

        Spacer(Modifier.width(14.dp))

        // 配置文字信息
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = config.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isActive) primaryColor
                else AppTheme.colors.getPrimaryTextColor()
            )
            Text(
                text = config.testUrl,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.getOnInactiveContainerColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(
                    R.string.speed_test_config_info,
                    config.estimatedDataMb,
                    config.durationSeconds
                ),
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.getOnInactiveContainerColor().copy(alpha = 0.7f)
            )
        }

        // 编辑按钮
        IconButton(
            onClick = onEdit,
            modifier = Modifier.size(36.dp),
            colors = AppTheme.colors.iconButtonColors()
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                contentDescription = stringResource(R.string.speed_test_config_edit_desc),
                tint = AppTheme.colors.getOnInactiveContainerColor(),
                modifier = Modifier.size(16.dp)
            )
        }

        // 删除按钮（仅用户自定义配置显示）
        if (!config.isPreset) {
            IconButton(
                onClick = onDeleteRequest,
                modifier = Modifier.size(36.dp),
                colors = AppTheme.colors.iconButtonColors()
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.speed_test_config_delete_desc),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            Spacer(Modifier.width(36.dp))
        }
    }
}

@Composable
private fun SpeedTestDeleteConfirmDialog(
    visible: Boolean,
    configName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    EnhancedAlertDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.speed_test_config_delete_dialog_title)) },
        text = { Text(stringResource(R.string.speed_test_config_delete_dialog_msg, configName)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = AppTheme.colors.buttonColors()
            ) {
                Text(
                    stringResource(R.string.speed_test_config_confirm_delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = AppTheme.colors.buttonColors()
            ) { Text(stringResource(R.string.speed_test_cancel)) }
        }
    )
}
