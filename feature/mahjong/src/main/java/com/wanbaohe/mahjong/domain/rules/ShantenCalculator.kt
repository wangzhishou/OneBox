package com.wanbaohe.mahjong.domain.rules

import com.wanbaohe.mahjong.domain.model.Meld
import com.wanbaohe.mahjong.domain.model.Tiles

object ShantenCalculator {

    fun calculate(hand: List<Int>, melds: List<Meld> = emptyList()): Int {
        val counts = IntArray(Tiles.KIND_COUNT)
        hand.forEach { counts[Tiles.kindOf(it)]++ }
        val standard = standardShanten(counts, melds.size)
        if (melds.isNotEmpty()) return standard
        return minOf(standard, sevenPairsShanten(counts))
    }

    private fun sevenPairsShanten(counts: IntArray): Int {
        val pairs = counts.count { it >= 2 }.coerceAtMost(7)
        val distinct = counts.count { it > 0 }
        return 6 - pairs + (7 - distinct).coerceAtLeast(0)
    }

    private fun standardShanten(counts: IntArray, meldCount: Int): Int {
        var best = 8

        fun update(groups: Int, hasPair: Boolean, partials: Int) {
            val complete = meldCount + groups
            val usablePartials = partials.coerceAtMost((4 - complete).coerceAtLeast(0))
            val value = 8 - 2 * (complete + usablePartials) - if (hasPair) 1 else 0
            best = minOf(best, value)
        }

        fun visit(current: IntArray, groups: Int, hasPair: Boolean, partials: Int) {
            update(groups, hasPair, partials)
            val first = current.indexOfFirst { it > 0 }
            if (first < 0) return

            if (current[first] >= 3) {
                val next = current.copyOf()
                next[first] -= 3
                visit(next, groups + 1, hasPair, partials)
            }

            if (Tiles.isNumeric(first) && Tiles.rankOfKind(first) <= 7 &&
                current[first + 1] > 0 && current[first + 2] > 0
            ) {
                val next = current.copyOf()
                next[first]--
                next[first + 1]--
                next[first + 2]--
                visit(next, groups + 1, hasPair, partials)
            }

            if (current[first] >= 2) {
                if (!hasPair) {
                    val pair = current.copyOf()
                    pair[first] -= 2
                    visit(pair, groups, true, partials)
                }
                val partial = current.copyOf()
                partial[first] -= 2
                visit(partial, groups, hasPair, partials + 1)
            }

            if (Tiles.isNumeric(first)) {
                val rank = Tiles.rankOfKind(first)
                if (rank <= 8 && current[first + 1] > 0) {
                    val next = current.copyOf()
                    next[first]--
                    next[first + 1]--
                    visit(next, groups, hasPair, partials + 1)
                }
                if (rank <= 7 && current[first + 2] > 0) {
                    val next = current.copyOf()
                    next[first]--
                    next[first + 2]--
                    visit(next, groups, hasPair, partials + 1)
                }
            }

            val skipped = current.copyOf()
            skipped[first]--
            visit(skipped, groups, hasPair, partials)
        }

        visit(counts, groups = 0, hasPair = false, partials = 0)
        return best.coerceAtLeast(-1)
    }
}
