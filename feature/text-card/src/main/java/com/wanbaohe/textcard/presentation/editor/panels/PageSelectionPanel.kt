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
