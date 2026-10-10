package com.wanbaohe.chess.application

import com.wanbaohe.chess.application.port.outbound.GameEntity
import com.wanbaohe.chess.application.port.outbound.GameStore
import com.wanbaohe.chess.application.port.outbound.GameSummaryEntity
import com.wanbaohe.chess.application.port.outbound.MoveStore
import com.wanbaohe.chess.application.port.outbound.PlyEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class MemoryGames(initial: GameEntity) : GameStore {
    val records = mutableMapOf(initial.id to initial)
    var beforeUpdate: suspend () -> Unit = {}
    var game: GameEntity
        get() = requireNotNull(records["game"])
        set(value) { records[value.id] = value }

    override fun observeAll(): Flow<List<GameSummaryEntity>> = flowOf(emptyList())
    override fun observeById(gameId: String): Flow<GameEntity?> = flowOf(records[gameId])
    override suspend fun getById(gameId: String): GameEntity? = records[gameId]
    override suspend fun insert(entity: GameEntity): String { records[entity.id] = entity; return entity.id }
    override suspend fun update(entity: GameEntity) { beforeUpdate(); records[entity.id] = entity }
    override suspend fun archive(gameId: String) { records.remove(gameId) }
}

internal class MemoryMoves : MoveStore {
    val plies = mutableListOf<PlyEntity>()
    var beforeInsert: suspend () -> Unit = {}
    override fun observeByGame(gameId: String): Flow<List<PlyEntity>> = flowOf(plies.filter { it.gameId == gameId })
    override suspend fun getByGame(gameId: String): List<PlyEntity> = plies.filter { it.gameId == gameId }
    override suspend fun getPly(gameId: String, plyNumber: Int): PlyEntity? =
        plies.find { it.gameId == gameId && it.ply == plyNumber }
    override suspend fun insert(entity: PlyEntity) { beforeInsert(); plies += entity }
    override suspend fun deleteAfterPly(gameId: String, ply: Int) { plies.removeAll { it.gameId == gameId && it.ply > ply } }
    override suspend fun deleteByGame(gameId: String) { plies.removeAll { it.gameId == gameId } }
}
