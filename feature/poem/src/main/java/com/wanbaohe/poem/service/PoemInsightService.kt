package com.wanbaohe.poem.service

import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.common.ai.AIPromptExecutor
import com.shifenmiao.common.ai.AIPromptResult
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.poem.repo.PoemRepository
import com.t8rin.logger.makeLog
import com.wanbaohe.poem.model.Poem
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 诗词 AI 解读服务。
 *
 * 编排逻辑(对齐 MilestoneInsightService):
 * 1. 调用 [AIPromptExecutor] 一次性生成赏析
 * 2. 成功后写入 [PoemRepository] 并记录行为日志
 * 3. 失败/引擎未配置时只返回失败原因,不落库
 */
@Singleton
class PoemInsightService @Inject constructor(
    private val aiExecutor: AIPromptExecutor,
    private val poemRepository: PoemRepository,
    private val activityLogRecorder: ActivityLogRecorder,
) {

    suspend fun generateInsight(poem: Poem): GenerationResult {
        val result = aiExecutor.execute(
            input = buildInput(poem),
            systemPrompt = SYSTEM_PROMPT,
        )
        if (!result.isSuccess) {
            return GenerationResult.Failed(result.errorMessage.orEmpty())
        }
        val content = result.content.trim()
        if (content.isBlank()) {
            return GenerationResult.Failed("AI 返回内容为空")
        }
        poemRepository.updateAiInsight(id = poem.id, insight = content)
        chargePoints(result, buildInput(poem), "诗意解读")
        recordActivity(poem = poem, actionType = "INSIGHT")
        return GenerationResult.Success(content)
    }

    /**
     * 逐字拼音:每句一行,行内每个汉字的拼音用空格分隔,标点不占位。
     * 走快速模型(短格式化输出,不需要深度推理);
     * 由页面在诗词加载后自动调用;失败/引擎未配置时不落库(UI 无拼音即隐藏)。
     */
    suspend fun generatePinyin(poem: Poem): GenerationResult {
        val result = aiExecutor.execute(
            input = buildInput(poem),
            systemPrompt = PINYIN_SYSTEM_PROMPT,
            engineMode = AIPromptExecutor.EngineMode.FAST,
        )
        if (!result.isSuccess) {
            return GenerationResult.Failed(result.errorMessage.orEmpty())
        }
        val content = result.content.trim()
        if (content.isBlank()) {
            return GenerationResult.Failed("AI 返回内容为空")
        }
        poemRepository.updatePinyin(id = poem.id, pinyin = content)
        chargePoints(result, buildInput(poem), "拼音标注")
        return GenerationResult.Success(content)
    }

    /** 现代汉语翻译(100 字左右),由 UI「点击生成」触发 */
    suspend fun generateTranslation(poem: Poem): GenerationResult {
        val result = aiExecutor.execute(
            input = buildInput(poem),
            systemPrompt = TRANSLATION_SYSTEM_PROMPT,
        )
        if (!result.isSuccess) {
            return GenerationResult.Failed(result.errorMessage.orEmpty())
        }
        val content = result.content.trim()
        if (content.isBlank()) {
            return GenerationResult.Failed("AI 返回内容为空")
        }
        poemRepository.updateTranslation(id = poem.id, translation = content)
        chargePoints(result, buildInput(poem), "现代翻译")
        recordActivity(poem = poem, actionType = "TRANSLATION")
        return GenerationResult.Success(content)
    }

    private suspend fun recordActivity(poem: Poem, actionType: String) {
        runCatching {
            activityLogRecorder.recordPoem(
                entityId = poem.id.toString(),
                actorType = "USER",
                actionType = actionType,
                source = "PoemScreen",
                title = poem.title,
                description = "${poem.author}·${poem.dynasty}",
                screenRoute = "onebox://screen/poem?poem_id=${poem.id}",
            )
        }.onFailure { it.makeLog("PoemInsightService") }
    }

    /**
     * 代理路由(我方服务器引擎)按量扣积分;BYOK 直连不扣。
     * 接口带 usage 时按 totalTokens,否则按输入+输出文本估算。
     */
    private fun chargePoints(result: AIPromptResult, input: String, desc: String) {
        if (!result.isProxyRoute) return
        val tokens = result.totalTokens.takeIf { it > 0 }
            ?: (StringUtils.calculateTokens(input) + StringUtils.calculateTokens(result.content))
        if (tokens <= 0) return
        runCatching {
            BaseUtils.consumePoints(
                degree = BaseUtils.tokenToPoints(tokens),
                desc = desc,
                source = result.engineName.ifBlank { "poem" },
            )
        }.onFailure { it.makeLog("PoemInsightService") }
    }

    private fun buildInput(poem: Poem): String = buildString {
        append("标题:").append(poem.title).append('\n')
        append("朝代:").append(poem.dynasty).append('\n')
        append("作者:").append(poem.author).append('\n')
        if (poem.type.isNotBlank()) {
            append("体裁:").append(poem.type).append('\n')
        }
        append("正文:\n").append(poem.content.joinToString("\n"))
    }

    sealed interface GenerationResult {
        data class Success(val content: String) : GenerationResult
        data class Failed(val reason: String) : GenerationResult
    }

    companion object {
        private const val SYSTEM_PROMPT =
            "你是一位古典文学鉴赏家。请用 200 字左右赏析用户给出的诗词,解读其意境、艺术手法与情感,语言优美简洁,直接输出赏析正文,不要加标题或多余说明。"

        private const val PINYIN_SYSTEM_PROMPT =
            "你是汉语拼音专家。请为用户给出的古诗词逐字标注拼音。严格要求:每句诗占一行;行内每个汉字的拼音用单个空格分隔;标点符号不占位、不输出;拼音必须带声调符号(如 shān、lǐ);只返回拼音文本,不要输出原文、序号或任何其他内容。"

        private const val TRANSLATION_SYSTEM_PROMPT =
            "你是一位古典文学翻译专家。请把用户给出的诗词翻译成现代汉语,100 字左右,语言自然流畅、忠实原意,直接输出译文正文,不要加标题或多余说明。"
    }
}
