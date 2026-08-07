package com.wanbaohe.decisionwheel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalTopSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.decisionwheel.R
import com.wanbaohe.decisionwheel.component.DecisionWheel
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Delete

/**
 * 转盘列表对话框
 */
@Composable
fun WheelListDialog(
    visible: Boolean = true,
    wheels: List<DecisionWheel>,
    currentWheelId: String?,
    onDismiss: () -> Unit,
    onSelectWheel: (DecisionWheel) -> Unit,
    onDeleteWheel: (String) -> Unit,
    onCreateWheel: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf<String?>(null) }

    EnhancedModalTopSheet(
        visible = visible,
        onDismiss = { onDismiss() }
    ) {
        Column(
            modifier = Modifier.padding(
                start = AppTheme.dimens.paddingNormal,
                top = AppTheme.dimens.paddingNormal,
                end = AppTheme.dimens.paddingNormal,
                bottom = AppTheme.dimens.paddingNormal
            ),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceNormal, Alignment.Top)
        ) {
            // 标题栏
            Text(
                text = stringResource(R.string.my_wheels),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // 转盘列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(wheels) { wheel ->
                    WheelListItem(
                        wheel = wheel,
                        isSelected = wheel.id == currentWheelId,
                        onClick = { onSelectWheel(wheel) },
                        onDelete = { showDeleteConfirm = wheel.id }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FilledTonalButton(
                    onClick = onCreateWheel,
                    colors = AppTheme.colors.getPrimaryButtonColors(),
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.create_wheel)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.create_wheel))
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteConfirm != null) {
        AlertDialog(
            containerColor = AppTheme.colors.getContainerSurfaceColor(),
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text(stringResource(R.string.delete_wheel_confirm)) },
            confirmButton = {
                GlassTonalButton(
                    onClick = {
                        showDeleteConfirm?.let { onDeleteWheel(it) }
                        showDeleteConfirm = null
                    },
                    colors = AppTheme.colors.getSecondaryContainerButtonColors()
                ) {
                    Text(
                        text = stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                FilledTonalButton(
                    onClick = { showDeleteConfirm = null },
                    colors = AppTheme.colors.getSurfaceContainerButtonColors()
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/**
 * 转盘列表项
 */
@Composable
fun WheelListItem(
    wheel: DecisionWheel,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = wheel.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                // 配色预览（扁平：方块色卡）。
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val fallback = MaterialTheme.colorScheme.primary
                    wheel.options.take(6).forEach { option ->
                        val c = option.color.takeIf { it != Color.Unspecified } ?: fallback
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(c)
                        )
                    }
                    Text(
                        text = stringResource(R.string.options_count, wheel.options.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
