package com.shifenmiao.ai.utils

import com.wanbaohe.a2ui.A2uiContentParser
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

object PromptBuildUtils {

    fun buildPromptString(json: String): String? {
        if (json.isBlank()) return null
        return try {
            val message = A2uiContentParser.parse(json) ?: return null
            val submitAction = message.components.firstOrNull { it.action != null }?.action
            val template = submitAction?.event?.context
                ?.get("prompt")?.jsonPrimitive?.contentOrNull
            val result = if (template != null) {
                template.replace(Regex("\\$\\{(\\w+)}")) { match ->
                    resolveValue(message.dataModel[match.groupValues[1]]) ?: ""
                }
            } else {
                message.dataModel.entries
                    .mapNotNull { (key, value) ->
                        resolveValue(value)
                            ?.takeIf { it.isNotBlank() }
                            ?.let { "$key: $it" }
                    }
                    .joinToString("")
            }
            result.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    private fun resolveValue(element: kotlinx.serialization.json.JsonElement?): String? = when (element) {
        is JsonPrimitive -> element.contentOrNull
        is JsonArray -> element.mapNotNull { resolveValue(it) }
            .joinToString(", ")
            .takeIf { it.isNotBlank() }
            ?: element.toString()
        is JsonObject -> element.toString()
        else -> null
    }
}
