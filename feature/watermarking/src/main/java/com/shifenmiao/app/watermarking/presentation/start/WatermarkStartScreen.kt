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
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.app.watermarking.domain.model.WatermarkDocument
import com.shifenmiao.app.watermarking.domain.preset.BuiltinWatermarkPresets
import com.shifenmiao.app.watermarking.domain.preset.WatermarkPreset
import com.shifenmiao.app.watermarking.presentation.components.PresetSchematic
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.base.ui.button.ConfirmButton
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.tintedGlassCardColors
import com.wanbaohe.watermark.template.domain.BuiltinTemplateId

/**
 * 新建水印的引导页。三个模板相当于 Android Studio 新建项目时的项目类型：
 * 先挑起点，再选照片；进入编辑器后仍可以增删图层。
 */
enum class WatermarkStartTemplate(
    val presetId: String?,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
) {
    BLANK(null, R.string.watermark_template_blank, R.string.watermark_template_blank_desc),
    REPEAT(
        BuiltinTemplateId.COPYRIGHT_TILE.id,
        R.string.watermark_template_repeat,
        R.string.watermark_template_repeat_desc,
    ),
    LEICA(
        BuiltinTemplateId.LEICA_CLASSIC.id,
        R.string.watermark_template_leica,
        R.string.watermark_template_leica_desc,
    );

    companion object {
        fun fromPresetId(id: String?): WatermarkStartTemplate =
            entries.firstOrNull { it.presetId == id } ?: BLANK
    }
}

private val CARD_SPACING = 12.dp
private val PREVIEW_HEIGHT = 140.dp

@Composable
fun WatermarkStartScreen(
    selected: WatermarkStartTemplate,
    onSelect: (WatermarkStartTemplate) -> Unit,
    onStart: () -> Unit,
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
        Text(
            text = stringResource(R.string.watermark_start_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))

        // Flex 式卡片网格：窄屏单列、平板两列、宽屏三列。卡片区独占剩余高度单独滚动，
        // 选择按钮不跟着滚，始终右对齐贴在内容区底部。
        BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val columns = when {
                maxWidth < 360.dp -> 1
                maxWidth < 840.dp -> 2
                else -> 3
            }
            val cardWidth: Dp = if (columns == 1) {
                maxWidth
            } else {
                (maxWidth - CARD_SPACING * (columns - 1)) / columns
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                maxItemsInEachRow = columns,
                horizontalArrangement = Arrangement.spacedBy(CARD_SPACING),
                verticalArrangement = Arrangement.spacedBy(CARD_SPACING),
            ) {
                WatermarkStartTemplate.entries.forEach { template ->
                    StartTemplateCard(
                        template = template,
                        selected = template == selected,
                        onClick = { onSelect(template) },
                        modifier = Modifier.width(cardWidth),
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            ConfirmButton(
                text = stringResource(R.string.watermark_start_pick_photo),
                onClick = onStart,
            )
        }
    }
}

@Composable
private fun StartTemplateCard(
    template: WatermarkStartTemplate,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        // 选中态走 tintedGlassCardColors：容器色与内容色一起变，标题/说明/勾选
        // 都读 LocalContentColor，因此背景一变文字自动跟着变，没有硬编码描边。
        GlassCard(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = if (selected) {
                tintedGlassCardColors(MaterialTheme.colorScheme.primaryContainer)
            } else null,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PREVIEW_HEIGHT)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
            ) {
                PresetSchematic(
                    preset = template.preset(),
                    modifier = Modifier.fillMaxWidth(),
                    height = PREVIEW_HEIGHT,
                )
                if (template == WatermarkStartTemplate.BLANK) {
                    Icon(
                        imageVector = MaterialIcons.Outlined.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.align(Alignment.Center).size(28.dp),
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(template.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    if (selected) {
                        Icon(
                            imageVector = MaterialIcons.Outlined.Check,
                            contentDescription = stringResource(R.string.watermark_start_selected),
                            tint = LocalContentColor.current,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(template.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalContentColor.current.copy(alpha = 0.78f),
                )
            }
        }
    }
}

@Composable
private fun WatermarkStartTemplate.preset(): WatermarkPreset = when (this) {
    WatermarkStartTemplate.BLANK -> WatermarkPreset(
        id = "start_blank",
        nameRes = R.string.watermark_template_blank,
        document = WatermarkDocument(elements = emptyList()),
        isBuiltin = true,
    )
    else -> BuiltinWatermarkPresets.findById(requireNotNull(presetId))!!
}
