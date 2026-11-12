package com.halilibo.richtext.markwon

internal object MarkdownMathPreprocessor {
    private val BlockBracketMathRegex = Regex("""\\\[(.*?)\\\]""", RegexOption.DOT_MATCHES_ALL)
    private val InlineParenMathRegex = Regex("""\\\((.*?)\\\)""", RegexOption.DOT_MATCHES_ALL)
    private val InlineDollarMathRegex = Regex(
        "(?<![\\\\$])\\$(?!\\$)(.+?)(?<![\\\\$])\\$(?!\\$)",
        RegexOption.DOT_MATCHES_ALL
    )

    fun preprocess(text: String): String {
        if (!text.contains('$') && !text.contains('\\')) return text
        if (!text.contains('`') && !text.contains('~')) {
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
        return MarkdownCodeSpans.transformOutside(text, ::preprocessLatexText)
    }

    private fun preprocessLatexText(text: String): String {
        return text
            .replace(BlockBracketMathRegex) { match ->
                "$$" + match.groupValues[1] + "$$"
            }
            .replace(InlineParenMathRegex) { match ->
                "\\\\(" + match.groupValues[1] + "\\\\)"
            }
            .replace(InlineDollarMathRegex) { match ->
                val content = match.groupValues[1]
                val hasLineBreak = content.contains('\n') || content.contains('\r')
                if (content.isBlank() || hasLineBreak) {
                    match.value
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
}
