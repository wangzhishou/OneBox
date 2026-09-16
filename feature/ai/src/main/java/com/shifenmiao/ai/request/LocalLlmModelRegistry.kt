package com.shifenmiao.ai.request

/**
 * 本地模型注册表。
 *
 * Phase 1：内存桩实现，预置一个 demo spec，保证主链路在没有任何用户导入时也能跑通。
 * Phase 2：替换为 [LocalLlmModelEntity] + Room 查询实现，对接 LocalLlmModelRepository。
 *
 * 实现按渠道拆分:google/foss 见 src/litertlm 的 [InMemoryLocalLlmModelRegistry],
 * 国内渠道见 src/domestic 的空实现(端侧模型仅海外渠道可用)。
 */
interface LocalLlmModelRegistry {
    fun resolve(modelName: String): LocalLlmModelSpec?
}
