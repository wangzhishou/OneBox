package com.wanbaohe.a2ui.catalog.builtin.input

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import com.wanbaohe.a2ui.domain.model.DynamicValue
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject

class TextFieldRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "TextField"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val valueDynamic = component.properties["value"]
        val currentValue = context.resolveString(valueDynamic) ?: ""
        val pointerPath = (valueDynamic as? DynamicValue.Pointer)?.path
        val label = context.resolveString(component.properties["label"])
        val placeholder = context.resolveString(component.properties["placeholder"])
        val enabled = context.resolveBoolean(component.properties["enabled"]) ?: true
        val isError = context.resolveBoolean(component.properties["error"]) ?: false
        val supportingText = context.resolveString(component.properties["supportingText"])
        val singleLine = context.resolveBoolean(component.properties["singleLine"]) ?: false
        val maxLines = context.resolveInt(component.properties["maxLines"])
        val minLines = context.resolveInt(component.properties["minLines"])

        GlassOutlinedTextField(
            value = currentValue,
            onValueChange = { newValue ->
                pointerPath?.let { context.updateDataModel(it, JsonPrimitive(newValue)) }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            label = label.takeIf { !it.isNullOrBlank() }?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it) } },
            isError = isError,
            supportingText = supportingText?.let { { Text(it) } },
            singleLine = singleLine,
            maxLines = maxLines ?: if (singleLine) 1 else Int.MAX_VALUE,
            minLines = minLines ?: 1,
        )
    }
}
