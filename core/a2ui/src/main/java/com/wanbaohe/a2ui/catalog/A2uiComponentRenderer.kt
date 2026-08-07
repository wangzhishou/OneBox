package com.wanbaohe.a2ui.catalog

import androidx.compose.runtime.Composable
import com.wanbaohe.a2ui.domain.model.A2uiComponent

interface A2uiComponentRenderer {

    val componentType: String

    @Composable
    fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    )
}
