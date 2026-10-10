package com.wanbaohe.chess.ui.board

import com.wanbaohe.chess.domain.model.BoardPoint
import com.wanbaohe.chess.domain.model.Side
import org.junit.Test
import kotlin.test.assertEquals

class ChessBoardCoordinateTest {
    @Test
    fun humanWhiteIsBelowAiBlackAtFirstEntry() {
        assertEquals(BoardPoint(0, 0), displayPointToBoardPoint(0, 7, Side.WHITE))
        assertEquals(BoardPoint(4, 0), displayPointToBoardPoint(4, 7, Side.WHITE))
        assertEquals(BoardPoint(4, 7), displayPointToBoardPoint(4, 0, Side.WHITE))
    }

    @Test
    fun selectingHumanBlackRotatesBothFilesAndRanks() {
        assertEquals(BoardPoint(7, 7), displayPointToBoardPoint(0, 7, Side.BLACK))
        assertEquals(BoardPoint(0, 0), displayPointToBoardPoint(7, 0, Side.BLACK))
    }
}
