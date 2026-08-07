package com.wanbaohe.a2ui.catalog

import com.wanbaohe.a2ui.state.A2uiActionBus
import com.wanbaohe.a2ui.state.A2uiSurfaceHolder
import com.wanbaohe.a2ui.ui.A2uiViewerContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class A2uiRenderProvider @Inject constructor(
    val surfaceHolder: A2uiSurfaceHolder,
    val actionBus: A2uiActionBus,
    val registry: A2uiComponentRegistry,
    val themeMapper: A2uiThemeMapper,
) {
    fun viewerContext(): A2uiViewerContext =
        A2uiViewerContext(surfaceHolder, actionBus, themeMapper, registry)
}
