package com.wanbaohe.a2ui.workbench

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

internal const val MAX_SOURCE = 128_000
internal const val MAX_FRAMES = 200

internal object WorkbenchSource {
    private val formatter = Json { prettyPrint = true }

    fun frames(source: String): List<String> {
        require(source.length <= MAX_SOURCE)
        val whole = runCatching { Json.parseToJsonElement(source) }.getOrNull()
        val messages = when (whole) {
            is JsonArray -> whole.map { it.toString() }
            is JsonObject -> listOf(source)
            else -> source.lineSequence().filter(String::isNotBlank).take(MAX_FRAMES + 1).toList()
        }
        require(messages.isNotEmpty() && messages.size <= MAX_FRAMES)
        return messages
    }

    fun format(source: String): String {
        val objects = frames(source).map { Json.parseToJsonElement(it) as JsonObject }
        val formatted = formatter.encodeToString(JsonArray.serializer(), JsonArray(objects))
        require(formatted.length <= MAX_SOURCE)
        return formatted
    }
}
