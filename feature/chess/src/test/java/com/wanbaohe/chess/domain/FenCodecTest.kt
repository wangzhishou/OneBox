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
