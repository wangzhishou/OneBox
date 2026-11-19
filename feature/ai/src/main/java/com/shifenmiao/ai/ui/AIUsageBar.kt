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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.wanbaohe.base.ui.ActionButton
import com.wanbaohe.base.ui.DeleteConfirmDialog
import com.shifenmiao.core.R
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.utils.helper.Clipboard
// 统一使用 core/resources 里的系统 Outline 语义图标：造型一眼可辨，
// 线条仍是项目自绘的 24dp 单色模板，由 ActionButton 统一 size / tint 收口
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Image
import com.t8rin.imagetoolbox.core.resources.icons.NoteAdd
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.Share
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFlag

@Composable
fun AIUsageBar(
    modifier: Modifier = Modifier,
    message: MessageEntity,
    isShowReGenerate: Boolean = false,
    showTokens: Boolean = true,
    // 图标/计数的前景色由容器色系决定：中性卡片传 onSurfaceVariant，
    // 彩色卡片（AI 互聊里 primaryContainer 那一侧）传 onPrimaryContainer，否则图标会糊在底上
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
    onDelete: (String) -> Unit = {},
    reGenerate: () -> Unit = {},
    onShare: (String) -> Unit = {},
    onNoteAdd: () -> Unit = {},
    onCreateCard: () -> Unit = {},
    onReport: (() -> Unit)? = null,
) {
    val showDeleteDialog = remember { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(
            horizontal = AppTheme.dimens.paddingNormal,
            vertical = AppTheme.dimens.paddingSmall
        ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isShowReGenerate) {
            ActionButton(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                contentDescription = "",
                contentColor = contentColor,
                onClick = {
                    reGenerate.invoke()
                }
            )
            Spacer(modifier = Modifier.width(AppTheme.dimens.spaceSmall))
        }
        ActionButton(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ContentCopy,
            contentDescription = "",
            contentColor = contentColor,
            onClick = {
                Clipboard.copy(message.answer)
            }
        )
        Spacer(modifier = Modifier.width(AppTheme.dimens.spaceSmall))
        ActionButton(
            // 原 LineTextCard 是「卡片+横线+小图」的自绘方块，表意含混；
            // 功能是生成图片卡片，换成一眼可辨的 Outline 图片图标
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Image,
            contentDescription = stringResource(R.string.text_card),
            contentColor = contentColor,
            onClick = onCreateCard
        )
        Spacer(modifier = Modifier.width(AppTheme.dimens.spaceSmall))
        ActionButton(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
            contentDescription = "",
            contentColor = contentColor,
            onClick = {
                showDeleteDialog.value = true
            }
        )
        onReport?.let { report ->
            Spacer(modifier = Modifier.width(AppTheme.dimens.spaceSmall))
            ActionButton(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFlag,
                contentDescription = stringResource(R.string.ai_content_report_action),
                contentColor = contentColor,
                onClick = report
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (showTokens) {
            Text(
                text = pluralStringResource(
                    R.plurals.ai_chat_token_count,
                    message.totalTokens.coerceAtLeast(0),
                    message.totalTokens.toString(),
                ),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = contentColor
                )
            )
            Spacer(modifier = Modifier.width(AppTheme.dimens.spaceSmall))
        }
        ActionButton(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Share,
            contentDescription = "",
            contentColor = contentColor,
            onClick = {
                onShare("")
            }
        )
        Spacer(modifier = Modifier.width(AppTheme.dimens.spaceSmall))
        ActionButton(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.NoteAdd,
            contentDescription = "",
            contentColor = contentColor,
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
