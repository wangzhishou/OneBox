package com.wanbaohe.a2ui.catalog.builtin.layout

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import javax.inject.Inject

class SpacerRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "Spacer"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val width = context.resolveInt(component.properties["width"])?.dp ?: 0.dp
        val height = context.resolveInt(component.properties["height"])?.dp ?: 0.dp
        Spacer(modifier = Modifier.size(width = width, height = height))
    }
}
