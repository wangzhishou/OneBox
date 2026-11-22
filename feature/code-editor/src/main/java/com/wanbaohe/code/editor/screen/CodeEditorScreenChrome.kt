/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

@file:Suppress("FunctionNaming")

package com.wanbaohe.code.editor.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.database.recent_access.entity.RecentAccessEntity
import com.t8rin.imagetoolbox.core.resources.icons.DeleteSweep
import com.t8rin.imagetoolbox.core.resources.icons.NoteAdd
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDataObject
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFileOpen
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSaveAs
import com.wanbaohe.base.ui.button.CancelButton
import com.wanbaohe.base.ui.button.ConfirmButton
import com.wanbaohe.base.ui.button.SecondaryButton
import com.wanbaohe.code.editor.component.CodeEditorUiState
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedDropdownMenu
import androidx.compose.material.icons.Icons as MaterialIcons
import com.t8rin.imagetoolbox.core.resources.Icons as OneBoxIcons
import com.wanbaohe.code.editor.R as EditorR

/** 顶栏保存按钮：有未保存改动时用强调色提示。 */
@Composable
internal fun CodeEditorSaveAction(
    isDirty: Boolean,
    onSave: () -> Unit,
) {
    IconButton(onClick = onSave) {
        Icon(
            imageVector = OneBoxIcons.Outlined.LineSave,
            contentDescription = stringResource(R.string.save_file),
            tint = if (isDirty) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

/** 更多菜单里的动作，页面只负责接线，菜单自己管理展开状态。 */
internal data class CodeEditorMenuActions(
    val onNewFile: () -> Unit,
    val onOpenFile: () -> Unit,
    val onSaveAs: () -> Unit,
    val onOpenRecent: (String) -> Unit,
    val onClearHistory: () -> Unit,
    val onFormatJson: () -> Unit,
    val onExpandAll: () -> Unit,
)

@Composable
internal fun CodeEditorMoreMenu(
    uiState: CodeEditorUiState,
    isEditMode: Boolean,
    actions: CodeEditorMenuActions,
    canFormatJson: Boolean,
    hasFoldedBlocks: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = MaterialIcons.Filled.MoreVert,
                contentDescription = stringResource(R.string.open_menu),
            )
        }
        EnhancedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            if (!isEditMode) {
                MenuFileActions(actions = actions, onDismiss = { expanded = false })
            }
            DropdownMenuItem(
                text = { Text(stringResource(EditorR.string.code_editor_format_json)) },
                leadingIcon = { Icon(OneBoxIcons.Outlined.LineDataObject, contentDescription = null) },
                enabled = canFormatJson,
                onClick = {
                    expanded = false
                    actions.onFormatJson()
                },
            )
            if (hasFoldedBlocks) {
                DropdownMenuItem(
                    text = { Text(stringResource(EditorR.string.code_editor_expand_all)) },
                    leadingIcon = { Icon(OneBoxIcons.Outlined.LineExpandMore, contentDescription = null) },
                    onClick = {
                        expanded = false
                        actions.onExpandAll()
                    },
                )
            }
            if (uiState.historyList.isNotEmpty()) {
                MenuHistorySection(
                    historyList = uiState.historyList,
                    actions = actions,
                    onDismiss = { expanded = false },
                )
            }
        }
    }
}

@Composable
private fun MenuFileActions(
    actions: CodeEditorMenuActions,
    onDismiss: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(stringResource(R.string.new_file)) },
        onClick = {
            onDismiss()
            actions.onNewFile()
        },
        leadingIcon = { Icon(OneBoxIcons.Outlined.NoteAdd, contentDescription = null) },
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.open_file)) },
        onClick = {
            onDismiss()
            actions.onOpenFile()
        },
        leadingIcon = { Icon(OneBoxIcons.Outlined.LineFileOpen, contentDescription = null) },
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.save_as)) },
        onClick = {
            onDismiss()
            actions.onSaveAs()
        },
        leadingIcon = { Icon(OneBoxIcons.Outlined.LineSaveAs, contentDescription = null) },
    )
}

@Composable
private fun MenuHistorySection(
    historyList: List<RecentAccessEntity>,
    actions: CodeEditorMenuActions,
    onDismiss: () -> Unit,
) {
    HorizontalDivider()
    DropdownMenuItem(
        text = {
            Text(
                text = stringResource(R.string.recent_files),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        },
        onClick = {},
        enabled = false,
    )
    historyList.take(HISTORY_MENU_LIMIT).forEach { entry ->
        DropdownMenuItem(
            text = { Text(text = entry.displayName, maxLines = 1) },
            onClick = {
                onDismiss()
                actions.onOpenRecent(entry.uri)
            },
            leadingIcon = { Icon(OneBoxIcons.Outlined.LineHistory, contentDescription = null) },
        )
    }
    DropdownMenuItem(
        text = {
            Text(
                text = stringResource(R.string.clear_history),
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        },
        onClick = {
            onDismiss()
            actions.onClearHistory()
        },
        leadingIcon = {
            Icon(
                imageVector = OneBoxIcons.Outlined.DeleteSweep,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
        },
    )
}

/** 退出前的未保存确认。 */
@Composable
internal fun CodeEditorExitDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSaveAndExit: () -> Unit,
    onDiscard: () -> Unit,
) {
    if (!visible) return
    EnhancedAlertDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.unsaved_changes)) },
        text = { Text(stringResource(R.string.unsaved_changes_message)) },
        confirmButton = {
            ConfirmButton(stringResource(R.string.save_file), onClick = onSaveAndExit)
        },
        dismissButton = {
            // 三个动作统一走胶囊按钮：TextButton 在弹窗里没有按钮外框，
            // 又自带 48dp 最小触控高度，夹在两个胶囊中间会整体下沉，看着像漏排版的链接。
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CancelButton(stringResource(R.string.discard), onClick = onDiscard)
                SecondaryButton(
                    text = stringResource(com.shifenmiao.common.R.string.note_keep_editing),
                    onClick = onDismiss,
                )
            }
        },
    )
}

private const val HISTORY_MENU_LIMIT = 5
