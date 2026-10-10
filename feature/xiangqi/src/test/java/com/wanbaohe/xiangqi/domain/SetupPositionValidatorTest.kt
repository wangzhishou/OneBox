package com.wanbaohe.xiangqi.domain

import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.Piece
import com.wanbaohe.xiangqi.domain.model.PieceType
import com.wanbaohe.xiangqi.domain.model.Side
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SetupPositionValidatorTest {
    private val base = FenCodec.parse("4k4/9/9/9/4p4/9/9/9/9/4K4 w 0 1")

    @Test
    fun standardPositionAndBlackFirstPositionArePlayable() {
        val board = FenCodec.parse(FenCodec.INITIAL_FEN)
        assertNull(SetupPositionValidator.validate(board))
        assertNull(SetupPositionValidator.validate(board.copy(sideToMove = Side.BLACK)))
    }

    @Test
    fun eachSideNeedsExactlyOneGeneral() {
        val missing = BoardSetupDraft(base).selectTool(SetupTool.ERASE).tap(BoardPoint(4, 9)).boardState
        assertEquals(SetupPositionIssue.KingCount(Side.RED), SetupPositionValidator.validate(missing))
        val duplicate = withPiece(base, BoardPoint(3, 9), Side.RED, PieceType.KING)
        assertEquals(SetupPositionIssue.KingCount(Side.RED), SetupPositionValidator.validate(duplicate))
    }

    @Test
    fun rejectsExcessPieces() {
        val board = (0..2).fold(base) { board, file ->
            withPiece(board, BoardPoint(file, 9), Side.RED, PieceType.ROOK)
        }
        assertEquals(SetupPositionIssue.TooManyPieces(Side.RED, PieceType.ROOK), SetupPositionValidator.validate(board))
    }

    @Test
    fun rejectsAnAdvisorOnAnUnreachablePalaceSquare() {
        val board = withPiece(base, BoardPoint(3, 8), Side.RED, PieceType.ADVISOR)
        assertEquals(SetupPositionIssue.InvalidSquare(Side.RED, PieceType.ADVISOR), SetupPositionValidator.validate(board))
    }

    @Test
    fun rejectsAnElephantOnTheWrongSquareOrAcrossTheRiver() {
        for (point in listOf(BoardPoint(2, 7), BoardPoint(4, 4))) {
            val board = withPiece(base, point, Side.RED, PieceType.BISHOP)
            assertEquals(SetupPositionIssue.InvalidSquare(Side.RED, PieceType.BISHOP), SetupPositionValidator.validate(board))
        }
    }

    @Test
    fun rejectsAnUncrossedPawnOnAnOddFile() {
        val board = withPiece(base, BoardPoint(1, 6), Side.RED, PieceType.PAWN)
        assertEquals(SetupPositionIssue.InvalidSquare(Side.RED, PieceType.PAWN), SetupPositionValidator.validate(board))
    }

    @Test
    fun rejectsFacingGenerals() {
        val board = BoardSetupDraft(base).selectTool(SetupTool.ERASE).tap(BoardPoint(4, 4)).boardState
        assertEquals(SetupPositionIssue.KingsFacing, SetupPositionValidator.validate(board))
    }

    @Test
    fun allowsCheckOnlyWhenTheCheckedSideIsToMove() {
        val board = withPiece(base, BoardPoint(4, 1), Side.RED, PieceType.ROOK)
        assertEquals(SetupPositionIssue.WrongTurn, SetupPositionValidator.validate(board))
        assertNull(SetupPositionValidator.validate(board.copy(sideToMove = Side.BLACK)))
    }

    @Test
    fun rejectsAStalematedSideInsteadOfStartingAnAlreadyFinishedGame() {
        val board = FenCodec.parse("4k4/3R1R3/9/9/4P4/9/9/9/9/4K4 b 0 1")
        assertEquals(SetupPositionIssue.NoLegalMoves, SetupPositionValidator.validate(board))
    }

    private fun withPiece(board: BoardState, point: BoardPoint, side: Side, type: PieceType): BoardState {
        val cells = board.board.toMutableList()
        cells[point.index] = Piece(side, type)
        return board.copy(board = cells)
    }
}
