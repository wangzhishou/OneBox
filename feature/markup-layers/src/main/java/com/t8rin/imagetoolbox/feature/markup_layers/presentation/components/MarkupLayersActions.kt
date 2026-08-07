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

package com.t8rin.imagetoolbox.feature.markup_layers.presentation.components

import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.emoji.Emoji
import com.t8rin.imagetoolbox.core.resources.icons.Stacks
import com.t8rin.imagetoolbox.core.resources.icons.StickerEmoji
import com.t8rin.imagetoolbox.core.ui.theme.takeColorFromScheme
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.isPortraitOrientationAsState
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.modifier.flatGlassContainer
import com.t8rin.imagetoolbox.core.ui.widget.modifier.fadingEdges
import com.t8rin.imagetoolbox.core.ui.widget.sheets.EmojiSelectionSheet
import com.t8rin.imagetoolbox.feature.markup_layers.domain.LayerType
import com.t8rin.imagetoolbox.feature.markup_layers.presentation.components.model.UiMarkupLayer
import com.t8rin.imagetoolbox.feature.markup_layers.presentation.screenLogic.MarkupLayersComponent
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineText
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStickerEmoji

@Composable
internal fun MarkupLayersActions(
    component: MarkupLayersComponent,
    showLayersSelection: Boolean,
    onToggleLayersSection: () -> Unit,
    onToggleLayersSectionQuick: () -> Unit
) {
    val layerImagePicker = rememberImagePicker { uri: Uri ->
        component.deactivateAllLayers()
        component.addLayer(
            UiMarkupLayer(
                type = LayerType.Picture.Image(uri)
            )
        )
    }
    var showTextEnteringDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var showEmojiPicker by rememberSaveable {
        mutableStateOf(false)
    }

    val state = rememberScrollState()
    Row(
        modifier = Modifier
            .fadingEdges(state)
            .horizontalScroll(state)
            .padding(bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EnhancedIconButton(
            containerColor = takeColorFromScheme {
                if (showLayersSelection) tertiary
                else Color.Transparent
            },
            onLongClick = onToggleLayersSectionQuick,
            onClick = onToggleLayersSection,
            enabled = component.layers.isNotEmpty()
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Stacks,
                contentDescription = null
            )
        }
        EnhancedIconButton(
            onClick = {
                showTextEnteringDialog = true
            }
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineText,
                contentDescription = null
            )
        }
        EnhancedIconButton(
            onClick = {
                showEmojiPicker = true
            }
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStickerEmoji,
                contentDescription = null
            )
        }
        EnhancedIconButton(
            onClick = layerImagePicker::pickImage
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage,
                contentDescription = null
            )
        }

        val isPortrait by isPortraitOrientationAsState()
        if (isPortrait) {
            MarkupLayersUndoRedo(
                component = component,
                color = MaterialTheme.colorScheme.surface,
                removePadding = true
            )
            Spacer(Modifier.width(8.dp))
        }
    }

    val allEmojis = Emoji.allIcons()

    EmojiSelectionSheet(
        selectedEmojiIndex = null,
        allEmojis = allEmojis,
        onEmojiPicked = {
            component.deactivateAllLayers()
            component.addLayer(
                UiMarkupLayer(
                    type = LayerType.Picture.Sticker(allEmojis[it])
                )
            )
            showEmojiPicker = false
        },
        visible = showEmojiPicker,
        onDismiss = {
            showEmojiPicker = false
        }
    )

    AddTextLayerDialog(
        visible = showTextEnteringDialog,
        onDismiss = { showTextEnteringDialog = false },
        onAddLayer = {
            component.deactivateAllLayers()
            component.addLayer(it)
        }
    )
}

@Composable
internal fun MarkupLayersUndoRedo(
    component: MarkupLayersComponent,
    color: Color,
    removePadding: Boolean
) {
    Row(
        modifier = Modifier.flatGlassContainer(
            shape = CircleShape,
            color = color,
            resultPadding = if (removePadding) 0.dp else 4.dp
        )
    ) {
        EnhancedIconButton(
            onClick = component::undo,
            enabled = component.lastLayers.isNotEmpty() || component.layers.isNotEmpty()
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUndo,
                contentDescription = "Undo"
            )
        }
        EnhancedIconButton(
            onClick = component::redo,
            enabled = component.undoneLayers.isNotEmpty()
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRedo,
                contentDescription = "Redo"
            )
        }
    }
}