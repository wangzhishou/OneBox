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

package com.wanbaohe.mahjong.domain.rules

import com.wanbaohe.mahjong.domain.model.FanType
import com.wanbaohe.mahjong.domain.model.Meld
import com.wanbaohe.mahjong.domain.model.MeldType
import com.wanbaohe.mahjong.domain.model.Tiles

object WinDetector {

    fun isWinningHand(hand: List<Int>, melds: List<Meld>): Boolean {
        if (hand.size + melds.size * 3 != 14) return false
        if (isSevenPairs(hand, melds)) return true
        return canFormStandard(countsOf(hand), melds.size, sequencesAllowed = true)
    }

    fun evaluate(
        hand: List<Int>,
        melds: List<Meld>,
        isSelfDraw: Boolean,
        isRobbingKong: Boolean,
    ): List<FanType> {
        if (!isWinningHand(hand, melds)) return emptyList()
        val fans = mutableListOf(FanType.BASE)
        if (isSevenPairs(hand, melds)) fans += FanType.SEVEN_PAIRS
        if (isAllPungs(hand, melds)) fans += FanType.ALL_PUNGS
        if (isOneSuit(hand, melds)) fans += FanType.ONE_SUIT
        if (isSelfDraw) fans += FanType.SELF_DRAW
        if (isRobbingKong) fans += FanType.ROB_KONG
        return fans
    }

    fun pointsFor(fans: List<FanType>): Int {
        val fanCount = fans.sumOf {
            when (it) {
                FanType.BASE -> 1
                FanType.SEVEN_PAIRS, FanType.ALL_PUNGS, FanType.SELF_DRAW,
                FanType.ROB_KONG -> 2
                FanType.ONE_SUIT -> 4
            }
        }
        return 10 * (1 shl (fanCount - 1).coerceAtMost(4))
    }

    private fun isSevenPairs(hand: List<Int>, melds: List<Meld>): Boolean =
        melds.isEmpty() && hand.size == 14 && countsOf(hand).all { it % 2 == 0 }

    private fun isAllPungs(hand: List<Int>, melds: List<Meld>): Boolean {
        if (melds.any { it.type == MeldType.CHI }) return false
        return canFormStandard(countsOf(hand), melds.size, sequencesAllowed = false)
    }

    private fun isOneSuit(hand: List<Int>, melds: List<Meld>): Boolean {
        val tileIds = hand + melds.flatMap { it.tileIds }
        val firstKind = tileIds.firstOrNull()?.let(Tiles::kindOf) ?: return false
        if (!Tiles.isNumeric(firstKind)) return false
        val suit = Tiles.suitOfKind(firstKind)
        return tileIds.all { Tiles.suitOfKind(Tiles.kindOf(it)) == suit }
    }

    private fun countsOf(hand: List<Int>): IntArray =
        IntArray(Tiles.KIND_COUNT).also { counts ->
            hand.forEach { counts[Tiles.kindOf(it)]++ }
        }

    private fun canFormStandard(
        counts: IntArray,
        meldCount: Int,
        sequencesAllowed: Boolean,
    ): Boolean {
        val groupsNeeded = 4 - meldCount
        if (groupsNeeded < 0) return false
        for (pair in counts.indices) {
            if (counts[pair] < 2) continue
            val next = counts.copyOf()
            next[pair] -= 2
            if (formGroups(next, groupsNeeded, sequencesAllowed)) return true
        }
        return false
    }

    private fun formGroups(counts: IntArray, remaining: Int, sequencesAllowed: Boolean): Boolean {
        if (remaining == 0) return counts.all { it == 0 }
        val first = counts.indexOfFirst { it > 0 }
        if (first < 0) return false

        if (counts[first] >= 3) {
            val next = counts.copyOf()
            next[first] -= 3
            if (formGroups(next, remaining - 1, sequencesAllowed)) return true
        }

        if (sequencesAllowed && Tiles.isNumeric(first) && Tiles.rankOfKind(first) <= 7) {
            val second = first + 1
            val third = first + 2
            if (counts[second] > 0 && counts[third] > 0) {
                val next = counts.copyOf()
                next[first]--
                next[second]--
                next[third]--
                if (formGroups(next, remaining - 1, sequencesAllowed)) return true
            }
        }
        return false
    }
}
