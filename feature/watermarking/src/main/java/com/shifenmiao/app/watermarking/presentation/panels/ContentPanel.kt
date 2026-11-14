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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.R
import com.shifenmiao.app.watermarking.domain.model.WatermarkContent
import com.shifenmiao.app.watermarking.domain.model.WatermarkElement
import com.shifenmiao.app.watermarking.domain.model.WatermarkValueSource
import com.shifenmiao.app.watermarking.domain.model.kind
import com.shifenmiao.app.watermarking.domain.model.sourceOrNull
import com.shifenmiao.app.watermarking.presentation.model.title
import com.wanbaohe.base.ui.button.ConfirmButton
import com.wanbaohe.core.ui.widget.controls.selection.ImageSelector
import com.wanbaohe.core.ui.widget.enhanced.EnhancedButtonGroup
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.core.ui.widget.modifier.flatGlassContainer
import com.wanbaohe.core.ui.widget.preferences.PreferenceRowSwitch
import com.wanbaohe.core.ui.widget.text.RoundedTextField

@Composable
fun ContentPanel(
    selectedElement: WatermarkElement?,
    needsDeviceLocation: Boolean,
    weatherAvailable: Boolean,
    isDynamicDataLoading: Boolean,
    locationLoadFailed: Boolean,
    onContentChange: (WatermarkContent, Boolean) -> Unit,
    onRequestLocation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (selectedElement == null) {
        EmptySelectionHint(R.string.watermark_no_selection, modifier)
        return
    }
    if (selectedElement.transform.locked) {
        EmptySelectionHint(R.string.watermark_layer_locked, modifier)
        return
    }
    val content = selectedElement.content
    Column(modifier = modifier) {
        Text(
            text = content.kind.title(),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        when (content) {
            is WatermarkContent.Text -> WatermarkTextField(content.text) {
                onContentChange(content.copy(text = it), true)
            }
            is WatermarkContent.Stamp -> WatermarkTextField(content.text) {
                onContentChange(content.copy(text = it), true)
            }
            is WatermarkContent.Image -> ImageSelector(
                value = content.imageData,
                title = stringResource(R.string.watermark_pick_logo),
                subtitle = stringResource(
                    if (content.imageData.isBlank()) R.string.watermark_pick_logo_sub
                    else R.string.watermarking_image_sub
                ),
                onValueChange = { onContentChange(content.copy(imageData = it.toString()), false) },
                modifier = Modifier.fillMaxWidth(),
            )
            is WatermarkContent.Timestamp -> {
                TimestampFormats(content.format) { format ->
                    onContentChange(content.copy(format = format), false)
                }
                Spacer(Modifier.height(8.dp))
                WatermarkTextField(
                    value = content.format,
                    labelRes = R.string.watermark_time_format,
                ) { onContentChange(content.copy(format = it), true) }
                PreferenceRowSwitch(
                    title = stringResource(R.string.watermark_use_photo_time),
                    checked = content.usePhotoTime,
                    onClick = { onContentChange(content.copy(usePhotoTime = it), false) },
                    drawContainer = false,
                )
            }
            else -> DynamicContentControls(
                content = content,
                onContentChange = onContentChange,
            )
        }
        val source = content.sourceOrNull()
        if (source != null && source != WatermarkValueSource.MANUAL) {
            if (source == WatermarkValueSource.DEVICE || needsDeviceLocation) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ConfirmButton(
                        text = stringResource(R.string.watermark_use_current_location),
                        enabled = !isDynamicDataLoading,
                        onClick = onRequestLocation,
                        modifier = Modifier.weight(1f),
                    )
                    if (isDynamicDataLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
            val status = when {
                locationLoadFailed && (source == WatermarkValueSource.DEVICE || needsDeviceLocation) ->
                    R.string.watermark_location_failed
                content is WatermarkContent.Weather && !weatherAvailable ->
                    R.string.watermark_weather_unavailable
                needsDeviceLocation && source == WatermarkValueSource.PHOTO ->
                    R.string.watermark_missing_dynamic_data
                source == WatermarkValueSource.DEVICE && needsDeviceLocation ->
                    R.string.watermark_tap_for_location
                else -> null
            }
            if (status != null) StatusText(status)
        }
    }
}

@Composable
private fun TimestampFormats(
    selected: String,
    onSelect: (String) -> Unit,
) {
    val patterns = listOf(
        stringResource(R.string.watermark_time_date),
        stringResource(R.string.watermark_time_datetime),
        stringResource(R.string.watermark_time_local),
        stringResource(R.string.watermark_time_clock),
    )
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        patterns.forEach { pattern ->
            val chosen = pattern == selected
            Text(
                text = pattern,
                style = MaterialTheme.typography.labelMedium,
                color = if (chosen) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (chosen) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainer
                    )
                    .clickable { onSelect(pattern) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun DynamicContentControls(
    content: WatermarkContent,
    onContentChange: (WatermarkContent, Boolean) -> Unit,
) {
    val source = content.sourceOrNull() ?: return
    val sources = WatermarkValueSource.entries
    EnhancedButtonGroup(
        items = sources.map {
            stringResource(
                when (it) {
                    WatermarkValueSource.PHOTO -> R.string.watermark_source_photo
                    WatermarkValueSource.DEVICE -> R.string.watermark_source_device
                    WatermarkValueSource.MANUAL -> R.string.watermark_source_manual
                }
            )
        },
        selectedIndex = sources.indexOf(source),
        onIndexChange = { index ->
            val newSource = sources[index]
            onContentChange(
                when (content) {
                    is WatermarkContent.Altitude -> content.copy(source = newSource)
                    is WatermarkContent.Weather -> content.copy(source = newSource)
                    is WatermarkContent.Coordinates -> content.copy(source = newSource)
                    else -> content
                },
                false,
            )
        },
        inactiveButtonColor = MaterialTheme.colorScheme.surfaceContainer,
    )
    if (source == WatermarkValueSource.MANUAL) {
        Spacer(Modifier.height(8.dp))
        WatermarkTextField(
            value = when (content) {
                is WatermarkContent.Altitude -> content.manualValue
                is WatermarkContent.Weather -> content.manualValue
                is WatermarkContent.Coordinates -> content.manualValue
                else -> ""
            },
            labelRes = R.string.watermark_manual_value,
        ) { value ->
            onContentChange(
                when (content) {
                    is WatermarkContent.Altitude -> content.copy(manualValue = value)
                    is WatermarkContent.Weather -> content.copy(manualValue = value)
                    is WatermarkContent.Coordinates -> content.copy(manualValue = value)
                    else -> content
                },
                true,
            )
        }
    }
    if (source == WatermarkValueSource.MANUAL) return
    when (content) {
        is WatermarkContent.Altitude -> PreferenceRowSwitch(
            title = stringResource(R.string.watermark_use_feet),
            checked = content.useFeet,
            onClick = { onContentChange(content.copy(useFeet = it), false) },
            drawContainer = false,
        )
        is WatermarkContent.Weather -> Column {
            PreferenceRowSwitch(
                title = stringResource(R.string.watermark_include_temperature),
                checked = content.includeTemperature,
                onClick = { onContentChange(content.copy(includeTemperature = it), false) },
                drawContainer = false,
            )
            PreferenceRowSwitch(
                title = stringResource(R.string.watermark_include_observation_time),
                checked = content.includeObservationTime,
                onClick = { onContentChange(content.copy(includeObservationTime = it), false) },
                drawContainer = false,
            )
        }
        is WatermarkContent.Coordinates -> PreferenceRowSwitch(
            title = stringResource(R.string.watermark_decimal_coordinates),
            checked = content.useDecimalFormat,
            onClick = { onContentChange(content.copy(useDecimalFormat = it), false) },
            drawContainer = false,
        )
        else -> Unit
    }
}

@Composable
private fun WatermarkTextField(
    value: String,
    labelRes: Int = R.string.text,
    onValueChange: (String) -> Unit,
) {
    RoundedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(labelRes)) },
        singleLine = false,
        modifier = Modifier.fillMaxWidth().flatGlassContainer(ShapeDefaults.large, resultPadding = 8.dp),
    )
}

@Composable
private fun StatusText(messageRes: Int) {
    Text(
        text = stringResource(messageRes),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** 图层被删空 / 没有选中项时的常驻引导,避免面板变成一片空白。 */
@Composable
internal fun EmptySelectionHint(messageRes: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(messageRes),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(vertical = 12.dp),
    )
}
