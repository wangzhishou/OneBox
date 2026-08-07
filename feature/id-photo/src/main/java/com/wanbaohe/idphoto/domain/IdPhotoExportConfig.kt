package com.wanbaohe.idphoto.domain

import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat

/**
 * 证件照导出配置
 */
data class IdPhotoExportConfig(
    val format: ImageFormat = ImageFormat.Jpg,
    val quality: Int = 95,
)

