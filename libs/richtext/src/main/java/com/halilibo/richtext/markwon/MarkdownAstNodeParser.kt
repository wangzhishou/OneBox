package com.halilibo.richtext.markwon

import android.content.Context
import android.text.util.Linkify
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.commonmark.CommonMarkdownParseOptions
import com.halilibo.richtext.commonmark.convert
import com.shifenmiao.model.node.AstNode
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.handler.FontTagHandler
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import io.noties.markwon.linkify.LinkifyPlugin

class MarkdownAstNodeParser @AssistedInject constructor(
    @ApplicationContext context: Context,
    options: CommonMarkdownParseOptions
) {
    private val markwonBuilder = Markwon.builder(context)
        .usePlugin(MarkwonInlineParserPlugin.create())
        .usePlugin(JLatexMathPlugin.create(14.sp.value) { builder ->
            builder.inlinesEnabled(true)
            builder.blocksEnabled(true)
        })
        .usePlugin(CorePlugin.create())
        .usePlugin(HtmlPlugin.create { plugin ->
            plugin.addHandler(FontTagHandler())
        })
        .usePlugin(LinkifyPlugin.create(Linkify.EMAIL_ADDRESSES or Linkify.PHONE_NUMBERS))
        .usePlugin(StrikethroughPlugin.create())
        .usePlugin(TablePlugin.create(context))
    var markdown: Markwon? = null

    init {
        markdown = markwonBuilder.build()
    }


    fun parse(text: String): AstNode {
        // 预处理 LaTeX 分隔符，将 \[ \] 转换为 $$ $$，\( \) 转换为 $ $
        val processedText = preprocessLatex(text)

        val commonmarkNode = markdown?.parse(processedText)
            ?: throw IllegalArgumentException(
                "Could not parse the given text content into a meaningful Markdown representation!"
            )

        return convert(commonmarkNode, markdownAstNodeParser = this)
            ?: throw IllegalArgumentException(
                "Could not convert the generated Common mark Node into an ASTNode!"
            )
    }

    private fun preprocessLatex(text: String): String {
        if (text.indexOf('`') < 0 && text.indexOf('~') < 0) {
            return preprocessLatexSegment(text)
        }

        val result = StringBuilder(text.length)
        val pending = StringBuilder()
        var inFence = false
        var fenceMarker: String? = null

        fun flushPending() {
            if (pending.isNotEmpty()) {
                result.append(preprocessLatexSegment(pending.toString()))
                pending.clear()
            }
        }

        text.lineSequence().forEachIndexed { index, line ->
            if (index > 0) {
                if (inFence) result.append('\n') else pending.append('\n')
            }

            val marker = detectFenceMarker(line)
            if (marker != null) {
                if (!inFence) {
                    flushPending()
                    inFence = true
                    fenceMarker = marker
                    result.append(line)
                } else {
                    result.append(line)
                    if (marker == fenceMarker) {
                        inFence = false
                        fenceMarker = null
                    }
                }
            } else if (inFence) {
                result.append(line)
            } else {
                pending.append(line)
            }
        }
        flushPending()
        return result.toString()
    }

    private fun preprocessLatexSegment(text: String): String {
        return text
            // Replace block math \[ ... \] with $$ ... $$
            .replace(Regex("""\\\[(.*?)\\\]""", RegexOption.DOT_MATCHES_ALL)) { matchResult ->
                "$$" + matchResult.groupValues[1] + "$$"
            }
            // Preserve inline math \( ... \) by escaping backslashes so CommonMark doesn't drop them
            .replace(Regex("""\\\((.*?)\\\)""", RegexOption.DOT_MATCHES_ALL)) { matchResult ->
                "\\\\(" + matchResult.groupValues[1] + "\\\\)"
            }
            // Convert inline math $ ... $ to \( ... \) (escaped) so inline parser can recognize it
            .replace(Regex("(?<!\\\\)\\$(.+?)(?<!\\\\)\\$", RegexOption.DOT_MATCHES_ALL)) { matchResult ->
                val content = matchResult.groupValues[1]
                val hasLineBreak = content.contains('\n') || content.contains('\r')
                if (content.isBlank() || hasLineBreak) {
                    matchResult.value
                } else {
                    "\\\\(" + content + "\\\\)"
                }
            }
    }

    private fun detectFenceMarker(line: String): String? {
        val trimmed = line.trimStart()
        return when {
            trimmed.startsWith("```") -> "```"
            trimmed.startsWith("~~~") -> "~~~"
            else -> null
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(): MarkdownAstNodeParser
    }
}
