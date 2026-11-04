package com.shifenmiao.ai.request

/**
 * 本地模型注册表。
 *
 * google/foss 通过 LocalModelRepository 解析已安装模型，国内渠道使用空实现。
 * 实现按渠道拆分:google/foss 见 src/litertlm，国内渠道见 src/domestic。
 */
interface LocalLlmModelRegistry {
    fun resolve(modelName: String): LocalLlmModelSpec?
}
