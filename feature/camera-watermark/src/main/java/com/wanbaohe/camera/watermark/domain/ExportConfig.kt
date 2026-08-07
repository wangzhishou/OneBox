package com.wanbaohe.camera.watermark.domain

import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat

/**
 * 导出配置
 */
data class ExportConfig(
    val format: ImageFormat = ImageFormat.Jpg,
    val quality: Int = 95,
    val keepOriginalSize: Boolean = true,
    val maxWidth: Int = 4096,
    val maxHeight: Int = 4096,
)

/**
 * 导出结果
 */
sealed class ExportResult {
    data class Success(val path: String, val fileSize: Long) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

