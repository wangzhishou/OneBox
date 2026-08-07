package com.wanbaohe.a2ui.ui

import androidx.compose.runtime.Immutable
import com.wanbaohe.a2ui.catalog.A2uiComponentRegistry
import com.wanbaohe.a2ui.catalog.A2uiThemeMapper
import com.wanbaohe.a2ui.state.A2uiActionBus
import com.wanbaohe.a2ui.state.A2uiSurfaceHolder

@Immutable
data class A2uiViewerContext(
    val surfaceHolder: A2uiSurfaceHolder,
    val actionBus: A2uiActionBus,
    val themeMapper: A2uiThemeMapper,
    val registry: A2uiComponentRegistry,
)
