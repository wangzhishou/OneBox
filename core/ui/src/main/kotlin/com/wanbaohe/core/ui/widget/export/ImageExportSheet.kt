/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wanbaohe.core.ui.widget.export

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.domain.image.export.ImageExportCapabilities
import com.t8rin.imagetoolbox.core.domain.image.export.ImageExportCompressionControl
import com.t8rin.imagetoolbox.core.domain.image.export.ImageExportConfig
import com.t8rin.imagetoolbox.core.domain.image.export.ImageExportMetadataPolicy
import com.t8rin.imagetoolbox.core.domain.image.export.ImageExportResolution
import com.t8rin.imagetoolbox.core.domain.image.export.ImageExportSummary
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormatGroup
import com.t8rin.imagetoolbox.core.domain.image.model.alphaContainedEntries
import com.t8rin.imagetoolbox.core.domain.utils.humanFileSize
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.Share
import com.wanbaohe.base.ui.button.PrimaryButton
import com.wanbaohe.base.ui.button.SecondaryButton
import com.wanbaohe.core.ui.widget.controls.selection.ColorRowSelector
import com.wanbaohe.core.ui.widget.enhanced.EnhancedChip
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.enhanced.EnhancedSlider
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassSegmentedButtonRow
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.glassBackground
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

@Composable
fun ImageExportSheet(
    visible: Boolean,
    config: ImageExportConfig,
    capabilities: ImageExportCapabilities,
    summary: ImageExportSummary,
    onConfigChange: (ImageExportConfig) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.image_export_title),
    estimateBitmap: Bitmap? = null,
    saveLocation: String? = null,
    onChooseSaveLocation: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    saveEnabled: Boolean = true,
    shareEnabled: Boolean = true,
    blockingMessage: String? = null,
    onFixBlocking: (() -> Unit)? = null,
    backgroundColor: Color = Color(config.backgroundColor),
    onBackgroundColorChange: ((Color) -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
    advancedContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    var showAdvanced by rememberSaveable { mutableStateOf(false) }
    val hasAdvanced = capabilities.formats.size > 3 ||
        capabilities.supportsTargetFileSize ||
        capabilities.metadataPolicies.isNotEmpty() ||
        capabilities.supportsBackgroundColor ||
        advancedContent != null

    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss() },
        title = title,
        modifier = modifier,
        confirmButton = {
            // 按钮按内容自适应宽度，整行靠右对齐（父级操作栏本身是 Arrangement.End）。
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (capabilities.supportsSharing && onShare != null) {
                    SecondaryButton(
                        text = stringResource(R.string.image_export_share),
                        enable = shareEnabled && blockingMessage == null,
                        icon = {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        onClick = onShare,
                    )
                }
                PrimaryButton(
                    text = if (summary.itemCount > 1) {
                        stringResource(R.string.image_export_save_count, summary.itemCount)
                    } else {
                        stringResource(R.string.image_export_save)
                    },
                    enable = saveEnabled && blockingMessage == null,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    onClick = onSave,
                )
            }
        },
        endConfirmButtonPadding = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ImageExportSummaryCard(
                config = config,
                summary = summary,
                estimateBitmap = estimateBitmap,
            )
            content?.invoke(this)
            ImageExportSection(title = stringResource(R.string.image_export_format)) {
                PrimaryFormatSelector(
                    config = config,
                    capabilities = capabilities,
                    onConfigChange = onConfigChange,
                )
                Spacer(Modifier.height(12.dp))
                QualityControl(
                    config = config,
                    onConfigChange = onConfigChange,
                )
            }
            if (capabilities.supportsResize) {
                ImageExportSection(title = stringResource(R.string.image_export_size)) {
                    DimensionControl(
                        config = config,
                        summary = summary,
                        onConfigChange = onConfigChange,
                    )
                }
            }
            if (capabilities.supportsSaveLocation && onChooseSaveLocation != null) {
                SaveLocationRow(
                    value = saveLocation ?: stringResource(R.string.image_export_default_location),
                    onClick = onChooseSaveLocation,
                )
            }
            if (hasAdvanced) {
                AdvancedHeader(
                    expanded = showAdvanced,
                    onClick = { showAdvanced = !showAdvanced },
                )
                AnimatedVisibility(visible = showAdvanced) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AdvancedFormatSelector(
                            config = config,
                            capabilities = capabilities,
                            onConfigChange = onConfigChange,
                        )
                        if (capabilities.supportsTargetFileSize) {
                            CompressionControl(
                                config = config,
                                onConfigChange = onConfigChange,
                            )
                        }
                        if (capabilities.metadataPolicies.isNotEmpty()) {
                            MetadataControl(
                                config = config,
                                capabilities = capabilities,
                                onConfigChange = onConfigChange,
                            )
                        }
                        if (capabilities.supportsBackgroundColor &&
                            config.format !in ImageFormat.alphaContainedEntries
                        ) {
                            ImageExportSection(title = null) {
                                ColorRowSelector(
                                    value = backgroundColor,
                                    onValueChange = onBackgroundColorChange ?: { color ->
                                        onConfigChange(
                                            config.copy(backgroundColor = color.toArgb())
                                        )
                                    },
                                    allowAlpha = false,
                                    contentHorizontalPadding = 0.dp,
                                )
                            }
                        }
                        advancedContent?.invoke(this)
                    }
                }
            }
            if (blockingMessage != null) {
                ImageExportSection(title = null) {
                    Text(
                        text = blockingMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    if (onFixBlocking != null) {
                        Spacer(Modifier.height(10.dp))
                        SecondaryButton(
                            text = stringResource(R.string.image_export_use_png),
                            onClick = onFixBlocking,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ImageExportSummaryCard(
    config: ImageExportConfig,
    summary: ImageExportSummary,
    estimateBitmap: Bitmap?,
) {
    val estimatedBytes by produceState<Long?>(
        initialValue = null,
        estimateBitmap,
        config.format,
        config.quality,
        config.backgroundColor,
        summary.width,
        summary.height,
        summary.itemCount,
    ) {
        val bitmap = estimateBitmap
        val width = summary.width
        val height = summary.height
        if (bitmap == null || width == null || height == null) {
            value = null
            return@produceState
        }
        delay(180)
        value = withContext(Dispatchers.Default) {
            estimateEncodedSize(
                bitmap = bitmap,
                format = config.format,
                quality = config.quality,
                backgroundColor = config.backgroundColor,
                targetWidth = width,
                targetHeight = height,
                itemCount = summary.itemCount,
            )
        }
    }
    val outputWidth = summary.width
    val outputHeight = summary.height
    val sourceBytes = summary.sourceSizeBytes?.takeIf { it > 0 }
    val showEstimate = estimateBitmap != null && outputWidth != null && outputHeight != null
    GlassCard(shape = ShapeDefaults.extraLarge) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SummaryItem(
                label = stringResource(R.string.image_export_output_size),
                value = if (outputWidth != null && outputHeight != null) {
                    stringResource(
                        R.string.image_export_dimensions,
                        outputWidth,
                        outputHeight,
                    )
                } else {
                    stringResource(R.string.image_export_unavailable)
                },
                modifier = Modifier.weight(1.2f),
            )
            if (sourceBytes != null) {
                SummaryItem(
                    label = stringResource(R.string.image_export_source_size),
                    value = humanFileSize(sourceBytes),
                    modifier = Modifier.weight(1f),
                )
            }
            if (showEstimate) {
                SummaryItem(
                    label = stringResource(R.string.image_export_estimated_size),
                    value = estimatedBytes?.let {
                        stringResource(R.string.image_export_approx, humanFileSize(it))
                    } ?: stringResource(R.string.image_export_unavailable),
                    modifier = Modifier.weight(1f),
                )
            }
            if (sourceBytes == null && !showEstimate) {
                SummaryItem(
                    label = stringResource(R.string.image_export_format),
                    value = config.format.familyTitle(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PrimaryFormatSelector(
    config: ImageExportConfig,
    capabilities: ImageExportCapabilities,
    onConfigChange: (ImageExportConfig) -> Unit,
) {
    val primaryFormats = listOf(
        ImageFormat.Jpg,
        ImageFormat.Png.Lossless,
        ImageFormat.Webp.Lossy,
    ).filter { primary ->
        capabilities.formats.any { it.family() == primary.family() }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        primaryFormats.forEach { format ->
            val selected = config.format.family() == format.family()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 68.dp)
                    .glassBackground(
                        style = if (selected) GlassStyle.Medium else GlassStyle.Regular,
                        shape = ShapeDefaults.large,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        },
                    )
                    .semantics {
                        role = Role.Tab
                        this.selected = selected
                    }
                    .clickable { onConfigChange(config.withFormat(format)) }
                    .padding(horizontal = 6.dp, vertical = 10.dp),
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = format.familyTitle(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    if (selected) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = stringResource(
                        when (format) {
                            ImageFormat.Jpg -> R.string.image_export_jpg_sub
                            is ImageFormat.Png -> R.string.image_export_png_sub
                            else -> R.string.image_export_webp_sub
                        }
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun QualityControl(
    config: ImageExportConfig,
    onConfigChange: (ImageExportConfig) -> Unit,
) {
    val compression = config.format.compressionTypes.firstOrNull()
    if (!config.format.canChangeCompressionValue || compression == null) {
        Text(
            text = stringResource(R.string.image_export_lossless),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    if (config.compressionControl == ImageExportCompressionControl.TargetFileSize) {
        Text(
            text = stringResource(R.string.image_export_target_size_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    val range = compression.compressionRange
    val isQuality = compression is ImageFormat.CompressionType.Quality
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(if (isQuality) R.string.image_export_quality else R.string.effort),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = if (isQuality) "${config.quality}%" else config.quality.toString(),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
    EnhancedSlider(
        value = config.quality.coerceIn(range).toFloat(),
        onValueChange = {
            onConfigChange(config.copy(quality = it.roundToInt().coerceIn(range)))
        },
        valueRange = range.first.toFloat()..range.last.toFloat(),
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        text = stringResource(R.string.image_export_quality_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun DimensionControl(
    config: ImageExportConfig,
    summary: ImageExportSummary,
    onConfigChange: (ImageExportConfig) -> Unit,
) {
    GlassSegmentedButtonRow(
        options = ImageExportResolution.entries,
        selectedOption = config.resolution,
        onOptionSelected = { resolution ->
            onConfigChange(
                config.copy(
                    resolution = resolution,
                    customWidth = if (resolution == ImageExportResolution.Custom) {
                        config.customWidth.takeIf { it > 0 } ?: summary.width ?: 0
                    } else {
                        config.customWidth
                    },
                    customHeight = if (resolution == ImageExportResolution.Custom) {
                        config.customHeight.takeIf { it > 0 } ?: summary.height ?: 0
                    } else {
                        config.customHeight
                    },
                )
            )
        },
        label = { resolution ->
            Text(
                text = stringResource(
                    when (resolution) {
                        ImageExportResolution.Original -> R.string.image_export_original
                        ImageExportResolution.Half -> R.string.image_export_half
                        ImageExportResolution.Quarter -> R.string.image_export_quarter
                        ImageExportResolution.Custom -> R.string.image_export_custom
                    }
                )
            )
        },
        modifier = Modifier.fillMaxWidth(),
    )
    AnimatedVisibility(visible = config.resolution == ImageExportResolution.Custom) {
        Row(
            modifier = Modifier.padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DimensionField(
                label = stringResource(R.string.image_export_width),
                value = config.customWidth,
                onValueChange = { onConfigChange(config.copy(customWidth = it)) },
                modifier = Modifier.weight(1f),
            )
            Text("×", color = MaterialTheme.colorScheme.onSurfaceVariant)
            DimensionField(
                label = stringResource(R.string.image_export_height),
                value = config.customHeight,
                onValueChange = { onConfigChange(config.copy(customHeight = it)) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DimensionField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassOutlinedTextField(
        value = value.takeIf { it > 0 }?.toString().orEmpty(),
        onValueChange = { text ->
            onValueChange(text.filter(Char::isDigit).take(5).toIntOrNull() ?: 0)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
    )
}

@Composable
private fun SaveLocationRow(
    value: String,
    onClick: () -> Unit,
) {
    GlassCard(
        onClick = onClick,
        shape = ShapeDefaults.extraLarge,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.FolderOpen, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.image_export_save_location),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
private fun AdvancedHeader(
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.image_export_advanced),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Outlined.ExpandMore,
            contentDescription = null,
            modifier = Modifier.rotate(if (expanded) 180f else 0f),
        )
    }
}

@Composable
private fun AdvancedFormatSelector(
    config: ImageExportConfig,
    capabilities: ImageExportCapabilities,
    onConfigChange: (ImageExportConfig) -> Unit,
) {
    val primaryFormats = setOf(
        ImageFormat.Jpg,
        ImageFormat.Png.Lossless,
        ImageFormat.Webp.Lossy,
    )
    val formats = capabilities.formats.filterNot { it in primaryFormats }
    if (formats.isEmpty()) return
    ImageExportSection(title = stringResource(R.string.image_export_more_formats)) {
        if (config.format !in primaryFormats) {
            Text(
                text = stringResource(R.string.image_export_current_encoder, config.format.title),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
        }
        FlexBox(
            config = {
                wrap(FlexWrap.Wrap)
                columnGap(8.dp)
                rowGap(8.dp)
            },
        ) {
            formats.forEach { format ->
                EnhancedChip(
                    selected = config.format == format,
                    onClick = { onConfigChange(config.withFormat(format)) },
                    selectedColor = MaterialTheme.colorScheme.primaryContainer,
                    label = { Text(format.title) },
                )
            }
        }
    }
}

@Composable
private fun CompressionControl(
    config: ImageExportConfig,
    onConfigChange: (ImageExportConfig) -> Unit,
) {
    ImageExportSection(title = stringResource(R.string.image_export_compression_control)) {
        GlassSegmentedButtonRow(
            options = ImageExportCompressionControl.entries,
            selectedOption = config.compressionControl,
            onOptionSelected = { onConfigChange(config.copy(compressionControl = it)) },
            label = { control ->
                Text(
                    stringResource(
                        if (control == ImageExportCompressionControl.Quality) {
                            R.string.image_export_control_quality
                        } else {
                            R.string.image_export_control_size
                        }
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )
        AnimatedVisibility(
            visible = config.compressionControl == ImageExportCompressionControl.TargetFileSize
        ) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                GlassOutlinedTextField(
                    value = config.maxFileSizeBytes?.div(1024)?.toString().orEmpty(),
                    onValueChange = { text ->
                        val kilobytes = text.filter(Char::isDigit).take(10).toLongOrNull()
                        onConfigChange(config.copy(maxFileSizeBytes = kilobytes?.times(1024)))
                    },
                    label = { Text(stringResource(R.string.image_export_target_size_kb)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.image_export_target_size_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MetadataControl(
    config: ImageExportConfig,
    capabilities: ImageExportCapabilities,
    onConfigChange: (ImageExportConfig) -> Unit,
) {
    LaunchedEffect(config.format, capabilities.metadataPolicies) {
        if ((!config.format.canWriteExif || config.metadataPolicy !in capabilities.metadataPolicies) &&
            config.metadataPolicy != ImageExportMetadataPolicy.RemoveAll
        ) {
            onConfigChange(config.copy(metadataPolicy = ImageExportMetadataPolicy.RemoveAll))
        }
    }
    ImageExportSection(title = stringResource(R.string.image_export_metadata)) {
        if (!config.format.canWriteExif) {
            Text(
                text = stringResource(R.string.image_export_metadata_unsupported),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            GlassSegmentedButtonRow(
                options = capabilities.metadataPolicies,
                selectedOption = config.metadataPolicy,
                onOptionSelected = { onConfigChange(config.copy(metadataPolicy = it)) },
                label = { policy ->
                    Text(
                        stringResource(
                            when (policy) {
                                ImageExportMetadataPolicy.RemoveAll -> R.string.image_export_metadata_remove
                                ImageExportMetadataPolicy.KeepWithoutLocation ->
                                    R.string.image_export_metadata_remove_location
                                ImageExportMetadataPolicy.KeepAll -> R.string.image_export_metadata_keep
                            }
                        ),
                        maxLines = 2,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(
                    when (config.metadataPolicy) {
                        ImageExportMetadataPolicy.RemoveAll -> R.string.image_export_metadata_remove_sub
                        ImageExportMetadataPolicy.KeepWithoutLocation ->
                            R.string.image_export_metadata_remove_location_sub
                        ImageExportMetadataPolicy.KeepAll -> R.string.image_export_metadata_keep_sub
                    }
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun ImageExportSection(
    title: String?,
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassCard(shape = ShapeDefaults.extraLarge) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
            content()
        }
    }
}

private fun ImageExportConfig.withFormat(format: ImageFormat): ImageExportConfig {
    val range = format.compressionTypes.firstOrNull()?.compressionRange ?: 10..100
    return copy(
        format = format,
        quality = quality.coerceIn(range),
        metadataPolicy = if (format.canWriteExif) {
            metadataPolicy
        } else {
            ImageExportMetadataPolicy.RemoveAll
        },
    )
}

private fun ImageFormat.family(): ImageFormat = when (this) {
    is ImageFormat.Png -> ImageFormat.Png.Lossless
    is ImageFormat.Webp -> ImageFormat.Webp.Lossy
    ImageFormat.Jpg, ImageFormat.Jpeg, ImageFormat.MozJpeg, ImageFormat.Jpegli -> ImageFormat.Jpg
    else -> this
}

private fun ImageFormat.familyTitle(): String = when (val group = ImageFormatGroup.fromFormat(this)) {
    ImageFormatGroup.Jpeg2000 -> "JPEG 2000"
    else -> group.title
}

private fun estimateEncodedSize(
    bitmap: Bitmap,
    format: ImageFormat,
    quality: Int,
    backgroundColor: Int,
    targetWidth: Int,
    targetHeight: Int,
    itemCount: Int,
): Long? = runCatching {
    if (bitmap.isRecycled || targetWidth <= 0 || targetHeight <= 0) return null
    ByteArrayOutputStream().use { output ->
        val compressFormat = when (format) {
            is ImageFormat.Png -> Bitmap.CompressFormat.PNG
            is ImageFormat.Webp -> Bitmap.CompressFormat.WEBP
            else -> Bitmap.CompressFormat.JPEG
        }
        if (!bitmap.compress(compressFormat, quality.coerceIn(0, 100), output)) return null
        val pixels = bitmap.width.toLong() * bitmap.height
        if (pixels <= 0) return null
        val bytesPerPixel = output.size().toDouble() / pixels
        (bytesPerPixel * targetWidth * targetHeight * itemCount.coerceAtLeast(1)).toLong()
    }
}.getOrNull()
