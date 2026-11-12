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

package com.wanbaohe.textcard.presentation.editor.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.shifenmiao.common.components.text.applySelectionStyle
import com.shifenmiao.common.components.text.clearSelectionStyles
import com.t8rin.imagetoolbox.core.settings.presentation.model.asUi
import com.wanbaohe.core.ui.widget.color_picker.ColorSelectionRow
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.effectiveColorAt
import com.wanbaohe.textcard.domain.model.effectiveSizeScaleAt
import com.wanbaohe.textcard.domain.model.isRangeEffectivelyBold
import com.wanbaohe.textcard.domain.model.isRangeEffectivelyItalic
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent

@Composable
fun PageSelectionPanel(
    component: TextCardComponent,
    state: TextFieldState,
    onStylesChanged: () -> Unit,
) {
    val session = component.canvasTextEditSession ?: return
    if (state.selection.collapsed) return
    val block = session.block
    val range = state.selection

    fun applyStyle(style: SpanStyle) {
        state.applySelectionStyle(style = style, onChanged = onStylesChanged)
    }

    PanelSliderRow(
        label = stringResource(R.string.textcard_text_size),
        value = block.effectiveSizeScaleAt(range.min, range.max),
        valueRange = 0.5f..4f,
        valueText = "${(block.effectiveSizeScaleAt(range.min, range.max) * 100).toInt()}%",
        onValueChange = {
            component.beginContinuousEdit()
            applyStyle(SpanStyle(fontSize = it.em))
        },
        onValueChangeFinished = component::endContinuousEdit
    )
    PanelSettingRow(label = stringResource(R.string.textcard_text_color)) {
        ColorSelectionRow(
            value = Color(block.effectiveColorAt(range.min, range.max)),
            onValueChange = { applyStyle(SpanStyle(color = it)) },
            allowAlpha = false,
            modifier = Modifier.weight(1f)
        )
    }
    val fontSpan = block.fontSpans.lastOrNull {
        it.start < range.max && it.end > range.min
    }
    FontPickerRow(
        component = component,
        block = block.copy(font = if (fontSpan != null) fontSpan.font else block.font),
        onFontSelected = { font ->
            component.beginContinuousEdit()
            state.applySelectionStyle(
                style = SpanStyle(fontFamily = font.asUi().fontFamily),
                onChanged = onStylesChanged
            )
            component.applyPageSelectionFont(font)
            component.endContinuousEdit()
        }
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        val bold = block.isRangeEffectivelyBold(range.min, range.max)
        val italic = block.isRangeEffectivelyItalic(range.min, range.max)
        StyleChip(
            glyph = "B",
            labelRes = R.string.textcard_bold,
            selected = bold,
            onClick = {
                applyStyle(
                    SpanStyle(fontWeight = if (bold) FontWeight.Normal else FontWeight.Bold)
                )
            },
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        StyleChip(
            glyph = "I",
            labelRes = R.string.textcard_italic,
            selected = italic,
            onClick = {
                applyStyle(
                    SpanStyle(fontStyle = if (italic) FontStyle.Normal else FontStyle.Italic)
                )
            },
            fontStyle = FontStyle.Italic,
            modifier = Modifier.weight(1f)
        )
        StyleChip(
            glyph = "A",
            labelRes = R.string.textcard_clear_selection_style,
            selected = false,
            onClick = {
                component.beginContinuousEdit()
                state.clearSelectionStyles(onChanged = onStylesChanged)
                component.clearPageSelectionStyles()
                component.endContinuousEdit()
            },
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.padding(bottom = 4.dp))
}
