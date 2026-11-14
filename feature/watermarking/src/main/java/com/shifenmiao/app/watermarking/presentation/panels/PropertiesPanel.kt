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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifenmiao.app.watermarking.domain.model.WatermarkContent
import com.shifenmiao.app.watermarking.domain.model.WatermarkElement
import com.t8rin.imagetoolbox.core.resources.R

@Composable
fun PropertiesPanel(
    selectedElement: WatermarkElement?,
    needsDeviceLocation: Boolean,
    weatherAvailable: Boolean,
    isDynamicDataLoading: Boolean,
    locationLoadFailed: Boolean,
    onContentChange: (WatermarkContent, Boolean) -> Unit,
    onRequestLocation: () -> Unit,
    onUpdateElement: (recordHistory: Boolean, transform: (WatermarkElement) -> WatermarkElement) -> Unit,
    onBeginCompoundEdit: () -> Unit,
    onEndCompoundEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        when {
            selectedElement == null -> EmptySelectionHint(R.string.watermark_no_selection)
            selectedElement.transform.locked -> EmptySelectionHint(R.string.watermark_layer_locked)
            else -> {
                ContentPanel(
                    selectedElement = selectedElement,
                    needsDeviceLocation = needsDeviceLocation,
                    weatherAvailable = weatherAvailable,
                    isDynamicDataLoading = isDynamicDataLoading,
                    locationLoadFailed = locationLoadFailed,
                    onContentChange = onContentChange,
                    onRequestLocation = onRequestLocation,
                )
                Spacer(Modifier.height(16.dp))
                StylePanel(
                    selectedElement = selectedElement,
                    onUpdateElement = onUpdateElement,
                    onBeginCompoundEdit = onBeginCompoundEdit,
                    onEndCompoundEdit = onEndCompoundEdit,
                )
            }
        }
    }
}
