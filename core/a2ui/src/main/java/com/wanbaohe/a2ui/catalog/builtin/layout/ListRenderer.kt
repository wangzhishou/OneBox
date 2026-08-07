package com.wanbaohe.a2ui.catalog.builtin.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import javax.inject.Inject

class ListRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "List"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val spacing = context.resolveInt(component.properties["spacing"])?.dp ?: 0.dp
        val padding = context.resolveInt(component.properties["padding"])?.dp ?: 0.dp
        val horizontalAlignment = parseHorizontalAlignment(context.resolveString(component.properties["alignment"]))
        val childIds = component.childIds

        LazyColumn(
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalAlignment = horizontalAlignment,
        ) {
            if (childIds.isNotEmpty()) {
                items(childIds, key = { it }) { childId ->
                    context.renderChild(childId)
                }
            } else {
                item {
                    children()
                }
            }
        }
    }
}
