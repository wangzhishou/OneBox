package com.shifenmiao.imagegeneration.service

import com.shifenmiao.imagegeneration.model.ImageGenerationRequest
import com.shifenmiao.imagegeneration.model.ImageGenerationResult
import com.shifenmiao.imagegeneration.model.ImageProviderConfig
import com.shifenmiao.imagegeneration.model.ImageProviderDescriptor
import com.shifenmiao.imagegeneration.provider.ImageGenerationProvider
import com.shifenmiao.imagegeneration.repository.ImageProviderConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.CancellationException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageGenerationManagerImpl @Inject constructor(
    providers: Set<@JvmSuppressWildcards ImageGenerationProvider>,
    private val repository: ImageProviderConfigRepository,
) : ImageGenerationManager {
    private val providersById: Map<String, ImageGenerationProvider> = providers.associateBy {
        it.descriptor.providerId
    }.also { providerMap ->
        require(providerMap.size == providers.size) { "Duplicate image generation provider id" }
        require(providerMap.isNotEmpty()) { "No image generation provider registered" }
    }

    init {
        if (repository.configs.value.isEmpty() && !repository.isInitialized()) {
            val defaultProvider = providersById.values.minBy { it.descriptor.providerId }
            val config = defaultProvider.descriptor.toConfig(id = ImageGenerationManager.DEFAULT_CONFIG_ID)
            repository.replace(listOf(config), config.id)
        }
    }

    override fun observeConfigs(): Flow<List<ImageProviderConfig>> = repository.configs

    override fun observeActiveConfig(): Flow<ImageProviderConfig?> = combine(
        repository.configs,
        repository.activeConfigId,
    ) { configs, activeId ->
        configs.firstOrNull { it.id == activeId && it.enabled }
    }

    override fun getConfigs(): List<ImageProviderConfig> = repository.configs.value

    override fun getActiveConfig(): ImageProviderConfig? {
        val activeId = repository.activeConfigId.value
        return repository.configs.value.firstOrNull { it.id == activeId && it.enabled }
    }

    override fun getProviderDescriptors(): List<ImageProviderDescriptor> = providersById.values
        .map(ImageGenerationProvider::descriptor)
        .sortedBy(ImageProviderDescriptor::displayName)

    override fun createDefaultConfig(providerId: String): ImageProviderConfig {
        val provider = providersById[providerId] ?: error("Unsupported image provider: $providerId")
        return provider.descriptor.toConfig(id = UUID.randomUUID().toString())
    }

    override fun saveConfig(config: ImageProviderConfig, makeActive: Boolean) {
        require(config.id.isNotBlank()) { "config id must not be blank" }
        require(config.displayName.isNotBlank()) { "display name must not be blank" }
        require(config.model.isNotBlank()) { "model must not be blank" }
        require(providersById.containsKey(config.providerId)) { "Unsupported image provider: ${config.providerId}" }
        require(!makeActive || config.enabled) { "Disabled image provider config cannot be active" }
        repository.upsert(config, makeActive)
    }

    override fun deleteConfig(id: String) {
        require(id != ImageGenerationManager.DEFAULT_CONFIG_ID) { "Default image provider config cannot be deleted" }
        repository.delete(id)
    }

    override fun setActiveConfig(id: String) {
        repository.setActive(id)
    }

    override suspend fun generate(request: ImageGenerationRequest): Result<ImageGenerationResult> {
        val config = getActiveConfig()
            ?: return Result.failure(IllegalStateException("No active image provider config"))
        return generateWithConfig(config, request)
    }

    override suspend fun generateWithConfig(
        config: ImageProviderConfig,
        request: ImageGenerationRequest,
    ): Result<ImageGenerationResult> {
        val provider = providersById[config.providerId]
            ?: return Result.failure(IllegalArgumentException("Unsupported image provider: ${config.providerId}"))
        return try {
            provider.generate(config, request)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ImageProviderDescriptor.toConfig(id: String) = ImageProviderConfig(
        id = id,
        providerId = providerId,
        displayName = displayName,
        baseUrl = defaultBaseUrl,
        proxyUrl = defaultProxyUrl,
        proxyPath = defaultProxyPath,
        model = defaultModel,
    )
}
