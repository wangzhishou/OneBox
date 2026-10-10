package com.wanbaohe.gomoku.domain

import com.wanbaohe.gomoku.domain.model.Side
import org.junit.Test
import kotlin.test.assertEquals

class HumanAiHistoryTest {
    @Test
    fun undoWhileAiPendingGoesBackOneMoveAndAfterAiReplyGoesBackTwo() {
        val sides = listOf(Side.BLACK, Side.WHITE, Side.BLACK, Side.WHITE)
        assertEquals(1, HumanAiHistory.undoSteps(sides, 3, Side.BLACK))
        assertEquals(2, HumanAiHistory.undoSteps(sides, 4, Side.BLACK))
        assertEquals(0, HumanAiHistory.undoSteps(sides, 0, Side.BLACK))
    }

    @Test
    fun humanWhiteNeverUndoesTheAiOpeningBeforeTheirFirstDecision() {
        val sides = listOf(Side.BLACK, Side.WHITE, Side.BLACK)
        assertEquals(0, HumanAiHistory.undoSteps(sides, 1, Side.WHITE))
        assertEquals(1, HumanAiHistory.undoSteps(sides, 2, Side.WHITE))
        assertEquals(2, HumanAiHistory.undoSteps(sides, 3, Side.WHITE))
    }

    @Test
    fun redoIncludesTheResponseButSafelyHandlesAPendingFinalHumanMove() {
        assertEquals(2, HumanAiHistory.redoSteps(listOf(Side.BLACK, Side.WHITE), 0, Side.BLACK))
        assertEquals(1, HumanAiHistory.redoSteps(listOf(Side.BLACK), 0, Side.BLACK))
        assertEquals(0, HumanAiHistory.redoSteps(listOf(Side.BLACK), 1, Side.BLACK))
        assertEquals(1, HumanAiHistory.redoSteps(listOf(Side.WHITE, Side.BLACK), 1, Side.WHITE))
    }
}
