package com.wanbaohe.dynamicui.renderer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.wanbaohe.dynamicui.ir.ActionSpec
import com.wanbaohe.dynamicui.ir.UiNode
import com.wanbaohe.dynamicui.modifier.ModifierPipeline
import com.wanbaohe.dynamicui.state.UiStateScope
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NodeRenderer – the composable factory for a single component type.
 *
 * Implement this interface to render a custom component and register it via
 * [ComponentRegistry.register] or Hilt @IntoMap injection.
 */
fun interface NodeRenderer {
    @Composable
    fun Render(
        node: UiNode,
        scope: UiStateScope,
        context: RenderContext,
        itemContext: Map<String, Any?>,
    )
}

/**
 * RenderContext – read-only snapshot of render-time dependencies injected into
 * every [NodeRenderer]. Passed down the tree without requiring CompositionLocals
 * for the hot path.
 */
@Stable
data class RenderContext(
    val registry: ComponentRegistry,
    val modifierPipeline: ModifierPipeline,
    val actionDispatcher: (ActionSpec, UiStateScope, RenderContext, Map<String, Any?>) -> Unit,
)

/**
 * ComponentRegistry – the open registry of [NodeRenderer] factories.
 *
 * Renderers added via [register] override built-in ones of the same type name,
 * allowing full customisation without forking the engine.
 */
@Singleton
class ComponentRegistry @Inject constructor() {

    private val renderers = mutableMapOf<String, NodeRenderer>()

    /**
     * Register a renderer for a component type.
     * Calling this again with the same type overwrites the previous renderer.
     * Registration should be static. Per-render state belongs in [RenderContext].
     */
    fun register(type: String, renderer: NodeRenderer) {
        renderers[type] = renderer
    }

    fun rendererFor(type: String): NodeRenderer? = renderers[type]

    fun registeredTypes(): Set<String> = renderers.keys.toSet()
}
