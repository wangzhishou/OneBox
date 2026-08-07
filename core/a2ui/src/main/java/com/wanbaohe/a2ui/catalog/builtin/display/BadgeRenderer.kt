package com.wanbaohe.a2ui.catalog.builtin.display

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassBadge
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import javax.inject.Inject

class BadgeRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "Badge"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val text = context.resolveString(component.properties["text"]) ?: ""
        val style = context.resolveString(component.properties["style"])
        val containerColor = mapBadgeColor(style)
        val contentColor = if (containerColor.luminance() > 0.5f) Color.Black else Color.White

        GlassBadge(
            containerColor = containerColor,
            contentColor = contentColor,
        ) {
            Text(text = text)
        }
    }

    @Composable
    private fun mapBadgeColor(style: String?): Color = when (style?.lowercase()) {
        "error" -> MaterialTheme.colorScheme.error
        "success" -> MaterialTheme.colorScheme.primary
        "warning" -> MaterialTheme.colorScheme.tertiary
        "info" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }
}
