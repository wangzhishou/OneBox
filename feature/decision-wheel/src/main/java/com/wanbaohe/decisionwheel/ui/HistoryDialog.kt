package com.wanbaohe.decisionwheel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.database.decision_wheel.entity.WheelHistoryEntity
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalTopSheet
import com.wanbaohe.decisionwheel.R
import java.text.SimpleDateFormat
import java.util.*
import com.t8rin.imagetoolbox.core.resources.icons.Delete

/**
 * 历史记录对话框
 */
@Composable
fun HistoryDialog(
    visible: Boolean = false,
    historyList: List<WheelHistoryEntity>,
    onDismiss: () -> Unit,
    onClearHistory: () -> Unit
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.history),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (historyList.isNotEmpty()) {
                    IconButton(onClick = onClearHistory) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.clear_history),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (historyList.isEmpty()) {
                // 空状态
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_history),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // 历史记录列表
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(historyList) { history ->
                        HistoryItem(history = history)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FilledTonalButton(
                    onClick = onDismiss,
                    colors = AppTheme.colors.getSurfaceContainerButtonColors(),
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}

/**
 * 历史记录项
 */
@Composable
fun HistoryItem(
    history: WheelHistoryEntity
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val formattedTime = dateFormat.format(Date(history.timestamp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // 结果图标
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.target_emoji),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Column {
                    Text(
                        text = history.selectedOptionName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
