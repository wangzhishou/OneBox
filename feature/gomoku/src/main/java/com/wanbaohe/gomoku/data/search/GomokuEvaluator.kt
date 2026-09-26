package com.wanbaohe.gomoku.data.search

import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.GomokuMove
import com.wanbaohe.gomoku.domain.model.Side

/**
 * 五子棋搜索用的局面评估与候选着法生成。
 *
 * 两套评价并存,各司其职:
 * - [evaluate] 是「五连窗口计数」:枚举全场所有长度 5 的窗口,窗口内只有一方棋子才计分,
 *   按子数分段加权。它同时覆盖连子和跳子(如 X.XX),整盘只需扫一遍约 570 个窗口,够便宜,
 *   用作搜索叶节点的局面分。
 * - [placementScore] 是「棋型匹配」:把落子点当作窗口中心,取 11 格线段做棋型匹配
 *   (成五/活四/冲四/活三/眠三/跳三…)。它比分段计分锐利得多,用来给着法排序、
 *   挑候选点,并直接识别「活四」这类必胜形。
 *
 * 候选点只取已有棋子附近(切比雪夫距离 ≤ 2)的空位:五子棋的每一步都必须与现有棋子发生
 * 联系,离得远的空位纯属浪费搜索宽度——这是让浅层搜索在手机上可用的前提。
 */
internal object GomokuEvaluator {

    /** 成五的分数,搜索层据此判定胜负 */
    const val WIN_SCORE = 10_000_000

    private val DIRECTIONS = listOf(1 to 0, 0 to 1, 1 to 1, 1 to -1)

    private const val CANDIDATE_NEIGHBOURHOOD = 2
    private const val WINDOW_HALF_SPAN = 5

    /** 全场所有「五连窗口」的格子下标,按 15×15×4 方向预计算一次 */
    private val WINDOWS: Array<IntArray> = buildList {
        val fileCount = BoardPoint.FILE_COUNT
        val rankCount = BoardPoint.RANK_COUNT
        for (rank in 0 until rankCount) {
            for (file in 0 until fileCount) {
                for ((df, dr) in DIRECTIONS) {
                    val endFile = file + df * 4
                    val endRank = rank + dr * 4
                    if (endFile !in 0 until fileCount || endRank !in 0 until rankCount) continue
                    add(IntArray(5) { step -> BoardPoint(file + df * step, rank + dr * step).index })
                }
            }
        }
    }.toTypedArray()

    /** 下标 = 窗口内某一方的子数 */
    private val WINDOW_SCORE = intArrayOf(0, 1, 12, 120, 1200, 200_000)

    /**
     * 棋型分数表,按分值降序:先匹配到的即命中(整行 contains 判定)。
     * 'X' = 待评估方, '.' = 空,'B' = 阻挡(对方子或出界,两者对己方威胁等价)。
     */
    private val PATTERN_SCORES: List<Pair<String, Int>> = listOf(
        "XXXXX" to 1_000_000,
        ".XXXX." to 100_000,
        "XXXX." to 20_000,
        ".XXXX" to 20_000,
        "XXX.X" to 20_000,
        "X.XXX" to 20_000,
        "XX.XX" to 20_000,
        ".XXX." to 10_000,
        ".X.XX." to 8_000,
        ".XX.X." to 8_000,
        "XXX.." to 1_000,
        "..XXX" to 1_000,
        ".XX.X" to 1_000,
        "X.XX." to 1_000,
        "XX.X." to 1_000,
        ".X.XX" to 1_000,
        "..XX.." to 300,
        ".XX." to 100,
        ".X.X." to 80,
    )

    /** 以 [sideToMove] 为视角的局面分(negamax 约定:越大越有利于行棋方) */
    fun evaluate(cells: List<Side?>, sideToMove: Side): Int {
        var black = 0
        var white = 0
        for (window in WINDOWS) {
            var mine = 0
            var theirs = 0
            for (index in window) {
                when (cells[index]) {
                    Side.BLACK -> mine++
                    Side.WHITE -> theirs++
                    null -> Unit
                }
            }
            // 双方都占的窗口谁都不算:这种窗口永远是死窗口
            if (mine > 0 && theirs > 0) continue
            if (mine > 0) black += WINDOW_SCORE[mine]
            if (theirs > 0) white += WINDOW_SCORE[theirs]
        }
        return if (sideToMove == Side.BLACK) black - white else white - black
    }

    /**
     * 生成 [limit] 个候选点,按「进攻分 + 防守分」降序。
     * 进攻分 = 我方落在该点的棋型增量;防守分 = 对方落在该点的棋型增量(即堵点价值)。
     */
    fun candidates(cells: List<Side?>, side: Side, limit: Int): List<GomokuMove> {
        val opponent = side.opposite()
        if (cells.none { it != null }) {
            // 空盘:占天元
            val center = BoardPoint(BoardPoint.FILE_COUNT / 2, BoardPoint.RANK_COUNT / 2)
            return listOf(GomokuMove(center, side))
        }

        val scored = ArrayList<Pair<Int, BoardPoint>>(limit * 2)
        for (rank in 0 until BoardPoint.RANK_COUNT) {
            for (file in 0 until BoardPoint.FILE_COUNT) {
                val point = BoardPoint(file, rank)
                if (cells[point.index] != null) continue
                if (!hasNeighbour(cells, point)) continue
                val attack = placementScore(cells, point, side)
                val defence = placementScore(cells, point, opponent)
                scored += (attack + defence) to point
            }
        }
        if (scored.isEmpty()) return emptyList()
        return scored
            .sortedByDescending { it.first }
            .take(limit)
            .map { GomokuMove(it.second, side) }
    }

    /**
     * 落子 [point] 对 [side] 的棋型增量:四个方向的「落子后棋型分 - 落子前棋型分」之和。
     *
     * 做减法而不是直接取落子后的分数,是为了把线段内其它位置已有的棋型排除掉,
     * 只衡量这一手本身的价值。
     */
    fun placementScore(cells: List<Side?>, point: BoardPoint, side: Side): Int {
        var total = 0
        for ((df, dr) in DIRECTIONS) {
            val after = line(cells, point, df, dr, side, placeHere = true)
            val before = line(cells, point, df, dr, side, placeHere = false)
            total += lineScore(after) - lineScore(before)
        }
        return total
    }

    /** 取以 [point] 为中心、长度 11 的线段;出界记为阻挡 */
    private fun line(
        cells: List<Side?>,
        point: BoardPoint,
        df: Int,
        dr: Int,
        side: Side,
        placeHere: Boolean,
    ): String {
        val builder = StringBuilder(WINDOW_HALF_SPAN * 2 + 1)
        for (step in -WINDOW_HALF_SPAN..WINDOW_HALF_SPAN) {
            val file = point.file + df * step
            val rank = point.rank + dr * step
            val inside = file in 0 until BoardPoint.FILE_COUNT && rank in 0 until BoardPoint.RANK_COUNT
            if (!inside) {
                // 出界与「被对方挡住」对己方棋型是等价的,都记作阻挡
                builder.append('B')
                continue
            }
            val cell = if (step == 0 && placeHere) side else cells[BoardPoint(file, rank).index]
            builder.append(
                when {
                    // 空格必须记作 '.',否则所有含空位的棋型(活四/活三/跳三)永远匹配不上
                    cell == null -> '.'
                    cell == side -> 'X'
                    else -> 'B'
                }
            )
        }
        return builder.toString()
    }

    private fun lineScore(line: String): Int {
        for ((pattern, score) in PATTERN_SCORES) {
            if (line.contains(pattern)) return score
        }
        return 0
    }

    /** 点周围 [CANDIDATE_NEIGHBOURHOOD] 格内是否有子 */
    private fun hasNeighbour(cells: List<Side?>, point: BoardPoint): Boolean {
        for (dr in -CANDIDATE_NEIGHBOURHOOD..CANDIDATE_NEIGHBOURHOOD) {
            for (df in -CANDIDATE_NEIGHBOURHOOD..CANDIDATE_NEIGHBOURHOOD) {
                if (df == 0 && dr == 0) continue
                val file = point.file + df
                val rank = point.rank + dr
                if (file !in 0 until BoardPoint.FILE_COUNT || rank !in 0 until BoardPoint.RANK_COUNT) continue
                if (cells[BoardPoint(file, rank).index] != null) return true
            }
        }
        return false
    }
}
