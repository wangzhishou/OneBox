package com.wanbaohe.dsh.wire.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

/**
 * settings / credentials / llm 配置面 wire 模型(DSH-PROTOCOL §3/§6,
 * 对齐 Flutter settings.dart / credentials.dart / llm.dart)。
 *
 * - settings/credentials 全族与 llm.discoverModels 是特权方法,仅 loopback
 *   (或经网关鉴权的远程形态)可用;LAN 直连 403,UI 按 PrivilegeScope 门控
 * - settings.describe 一次读全部 namespace(快照 + schema + revision)
 * - settings.mutate 走路径 op(set/unset),expectedRevision 乐观锁(CAS);
 *   冲突 settings-conflict → 自动重读后再抛
 * - credentials.describe 只报 configured/source/writable,不含值
 */

/** settings 命名空间视图(settings.describe 的 namespaces 元素 / mutate/update 回值) */
@Serializable
data class SettingsNamespaceView(
    val ns: String,
    /** 结构 schema(动态读枚举/默认值用;原样保留 JsonElement) */
    val schema: JsonElement = JsonNull,
    /** 生效值(base + user 合并后) */
    val value: JsonElement = JsonNull,
    val base: JsonElement? = null,
    val user: JsonElement? = null,
    val applies: JsonElement = JsonNull,
    val secrets: List<SettingsSecretView> = emptyList(),
    /** 乐观锁水位:每次写入递增,mutate/update 的 expectedRevision 取自它 */
    val revision: Double = 0.0
)

/** mutate 回值与命名空间视图同形,直接复用 */
typealias SettingsMutateValue = SettingsNamespaceView

/** settings 文档中的 secret 槽位(只报是否已设置,不含值) */
@Serializable
data class SettingsSecretView(
    val path: List<String>,
    @SerialName("set") val set: Boolean
)

@Serializable
data class SettingsDescribeValue(
    val writable: Boolean,
    val hasDocument: Boolean,
    val namespaces: List<SettingsNamespaceView>
)

@Serializable
data class SettingsOpenDocumentValue(
    val opened: Boolean
)

// ───────────────────────────── credentials ─────────────────────────────

/** 凭据状态视图(只报 configured/source/writable,不含值) */
@Serializable
data class CredentialView(
    val configured: Boolean,
    val source: String? = null,
    val writable: Boolean
)

@Serializable
data class CredentialsDescribeValue(
    val credentials: Map<String, CredentialView>
)

// ───────────────────────────── llm ─────────────────────────────

/** llm.providers 的可配置提供方视图;[active] = 适配器当前可路由 */
@Serializable
data class ConfigurableProviderView(
    val provider: String,
    val displayName: String,
    val settingsNs: String,
    val settingsPath: List<String>,
    val active: Boolean,
    /** 自定义提供方(declared == true) */
    val declared: Boolean? = null
)

@Serializable
data class LlmProvidersValue(
    val providers: List<ConfigurableProviderView>
)

/** llm.discoverModels 发现的单个模型 */
@Serializable
data class DiscoveredModelView(
    val id: String,
    val name: String? = null,
    val contextWindow: Int? = null,
    val maxTokens: Int? = null
)

@Serializable
data class LlmDiscoverModelsValue(
    val models: List<DiscoveredModelView>
)

/** llm.models:全量模型目录(ModelProviderGroup/ModelCatalogFailure 见 Llm.kt) */
@Serializable
data class LlmModelsValue(
    val groups: List<ModelProviderGroup>,
    val failures: List<ModelCatalogFailure> = emptyList()
)
