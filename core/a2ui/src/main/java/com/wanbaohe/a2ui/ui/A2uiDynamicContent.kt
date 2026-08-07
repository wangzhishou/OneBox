package com.wanbaohe.a2ui.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.wanbaohe.a2ui.A2uiContentParser
import com.wanbaohe.a2ui.catalog.A2uiRenderProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

@Composable
fun A2uiDynamicContent(
    json: String,
    onSubmit: ((promptText: String) -> Unit)?,
    renderProvider: A2uiRenderProvider,
    modifier: Modifier = Modifier,
) {
    val surfaceId = remember { "ai_dynamic_${UUID.randomUUID()}" }

    LaunchedEffect(json) {
        val message = withContext(Dispatchers.Default) {
            A2uiContentParser.parse(json, surfaceId)
        }
        if (message != null) {
            renderProvider.surfaceHolder.getOrCreate(surfaceId).applyCreateSurface(message)
        }
    }

    LaunchedEffect(surfaceId) {
        renderProvider.actionBus.events.collect { event ->
            if (event.surfaceId != surfaceId) return@collect
            val actionEvent = event.action.event ?: return@collect
            if (actionEvent.name != "submit") return@collect

            val dataModel = renderProvider.surfaceHolder.get(surfaceId)?.dataModel
                ?: JsonObject(emptyMap())
            val promptText = formatPromptText(dataModel, actionEvent.context)
            if (promptText.isNotBlank()) {
                onSubmit?.invoke(promptText)
            }
        }
    }

    A2uiSurfaceView(
        surfaceId = surfaceId,
        viewerContext = renderProvider.viewerContext(),
        modifier = modifier,
    )

    DisposableEffect(surfaceId) {
        onDispose {
            renderProvider.surfaceHolder.remove(surfaceId)
        }
    }
}

private fun formatPromptText(
    dataModel: JsonObject,
    actionContext: JsonObject?,
): String {
    val template = actionContext?.get("prompt")?.jsonPrimitive?.contentOrNull
    if (template != null) {
        var result: String = template
        dataModel.forEach { (key, value) ->
            result = result.replace("\${$key}", resolveValue(value) ?: "")
        }
        return result
    }
    return dataModel.entries
        .mapNotNull { (key, value) ->
            resolveValue(value)?.takeIf { it.isNotBlank() }?.let { "$key: $it" }
        }
        .joinToString("\n")
}

private fun resolveValue(element: JsonElement?): String? = when (element) {
    is JsonPrimitive -> element.contentOrNull
    is JsonArray -> element.mapNotNull { resolveValue(it) }
        .joinToString(", ")
        .takeIf { it.isNotBlank() }
        ?: element.toString()
    is JsonObject -> element.toString()
    else -> null
}
