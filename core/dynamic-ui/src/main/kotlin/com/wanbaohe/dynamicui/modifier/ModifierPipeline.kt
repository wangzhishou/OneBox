package com.wanbaohe.dynamicui.modifier

import android.util.LruCache
import androidx.compose.ui.Modifier
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ModifierPipeline – converts a list of modifier expression strings into a single
 * chained Compose [Modifier].
 *
 * Usage in renderers:
 * ```kotlin
 * val mod = pipeline.build(node.modifier)
 * Column(modifier = mod) { ... }
 * ```
 */
@Singleton
class ModifierPipeline @Inject constructor(
    private val registry: ModifierRegistry,
) {

    /**
     * Build a chained [Modifier] from an ordered list of expression strings.
     * Entries are applied in declaration order, matching Compose's left-to-right chain.
     * 
     * Note: Modifier instances are not cached here globally to avoid state pollution
     * for stateful modifiers (like clickable or pointerInput) across multiple nodes.
     * The expression parsing (String -> tokens) is cached inside [ModifierRegistry].
     */
    fun build(expressions: List<String>): Modifier {
        if (expressions.isEmpty()) return Modifier
        return expressions.fold(Modifier as Modifier) { acc, expr ->
            acc.then(registry.parse(expr))
        }
    }

    /** Convenience: build from a single expression string. */
    fun parse(expr: String): Modifier = registry.parse(expr)
}

