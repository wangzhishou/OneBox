package com.wanbaohe.gomoku.application.usecase

import com.shifenmiao.model.ModelProvider.AppJson
import com.wanbaohe.gomoku.application.dto.ExportLabels
import com.wanbaohe.gomoku.application.dto.GameDetail
import com.wanbaohe.gomoku.application.dto.NotationRows
import com.wanbaohe.gomoku.application.dto.PlyRecord
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportGameUseCase @Inject constructor(
    private val query: GameQueryUseCase,
) {

    suspend fun asFen(gameId: String): String =
        query.getById(gameId)?.currentFen.orEmpty()

    suspend fun asJson(gameId: String): String {
        val detail = query.getById(gameId) ?: return ""
        return buildJson(detail)
    }

    suspend fun asText(gameId: String, labels: ExportLabels, resultText: String): String {
        val detail = query.getById(gameId) ?: return ""
        return buildText(detail, labels, resultText)
    }

    /**
     * 序列化为可回读的棋谱 JSON。
     *
     * 用 JSON builder 而非字符串插值——标题含引号/反斜杠/换行时插值会产出非法 JSON，
     * 形成"能导出、不能导入"的单向格式。
     *
     * `result` 写 [GameResultCode] 词表（与导入侧对齐），另出 `winnerSide`，
     * 否则认输局无法还原胜方。
     * `moves` 保留全部着法（包括重做区），`currentPly` 单独记录当前局面游标。
     */
    private fun buildJson(detail: GameDetail): String {
        val payload = buildJsonObject {
            put("version", 3)
            put("title", detail.title)
            put("mode", detail.mode.name)
            put("blackPlayerType", detail.blackPlayerType.name)
            put("whitePlayerType", detail.whitePlayerType.name)
            detail.blackAiConfig?.let { put("blackAiConfig", AppJson.parseToJsonElement(it.encode())) }
            detail.whiteAiConfig?.let { put("whiteAiConfig", AppJson.parseToJsonElement(it.encode())) }
            detail.origin?.let { put("origin", AppJson.parseToJsonElement(it.encode())) }
            put("initialFen", detail.initialFen)
            put("currentPly", detail.currentPly)
            put("status", detail.status.name)
            // Resignation is not derivable from the board; preserve its stored result and winner.
            put("result", detail.resultText)
            put("winnerSide", detail.winnerSide)
            put("moves", buildJsonArray {
                detail.plies.sortedBy { it.ply }.forEach { ply ->
                    add(buildJsonObject {
                        put("move", ply.moveUcci)
                        put("moveCn", ply.moveCn)
                        put("side", ply.moverSide.name)
                    })
                }
            })
        }
        return Json { prettyPrint = true }.encodeToString(JsonObject.serializer(), payload)
    }

    /**
     * 渲染文本棋谱。
     *
     * 按 [PlyRecord.moverSide] 分组而非 `chunked(2)`：白先残局不能被排成黑先。
     * 白先时首个回合渲染成 `1. ... <白手>`。
     */
    private fun buildText(detail: GameDetail, labels: ExportLabels, resultText: String): String {
        val builder = StringBuilder()
        builder.appendLine(labels.header)
        builder.appendLine("${labels.titleLabel}: ${detail.title}")
        builder.appendLine("${labels.initialFenLabel}: ${detail.initialFen}")
        detail.origin?.let { origin ->
            if (labels.sourceLabel.isNotBlank()) {
                builder.appendLine("${labels.sourceLabel}: ${origin.title} · ${origin.ply} (${origin.gameId})")
                builder.appendLine(origin.fen)
            }
        }
        builder.appendLine()

        NotationRows.of(detail.plies.filter { it.ply <= detail.currentPly }).forEach { row ->
            val black = row.black?.notationText()
            val white = row.white?.notationText()
            builder.appendLine(
                buildString {
                    append("${row.turn}.")
                    append(" ")
                    append(black ?: "...")
                    if (white != null) {
                        append(" ")
                        append(white)
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
