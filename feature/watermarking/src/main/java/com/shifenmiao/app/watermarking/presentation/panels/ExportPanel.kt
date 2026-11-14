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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.app.watermarking.domain.HiddenWatermark
import com.shifenmiao.app.watermarking.domain.model.HiddenWatermarkParams
import com.shifenmiao.app.watermarking.presentation.components.HiddenWatermarkInfo
import com.shifenmiao.app.watermarking.presentation.keepsHiddenWatermark
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.Quality
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.base.ui.button.ConfirmButton
import com.wanbaohe.core.ui.widget.controls.selection.ImageFormatSelector
import com.wanbaohe.core.ui.widget.controls.selection.QualitySelector
import com.wanbaohe.core.ui.widget.enhanced.EnhancedButtonGroup
import com.wanbaohe.core.ui.widget.preferences.PreferenceRowSwitch
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton

@Composable
fun ExportPanel(
    imageCount: Int,
    onlyCurrent: Boolean,
    keepExif: Boolean,
    stripLocationMetadata: Boolean,
    currentHiddenWatermark: HiddenWatermark?,
    hiddenWatermark: HiddenWatermarkParams,
    supportsHiddenWatermark: Boolean,
    imageFormat: ImageFormat,
    quality: Quality,
    onOnlyCurrentChange: (Boolean) -> Unit,
    onKeepExifChange: (Boolean) -> Unit,
    onStripLocationChange: (Boolean) -> Unit,
    onHiddenWatermarkChange: (HiddenWatermarkParams) -> Unit,
    onHiddenRejected: () -> Unit,
    onFormatChange: (ImageFormat) -> Unit,
    onQualityChange: (Quality) -> Unit,
    onSave: () -> Unit,
    onChooseLocation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        HiddenWatermarkInfo(currentHiddenWatermark)
        if (imageCount > 1) {
            EnhancedButtonGroup(
                items = listOf(
                    stringResource(R.string.watermark_current_image),
                    stringResource(R.string.watermark_all_images),
                ),
                selectedIndex = if (onlyCurrent) 0 else 1,
                onIndexChange = { onOnlyCurrentChange(it == 0) },
            )
            Spacer(Modifier.height(8.dp))
        }
        PreferenceRowSwitch(
            title = stringResource(R.string.keep_exif),
            checked = keepExif,
            onClick = onKeepExifChange,
            drawContainer = false,
        )
        PreferenceRowSwitch(
            title = stringResource(R.string.watermark_strip_location),
            subtitle = stringResource(R.string.watermark_strip_location_sub),
            checked = stripLocationMetadata,
            enabled = keepExif,
            onClick = onStripLocationChange,
            drawContainer = false,
        )
        PreferenceRowSwitch(
            title = stringResource(R.string.watermark_hidden_title),
            subtitle = stringResource(R.string.watermark_hidden_subtitle),
            checked = hiddenWatermark.enabled,
            onClick = { enabled ->
                if (enabled && !supportsHiddenWatermark) onHiddenRejected()
                else onHiddenWatermarkChange(hiddenWatermark.copy(enabled = enabled))
            },
            drawContainer = false,
        )
        if (hiddenWatermark.enabled) {
            PreferenceRowSwitch(
                title = stringResource(R.string.watermark_lsb_title),
                subtitle = stringResource(R.string.watermark_lsb_subtitle),
                checked = hiddenWatermark.isLSB,
                onClick = { isLSB ->
                    onHiddenWatermarkChange(hiddenWatermark.copy(isLSB = isLSB))
                },
                drawContainer = false,
            )
            // 常驻说明而非弹窗:LSB 写在像素最低位,有损压缩会直接抹掉。
            val lossy = !imageFormat.keepsHiddenWatermark()
            Text(
                text = stringResource(
                    if (lossy) R.string.watermark_hidden_lossy_warning
                    else R.string.watermark_hidden_lossless_hint
                ),
                style = MaterialTheme.typography.bodySmall,
                color = if (lossy) {
                    MaterialTheme.colorScheme.error
                } else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp),
            )
            Spacer(Modifier.height(4.dp))
        }
        QualitySelector(
            imageFormat = imageFormat,
            quality = quality,
            onQualityChange = onQualityChange,
        )
        ImageFormatSelector(
            value = imageFormat,
            onValueChange = onFormatChange,
        )
        Spacer(Modifier.height(12.dp))
        Row {
            OneSecondaryButton(
                text = stringResource(R.string.watermark_choose_location),
                onClick = onChooseLocation,
                modifier = Modifier.weight(1f),
            )
            ConfirmButton(
                text = stringResource(R.string.save),
                onClick = onSave,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
