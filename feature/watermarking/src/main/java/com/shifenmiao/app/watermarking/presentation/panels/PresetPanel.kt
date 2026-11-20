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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.Icons as CoreIcons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoAwesomeMosaic
import com.shifenmiao.app.watermarking.domain.model.WatermarkContent
import com.shifenmiao.app.watermarking.domain.model.WatermarkElement
import com.shifenmiao.app.watermarking.domain.model.kind
import com.shifenmiao.app.watermarking.domain.preset.WatermarkPreset
import com.shifenmiao.app.watermarking.presentation.model.sampleText
import com.shifenmiao.app.watermarking.presentation.model.title
import com.wanbaohe.watermark.template.domain.BuiltinTemplateId
import com.wanbaohe.base.ui.button.ConfirmButton
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.system.OneBoxDangerButton
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import com.wanbaohe.core.ui.widget.text.RoundedTextField
import com.wanbaohe.watermark.template.R as TemplateR

@Composable
fun PresetPanel(
    presets: List<WatermarkPreset>,
    hasLayers: Boolean,
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
    Column(modifier = modifier, verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
        LazyRow(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
            items(presets, key = WatermarkPreset::id) { preset ->
                PresetCard(
                    preset = preset,
                    onApply = {
                        if (hasLayers) pendingApply = preset.id else onApplyPreset(preset)
                    },
                    onDelete = { pendingDelete = preset.id },
                )
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
    val typeNames = if (preset.id == com.wanbaohe.watermark.template.domain.BuiltinTemplateId.PRIVACY_REDACTION.id) {
        stringResource(com.wanbaohe.privacy.redaction.R.string.privacy_redaction_subtitle)
    } else {
        preset.document.elements.map { it.content.kind.title() }.joinToString(" · ")
    }
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
                        text = typeNames,
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

@Composable
private fun PresetSchematic(preset: WatermarkPreset) {
    if (preset.id == BuiltinTemplateId.LEICA_CLASSIC.id) {
        LeicaPresetSchematic()
        return
    }
    if (preset.id == BuiltinTemplateId.PRIVACY_REDACTION.id) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = CoreIcons.Outlined.LineAutoAwesomeMosaic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(48.dp),
            )
        }
        return
    }
    val elements = preset.document.elements.take(3)
    val ink = elements.firstOrNull()?.let { Color(it.style.color) } ?: Color.White
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .clipToBounds()
            .background(
                if (ink.luminance() > 0.65f) Color(0xFF2C2C2C)
                else MaterialTheme.colorScheme.surfaceContainer
            ),
    ) {
        elements.forEach { element ->
            val spots = if (element.layout.isRepeated) {
                listOf(
                    -0.05f to 0.12f, 0.35f to 0.12f, 0.75f to 0.12f,
                    0.15f to 0.4f, 0.55f to 0.4f, 0.95f to 0.4f,
                    -0.05f to 0.68f, 0.35f to 0.68f, 0.75f to 0.68f,
                    0.15f to 0.96f, 0.55f to 0.96f, 0.95f to 0.96f,
                )
            } else {
                listOf(element.transform.centerX to element.transform.centerY)
            }
            spots.forEach { (x, y) ->
                SchematicLabel(
                    text = presetLabel(element),
                    color = Color(element.style.color).copy(
                        alpha = Color(element.style.color).alpha * element.transform.alpha,
                    ),
                    x = x,
                    y = y,
                    rotation = element.transform.rotation,
                    width = maxWidth,
                    height = maxHeight,
                )
            }
        }
    }
}

@Composable
private fun LeicaPresetSchematic() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .background(Color(0xFF2C2C2C)),
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(30.dp)
                .background(Color.White)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(Modifier.width(30.dp).height(3.dp).background(Color(0xFF202020)))
                Spacer(Modifier.height(5.dp))
                Box(Modifier.width(23.dp).height(2.dp).background(Color(0xFF888888)))
            }
            Image(
                painter = painterResource(TemplateR.drawable.leica_logo),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Box(Modifier.width(1.dp).height(18.dp).background(Color(0xFFCCCCCC)))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(Modifier.width(30.dp).height(3.dp).background(Color(0xFF202020)))
                Spacer(Modifier.height(5.dp))
                Box(Modifier.width(23.dp).height(2.dp).background(Color(0xFF888888)))
            }
        }
    }
}

@Composable
private fun SchematicLabel(
    text: String,
    color: Color,
    x: Float,
    y: Float,
    rotation: Float,
    width: Dp,
    height: Dp,
) {
    Text(
        text = text,
        color = color,
        fontSize = 11.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .offset(x = width * x - 36.dp, y = height * y - 8.dp)
            .width(72.dp)
            .graphicsLayer { rotationZ = rotation },
    )
}

@Composable
private fun presetLabel(element: WatermarkElement): String {
    val content = element.content
    if (content is WatermarkContent.Image && content.imageData.isBlank() && content.builtinResId == 0) {
        return stringResource(R.string.watermark_pick_logo)
    }
    return content.sampleText()
}
