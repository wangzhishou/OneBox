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

package com.t8rin.imagetoolbox.feature.gradient_maker.presentation.components

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.ImageOverlay
import com.t8rin.imagetoolbox.core.resources.icons.MeshDownload
import com.t8rin.imagetoolbox.core.resources.icons.MeshGradient
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.preferences.PreferenceItemsGrid
import com.t8rin.imagetoolbox.core.ui.widget.preferences.TypeSelectionCard
import com.t8rin.imagetoolbox.feature.gradient_maker.presentation.components.model.GradientMakerType
import com.t8rin.imagetoolbox.feature.gradient_maker.presentation.screenLogic.GradientMakerComponent
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImagePreview

@Composable
internal fun GradientMakerNoDataControls(
    component: GradientMakerComponent
) {
    var requestedType by rememberSaveable(component.screenType) {
        mutableStateOf<GradientMakerType?>(null)
    }

    val imagePicker = rememberImagePicker { uris: List<Uri> ->
        component.setScreenType(requestedType)
        component.setUris(uris)
        component.updateGradientAlpha(0.5f)
    }

    val screen = remember { Screen.GradientMaker() }

    PreferenceItemsGrid(
        spacing = 16.dp,
        items = listOf(
            @Composable {
                TypeSelectionCard(
                    title = stringResource(screen.title),
                    subtitle = stringResource(screen.subtitle),
                    icon = screen.icon!!,
                    index = 0,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { component.setScreenType(GradientMakerType.Default) }
                )
            },
            @Composable {
                TypeSelectionCard(
                    title = stringResource(R.string.gradient_maker_type_image),
                    subtitle = stringResource(R.string.gradient_maker_type_image_sub),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImagePreview,
                    index = 1,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        requestedType = GradientMakerType.Overlay
                        imagePicker.pickImage()
                    }
                )
            },
            @Composable {
                TypeSelectionCard(
                    title = stringResource(R.string.mesh_gradients),
                    subtitle = stringResource(R.string.mesh_gradients_sub),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.MeshGradient,
                    index = 2,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { component.setScreenType(GradientMakerType.Mesh) }
                )
            },
            @Composable {
                TypeSelectionCard(
                    title = stringResource(R.string.gradient_maker_type_image_mesh),
                    subtitle = stringResource(R.string.gradient_maker_type_image_mesh_sub),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ImageOverlay,
                    index = 3,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        requestedType = GradientMakerType.MeshOverlay
                        imagePicker.pickImage()
                    }
                )
            },
            @Composable {
                TypeSelectionCard(
                    title = stringResource(R.string.collection_mesh_gradients),
                    subtitle = stringResource(R.string.collection_mesh_gradients_sub),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.MeshDownload,
                    index = 4,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { component.onNavigate(Screen.MeshGradients) }
                )
            }
        )
    )
}
