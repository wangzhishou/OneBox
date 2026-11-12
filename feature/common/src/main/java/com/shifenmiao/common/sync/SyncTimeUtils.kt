/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
