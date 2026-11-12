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

package com.shifenmiao.common.components.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import com.shifenmiao.base.provider.LocalDataDraftHelper
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.core.R
import com.shifenmiao.model.ListItemType
import com.t8rin.imagetoolbox.core.resources.Icons
// 与 AI 聊天底部操作栏保持同一套系统 Outline 语义图标(项目自绘线条)，
// 避免同一功能在两处出现不同造型
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.Image
import com.t8rin.imagetoolbox.core.resources.icons.NoteAdd
import com.t8rin.imagetoolbox.core.resources.icons.Share
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.wanbaohe.core.ui.utils.navigation.Screen
import kotlinx.coroutines.launch

/**
 * 选中文字后的标准四项动作:复制、分享、图片、笔记。
 * AI 聊天、AI 互聊、博客详情、笔记详情共用同一套全局工具条。
 * 标签尽量复用已有全语言译文(copy / share_url / button_export_image)，
 * 只有「笔记」没有合适的短词，单独补了 key 并做全语言本地化。
 */
@Composable
fun defaultTextSelectionMenuItems(
    appComponent: AppComponent,
    onNavigate: (Screen) -> Unit,
): List<TextSelectionMenuItem> {
    val dataDraftHelper = LocalDataDraftHelper.current
    val scope = rememberCoroutineScope()

    return listOf(
        TextSelectionMenuItem(
            key = "copy",
            label = stringResource(R.string.copy),
            icon = Icons.Outlined.ContentCopy,
            onSelected = { text -> Clipboard.copy(text) },
        ),
        TextSelectionMenuItem(
            key = "share",
            label = stringResource(R.string.share_url),
            icon = Icons.Outlined.Share,
            onSelected = { text -> appComponent.shareText(text) },
        ),
        TextSelectionMenuItem(
            key = "image",
            label = stringResource(R.string.button_export_image),
            icon = Icons.Outlined.Image,
            onSelected = { text ->
                onNavigate(Screen.TextCard(initialText = text))
            },
        ),
        TextSelectionMenuItem(
            key = "note",
            label = stringResource(R.string.text_selection_action_note),
            icon = Icons.Outlined.NoteAdd,
            onSelected = { text ->
                scope.launch {
                    val draftId = dataDraftHelper.createDraft(
                        draftType = ListItemType.NOTE.id,
                        data = text,
                    )
                    onNavigate(Screen.CreateNote(draftId = draftId))
                }
            },
        ),
    )
}
