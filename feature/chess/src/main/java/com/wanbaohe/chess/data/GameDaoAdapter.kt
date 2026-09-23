package com.wanbaohe.chess.data

import com.shifenmiao.database.chess.dao.ChessGameDao
import com.shifenmiao.database.chess.entity.ChessGameEntity
import com.wanbaohe.chess.application.port.outbound.GameEntity
import com.wanbaohe.chess.application.port.outbound.GameStore
import com.wanbaohe.chess.application.port.outbound.GameSummaryEntity
import com.wanbaohe.chess.domain.model.GameMode
import com.wanbaohe.chess.domain.model.GameStatus
import com.wanbaohe.chess.domain.model.PlayerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameDaoAdapter @Inject constructor(
    private val gameDao: ChessGameDao,
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

    private fun ChessGameEntity.toEntity() = GameEntity(
        id = id,
        title = title,
        mode = GameMode.valueOf(mode),
        // chess 沿用象棋共享列名:red_player_type 列存白方(Side.WHITE)
        whitePlayerType = PlayerType.valueOf(redPlayerType),
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
    )

    private fun ChessGameEntity.toSummaryEntity() = GameSummaryEntity(
        id = id,
        title = title,
        mode = GameMode.valueOf(mode),
        // chess 沿用象棋共享列名:red_player_type 列存白方(Side.WHITE)
        whitePlayerType = PlayerType.valueOf(redPlayerType),
        blackPlayerType = PlayerType.valueOf(blackPlayerType),
        status = GameStatus.valueOf(status),
        resultText = result,
        updatedAt = updatedAt,
        plyCount = currentPly,
    )

    private fun GameEntity.toDbEntity() = ChessGameEntity(
        id = id,
        title = title,
        mode = mode.name,
        redPlayerType = whitePlayerType.name,
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
    )
}
