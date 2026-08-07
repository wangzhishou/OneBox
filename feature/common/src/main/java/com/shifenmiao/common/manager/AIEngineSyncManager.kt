package com.shifenmiao.common.manager

import com.shifenmiao.base.utils.RateLimiter
import com.shifenmiao.database.ai.entity.AiEngineEntity
import com.shifenmiao.database.ai.entity.AiModelEntity
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiProvider
import com.shifenmiao.model.ai.config.AiConfigData
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.network.api.ApiService
import com.shifenmiao.storage.AiConfigVersionStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.logger.makeLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIEngineSyncManager @Inject constructor(
    private val aiEngineRepository: AIEngineRepository,
    private val apiService: ApiService,
    dispatchersHolder: DispatchersHolder,
) : DispatchersHolder by dispatchersHolder {

    private val managerScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    fun refreshEnginesFromRemote(
        forceUpdate: Boolean = false,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        managerScope.launch {
            val sevenDaysMillis = TimeUnit.DAYS.toMillis(7)
            if (!forceUpdate && !AiConfigVersionStorage.shouldUpdate(sevenDaysMillis)) {
                makeLog { "AIEngineSyncManager: Refresh skipped, last update within 7 days" }
                return@launch
            }

            if (!forceUpdate && !RateLimiter.shouldProceed(
                    "refreshEnginesFromRemote",
                    TimeUnit.MINUTES.toMillis(10)
                )
            ) {
                makeLog { "AIEngineSyncManager: Refresh rate limited" }
                return@launch
            }

            try {
                val currentVersion = AiConfigVersionStorage.getConfigVersion()
                val response = apiService.fetchAiConfig(currentVersion)
                if (response.isSuccessful) {
                    val configResponse = response.body()
                    val configData = configResponse?.data
                    if (configResponse != null && configResponse.code == 0 && configData != null) {
                        if (configData.engines.isNotEmpty() || configData.models.isNotEmpty()) {
                            performReplaceUpdate(configData)
                            AiConfigVersionStorage.saveConfigVersion(configData.version)
                            AiConfigVersionStorage.saveLastUpdateTime()
                            makeLog { "AIEngineSyncManager: Config updated to version ${configData.version}" }
                        }
                        onSuccess()
                    } else {
                        val message = configResponse?.message ?: "Unknown error"
                        makeLog { "AIEngineSyncManager: Refresh failed: $message" }
                        onFailure(message)
                    }
                } else {
                    val message = "HTTP ${response.code()}"
                    makeLog { "AIEngineSyncManager: Refresh failed: $message" }
                    onFailure(message)
                }
            } catch (e: Exception) {
                makeLog { "AIEngineSyncManager: Refresh exception: $e" }
                onFailure(e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun performReplaceUpdate(configData: AiConfigData) {
        val existingEngineList = aiEngineRepository.getAllEnginesList()
        val existingEnginesByIdentity = existingEngineList.associateBy {
            AiEngineEntity.buildIdentityKey(it.name, it.requestProtocol)
        }
        val existingModelList = aiEngineRepository.getAllModelsList()
        val existingModelsByIdentity = existingModelList.associateBy {
            AiModelEntity.buildIdentityKey(it.name, it.engineName)
        }
        val availableModelNamesByEngine = existingModelList
            .groupBy { it.engineName.lowercase() }
            .mapValues { (_, models) -> models.mapTo(linkedSetOf()) { it.name } }
            .toMutableMap()

        makeLog { "AIEngineSyncManager: Received ${configData.engines.size} engines, ${configData.models.size} models from server" }

        if (configData.models.isNotEmpty()) {
            val modelEntities = configData.models
                .asSequence()
                .filter { it.enabled != false }
                .mapNotNull { config ->
                    val modelName = config.name?.trim().orEmpty()
                    val engineName = config.provider?.trim().orEmpty()
                    val existingByIdentity = existingModelsByIdentity[
                        AiModelEntity.buildIdentityKey(modelName, engineName)
                    ]
                    val existing = existingByIdentity

                    when {
                        modelName.isBlank() -> {
                            makeLog { "AIEngineSyncManager: Skip model with blank name: $config" }
                            null
                        }

                        engineName.isBlank() -> {
                            makeLog { "AIEngineSyncManager: Skip model with blank engineName/provider: $config" }
                            null
                        }

                        existingByIdentity?.isLocalOwned() == true -> {
                            makeLog {
                                "AIEngineSyncManager: Skip remote model '$modelName' for engine '$engineName' because local-owned config exists"
                            }
                            null
                        }

                        else -> AiModelEntity.fromModelConfig(config, existing)
                    }
                }
                .toList()
            aiEngineRepository.upsertRemoteModels(modelEntities)
            modelEntities.forEach { model ->
                val engineName = model.engineName.lowercase()
                val modelNames = availableModelNamesByEngine.getOrPut(engineName) { linkedSetOf() }
                modelNames += model.name
            }
            makeLog { "AIEngineSyncManager: Updated ${modelEntities.size} models" }
        }

        if (configData.engines.isNotEmpty()) {
            val engineEntities = configData.engines
                .asSequence()
                .filter { it.enabled != false }
                .mapNotNull { config ->
                    val engineName = config.name?.trim().orEmpty()
                    val requestProtocol = AiRequestProtocol.fromValue(config.requestProtocol).name
                    if (engineName.isBlank()) {
                        makeLog { "AIEngineSyncManager: Skip engine with blank name: $config" }
                        null
                    } else {
                        val existingByIdentity = existingEnginesByIdentity[
                            AiEngineEntity.buildIdentityKey(engineName, requestProtocol)
                        ]

                        when {
                            existingByIdentity?.isLocalOwned() == true -> {
                                makeLog {
                                    "AIEngineSyncManager: Skip remote engine '$engineName' protocol=$requestProtocol because local-owned config exists"
                                }
                                null
                            }

                            else -> AiEngineEntity.fromEngineConfig(config, existingByIdentity).copy(
                                selectedModelName = resolveSyncedModelName(
                                    engineName = engineName,
                                    existingSelectedModelName = existingByIdentity?.selectedModelName,
                                    availableModelNamesByEngine = availableModelNamesByEngine,
                                )
                            )
                        }
                    }
                }
                .toList()
            aiEngineRepository.saveEngines(engineEntities)
            makeLog { "AIEngineSyncManager: Updated ${engineEntities.size} engines" }
        }
    }

    private fun resolveSyncedModelName(
        engineName: String,
        existingSelectedModelName: String?,
        availableModelNamesByEngine: Map<String, Set<String>>,
    ): String {
        val availableModelNames = availableModelNamesByEngine[engineName.lowercase()].orEmpty()
        if (existingSelectedModelName != null && existingSelectedModelName in availableModelNames) {
            return existingSelectedModelName
        }

        val defaultModelName = AiProvider.fromValue(engineName)
            .takeUnless { it == AiProvider.Default }
            ?.let(AiModel::getDefaultModelForProvider)
            ?.name

        return defaultModelName
            ?.takeIf { it in availableModelNames }
            ?: availableModelNames.firstOrNull().orEmpty()
    }
}

