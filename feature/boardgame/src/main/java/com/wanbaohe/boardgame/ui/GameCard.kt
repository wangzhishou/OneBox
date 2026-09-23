package com.wanbaohe.boardgame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowForwardIos
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.wanbaohe.boardgame.model.GameCardData
import com.wanbaohe.boardgame.model.GameCardLabels
import com.wanbaohe.boardgame.model.GameCardPlayer
import java.text.DateFormat
import java.util.Date

/**
 * 棋谱库历史对局卡片,棋种无关。
 * 点卡片 = 继续对局/进入复盘(由 [onContinue] 语义决定),右下角入口 = 复盘/回放。
 * 重命名/删除确认弹窗内聚在卡片里,文案由 [labels] 传入。
 */
@Composable
fun GameCard(
    data: GameCardData,
    labels: GameCardLabels,
    onContinue: () -> Unit,
    onAnalysis: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    GlassSurface(
        modifier = modifier.fillMaxWidth().clickable { onContinue() },
        style = GlassStyle.Medium,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = data.modeBadge.uppercase(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp)
                    )
                }
                Text(
                    text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(data.updatedAt)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PlayerBadge(player = data.firstPlayer)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))))
                    Text(data.vsLabel, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp))
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), Color.Transparent))))
                }

                PlayerBadge(player = data.secondPlayer)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = data.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = data.statusText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // 副标题：找局时最先看的两项信息——手数与结果。缺了它们，列表里全是"已结束"。
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = data.plyCountText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (data.resultText.isNotBlank()) {
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = data.resultText,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: edit + delete icons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    val subtleTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    IconButton(onClick = { showRenameDialog = true }) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = labels.renameContentDescription,
                            tint = subtleTint,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = labels.deleteContentDescription,
                            tint = subtleTint,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                // Right: analysis link
                Row(
                    modifier = Modifier.clickable { onAnalysis() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = data.actionLabel,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Icon(Icons.Outlined.LineArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(labels.deleteConfirmTitle) },
            text = { Text(labels.deleteConfirmMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text(labels.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(labels.cancel)
                }
            },
        )
    }

    if (showRenameDialog) {
        RenameGameDialog(
            currentTitle = data.title,
            labels = labels,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newTitle ->
                showRenameDialog = false
                onRename(newTitle)
            },
        )
    }
}

@Composable
private fun PlayerBadge(
    player: GameCardPlayer,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(player.badgeBackground, CircleShape)
                .border(1.dp, player.badgeBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(player.badge, color = player.badgeColor, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(player.name, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun RenameGameDialog(
    currentTitle: String,
    labels: GameCardLabels,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(currentTitle) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(labels.renameTitle) },
        text = {
            GlassOutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(labels.renameHint) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank(),
            ) {
                Text(labels.confirm)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(labels.cancel)
            }
        },
    )
}
