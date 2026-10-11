package com.wanbaohe.markuplayers.domain.recipe

import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo

data class ExportEncoded(
    val imageInfo: ImageInfo,
    val data: ByteArray
)

class ExportSizeLimitException(
    val limitBytes: Long,
    val smallestBytes: Long,
    val format: ImageFormat
) : IllegalArgumentException(
    if (format is ImageFormat.Png) {
        "Lossless PNG needs $smallestBytes bytes, exceeding the $limitBytes-byte limit. " +
            "Reduce the dimensions, choose JPG or WebP, or increase the limit."
    } else {
        "Even at minimum quality this image needs $smallestBytes bytes, exceeding the " +
            "$limitBytes-byte limit. Reduce the dimensions or increase the limit."
    }
)
