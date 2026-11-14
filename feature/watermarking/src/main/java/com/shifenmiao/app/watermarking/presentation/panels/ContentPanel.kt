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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.R
import com.shifenmiao.app.watermarking.domain.model.WatermarkContent
import com.shifenmiao.app.watermarking.domain.model.WatermarkElement
import com.shifenmiao.app.watermarking.domain.model.WatermarkKind
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
    onKindChange: (WatermarkKind) -> Unit,
    onContentChange: (WatermarkContent) -> Unit,
    onRequestLocation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val element = selectedElement ?: return
    val content = element.content
    Column(modifier = modifier) {
        WatermarkKind.entries.chunked(4).forEach { kinds ->
            EnhancedButtonGroup(
                items = kinds.map { it.title() },
                selectedIndex = kinds.indexOf(content.kind),
                onIndexChange = { onKindChange(kinds[it]) },
                inactiveButtonColor = MaterialTheme.colorScheme.surfaceContainer,
            )
            Spacer(Modifier.height(6.dp))
        }
        Spacer(Modifier.height(8.dp))
        when (content) {
            is WatermarkContent.Text -> WatermarkTextField(content.text) {
                onContentChange(content.copy(text = it))
            }
            is WatermarkContent.Stamp -> WatermarkTextField(content.text) {
                onContentChange(content.copy(text = it))
            }
            is WatermarkContent.Image -> ImageSelector(
                value = content.imageData,
                subtitle = stringResource(R.string.watermarking_image_sub),
                onValueChange = { onContentChange(content.copy(imageData = it.toString())) },
                modifier = Modifier.fillMaxWidth(),
            )
            is WatermarkContent.Timestamp -> {
                WatermarkTextField(
                    value = content.format,
                    labelRes = R.string.format_pattern,
                ) { onContentChange(content.copy(format = it)) }
                PreferenceRowSwitch(
                    title = stringResource(R.string.watermark_use_photo_time),
                    checked = content.usePhotoTime,
                    onClick = { onContentChange(content.copy(usePhotoTime = it)) },
                    drawContainer = false,
                )
            }
            else -> DynamicContentControls(
                content = content,
                onContentChange = onContentChange,
            )
        }
        if (content.sourceOrNull() != null) {
            if (needsDeviceLocation || content.sourceOrNull() == WatermarkValueSource.DEVICE) {
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
            if (locationLoadFailed) {
                StatusText(R.string.watermark_location_failed)
            } else if (!weatherAvailable && content is WatermarkContent.Weather) {
                StatusText(R.string.watermark_weather_unavailable)
            } else if (needsDeviceLocation) {
                StatusText(R.string.watermark_missing_dynamic_data)
            }
        }
    }
}

@Composable
private fun DynamicContentControls(
    content: WatermarkContent,
    onContentChange: (WatermarkContent) -> Unit,
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
                }
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
                }
            )
        }
    }
    when (content) {
        is WatermarkContent.Altitude -> PreferenceRowSwitch(
            title = stringResource(R.string.watermark_use_feet),
            checked = content.useFeet,
            onClick = { onContentChange(content.copy(useFeet = it)) },
            drawContainer = false,
        )
        is WatermarkContent.Weather -> Column {
            PreferenceRowSwitch(
                title = stringResource(R.string.watermark_include_temperature),
                checked = content.includeTemperature,
                onClick = { onContentChange(content.copy(includeTemperature = it)) },
                drawContainer = false,
            )
            PreferenceRowSwitch(
                title = stringResource(R.string.watermark_include_observation_time),
                checked = content.includeObservationTime,
                onClick = { onContentChange(content.copy(includeObservationTime = it)) },
                drawContainer = false,
            )
        }
        is WatermarkContent.Coordinates -> PreferenceRowSwitch(
            title = stringResource(R.string.watermark_decimal_coordinates),
            checked = content.useDecimalFormat,
            onClick = { onContentChange(content.copy(useDecimalFormat = it)) },
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
