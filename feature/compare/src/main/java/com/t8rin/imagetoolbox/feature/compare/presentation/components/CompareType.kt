/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.feature.compare.presentation.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.Cube
import com.t8rin.imagetoolbox.core.resources.icons.Transparency
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTouchApp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineZoomIn
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCompare

sealed class CompareType(
    val icon: ImageVector,
    @StringRes val title: Int
) {
    data object Slide : CompareType(
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCompare,
        title = R.string.slide
    )

    data object SideBySide : CompareType(
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineZoomIn,
        title = R.string.side_by_side
    )

    data object Tap : CompareType(
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTouchApp,
        title = R.string.toggle_tap
    )

    data object Transparency : CompareType(
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Filled.Transparency,
        title = R.string.transparency
    )

    data object PixelByPixel : CompareType(
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Cube,
        title = R.string.pixel_by_pixel
    )

    companion object {
        val entries by lazy {
            listOf(Slide, SideBySide, Tap, Transparency, PixelByPixel)
        }
    }
}