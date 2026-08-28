package com.wanbaohe.iching.domain

import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.common.ai.AIPromptExecutor
import com.shifenmiao.common.ai.AIPromptResult
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.database.chat_prompt.dao.PromptDao
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.wanbaohe.iching.model.DivinationResult
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class IChingInterpretationService @Inject constructor(
    private val executor: AIPromptExecutor,
    private val promptDao: PromptDao,
) {
    suspend fun interpret(result: DivinationResult): Result<String> = try {
        Result.success(
            run {
                val input = buildInput(result)
                val response = executor.execute(
                    systemPrompt = systemPrompt(),
                    input = input,
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

    /** 解读请求输入文本(请求体与积分预估共用,保持一致口径) */
    fun buildInput(result: DivinationResult): String {
        val changedText = result.changed?.let { "变卦：第${it.number}卦 ${it.name}" } ?: "无变爻"
        return """
            所问：${result.question.ifBlank { "未指定具体事项" }}
            本卦：第${result.primary.number}卦 ${result.primary.name}
            上卦：${result.primary.upperTrigram}；下卦：${result.primary.lowerTrigram}
            爻值（自下而上）：${result.lines.joinToString(",") { it.value.toString() }}
            变爻：${result.changingLineNumbers.joinToString("、").ifBlank { "无" }}
            $changedText
        """.trimIndent()
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

    /** 优先读「系统提示词管理」中的预置提示词，取不到时回退到内置默认值 */
    private suspend fun systemPrompt(): String =
        promptDao.getSystemPromptByKey(PromptEntity.SYSTEM_PROMPT_KEY_ICHING_INTERPRETATION)
            ?.prompt
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?: DEFAULT_SYSTEM_PROMPT

    private companion object {
        const val DEFAULT_SYSTEM_PROMPT = """你是一位严谨、温和的《易经》文化解读助手。请基于用户所问事项、本卦和变爻，用简体中文给出参考性解读，输出 Markdown 格式，依次包含“卦象总述”“事业”“感情”“财运”“健康”小节，各部分简洁清晰、紧扣所问。不得声称能预测确定未来，不得替代医疗、法律或投资专业意见。"""
    }
}
