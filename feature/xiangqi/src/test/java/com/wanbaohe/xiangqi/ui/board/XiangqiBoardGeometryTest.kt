package com.wanbaohe.xiangqi.ui.board

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class XiangqiBoardGeometryTest {
    @Test
    fun tallViewportsKeepTheTraditionalBoardRatio() {
        assertEquals(9f / 10f, xiangqiBoardAspectRatio(336f, 500f))
    }

    @Test
    fun compactViewportsFitTheBoardCloserToTheFullWidth() {
        assertEquals(1.05f, xiangqiBoardAspectRatio(336f, 320f))
    }

    @Test
    fun veryShortViewportsNeverFlattenTheBoardBeyondItsLimit() {
        assertEquals(1.25f, xiangqiBoardAspectRatio(336f, 80f))
        assertEquals(1.25f, xiangqiBoardAspectRatio(336f, 0f))
    }

    @Test
    fun piecesAndLastMoveRingsFitBothAxesOfCompactCells() {
        for ((horizontal, vertical) in listOf(34f to 24f, 24f to 34f, 30f to 30f)) {
            val diameter = xiangqiPieceDiameter(horizontal, vertical)
            assertTrue(diameter < minOf(horizontal, vertical))
            assertTrue(diameter * 1.12f < minOf(horizontal, vertical))
        }
    }
}
