package com.wanbaohe.camera.watermark.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCustomSlider
import com.wanbaohe.camera.watermark.R
import com.wanbaohe.camera.watermark.domain.ExportConfig

/**
 * 导出配置面板
 * @param config 导出配置
 * @param onConfigChanged 配置变更回调
 * @param authorSignature 作者签名
 * @param onAuthorSignatureChanged 作者签名变更回调
 * @param keepOriginalExif 是否保留原始 EXIF
 * @param onKeepOriginalExifChanged 保留 EXIF 变更回调
 */
@Composable
fun ExportConfigPanel(
    config: ExportConfig,
    onConfigChanged: (ExportConfig) -> Unit,
    authorSignature: String = "万宝盒",
    onAuthorSignatureChanged: (String) -> Unit = {},
    keepOriginalExif: Boolean = true,
    onKeepOriginalExifChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // 格式选择
        Text(
            text = stringResource(R.string.camera_watermark_export_format),
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.camera_watermark_quality),
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

        Spacer(modifier = Modifier.height(16.dp))

        // 保留原始 EXIF 开关
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.camera_watermark_keep_exif),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(R.string.camera_watermark_keep_exif_tip),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            GlassSwitch(
                checked = keepOriginalExif,
                onCheckedChange = onKeepOriginalExifChanged,
                colors = AppTheme.colors.switchColors()
            )
        }

        // 作者签名（仅在保留 EXIF 时显示）
        if (keepOriginalExif) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = authorSignature,
                onValueChange = onAuthorSignatureChanged,
                label = { Text(stringResource(R.string.camera_watermark_author_signature)) },
                placeholder = { Text(stringResource(R.string.camera_watermark_author_signature_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = AppTheme.colors.getOutlinedTextFieldColors()
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

    androidx.compose.material3.FilledTonalButton(
        onClick = onClick,
        colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(text = text)
    }
}

