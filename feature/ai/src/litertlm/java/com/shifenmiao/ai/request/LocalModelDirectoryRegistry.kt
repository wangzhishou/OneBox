package com.shifenmiao.ai.request

import com.shifenmiao.common.localmodel.LocalModelRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalModelDirectoryRegistry @Inject constructor(
    private val localModelRepository: LocalModelRepository,
) : LocalLlmModelRegistry {

    override fun resolve(modelName: String): LocalLlmModelSpec? {
        if (modelName == STUB_SPEC.id) return STUB_SPEC
        val model = localModelRepository.findInstalledModel(modelName) ?: return null
        return LocalLlmModelSpec(
            id = modelName,
            displayName = modelName,
            modelPath = model.path,
            contextWindowTokens = 4096,
            maxOutputTokens = 1024,
            quantization = "unknown",
            estimatedMemoryMb = (model.sizeBytes / (1024 * 1024)).toInt(),
        )
    }

    private companion object {
        val STUB_SPEC = LocalLlmModelSpec(
            id = "local-stub",
            displayName = "Local Stub (demo)",
            modelPath = "/dev/null",
            contextWindowTokens = 4096,
            maxOutputTokens = 512,
            quantization = "Q4_K_M",
            estimatedMemoryMb = 0,
        )
    }
}
