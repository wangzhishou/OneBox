package com.wanbaohe.richtext.editor.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import com.wanbaohe.richtext.editor.RichTextSnapshot
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme

class RichTextMarkdownDecoder(
    private val scheme: RichTextStyleScheme,
) {
    private val inlineDecoder = MarkdownInlineDecoder(scheme)
    private val rawClassifier = MarkdownRawBlockClassifier(scheme.inlineStyle("highlight") != null)
    private val blocks = mutableListOf<MarkdownDocumentBlock>()
    private val richLines = mutableListOf<String>()
    private val occurrences = mutableMapOf<String, Int>()

    fun decode(markdown: String): List<MarkdownDocumentBlock> {
        blocks.clear()
        richLines.clear()
        occurrences.clear()
        val lines = markdown.replace("\r\n", "\n").replace('\r', '\n').split('\n')
        var index = 0
        while (index < lines.size) {
            val trimmed = lines[index].trim()
            when {
                trimmed.isEmpty() -> index++
                FenceRegex.containsMatchIn(trimmed) -> {
                    flushRichText()
                    index = readFence(lines, index)
                }

                MediaDirectiveRegex.containsMatchIn(trimmed) -> {
                    flushRichText()
                    index = readMedia(lines, index)
                }

                trimmed.startsWith("$$") -> {
                    flushRichText()
                    index = readMath(lines, index)
                }

                rawClassifier.isRawParagraph(trimmed) -> {
                    flushRichText()
                    index = readRawParagraph(lines, index)
                }

                else -> {
                    richLines += lines[index]
                    index++
                }
            }
        }
        flushRichText()
        return blocks.ifEmpty {
            listOf(MarkdownDocumentBlock.RichText("rich-text-empty", RichTextSnapshot.Empty))
        }
    }

    private fun readFence(lines: List<String>, start: Int): Int {
        val opening = lines[start].trim()
        val fence = FenceRegex.find(opening)?.value ?: return start + 1
        val marker = fence.first()
        val language = opening.drop(fence.length).trim().lowercase()
        val rawLines = mutableListOf(lines[start])
        var index = start + 1
        while (index < lines.size) {
            rawLines += lines[index]
            val closing = lines[index].trim()
            index++
            if (closing.length >= fence.length && closing.all { it == marker }) break
        }
        val kind = if (language == "mermaid" || language == "mmd") {
            MarkdownRawBlockKind.Diagram
        } else {
            MarkdownRawBlockKind.Code
        }
        addRaw(kind, rawLines)
        return index
    }

    private fun readMedia(lines: List<String>, start: Int): Int {
        val opening = lines[start].trim()
        val isVideo = MediaDirectiveRegex.find(opening)?.groupValues?.get(1) == "video"
        val closingDirective = if (isVideo) "{% endvideo %}" else "{% endaudio %}"
        val rawLines = mutableListOf(lines[start])
        var index = start + 1
        while (index < lines.size && !lines[index].trim().startsWith(closingDirective)) {
            rawLines += lines[index]
            index++
        }
        if (index < lines.size) {
            rawLines += lines[index]
            index++
        }
        addRaw(if (isVideo) MarkdownRawBlockKind.Video else MarkdownRawBlockKind.Audio, rawLines)
        return index
    }

    private fun readMath(lines: List<String>, start: Int): Int {
        val rawLines = mutableListOf(lines[start])
        if (lines[start].trim().length > 2 && lines[start].trim().endsWith("$$")) {
            addRaw(MarkdownRawBlockKind.Math, rawLines)
            return start + 1
        }
        var index = start + 1
        while (index < lines.size) {
            rawLines += lines[index]
            val isClosing = lines[index].trim() == "$$"
            index++
            if (isClosing) break
        }
        addRaw(MarkdownRawBlockKind.Math, rawLines)
        return index
    }

    private fun readRawParagraph(lines: List<String>, start: Int): Int {
        val rawLines = mutableListOf<String>()
        var index = start
        while (index < lines.size && lines[index].isNotBlank()) {
            rawLines += lines[index]
            index++
        }
        addRaw(rawClassifier.kindOf(lines[start].trim()), rawLines)
        return index
    }

    private fun flushRichText() {
        if (richLines.isEmpty()) return
        val source = richLines.joinToString("\n")
        blocks += MarkdownDocumentBlock.RichText(
            id = nextId("rich-text", source),
            snapshot = decodeRichText(richLines),
        )
        richLines.clear()
    }

    private fun addRaw(kind: MarkdownRawBlockKind, lines: List<String>) {
        val markdown = lines.joinToString("\n")
        blocks += MarkdownDocumentBlock.Raw(
            id = nextId(kind.name.lowercase(), markdown),
            markdown = markdown,
            kind = kind,
        )
    }

    private fun nextId(kind: String, content: String): String {
        val key = "$kind:$content"
        val occurrence = occurrences[key] ?: 0
        occurrences[key] = occurrence + 1
        return MarkdownBlockId.content(kind, content, occurrence)
    }

    private fun decodeRichText(lines: List<String>): RichTextSnapshot {
        val text = StringBuilder()
        val spanStyles = mutableListOf<AnnotatedString.Range<SpanStyle>>()
        val paragraphStyles = mutableListOf<AnnotatedString.Range<ParagraphStyle>>()
        lines.forEachIndexed { lineIndex, line ->
            val heading = HeadingRegex.find(line)
            val quoteStyle = scheme.blockStyles.firstOrNull {
                it.markdownPrefix == "> " && line.startsWith("> ")
            }
            val bullet = BulletRegex.find(line)
            val content = when {
                heading != null -> heading.groupValues[2]
                quoteStyle != null -> line.removePrefix("> ")
                bullet != null -> scheme.listStyle.marker + bullet.groupValues[1]
                else -> line
            }
            val inline = inlineDecoder.decode(content)
            val contentStart = text.length
            val contentEnd = contentStart + inline.text.length
            spanStyles += inline.styles.map { range ->
                AnnotatedString.Range(range.item, range.start + contentStart, range.end + contentStart)
            }
            val blockStyle = heading?.let { match ->
                scheme.blockStyles.firstOrNull { it.markdownPrefix == match.groupValues[1] + " " }
            } ?: quoteStyle
            if (contentEnd > contentStart) {
                blockStyle?.let { style ->
                    spanStyles += AnnotatedString.Range(style.spanStyle, contentStart, contentEnd)
                    paragraphStyles += paragraphRange(
                        style.paragraphStyle,
                        contentStart,
                        contentEnd,
                        lineIndex < lines.lastIndex,
                    )
                }
                if (bullet != null) {
                    paragraphStyles += paragraphRange(
                        scheme.listStyle.paragraphStyle,
                        contentStart,
                        contentEnd,
                        lineIndex < lines.lastIndex,
                    )
                }
            }
            text.append(inline.text)
            if (lineIndex < lines.lastIndex) text.append('\n')
        }
        return RichTextSnapshot(text.toString(), spanStyles, paragraphStyles)
    }

    private fun paragraphRange(
        style: ParagraphStyle,
        start: Int,
        end: Int,
        includeNewline: Boolean,
    ) = AnnotatedString.Range(style, start, end + if (includeNewline) 1 else 0)
}

private val FenceRegex = Regex("^(`{3,}|~{3,})")
private val MediaDirectiveRegex = Regex("^\\{%\\s*(video|audio)\\b")
private val HeadingRegex = Regex("^(#{1,3})\\s+(.*)$")
private val BulletRegex = Regex("^[-*+]\\s+(.*)$")
