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

package com.wanbaohe.dynamicui.parser

import android.util.LruCache
import com.wanbaohe.dynamicui.ir.UiNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DualFormatParser – the single entry-point for turning JSON strings
 * into an [UiNode] IR tree + initial state map.
 * ## Caching
 * Parsed results are stored in a [LruCache] keyed by the input's hashCode so that
 * re-renders of unchanged JSON configs skip the parsing step entirely.
 */
@Singleton
class DualFormatParser @Inject constructor() {

    private val cache = LruCache<String, ParseResult>(50)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = false
    }

    data class ParseResult(
        val root: UiNode,
        val initialState: Map<String, Any?>,
    )

    // ─── Synchronous API (use on background thread) ───────────────────────────

    fun parseJson(jsonString: String): ParseResult {
        val key = cacheKey(jsonString)
        cache.get(key)?.let { return it }
        val doc = json.parseToJsonElement(jsonString).jsonObject
        val (root, state) = SchemaToIrMapper.map(doc)
        return ParseResult(root, state).also { cache.put(key, it) }
    }

    suspend fun parseJsonAsync(jsonString: String): ParseResult =
        withContext(Dispatchers.Default) { parseJson(jsonString) }

    /** Apply a JSON Patch (RFC 6902) diff to an already-parsed result. */
    fun applyPatch(base: ParseResult, patchJson: String): ParseResult {
        // Minimal implementation: full re-parse after server merges patch server-side.
        // A full RFC 6902 client-side patch engine can replace this later.
        return parseJson(patchJson)
    }

    /** Invalidate a specific cache entry (e.g. after a hot-reload push). */
    fun invalidate(jsonString: String) {
        cache.remove(cacheKey(jsonString))
    }

    fun clearCache() = cache.evictAll()

    private fun cacheKey(jsonString: String): String =
        "json:${jsonString.length}:${jsonString.hashCode()}"
}
