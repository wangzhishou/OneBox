package com.shifenmiao.lifetime.domain

import com.shifenmiao.common.ai.AIPromptExecutor
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
            systemPrompt = SYSTEM_PROMPT,
            input = prompt,
        )
        if (!result.isSuccess) {
            return GenerationResult.Failed(result.errorMessage.orEmpty())
        }
        val content = result.content.trim()
        if (content.isBlank()) {
            return GenerationResult.Failed("AI 返回内容为空")
        }
        insightRepository.addInsight(milestoneId = milestone.id, content = content)
        return GenerationResult.Success(content)
    }

    private fun buildPrompt(milestone: PersonalMilestone): String {
        val dateText = milestone.targetDate?.let { "${it.year}年${it.monthValue}月${it.dayOfMonth}日" } ?: "未定"
        val noteText = milestone.note?.takeIf { it.isNotBlank() } ?: "无"
        return "用户的里程碑：${milestone.name}，目标日期：$dateText，备注：$noteText"
    }

    sealed interface GenerationResult {
        data class Success(val content: String) : GenerationResult
        data class Failed(val reason: String) : GenerationResult
    }

    companion object {
        private const val SYSTEM_PROMPT =
            "你是一个富有哲理的时间思考者。请针对用户的里程碑事件，用一句话给出简短、温暖、有洞察力的评论，不超过30个字。"
    }
}
