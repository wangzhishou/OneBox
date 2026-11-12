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

package com.t8rin.imagetoolbox.feature.load_net_image.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.wanbaohe.core.ui.widget.buttons.ShareButton
import com.wanbaohe.core.ui.widget.buttons.ZoomButton
import com.wanbaohe.core.ui.widget.sheets.ZoomModalSheet
import com.t8rin.imagetoolbox.feature.load_net_image.presentation.screenLogic.LoadNetImageComponent

@Composable
internal fun RowScope.LoadNetImageAdaptiveActions(
    component: LoadNetImageComponent
) {
    AnimatedVisibility(component.parsedImages.isNotEmpty()) {
        ShareButton(
            onShare = {
                component.performSharing(AppToastHost::showConfetti)
            },
            onCopy = {
                component.cacheCurrentImage(Clipboard::copy)
            }
        )
    }
    var showZoomSheet by rememberSaveable { mutableStateOf(false) }
    ZoomButton(
        onClick = { showZoomSheet = true },
        visible = component.bitmap != null,
    )
    ZoomModalSheet(
        data = component.bitmap,
        visible = showZoomSheet,
        onDismiss = {
            showZoomSheet = false
        }
    )
}