package com.shifenmiao.common.markdown

import androidx.compose.runtime.Composable
import com.shifenmiao.common.components.text.TextSelectionMenuItem
import com.wanbaohe.markdown.OneBoxMarkdownComponents
import io.noties.markwon.plugins.codeblock.CodeBlockClickListener

fun oneBoxMarkdownComponents(
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    selectionItems: List<TextSelectionMenuItem> = emptyList(),
    codeBlockClickListener: CodeBlockClickListener? = null,
): OneBoxMarkdownComponents = OneBoxMarkdownComponents(
    header = header,
    footer = footer,
    codeBlockHeader = codeBlockClickListener?.let { listener ->
        { info -> OneBoxMarkdownCodeBlockHeader(info, listener) }
    },
    selectionToolbar = if (selectionItems.isEmpty()) {
        null
    } else {
        { info, actions -> OneBoxMarkdownSelectionToolbar(info, actions, selectionItems) }
    },
)
