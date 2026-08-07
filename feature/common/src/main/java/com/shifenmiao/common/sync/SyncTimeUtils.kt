package com.shifenmiao.common.sync

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

internal object SyncTimeUtils {
    private val ISO_OFFSET_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    private val ISO_INSTANT_FORMATTER = DateTimeFormatter.ISO_INSTANT

    fun formatTimestamp(timestampMillis: Long): String {
        if (timestampMillis <= 0) {
            return "1970-01-01T00:00:00Z"
        }
        return ISO_INSTANT_FORMATTER.format(Instant.ofEpochMilli(timestampMillis))
    }

    fun parseIsoTime(isoTime: String): Long {
        if (isoTime.isBlank()) return 0L
        return try {
            Instant.from(ISO_OFFSET_FORMATTER.parse(isoTime)).toEpochMilli()
        } catch (_: DateTimeParseException) {
            try {
                Instant.parse(isoTime).toEpochMilli()
            } catch (_: DateTimeParseException) {
                0L
            }
        }
    }
}
