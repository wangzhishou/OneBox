package com.wanbaohe.a2ui.catalog.builtin.input

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCustomSlider
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import com.wanbaohe.a2ui.domain.model.DynamicValue
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject

class SliderRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "Slider"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val valueDynamic = component.properties["value"]
        val value = context.resolveFloat(valueDynamic) ?: 0f
        val pointerPath = (valueDynamic as? DynamicValue.Pointer)?.path
        val min = context.resolveFloat(component.properties["min"]) ?: 0f
        val max = context.resolveFloat(component.properties["max"]) ?: 100f
        val steps = context.resolveInt(component.properties["steps"]) ?: 0
        val enabled = context.resolveBoolean(component.properties["enabled"]) ?: true
        val label = context.resolveString(component.properties["label"])

        if (!label.isNullOrBlank()) {
            Text(text = "$label: ${value.toInt()}")
        }

        GlassCustomSlider(
            value = value,
            onValueChange = { newValue ->
                pointerPath?.let {
                    val primitive = if (newValue % 1f == 0f) {
                        JsonPrimitive(newValue.toInt())
                    } else {
                        JsonPrimitive(newValue)
                    }
                    context.updateDataModel(it, primitive)
                }
            },
            modifier = Modifier,
            enabled = enabled,
            valueRange = min..max,
            steps = steps,
        )
    }
}
