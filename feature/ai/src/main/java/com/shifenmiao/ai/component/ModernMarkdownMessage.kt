package com.shifenmiao.ai.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.components.text.TextSelectionMenuItem
import com.shifenmiao.common.markdown.oneBoxMarkdownComponents
import com.wanbaohe.markdown.OneBoxMarkdown
import io.noties.markwon.plugins.codeblock.CodeBlockClickListener

val LocalAiMarkdownSelectionItems = compositionLocalOf<List<TextSelectionMenuItem>> { emptyList() }

@Composable
fun ModernMarkdownMessage(
    source: String,
    isStreaming: Boolean,
    codeBlockClickListener: CodeBlockClickListener,
    modifier: Modifier = Modifier,
    showCursor: Boolean = false,
    textStyle: TextStyle? = null,
    contentColor: Color? = null,
    blockSpacing: Dp? = null,
) {
    val selectionItems = LocalAiMarkdownSelectionItems.current
    val baseComponents = oneBoxMarkdownComponents(
        selectionItems = selectionItems,
        codeBlockClickListener = codeBlockClickListener,
    )
    val components = if (showCursor && isStreaming) {
        baseComponents.copy(footer = { StreamingMarkdownCursor() })
    } else {
        baseComponents
    }
    OneBoxMarkdown(
        markdown = source,
        modifier = modifier,
        isStreaming = isStreaming,
        enableScroll = false,
        textStyle = textStyle,
        contentColor = contentColor,
        blockSpacing = blockSpacing,
        components = components,
    )
}

@Composable
private fun StreamingMarkdownCursor() {
    val transition = rememberInfiniteTransition(label = "markdown_cursor")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "markdown_cursor_alpha",
    )
    Box(
        modifier = Modifier
            .size(width = 8.dp, height = 20.dp)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = alpha))
    )
}
