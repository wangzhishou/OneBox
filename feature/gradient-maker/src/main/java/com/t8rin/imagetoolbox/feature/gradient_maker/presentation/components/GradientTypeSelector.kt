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

package com.t8rin.imagetoolbox.feature.gradient_maker.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.core.ui.widget.glass.GlassSegmentedButtonRow
import com.wanbaohe.core.ui.widget.other.ExpandableItem
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.text.TitleItem
import com.t8rin.imagetoolbox.feature.gradient_maker.domain.GradientType
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTune

@Composable
fun GradientTypeSelector(
    value: GradientType,
    onValueChange: (GradientType) -> Unit,
    modifier: Modifier = Modifier,
    propertiesContent: @Composable () -> Unit
) {
    // 分段行自带玻璃底，父级不再套容器卡 —— 避免「卡片里面还有卡片」的多层背景。
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlassSegmentedButtonRow(
            options = GradientType.entries,
            selectedOption = value,
            onOptionSelected = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { type ->
                Text(
                    text = type.translatedName,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            },
            buttonHeight = 40.dp,
        )
        ExpandableItem(
            visibleContent = {
                TitleItem(
                    text = stringResource(id = R.string.properties),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTune
                )
            },
            expandableContent = {
                Column(
                    modifier = Modifier.padding(horizontal = OneBoxDesignSystem.microSpacing)
                ) {
                    propertiesContent()
                }
            },
            color = Color.Unspecified
        )
    }
}

private val GradientType.translatedName: String
    @Composable
    get() = when (this) {
        GradientType.Linear -> stringResource(id = R.string.gradient_type_linear)
        GradientType.Radial -> stringResource(id = R.string.gradient_type_radial)
        GradientType.Sweep -> stringResource(id = R.string.gradient_type_sweep)
    }
