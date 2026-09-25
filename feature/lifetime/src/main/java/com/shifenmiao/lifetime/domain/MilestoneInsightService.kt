package com.shifenmiao.lifetime.domain

import com.shifenmiao.common.ai.AIPromptExecutor
import com.shifenmiao.common.ai.AiLanguagePrompt
import com.shifenmiao.core.R as CoreR
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.lifetime.data.MilestoneAiInsightRepository
import com.shifenmiao.lifetime.domain.model.PersonalMilestone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 里程碑 AI 文案服务。
 *
 * 编排逻辑：
 * 1. 调用 [AIPromptExecutor] 生成一句简短文案
 * 2. 成功后写入 [MilestoneAiInsightRepository] 形成历史
 * 3. 失败/引擎未配置时只更新加载态，不入库
 *
 * Component / Tool 不直接接触 DAO 与 AIPromptExecutor，必须通过本服务组合。
 */
@Singleton
class MilestoneInsightService @Inject constructor(
    private val aiExecutor: AIPromptExecutor,
    private val insightRepository: MilestoneAiInsightRepository,
) {

    /**
     * 生成并保存一条新文案。返回生成结果，调用方可决定是否刷新 UI。
     */
    suspend fun generateAndSave(milestone: PersonalMilestone): GenerationResult {
        val prompt = buildPrompt(milestone)
        val result = aiExecutor.execute(
            systemPrompt = systemPrompt(),
            input = prompt,
            // 自动触发的一句话文案,有意保持免费
            billing = AIPromptExecutor.PromptBilling.EXTERNAL,
        )
        if (!result.isSuccess) {
            return GenerationResult.Failed(result.errorMessage.orEmpty())
        }
        val content = result.content.trim()
        if (content.isBlank()) {
            return GenerationResult.Failed(
                AppContext.getString(CoreR.string.ai_empty_response_error)
            )
        }
        insightRepository.addInsight(milestoneId = milestone.id, content = content)
        return GenerationResult.Success(content)
    }

    private fun buildPrompt(milestone: PersonalMilestone): String {
        val dateText = milestone.targetDate
            ?.let { "%04d-%02d-%02d".format(it.year, it.monthValue, it.dayOfMonth) }
            ?: "unspecified"
        val noteText = milestone.note?.takeIf { it.isNotBlank() } ?: "none"
        return "User's milestone: ${milestone.name}\nTarget date: $dateText\nNote: $noteText"
    }

    sealed interface GenerationResult {
        data class Success(val content: String) : GenerationResult
        data class Failed(val reason: String) : GenerationResult
    }

    companion object {
        /** 文案直接展示在里程碑卡片上, 跟随应用语言; 中文长度限制不适用于外语, 故给出按词计的等价量。 */
        private fun systemPrompt(): String =
            "You are a philosophical thinker about time. Write ONE short, warm, insightful comment " +
                "on the user's milestone: a single sentence, at most ~30 characters in Chinese or " +
                "roughly 10-15 words in other languages. Output the comment only, with no title and " +
                "no extra explanation.\n\n" +
                AiLanguagePrompt.outputInCurrentLanguage("the comment")
    }
}
