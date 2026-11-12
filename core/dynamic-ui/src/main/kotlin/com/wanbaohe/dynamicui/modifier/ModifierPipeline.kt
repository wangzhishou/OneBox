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

