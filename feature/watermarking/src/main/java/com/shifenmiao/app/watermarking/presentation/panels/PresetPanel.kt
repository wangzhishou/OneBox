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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.R
import com.shifenmiao.app.watermarking.domain.preset.WatermarkPreset
import com.shifenmiao.app.watermarking.presentation.components.PresetSchematic
import com.shifenmiao.app.watermarking.presentation.components.presetTypeNames
import com.wanbaohe.base.ui.button.ConfirmButton
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.system.OneBoxDangerButton
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import com.wanbaohe.core.ui.widget.text.RoundedTextField

/** 编辑器里的模板面板：内置预设 + 用户保存的模板。 */
@Composable
fun PresetPanel(
    presets: List<WatermarkPreset>,
    hasLayers: Boolean,
    appliesWithoutReplacing: (WatermarkPreset) -> Boolean,
    onApplyPreset: (WatermarkPreset) -> Unit,
    onSaveCurrent: (String, (Boolean) -> Unit) -> Unit,
    onDeletePreset: (String, (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    var presetName by rememberSaveable { mutableStateOf("") }
    var pendingApply by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDelete by rememberSaveable { mutableStateOf<String?>(null) }
    val presetToApply = presets.firstOrNull { it.id == pendingApply }
    val presetToDelete = presets.firstOrNull { it.id == pendingDelete }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(presets, key = WatermarkPreset::id) { preset ->
                PresetCard(
                    preset = preset,
                    onApply = {
                        if (hasLayers && !appliesWithoutReplacing(preset)) pendingApply = preset.id
                        else onApplyPreset(preset)
                    },
                    onDelete = { pendingDelete = preset.id },
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                    onSaveCurrent(presetName) { success ->
                        if (success) {
                            presetName = ""
                            AppToastHost.showToast(R.string.watermark_preset_saved)
                        } else {
                            AppToastHost.showFailureToast(R.string.watermark_preset_save_failed)
                        }
                    }
                },
            )
        }
    }
    EnhancedAlertDialog(
        visible = presetToApply != null,
        onDismissRequest = { pendingApply = null },
        title = { Text(stringResource(R.string.watermark_replace_preset_title)) },
        text = { Text(stringResource(R.string.watermark_replace_preset_sub)) },
        dismissButton = {
            OneSecondaryButton(
                text = stringResource(R.string.close),
                onClick = { pendingApply = null },
            )
        },
        confirmButton = {
            ConfirmButton(
                text = stringResource(R.string.watermark_replace_preset_confirm),
                onClick = {
                    presetToApply?.let(onApplyPreset)
                    pendingApply = null
                },
            )
        },
    )
    EnhancedAlertDialog(
        visible = presetToDelete != null,
        onDismissRequest = { pendingDelete = null },
        title = { Text(stringResource(R.string.watermark_delete_preset_title)) },
        text = { Text(stringResource(R.string.watermark_delete_preset_sub)) },
        dismissButton = {
            OneSecondaryButton(
                text = stringResource(R.string.close),
                onClick = { pendingDelete = null },
            )
        },
        confirmButton = {
            OneBoxDangerButton(
                text = stringResource(R.string.delete),
                onClick = {
                    val id = pendingDelete
                    pendingDelete = null
                    if (id != null) {
                        onDeletePreset(id) { success ->
                            if (success) AppToastHost.showToast(R.string.watermark_preset_deleted)
                            else AppToastHost.showFailureToast(R.string.watermark_preset_delete_failed)
                        }
                    }
                },
            )
        },
    )
}

@Composable
private fun PresetCard(
    preset: WatermarkPreset,
    onApply: () -> Unit,
    onDelete: () -> Unit,
) {
    val title = if (preset.nameRes != 0) stringResource(preset.nameRes) else preset.name
    Box {
        GlassCard(
            onClick = onApply,
            modifier = Modifier.width(148.dp),
        ) {
            PresetSchematic(preset)
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!preset.isBuiltin) {
                    Text(
                        text = presetTypeNames(preset),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        if (!preset.isBuiltin) {
            EnhancedIconButton(
                onClick = onDelete,
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
