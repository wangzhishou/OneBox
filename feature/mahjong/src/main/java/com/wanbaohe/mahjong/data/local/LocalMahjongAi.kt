package com.wanbaohe.mahjong.data.local

import com.wanbaohe.mahjong.application.port.outbound.AiObservation
import com.wanbaohe.mahjong.application.port.outbound.MahjongAi
import com.wanbaohe.mahjong.domain.model.ClaimType
import com.wanbaohe.mahjong.domain.model.MahjongAction
import com.wanbaohe.mahjong.domain.model.Meld
import com.wanbaohe.mahjong.domain.model.MeldType
import com.wanbaohe.mahjong.domain.model.Tiles
import com.wanbaohe.mahjong.domain.rules.ShantenCalculator
import javax.inject.Inject

class LocalMahjongAi @Inject constructor() : MahjongAi {

    override fun chooseAction(observation: AiObservation): MahjongAction {
        observation.legalActions.filterIsInstance<MahjongAction.SelfHu>().firstOrNull()?.let {
            return it
        }
        val responses = observation.legalActions.filterIsInstance<MahjongAction.Respond>()
        responses.firstOrNull { it.choice.type == ClaimType.HU }?.let { return it }
        responses.firstOrNull { it.choice.type == ClaimType.KONG }?.let { return it }
        observation.legalActions.filterIsInstance<MahjongAction.ConcealedKong>()
            .firstOrNull()?.let { return it }
        observation.legalActions.filterIsInstance<MahjongAction.AddedKong>()
            .firstOrNull()?.let { return it }

        if (responses.isNotEmpty()) return chooseResponse(observation, responses)
        return chooseDiscard(observation)
    }

    private fun chooseResponse(
        observation: AiObservation,
        responses: List<MahjongAction.Respond>,
    ): MahjongAction {
        val pendingTile = observation.pendingTileId
        val currentShanten = ShantenCalculator.calculate(observation.hand, observation.melds)
        val candidates = responses.mapNotNull { response ->
            val choice = response.choice
            if (choice.type != ClaimType.PONG && choice.type != ClaimType.CHI) {
                return@mapNotNull null
            }
            val claimedKind = pendingTile?.let(Tiles::kindOf) ?: return@mapNotNull null
            val consumedKinds = when (choice.type) {
                ClaimType.PONG -> listOf(claimedKind, claimedKind)
                ClaimType.CHI -> listOf(choice.chiStart, choice.chiStart + 1, choice.chiStart + 2)
                    .filter { it != claimedKind }
                else -> emptyList()
            }
            val consumed = observation.hand.takeByKinds(consumedKinds)
            val remaining = observation.hand.removeAllOnce(consumed)
            val meldType = if (choice.type == ClaimType.PONG) MeldType.PONG else MeldType.CHI
            val meld = Meld(meldType, consumed + pendingTile, observation.seat)
            val shanten = ShantenCalculator.calculate(remaining, observation.melds + meld)
            Triple(response, shanten, shapeScore(remaining))
        }
        val bestPong = candidates.filter { it.first.choice.type == ClaimType.PONG }
            .minWithOrNull(compareBy({ it.second }, { -it.third }))
        if (bestPong != null && bestPong.second <= currentShanten) return bestPong.first

        val bestChi = candidates.filter { it.first.choice.type == ClaimType.CHI }
            .minWithOrNull(compareBy({ it.second }, { -it.third }))
        if (bestChi != null && bestChi.second < currentShanten) return bestChi.first

        return responses.first { it.choice.type == ClaimType.PASS }
    }

    private fun chooseDiscard(observation: AiObservation): MahjongAction {
        val discards = observation.legalActions.filterIsInstance<MahjongAction.Discard>()
        require(discards.isNotEmpty()) { "AI has no legal action" }
        val candidates = discards
            .distinctBy { Tiles.kindOf(it.tileId) }
            .map { action ->
                val remaining = observation.hand.removeOne(action.tileId)
                val shanten = ShantenCalculator.calculate(remaining, observation.melds)
                DiscardCandidate(action, remaining, shanten, shapeScore(remaining))
            }
        val bestShanten = candidates.minOf { it.shanten }
        return candidates.map { candidate ->
            val effective = if (candidate.shanten <= bestShanten + 1) {
                effectiveTileCount(observation, candidate)
            } else 0
            val kind = Tiles.kindOf(candidate.action.tileId)
            val safety = when {
                observation.visibleCounts[kind] >= 3 -> 0
                !Tiles.isNumeric(kind) -> 2
                Tiles.rankOfKind(kind) in setOf(1, 9) -> 4
                else -> 8
            }
            val score = -candidate.shanten * 10_000 +
                effective * 40 + candidate.shape * 8 - safety
            candidate to score
        }.maxWithOrNull(compareBy({ it.second }, { -Tiles.kindOf(it.first.action.tileId) }))!!
            .first.action
    }

    private fun effectiveTileCount(
        observation: AiObservation,
        candidate: DiscardCandidate,
    ): Int {
        var total = 0
        for (kind in 0 until Tiles.KIND_COUNT) {
            val unseen = 4 - observation.visibleCounts[kind]
            if (unseen <= 0) continue
            val simulated = candidate.remaining + kind * 4
            if (ShantenCalculator.calculate(simulated, observation.melds) < candidate.shanten) {
                total += unseen
            }
        }
        return total
    }

    private fun shapeScore(hand: List<Int>): Int {
        val counts = hand.groupingBy(Tiles::kindOf).eachCount()
        var score = counts.values.sumOf { if (it >= 2) 8 else 0 }
        counts.keys.filter(Tiles::isNumeric).forEach { kind ->
            if (counts.getOrElse(kind + 1) { 0 } > 0 && Tiles.rankOfKind(kind) <= 8) score += 5
            if (counts.getOrElse(kind + 2) { 0 } > 0 && Tiles.rankOfKind(kind) <= 7) score += 3
            if (counts.getOrElse(kind - 1) { 0 } > 0 && Tiles.rankOfKind(kind) >= 2) score += 2
            if (counts.getOrElse(kind - 2) { 0 } > 0 && Tiles.rankOfKind(kind) >= 3) score += 1
        }
        return score - hand.count { Tiles.isNumeric(Tiles.kindOf(it)) && isIsolated(it, counts) } * 2
    }

    private fun isIsolated(tileId: Int, counts: Map<Int, Int>): Boolean {
        val kind = Tiles.kindOf(tileId)
        return (kind - 2..kind + 2).filter { it != kind }.none { counts.getOrElse(it) { 0 } > 0 } &&
            counts.getOrElse(kind) { 0 } == 1
    }

    private data class DiscardCandidate(
        val action: MahjongAction.Discard,
        val remaining: List<Int>,
        val shanten: Int,
        val shape: Int,
    )
}

private fun List<Int>.removeOne(value: Int): List<Int> =
    toMutableList().apply { remove(value) }

private fun List<Int>.takeByKinds(kinds: List<Int>): List<Int> {
    val remaining = toMutableList()
    return kinds.map { kind ->
        val index = remaining.indexOfFirst { Tiles.kindOf(it) == kind }
        require(index >= 0)
        remaining.removeAt(index)
    }
}

private fun List<Int>.removeAllOnce(values: List<Int>): List<Int> {
    val remaining = toMutableList()
    values.forEach { remaining.remove(it) }
    return remaining
}
