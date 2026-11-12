/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.dsh.session

import com.wanbaohe.dsh.connection.ConnectionPhase
import com.wanbaohe.dsh.connection.DshApiClient
import com.wanbaohe.dsh.connection.DshConnectionController
import com.wanbaohe.dsh.wire.DshEndpoints
import com.wanbaohe.dsh.wire.DshJson
import com.wanbaohe.dsh.wire.RemoteEventFrame
import com.wanbaohe.dsh.wire.RpcBusinessException
import com.wanbaohe.dsh.wire.RpcErrorCodes
import com.wanbaohe.dsh.wire.model.ConfigurableProviderView
import com.wanbaohe.dsh.wire.model.CredentialView
import com.wanbaohe.dsh.wire.model.DiscoveredModelView
import com.wanbaohe.dsh.wire.model.LlmDiscoverModelsValue
import com.wanbaohe.dsh.wire.model.ModelCatalog
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
import kotlinx.serialization.Serializable
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
 * 0.1.5-rc.2 差异(旧的 0.1.0/0.1.1 协议面已删,不再兼容):
 * - 端点全部改成斜杠名并集中到 [DshEndpoints];回值统一走 [DshApiClient.callRemote]
 *   (拆掉 RemoteResult 信封),旧点号端点与 `api.call` 一律不再使用
 * - 提供方目录:`llm/providers` → [DshEndpoints.LLM_LIST_PROVIDERS](`llm/listProviders`),
 *   回值是**裸数组** `[{id,name}]`(旧 `{providers:[…]}` 已删);旧目录携带的
 *   settingsNs/settingsPath/declared 不再下发,这里按 settings 快照做尽力桥接
 *   (规则见 [configLocationOf]),定位不到 = 该条目无配置段/无凭据引用/恒非自定义
 * - 模型目录:`llm.models` 已删,改走 [DshEndpoints.SESSION_MODEL_CATALOG]
 *   (`session/modelCatalog`)→ [ModelCatalog](default + routableProviders + groups + failures)
 * - `llm/discoverModels` 的 args 改为 `{settingsNs, request:{provider?,baseURL?,api?,apiKey?}}`,
 *   回值是裸数组 `LlmDiscoveredModel[]`(旧 `{models:[…]}` 已删);这里就地包成
 *   [LlmDiscoverModelsValue],保持 UI 的消费形状不变
 * - `settings/mutate(ns, ops, expectedRevision)`:args 形状不变,ops 元素仍是
 *   SettingsPathOpView 的 `{op:'set'|'unset', path:[…], value?}`(0.1.5 实测形状)
 * - `credentials/describe` 的 args **必填** `refs`(回值是裸映射 `ref → 状态`,
 *   单批上限 64):关心集合由调用方显式传入,不再由端点内部推断
 * - `settings/describe`、`settings/openSettingsDocument`、`session/modelCatalog`
 *   的 args 是空对象
 * - 错误码改斜杠词表([RpcErrorCodes]):CAS 冲突判定用 [RpcErrorCodes.SettingsConflict],
 *   [RpcErrorCodes.SettingsRejected] 等码原样透出,不再折叠成 internal
 * - 配置面失效事件经 `$events`([RemoteEventFrame.Emit])到达,名字为
 *   settings/document-updated、credentials/reference-updated(旧 credentials/updated 已更名)、
 *   llm/adapters-updated;旧的 `/api/events.host` + `HostFrame` 通道已删除
 *
 * 原有语义要点(未变):
 * - settings/credentials 全族与 llm/discoverModels 是特权方法,仅 loopback
 *   (或经网关鉴权的远程形态)可用;UI 按 PrivilegeScope 门控,本 store 不重复判断
 * - settings/describe 一次读全部 namespace(快照 + schema + revision)
 * - settings/mutate 走路径 op(set/unset),expectedRevision 乐观锁(CAS);
 *   冲突 settings/conflict → 自动重读并抛 [SettingsConflictException](UI 提示重试)
 * - credentials/describe 只报 configured/source/writable,不含值
 * - 提供方目录 = llm/listProviders(只列已注册适配器的路由)+ settings 值 + credentials 徽标
 *   合并:绿点=密钥已配置,红点=引用缺失,无引用=无点
 *
 * 生命周期与连接实例绑定:由组件层创建并 [dispose]。
 */

/** 凭据徽标状态(绿=已配置,红=引用缺失,无=无引用/无法判定) */
enum class CredentialStatus { Configured, Missing, None }

/** llm/listProviders 目录条目(0.1.5 的裸数组元素,只有 id/name) */
@Serializable
data class LlmProviderInfoView(
    val id: String,
    val name: String = ""
)

/** 提供方目录条目:llm/listProviders 视图 + settings 配置值 + credentials 徽标 */
data class ProviderEntry(
    val view: LlmProviderInfoView,
    /** 该提供方在 settings 里的配置值(settingsPath 下钻后的 map) */
    val config: JsonObject?,
    /** 关联凭据引用(配置值的 apiKeyEnv;无引用 = null) */
    val credentialRef: String?,
    val credentialStatus: CredentialStatus,
    /** 配置段所在 settings namespace(定位不到 = 空串,UI 退化为只读展示) */
    val namespace: String,
    /** 配置段在 namespace value 里的路径(定位不到 = 空列表) */
    val settingsPath: List<String>,
    /** 目录构建时刻的 namespace revision(写入走最新快照的 revision) */
    val revision: Double
) {
    val providerId: String get() = view.id
    val displayName: String get() = view.name

    /** 适配器当前是否服务该 provider:目录只列已注册适配器的路由 → 恒为 true */
    val routable: Boolean get() = true

    /** 自定义提供方标签:0.1.5 的目录不再下发 declared → 无法判定,恒为 false */
    val custom: Boolean get() = false

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

/** CAS 冲突:mutate/update 被 settings/conflict 拒绝后已自动重读,UI 提示用户重试 */
class SettingsConflictException(
    val namespace: String,
    val expectedRevision: Double?,
    val latestRevision: Double?
) : Exception("settings/conflict($namespace)")

/** provider 配置段的位置:settings namespace + 该段在 namespace value 里的路径 */
private data class ProviderConfigLocation(val namespace: String, val path: List<String>)

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
        scope.launch { connection.eventFrames.collect(::onRemoteEvent) }
    }

    fun dispose() {
        disposed = true
        scope.cancel()
    }

    /** 某 namespace 的当前视图(读时取最新) */
    fun namespace(ns: String): SettingsMutateValue? = _snapshot.value.namespaces[ns]

    /**
     * 全量重拉:settings/describe → llm/listProviders → credentials/describe。
     *
     * [credentialRefs] = 调用方显式关心的凭据引用集合(0.1.5 的 credentials/describe 必填
     * `refs`,不再由端点内部推断);实际请求集合 = 它 ∪ 目录里能定位到的 apiKeyEnv,
     * 保证提供方徽标仍能对上。默认空列表 = 只靠目录推导。
     */
    suspend fun refresh(credentialRefs: List<String> = emptyList()) {
        if (disposed) return
        val desc = DshJson.decodeFromJsonElement<SettingsDescribeValue>(
            api.callRemote(DshEndpoints.SETTINGS_DESCRIBE)
        )
        val namespaces = desc.namespaces.associateBy { it.ns }
        val providers = DshJson.decodeFromJsonElement<List<LlmProviderInfoView>>(
            api.callRemote(DshEndpoints.LLM_LIST_PROVIDERS)
        )
        // 0.1.5 新增 listConfigurableProviders:直接给 settingsNs/settingsPath,
        // 拿不到(旧主机/端点缺失)时才退回按 settings 快照猜位置的启发式。
        val exact = fetchConfigurableLocations()
        val locations = providers.associate { provider ->
            provider.id to (exact[provider.id] ?: configLocationOf(provider.id, namespaces))
        }
        val refs = (credentialRefs + providers.mapNotNull { provider ->
            locations[provider.id]?.let { location ->
                credentialRefOf(namespaces[location.namespace], location.path)
            }
        }).filter { it.isNotEmpty() }.distinct()
        val creds = describeCredentials(refs)
        if (disposed) return
        loadedOnce = true
        _snapshot.value = SettingsSnapshot(
            providers = providers.map { buildProviderEntry(it, namespaces, locations[it.id], creds) },
            namespaces = namespaces,
            credentials = creds,
            writable = desc.writable,
            hasDocument = desc.hasDocument,
            loading = false
        )
    }

    /**
     * credentials/describe:批量取凭据状态(只报 configured/source/writable,不含值)。
     *
     * 0.1.5 的 args **必填** `refs`(旧版无参调用会被判 missing "refs"),单批上限 64,
     * 故这里分片合并;回值是**裸映射** `ref → 状态`(旧 `{credentials:{…}}` 形状已改),
     * 元素按 [CredentialView] 宽松解码。空集合直接短路(不发无意义请求)。
     */
    suspend fun describeCredentials(refs: List<String> = emptyList()): Map<String, CredentialView> {
        if (refs.isEmpty()) return emptyMap()
        val described = LinkedHashMap<String, CredentialView>()
        for (chunk in refs.distinct().chunked(MaxDescribeRefs)) {
            val value = api.callRemote(
                DshEndpoints.CREDENTIALS_DESCRIBE,
                buildJsonObject {
                    putJsonArray("refs") { chunk.forEach { add(JsonPrimitive(it)) } }
                }
            )
            described += parseCredentialMap(value)
        }
        return described
    }

    /**
     * settings/mutate:路径 op(set/unset)+ expectedRevision 乐观锁(CAS)。
     * args 形状 `{ns, ops, expectedRevision}`;成功 → 本地快照用响应覆盖
     * (新 revision 供下次 CAS),并写后重读;settings/conflict → 自动重读后抛
     * [SettingsConflictException]。
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
        return runMutation(namespace, payload, DshEndpoints.SETTINGS_MUTATE, expectedRevision)
    }

    /** settings/mutate 单字段 set 便捷形态(op 形状 = SettingsPathOpView 的 set 分支) */
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

    /** settings/mutate 单字段 unset 便捷形态(op 形状 = SettingsPathOpView 的 unset 分支) */
    suspend fun unsetField(namespace: String, path: List<String>): SettingsMutateValue = mutate(
        namespace,
        listOf(buildJsonObject {
            put("op", "unset")
            putJsonArray("path") { path.forEach { add(JsonPrimitive(it)) } }
        }),
        expectedRevision = namespace(namespace)?.revision
    )

    /** settings/update:section patch 合并(同 CAS 语义;args `{ns, patch, expectedRevision}`) */
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
        return runMutation(namespace, payload, DshEndpoints.SETTINGS_UPDATE, expectedRevision)
    }

    /** mutate/update 公共回环:成功落地 + 写后重读;冲突自动重读后抛 */
    private suspend fun runMutation(
        namespace: String,
        payload: JsonObject,
        endpoint: String,
        expectedRevision: Double?
    ): SettingsMutateValue {
        if (disposed) throw CancellationException("SettingsStore disposed")
        try {
            val value = DshJson.decodeFromJsonElement<SettingsMutateValue>(
                api.callRemote(endpoint, payload)
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

    /** credentials/set:只写(响应为空体);徽标对齐交给 credentials/reference-updated,再保险重拉 */
    suspend fun setCredential(ref: String, value: String) {
        api.callRemote(DshEndpoints.CREDENTIALS_SET, buildJsonObject {
            put("ref", ref)
            put("value", value)
        })
        refreshQuietly()
    }

    /** credentials/unset(响应为空体) */
    suspend fun unsetCredential(ref: String) {
        api.callRemote(DshEndpoints.CREDENTIALS_UNSET, buildJsonObject { put("ref", ref) })
        refreshQuietly()
    }

    /**
     * session/modelCatalog:全量模型目录(0.1.5 起 `llm.models` 并入此端点;args 是空对象)。
     * 回值 [ModelCatalog] = 默认选择 + 可路由 provider + 分组目录 + 失败项。
     */
    suspend fun modelCatalog(): ModelCatalog =
        DshJson.decodeFromJsonElement<ModelCatalog>(
            api.callRemote(DshEndpoints.SESSION_MODEL_CATALOG)
        )

    /**
     * llm/discoverModels:带未保存的草稿端点/密钥拉可用模型(web 同款语义)。
     *
     * 0.1.5 的 args 是 `{settingsNs, request:{provider?,baseURL?,api?,apiKey?}}`
     * (旧版把 provider/baseURL/api/apiKey 平铺在 args 顶层,现在会被判 arguments-invalid);
     * 回值是裸数组 `LlmDiscoveredModel[]`(旧 `{models:[…]}` 已删),这里就地包成
     * [LlmDiscoverModelsValue] 保持 UI 消费形状。
     */
    suspend fun discoverModels(
        settingsNs: String,
        provider: String? = null,
        baseURL: String? = null,
        apiKind: String? = null,
        apiKey: String? = null
    ): LlmDiscoverModelsValue {
        val request = buildJsonObject {
            provider?.let { put("provider", it) }
            baseURL?.let { put("baseURL", it) }
            apiKind?.let { put("api", it) }
            apiKey?.let { put("apiKey", it) }
        }
        val value = api.callRemote(
            DshEndpoints.LLM_DISCOVER_MODELS,
            buildJsonObject {
                put("settingsNs", settingsNs)
                put("request", request)
            }
        )
        return LlmDiscoverModelsValue(
            models = DshJson.decodeFromJsonElement<List<DiscoveredModelView>>(value)
        )
    }

    /** settings/openSettingsDocument:外壳「打开配置文件」(仅 loopback 可达;args 是空对象) */
    suspend fun openDocument(): SettingsOpenDocumentValue =
        DshJson.decodeFromJsonElement<SettingsOpenDocumentValue>(
            api.callRemote(DshEndpoints.SETTINGS_OPEN_DOCUMENT)
        )

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

    /** 失效事件三件套:收到即重拉(`$events` 转发不重放,消费端自行重拉) */
    private fun onRemoteEvent(frame: RemoteEventFrame) {
        if (disposed || frame !is RemoteEventFrame.Emit) return
        when (frame.event) {
            EventSettingsDocumentUpdated,
            EventCredentialsReferenceUpdated,
            EventLlmAdaptersUpdated -> refreshQuietly()
        }
    }

    // ───────────────────────────── 配置定位与目录合并 ─────────────────────────────

    /**
     * 定位 provider 的配置段(0.1.5 的 llm/listProviders 只给 id/name,旧目录的
     * settingsNs/settingsPath 已随 `llm/providers` 删除,这里按 settings 快照桥接):
     * 1. 命名空间名 == provider id(整段即配置)
     * 2. 任一命名空间的 `providers.<id>` 子树(pi-ai 系目录约定)
     * 3. 任一命名空间的 `<id>` 子树
     * 4. `llm-<x>` 命名空间的根段,且 x 与 provider id 同族(单提供方适配器,
     *    如 llm-deepseek ↔ deepseek-official)
     * 定位不到返回 null = 该条目没有可编辑配置段(只读展示,无凭据引用)。
     */
    /**
     * 拉 `llm/listConfigurableProviders` 并映射成精确配置位置;端点不可用(旧主机、
     * 未装载 pi-ai 目录)时返回空表,由 [configLocationOf] 的启发式兜底。
     */
    private suspend fun fetchConfigurableLocations(): Map<String, ProviderConfigLocation> =
        try {
            val list = DshJson.decodeFromJsonElement<List<ConfigurableProviderView>>(
                api.callRemote(DshEndpoints.LLM_LIST_CONFIGURABLE_PROVIDERS)
            )
            list.associate { it.provider to ProviderConfigLocation(it.settingsNs, it.settingsPath) }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            emptyMap()
        }

    private fun configLocationOf(
        providerId: String,
        namespaces: Map<String, SettingsMutateValue>
    ): ProviderConfigLocation? {
        namespaces[providerId]?.let { return ProviderConfigLocation(providerId, emptyList()) }
        for ((ns, view) in namespaces) {
            if (configAt(view.value, listOf("providers", providerId)) != null) {
                return ProviderConfigLocation(ns, listOf("providers", providerId))
            }
        }
        for ((ns, view) in namespaces) {
            if (configAt(view.value, listOf(providerId)) != null) {
                return ProviderConfigLocation(ns, listOf(providerId))
            }
        }
        for ((ns, view) in namespaces) {
            if (!ns.startsWith(LlmNamespacePrefix)) continue
            val family = ns.removePrefix(LlmNamespacePrefix)
            val related = providerId == family ||
                providerId.startsWith("$family-") ||
                family.startsWith("$providerId-")
            if (related && configAt(view.value, emptyList()) != null) {
                return ProviderConfigLocation(ns, emptyList())
            }
        }
        return null
    }

    private fun buildProviderEntry(
        view: LlmProviderInfoView,
        namespaces: Map<String, SettingsMutateValue>,
        location: ProviderConfigLocation?,
        credentials: Map<String, CredentialView>
    ): ProviderEntry {
        val ns = location?.let { namespaces[it.namespace] }
        val config = location?.let { configAt(ns?.value, it.path) }
        val ref = credentialRefOf(ns, location?.path ?: emptyList())
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
            namespace = location?.namespace.orEmpty(),
            settingsPath = location?.path.orEmpty(),
            revision = ns?.revision ?: 0.0
        )
    }

    /** 凭据引用 = 配置段的 apiKeyEnv(无段/无引用返回 null) */
    private fun credentialRefOf(ns: SettingsMutateValue?, path: List<String>): String? {
        val config = configAt(ns?.value, path) ?: return null
        return (config["apiKeyEnv"] as? JsonPrimitive)
            ?.contentOrNull?.takeIf { it.isNotEmpty() }
    }

    /** 裸映射 ref → 状态:元素按 [CredentialView] 宽松解码,坏元素丢弃(不拖垮整批) */
    private fun parseCredentialMap(value: JsonElement): Map<String, CredentialView> {
        val obj = value as? JsonObject ?: return emptyMap()
        return obj.mapNotNull { (ref, element) ->
            runCatching {
                ref to DshJson.decodeFromJsonElement(CredentialView.serializer(), element)
            }.getOrNull()
        }.toMap()
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
        /** llm 配置命名空间前缀(llm-deepseek / llm-pi-ai …) */
        private const val LlmNamespacePrefix = "llm-"

        /** credentials/describe 单批上限(服务端 MAX_DESCRIBE_REFS = 64) */
        private const val MaxDescribeRefs = 64

        /** `$events` 转发的配置面失效事件(重连不重放) */
        private const val EventSettingsDocumentUpdated = "settings/document-updated"
        private const val EventCredentialsReferenceUpdated = "credentials/reference-updated"
        private const val EventLlmAdaptersUpdated = "llm/adapters-updated"
    }
}
