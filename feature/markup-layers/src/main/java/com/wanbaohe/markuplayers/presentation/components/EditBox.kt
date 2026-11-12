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

package com.wanbaohe.markuplayers.presentation.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wanbaohe.core.ui.widget.editor.BoxResizeConfig
import com.wanbaohe.core.ui.widget.editor.EditorLayerBox
import com.wanbaohe.core.ui.widget.editor.EditorLayerBoxLabels
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.LayerTransform

@Composable
fun BoxWithConstraintsScope.EditBox(
    transform: LayerTransform,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onTransformEnd: (LayerTransform) -> Unit,
    modifier: Modifier = Modifier,
    onTransformStart: () -> Unit = {},
    onTransformChange: ((LayerTransform) -> Unit)? = null,
    onEditRequest: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    tapSelectable: Boolean = true,
    gesturesEnabled: Boolean = true,
    showHandles: Boolean = true,
    canvasScale: Float = 1f,
    resizeConfig: BoxResizeConfig? = null,
    content: @Composable BoxScope.() -> Unit,
) = EditorLayerBox(
    transform = transform,
    isSelected = isSelected,
    onSelect = onSelect,
    onTransformEnd = onTransformEnd,
    labels = EditorLayerBoxLabels(
        scale = stringResource(R.string.studio_layer_scale),
        rotate = stringResource(R.string.studio_layer_rotate),
        resizeWidth = stringResource(R.string.studio_layer_resize_width),
        delete = stringResource(R.string.markup_delete_layer),
    ),
    modifier = modifier,
    onTransformStart = onTransformStart,
    onTransformChange = onTransformChange,
    onEditRequest = onEditRequest,
    onDelete = onDelete,
    tapSelectable = tapSelectable,
    gesturesEnabled = gesturesEnabled,
    showHandles = showHandles,
    canvasScale = canvasScale,
    resizeConfig = resizeConfig,
    content = content,
)
