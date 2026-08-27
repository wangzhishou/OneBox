package com.shifenmiao.imagegeneration.provider

import com.shifenmiao.imagegeneration.model.ImageGenerationRequest
import com.shifenmiao.imagegeneration.model.ImageGenerationResult
import com.shifenmiao.imagegeneration.model.ImageProviderConfig
import com.shifenmiao.imagegeneration.model.ImageProviderDescriptor

/**
 * 图片模型 Provider 扩展点。
 *
 * 新增厂商只需实现本接口并通过 Hilt @IntoSet 注册，无需修改中央路由逻辑。
 */
interface ImageGenerationProvider {
    val descriptor: ImageProviderDescriptor

    suspend fun generate(
        config: ImageProviderConfig,
        request: ImageGenerationRequest,
    ): Result<ImageGenerationResult>
}
