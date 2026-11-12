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

package com.t8rin.imagetoolbox.feature.image_stitch.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.core.ui.utils.state.derivedValueOf
import com.wanbaohe.core.ui.widget.enhanced.EnhancedButtonGroup
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.core.ui.widget.modifier.flatGlassContainer
import com.t8rin.imagetoolbox.feature.image_stitch.domain.StitchAlignment


@Composable
fun StitchAlignmentSelector(
    modifier: Modifier = Modifier,
    value: StitchAlignment,
    onValueChange: (StitchAlignment) -> Unit
) {
    Column(
        modifier = modifier
            .flatGlassContainer(shape = ShapeDefaults.extraLarge)
    ) {
        EnhancedButtonGroup(
            modifier = Modifier.padding(start = 3.dp, end = 2.dp),
            enabled = true,
            title = {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(id = R.string.alignment))
                    Spacer(modifier = Modifier.height(8.dp))
                }
            },
            itemCount = StitchAlignment.entries.size,
            selectedIndex = derivedValueOf(value) {
                StitchAlignment.entries.indexOfFirst { it == value }
            },
            onIndexChange = {
                onValueChange(StitchAlignment.entries[it])
            },
            itemContent = {
                val text = when (StitchAlignment.entries[it]) {
                    StitchAlignment.Start -> R.string.start
                    StitchAlignment.Center -> R.string.center
                    StitchAlignment.End -> R.string.end
                }
                Text(stringResource(text))
            }
        )
    }
}