package com.wanbaohe.a2ui.catalog.builtin.input

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSegmentedButtonRow
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import com.wanbaohe.a2ui.domain.model.DynamicValue
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class ChoicePickerRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "ChoicePicker"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val options = parseOptions(context.resolveDynamic(component.properties["options"]))
        val selectedDynamic = component.properties["selected"] ?: component.properties["value"]
        val selectedValue = context.resolveString(selectedDynamic) ?: options.firstOrNull()?.value ?: ""
        val pointerPath = (selectedDynamic as? DynamicValue.Pointer)?.path

        if (options.isEmpty()) return

        GlassSegmentedButtonRow(
            options = options.map { it.value },
            selectedOption = selectedValue,
            onOptionSelected = { option ->
                pointerPath?.let { context.updateDataModel(it, JsonPrimitive(option)) }
            },
            modifier = Modifier,
            label = { option -> Text(options.find { it.value == option }?.label ?: option) },
        )
    }

    private fun parseOptions(json: JsonElement?): List<ChoiceOption> {
        if (json == null) return emptyList()
        return runCatching {
            when (json) {
                is JsonArray -> json.mapNotNull { element ->
                    when (element) {
                        is JsonPrimitive -> element.contentOrNull?.let { ChoiceOption(it, it) }
                        is JsonObject -> {
                            val label = element["label"]?.jsonPrimitive?.contentOrNull
                            val value = element["value"]?.jsonPrimitive?.contentOrNull
                            if (label != null && value != null) ChoiceOption(label, value) else null
                        }
                        else -> null
                    }
                }
                is JsonPrimitive -> json.contentOrNull?.let { listOf(ChoiceOption(it, it)) } ?: emptyList()
                else -> emptyList()
            }
        }.getOrDefault(emptyList())
    }

    private data class ChoiceOption(
        val label: String,
        val value: String,
    )
}
