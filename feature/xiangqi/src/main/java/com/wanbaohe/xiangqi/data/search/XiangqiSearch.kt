package com.wanbaohe.xiangqi.data.search

import com.wanbaohe.xiangqi.domain.MoveGenerator
import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.Piece
import com.wanbaohe.xiangqi.domain.model.PieceType
import com.wanbaohe.xiangqi.domain.model.Side
import com.wanbaohe.xiangqi.domain.model.XiangqiMove
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * 端侧兜底用的浅层象棋搜索引擎:迭代加深 negamax + alpha-beta + 吃子静态搜索。
 *
 * 定位是「服务端 Pikafish 不可达时的最后一道防线」,因此设计取舍偏可用性:
 * - 有时间预算(默认 700ms)与节点上限双保险,任何设备上都不会把对局卡住;
 * - 迭代加深逐层加深,超时/超节点就沿用上一层已完成的最优着,绝不返回空;
 * - 候选着法直接用 [MoveGenerator](唯一事实来源),自身不重复实现走子规则,
 *   只把合法性判定换成 [XiangqiAttacks] 的快速检测;
 * - 上层 [com.wanbaohe.xiangqi.data.HeuristicMoveFallback] 仍保留贪心兜底,
 *   本搜索抛任何异常都不会让走棋失败。
 *
 * 与规则的一致性:象棋「无着可走即为负」(困毙等于被将死),所以无合法着直接返回 -MATE;
 * 和棋沿用 [com.wanbaohe.xiangqi.domain.GameArbiter] 的 120 半回合判和口径。
 */
internal object XiangqiSearch {

    /** 杀棋分。低于此值即为普通局面分,便于外层识别「算到杀」 */
    const val MATE_SCORE = 30_000

    private const val INFINITY = 1_000_000
    private const val MAX_PLY = 48
    private const val DRAW_HALF_MOVE_CLOCK = 120
    private const val DEFAULT_TIME_BUDGET_MS = 700L
    private const val DEFAULT_MAX_DEPTH = 6
    private const val DEFAULT_MAX_NODES = 150_000

    /** 时间/节点用尽:仅用于在递归里跳出,不外传 */
    private object SearchAbort : RuntimeException() {
        /** 用单例 + 不采集栈帧:搜索里会反复抛出,采栈会拖慢热路径 */
        override fun fillInStackTrace(): Throwable = this
    }

    data class Result(
        val move: XiangqiMove,
        val score: Int,
        val depth: Int,
        val nodes: Int,
        val elapsedMs: Long,
    )

    suspend fun findBestMove(
        boardState: BoardState,
        legalMoves: List<XiangqiMove>,
        timeBudgetMs: Long = DEFAULT_TIME_BUDGET_MS,
        maxDepth: Int = DEFAULT_MAX_DEPTH,
        maxNodes: Int = DEFAULT_MAX_NODES,
    ): Result? = withContext(Dispatchers.Default) {
        if (legalMoves.isEmpty()) return@withContext null

        val side = boardState.sideToMove
        val startedAt = System.nanoTime()
        val context = SearchContext(startedAt, timeBudgetMs * 1_000_000, maxNodes)
        val killers = arrayOfNulls<XiangqiMove>(MAX_PLY)

        // 兜底结果:即使第一层都没搜完,也要给出一手(按吃子价值排序最优先的那个)
        val orderedRoot = orderMoves(legalMoves, null)
        var best = orderedRoot.first()
        var bestScore = 0
        var completedDepth = 0

        try {
            for (depth in 1..maxDepth) {
                // 每层开始时响应协程取消(离开对局页时不必等满整个预算)
                currentCoroutineContext().ensureActive()

                var alpha = -INFINITY
                var depthBest: XiangqiMove? = null
                var depthBestScore = -INFINITY

                for (move in orderMoves(orderedRoot, best)) {
                    val score = if (move.captured?.type == PieceType.KING) {
                        // 飞将吃将:直接就是杀,不必再搜
                        MATE_SCORE - 1
                    } else {
                        val next = boardState.withPieceMoved(move)
                        -negamax(
                            state = next,
                            side = side.opposite(),
                            depth = depth - 1,
                            alpha = -INFINITY,
                            beta = -alpha,
                            ply = 1,
                            killers = killers,
                            context = context,
                        )
                    }
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
                // 已经算到杀棋,再深也只会更慢
                if (bestScore >= MATE_SCORE - MAX_PLY) break
            }
        } catch (_: SearchAbort) {
            // 超时/超节点:保留上一层完成深度的结果
        }

        val mapped = legalMoves.firstOrNull { it.from == best.from && it.to == best.to } ?: best
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
        killers: Array<XiangqiMove?>,
        context: SearchContext,
    ): Int {
        context.visit()
        if (depth <= 0 || ply >= MAX_PLY) {
            return quiescence(state, side, alpha, beta, ply, context)
        }

        val king = findKing(state.board, side) ?: return -MATE_SCORE + ply
        if (state.halfMoveClock >= DRAW_HALF_MOVE_CLOCK) return 0

        val moves = legalMovesOf(state, side, king)
        // 象棋无着可走即负(含被将死与困毙),与 GameArbiter.evaluateStatus 同口径
        if (moves.isEmpty()) return -MATE_SCORE + ply

        var best = -INFINITY
        var currentAlpha = alpha
        for (move in orderMoves(moves, killers[ply])) {
            if (move.captured?.type == PieceType.KING) return MATE_SCORE - ply

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
                // 吃子着法本身就是好排序,只把安静着法记为杀手,避免覆盖
                if (move.captured == null) killers[ply] = move
                break
            }
        }
        return best
    }

    /**
     * 吃子静态搜索:只展开吃子,消除「 horizon 效应」——否则浅层搜索会在
     * 兑子中途停手,把「下一步就被吃回」的假便宜当成好棋(兜底棋力最刺眼的毛病)。
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
        val king = findKing(state.board, side) ?: return -MATE_SCORE + ply

        var currentAlpha = alpha
        val standPat = XiangqiEvaluator.evaluate(state.board, side)
        if (standPat >= beta) return beta
        if (standPat > currentAlpha) currentAlpha = standPat
        if (ply >= MAX_PLY) return currentAlpha

        val captures = legalMovesOf(state, side, king).filter { it.captured != null }
        for (move in orderMoves(captures, null)) {
            if (move.captured?.type == PieceType.KING) return MATE_SCORE - ply

            val next = state.withPieceMoved(move)
            val score = -quiescence(next, side.opposite(), -beta, -currentAlpha, ply + 1, context)
            if (score >= beta) return beta
            if (score > currentAlpha) currentAlpha = score
        }
        return currentAlpha
    }

    /**
     * 合法着法 = 伪合法着法过滤自陷将军。[king] 由调用方传入,
     * 只有走将帅时才需要重新定位,省掉每个候选着法一次 90 格扫描。
     */
    private fun legalMovesOf(state: BoardState, side: Side, king: BoardPoint): List<XiangqiMove> {
        val opponent = side.opposite()
        return MoveGenerator.pseudoLegalMoves(state, side).filter { move ->
            val movedKing = if (move.piece.type == PieceType.KING) move.to else king
            val next = state.withPieceMoved(move)
            !XiangqiAttacks.isKingAttacked(next.board, movedKing, opponent)
        }
    }

    /**
     * 着法排序:上一层最优着优先(迭代加深的核心收益),其次 MVV-LVA
     * (吃大子、用小子吃),最后是指定杀手着法。排序质量直接决定 alpha-beta 的剪枝率。
     */
    private fun orderMoves(moves: List<XiangqiMove>, preferred: XiangqiMove?): List<XiangqiMove> {
        if (moves.size <= 1) return moves
        return moves.sortedByDescending { move ->
            var score = 0
            if (preferred != null && move.from == preferred.from && move.to == preferred.to) {
                score += 1_000_000
            }
            val victim = move.captured
            if (victim != null) {
                score += 10_000 + victimValue(victim.type) * 10 - victimValue(move.piece.type)
            }
            score
        }
    }

    private fun victimValue(type: PieceType): Int = when (type) {
        PieceType.KING -> 10_000
        PieceType.ROOK -> 900
        PieceType.CANNON -> 450
        PieceType.KNIGHT -> 400
        PieceType.BISHOP -> 200
        PieceType.ADVISOR -> 200
        PieceType.PAWN -> 100
    }

    private fun findKing(cells: List<Piece?>, side: Side): BoardPoint? {
        for (index in cells.indices) {
            val piece = cells[index] ?: continue
            if (piece.side == side && piece.type == PieceType.KING) {
                return BoardPoint(index % BoardPoint.FILE_COUNT, index / BoardPoint.FILE_COUNT)
            }
        }
        return null
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
            // nanoTime 不便宜,每 1024 个节点查一次表,精度损失远小于调用开销
            if ((nodes and 0x3FF) == 0 && System.nanoTime() - startedAtNanos > timeBudgetNanos) {
                throw SearchAbort
            }
        }
    }
}
