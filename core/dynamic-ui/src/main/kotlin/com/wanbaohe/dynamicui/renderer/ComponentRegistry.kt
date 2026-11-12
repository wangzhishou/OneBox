/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
