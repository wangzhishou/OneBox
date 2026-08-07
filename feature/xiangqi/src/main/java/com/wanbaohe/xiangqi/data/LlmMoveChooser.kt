package com.wanbaohe.xiangqi.data

import android.content.Context
import com.shifenmiao.common.ai.AIPromptExecutor
import com.shifenmiao.database.chat_prompt.dao.PromptDao
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.wanbaohe.xiangqi.application.port.outbound.EngineSlot
import com.wanbaohe.xiangqi.application.port.outbound.MoveChooser
import com.wanbaohe.xiangqi.application.port.outbound.MoveDecision
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.Side
import com.wanbaohe.xiangqi.domain.model.XiangqiMove
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
        legalMoves: List<XiangqiMove>,
        slot: EngineSlot,
    ): MoveDecision? {
        if (legalMoves.isEmpty()) return null

        val systemPrompt = buildSystemPrompt()
        val userPrompt = buildUserPrompt(fen, boardState.sideToMove, history, legalMoves)
        val engineMode = slot.toEngineMode()

        repeat(2) {
            val result = aiPromptExecutor.execute(
                input = userPrompt,
                systemPrompt = systemPrompt,
                engineMode = engineMode,
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

        return fallback(legalMoves)
    }

    private fun parseSelectedMove(content: String, legalMoves: List<XiangqiMove>): XiangqiMove? {
        val cleaned = content.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val selectedMove = runCatching {
            JSONObject(cleaned).optString("selectedMove")
        }.getOrDefault("")
        return legalMoves.firstOrNull { it.notationUcci == selectedMove }
    }

    private fun parseReason(content: String): String = runCatching {
        val cleaned = content.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        JSONObject(cleaned).optString("reason")
    }.getOrDefault("")

    private fun fallback(legalMoves: List<XiangqiMove>): MoveDecision? {
        val move = legalMoves.sortedWith(
            compareByDescending<XiangqiMove> { it.captured != null }
                .thenByDescending { it.notationCn.contains("进") }
        ).firstOrNull() ?: return null
        return MoveDecision(move, "fallback", "fallback", fallbackUsed = true)
    }

    private suspend fun buildSystemPrompt(): String {
        val preset = promptDao
            .getSystemPromptByKey(PromptEntity.SYSTEM_PROMPT_KEY_XIANGQI_MOVE)
            ?.prompt
            ?.trim()
            .orEmpty()
        if (preset.isNotBlank()) return preset
        return context.resources.openRawResource(com.shifenmiao.database.R.raw.prompt_xiangqi_move)
            .bufferedReader()
            .use { it.readText() }
            .trim()
    }

    private fun buildUserPrompt(
        fen: String,
        side: Side,
        history: List<String>,
        legalMoves: List<XiangqiMove>,
    ): String {
        val historyText = history.ifEmpty { listOf(context.getString(com.wanbaohe.xiangqi.R.string.xiangqi_llm_history_none)) }.joinToString("\n")
        val legalText = legalMoves.joinToString("\n") {
            "- ${it.notationUcci} | ${it.notationCn.ifBlank { it.notationUcci }}"
        }
        return """
当前 FEN：$fen
执子方：${if (side == Side.RED) "RED" else "BLACK"}
最近走子：
$historyText

可选合法走法：
$legalText

请只输出 JSON：
{
  "selectedMove": "a0a1",
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
