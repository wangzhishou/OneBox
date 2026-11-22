package com.wanbaohe.codeeditor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import com.hrm.codehigh.ast.TokenType
import com.hrm.codehigh.lexer.LanguageRegistry

/** 原文偏移：[start, end) 被隐藏，首尾分隔符仍然可见。 */
internal data class CodeFold(val start: Int, val end: Int)

internal fun findCodeFolds(
    source: String,
    language: CodeLanguage,
    lineStarts: List<Int>,
): Map<Int, CodeFold> {
    if (lineStarts.size < 2 || language !in BRACE_LANGUAGES) return emptyMap()
    val tokens = runCatching { LanguageRegistry.getOrPlain(language.lexerKey).tokenize(source) }
        .getOrDefault(emptyList())
    val openings = ArrayDeque<Int>()
    val folds = mutableMapOf<Int, CodeFold>()
    for (token in tokens) {
        if (token.type in NON_STRUCTURAL_TOKENS) continue
        val start = token.range.first.coerceIn(0, source.length)
        val end = token.range.last.coerceAtMost(source.lastIndex)
        for (offset in start..end) {
            when (source[offset]) {
                '{', '[', '(' -> openings.addLast(offset)
                '}', ']', ')' -> {
                    val open = openings.removeLastOrNull() ?: continue
                    if ("{[(".indexOf(source[open]) != "}])".indexOf(source[offset])) {
                        openings.clear()
                        continue
                    }
                    if (source[open] == '(' && language == CodeLanguage.Json) continue
                    val line = lineStarts.lineAt(open)
                    if (line == lineStarts.lineAt(offset)) continue
                    if (offset > (folds[line]?.end ?: -1)) folds[line] = CodeFold(open + 1, offset)
                }
            }
        }
    }
    return folds
}

private fun List<Int>.lineAt(offset: Int): Int =
    binarySearch(offset).let { if (it >= 0) it else -it - 2 }

/** 仅转换显示和坐标；查找、复制、编辑历史与保存使用原文。 */
internal class FoldProjection(folds: Set<CodeFold>) : OffsetMapping {
    private val segments = buildList<Segment> {
        for (fold in folds.sortedBy { it.start }) {
            val previous = lastOrNull()
            if (previous != null && fold.start < previous.fold.end) continue
            val displayStart = fold.start - (previous?.removedThrough ?: 0)
            add(Segment(fold, displayStart))
        }
    }

    val isEmpty: Boolean get() = segments.isEmpty()

    fun hidesLineStart(offset: Int): Boolean {
        val fold = segmentAt(offset) { it.fold.start }?.fold ?: return false
        return offset > fold.start && offset <= fold.end
    }

    fun project(source: AnnotatedString): AnnotatedString = buildAnnotatedString {
        var cursor = 0
        for ((fold) in segments) {
            append(source, cursor, fold.start)
            append(PLACEHOLDER)
            cursor = fold.end
        }
        append(source, cursor, source.length)
    }

    override fun originalToTransformed(offset: Int): Int {
        val segment = segmentAt(offset) { it.fold.start } ?: return offset
        return when {
            offset == segment.fold.start -> segment.displayStart
            offset < segment.fold.end -> segment.displayStart + 1
            else -> offset - segment.removedThrough
        }
    }

    override fun transformedToOriginal(offset: Int): Int {
        val segment = segmentAt(offset) { it.displayStart } ?: return offset
        return if (offset < segment.displayEnd) segment.fold.start
        else offset + segment.removedThrough
    }

    private inline fun segmentAt(offset: Int, crossinline start: (Segment) -> Int): Segment? {
        val found = segments.binarySearch { start(it).compareTo(offset) }
        return segments.getOrNull(if (found >= 0) found else -found - 2)
    }

    private data class Segment(val fold: CodeFold, val displayStart: Int) {
        val displayEnd = displayStart + PLACEHOLDER.length
        val removedThrough = fold.end - displayEnd
    }
}

private const val PLACEHOLDER = "…"
private val NON_STRUCTURAL_TOKENS = setOf(TokenType.STRING, TokenType.COMMENT)
private val BRACE_LANGUAGES = setOf(
    CodeLanguage.Json, CodeLanguage.Kotlin, CodeLanguage.Java, CodeLanguage.JavaScript,
    CodeLanguage.TypeScript, CodeLanguage.Go, CodeLanguage.Rust, CodeLanguage.C,
    CodeLanguage.Cpp, CodeLanguage.Swift, CodeLanguage.Dart, CodeLanguage.Scala,
    CodeLanguage.Php, CodeLanguage.Css,
)
