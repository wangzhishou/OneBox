package com.wanbaohe.gomoku.application.usecase

import com.shifenmiao.model.ModelProvider.AppJson
import com.wanbaohe.gomoku.application.dto.GameAiPlayerConfig
import com.wanbaohe.gomoku.application.dto.GamePreparation
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.GameArbiter
import com.wanbaohe.gomoku.domain.GameResultCode
import com.wanbaohe.gomoku.domain.GameResultResolver
import com.wanbaohe.gomoku.domain.model.GameMode
import com.wanbaohe.gomoku.domain.model.GameOrigin
import com.wanbaohe.gomoku.domain.model.GameSetup
import com.wanbaohe.gomoku.domain.model.GameStatus
import com.wanbaohe.gomoku.domain.model.PlayerSeat
import com.wanbaohe.gomoku.domain.model.PlayerType
import com.wanbaohe.gomoku.domain.model.Side
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 导入结果。
 *
 * 导入是**部分失败也要如实汇报**的操作：非法着法会被跳过，调用方必须把跳过的手数与原因告诉用户，
 * 而不是静默产出一个残缺棋谱（历史行为）。
 */
sealed interface ImportResult {

    data class Success(
        val gameId: String,
        val importedPlies: Int,
        val skippedPlies: List<SkippedPly>,
    ) : ImportResult {
        val hasSkipped: Boolean get() = skippedPlies.isNotEmpty()
    }

    /** 致命失败：没有产生任何可用对局。[message] 为底层原因，仅供日志。 */
    data class Failure(
        val cause: ImportFailureCause,
        val message: String,
    ) : ImportResult
}

enum class ImportFailureCause { INVALID_FEN, INVALID_JSON }

/** 被跳过的着法，[ply] 为它在原始棋谱中的序号（从 1 开始）。[moveUcci] 可能为空串。 */
data class SkippedPly(val ply: Int, val moveUcci: String)

@Singleton
class ImportGameUseCase internal constructor(
    private val createImportedGame: suspend (GamePreparation) -> String,
    private val playMove: PlayMoveUseCase,
    private val manageGame: ManageGameUseCase,
    private val deleteGame: DeleteGameUseCase,
) {
    @Inject
    constructor(
        createGame: CreateGameUseCase,
        playMove: PlayMoveUseCase,
        manageGame: ManageGameUseCase,
        deleteGame: DeleteGameUseCase,
    ) : this(
        createImportedGame = { game ->
            createGame.create(
                title = game.title,
                setup = game.setup,
                initialFen = game.initialFen,
                redPlayerConfigJson = game.whiteAiConfig?.encode() ?: "{}",
                blackPlayerConfigJson = game.blackAiConfig?.encode() ?: "{}",
                origin = game.origin,
            )
        },
        playMove = playMove,
        manageGame = manageGame,
        deleteGame = deleteGame,
    )

    suspend fun importFen(title: String, fen: String, defaultTitle: String): ImportResult {
        val normalized = try {
            FenCodec.encode(FenCodec.parse(fen))
        } catch (error: IllegalArgumentException) {
            return ImportResult.Failure(ImportFailureCause.INVALID_FEN, error.message.orEmpty())
        }
        val gameId = createImportedGame(GamePreparation(
            title = title.ifBlank { defaultTitle }, setup = GameSetup.local(), initialFen = normalized,
        ))
        return ImportResult.Success(gameId, importedPlies = 0, skippedPlies = emptyList())
    }

    /**
     * 从 FEN 导入残局为**人机对局**。
     * [aiSide] 为 null 时人类执当前回合方（轮到谁走谁就是人类方），AI 执另一方。
     */
    suspend fun importFenAsAiGame(
        title: String,
        fen: String,
        defaultTitle: String,
        aiSide: Side? = null,
    ): ImportResult {
        val state = try {
            FenCodec.parse(fen)
        } catch (error: IllegalArgumentException) {
            return ImportResult.Failure(ImportFailureCause.INVALID_FEN, error.message.orEmpty())
        }
        val effectiveAiSide = aiSide ?: state.sideToMove.opposite()
        val gameId = createImportedGame(GamePreparation(
            title = title.ifBlank { defaultTitle },
            setup = GameSetup.humanVsAi(effectiveAiSide),
            initialFen = FenCodec.encode(state),
        ))
        return ImportResult.Success(gameId, importedPlies = 0, skippedPlies = emptyList())
    }

    suspend fun importJson(title: String, json: String, defaultTitle: String): ImportResult {
        val parsed = try {
            val value = AppJson.parseToJsonElement(json)
            require(value is JsonObject) { "Expected a game object" }
            parseImport(value, title, defaultTitle)
        } catch (error: InvalidImport) {
            return ImportResult.Failure(error.failureCause, error.message.orEmpty())
        } catch (error: SerializationException) {
            return ImportResult.Failure(ImportFailureCause.INVALID_JSON, error.message.orEmpty())
        } catch (error: IllegalArgumentException) {
            return ImportResult.Failure(ImportFailureCause.INVALID_JSON, error.message.orEmpty())
        }

        val gameId = createImportedGame(parsed.preparation)
        val skipped = mutableListOf<SkippedPly>()
        var imported = 0
        var activePly = 0
        var currentFen = parsed.preparation.initialFen
        try {
            parsed.moves.forEachIndexed { index, moveUcci ->
                currentCoroutineContext().ensureActive()
                val sourcePly = index + 1
                when (val step = applyMove(gameId, currentFen, moveUcci)) {
                    is MoveStep.Applied -> {
                        imported++
                        if (sourcePly <= parsed.currentPly) activePly++
                        currentFen = step.fen
                    }
                    MoveStep.Skipped -> skipped += SkippedPly(sourcePly, moveUcci)
                    MoveStep.Aborted -> return abortImport(gameId, "import aborted at ply $sourcePly")
                }
            }

            currentCoroutineContext().ensureActive()
            // The cursor is a boundary in the original file. Skipped plies before it
            // reduce the active imported count; valid future plies remain redoable.
            val restored = manageGame.undo(gameId, imported - activePly)
                ?: return abortImport(gameId, "imported game is unavailable")
            if (restored.currentPly != activePly) return abortImport(gameId, "failed to restore import cursor")
            if (parsed.terminalStatus != null) {
                if (manageGame.applyImportedResult(gameId, parsed.terminalStatus, parsed.winnerSide) == null) {
                    return abortImport(gameId, "imported game is unavailable")
                }
            } else {
                if (restored.status == GameStatus.NOT_STARTED &&
                    parsed.status in setOf(GameStatus.PLAYING, GameStatus.PAUSED)
                ) manageGame.start(gameId)
                if (manageGame.pause(gameId) == null) return abortImport(gameId, "imported game is unavailable")
            }
            currentCoroutineContext().ensureActive()
            return ImportResult.Success(gameId, imported, skipped)
        } catch (cancelled: CancellationException) {
            withContext(NonCancellable) { deleteGame.delete(gameId) }
            throw cancelled
        }
    }

    private data class ParsedImport(
        val preparation: GamePreparation,
        val moves: List<String>,
        val currentPly: Int,
        val status: GameStatus?,
        val terminalStatus: GameStatus?,
        val winnerSide: String?,
    )

    private class InvalidImport(val failureCause: ImportFailureCause, message: String) : IllegalArgumentException(message)

    private fun parseImport(parsed: JsonObject, title: String, defaultTitle: String): ParsedImport {
        val initialFen = try {
            FenCodec.encode(FenCodec.parse(parsed.string("initialFen", FenCodec.INITIAL_FEN)))
        } catch (error: IllegalArgumentException) {
            throw InvalidImport(ImportFailureCause.INVALID_FEN, error.message.orEmpty())
        }
        val mode = GameMode.valueOf(parsed.string("mode", GameMode.LOCAL_PVP.name))
        // Imported online records are local replays, never a live room reconnection.
        val setup = if (mode == GameMode.ONLINE_PVP) GameSetup.local() else GameSetup(
            mode,
            listOf(
                PlayerSeat(Side.BLACK, PlayerType.valueOf(parsed.string("blackPlayerType", PlayerType.HUMAN.name))),
                PlayerSeat(Side.WHITE, PlayerType.valueOf(parsed.string("whitePlayerType", PlayerType.HUMAN.name))),
            ),
        )
        val moves = when (val value = parsed["moves"]) {
            null, JsonNull -> emptyList()
            is JsonArray -> value.map { move ->
                ((move as? JsonObject)?.get("move") as? JsonPrimitive)?.takeIf { it.isString }?.content.orEmpty()
            }
            else -> throw IllegalArgumentException("Expected a moves array")
        }
        val currentPly = if ("currentPly" in parsed) {
            val value = parsed["currentPly"] as? JsonPrimitive
            requireNotNull(value?.takeUnless { it.isString }?.intOrNull) { "Invalid currentPly" }
                .also { require(it in 0..moves.size) { "currentPly is outside the stored history" } }
        } else moves.size
        val status = parsed["status"]?.let { GameStatus.valueOf(parsed.string("status")) }
        val terminalStatus = resolveStatus(parsed.string("result")) ?: status?.takeIf {
            GameResultResolver.resultText(it).isNotEmpty()
        }
        val winnerSide = parsed.string("winnerSide").takeIf { it.isNotEmpty() }?.also {
            require(it == Side.BLACK.name || it == Side.WHITE.name) { "Invalid winnerSide" }
        }
        return ParsedImport(
            preparation = GamePreparation(
                title = title.ifBlank { parsed.string("title").ifBlank { defaultTitle } },
                setup = setup,
                initialFen = initialFen,
                blackAiConfig = parseAiConfig(parsed, "blackAiConfig"),
                whiteAiConfig = parseAiConfig(parsed, "whiteAiConfig"),
                origin = parsed.optionalObject("origin")?.let { AppJson.decodeFromJsonElement<GameOrigin>(it) },
            ),
            moves = moves,
            currentPly = currentPly,
            status = status,
            terminalStatus = terminalStatus,
            winnerSide = winnerSide,
        )
    }

    private fun JsonObject.string(key: String, default: String = ""): String {
        val value = get(key) ?: return default
        require(value is JsonPrimitive && value.isString) { "Expected string field $key" }
        return value.content
    }

    private fun JsonObject.optionalObject(key: String): JsonObject? = when (val value: JsonElement? = get(key)) {
        null, JsonNull -> null
        is JsonObject -> value
        else -> throw IllegalArgumentException("Expected object field $key")
    }

    private fun parseAiConfig(parsed: JsonObject, key: String): GameAiPlayerConfig? =
        parsed.optionalObject(key)?.let {
            AppJson.decodeFromJsonElement<GameAiPlayerConfig>(it).also { config ->
                require(config.isSupported) { "Invalid AI opponent" }
            }
        }

    private suspend fun abortImport(gameId: String, message: String): ImportResult.Failure {
        deleteGame.delete(gameId)
        return ImportResult.Failure(ImportFailureCause.INVALID_JSON, message)
    }

    /** 单步重放的三种结局，避免用 null / 哨兵字符串表达。 */
    private sealed interface MoveStep {
        data class Applied(val fen: String) : MoveStep

        /** 着法非法或缺失：跳过，不阻断整体导入。 */
        data object Skipped : MoveStep

        /** 不可继续的数据层错误：触发整次导入回滚。 */
        data object Aborted : MoveStep
    }

    private suspend fun applyMove(gameId: String, currentFen: String, moveUcci: String): MoveStep {
        if (moveUcci.isBlank()) return MoveStep.Skipped
        val before = try {
            FenCodec.parse(currentFen)
        } catch (_: IllegalArgumentException) {
            return MoveStep.Aborted
        }
        val legal = GameArbiter.legalMoves(before).firstOrNull { it.notationUcci.equals(moveUcci, ignoreCase = true) }
            ?: return MoveStep.Skipped
        return when (val result = playMove.commit(gameId, legal)) {
            is PlayMoveUseCase.Result.Success -> MoveStep.Applied(result.detail.currentFen)
            is PlayMoveUseCase.Result.Rejected -> MoveStep.Skipped
        }
    }

    private fun resolveStatus(rawResult: String): GameStatus? = when (rawResult) {
        "", "NONE" -> null
        GameResultCode.WHITE, "WHITE_WINS" -> GameStatus.WHITE_WINS
        GameResultCode.BLACK, "BLACK_WINS" -> GameStatus.BLACK_WINS
        GameResultCode.DRAW -> GameStatus.DRAW
        GameResultCode.RESIGNED -> GameStatus.RESIGNED
        else -> throw IllegalArgumentException("Invalid game result")
    }
}
