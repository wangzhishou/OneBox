/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.camera.watermark.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.t8rin.imagetoolbox.core.resources.R as CoreResourcesR
import com.wanbaohe.camera.watermark.R
import com.wanbaohe.camera.watermark.domain.CameraCaptureMode
import com.wanbaohe.camera.watermark.domain.CameraGrid
import com.wanbaohe.camera.watermark.domain.CameraRatio
import com.wanbaohe.camera.watermark.domain.CameraTimer
import com.wanbaohe.camera.watermark.domain.CameraWatermarkStyle
import com.wanbaohe.camera.watermark.domain.CreativeCameraSettings
import com.wanbaohe.camera.watermark.domain.CreativeCameraUiState
import com.wanbaohe.camera.watermark.domain.CreativeFilter
import com.wanbaohe.camera.watermark.domain.CreativeScene
import com.wanbaohe.camera.watermark.domain.PhotoQuality
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.glass.GlassCustomSlider
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.core.ui.widget.preferences.PreferenceRowSwitch
import kotlin.math.roundToInt

enum class CameraSettingsTab(@StringRes val labelRes: Int) {
    WATERMARK(R.string.camera_tab_watermark),
    CAPTURE(R.string.camera_tab_capture),
    PROFESSIONAL(R.string.camera_tab_professional),
}

@Composable
fun CameraSettingsSheet(
    visible: Boolean,
    state: CreativeCameraUiState,
    selectedTab: CameraSettingsTab,
    onDismiss: () -> Unit,
    onSettingsChange: ((CreativeCameraSettings) -> CreativeCameraSettings) -> Unit,
    onExposureChange: (Int) -> Unit,
    onZoomChange: (Float) -> Unit,
    onSceneChange: (CreativeScene) -> Unit,
    onLocationChange: (Boolean) -> Unit,
    onModeChange: (CameraCaptureMode) -> Unit,
    onFilterChange: (CreativeFilter) -> Unit,
) {
    if (!visible) return
    EnhancedModalBottomSheet(
        visible = true,
        onDismiss = { onDismiss() },
        title = stringResource(R.string.camera_settings),
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            when (selectedTab) {
                CameraSettingsTab.WATERMARK -> WatermarkSettings(
                    state = state,
                    onSettingsChange = onSettingsChange,
                    onLocationChange = onLocationChange,
                )
                CameraSettingsTab.CAPTURE -> {
                    CaptureSettings(
                        state = state,
                        onSettingsChange = onSettingsChange,
                        onModeChange = onModeChange,
                    )
                    if (state.settings.mode == CameraCaptureMode.VIDEO) {
                        VideoSettings(state = state, onSettingsChange = onSettingsChange)
                    }
                }
                CameraSettingsTab.PROFESSIONAL -> ProfessionalSettings(
                    state = state,
                    onSettingsChange = onSettingsChange,
                    onExposureChange = onExposureChange,
                    onZoomChange = onZoomChange,
                    onSceneChange = onSceneChange,
                    onFilterChange = onFilterChange,
                )
            }
        }
    }
}

@Composable
private fun WatermarkSettings(
    state: CreativeCameraUiState,
    onSettingsChange: ((CreativeCameraSettings) -> CreativeCameraSettings) -> Unit,
    onLocationChange: (Boolean) -> Unit,
) {
    SettingsSection(titleRes = R.string.camera_watermark_style) {
        WatermarkTemplateOptions(
            selected = state.settings.watermarkStyle,
            onSelected = { style -> onSettingsChange { it.copy(watermarkStyle = style) } },
        )
        Text(
            text = stringResource(state.settings.watermarkStyle.descriptionRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    SettingsSection(titleRes = R.string.camera_section_watermark) {
        Text(
            text = stringResource(R.string.camera_watermark_info_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column {
            WatermarkInfoSwitch(
                title = stringResource(R.string.camera_info_location),
                subtitle = stringResource(R.string.camera_info_location_summary),
                checked = state.settings.locationEnabled,
                onClick = { onLocationChange(!state.settings.locationEnabled) },
            )
            WatermarkInfoSwitch(
                title = stringResource(R.string.camera_info_altitude),
                subtitle = stringResource(R.string.camera_info_altitude_summary),
                checked = state.settings.altitudeEnabled,
                onClick = {
                    val enabled = !state.settings.altitudeEnabled
                    onSettingsChange { it.copy(altitudeEnabled = enabled) }
                    if (enabled && !state.settings.locationEnabled) onLocationChange(true)
                },
            )
            WatermarkInfoSwitch(
                title = stringResource(R.string.camera_info_weather),
                subtitle = stringResource(R.string.camera_info_weather_summary),
                checked = state.settings.weatherEnabled,
                onClick = {
                    val enabled = !state.settings.weatherEnabled
                    onSettingsChange { it.copy(weatherEnabled = enabled) }
                    if (enabled && !state.settings.locationEnabled) onLocationChange(true)
                },
            )
            WatermarkInfoSwitch(
                title = stringResource(R.string.camera_info_timestamp),
                subtitle = stringResource(R.string.camera_info_timestamp_summary),
                checked = state.settings.timestampEnabled,
                onClick = {
                    onSettingsChange { it.copy(timestampEnabled = !it.timestampEnabled) }
                },
            )
            WatermarkInfoSwitch(
                title = stringResource(R.string.camera_info_camera),
                subtitle = stringResource(R.string.camera_info_camera_summary),
                checked = state.settings.cameraParametersEnabled,
                onClick = {
                    onSettingsChange { it.copy(cameraParametersEnabled = !it.cameraParametersEnabled) }
                },
            )
        }
    }
    SettingsSection(titleRes = R.string.camera_missing_info) {
        OptionFlexBox(
            options = listOf(false, true),
            selected = state.settings.hideUnavailableWatermarkInfo,
            label = { hide ->
                stringResource(
                    if (hide) R.string.camera_missing_hide else R.string.camera_missing_placeholder,
                )
            },
            onSelected = { hide -> onSettingsChange { it.copy(hideUnavailableWatermarkInfo = hide) } },
        )
    }
    Text(
        text = stringResource(R.string.camera_watermark_output_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun WatermarkInfoSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onClick: () -> Unit,
) {
    PreferenceRowSwitch(
        title = title,
        subtitle = subtitle,
        checked = checked,
        onClick = { onClick() },
        applyHorizontalPadding = false,
        resultModifier = Modifier.padding(vertical = 4.dp),
        drawContainer = false,
    )
}

@Composable
private fun CaptureSettings(
    state: CreativeCameraUiState,
    onSettingsChange: ((CreativeCameraSettings) -> CreativeCameraSettings) -> Unit,
    onModeChange: (CameraCaptureMode) -> Unit,
) {
    SettingsSection(titleRes = R.string.camera_section_capture) {
        OptionFlexBox(
            options = CameraCaptureMode.entries,
            selected = state.settings.mode,
            label = {
                stringResource(
                    if (it == CameraCaptureMode.PHOTO) R.string.camera_mode_photo else R.string.camera_mode_video,
                )
            },
            onSelected = onModeChange,
        )
        OptionLabel(stringResource(R.string.camera_ratio))
        OptionFlexBox(
            options = CameraRatio.entries,
            selected = state.settings.ratio,
            label = {
                when (it) {
                    CameraRatio.FULL -> stringResource(R.string.camera_ratio_full)
                    CameraRatio.RATIO_4_3 -> "4:3"
                    CameraRatio.RATIO_1_1 -> "1:1"
                    CameraRatio.RATIO_16_9 -> "16:9"
                }
            },
            onSelected = { ratio -> onSettingsChange { it.copy(ratio = ratio) } },
        )
        OptionLabel(stringResource(R.string.camera_timer))
        OptionFlexBox(
            options = CameraTimer.entries,
            selected = state.settings.timer,
            label = { "${it.seconds}s" },
            onSelected = { timer -> onSettingsChange { it.copy(timer = timer) } },
        )
        OptionLabel(stringResource(R.string.camera_grid))
        OptionFlexBox(
            options = CameraGrid.entries,
            selected = state.settings.grid,
            label = { grid ->
                stringResource(
                    when (grid) {
                        CameraGrid.OFF -> R.string.camera_grid_off
                        CameraGrid.THIRDS -> R.string.camera_grid_thirds
                        CameraGrid.GOLDEN -> R.string.camera_grid_golden
                        CameraGrid.DIAGONAL -> R.string.camera_grid_diagonal
                    },
                )
            },
            onSelected = { grid -> onSettingsChange { it.copy(grid = grid) } },
        )
        if (state.settings.mode == CameraCaptureMode.PHOTO) {
            OptionLabel(stringResource(R.string.camera_photo_quality))
            OptionFlexBox(
                options = PhotoQuality.entries,
                selected = state.settings.photoQuality,
                label = { stringResource(it.labelRes) },
                onSelected = { quality -> onSettingsChange { it.copy(photoQuality = quality) } },
            )
        }
    }
    SettingsSection(titleRes = R.string.camera_section_capture) {
        PreferenceRowSwitch(
            title = stringResource(R.string.camera_mirror_front),
            checked = state.settings.mirrorFrontCamera,
            onClick = { onSettingsChange { it.copy(mirrorFrontCamera = !it.mirrorFrontCamera) } },
            applyHorizontalPadding = false,
            resultModifier = Modifier.padding(vertical = 4.dp),
            drawContainer = false,
        )
        PreferenceRowSwitch(
            title = stringResource(R.string.camera_histogram),
            checked = state.settings.histogramEnabled,
            onClick = { onSettingsChange { it.copy(histogramEnabled = !it.histogramEnabled) } },
            applyHorizontalPadding = false,
            resultModifier = Modifier.padding(vertical = 4.dp),
            drawContainer = false,
        )
    }
}

@Composable
private fun ProfessionalSettings(
    state: CreativeCameraUiState,
    onSettingsChange: ((CreativeCameraSettings) -> CreativeCameraSettings) -> Unit,
    onExposureChange: (Int) -> Unit,
    onZoomChange: (Float) -> Unit,
    onSceneChange: (CreativeScene) -> Unit,
    onFilterChange: (CreativeFilter) -> Unit,
) {
    SettingsSection(titleRes = R.string.camera_filter) {
        CameraFilterStrip(
            filters = state.capabilities.supportedFilters.sortedBy { it.ordinal },
            selected = state.settings.filter,
            onSelected = onFilterChange,
        )
    }
    SettingsSection(titleRes = R.string.camera_section_professional) {
        if (state.capabilities.minExposureIndex < state.capabilities.maxExposureIndex) {
            OptionLabel(
                stringResource(
                    R.string.camera_exposure_value,
                    state.exposureIndex * state.capabilities.exposureStep,
                ),
            )
            GlassCustomSlider(
                value = state.exposureIndex.toFloat(),
                onValueChange = { onExposureChange(it.roundToInt()) },
                modifier = Modifier.fillMaxWidth(),
                valueRange = state.capabilities.minExposureIndex.toFloat()..
                    state.capabilities.maxExposureIndex.toFloat(),
                steps = (
                    state.capabilities.maxExposureIndex -
                        state.capabilities.minExposureIndex - 1
                    ).coerceAtLeast(0),
            )
        }
        if (state.capabilities.maxZoomRatio > state.capabilities.minZoomRatio) {
            OptionLabel(stringResource(R.string.camera_zoom_value, state.zoomRatio))
            GlassCustomSlider(
                value = state.zoomRatio,
                onValueChange = onZoomChange,
                modifier = Modifier.fillMaxWidth(),
                valueRange = state.capabilities.minZoomRatio..state.capabilities.maxZoomRatio,
            )
        }
        val sceneOptions = state.capabilities.supportedScenes.sortedBy { it.ordinal }
        if (sceneOptions.size > 1) {
            OptionLabel(stringResource(R.string.camera_scene))
            OptionFlexBox(
                options = sceneOptions,
                selected = state.settings.scene,
                label = { stringResource(it.labelRes) },
                onSelected = onSceneChange,
            )
        }
        val whiteBalanceOptions = state.capabilities.supportedWhiteBalances.sortedBy { it.ordinal }
        if (whiteBalanceOptions.size > 1) {
            OptionLabel(stringResource(R.string.camera_white_balance))
            OptionFlexBox(
                options = whiteBalanceOptions,
                selected = state.settings.whiteBalance,
                label = { stringResource(it.labelRes) },
                onSelected = { whiteBalance -> onSettingsChange { it.copy(whiteBalance = whiteBalance) } },
            )
        }
        if (state.capabilities.supportsLowLightBoost) {
            PreferenceRowSwitch(
                title = stringResource(R.string.camera_low_light_boost),
                checked = state.settings.lowLightBoostEnabled,
                onClick = { onSettingsChange { it.copy(lowLightBoostEnabled = !it.lowLightBoostEnabled) } },
                applyHorizontalPadding = false,
                resultModifier = Modifier.padding(vertical = 4.dp),
                drawContainer = false,
            )
        }
    }
}

@Composable
private fun CameraFilterStrip(
    filters: List<CreativeFilter>,
    selected: CreativeFilter,
    onSelected: (CreativeFilter) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(filters, key = CreativeFilter::name) { filter ->
            val isSelected = filter == selected
            GlassSurface(
                onClick = { onSelected(filter) },
                modifier = Modifier.width(72.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
                contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                style = GlassStyle.Regular,
                borderWidth = if (isSelected) 2.dp else 1.dp,
            ) {
                Column(
                    modifier = Modifier.padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AsyncImage(
                        model = CoreResourcesR.drawable.filter_preview_source,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        colorFilter = filter.previewColorFilter(),
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    )
                    Text(
                        text = stringResource(filter.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

private fun CreativeFilter.previewColorFilter(): ColorFilter? = when (this) {
    CreativeFilter.MONO -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    CreativeFilter.SEPIA -> ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            ),
        ),
    )
    CreativeFilter.NEGATIVE -> ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f,
            ),
        ),
    )
    CreativeFilter.AQUA -> ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                0.55f, 0f, 0f, 0f, 0f,
                0f, 0.85f, 0f, 0f, 0f,
                0f, 0f, 1.2f, 0f, 12f,
                0f, 0f, 0f, 1f, 0f,
            ),
        ),
    )
    else -> null
}

@Composable
private fun VideoSettings(
    state: CreativeCameraUiState,
    onSettingsChange: ((CreativeCameraSettings) -> CreativeCameraSettings) -> Unit,
) {
    SettingsSection(titleRes = R.string.camera_section_video) {
        OptionLabel(stringResource(R.string.camera_video_quality))
        OptionFlexBox(
            options = state.capabilities.supportedVideoQualities.sortedByDescending { it.ordinal },
            selected = state.settings.videoQuality,
            label = { it.shortLabel },
            onSelected = { quality -> onSettingsChange { it.copy(videoQuality = quality) } },
        )
        OptionLabel(stringResource(R.string.camera_frame_rate))
        OptionFlexBox(
            options = state.capabilities.supportedFrameRates.sortedBy { it.fps },
            selected = state.settings.frameRate,
            label = { if (it.fps == 0) stringResource(R.string.camera_auto) else "${it.fps} fps" },
            onSelected = { frameRate -> onSettingsChange { it.copy(frameRate = frameRate) } },
        )
        PreferenceRowSwitch(
            title = stringResource(R.string.camera_record_audio),
            checked = state.settings.audioEnabled,
            onClick = { onSettingsChange { it.copy(audioEnabled = !it.audioEnabled) } },
            applyHorizontalPadding = false,
            resultModifier = Modifier.padding(vertical = 4.dp),
            drawContainer = false,
        )
        if (state.capabilities.supportsVideoStabilization) {
            PreferenceRowSwitch(
                title = stringResource(R.string.camera_video_stabilization),
                checked = state.settings.videoStabilization,
                onClick = { onSettingsChange { it.copy(videoStabilization = !it.videoStabilization) } },
                applyHorizontalPadding = false,
                resultModifier = Modifier.padding(vertical = 4.dp),
                drawContainer = false,
            )
        }
        if (state.capabilities.supportsHdr) {
            PreferenceRowSwitch(
                title = stringResource(R.string.camera_hdr_video),
                checked = state.settings.hdrEnabled,
                onClick = { onSettingsChange { it.copy(hdrEnabled = !it.hdrEnabled) } },
                applyHorizontalPadding = false,
                resultModifier = Modifier.padding(vertical = 4.dp),
                drawContainer = false,
            )
        }
    }
}

@Composable
private fun SettingsSection(
    titleRes: Int,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        content()
    }
}

@Composable
private fun OptionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun WatermarkTemplateOptions(
    selected: CameraWatermarkStyle,
    onSelected: (CameraWatermarkStyle) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(CameraWatermarkStyle.entries, key = CameraWatermarkStyle::name) { style ->
            val isSelected = style == selected
            GlassFilterChip(
                selected = isSelected,
                onClick = { onSelected(style) },
                label = {
                    Text(
                        text = stringResource(style.labelRes),
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                shape = MaterialTheme.shapes.small,
                colors = FilterChipDefaults.filterChipColors(
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                glassContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                glassSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                style = GlassStyle.Regular,
            )
        }
    }
}

@Composable
private fun <T> OptionFlexBox(
    options: List<T>,
    selected: T,
    modifier: Modifier = Modifier,
    label: @Composable (T) -> String,
    onSelected: (T) -> Unit,
) {
    FlexBox(
        modifier = modifier.fillMaxWidth(),
        config = {
            wrap(FlexWrap.Wrap)
            columnGap(8.dp)
            rowGap(8.dp)
        },
    ) {
        options.forEach { option ->
            key(option) {
                val isSelected = option == selected
                GlassFilterChip(
                    selected = isSelected,
                    onClick = { onSelected(option) },
                    label = {
                        Text(
                            text = label(option),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                    shape = MaterialTheme.shapes.small,
                    colors = FilterChipDefaults.filterChipColors(
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    glassContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    glassSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    style = GlassStyle.Regular,
                )
            }
        }
    }
}
