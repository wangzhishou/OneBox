package com.wanbaohe.idphoto.presentation.components

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.utils.humanFileSize
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCustomSlider
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.idphoto.R
import com.wanbaohe.idphoto.domain.IdPhotoExportConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * 证件照导出配置面板(保存时弹出,参考图片创作的导出设置):
 * 分辨率提示 + 格式 + 质量 + 打印尺寸说明;背景选择已挪到「背景」tab。
 */
@Composable
fun IdPhotoExportConfigPanel(
    config: IdPhotoExportConfig,
    onConfigChanged: (IdPhotoExportConfig) -> Unit,
    resolutionTip: String = "",
    isResolutionSufficient: Boolean? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 分辨率提示（如果有）
        if (resolutionTip.isNotEmpty()) {
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                color = if (isResolutionSufficient == true) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                }
            ) {
                Text(
                    text = resolutionTip,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = if (isResolutionSufficient == true) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 格式选择
        Text(
            text = stringResource(R.string.id_photo_export_format),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        FormatSelector(
            selectedFormat = config.format,
            onFormatSelected = { onConfigChanged(config.copy(format = it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 质量调节
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.id_photo_quality),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(16.dp))
                GlassCustomSlider(
                    value = config.quality.toFloat(),
                    onValueChange = { onConfigChanged(config.copy(quality = it.toInt())) },
                    valueRange = 50f..100f,
                    steps = 9,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${config.quality}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = stringResource(R.string.id_photo_quality_tip),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 打印尺寸说明（只读信息）
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.id_photo_print_info),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.id_photo_print_info_tip),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    Spacer(modifier = Modifier.height(AppTheme.dimens.paddingSmall))
}

/**
 * 格式选择器
 */
@Composable
private fun FormatSelector(
    selectedFormat: ImageFormat,
    onFormatSelected: (ImageFormat) -> Unit
) {
    val formats = listOf(
        "JPG" to ImageFormat.Jpg,
        "PNG" to ImageFormat.Png.Lossy,
        "WebP" to ImageFormat.Webp.Lossy
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        formats.forEach { (name, format) ->
            val isSelected = selectedFormat::class == format::class
            FormatChip(
                text = name,
                isSelected = isSelected,
                onClick = { onFormatSelected(format) }
            )
        }
    }
}

/**
 * 格式选择按钮
 */
@Composable
private fun FormatChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    GlassTonalButton(
        onClick = onClick,
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(text = text)
    }
}


/**
 * 导出信息行(参考图像创作的导出设置):导出尺寸 / 源图尺寸 / 预估导出大小。
 * 预估导出大小为纯估算:用当前预览位图按所选格式/质量编码一次得 bytesPerPixel,
 * 外推到目标导出分辨率;设置或预览变化时自动重算。
 */
@Composable
fun IdPhotoExportInfoRow(
    targetWidth: Int,
    targetHeight: Int,
    sourceSize: Pair<Int, Int>?,
    estimateBitmap: Bitmap?,
    format: ImageFormat,
    quality: Int,
    modifier: Modifier = Modifier
) {
    val unavailable = stringResource(R.string.id_photo_export_size_unavailable)
    val dimensionsText = stringResource(R.string.id_photo_export_dimensions, targetWidth, targetHeight)
    val sourceSizeText = sourceSize?.let {
        stringResource(R.string.id_photo_export_dimensions, it.first, it.second)
    } ?: unavailable

    val estimatedBytes by produceState<Long?>(
        initialValue = null,
        estimateBitmap, format, quality, targetWidth, targetHeight
    ) {
        val bitmap = estimateBitmap
        if (bitmap == null) {
            value = null
            return@produceState
        }
        value = withContext(Dispatchers.Default) {
            estimateEncodedSize(bitmap, format, quality, targetWidth, targetHeight)
        }
    }
    val estimatedText = estimatedBytes?.let {
        stringResource(R.string.id_photo_export_size_approx, humanFileSize(it))
    } ?: unavailable

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(ShapeDefaults.large)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        ExportInfoItem(
            label = stringResource(R.string.id_photo_export_target_size),
            value = dimensionsText,
            modifier = Modifier.weight(1f)
        )
        ExportInfoItem(
            label = stringResource(R.string.id_photo_export_source_size),
            value = sourceSizeText,
            modifier = Modifier.weight(1f)
        )
        ExportInfoItem(
            label = stringResource(R.string.id_photo_export_estimated_size),
            value = estimatedText,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ExportInfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1
        )
    }
}

/** 编码预览位图求 bytesPerPixel 后外推到目标分辨率;失败返回 null */
private fun estimateEncodedSize(
    bitmap: Bitmap,
    format: ImageFormat,
    quality: Int,
    targetWidth: Int,
    targetHeight: Int
): Long? = runCatching {
    val output = ByteArrayOutputStream()
    bitmap.compress(format.toCompressFormat(), quality, output)
    val pixels = bitmap.width.toLong() * bitmap.height
    if (pixels <= 0) return null
    val bytesPerPixel = output.size().toDouble() / pixels
    (bytesPerPixel * targetWidth * targetHeight).toLong()
}.getOrNull()

private fun ImageFormat.toCompressFormat(): Bitmap.CompressFormat = when (this) {
    is ImageFormat.Png -> Bitmap.CompressFormat.PNG
    is ImageFormat.Webp -> Bitmap.CompressFormat.WEBP
    else -> Bitmap.CompressFormat.JPEG
}
