package com.wanbaohe.diceroller.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.wanbaohe.diceroller.R
import com.wanbaohe.diceroller.component.RollRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.t8rin.imagetoolbox.core.resources.icons.DeleteSweep

/**
 * 投掷历史 BottomSheet
 *
 * - 列出历史记录（最新在前），显示时间、骰子类型及总点数
 * - 底部提供"清空"按钮，带二次确认对话框
 *
 * @param records    历史记录列表
 * @param onDismiss  关闭回调
 * @param onClear    清空确认后回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiceHistorySheet(
    records: List<RollRecord>,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
    state: Boolean,
) {
    var showClearDialog by remember { mutableStateOf(false) }

    EnhancedModalBottomSheet(
        onDismiss = {
            onDismiss()
        },
        visible = state,
        dragHandle = {
            // ── 标题行 ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.dice_roller_history),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // 清空按钮
                AnimatedVisibility(visible = records.isNotEmpty()) {
                    TextButton(onClick = { showClearDialog = true }) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.DeleteSweep,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = stringResource(R.string.dice_roller_clear_history),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // ── 记录列表 ────────────────────────────────────────────────────
            if (records.isEmpty()) {
                Spacer(Modifier.height(32.dp))
                Text(
                    text = stringResource(R.string.dice_roller_history_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp, vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(records, key = { _, r -> r.id }) { _, record ->
                        HistoryRecordItem(record = record)
                    }
                }
            }
        }
    }

    // ── 二次确认对话框 ────────────────────────────────────────────────────────
    EnhancedAlertDialog(
        visible = showClearDialog,
        onDismissRequest = { showClearDialog = false },
        containerColor = AppTheme.colors.getContainerSurfaceColor(),
        title = {
            Text(
                stringResource(R.string.dice_roller_clear_history),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                stringResource(R.string.dice_roller_clear_confirm),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = {
                showClearDialog = false
                onClear()
                onDismiss()
            }) {
                Text(
                    stringResource(R.string.dice_roller_confirm),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { showClearDialog = false }) {
                Text(stringResource(R.string.dice_roller_cancel))
            }
        }
    )
}

// ─── 单条历史记录 Item ─────────────────────────────────────────────────────────

private val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
private val dateFmt = SimpleDateFormat("MM-dd", Locale.getDefault())

@Composable
private fun HistoryRecordItem(record: RollRecord) {
    GlassCard(
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        containerAlpha = 0.3f,
        borderWidth = 0.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 骰子类型 + 每颗结果
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val typeLabel = record.dice.firstOrNull()?.type?.label ?: "D6"
                val countLabel = "${record.dice.size}×$typeLabel"
                Text(
                    text = countLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = record.dice.joinToString("  ") { it.value.toString() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 时间 + 总点数
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${stringResource(R.string.dice_roller_total)}: ${record.total}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${dateFmt.format(Date(record.timestamp))} ${timeFmt.format(Date(record.timestamp))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

