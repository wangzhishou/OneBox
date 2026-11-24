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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.app.watermarking.domain.model.ExifFieldKey
import com.shifenmiao.app.watermarking.domain.model.ResolvedWatermarkElement
import com.shifenmiao.app.watermarking.domain.model.WatermarkContent
import com.shifenmiao.app.watermarking.domain.model.WatermarkElement
import com.shifenmiao.app.watermarking.domain.model.WatermarkValueSource
import com.shifenmiao.app.watermarking.domain.model.kind
import com.shifenmiao.app.watermarking.domain.model.sourceOrNull
import com.shifenmiao.app.watermarking.presentation.components.DynamicDataCard
import com.shifenmiao.app.watermarking.presentation.components.PropertySectionTitle
import com.shifenmiao.app.watermarking.presentation.model.icon
import com.shifenmiao.app.watermarking.presentation.model.title
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.core.ui.widget.controls.selection.ImageSelector
import com.wanbaohe.core.ui.widget.enhanced.EnhancedChip
import com.wanbaohe.core.ui.widget.glass.SingleChoiceGlassSegmentedButtonRow
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.core.ui.widget.modifier.flatGlassContainer
import com.wanbaohe.core.ui.widget.preferences.PreferenceRowSwitch
import com.wanbaohe.core.ui.widget.text.RoundedTextField

@Composable
fun ContentPanel(
    selectedElement: WatermarkElement?,
    resolvedElement: ResolvedWatermarkElement?,
    needsDeviceLocation: Boolean,
    weatherAvailable: Boolean,
    isDynamicDataLoading: Boolean,
    isResolvingPhoto: Boolean,
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
    val resolvedValue = resolvedElement
        ?.takeIf { it.hasData }
        ?.text
        ?.takeIf { it.isNotBlank() && it != "\u2014" }
    val resolvedFromDevice = resolvedElement?.fromPhoto == false

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PropertySectionTitle(
            text = content.kind.title(),
            icon = content.kind.icon(),
        )
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
            is WatermarkContent.Timestamp -> TimestampContent(
                content = content,
                resolvedValue = resolvedValue,
                isLoading = isResolvingPhoto,
                usesFileTime = resolvedElement?.isFileTime == true,
                onContentChange = onContentChange,
            )
            is WatermarkContent.CameraParams -> {
                resolvedValue?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                content.fields.distinct().forEach { field ->
                    WatermarkTextField(
                        value = content.manualOverrides[field.name].orEmpty(),
                        labelRes = field.labelRes(),
                        singleLine = true,
                    ) { value ->
                        val overrides = if (value.isEmpty()) content.manualOverrides - field.name
                        else content.manualOverrides + (field.name to value)
                        onContentChange(content.copy(manualOverrides = overrides), true)
                    }
                }
            }
            else -> DynamicContentControls(
                content = content,
                resolvedValue = resolvedValue,
                resolvedFromDevice = resolvedFromDevice,
                needsDeviceLocation = needsDeviceLocation,
                weatherAvailable = weatherAvailable,
                isDynamicDataLoading = isDynamicDataLoading,
                locationLoadFailed = locationLoadFailed,
                onContentChange = onContentChange,
                onRequestLocation = onRequestLocation,
            )
        }
    }
}

@Composable
private fun TimestampContent(
    content: WatermarkContent.Timestamp,
    resolvedValue: String?,
    isLoading: Boolean,
    usesFileTime: Boolean,
    onContentChange: (WatermarkContent, Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        WatermarkTextField(
            value = content.format,
            labelRes = R.string.watermark_time_format,
        ) { onContentChange(content.copy(format = it), true) }
        TimestampFormats(content.format) { format ->
            onContentChange(content.copy(format = format), false)
        }
        PreferenceRowSwitch(
            title = stringResource(R.string.watermark_use_photo_time),
            checked = content.usePhotoTime,
            onClick = { onContentChange(content.copy(usePhotoTime = it), false) },
            drawContainer = false,
        )
        if (content.usePhotoTime) {
            if (usesFileTime) {
                Text(
                    text = stringResource(R.string.watermark_file_time_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (!isLoading && resolvedValue == null) {
                EmptySelectionHint(R.string.watermark_no_photo_time)
            }
            WatermarkTextField(
                value = content.fallbackText,
                labelRes = R.string.watermark_time_fallback_text,
                singleLine = true,
            ) { onContentChange(content.copy(fallbackText = it), true) }
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
    FlexBox(
        modifier = Modifier.fillMaxWidth(),
        config = {
            wrap(FlexWrap.Wrap)
            columnGap(8.dp)
            rowGap(8.dp)
        },
    ) {
        patterns.forEach { pattern ->
            EnhancedChip(
                selected = pattern == selected,
                onClick = { onSelect(pattern) },
                selectedColor = MaterialTheme.colorScheme.primaryContainer,
                selectedContentColor = MaterialTheme.colorScheme.primary,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(text = pattern)
            }
        }
    }
}

@Composable
private fun DynamicContentControls(
    content: WatermarkContent,
    resolvedValue: String?,
    resolvedFromDevice: Boolean,
    needsDeviceLocation: Boolean,
    weatherAvailable: Boolean,
    isDynamicDataLoading: Boolean,
    locationLoadFailed: Boolean,
    onContentChange: (WatermarkContent, Boolean) -> Unit,
    onRequestLocation: () -> Unit,
) {
    val source = content.sourceOrNull() ?: return
    val sources = WatermarkValueSource.entries

    SingleChoiceGlassSegmentedButtonRow(
        options = sources,
        selectedOption = source,
        onOptionSelected = { newSource ->
            onContentChange(content.withSource(newSource), false)
        },
        modifier = Modifier.fillMaxWidth(),
        label = { option ->
            Text(
                text = stringResource(option.labelRes()),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )

    if (source == WatermarkValueSource.MANUAL) {
        WatermarkTextField(
            value = content.manualValue(),
            labelRes = R.string.watermark_manual_value,
        ) { value ->
            onContentChange(content.withManualValue(value), true)
        }
        return
    }

    when (content) {
        is WatermarkContent.Altitude -> PreferenceRowSwitch(
            title = stringResource(R.string.watermark_use_feet),
            checked = content.useFeet,
            onClick = { onContentChange(content.copy(useFeet = it), false) },
            drawContainer = false,
        )
        is WatermarkContent.Weather -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

    val weatherMissing = content is WatermarkContent.Weather && !weatherAvailable
    val offersFetch = source == WatermarkValueSource.DEVICE ||
        needsDeviceLocation ||
        weatherMissing ||
        // 照片本身没有这条信息、已经用设备位置补出来时，卡片继续留着，
        // 让用户看得到取到的值，也能一键刷新。
        resolvedFromDevice
    if (!offersFetch) return

    val failed = locationLoadFailed || weatherMissing
    val hint = when {
        isDynamicDataLoading -> stringResource(R.string.watermark_location_fetching)
        failed -> stringResource(
            if (weatherMissing && !locationLoadFailed) R.string.watermark_weather_unavailable
            else R.string.watermark_location_failed
        )
        needsDeviceLocation && source == WatermarkValueSource.PHOTO ->
            stringResource(R.string.watermark_missing_dynamic_data)
        else -> stringResource(R.string.watermark_tap_for_location)
    }
    DynamicDataCard(
        title = stringResource(
            when (content) {
                is WatermarkContent.Weather -> R.string.watermark_fetch_weather_title
                is WatermarkContent.Altitude -> R.string.watermark_fetch_altitude_title
                else -> R.string.watermark_use_current_location
            }
        ),
        hint = hint,
        value = resolvedValue,
        icon = content.kind.icon(),
        isLoading = isDynamicDataLoading,
        failed = failed,
        onAction = onRequestLocation,
    )
}

private fun WatermarkValueSource.labelRes(): Int = when (this) {
    WatermarkValueSource.PHOTO -> R.string.watermark_source_photo
    WatermarkValueSource.DEVICE -> R.string.watermark_source_device
    WatermarkValueSource.MANUAL -> R.string.watermark_source_manual
}

private fun WatermarkContent.withSource(source: WatermarkValueSource): WatermarkContent = when (this) {
    is WatermarkContent.Altitude -> copy(source = source)
    is WatermarkContent.Weather -> copy(source = source)
    is WatermarkContent.Coordinates -> copy(source = source)
    else -> this
}

private fun WatermarkContent.withManualValue(value: String): WatermarkContent = when (this) {
    is WatermarkContent.Altitude -> copy(manualValue = value)
    is WatermarkContent.Weather -> copy(manualValue = value)
    is WatermarkContent.Coordinates -> copy(manualValue = value)
    else -> this
}

private fun WatermarkContent.manualValue(): String = when (this) {
    is WatermarkContent.Altitude -> manualValue
    is WatermarkContent.Weather -> manualValue
    is WatermarkContent.Coordinates -> manualValue
    else -> ""
}

private fun ExifFieldKey.labelRes(): Int = when (this) {
    ExifFieldKey.CAMERA_MAKE -> R.string.tag_make
    ExifFieldKey.CAMERA_MODEL -> R.string.tag_model
    ExifFieldKey.LENS_MODEL -> R.string.tag_lens_model
    ExifFieldKey.FOCAL_LENGTH -> R.string.tag_focal_length
    ExifFieldKey.FOCAL_LENGTH_35MM -> R.string.tag_focal_length_in_35mm_film
    ExifFieldKey.APERTURE -> R.string.tag_f_number
    ExifFieldKey.SHUTTER -> R.string.tag_exposure_time
    ExifFieldKey.ISO -> R.string.tag_photographic_sensitivity
    ExifFieldKey.CAPTURED_AT -> R.string.tag_datetime_original
    ExifFieldKey.GPS -> R.string.watermark_coordinates
}

@Composable
private fun WatermarkTextField(
    value: String,
    labelRes: Int = R.string.text,
    singleLine: Boolean = false,
    onValueChange: (String) -> Unit,
) {
    RoundedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(labelRes)) },
        singleLine = singleLine,
        modifier = Modifier
            .fillMaxWidth()
            .flatGlassContainer(ShapeDefaults.large, resultPadding = 8.dp),
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
