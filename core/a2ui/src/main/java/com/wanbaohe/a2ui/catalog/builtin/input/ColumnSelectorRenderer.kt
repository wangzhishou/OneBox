package com.wanbaohe.a2ui.catalog.builtin.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import javax.inject.Inject
import com.t8rin.imagetoolbox.core.resources.icons.CheckCircle
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCheckBoxBlank

class ColumnSelectorRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "ColumnSelector"

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

        val state = rememberSelectorState(
            component = component,
            context = context,
            options = options,
            defaultMaxSelected = 1,
        )

        val label = context.resolveString(component.properties["label"])
        val enabled = context.resolveBoolean(component.properties["enabled"]) ?: true
        val spacing = context.resolveInt(component.properties["spacing"])?.dp ?: 8.dp
        val padding = context.resolveInt(component.properties["padding"])?.dp ?: 0.dp
        val textColor = MaterialTheme.colorScheme.onSurface
        val selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
        val containerColor = MaterialTheme.colorScheme.surfaceContainer
        val selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
        val iconColor = MaterialTheme.colorScheme.primary

        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        Column(
            modifier = Modifier
                .padding(padding),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(spacing),
        ) {
            options.forEach { option ->
                val isSelected = option.value in state.selectedValues
                GlassFilterChip(
                    selected = isSelected,
                    onClick = { state.toggle(option) },
                    enabled = enabled,
                    label = {
                        Text(
                            text = option.label,
                            color = if (isSelected) selectedTextColor else textColor,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isSelected) {
                                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.CheckCircle
                            } else {
                                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCheckBoxBlank
                            },
                            contentDescription = option.label,
                            tint = iconColor,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    glassContainerColor = containerColor,
                    glassSelectedContainerColor = selectedContainerColor,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (state.showCustomInput) {
            children()
        }
    }
}
