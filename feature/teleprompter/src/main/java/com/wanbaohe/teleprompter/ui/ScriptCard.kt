package com.wanbaohe.teleprompter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.database.teleprompter.entity.TeleprompterScriptEntity
import com.wanbaohe.teleprompter.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.resources.icons.PlayCircle
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMore

@Composable
fun ScriptCard(
    script: TeleprompterScriptEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    GlassCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        containerAlpha = 0.3f,
        borderWidth = 0.dp,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = script.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // 更多菜单（Box 锚定，确保菜单从按钮位置弹出）
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMore,
                            contentDescription = stringResource(R.string.teleprompter_more_actions),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.teleprompter_edit_script)) },
                            onClick = { showMenu = false; onEdit() },
                            leadingIcon = {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                                    contentDescription = null,
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.teleprompter_delete),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            },
                            onClick = { showMenu = false; onDelete() },
                            leadingIcon = {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = script.content.take(100).replace("\n", " "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(12.dp))

            // ── 底部：字数 + 时间 + 外露播放按钮 ───────────────────
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.teleprompter_word_count, script.wordCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = formatRelativeTime(script.updatedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }

                // ★ 外露播放按钮（图标 + 文案，一眼看懂）
                EnhancedButton(
                    onClick = onPlay,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.teleprompter_play),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000L -> "刚刚"
        diff < 3_600_000L -> "${diff / 60_000}分钟前"
        diff < 86_400_000L -> "${diff / 3_600_000}小时前"
        diff < 604_800_000L -> "${diff / 86_400_000}天前"
        else -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
    }
}
