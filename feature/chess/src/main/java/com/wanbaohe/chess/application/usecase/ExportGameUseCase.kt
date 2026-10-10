package com.wanbaohe.chess.application.usecase

import com.shifenmiao.model.ModelProvider.AppJson
import kotlinx.serialization.encodeToString
import com.wanbaohe.chess.application.dto.ExportLabels
import com.wanbaohe.chess.application.dto.GameDetail
import com.wanbaohe.chess.application.dto.NotationRows
import com.wanbaohe.chess.application.dto.PlyRecord
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportGameUseCase @Inject constructor(
    private val query: GameQueryUseCase,
    private val mutationLock: GameMutationLock,
) {

    suspend fun asFen(gameId: String): String =
        query.getById(gameId)?.currentFen.orEmpty()

    suspend fun asJson(gameId: String): String = mutationLock.withGame(gameId) {
        query.getById(gameId)?.let(::buildJson).orEmpty()
    }

    suspend fun asText(gameId: String, labels: ExportLabels, resultText: String): String {
        val detail = query.getById(gameId) ?: return ""
        return buildText(detail, labels, resultText)
    }

    /**
     * 序列化为可回读的棋谱 JSON。
     *
     * 用 JSON 编码器而非字符串插值——标题含引号/反斜杠/换行时插值会产出非法 JSON，
     * 形成"能导出、不能导入"的单向格式。
     *
     * `result` 写 [GameResultCode] 词表（与导入侧对齐），另出 `winnerSide`，
     * 否则认输局无法还原胜方。
     */
    private fun buildJson(detail: GameDetail): String {
        return AppJson.encodeToString(buildJsonObject {
            put("version", 1)
            put("title", detail.title)
            put("initialFen", detail.initialFen)
            put("mode", detail.mode.name)
            put("whitePlayerType", detail.whitePlayerType.name)
            put("blackPlayerType", detail.blackPlayerType.name)
            put("whiteAiConfig", AppJson.encodeToJsonElement(detail.whiteAiConfig))
            put("blackAiConfig", AppJson.encodeToJsonElement(detail.blackAiConfig))
            put("status", detail.status.name)
            // 结果与胜方都直接透传落库值：认输属非盘面终局，从 status 反推不出胜方
            put("result", detail.resultText)
            put("winnerSide", detail.winnerSide)
            // JSON is a full-history backup; the cursor distinguishes active moves from redo.
            put("currentPly", detail.currentPly)
            put("moves", buildJsonArray {
                detail.plies.sortedBy { it.ply }.forEach { ply ->
                    add(buildJsonObject {
                        put("move", ply.moveUcci)
                        put("moveCn", ply.moveCn)
                        put("side", ply.moverSide.name)
                    })
                }
            })
            detail.origin?.let { put("origin", AppJson.encodeToJsonElement(it)) }
        })
    }

    /**
     * 渲染文本棋谱。
     *
     * 按 [PlyRecord.moverSide] 分组而非 `chunked(2)`：黑先残局下按位置配对会让整谱红黑错位。
     * 黑先时首个回合渲染成 `1. ... <黑手>`（标准记谱写法）。
     */
    private fun buildText(detail: GameDetail, labels: ExportLabels, resultText: String): String {
        val builder = StringBuilder()
        builder.appendLine(labels.header)
        builder.appendLine("${labels.titleLabel}: ${detail.title}")
        builder.appendLine("${labels.initialFenLabel}: ${detail.initialFen}")
        builder.appendLine()

        NotationRows.of(detail.plies.filter { it.ply <= detail.currentPly }).forEach { row ->
            val white = row.white?.notationText()
            val black = row.black?.notationText()
            builder.appendLine(
                buildString {
                    append("${row.turn}.")
                    append(" ")
                    append(white ?: "...")
                    if (black != null) {
                        append(" ")
                        append(black)
                    }
                },
            )
        }

        if (resultText.isNotBlank()) {
            builder.appendLine()
            builder.appendLine("${labels.resultLabel}: $resultText")
        }

        return builder.toString().trim()
    }

    /** UI 渲染 `moveCn` 时对空值回退到 UCCI，导出应与之一致，避免同一步两种写法。 */
    private fun PlyRecord.notationText(): String = moveCn.ifBlank { moveUcci }
}
