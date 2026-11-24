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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.app.watermarking.domain.model.WatermarkDocument
import com.shifenmiao.app.watermarking.domain.preset.BuiltinWatermarkPresets
import com.shifenmiao.app.watermarking.domain.preset.WatermarkPreset
import com.shifenmiao.app.watermarking.presentation.components.PresetSchematic
import com.shifenmiao.app.watermarking.presentation.components.presetTypeNames
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.base.ui.button.ConfirmButton
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.tintedGlassCardColors
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
    selectedPresetId: String?,
    onSelect: (String?) -> Unit,
    customPresets: List<WatermarkPreset>,
    lastSignature: String,
    onStart: (WatermarkPreset?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedCustomPreset = customPresets.firstOrNull { it.id == selectedPresetId }
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
        Text(
            text = stringResource(R.string.watermark_start_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val columns = if (maxWidth < 360.dp) 1 else 2
            val width = with(LocalDensity.current) {
                ((maxWidth.roundToPx() - 12.dp.roundToPx() * (columns - 1)) / columns).toDp()
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                FlexBox(
                    modifier = Modifier.fillMaxWidth(),
                    config = {
                        wrap(FlexWrap.Wrap)
                        maxItemsInEachLine(columns)
                        columnGap(12.dp)
                        rowGap(12.dp)
                    },
                ) {
                    WatermarkStartTemplate.entries.forEach { item ->
                        val example = stringResource(item.descriptionRes)
                        val schematic = remember(item, example, lastSignature) {
                            item.makePreset(
                                if (item == WatermarkStartTemplate.SIGNATURE) {
                                    lastSignature.ifBlank { example }
                                } else {
                                    example
                                },
                            ) ?: WatermarkPreset(
                                id = "start_blank",
                                document = WatermarkDocument(elements = emptyList()),
                            )
                        }
                        WatermarkStartCard(
                            preset = schematic,
                            title = stringResource(item.titleRes),
                            description = example,
                            selected = selectedPresetId == item.presetId,
                            onClick = { onSelect(item.presetId) },
                            showAddIcon = item == WatermarkStartTemplate.BLANK,
                            modifier = Modifier.width(width),
                        )
                    }
                }
                if (customPresets.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.watermark_my_templates),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 24.dp, bottom = 12.dp),
                    )
                    FlexBox(
                        modifier = Modifier.fillMaxWidth(),
                        config = {
                            wrap(FlexWrap.Wrap)
                            maxItemsInEachLine(columns)
                            columnGap(12.dp)
                            rowGap(12.dp)
                        },
                    ) {
                        customPresets.forEach { preset ->
                            WatermarkStartCard(
                                preset = preset,
                                title = if (preset.nameRes != 0) {
                                    stringResource(preset.nameRes)
                                } else {
                                    preset.name
                                },
                                description = presetTypeNames(preset),
                                selected = selectedPresetId == preset.id,
                                onClick = { onSelect(preset.id) },
                                modifier = Modifier.width(width),
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ConfirmButton(
                text = stringResource(R.string.watermark_start_pick_photo),
                onClick = {
                    val selected = WatermarkStartTemplate.fromPresetId(selectedPresetId)
                    onStart(
                        selectedCustomPreset ?: selected.makePreset(
                            if (selected == WatermarkStartTemplate.SIGNATURE) lastSignature else "",
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun WatermarkStartCard(
    preset: WatermarkPreset,
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showAddIcon: Boolean = false,
) {
    GlassCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = if (selected) {
            tintedGlassCardColors(MaterialTheme.colorScheme.primaryContainer)
        } else {
            null
        },
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        ) {
            PresetSchematic(preset, height = 140.dp)
            if (showAddIcon) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(28.dp),
                )
            }
        }
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (selected) {
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = stringResource(R.string.watermark_start_selected),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            if (description.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun WatermarkStartTemplate.makePreset(text: String): WatermarkPreset? = when (this) {
    WatermarkStartTemplate.DOCUMENT, WatermarkStartTemplate.SIGNATURE ->
        BuiltinWatermarkPresets.textPreset(requireNotNull(presetId), titleRes, text)

    else -> presetId?.let(BuiltinWatermarkPresets::findById)
}
