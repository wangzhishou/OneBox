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

package com.wanbaohe.gomoku.domain.model

import com.wanbaohe.gomoku.domain.FenCodec

/**
 * Pure online-room rules shared by app use cases and IO adapters.
 * The server stores this object but the app remains the source of truth for FEN validation.
 * 五子棋黑先:房主默认执黑。
 */
data class OnlineRoomConfig(
    val initialFen: String = FenCodec.INITIAL_FEN,
    val hostSide: Side = Side.BLACK,
    val guestSide: Side = Side.WHITE,
    val allowUndo: Boolean = false,
) {
    init {
        FenCodec.parse(initialFen)
        require(hostSide != guestSide) { "Host and guest must sit on different sides" }
    }


    companion object {
        fun fromFen(fen: String): OnlineRoomConfig {
            val normalized = FenCodec.encode(FenCodec.parse(fen.ifBlank { FenCodec.INITIAL_FEN }))
            return OnlineRoomConfig(initialFen = normalized)
        }
    }
}
