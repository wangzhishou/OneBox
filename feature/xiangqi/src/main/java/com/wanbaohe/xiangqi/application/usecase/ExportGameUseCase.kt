package com.wanbaohe.xiangqi.application.usecase

import com.wanbaohe.xiangqi.application.dto.ExportLabels
import com.wanbaohe.xiangqi.application.dto.GameDetail
import com.wanbaohe.xiangqi.domain.model.GameStatus
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

    suspend fun asText(gameId: String, labels: ExportLabels): String {
        val detail = query.getById(gameId) ?: return ""
        return buildText(detail, labels)
    }

    private fun buildJson(detail: GameDetail): String {
        val moves = detail.plies.joinToString(",\n") {
            "  { \"move\": \"${it.moveUcci}\", \"moveCn\": \"${it.moveCn}\" }"
        }
        return """{
  "version": 1,
  "title": "${detail.title}",
  "initialFen": "${detail.initialFen}",
  "result": "${detail.status.name}",
  "moves": [
$moves
  ]
}""".trimIndent()
    }

    private fun buildText(detail: GameDetail, labels: ExportLabels): String {
        val builder = StringBuilder()
        builder.appendLine(labels.header)
        builder.appendLine("${labels.titleLabel}: ${detail.title}")
        builder.appendLine("${labels.initialFenLabel}: ${detail.initialFen}")
        builder.appendLine()

        detail.plies.chunked(2).forEachIndexed { index, chunk ->
            val red = chunk.getOrNull(0)?.moveCn.orEmpty()
            val black = chunk.getOrNull(1)?.moveCn.orEmpty()
            builder.appendLine("${index + 1}. $red $black".trim())
        }

        if (detail.status.isTerminal()) {
            builder.appendLine()
            builder.appendLine("${labels.resultLabel}: ${detail.status.name}")
        }

        return builder.toString().trim()
    }

    private fun GameStatus.isTerminal(): Boolean =
        this == GameStatus.RED_WINS || this == GameStatus.BLACK_WINS ||
            this == GameStatus.DRAW || this == GameStatus.RESIGNED
}
