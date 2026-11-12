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
