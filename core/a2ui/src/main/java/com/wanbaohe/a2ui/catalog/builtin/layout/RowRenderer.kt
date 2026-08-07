package com.wanbaohe.a2ui.catalog.builtin.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import javax.inject.Inject

class RowRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "Row"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val spacing = context.resolveInt(component.properties["spacing"])?.dp ?: 0.dp
        val padding = context.resolveInt(component.properties["padding"])?.dp ?: 0.dp
        val styleStr = context.resolveString(component.properties["style"])
        val glassStyle = context.themeMapper.mapGlassStyle(styleStr)
        val verticalAlignment = parseVerticalAlignment(context.resolveString(component.properties["alignment"]))

        Row(
            modifier = Modifier
                .padding(padding)
                .then(if (styleStr != null) Modifier.glassBackground(style = glassStyle) else Modifier),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = verticalAlignment,
        ) {
            children()
        }
    }
}
