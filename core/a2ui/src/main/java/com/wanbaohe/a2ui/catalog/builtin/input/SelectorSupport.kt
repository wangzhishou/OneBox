package com.wanbaohe.a2ui.catalog.builtin.input

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import com.wanbaohe.a2ui.domain.model.DynamicValue
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal data class SelectorOption(
    val value: String,
    val label: String,
    val kind: String = "text",
)

internal fun parseSelectorOptions(json: JsonElement?): List<SelectorOption> {
    if (json == null) return emptyList()
    return runCatching {
        when (json) {
            is JsonArray -> json.mapNotNull { element ->
                when (element) {
                    is JsonPrimitive -> element.contentOrNull?.let { SelectorOption(it, it) }
                    is JsonObject -> {
                        val label = element["label"]?.jsonPrimitive?.contentOrNull
                        val value = element["value"]?.jsonPrimitive?.contentOrNull
                        val kind = element["kind"]?.jsonPrimitive?.contentOrNull ?: "text"
                        if (label != null && value != null) SelectorOption(value, label, kind) else null
                    }
                    else -> null
                }
            }
            is JsonPrimitive -> json.contentOrNull?.let { listOf(SelectorOption(it, it)) } ?: emptyList()
            else -> emptyList()
        }
    }.getOrDefault(emptyList())
}

internal class SelectorState(
    val selectedValues: List<String>,
    val showCustomInput: Boolean,
    val toggle: (SelectorOption) -> Unit,
)

@Composable
internal fun rememberSelectorState(
    component: A2uiComponent,
    context: A2uiRenderContext,
    options: List<SelectorOption>,
    defaultMaxSelected: Int,
): SelectorState {
    val maxSelected = context.resolveInt(component.properties["maxSelected"]) ?: defaultMaxSelected
    val valueDynamic = component.properties["value"] ?: component.properties["selected"]
    val pointerPath = (valueDynamic as? DynamicValue.Pointer)?.path

    val currentElement by remember(valueDynamic) {
        derivedStateOf { valueDynamic?.let { context.resolveDynamic(it) } }
    }

    val selectedValues by remember(currentElement) {
        derivedStateOf { parseSelectedValues(currentElement) }
    }

    val showCustomInput by remember(selectedValues, options) {
        derivedStateOf {
            options.any { it.kind == "custom" && it.value in selectedValues }
        }
    }

    val selectIndex = context.resolveInt(component.properties["selectIndex"]) ?: -1
    LaunchedEffect(selectIndex, options, selectedValues, pointerPath) {
        if (selectedValues.isNotEmpty() || selectIndex !in options.indices || pointerPath == null) {
            return@LaunchedEffect
        }
        val option = options[selectIndex]
        val newValue = if (maxSelected == 1) {
            JsonPrimitive(option.value)
        } else {
            JsonArray(listOf(JsonPrimitive(option.value)))
        }
        context.updateDataModel(pointerPath, newValue)
    }

    val toggle: (SelectorOption) -> Unit = { option ->
        if (pointerPath != null) {
            val isSelected = option.value in selectedValues
            val newValues = if (isSelected) {
                selectedValues - option.value
            } else {
                when {
                    maxSelected == 1 -> listOf(option.value)
                    maxSelected > 1 && selectedValues.size >= maxSelected -> selectedValues.drop(1) + option.value
                    else -> selectedValues + option.value
                }
            }
            val jsonValue = when {
                newValues.isEmpty() -> JsonNull
                maxSelected == 1 -> JsonPrimitive(newValues.first())
                else -> JsonArray(newValues.map { JsonPrimitive(it) })
            }
            context.updateDataModel(pointerPath, jsonValue)
        }
    }

    return SelectorState(
        selectedValues = selectedValues,
        showCustomInput = showCustomInput,
        toggle = toggle,
    )
}

private fun parseSelectedValues(element: JsonElement?): List<String> = when (element) {
    is JsonArray -> element.mapNotNull { it.jsonPrimitive.contentOrNull }
    is JsonPrimitive -> element.contentOrNull?.let { listOf(it) } ?: emptyList()
    else -> emptyList()
}
