package com.wanbaohe.richtext.editor.markdown

import com.wanbaohe.richtext.editor.style.RichTextStyleScheme

class RichTextMarkdownDocumentCodec(
    scheme: RichTextStyleScheme,
) {
    private val createDecoder: () -> RichTextMarkdownDecoder = { RichTextMarkdownDecoder(scheme) }
    private val encoder = RichTextMarkdownEncoder(scheme)

    fun decode(markdown: String): MarkdownDocument = MarkdownDocument(
        createDecoder().decode(markdown).ifEmpty { MarkdownDocument.Empty.blocks },
    )

    fun encode(blocks: List<MarkdownDocumentBlock>): String = blocks.mapNotNull { block ->
        when (block) {
            is MarkdownDocumentBlock.RichText -> encoder.encode(block.snapshot).takeIf(String::isNotBlank)
            is MarkdownDocumentBlock.Raw -> block.markdown.takeIf(String::isNotBlank)
        }
    }.joinToString("\n\n")
}
