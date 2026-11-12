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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.ai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.wanbaohe.base.ui.ActionButton
import com.wanbaohe.base.ui.DeleteConfirmDialog
import com.shifenmiao.core.R
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.NoteAdd
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTextCard

@Composable
fun AIUsageBar(
    modifier: Modifier = Modifier,
    message: MessageEntity,
    isShowReGenerate: Boolean = false,
    onDelete: (String) -> Unit = {},
    reGenerate: () -> Unit = {},
    onShare: (String) -> Unit = {},
    onNoteAdd: () -> Unit = {},
    onCreateCard: () -> Unit = {},
) {
    val showDeleteDialog = remember { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(
            horizontal = AppTheme.dimens.paddingNormal,
            vertical = AppTheme.dimens.paddingNormal
        ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isShowReGenerate) {
            ActionButton(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                contentDescription = "",
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                onClick = {
                    reGenerate.invoke()
                }
            )
            Spacer(modifier = Modifier.width(AppTheme.dimens.spaceSmall))
        }
        ActionButton(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
            contentDescription = "",
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            onClick = {
                Clipboard.copy(message.answer)
            }
        )
        Spacer(modifier = Modifier.width(AppTheme.dimens.spaceSmall))
        ActionButton(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTextCard,
            contentDescription = stringResource(R.string.text_card),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            onClick = onCreateCard
        )
        Spacer(modifier = Modifier.width(AppTheme.dimens.spaceSmall))
        ActionButton(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
            contentDescription = "",
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            onClick = {
                showDeleteDialog.value = true
            }
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = pluralStringResource(
                R.plurals.ai_chat_token_count,
                message.totalTokens.coerceAtLeast(0),
                message.totalTokens.toString(),
            ),
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        )
        Spacer(modifier = Modifier.width(AppTheme.dimens.spaceSmall))
        ActionButton(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShare,
            contentDescription = "",
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            onClick = {
                onShare("")
            }
        )
        Spacer(modifier = Modifier.width(AppTheme.dimens.spaceSmall))
        ActionButton(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.NoteAdd,
            contentDescription = "",
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            onClick = {
                onNoteAdd()
            }
        )
    }
    if (showDeleteDialog.value) {
        DeleteConfirmDialog(
            showDeleteDialogState = showDeleteDialog,
            onDelete = {
                onDelete.invoke(message.completionId)
            }
        )
    }
}
