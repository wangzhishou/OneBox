package com.wanbaohe.gomoku.domain

import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.Side
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SetupPositionValidatorTest {
    @Test
    fun practiceUsesExplicitTurnWithoutHistoricalCountParity() {
        assertNull(SetupPositionValidator.validate(BoardState.empty(Side.WHITE)))
        val cells = MutableList<Side?>(225) { null }
        (0..14 step 2).forEach { cells[BoardPoint(it, 0).index] = Side.BLACK }
        for (side in Side.entries) assertNull(SetupPositionValidator.validate(BoardState(cells, side)))
    }

    @Test
    fun fiveAndOverlinesForEitherColorAreTerminalNotRenjuFouls() {
        for (side in Side.entries) {
            for (count in listOf(5, 6)) {
                val cells = MutableList<Side?>(225) { null }
                repeat(count) { cells[BoardPoint(it, it).index] = side }
                val board = BoardState(cells, side.opposite())
                assertEquals(SetupPositionIssue.AlreadyWon(side), SetupPositionValidator.validate(board))
                assertEquals(side, GameArbiter.detectWinner(board))
                assertTrue(GameArbiter.legalMoves(board).isEmpty())
            }
        }
    }

    @Test
    fun fullBoardCannotStartButStillExportsFen() {
        val board = BoardState(List(225) { Side.WHITE }, Side.BLACK)
        assertEquals(SetupPositionIssue.FullBoard, SetupPositionValidator.validate(board))
        assertEquals(board, FenCodec.parse(FenCodec.encode(board)))
    }

    @Test
    fun doubleThreatsAreAllowedInFreestylePractice() {
        val cells = MutableList<Side?>(225) { null }
        listOf(BoardPoint(6, 7), BoardPoint(7, 7), BoardPoint(8, 7), BoardPoint(7, 6), BoardPoint(7, 8))
            .forEach { cells[it.index] = Side.BLACK }
        assertNull(SetupPositionValidator.validate(BoardState(cells, Side.BLACK)))
    }

    @Test
    fun codecRejectsUnknownSideAndInvalidMoveNumbersRatherThanGuessingBlack() {
        assertFailsWith<IllegalArgumentException> { FenCodec.parse(FenCodec.INITIAL_FEN.replace(" b ", " x ")) }
        assertFailsWith<IllegalArgumentException> { FenCodec.parse(FenCodec.INITIAL_FEN.dropLast(1) + "0") }
        assertFailsWith<IllegalArgumentException> { FenCodec.parse(FenCodec.INITIAL_FEN.dropLast(1) + "abc") }
        assertFailsWith<IllegalArgumentException> { FenCodec.parse(FenCodec.INITIAL_FEN.replaceFirst("15/", "999999999999999999/")) }
        assertEquals(BoardState.empty(Side.WHITE), FenCodec.parse(FenCodec.INITIAL_FEN.replace(" b ", " w ")))
    }
}
