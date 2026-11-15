package com.shifenmiao.common.markdown

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.shifenmiao.common.components.text.TextSelectionAction
import com.shifenmiao.common.components.text.TextSelectionMenuItem
import com.shifenmiao.common.components.text.TextSelectionToolbar
import com.wanbaohe.markdown.OneBoxMarkdownSelectionActions
import com.wanbaohe.markdown.OneBoxMarkdownSelectionInfo

@Composable
internal fun OneBoxMarkdownSelectionToolbar(
    info: OneBoxMarkdownSelectionInfo,
    actions: OneBoxMarkdownSelectionActions,
    items: List<TextSelectionMenuItem>,
) {
    BackHandler { actions.dismiss() }
    TextSelectionToolbar(
        anchorBounds = info.boundsInWindow,
        preferAbove = true,
        actions = items.map { item ->
            TextSelectionAction(
                key = item.key,
                label = item.label,
                icon = item.icon,
                showLabel = true,
                onClick = {
                    item.onSelected(info.text)
                    actions.dismiss()
                },
            )
        },
    )
}
