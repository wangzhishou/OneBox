package com.wanbaohe.markuplayers.presentation.export

import android.graphics.Color
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.domain.image.model.Quality
import kotlin.math.roundToInt

data class ExportSettings(
    val format: ImageFormat = ImageFormat.Jpg,
    val quality: Int = 90,
    val resolution: ExportResolution = ExportResolution.Original,
    val customWidth: Int = 0,
    val customHeight: Int = 0,
    val shareAfterSave: Boolean = false,
    val saveCopy: Boolean = true,
    val jpegBackgroundColor: Int = Color.WHITE,
    val maxFileSizeBytes: Long? = null
) {

    fun validationError(sourceWidth: Int, sourceHeight: Int): ExportValidationError? = when {
        sourceWidth <= 0 || sourceHeight <= 0 -> ExportValidationError.InvalidSourceSize
        format !in supportedFormats -> ExportValidationError.UnsupportedFormat
        quality !in MIN_QUALITY..100 -> ExportValidationError.InvalidQuality
        resolution == ExportResolution.Custom && (customWidth <= 0 || customHeight <= 0) ->
            ExportValidationError.InvalidCustomSize
        format == ImageFormat.Jpg && Color.alpha(jpegBackgroundColor) != 255 ->
            ExportValidationError.TransparentJpegBackground
        maxFileSizeBytes != null && maxFileSizeBytes < MIN_FILE_SIZE_BYTES ->
            ExportValidationError.FileSizeTooSmall
        else -> null
    }

    /** Custom dimensions are a bounding box, never a request to stretch the image. */
    fun toImageInfo(
        sourceWidth: Int,
        sourceHeight: Int
    ): ImageInfo {
        require(validationError(sourceWidth, sourceHeight) == null) {
            validationError(sourceWidth, sourceHeight)?.message.orEmpty()
        }
        val (width, height) = when (resolution) {
            ExportResolution.Original -> sourceWidth to sourceHeight
            ExportResolution.Half -> (sourceWidth / 2).coerceAtLeast(1) to
                (sourceHeight / 2).coerceAtLeast(1)
            ExportResolution.Quarter -> (sourceWidth / 4).coerceAtLeast(1) to
                (sourceHeight / 4).coerceAtLeast(1)
            ExportResolution.Custom -> fitDimensions(
                sourceWidth, sourceHeight, customWidth, customHeight
            )
        }
        return ImageInfo(
            width = width,
            height = height,
            quality = Quality.Base(quality),
            imageFormat = format
        )
    }

    fun withPurposePreset(
        preset: ExportPurposePreset,
        sourceWidth: Int,
        sourceHeight: Int
    ): ExportSettings {
        require(sourceWidth > 0 && sourceHeight > 0) { ExportValidationError.InvalidSourceSize.message }
        val (width, height) = fitDimensions(
            sourceWidth, sourceHeight, preset.longEdge, preset.longEdge,
            allowUpscale = false
        )
        return copy(
            resolution = ExportResolution.Custom,
            customWidth = width,
            customHeight = height
        )
    }

    companion object {
        const val MIN_QUALITY = 10
        const val MIN_FILE_SIZE_BYTES = 1024L

        val supportedFormats = listOf(
            ImageFormat.Jpg,
            ImageFormat.Png.Lossless,
            ImageFormat.Webp.Lossy
        )
    }
}

enum class ExportPurposePreset(val longEdge: Int) {
    Sharing(1600),
    Avatar(512),
    Wallpaper(2560),
    Print(4096)
}

enum class ExportValidationError(val message: String) {
    InvalidSourceSize("The source image has no valid dimensions."),
    InvalidCustomSize("Enter a positive width and height."),
    InvalidQuality("Quality must be between 10 and 100."),
    UnsupportedFormat("This export format is not supported."),
    TransparentJpegBackground("Choose an opaque background for JPG."),
    FileSizeTooSmall("The file size limit must be at least 1 KB; smaller limits cannot reliably hold an encoded image.")
}

private fun fitDimensions(
    sourceWidth: Int,
    sourceHeight: Int,
    maxWidth: Int,
    maxHeight: Int,
    allowUpscale: Boolean = true
): Pair<Int, Int> {
    val scale = minOf(
        maxWidth.toDouble() / sourceWidth,
        maxHeight.toDouble() / sourceHeight,
        if (allowUpscale) Double.MAX_VALUE else 1.0
    )
    return (sourceWidth * scale).roundToInt().coerceIn(1, maxWidth) to
        (sourceHeight * scale).roundToInt().coerceIn(1, maxHeight)
}

enum class ExportResolution {
    Original, Half, Quarter, Custom
}

/** 把加载到的原图格式归并到面板支持的导出格式 */
internal fun ImageFormat.toExportFormat(): ImageFormat = when (this) {
    is ImageFormat.Png -> ImageFormat.Png.Lossless
    is ImageFormat.Webp -> ImageFormat.Webp.Lossy
    else -> ImageFormat.Jpg
}
