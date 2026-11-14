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

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.resources.R
import com.shifenmiao.app.watermarking.domain.model.kind
import com.shifenmiao.app.watermarking.domain.preset.WatermarkPreset
import com.shifenmiao.app.watermarking.presentation.model.sampleText
import com.shifenmiao.app.watermarking.presentation.model.title
import com.wanbaohe.base.ui.button.ConfirmButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.image.Picture
import com.wanbaohe.core.ui.widget.text.RoundedTextField

@Composable
fun PresetPanel(
    presets: List<WatermarkPreset>,
    previewBitmap: Bitmap?,
    onApplyPreset: (WatermarkPreset) -> Unit,
    onSaveCurrent: (String) -> Unit,
    onDeletePreset: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var presetName by remember { mutableStateOf("") }
    Column(modifier = modifier, verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
        LazyRow(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
            items(presets, key = WatermarkPreset::id) { preset ->
                PresetCard(preset, previewBitmap, onApplyPreset, onDeletePreset)
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            RoundedTextField(
                value = presetName,
                onValueChange = { presetName = it.take(30) },
                label = { Text(stringResource(R.string.watermark_preset_name)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            ConfirmButton(
                text = stringResource(R.string.watermark_save_preset),
                enabled = presetName.isNotBlank(),
                onClick = {
                    onSaveCurrent(presetName)
                    presetName = ""
                },
            )
        }
    }
}

@Composable
private fun PresetCard(
    preset: WatermarkPreset,
    previewBitmap: Bitmap?,
    onApply: (WatermarkPreset) -> Unit,
    onDelete: (String) -> Unit,
) {
    val title = if (preset.nameRes != 0) stringResource(preset.nameRes) else preset.name
    val typeNames = preset.document.elements.map { it.content.kind.title() }.joinToString(" · ")
    Box {
        GlassCard(
            onClick = { onApply(preset) },
            modifier = Modifier.width(148.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(82.dp)) {
                previewBitmap?.let {
                    Picture(
                        model = it,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        showTransparencyChecker = false,
                    )
                }
                preset.document.elements.firstOrNull()?.let { element ->
                    Text(
                        text = element.content.sampleText(),
                        color = Color(element.style.color),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.Center).padding(8.dp),
                    )
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = typeNames,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (!preset.isBuiltin) {
            EnhancedIconButton(
                onClick = { onDelete(preset.id) },
                modifier = Modifier.align(Alignment.TopEnd).size(36.dp),
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.delete),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
