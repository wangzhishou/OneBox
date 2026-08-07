/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.feature.draw.presentation.components.controls

import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.t8rin.imagetoolbox.core.ui.widget.buttons.EraseModeButton
import com.t8rin.imagetoolbox.core.ui.widget.buttons.PanModeButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.feature.draw.presentation.screenLogic.DrawComponent
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo

@Composable
internal fun DrawContentSecondaryControls(
    component: DrawComponent,
    panEnabled: Boolean,
    onTogglePanEnabled: () -> Unit,
    isEraserOn: Boolean,
    onToggleIsEraserOn: () -> Unit
) {
    PanModeButton(
        selected = panEnabled,
        onClick = onTogglePanEnabled
    )
    EnhancedIconButton(
        onClick = component::undo,
        enabled = component.lastPaths.isNotEmpty() || component.paths.isNotEmpty()
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUndo,
            contentDescription = "Undo"
        )
    }
    EnhancedIconButton(
        onClick = component::redo,
        enabled = component.undonePaths.isNotEmpty()
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRedo,
            contentDescription = "Redo"
        )
    }
    EraseModeButton(
        selected = isEraserOn,
        enabled = !panEnabled,
        onClick = onToggleIsEraserOn
    )
}