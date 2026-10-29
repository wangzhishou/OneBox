package com.shifenmiao.common.manager

import com.shifenmiao.common.ai.AiEngineConnectivityTester
import com.shifenmiao.common.ai.AiEngineTestFailureKind
import com.shifenmiao.database.ai.entity.AiEngineEntity
import com.shifenmiao.database.ai.entity.AiModelEntity
import com.shifenmiao.model.ai.AiConfigSource
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiProvider
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.remote.AiEngineConfig
import com.shifenmiao.storage.RemoteConfigStorage
import com.shifenmiao.storage.TokenStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.remote.ErrorReporter
import com.t8rin.logger.makeLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIEngineCatalogManager @Inject constructor(
    private val aiEngineRepository: AIEngineRepository,
    private val aiEngineSyncManager: AIEngineSyncManager,
    private val aiEngineManager: AIEngineManager,
    private val errorReporter: ErrorReporter,
    dispatchersHolder: DispatchersHolder,
) : DispatchersHolder by dispatchersHolder {

    private val managerScope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val modelNameToTitleCache = MutableStateFlow<Map<String, String>>(emptyMap())
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing
    private val _lastRefreshError = MutableStateFlow<String?>(null)
    val lastRefreshError: StateFlow<String?> = _lastRefreshError

    init {
        managerScope.launch {
            kotlin.runCatching {
                aiEngineRepository.initDefaultEnginesIfEmpty()
                aiEngineRepository.ensureFlavorPresetEngines()
            }.onFailure {
                makeLog { "AIEngineCatalogManager: Init default engines failed: $it" }
            }
        }
        managerScope.launch {
            aiEngineRepository.getAllModels().collectLatest { entities ->
                modelNameToTitleCache.value = entities.associate { entity ->
                    entity.name.lowercase() to entity.title.ifBlank { entity.name }
                }
            }
        }
        managerScope.launch {
            observeAvailableEngines().collectLatest { engines ->
                aiEngineManager.reconcileAvailableEngines(engines)
            }
        }
    }

    fun observeAvailableEngines(
        userLevel: Int = TokenStorage.getUserVipLevel()
    ): Flow<List<AiEngine>> {
        return combine(
            aiEngineRepository.observeResolvedEnginesByUserLevel(userLevel),
            observeLocalOwnedEngineIdentityKeys(),
        ) { engines, localOwnedKeys -> engines to localOwnedKeys }
            .map { (engines, localOwnedKeys) ->
                val defaultEngines = AiEngineConfig.getDefaultEngines(
                    configuredEngines = RemoteConfigStorage.getRemoteConfig().defaultEngines,
                )
                val defaultEngineNameSet = defaultEngines.map(String::lowercase).toSet()

                engines.filter { engine ->
                    // 本地协议与本地自有(用户自建)引擎必须始终直通,不被远程 defaultEngines 白名单过滤,
                    // 否则用户添加的自定义引擎保存成功后永远进不了引擎列表。
                    engine.requestProtocol == AiRequestProtocol.LOCAL_ON_DEVICE ||
                        // Jev/Pikafish 为客户端内置专用引擎, 服务端种子可能尚未下发, 同样豁免白名单过滤。
                        engine.requestProtocol.isNonChat ||
                        engine.identityKey() in localOwnedKeys ||
                        defaultEngineNameSet.isEmpty() ||
                        defaultEngineNameSet.contains(engine.name.lowercase())
                }.ifEmpty {
                    defaultEngines.mapNotNull { engineName ->
                        AiProvider.fromValue(engineName)
                            .takeUnless { it == AiProvider.Default }
                            ?.let(AiEngine::builtInEngine)
                    }
                        .takeIf { it.isNotEmpty() }
                        ?: listOf(AiEngine.defaultEngine())
                }
            }
    }

    fun observeModelsByProvider(): Flow<Map<String, List<AiModel>>> {
        return aiEngineRepository.getAllModels().map { entities ->
            val defaultModels = AiEngineConfig.getDefaultModels(
                configuredEngines = RemoteConfigStorage.getRemoteConfig().defaultEngines,
            )
            val defaultEngineNameSet = defaultModels.map { it.engineName.lowercase() }.toSet()

            // 本地自有的引擎/模型不受远程 defaultEngines 白名单限制,否则用户自建引擎的模型永远进不了选择器
            entities.filter { entity ->
                entity.isLocalOwned() ||
                    entity.engineName.equals(AiProvider.Jev.value, ignoreCase = true) ||
                    defaultEngineNameSet.isEmpty() ||
                    defaultEngineNameSet.contains(entity.engineName.lowercase())
            }
                .map(AiModelEntity::toAiModel)
                .ifEmpty { defaultModels }
                .groupBy { it.engineName.lowercase() }
        }
    }

    fun observeLocalOwnedEngineIdentityKeys(): Flow<Set<String>> {
        return aiEngineRepository.observeAllEngines().map { entities ->
            entities.asSequence()
                .filter { it.isLocalOwned() }
                .map { AiEngineEntity.buildIdentityKey(it.name, it.requestProtocol) }
                .toSet()
        }
    }

    suspend fun getEngineByName(name: String): AiEngine? {
        return aiEngineRepository.getResolvedEngineByName(name)
    }

    suspend fun getEngineByNameAndProtocol(name: String, requestProtocol: String): AiEngine? {
        return aiEngineRepository.getResolvedEngineByNameAndProtocol(
            name = name,
            requestProtocol = AiRequestProtocol.fromValue(requestProtocol).name,
        )
    }

    /**
     * 保存已有引擎的配置改动(引擎列表页/本地模型管理页调用, 可能改的是内置预置行)。
     *
     * 编辑语义与 [saveEditedLocalEngine] 一致: 按 (name, protocol) 查不到时会收敛到
     * 同名首行原位更新(保留行 id, 允许协议漂移), 不会 insert 出同名重复行。
     * 需要显式传"原始协议/原名"做改名冲突拒绝的详情页编辑, 走 [saveEditedLocalEngine]。
     */
    fun saveEngineConfigOnly(engine: AiEngine, onComplete: (Boolean) -> Unit = {}) {
        persistEngineConfig(engine = engine, isNewEngine = false) { result ->
            onComplete(result is AddEngineResult.Success)
        }
    }

    /**
     * 保存编辑中的引擎(用户自建的与内置预置的都走这里)。
     *
     * 编辑 = 原位更新原来那一行(保留行 id), 允许改协议; 用 ([originalName], [originalProtocol])
     * 定位被编辑的原行, 绝不允许因为换了协议导致按 (新名, 新协议) 查不到就 insert 新行 ——
     * 旧行残留会让详情页/聊天按名字加载到旧协议行, 表现为「保存了又变回去」。
     * [originalName] 非空且与 [engine].name 不同时表示重命名服务标识, 同步迁移模型归属。
     * 内置预置行(source=REMOTE)只原位更新配置, source/canEdit 保持不变。
     */
    fun saveEditedLocalEngine(
        engine: AiEngine,
        originalName: String?,
        originalProtocol: String?,
        onComplete: (Boolean) -> Unit = {},
    ) {
        managerScope.launch {
            try {
                val previousName = originalName?.trim().orEmpty()
                val newName = engine.name.trim()
                if (previousName.isEmpty()) {
                    // 拿不到原行标识时退回按 (name, protocol) 查重更新的旧路径
                    persistEngineConfig(engine = engine, isNewEngine = false) { result ->
                        onComplete(result is AddEngineResult.Success)
                    }
                    return@launch
                }

                // 优先用 (旧名 + 原协议) 定位原行; originalProtocol 缺失时退回用草稿协议兜底
                val originalProtocolName = originalProtocol
                    ?.let { AiRequestProtocol.fromValue(it).name }
                    ?: engine.requestProtocol.name
                val existing = aiEngineRepository.getEngineByNameAndProtocol(
                    name = previousName,
                    requestProtocol = originalProtocolName,
                ) ?: aiEngineRepository.getEngineByNameAndProtocol(
                    name = previousName,
                    requestProtocol = engine.requestProtocol.name,
                )
                if (existing == null) {
                    makeLog {
                        "AIEngineCatalogManager: Save edited engine failed: " +
                            "row not found for '$previousName' ($originalProtocolName)"
                    }
                    reportEngineSaveFailure(
                        engine = engine,
                        reason = "row_not_found",
                        detail = "原名 '$previousName' 协议 $originalProtocolName 定位不到原行",
                    )
                    onComplete(false)
                    return@launch
                }
                // 内置/远程引擎(source=REMOTE, canEdit=false)也允许原位保存:
                // 给 DeepSeek 这类预制服务商填自己的 token 是正常用法(UI 只锁改名和删除),
                // mergeConfigToEntity 会保留原行的 source/canEdit, 不影响远程同步语义。

                // 唯一键冲突处理: 目标 (newName, 新协议) 已被另一行占用时,
                // 要么是该 bug 留下的同名重复行(名字没变、只换了协议, 正是历史脏数据的形状),
                // 删之; 要么是改名撞上了另一条真实存在的引擎, 拒绝保存, 不悄悄删别人的引擎。
                val collision = aiEngineRepository.getEngineByNameAndProtocol(
                    name = newName,
                    requestProtocol = engine.requestProtocol.name,
                )
                if (collision != null && collision.id != existing.id) {
                    if (newName == previousName) {
                        // 只删引擎行: ai_models 按 engineName(仅名字)归属, 对同名多协议行共享,
                        // 连模型一起删会让原位更新后留下的那行也失去模型。
                        aiEngineRepository.deleteEngineByNameAndProtocol(
                            name = newName,
                            requestProtocol = engine.requestProtocol.name,
                        )
                    } else {
                        onComplete(false)
                        return@launch
                    }
                }

                // 改名时同步迁移模型归属, 避免「改了名字变成新增一条、旧的还在」
                if (previousName != newName) {
                    aiEngineRepository.getModelsByEngineName(previousName).forEach { model ->
                        aiEngineRepository.updateModel(model.copy(engineName = newName))
                    }
                }

                val entity = mergeConfigToEntity(
                    engine = engine.copy(name = newName),
                    existing = existing.copy(name = newName),
                ).copy(id = existing.id, name = newName)
                aiEngineRepository.updateEngine(entity)

                // 同步模型配置(canUploadFile、canImage 等字段存储在模型表中), 与 persistEngineConfig 同一套逻辑
                persistEngineModelConfig(
                    engine = engine.copy(name = newName),
                    isLocalOwnedEngine = existing.isLocalOwned(),
                )

                // 详情页原位编辑不走 persistEngineConfig, 静默复查在这里补触发(F8)
                scheduleSilentConnectivityCheck(engine.copy(name = newName))

                onComplete(true)
            } catch (e: Exception) {
                makeLog { "AIEngineCatalogManager: Save edited engine failed: $e" }
                onComplete(false)
            }
        }
    }

    /**
     * 新增一个用户自建引擎。
     *
     * 与 [saveEngineConfigOnly] 的差别是多了两道"别假装成功"的防线:
     * 名称撞上已有引擎时拒绝而不是静默覆盖, 写入后回读确认真的落成了用户自有的行。
     * 这两条对应的都是 #29 那一类"提示添加成功、列表里却没有"的假成功。
     *
     * 回调给的是具体结果而不是 Boolean, 这样 UI 能对"名称重复"给出可操作的提示,
     * 而不是一句"请稍后重试"。
     */
    fun addLocalEngine(engine: AiEngine, onComplete: (AddEngineResult) -> Unit = {}) {
        persistEngineConfig(engine = engine, isNewEngine = true, onComplete = onComplete)
    }

    private fun persistEngineConfig(
        engine: AiEngine,
        isNewEngine: Boolean,
        onComplete: (AddEngineResult) -> Unit,
    ) {
        managerScope.launch {
            try {
                val existing = aiEngineRepository.getEngineByNameAndProtocol(
                    name = engine.name,
                    requestProtocol = engine.requestProtocol.name,
                )

                // 编辑路径的协议漂移防护: 按 (name, 新协议) 查不到、但库内已有同名行时,
                // 说明调用方改了协议(或库里留有历史重复行)。编辑语义是原位更新原行,
                // 与 saveEditedLocalEngine 一致: 收敛到同名首行(详情页按名字加载、
                // 聊天 identityKey 匹配命中的就是它), 而不是 insert 出一行同名引擎 ——
                // 那正是"保存了又变回去"bug 的根。新增路径不受影响(target 恒等于 existing)。
                val target = if (isNewEngine) {
                    existing
                } else {
                    existing ?: aiEngineRepository.getEngineByName(name = engine.name)
                }

                // 撞名保护: 新增时只要名称已被任何一行占用就拒绝 ——
                // 无论那一行是内置预设还是用户之前建的另一条自定义引擎。
                // 否则会走 update 分支: 用户既看不到新条目, 已有的引擎还会被悄悄改掉。
                if (isNewEngine && existing != null) {
                    makeLog {
                        "AIEngineCatalogManager: Reject new engine '${engine.name}', " +
                            "name already taken by source=${existing.source} canEdit=${existing.canEdit}"
                    }
                    reportEngineSaveFailure(
                        engine = engine,
                        reason = "name_collision",
                        detail = "已存在同名引擎(source=${existing.source}, canEdit=${existing.canEdit})",
                    )
                    onComplete(AddEngineResult.NameTaken)
                    return@launch
                }

                val isLocalOwnedEngine = target?.isLocalOwned() ?: true
                val entity = mergeConfigToEntity(engine = engine, existing = target)

                if (target == null) {
                    aiEngineRepository.saveEngine(entity)
                } else {
                    aiEngineRepository.updateEngine(entity)
                }

                // 回读断言: 写库成功 != 列表里能看到。新增入口必须落成"用户自有"的行,
                // 否则宁可回滚报失败, 也不再让 UI 出现"提示成功、列表没变化"的假成功。
                val persisted = aiEngineRepository.getEngineByNameAndProtocol(
                    name = engine.name,
                    requestProtocol = engine.requestProtocol.name,
                )
                if (persisted == null || (isNewEngine && !persisted.isLocalOwned())) {
                    makeLog {
                        "AIEngineCatalogManager: Engine '${engine.name}' not persisted as expected " +
                            "(existing=${target != null}, persisted=${persisted?.source}/${persisted?.canEdit})"
                    }
                    if (target == null && persisted != null) {
                        // 本次是真新增: 回滚掉这条用户永远看不到的行, 不给库里留脏数据。
                        // 注意用 target 判断: 编辑路径收敛到同名原行时也是 update,
                        // 这里不能把刚更新的行删掉。
                        aiEngineRepository.deleteEngineByNameAndProtocol(
                            name = persisted.name,
                            requestProtocol = persisted.requestProtocol,
                        )
                    }
                    reportEngineSaveFailure(
                        engine = engine,
                        reason = "not_visible_after_save",
                        detail = "回读 source=${persisted?.source} canEdit=${persisted?.canEdit}",
                    )
                    onComplete(AddEngineResult.NotVisible)
                    return@launch
                }

                // 同步更新模型配置（canUploadFile、canImage 等字段存储在模型表中）
                persistEngineModelConfig(engine = engine, isLocalOwnedEngine = isLocalOwnedEngine)

                // 保存后的静默连通性复查统一在这里触发(F8): 新增(addLocalEngine)与
                // 编辑(saveEngineConfigOnly / saveEditedLocalEngine 兜底路径)都会走到,
                // 不再只挂在详情页保存上
                scheduleSilentConnectivityCheck(engine)

                onComplete(AddEngineResult.Success)
            } catch (e: Exception) {
                makeLog { "AIEngineCatalogManager: Save engine config failed: $e" }
                reportEngineSaveFailure(
                    engine = engine,
                    reason = "exception",
                    detail = e.message.orEmpty(),
                    throwable = e,
                )
                onComplete(AddEngineResult.Failed)
            }
        }
    }

    /**
     * 同步引擎当前模型的配置（canUploadFile、canImage 等字段存储在模型表中）。
     *
     * persistEngineConfig(新增/旧编辑路径)与 saveEditedLocalEngine(详情页原位编辑)共用,
     * 保证两条保存路径落出来的模型行行为一致。
     */
    private suspend fun persistEngineModelConfig(
        engine: AiEngine,
        isLocalOwnedEngine: Boolean,
    ) {
        val model = engine.model
        val engineName = engine.name.ifBlank { model.engineName.ifBlank { model.provider.value } }
        if (model.name.isBlank()) return

        val existingModel = aiEngineRepository.getModelById(model.id)
            ?.takeIf { it.engineName.equals(engineName, ignoreCase = true) }
            ?: aiEngineRepository.getModelByNameAndEngineName(model.name, engineName)

        if (isLocalOwnedEngine || existingModel?.isLocalOwned() == true) {
            // 用户自建引擎的模型也必须是"本地自有": 落成 REMOTE 会被远程引擎白名单
            // 连带过滤, 表现为引擎能选、模型选择器里却没有它。
            val localModelEntity = AiModelEntity.fromAiModel(
                model = model.copy(
                    engineName = engineName,
                    canEdit = true,
                ),
                existingEntity = existingModel,
                source = AiConfigSource.LOCAL,
            ).copy(
                id = existingModel?.id ?: 0,
                engineName = engineName,
                source = AiConfigSource.LOCAL.name,
                canEdit = true,
                enabled = existingModel?.enabled ?: true,
                sortOrder = existingModel?.sortOrder
                    ?: (aiEngineRepository.getMaxModelSortOrderForEngine(engineName) + 1),
            )
            if (existingModel == null) {
                aiEngineRepository.saveModel(localModelEntity)
            } else {
                aiEngineRepository.updateModel(localModelEntity)
            }
        } else {
            val baseRemoteEntity = existingModel ?: AiModelEntity.fromAiModel(
                model = model.copy(
                    engineName = engineName,
                    canEdit = false,
                ),
                source = AiConfigSource.REMOTE,
            ).copy(
                id = 0,
                engineName = engineName,
                source = AiConfigSource.REMOTE.name,
                canEdit = false,
                enabled = true,
                sortOrder = aiEngineRepository.getMaxModelSortOrderForEngine(engineName) + 1,
            )

            val updatedRemoteModelEntity = baseRemoteEntity.applyPartialOverrides(
                model = model.copy(engineName = engineName),
                forceStoreValues = existingModel == null,
            )

            if (existingModel == null) {
                aiEngineRepository.saveModel(updatedRemoteModelEntity.copy(id = 0))
            } else {
                aiEngineRepository.updateModel(updatedRemoteModelEntity)
            }
        }
    }

    /**
     * 保存失败上报后台。
     *
     * 这类"看起来成功、其实没生效"的问题以前只能靠用户发 issue 才知道(#29 就是),
     * 上报后能直接从数据里看趋势, 而不是等下一个用户来踩。
     */
    private fun reportEngineSaveFailure(
        engine: AiEngine,
        reason: String,
        detail: String,
        throwable: Throwable? = null,
    ) {
        val extra = mapOf(
            "reason" to reason,
            "engine_name" to engine.name,
            "protocol" to engine.requestProtocol.name,
            "detail" to detail.take(128),
        )
        if (throwable != null) {
            errorReporter.reportError(SOURCE, throwable, extra)
        } else {
            errorReporter.reportAnomaly(SOURCE, "自定义引擎保存未生效: $reason", extra)
        }
    }

    fun updateModel(aiModel: AiModel) {
        managerScope.launch {
            kotlin.runCatching {
                aiEngineRepository.updateModel(aiModel)
            }.onFailure {
                makeLog { "AIEngineCatalogManager: Update model failed: $it" }
            }
        }
    }

    /**
     * 保存后静默连通性复查。
     *
     * 针对「走直连路由但本次未通过测试」的引擎(典型场景: 海外渠道填了 token 直接保存,
     * canChatDirectly 因 hasOwnToken 成立, 但 isDetestPassed 从未置真): 在 managerScope 里
     * 后台跑一次真实连通测试, 不阻塞保存流程、不弹窗。
     *
     * 结果处理(F2/R2):
     * - 成功: 显式清掉 isUrlError(可能之前失败留下过), 引擎恢复可用;
     * - 失败且是 MODEL_NOT_FOUND(404, 地址/路径确实错): 落库 isUrlError=true,
     *   用户可见反馈就是引擎列表页已有的红色「不可用」徽章;
     *   AUTH_INVALID(401/403) 真因在 Token 不在 URL, 不落这个位(否则详情页 URL 框
     *   无辜变红且文案指错方向);
     * - 超时/限流 429/5xx 等瞬时故障: 只记日志不落库, 不让一次网络抖动变成永久红徽章。
     *
     * 中期方案(未做): 把布尔位升级为 lastTestedAt + lastTestError, 列表徽章带时间语义。
     */
    fun scheduleSilentConnectivityCheck(engine: AiEngine) {
        if (engine.requestProtocol.isNonChat) return
        if (engine.requestUrl.isBlank()) return
        if (!engine.canChatDirectly() || engine.isDetestPassed) return
        managerScope.launch {
            val result = AiEngineConnectivityTester.test(engine)
            when {
                result.success -> setEngineUrlError(engine, hasError = false)
                // 只落「地址/路径确实错」的 404(R2): AUTH_INVALID(401/403) 真因在 Token,
                // 落 isUrlError 会让详情页 URL 框无辜变红、文案指错方向, 不落
                result.failureKind == AiEngineTestFailureKind.MODEL_NOT_FOUND -> {
                    makeLog {
                        "AIEngineCatalogManager: Silent connectivity check failed for " +
                            "'${engine.name}' (${result.failureKind}), marking isUrlError"
                    }
                    setEngineUrlError(engine, hasError = true)
                }
                else -> makeLog {
                    "AIEngineCatalogManager: Silent connectivity check transiently failed for " +
                        "'${engine.name}' (${result.failureKind}), not marking isUrlError"
                }
            }
        }
    }

    private suspend fun setEngineUrlError(engine: AiEngine, hasError: Boolean) {
        runCatching {
            val row = aiEngineRepository.getEngineByNameAndProtocol(
                name = engine.name,
                requestProtocol = engine.requestProtocol.name,
            ) ?: return@runCatching
            if (row.isUrlError != hasError) {
                aiEngineRepository.updateEngine(row.copy(isUrlError = hasError))
            }
        }.onFailure {
            makeLog { "AIEngineCatalogManager: Update engine url error flag failed: $it" }
        }
    }

    fun refreshCatalog(forceUpdate: Boolean = true) {
        if (_isRefreshing.value) return
        _isRefreshing.value = true
        _lastRefreshError.value = null
        aiEngineSyncManager.refreshEnginesFromRemote(
            forceUpdate = forceUpdate,
            onSuccess = {
                _isRefreshing.value = false
            },
            onFailure = { error ->
                _isRefreshing.value = false
                _lastRefreshError.value = error
            }
        )
    }

    fun createLocalEngineDraft(): AiEngine {
        return AiEngine(
            name = "",
            title = "",
            description = "",
            requestProtocol = AiRequestProtocol.OPENAI_COMPATIBLE,
            model = createLocalModelDraft(engineName = ""),
            apiCanSet = true
        )
    }

    fun createLocalModelDraft(engineName: String): AiModel {
        return AiModel(
            id = 0,
            name = "",
            title = "",
            provider = AiProvider.Default,
            canEdit = true,
            engineName = engineName,
        )
    }

    fun upsertLocalModel(aiModel: AiModel, onComplete: (Boolean, AiModel?) -> Unit = { _, _ -> }) {
        managerScope.launch {
            try {
                val engineName = aiModel.engineName.ifBlank { aiModel.provider.value }
                val existingById = aiModel.id.takeIf { it > 0 }?.let { modelId ->
                    aiEngineRepository.getModelById(modelId)
                }
                val existingByIdentity = aiEngineRepository.getModelByNameAndEngineName(aiModel.name, engineName)
                if (existingById != null && existingByIdentity != null && existingById.id != existingByIdentity.id) {
                    onComplete(false, null)
                    return@launch
                }
                val existing = existingById ?: existingByIdentity
                val sortOrder = existing?.sortOrder ?: (aiEngineRepository.getMaxModelSortOrderForEngine(engineName) + 1)
                val entity = AiModelEntity.fromAiModel(
                    model = aiModel.copy(
                        id = existing?.id ?: 0,
                        canEdit = true,
                        engineName = engineName,
                    ),
                    existingEntity = existing,
                    source = AiConfigSource.LOCAL,
                ).copy(
                    sortOrder = sortOrder,
                    enabled = true,
                    source = AiConfigSource.LOCAL.name,
                    canEdit = true,
                    engineName = engineName,
                )
                val savedEntity = if (existing == null) {
                    val localId = aiEngineRepository.saveModel(entity.copy(id = 0))
                    entity.copy(id = localId)
                } else {
                    aiEngineRepository.updateModel(entity)
                    entity
                }
                if (existing != null && existing.name != savedEntity.name) {
                    aiEngineRepository.updateEnginesSelectedModelNameByNameAndCurrentModelName(
                        name = engineName,
                        currentModelName = existing.name,
                        newModelName = savedEntity.name,
                    )
                }
                onComplete(true, savedEntity.toAiModel())
            } catch (e: Exception) {
                makeLog { "AIEngineCatalogManager: Upsert local model failed: $e" }
                onComplete(false, null)
            }
        }
    }

    fun deleteLocalEngine(engine: AiEngine, onComplete: (Boolean) -> Unit = {}) {
        managerScope.launch {
            try {
                val existing = aiEngineRepository.getEngineByNameAndProtocol(
                    name = engine.name,
                    requestProtocol = engine.requestProtocol.name,
                )
                if (existing == null || !existing.isLocalOwned()) {
                    onComplete(false)
                    return@launch
                }

                aiEngineRepository.deleteEngineByNameAndProtocol(
                    name = existing.name,
                    requestProtocol = existing.requestProtocol,
                )

                val remainingSameNameEngines = aiEngineRepository.getEnginesByName(existing.name)
                if (remainingSameNameEngines.isEmpty()) {
                    aiEngineRepository.deleteModelsByEngineName(existing.name)
                }

                onComplete(true)
            } catch (e: Exception) {
                makeLog { "AIEngineCatalogManager: Delete local engine failed: $e" }
                onComplete(false)
            }
        }
    }

    fun deleteLocalModel(
        model: AiModel,
        engineRequestProtocol: AiRequestProtocol,
        onComplete: (Boolean, AiEngine?) -> Unit = { _, _ -> },
    ) {
        managerScope.launch {
            try {
                val engineName = model.engineName.ifBlank { model.provider.value }
                val existing = model.id.takeIf { it > 0 }?.let { modelId ->
                    aiEngineRepository.getModelById(modelId)
                }
                    ?: aiEngineRepository.getModelByNameAndEngineName(model.name, engineName)

                if (existing == null || !existing.isLocalOwned()) {
                    onComplete(false, null)
                    return@launch
                }

                aiEngineRepository.deleteModel(existing)

                val fallbackModelName = aiEngineRepository.getFirstAvailableModelForEngine(engineName)?.name.orEmpty()
                aiEngineRepository.updateEnginesSelectedModelNameByNameAndCurrentModelName(
                    name = engineName,
                    currentModelName = existing.name,
                    newModelName = fallbackModelName,
                )

                val updatedEngine = aiEngineRepository.getResolvedEngineByNameAndProtocol(
                    name = engineName,
                    requestProtocol = engineRequestProtocol.name,
                )

                onComplete(true, updatedEngine)
            } catch (e: Exception) {
                makeLog { "AIEngineCatalogManager: Delete local model failed: $e" }
                onComplete(false, null)
            }
        }
    }

    fun clearRemoteModelOverrides(
        model: AiModel,
        engineRequestProtocol: AiRequestProtocol,
        onComplete: (Boolean, AiEngine?) -> Unit = { _, _ -> },
    ) {
        managerScope.launch {
            try {
                val engineName = model.engineName.ifBlank { model.provider.value }
                val existing = model.id.takeIf { it > 0 }?.let { modelId ->
                    aiEngineRepository.getModelById(modelId)
                }
                    ?.takeIf { it.engineName.equals(engineName, ignoreCase = true) }
                    ?: aiEngineRepository.getModelByNameAndEngineName(model.name, engineName)

                if (existing == null || existing.isLocalOwned()) {
                    onComplete(false, null)
                    return@launch
                }

                aiEngineRepository.updateModel(existing.clearPartialOverrides())
                val updatedEngine = aiEngineRepository.getResolvedEngineByNameAndProtocol(
                    name = engineName,
                    requestProtocol = engineRequestProtocol.name,
                )
                onComplete(true, updatedEngine)
            } catch (e: Exception) {
                makeLog { "AIEngineCatalogManager: Clear remote model overrides failed: $e" }
                onComplete(false, null)
            }
        }
    }

    fun getAiModelTitleByModel(model: String): String {
        return modelNameToTitleCache.value[model.lowercase()] ?: model
    }

    private fun mergeConfigToEntity(engine: AiEngine, existing: AiEngineEntity?): AiEngineEntity {
        val base = existing ?: AiEngineEntity.fromAiEngine(
            engine = engine,
            source = AiConfigSource.LOCAL,
            canEdit = true,
        )
        return base.copy(
            title = engine.title,
            description = engine.description,
            requestUrl = engine.requestUrl,
            requestPath = engine.requestPath,
            proxyUrl = engine.proxyUrl,
            proxyPath = engine.proxyPath,
            requestProtocol = engine.requestProtocol.name,
            authType = engine.authType.name,
            authorizationCode = engine.authorizationCode,
            stream = engine.stream,
            isUrlError = engine.isUrlError,
            isDetestPassed = engine.isDetestPassed,
            selectedModelName = engine.model.name,
            source = existing?.source ?: AiConfigSource.LOCAL.name,
            canEdit = existing?.canEdit ?: true,
        )
    }

    private companion object {
        const val SOURCE = "AIEngineCatalogManager.saveEngineConfigOnly"
    }
}

