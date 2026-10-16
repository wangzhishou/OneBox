package com.wanbaohe.markuplayers.presentation.export

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.button.CancelButton
import com.shifenmiao.base.ui.button.ConfirmButton
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.utils.humanFileSize
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedSlider
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.utils.fileSize
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.presentation.components.SheetHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.Color as ComposeColor

/**
 * 导出保存设置面板(设计稿「导出/保存」):
 * 顶部信息行(导出尺寸/源文件大小/预估导出大小)+ 格式(JPG/PNG/WEBP)
 * + 质量(有损格式可用)+ 分辨率(原始/1/2/1/4/自定义)+ 更多选项。
 * 底部左侧「取消」、右侧「保存」。
 *
 * 预估导出大小为纯估算:用预览位图按当前格式/质量编码一次得 bytesPerPixel,
 * 外推到目标分辨率;设置或底图变化时自动重算。
 */
@Composable
fun ExportSettingsSheet(
    visible: Boolean,
    settings: ExportSettings,
    sourceSize: IntSize?,
    imageUri: Uri?,
    estimateBitmap: Bitmap?,
    onDismiss: () -> Unit,
    onSettingsChange: (ExportSettings) -> Unit,
    onSave: () -> Unit,
    canOverwriteOriginal: Boolean = false,
    validationError: String? = null,
    saveLocation: String? = null,
    onChooseSaveLocation: (() -> Unit)? = null,
    saveExif: Boolean = false,
    onSaveExifChange: (Boolean) -> Unit = {},
) {
    val localError = settings.validationError(sourceSize?.width ?: 0, sourceSize?.height ?: 0)
    val errorText = validationError ?: localError?.let { error ->
        stringResource(
            when (error) {
                ExportValidationError.InvalidSourceSize -> R.string.studio_export_invalid_source
                ExportValidationError.InvalidCustomSize -> R.string.studio_export_invalid_dimensions
                ExportValidationError.InvalidQuality -> R.string.studio_export_invalid_quality
                ExportValidationError.UnsupportedFormat -> R.string.studio_export_invalid_format
                ExportValidationError.TransparentJpegBackground -> R.string.studio_export_opaque_background
                ExportValidationError.FileSizeTooSmall -> R.string.studio_export_size_too_small
            }
        )
    } ?: if (!canOverwriteOriginal && !settings.saveCopy) {
        stringResource(R.string.studio_export_copy_required)
    } else null

    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss() },
        title = stringResource(R.string.markup_export_title),
        onClose = onDismiss,
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CancelButton(
                    text = stringResource(R.string.markup_cancel),
                    onClick = onDismiss
                )
                ConfirmButton(
                    text = stringResource(R.string.markup_save),
                    enabled = errorText == null,
                    onClick = onSave
                )
            }
        },
        sheetContent = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.markup_export_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ExportInfoRow(
                    settings = settings,
                    sourceSize = sourceSize,
                    imageUri = imageUri,
                    estimateBitmap = estimateBitmap
                )

                if (errorText != null) {
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                SectionLabel(text = stringResource(R.string.studio_export_purpose))
                ExportPurposePreset.entries.chunked(2).forEach { presets ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        presets.forEach { preset ->
                            SelectableCard(
                                label = stringResource(
                                    when (preset) {
                                        ExportPurposePreset.Sharing -> R.string.studio_export_preset_sharing
                                        ExportPurposePreset.Avatar -> R.string.studio_export_preset_avatar
                                        ExportPurposePreset.Wallpaper -> R.string.studio_export_preset_wallpaper
                                        ExportPurposePreset.Print -> R.string.studio_export_preset_print
                                    }
                                ),
                                subtitle = stringResource(
                                    R.string.studio_export_preset_long_edge, preset.longEdge
                                ),
                                selected = false,
                                enabled = sourceSize != null && sourceSize.width > 0 && sourceSize.height > 0,
                                onClick = {
                                    sourceSize?.let {
                                        onSettingsChange(
                                            settings.withPurposePreset(preset, it.width, it.height)
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.studio_export_presets_keep_ratio),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SectionLabel(text = stringResource(R.string.markup_export_format))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExportSettings.supportedFormats.forEach { format ->
                        FormatCard(
                            format = format,
                            selected = settings.format == format,
                            onClick = { onSettingsChange(settings.copy(format = format)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (settings.format == ImageFormat.Jpg) {
                    Text(
                        text = stringResource(R.string.studio_export_jpeg_alpha_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            Color.WHITE to R.string.studio_export_color_white,
                            Color.BLACK to R.string.studio_export_color_black,
                            Color.LTGRAY to R.string.studio_export_color_gray,
                            0xFFFFF4DE.toInt() to R.string.studio_export_color_cream
                        ).forEach { (color, label) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    onSettingsChange(settings.copy(jpegBackgroundColor = color))
                                }
                            ) {
                                Box(
                                    Modifier
                                        .size(36.dp)
                                        .background(ComposeColor(color), ShapeDefaults.large)
                                        .border(
                                            if (settings.jpegBackgroundColor == color) 3.dp else 1.dp,
                                            if (settings.jpegBackgroundColor == color) {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            } else MaterialTheme.colorScheme.outline,
                                            ShapeDefaults.large
                                        )
                                )
                                Text(
                                    text = stringResource(label),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SectionLabel(text = stringResource(R.string.markup_export_quality))
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${settings.quality}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (settings.format.canChangeCompressionValue) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                EnhancedSlider(
                    value = settings.quality.toFloat(),
                    onValueChange = {
                        onSettingsChange(settings.copy(quality = it.roundToInt()))
                    },
                    valueRange = 10f..100f,
                    enabled = settings.format.canChangeCompressionValue,
                    // 滑杆不带背景容器,直接排布
                    modifier = Modifier.fillMaxWidth()
                )

                SectionLabel(text = stringResource(R.string.markup_export_resolution))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResolutionCard(
                        label = stringResource(R.string.markup_export_res_original),
                        size = sourceSize,
                        selected = settings.resolution == ExportResolution.Original,
                        onClick = {
                            onSettingsChange(settings.copy(resolution = ExportResolution.Original))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ResolutionCard(
                        label = "1/2",
                        size = sourceSize?.let { IntSize(it.width / 2, it.height / 2) },
                        selected = settings.resolution == ExportResolution.Half,
                        onClick = {
                            onSettingsChange(settings.copy(resolution = ExportResolution.Half))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ResolutionCard(
                        label = "1/4",
                        size = sourceSize?.let { IntSize(it.width / 4, it.height / 4) },
                        selected = settings.resolution == ExportResolution.Quarter,
                        onClick = {
                            onSettingsChange(settings.copy(resolution = ExportResolution.Quarter))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ResolutionCard(
                        label = stringResource(R.string.markup_export_res_custom),
                        subtitle = stringResource(R.string.markup_export_res_custom_sub),
                        selected = settings.resolution == ExportResolution.Custom,
                        onClick = {
                            onSettingsChange(
                                settings.copy(
                                    resolution = ExportResolution.Custom,
                                    customWidth = settings.customWidth.takeIf { it > 0 }
                                        ?: sourceSize?.width ?: 0,
                                    customHeight = settings.customHeight.takeIf { it > 0 }
                                        ?: sourceSize?.height ?: 0
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (settings.resolution == ExportResolution.Custom) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SizeField(
                            label = stringResource(R.string.markup_export_width),
                            value = settings.customWidth,
                            onValueChange = { width ->
                                onSettingsChange(
                                    settings.copy(
                                        customWidth = width,
                                        customHeight = linkedHeight(width, sourceSize)
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "×",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        SizeField(
                            label = stringResource(R.string.markup_export_height),
                            value = settings.customHeight,
                            onValueChange = { height ->
                                onSettingsChange(
                                    settings.copy(
                                        customHeight = height,
                                        customWidth = linkedWidth(height, sourceSize)
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        text = stringResource(R.string.studio_export_custom_fit),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                GlassOutlinedTextField(
                    value = settings.maxFileSizeBytes?.div(1024)?.toString().orEmpty(),
                    onValueChange = { text ->
                        val kilobytes = text.filter(Char::isDigit).take(10).toLongOrNull()
                        onSettingsChange(settings.copy(maxFileSizeBytes = kilobytes?.times(1024)))
                    },
                    label = { Text(stringResource(R.string.studio_export_file_size_limit)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(
                        if (settings.format is ImageFormat.Png) {
                            R.string.studio_export_png_limit_hint
                        } else R.string.studio_export_lossy_limit_hint
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SectionLabel(text = stringResource(R.string.markup_export_more))
                if (canOverwriteOriginal) {
                    ExportSwitchRow(
                        label = stringResource(R.string.studio_export_save_copy),
                        subtitle = stringResource(
                            if (settings.saveCopy) R.string.studio_export_save_copy_hint
                            else R.string.studio_export_overwrite_warning
                        ),
                        checked = settings.saveCopy,
                        onCheckedChange = { onSettingsChange(settings.copy(saveCopy = it)) }
                    )
                } else {
                    Text(
                        text = stringResource(R.string.studio_export_copy_required),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (onChooseSaveLocation != null) {
                    SelectableCard(
                        label = stringResource(R.string.studio_export_save_location),
                        subtitle = saveLocation ?: stringResource(R.string.studio_export_choose_location),
                        selected = false,
                        enabled = settings.saveCopy,
                        onClick = onChooseSaveLocation,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (saveLocation != null) {
                    Text(
                        text = stringResource(R.string.studio_export_location_value, saveLocation),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                ExportSwitchRow(
                    label = stringResource(R.string.markup_export_share_after),
                    subtitle = stringResource(R.string.markup_export_share_after_sub),
                    checked = settings.shareAfterSave,
                    onCheckedChange = {
                        onSettingsChange(settings.copy(shareAfterSave = it))
                    }
                )
                ExportSwitchRow(
                    label = stringResource(R.string.markup_menu_save_exif),
                    subtitle = stringResource(R.string.studio_export_save_exif_sub),
                    checked = saveExif,
                    onCheckedChange = onSaveExifChange
                )

                Spacer(Modifier.height(8.dp))
            }
        }
    )
}

@Composable
private fun ExportSwitchRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .glassBackground(
                style = GlassStyle.Regular,
                shape = ShapeDefaults.large,
                color = MaterialTheme.colorScheme.surfaceContainer
            )
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        GlassSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun FormatCard(
    format: ImageFormat,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val subtitle = when (format) {
        ImageFormat.Jpg -> stringResource(R.string.markup_export_format_jpg_sub)
        is ImageFormat.Png -> stringResource(R.string.markup_export_format_png_sub)
        else -> stringResource(R.string.markup_export_format_webp_sub)
    }
    SelectableCard(
        label = when (format) {
            ImageFormat.Jpg -> "JPG"
            is ImageFormat.Png -> "PNG"
            else -> "WEBP"
        },
        subtitle = subtitle,
        selected = selected,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun ResolutionCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: IntSize? = null,
    subtitle: String? = null,
) {
    SelectableCard(
        label = label,
        subtitle = subtitle ?: size?.let {
            stringResource(R.string.markup_export_dimensions, it.width, it.height)
        }.orEmpty(),
        selected = selected,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun SelectableCard(
    label: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .glassBackground(
                style = GlassStyle.Regular,
                shape = ShapeDefaults.large,
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else MaterialTheme.colorScheme.surfaceContainer
            )
            .then(
                if (selected) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = ShapeDefaults.large
                    )
                } else Modifier
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (subtitle.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SizeField(
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
        modifier = modifier
    )
}

/** 宽变动时按原图比例联动高度 */
private fun linkedHeight(
    width: Int,
    sourceSize: IntSize?
): Int {
    if (width <= 0 || sourceSize == null || sourceSize.width <= 0) return 0
    return (width.toDouble() * sourceSize.height / sourceSize.width).roundToInt().coerceAtLeast(1)
}

/** 高变动时按原图比例联动宽度 */
private fun linkedWidth(
    height: Int,
    sourceSize: IntSize?
): Int {
    if (height <= 0 || sourceSize == null || sourceSize.height <= 0) return 0
    return (height.toDouble() * sourceSize.width / sourceSize.height).roundToInt().coerceAtLeast(1)
}

/** 顶部信息行:导出尺寸 / 源文件大小 / 预估导出大小,三项均拿不到时显示「--」 */
@Composable
private fun ExportInfoRow(
    settings: ExportSettings,
    sourceSize: IntSize?,
    imageUri: Uri?,
    estimateBitmap: Bitmap?,
) {
    val unavailable = stringResource(R.string.markup_export_size_unavailable)
    val targetSize = remember(settings, sourceSize) { exportTargetSize(settings, sourceSize) }
    val dimensionsText = targetSize?.let {
        stringResource(R.string.markup_export_dimensions, it.width, it.height)
    } ?: unavailable

    // 源文件大小:uri 经 contentResolver 查 OpenableColumns.SIZE;空白画布等
    // 无 uri(或查不到)场景用位图内存占用估算,估算值带「约」前缀
    val sourceSizeInfo = remember(imageUri, estimateBitmap) {
        imageUri?.fileSize()?.takeIf { it > 0 }?.let { it to false }
            ?: estimateBitmap?.byteCount?.toLong()?.let { it to true }
    }
    val sourceSizeText = sourceSizeInfo?.let { (bytes, approx) ->
        val text = humanFileSize(bytes)
        if (approx) stringResource(R.string.markup_export_size_approx, text) else text
    } ?: unavailable

    // 预估导出大小:预览位图按当前格式/质量编码一次得 bytesPerPixel,外推到目标
    // 分辨率;设置/底图变化时 produceState 自动取消重算,期间保留上一次结果避免闪烁
    val estimatedBytes by produceState<Long?>(
        initialValue = null,
        estimateBitmap, settings.format, settings.quality, settings.jpegBackgroundColor, targetSize
    ) {
        val bitmap = estimateBitmap
        val target = targetSize
        if (bitmap == null || target == null) {
            value = null
            return@produceState
        }
        value = withContext(Dispatchers.Default) {
            estimateEncodedSize(
                bitmap, settings.format, settings.quality, settings.jpegBackgroundColor, target
            )
        }
    }
    val estimatedText = estimatedBytes?.let {
        stringResource(R.string.markup_export_size_approx, humanFileSize(it))
    } ?: unavailable

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .glassBackground(
                style = GlassStyle.Regular,
                shape = ShapeDefaults.large,
                color = MaterialTheme.colorScheme.surfaceContainer
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        ExportInfoItem(
            label = stringResource(R.string.markup_export_current_size),
            value = dimensionsText,
            modifier = Modifier.weight(1f)
        )
        ExportInfoItem(
            label = stringResource(R.string.markup_export_source_size),
            value = sourceSizeText,
            modifier = Modifier.weight(1f)
        )
        ExportInfoItem(
            label = stringResource(R.string.markup_export_estimated_size),
            value = estimatedText,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ExportInfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1
        )
    }
}

/** 按导出设置换算目标分辨率(与保存链路一致,见 [ExportSettings.toImageInfo]) */
private fun exportTargetSize(
    settings: ExportSettings,
    sourceSize: IntSize?
): IntSize? {
    sourceSize ?: return null
    if (settings.validationError(sourceSize.width, sourceSize.height) != null) return null
    val info = settings.toImageInfo(sourceSize.width, sourceSize.height)
    return IntSize(info.width, info.height)
}

/** 编码预览位图求 bytesPerPixel 后外推到目标分辨率;失败返回 null */
private fun estimateEncodedSize(
    bitmap: Bitmap,
    format: ImageFormat,
    quality: Int,
    jpegBackgroundColor: Int,
    targetSize: IntSize,
): Long? = runCatching {
    val flattened = if (format == ImageFormat.Jpg) {
        Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888).apply {
            Canvas(this).apply {
                drawColor(jpegBackgroundColor)
                drawBitmap(bitmap, 0f, 0f, null)
            }
            setHasAlpha(false)
        }
    } else null
    try {
        ByteArrayOutputStream().use { output ->
            if (!(flattened ?: bitmap).compress(format.toCompressFormat(), quality, output)) {
                return null
            }
            val pixels = bitmap.width.toLong() * bitmap.height
            if (pixels <= 0) return null
            val bytesPerPixel = output.size().toDouble() / pixels
            (bytesPerPixel * targetSize.width * targetSize.height).toLong()
        }
    } finally {
        flattened?.recycle()
    }
}.getOrNull()

private fun ImageFormat.toCompressFormat(): Bitmap.CompressFormat = when (this) {
    is ImageFormat.Png -> Bitmap.CompressFormat.PNG
    is ImageFormat.Webp -> Bitmap.CompressFormat.WEBP
    else -> Bitmap.CompressFormat.JPEG
}
