package com.wanbaohe.chess.application.usecase

import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.AiRequestProtocol
import com.wanbaohe.chess.application.dto.GameAiPlayerConfig
import com.wanbaohe.chess.application.dto.GamePreparation
import com.wanbaohe.chess.application.port.outbound.ChessAiSource
import com.wanbaohe.chess.domain.FenCodec
import com.wanbaohe.chess.domain.GameArbiter
import com.wanbaohe.chess.domain.GameResultCode
import com.wanbaohe.chess.domain.GameResultResolver
import com.wanbaohe.chess.domain.model.GameMode
import com.wanbaohe.chess.domain.model.GameSetup
import com.wanbaohe.chess.domain.model.GameStatus
import com.wanbaohe.chess.domain.model.Side
import com.wanbaohe.chess.domain.model.GameOrigin
import com.wanbaohe.chess.domain.model.PlayerSeat
import com.wanbaohe.chess.domain.model.PlayerType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.doubleOrNull
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
    private val backupJson = Json(AppJson) {
        isLenient = false
        coerceInputValues = false
        ignoreUnknownKeys = false
    }

    @Inject
    constructor(
        createGame: CreateGameUseCase,
        playMove: PlayMoveUseCase,
        manageGame: ManageGameUseCase,
        deleteGame: DeleteGameUseCase,
    ) : this(
        createImportedGame = { preparation -> createGame.createPrepared(preparation) },
        playMove = playMove,
        manageGame = manageGame,
        deleteGame = deleteGame,
    )

    suspend fun importFen(title: String, fen: String, defaultTitle: String): ImportResult {
        val normalized = runCatching { FenCodec.encode(FenCodec.parse(fen)) }.getOrElse { error ->
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
        val state = runCatching { FenCodec.parse(fen) }.getOrElse { error ->
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
            val root = backupJson.parseToJsonElement(json)
            require(root is JsonObject) { "Expected a game object" }
            parseImport(root, title, defaultTitle)
        } catch (error: InvalidImport) {
            return ImportResult.Failure(error.failureCause, error.message.orEmpty())
        } catch (error: SerializationException) {
            return ImportResult.Failure(ImportFailureCause.INVALID_JSON, error.message.orEmpty())
        } catch (error: IllegalArgumentException) {
            return ImportResult.Failure(ImportFailureCause.INVALID_JSON, error.message.orEmpty())
        }

        currentCoroutineContext().ensureActive()
        val gameId = withContext(NonCancellable) { createImportedGame(parsed.preparation) }
        val skipped = mutableListOf<SkippedPly>()
        var imported = 0
        var currentFen = parsed.preparation.initialFen
        try {
            parsed.moves.forEachIndexed { index, moveUcci ->
                currentCoroutineContext().ensureActive()
                val ply = index + 1
                when (val step = applyMove(gameId, currentFen, moveUcci)) {
                    is MoveStep.Applied -> {
                        imported++
                        currentFen = step.fen
                    }
                    MoveStep.Skipped -> skipped += SkippedPly(ply, moveUcci)
                    MoveStep.Aborted -> return abortImport(gameId, "import aborted at ply $ply")
                }
            }

            currentCoroutineContext().ensureActive()
            val restoredCursor = ImportedHistoryCursor.afterSkipping(parsed.currentPly, skipped)
            val restored = manageGame.undo(gameId, imported - restoredCursor)
                ?: return abortImport(gameId, "imported game is unavailable")
            if (restored.currentPly != restoredCursor) return abortImport(gameId, "failed to restore import cursor")
            if (parsed.terminalStatus != null) {
                manageGame.applyImportedResult(gameId, parsed.terminalStatus, parsed.winnerSide)
                    ?: return abortImport(gameId, "failed to restore result")
            } else {
                manageGame.pauseImported(gameId, parsed.wasStarted)
                    ?: return abortImport(gameId, "failed to pause imported game")
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
        val terminalStatus: GameStatus?,
        val winnerSide: String?,
        val wasStarted: Boolean,
    )

    private class InvalidImport(val failureCause: ImportFailureCause, message: String) : IllegalArgumentException(message)

    private fun parseImport(root: JsonObject, title: String, defaultTitle: String): ParsedImport {
        val initialFen = normalizeFen(root.string("initialFen", FenCodec.INITIAL_FEN))
        val mode = GameMode.valueOf(root.string("mode", GameMode.LOCAL_PVP.name))
        val hasSeats = "whitePlayerType" in root || "blackPlayerType" in root
        val declaredSetup = if (mode == GameMode.ONLINE_PVP && !hasSeats) {
            GameSetup.online(Side.WHITE)
        } else GameSetup(mode, listOf(
            PlayerSeat(Side.WHITE, PlayerType.valueOf(root.string("whitePlayerType", PlayerType.HUMAN.name))),
            PlayerSeat(Side.BLACK, PlayerType.valueOf(root.string("blackPlayerType", PlayerType.HUMAN.name))),
        ))
        val whiteConfig = parseAiConfig(root, "whiteAiConfig", declaredSetup.playerTypeFor(Side.WHITE))
        val blackConfig = parseAiConfig(root, "blackAiConfig", declaredSetup.playerTypeFor(Side.BLACK))
        val origin = root.optionalObject("origin")?.let {
            val source = backupJson.decodeFromJsonElement<GameOrigin>(it)
            require(source.gameId.isNotBlank() && source.ply >= 0) { "Invalid source snapshot" }
            normalizeFen(source.fen)
            source
        }
        val moves = when (val value = root["moves"]) {
            null, JsonNull -> emptyList()
            is JsonArray -> value.map {
                ((it as? JsonObject)?.get("move") as? JsonPrimitive)
                    ?.takeIf { move -> move.isString }?.content.orEmpty()
            }
            else -> throw IllegalArgumentException("moves must be an array")
        }
        val cursor = if ("currentPly" !in root) moves.size else {
            val value = root["currentPly"] as? JsonPrimitive
            require(value != null && !value.isString && value != JsonNull) { "currentPly must be an integer" }
            ImportedHistoryCursor.read(requireNotNull(value.doubleOrNull) { "currentPly must be an integer" }, moves.size)
        }
        val savedStatus = if ("status" in root) GameStatus.valueOf(root.string("status")) else null
        val result = root.string("result")
        val resultStatus = resolveStatus(result)
        val terminalStatus = resultStatus ?: savedStatus?.takeIf {
            GameResultResolver.resultText(it).isNotEmpty()
        }
        require(result.isEmpty() || resultStatus != null) { "Invalid result" }
        require(resultStatus == null || savedStatus == null || resultStatus == savedStatus) {
            "Result and status disagree"
        }
        val winner = root.string("winnerSide").takeIf { it.isNotEmpty() }?.also {
            require(it == Side.WHITE.name || it == Side.BLACK.name) { "Invalid winnerSide" }
        }
        require(winner == null || terminalStatus == GameStatus.RESIGNED ||
            winner == terminalStatus?.let(GameResultResolver::winnerSide)
        ) { "Winner and result disagree" }
        return ParsedImport(
            preparation = GamePreparation(
                title = title.ifBlank { root.string("title").ifBlank { defaultTitle } },
                // A backup is a replay, never permission to reconnect to an old room.
                setup = if (mode == GameMode.ONLINE_PVP) GameSetup.local() else declaredSetup,
                initialFen = initialFen,
                whiteAiConfig = whiteConfig,
                blackAiConfig = blackConfig,
                origin = origin,
            ),
            moves = moves,
            currentPly = cursor,
            terminalStatus = terminalStatus,
            winnerSide = winner,
            wasStarted = savedStatus != null && savedStatus != GameStatus.NOT_STARTED,
        )
    }

    private fun parseAiConfig(root: JsonObject, key: String, playerType: PlayerType): GameAiPlayerConfig? {
        val objectValue = root.optionalObject(key)
        if (playerType != PlayerType.LLM) {
            require(objectValue == null) { "$key is only valid for an AI seat" }
            return null
        }
        requireNotNull(objectValue) { "$key is required for an AI seat" }
        require(objectValue.keys.all { it in setOf("sourceKey", "engineName", "engineProtocol", "engineTitle", "model") }) {
            "$key must contain an opponent snapshot, not connection credentials"
        }
        val sourceKey = objectValue.string("sourceKey")
        require(sourceKey == "working_model" || ChessAiSource.RemoteEngine.presets.any { it.engineId == sourceKey }) {
            "Unsupported AI source"
        }
        val engineName = objectValue.string("engineName")
        val engineProtocol = objectValue.string("engineProtocol")
        objectValue.string("engineTitle")
        val modelValue = objectValue.optionalObject("model")
        if (sourceKey == "working_model") {
            require(engineName.isNotBlank()) { "Missing saved provider" }
            val protocol = AiRequestProtocol.entries.firstOrNull { it.name == engineProtocol }
            require(protocol != null && protocol != AiRequestProtocol.LOCAL_ON_DEVICE && !protocol.isNonChat) {
                "Unsupported saved provider protocol"
            }
            requireNotNull(modelValue) { "Missing saved model" }
            require(modelValue.string("name").isNotBlank()) { "Missing saved model name" }
            require("title" in modelValue) { "Missing saved model title" }
            modelValue.string("title")
        } else {
            require(engineName.isEmpty() && engineProtocol.isEmpty() &&
                objectValue.string("engineTitle").isEmpty() && modelValue == null
            ) { "Remote-engine config cannot contain a chat provider" }
        }
        val config = backupJson.decodeFromJsonElement<GameAiPlayerConfig>(objectValue)
        config.model?.let { model ->
            require(model.temperature.isFinite() && model.temperature >= 0 && model.topP in 0.0..1.0 &&
                model.basePoints.isFinite() && model.basePoints >= 0 && model.maxTokens > 0 &&
                model.contextWindowTokens >= 0
            ) { "Invalid saved model parameters" }
        }
        return config
    }

    private fun normalizeFen(fen: String): String = try {
        FenCodec.encode(FenCodec.parse(fen))
    } catch (error: IllegalArgumentException) {
        throw InvalidImport(ImportFailureCause.INVALID_FEN, error.message.orEmpty())
    }

    private fun JsonObject.string(key: String, default: String = ""): String = when (val value = this[key]) {
        null -> default
        is JsonPrimitive -> {
            require(value != JsonNull && value.isString) { "$key must be a string" }
            value.content
        }
        else -> throw IllegalArgumentException("$key must be a string")
    }

    private fun JsonObject.optionalObject(key: String): JsonObject? = when (val value = this[key]) {
        null, JsonNull -> null
        is JsonObject -> value
        else -> throw IllegalArgumentException("$key must be an object")
    }

    private suspend fun abortImport(gameId: String, message: String): ImportResult.Failure {
        withContext(NonCancellable) { deleteGame.delete(gameId) }
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
        val before = runCatching { FenCodec.parse(currentFen) }.getOrNull() ?: return MoveStep.Aborted
        val legal = GameArbiter.legalMoves(before).firstOrNull { it.notationUcci == moveUcci }
            ?: return MoveStep.Skipped
        return when (val result = playMove.commit(gameId, legal, importing = true)) {
            is PlayMoveUseCase.Result.Success -> MoveStep.Applied(result.detail.currentFen)
            is PlayMoveUseCase.Result.Rejected -> MoveStep.Skipped
        }
    }

    /** Also accepts legacy exports that wrote terminal status names instead of result codes. */
    private fun resolveStatus(rawResult: String): GameStatus? = when (rawResult) {
        GameResultCode.WHITE, "WHITE_WINS" -> GameStatus.WHITE_WINS
        GameResultCode.BLACK, "BLACK_WINS" -> GameStatus.BLACK_WINS
        GameResultCode.DRAW -> GameStatus.DRAW
        GameResultCode.RESIGNED -> GameStatus.RESIGNED
        else -> null
    }
}
