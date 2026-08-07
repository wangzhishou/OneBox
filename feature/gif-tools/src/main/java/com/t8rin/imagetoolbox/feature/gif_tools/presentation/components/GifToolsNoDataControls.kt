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

package com.t8rin.imagetoolbox.feature.gif_tools.presentation.components

import androidx.compose.runtime.Composable
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.preferences.TypeSelectionGrid
import com.t8rin.imagetoolbox.core.ui.widget.preferences.TypeSelectionItem

@Composable
internal fun GifToolsNoDataControls(
    onClickType: (Screen.GifTools.Type) -> Unit
) {
    val imageToGif = Screen.GifTools.Type.ImageToGif()
    val gifToImage = Screen.GifTools.Type.GifToImage()
    val gifToWebp = Screen.GifTools.Type.GifToWebp()

    TypeSelectionGrid(
        items = listOf(
            TypeSelectionItem(imageToGif.title, imageToGif.subtitle, imageToGif.icon),
            TypeSelectionItem(gifToImage.title, gifToImage.subtitle, gifToImage.icon),
            TypeSelectionItem(gifToWebp.title, gifToWebp.subtitle, gifToWebp.icon)
        ),
        onClick = { index ->
            when (index) {
                0 -> onClickType(imageToGif)
                1 -> onClickType(gifToImage)
                2 -> onClickType(gifToWebp)
            }
        }
    )
}
