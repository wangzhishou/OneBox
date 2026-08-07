package com.wanbaohe.a2ui

import com.wanbaohe.a2ui.domain.model.A2uiMessage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

object A2uiContentParser {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = false
    }

    fun parse(
        jsonString: String,
        surfaceId: String = "ai_dynamic",
    ): A2uiMessage.CreateSurface? {
        if (jsonString.isBlank()) return null
        return try {
            val root = json.parseToJsonElement(jsonString).jsonObject

            val surfaceObj = root["createSurface"]?.jsonObject
                ?: root["body"]?.jsonObject
                ?: root

            if (surfaceObj["components"] == null) return null

            val withSurfaceId = JsonObject(
                surfaceObj.toMutableMap().apply {
                    put("surfaceId", JsonPrimitive(surfaceId))
                }
            )

            json.decodeFromJsonElement(A2uiMessage.CreateSurface.serializer(), withSurfaceId)
        } catch (_: Exception) {
            null
        }
    }

    fun isA2uiFormat(jsonString: String): Boolean = parse(jsonString) != null
}
