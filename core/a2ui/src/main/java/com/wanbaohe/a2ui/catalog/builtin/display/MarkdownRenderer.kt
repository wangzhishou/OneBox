package com.wanbaohe.a2ui.catalog.builtin.display

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.markwon.MarkdownAstNodeParser
import com.halilibo.richtext.ui.material3.RichText
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import javax.inject.Inject

/**
 * Markdown 文本渲染器。
 * 与聊天消息同一套 richtext 管线;解析失败时退化为纯文本,保证界面可用。
 */
class MarkdownRenderer @Inject constructor(
    private val markdownParserFactory: MarkdownAstNodeParser.Factory,
) : A2uiComponentRenderer {

    override val componentType = "Markdown"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val text = context.resolveString(component.properties["text"]) ?: return
        if (text.isBlank()) return

        val parser = remember { markdownParserFactory.create() }
        val astNode = remember(text) {
            runCatching { parser.parse(text) }.getOrNull()
        }

        if (astNode == null) {
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
            return
        }

        RichText(
            textStyle = MaterialTheme.typography.bodyMedium,
        ) {
            BasicMarkdown(astNode = astNode, compactBlocks = true)
        }
    }
}
