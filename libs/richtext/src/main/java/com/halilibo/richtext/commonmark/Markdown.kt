package com.halilibo.richtext.commonmark

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.halilibo.richtext.markdown.AstBlockNodeComposer
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.markwon.MarkdownAstNodeParser
import com.halilibo.richtext.ui.RichTextScope
import com.shifenmiao.model.node.AstNode
import io.noties.markwon.plugins.codeblock.CodeBlockClickListener

/**
 * A composable that renders Markdown content according to Commonmark specification using RichText.
 *
 * @param content Markdown text. No restriction on length.
 * @param markdownParseOptions Options for the Markdown parser.
 * @param astBlockNodeComposer An interceptor to take control of composing any block type node's
 * rendering. Use it to render images, html text, tables with your own components.
 */
@Composable
fun RichTextScope.Markdown(
  content: String,
  markdownParseOptions: CommonMarkdownParseOptions = CommonMarkdownParseOptions.Default,
  astBlockNodeComposer: AstBlockNodeComposer? = null,
  codeBlockClickListener: CodeBlockClickListener? = null
) {
  val context = LocalContext.current
  val commonmarkAstNodeParser = remember(markdownParseOptions) {
    MarkdownAstNodeParser(context, markdownParseOptions)
  }

  val astRootNode by produceState<AstNode?>(
    initialValue = null,
    key1 = commonmarkAstNodeParser,
    key2 = content
  ) {
    value = commonmarkAstNodeParser.parse(content)
  }

  astRootNode?.let {
    BasicMarkdown(
        astNode = it,
        astBlockNodeComposer = astBlockNodeComposer,
        codeBlockClickListener = codeBlockClickListener
    )
  }
}