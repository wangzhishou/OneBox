package com.wanbaohe.gomoku.application.usecase

import com.wanbaohe.gomoku.application.dto.ExportLabels
import com.wanbaohe.gomoku.application.dto.GameDetail
import com.wanbaohe.gomoku.application.dto.NotationRows
import com.wanbaohe.gomoku.application.dto.PlyRecord
import org.json.JSONArray
import org.json.JSONObject
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
     * 用 [JSONObject] 而非字符串插值——标题含引号/反斜杠/换行时插值会产出非法 JSON，
     * 形成"能导出、不能导入"的单向格式。
     *
     * `result` 写 [GameResultCode] 词表（与导入侧对齐），另出 `winnerSide`，
     * 否则认输局无法还原胜方。
     */
    private fun buildJson(detail: GameDetail): String {
        val moves = JSONArray()
        detail.plies.forEach { ply ->
            moves.put(
                JSONObject()
                    .put("move", ply.moveUcci)
                    .put("moveCn", ply.moveCn)
                    .put("side", ply.moverSide.name),
            )
        }

        return JSONObject()
            .put("version", 1)
            .put("title", detail.title)
            .put("initialFen", detail.initialFen)
            // 结果与胜方都直接透传落库值：认输属非盘面终局，从 status 反推不出胜方
            .put("result", detail.resultText)
            .put("winnerSide", detail.winnerSide)
            .put("moves", moves)
            .toString(2)
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

        NotationRows.of(detail.plies).forEach { row ->
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
