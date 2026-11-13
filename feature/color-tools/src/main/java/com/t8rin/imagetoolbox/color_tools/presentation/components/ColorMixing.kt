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

package com.t8rin.imagetoolbox.color_tools.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.colordetector.parser.ColorNameParser
import com.t8rin.dynamic.theme.ColorTuple
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.wanbaohe.core.ui.utils.helper.toHex
import com.wanbaohe.core.ui.widget.color_picker.LargeColorBlock
import com.wanbaohe.core.ui.widget.controls.selection.ColorRowSelector
import com.wanbaohe.core.ui.widget.saver.ColorSaver

@Composable
internal fun ColorMixing(
    selectedColor: Color,
    appColorTuple: ColorTuple,
) {
    var mixingVariation by rememberSaveable {
        mutableIntStateOf(8)
    }
    var colorToMix by rememberSaveable(
        stateSaver = ColorSaver
    ) {
        mutableStateOf(appColorTuple.tertiary ?: Color.Yellow)
    }
    val mixedColors by remember(selectedColor, mixingVariation, colorToMix) {
        derivedStateOf {
            selectedColor.mixWith(
                color = colorToMix,
                variations = mixingVariation,
                maxPercent = 1f
            )
        }
    }

    Column {
        ColorRowSelector(
            value = colorToMix,
            onValueChange = { colorToMix = it },
            modifier = Modifier.fillMaxWidth(),
            icon = null,
            title = stringResource(R.string.color_to_mix)
        )
        Spacer(modifier = Modifier.height(16.dp))
        VariationSlider(
            value = mixingVariation,
            onValueChange = { mixingVariation = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            mixedColors.chunked(2).forEach { rowColors ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowColors.forEach { color ->
                        val colorName = remember(color) {
                            ColorNameParser.parseColorName(color)
                        }
                        LargeColorBlock(
                            color = color,
                            onClick = {
                                Clipboard.copy(
                                    text = getFormattedColor(color),
                                    message = R.string.color_copied
                                )
                            },
                            primaryText = color.toHex(),
                            secondaryText = colorName,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 100.dp)
                        )
                    }
                    if (rowColors.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
