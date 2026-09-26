package com.wanbaohe.xiangqi.data

import com.wanbaohe.xiangqi.application.port.outbound.MoveDecision
import com.wanbaohe.xiangqi.data.search.XiangqiSearch
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.XiangqiMove
import kotlinx.coroutines.CancellationException

/**
 * 服务端引擎 / LLM 全链路失败时的本地兜底。
 *
 * 主路径是 [XiangqiSearch] 的浅层搜索(迭代加深 alpha-beta + 吃子静态搜索),离线时能正常陪练;
 * 搜索万一抛异常(理论上不该发生)再退到「优先吃子、其次进兵」的贪心挑法,
 * 保证任何情况下都给出合法着法,不会因为兜底自身失败而卡死对局。
 *
 * 两条路径都带 `fallbackUsed = true`,由 [MoveDecision.storedReason] 写入 `local-fallback::` 前缀,
 * UI 据此区分「引擎给的手」与「本地兜底的手」。
 */
internal object HeuristicMoveFallback {

    suspend fun decision(boardState: BoardState, legalMoves: List<XiangqiMove>): MoveDecision? {
        if (legalMoves.isEmpty()) return null

        val searched = try {
            XiangqiSearch.findBestMove(boardState, legalMoves)
        } catch (cancellation: CancellationException) {
            // 协程取消必须原样上抛,否则离开对局页后兜底还在后台空转
            throw cancellation
        } catch (_: Throwable) {
            null
        }

        if (searched != null) {
            return MoveDecision(
                move = searched.move,
                reason = "local-search d=${searched.depth} score=${searched.score} " +
                    "n=${searched.nodes} t=${searched.elapsedMs}ms",
                rawResponse = "local-search",
                fallbackUsed = true,
            )
        }
        return greedy(legalMoves)
    }

    /** 最后的保命路径:不搜索,直接按「能否吃子 / 是否进兵」挑一手 */
    private fun greedy(legalMoves: List<XiangqiMove>): MoveDecision? {
        val move = legalMoves.sortedWith(
            compareByDescending<XiangqiMove> { it.captured != null }
                .thenByDescending { it.notationCn.contains("进") }
        ).firstOrNull() ?: return null
        return MoveDecision(move, "greedy-fallback", "fallback", fallbackUsed = true)
    }
}

/**
 * 保留远程失败原因的同时带上本地搜索的实际深度/节点/耗时,
 * 排障时不至于只看到「失败了」而不知道兜底到底搜到多深。
 */
internal fun MoveDecision.withFailureReason(failure: String): MoveDecision =
    copy(reason = "$failure | $reason", fallbackUsed = true)
