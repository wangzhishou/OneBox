@file:OptIn(ExperimentalFoundationApi::class)

package com.shifenmiao.common.components.text

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.TextUnit

/**
 * 就地文字编辑的选区 API(图文卡片 / 图片创作共用)。
 *
 * 选区是 [TextFieldState] 的 snapshot state,宿主直接读 [TextFieldState.selection]
 * 即可重组;这里只收口"对选区做什么",保证两个编辑器的行为一致:
 * - 局部样式写入(同属性区间互斥,见下)
 * - 清除选区样式
 * - 复制 / 剪切 / 粘贴 / 全选
 *
 * 关键不变量:**同属性区间必须互斥**。em 字号在 Android 端会转成
 * RelativeSizeSpan,重叠区间是 textSize 相乘而不是覆盖;拖一次字号滑杆的
 * 中间值若各自追加一条区间,字号会成倍爆炸。所以应用/清除都走
 * "快照 → 剥离选区同属性 → 重建样式层"的路径。
 */

/** 选区字号倍率范围(与块级/图层级字号语义对齐) */
const val MIN_SELECTION_SIZE_SCALE = 0.5f
const val MAX_SELECTION_SIZE_SCALE = 4f

val TextFieldState.hasSelection: Boolean
    get() = !selection.collapsed

val TextFieldState.selectedText: String
    get() {
        val current = selection
        if (current.collapsed) return ""
        return text.substring(
            current.min.coerceIn(0, text.length),
            current.max.coerceIn(0, text.length)
        )
    }

/** 当前全部局部样式区间(编辑器样式层唯一事实源) */
fun TextFieldState.selectedSpanStyles(): List<AnnotatedString.Range<SpanStyle>> =
    textStyles.getSpanStyles(TextRange(0, text.length))

/** 给当前选区应用局部样式:先剥离选区重叠部分的同属性旧样式,再追加新样式 */
fun TextFieldState.applySelectionStyle(
    style: SpanStyle,
    onChanged: () -> Unit,
) {
    val current = selection
    if (current.collapsed) return
    val survivors = selectedSpanStyles().flatMap { range ->
        range.withoutAttribute(current, style)
    }
    rebuildStyles(survivors, current) {
        addStyle(style, current.min, current.max)
    }
    onChanged()
}

/** 清除选区全部局部样式(选区重叠部分整段剥掉,选区外保留) */
fun TextFieldState.clearSelectionStyles(onChanged: () -> Unit) {
    val current = selection
    if (current.collapsed) return
    val survivors = selectedSpanStyles().flatMap { range ->
        TextRange(range.start, range.end)
            .without(current.min, current.max)
            .map { range.item to it }
    }
    rebuildStyles(survivors, current)
    onChanged()
}

/** 全选(光标/选区写入 state,编辑器与工具条共享同一实例) */
fun TextFieldState.selectAllText() {
    edit { selection = TextRange(0, length) }
}

/** 复制选区;返回是否真的写入了剪贴板 */
fun TextFieldState.copySelection(clipboard: ClipboardManager): Boolean {
    val value = selectedText
    if (value.isEmpty()) return false
    clipboard.setText(AnnotatedString(value))
    return true
}

/** 剪切选区:先复制,再删除选区内容 */
fun TextFieldState.cutSelection(
    clipboard: ClipboardManager,
    onChanged: () -> Unit,
): Boolean {
    val current = selection
    if (current.collapsed) return false
    val value = selectedText
    if (value.isEmpty()) return false
    clipboard.setText(AnnotatedString(value))
    edit {
        replace(current.min, current.max, "")
        selection = TextRange(current.min)
    }
    onChanged()
    return true
}

/** 在选区处粘贴;返回是否发生了修改 */
fun TextFieldState.pasteAtSelection(
    clipboard: ClipboardManager,
    onChanged: () -> Unit,
): Boolean {
    val value = clipboard.getText()?.text.orEmpty()
    if (value.isEmpty()) return false
    val current = selection
    val start = current.min.coerceIn(0, text.length)
    val end = current.max.coerceIn(0, text.length)
    edit {
        replace(start, end, value)
        selection = TextRange(start + value.length)
    }
    onChanged()
    return true
}

/**
 * 重建样式层:replace 全文(内容不变)使旧样式区间全部坍缩移除,回灌幸存区间;
 * 末尾恢复选区。replace 会把选区坍缩到替换末尾,故显式恢复。
 */
private inline fun TextFieldState.rebuildStyles(
    survivors: List<Pair<SpanStyle, TextRange>>,
    selection: TextRange,
    crossinline extra: TextFieldBuffer.() -> Unit = {},
) {
    val text = text.toString()
    edit {
        replace(0, length, text)
        survivors.forEach { (style, range) ->
            if (style != SpanStyle()) addStyle(style, range.start, range.end)
        }
        extra()
        this.selection = selection
    }
}

/**
 * 区间按选区切分:选区外部分原样保留;选区内部分剥掉 [newStyle] 设置的属性
 * (newStyle 里设置了的字段 = 本次要替换的属性,剥成未设置即继承块级)。
 */
private fun AnnotatedString.Range<SpanStyle>.withoutAttribute(
    selection: TextRange,
    newStyle: SpanStyle,
): List<Pair<SpanStyle, TextRange>> = buildList {
    TextRange(start, end).without(selection.min, selection.max)
        .forEach { add(item to it) }
    TextRange(start, end).intersectRange(selection.min, selection.max)
        ?.let { add(item.strippedOf(newStyle) to it) }
}

/** 剥掉 [other] 中设置了的四个属性(颜色/字号/粗/斜),其余保留 */
private fun SpanStyle.strippedOf(other: SpanStyle): SpanStyle = SpanStyle(
    color = if (other.color != Color.Unspecified) Color.Unspecified else color,
    fontSize = if (other.fontSize != TextUnit.Unspecified) TextUnit.Unspecified else fontSize,
    fontWeight = if (other.fontWeight != null) null else fontWeight,
    fontStyle = if (other.fontStyle != null) null else fontStyle,
)

/** 区间裁掉 [start, end) 部分,保留两侧残段(0~2 段) */
private fun TextRange.without(start: Int, end: Int): List<TextRange> = buildList {
    if (min < start) add(TextRange(min, start))
    if (max > end) add(TextRange(end, max))
}

/** 区间与 [start, end) 的交集,不相交返回 null */
private fun TextRange.intersectRange(start: Int, end: Int): TextRange? {
    val s = maxOf(min, start)
    val e = minOf(max, end)
    return if (s < e) TextRange(s, e) else null
}
