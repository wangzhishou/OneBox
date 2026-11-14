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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.Quality
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.base.ui.button.CancelButton
import com.wanbaohe.base.ui.button.ConfirmButton
import com.wanbaohe.core.ui.widget.controls.selection.ImageFormatSelector
import com.wanbaohe.core.ui.widget.controls.selection.QualitySelector
import com.wanbaohe.core.ui.widget.enhanced.EnhancedButtonGroup
import com.wanbaohe.core.ui.widget.preferences.PreferenceRowSwitch

@Composable
fun ExportPanel(
    imageCount: Int,
    onlyCurrent: Boolean,
    keepExif: Boolean,
    stripLocationMetadata: Boolean,
    imageFormat: ImageFormat,
    quality: Quality,
    onOnlyCurrentChange: (Boolean) -> Unit,
    onKeepExifChange: (Boolean) -> Unit,
    onStripLocationChange: (Boolean) -> Unit,
    onFormatChange: (ImageFormat) -> Unit,
    onQualityChange: (Quality) -> Unit,
    onSave: () -> Unit,
    onChooseLocation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
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
            CancelButton(
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
