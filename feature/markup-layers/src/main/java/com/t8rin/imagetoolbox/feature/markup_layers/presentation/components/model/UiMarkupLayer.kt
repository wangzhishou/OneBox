/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.feature.markup_layers.presentation.components.model

import androidx.compose.material.icons.Icons
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import com.t8rin.imagetoolbox.feature.markup_layers.domain.DomainTextDecoration
import com.t8rin.imagetoolbox.feature.markup_layers.domain.LayerPosition
import com.t8rin.imagetoolbox.feature.markup_layers.domain.LayerType
import com.t8rin.imagetoolbox.feature.markup_layers.domain.MarkupLayer
import com.t8rin.imagetoolbox.feature.markup_layers.presentation.components.EditBoxState
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFormatBold
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFormatItalic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFormatStrikethrough
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFormatUnderlined

data class UiMarkupLayer(
    val type: LayerType,
    val state: EditBoxState = EditBoxState(isActive = true)
) {
    fun copy(
        isActive: Boolean = state.isActive,
        coerceToBounds: Boolean = state.coerceToBounds
    ) = UiMarkupLayer(
        type = type,
        state = state.copy(
            isActive = isActive,
            coerceToBounds = coerceToBounds
        )
    )
}

fun UiMarkupLayer.asDomain(): MarkupLayer = MarkupLayer(
    type = type,
    position = LayerPosition(
        scale = state.scale,
        rotation = state.rotation,
        offsetX = state.offset.x,
        offsetY = state.offset.y,
        alpha = state.alpha,
        currentCanvasSize = state.canvasSize,
        coerceToBounds = state.coerceToBounds
    )
)

fun MarkupLayer.asUi(): UiMarkupLayer = UiMarkupLayer(
    type = type,
    state = EditBoxState(
        scale = position.scale,
        rotation = position.rotation,
        offset = Offset(
            x = position.offsetX,
            y = position.offsetY
        ),
        isActive = false,
        canvasSize = position.currentCanvasSize,
        coerceToBounds = position.coerceToBounds
    )
)

val DomainTextDecoration.icon: ImageVector
    get() = when (this) {
        LayerType.Text.Decoration.Bold -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFormatBold
        LayerType.Text.Decoration.Italic -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFormatItalic
        LayerType.Text.Decoration.Underline -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFormatUnderlined
        LayerType.Text.Decoration.LineThrough -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFormatStrikethrough
    }