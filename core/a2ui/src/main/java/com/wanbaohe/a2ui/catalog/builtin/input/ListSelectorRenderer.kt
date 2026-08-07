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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCheckbox
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassRadioButton
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import javax.inject.Inject

class ListSelectorRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "ListSelector"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val options by remember(component.properties["options"], component.properties["data"]) {
            derivedStateOf {
                parseSelectorOptions(
                    context.resolveDynamic(component.properties["options"] ?: component.properties["data"]),
                )
            }
        }
        if (options.isEmpty()) return

        val maxSelected = context.resolveInt(component.properties["maxSelected"]) ?: 1
        val state = rememberSelectorState(
            component = component,
            context = context,
            options = options,
            defaultMaxSelected = 1,
        )

        val label = context.resolveString(component.properties["label"])
        val enabled = context.resolveBoolean(component.properties["enabled"]) ?: true
        val spacing = context.resolveInt(component.properties["spacing"])?.dp ?: 0.dp
        val padding = context.resolveInt(component.properties["padding"])?.dp ?: 0.dp
        val textColor = MaterialTheme.colorScheme.onSurface
        val selectedTextColor = MaterialTheme.colorScheme.primary

        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        Column(
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            options.forEach { option ->
                val isSelected = option.value in state.selectedValues
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = enabled) { state.toggle(option) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (maxSelected == 1) {
                        GlassRadioButton(
                            selected = isSelected,
                            onClick = null,
                            enabled = enabled,
                        )
                    } else {
                        GlassCheckbox(
                            checked = isSelected,
                            onCheckedChange = null,
                            enabled = enabled,
                        )
                    }
                    Text(
                        text = option.label,
                        color = if (isSelected) selectedTextColor else textColor,
                    )
                }
            }
        }

        if (state.showCustomInput) {
            children()
        }
    }
}
