package com.wanbaohe.idphoto.presentation.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCustomSlider
import com.wanbaohe.idphoto.R
import com.wanbaohe.idphoto.domain.IdPhotoBackground
import com.wanbaohe.idphoto.domain.IdPhotoExportConfig
import com.wanbaohe.idphoto.util.localizedBackgroundName
import com.t8rin.imagetoolbox.core.resources.icons.Check

/**
 * 证件照导出配置面板
 */
@Composable
fun IdPhotoExportConfigPanel(
    config: IdPhotoExportConfig,
    onConfigChanged: (IdPhotoExportConfig) -> Unit,
    currentBackground: IdPhotoBackground,
    onBackgroundSelected: (IdPhotoBackground) -> Unit,
    resolutionTip: String = "",
    isResolutionSufficient: Boolean? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 分辨率提示（如果有）
        if (resolutionTip.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isResolutionSufficient == true) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        },
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = resolutionTip,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isResolutionSufficient == true) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 背景色选择
        Text(
            text = stringResource(R.string.id_photo_background_color),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        BackgroundColorSelector(
            selectedBackground = currentBackground,
            onBackgroundSelected = onBackgroundSelected
        )

        Spacer(modifier = Modifier.height(16.dp))

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
 * 背景色选择器
 */
@Composable
private fun BackgroundColorSelector(
    selectedBackground: IdPhotoBackground,
    onBackgroundSelected: (IdPhotoBackground) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IdPhotoBackground.PRESETS.forEach { background ->
            BackgroundColorItem(
                background = background,
                isSelected = background.color == selectedBackground.color,
                onClick = { onBackgroundSelected(background) }
            )
        }
    }
}

/**
 * 单个背景色选项
 */
@Composable
private fun BackgroundColorItem(
    background: IdPhotoBackground,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(background.getColor(), CircleShape)
                .then(
                    if (isSelected) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    } else {
                        Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                    }
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                    contentDescription = stringResource(R.string.id_photo_selected),
                    tint = if (background.color == 0xFFFFFFFF) {
                        Color.Black
                    } else {
                        Color.White
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = localizedBackgroundName(background.name),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
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

    FilledTonalButton(
        onClick = onClick,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(text = text)
    }
}

