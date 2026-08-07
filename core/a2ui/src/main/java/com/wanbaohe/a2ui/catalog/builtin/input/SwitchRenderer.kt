package com.wanbaohe.a2ui.catalog.builtin.input

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import com.wanbaohe.a2ui.domain.model.DynamicValue
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject

class SwitchRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "Switch"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val checkedDynamic = component.properties["checked"]
        val checked = context.resolveBoolean(checkedDynamic) ?: false
        val pointerPath = (checkedDynamic as? DynamicValue.Pointer)?.path
        val label = context.resolveString(component.properties["label"])
        val enabled = context.resolveBoolean(component.properties["enabled"]) ?: true

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassSwitch(
                checked = checked,
                onCheckedChange = { newValue ->
                    pointerPath?.let { context.updateDataModel(it, JsonPrimitive(newValue)) }
                },
                enabled = enabled,
            )
            if (!label.isNullOrBlank()) {
                Text(text = label)
            }
        }
    }
}
