package com.halilibo.richtext.markwon

internal object MarkdownCompatibilityNormalizer {
    private val inlineHeading = Regex("""([。！？!?；;：:.][”’」』】）)]?)(#{1,6})([ \t]+)""")
    private val strongEmphasis = Regex("""(?<!\*)\*\*(?!\*)(.+?)(?<!\*)\*\*(?!\*)""")
    private val codeSpan = Regex("""(?<!`)(`+)(?!`)(.*?)(?<!`)\1(?!`)""")

    fun normalize(text: String): String {
        if (!text.contains('#') && !text.contains("**")) return text

        val result = StringBuilder(text.length + 16)
        var openFence: Fence? = null
        val lines = text.split('\n')
        lines.forEachIndexed { index, line ->
            val fence = fenceAtLineStart(line)
            val currentFence = openFence
            when {
                currentFence != null -> {
                    result.append(line)
                    if (fence != null && fence.closes(currentFence, line)) {
                        openFence = null
                    }
                }

                fence != null -> {
                    result.append(line)
                    openFence = fence
                }

                else -> result.append(normalizeOutsideCodeSpans(line))
            }
            if (index < lines.lastIndex) result.append('\n')
        }
        return result.toString()
    }

    private fun normalizeOutsideCodeSpans(line: String): String {
        if (!line.contains('#') && !line.contains("**")) return line
        if (!line.contains('`')) return normalizePlainText(line)

        val result = StringBuilder(line.length + 8)
        var cursor = 0
        codeSpan.findAll(line).forEach { match ->
            if (isEscaped(line, match.range.first)) return@forEach
            result.append(normalizePlainText(line.substring(cursor, match.range.first)))
            result.append(match.value)
            cursor = match.range.last + 1
        }
        result.append(normalizePlainText(line.substring(cursor)))
        return result.toString()
    }

    private fun normalizePlainText(text: String): String {
        val withHeadings = if (text.contains('#')) {
            inlineHeading.replace(text) { match ->
                "${match.groupValues[1]}\n\n${match.groupValues[2]}${match.groupValues[3]}"
            }
        } else {
            text
        }
        if (!withHeadings.contains("**")) return withHeadings
        return normalizeStrongEmphasis(withHeadings)
    }

    private fun normalizeStrongEmphasis(text: String): String {
        val result = StringBuilder(text.length)
        var cursor = 0
        var changed = false
        strongEmphasis.findAll(text).forEach { match ->
            val content = match.groupValues[1]
            val replacement = repairPunctuationBoundaries(text, match.range, content) ?: return@forEach
            result.append(text, cursor, match.range.first)
            result.append(replacement)
            cursor = match.range.last + 1
            changed = true
        }
        if (!changed) return text
        result.append(text, cursor, text.length)
        return result.toString()
    }

    private fun repairPunctuationBoundaries(text: String, range: IntRange, content: String): String? {
        if (content.length < 2) return null
        val before = text.getOrNull(range.first - 1)
        val after = text.getOrNull(range.last + 1)
        val moveOpening = before != null &&
            !before.isWhitespace() &&
            !before.isMarkdownPunctuation() &&
            content.first().isMarkdownPunctuation()
        val moveClosing = after != null &&
            !after.isWhitespace() &&
            !after.isMarkdownPunctuation() &&
            content.last().isMarkdownPunctuation()
        if (!moveOpening && !moveClosing) return null

        return buildString(text.length) {
            if (moveOpening) append(content.first())
            append("**")
            append(
                content,
                if (moveOpening) 1 else 0,
                content.length - if (moveClosing) 1 else 0
            )
            append("**")
            if (moveClosing) append(content.last())
        }
    }

    private fun fenceAtLineStart(line: String): Fence? {
        val trimmed = line.trimStart()
        if (line.length - trimmed.length > 3) return null
        val marker = trimmed.firstOrNull()
        if (marker != '`' && marker != '~') return null
        val length = trimmed.takeWhile { it == marker }.length
        return if (length >= 3) Fence(marker, length) else null
    }

    private fun Fence.closes(opening: Fence, line: String): Boolean {
        if (marker != opening.marker || length < opening.length) return false
        return line.trimStart().drop(length).isBlank()
    }

    private fun isEscaped(text: String, index: Int): Boolean {
        var backslashes = 0
        var cursor = index - 1
        while (cursor >= 0 && text[cursor] == '\\') {
            backslashes++
            cursor--
        }
        return backslashes % 2 != 0
    }

    private fun Char.isMarkdownPunctuation(): Boolean {
        return when (Character.getType(this).toByte()) {
            Character.CONNECTOR_PUNCTUATION,
            Character.DASH_PUNCTUATION,
            Character.START_PUNCTUATION,
            Character.END_PUNCTUATION,
            Character.INITIAL_QUOTE_PUNCTUATION,
            Character.FINAL_QUOTE_PUNCTUATION,
            Character.OTHER_PUNCTUATION -> true

            else -> false
        }
    }

    private data class Fence(val marker: Char, val length: Int)
}
