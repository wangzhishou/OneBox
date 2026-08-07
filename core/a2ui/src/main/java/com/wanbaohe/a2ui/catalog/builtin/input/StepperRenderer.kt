package com.wanbaohe.a2ui.catalog.builtin.input

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStepper
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import com.wanbaohe.a2ui.domain.model.DynamicValue
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject

class StepperRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "Stepper"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val valueDynamic = component.properties["value"]
        val value = context.resolveInt(valueDynamic) ?: 0
        val pointerPath = (valueDynamic as? DynamicValue.Pointer)?.path
        val min = context.resolveInt(component.properties["min"]) ?: 0
        val max = context.resolveInt(component.properties["max"]) ?: 100
        val step = context.resolveInt(component.properties["step"]) ?: 1
        val enabled = context.resolveBoolean(component.properties["enabled"]) ?: true
        val label = context.resolveString(component.properties["label"])

        if (!label.isNullOrBlank()) {
            Text(text = "$label: $value")
        }

        GlassStepper(
            value = value,
            onValueChange = { newValue ->
                pointerPath?.let { context.updateDataModel(it, JsonPrimitive(newValue)) }
            },
            valueRange = min..max,
            step = step,
            enabled = enabled,
        )
    }
}
