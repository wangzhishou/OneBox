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

package com.wanbaohe.core.ui.widget.controls.selection

import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSimpleSettingsInteractor
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.core.ui.widget.preferences.PreferenceRowSwitch
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineZoomIn

@Composable
fun MagnifierEnabledSelector(
    modifier: Modifier = Modifier,
    shape: Shape = ShapeDefaults.default,
) {
    val scope = rememberCoroutineScope()
    val settingsState = LocalSettingsState.current
    val settingsInteractor = LocalSimpleSettingsInteractor.current
    PreferenceRowSwitch(
        modifier = modifier,
        shape = shape,
        title = stringResource(R.string.magnifier),
        subtitle = stringResource(R.string.magnifier_sub),
        checked = settingsState.magnifierEnabled,
        onClick = {
            scope.launch {
                settingsInteractor.toggleMagnifierEnabled()
            }
        },
        startIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineZoomIn
    )
}