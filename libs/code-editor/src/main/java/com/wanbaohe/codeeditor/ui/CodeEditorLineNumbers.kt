/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

@file:Suppress("FunctionNaming")

package com.wanbaohe.codeeditor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.wanbaohe.codeeditor.CodeFold

internal data class CodeEditorGutterLine(
    val number: Int,
    val offset: Int,
    val fold: CodeFold?,
)

/**
 * 行号槽。
 *
 * 位置不再来自「每行固定高度」的估算，而是直接问文本框的 [TextLayoutResult]：
 * 关掉软换行时一个逻辑行占一个视觉行；打开软换行时，折行后的续行不画号，
 * 行号只出现在逻辑行首，纵向位置来自该行首所在的视觉行。
 *
 * 滚动不额外同步一个 ScrollState —— 行号按「内容坐标 - 当前滚动量」绘制，
 * 因此永远和正文同帧对齐。
 */
@Composable
@Suppress("LongParameterList") // Compose 数据参数，全部带默认值
internal fun CodeEditorLineNumbers(
    lines: List<CodeEditorGutterLine>,
    layoutResult: TextLayoutResult?,
    activeOffset: Int,
    scrollState: ScrollState,
    style: CodeEditorGutterStyle,
    foldColumn: Dp,
    foldedRanges: Set<CodeFold>,
    onToggleFold: (CodeFold) -> Unit,
    foldActionLabel: ((Int, Boolean) -> String)?,
    modifier: Modifier = Modifier,
) {
    val layout = layoutResult ?: return
    val textMeasurer = rememberTextMeasurer(cacheSize = 128)
    val density = LocalDensity.current
    val visualLines = remember(layout, lines) {
        val length = layout.layoutInput.text.length
        lines.map { layout.getLineForOffset(it.offset.coerceAtMost(length)) }
    }
    val activeNumber = lines.getOrNull(lines.binarySearch { it.offset.compareTo(activeOffset) }
        .let { if (it >= 0) it else -it - 2 })?.number
    val viewConfiguration = LocalViewConfiguration.current
    val gutterConfiguration = remember(viewConfiguration) {
        // 相邻按钮按实际行高分隔；默认扩大的触控区域会让读屏边界跨到上一行。
        object : ViewConfiguration by viewConfiguration {
            override val minimumTouchTargetSize = DpSize.Zero
        }
    }
    BoxWithConstraints(modifier = modifier.clipToBounds()) {
        val scroll = scrollState.value
        val viewportHeight = with(density) { maxHeight.toPx() }
        val firstVisible = layout.getLineForVerticalPosition(scroll.toFloat())
        val lastVisible = layout.getLineForVerticalPosition(scroll + viewportHeight)
        val visible = visualLines.lowerBound(firstVisible) until visualLines.lowerBound(lastVisible + 1)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val paddingPx = (foldColumn + NUMBER_PADDING).toPx()
            for (index in visible) {
                val number = lines[index].number
                val row = visualLines[index]
                val measure = textMeasurer.measure(
                    text = number.toString(),
                    style = if (number == activeNumber) style.activeNumberStyle else style.numberStyle,
                )
                drawText(
                    textLayoutResult = measure,
                    topLeft = Offset(
                        x = if (layoutDirection == androidx.compose.ui.unit.LayoutDirection.Ltr) {
                            size.width - paddingPx - measure.size.width
                        } else {
                            paddingPx
                        },
                        y = (layout.getLineTop(row) + layout.getLineBottom(row) - measure.size.height) / 2f - scroll,
                    ),
                )
            }
        }
        for (index in visible) {
            val line = lines[index]
            val fold = line.fold ?: continue
            val row = visualLines[index]
            val folded = fold in foldedRanges
            val (top, height) = with(density) {
                (layout.getLineTop(row) - scroll).toDp() to
                    (layout.getLineBottom(row) - layout.getLineTop(row)).toDp()
            }
            val label = foldActionLabel?.invoke(line.number, folded)
            CompositionLocalProvider(LocalViewConfiguration provides gutterConfiguration) {
                Box(
                    modifier = Modifier.align(Alignment.TopEnd)
                        .offset(y = top)
                        .size(maxWidth, height)
                        .semantics { if (label != null) contentDescription = label }
                        .clickable(role = Role.Button, onClick = { onToggleFold(fold) }),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Box(Modifier.size(foldColumn, height), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(8.dp)) {
                            val color = style.activeNumberStyle.color
                            val strokeWidth = 1.dp.toPx()
                            drawLine(color, Offset(0f, center.y), Offset(size.width, center.y), strokeWidth)
                            if (folded) {
                                drawLine(color, Offset(center.x, 0f), Offset(center.x, size.height), strokeWidth)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun List<Int>.lowerBound(target: Int): Int {
    var low = 0
    var high = size
    while (low < high) {
        val mid = (low + high) ushr 1
        if (this[mid] < target) low = mid + 1 else high = mid
    }
    return low
}

/**
 * 行号槽宽度：按最大行号的位数算，随行数增长而加宽，避免 4 位数被裁。
 */
@Composable
fun rememberCodeEditorGutterWidth(
    lineCount: Int,
    numberStyle: TextStyle,
): Dp {
    val textMeasurer = rememberTextMeasurer(cacheSize = 8)
    val density = LocalDensity.current
    val digits = lineCount.coerceAtLeast(1).toString().length
    val sample = remember(digits) { "8".repeat(digits) }
    val measured = remember(sample, numberStyle) {
        textMeasurer.measure(text = sample, style = numberStyle)
    }
    return with(density) { measured.size.width.toDp() + NUMBER_PADDING * 2 }
}

private val NUMBER_PADDING = 4.dp
