package com.wanbaohe.a2ui.domain

import com.wanbaohe.a2ui.domain.model.DynamicValue
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

object A2uiJsonPointerResolver {

    fun resolve(path: String, dataModel: JsonObject): JsonElement? {
        if (path.isEmpty() || path == "/") return dataModel
        if (!path.startsWith("/")) return null
        val tokens = tokenize(path)
        if (tokens.isEmpty()) return dataModel

        var current: JsonElement? = dataModel
        for (token in tokens) {
            current = when (current) {
                is JsonObject -> current[token]
                is JsonArray -> token.toIntOrNull()?.let { idx ->
                    current.getOrNull(idx)
                }
                else -> null
            } ?: return null
        }
        return current
    }

    fun resolveDynamic(
        dynamic: DynamicValue,
        dataModel: JsonObject,
        functionInvoker: ((String, List<DynamicValue>) -> JsonElement?)? = null,
    ): JsonElement? = when (dynamic) {
        is DynamicValue.Literal -> dynamic.value
        is DynamicValue.Pointer -> resolve(dynamic.path, dataModel)
        is DynamicValue.Function -> functionInvoker?.invoke(dynamic.name, dynamic.arguments)
    }

    fun resolveString(
        dynamic: DynamicValue,
        dataModel: JsonObject,
        functionInvoker: ((String, List<DynamicValue>) -> JsonElement?)? = null,
    ): String? = resolveDynamic(dynamic, dataModel, functionInvoker)
        ?.jsonPrimitive
        ?.contentOrNull

    fun resolveBoolean(
        dynamic: DynamicValue,
        dataModel: JsonObject,
        functionInvoker: ((String, List<DynamicValue>) -> JsonElement?)? = null,
    ): Boolean? = resolveDynamic(dynamic, dataModel, functionInvoker)
        ?.jsonPrimitive
        ?.contentOrNull
        ?.let { it == "true" }

    fun resolveInt(
        dynamic: DynamicValue,
        dataModel: JsonObject,
        functionInvoker: ((String, List<DynamicValue>) -> JsonElement?)? = null,
    ): Int? = resolveDynamic(dynamic, dataModel, functionInvoker)
        ?.jsonPrimitive
        ?.intOrNull

    fun resolveLong(
        dynamic: DynamicValue,
        dataModel: JsonObject,
        functionInvoker: ((String, List<DynamicValue>) -> JsonElement?)? = null,
    ): Long? = resolveDynamic(dynamic, dataModel, functionInvoker)
        ?.jsonPrimitive
        ?.longOrNull

    fun resolveFloat(
        dynamic: DynamicValue,
        dataModel: JsonObject,
        functionInvoker: ((String, List<DynamicValue>) -> JsonElement?)? = null,
    ): Float? = resolveDynamic(dynamic, dataModel, functionInvoker)
        ?.jsonPrimitive
        ?.contentOrNull
        ?.toFloatOrNull()

    fun upsert(path: String, value: JsonElement?, dataModel: JsonObject): JsonObject {
        if (path.isEmpty() || path == "/") {
            return if (value is JsonObject) value else dataModel
        }
        if (!path.startsWith("/")) return dataModel
        val tokens = tokenize(path)
        if (tokens.isEmpty()) return if (value is JsonObject) value else dataModel
        return if (value == null) {
            removeTokens(tokens, dataModel)
        } else {
            upsertTokens(tokens, value, dataModel)
        }
    }

    private fun tokenize(path: String): List<String> {
        if (path.length == 1) return emptyList()
        return path.substring(1).split("/").map { unescape(it) }
    }

    private fun unescape(token: String): String = token
        .replace("~1", "/")
        .replace("~0", "~")

    private fun upsertTokens(tokens: List<String>, value: JsonElement, current: JsonElement): JsonObject {
        if (tokens.isEmpty()) return value as? JsonObject ?: (current as? JsonObject ?: JsonObject(emptyMap()))
        val first = tokens.first()
        val rest = tokens.drop(1)
        val obj = current as? JsonObject ?: JsonObject(emptyMap())
        val existing = obj[first] ?: JsonObject(emptyMap())
        val updated = if (rest.isEmpty()) {
            value
        } else {
            upsertTokens(rest, value, existing)
        }
        return JsonObject(obj.toMutableMap().apply { put(first, updated) })
    }

    private fun removeTokens(tokens: List<String>, current: JsonElement): JsonObject {
        if (tokens.isEmpty()) return current as? JsonObject ?: JsonObject(emptyMap())
        val first = tokens.first()
        val rest = tokens.drop(1)
        val obj = current as? JsonObject ?: return JsonObject(emptyMap())
        val existing = obj[first] ?: return obj
        val updated = if (rest.isEmpty()) {
            null
        } else {
            removeTokens(rest, existing)
        }
        return JsonObject(obj.toMutableMap().apply {
            if (updated == null) remove(first) else put(first, updated)
        })
    }

    fun toAbsolutePath(path: String, scopePrefix: String?): String = when {
        path.startsWith("/") -> path
        path == "@index" -> "@index"
        scopePrefix.isNullOrEmpty() -> "/$path"
        else -> "$scopePrefix/$path"
    }
}
