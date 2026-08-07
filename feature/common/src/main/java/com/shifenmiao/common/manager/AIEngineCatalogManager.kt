package com.shifenmiao.common.manager

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
import com.t8rin.logger.makeLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIEngineCatalogManager @Inject constructor(
    private val aiEngineRepository: AIEngineRepository,
    private val aiEngineSyncManager: AIEngineSyncManager,
    private val aiEngineManager: AIEngineManager,
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
        return aiEngineRepository.observeResolvedEnginesByUserLevel(userLevel)
            .map { engines ->
                val defaultEngines = AiEngineConfig.getDefaultEngines(
                    configuredEngines = RemoteConfigStorage.getRemoteConfig().defaultEngines,
                )
                val defaultEngineNameSet = defaultEngines.map(String::lowercase).toSet()

                engines.filter { engine ->
                    // 本地协议引擎必须始终直通，不被远程 defaultEngines 白名单过滤，
                    // 否则用户导入的本地模型永远进不了引擎列表。
                    engine.requestProtocol == AiRequestProtocol.LOCAL_ON_DEVICE ||
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

            entities.map(AiModelEntity::toAiModel)
                .filter { model ->
                    defaultEngineNameSet.isEmpty() || defaultEngineNameSet.contains(model.engineName.lowercase())
                }
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

    fun saveEngineConfigOnly(engine: AiEngine, onComplete: (Boolean) -> Unit = {}) {
        managerScope.launch {
            try {
                val existing = aiEngineRepository.getEngineByNameAndProtocol(
                    name = engine.name,
                    requestProtocol = engine.requestProtocol.name,
                )
                val entity = mergeConfigToEntity(engine = engine, existing = existing)

                if (existing == null) {
                    aiEngineRepository.saveEngine(entity)
                } else {
                    aiEngineRepository.updateEngine(entity)
                }

                // 同步更新模型配置（canUploadFile、canImage 等字段存储在模型表中）
                val model = engine.model
                val engineName = engine.name.ifBlank { model.engineName.ifBlank { model.provider.value } }
                if (model.name.isNotBlank()) {
                    val existingModel = aiEngineRepository.getModelById(model.id)
                        ?.takeIf { it.engineName.equals(engineName, ignoreCase = true) }
                        ?: aiEngineRepository.getModelByNameAndEngineName(model.name, engineName)

                    if (existingModel?.isLocalOwned() == true) {
                        val updatedLocalModelEntity = AiModelEntity.fromAiModel(
                            model = model.copy(
                                engineName = engineName,
                                canEdit = true,
                            ),
                            existingEntity = existingModel,
                            source = AiConfigSource.LOCAL,
                        ).copy(
                            id = existingModel.id,
                            engineName = engineName,
                            source = AiConfigSource.LOCAL.name,
                            canEdit = true,
                            enabled = existingModel.enabled,
                            sortOrder = existingModel.sortOrder,
                        )
                        aiEngineRepository.updateModel(updatedLocalModelEntity)
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

                onComplete(true)
            } catch (e: Exception) {
                makeLog { "AIEngineCatalogManager: Save engine config failed: $e" }
                onComplete(false)
            }
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
}

