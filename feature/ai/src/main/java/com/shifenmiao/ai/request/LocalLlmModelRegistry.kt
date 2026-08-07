package com.shifenmiao.ai.request

import com.shifenmiao.model.ai.AiProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 本地模型注册表。
 *
 * Phase 1：内存桩实现，预置一个 demo spec，保证主链路在没有任何用户导入时也能跑通。
 * Phase 2：替换为 [LocalLlmModelEntity] + Room 查询实现，对接 LocalLlmModelRepository。
 */
interface LocalLlmModelRegistry {
    fun resolve(modelName: String): LocalLlmModelSpec?
}

@Singleton
class InMemoryLocalLlmModelRegistry @Inject constructor() : LocalLlmModelRegistry {

    private val specs: Map<String, LocalLlmModelSpec> = mapOf(
        "local-stub" to LocalLlmModelSpec(
            id = "local-stub",
            displayName = "Local Stub (demo)",
            modelPath = "/dev/null",
            contextWindowTokens = 4096,
            maxOutputTokens = 512,
            quantization = "Q4_K_M",
            estimatedMemoryMb = 0,
        ),
    )

    override fun resolve(modelName: String): LocalLlmModelSpec? = specs[modelName]
}
