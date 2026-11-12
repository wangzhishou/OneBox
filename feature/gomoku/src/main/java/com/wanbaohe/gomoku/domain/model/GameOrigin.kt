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

import com.shifenmiao.model.ModelProvider.AppJson
import com.wanbaohe.gomoku.domain.FenCodec
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

@Serializable
data class GameOrigin(
    val gameId: String,
    val ply: Int,
    val fen: String,
    val title: String,
) {
    init {
        require(gameId.isNotBlank())
        require(ply >= 0)
        require(fen.isNotBlank())
        FenCodec.parse(fen)
    }

    fun encode(): String = AppJson.encodeToString(this)

    companion object {
        fun decode(json: String): GameOrigin? =
            runCatching { AppJson.decodeFromString<GameOrigin>(json) }.getOrNull()
    }
}
