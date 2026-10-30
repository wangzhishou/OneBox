package com.wanbaohe.ocr.domain

fun List<RecognizedLine>.toPlainText(): String = joinToString(separator = "\n") { it.text }

fun List<RecognizedLine>.toMarkdown(): String {
    val content = joinToString(separator = "\n\n") { it.text.trim() }
    return if (content.isBlank()) content else "$content\n"
}

fun List<RecognizedLine>.markdownFileName(): String {
    val title = firstOrNull()?.text
        ?.replace(Regex("[\\\\/:*?\"<>|\\r\\n]"), "")
        ?.trim()
        ?.take(48)
        .orEmpty()
    return if (title.isEmpty()) "ocr-result.md" else "$title.md"
}
