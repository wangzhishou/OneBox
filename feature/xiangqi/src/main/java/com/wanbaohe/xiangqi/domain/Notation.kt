package com.wanbaohe.xiangqi.domain

import com.wanbaohe.xiangqi.domain.model.BoardPoint
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

object ChineseNotationFormatter {

    fun format(move: XiangqiMove): String {
        val sourceFile = move.sourceFileNumber()
        val targetFile = move.targetFileNumber()
        return if (move.from.file == move.to.file) {
            formatVertical(move, sourceFile, targetFile)
        } else {
            "${move.piece.cnName()}${sourceFile}平${targetFile}"
        }
    }

    private fun formatVertical(move: XiangqiMove, sourceFile: Int, targetFile: Int): String {
        val action = if (move.isForward()) "进" else "退"
        val suffix = if (move.piece.type.needsTargetFileOnVerticalMove()) {
            targetFile.toString()
        } else {
            abs(move.to.rank - move.from.rank).toString()
        }
        return "${move.piece.cnName()}${sourceFile}${action}${suffix}"
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

    private fun com.wanbaohe.xiangqi.domain.model.Piece.cnName(): String = when (side) {
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
