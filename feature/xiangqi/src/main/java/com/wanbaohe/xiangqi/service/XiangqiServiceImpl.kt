package com.wanbaohe.xiangqi.service

import com.shifenmiao.model.xiangqi.XiangqiGameDetailDto
import com.shifenmiao.model.xiangqi.XiangqiGameSummaryDto
import com.shifenmiao.model.xiangqi.XiangqiMoveDto
import com.shifenmiao.model.xiangqi.XiangqiServiceInterface
import com.wanbaohe.xiangqi.application.dto.GameDetail
import com.wanbaohe.xiangqi.application.dto.GameSummary
import com.wanbaohe.xiangqi.application.usecase.CreateGameUseCase
import com.wanbaohe.xiangqi.application.usecase.DeleteGameUseCase
import com.wanbaohe.xiangqi.application.usecase.ExportGameUseCase
import com.wanbaohe.xiangqi.application.usecase.GameQueryUseCase
import com.wanbaohe.xiangqi.application.usecase.ImportGameUseCase
import com.wanbaohe.xiangqi.application.usecase.ImportResult
import com.wanbaohe.xiangqi.domain.model.Side
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XiangqiServiceImpl @Inject constructor(
    private val gameQuery: GameQueryUseCase,
    private val createGame: CreateGameUseCase,
    private val importGame: ImportGameUseCase,
    private val deleteGame: DeleteGameUseCase,
    private val exportGame: ExportGameUseCase,
) : XiangqiServiceInterface {

    override suspend fun listGames(): List<XiangqiGameSummaryDto> =
        gameQuery.observeAll().first().map { it.toDto() }

    override suspend fun getGameDetail(gameId: String): XiangqiGameDetailDto? =
        gameQuery.getById(gameId)?.toDto()

    override suspend fun createLocalGame(title: String): Result<String> =
        runCatching { createGame.createLocal(title) }

    override suspend fun createAiGame(title: String, aiAsRed: Boolean): Result<String> =
        runCatching {
            val aiSide = if (aiAsRed) Side.RED else Side.BLACK
            createGame.createHumanVsAi(title, aiSide)
        }

    /** Agent 侧缺省标题：调用方未给标题时直接沿用，避免依赖 UI 资源。 */
    override suspend fun importFen(title: String, fen: String): Result<String> =
        runCatching { importGame.importFen(title, fen, title).requireGameId() }

    override suspend fun importFenAsAiGame(title: String, fen: String, aiAsRed: Boolean?): Result<String> =
        runCatching {
            val aiSide = aiAsRed?.let { if (it) Side.RED else Side.BLACK }
            importGame.importFenAsAiGame(title, fen, title, aiSide).requireGameId()
        }

    override suspend fun importJson(title: String, json: String): Result<String> =
        runCatching { importGame.importJson(title, json, title).requireGameId() }

    override suspend fun deleteGame(gameId: String): Result<Unit> =
        runCatching { deleteGame.delete(gameId) }

    override suspend fun exportFen(gameId: String): String =
        exportGame.asFen(gameId)

    override suspend fun exportJson(gameId: String): String =
        exportGame.asJson(gameId)

    // ── DTO 转换 ─────────────────────────────────────

    /**
     * Service 契约（[XiangqiServiceInterface]）只回 gameId 或抛异常，
     * 这里把结构化导入结果收敛回该契约，失败原因原样带出给 Agent 展示。
     */
    private fun ImportResult.requireGameId(): String = when (this) {
        is ImportResult.Success -> gameId
        is ImportResult.Failure -> throw IllegalStateException(message.ifBlank { cause.name })
    }

    private fun GameSummary.toDto() = XiangqiGameSummaryDto(
        id = id,
        title = title,
        mode = mode.name,
        status = status.name,
        resultText = resultText,
        updatedAt = updatedAt,
    )

    private fun GameDetail.toDto() = XiangqiGameDetailDto(
        id = id,
        title = title,
        mode = mode.name,
        status = status.name,
        initialFen = initialFen,
        currentFen = currentFen,
        currentPly = currentPly,
        moves = plies.map { ply ->
            XiangqiMoveDto(
                ply = ply.ply,
                moveUcci = ply.moveUcci,
                moveCn = ply.moveCn,
            )
        },
    )
}
