package com.wanbaohe.calendar.share

import com.shifenmiao.common.ai.AIPromptExecutor
import com.shifenmiao.common.ai.AiLanguagePrompt
import com.shifenmiao.core.R as CoreR
import com.shifenmiao.interfaces.singleton.AppContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

/**
 * 分享页的 AI 心语。
 *
 * 走 [AIPromptExecutor] 的 MANAGED 计费：登录门槛与积分闸门由执行器统一处理，
 * 代理路由成功后才扣积分；BYOK 直连不扣。调用方只负责先拉起登录。
 */
@Singleton
class CalendarShareNarrativeService @Inject constructor(
    private val executor: AIPromptExecutor,
) {

    suspend fun generate(input: CalendarShareNarrativeInput): Result<String> = try {
        val response = executor.execute(
            systemPrompt = systemPrompt(),
            input = input.toPrompt(),
            billing = AIPromptExecutor.PromptBilling.MANAGED,
            billingDesc = "日历分享心语",
        )
        if (!response.isSuccess) {
            Result.failure(IllegalStateException(
                response.errorMessage?.takeIf { it.isNotBlank() }
                    ?: AppContext.getString(CoreR.string.ai_empty_response_error)
            ))
        } else {
            val text = response.content.toNote(MAX_NOTE_LENGTH)
            if (text.isBlank()) {
                Result.failure(IllegalStateException(
                    AppContext.getString(CoreR.string.ai_empty_response_error)
                ))
            } else {
                Result.success(text)
            }
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (throwable: Throwable) {
        Result.failure(throwable)
    }

    private fun systemPrompt(): String = buildString {
        appendLine("You write one short, warm, grounded reflection for a Chinese calendar share card.")
        appendLine("Use the date, lunar date, solar term/festival, Yi (auspicious) and Ji (inauspicious) items,")
        appendLine("and weather only as gentle imagery. Do not predict fortune, do not give medical, legal or")
        appendLine("financial advice, and do not present superstition as fact.")
        appendLine("Output ONE sentence only: 20-40 Chinese characters, or about 10-14 words in other")
        appendLine("languages (keep it under 70 characters).")
        appendLine("No title, no quotation marks, no explanation, no bullet points.")
        append(AiLanguagePrompt.outputInCurrentLanguage("the reflection"))
    }
}

/**
 * 把模型输出收成适合卡面 2 行的长度：优先在句末截断，其次在词边界截断，避免半个单词。
 */
private fun String.toNote(maxLength: Int): String {
    val clean = replace('\n', ' ')
        .replace(Regex("\\s+"), " ")
        .trim()
    if (clean.length <= maxLength) return clean

    val clipped = clean.substring(0, maxLength)
    val sentenceEnd = clipped.indexOfLast { it in "。！？.!?；;" }
    if (sentenceEnd >= maxLength * 55 / 100) {
        return clipped.substring(0, sentenceEnd + 1).trim()
    }
    val wordEnd = clipped.lastIndexOf(' ')
    if (wordEnd >= maxLength * 55 / 100) {
        return clipped.substring(0, wordEnd).trim().trimEnd(',', '，', '、') + "…"
    }
    return clipped.trimEnd() + "…"
}

data class CalendarShareNarrativeInput(
    val date: String,
    val weekday: String,
    val lunar: String,
    val festivals: String,
    val yi: List<String>,
    val ji: List<String>,
    val weather: String?,
) {
    fun toPrompt(): String = buildString {
        appendLine("Date: $date ($weekday)")
        appendLine("Lunar: $lunar")
        if (festivals.isNotBlank()) appendLine("Festival/solar term: $festivals")
        if (yi.isNotEmpty()) appendLine("Yi (suitable): ${yi.joinToString("、")}")
        if (ji.isNotEmpty()) appendLine("Ji (avoid): ${ji.joinToString("、")}")
        weather?.takeIf { it.isNotBlank() }?.let { appendLine("Weather: $it") }
        append("Write the reflection.")
    }
}
