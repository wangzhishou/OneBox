/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.presentation.start

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.remember
import com.shifenmiao.app.watermarking.domain.model.WatermarkDocument
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.tintedGlassCardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.app.watermarking.domain.preset.BuiltinWatermarkPresets
import com.shifenmiao.app.watermarking.domain.preset.WatermarkPreset
import com.shifenmiao.app.watermarking.presentation.components.PresetSchematic
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.base.ui.button.ConfirmButton
import com.wanbaohe.watermark.template.domain.BuiltinTemplateId

/** 按用途起步；自由编辑不需要预设。 */
enum class WatermarkStartTemplate(
    val presetId: String?,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
) {
    BLANK(null, R.string.watermark_template_blank, R.string.watermark_template_blank_desc),
    REPEAT(BuiltinTemplateId.COPYRIGHT_TILE.id, R.string.watermark_template_repeat, R.string.watermark_template_repeat_desc),
    LEICA(BuiltinTemplateId.LEICA_CLASSIC.id, R.string.watermark_template_leica, R.string.watermark_template_leica_desc),
    MOUNT(
        BuiltinWatermarkPresets.PHOTO_MOUNT,
        R.string.watermark_photo_mount,
        R.string.watermark_photo_mount_desc,
    ),
    DOCUMENT(
        BuiltinWatermarkPresets.DOCUMENT_USE,
        R.string.watermark_use_document,
        R.string.watermark_use_example,
    ),
    SIGNATURE(
        BuiltinWatermarkPresets.SIGNATURE,
        R.string.watermark_use_signature,
        R.string.watermark_signature_example,
    ),
    ;

    companion object {
        fun fromPresetId(id: String?): WatermarkStartTemplate =
            entries.firstOrNull { it.presetId == id } ?: BLANK
    }
}

@Composable
fun WatermarkStartScreen(
    selected: WatermarkStartTemplate,
    onSelect: (WatermarkStartTemplate) -> Unit,
    lastSignature: String,
    onStart: (WatermarkPreset?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 20.dp),
    ) {
        Text(
            text = stringResource(R.string.watermark_start_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.watermark_start_subtitle), style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val columns = when { maxWidth < 360.dp -> 1; maxWidth < 840.dp -> 2; else -> 3 }
            val width = (maxWidth - 12.dp * (columns - 1)) / columns
            FlowRow(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                maxItemsInEachRow = columns,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                WatermarkStartTemplate.entries.forEach { item ->
                    val example = stringResource(item.descriptionRes)
                    val schematic = remember(item, example, lastSignature) {
                        item.makePreset(if (item == WatermarkStartTemplate.SIGNATURE) lastSignature.ifBlank { example } else example)
                            ?: WatermarkPreset(id = "start_blank", document = WatermarkDocument(elements = emptyList()))
                    }
                    GlassCard(onClick = { onSelect(item) }, modifier = Modifier.width(width),
                        shape = RoundedCornerShape(20.dp),
                        colors = if (selected == item) tintedGlassCardColors(MaterialTheme.colorScheme.primaryContainer) else null,
                    ) {
                        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))) {
                            PresetSchematic(schematic, height = 140.dp)
                            if (item == WatermarkStartTemplate.BLANK) Icon(Icons.Outlined.Add, null,
                                Modifier.align(Alignment.Center).size(28.dp))
                        }
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(item.titleRes), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                                if (item == selected) Icon(Icons.Outlined.Check, stringResource(R.string.watermark_start_selected), Modifier.size(20.dp))
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(example, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ConfirmButton(
                text = stringResource(R.string.watermark_start_pick_photo),
                onClick = { onStart(selected.makePreset(if (selected == WatermarkStartTemplate.SIGNATURE) lastSignature else "")) },
            )
        }
    }
}

private fun WatermarkStartTemplate.makePreset(text: String): WatermarkPreset? = when (this) {
    WatermarkStartTemplate.DOCUMENT, WatermarkStartTemplate.SIGNATURE ->
        BuiltinWatermarkPresets.textPreset(requireNotNull(presetId), titleRes, text)
    else -> presetId?.let(BuiltinWatermarkPresets::findById)
}
