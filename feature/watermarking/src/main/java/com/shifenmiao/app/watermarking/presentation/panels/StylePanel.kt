/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.presentation.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.app.watermarking.domain.model.WatermarkContent
import com.shifenmiao.app.watermarking.domain.model.WatermarkElement
import com.shifenmiao.app.watermarking.domain.model.isTextBased
import com.shifenmiao.app.watermarking.presentation.components.FontStyleRow
import com.shifenmiao.app.watermarking.presentation.components.OverlayModeSelector
import com.shifenmiao.app.watermarking.presentation.components.PropertySectionTitle
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStyle
import com.wanbaohe.core.ui.theme.toColor
import com.wanbaohe.core.ui.widget.controls.selection.ColorRowSelector
import com.wanbaohe.core.ui.widget.enhanced.EnhancedSliderItem
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.core.ui.widget.preferences.PreferenceRowSwitch
import kotlin.math.roundToInt

@Composable
fun StylePanel(
    selectedElement: WatermarkElement,
    onUpdateElement: (recordHistory: Boolean, transform: (WatermarkElement) -> WatermarkElement) -> Unit,
    onBeginCompoundEdit: () -> Unit,
    onEndCompoundEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PropertySectionTitle(
            text = stringResource(R.string.watermark_style),
            icon = Icons.Outlined.LineStyle,
        )
        if (selectedElement.isTextBased()) {
            FontStyleRow(
                font = selectedElement.style.font,
                onFontSelected = { font ->
                    onUpdateElement(true) { it.copy(style = it.style.copy(font = font)) }
                },
            )
            EnhancedSliderItem(
                value = selectedElement.style.sizeRatio,
                title = stringResource(R.string.font_size),
                valueRange = 0.01f..0.5f,
                internalStateTransformation = { (it * 100).roundToInt() / 100f },
                onValueChange = { value ->
                    onBeginCompoundEdit()
                    onUpdateElement(false) { it.copy(style = it.style.copy(sizeRatio = value)) }
                },
                onValueChangeFinished = { onEndCompoundEdit() },
                shape = ShapeDefaults.large,
                containerColor = Color.Unspecified,
            )
            ColorRowSelector(
                value = selectedElement.style.color.toColor(),
                onValueChange = { color ->
                    onUpdateElement(true) {
                        it.copy(style = it.style.copy(color = color.toArgb()))
                    }
                },
                title = stringResource(R.string.text_color),
            )
            ColorRowSelector(
                value = selectedElement.style.backgroundColor.toColor(),
                onValueChange = { color ->
                    onUpdateElement(true) {
                        it.copy(style = it.style.copy(backgroundColor = color.toArgb()))
                    }
                },
                title = stringResource(R.string.background_color),
            )
        } else {
            val image = selectedElement.content as? WatermarkContent.Image
            if (image != null) {
                EnhancedSliderItem(
                    value = image.size,
                    title = stringResource(R.string.watermark_logo_size),
                    valueRange = 0.01f..1f,
                    internalStateTransformation = { (it * 100).roundToInt() / 100f },
                    onValueChange = { value ->
                        onBeginCompoundEdit()
                        onUpdateElement(false) { it.copy(content = image.copy(size = value)) }
                    },
                    onValueChangeFinished = { onEndCompoundEdit() },
                    shape = ShapeDefaults.large,
                    containerColor = Color.Unspecified,
                )
            }
        }
        EnhancedSliderItem(
            value = selectedElement.transform.alpha,
            title = stringResource(R.string.paint_alpha),
            valueRange = 0f..1f,
            internalStateTransformation = { (it * 100).roundToInt() },
            onValueChange = { value ->
                onBeginCompoundEdit()
                onUpdateElement(false) { it.copy(transform = it.transform.copy(alpha = value)) }
            },
            onValueChangeFinished = { onEndCompoundEdit() },
            shape = ShapeDefaults.large,
            containerColor = Color.Unspecified,
        )
        EnhancedSliderItem(
            value = selectedElement.transform.rotation,
            title = stringResource(R.string.angle),
            valueRange = -180f..180f,
            internalStateTransformation = Float::roundToInt,
            onValueChange = { value ->
                onBeginCompoundEdit()
                onUpdateElement(false) { it.copy(transform = it.transform.copy(rotation = value)) }
            },
            onValueChangeFinished = { onEndCompoundEdit() },
            shape = ShapeDefaults.large,
            containerColor = Color.Unspecified,
        )
        PreferenceRowSwitch(
            title = stringResource(R.string.repeat_watermark),
            subtitle = stringResource(R.string.watermark_repeat_sub),
            checked = selectedElement.layout.isRepeated,
            onClick = { repeated ->
                onUpdateElement(true) { it.copy(layout = it.layout.copy(isRepeated = repeated)) }
            },
            drawContainer = false,
        )
        if (selectedElement.layout.isRepeated) {
            EnhancedSliderItem(
                value = selectedElement.layout.repeatSpacing,
                title = stringResource(R.string.watermark_repeat_spacing),
                valueRange = 1.1f..4f,
                internalStateTransformation = { (it * 10).roundToInt() / 10f },
                onValueChange = { value ->
                    onBeginCompoundEdit()
                    onUpdateElement(false) { it.copy(layout = it.layout.copy(repeatSpacing = value)) }
                },
                onValueChangeFinished = { onEndCompoundEdit() },
                shape = ShapeDefaults.large,
                containerColor = Color.Unspecified,
            )
        }
        OverlayModeSelector(
            value = selectedElement.overlayMode,
            onValueChange = { mode ->
                onUpdateElement(true) { it.copy(overlayMode = mode) }
            },
        )
    }
}
