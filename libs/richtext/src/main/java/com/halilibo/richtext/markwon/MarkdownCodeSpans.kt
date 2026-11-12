package com.halilibo.richtext.markwon

internal object MarkdownCodeSpans {
    private val CodeSpanRegex = Regex(
        "(?<!`)(`+)(?!`)(.*?)(?<!`)\\1(?!`)",
        RegexOption.DOT_MATCHES_ALL
    )

    fun transformOutside(text: String, transform: (String) -> String): String {
        if (!text.contains('`')) return transform(text)

        val result = StringBuilder(text.length)
        var cursor = 0
        var matched = false
        CodeSpanRegex.findAll(text).forEach { match ->
            if (isEscaped(text, match.range.first)) return@forEach
            result.append(transform(text.substring(cursor, match.range.first)))
            result.append(match.value)
            cursor = match.range.last + 1
            matched = true
        }
        if (!matched) return transform(text)
        result.append(transform(text.substring(cursor)))
        return result.toString()
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
}
