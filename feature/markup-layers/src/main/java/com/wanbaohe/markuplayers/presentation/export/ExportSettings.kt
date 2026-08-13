package com.wanbaohe.markuplayers.presentation.export

import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.domain.image.model.Quality

/**
 * 导出设置(对应设计稿「导出/保存」面板)。
 *
 * 已核实的核心能力边界:
 * - 格式 / 质量 / 分辨率:经 [toImageInfo] 写入 [ImageInfo],
 *   由 `ImageCompressor.compressAndTransform` 消费(按宽高缩放、按格式与质量编码)。
 * - 自定义文件名:FileController 保存时一律用 FilenameCreator 重算文件名,
 *   调用方传入的 filename 会被覆盖,故不提供文件名输入。
 * - 「保存副本(不覆盖原图)」:覆盖写只有全局设置开关,无按次保存的 API,故不提供。
 */
data class ExportSettings(
    val format: ImageFormat = ImageFormat.Jpg,
    val quality: Int = 90,
    val resolution: ExportResolution = ExportResolution.Original,
    val customWidth: Int = 0,
    val customHeight: Int = 0,
    val shareAfterSave: Boolean = false
) {

    /** 按导出设置生成目标 [ImageInfo],[sourceWidth]/[sourceHeight] 为渲染结果(原图)尺寸 */
    fun toImageInfo(
        sourceWidth: Int,
        sourceHeight: Int
    ): ImageInfo {
        val (width, height) = when (resolution) {
            ExportResolution.Original -> sourceWidth to sourceHeight
            ExportResolution.Half -> sourceWidth / 2 to sourceHeight / 2
            ExportResolution.Quarter -> sourceWidth / 4 to sourceHeight / 4
            ExportResolution.Custom -> {
                if (customWidth > 0 && customHeight > 0) customWidth to customHeight
                else sourceWidth to sourceHeight
            }
        }
        return ImageInfo(
            width = width.coerceAtLeast(1),
            height = height.coerceAtLeast(1),
            quality = Quality.Base(quality.coerceIn(10, 100)),
            imageFormat = format
        )
    }

    companion object {
        /** 面板提供的三种导出格式 */
        val supportedFormats = listOf(
            ImageFormat.Jpg,
            ImageFormat.Png.Lossless,
            ImageFormat.Webp.Lossy
        )
    }
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
