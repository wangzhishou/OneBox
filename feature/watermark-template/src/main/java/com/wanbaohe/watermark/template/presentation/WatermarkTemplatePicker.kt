/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.watermark.template.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wanbaohe.watermark.template.domain.TemplateCatalog
import com.wanbaohe.watermark.template.domain.WatermarkTemplate

/**
 * 模板选择器骨架。
 *
 * 宿主通过 [thumbnail] 决定缩略图画法（图片水印页真渲染，拍照入口用静态封面），
 * 并按 (presetId, updatedAt) 缓存，避免一屏多张时重复跑 visualFactory。
 * 卡片外观在 P2 接入玻璃管线与选中态，这里只固定契约与列表结构。
 */
@Composable
fun WatermarkTemplatePicker(
    catalog: TemplateCatalog,
    onTemplateSelected: (WatermarkTemplate) -> Unit,
    modifier: Modifier = Modifier,
    selectedId: String? = null,
    thumbnail: @Composable (WatermarkTemplate) -> Unit = {},
) {
    val templates by catalog.observe().collectAsState(initial = emptyList())
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(templates, key = { it.id }) { template ->
            Column(modifier = Modifier.width(120.dp)) {
                thumbnail(template)
                Text(
                    text = if (template.nameRes != 0) {
                        stringResource(template.nameRes)
                    } else {
                        template.name
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (template.id == selectedId) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                androidx.compose.material3.TextButton(onClick = { onTemplateSelected(template) }) {
                    Text(stringResource(com.wanbaohe.watermark.template.R.string.watermark_template_apply))
                }
            }
        }
    }
}
