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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.domain.utils.roundTo
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.core.ui.widget.enhanced.EnhancedSliderItem
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.feature.draw.domain.DrawPathMode
import com.t8rin.imagetoolbox.feature.draw.presentation.components.utils.angle
import com.t8rin.imagetoolbox.feature.draw.presentation.components.utils.isArrow
import com.t8rin.imagetoolbox.feature.draw.presentation.components.utils.sizeScale
import com.t8rin.imagetoolbox.feature.draw.presentation.components.utils.updateArrow

@Composable
internal fun ArrowParamsSelector(
    value: DrawPathMode,
    onValueChange: (DrawPathMode) -> Unit
) {
    AnimatedVisibility(
        visible = value.isArrow(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column {
            EnhancedSliderItem(
                value = value.sizeScale(),
                title = stringResource(R.string.head_length_scale),
                valueRange = 0.5f..8f,
                internalStateTransformation = {
                    it.roundTo(1)
                },
                onValueChange = {
                    onValueChange(
                        value.updateArrow(sizeScale = it)
                    )
                },
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = ShapeDefaults.top
            )
            Spacer(modifier = Modifier.height(4.dp))
            EnhancedSliderItem(
                value = value.angle() - 90f,
                title = stringResource(R.string.angle),
                valueRange = 0f..90f,
                internalStateTransformation = {
                    it.roundTo(1)
                },
                onValueChange = {
                    onValueChange(
                        value.updateArrow(angle = it + 90f)
                    )
                },
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = ShapeDefaults.bottom
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}