package com.wanbaohe.xiangqi.domain

import com.wanbaohe.xiangqi.domain.model.Side
import org.junit.Test
import kotlin.test.assertEquals

class HumanAiHistoryTest {
    @Test
    fun undoBeforeAiResponseRemovesOnlyTheHumanMove() {
        assertEquals(1, HumanAiHistory.undoSteps(listOf(Side.RED), 1, Side.RED))
    }

    @Test
    fun undoAfterAiResponseReturnsToTheHumanDecision() {
        assertEquals(2, HumanAiHistory.undoSteps(listOf(Side.RED, Side.BLACK), 2, Side.RED))
    }

    @Test
    fun redoDoesNotTreatUndoneFutureMovesAsTheCurrentPosition() {
        val history = listOf(Side.RED, Side.BLACK, Side.RED, Side.BLACK)
        assertEquals(1, HumanAiHistory.undoSteps(history, 3, Side.RED))
        assertEquals(2, HumanAiHistory.redoSteps(history, 2, Side.RED))
    }

    @Test
    fun humanPlayingBlackCannotUndoAnAiOpeningThatTheyHaveNotAnswered() {
        assertEquals(0, HumanAiHistory.undoSteps(listOf(Side.RED), 1, Side.BLACK))
        assertEquals(2, HumanAiHistory.undoSteps(listOf(Side.RED, Side.BLACK, Side.RED), 3, Side.BLACK))
    }

    @Test
    fun redoHandlesAPendingAiResponseAndBlackFirstCustomPositions() {
        assertEquals(1, HumanAiHistory.redoSteps(listOf(Side.RED), 0, Side.RED))
        assertEquals(2, HumanAiHistory.redoSteps(listOf(Side.BLACK, Side.RED), 0, Side.BLACK))
        assertEquals(0, HumanAiHistory.redoSteps(emptyList(), 0, Side.RED))
    }
}
