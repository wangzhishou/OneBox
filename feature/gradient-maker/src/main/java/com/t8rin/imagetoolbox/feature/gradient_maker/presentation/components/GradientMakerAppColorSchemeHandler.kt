/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file has been modified from the original ImageToolbox source.
 *
 * Origin: ImageToolbox derivative, modified
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

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.t8rin.imagetoolbox.core.domain.model.IntegerSize
import com.wanbaohe.core.ui.theme.blend
import com.wanbaohe.core.ui.widget.utils.AutoContentBasedColors
import com.t8rin.imagetoolbox.feature.gradient_maker.presentation.screenLogic.GradientMakerComponent

@Composable
internal fun GradientMakerAppColorSchemeHandler(component: GradientMakerComponent) {
    AutoContentBasedColors(
        model = Triple(component.brush, component.meshPoints, component.selectedUri),
        selector = { (_, uri) ->
            component.createGradientBitmap(
                data = uri,
                integerSize = IntegerSize(1000, 1000)
            )
        },
        allowChangeColor = component.screenType != null
    )

    val colorScheme = MaterialTheme.colorScheme
    LaunchedEffect(component.colorStops) {
        if (component.colorStops.isEmpty()) {
            colorScheme.apply {
                component.addColorStop(
                    pair = 0f to primary.blend(primaryContainer, 0.5f),
                    isInitial = true
                )
                component.addColorStop(
                    pair = 0.5f to secondary.blend(secondaryContainer, 0.5f),
                    isInitial = true
                )
                component.addColorStop(
                    pair = 1f to tertiary.blend(tertiaryContainer, 0.5f),
                    isInitial = true
                )
            }
        }
    }
}