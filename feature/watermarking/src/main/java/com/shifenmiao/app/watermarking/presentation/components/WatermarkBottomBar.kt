/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFilters
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoAwesomeMosaic
import com.shifenmiao.app.watermarking.presentation.WatermarkPanel
import com.shifenmiao.app.watermarking.presentation.title
import com.wanbaohe.base.ui.button.ConfirmButton
import com.wanbaohe.core.ui.widget.editor.EditorToolLabel
import com.wanbaohe.core.ui.widget.glass.glassDense
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults

@Composable
fun WatermarkBottomBar(
    activePanel: WatermarkPanel?,
    isPrivacyActive: Boolean,
    isToolRailVisible: Boolean,
    isFilterPanelVisible: Boolean,
    onPanelClick: (WatermarkPanel) -> Unit,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassDense(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            // 手势区 inset 只在玻璃底色内部让位;竖直 padding 收到 4dp,
            // 避免 56dp 条目 + 8dp 内边距 + 系统 inset 叠出一条过高的空带。
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 标签本体原本是紧贴的(默认间距 0),未选中项背景透明时看不出来;
        // 一旦相邻两项同时高亮(如 Filter + Tools),两块蓝色胶囊就会贴成一片。
        // 给整行统一留 4dp 间距,让高亮项之间也有可见间隙;条目宽度同步
        // 56→52dp,总宽 332dp(原 336dp),6 个标签仍完整显示、不被 Export 挤掉。
        Row(
            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            WatermarkPanel.entries.filter { panel ->
                if (isPrivacyActive) {
                    panel != WatermarkPanel.PROPERTIES &&
                        panel != WatermarkPanel.LAYERS &&
                        panel != WatermarkPanel.TOOLS
                } else {
                    panel != WatermarkPanel.PRIVACY
                }
            }.forEach { panel ->
                val selected = when (panel) {
                    WatermarkPanel.TOOLS -> isToolRailVisible
                    WatermarkPanel.FILTER -> isFilterPanelVisible
                    else -> panel == activePanel
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .size(width = 52.dp, height = 52.dp)
                        .clip(ShapeDefaults.default)
                        .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .clickable { onPanelClick(panel) }
                        .padding(2.dp),
                ) {
                    Icon(
                        imageVector = when (panel) {
                            WatermarkPanel.PRESETS -> MaterialIcons.Outlined.Star
                            WatermarkPanel.FILTER -> Icons.Outlined.LineFilters
                            WatermarkPanel.TOOLS -> MaterialIcons.Outlined.Build
                            WatermarkPanel.PROPERTIES -> MaterialIcons.Outlined.Tune
                            WatermarkPanel.LAYERS -> MaterialIcons.Outlined.Layers
                            WatermarkPanel.PRIVACY -> Icons.Outlined.LineAutoAwesomeMosaic
                            WatermarkPanel.IMAGES -> Icons.Outlined.LineImage
                        },
                        contentDescription = panel.title(),
                        tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    EditorToolLabel(
                        text = panel.title(),
                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Spacer(Modifier.width(4.dp))
        ConfirmButton(text = stringResource(R.string.export), onClick = onExportClick)
    }
}
