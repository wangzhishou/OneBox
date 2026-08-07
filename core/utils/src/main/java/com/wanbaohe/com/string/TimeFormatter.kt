package com.wanbaohe.com.string

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import java.text.SimpleDateFormat
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

    /** 将 Date 格式化为相对时间描述 */
    @Composable
    fun formatRelativeTime(date: Date): String {
        val now = System.currentTimeMillis()
        val diff = now - date.time
        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> stringResource(R.string.favorite_time_just_now)
            diff < TimeUnit.HOURS.toMillis(1) -> stringResource(
                R.string.favorite_time_minutes_ago,
                diff / TimeUnit.MINUTES.toMillis(1)
            )
            diff < TimeUnit.DAYS.toMillis(1) -> stringResource(
                R.string.favorite_time_hours_ago,
                diff / TimeUnit.HOURS.toMillis(1)
            )
            diff < TimeUnit.DAYS.toMillis(3) -> stringResource(
                R.string.favorite_time_days_ago,
                diff / TimeUnit.DAYS.toMillis(1)
            )
            else -> SimpleDateFormat("MM月dd日", Locale.CHINA).format(date)
        }
    }

    /**
     * 格式化日期为指定格式
     *
     * @param timestamp Unix时间戳（毫秒）
     * @param pattern 日期格式，例如 "MM月dd日"、"yyyy-MM-dd HH:mm"
     * @return 格式化后的日期字符串
     */
    fun formatDate(timestamp: Long, pattern: String = "MM月dd日"): String {
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 格式化截止日期，带过期提醒
     *
     * @param dueDate 截止日期时间戳（毫秒）
     * @param isCompleted 任务是否已完成
     * @return 截止日期描述，例如 "明天到期"、"已过期2天"
     */
    fun formatDueDate(dueDate: Long?, isCompleted: Boolean = false): String? {
        if (dueDate == null) return null

        val now = System.currentTimeMillis()
        val diff = dueDate - now

        return when {
            isCompleted -> formatDate(dueDate, "MM月dd日")
            diff < 0 -> {
                // 已过期
                val overdueDays = TimeUnit.MILLISECONDS.toDays(-diff)
                when {
                    overdueDays == 0L -> "今天到期"
                    overdueDays == 1L -> "昨天到期"
                    else -> "过期${overdueDays}天"
                }
            }
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "${minutes}分钟后到期"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "${hours}小时后到期"
            }
            diff < TimeUnit.DAYS.toMillis(2) -> "明天到期"
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "${days}天后到期"
            }
            else -> "到期：${formatDate(dueDate, "MM月dd日")}"
        }
    }

    /**
     * 格式化日期和时间
     *
     * @param timestamp Unix时间戳（毫秒）
     * @return 格式化的日期时间字符串，例如 "12月28日 14:30"
     */
    fun formatDateTime(timestamp: Long): String {
        return formatDate(timestamp, "MM月dd日 HH:mm")
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