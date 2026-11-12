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
