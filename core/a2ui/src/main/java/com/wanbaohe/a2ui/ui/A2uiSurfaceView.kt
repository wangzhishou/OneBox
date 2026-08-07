package com.wanbaohe.a2ui.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCircularProgressIndicator

private val scrollableRootTypes = setOf("Column", "Card", "Row")

@Composable
fun A2uiSurfaceView(
    surfaceId: String,
    viewerContext: A2uiViewerContext,
    modifier: Modifier = Modifier,
) {
    val surfaceState = viewerContext.surfaceHolder.getOrCreate(surfaceId)
    val rootId = surfaceState.rootComponentId

    if (rootId == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            GlassCircularProgressIndicator()
        }
        return
    }

    val rootType = surfaceState.components[rootId]?.type
    val isScrollableRoot = rootType in scrollableRootTypes

    Box(
        modifier = if (isScrollableRoot) {
            modifier.verticalScroll(rememberScrollState())
        } else {
            modifier
        },
    ) {
        A2uiComponentTree(
            componentId = rootId,
            surfaceState = surfaceState,
            actionBus = viewerContext.actionBus,
            themeMapper = viewerContext.themeMapper,
            registry = viewerContext.registry,
        )
    }
}
