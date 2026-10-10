package com.wanbaohe.chess.domain

import com.wanbaohe.chess.domain.model.Side
import org.junit.Test
import kotlin.test.assertEquals

class HumanAiHistoryTest {
    @Test
    fun undoReturnsToThePreviousHumanDecisionEvenWhileAiIsThinking() {
        val sides = listOf(Side.WHITE, Side.BLACK, Side.WHITE)
        assertEquals(1, HumanAiHistory.undoSteps(sides, 3, Side.WHITE))
        assertEquals(2, HumanAiHistory.undoSteps(sides, 2, Side.WHITE))
    }

    @Test
    fun blackFirstAiMoveIsNotUndoneWithoutAHumanDecision() {
        val sides = listOf(Side.BLACK, Side.WHITE, Side.BLACK)
        assertEquals(0, HumanAiHistory.undoSteps(sides, 1, Side.WHITE))
        assertEquals(2, HumanAiHistory.undoSteps(sides, 3, Side.WHITE))
        assertEquals(2, HumanAiHistory.redoSteps(sides, 1, Side.WHITE))
    }

    @Test
    fun redoPairsTheHumanMoveWithItsRecordedAiResponse() {
        val sides = listOf(Side.WHITE, Side.BLACK, Side.WHITE)
        assertEquals(2, HumanAiHistory.redoSteps(sides, 0, Side.WHITE))
        assertEquals(1, HumanAiHistory.redoSteps(sides, 2, Side.WHITE))
        assertEquals(0, HumanAiHistory.redoSteps(sides, 3, Side.WHITE))
    }
}
