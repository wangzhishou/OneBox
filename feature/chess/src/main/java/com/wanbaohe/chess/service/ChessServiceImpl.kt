package com.wanbaohe.chess.service

import com.shifenmiao.model.chess.ChessGameDetailDto
import com.shifenmiao.model.chess.ChessGameSummaryDto
import com.shifenmiao.model.chess.ChessImportResultDto
import com.shifenmiao.model.chess.ChessMoveDto
import com.shifenmiao.model.chess.ChessServiceInterface
import com.wanbaohe.chess.application.dto.GameDetail
import com.wanbaohe.chess.application.dto.GameSummary
import com.wanbaohe.chess.application.usecase.CreateGameUseCase
import com.wanbaohe.chess.application.usecase.DeleteGameUseCase
import com.wanbaohe.chess.application.usecase.ExportGameUseCase
import com.wanbaohe.chess.application.usecase.GameQueryUseCase
import com.wanbaohe.chess.application.usecase.ImportGameUseCase
import com.wanbaohe.chess.application.usecase.ImportResult
import com.wanbaohe.chess.domain.model.Side
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChessServiceImpl @Inject constructor(
    private val gameQuery: GameQueryUseCase,
    private val createGame: CreateGameUseCase,
    private val importGame: ImportGameUseCase,
    private val deleteGame: DeleteGameUseCase,
    private val exportGame: ExportGameUseCase,
) : ChessServiceInterface {

    override suspend fun listGames(): List<ChessGameSummaryDto> =
        gameQuery.observeAll().first().map { it.toDto() }

    override suspend fun getGameDetail(gameId: String): ChessGameDetailDto? =
        gameQuery.getById(gameId)?.toDto()

    override suspend fun createLocalGame(title: String): Result<String> =
        runCatching { createGame.createLocal(title) }

    override suspend fun createAiGame(title: String, aiAsWhite: Boolean): Result<String> =
        runCatching {
            val aiSide = if (aiAsWhite) Side.WHITE else Side.BLACK
            createGame.createHumanVsAi(title, aiSide)
        }

    /** Agent 侧缺省标题：调用方未给标题时直接沿用，避免依赖 UI 资源。 */
    override suspend fun importFen(title: String, fen: String): Result<String> =
        runCatching { importGame.importFen(title, fen, title).requireGameId() }

    override suspend fun importFenAsAiGame(title: String, fen: String, aiAsWhite: Boolean?): Result<String> =
        runCatching {
            val aiSide = aiAsWhite?.let { if (it) Side.WHITE else Side.BLACK }
            importGame.importFenAsAiGame(title, fen, title, aiSide).requireGameId()
        }

    override suspend fun importJson(title: String, json: String): Result<ChessImportResultDto> =
        runCatching {
            when (val result = importGame.importJson(title, json, title)) {
                is ImportResult.Success -> ChessImportResultDto(
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

    private fun GameSummary.toDto() = ChessGameSummaryDto(
        id = id,
        title = title,
        mode = mode.name,
        status = status.name,
        resultText = resultText,
        updatedAt = updatedAt,
    )

    private fun GameDetail.toDto() = ChessGameDetailDto(
        id = id,
        title = title,
        mode = mode.name,
        status = status.name,
        initialFen = initialFen,
        currentFen = currentFen,
        currentPly = currentPly,
        moves = plies.map { ply ->
            ChessMoveDto(
                ply = ply.ply,
                moveUcci = ply.moveUcci,
                moveCn = ply.moveCn,
            )
        },
    )
}
