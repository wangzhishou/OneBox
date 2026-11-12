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

package com.t8rin.imagetoolbox.feature.draw.presentation.components.element

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.widget.controls.resize_group.components.BlurRadiusSelector
import com.t8rin.imagetoolbox.feature.draw.domain.DrawMode

@Composable
internal fun PrivacyBlurParamsSelector(
    value: DrawMode,
    onValueChange: (DrawMode) -> Unit
) {
    AnimatedVisibility(
        visible = value is DrawMode.PathEffect.PrivacyBlur,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        BlurRadiusSelector(
            modifier = Modifier.padding(8.dp),
            value = (value as? DrawMode.PathEffect.PrivacyBlur)?.blurRadius ?: 0,
            valueRange = 5f..50f,
            onValueChange = {
                onValueChange(DrawMode.PathEffect.PrivacyBlur(it))
            },
            color = MaterialTheme.colorScheme.surface
        )
    }
}