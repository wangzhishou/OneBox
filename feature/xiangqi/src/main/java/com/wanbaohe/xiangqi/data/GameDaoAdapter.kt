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

package com.wanbaohe.xiangqi.data

import com.shifenmiao.database.xiangqi.dao.XiangqiGameDao
import com.shifenmiao.database.xiangqi.entity.XiangqiGameEntity
import com.shifenmiao.model.ModelProvider.AppJson
import com.wanbaohe.xiangqi.application.port.outbound.GameEntity
import com.wanbaohe.xiangqi.application.port.outbound.GameStore
import com.wanbaohe.xiangqi.application.port.outbound.GameSummaryEntity
import com.wanbaohe.xiangqi.domain.model.GameMode
import com.wanbaohe.xiangqi.domain.model.GameOrigin
import com.wanbaohe.xiangqi.domain.model.GameStatus
import com.wanbaohe.xiangqi.domain.model.PlayerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameDaoAdapter @Inject constructor(
    private val gameDao: XiangqiGameDao,
) : GameStore {

    override fun observeAll(): Flow<List<GameSummaryEntity>> =
        gameDao.observeGames().map { list ->
            list.map { it.toSummaryEntity() }
                .filter { it.status != GameStatus.NOT_STARTED }
        }

    override fun observeById(gameId: String): Flow<GameEntity?> =
        gameDao.observeGame(gameId).map { it?.toEntity() }

    override suspend fun getById(gameId: String): GameEntity? =
        gameDao.getGame(gameId)?.toEntity()

    override suspend fun insert(entity: GameEntity): String {
        gameDao.upsert(entity.toDbEntity())
        return entity.id
    }

    override suspend fun update(entity: GameEntity) {
        gameDao.upsert(entity.toDbEntity())
    }

    override suspend fun archive(gameId: String) {
        gameDao.archive(gameId)
    }

    private fun XiangqiGameEntity.toEntity() = GameEntity(
        id = id,
        title = title,
        mode = GameMode.valueOf(mode),
        redPlayerType = PlayerType.valueOf(redPlayerType),
        blackPlayerType = PlayerType.valueOf(blackPlayerType),
        redPlayerConfigJson = redPlayerConfigJson,
        blackPlayerConfigJson = blackPlayerConfigJson,
        initialFen = initialFen,
        currentFen = currentFen,
        currentPly = currentPly,
        status = GameStatus.valueOf(status),
        resultText = result,
        winnerSide = winnerSide,
        startedAt = startedAt,
        lastMoveAt = lastMoveAt,
        lastPlayedAt = lastPlayedAt,
        updatedAt = updatedAt,
        origin = decodeOrigin(originJson),
    )

    private fun XiangqiGameEntity.toSummaryEntity() = GameSummaryEntity(
        id = id,
        title = title,
        mode = GameMode.valueOf(mode),
        redPlayerType = PlayerType.valueOf(redPlayerType),
        blackPlayerType = PlayerType.valueOf(blackPlayerType),
        status = GameStatus.valueOf(status),
        resultText = result,
        updatedAt = updatedAt,
        plyCount = currentPly,
        lastPlayedAt = lastPlayedAt,
        redPlayerConfigJson = redPlayerConfigJson,
        blackPlayerConfigJson = blackPlayerConfigJson,
        origin = decodeOrigin(originJson),
    )

    private fun GameEntity.toDbEntity() = XiangqiGameEntity(
        id = id,
        title = title,
        mode = mode.name,
        redPlayerType = redPlayerType.name,
        blackPlayerType = blackPlayerType.name,
        redPlayerConfigJson = redPlayerConfigJson,
        blackPlayerConfigJson = blackPlayerConfigJson,
        initialFen = initialFen,
        currentFen = currentFen,
        currentPly = currentPly,
        status = status.name,
        result = resultText,
        winnerSide = winnerSide,
        startedAt = startedAt,
        lastMoveAt = lastMoveAt,
        lastPlayedAt = lastPlayedAt,
        updatedAt = updatedAt,
        originJson = origin?.let { AppJson.encodeToString(it) } ?: "{}",
    )

    private fun decodeOrigin(json: String): GameOrigin? =
        if (json == "{}") null else AppJson.decodeFromString<GameOrigin>(json)
}
