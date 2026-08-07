package com.wanbaohe.a2ui.catalog.builtin.display

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.icon.IconRegistry
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import javax.inject.Inject
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHelp

class IconRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "Icon"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val iconName = context.resolveString(component.properties["name"])
        val size = context.resolveInt(component.properties["size"])?.dp ?: 24.dp

        Icon(
            imageVector = resolveIcon(iconName),
            contentDescription = context.resolveString(component.properties["description"]),
            modifier = Modifier.size(size),
            tint = context.resolveString(component.properties["color"])
                ?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() }
                ?: Color.Unspecified,
        )
    }

    private fun resolveIcon(name: String?): ImageVector {
        if (name == null) return com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHelp
        return IconRegistry.resolve(name)
            ?: IconRegistry.resolve(name.replaceFirstChar { it.uppercase() })
            ?: com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHelp
    }
}
