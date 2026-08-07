package com.shifenmiao.common.manager

import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiProvider
import com.shifenmiao.model.ai.AiWorkingModelSlot
import com.shifenmiao.model.remote.AiEngineConfig
import com.shifenmiao.model.remote.RemoteConfig
import com.shifenmiao.storage.AiConfigVersionStorage
import com.t8rin.logger.makeLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI 引擎管理器
 *
 * 设计要点：
 * - 引擎/模型目录来自数据库（远程更新后入库）
 * - 当前默认模型与快速模型直接持久化在 MMKV
 * - 即使数据库加载失败，也能使用硬编码默认引擎继续工作
 * - 支持 RemoteConfig 控制默认值（仅在用户未手动选择时生效）
 */
@Singleton
class AIEngineManager @Inject constructor(
    private val aiEngineRepository: AIEngineRepository,
){

    private val fallbackDefaultEngine = flavorFallbackEngine(AiWorkingModelSlot.DEFAULT)
    private val fallbackFastEngine = flavorFallbackEngine(AiWorkingModelSlot.FAST)
    private val fallbackDuelEngineA = fallbackDefaultEngine
    private val fallbackDuelEngineB = fallbackFastEngine

    // 当前默认引擎和模型（MMKV + 硬编码兜底）
    private val _currentAIEngine = MutableStateFlow(
        loadInitialEngine(
            hasUserSelection = AiConfigVersionStorage.hasUserSetDefault(),
            storedEngine = AiConfigVersionStorage.getDefaultEngine(),
            fallbackEngine = fallbackDefaultEngine,
        )
    )
    val currentAIEngine: StateFlow<AiEngine> = _currentAIEngine

    private val _currentAIModel = MutableStateFlow(_currentAIEngine.value.model)
    val currentAIModel: StateFlow<AiModel> = _currentAIModel

    // 快速任务引擎和模型（MMKV + 硬编码兜底）
    private val _fastAIEngine = MutableStateFlow(
        loadInitialEngine(
            hasUserSelection = AiConfigVersionStorage.hasUserSetFast(),
            storedEngine = AiConfigVersionStorage.getFastEngine(),
            fallbackEngine = fallbackFastEngine,
        )
    )
    val fastAIEngine: StateFlow<AiEngine> = _fastAIEngine

    private val _fastAIModel = MutableStateFlow(_fastAIEngine.value.model)
    val fastAIModel: StateFlow<AiModel> = _fastAIModel

    // AI 互聊双模型（独立 MMKV 键）
    private val _duelEngineA = MutableStateFlow(
        loadInitialEngine(
            hasUserSelection = AiConfigVersionStorage.hasUserSetDuelA(),
            storedEngine = AiConfigVersionStorage.getDuelEngineA(),
            fallbackEngine = fallbackDuelEngineA,
        )
    )
    val duelEngineA: StateFlow<AiEngine> = _duelEngineA

    private val _duelEngineB = MutableStateFlow(
        loadInitialEngine(
            hasUserSelection = AiConfigVersionStorage.hasUserSetDuelB(),
            storedEngine = AiConfigVersionStorage.getDuelEngineB(),
            fallbackEngine = fallbackDuelEngineB,
        )
    )
    val duelEngineB: StateFlow<AiEngine> = _duelEngineB

    init {
        persistSelections()
    }

    fun switchEngine(engine: AiEngine) {
        setDefaultEngine(engine)
    }

    fun switchModel(engine: AiEngine, model: AiModel, onComplete: () -> Unit = {}) {
        setDefaultEngine(engine.copy(model = model))
        onComplete()
    }

    fun getCurrentAiEngine(): AiEngine {
        return _currentAIEngine.value
    }

    fun getFastAiEngine(): AiEngine {
        return _fastAIEngine.value
    }

    fun getEngineForFastTask(): AiEngine {
        return _fastAIEngine.value
    }

    fun getDuelEngineA(): AiEngine {
        return _duelEngineA.value
    }

    fun getDuelEngineB(): AiEngine {
        return _duelEngineB.value
    }

    fun setDefaultEngine(engine: AiEngine) {
        if (!isValidProviderName(engine.name)) {
            makeLog { "AIEngineManager: Ignore invalid default engine name=${engine.name}" }
            return
        }
        applyDefaultEngine(engine = engine, markUserSelection = true)
    }

    fun setFastEngine(engine: AiEngine) {
        if (!isValidProviderName(engine.name)) {
            makeLog { "AIEngineManager: Ignore invalid fast engine name=${engine.name}" }
            return
        }
        applyFastEngine(engine = engine, markUserSelection = true)
    }

    fun switchFastEngine(engine: AiEngine) {
        setFastEngine(engine)
    }

    fun switchFastModel(engine: AiEngine, model: AiModel, onComplete: () -> Unit = {}) {
        setFastEngine(engine.copy(model = model))
        onComplete()
    }

    fun setDuelEngineA(engine: AiEngine) {
        if (!isValidProviderName(engine.name)) {
            makeLog { "AIEngineManager: Ignore invalid duel engineA name=${engine.name}" }
            return
        }
        applyDuelEngineA(engine = engine, markUserSelection = true)
    }

    fun setDuelEngineB(engine: AiEngine) {
        if (!isValidProviderName(engine.name)) {
            makeLog { "AIEngineManager: Ignore invalid duel engineB name=${engine.name}" }
            return
        }
        applyDuelEngineB(engine = engine, markUserSelection = true)
    }

    fun switchDuelModelA(model: AiModel, onComplete: () -> Unit = {}) {
        setDuelEngineA(_duelEngineA.value.copy(model = model))
        onComplete()
    }

    fun switchDuelModelB(model: AiModel, onComplete: () -> Unit = {}) {
        setDuelEngineB(_duelEngineB.value.copy(model = model))
        onComplete()
    }

    /**
     * 应用 RemoteConfig 中的默认值到工作引擎槽位。
     * 仅在用户从未手动设置过对应槽位时生效，避免覆盖用户选择。
     */
    suspend fun applyRemoteConfigDefaults(remoteConfig: RemoteConfig) {
        // DEFAULT 槽位
        if (!AiConfigVersionStorage.hasUserSetDefault()) {
            val slotConfig = AiEngineConfig.getDefaultSlotConfig(
                slot = AiWorkingModelSlot.DEFAULT,
                configuredEngines = remoteConfig.defaultEngines,
            )
            val engine = resolveEngineFromRemoteConfig(
                engineName = remoteConfig.defaultWorkingEngine
                    .takeIf { it == null || isConfiguredEngineName(it, remoteConfig) }
                    ?: slotConfig.engineName,
                modelName = remoteConfig.defaultWorkingModel
                    .takeIf { remoteConfig.defaultWorkingEngine?.let { engineName -> isConfiguredEngineName(engineName, remoteConfig) } != false }
                    ?: slotConfig.modelName,
                fallback = fallbackForSlot(slotConfig.engineName, fallbackDefaultEngine),
            )
            if (_currentAIEngine.value != engine) {
                applyDefaultEngine(engine = engine, markUserSelection = false)
                makeLog { "AIEngineManager: Applied remote default engine=${engine.name} model=${engine.model.name}" }
            }
        }

        // FAST 槽位
        if (!AiConfigVersionStorage.hasUserSetFast()) {
            val slotConfig = AiEngineConfig.getDefaultSlotConfig(
                slot = AiWorkingModelSlot.FAST,
                configuredEngines = remoteConfig.defaultEngines,
            )
            val engine = resolveEngineFromRemoteConfig(
                engineName = remoteConfig.defaultFastEngine
                    .takeIf { it == null || isConfiguredEngineName(it, remoteConfig) }
                    ?: slotConfig.engineName,
                modelName = remoteConfig.defaultFastModel
                    .takeIf { remoteConfig.defaultFastEngine?.let { engineName -> isConfiguredEngineName(engineName, remoteConfig) } != false }
                    ?: slotConfig.modelName,
                fallback = fallbackForSlot(slotConfig.engineName, fallbackFastEngine),
            )
            if (_fastAIEngine.value != engine) {
                applyFastEngine(engine = engine, markUserSelection = false)
                makeLog { "AIEngineManager: Applied remote fast engine=${engine.name} model=${engine.model.name}" }
            }
        }

        // DUEL_A 槽位
        if (!AiConfigVersionStorage.hasUserSetDuelA()) {
            val slotConfig = AiEngineConfig.getDefaultSlotConfig(
                slot = AiWorkingModelSlot.DUEL_A,
                configuredEngines = remoteConfig.defaultEngines,
            )
            val engine = resolveEngineFromRemoteConfig(
                engineName = remoteConfig.defaultDuelAEngine
                    .takeIf { it == null || isConfiguredEngineName(it, remoteConfig) }
                    ?: slotConfig.engineName,
                modelName = remoteConfig.defaultDuelAModel
                    .takeIf { remoteConfig.defaultDuelAEngine?.let { engineName -> isConfiguredEngineName(engineName, remoteConfig) } != false }
                    ?: slotConfig.modelName,
                fallback = fallbackForSlot(slotConfig.engineName, fallbackDuelEngineA),
            )
            if (_duelEngineA.value != engine) {
                applyDuelEngineA(engine = engine, markUserSelection = false)
                makeLog { "AIEngineManager: Applied remote duelA engine=${engine.name} model=${engine.model.name}" }
            }
        }

        // DUEL_B 槽位
        if (!AiConfigVersionStorage.hasUserSetDuelB()) {
            val slotConfig = AiEngineConfig.getDefaultSlotConfig(
                slot = AiWorkingModelSlot.DUEL_B,
                configuredEngines = remoteConfig.defaultEngines,
            )
            val engine = resolveEngineFromRemoteConfig(
                engineName = remoteConfig.defaultDuelBEngine
                    .takeIf { it == null || isConfiguredEngineName(it, remoteConfig) }
                    ?: slotConfig.engineName,
                modelName = remoteConfig.defaultDuelBModel
                    .takeIf { remoteConfig.defaultDuelBEngine?.let { engineName -> isConfiguredEngineName(engineName, remoteConfig) } != false }
                    ?: slotConfig.modelName,
                fallback = fallbackForSlot(slotConfig.engineName, fallbackDuelEngineB),
            )
            if (_duelEngineB.value != engine) {
                applyDuelEngineB(engine = engine, markUserSelection = false)
                makeLog { "AIEngineManager: Applied remote duelB engine=${engine.name} model=${engine.model.name}" }
            }
        }
    }

    private suspend fun resolveEngineFromRemoteConfig(
        engineName: String?,
        modelName: String?,
        fallback: AiEngine,
    ): AiEngine {
        if (engineName.isNullOrBlank()) return fallback

        return try {
            val engine = aiEngineRepository.getResolvedEngineByName(engineName)
            if (engine == null) {
                makeLog { "AIEngineManager: Remote config engine '$engineName' not found in DB, fallback to ${fallback.name}" }
                return fallback
            }

            // 如果 remote config 指定了模型名，尝试精确匹配
            if (!modelName.isNullOrBlank()) {
                val exactModel = aiEngineRepository.getModelByNameAndEngineName(
                    name = modelName,
                    engineName = engineName,
                )
                if (exactModel != null) {
                    return engine.copy(model = exactModel.toAiModel())
                } else {
                    makeLog { "AIEngineManager: Remote config model '$modelName' for engine '$engineName' not found, use engine default" }
                }
            }

            engine
        } catch (e: Exception) {
            makeLog { "AIEngineManager: resolveEngineFromRemoteConfig failed: $e" }
            fallback
        }
    }

    fun reconcileAvailableEngines(availableEngines: List<AiEngine>) {
        val resolvedDefault = resolveSelection(
            current = _currentAIEngine.value,
            availableEngines = availableEngines,
            preferredFallback = fallbackDefaultEngine,
            hasUserSelection = AiConfigVersionStorage.hasUserSetDefault(),
        )
        if (_currentAIEngine.value != resolvedDefault) {
            applyDefaultEngine(engine = resolvedDefault, markUserSelection = false)
        }

        val resolvedFast = resolveSelection(
            current = _fastAIEngine.value,
            availableEngines = availableEngines,
            preferredFallback = fallbackFastEngine,
            hasUserSelection = AiConfigVersionStorage.hasUserSetFast(),
        )
        if (_fastAIEngine.value != resolvedFast) {
            applyFastEngine(engine = resolvedFast, markUserSelection = false)
        }

        val resolvedDuelA = resolveSelection(
            current = _duelEngineA.value,
            availableEngines = availableEngines,
            preferredFallback = fallbackDuelEngineA,
            hasUserSelection = AiConfigVersionStorage.hasUserSetDuelA(),
        )
        if (_duelEngineA.value != resolvedDuelA) {
            applyDuelEngineA(engine = resolvedDuelA, markUserSelection = false)
        }

        val resolvedDuelB = resolveSelection(
            current = _duelEngineB.value,
            availableEngines = availableEngines,
            preferredFallback = fallbackDuelEngineB,
            hasUserSelection = AiConfigVersionStorage.hasUserSetDuelB(),
        )
        if (_duelEngineB.value != resolvedDuelB) {
            applyDuelEngineB(engine = resolvedDuelB, markUserSelection = false)
        }
    }

    private fun applyDefaultEngine(engine: AiEngine, markUserSelection: Boolean) {
        _currentAIEngine.value = engine
        _currentAIModel.value = engine.model
        AiConfigVersionStorage.saveDefaultEngine(engine)
        if (markUserSelection) {
            AiConfigVersionStorage.setHasUserSetDefault(true)
        }
    }

    private fun applyFastEngine(engine: AiEngine, markUserSelection: Boolean) {
        _fastAIEngine.value = engine
        _fastAIModel.value = engine.model
        AiConfigVersionStorage.saveFastEngine(engine)
        if (markUserSelection) {
            AiConfigVersionStorage.setHasUserSetFast(true)
        }
    }

    private fun applyDuelEngineA(engine: AiEngine, markUserSelection: Boolean) {
        _duelEngineA.value = engine
        AiConfigVersionStorage.saveDuelEngineA(engine)
        if (markUserSelection) {
            AiConfigVersionStorage.setHasUserSetDuelA(true)
        }
    }

    private fun applyDuelEngineB(engine: AiEngine, markUserSelection: Boolean) {
        _duelEngineB.value = engine
        AiConfigVersionStorage.saveDuelEngineB(engine)
        if (markUserSelection) {
            AiConfigVersionStorage.setHasUserSetDuelB(true)
        }
    }

    private fun persistSelections() {
        AiConfigVersionStorage.saveDefaultEngine(_currentAIEngine.value)
        AiConfigVersionStorage.saveFastEngine(_fastAIEngine.value)
        AiConfigVersionStorage.saveDuelEngineA(_duelEngineA.value)
        AiConfigVersionStorage.saveDuelEngineB(_duelEngineB.value)
    }

    /**
     * flavor 感知的槽位兜底引擎: Google 渠道下硬编码默认值(Mimo 等国内引擎)
     * 不在白名单内, getDefaultSlotConfig 会回落到白名单第一个引擎(如 openai),
     * 保证冷启动默认值与渠道可用目录一致。
     */
    private fun flavorFallbackEngine(slot: AiWorkingModelSlot): AiEngine {
        val engineName = AiEngineConfig.getDefaultSlotConfig(slot = slot).engineName
        return AiProvider.fromValue(engineName).takeUnless { it == AiProvider.Default }
            ?.let(AiEngine::builtInEngine) ?: AiEngine.defaultEngine()
    }

    private fun loadInitialEngine(
        hasUserSelection: Boolean,
        storedEngine: AiEngine?,
        fallbackEngine: AiEngine,
    ): AiEngine {
        if (!hasUserSelection) return fallbackEngine
        return storedEngine?.takeIf { engine -> isValidProviderName(engine.name) } ?: fallbackEngine
    }

    private fun resolveSelection(
        current: AiEngine,
        availableEngines: List<AiEngine>,
        preferredFallback: AiEngine,
        hasUserSelection: Boolean,
    ): AiEngine {
        if (availableEngines.isEmpty()) {
            return if (hasUserSelection) current else preferredFallback
        }

        val fromCatalog = availableEngines.firstOrNull { it.identityKey() == current.identityKey() }
            ?: availableEngines.firstOrNull { it.identityKey() == preferredFallback.identityKey() }
            ?: return if (hasUserSelection) current else preferredFallback

        // 用户已手动选择模型：保留内存中的 model（用户偏好），仅用 DB 同步引擎元数据
        // （URL、proxy、auth 等）。如果未选择，则直接采用 DB 中的默认 model。
        return if (hasUserSelection && current.model.name.isNotBlank()) {
            fromCatalog.copy(model = current.model)
        } else {
            fromCatalog
        }
    }


    private fun isValidProviderName(name: String): Boolean {
        return name.isNotBlank()
    }

    private fun isConfiguredEngineName(engineName: String, remoteConfig: RemoteConfig): Boolean {
        val configuredEngineNames = AiEngineConfig.getDefaultEngines(remoteConfig.defaultEngines)
            .map(String::lowercase)
            .toSet()
        return configuredEngineNames.isEmpty() || configuredEngineNames.contains(engineName.lowercase())
    }

    private fun fallbackForSlot(engineName: String, fallback: AiEngine): AiEngine {
        val provider = AiProvider.fromValue(engineName)
        return provider.takeUnless { it == AiProvider.Default }
            ?.let(AiEngine::builtInEngine)
            ?: fallback
    }
}
