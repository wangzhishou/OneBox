package com.halilibo.richtext.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.ui.button.CopyCodeActionButton
import com.halilibo.richtext.ui.button.CreateWidgetActionButton
import com.halilibo.richtext.ui.button.RunCodeActionButton
import com.halilibo.richtext.ui.button.SaveCodeActionButton
import com.halilibo.richtext.ui.a2ui.A2uiCodeBlock
import com.halilibo.richtext.ui.mermaid.MermaidCodeBlock
import com.shifenmiao.model.node.AstFencedCodeBlock
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import io.noties.markwon.SpannableBuilder
import io.noties.markwon.locator.PrismGrammarLocator
import io.noties.markwon.plugins.codeblock.CodeBlockClickListener
import io.noties.markwon.prism4j.Prism4jThemeMaterial3
import io.noties.markwon.prism4j.syntax.Prism4jSyntaxHighlight
import io.noties.markwon.utils.MarkdownStringUtils
import io.noties.prism4j.Prism4j
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RichTextScope.FencedCodeBlock(
    codeBlock: AstFencedCodeBlock,
    codeBlockClickListener: CodeBlockClickListener?
) {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.medium
            )
            .padding(
                vertical = 8.dp,
                horizontal = 10.dp
            )
    ) {
        if (codeBlock.annotatedString != null) {
            // 使用已解析的带语法高亮的AnnotatedString
            Text(
                style = MaterialTheme.typography.bodySmall,
                text = codeBlock.annotatedString!!,
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .fillMaxWidth()
            )
        } else {
            // 使用普通文本
            CodeBlock(
                text = codeBlock.literal.trim()
            )
        }

        // 如果有语言信息，显示在右上角
        if (codeBlock.info.isNotEmpty()) {
            Text(
                text = codeBlock.info,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier
                    .padding(4.dp)
                    .align(Alignment.TopEnd)
            )
        }
    }
}


@Composable
fun AsyncFencedCodeBlock(
    codeBlock: AstFencedCodeBlock,
    enableAsyncHighlight: Boolean = true,
    codeBlockClickListener: CodeBlockClickListener?
) {
    val normalizedLanguage = codeBlock.info
        .trim()
        .substringBefore(' ')
        .substringBefore('\t')
        .lowercase()

    // Mermaid 代码块使用专用渲染组件
    if (normalizedLanguage == "mermaid") {
        MermaidCodeBlock(
            codeBlock = codeBlock,
            codeBlockClickListener = codeBlockClickListener
        )
        return
    }

    // a2ui 代码块使用 A2UI 渲染组件（"uijson" 为旧标记，向后兼容）
    if (normalizedLanguage == "a2ui" || normalizedLanguage == "uijson") {
        A2uiCodeBlock(
            codeBlock = codeBlock,
            codeBlockClickListener = codeBlockClickListener
        )
        return
    }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // 先使用未高亮文本，高亮后更新
    var highlightedText by remember { mutableStateOf<AnnotatedString?>(null) }

    // 只有在启用异步高亮时才执行高亮处理
    if (enableAsyncHighlight) {
        LaunchedEffect(codeBlock.literal, codeBlock.info) {
            coroutineScope.launch {
                highlightedText = withContext(Dispatchers.IO) {
                    parseAndHighlightCode(codeBlock.literal, codeBlock.info)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .glassBackground(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
            .padding(
                vertical = 8.dp,
                horizontal = 10.dp
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
        ) {
            // 如果有语言信息，显示在右上角
            if (codeBlock.info.isNotEmpty()) {
                Text(
                    text = codeBlock.info.uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(4.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            codeBlockClickListener?.let {
                if (codeBlockClickListener.isLanguageRunnable(codeBlock.info)) {
                    CreateWidgetActionButton {
                        codeBlockClickListener.onWidgetButtonClicked(
                            language = codeBlock.info,
                            code = codeBlock.literal
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                CopyCodeActionButton {
                    codeBlockClickListener.onCopyButtonClicked(code = codeBlock.literal)
                }
                Spacer(modifier = Modifier.width(4.dp))
                if (codeBlockClickListener.isLanguageRunnable(codeBlock.info)) {
                    RunCodeActionButton {
                        codeBlockClickListener.onRunButtonClicked(
                            code = codeBlock.literal,
                            language = codeBlock.info
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                if (codeBlockClickListener.isLanguageSave(codeBlock.info)) {
                    SaveCodeActionButton {
                        codeBlockClickListener.onSaveButtonClicked(
                            code = codeBlock.literal,
                            language = codeBlock.info
                        )
                    }
                }
            }
        }
        Text(
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            text = highlightedText ?: AnnotatedString(codeBlock.literal.trim()),
            modifier = Modifier
                .horizontalScroll(scrollState)
                .fillMaxWidth()
        )
    }
}

private suspend fun parseAndHighlightCode(
    code: String,
    language: String
): AnnotatedString {
    return withContext(Dispatchers.IO) {
        try {
            // 创建Prism4j实例
            val prism4j = Prism4j(PrismGrammarLocator())
            // 创建默认主题
            val theme = Prism4jThemeMaterial3.create()

            val prism4jSyntaxHighlight = Prism4jSyntaxHighlight(prism4j, theme, "java")

            // 将Markdown渲染为Spanned
            val spanned = prism4jSyntaxHighlight.highlight(language, code)

            // 将Spanned转换为AnnotatedString
            MarkdownStringUtils.spannedToAnnotatedString(SpannableBuilder(spanned).spannableStringBuilder())
        } catch (e: Exception) {
            // 发生异常时返回普通文本
            AnnotatedString(code)
        }
    }
}
