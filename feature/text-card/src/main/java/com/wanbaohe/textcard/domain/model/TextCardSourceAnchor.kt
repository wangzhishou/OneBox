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

package com.wanbaohe.textcard.domain.model

import com.wanbaohe.textcard.domain.render.TextCardLayout

data class TextCardSourceAnchor(
    val role: TextCardRole = TextCardRole.Body,
    val offset: Int = 0,
    val affinity: TextCardAnchorAffinity = TextCardAnchorAffinity.Next,
)

enum class TextCardAnchorAffinity {
    Previous, Next
}

fun TextCardDocument.normalizedAnchor(anchor: TextCardSourceAnchor): TextCardSourceAnchor =
    anchor.copy(offset = anchor.offset.coerceIn(0, block(anchor.role).content.length))

fun TextCardLayout.pageFor(anchor: TextCardSourceAnchor): Int {
    var closestPage = 0
    var closestDistance = Int.MAX_VALUE
    pages.forEachIndexed { pageIndex, page ->
        page.segments.forEach { segment ->
            if (segment.role != anchor.role) return@forEach
            val contains = anchor.offset in segment.sourceStart until segment.sourceEnd
            val atStart = anchor.offset == segment.sourceStart &&
                anchor.affinity == TextCardAnchorAffinity.Next
            val atEnd = anchor.offset == segment.sourceEnd &&
                anchor.affinity == TextCardAnchorAffinity.Previous
            if (contains || atStart || atEnd) return pageIndex
            val distance = when {
                anchor.offset < segment.sourceStart -> segment.sourceStart - anchor.offset
                anchor.offset >= segment.sourceEnd -> anchor.offset - segment.sourceEnd + 1
                else -> 0
            }
            if (distance < closestDistance) {
                closestDistance = distance
                closestPage = pageIndex
            }
        }
    }
    return closestPage.coerceIn(0, (pages.size - 1).coerceAtLeast(0))
}

data class TextCardManualPageBreak(
    val offset: Int,
    val before: String,
    val after: String,
)

fun String.manualPageBreaks(): List<TextCardManualPageBreak> = buildList {
    var start = 0
    while (start < length) {
        val offset = indexOf(TextCardDocument.MANUAL_PAGE_BREAK, start)
        if (offset < 0) break
        add(
            TextCardManualPageBreak(
                offset = offset,
                before = substring(0, offset).trimEnd().takeLast(24),
                after = substring(offset + 1).trimStart().take(24),
            )
        )
        start = offset + 1
    }
}

fun TextCardRenderState.startsAtManualPageBreak(document: TextCardDocument): Boolean {
    val segment = segments.firstOrNull() ?: return false
    return document.block(segment.role).content.getOrNull(segment.sourceStart - 1) ==
        TextCardDocument.MANUAL_PAGE_BREAK
}
