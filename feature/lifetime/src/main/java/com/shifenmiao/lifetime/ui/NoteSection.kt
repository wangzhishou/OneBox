package com.shifenmiao.lifetime.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.lifetime.R
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionHeader
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.NoteAdd

/**
 * 备注区域：默认展示备注文本，提供「编辑 / 删除」按钮；空态显示「添加备注」。
 *
 * - 展示态：备注文本（如有）+ 右上角 Edit / Delete 按钮
 * - 空态：「+ 添加备注」按钮
 * - 编辑态：OneBoxOutlinedTextField + 保存 / 取消
 *
 * 通过 [onSave] / [onDelete] 回调上抛，调用方决定持久化策略。
 */
@Composable
fun NoteSection(
    note: String?,
    onSave: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    sectionTitle: String = stringResource(R.string.lifetime_milestone_note),
    placeholder: String = stringResource(R.string.lifetime_milestone_note_placeholder),
) {
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var draft by rememberSaveable { mutableStateOf(note.orEmpty()) }

    // 当外部 note 变化（保存成功、首次加载等）且不在编辑态时同步草稿
    LaunchedEffect(note, isEditing) {
        if (!isEditing) draft = note.orEmpty()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                OneBoxSectionHeader(title = sectionTitle)
            }
            if (isEditing) {
                TextButton(onClick = { isEditing = false }) {
                    Text(
                        text = stringResource(R.string.lifetime_cancel),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                TextButton(onClick = {
                    onSave(draft.trim())
                    isEditing = false
                }) {
                    Text(
                        text = stringResource(R.string.lifetime_note_save),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                    )
                }
            } else {
                if (note.isNullOrBlank()) {
                    TextButton(onClick = { isEditing = true }) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.NoteAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = stringResource(R.string.lifetime_note_add),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                } else {
                    IconButton(onClick = {
                        draft = note
                        isEditing = true
                    }) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.lifetime_note_edit),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.lifetime_note_delete),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        if (isEditing) {
            OneBoxOutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                placeholder = { Text(placeholder) },
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = OneBoxDesignSystem.sectionCardShape,
                containerAlpha = 0.18f,
            ) {
                Text(
                    text = note?.takeIf { it.isNotBlank() } ?: stringResource(R.string.lifetime_note_empty),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (note.isNullOrBlank()) FontWeight.Normal else FontWeight.Medium
                    ),
                    color = if (note.isNullOrBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .padding(OneBoxDesignSystem.cardPadding),
                )
            }
        }
    }
}
