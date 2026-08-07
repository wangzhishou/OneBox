package com.wanbaohe.a2ui.catalog.builtin.display

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import javax.inject.Inject

class DividerRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "Divider"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val thickness = context.resolveFloat(component.properties["thickness"])?.dp ?: 1.dp
        val padding = context.resolveInt(component.properties["padding"])?.dp ?: 8.dp

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = padding),
            thickness = thickness,
            color = context.resolveString(component.properties["color"])
                ?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() }
                ?: Color.Unspecified,
        )
    }
}
