package com.wanbaohe.dsh.wire.model

import kotlinx.serialization.Serializable

/**
 * 模型选择面 wire 模型(session.models / session.selectModel,DSH-PROTOCOL §5)。
 *
 * 对齐 Flutter sessions.dart 的 ModelSelection / ModelProviderGroup /
 * SessionModelsValue / SessionSelectModelValue:
 * - 选择可与目录成员无关(服务端语义,目录只是展示面)
 * - [SessionModelsValue.routable] = 适配器当前是否服务该 provider;
 *   false 时 prompt 前服务端会 model-unavailable,UI 只警示不拦截
 */

/** 当前选择(provider + model + 可选推理力度) */
@Serializable
data class ModelSelection(
    val provider: String,
    val model: String,
    val reasoningEffort: String? = null
)

/** 推理力度档(如 low/high) */
@Serializable
data class ModelReasoningEffort(
    val id: String,
    val name: String,
    val description: String? = null
)

/** 模型的推理能力声明;[defaultEffort] 为主机推荐默认档 */
@Serializable
data class ModelReasoning(
    val efforts: List<ModelReasoningEffort>,
    val defaultEffort: String? = null
)

/** 目录中的单个模型 */
@Serializable
data class ModelCatalogModel(
    val id: String,
    val name: String,
    val description: String? = null,
    val reasoning: ModelReasoning? = null
)

/** provider 分组的目录块 */
@Serializable
data class ModelProviderGroup(
    val id: String,
    val name: String,
    val models: List<ModelCatalogModel>
)

/** 单个 provider 目录拉取失败(不拖垮整个目录) */
@Serializable
data class ModelCatalogFailure(
    val id: String,
    val name: String,
    val message: String
)

/** session.models 的响应 value:目录 + 当前选择 + routable */
@Serializable
data class SessionModelsValue(
    val current: ModelSelection,
    val routable: Boolean,
    val groups: List<ModelProviderGroup>,
    val failures: List<ModelCatalogFailure> = emptyList()
)

/** session.selectModel 的响应 value:回带规范化后的选择 */
@Serializable
data class SessionSelectModelValue(
    val selected: ModelSelection
)

/** 在目录中查找某选择对应的模型条目(选择可与目录成员无关,找不到返回 null) */
fun SessionModelsValue.catalogEntryOf(selection: ModelSelection): ModelCatalogModel? {
    for (group in groups) {
        if (group.id != selection.provider) continue
        return group.models.firstOrNull { it.id == selection.model }
    }
    return null
}
