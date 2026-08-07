package com.shifenmiao.webview

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

object HtmlMarkdownConverter {
    private val voidTags = setOf("area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "param", "source", "track", "wbr")
    private val supportedBlockRootTags = setOf(
        "table",
        "div",
        "p",
        "ul",
        "ol",
        "blockquote",
        "pre",
        "h1",
        "h2",
        "h3",
        "h4",
        "h5",
        "h6",
        "hr",
        "img",
    )

    fun convertHtmlTablesToGfm(markdownWithHtml: String): String {
        return convertEmbeddedHtmlToMarkdown(markdownWithHtml)
    }

    fun convertEmbeddedHtmlToMarkdown(markdown: String): String {
        val parts = splitByFencedCodeBlocks(markdown)
        return buildString {
            parts.forEach { part ->
                append(
                    if (part.isCode) part.text else convertHtmlBlocks(part.text)
                )
            }
        }
    }

    private data class Part(val text: String, val isCode: Boolean)

    private fun splitByFencedCodeBlocks(text: String): List<Part> {
        val parts = ArrayList<Part>()
        var index = 0
        while (index < text.length) {
            val fenceStart = text.indexOf("```", startIndex = index)
            if (fenceStart == -1) {
                parts.add(Part(text.substring(index), isCode = false))
                break
            }
            if (fenceStart > index) {
                parts.add(Part(text.substring(index, fenceStart), isCode = false))
            }
            val fenceEnd = text.indexOf("```", startIndex = fenceStart + 3)
            if (fenceEnd == -1) {
                parts.add(Part(text.substring(fenceStart), isCode = true))
                break
            }
            parts.add(Part(text.substring(fenceStart, fenceEnd + 3), isCode = true))
            index = fenceEnd + 3
        }
        return parts
    }

    private fun convertHtmlBlocks(text: String): String {
        if (text.isBlank()) return text

        val sb = StringBuilder(text.length)
        var i = 0
        while (i < text.length) {
            val lineStart = i == 0 || text[i - 1] == '\n'
            if (!lineStart) {
                sb.append(text[i])
                i++
                continue
            }

            var j = i
            while (j < text.length && (text[j] == ' ' || text[j] == '\t')) j++
            if (j >= text.length || text[j] != '<') {
                sb.append(text[i])
                i++
                continue
            }

            val extracted = extractHtmlFragment(text, j)
            if (extracted == null) {
                sb.append(text[i])
                i++
                continue
            }

            val (html, endExclusive) = extracted
            val indent = text.substring(i, j)
            val converted = runCatching { htmlFragmentToMarkdown(html) }.getOrNull()
                ?.takeIf { it.isNotBlank() }

            if (converted == null) {
                sb.append(text.substring(i, endExclusive))
                i = endExclusive
                continue
            }

            val normalized = ensureBlockSeparation(converted)
            sb.append(indent)
            sb.append(indentMultiline(normalized, indent))
            i = endExclusive
        }

        return sb.toString()
    }

    private fun ensureBlockSeparation(markdown: String): String {
        val trimmed = markdown.trim('\n')
        return "\n\n$trimmed\n\n"
    }

    private fun indentMultiline(text: String, indent: String): String {
        if (indent.isEmpty()) return text
        return text.lineSequence().joinToString("\n") { line ->
            if (line.isBlank()) line else indent + line
        }
    }

    private fun extractHtmlFragment(text: String, startIndex: Int): Pair<String, Int>? {
        if (startIndex !in 0 until text.length) return null
        if (text[startIndex] != '<') return null

        if (text.regionMatches(startIndex, "<!--", 0, 4, ignoreCase = true)) {
            val end = text.indexOf("-->", startIndex = startIndex + 4)
            return if (end == -1) null else text.substring(startIndex, end + 3) to (end + 3)
        }

        val tagName = parseTagName(text, startIndex) ?: return null
        if (!supportedBlockRootTags.contains(tagName.lowercase())) return null

        val endExclusive = if (voidTags.contains(tagName.lowercase())) {
            val gt = text.indexOf('>', startIndex = startIndex + 1)
            if (gt == -1) return null
            gt + 1
        } else {
            findMatchingTagEnd(text, startIndex, tagName) ?: return null
        }

        return text.substring(startIndex, endExclusive) to endExclusive
    }

    private fun parseTagName(text: String, startIndex: Int): String? {
        var i = startIndex + 1
        if (i >= text.length) return null
        if (text[i] == '/') i++
        while (i < text.length && text[i].isWhitespace()) i++
        if (i >= text.length) return null

        val start = i
        while (i < text.length) {
            val c = text[i]
            if (!(c.isLetterOrDigit())) break
            i++
        }
        if (i <= start) return null
        return text.substring(start, i)
    }

    private fun findMatchingTagEnd(text: String, startIndex: Int, tagNameRaw: String): Int? {
        val tagName = tagNameRaw.lowercase()
        val regex = Regex("<\\s*/?\\s*${Regex.escape(tagName)}\\b[^>]*>", setOf(RegexOption.IGNORE_CASE))

        var depth = 0
        var match = regex.find(text, startIndex)
        while (match != null) {
            val value = match.value
            val isClosing = value.startsWith("</", ignoreCase = true) || value.startsWith("< /", ignoreCase = true)
            val isSelfClosing = value.endsWith("/>") || voidTags.contains(tagName)

            if (!isClosing) {
                depth += 1
                if (isSelfClosing) {
                    depth -= 1
                }
            } else {
                depth -= 1
            }

            if (depth == 0) {
                return match.range.last + 1
            }
            match = regex.find(text, match.range.last + 1)
        }
        return null
    }

    private fun htmlFragmentToMarkdown(html: String): String {
        val doc = Jsoup.parseBodyFragment(html)
        val bodyNodes = doc.body().childNodes()
        val blocks = bodyNodes.mapNotNull { node ->
            val md = nodeToMarkdown(node, inTableCell = false).trim()
            md.takeIf { it.isNotBlank() }
        }
        return blocks.joinToString("\n\n").replace(Regex("\n{3,}"), "\n\n")
    }

    private fun nodeToMarkdown(node: Node, inTableCell: Boolean): String {
        return when (node) {
            is TextNode -> normalizeText(node.text())
            is Element -> elementToMarkdown(node, inTableCell = inTableCell)
            else -> normalizeText(node.outerHtml())
        }
    }

    private fun elementToMarkdown(element: Element, inTableCell: Boolean): String {
        return when (element.tagName().lowercase()) {
            "br" -> if (inTableCell) "<br>" else "\n"
            "hr" -> "---"
            "img" -> imageToMarkdown(element)
            "a" -> linkToMarkdown(element, inTableCell)
            "strong", "b" -> wrapInline("**", renderInline(element, inTableCell).trim())
            "em", "i" -> wrapInline("*", renderInline(element, inTableCell).trim())
            "code" -> inlineCodeToMarkdown(element.text())
            "pre" -> fencedCodeToMarkdown(element)
            "p", "div", "span", "section", "article" -> {
                val inner = renderInline(element, inTableCell).trim()
                inner
            }
            "blockquote" -> blockquoteToMarkdown(element)
            "ul" -> listToMarkdown(element, ordered = false)
            "ol" -> listToMarkdown(element, ordered = true)
            "li" -> renderInline(element, inTableCell).trim()
            "h1", "h2", "h3", "h4", "h5", "h6" -> {
                val level = element.tagName().substring(1).toIntOrNull()?.coerceIn(1, 6) ?: 1
                val inner = renderInline(element, inTableCell).trim()
                "#".repeat(level) + " " + inner
            }
            "table" -> tableElementToGfm(element)
            "thead", "tbody", "tfoot", "tr", "th", "td" -> renderInline(element, inTableCell)
            else -> renderInline(element, inTableCell)
        }
    }

    private fun renderInline(element: Element, inTableCell: Boolean): String {
        return element.childNodes().joinToString("") { child ->
            nodeToMarkdown(child, inTableCell = inTableCell)
        }
    }

    private fun normalizeText(text: String): String {
        return text
            .replace("\r\n", "\n")
            .replace("\r", "\n")
    }

    private fun wrapInline(wrapper: String, content: String): String {
        if (content.isBlank()) return ""
        return wrapper + content + wrapper
    }

    private fun inlineCodeToMarkdown(code: String): String {
        val normalized = code.replace("\n", " ").trim()
        val backtickCount = normalized.maxConsecutiveChar('`')
        val fence = "`".repeat(backtickCount + 1)
        return fence + normalized + fence
    }

    private fun String.maxConsecutiveChar(ch: Char): Int {
        var max = 0
        var cur = 0
        for (c in this) {
            if (c == ch) {
                cur += 1
                if (cur > max) max = cur
            } else {
                cur = 0
            }
        }
        return max
    }

    private fun fencedCodeToMarkdown(pre: Element): String {
        val codeEl = pre.selectFirst("> code") ?: pre
        val codeText = normalizeText(codeEl.wholeText()).trimEnd('\n')
        val fence = "```"
        return buildString {
            append(fence)
            append('\n')
            append(codeText)
            append('\n')
            append(fence)
        }
    }

    private fun linkToMarkdown(a: Element, inTableCell: Boolean): String {
        val href = normalizeUrl(a.attr("href"))
        val text = renderInline(a, inTableCell).trim().ifBlank { href }
        if (href.isBlank()) return text
        return "[$text]($href)"
    }

    private fun imageToMarkdown(img: Element): String {
        val src = normalizeUrl(img.attr("src"))
        val alt = img.attr("alt").trim()
        if (src.isBlank()) return ""
        val width = normalizeSize(img.attr("width")).ifBlank {
            extractSizeFromStyle(img.attr("style"), "width")
        }
        val height = normalizeSize(img.attr("height")).ifBlank {
            extractSizeFromStyle(img.attr("style"), "height")
        }

        if (width.isBlank() && height.isBlank()) {
            return "![$alt]($src)"
        }

        return buildString {
            append("<img src=\"")
            append(escapeHtmlAttr(src))
            append("\"")
            if (alt.isNotBlank()) {
                append(" alt=\"")
                append(escapeHtmlAttr(alt))
                append("\"")
            }
            if (width.isNotBlank()) {
                append(" width=\"")
                append(escapeHtmlAttr(width))
                append("\"")
            }
            if (height.isNotBlank()) {
                append(" height=\"")
                append(escapeHtmlAttr(height))
                append("\"")
            }
            append(" />")
        }
    }

    private fun normalizeUrl(raw: String): String {
        var s = raw.trim()
        s = s.trim('"', '\'', '`')
        s = s.trim()
        return s
    }

    private fun normalizeSize(raw: String): String {
        var s = raw.trim()
        s = s.trim('"', '\'', '`')
        return s.trim()
    }

    private fun extractSizeFromStyle(styleRaw: String, key: String): String {
        if (styleRaw.isBlank()) return ""
        val style = styleRaw.trim()
        val match = Regex("(?i)(?:^|;)\\s*${Regex.escape(key)}\\s*:\\s*([^;]+)").find(style) ?: return ""
        return normalizeSize(match.groupValues.getOrNull(1).orEmpty())
    }

    private fun escapeHtmlAttr(raw: String): String {
        return raw
            .replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }

    private fun blockquoteToMarkdown(element: Element): String {
        val inner = element.childNodes().joinToString("\n") { node ->
            nodeToMarkdown(node, inTableCell = false).trimEnd()
        }.trim()
        if (inner.isBlank()) return ""
        return inner.lines().joinToString("\n") { line ->
            if (line.isBlank()) ">" else "> $line"
        }
    }

    private fun listToMarkdown(listEl: Element, ordered: Boolean, indent: String = ""): String {
        val items = listEl.children().filter { it.tagName().equals("li", ignoreCase = true) }
        return items.mapIndexedNotNull { index, li ->
            val marker = if (ordered) "${index + 1}. " else "- "
            val nestedLists = li.children().filter { it.tagName().equals("ul", true) || it.tagName().equals("ol", true) }
            nestedLists.forEach { it.remove() }
            val content = renderInline(li, inTableCell = false).trim()
            val nested = nestedLists.joinToString("\n") { nestedList ->
                val nestedOrdered = nestedList.tagName().equals("ol", true)
                listToMarkdown(nestedList, ordered = nestedOrdered, indent = indent + "  ")
            }.takeIf { it.isNotBlank() }
            buildString {
                append(indent)
                append(marker)
                append(content)
                if (!nested.isNullOrBlank()) {
                    append('\n')
                    append(nested)
                }
            }.takeIf { it.isNotBlank() }
        }.joinToString("\n")
    }

    private data class TableCell(val content: String, val colspan: Int)

    private fun tableElementToGfm(tableEl: Element): String {
        data class ParsedRow(
            val cells: List<TableCell>,
            val hasTh: Boolean,
        )

        val rowEls = tableEl.select("tr")
        if (rowEls.isEmpty()) return ""

        val parsedRows = rowEls.mapNotNull { rowEl ->
            val cellEls = rowEl.select("> th, > td")
            if (cellEls.isEmpty()) return@mapNotNull null

            val cells = cellEls.map { cellEl ->
                val replaced = cellEl.clone()
                replaced.select("table").forEach { nested ->
                    val nestedMd = tableElementToGfm(nested).trim()
                    val inlineNested = nestedMd.replace("\n", "<br>")
                    nested.replaceWith(TextNode(inlineNested))
                }
                val content = renderInline(replaced, inTableCell = true)
                val colspan = cellEl.attr("colspan").toIntOrNull()?.coerceAtLeast(1) ?: 1
                TableCell(content, colspan)
            }
            ParsedRow(
                cells = cells,
                hasTh = rowEl.select("> th").isNotEmpty(),
            )
        }

        if (parsedRows.isEmpty()) return ""

        val captionRow = parsedRows.firstOrNull()?.takeIf { first ->
            first.cells.size == 1 && first.cells.first().colspan > 1 && parsedRows.size > 1
        }
        val caption = captionRow?.cells?.firstOrNull()?.content
            ?.let(::normalizeText)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { "**$it**" }

        val startRowIndex = if (captionRow != null) 1 else 0
        val rowsAfterCaption = parsedRows.drop(startRowIndex)
        if (rowsAfterCaption.isEmpty()) return caption.orEmpty()

        val headerRowIndexInAfterCaption = rowsAfterCaption.indexOfFirst { it.hasTh }.takeIf { it >= 0 } ?: 0
        val headerExpanded = expandRow(rowsAfterCaption[headerRowIndexInAfterCaption].cells)
        val expandedRows = rowsAfterCaption.map { expandRow(it.cells) }
        val columnCount = expandedRows.maxOf { it.size }.coerceAtLeast(headerExpanded.size)

        fun normalizeCellForTable(cell: String): String {
            return cell
                .trim()
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("\n", "<br>")
                .replace("|", "\\|")
        }

        fun rowToLine(row: List<String>): String {
            val padded = if (row.size < columnCount) row + List(columnCount - row.size) { "" } else row
            return "| " + padded.map { normalizeCellForTable(it) }.joinToString(" | ") + " |"
        }

        val headerLine = rowToLine(headerExpanded)
        val separatorLine = "| " + List(columnCount) { "---" }.joinToString(" | ") + " |"
        val bodyLines = expandedRows.withIndex()
            .filter { it.index != headerRowIndexInAfterCaption }
            .joinToString("\n") { (_, row) -> rowToLine(row) }
            .trim()

        return buildString {
            if (!caption.isNullOrBlank()) {
                append(caption)
                append("\n\n")
            }
            append(headerLine)
            append('\n')
            append(separatorLine)
            if (bodyLines.isNotBlank()) {
                append('\n')
                append(bodyLines)
            }
        }
    }

    private fun expandRow(cells: List<TableCell>): List<String> {
        val expanded = ArrayList<String>()
        cells.forEach { cell ->
            val colspan = cell.colspan.coerceAtLeast(1)
            expanded.add(cell.content)
            repeat(colspan - 1) { expanded.add("") }
        }
        return expanded
    }
}
