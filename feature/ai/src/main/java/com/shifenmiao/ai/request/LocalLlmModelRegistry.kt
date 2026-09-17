package com.shifenmiao.ai.request

/**
 * 本地模型注册表。
 *
 * Phase 1：按目录扫描实现（google/foss 扫描 `files/local_models` 下的模型文件，
 * 国内渠道为空实现，端侧模型仅海外渠道可用）。
 * Phase 2：替换为 [LocalLlmModelEntity] + Room 查询实现，对接 LocalLlmModelRepository。
 *
 * 实现按渠道拆分:google/foss 见 src/litertlm 的 LocalModelDirectoryRegistry,
 * 国内渠道见 src/domestic 的空实现。
 */
interface LocalLlmModelRegistry {
    fun resolve(modelName: String): LocalLlmModelSpec?
}
