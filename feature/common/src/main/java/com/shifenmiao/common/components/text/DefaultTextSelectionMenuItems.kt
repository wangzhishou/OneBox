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
 * 选中文字后的标准四项动作:复制、分享、做成文字卡片、存为笔记。
 * AI 聊天、博客详情、笔记详情共用,保证三处长按弹出的工具条完全一致。
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
            label = stringResource(R.string.share_title),
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
            label = stringResource(R.string.nav_notebook),
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
