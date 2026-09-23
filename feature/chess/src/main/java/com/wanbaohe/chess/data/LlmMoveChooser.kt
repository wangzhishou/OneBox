package com.wanbaohe.chess.data

import android.content.Context
import com.shifenmiao.common.ai.AIPromptExecutor
import com.shifenmiao.database.chat_prompt.dao.PromptDao
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.wanbaohe.chess.application.port.outbound.EngineSlot
import com.wanbaohe.chess.application.port.outbound.MoveChooser
import com.wanbaohe.chess.application.port.outbound.MoveDecision
import com.wanbaohe.chess.domain.model.BoardState
import com.wanbaohe.chess.domain.model.Side
import com.wanbaohe.chess.domain.model.ChessMove
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlmMoveChooser @Inject constructor(
    private val aiPromptExecutor: AIPromptExecutor,
    private val promptDao: PromptDao,
    @ApplicationContext private val context: Context,
) : MoveChooser {

    override suspend fun choose(
        boardState: BoardState,
        fen: String,
        history: List<String>,
        legalMoves: List<ChessMove>,
        slot: EngineSlot,
    ): MoveDecision? {
        if (legalMoves.isEmpty()) return null

        val systemPrompt = buildSystemPrompt()
        val userPrompt = buildUserPrompt(fen, boardState, history, legalMoves)
        val engineMode = slot.toEngineMode()

        repeat(2) {
            val result = aiPromptExecutor.execute(
                input = userPrompt,
                systemPrompt = systemPrompt,
                engineMode = engineMode,
                // 对局内每步都要调用,按量扣分会让棋局不可玩:有意保持免费
                billing = AIPromptExecutor.PromptBilling.EXTERNAL,
            )
            if (!result.isSuccess) return@repeat

            val selected = parseSelectedMove(result.content, legalMoves)
            if (selected != null) {
                return MoveDecision(
                    move = selected,
                    reason = parseReason(result.content),
                    rawResponse = result.content,
                    fallbackUsed = false,
                )
            }
        }

        return ChessMoveFallback.decision(boardState, legalMoves)
    }

    private fun parseSelectedMove(content: String, legalMoves: List<ChessMove>): ChessMove? {
        val cleaned = content.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val selectedMove = runCatching {
            JSONObject(cleaned).optString("selectedMove")
        }.getOrDefault("")
        // 模型输出大小写不定,坐标匹配不区分大小写
        return legalMoves.firstOrNull { it.notationUcci.equals(selectedMove, ignoreCase = true) }
    }

    private fun parseReason(content: String): String = runCatching {
        val cleaned = content.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        JSONObject(cleaned).optString("reason")
    }.getOrDefault("")

    private suspend fun buildSystemPrompt(): String {
        val preset = promptDao
            .getSystemPromptByKey(PromptEntity.SYSTEM_PROMPT_KEY_CHESS_MOVE)
            ?.prompt
            ?.trim()
            .orEmpty()
        if (preset.isNotBlank()) return preset
        return context.resources.openRawResource(com.shifenmiao.database.R.raw.prompt_chess_move)
            .bufferedReader()
            .use { it.readText() }
            .trim()
    }

    private fun buildUserPrompt(
        fen: String,
        boardState: BoardState,
        history: List<String>,
        legalMoves: List<ChessMove>,
    ): String {
        val historyText = history.ifEmpty { listOf(context.getString(com.wanbaohe.chess.R.string.chess_llm_history_none)) }.joinToString("\n")
        // 国际象棋合法着全部列出(一局通常 30-40 个,prompt 体量可控)
        val legalText = legalMoves.joinToString("\n") { "- ${it.notationUcci}" }
        return """
当前局面(标准 FEN):$fen
执子方:${if (boardState.sideToMove == Side.WHITE) "WHITE(白方)" else "BLACK(黑方)"}
最近走子:
$historyText

全部合法着法(UCI 坐标,如 "e2e4";升变带后缀,如 "e7e8q"):
$legalText

请只输出 JSON:
{
  "selectedMove": "e2e4",
  "reason": "一句简短理由",
  "plan": "一句后续计划",
  "confidence": 0.0
}
        """.trimIndent()
    }

    private fun EngineSlot.toEngineMode() = when (this) {
        EngineSlot.FAST -> AIPromptExecutor.EngineMode.FAST
        EngineSlot.DUEL_A -> AIPromptExecutor.EngineMode.DUEL_A
        EngineSlot.DUEL_B -> AIPromptExecutor.EngineMode.DUEL_B
    }
}
