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

package com.t8rin.imagetoolbox.core.ui.utils.helper

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalWindowSizeClass

@Composable
operator fun PaddingValues.plus(paddingValues: PaddingValues): PaddingValues {
    val ld = LocalLayoutDirection.current
    return remember(ld, paddingValues) {
        derivedStateOf {
            PaddingValues(
                start = calculateStartPadding(ld) + paddingValues.calculateStartPadding(ld),
                top = calculateTopPadding() + paddingValues.calculateTopPadding(),
                end = calculateEndPadding(ld) + paddingValues.calculateEndPadding(ld),
                bottom = calculateBottomPadding() + paddingValues.calculateBottomPadding(),
            )
        }
    }.value
}

private const val COMPACT_SCREEN_WIDTH_DP = 600

/**
 * Legacy portrait-layout helper used across the project.
 *
 * This is **not** a pure physical-orientation check. It returns `true` whenever the UI
 * should keep using the portrait-like / compact shell:
 * - the device is not in landscape, or
 * - the available width is still compact (`< 600dp`).
 *
 * Keep using this in existing screens for consistency. Prefer
 * [isPortraitOrCompactWidthLayoutAsState] in new code when you want the same behavior with
 * a more explicit name.
 */
@Composable
fun isPortraitOrientationAsState(): State<Boolean> = rememberUpdatedState(true)

/**
 * Dedicated helper for app-shell switching.
 *
 * Unlike [isPortraitOrientationAsState], this helper follows the physical device orientation and
 * is meant for outer containers such as navigation rails, bottom bars and startup tab selection.
 * Internal content screens should keep using [isPortraitOrientationAsState] so they can remain in
 * a portrait-like layout even when hosted inside a wide shell.
 */
@Composable
fun isShellPortraitOrientationAsState(): State<Boolean> {
    val configuration = LocalConfiguration.current

    return rememberUpdatedState(
        configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
    )
}

/**
 * Returns `true` when the UI should stay in a portrait-like / compact layout.
 *
 * This helper is the clearer version of [isPortraitOrientationAsState]. It is suitable for
 * adaptive shells that should remain in the compact layout on narrow landscape windows.
 *
 * The returned [State] always reflects the latest [LocalConfiguration]. Do not wrap this in an
 * additional `remember { derivedStateOf { ... } }`, otherwise configuration changes may stop
 * propagating until the process is recreated.
 */
@Composable
fun isPortraitOrCompactWidthLayoutAsState(): State<Boolean> {
    val configuration = LocalConfiguration.current
    val sizeClass = LocalWindowSizeClass.current

    return rememberUpdatedState(
        configuration.orientation != Configuration.ORIENTATION_LANDSCAPE ||
            sizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    )
}

/**
 * Non-Compose variant of [isPortraitOrCompactWidthLayoutAsState].
 *
 * Use this in places such as startup routing, where the same shell rule is needed before the
 * first composition is created.
 */
fun Context.isPortraitOrCompactWidthLayout(): Boolean =
    resources.configuration.isPortraitOrCompactWidthLayout()

/**
 * Non-Compose variant of [isShellPortraitOrientationAsState].
 */
fun Context.isShellPortraitOrientation(): Boolean =
    resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE

private fun Configuration.isPortraitOrCompactWidthLayout(): Boolean {
    return orientation != Configuration.ORIENTATION_LANDSCAPE ||
        screenWidthDp in 0 until COMPACT_SCREEN_WIDTH_DP
}

