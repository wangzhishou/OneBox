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

package com.wanbaohe.chess.application.usecase

import com.wanbaohe.chess.application.dto.GameDetail
import com.wanbaohe.chess.domain.model.BoardPoint
import javax.inject.Inject
import javax.inject.Singleton

sealed interface OnlineGameEvent {
    val roomId: String

    data class Move(
        override val roomId: String,
        val from: BoardPoint,
        val to: BoardPoint,
    ) : OnlineGameEvent

    data class Started(override val roomId: String) : OnlineGameEvent
    data class Resigned(override val roomId: String) : OnlineGameEvent
}

@Singleton
class OnlineGameSyncUseCase @Inject constructor(
    private val playMove: PlayMoveUseCase,
    private val manageGame: ManageGameUseCase,
) {
    suspend fun accept(gameId: String, event: OnlineGameEvent): GameDetail? = when (event) {
        is OnlineGameEvent.Move -> when (
            val result = playMove.commitOnline(gameId, event.roomId, event.from, event.to)
        ) {
            is PlayMoveUseCase.Result.Success -> result.detail
            is PlayMoveUseCase.Result.Rejected -> null
        }
        is OnlineGameEvent.Started -> manageGame.startOnline(gameId, event.roomId)
        is OnlineGameEvent.Resigned -> manageGame.resignOnline(gameId, event.roomId)
    }
}
