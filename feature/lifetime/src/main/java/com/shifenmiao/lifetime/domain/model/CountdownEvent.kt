package com.shifenmiao.lifetime.domain.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate

/**
 * 倒数日领域模型。
 *
 * - [targetDate] 公历目标日期，存 epochDay；[isLunarTarget] = true 时为空
 * - [lunarMonth] / [lunarDay] 农历月日（仅 [isLunarTarget] = true 时使用），跨年时由 [nextOccurrence] 滚动到下一年
 * - [isPreset] 标识是否由系统/节日兜底生成
 */
@Immutable
data class CountdownEvent(
    val id: Long = 0,
    val name: String,
    val iconKey: String = "Event",
    val targetDate: LocalDate? = null,
    val isLunarTarget: Boolean = false,
    val lunarMonth: Int? = null,
    val lunarDay: Int? = null,
    val note: String? = null,
    val color: String? = null,
    val sortOrder: Int = 999,
    val isPreset: Boolean = false,
    val isFromHoliday: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * 计算下一次发生的公历日期。
     * 对公历：直接用 [targetDate]
     * 对农历：用当前日期之后最近的一次农历月日（同年或下一年）
     */
    fun nextOccurrence(now: LocalDate = LocalDate.now()): LocalDate? {
        if (!isLunarTarget) return targetDate?.takeIf { !it.isBefore(now) }
        val lm = lunarMonth ?: return null
        val ld = lunarDay ?: return null
        val candidates = listOf(
            resolveLunarSolarDate(now.year, lm, ld),
            resolveLunarSolarDate(now.year + 1, lm, ld),
        ).filterNotNull()
        return candidates.firstOrNull { !it.isBefore(now) }
    }
}

/**
 * 解析农历月日到公历日期；委托给 feature:calendar 的 LunarJavaBridge。
 * 解析失败时返回 null（节日源仍可写入数据库，但卡片会显示「日期待定」）。
 */
internal fun resolveLunarSolarDate(year: Int, month: Int, day: Int): java.time.LocalDate? {
    return try {
        val solar = com.wanbaohe.calendar.data.LunarJavaBridge.lunarToSolarDate(year, month, day, false)
            ?: return null
        java.time.LocalDate.of(solar.year, solar.month, solar.day)
    } catch (_: Throwable) {
        null
    }
}

@Immutable
data class CountdownStatus(
    val event: CountdownEvent,
    val nextOccurrence: LocalDate?,
    val daysUntil: Long,
    val isToday: Boolean,
    val isPast: Boolean,
    val progress: Float,
)
