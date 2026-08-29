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

package com.t8rin.imagetoolbox.core.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import com.t8rin.dynamic.theme.DynamicTheme
import com.t8rin.dynamic.theme.rememberDynamicThemeState
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.settings.presentation.provider.rememberAppColorTuple
import com.t8rin.imagetoolbox.core.ui.widget.glass.MeshGradientBackground
import com.t8rin.imagetoolbox.core.ui.widget.modifier.AutoCornersShape

@SuppressLint("NewApi")
@Composable
fun ImageToolboxTheme(
    content: @Composable () -> Unit
) {
    val settingsState = LocalSettingsState.current
    DynamicTheme(
        typography = rememberTypography(settingsState.font),
        state = rememberDynamicThemeState(rememberAppColorTuple()),
        colorBlindType = settingsState.colorBlindType,
        defaultColorTuple = settingsState.appColorTuple,
        dynamicColor = settingsState.isDynamicColors,
        amoledMode = settingsState.isAmoledMode,
        isDarkTheme = settingsState.isNightMode,
        contrastLevel = settingsState.themeContrastLevel,
        style = settingsState.themeStyle,
        isInvertColors = settingsState.isInvertThemeColors,
        content = {
            MaterialTheme(
                motionScheme = CustomMotionScheme,
                colorScheme = modifiedColorScheme(),
                content = content
            )
        }
    )
}

@Composable
fun ImageToolboxThemeSurface(
    content: @Composable BoxScope.() -> Unit
) {
    ImageToolboxTheme {
        MeshGradientBackground {
            content()
        }
    }
}


@Composable
internal fun modifiedShapes(): Shapes {
    val shapes = MaterialTheme.shapes
    val shapesType = LocalSettingsState.current.shapesType

    return remember(shapes, shapesType) {
        derivedStateOf {
            shapes.copy(
                extraSmall = AutoCornersShape(
                    topStart = shapes.extraSmall.topStart,
                    topEnd = shapes.extraSmall.topEnd,
                    bottomEnd = shapes.extraSmall.bottomEnd,
                    bottomStart = shapes.extraSmall.bottomStart,
                    shapesType = shapesType
                ),
                small = AutoCornersShape(
                    topStart = shapes.small.topStart,
                    topEnd = shapes.small.topEnd,
                    bottomEnd = shapes.small.bottomEnd,
                    bottomStart = shapes.small.bottomStart,
                    shapesType = shapesType
                ),
                medium = AutoCornersShape(
                    topStart = shapes.medium.topStart,
                    topEnd = shapes.medium.topEnd,
                    bottomEnd = shapes.medium.bottomEnd,
                    bottomStart = shapes.medium.bottomStart,
                    shapesType = shapesType
                ),
                large = AutoCornersShape(
                    topStart = shapes.large.topStart,
                    topEnd = shapes.large.topEnd,
                    bottomEnd = shapes.large.bottomEnd,
                    bottomStart = shapes.large.bottomStart,
                    shapesType = shapesType
                ),
                extraLarge = AutoCornersShape(
                    topStart = shapes.extraLarge.topStart,
                    topEnd = shapes.extraLarge.topEnd,
                    bottomEnd = shapes.extraLarge.bottomEnd,
                    bottomStart = shapes.extraLarge.bottomStart,
                    shapesType = shapesType
                ),
                largeIncreased = AutoCornersShape(
                    topStart = shapes.largeIncreased.topStart,
                    topEnd = shapes.largeIncreased.topEnd,
                    bottomEnd = shapes.largeIncreased.bottomEnd,
                    bottomStart = shapes.largeIncreased.bottomStart,
                    shapesType = shapesType
                ),
                extraLargeIncreased = AutoCornersShape(
                    topStart = shapes.extraLargeIncreased.topStart,
                    topEnd = shapes.extraLargeIncreased.topEnd,
                    bottomEnd = shapes.extraLargeIncreased.bottomEnd,
                    bottomStart = shapes.extraLargeIncreased.bottomStart,
                    shapesType = shapesType
                ),
                extraExtraLarge = AutoCornersShape(
                    topStart = shapes.extraExtraLarge.topStart,
                    topEnd = shapes.extraExtraLarge.topEnd,
                    bottomEnd = shapes.extraExtraLarge.bottomEnd,
                    bottomStart = shapes.extraExtraLarge.bottomStart,
                    shapesType = shapesType
                )
            )
        }
    }.value
}

@Composable
internal fun modifiedColorScheme(): ColorScheme {
    val scheme = MaterialTheme.colorScheme

    return remember(scheme) {
        derivedStateOf {
            scheme.copy(
                errorContainer = scheme.errorContainer.blend(
                    color = scheme.primary,
                    fraction = 0.15f
                )
            )
        }
    }.value
}

const val DisabledAlpha = 0.38f