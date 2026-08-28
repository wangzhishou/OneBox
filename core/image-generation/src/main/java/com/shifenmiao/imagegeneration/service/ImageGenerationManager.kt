package com.shifenmiao.imagegeneration.service

import com.shifenmiao.imagegeneration.model.ImageGenerationRequest
import com.shifenmiao.imagegeneration.model.ImageGenerationResult
import com.shifenmiao.imagegeneration.model.ImageProviderConfig
import com.shifenmiao.imagegeneration.model.ImageProviderDescriptor
import kotlinx.coroutines.flow.Flow

interface ImageGenerationManager {
    fun observeConfigs(): Flow<List<ImageProviderConfig>>
    fun observeActiveConfig(): Flow<ImageProviderConfig?>
    fun getConfigs(): List<ImageProviderConfig>
    fun getActiveConfig(): ImageProviderConfig?
    fun getProviderDescriptors(): List<ImageProviderDescriptor>
    fun createDefaultConfig(providerId: String): ImageProviderConfig
    fun saveConfig(config: ImageProviderConfig, makeActive: Boolean = false)
    fun deleteConfig(id: String)
    fun setActiveConfig(id: String)

    suspend fun generate(request: ImageGenerationRequest): Result<ImageGenerationResult>

    /** 使用未保存草稿调用，供设置页连通性测试。 */
    suspend fun generateWithConfig(
        config: ImageProviderConfig,
        request: ImageGenerationRequest,
    ): Result<ImageGenerationResult>

    companion object {
        /** 内置默认配置 id，不可删除。 */
        const val DEFAULT_CONFIG_ID = "default-image-provider"
    }
}
