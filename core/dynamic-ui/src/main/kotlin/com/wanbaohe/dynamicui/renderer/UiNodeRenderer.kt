package com.wanbaohe.dynamicui.renderer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import android.util.Log
import com.wanbaohe.dynamicui.action.DynamicUiInternalState
import com.wanbaohe.dynamicui.ir.UiNode
import com.wanbaohe.dynamicui.state.UiStateScope
import com.wanbaohe.dynamicui.state.ValueExprResolver

/**
 * UiNodeRenderer – the recursive Compose entry-point for rendering any [UiNode].
 *
 * ## Flow
 * 1. Evaluate `node.visibleExpr` via [derivedStateOf] (no recompose if unchanged).
 * 2. If invisible → return early.
 * 3. Look up the renderer in [RenderContext.registry].
 * 4. Delegate rendering; unknown types show a debug placeholder in DEBUG builds.
 *
 * Recursion happens inside individual renderers when they call [UiNodeRenderer.Render]
 * on their own `children`.
 */
object UiNodeRenderer {

    @Composable
    fun Render(
        node: UiNode,
        scope: UiStateScope,
        context: RenderContext,
        itemContext: Map<String, Any?> = emptyMap(),
    ) {
        // Evaluate visibility. derivedStateOf ensures only this site recomposes when
        // the referenced state key changes.
        val isVisible by remember(node.visibleExpr, node.id, itemContext) {
            derivedStateOf {
                if (node.visibleExpr == null) true
                else ValueExprResolver.resolveBool(node.visibleExpr, scope, itemContext)
            }
        }

        if (!isVisible) return

        val renderer = context.registry.rendererFor(node.type)
        if (renderer != null) {
            // Note: Compose does not support try-catch around @Composable invocations.
            // Defensive validation of node props is done inside individual renderers
            // to prevent runtime crashes from malformed input.
            renderer.Render(node, scope, context, itemContext)
        } else {
            UnknownComponentReport(type = node.type, nodeId = node.id, scope = scope)
            // Unknown component – render fallback in debug mode only
            if (isDebugBuild()) {
                UnknownComponentPlaceholder(type = node.type)
            }
        }
    }

    // ── Debug placeholder ─────────────────────────────────────────────────────
    
    @Composable
    private fun UnknownComponentPlaceholder(type: String) {
        androidx.compose.material3.Text(
            text = "\u26A0 Unknown: $type",
            color = androidx.compose.ui.graphics.Color.Red,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
        )
    }

    @Composable
    private fun UnknownComponentReport(type: String, nodeId: String?, scope: UiStateScope) {
        val message = if (nodeId.isNullOrBlank()) {
            "Unknown component type: $type"
        } else {
            "Unknown component type: $type, nodeId=$nodeId"
        }
        LaunchedEffect(type, nodeId) {
            Log.e("DynamicUi", message)
            scope.setByPath(DynamicUiInternalState.ERROR, message)
            scope.setByPath(DynamicUiInternalState.LEGACY_ERROR, message)
        }
    }
    
    private fun isDebugBuild(): Boolean = try {
        // Checked at runtime; no BuildConfig dependency on this module
        Class.forName("android.os.Debug")
        android.os.Build.TYPE != "user"
    } catch (_: Exception) { false }
}
