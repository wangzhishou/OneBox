package com.shifenmiao.common.components.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import com.shifenmiao.base.provider.LocalDataDraftHelper
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.core.R
import com.shifenmiao.model.ListItemType
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.NoteAdd
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTextCard
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
            icon = Icons.Rounded.ContentCopy,
            onSelected = { text -> Clipboard.copy(text) },
        ),
        TextSelectionMenuItem(
            key = "share",
            label = stringResource(R.string.share_url),
            icon = Icons.Outlined.LineShare,
            onSelected = { text -> appComponent.shareText(text) },
        ),
        TextSelectionMenuItem(
            key = "image",
            label = stringResource(R.string.button_export_image),
            icon = Icons.Outlined.LineTextCard,
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
