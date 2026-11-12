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

package com.wanbaohe.chess.data

import com.shifenmiao.database.chess.dao.ChessAiTaskDao
import com.shifenmiao.database.chess.entity.ChessAiTaskEntity
import com.wanbaohe.chess.application.port.outbound.AiTaskEntity
import com.wanbaohe.chess.application.port.outbound.AiTaskStatus
import com.wanbaohe.chess.application.port.outbound.AiTaskStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiTaskDaoAdapter @Inject constructor(
    private val aiTaskDao: ChessAiTaskDao,
) : AiTaskStore {

    override suspend fun getLatestByGame(gameId: String): AiTaskEntity? =
        aiTaskDao.getLatestTask(gameId)?.takeUnless { it.status == "DONE" }?.toEntity()

    override suspend fun upsert(entity: AiTaskEntity) {
        aiTaskDao.upsert(entity.toDbEntity())
    }

    override suspend fun deleteByGame(gameId: String) {
        aiTaskDao.deleteByGameId(gameId)
    }

    private fun ChessAiTaskEntity.toEntity() = AiTaskEntity(
        id = id,
        gameId = gameId,
        targetPly = targetPly,
        requestJson = requestJson,
        status = AiTaskStatus.valueOf(status),
        validatedMove = validatedMove,
        responseJson = responseJson,
        errorMessage = errorMessage,
    )

    private fun AiTaskEntity.toDbEntity() = ChessAiTaskEntity(
        id = id,
        gameId = gameId,
        targetPly = targetPly,
        requestJson = requestJson,
        status = status.name,
        validatedMove = validatedMove,
        responseJson = responseJson,
        errorMessage = errorMessage,
    )
}
