package com.wanbaohe.xiangqi.data.search

import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.model.Piece
import com.wanbaohe.xiangqi.domain.model.PieceType
import com.wanbaohe.xiangqi.domain.model.Side

/**
 * 搜索内部专用的攻击检测:只回答「将/帅所在格是否被 [bySide] 攻击」。
 *
 * 为什么不直接复用 [com.wanbaohe.xiangqi.domain.GameArbiter.isInCheck]:
 * 后者为了判定一处挨打会先生成对方的全部伪合法着法(90 格扫描 + 每子着法对象分配),
 * 搜索里每走一步都要判一次合法性,复用会把单节点成本放大两个数量级
 * (实测口径:浅层搜索每层要判上千次)。这里按攻击几何直接检测,一次只走棋盘上的几条线。
 *
 * 覆盖的规则与 [com.wanbaohe.xiangqi.domain.MoveGenerator] 的走子规则一一对应:
 * - 车:直线首个挡子
 * - 炮:直线隔一个炮架后的首个挡子
 * - 将/帅:相邻一格,以及同列无阻挡时的「飞将」照面
 * - 马:八向来源格,且马腿(马身边、朝目标方向的直邻格)为空
 * - 兵/卒:向前一格吃,以及过河后的左右横吃
 *
 * 士与象永远够不到对方九宫内的将帅(象不过河、士不出九宫),故不检测——这是有意省略,不是遗漏。
 */
internal object XiangqiAttacks {

    private val ORTHOGONAL = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)

    /** 相对将帅位置的八个马身来源格 */
    private val KNIGHT_ORIGINS = listOf(
        -1 to -2, 1 to -2, -1 to 2, 1 to 2,
        -2 to -1, -2 to 1, 2 to -1, 2 to 1,
    )

    /**
     * [king] 是否被 [bySide] 攻击。
     *
     * 将帅缺失(理论上只出现在搜索已经把对方将吃掉的分支里)一律视为被攻击,
     * 让上层直接按「已被将死」处理,避免出现将帅失踪却继续搜索的怪局面。
     */
    fun isKingAttacked(cells: List<Piece?>, king: BoardPoint, bySide: Side): Boolean {
        if (!king.isInside()) return true
        return byRookCannonOrKing(cells, king, bySide) ||
            byKnight(cells, king, bySide) ||
            byPawn(cells, king, bySide)
    }

    /** 车/炮/将帅:沿四条正交线各扫一趟,一趟同时解决车、炮与飞将 */
    private fun byRookCannonOrKing(cells: List<Piece?>, king: BoardPoint, bySide: Side): Boolean {
        for ((df, dr) in ORTHOGONAL) {
            var pos = king.offset(df, dr)
            var distance = 1
            var blocker: Piece? = null
            while (pos.isInside()) {
                val piece = cells[pos.index]
                if (piece != null) {
                    blocker = piece
                    break
                }
                pos = pos.offset(df, dr)
                distance++
            }
            val first = blocker ?: continue

            if (first.side == bySide) {
                // 同列首个挡子是将/帅 = 飞将照面;相邻一格则是将帅的正常吃子
                val flyingKing = first.type == PieceType.KING && df == 0
                if (first.type == PieceType.ROOK ||
                    (first.type == PieceType.KING && (flyingKing || distance == 1))
                ) {
                    return true
                }
            }

            // 越过炮架继续找炮:需要正好一个挡子,故从这里再找首个挡子即可
            var cannonPos = pos.offset(df, dr)
            while (cannonPos.isInside()) {
                val piece = cells[cannonPos.index]
                if (piece != null) {
                    if (piece.side == bySide && piece.type == PieceType.CANNON) return true
                    break
                }
                cannonPos = cannonPos.offset(df, dr)
            }
        }
        return false
    }

    private fun byKnight(cells: List<Piece?>, king: BoardPoint, bySide: Side): Boolean {
        for ((df, dr) in KNIGHT_ORIGINS) {
            val origin = king.offset(df, dr)
            if (!origin.isInside()) continue
            val piece = cells[origin.index] ?: continue
            if (piece.side != bySide || piece.type != PieceType.KNIGHT) continue

            // 马从 origin 走到 king:朝目标方向、跨度为 2 的那一维上的直邻格就是马腿
            val stepFile = -df
            val stepRank = -dr
            val leg = origin.offset(
                if (stepFile == 2 || stepFile == -2) stepFile / 2 else 0,
                if (stepRank == 2 || stepRank == -2) stepRank / 2 else 0,
            )
            if (cells[leg.index] == null) return true
        }
        return false
    }

    private fun byPawn(cells: List<Piece?>, king: BoardPoint, bySide: Side): Boolean {
        val crossedRiverRank = king.rank
        return if (bySide == Side.RED) {
            isPawn(cells, king.offset(0, 1), Side.RED) ||
                (crossedRiverRank <= 4 && (
                    isPawn(cells, king.offset(-1, 0), Side.RED) ||
                        isPawn(cells, king.offset(1, 0), Side.RED)
                    ))
        } else {
            isPawn(cells, king.offset(0, -1), Side.BLACK) ||
                (crossedRiverRank >= 5 && (
                    isPawn(cells, king.offset(-1, 0), Side.BLACK) ||
                        isPawn(cells, king.offset(1, 0), Side.BLACK)
                    ))
        }
    }

    private fun isPawn(cells: List<Piece?>, point: BoardPoint, side: Side): Boolean {
        if (!point.isInside()) return false
        val piece = cells[point.index] ?: return false
        return piece.side == side && piece.type == PieceType.PAWN
    }
}
