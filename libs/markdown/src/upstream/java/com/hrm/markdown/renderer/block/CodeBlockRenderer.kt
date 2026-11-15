package com.hrm.markdown.renderer.block

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import com.hrm.codehigh.renderer.CodeBlock
import com.hrm.markdown.renderer.LocalIsStreaming
import com.hrm.markdown.renderer.LocalMarkdownComponents
import com.hrm.markdown.renderer.LocalMarkdownStyle
import com.hrm.markdown.renderer.MarkdownCodeBlockInfo
import com.hrm.markdown.renderer.internal.LocalMarkdownInteractions

/**
 * 围栏代码块渲染器 (``` 或 ~~~)
 *
 * 支持通过 info-string 的 `{...}` 属性语法控制：
 * - **title**: 标题栏，如 `{title="main.kt"}`
 * - **linenos / lineNumbers**: 行号显示
 * - **highlight / hl_lines**: 高亮指定行，如 `{highlight="2,5-7"}`
 */
@Composable
internal fun FencedCodeBlockRenderer(
    text: String,
    language: String,
    title: String?,
    showLineNumbers: Boolean,
    startLine: Int,
    highlightedLines: Set<Int>,
    modifier: Modifier = Modifier,
) {
    CodeBlockText(
        text = text,
        language = language,
        title = title,
        showLineNumbers = showLineNumbers,
        startLine = startLine,
        highlightedLines = highlightedLines,
        modifier = modifier,
    )
}

/**
 * 缩进代码块渲染器
 */
@Composable
internal fun IndentedCodeBlockRenderer(
    text: String,
    modifier: Modifier = Modifier,
) {
    CodeBlockText(
        text = text,
        language = "",
        title = null,
        showLineNumbers = true,
        startLine = 1,
        highlightedLines = emptySet(),
        modifier = modifier,
    )
}

@Composable
private fun CodeBlockText(
    text: String,
    language: String,
    title: String?,
    showLineNumbers: Boolean,
    startLine: Int,
    highlightedLines: Set<Int>,
    modifier: Modifier = Modifier,
) {
    val theme = LocalMarkdownStyle.current
    val isStreaming = LocalIsStreaming.current
    val controller = LocalMarkdownInteractions.current
    val onTokenClick = remember(controller, controller?.codeTokenEnabled) {
        controller?.takeIf { it.codeTokenEnabled }?.let { it::codeToken }
    }

    val slot = LocalMarkdownComponents.current.codeBlockHeader
    val body: @Composable (Modifier) -> Unit = { bodyModifier ->
        CodeBlock(
            code = text.ifEmpty { " " },
            language = language,
            title = title.orEmpty(),
            modifier = bodyModifier.fillMaxWidth(),
            isStreaming = isStreaming,
            colors = theme.codeColors,
            typography = theme.codeTypography,
            layout = theme.codeLayout,
            onTokenClick = onTokenClick,
            showCopyButton = slot == null,
            showLineNumbers = showLineNumbers,
            startLine = startLine,
            highlightedLines = highlightedLines,
        )
    }
    if (slot == null) {
        body(modifier)
    } else {
        Column(modifier = modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth().height(slot.height).clipToBounds()) {
                slot.content(MarkdownCodeBlockInfo(text, language, title))
            }
            body(Modifier)
        }
    }
}
