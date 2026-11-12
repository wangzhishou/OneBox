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

package com.wanbaohe.ocr.domain

import kotlinx.serialization.Serializable
import kotlin.math.abs

@Serializable
data class PixelPoint(
    val x: Float,
    val y: Float,
)

@Serializable
data class RecognizedLine(
    val text: String,
    val confidence: Float,
    val corners: List<PixelPoint>,
) {
    val left: Float get() = corners.minOf { it.x }
    val top: Float get() = corners.minOf { it.y }
    val centerY: Float get() = corners.map { it.y }.average().toFloat()
    val height: Float get() = corners.maxOf { it.y } - top
}

/**
 * 先按行聚拢（中心高度接近的算同一行），行内再从左到右。
 */
fun orderReadingLines(lines: List<RecognizedLine>): List<RecognizedLine> {
    if (lines.size < 2) return lines
    val sorted = lines.sortedBy { it.top }
    val rows = mutableListOf<MutableList<RecognizedLine>>()
    for (line in sorted) {
        val row = rows.lastOrNull()
        val anchor = row?.firstOrNull()
        val closeEnough = anchor != null &&
            abs(line.centerY - anchor.centerY) <= line.height.coerceAtLeast(anchor.height) * ROW_OVERLAP
        if (row != null && closeEnough) {
            row += line
        } else {
            rows += mutableListOf(line)
        }
    }
    return rows.flatMap { row -> row.sortedBy { it.left } }
}

private const val ROW_OVERLAP = 0.5f
