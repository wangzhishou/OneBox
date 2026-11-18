package com.wanbaohe.richtext.editor.markdown

internal class MarkdownRawBlockClassifier(
    private val supportsHighlight: Boolean,
) {
    fun isRawParagraph(line: String): Boolean =
        hasRawContainerPrefix(line) || hasRawInlineSyntax(line) || isRawList(line)

    fun kindOf(line: String): MarkdownRawBlockKind = when {
        line.startsWith("!VIDEO[") -> MarkdownRawBlockKind.Video
        line.startsWith("!AUDIO[") -> MarkdownRawBlockKind.Audio
        line.startsWith("![") -> MarkdownRawBlockKind.Image
        line.startsWith("<") -> MarkdownRawBlockKind.Html
        line.startsWith("|") -> MarkdownRawBlockKind.Table
        line.contains(LinkRegex) -> MarkdownRawBlockKind.Link
        OrderedListRegex.matches(line) -> MarkdownRawBlockKind.OrderedList
        TaskListRegex.matches(line) -> MarkdownRawBlockKind.TaskList
        else -> MarkdownRawBlockKind.Other
    }

    private fun hasRawContainerPrefix(line: String): Boolean =
        line.startsWith("    ") ||
            line.startsWith('\t') ||
            line.startsWith('|') ||
            line.startsWith("######") ||
            line.startsWith("<") && !line.startsWith("<u>")

    private fun hasRawInlineSyntax(line: String): Boolean =
        line.contains(MediaImageRegex) ||
            line.contains(LinkRegex) ||
            line.contains(HtmlRegex) ||
            line.contains('$') ||
            !supportsHighlight && line.contains("==") ||
            line.contains('^') ||
            line.contains(SingleTildeRegex) ||
            line.contains(AlertRegex)

    private fun isRawList(line: String): Boolean = TaskListRegex.matches(line)

    private companion object {
        val MediaImageRegex = Regex("!\\[|!VIDEO\\[|!AUDIO\\[")
        val LinkRegex = Regex("\\]\\(")
        val HtmlRegex = Regex("<(?!/?u\\b)")
        val SingleTildeRegex = Regex("(?<!~)~(?!~)")
        val AlertRegex = Regex("\\[!")
        val OrderedListRegex = Regex("^\\d+[.)]\\s+.*$")
        val TaskListRegex = Regex("^[-*+]\\s+\\[[ xX]?]\\s+.*$")
    }
}
