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

package com.t8rin.imagetoolbox.core.filters.domain.model.params

import androidx.compose.ui.graphics.Color
import com.t8rin.imagetoolbox.core.domain.model.ColorModel
import com.wanbaohe.core.ui.utils.helper.toModel

data class VoronoiCrystallizeParams(
    val borderThickness: Float,
    val scale: Float,
    val randomness: Float,
    val shape: Int,
    val turbulence: Float,
    val angle: Float,
    val stretch: Float,
    val amount: Float,
    val color: ColorModel,
) {
    companion object {
        val Default = VoronoiCrystallizeParams(
            borderThickness = 0.4f,
            color = Color(0xff000000).toModel(),
            scale = 32f,
            randomness = 5f,
            shape = 0,
            turbulence = 1f,
            angle = 0f,
            stretch = 1f,
            amount = 1f
        )
    }
}