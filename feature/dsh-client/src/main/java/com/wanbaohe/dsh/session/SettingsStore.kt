package com.wanbaohe.dsh.session

import com.wanbaohe.dsh.connection.ConnectionPhase
import com.wanbaohe.dsh.connection.DshApiClient
import com.wanbaohe.dsh.connection.DshConnectionController
import com.wanbaohe.dsh.wire.DshJson
import com.wanbaohe.dsh.wire.HostFrame
import com.wanbaohe.dsh.wire.RpcBusinessException
import com.wanbaohe.dsh.wire.RpcErrorCodes
import com.wanbaohe.dsh.wire.model.ConfigurableProviderView
import com.wanbaohe.dsh.wire.model.CredentialView
import com.wanbaohe.dsh.wire.model.CredentialsDescribeValue
import com.wanbaohe.dsh.wire.model.LlmDiscoverModelsValue
import com.wanbaohe.dsh.wire.model.LlmModelsValue
import com.wanbaohe.dsh.wire.model.LlmProvidersValue
import com.wanbaohe.dsh.wire.model.SettingsDescribeValue
import com.wanbaohe.dsh.wire.model.SettingsMutateValue
import com.wanbaohe.dsh.wire.model.SettingsOpenDocumentValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 * settings / credentials / llm 配置域(对齐 Flutter settings_store.dart,DSH-PROTOCOL §6)。
 *
 * - settings/credentials 全族与 llm.discoverModels 是特权方法,仅 loopback
 *   (或经网关鉴权的远程形态)可用;UI 按 PrivilegeScope 门控,本 store 不重复判断
 * - settings.describe 一次读全部 namespace(快照 + schema + revision)
 * - settings.mutate 走路径 op(set/unset),expectedRevision 乐观锁(CAS);
 *   冲突 settings-conflict → 自动重读并抛 [SettingsConflictException](UI 提示重试)
 * - credentials.describe 只报 configured/source/writable,不含值
 * - providers 目录 = llm.providers(active=可路由)+ settings 值 + credentials
 *   徽标合并:绿点=密钥已配置,红点=引用缺失,无引用=无点
 * - 失效事件 settings/document-updated、credentials/updated、llm/adapters-updated
 *   经 host/remote-event 到达 → 收到即重拉;代际 ready → 全量重拉
 *
 * 生命周期与连接实例绑定:由组件层创建并 [dispose]。
 */

/** 凭据徽标状态(绿=已配置,红=引用缺失,无=无引用/无法判定) */
enum class CredentialStatus { Configured, Missing, None }

/** 提供方目录条目:llm.providers 视图 + settings 配置值 + credentials 徽标 */
data class ProviderEntry(
    val view: ConfigurableProviderView,
    /** 该提供方在 settings 里的配置值(settingsPath 下钻后的 map) */
    val config: JsonObject?,
    /** 关联凭据引用(配置值的 apiKeyEnv;无引用 = null) */
    val credentialRef: String?,
    val credentialStatus: CredentialStatus,
    val namespace: String,
    val settingsPath: List<String>,
    /** 目录构建时刻的 namespace revision(写入走最新快照的 revision) */
    val revision: Double
) {
    val providerId: String get() = view.provider
    val displayName: String get() = view.displayName

    /** 适配器当前是否服务该 provider(routable) */
    val routable: Boolean get() = view.active

    /** 自定义提供方标签(declared == true) */
    val custom: Boolean get() = view.declared == true

    /** 配置字段便捷读(值缺失/非字符串时返回 null) */
    fun field(key: String): String? =
        (config?.get(key) as? JsonPrimitive)?.contentOrNull
}

/** 整体目录快照(providers + namespaces + credentials 徽标) */
data class SettingsSnapshot(
    val providers: List<ProviderEntry> = emptyList(),
    val namespaces: Map<String, SettingsMutateValue> = emptyMap(),
    val credentials: Map<String, CredentialView> = emptyMap(),
    val writable: Boolean = false,
    val hasDocument: Boolean = false,
    /** 首次重拉尚未成功时为 true(UI 显示加载态) */
    val loading: Boolean = true
)

/** CAS 冲突:mutate/update 被 settings-conflict 拒绝后已自动重读,UI 提示用户重试 */
class SettingsConflictException(
    val namespace: String,
    val expectedRevision: Double?,
    val latestRevision: Double?
) : Exception("settings-conflict($namespace)")

class SettingsStore(
    private val api: DshApiClient,
    private val connection: DshConnectionController,
    parentScope: CoroutineScope
) {

    private val scope = CoroutineScope(
        parentScope.coroutineContext + SupervisorJob(parentScope.coroutineContext[Job])
    )

    private val _snapshot = MutableStateFlow(SettingsSnapshot())
    /** 目录快照流(refresh/mutate 成功后推) */
    val snapshot: StateFlow<SettingsSnapshot> = _snapshot.asStateFlow()

    @Volatile
    private var disposed = false
    private var started = false
    private var lastReadyGeneration = 0
    private var loadedOnce = false

    fun start() {
        if (started) return
        started = true
        scope.launch {
            connection.snapshots.collect { snapshot ->
                if (disposed || snapshot.phase != ConnectionPhase.Ready) return@collect
                if (snapshot.generation <= lastReadyGeneration) return@collect
                lastReadyGeneration = snapshot.generation
                // 重连 = 全量重取(无 since 续传)
                refreshQuietly()
            }
        }
        scope.launch { connection.hostFrames.collect(::onHostFrame) }
    }

    fun dispose() {
        disposed = true
        scope.cancel()
    }

    /** 某 namespace 的当前视图(读时取最新) */
    fun namespace(ns: String): SettingsMutateValue? = _snapshot.value.namespaces[ns]

    /**
     * 全量重拉:settings.describe → llm.providers → credentials.describe。
     * 顺序依赖:凭据引用(apiKeyEnv)要从 describe 的配置值里推导。
     */
    suspend fun refresh() {
        if (disposed) return
        val desc = DshJson.decodeFromJsonElement<SettingsDescribeValue>(
            api.call(RpcSettingsDescribe, buildJsonObject {})
        )
        val namespaces = desc.namespaces.associateBy { it.ns }
        val provs = DshJson.decodeFromJsonElement<LlmProvidersValue>(
            api.call(RpcLlmProviders, buildJsonObject {})
        )
        val refs = provs.providers
            .mapNotNull { credentialRefOf(it, namespaces) }
            .toSet()
        val creds = DshJson.decodeFromJsonElement<CredentialsDescribeValue>(
            api.call(RpcCredentialsDescribe, buildJsonObject {
                putJsonArray("refs") { refs.forEach { add(JsonPrimitive(it)) } }
            })
        )
        if (disposed) return
        loadedOnce = true
        _snapshot.value = SettingsSnapshot(
            providers = provs.providers.map { buildProviderEntry(it, namespaces, creds.credentials) },
            namespaces = namespaces,
            credentials = creds.credentials,
            writable = desc.writable,
            hasDocument = desc.hasDocument,
            loading = false
        )
    }

    /**
     * settings.mutate:路径 op(set/unset)+ expectedRevision 乐观锁。
     * 成功 → 本地快照用响应覆盖(新 revision 供下次 CAS),并写后重读;
     * settings-conflict → 自动重读后抛 [SettingsConflictException]。
     */
    suspend fun mutate(
        namespace: String,
        ops: List<JsonObject>,
        expectedRevision: Double? = null
    ): SettingsMutateValue {
        val payload = buildJsonObject {
            put("ns", namespace)
            putJsonArray("ops") { ops.forEach { add(it) } }
            expectedRevision?.let { put("expectedRevision", it) }
        }
        return runMutation(namespace, payload, RpcSettingsMutate, expectedRevision)
    }

    /** settings.mutate 单字段 set 便捷形态 */
    suspend fun setField(
        namespace: String,
        path: List<String>,
        value: JsonElement
    ): SettingsMutateValue = mutate(
        namespace,
        listOf(buildJsonObject {
            put("op", "set")
            putJsonArray("path") { path.forEach { add(JsonPrimitive(it)) } }
            put("value", value)
        }),
        expectedRevision = namespace(namespace)?.revision
    )

    /** settings.mutate 单字段 unset 便捷形态 */
    suspend fun unsetField(namespace: String, path: List<String>): SettingsMutateValue = mutate(
        namespace,
        listOf(buildJsonObject {
            put("op", "unset")
            putJsonArray("path") { path.forEach { add(JsonPrimitive(it)) } }
        }),
        expectedRevision = namespace(namespace)?.revision
    )

    /** settings.update:section patch 合并(同 CAS 语义) */
    suspend fun update(
        namespace: String,
        patch: JsonObject,
        expectedRevision: Double? = null
    ): SettingsMutateValue {
        val payload = buildJsonObject {
            put("ns", namespace)
            put("patch", patch)
            expectedRevision?.let { put("expectedRevision", it) }
        }
        return runMutation(namespace, payload, RpcSettingsUpdate, expectedRevision)
    }

    /** mutate/update 公共回环:成功落地 + 写后重读;冲突自动重读后抛 */
    private suspend fun runMutation(
        namespace: String,
        payload: JsonObject,
        method: String,
        expectedRevision: Double?
    ): SettingsMutateValue {
        if (disposed) throw CancellationException("SettingsStore disposed")
        try {
            val value = DshJson.decodeFromJsonElement<SettingsMutateValue>(
                api.call(method, payload)
            )
            // 用响应覆盖本地快照(权威 revision 落地,下一次 CAS 用它)
            val current = _snapshot.value
            _snapshot.value = current.copy(
                namespaces = current.namespaces + (namespace to value)
            )
            refreshQuietly()
            return value
        } catch (e: RpcBusinessException) {
            if (e.error.code == RpcErrorCodes.SettingsConflict) {
                // 冲突恢复语义:自动重读(权威 revision 落地),再抛给 UI 提示重试
                runCatching { refresh() }
                throw SettingsConflictException(
                    namespace,
                    expectedRevision = expectedRevision,
                    latestRevision = namespace(namespace)?.revision
                )
            }
            throw e
        }
    }

    /** credentials.set:只写(响应为空体);徽标对齐交给 credentials/updated,再保险重拉 */
    suspend fun setCredential(ref: String, value: String) {
        api.call(RpcCredentialsSet, buildJsonObject {
            put("ref", ref)
            put("value", value)
        })
        refreshQuietly()
    }

    /** credentials.unset(响应为空体) */
    suspend fun unsetCredential(ref: String) {
        api.call(RpcCredentialsUnset, buildJsonObject { put("ref", ref) })
        refreshQuietly()
    }

    /** llm.models:全量模型目录(组 + 失败项) */
    suspend fun llmModels(): LlmModelsValue =
        DshJson.decodeFromJsonElement(api.call(RpcLlmModels, buildJsonObject {}))

    /** llm.discoverModels:带未保存的草稿端点/密钥拉可用模型(web 同款语义) */
    suspend fun discoverModels(
        settingsNs: String,
        provider: String? = null,
        baseURL: String? = null,
        apiKind: String? = null,
        apiKey: String? = null
    ): LlmDiscoverModelsValue {
        return DshJson.decodeFromJsonElement(
            api.call(RpcLlmDiscoverModels, buildJsonObject {
                put("settingsNs", settingsNs)
                provider?.let { put("provider", it) }
                baseURL?.let { put("baseURL", it) }
                apiKind?.let { put("api", it) }
                apiKey?.let { put("apiKey", it) }
            })
        )
    }

    /** settings.openDocument(外壳「打开配置文件」;仅 loopback 可达) */
    suspend fun openDocument(): SettingsOpenDocumentValue =
        DshJson.decodeFromJsonElement(api.call(RpcSettingsOpenDocument, buildJsonObject {}))

    /** 静默重拉(帧/写后触发;失败不落地,下次触发再试) */
    private fun refreshQuietly() {
        if (disposed) return
        scope.launch {
            try {
                refresh()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                // 首次重拉失败保持 loading;已有数据时保持旧快照
                if (!loadedOnce) return@launch
            }
        }
    }

    /** 失效事件三件套:收到即重拉(host/remote-event 不重放,消费端自行重拉) */
    private fun onHostFrame(frame: HostFrame) {
        if (disposed || frame !is HostFrame.RemoteEvent) return
        when (frame.event) {
            EventSettingsDocumentUpdated,
            EventCredentialsUpdated,
            EventLlmAdaptersUpdated -> refreshQuietly()
        }
    }

    // ───────────────────────────── providers 目录合并 ─────────────────────────────

    private fun buildProviderEntry(
        view: ConfigurableProviderView,
        namespaces: Map<String, SettingsMutateValue>,
        credentials: Map<String, CredentialView>
    ): ProviderEntry {
        val ns = namespaces[view.settingsNs]
        val config = configAt(ns?.value, view.settingsPath)
        val ref = credentialRefOf(view, namespaces)
        val status = when {
            ref == null -> CredentialStatus.None
            credentials[ref]?.configured == true -> CredentialStatus.Configured
            else -> CredentialStatus.Missing
        }
        return ProviderEntry(
            view = view,
            config = config,
            credentialRef = ref,
            credentialStatus = status,
            namespace = view.settingsNs,
            settingsPath = view.settingsPath,
            revision = ns?.revision ?: 0.0
        )
    }

    /** 凭据引用 = 配置值的 apiKeyEnv(schema credential-ref 默认回退裁剪,见交付报告) */
    private fun credentialRefOf(
        view: ConfigurableProviderView,
        namespaces: Map<String, SettingsMutateValue>
    ): String? {
        val ns = namespaces[view.settingsNs] ?: return null
        val config = configAt(ns.value, view.settingsPath) ?: return null
        return (config["apiKeyEnv"] as? JsonPrimitive)
            ?.contentOrNull?.takeIf { it.isNotEmpty() }
    }

    /** 沿 path 下钻 value(中间必须是 object;缺失返回 null) */
    private fun configAt(value: JsonElement?, path: List<String>): JsonObject? {
        var current = value ?: return null
        for (seg in path) {
            current = (current as? JsonObject)?.get(seg) ?: return null
        }
        return current as? JsonObject
    }

    companion object {
        private const val RpcSettingsDescribe = "settings.describe"
        private const val RpcSettingsMutate = "settings.mutate"
        private const val RpcSettingsUpdate = "settings.update"
        private const val RpcSettingsOpenDocument = "settings.openDocument"
        private const val RpcCredentialsDescribe = "credentials.describe"
        private const val RpcCredentialsSet = "credentials.set"
        private const val RpcCredentialsUnset = "credentials.unset"
        private const val RpcLlmProviders = "llm.providers"
        private const val RpcLlmModels = "llm.models"
        private const val RpcLlmDiscoverModels = "llm.discoverModels"

        /** host/remote-event 转发的配置面失效事件(重连不重放) */
        private const val EventSettingsDocumentUpdated = "settings/document-updated"
        private const val EventCredentialsUpdated = "credentials/updated"
        private const val EventLlmAdaptersUpdated = "llm/adapters-updated"
    }
}
