package com.wanbaohe.bookkeeping.screen.util

import com.shifenmiao.model.money.AppCurrency
import com.shifenmiao.model.money.MoneyFormat
import com.wanbaohe.bookkeeping.model.BookkeepingRecordType
import com.wanbaohe.bookkeeping.model.BookkeepingRecordUi
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** 带币种符号的金额,如 ¥12.50 / ₩12,300(币种与小数位由调用方传入的 [currency] 决定) */
internal fun centsText(cents: Long, currency: AppCurrency): String =
    MoneyFormat.amount(cents, currency)

/** 仅格式化绝对值,不带货币符号也不带正负号,如 12.50 */
internal fun centsValueText(cents: Long, currency: AppCurrency): String =
    MoneyFormat.value(cents, currency)

/** 根据账单类型加上 +/- 前缀 */
internal fun signedAmount(record: BookkeepingRecordUi, currency: AppCurrency): String {
    val value = centsValueText(record.amountCents, currency)
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
    // 跟随语言习惯,不要在非中文语种下输出 "M月d日"
    return "${date.format(LOCALIZED_MONTH_DAY)} $suffix"
}

/** 账单行副标题：时间（+ 备注） */
private val LOCALIZED_MONTH_DAY = DateTimeFormatter.ofPattern("MMM d", java.util.Locale.getDefault())

internal fun recordLineSubtitle(record: BookkeepingRecordUi): String {
    val time = Instant.ofEpochMilli(record.happenedAt)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("HH:mm"))
    return if (record.note.isBlank()) time else "$time | ${record.note}"
}

