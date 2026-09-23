package com.wanbaohe.chess.data

import com.shifenmiao.database.chess.dao.ChessPlyDao
import com.shifenmiao.database.chess.entity.ChessPlyEntity
import com.wanbaohe.chess.application.port.outbound.MoveStore
import com.wanbaohe.chess.application.port.outbound.PlyEntity
import com.wanbaohe.chess.domain.model.Side
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlyDaoAdapter @Inject constructor(
    private val plyDao: ChessPlyDao,
) : MoveStore {

    override fun observeByGame(gameId: String): Flow<List<PlyEntity>> =
        plyDao.observePlies(gameId).map { list -> list.map { it.toEntity() } }

    override suspend fun getByGame(gameId: String): List<PlyEntity> =
        plyDao.getPlies(gameId).map { it.toEntity() }

    override suspend fun getPly(gameId: String, plyNumber: Int): PlyEntity? =
        plyDao.getPly(gameId, plyNumber)?.toEntity()

    override suspend fun insert(entity: PlyEntity) {
        plyDao.upsert(entity.toDbEntity())
    }

    override suspend fun deleteAfterPly(gameId: String, ply: Int) {
        plyDao.deleteAfterPly(gameId, ply)
    }

    override suspend fun deleteByGame(gameId: String) {
        plyDao.deleteByGameId(gameId)
    }

    private fun ChessPlyEntity.toEntity() = PlyEntity(
        id = id,
        gameId = gameId,
        ply = ply,
        moveUcci = moveUcci,
        moveCn = moveCn,
        moverSide = Side.valueOf(moverSide),
        beforeFen = beforeFen,
        afterFen = afterFen,
        isCapture = isCapture,
        isCheck = isCheck,
        isCheckmate = isCheckmate,
        aiReason = aiReason,
        aiRawResponse = aiRawResponse,
        thinkDurationMs = thinkDurationMs,
    )

    private fun PlyEntity.toDbEntity() = ChessPlyEntity(
        id = id,
        gameId = gameId,
        ply = ply,
        moveUcci = moveUcci,
        moveCn = moveCn,
        moverSide = moverSide.name,
        beforeFen = beforeFen,
        afterFen = afterFen,
        isCapture = isCapture,
        isCheck = isCheck,
        isCheckmate = isCheckmate,
        aiReason = aiReason,
        aiRawResponse = aiRawResponse,
        thinkDurationMs = thinkDurationMs,
    )
}
