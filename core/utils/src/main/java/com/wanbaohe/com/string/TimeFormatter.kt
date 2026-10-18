package com.wanbaohe.com.string

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.shifenmiao.core.R

/**
 * 时间格式化工具类
 * 提供友好的时间显示格式
 */
object TimeFormatter {

    private val BIDI_CONTROLS = Regex("[\\u200E\\u200F\\u061C\\u202A-\\u202E\\u2066-\\u2069]")

    /**
     * 纯数字日期在 RTL 段落里会被双向算法按段重排(如 07/10/2026 显示成 072026/10/)。
     * 去掉 ICU 插入的方向标记,再用 LRI/PDI 隔离成一段 LTR 文本,保证阿语界面也按原样显示。
     */
    private fun ltrIsolated(text: String): String =
        "\u2066" + BIDI_CONTROLS.replace(text, "") + "\u2069"

    /** 将 Date 格式化为相对时间描述 */
    @Composable
    fun formatRelativeTime(date: Date): String {
        val now = System.currentTimeMillis()
        val diff = now - date.time
        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> stringResource(R.string.favorite_time_just_now)
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = (diff / TimeUnit.MINUTES.toMillis(1)).toInt()
                pluralStringResource(R.plurals.favorite_time_minutes_ago, minutes, minutes)
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = (diff / TimeUnit.HOURS.toMillis(1)).toInt()
                pluralStringResource(R.plurals.favorite_time_hours_ago, hours, hours)
            }
            diff < TimeUnit.DAYS.toMillis(3) -> {
                val days = (diff / TimeUnit.DAYS.toMillis(1)).toInt()
                pluralStringResource(R.plurals.favorite_time_days_ago, days, days)
            }
            else -> ltrIsolated(
                DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(date)
            )
        }
    }

    /**
     * 格式化日期为本地化格式（或指定 pattern）
     *
     * @param timestamp Unix时间戳（毫秒）
     * @param pattern 日期格式，例如 "yyyy-MM-dd HH:mm"；为 null 时使用系统本地化日期
     * @return 格式化后的日期字符串
     */
    fun formatDate(timestamp: Long, pattern: String? = null): String {
        return try {
            val formatted = if (pattern != null) {
                java.text.SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
            } else {
                DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(Date(timestamp))
            }
            ltrIsolated(formatted)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 格式化截止日期，带过期提醒
     *
     * @param dueDate 截止日期时间戳（毫秒）
     * @param isCompleted 任务是否已完成
     * @return 截止日期描述
     */
    @Composable
    fun formatDueDate(dueDate: Long?, isCompleted: Boolean = false): String? {
        if (dueDate == null) return null

        val now = System.currentTimeMillis()
        val diff = dueDate - now

        return when {
            isCompleted -> formatDate(dueDate)
            diff < 0 -> {
                // 已过期
                val overdueDays = TimeUnit.MILLISECONDS.toDays(-diff)
                when {
                    overdueDays == 0L -> stringResource(R.string.time_due_today)
                    overdueDays == 1L -> stringResource(R.string.time_due_yesterday)
                    else -> stringResource(R.string.time_overdue_days, overdueDays.toInt())
                }
            }
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff).toInt()
                stringResource(R.string.time_due_in_minutes, minutes)
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff).toInt()
                stringResource(R.string.time_due_in_hours, hours)
            }
            diff < TimeUnit.DAYS.toMillis(2) -> stringResource(R.string.time_due_tomorrow)
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff).toInt()
                stringResource(R.string.time_due_in_days, days)
            }
            else -> stringResource(R.string.time_due_on, formatDate(dueDate))
        }
    }

    /**
     * 格式化日期和时间
     *
     * @param timestamp Unix时间戳（毫秒）
     * @return 格式化的日期时间字符串
     */
    fun formatDateTime(timestamp: Long): String {
        return try {
            ltrIsolated(
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
                    .format(Date(timestamp))
            )
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 检查日期是否是今天
     */
    fun isToday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        val todayYear = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = timestamp
        val targetDay = calendar.get(Calendar.DAY_OF_YEAR)
        val targetYear = calendar.get(Calendar.YEAR)

        return today == targetDay && todayYear == targetYear
    }

    /**
     * 检查日期是否是明天
     */
    fun isTomorrow(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = calendar.get(Calendar.DAY_OF_YEAR)
        val tomorrowYear = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = timestamp
        val targetDay = calendar.get(Calendar.DAY_OF_YEAR)
        val targetYear = calendar.get(Calendar.YEAR)

        return tomorrow == targetDay && tomorrowYear == targetYear
    }
}
