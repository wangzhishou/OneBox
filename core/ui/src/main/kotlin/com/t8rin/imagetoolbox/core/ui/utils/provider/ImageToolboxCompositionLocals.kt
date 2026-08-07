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

package com.t8rin.imagetoolbox.core.ui.utils.provider

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import com.t8rin.imagetoolbox.core.domain.model.ImageModel
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import com.t8rin.imagetoolbox.core.settings.presentation.model.UiSettingsState
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalEditPresetsController
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsManager
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.settings.presentation.provider.rememberEditPresetsController
import com.t8rin.imagetoolbox.core.ui.theme.ImageToolboxThemeSurface
import com.t8rin.imagetoolbox.core.ui.utils.confetti.ConfettiHost
import com.t8rin.imagetoolbox.core.ui.utils.blessing.BlessingEffectHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.LocalFilterPreviewModelProvider
import com.t8rin.imagetoolbox.core.ui.utils.helper.rememberFilterPreviewProvider
import com.t8rin.imagetoolbox.core.ui.utils.helper.rememberSafeUriHandler
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.rememberEnhancedHapticFeedback
import com.t8rin.imagetoolbox.core.ui.widget.other.ToastHost

@Composable
fun ImageToolboxCompositionLocals(
    settingsState: UiSettingsState,
    settingsManager: SettingsManager? = null,
    filterPreviewModel: ImageModel? = null,
    canSetDynamicFilterPreview: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val editPresetsController = rememberEditPresetsController()
    val context = LocalContext.current
    val customHapticFeedback = rememberEnhancedHapticFeedback(settingsState.hapticsStrength)
    val screenSize = rememberScreenSize()
    val previewProvider = filterPreviewModel?.let {
        rememberFilterPreviewProvider(
            preview = it,
            canSetDynamicFilterPreview = canSetDynamicFilterPreview
        )
    }
    val safeUriHandler = rememberSafeUriHandler()

    val values = remember(
        context,
        settingsState,
        settingsManager,
        editPresetsController,
        customHapticFeedback,
        screenSize,
        filterPreviewModel,
        safeUriHandler
    ) {
        derivedStateOf {
            listOfNotNull(
                LocalSettingsState provides settingsState,
                LocalSettingsManager providesOrNull settingsManager,
                LocalEditPresetsController provides editPresetsController,
                LocalFilterPreviewModelProvider providesOrNull previewProvider,
                LocalHapticFeedback provides customHapticFeedback,
                LocalScreenSize provides screenSize,
                LocalUriHandler provides safeUriHandler
            ).toTypedArray()
        }
    }

    CompositionLocalProvider(
        *values.value,
        content = {
            ImageToolboxThemeSurface {
                content()

                ToastHost()

                ConfettiHost()
                BlessingEffectHost(AppToastHost.blessingEffectState)
            }
        }
    )
}

private infix fun <T : Any> ProvidableCompositionLocal<T>.providesOrNull(
    value: T?
): ProvidedValue<T>? = if (value != null) provides(value) else null
