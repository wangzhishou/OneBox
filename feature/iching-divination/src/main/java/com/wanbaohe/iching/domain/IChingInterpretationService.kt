package com.wanbaohe.iching.domain

import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.common.ai.AIPromptExecutor
import com.shifenmiao.common.ai.AIPromptResult
import com.shifenmiao.common.ai.AiLanguagePrompt
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.database.chat_prompt.dao.PromptDao
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.wanbaohe.iching.model.DivinationResult
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class IChingInterpretationService @Inject constructor(
    private val executor: AIPromptExecutor,
    private val promptDao: PromptDao,
    private val textLibrary: IChingTextLibrary,
) {
    /**
     * 流式生成 AI 解读:[onDelta] 逐段回调累计全文快照供 UI 流式展示,
     * 结束后返回完整内容(成功时)。
     */
    suspend fun interpret(
        result: DivinationResult,
        onDelta: (String) -> Unit = {},
        onReasoningDelta: (String) -> Unit = {},
    ): Result<String> = try {
        Result.success(
            run {
                val input = buildInput(result)
                val response = executor.executeStreaming(
                    systemPrompt = systemPrompt(),
                    input = input,
                    onDelta = onDelta,
                    onReasoningDelta = onReasoningDelta,
                    // 本 Service 自己按 chargePoints() 扣费,避免重复扣
                    billing = AIPromptExecutor.PromptBilling.EXTERNAL,
                )
                check(response.isSuccess && response.content.isNotBlank()) {
                    response.errorMessage?.takeIf(String::isNotBlank) ?: "AI 解读生成失败"
                }
                chargePoints(response, input)
                response.content.trim()
            }
        )
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (throwable: Throwable) {
        Result.failure(throwable)
    }

    /**
     * 解读请求输入文本(请求体与积分预估共用,保持一致口径)。
     * 语言跟随当前资源 locale;附上卦辞与动爻爻辞原文,让解读有所依据。
     */
    fun buildInput(result: DivinationResult): String =
        if (textLibrary.isChinese) buildInputZh(result) else buildInputEn(result)

    private fun buildInputZh(result: DivinationResult): String {
        val primary = textLibrary.hexagram(result.primary.number)
        val changing = result.changingLineNumbers
        val changingText = changing.mapNotNull { pos ->
            primary.lines.getOrNull(pos - 1)?.takeIf(String::isNotBlank)?.let { "第${pos}爻:$it" }
        }.joinToString("；")
        val changedText = result.changed?.let {
            val changed = textLibrary.hexagram(it.number)
            buildString {
                append("变卦:第${it.number}卦 ${changed.name}")
                if (changed.judgment.isNotBlank()) append("(卦辞:${changed.judgment})")
            }
        } ?: "无动爻,以本卦卦辞为准"
        return buildString {
            appendLine("所问:${result.question.ifBlank { "未指定具体事项" }}")
            appendLine("本卦:第${result.primary.number}卦 ${primary.name}")
            appendLine("上卦:${textLibrary.trigramName(result.primary.upperTrigramCode)};下卦:${textLibrary.trigramName(result.primary.lowerTrigramCode)}")
            if (primary.judgment.isNotBlank()) appendLine("卦辞:${primary.judgment}")
            appendLine("爻值(自下而上):${result.lines.joinToString(",") { it.value.toString() }}")
            appendLine("动爻:${changing.joinToString("、").ifBlank { "无" }}${if (changingText.isNotBlank()) "($changingText)" else ""}")
            append(changedText)
        }.trimEnd()
    }

    private fun buildInputEn(result: DivinationResult): String {
        val primary = textLibrary.hexagram(result.primary.number)
        val changing = result.changingLineNumbers
        val changingText = changing.mapNotNull { pos ->
            primary.lines.getOrNull(pos - 1)?.takeIf(String::isNotBlank)?.let { "line $pos: $it" }
        }.joinToString("; ")
        val changedText = result.changed?.let {
            val changed = textLibrary.hexagram(it.number)
            buildString {
                append("Changed hexagram: #${it.number} ${changed.name}")
                if (changed.judgment.isNotBlank()) append(" (judgment: ${changed.judgment})")
            }
        } ?: "No changing lines; read the judgment of the primary hexagram"
        return buildString {
            appendLine("Question: ${result.question.ifBlank { "Not specified" }}")
            appendLine("Primary hexagram: #${result.primary.number} ${primary.name}")
            appendLine("Upper trigram: ${textLibrary.trigramName(result.primary.upperTrigramCode)}; Lower trigram: ${textLibrary.trigramName(result.primary.lowerTrigramCode)}")
            if (primary.judgment.isNotBlank()) appendLine("Judgment: ${primary.judgment}")
            appendLine("Line values (bottom to top): ${result.lines.joinToString(",") { it.value.toString() }}")
            appendLine("Changing lines: ${changing.joinToString(", ").ifBlank { "none" }}${if (changingText.isNotBlank()) " ($changingText)" else ""}")
            append(changedText)
        }.trimEnd()
    }

    /**
     * 代理路由(我方服务器引擎)按量扣积分;BYOK 直连不扣。
     * 接口带 usage 时按 totalTokens,否则按输入+输出文本估算。失败不扣(此处只在成功后调用)。
     */
    private fun chargePoints(response: AIPromptResult, input: String) {
        if (!response.isProxyRoute) return
        val tokens = response.totalTokens.takeIf { it > 0 }
            ?: (StringUtils.calculateTokens(input) + StringUtils.calculateTokens(response.content))
        if (tokens <= 0) return
        runCatching {
            BaseUtils.consumePoints(
                degree = BaseUtils.tokenToPoints(tokens),
                desc = "易经AI解读",
                source = response.engineName.ifBlank { "iching" },
            )
        }
    }

    /** 优先读「系统提示词管理」中的预置提示词,取不到时按应用语言回退到内置默认值 */
    private suspend fun systemPrompt(): String =
        promptDao.getSystemPromptByKey(PromptEntity.SYSTEM_PROMPT_KEY_ICHING_INTERPRETATION)
            ?.prompt
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?: if (textLibrary.isChinese) {
                DEFAULT_SYSTEM_PROMPT
            } else {
                // 卦辞原文只有中/英两版, 但白话解读应当跟随界面语言:
                // 以前非中文一律给英文解读, 土耳其语用户拿到的就是英文。
                DEFAULT_SYSTEM_PROMPT_EN + "\n\n" + AiLanguagePrompt.outputInCurrentLanguage(
                    "the interpretation and its Markdown section headings"
                )
            }

    private companion object {
        const val DEFAULT_SYSTEM_PROMPT = """你是一位严谨、温和的《易经》文化解读助手。请基于用户所问事项、本卦和变爻,用简体中文给出参考性解读,输出 Markdown 格式,依次包含"卦象总述""事业""感情""财运""健康"小节,各部分简洁清晰、紧扣所问。不得声称能预测确定未来,不得替代医疗、法律或投资专业意见。"""

        const val DEFAULT_SYSTEM_PROMPT_EN = """You are a rigorous and gentle I Ching cultural interpretation assistant. Based on the user's question, the primary hexagram, and the changing lines, give a reference interpretation in English, in Markdown, with the sections "Overview", "Career", "Relationships", "Finances", and "Health" — each concise, clear, and tied to the question. Never claim to predict a certain future, and never replace professional medical, legal, or investment advice."""
    }
}
