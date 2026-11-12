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

package com.wanbaohe.cloud.storage.data.protocol

object ObjectStoragePathResolver {

    fun normalizePrefix(prefix: String): String {
        val trimmed = prefix.trim().trim('/')
        return if (trimmed.isBlank()) "" else "$trimmed/"
    }

    fun splitBreadcrumbs(prefix: String): List<String> {
        val normalized = normalizePrefix(prefix)
        if (normalized.isBlank()) return listOf("")
        val rawParts = normalized.removeSuffix("/").split("/").filter { it.isNotBlank() }
        val crumbs = mutableListOf("")
        var acc = ""
        rawParts.forEach { part ->
            acc = if (acc.isBlank()) "$part/" else "$acc$part/"
            crumbs += acc
        }
        return crumbs
    }

    fun parentPrefix(prefix: String): String {
        val parts = normalizePrefix(prefix).removeSuffix("/").split("/").filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> ""
            parts.size == 1 -> ""
            else -> parts.dropLast(1).joinToString(separator = "/", postfix = "/")
        }
    }

    fun childPrefix(currentPrefix: String, childName: String): String =
        normalizePrefix("${normalizePrefix(currentPrefix)}${childName.trim().trim('/')}")

    fun displayName(key: String, isDirectory: Boolean): String {
        val normalized = if (isDirectory) key.removeSuffix("/") else key
        return normalized.substringAfterLast('/', normalized)
    }
}
