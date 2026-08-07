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
