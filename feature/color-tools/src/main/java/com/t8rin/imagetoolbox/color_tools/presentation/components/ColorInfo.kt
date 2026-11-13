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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.colordetector.parser.ColorNameParser
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.wanbaohe.core.ui.utils.helper.toHex
import com.wanbaohe.core.ui.widget.color_picker.LargeColorBlock
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun ColorInfo(
    selectedColor: Color,
    onColorChange: (Color) -> Unit,
) {
    val scope = rememberCoroutineScope()
    Column {
        LargeColorBlock(
            color = selectedColor,
            onClick = {
                Clipboard.copy(
                    text = getFormattedColor(selectedColor),
                    message = R.string.color_copied
                )
            },
            primaryText = selectedColor.toHex(),
            secondaryText = remember(selectedColor) {
                ColorNameParser.parseColorName(selectedColor)
            },
            modifier = Modifier
                .heightIn(min = 80.dp)
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        var wasNull by rememberSaveable {
            mutableStateOf(false)
        }
        var resetJob by remember {
            mutableStateOf<Job?>(null)
        }
        ColorInfoDisplay(
            value = selectedColor,
            onValueChange = {
                wasNull = it == null

                onColorChange(it ?: selectedColor)
            },
            onCopy = {
                Clipboard.copy(
                    text = it,
                    message = R.string.color_copied
                )
            },
            onLoseFocus = {
                resetJob?.cancel()
                resetJob = scope.launch {
                    delay(100.milliseconds)
                    if (wasNull) {
                        onColorChange(Color.White)
                        delay(100.milliseconds)
                        onColorChange(selectedColor)
                    }
                }
            }
        )
    }
}
