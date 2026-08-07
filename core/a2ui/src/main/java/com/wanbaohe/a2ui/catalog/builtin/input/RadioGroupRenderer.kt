package com.wanbaohe.a2ui.catalog.builtin.input

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassRadioButton
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import com.wanbaohe.a2ui.domain.model.DynamicValue
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class RadioGroupRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "RadioGroup"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val valueDynamic = component.properties["value"]
        val selectedValue = context.resolveString(valueDynamic) ?: ""
        val pointerPath = (valueDynamic as? DynamicValue.Pointer)?.path
        val label = context.resolveString(component.properties["label"])
        val options = parseOptions(context.resolveDynamic(component.properties["options"]))
        val enabled = context.resolveBoolean(component.properties["enabled"]) ?: true
        val spacing = context.resolveInt(component.properties["spacing"])?.dp ?: 0.dp

        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = enabled) {
                            pointerPath?.let {
                                context.updateDataModel(it, JsonPrimitive(option))
                            }
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GlassRadioButton(
                        selected = option == selectedValue,
                        onClick = null,
                        enabled = enabled,
                    )
                    Text(text = option)
                }
            }
        }
    }

    private fun parseOptions(json: JsonElement?): List<String> {
        if (json == null) return emptyList()
        return runCatching {
            (json as? JsonArray)?.mapNotNull { element ->
                runCatching { element.jsonPrimitive.contentOrNull }.getOrNull()
            } ?: emptyList()
        }.getOrDefault(emptyList())
    }
}
