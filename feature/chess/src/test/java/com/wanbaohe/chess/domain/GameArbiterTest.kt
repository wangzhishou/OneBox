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

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameArbiterTest {
    @Test
    fun bareKingsAndOneMinorPieceCannotProduceMate() {
        listOf(
            "4k3/8/8/8/8/8/8/4K3 w - - 0 1",
            "4k3/8/8/8/8/8/2B5/4K3 w - - 0 1",
            "4k3/8/8/8/8/8/2N5/4K3 w - - 0 1",
            "4k3/8/8/8/4b3/8/2B5/4K3 w - - 0 1",
        ).forEach { assertTrue(GameArbiter.isInsufficientMaterial(FenCodec.parse(it)), it) }
    }

    @Test
    fun promotionsAndHelpMateMaterialAreNotMistakenForDeadPositions() {
        listOf(
            "4k3/8/8/8/8/8/4P3/4K3 w - - 0 1",
            "4k3/8/8/8/8/8/2BB4/4K3 w - - 0 1",
            "4k3/8/8/8/8/8/2NN4/4K3 w - - 0 1",
            "4k3/8/8/8/8/4n3/2B5/4K3 w - - 0 1",
        ).forEach { assertFalse(GameArbiter.isInsufficientMaterial(FenCodec.parse(it)), it) }
    }
}
