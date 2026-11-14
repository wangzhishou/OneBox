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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.R
import com.shifenmiao.app.watermarking.domain.model.ResolvedWatermarkElement
import com.shifenmiao.app.watermarking.domain.model.WatermarkContent
import com.shifenmiao.app.watermarking.domain.model.WatermarkDocument
import com.shifenmiao.app.watermarking.domain.model.kind
import com.shifenmiao.app.watermarking.presentation.model.sampleText
import com.shifenmiao.app.watermarking.presentation.model.title
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton
import com.wanbaohe.core.ui.widget.glass.GlassCard

@Composable
fun LayerPanel(
    document: WatermarkDocument,
    selectedElementId: String?,
    onSelect: (String) -> Unit,
    onToggleVisible: (String) -> Unit,
    onToggleLocked: (String) -> Unit,
    onMove: (Int) -> Unit,
    onDelete: () -> Unit,
    resolvedElements: List<ResolvedWatermarkElement>,
    modifier: Modifier = Modifier,
) {
    val resolvedById = resolvedElements.associateBy { it.element.id }
    val selected = document.elements.firstOrNull { it.id == selectedElementId }
    val canArrange = selected != null && !selected.transform.locked
    Column(modifier = modifier) {
        if (document.elements.isEmpty()) {
            EmptySelectionHint(R.string.watermark_no_layers)
        }
        document.elements.asReversed().forEach { element ->
            GlassCard(
                onClick = { onSelect(element.id) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = element.content.kind.title(),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (element.id == selectedElementId) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = layerSubtitle(element.content, resolvedById[element.id]),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    LayerAction(
                        icon = if (element.transform.visible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        description = stringResource(R.string.watermark_toggle_visibility),
                    ) { onToggleVisible(element.id) }
                    LayerAction(
                        icon = if (element.transform.locked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                        description = stringResource(R.string.watermark_toggle_lock),
                    ) { onToggleLocked(element.id) }
                }
            }
        }
        if (document.elements.isNotEmpty()) {
            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)) {
                LayerAction(Icons.Outlined.ArrowUpward, stringResource(R.string.watermark_bring_forward), canArrange) { onMove(1) }
                LayerAction(Icons.Outlined.ArrowDownward, stringResource(R.string.watermark_send_backward), canArrange) { onMove(-1) }
                LayerAction(Icons.Outlined.Delete, stringResource(R.string.delete), selected != null) { onDelete() }
            }
        }
    }
}

@Composable
private fun layerSubtitle(
    content: WatermarkContent,
    resolved: ResolvedWatermarkElement?,
): String {
    val resolvedText = resolved?.text
    if (!resolvedText.isNullOrBlank() && resolved.hasData) return resolvedText
    if (content is WatermarkContent.Image && content.imageData.isBlank()) {
        return stringResource(R.string.watermark_pick_logo)
    }
    return content.sampleText()
}

@Composable
private fun LayerAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    EnhancedIconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(40.dp)) {
        Icon(icon, contentDescription = description, modifier = Modifier.size(18.dp))
    }
}
