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
