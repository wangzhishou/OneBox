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

package com.t8rin.imagetoolbox.core.domain.image.export

import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat

data class ImageExportConfig(
    val format: ImageFormat = ImageFormat.Jpg,
    val quality: Int = 90,
    val resolution: ImageExportResolution = ImageExportResolution.Original,
    val customWidth: Int = 0,
    val customHeight: Int = 0,
    val compressionControl: ImageExportCompressionControl = ImageExportCompressionControl.Quality,
    val maxFileSizeBytes: Long? = null,
    val metadataPolicy: ImageExportMetadataPolicy = ImageExportMetadataPolicy.RemoveAll,
    val backgroundColor: Int = -1,
)

enum class ImageExportResolution {
    Original, Half, Quarter, Custom
}

enum class ImageExportCompressionControl {
    Quality, TargetFileSize
}

enum class ImageExportMetadataPolicy {
    RemoveAll, KeepWithoutLocation, KeepAll
}

data class ImageExportCapabilities(
    val formats: List<ImageFormat> = listOf(
        ImageFormat.Jpg,
        ImageFormat.Png.Lossless,
        ImageFormat.Webp.Lossy,
    ),
    val supportsResize: Boolean = false,
    val supportsTargetFileSize: Boolean = false,
    val supportsSaveLocation: Boolean = false,
    val supportsSharing: Boolean = true,
    val supportsBackgroundColor: Boolean = false,
    val metadataPolicies: List<ImageExportMetadataPolicy> = emptyList(),
)

data class ImageExportSummary(
    val itemCount: Int = 1,
    val width: Int? = null,
    val height: Int? = null,
    val sourceSizeBytes: Long? = null,
)

fun ImageExportConfig.targetDimensions(
    sourceWidth: Int,
    sourceHeight: Int,
): Pair<Int, Int>? {
    if (sourceWidth <= 0 || sourceHeight <= 0) return null
    return when (resolution) {
        ImageExportResolution.Original -> sourceWidth to sourceHeight
        ImageExportResolution.Half -> (sourceWidth / 2).coerceAtLeast(1) to
            (sourceHeight / 2).coerceAtLeast(1)
        ImageExportResolution.Quarter -> (sourceWidth / 4).coerceAtLeast(1) to
            (sourceHeight / 4).coerceAtLeast(1)
        ImageExportResolution.Custom -> fitExportDimensions(
            sourceWidth = sourceWidth,
            sourceHeight = sourceHeight,
            maxWidth = customWidth,
            maxHeight = customHeight,
        )
    }
}

private fun fitExportDimensions(
    sourceWidth: Int,
    sourceHeight: Int,
    maxWidth: Int,
    maxHeight: Int,
): Pair<Int, Int>? {
    if (maxWidth <= 0 || maxHeight <= 0) return null
    val scale = minOf(
        maxWidth.toDouble() / sourceWidth,
        maxHeight.toDouble() / sourceHeight,
    )
    return (sourceWidth * scale).toInt().coerceIn(1, maxWidth) to
        (sourceHeight * scale).toInt().coerceIn(1, maxHeight)
}
