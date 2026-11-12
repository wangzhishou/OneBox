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

package com.wanbaohe.iching.domain

import com.wanbaohe.iching.model.DivinationResult
import com.wanbaohe.iching.model.HexagramInfo
import com.wanbaohe.iching.model.HexagramLine
import javax.inject.Inject
import kotlin.random.Random

class HexagramGenerator @Inject constructor() {

    fun tossLine(random: Random = Random.Default): HexagramLine {
        val value = List(3) { if (random.nextBoolean()) 3 else 2 }.sum()
        return HexagramLine(value)
    }

    fun create(question: String, lines: List<HexagramLine>): DivinationResult {
        require(lines.size == 6) { "A hexagram must contain exactly six lines" }
        val primary = resolve(lines.map(HexagramLine::isYang))
        val changed = if (lines.any(HexagramLine::isChanging)) {
            resolve(lines.map(HexagramLine::changedIsYang))
        } else null
        return DivinationResult(question.trim(), lines, primary, changed)
    }

    internal fun resolve(lines: List<Boolean>): HexagramInfo {
        require(lines.size == 6)
        val lower = trigramCode(lines.subList(0, 3))
        val upper = trigramCode(lines.subList(3, 6))
        return HexagramInfo(HEXAGRAM_NUMBERS.getValue(upper to lower), upper, lower)
    }

    private fun trigramCode(lines: List<Boolean>): Int =
        lines.foldIndexed(0) { index, result, yang ->
            result or ((if (yang) 1 else 0) shl (2 - index))
        }

    private companion object {
        // Binary codes follow the mapping table: bottom line is the most significant bit.
        val KING_WEN_KEYS = listOf(
            7 to 7, 0 to 0, 2 to 4, 1 to 2, 2 to 7, 7 to 2, 0 to 2, 2 to 0,
            3 to 7, 7 to 6, 0 to 7, 7 to 0, 7 to 5, 5 to 7, 0 to 1, 4 to 0,
            6 to 4, 1 to 3, 0 to 6, 3 to 0, 5 to 4, 1 to 5, 1 to 0, 0 to 4,
            7 to 4, 1 to 7, 1 to 4, 6 to 3, 2 to 2, 5 to 5, 6 to 1, 4 to 3,
            7 to 1, 4 to 7, 5 to 0, 0 to 5, 3 to 5, 5 to 6, 2 to 1, 4 to 2,
            1 to 6, 3 to 4, 6 to 7, 7 to 3, 6 to 0, 0 to 3, 6 to 2, 2 to 3,
            6 to 5, 5 to 3, 4 to 4, 1 to 1, 3 to 1, 4 to 6, 4 to 5, 5 to 1,
            3 to 3, 6 to 6, 3 to 2, 2 to 6, 3 to 6, 4 to 1, 2 to 5, 5 to 2,
        )

        // King Wen sequence, keyed by upper then lower trigram.
        val HEXAGRAM_NUMBERS: Map<Pair<Int, Int>, Int> =
            (1..64).associateBy({ number ->
                // Canonical upper/lower trigram pairs in King Wen order.
                KING_WEN_KEYS[number - 1]
            }, { it })

    }
}


