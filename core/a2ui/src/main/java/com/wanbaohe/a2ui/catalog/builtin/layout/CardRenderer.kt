package com.wanbaohe.a2ui.catalog.builtin.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import javax.inject.Inject

class CardRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "Card"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val padding = context.resolveInt(component.properties["padding"])?.dp ?: 16.dp
        val horizontalAlignment = parseHorizontalAlignment(context.resolveString(component.properties["alignment"]))

        GlassCard {
            Column(
                modifier = Modifier.padding(padding),
                horizontalAlignment = horizontalAlignment,
            ) {
                children()
            }
        }
    }
}
