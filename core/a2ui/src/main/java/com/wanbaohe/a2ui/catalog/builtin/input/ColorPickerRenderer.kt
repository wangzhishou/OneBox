package com.wanbaohe.a2ui.catalog.builtin.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorSelectionRow
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorSelectionRowDefaults
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

class ColorPickerRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "ColorPicker"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val valueDynamic = component.properties["value"]
        val selectedColor = context.resolveString(valueDynamic)
            ?.takeIf { it.isNotBlank() }
            ?.let(::parseColor)
            ?: Color.Black
        val pointerPath = (valueDynamic as? DynamicValue.Pointer)?.path
        val label = context.resolveString(component.properties["label"])
        val colors = parseColors(context.resolveDynamic(component.properties["colors"]))
            .takeIf { it.isNotEmpty() }
            ?: ColorSelectionRowDefaults.colorList
        val allowAlpha = context.resolveBoolean(component.properties["allowAlpha"]) ?: false
        val enabled = context.resolveBoolean(component.properties["enabled"]) ?: true

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else 0.5f),
        ) {
            if (!label.isNullOrBlank()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            ColorSelectionRow(
                value = selectedColor,
                onValueChange = { color ->
                    pointerPath?.let {
                        context.updateDataModel(it, JsonPrimitive(color.toHex(allowAlpha)))
                    }
                },
                defaultColors = colors,
                allowAlpha = allowAlpha,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    private fun parseColors(json: JsonElement?): List<Color> {
        if (json == null) return emptyList()
        return runCatching {
            (json as? JsonArray)?.mapNotNull { element ->
                runCatching {
                    element.jsonPrimitive.contentOrNull?.let(::parseColor)
                }.getOrNull()
            } ?: emptyList()
        }.getOrDefault(emptyList())
    }

    private fun parseColor(hex: String): Color = runCatching {
        Color(android.graphics.Color.parseColor(hex))
    }.getOrDefault(Color.Gray)

    private fun Color.toHex(includeAlpha: Boolean): String {
        val argb = toArgb()
        return if (includeAlpha) {
            String.format("#%08X", argb.toLong() and 0xFFFFFFFF)
        } else {
            String.format("#%06X", argb and 0x00FFFFFF)
        }
    }
}
