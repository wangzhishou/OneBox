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
