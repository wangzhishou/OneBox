package com.wanbaohe.gomoku.application.usecase

import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.GameArbiter
import com.wanbaohe.gomoku.domain.GameResultCode
import com.wanbaohe.gomoku.domain.model.GameSetup
import com.wanbaohe.gomoku.domain.model.GameStatus
import com.wanbaohe.gomoku.domain.model.Side
import org.json.JSONArray
import org.json.JSONObject
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
class ImportGameUseCase @Inject constructor(
    private val createGame: CreateGameUseCase,
    private val playMove: PlayMoveUseCase,
    private val manageGame: ManageGameUseCase,
    private val deleteGame: DeleteGameUseCase,
) {

    suspend fun importFen(title: String, fen: String, defaultTitle: String): ImportResult {
        val normalized = runCatching { FenCodec.encode(FenCodec.parse(fen)) }.getOrElse { error ->
            return ImportResult.Failure(ImportFailureCause.INVALID_FEN, error.message.orEmpty())
        }
        val gameId = createGame.create(title.ifBlank { defaultTitle }, GameSetup.local(), normalized)
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
        val gameId = createGame.create(
            title.ifBlank { defaultTitle },
            GameSetup.humanVsAi(effectiveAiSide),
            FenCodec.encode(state),
        )
        return ImportResult.Success(gameId, importedPlies = 0, skippedPlies = emptyList())
    }

    suspend fun importJson(title: String, json: String, defaultTitle: String): ImportResult {
        val parsed = runCatching { JSONObject(json) }.getOrElse { error ->
            return ImportResult.Failure(ImportFailureCause.INVALID_JSON, error.message.orEmpty())
        }

        val rawInitialFen = parsed.optString("initialFen", FenCodec.INITIAL_FEN)
        val initialFen = runCatching { FenCodec.encode(FenCodec.parse(rawInitialFen)) }
            .getOrElse { error ->
                return ImportResult.Failure(ImportFailureCause.INVALID_FEN, error.message.orEmpty())
            }

        val gameTitle = title.ifBlank { parsed.optString("title").ifBlank { defaultTitle } }
        val gameId = createGame.create(gameTitle, GameSetup.local(), initialFen)

        val movesArray = parsed.optJSONArray("moves") ?: JSONArray()
        val skipped = mutableListOf<SkippedPly>()
        var imported = 0
        // 盘面在内存里增量推进。原实现每手都回库全量读取（含反序列化全部 plies），是 O(N) 次全量查询。
        var currentFen = initialFen

        for (index in 0 until movesArray.length()) {
            val ply = index + 1
            val moveUcci = movesArray.optJSONObject(index)?.optString("move").orEmpty()
            when (val step = applyMove(gameId, currentFen, moveUcci)) {
                is MoveStep.Applied -> {
                    imported++
                    currentFen = step.fen
                }
                // 这一手不合法/取不到：计入跳过，盘面停在原地，继续尝试后续着法。
                MoveStep.Skipped -> skipped += SkippedPly(ply, moveUcci)
                // 数据层不可继续（如棋局不存在）：删掉半成品，不留残缺对局在历史库里。
                MoveStep.Aborted -> {
                    deleteGame.delete(gameId)
                    return ImportResult.Failure(
                        cause = ImportFailureCause.INVALID_JSON,
                        message = "import aborted at ply $ply",
                    )
                }
            }
        }

        applyImportedResult(gameId, parsed)

        return ImportResult.Success(gameId, imported, skipped)
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
        return when (val result = playMove.commit(gameId, legal)) {
            is PlayMoveUseCase.Result.Success -> MoveStep.Applied(result.detail.currentFen)
            is PlayMoveUseCase.Result.Rejected -> MoveStep.Skipped
        }
    }

    /**
     * 回读导出的终局结果。
     *
     * 认输、和棋这类结果无法从盘面重算出来，必须从文件恢复，否则"导入一局认输的棋"会变成一个
     * 无法解释的进行中对局。同时兼容历史版本写出的 `status.name`（如 `BLACK_WINS`）。
     */
    private suspend fun applyImportedResult(gameId: String, parsed: JSONObject) {
        val status = resolveStatus(parsed.optString("result")) ?: return
        val winnerSide = parsed.optString("winnerSide")
            .takeIf { it == "BLACK" || it == "WHITE" }
        manageGame.applyImportedResult(gameId, status, winnerSide)
    }

    private fun resolveStatus(rawResult: String): GameStatus? = when (rawResult) {
        GameResultCode.WHITE, "WHITE_WINS" -> GameStatus.WHITE_WINS
        GameResultCode.BLACK, "BLACK_WINS" -> GameStatus.BLACK_WINS
        GameResultCode.DRAW -> GameStatus.DRAW
        GameResultCode.RESIGNED -> GameStatus.RESIGNED
        else -> null
    }
}
