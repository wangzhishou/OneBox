@file:OptIn(ExperimentalFoundationApi::class)

package com.shifenmiao.demo.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.base.ui.MarkdownLazyContent

/**
 * Debug-only native Markdown editor experiment.
 *
 * Markdown text remains the sole persisted representation. Formatting commands edit Markdown
 * delimiters, while [markdownOutputTransformation] adds display-only syntax highlighting without
 * modifying the buffer or selection offsets. This keeps the experiment suitable for evaluating
 * Compose input performance before replacing the production WebView editor.
 */
@Composable
fun NativeMarkdownEditorDemoScreen(
    onDismiss: () -> Unit,
) {
    val editorState = rememberTextFieldState(initialText = initialMarkdown)
    var markdown by remember { mutableStateOf(editorState.text.toString()) }
    var showPreview by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(editorState) {
        snapshotFlow { editorState.text.toString() }.collect { markdown = it }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 520.dp, max = 760.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "原生 Markdown 编辑器实验",
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = "不使用 WebView；Markdown 源码是唯一数据源。光标进入格式范围时会恢复显示语法标记。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "${markdown.length} 字符 · ${markdown.lineCount()} 行 · ${if (showPreview) "原生预览" else "编辑模式"}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        MarkdownEditorToolbar(
            state = editorState,
            onTogglePreview = { showPreview = !showPreview },
            previewVisible = showPreview,
            onLoadSample = { editorState.replaceAll(initialMarkdown) },
            onLoadLargeDocument = { editorState.replaceAll(largeMarkdownDocument()) },
        )

        HorizontalDivider()

        if (showPreview) {
            MarkdownLazyContent(
                message = markdown,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        } else {
            NativeMarkdownTextField(
                state = editorState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("关闭实验")
        }
    }
}

@Composable
private fun MarkdownEditorToolbar(
    state: TextFieldState,
    previewVisible: Boolean,
    onTogglePreview: () -> Unit,
    onLoadSample: () -> Unit,
    onLoadLargeDocument: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ToolbarButton("B") { state.wrapSelection("**") }
        ToolbarButton("I") { state.wrapSelection("_") }
        ToolbarButton("代码") { state.wrapSelection("`") }
        ToolbarButton("标题") { state.toggleHeading() }
        ToolbarButton("列表") { state.toggleList() }
        ToolbarButton("任务") { state.toggleTaskList() }
        ToolbarButton("编号") { state.toggleLinePrefix("1. ") }
        ToolbarButton("引用") { state.toggleLinePrefix("> ") }
        ToolbarButton("代码块") { state.wrapSelection("```\n", "\n```") }
        ToolbarButton("链接") { state.wrapSelection("[", "](https://)") }
        Spacer(Modifier.width(4.dp))
        Button(onClick = onTogglePreview) {
            Text(if (previewVisible) "继续编辑" else "原生预览")
        }
        OutlinedButton(onClick = onLoadSample) { Text("示例") }
        OutlinedButton(onClick = onLoadLargeDocument) { Text("压力文档") }
    }
}

@Composable
private fun ToolbarButton(
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun NativeMarkdownTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Box(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest, MaterialTheme.shapes.medium)
            .padding(12.dp),
    ) {
        BasicTextField(
            state = state,
            inputTransformation = markdownInputTransformation(),
            outputTransformation = markdownOutputTransformation(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        )
    }
}

@Composable
private fun markdownOutputTransformation(): OutputTransformation {
    val colors = MaterialTheme.colorScheme
    return remember(colors) {
        OutputTransformation {
            val source = toString()
            val activeRange = selection
            markdownPresentationRanges(source, activeRange, colors).forEach { (style, range) ->
                addStyle(style, range.start, range.end)
            }
            markdownMarkerReplacements(source, activeRange).forEach { replacement ->
                replace(replacement.range.start, replacement.range.end, replacement.value)
            }
        }
    }
}

@Composable
private fun markdownInputTransformation(): InputTransformation = remember {
    InputTransformation { autoContinueMarkdownList() }
}

private fun markdownPresentationRanges(
    source: String,
    activeRange: TextRange,
    colors: androidx.compose.material3.ColorScheme,
): List<Pair<SpanStyle, TextRange>> = buildList {
    headingPattern.findAll(source).forEach { match ->
        val level = match.groupValues[1].length
        add(
            SpanStyle(
                color = colors.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                fontSize = when (level) {
                    1 -> 28.sp
                    2 -> 24.sp
                    3 -> 21.sp
                    else -> 18.sp
                },
            ) to match.range.toTextRange(),
        )
        match.groups[1]?.range?.toTextRange()?.let { markerRange ->
            if (match.range.toTextRange().isActive(activeRange)) add(markerStyle(colors) to markerRange)
        }
    }
    quotePattern.findAll(source).forEach { match ->
        add(SpanStyle(color = colors.secondary) to match.range.toTextRange())
        match.groups[1]?.range?.toTextRange()?.let { markerRange ->
            if (match.range.toTextRange().isActive(activeRange)) add(markerStyle(colors) to markerRange)
        }
    }
    listPattern.findAll(source).forEach { match ->
        add(SpanStyle(color = colors.onSurface) to match.range.toTextRange())
        match.groups[1]?.range?.toTextRange()?.let { markerRange ->
            if (match.range.toTextRange().isActive(activeRange)) add(markerStyle(colors) to markerRange)
        }
    }
    inlineCodePattern.findAll(source).forEach { match ->
        add(
            SpanStyle(
                color = colors.tertiary,
                background = colors.tertiaryContainer.copy(alpha = 0.45f),
            ) to match.range.toTextRange(),
        )
    }
    markdownInlineMarkers(source).filter { it.scope.isActive(activeRange) }.forEach { marker ->
        add(markerStyle(colors) to marker.range)
    }
}

private fun markdownMarkerReplacements(
    source: String,
    activeRange: TextRange,
): List<MarkerReplacement> = buildList {
    headingPattern.findAll(source).forEach { match ->
        match.groups[1]?.range?.toTextRange()?.let { range ->
            if (!match.range.toTextRange().isActive(activeRange)) add(MarkerReplacement(range, "\u2060".repeat(range.length)))
        }
    }
    quotePattern.findAll(source).forEach { match ->
        match.groups[1]?.range?.toTextRange()?.let { range ->
            if (!match.range.toTextRange().isActive(activeRange)) add(MarkerReplacement(range, "│ "))
        }
    }
    listPattern.findAll(source).forEach { match ->
        val range = match.groups[1]?.range?.toTextRange() ?: return@forEach
        if (!match.range.toTextRange().isActive(activeRange)) {
            val marker = match.groupValues[1]
            add(MarkerReplacement(range, marker.visualListMarker()))
        }
    }
    markdownInlineMarkers(source).forEach { marker ->
        if (!marker.scope.isActive(activeRange)) {
            add(MarkerReplacement(marker.range, "\u2060".repeat(marker.range.length)))
        }
    }
}

private fun markdownInlineMarkers(source: String): List<MarkdownMarker> = buildList {
    strongPattern.findAll(source).forEach { match ->
        val scope = match.range.toTextRange()
        match.groups[1]?.range?.toTextRange()?.let { add(MarkdownMarker(it, scope)) }
        match.groups[3]?.range?.toTextRange()?.let { add(MarkdownMarker(it, scope)) }
    }
    emphasisPattern.findAll(source).forEach { match ->
        val scope = match.range.toTextRange()
        match.groups[1]?.range?.toTextRange()?.let { add(MarkdownMarker(it, scope)) }
        match.groups[3]?.range?.toTextRange()?.let { add(MarkdownMarker(it, scope)) }
    }
    inlineCodePattern.findAll(source).forEach { match ->
        val scope = match.range.toTextRange()
        match.groups[1]?.range?.toTextRange()?.let { add(MarkdownMarker(it, scope)) }
        match.groups[3]?.range?.toTextRange()?.let { add(MarkdownMarker(it, scope)) }
    }
    linkPattern.findAll(source).forEach { match ->
        val scope = match.range.toTextRange()
        match.groups[1]?.range?.toTextRange()?.let { add(MarkdownMarker(it, scope)) }
        match.groups[3]?.range?.toTextRange()?.let { add(MarkdownMarker(it, scope)) }
    }
}

private fun markerStyle(colors: androidx.compose.material3.ColorScheme) = SpanStyle(
    color = colors.outline.copy(alpha = 0.85f),
)

private fun TextRange.isActive(selection: TextRange): Boolean =
    selection.min <= max && selection.max >= min

private fun String.visualListMarker(): String = when {
    taskUncheckedPattern.matches(this) -> "☐" + "\u200A".repeat(length - 1)
    taskCheckedPattern.matches(this) -> "☑" + "\u200A".repeat(length - 1)
    else -> "•" + " ".repeat(length - 1)
}

private data class MarkerReplacement(
    val range: TextRange,
    val value: String,
)

private data class MarkdownMarker(
    val range: TextRange,
    val scope: TextRange,
)

private fun TextFieldBuffer.autoContinueMarkdownList() {
    if (changes.changeCount != 1) return
    val insertedRange = changes.getRange(0)
    val replacedRange = changes.getOriginalRange(0)
    if (!replacedRange.collapsed || toString().substring(insertedRange.min, insertedRange.max) != "\n") {
        return
    }

    val source = toString()
    val previousLineStart = source.lineStart(insertedRange.min)
    val previousLine = source.substring(previousLineStart, insertedRange.min)
    val continuation = previousLine.markdownListContinuation() ?: return
    if (continuation.isEmptyItem) {
        replace(previousLineStart, insertedRange.max, "\n")
        selection = TextRange(previousLineStart + 1)
    } else {
        replace(insertedRange.max, insertedRange.max, continuation.nextPrefix)
        selection = TextRange(insertedRange.max + continuation.nextPrefix.length)
    }
}

private fun String.markdownListContinuation(): MarkdownListContinuation? {
    taskListInputPattern.matchEntire(this)?.let { match ->
        val prefix = "${match.groupValues[1]}${match.groupValues[2]} [ ] "
        return MarkdownListContinuation(prefix, match.groupValues[4].isBlank())
    }
    unorderedListInputPattern.matchEntire(this)?.let { match ->
        val prefix = "${match.groupValues[1]}${match.groupValues[2]} "
        return MarkdownListContinuation(prefix, match.groupValues[3].isBlank())
    }
    orderedListInputPattern.matchEntire(this)?.let { match ->
        val prefix = "${match.groupValues[1]}${match.groupValues[2].toInt() + 1}. "
        return MarkdownListContinuation(prefix, match.groupValues[3].isBlank())
    }
    return null
}

private data class MarkdownListContinuation(
    val nextPrefix: String,
    val isEmptyItem: Boolean,
)

private fun IntRange.toTextRange(): TextRange = TextRange(first, last + 1)

private fun String.lineCount(): Int = if (isEmpty()) 0 else count { it == '\n' } + 1

private fun TextFieldState.replaceAll(value: String) {
    edit {
        replace(0, length, value)
        selection = TextRange(value.length)
    }
}

private fun TextFieldState.wrapSelection(prefix: String, suffix: String = prefix) {
    edit {
        val source = toString()
        val start = selection.min
        val end = selection.max
        val selected = source.substring(start, end)
        val isWrapped = start >= prefix.length && end + suffix.length <= length &&
            source.substring(start - prefix.length, start) == prefix &&
            source.substring(end, end + suffix.length) == suffix
        if (isWrapped) {
            replace(start - prefix.length, end + suffix.length, selected)
            selection = TextRange(start - prefix.length, end - prefix.length)
        } else {
            replace(start, end, prefix + selected + suffix)
            selection = TextRange(start + prefix.length, end + prefix.length)
        }
    }
}

private fun TextFieldState.toggleHeading() = toggleLinePrefix("# ")

private fun TextFieldState.toggleLinePrefix(prefix: String) {
    edit {
        val source = toString()
        val start = source.lineStart(selection.min)
        val end = source.lineEnd(selection.max)
        val lines = source.substring(start, end).split('\n')
        val allPrefixed = lines.all { it.startsWith(prefix) }
        val replacement = lines.joinToString("\n") { line ->
            if (allPrefixed) line.removePrefix(prefix) else prefix + line
        }
        replace(start, end, replacement)
        selection = TextRange(start, start + replacement.length)
    }
}

private fun TextFieldState.toggleList() = toggleLinePrefix("- ")

private fun TextFieldState.toggleTaskList() = toggleLinePrefix("- [ ] ")

private fun String.lineStart(offset: Int): Int {
    val previousNewline = lastIndexOf('\n', (offset - 1).coerceAtLeast(0))
    return if (previousNewline < 0) 0 else previousNewline + 1
}

private fun String.lineEnd(offset: Int): Int {
    val nextNewline = indexOf('\n', offset.coerceAtMost(length))
    return if (nextNewline < 0) length else nextNewline
}

private val headingPattern = Regex("(?m)^(#{1,6})\\s.*$")
private val quotePattern = Regex("(?m)^(> ).*$")
private val listPattern = Regex("(?m)^(\\s*(?:[-*+] \\[[ xX]] |[-*+] |\\d+\\. )).*$")
private val inlineCodePattern = Regex("(`{1,3})([^`]*)(`{1,3})")
private val strongPattern = Regex("(\\*\\*|__)(.+?)(\\*\\*|__)")
private val emphasisPattern = Regex("(?<![_*])([_*])([^_*\\n]+?)([_*])(?![_*])")
private val linkPattern = Regex("(\\[)([^]]+)(]\\([^)]*\\))")
private val taskUncheckedPattern = Regex("\\s*[-*+] \\[ ]")
private val taskCheckedPattern = Regex("\\s*[-*+] \\[x]", RegexOption.IGNORE_CASE)
private val taskListInputPattern = Regex("^(\\s*)([-*+]) \\[([ xX])]\\s*(.*)$")
private val unorderedListInputPattern = Regex("^(\\s*)([-*+])\\s+(.*)$")
private val orderedListInputPattern = Regex("^(\\s*)(\\d+)\\.\\s+(.*)$")

private const val initialMarkdown = """# 原生 Markdown 编辑器

这是一个 **不依赖 WebView** 的实验页。选中文本后点击工具栏，可直接生成 Markdown 语法。

> Markdown 源码是唯一事实来源；颜色只是 Compose 的输出样式层。

- 支持常用行内格式
- 支持标题、列表、引用与代码块
- 可切换到工程现有的原生 Markdown 预览

`TextFieldState` 会保留光标和选区。

```kotlin
val state = rememberTextFieldState("Hello Markdown")
```

[项目主页](https://github.com/)
"""

private fun largeMarkdownDocument(): String = buildString {
    append("# 原生 Markdown 编辑性能压力文档\n\n")
    repeat(2_000) { index ->
        append("## 第 ${index + 1} 节\n\n")
        append("这是一段用于评估 `BasicTextField` 输入、滚动与输出样式层性能的 Markdown 文本。")
        append("包含 **粗体**、_斜体_ 与 [链接](https://example.com/$index)。\n\n")
        append("- 列表项目 A\n- 列表项目 B\n\n")
    }
}
