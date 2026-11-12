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

package com.wanbaohe.core.ui.widget.utils

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.LocalContext
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.toBitmap
import com.t8rin.dynamic.theme.LocalDynamicThemeState
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.settings.presentation.provider.rememberAppColorTuple

@Composable
fun <T : Any> AutoContentBasedColors(
    model: T?,
    allowChangeColor: Boolean = true
) {
    val context = LocalContext.current

    AutoContentBasedColors(
        model = model,
        selector = {
            context.imageLoader.execute(
                ImageRequest.Builder(context).data(model).build()
            ).image?.toBitmap()
        },
        allowChangeColor = allowChangeColor
    )
}

@Composable
fun <T : Any> AutoContentBasedColors(
    model: T?,
    selector: suspend (T) -> Bitmap?,
    allowChangeColor: Boolean = true
) {
    val appColorTuple = rememberAppColorTuple()
    val settingsState = LocalSettingsState.current
    val themeState = LocalDynamicThemeState.current
    val internalAllowChangeColor = settingsState.allowChangeColorByImage && allowChangeColor

    LaunchedEffect(model, appColorTuple, internalAllowChangeColor) {
        internalAllowChangeColor.takeIf { it }
            ?.let {
                model?.let { selector(it) }
            }?.let {
                themeState.updateColorByImage(it)
            } ?: themeState.updateColorTuple(appColorTuple)
    }
}

@Composable
fun AutoContentBasedColors(
    model: Bitmap?,
    allowChangeColor: Boolean = true
) {
    AutoContentBasedColors(
        model = model,
        selector = { model },
        allowChangeColor = allowChangeColor
    )
}

@Composable
fun AutoContentBasedColors(
    model: Color,
    allowChangeColor: Boolean = true
) {
    val appColorTuple = rememberAppColorTuple()
    val settingsState = LocalSettingsState.current
    val themeState = LocalDynamicThemeState.current
    val internalAllowChangeColor = settingsState.allowChangeColorByImage && allowChangeColor

    LaunchedEffect(model, appColorTuple, internalAllowChangeColor) {
        internalAllowChangeColor.takeIf { it && model.isSpecified }
            ?.let {
                themeState.updateColor(model)
            } ?: themeState.updateColorTuple(appColorTuple)
    }
}
