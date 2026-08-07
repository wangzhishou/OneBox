package com.wanbaohe.bookkeeping.screen.util

import com.wanbaohe.bookkeeping.model.BookkeepingRecordType
import com.wanbaohe.bookkeeping.model.BookkeepingRecordUi
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** 将分（cents）格式化为带符号的货币字符串，如 ¥12.50 */
internal fun centsText(cents: Long): String {
    val sign = if (cents < 0) "-" else ""
    val absolute = kotlin.math.abs(cents)
    val major = absolute / 100
    val minor = absolute % 100
    return "${sign}¥${major}.${minor.toString().padStart(2, '0')}"
}

/** 仅格式化绝对值，不带货币符号也不带正负号，如 12.50 */
internal fun centsValueText(cents: Long): String {
    val absolute = kotlin.math.abs(cents)
    val major = absolute / 100
    val minor = absolute % 100
    return "${major}.${minor.toString().padStart(2, '0')}"
}

/** 根据账单类型加上 +/- 前缀 */
internal fun signedAmount(record: BookkeepingRecordUi): String {
    val value = centsValueText(record.amountCents)
    return when (record.type) {
        BookkeepingRecordType.INCOME   -> "+$value"
        BookkeepingRecordType.EXPENSE  -> "-$value"
        BookkeepingRecordType.EXCLUDED -> value
    }
}

/**
 * 日期区段标题（今天 / 昨天 / 星期X）。
 * 调用方通过参数传入本地化字符串，避免在非 Composable 上下文硬编码中文。
 *
 * @param dayLabels DayOfWeek → 本地化星期名的映射，如 DayOfWeek.MONDAY → "星期一"
 * @param todayLabel 本地化的"今天"
 * @param yesterdayLabel 本地化的"昨天"
 */
internal fun daySectionTitle(
    date: LocalDate,
    todayLabel: String,
    yesterdayLabel: String,
    dayLabels: Map<DayOfWeek, String>,
): String {
    val today = LocalDate.now()
    val suffix = when (date) {
        today              -> todayLabel
        today.minusDays(1) -> yesterdayLabel
        else               -> dayLabels[date.dayOfWeek] ?: date.dayOfWeek.name
    }
    return "${date.format(DateTimeFormatter.ofPattern("M月d日"))} $suffix"
}

/** 账单行副标题：时间（+ 备注） */
internal fun recordLineSubtitle(record: BookkeepingRecordUi): String {
    val time = Instant.ofEpochMilli(record.happenedAt)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("HH:mm"))
    return if (record.note.isBlank()) time else "$time | ${record.note}"
}

