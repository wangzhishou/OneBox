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
import com.shifenmiao.app.watermarking.domain.model.usesDeviceLocation
import com.shifenmiao.app.watermarking.domain.preset.WatermarkPreset
import com.shifenmiao.app.watermarking.presentation.WatermarkPanel
import com.shifenmiao.app.watermarking.presentation.panels.ImageBatchPanel
import com.shifenmiao.app.watermarking.presentation.panels.LayerPanel
import com.shifenmiao.app.watermarking.presentation.panels.PresetPanel
import com.shifenmiao.app.watermarking.presentation.panels.PropertiesPanel
import com.shifenmiao.app.watermarking.presentation.screenLogic.WatermarkingComponent

@Composable
fun WatermarkPanelHost(
    panel: WatermarkPanel,
    component: WatermarkingComponent,
    onPresetApplied: (WatermarkPreset) -> Unit,
    onRequestLocation: () -> Unit,
    onPickImages: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (panel) {
        WatermarkPanel.TOOLS -> Unit
        WatermarkPanel.FILTER -> Unit
        WatermarkPanel.PRESETS -> PresetPanel(
            presets = component.presets,
            hasLayers = component.document.elements.isNotEmpty(),
            onApplyPreset = { preset ->
                component.applyPreset(preset)
                onPresetApplied(preset)
                if (preset.document.usesDeviceLocation()) onRequestLocation()
            },
            onSaveCurrent = { name, result -> component.saveCurrentAsPreset(name, result) },
            onDeletePreset = { id, result -> component.deleteCustomPreset(id, result) },
            modifier = modifier,
        )
        WatermarkPanel.PROPERTIES -> PropertiesPanel(
            selectedElement = component.selectedElement,
            resolvedElement = component.resolvedElements.firstOrNull {
                it.element.id == component.selectedElementId
            },
            needsDeviceLocation = component.needsDeviceLocation,
            weatherAvailable = component.weatherAvailable,
            isDynamicDataLoading = component.isDynamicDataLoading,
            locationLoadFailed = component.locationLoadFailed,
            onContentChange = { content, mergeHistory ->
                component.updateSelectedContent(content, mergeHistory)
            },
            onRequestLocation = onRequestLocation,
            onUpdateElement = { recordHistory, transform ->
                component.updateSelectedElement(
                    resolve = true,
                    recordHistory = recordHistory,
                    transform = transform,
                )
            },
            onBeginCompoundEdit = component::beginCompoundEdit,
            onEndCompoundEdit = component::endCompoundEdit,
            modifier = modifier,
        )
        WatermarkPanel.LAYERS -> LayerPanel(
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
            onDelete = component::removeSelectedElement,
            resolvedElements = component.resolvedElements,
            modifier = modifier,
        )
        WatermarkPanel.PRIVACY -> com.wanbaohe.privacy.redaction.presentation.PrivacyRedactionPanel(
            document = component.privacy.document,
            analysisState = component.privacy.analysisState,
            selectedRegionId = component.privacy.selectedRegionId,
            isDrawing = component.privacy.isDrawing,
            onRescan = component.privacy::analyzeCurrent,
            onSelectRegion = component.privacy::selectRegion,
            onToggleRegion = component.privacy::toggleRegion,
            onDeleteRegion = component.privacy::deleteRegion,
            onDrawingChange = component.privacy::setDrawingMode,
            onMaskChange = component.privacy::updateMask,
            onStrengthChange = component.privacy::updateStrength,
            onDisable = component::disablePrivacyMode,
            modifier = modifier,
        )
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
