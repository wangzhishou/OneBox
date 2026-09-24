package com.wanbaohe.gomoku.service

import com.shifenmiao.model.gomoku.GomokuGameDetailDto
import com.shifenmiao.model.gomoku.GomokuGameSummaryDto
import com.shifenmiao.model.gomoku.GomokuImportResultDto
import com.shifenmiao.model.gomoku.GomokuMoveDto
import com.shifenmiao.model.gomoku.GomokuServiceInterface
import com.wanbaohe.gomoku.application.dto.GameDetail
import com.wanbaohe.gomoku.application.dto.GameSummary
import com.wanbaohe.gomoku.application.usecase.CreateGameUseCase
import com.wanbaohe.gomoku.application.usecase.DeleteGameUseCase
import com.wanbaohe.gomoku.application.usecase.ExportGameUseCase
import com.wanbaohe.gomoku.application.usecase.GameQueryUseCase
import com.wanbaohe.gomoku.application.usecase.ImportGameUseCase
import com.wanbaohe.gomoku.application.usecase.ImportResult
import com.wanbaohe.gomoku.domain.model.Side
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GomokuServiceImpl @Inject constructor(
    private val gameQuery: GameQueryUseCase,
    private val createGame: CreateGameUseCase,
    private val importGame: ImportGameUseCase,
    private val deleteGame: DeleteGameUseCase,
    private val exportGame: ExportGameUseCase,
) : GomokuServiceInterface {

    override suspend fun listGames(): List<GomokuGameSummaryDto> =
        gameQuery.observeAll().first().map { it.toDto() }

    override suspend fun getGameDetail(gameId: String): GomokuGameDetailDto? =
        gameQuery.getById(gameId)?.toDto()

    override suspend fun createLocalGame(title: String): Result<String> =
        runCatching { createGame.createLocal(title) }

    override suspend fun createAiGame(title: String, aiAsBlack: Boolean): Result<String> =
        runCatching {
            val aiSide = if (aiAsBlack) Side.BLACK else Side.WHITE
            createGame.createHumanVsAi(title, aiSide)
        }

    /** Agent 侧缺省标题：调用方未给标题时直接沿用，避免依赖 UI 资源。 */
    override suspend fun importFen(title: String, fen: String): Result<String> =
        runCatching { importGame.importFen(title, fen, title).requireGameId() }

    override suspend fun importJson(title: String, json: String): Result<GomokuImportResultDto> =
        runCatching {
            when (val result = importGame.importJson(title, json, title)) {
                is ImportResult.Success -> GomokuImportResultDto(
                    gameId = result.gameId,
                    importedPlies = result.importedPlies,
                    skippedPlies = result.skippedPlies.size,
                )
                is ImportResult.Failure ->
                    throw IllegalStateException(result.message.ifBlank { result.cause.name })
            }
        }

    override suspend fun deleteGame(gameId: String): Result<Unit> =
        runCatching { deleteGame.delete(gameId) }

    override suspend fun exportFen(gameId: String): String =
        exportGame.asFen(gameId)

    override suspend fun exportJson(gameId: String): String =
        exportGame.asJson(gameId)

    // ── DTO 转换 ─────────────────────────────────────

    private fun ImportResult.requireGameId(): String = when (this) {
        is ImportResult.Success -> gameId
        is ImportResult.Failure -> throw IllegalStateException(message.ifBlank { cause.name })
    }

    private fun GameSummary.toDto() = GomokuGameSummaryDto(
        id = id,
        title = title,
        mode = mode.name,
        status = status.name,
        resultText = resultText,
        updatedAt = updatedAt,
    )

    private fun GameDetail.toDto() = GomokuGameDetailDto(
        id = id,
        title = title,
        mode = mode.name,
        status = status.name,
        initialFen = initialFen,
        currentFen = currentFen,
        currentPly = currentPly,
        moves = plies.map { ply ->
            GomokuMoveDto(
                ply = ply.ply,
                moveUcci = ply.moveUcci,
                moveCn = ply.moveCn,
            )
        },
    )
}
