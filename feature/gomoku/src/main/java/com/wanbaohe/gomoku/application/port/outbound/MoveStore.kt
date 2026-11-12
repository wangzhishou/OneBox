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

package com.wanbaohe.gomoku.application.port.outbound

import com.wanbaohe.gomoku.domain.model.Side
import kotlinx.coroutines.flow.Flow

interface MoveStore {
    fun observeByGame(gameId: String): Flow<List<PlyEntity>>
    suspend fun getByGame(gameId: String): List<PlyEntity>
    suspend fun getPly(gameId: String, plyNumber: Int): PlyEntity?
    suspend fun insert(entity: PlyEntity)
    suspend fun deleteAfterPly(gameId: String, ply: Int)
    suspend fun deleteByGame(gameId: String)
}

data class PlyEntity(
    val id: String,
    val gameId: String,
    val ply: Int,
    val moveUcci: String,
    val moveCn: String,
    val moverSide: Side,
    val beforeFen: String,
    val afterFen: String,
    val isCapture: Boolean,
    val isCheck: Boolean,
    val isCheckmate: Boolean,
    val aiReason: String,
    val aiRawResponse: String,
    val thinkDurationMs: Long,
)
