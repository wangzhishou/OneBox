package com.shifenmiao.common.manager

import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.ai.entity.AiEngineEntity
import com.shifenmiao.database.ai.entity.AiModelEntity
import com.shifenmiao.model.ai.AiConfigSource
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiProvider
import com.shifenmiao.model.channel.FlavorType
import com.shifenmiao.model.remote.AiEngineConfig
import com.shifenmiao.storage.AiConfigVersionStorage
import com.shifenmiao.storage.RemoteConfigStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * AI 引擎和模型的数据访问层
 *
 * 设计要点：
 * - 数据库是唯一真相源（Single Source of Truth）
 * - "当前引擎"通过 ai_engines.is_current 标记（全局唯一）
 * - "各引擎的当前模型"通过 ai_engines.selected_model_name 记忆（每引擎独立）
 */
class AIEngineRepository @Inject constructor(private val appDatabase: AppDatabase) {

    // ==================== Engine 查询 ====================
    suspend fun getAllEnginesList(): List<AiEngineEntity> {
        return appDatabase.aiEngineDao().getAllEnginesList()
    }

    suspend fun getEngineByName(name: String): AiEngineEntity? {
        return appDatabase.aiEngineDao().getEngineByName(name)
    }

    suspend fun getEnginesByName(name: String): List<AiEngineEntity> {
        return appDatabase.aiEngineDao().getEnginesByName(name)
    }

    suspend fun getEngineById(id: Long): AiEngineEntity? {
        return appDatabase.aiEngineDao().getEngineById(id)
    }

    suspend fun getEngineByNameAndProtocol(name: String, requestProtocol: String): AiEngineEntity? {
        return appDatabase.aiEngineDao().getEngineByNameAndProtocol(name, requestProtocol)
    }

    // ==================== Engine + Model 解析 ====================

    suspend fun getResolvedEnginesByUserLevel(userLevel: Int): List<AiEngine> {
        val engines = appDatabase.aiEngineDao().getEnginesListByUserLevel(userLevel)
        val models = appDatabase.aiModelDao().getAllModelsList()
        return resolveEngines(engines = engines, models = models)
    }

    fun observeResolvedEnginesByUserLevel(userLevel: Int): Flow<List<AiEngine>> {
        return combine(
            appDatabase.aiEngineDao().getEnginesByUserLevel(userLevel),
            appDatabase.aiModelDao().getAllModels(),
        ) { engines, models ->
            resolveEngines(engines = engines, models = models)
        }
    }

    fun observeAllEngines(): Flow<List<AiEngineEntity>> {
        return appDatabase.aiEngineDao().getAllEngines()
    }

    suspend fun getResolvedEngineByName(name: String): AiEngine? {
        val engine = appDatabase.aiEngineDao().getEngineByName(name) ?: return null
        val models = appDatabase.aiModelDao().getModelsByEngineName(engine.name)
        return resolveEngine(engine = engine, modelsByEngineName = mapOf(engine.name.lowercase() to models))
    }

    suspend fun getResolvedEngineByNameAndProtocol(name: String, requestProtocol: String): AiEngine? {
        val engine = appDatabase.aiEngineDao().getEngineByNameAndProtocol(name, requestProtocol) ?: return null
        val models = appDatabase.aiModelDao().getModelsByEngineName(engine.name)
        return resolveEngine(engine = engine, modelsByEngineName = mapOf(engine.name.lowercase() to models))
    }

    // ==================== 引擎 CRUD ====================

    suspend fun saveEngine(engine: AiEngineEntity) {
        appDatabase.aiEngineDao().insertEngine(engine)
    }

    suspend fun saveEngine(engine: AiEngine) {
        val existing = getEngineByNameAndProtocol(engine.name, engine.requestProtocol.name)
        val entity = AiEngineEntity.fromAiEngine(engine, existingEntity = existing)
        appDatabase.aiEngineDao().insertEngine(entity)
    }

    suspend fun saveEngines(engines: List<AiEngineEntity>) {
        appDatabase.aiEngineDao().insertEngines(engines)
    }

    suspend fun saveEnginesIgnore(engines: List<AiEngineEntity>) {
        appDatabase.aiEngineDao().insertEnginesIgnore(engines)
    }

    suspend fun updateEngine(engine: AiEngineEntity) {
        appDatabase.aiEngineDao().updateEngine(engine)
    }

    suspend fun updateEnginesSelectedModelNameByNameAndCurrentModelName(name: String, currentModelName: String, newModelName: String) {
        appDatabase.aiEngineDao().updateEnginesSelectedModelNameByNameAndCurrentModelName(
            name = name,
            currentModelName = currentModelName,
            newModelName = newModelName,
        )
    }

    suspend fun deleteEngineByNameAndProtocol(name: String, requestProtocol: String) {
        appDatabase.aiEngineDao().deleteEngineByNameAndProtocol(name, requestProtocol)
    }

    suspend fun getEngineCount(): Int {
        return appDatabase.aiEngineDao().getEngineCount()
    }

    /**
     * 初始化默认引擎和模型（仅在数据库为空时插入）
     */
    suspend fun initDefaultEnginesIfEmpty() {
        if (getEngineCount() != 0) return

        val defaultEngineNames = AiEngineConfig.getDefaultEngines(
            configuredEngines = RemoteConfigStorage.getRemoteConfig().defaultEngines,
        ).distinct()
        val defaultModelsByEngineName = AiEngineConfig.getDefaultModels(
            configuredEngines = RemoteConfigStorage.getRemoteConfig().defaultEngines,
        ).associateBy { it.engineName.lowercase() }

        defaultEngineNames.forEachIndexed { sortOrder, engineName ->
            val provider = AiProvider.fromValue(engineName)
            if (provider == AiProvider.Default) return@forEachIndexed

            val defaultModel = defaultModelsByEngineName[engineName.lowercase()]
                ?: AiModel.getDefaultModelForProvider(provider).copy(engineName = engineName)
            saveModel(
                AiModelEntity.fromAiModel(
                    model = defaultModel,
                    source = AiConfigSource.REMOTE,
                ).copy(
                    id = 0,
                    engineName = engineName,
                    sortOrder = 0,
                    enabled = true,
                    source = AiConfigSource.REMOTE.name,
                )
            )

            val engine = AiEngine.builtInEngine(provider)
                .withGoogleFlavorRoutePolicy()
                .copy(
                    model = defaultModel.copy(
                        engineName = engineName,
                    )
                )
            saveEngine(
                AiEngineEntity.fromAiEngine(
                    engine = engine,
                    source = AiConfigSource.REMOTE,
                ).copy(
                    id = 0,
                    selectedModelName = defaultModel.name,
                    sortOrder = sortOrder,
                    enabled = true,
                    canEdit = false,
                    source = AiConfigSource.REMOTE.name,
                )
            )
        }
    }

    /**
     * 按 flavor 预制引擎增量补插（版本化，可随版本升级持续下发新预制引擎）。
     *
     * 与 [initDefaultEnginesIfEmpty] 的区别：不要求空表，只补本地缺失的预制引擎，
     * 已存在的行（含用户修改过 token/URL 的）一律跳过，永不覆盖。
     * 新增预制引擎时把 [AiEngineConfig.FLAVOR_PRESET_VERSION] +1 即可触达老安装。
     */
    suspend fun ensureFlavorPresetEngines() {
        val currentVersion = AiEngineConfig.FLAVOR_PRESET_VERSION
        val appliedVersion = AiConfigVersionStorage.getFlavorPresetVersion()
        if (appliedVersion >= currentVersion) return

        val flavorType = FlavorType.fromName()

        // v2 迁移: Google 渠道清理老安装预制引擎上误带的 Go 网关代理
        // (服务端种子即为空代理, 本地种子在首次同步前会从 builtInEngine 继承国内网关代理)
        // 注意: MiMo/DeepSeek 自 v3/v4 起是 Google 渠道保留代理的例外, 不在清理名单内。
        if (appliedVersion < AiEngineConfig.PRESET_VERSION_CLEAR_GOOGLE_PROXY &&
            flavorType == FlavorType.GOOGLE
        ) {
            appDatabase.aiEngineDao().clearProxyRoutesByNames(
                AiEngineConfig.googleEnabledEngines - AiEngineConfig.googleProxyEngines.toSet()
            )
        }

        // v4 迁移: Google 渠道 MiMo/DeepSeek 恢复 Go 网关代理(走自家网关按积分计费)。
        // 仅处理 REMOTE 预制行: token 为空或仍是内置注入值时清空、强制走代理;
        // 用户自配 token 的行保留 token(google 渠道"有 token 即可直连"优先级更高, 行为不变)。
        if (appliedVersion < AiEngineConfig.PRESET_VERSION_GOOGLE_PROXY_ENGINES &&
            flavorType == FlavorType.GOOGLE
        ) {
            AiEngineConfig.googleProxyEngines.forEach { engineName ->
                val builtIn = AiEngine.builtInEngine(AiProvider.fromValue(engineName))
                getEnginesByName(engineName)
                    .filter { it.source == AiConfigSource.REMOTE.name }
                    .forEach { row ->
                        val tokenIsBuiltIn = row.authorizationCode.isBlank() ||
                            row.authorizationCode == builtIn.authorizationCode
                        updateEngine(
                            row.copy(
                                proxyUrl = builtIn.proxyUrl,
                                proxyPath = builtIn.proxyPath,
                                authorizationCode = if (tokenIsBuiltIn) "" else row.authorizationCode,
                            )
                        )
                    }
            }
        }

        AiEngineConfig.getFlavorFallbackEngines(flavorType).distinct()
            .forEachIndexed { sortOrder, engineName ->
                val provider = AiProvider.fromValue(engineName)
                if (provider == AiProvider.Default) return@forEachIndexed

                val engine = AiEngine.builtInEngine(provider).withGoogleFlavorRoutePolicy()
                if (getEngineByNameAndProtocol(engineName, engine.requestProtocol.name) != null) {
                    return@forEachIndexed
                }

                val defaultModel = AiModel.getDefaultModelForProvider(provider).copy(engineName = engineName)
                if (getModelByNameAndEngineName(defaultModel.name, engineName) == null) {
                    saveModel(
                        AiModelEntity.fromAiModel(
                            model = defaultModel,
                            source = AiConfigSource.REMOTE,
                        ).copy(
                            id = 0,
                            engineName = engineName,
                            sortOrder = 0,
                            enabled = true,
                            source = AiConfigSource.REMOTE.name,
                        )
                    )
                }

                saveEngine(
                    AiEngineEntity.fromAiEngine(
                        engine = engine.copy(model = defaultModel),
                        source = AiConfigSource.REMOTE,
                    ).copy(
                        id = 0,
                        selectedModelName = defaultModel.name,
                        sortOrder = sortOrder,
                        enabled = true,
                        canEdit = false,
                        source = AiConfigSource.REMOTE.name,
                    )
                )
            }

        AiConfigVersionStorage.saveFlavorPresetVersion(currentVersion)
    }

    /**
     * Google 渠道预制引擎的路由策略：
     * - [AiEngineConfig.googleProxyEngines]（MiMo/DeepSeek）为例外：保留 Go 网关代理
     *   （走自家网关按积分计费），并清空内置 token 强制走代理
     *   （否则 google 渠道"有 token 即可直连"会绕过积分门槛）；
     * - 其余引擎不走 Go 网关代理（与服务端种子一致），入库前清空代理路由，用户自带 token 直连。
     */
    private fun AiEngine.withGoogleFlavorRoutePolicy(): AiEngine {
        if (FlavorType.fromName() != FlavorType.GOOGLE) return this
        return if (AiEngineConfig.googleProxyEngines.any { name.equals(it, ignoreCase = true) }) {
            copy(authorizationCode = "")
        } else {
            copy(proxyUrl = "", proxyPath = "")
        }
    }
    // ==================== Model 查询 ====================

    fun getAllModels(): Flow<List<AiModelEntity>> {
        return appDatabase.aiModelDao().getAllModels()
    }

    suspend fun getAllModelsList(): List<AiModelEntity> {
        return appDatabase.aiModelDao().getAllModelsList()
    }

    suspend fun getModelById(modelId: Int): AiModelEntity? {
        return appDatabase.aiModelDao().getModelById(modelId)
    }

    suspend fun getMaxModelSortOrderForEngine(engineName: String): Int {
        return appDatabase.aiModelDao().getMaxSortOrderForEngine(engineName) ?: 0
    }

    suspend fun getModelByNameAndEngineName(name: String, engineName: String): AiModelEntity? {
        return appDatabase.aiModelDao().getModelByNameAndEngineName(name, engineName)
    }

    suspend fun getModelsByEngineName(engineName: String): List<AiModelEntity> {
        return appDatabase.aiModelDao().getModelsByEngineName(engineName)
    }

    suspend fun getFirstAvailableModelForEngine(engineName: String): AiModelEntity? {
        return appDatabase.aiModelDao().getFirstAvailableModelForEngine(engineName)
    }

    // ==================== Model CRUD ====================
    suspend fun saveModel(model: AiModelEntity): Int {
        return appDatabase.aiModelDao().insertModel(model).toInt()
    }

    suspend fun updateModel(model: AiModelEntity) {
        appDatabase.aiModelDao().updateModel(model)
    }

    suspend fun updateModel(aiModel: AiModel) {
        val engineName = aiModel.engineName.ifBlank { aiModel.provider.value }
        val existing = appDatabase.aiModelDao().getModelById(aiModel.id)
            ?: appDatabase.aiModelDao().getModelByNameAndEngineName(aiModel.name, engineName)
        appDatabase.aiModelDao().updateModel(AiModelEntity.fromAiModel(aiModel, existing))
    }

    suspend fun deleteModel(model: AiModelEntity) {
        appDatabase.aiModelDao().deleteModel(model)
    }

    suspend fun deleteModelsByEngineName(engineName: String) {
        appDatabase.aiModelDao().deleteModelsByEngineName(engineName)
    }

    suspend fun upsertRemoteModels(models: List<AiModelEntity>) {
        models.forEach { model ->
            appDatabase.aiModelDao().upsertModelByIdentity(model)
        }
    }

    private fun resolveEngines(
        engines: List<AiEngineEntity>,
        models: List<AiModelEntity>,
    ): List<AiEngine> {
        val modelsByEngineName = models.groupBy { it.engineName.lowercase() }
        return engines.map { engine ->
            resolveEngine(engine = engine, modelsByEngineName = modelsByEngineName)
        }
    }

    private fun resolveEngine(
        engine: AiEngineEntity,
        modelsByEngineName: Map<String, List<AiModelEntity>>,
    ): AiEngine {
        val engineModels = modelsByEngineName[engine.name.lowercase()].orEmpty()
        val selectedModel = engineModels
            .firstOrNull { it.name == engine.selectedModelName }
            ?: engineModels.firstOrNull { it.name == AiEngine.builtInEngine(AiProvider.fromValue(engine.name)).model.name }
            ?: engineModels.firstOrNull()
        return engine.toAiEngine(selectedModel?.toAiModel())
    }
}