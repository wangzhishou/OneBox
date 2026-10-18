package com.shifenmiao.base.utils

import com.shifenmiao.base.entrypoint.AppEntryPoint
import com.shifenmiao.base.hilt.ResourceProvider
import com.shifenmiao.core.R
import com.shifenmiao.core.constants.Constants.CHINESE_DATE_FORMATTER
import com.shifenmiao.interfaces.singleton.AppContext
import dagger.hilt.android.EntryPointAccessors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {

    private val BIDI_CONTROLS = Regex("[\\u200E\\u200F\\u061C\\u202A-\\u202E\\u2066-\\u2069]")

    /**
     * 纯数字日期在 RTL 段落里会被双向算法重排(25/09/2026 → 2026/09/25 或更乱)。
     * 去掉 ICU 插入的方向标记,再用 LRI/PDI 隔离成一段 LTR 文本。
     */
    private fun ltrIsolated(text: String): String =
        "\u2066" + BIDI_CONTROLS.replace(text, "") + "\u2069"

    private val resourceProvider: ResourceProvider

    init {
        val entryPoint = EntryPointAccessors.fromApplication(
            context = AppContext.getContext(),
            entryPoint = AppEntryPoint::class.java
        )
        resourceProvider = entryPoint.getResourceProvider()
    }

    fun convertElapsedTimeIntoText(timeElapsed: Long): String {
        return getTimePassedInHourMinSec(resourceProvider, timeElapsed)
    }

    private fun getTimePassedInHourMinSec(resourceProvider: ResourceProvider, timePassedMs: Long): String {
        return when {
            timePassedMs < TimeUnit.MINUTES.toMillis(1) -> {
                val seconds = TimeUnit.MILLISECONDS.toSeconds(timePassedMs).toInt()
                resourceProvider.getQuantityString(R.plurals.d_seconds_ago, seconds, seconds)
            }

            timePassedMs < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(timePassedMs).toInt()
                resourceProvider.getQuantityString(R.plurals.d_minutes_ago, minutes, minutes)
            }

            timePassedMs < TimeUnit.HOURS.toMillis(4) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(timePassedMs)
                val minutes =
                    TimeUnit.MILLISECONDS.toMinutes(timePassedMs - hours * TimeUnit.HOURS.toMillis(1))
                resourceProvider.getQuantityString(R.plurals.d_hours_ago, hours.toInt(), hours.toInt())
            }

            else -> ""
        }
    }

    /**
     * 格式化时间戳为日期字符串:中文环境保持「yyyy年MM月dd日」,其他语种用系统本地化格式。
     *
     * @param timestampMs The timestamp in milliseconds to format, can be null
     * @return Formatted date string or empty string if input is null
     */
    fun formatDate(timestampMs: Long?): String {
        return try {
            if (timestampMs == null) return ""

            val instant = Instant.ofEpochMilli(timestampMs)
            val isZh = Locale.getDefault().language == Locale.CHINESE.language
            val formatter = if (isZh) {
                DateTimeFormatter.ofPattern(CHINESE_DATE_FORMATTER)
            } else {
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                    .withLocale(Locale.getDefault())
            }.withZone(ZoneId.systemDefault())
            ltrIsolated(formatter.format(instant))
        } catch (e: Exception) {
            ""
        }
    }

}