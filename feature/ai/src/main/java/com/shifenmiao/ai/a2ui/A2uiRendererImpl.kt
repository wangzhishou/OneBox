package com.shifenmiao.ai.a2ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.halilibo.richtext.ui.a2ui.A2uiRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderProvider
import com.wanbaohe.a2ui.ui.A2uiDynamicContent

class A2uiRendererImpl(
    private val renderProvider: A2uiRenderProvider,
) : A2uiRenderer {

    @Composable
    override fun RenderA2ui(
        json: String,
        modifier: Modifier,
        onSubmit: ((formData: String) -> Unit)?
    ) {
        A2uiDynamicContent(
            json = json,
            onSubmit = onSubmit,
            renderProvider = renderProvider,
            modifier = modifier,
        )
    }
}
