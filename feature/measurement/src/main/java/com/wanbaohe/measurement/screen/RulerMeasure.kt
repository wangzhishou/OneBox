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

package com.wanbaohe.measurement.screen

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanbaohe.measurement.component.RulerUnit
import kotlin.math.abs

private const val DefaultLineAFraction = 0.35f
private const val DefaultLineBFraction = 0.65f
private const val MinLineFraction = 0.02f
private const val MaxLineFraction = 0.98f

/**
 * 直尺双测量线状态。位置用轴向比例(0..1)存储,横竖屏切换时保持相对位置。
 * 竖屏为两条水平线(上下拖动),横屏为两条竖线(左右拖动)。
 */
class RulerMeasureState {
    var lineA by mutableStateOf(DefaultLineAFraction)
    var lineB by mutableStateOf(DefaultLineBFraction)

    // 0 = lineA,1 = lineB,null = 未命中
    var activeLine by mutableStateOf<Int?>(null)
}

@Composable
fun rememberRulerMeasureState(): RulerMeasureState = remember { RulerMeasureState() }

suspend fun PointerInputScope.detectRulerMeasureDrags(
    state: RulerMeasureState,
    touchRadiusPx: Float
) {
    detectDragGestures(
        onDragStart = { start ->
            val isPortrait = size.height > size.width
            val axisPx = if (isPortrait) size.height else size.width
            val pos = if (isPortrait) start.y else start.x
            val distA = abs(pos - state.lineA * axisPx)
            val distB = abs(pos - state.lineB * axisPx)
            state.activeLine = when {
                distA <= touchRadiusPx && distA <= distB -> 0
                distB <= touchRadiusPx -> 1
                else -> null
            }
        },
        onDrag = { change, dragAmount ->
            change.consume()
            val line = state.activeLine ?: return@detectDragGestures
            val isPortrait = size.height > size.width
            val axisPx = (if (isPortrait) size.height else size.width).toFloat()
            val delta = if (isPortrait) dragAmount.y else dragAmount.x
            val current = if (line == 0) state.lineA else state.lineB
            val next = (current + delta / axisPx).coerceIn(MinLineFraction, MaxLineFraction)
            if (line == 0) state.lineA = next else state.lineB = next
        },
        onDragEnd = { state.activeLine = null },
        onDragCancel = { state.activeLine = null }
    )
}

fun DrawScope.drawRulerMeasureLines(
    state: RulerMeasureState,
    unit: RulerUnit,
    pixelsPerUnit: Float,
    isPortrait: Boolean,
    rulerBreadth: Float,
    textMeasurer: TextMeasurer,
    lineColor: Color,
    chipColor: Color,
    chipTextColor: Color
) {
    val axisPx = if (isPortrait) size.height else size.width
    val posA = state.lineA * axisPx
    val posB = state.lineB * axisPx
    val strokeWidth = if (state.activeLine != null) 3.dp.toPx() else 2.dp.toPx()

    if (isPortrait) {
        drawLine(
            color = lineColor,
            start = Offset(0f, posA),
            end = Offset(size.width, posA),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = lineColor,
            start = Offset(0f, posB),
            end = Offset(size.width, posB),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    } else {
        drawLine(
            color = lineColor,
            start = Offset(posA, 0f),
            end = Offset(posA, size.height),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = lineColor,
            start = Offset(posB, 0f),
            end = Offset(posB, size.height),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }

    // 两线间距标签(拖动中加粗线宽,标签常驻以便读数)
    val distance = abs(posB - posA) / pixelsPerUnit
    val label = when (unit) {
        RulerUnit.CM -> "%.1f cm".format(distance)
        RulerUnit.INCH -> "%.2f\"".format(distance)
    }
    val textLayout = textMeasurer.measure(
        text = label,
        style = TextStyle(
            color = chipTextColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    )
    val padH = 12.dp.toPx()
    val padV = 6.dp.toPx()
    val chipWidth = textLayout.size.width + padH * 2
    val chipHeight = textLayout.size.height + padV * 2
    val mid = (posA + posB) / 2f
    val edgeGap = 12.dp.toPx()
    val chipTopLeft = if (isPortrait) {
        Offset(
            x = (size.width - rulerBreadth - edgeGap - chipWidth).coerceAtLeast(edgeGap),
            y = mid - chipHeight / 2f
        )
    } else {
        Offset(
            x = (mid - chipWidth / 2f).coerceIn(edgeGap, (size.width - chipWidth - edgeGap).coerceAtLeast(edgeGap)),
            y = rulerBreadth + edgeGap
        )
    }
    drawRoundRect(
        color = chipColor,
        topLeft = chipTopLeft,
        size = Size(chipWidth, chipHeight),
        cornerRadius = CornerRadius(chipHeight / 2f)
    )
    drawText(
        textLayoutResult = textLayout,
        topLeft = chipTopLeft + Offset(padH, padV)
    )
}
