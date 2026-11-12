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

package com.wanbaohe.markuplayers.domain.history

import com.t8rin.imagetoolbox.core.domain.history.DocumentHistory
import com.t8rin.imagetoolbox.core.filters.presentation.serialization.UiFilterCodec
import com.wanbaohe.markuplayers.domain.model.LayerEffect
import com.wanbaohe.markuplayers.domain.model.LayerMask
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.project.MarkupDocument

/**
 * Snapshot the complete document before an edit, including base edits and export preferences.
 * A gesture can take one pre-edit snapshot and apply any number of transient updates.
 */
class LayerHistory(maxSize: Int = 50) {

    private val history = DocumentHistory<MarkupDocument>(
        maxSize = maxSize,
        freeze = { it.frozen() },
    )

    val canUndo: Boolean get() = history.canUndo
    val canRedo: Boolean get() = history.canRedo

    fun snapshot(document: MarkupDocument) = history.snapshot(document)

    /** Replace a coalesced edit's pre-edit snapshot; an empty history is left unchanged. */
    fun replaceLastSnapshot(document: MarkupDocument): Boolean =
        history.replaceLastSnapshot(document)

    fun undo(current: MarkupDocument): MarkupDocument? = history.undo(current)

    fun redo(current: MarkupDocument): MarkupDocument? = history.redo(current)

    fun clear() = history.clear()

    private fun MarkupDocument.frozen(): MarkupDocument = copy(
        baseEffects = baseEffects.frozenEffects(),
        baseMask = baseMask?.frozen(),
        layers = layers.map { layer ->
            layer.copy(
                type = when (val type = layer.type) {
                    is LayerType.Draw -> type.copy(
                        strokes = type.strokes.map { it.copy(points = it.points.toList()) }
                    )
                    is LayerType.Text -> type.copy(decorations = type.decorations.toSet())
                    else -> type
                },
                filter = layer.filter?.let(UiFilterCodec::snapshot),
                effects = layer.effects.frozenEffects(),
                mask = layer.mask?.frozen(),
                imageHistory = layer.imageHistory.toList(),
            )
        },
    )

    private fun List<LayerEffect>.frozenEffects(): List<LayerEffect> =
        map { it.copy(filter = UiFilterCodec.snapshot(it.filter)) }

    private fun LayerMask.frozen(): LayerMask =
        copy(strokes = strokes.map { it.copy(points = it.points.toList()) })
}
