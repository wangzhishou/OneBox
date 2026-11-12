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

package com.wanbaohe.cloud.storage.screen.components

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * 把远端返回的时间串（S3 / WebDAV 多为 RFC 1123，如 `Tue, 10 Nov 2026 04:50:20 GMT`）
 * 格式化成当地 `yyyy-MM-dd HH:mm`，受不了就直接原样返回。
 */
internal fun formatRemoteTimestamp(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    val patterns = listOf(
        "EEE, dd MMM yyyy HH:mm:ss zzz",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd HH:mm:ss",
    )
    patterns.forEach { pattern ->
        val parsed = runCatching {
            SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("GMT")
                isLenient = false
            }.parse(raw)
        }.getOrNull()
        if (parsed != null) {
            return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(parsed)
        }
    }
    return raw
}
