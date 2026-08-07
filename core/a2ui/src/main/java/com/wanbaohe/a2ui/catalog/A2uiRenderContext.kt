package com.wanbaohe.a2ui.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.wanbaohe.a2ui.domain.A2uiJsonPointerResolver
import com.wanbaohe.a2ui.domain.model.A2uiAction
import com.wanbaohe.a2ui.domain.model.A2uiFunctionCall
import com.wanbaohe.a2ui.domain.model.DynamicValue
import com.wanbaohe.a2ui.state.A2uiActionBus
import com.wanbaohe.a2ui.state.A2uiSurfaceState
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

@Immutable
class A2uiRenderContext(
    val surfaceState: A2uiSurfaceState,
    val actionBus: A2uiActionBus,
    val themeMapper: A2uiThemeMapper,
    val registry: A2uiComponentRegistry,
    val renderChild: @Composable (String) -> Unit,
    val scopeStack: List<RenderScope> = emptyList(),
    val functionInvoker: A2uiFunctionInvoker = A2uiFunctionInvoker.Empty,
) {
    private val currentScope: RenderScope?
        get() = scopeStack.lastOrNull()

    fun resolveString(dynamic: DynamicValue?): String? =
        dynamic?.let { resolveDynamic(it) }?.let { element ->
            when (element) {
                is JsonNull -> ""
                is JsonPrimitive -> element.contentOrNull
                is JsonArray -> element.toString()
                is JsonObject -> element.toString()
            }
        }

    fun resolveBoolean(dynamic: DynamicValue?): Boolean? =
        (resolveDynamic(dynamic) as? JsonPrimitive)?.contentOrNull?.let { it == "true" }

    fun resolveInt(dynamic: DynamicValue?): Int? =
        (resolveDynamic(dynamic) as? JsonPrimitive)?.intOrNull

    fun resolveFloat(dynamic: DynamicValue?): Float? =
        (resolveDynamic(dynamic) as? JsonPrimitive)?.contentOrNull?.toFloatOrNull()

    fun resolveDynamic(dynamic: DynamicValue?): JsonElement? {
        if (dynamic == null) return null
        val effectiveDataModel = if (currentScope != null) buildScopedDataModel() else surfaceState.dataModel
        return A2uiJsonPointerResolver.resolveDynamic(
            dynamic = dynamic,
            dataModel = effectiveDataModel,
            functionInvoker = { name, args -> functionInvoker.invoke(name, args, this) },
        )
    }

    fun updateDataModel(path: String, value: JsonElement) {
        val absolutePath = A2uiJsonPointerResolver.toAbsolutePath(path, currentScope?.prefix)
        surfaceState.updateDataModelLocal(absolutePath, value)
    }

    fun dispatchAction(sourceComponentId: String, action: A2uiAction) {
        when {
            action.event != null -> {
                actionBus.tryEmit(
                    surfaceId = surfaceState.surfaceId,
                    sourceComponentId = sourceComponentId,
                    action = action,
                    actionId = UUID.randomUUID().toString(),
                )
            }
            action.functionCall != null -> {
                invokeFunctionCall(sourceComponentId, action.functionCall)
            }
        }
    }

    private fun invokeFunctionCall(sourceComponentId: String, functionCall: A2uiFunctionCall) {
        val args = functionCall.args?.let { argsObj ->
            argsObj.mapValues { (_, value) -> DynamicValue.fromJsonElement(value) }.values.toList()
        } ?: emptyList()
        functionInvoker.invoke(functionCall.call, args, this)
    }

    fun withScope(scope: RenderScope): A2uiRenderContext = A2uiRenderContext(
        surfaceState = surfaceState,
        actionBus = actionBus,
        themeMapper = themeMapper,
        registry = registry,
        renderChild = renderChild,
        scopeStack = scopeStack + scope,
        functionInvoker = functionInvoker,
    )

    private fun buildScopedDataModel(): JsonObject {
        val scope = currentScope ?: return surfaceState.dataModel
        val root = surfaceState.dataModel
        val scopedData = A2uiJsonPointerResolver.resolve(scope.prefix, root) as? JsonObject
            ?: JsonObject(emptyMap())
        val indexEntry = scope.index?.let { "@index" to JsonPrimitive(it) }
        return if (indexEntry != null) {
            JsonObject(scopedData.toMutableMap().apply { put(indexEntry.first, indexEntry.second) })
        } else {
            scopedData
        }
    }
}

data class RenderScope(
    val prefix: String,
    val index: Int? = null,
)

fun interface A2uiFunctionInvoker {
    fun invoke(name: String, args: List<DynamicValue>, context: A2uiRenderContext): JsonElement?

    companion object {
        val Empty = A2uiFunctionInvoker { _, _, _ -> null }

        val Default = A2uiFunctionInvoker { name, args, context ->
            when (name) {
                "add" -> {
                    val numbers = args.mapNotNull { context.resolveDynamic(it)?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() }
                    if (numbers.size == args.size && numbers.isNotEmpty()) {
                        JsonPrimitive(numbers.sum())
                    } else null
                }
                "concat" -> {
                    val strings = args.mapNotNull { context.resolveDynamic(it)?.jsonPrimitive?.contentOrNull }
                    if (strings.size == args.size) JsonPrimitive(strings.joinToString("")) else null
                }
                "length" -> {
                    val value = args.firstOrNull()?.let { context.resolveDynamic(it) }
                    when (value) {
                        is JsonArray -> JsonPrimitive(value.size)
                        is JsonPrimitive -> JsonPrimitive(value.contentOrNull?.length ?: 0)
                        else -> JsonPrimitive(0)
                    }
                }
                "not" -> {
                    val value = args.firstOrNull()?.let { context.resolveDynamic(it) }
                    JsonPrimitive(value?.jsonPrimitive?.contentOrNull != "true")
                }
                else -> null
            }
        }
    }
}
