package com.wanbaohe.chess.domain

import com.wanbaohe.chess.domain.model.BoardPoint
import com.wanbaohe.chess.domain.model.Side
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class FenCodecTest {
    @Test
    fun standardFenPreservesAllSixFields() {
        val fen = "r3k2r/8/8/3pP3/8/8/8/R3K2R w KQkq d6 0 24"
        val board = FenCodec.parse(fen)
        assertEquals(fen, FenCodec.encode(board))
        assertEquals(BoardPoint(3, 5), board.enPassant)
        assertEquals(24, board.fullMoveNumber)
        assertEquals(Side.WHITE, FenCodec.parse(FenCodec.INITIAL_FEN).sideToMove)
    }

    @Test
    fun legacyTwoFieldPositionsHaveNoInventedCastlingRights() {
        val board = FenCodec.parse("4k3/8/8/8/8/8/4P3/4K3 w")
        assertEquals("-", board.castlingRights)
        assertNull(board.enPassant)
    }

    @Test
    fun malformedMetadataIsRejectedInsteadOfSilentlyDefaulted() {
        val board = "4k3/8/8/8/8/8/4P3/4K3"
        listOf(
            "$board x - - 0 1", "$board w KK - 0 1", "$board w Z - 0 1",
            "$board w - z6 0 1", "$board w - d3 0 1",
            "$board w - - nope 1", "$board w - - -1 1", "$board w - - 0 0",
            "$board w - - 0", "$board w - - 0 1 extra",
            "4k3/8/8/8/8/8/4P3/04K3 w - - 0 1",
        ).forEach { fen -> assertFailsWith<IllegalArgumentException>(fen) { FenCodec.parse(fen) } }
    }

    @Test
    fun capturingAHomeRookRemovesTheOtherSidesCastlingRight() {
        val board = FenCodec.parse("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1")
        val capture = GameArbiter.legalMoves(board).first { it.from == BoardPoint(0, 0) && it.to == BoardPoint(0, 7) }
        assertEquals("Kk", board.withPieceMoved(capture).castlingRights)
    }
}
