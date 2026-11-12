package com.halilibo.richtext.markwon

import android.content.Context
import android.text.util.Linkify
import com.halilibo.richtext.commonmark.CommonMarkdownParseOptions
import com.halilibo.richtext.commonmark.convert
import com.shifenmiao.model.node.AstNode
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.handler.FontTagHandler
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.linkify.LinkifyPlugin

class MarkdownAstNodeParser @AssistedInject constructor(
    @ApplicationContext context: Context,
    options: CommonMarkdownParseOptions
) {
    private val markwonBuilder = Markwon.builder(context)
        .usePlugin(MarkdownMathPlugin())
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
        val processedText = MarkdownCompatibilityNormalizer.normalize(MarkdownMathPreprocessor.preprocess(text))

        val commonmarkNode = markdown?.parse(processedText)
            ?: throw IllegalArgumentException(
                "Could not parse the given text content into a meaningful Markdown representation!"
            )

        return convert(commonmarkNode)
            ?: throw IllegalArgumentException(
                "Could not convert the generated Common mark Node into an ASTNode!"
            )
    }

    @AssistedFactory
    interface Factory {
        fun create(): MarkdownAstNodeParser
    }
}
