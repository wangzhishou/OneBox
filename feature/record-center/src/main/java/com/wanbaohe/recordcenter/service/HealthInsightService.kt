package com.wanbaohe.recordcenter.service

import com.shifenmiao.common.ai.AIPromptExecutor
import com.shifenmiao.common.ai.AiLanguagePrompt
import com.shifenmiao.database.recordcenter.entity.HealthRecordEntity
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.recordcenter.data.HealthProfile
import com.wanbaohe.recordcenter.model.RecordFieldsCodec
import com.wanbaohe.recordcenter.registry.RecordTypeDefinition
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 健康记录 AI 解读服务(对齐 MilestoneInsightService)。
 *
 * 编排逻辑:
 * 1. 按记录类型定义 + 最近若干条记录拼装输入,调用 [AIPromptExecutor] 生成解读
 * 2. 失败/引擎未配置时只返回失败原因
 *
 * 与里程碑/诗词不同:数据变化频繁,解读结果**不落库、不缓存**,由页面手动触发生成。
 */
@Singleton
class HealthInsightService @Inject constructor(
    private val aiExecutor: AIPromptExecutor,
) {

    /**
     * 解读当前筛选范围内的记录。
     *
     * @param definition 记录类型定义(标题/字段/参考范围经 string resources 本地化)
     * @param records    当前范围内的记录(任意顺序,内部按时间倒序截取最近 [MAX_RECORDS] 条)
     * @param rangeLabel 已本地化的时间范围文案,如「近 30 天」
     * @param profile    用户基础信息(性别/年龄/身高/体重),已设置时作为上下文带上
     */
    suspend fun interpret(
        definition: RecordTypeDefinition,
        records: List<HealthRecordEntity>,
        rangeLabel: String,
        profile: HealthProfile? = null,
    ): GenerationResult {
        val input = buildInput(definition, records, rangeLabel, profile)
        val result = aiExecutor.execute(
            systemPrompt = systemPrompt(),
            input = input,
            // 扣费由 RecordListComponent 在成功后按其固定额度处理,避免重复扣
            billing = AIPromptExecutor.PromptBilling.EXTERNAL,
        )
        if (!result.isSuccess) {
            return GenerationResult.Failed(result.errorMessage.orEmpty())
        }
        val content = result.content.trim()
        if (content.isBlank()) {
            return GenerationResult.Failed("AI 返回内容为空")
        }
        return GenerationResult.Success(content)
    }

    private fun buildInput(
        definition: RecordTypeDefinition,
        records: List<HealthRecordEntity>,
        rangeLabel: String,
        profile: HealthProfile?,
    ): String = buildString {
        profile?.takeIf { it.isSet }?.let { p ->
            val parts = mutableListOf<String>()
            when (p.gender) {
                HealthProfile.Gender.MALE -> parts += "male"
                HealthProfile.Gender.FEMALE -> parts += "female"
                HealthProfile.Gender.UNSET -> {}
            }
            p.age?.let { parts += "age $it" }
            p.heightCm?.let { parts += "height ${RecordFieldsCodec.formatValue(it)}cm" }
            p.weightKg?.let { parts += "weight ${RecordFieldsCodec.formatValue(it)}kg" }
            if (parts.isNotEmpty()) {
                append("User profile: ").append(parts.joinToString(", ")).append('\n')
            }
        }
        append("Record type: ").append(AppContext.getString(definition.titleRes)).append('\n')
        definition.referenceRangeRes?.let {
            append("Reference range: ").append(AppContext.getString(it)).append('\n')
        }
        append("Fields: ").append(
            definition.fields.joinToString(", ") { field ->
                if (field.unit.isEmpty()) AppContext.getString(field.labelRes)
                else "${AppContext.getString(field.labelRes)}(${field.unit})"
            }
        ).append('\n')
        append("Time range: ").append(rangeLabel).append('\n')
        append("Records (newest first):\n")
        records.sortedByDescending { it.happenedAt }
            .take(MAX_RECORDS)
            .forEach { entity ->
                val values = RecordFieldsCodec.decode(entity.fieldsJson)
                val valueText = definition.fields.mapNotNull { field ->
                    values[field.key]?.let { value ->
                        val formatted = RecordFieldsCodec.formatValue(value)
                        val label = AppContext.getString(field.labelRes)
                        if (field.unit.isEmpty()) "$label $formatted"
                        else "$label $formatted ${field.unit}"
                    }
                }.joinToString(",")
                val timeText = Instant.ofEpochMilli(entity.happenedAt)
                    .atZone(ZoneId.systemDefault())
                    .format(DATE_FORMATTER)
                append(timeText).append(' ').append(valueText).append('\n')
            }
    }

    sealed interface GenerationResult {
        data class Success(val content: String) : GenerationResult
        data class Failed(val reason: String) : GenerationResult
    }

    companion object {
        /** 送给 AI 的记录条数上限,避免输入过长 */
        private const val MAX_RECORDS = 30

        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d HH:mm")

        /** 解读正文展示给用户, 跟随应用语言; 结语免责声明也必须同语言, 否则等于没声明。 */
        private fun systemPrompt(): String =
            "You are a cautious health advisor. Based on the health records provided, interpret the " +
                "trends in no more than 150 characters (Chinese) or roughly 80-100 words (other languages), " +
                "point out possible anomalies and give everyday-life suggestions. Use plain, conversational " +
                "language, in 2-3 short paragraphs. You MUST end with a disclaimer stating that this is " +
                "for reference only and does not constitute a medical diagnosis. Output the text only, " +
                "with no title and no extra explanation.\n\n" +
                AiLanguagePrompt.outputInCurrentLanguage("the interpretation and the closing disclaimer")
    }
}
