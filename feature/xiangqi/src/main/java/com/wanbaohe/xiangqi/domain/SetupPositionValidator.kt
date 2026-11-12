/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.xiangqi.domain

import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.PieceType
import com.wanbaohe.xiangqi.domain.model.Side

sealed interface SetupPositionIssue {
    data class KingCount(val side: Side) : SetupPositionIssue
    data class TooManyPieces(val side: Side, val type: PieceType) : SetupPositionIssue
    data class InvalidSquare(val side: Side, val type: PieceType) : SetupPositionIssue
    data object KingsFacing : SetupPositionIssue
    data object WrongTurn : SetupPositionIssue
    data object NoLegalMoves : SetupPositionIssue
}

object SetupPositionValidator {
    fun maxCount(type: PieceType): Int = when (type) {
        PieceType.KING -> 1
        PieceType.PAWN -> 5
        else -> 2
    }

    fun validate(board: BoardState): SetupPositionIssue? {
        for (side in Side.entries) {
            val pieces = board.board.filterNotNull().filter { it.side == side }
            if (pieces.count { it.type == PieceType.KING } != 1) {
                return SetupPositionIssue.KingCount(side)
            }
            for (type in PieceType.entries) {
                if (pieces.count { it.type == type } > maxCount(type)) {
                    return SetupPositionIssue.TooManyPieces(side, type)
                }
            }
        }
        for (rank in 0 until BoardPoint.RANK_COUNT) {
            for (file in 0 until BoardPoint.FILE_COUNT) {
                val point = BoardPoint(file, rank)
                val piece = board.pieceAt(point) ?: continue
                if (!validSquare(point, piece.side, piece.type)) {
                    return SetupPositionIssue.InvalidSquare(piece.side, piece.type)
                }
            }
        }
        if (GameArbiter.kingsFacing(board)) return SetupPositionIssue.KingsFacing
        // The side that just moved cannot have left its own king in check.
        if (GameArbiter.isInCheck(board, board.sideToMove.opposite())) {
            return SetupPositionIssue.WrongTurn
        }
        if (GameArbiter.legalMoves(board).isEmpty()) return SetupPositionIssue.NoLegalMoves
        return null
    }

    private fun validSquare(point: BoardPoint, side: Side, type: PieceType): Boolean {
        val rank = if (side == Side.BLACK) point.rank else 9 - point.rank
        return when (type) {
            PieceType.KING -> point.file in 3..5 && rank in 0..2
            PieceType.ADVISOR -> (point.file to rank) in advisorSquares
            PieceType.BISHOP -> (point.file to rank) in bishopSquares
            PieceType.PAWN -> rank >= 3 && (rank >= 5 || point.file % 2 == 0)
            else -> true
        }
    }

    private val advisorSquares = setOf(3 to 0, 5 to 0, 4 to 1, 3 to 2, 5 to 2)
    private val bishopSquares = setOf(2 to 0, 6 to 0, 0 to 2, 4 to 2, 8 to 2, 2 to 4, 6 to 4)
}
