package com.wanbaohe.xiangqi.domain

import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.Piece
import com.wanbaohe.xiangqi.domain.model.PieceType
import com.wanbaohe.xiangqi.domain.model.Side
import com.wanbaohe.xiangqi.domain.model.XiangqiMove
import kotlin.math.abs

object UcciNotation {
    fun format(from: BoardPoint, to: BoardPoint): String = buildString {
        append(('a'.code + from.file).toChar())
        append(from.rank)
        append(('a'.code + to.file).toChar())
        append(to.rank)
    }
}

/**
 * 中文记谱（`车五进一` / `马七平六` / `前车进一`）。
 *
 * **必须传入着法发生前的 [BoardState]**：同一纵线上出现两个及以上同种棋子时，
 * 记谱规则要求用「前/后」（三子用「前/中/后」，四兵及以上用「一二三四五」）替代路数，
 * 否则 `车五进一` 这类记法有歧义、且无法回读。
 */
object ChineseNotationFormatter {

    fun format(move: XiangqiMove, board: BoardState): String {
        // 同线同名 ≥2 时用「前车/后车/中卒」，否则用路数「车五」
        val label = disambiguationLabel(move, board)
        val lead = label?.let { it + move.piece.cnName() }
            ?: (move.piece.cnName() + chineseNumeral(move.sourceFileNumber()))
        return if (move.from.file == move.to.file) {
            formatVertical(move, lead)
        } else {
            // 横走时"平"只说明走到哪一路，**不能**区分是哪一只子：
            // 双车同线都能平到同一路，所以同线同名 ≥2 时同样必须用 前/后。
            "${lead}平${chineseNumeral(move.targetFileNumber())}"
        }
    }

    /**
     * 同线同名子力 ≥2 时返回「前」「后」「中」或数字序；否则返回 null，由调用方回退到路数。
     */
    private fun disambiguationLabel(move: XiangqiMove, board: BoardState): String? {
        val sameFilePieces = piecesOnFile(board, move.piece.side, move.piece.type, move.from.file)
        if (sameFilePieces.size < 2) return null
        val ordered = orderFromFront(sameFilePieces, move.piece.side)
        val index = ordered.indexOfFirst { it == move.from }
        if (index < 0) return null

        return when {
            // 兵/卒同线 ≥4：用数字序（一=最前）
            move.piece.type == PieceType.PAWN && ordered.size >= 4 ->
                NUMERALS.getOrNull(index) ?: chineseNumeral(index + 1)
            ordered.size == 2 -> if (index == 0) "前" else "后"
            ordered.size == 3 -> when (index) {
                0 -> "前"
                1 -> "中"
                else -> "后"
            }
            // 其他子力同线 ≥4 在象棋中不合法（双车/双马/双炮最多两个），保守回退
            else -> null
        }
    }

    /** 取该纵线上属于 [side]、类型为 [type] 的全部棋子位置。 */
    private fun piecesOnFile(
        board: BoardState,
        side: Side,
        type: PieceType,
        file: Int,
    ): List<BoardPoint> = (0 until BoardPoint.RANK_COUNT)
        .map { BoardPoint(file, it) }
        .filter { point ->
            val piece: Piece? = board.pieceAt(point)
            piece?.side == side && piece.type == type
        }

    /**
     * 按"前 -> 后"排序。
     *
     * 前 = 更靠近对方底线，即**该方前进方向**上更靠前的位置。
     * 注意 rank 方向：rank 0 是黑方底线、rank 9 是红方底线（见 [FenCodec.INITIAL_FEN]），
     * 红方向 rank 递减方向前进、黑方相反，因此红方的"前"是 rank 更小者。
     */
    private fun orderFromFront(points: List<BoardPoint>, side: Side): List<BoardPoint> =
        if (side == Side.RED) points.sortedBy { it.rank } else points.sortedByDescending { it.rank }

    /**
     * 渲染纵走着法。[prefix] 已是「前车」「车五」这类完整的前缀（含子力名）。
     */
    private fun formatVertical(move: XiangqiMove, prefix: String): String {
        val action = if (move.isForward()) "进" else "退"
        val suffix = if (move.piece.type.needsTargetFileOnVerticalMove()) {
            // 马/象/仕走斜线时记目标路数，而非步数
            chineseNumeral(move.targetFileNumber())
        } else {
            chineseNumeral(abs(move.to.rank - move.from.rank))
        }
        return "$prefix$action$suffix"
    }

    private fun XiangqiMove.isForward(): Boolean = when (piece.side) {
        Side.RED -> to.rank < from.rank
        Side.BLACK -> to.rank > from.rank
    }

    private fun XiangqiMove.sourceFileNumber(): Int = piece.side.fileNumber(from.file)
    private fun XiangqiMove.targetFileNumber(): Int = piece.side.fileNumber(to.file)

    private fun Side.fileNumber(file: Int): Int = if (this == Side.RED) 9 - file else file + 1

    private fun PieceType.needsTargetFileOnVerticalMove(): Boolean =
        this == PieceType.KNIGHT || this == PieceType.BISHOP || this == PieceType.ADVISOR

    /** 1-9 -> 一..九。中文记谱不使用阿拉伯数字（`车5进1` 不合规范）。 */
    private fun chineseNumeral(value: Int): String =
        NUMERALS.getOrNull(value - 1) ?: value.toString()

    private val NUMERALS = listOf("一", "二", "三", "四", "五", "六", "七", "八", "九")

    private fun Piece.cnName(): String = when (side) {
        Side.RED -> when (type) {
            PieceType.KING -> "帅"
            PieceType.ADVISOR -> "仕"
            PieceType.BISHOP -> "相"
            PieceType.KNIGHT -> "马"
            PieceType.ROOK -> "车"
            PieceType.CANNON -> "炮"
            PieceType.PAWN -> "兵"
        }
        Side.BLACK -> when (type) {
            PieceType.KING -> "将"
            PieceType.ADVISOR -> "士"
            PieceType.BISHOP -> "象"
            PieceType.KNIGHT -> "马"
            PieceType.ROOK -> "车"
            PieceType.CANNON -> "炮"
            PieceType.PAWN -> "卒"
        }
    }
}
