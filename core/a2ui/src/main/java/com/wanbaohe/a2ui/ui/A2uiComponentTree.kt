package com.wanbaohe.a2ui.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wanbaohe.a2ui.catalog.A2uiComponentRegistry
import com.wanbaohe.a2ui.catalog.A2uiFunctionInvoker
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.catalog.A2uiThemeMapper
import com.wanbaohe.a2ui.catalog.RenderScope
import com.wanbaohe.a2ui.domain.A2uiJsonPointerResolver
import com.wanbaohe.a2ui.domain.model.ChildList
import com.wanbaohe.a2ui.state.A2uiActionBus
import com.wanbaohe.a2ui.state.A2uiSurfaceState
import kotlinx.serialization.json.JsonArray

private const val MAX_DEPTH = 128

@Composable
fun A2uiComponentTree(
    componentId: String,
    surfaceState: A2uiSurfaceState,
    actionBus: A2uiActionBus,
    themeMapper: A2uiThemeMapper,
    registry: A2uiComponentRegistry,
    modifier: Modifier = Modifier,
    depth: Int = 0,
) {
    var contextRef: A2uiRenderContext? = null
    val context = A2uiRenderContext(
        surfaceState = surfaceState,
        actionBus = actionBus,
        themeMapper = themeMapper,
        registry = registry,
        renderChild = { childId ->
            contextRef?.let { ctx ->
                RenderComponent(
                    componentId = childId,
                    context = ctx,
                    depth = depth + 1,
                )
            }
        },
        functionInvoker = A2uiFunctionInvoker.Default,
    )
    contextRef = context

    RenderComponent(
        componentId = componentId,
        context = context,
        modifier = modifier,
        depth = depth,
    )
}

@Composable
private fun RenderComponent(
    componentId: String,
    context: A2uiRenderContext,
    modifier: Modifier = Modifier,
    depth: Int = 0,
) {
    if (depth > MAX_DEPTH) return

    val component = context.surfaceState.components[componentId] ?: return
    val renderer = context.registry.get(component.type)

    if (renderer == null) {
        Box(modifier = modifier) {
            component.childIds.forEach { childId ->
                RenderComponent(
                    componentId = childId,
                    context = context,
                    depth = depth + 1,
                )
            }
        }
        return
    }

    renderer.Render(
        component = component,
        context = context,
        children = {
            RenderChildren(
                children = component.children,
                context = context,
                depth = depth,
            )
        },
    )
}

@Composable
private fun RenderChildren(
    children: ChildList,
    context: A2uiRenderContext,
    depth: Int,
) {
    if (depth > MAX_DEPTH) return

    when (children) {
        is ChildList.Array -> {
            children.children.forEach { childId ->
                RenderComponent(
                    componentId = childId,
                    context = context,
                    depth = depth + 1,
                )
            }
        }

        is ChildList.Template -> {
            val data = A2uiJsonPointerResolver.resolve(children.path, context.surfaceState.dataModel)
            val array = data as? JsonArray ?: return
            array.forEachIndexed { index, _ ->
                val scopedContext = context.withScope(
                    RenderScope(
                        prefix = "${children.path}/${index}",
                        index = index,
                    )
                )
                RenderComponent(
                    componentId = children.componentId,
                    context = scopedContext,
                    depth = depth + 1,
                )
            }
        }
    }
}
