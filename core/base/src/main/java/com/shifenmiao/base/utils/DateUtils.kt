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
import java.util.concurrent.TimeUnit

object DateUtils {

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
                resourceProvider.getString(
                    R.string.d_seconds_ago,
                    TimeUnit.MILLISECONDS.toSeconds(timePassedMs)
                )
            }

            timePassedMs < TimeUnit.HOURS.toMillis(1) -> {
                resourceProvider.getString(
                    R.string.d_minutes_ago,
                    TimeUnit.MILLISECONDS.toMinutes(timePassedMs)
                )
            }

            timePassedMs < TimeUnit.HOURS.toMillis(4) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(timePassedMs)
                val minutes =
                    TimeUnit.MILLISECONDS.toMinutes(timePassedMs - hours * TimeUnit.HOURS.toMillis(1))
                resourceProvider.getString(R.string.d_hours_ago, hours, minutes)
            }

            else -> ""
        }
    }

    /**
     * Formats a timestamp in milliseconds to a date string using the Chinese date format.
     *
     * @param timestampMs The timestamp in milliseconds to format, can be null
     * @return Formatted date string or empty string if input is null
     */
    fun formatDate(timestampMs: Long?): String {
        return try {
            if (timestampMs == null) return ""

            val instant = Instant.ofEpochMilli(timestampMs)
            val formatter = DateTimeFormatter.ofPattern(CHINESE_DATE_FORMATTER)
                .withZone(ZoneId.systemDefault())
            formatter.format(instant)
        } catch (e: Exception) {
            ""
        }
    }

}