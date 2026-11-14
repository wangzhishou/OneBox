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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifenmiao.app.watermarking.presentation.WatermarkPanel
import com.shifenmiao.app.watermarking.presentation.panels.ContentPanel
import com.shifenmiao.app.watermarking.presentation.panels.ImageBatchPanel
import com.shifenmiao.app.watermarking.presentation.panels.LayerPanel
import com.shifenmiao.app.watermarking.presentation.panels.PresetPanel
import com.shifenmiao.app.watermarking.presentation.panels.StylePanel
import com.shifenmiao.app.watermarking.presentation.screenLogic.WatermarkingComponent

@Composable
fun WatermarkPanelHost(
    panel: WatermarkPanel,
    component: WatermarkingComponent,
    closeAfterPreset: Boolean,
    onClose: () -> Unit,
    onRequestLocation: () -> Unit,
    onPickImages: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (panel) {
        WatermarkPanel.PRESETS -> PresetPanel(
            presets = component.presets,
            previewBitmap = component.internalBitmap,
            onApplyPreset = { preset ->
                component.applyPreset(preset)
                if (closeAfterPreset) onClose()
            },
            onSaveCurrent = component::saveCurrentAsPreset,
            onDeletePreset = component::deleteCustomPreset,
            modifier = modifier,
        )
        WatermarkPanel.CONTENT -> ContentPanel(
            selectedElement = component.selectedElement,
            needsDeviceLocation = component.needsDeviceLocation,
            weatherAvailable = component.weatherAvailable,
            isDynamicDataLoading = component.isDynamicDataLoading,
            locationLoadFailed = component.locationLoadFailed,
            onKindChange = component::changeSelectedKind,
            onContentChange = component::updateSelectedContent,
            onRequestLocation = onRequestLocation,
            modifier = modifier,
        )
        WatermarkPanel.STYLE -> StylePanel(
            selectedElement = component.selectedElement,
            document = component.document,
            onUpdateElement = { recordHistory, transform ->
                component.updateSelectedElement(
                    resolve = true,
                    recordHistory = recordHistory,
                    transform = transform,
                )
            },
            onBeginCompoundEdit = component::beginCompoundEdit,
            onEndCompoundEdit = component::endCompoundEdit,
            onHiddenWatermarkChange = component::updateHiddenWatermark,
            modifier = modifier,
        )
        WatermarkPanel.LAYERS -> androidx.compose.foundation.layout.Column {
            HiddenWatermarkInfo(component.currentHiddenWatermark)
            LayerPanel(
                document = component.document,
                selectedElementId = component.selectedElementId,
                onSelect = component::selectElement,
                onToggleVisible = { id ->
                    component.updateElement(id) {
                        it.copy(transform = it.transform.copy(visible = !it.transform.visible))
                    }
                },
                onToggleLocked = { id ->
                    component.updateElement(id) {
                        it.copy(transform = it.transform.copy(locked = !it.transform.locked))
                    }
                },
                onMove = component::moveSelectedElement,
                onDuplicate = component::duplicateSelectedElement,
                onDelete = component::removeSelectedElement,
                modifier = modifier,
            )
        }
        WatermarkPanel.IMAGES -> ImageBatchPanel(
            uris = component.uris,
            selectedUri = component.selectedUri,
            onSelect = component::updateSelectedUri,
            onRemove = component::updateUrisSilently,
            onAdd = onPickImages,
            modifier = modifier,
        )
    }
}
