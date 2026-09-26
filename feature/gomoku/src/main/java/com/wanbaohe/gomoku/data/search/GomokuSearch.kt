package com.wanbaohe.gomoku.data.search

import com.wanbaohe.gomoku.domain.GameArbiter
import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.GomokuMove
import com.wanbaohe.gomoku.domain.model.Side
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * 端侧兜底用的浅层五子棋搜索引擎:迭代加深 negamax + alpha-beta。
 *
 * 与象棋/国际象棋那两个不同,五子棋的分支因子天然极大(15×15 全空位),
 * 所以这里的核心不是搜索深度而是**候选点裁剪**:只考虑已有棋子附近(切比雪夫距离 ≤ 2)的空位,
 * 并按棋型分排序取前若干。有了裁剪,浅层 alpha-beta 就能覆盖「活三/冲四/双威胁」这类
 * 原兜底完全看不见的两步手段。
 *
 * 三层决策,由快到慢:
 * 1. 我方能直接成五 → 立刻走(不必搜);
 * 2. 对方能直接成五 → 立刻堵(不堵必败,搜也是同一个结论,但这里省掉整棵树);
 * 3. 其余情形交给迭代加深搜索,时间预算(默认 450ms)与节点上限双保险。
 */
internal object GomokuSearch {

    private const val INFINITY = 100_000_000
    private const val MAX_PLY = 24
    private const val DEFAULT_TIME_BUDGET_MS = 450L
    private const val DEFAULT_MAX_DEPTH = 4
    private const val DEFAULT_MAX_NODES = 25_000
    private const val ROOT_CANDIDATES = 10
    private const val INNER_CANDIDATES = 6

    /** 时间/节点用尽:仅用于跳出递归,不外传 */
    private object SearchAbort : RuntimeException() {
        /** 用单例 + 不采集栈帧:搜索里会反复抛出,采栈会拖慢热路径 */
        override fun fillInStackTrace(): Throwable = this
    }

    data class Result(
        val move: GomokuMove,
        val score: Int,
        val depth: Int,
        val nodes: Int,
        val elapsedMs: Long,
    )

    suspend fun findBestMove(
        boardState: BoardState,
        legalMoves: List<GomokuMove>,
        timeBudgetMs: Long = DEFAULT_TIME_BUDGET_MS,
        maxDepth: Int = DEFAULT_MAX_DEPTH,
        maxNodes: Int = DEFAULT_MAX_NODES,
    ): Result? = withContext(Dispatchers.Default) {
        if (legalMoves.isEmpty()) return@withContext null

        val side = boardState.sideToMove
        val startedAt = System.nanoTime()
        val context = SearchContext(startedAt, timeBudgetMs * 1_000_000, maxNodes)

        val candidates = GomokuEvaluator.candidates(boardState.board, side, ROOT_CANDIDATES)
        if (candidates.isEmpty()) return@withContext null

        fun result(move: GomokuMove, score: Int, depth: Int) = Result(
            move = legalMoves.firstOrNull { it.to == move.to } ?: move,
            score = score,
            depth = depth,
            nodes = context.nodes,
            elapsedMs = (System.nanoTime() - startedAt) / 1_000_000,
        )

        // 1. 能成五就直接赢
        candidates.firstOrNull { move -> completesFive(boardState, move.to, side) }
            ?.let { return@withContext result(it, GomokuEvaluator.WIN_SCORE, 1) }

        // 2. 对方能成五就必须堵(堵在同一个点上)
        val opponent = side.opposite()
        candidates.firstOrNull { move -> completesFive(boardState, move.to, opponent) }
            ?.let { return@withContext result(it, 0, 1) }

        val orderedRoot = candidates
        var best = orderedRoot.first()
        var bestScore = 0
        var completedDepth = 0

        try {
            for (depth in 1..maxDepth) {
                // 每层开始时响应协程取消
                currentCoroutineContext().ensureActive()

                var alpha = -INFINITY
                var depthBest: GomokuMove? = null
                var depthBestScore = -INFINITY

                for (move in orderedRoot) {
                    val next = boardState.withStonePlaced(move)
                    val score = -negamax(
                        state = next,
                        side = opponent,
                        depth = depth - 1,
                        alpha = -INFINITY,
                        beta = -alpha,
                        ply = 1,
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
                if (bestScore >= GomokuEvaluator.WIN_SCORE - MAX_PLY) break
            }
        } catch (_: SearchAbort) {
            // 超时/超节点:保留上一层完成深度的结果
        }

        result(best, bestScore, completedDepth)
    }

    private fun negamax(
        state: BoardState,
        side: Side,
        depth: Int,
        alpha: Int,
        beta: Int,
        ply: Int,
        context: SearchContext,
    ): Int {
        context.visit()
        if (depth <= 0 || ply >= MAX_PLY) {
            return GomokuEvaluator.evaluate(state.board, side)
        }

        val candidates = GomokuEvaluator.candidates(state.board, side, INNER_CANDIDATES)
        if (candidates.isEmpty()) return 0

        var currentAlpha = alpha
        var best = -INFINITY
        for (move in candidates) {
            // 子局面递归本来就要建,顺手用它判五连,避免额外复制棋盘
            val next = state.withStonePlaced(move)
            if (GameArbiter.hasFive(next, move.to, side)) {
                // 成五即胜,越早越好:用 ply 体现「几步杀」
                return GomokuEvaluator.WIN_SCORE - ply
            }
            val score = -negamax(
                state = next,
                side = side.opposite(),
                depth = depth - 1,
                alpha = -beta,
                beta = -currentAlpha,
                ply = ply + 1,
                context = context,
            )
            if (score > best) best = score
            if (score > currentAlpha) currentAlpha = score
            if (currentAlpha >= beta) break
        }
        return best
    }

    /** [point] 落 [side] 的子是否直接成五(只在根节点少量调用,故允许复制棋盘) */
    private fun completesFive(state: BoardState, point: BoardPoint, side: Side): Boolean {
        val next = state.withStonePlaced(GomokuMove(to = point, side = side))
        return GameArbiter.hasFive(next, point, side)
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
