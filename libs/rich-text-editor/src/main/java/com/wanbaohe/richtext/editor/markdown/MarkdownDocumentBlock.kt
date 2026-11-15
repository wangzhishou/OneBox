package com.wanbaohe.richtext.editor.markdown

import androidx.compose.runtime.Immutable
import com.wanbaohe.richtext.editor.RichTextSnapshot

@Immutable
data class MarkdownDocument(
    val blocks: List<MarkdownDocumentBlock>,
) {
    companion object {
        val Empty = MarkdownDocument(
            listOf(MarkdownDocumentBlock.RichText("rich-text-empty", RichTextSnapshot.Empty)),
        )
    }
}

sealed interface MarkdownDocumentBlock {
    val id: String

    data class RichText(
        override val id: String,
        val snapshot: RichTextSnapshot,
    ) : MarkdownDocumentBlock

    data class Raw(
        override val id: String,
        val markdown: String,
        val kind: MarkdownRawBlockKind,
    ) : MarkdownDocumentBlock
}

enum class MarkdownRawBlockKind {
    Code,
    Diagram,
    Image,
    Video,
    Audio,
    Math,
    Html,
    Table,
    Link,
    Quote,
    OrderedList,
    TaskList,
    Other,
}

internal object MarkdownBlockId {
    fun content(kind: String, markdown: String, occurrence: Int): String {
        val hash = markdown.hashCode().toUInt().toString(HEX_RADIX)
        return "$kind-$hash-$occurrence"
    }
}

private const val HEX_RADIX = 16
