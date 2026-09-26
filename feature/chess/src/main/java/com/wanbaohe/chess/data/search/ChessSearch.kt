package com.wanbaohe.chess.data.search

import com.wanbaohe.chess.domain.GameArbiter
import com.wanbaohe.chess.domain.MoveGenerator
import com.wanbaohe.chess.domain.model.BoardState
import com.wanbaohe.chess.domain.model.ChessMove
import com.wanbaohe.chess.domain.model.PieceType
import com.wanbaohe.chess.domain.model.Side
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * 端侧兜底用的浅层国际象棋搜索引擎:迭代加深 negamax + alpha-beta + 战术静态搜索。
 *
 * 定位与象棋那边一致——服务端 Stockfish 不可达时的最后一道防线,取舍偏可用性:
 * 时间预算(默认 900ms)与节点上限双保险;迭代加深保证超时也能给出上一层的最优着;
 * 走子规则完全复用 [MoveGenerator],只把合法性判定换成直接调 [GameArbiter.isInCheck]
 * (它内部是按攻击几何检测的,不像象棋那边要生成全部着法,所以这里不必另写一份)。
 *
 * 与规则的一致性:将死 -MATE、逼和 0、「50 回合」判和沿用 [GameArbiter] 的 100 半回合口径。
 * 升变在静态搜索里也展开——升变是战术手段,漏掉会让搜索在残局严重误判。
 */
internal object ChessSearch {

    /** 杀棋分(厘兵)。远高于任何正常局面分,外层可据此识别「算到杀」 */
    const val MATE_SCORE = 100_000

    private const val INFINITY = 10_000_000
    private const val MAX_PLY = 48
    private const val DRAW_HALF_MOVE_CLOCK = 100
    private const val DEFAULT_TIME_BUDGET_MS = 900L
    private const val DEFAULT_MAX_DEPTH = 5
    private const val DEFAULT_MAX_NODES = 150_000

    /** 时间/节点用尽:仅用于跳出递归,不外传 */
    private object SearchAbort : RuntimeException() {
        /** 用单例 + 不采集栈帧:搜索里会反复抛出,采栈会拖慢热路径 */
        override fun fillInStackTrace(): Throwable = this
    }

    data class Result(
        val move: ChessMove,
        val score: Int,
        val depth: Int,
        val nodes: Int,
        val elapsedMs: Long,
    )

    suspend fun findBestMove(
        boardState: BoardState,
        legalMoves: List<ChessMove>,
        timeBudgetMs: Long = DEFAULT_TIME_BUDGET_MS,
        maxDepth: Int = DEFAULT_MAX_DEPTH,
        maxNodes: Int = DEFAULT_MAX_NODES,
    ): Result? = withContext(Dispatchers.Default) {
        if (legalMoves.isEmpty()) return@withContext null

        val side = boardState.sideToMove
        val startedAt = System.nanoTime()
        val context = SearchContext(startedAt, timeBudgetMs * 1_000_000, maxNodes)
        val killers = arrayOfNulls<ChessMove>(MAX_PLY)

        val orderedRoot = orderMoves(legalMoves, null)
        var best = orderedRoot.first()
        var bestScore = 0
        var completedDepth = 0

        try {
            for (depth in 1..maxDepth) {
                currentCoroutineContext().ensureActive()

                var alpha = -INFINITY
                var depthBest: ChessMove? = null
                var depthBestScore = -INFINITY

                for (move in orderMoves(orderedRoot, best)) {
                    val next = boardState.withPieceMoved(move)
                    val score = -negamax(
                        state = next,
                        side = side.opposite(),
                        depth = depth - 1,
                        alpha = -INFINITY,
                        beta = -alpha,
                        ply = 1,
                        killers = killers,
                        context = context,
                    )
                    if (score > depthBestScore) {
                        depthBestScore = score
                        depthBest = move
                    }
                    if (score > alpha) alpha = score
                }

                depthBest?.let {
                    best = it
                    bestScore = depthBestScore
                    completedDepth = depth
                }
                if (bestScore >= MATE_SCORE - MAX_PLY) break
            }
        } catch (_: SearchAbort) {
            // 超时/超节点:保留上一层完成深度的结果
        }

        val mapped = legalMoves.firstOrNull {
            it.from == best.from && it.to == best.to && it.promotion == best.promotion
        } ?: best
        Result(
            move = mapped,
            score = bestScore,
            depth = completedDepth,
            nodes = context.nodes,
            elapsedMs = (System.nanoTime() - startedAt) / 1_000_000,
        )
    }

    private fun negamax(
        state: BoardState,
        side: Side,
        depth: Int,
        alpha: Int,
        beta: Int,
        ply: Int,
        killers: Array<ChessMove?>,
        context: SearchContext,
    ): Int {
        context.visit()
        if (depth <= 0 || ply >= MAX_PLY) {
            return quiescence(state, side, alpha, beta, ply, context)
        }
        if (state.halfMoveClock >= DRAW_HALF_MOVE_CLOCK) return 0

        val moves = legalMovesOf(state, side)
        if (moves.isEmpty()) {
            // 无着可走:被将是将死(负),否则是逼和
            return if (GameArbiter.isInCheck(state, side)) -MATE_SCORE + ply else 0
        }

        var best = -INFINITY
        var currentAlpha = alpha
        for (move in orderMoves(moves, killers[ply])) {
            val next = state.withPieceMoved(move)
            val score = -negamax(
                state = next,
                side = side.opposite(),
                depth = depth - 1,
                alpha = -beta,
                beta = -currentAlpha,
                ply = ply + 1,
                killers = killers,
                context = context,
            )
            if (score > best) best = score
            if (score > currentAlpha) currentAlpha = score
            if (currentAlpha >= beta) {
                if (move.captured == null && move.promotion == null) killers[ply] = move
                break
            }
        }
        return best
    }

    /**
     * 战术静态搜索:只展开吃子与升变,消除 horizon 效应——
     * 否则搜索会在兑子半途停手,把「下一步被吃回」的假便宜当好棋。
     */
    private fun quiescence(
        state: BoardState,
        side: Side,
        alpha: Int,
        beta: Int,
        ply: Int,
        context: SearchContext,
    ): Int {
        context.visit()

        var currentAlpha = alpha
        val standPat = ChessEvaluator.evaluate(state.board, side)
        if (standPat >= beta) return beta
        if (standPat > currentAlpha) currentAlpha = standPat
        if (ply >= MAX_PLY) return currentAlpha

        val tactical = legalMovesOf(state, side).filter { it.captured != null || it.promotion != null }
        for (move in orderMoves(tactical, null)) {
            val next = state.withPieceMoved(move)
            val score = -quiescence(next, side.opposite(), -beta, -currentAlpha, ply + 1, context)
            if (score >= beta) return beta
            if (score > currentAlpha) currentAlpha = score
        }
        return currentAlpha
    }

    /** 合法着法 = 伪合法着法过滤自陷将军 */
    private fun legalMovesOf(state: BoardState, side: Side): List<ChessMove> =
        MoveGenerator.pseudoLegalMoves(state, side)
            .filter { !GameArbiter.isInCheck(state.withPieceMoved(it), side) }

    /**
     * 着法排序:上一层最优着优先,其次升变,最后 MVV-LVA(吃大子、用小子吃)。
     * 排序质量直接决定 alpha-beta 的剪枝率,升变不排前面会漏掉战术。
     */
    private fun orderMoves(moves: List<ChessMove>, preferred: ChessMove?): List<ChessMove> {
        if (moves.size <= 1) return moves
        return moves.sortedByDescending { move ->
            var score = 0
            if (preferred != null && move.from == preferred.from &&
                move.to == preferred.to && move.promotion == preferred.promotion
            ) {
                score += 10_000_000
            }
            move.promotion?.let { score += 900_000 + pieceValue(it) }
            val victim = move.captured
            if (victim != null) {
                score += 100_000 + pieceValue(victim.type) * 10 - pieceValue(move.piece.type)
            }
            score
        }
    }

    private fun pieceValue(type: PieceType): Int = when (type) {
        PieceType.PAWN -> 100
        PieceType.KNIGHT -> 320
        PieceType.BISHOP -> 330
        PieceType.ROOK -> 500
        PieceType.QUEEN -> 900
        PieceType.KING -> 100_000
    }

    private class SearchContext(
        private val startedAtNanos: Long,
        private val timeBudgetNanos: Long,
        private val maxNodes: Int,
    ) {
        var nodes: Int = 0
            private set

        fun visit() {
            nodes++
            if (nodes >= maxNodes) throw SearchAbort
            if ((nodes and 0x3FF) == 0 && System.nanoTime() - startedAtNanos > timeBudgetNanos) {
                throw SearchAbort
            }
        }
    }
}
