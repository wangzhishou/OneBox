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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
fun CodeEditorLineNumbers(
    lineStarts: List<Int>,
    layoutResult: TextLayoutResult?,
    activeLine: Int,
    scrollState: ScrollState,
    style: CodeEditorGutterStyle,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer(cacheSize = 128)
    val density = LocalDensity.current
    val gutterPadding = 6.dp
    val visualLines = remember(layoutResult, lineStarts) {
        layoutResult?.let { layout ->
            val textLength = layout.layoutInput.text.length
            lineStarts.map { start -> layout.getLineForOffset(start.coerceIn(0, textLength)) }
        }
    }
    Canvas(modifier = modifier) {
        val layout = layoutResult ?: return@Canvas
        val lines = visualLines ?: return@Canvas
        if (lines.isEmpty()) return@Canvas
        val scroll = scrollState.value
        val viewportHeight = size.height
        if (viewportHeight <= 0f) return@Canvas
        val paddingPx = with(density) { gutterPadding.toPx() }
        val firstVisible = layout.getLineForVerticalPosition(scroll.toFloat())
        val lastVisible = layout.getLineForVerticalPosition(scroll + viewportHeight)
        // 只画视口内的行号：先算出可见区间，循环体里不再 continue/break
        val startIndex = lines.indexOfFirst { it >= firstVisible }.let { if (it < 0) lines.size else it }
        val endIndex = lines.indexOfLast { it <= lastVisible }
        for (index in startIndex..endIndex) {
            val visualLine = lines[index]
            val isActive = index == activeLine
            val measure = textMeasurer.measure(
                text = (index + 1).toString(),
                style = if (isActive) style.activeNumberStyle else style.numberStyle,
            )
            val lineTop = layout.getLineTop(visualLine) - scroll
            val lineBottom = layout.getLineBottom(visualLine) - scroll
            drawText(
                textLayoutResult = measure,
                topLeft = Offset(
                    x = size.width - paddingPx - measure.size.width,
                    y = lineTop + (lineBottom - lineTop - measure.size.height) / 2f,
                ),
            )
        }
    }
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
    return with(density) { (measured.size.width + 16.dp.toPx() + 2.dp.toPx()).toDp() }
}
