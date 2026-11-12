/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
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

package com.wanbaohe.decisionwheel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.theme.toColor
import com.wanbaohe.core.ui.widget.color_picker.ColorSelection
import com.wanbaohe.core.ui.widget.enhanced.EnhancedButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.text.TitleItem
import com.wanbaohe.decisionwheel.R
import com.wanbaohe.com.color.ColorGenerator
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTheme

/**
 * Decision Wheel palette picker sheet.
 *
 * Contract:
 * - Input: [initialBaseColor]作为默认基色，[previewCount]决定生成/预览多少个扇区色。
 * - While sheet is visible: user can tweak the base color; we show live preview swatches.
 * - Output:
 *   - Only when the user presses the confirm button, [onConfirm] is called with the base color.
 *   - Dismiss/cancel will never trigger [onConfirm].
 *
 * Notes:
 * - This sheet reuses the Color Tools color selection UI components (HSV selector + hue slider)
 *   without modifying the color-tools feature module.
 * - Flat style: surfaces use 0 elevation.
 */
@Composable
fun DecisionWheelPalettePickerSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    initialBaseColor: Color,
    previewCount: Int,
    onConfirm: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val safePreviewCount = remember(previewCount) { previewCount.coerceAtLeast(0) }

    var tempColorInt by remember(visible, initialBaseColor) {
        mutableIntStateOf(initialBaseColor.toArgb())
    }

    val tempColor = remember(tempColorInt) { Color(tempColorInt) }

    val previewColors by remember(tempColorInt, safePreviewCount) {
        derivedStateOf {
            ColorGenerator.generateSegmentColors(
                baseColor = tempColor,
                count = safePreviewCount
            )
        }
    }

    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { if (!it) onDismiss() },
        title = stringResource(R.string.palette_scheme),
        confirmButton = {
            EnhancedButton(
                onClick = {
                    onConfirm(Color(tempColorInt))
                    onDismiss()
                }
            ) {
                Text(text = stringResource(R.string.apply))
            }
        },
        sheetContent = {
            Column(
                modifier = modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Preview first
                if (safePreviewCount > 0) {
                    Text(
                        text = stringResource(R.string.options_list_count, safePreviewCount),
                        style = MaterialTheme.typography.titleMedium
                    )

                    val swatchSize: Dp = 44.dp

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        previewColors.forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(swatchSize)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(c.background)
                            )
                        }
                    }
                }

                // Picker below
                ColorSelection(
                    value = tempColorInt.toColor(),
                    onValueChange = { tempColorInt = it.toArgb() },
                    infoContainerColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    )

    LaunchedEffect(visible) {
        if (visible) {
            tempColorInt = initialBaseColor.toArgb()
        }
    }
}
