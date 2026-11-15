package com.shifenmiao.common.markdown

import androidx.compose.runtime.Composable
import com.shifenmiao.common.components.text.TextSelectionMenuItem
import com.wanbaohe.markdown.OneBoxMarkdownCodeActions
import com.wanbaohe.markdown.OneBoxMarkdownComponents

fun oneBoxMarkdownComponents(
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    selectionItems: List<TextSelectionMenuItem> = emptyList(),
    codeActions: OneBoxMarkdownCodeActions? = null,
): OneBoxMarkdownComponents = OneBoxMarkdownComponents(
    header = header,
    footer = footer,
    codeBlockHeader = codeActions?.let { actions ->
        { info -> OneBoxMarkdownCodeBlockHeader(info, actions) }
    },
    selectionToolbar = if (selectionItems.isEmpty()) {
        null
    } else {
        { info, actions -> OneBoxMarkdownSelectionToolbar(info, actions, selectionItems) }
    },
)
