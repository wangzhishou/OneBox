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
import com.shifenmiao.app.watermarking.presentation.WatermarkPanel
import com.shifenmiao.app.watermarking.presentation.title
import com.wanbaohe.base.ui.button.ConfirmButton
import com.wanbaohe.core.ui.widget.editor.EditorToolLabel
import com.wanbaohe.core.ui.widget.glass.glassDense
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults

@Composable
fun WatermarkBottomBar(
    activePanel: WatermarkPanel?,
    onPanelClick: (WatermarkPanel) -> Unit,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassDense(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .navigationBarsPadding()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState())) {
            WatermarkPanel.entries.forEach { panel ->
                val selected = panel == activePanel
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .size(width = 62.dp, height = 56.dp)
                        .clip(ShapeDefaults.default)
                        .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .clickable { onPanelClick(panel) }
                        .padding(4.dp),
                ) {
                    Icon(
                        imageVector = when (panel) {
                            WatermarkPanel.PRESETS -> MaterialIcons.Outlined.Star
                            WatermarkPanel.PROPERTIES -> MaterialIcons.Outlined.Tune
                            WatermarkPanel.LAYERS -> MaterialIcons.Outlined.Layers
                            WatermarkPanel.IMAGES -> Icons.Outlined.LineImage
                        },
                        contentDescription = panel.title(),
                        tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp),
                    )
                    EditorToolLabel(
                        text = panel.title(),
                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        ConfirmButton(text = stringResource(R.string.export), onClick = onExportClick)
    }
}
