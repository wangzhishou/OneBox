package com.shifenmiao.imagegeneration.loader

import com.shifenmiao.imagegeneration.model.ImageGenerationRequest
import java.io.File

data class CachedGeneratedImage(
    /** App 内部持久文件，可直接传给 Coil/ImageGetter；跨 App 分享时需转换为 FileProvider URI。 */
    val file: File,
    val cacheKey: String,
    val fromCache: Boolean,
)

/** 将 Provider 返回的临时图片地址下载为本地持久缓存。 */
interface ImageGenerationLoader {

    /** 最简调用：同一提示词、活动配置和模型默认复用本地图片。 */
    suspend fun load(
        prompt: String,
        forceRefresh: Boolean = false,
    ): Result<CachedGeneratedImage>

    /** 高级调用：缓存键包含所有影响生成结果的请求参数。 */
    suspend fun load(
        request: ImageGenerationRequest,
        forceRefresh: Boolean = false,
    ): Result<CachedGeneratedImage>

    fun clearCache(): Result<Unit>
}
