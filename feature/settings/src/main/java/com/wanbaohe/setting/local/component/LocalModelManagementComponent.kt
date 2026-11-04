package com.wanbaohe.setting.local.component

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.localmodel.LocalModelDownloadState
import com.shifenmiao.common.localmodel.LocalModelImportResult
import com.shifenmiao.common.localmodel.LocalModelRepository
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiProvider
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.AiWorkingModelSlot
import com.shifenmiao.model.ai.AuthType
import com.shifenmiao.model.remote.AiEngineConfig
import com.shifenmiao.model.remote.LocalTranslationModel
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.settings.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlin.coroutines.resume
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

data class LocalModelFile(
    val fileName: String,
    val sizeBytes: Long,
) {
    val modelName: String get() = fileName.substringBeforeLast('.')
}

data class ModelDownloadUiState(
    val isDownloading: Boolean = false,
    val progress: Float? = null,
    val isFailed: Boolean = false,
)

data class RecommendedLocalModel(
    @StringRes val displayNameRes: Int,
    @StringRes val descriptionRes: Int,
    val fileName: String,
    val url: String,
    val sizeLabel: String,
)

class LocalModelManagementComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @ApplicationContext private val context: Context,
    private val aiEngineCatalogManager: AIEngineCatalogManager,
    private val aiEngineManager: AIEngineManager,
    private val localModelRepository: LocalModelRepository,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    // Gemma 优先使用 GPU；Qwen3-0.6B 在部分 Adreno 设备会回退 CPU。
    val recommendedModels: List<RecommendedLocalModel> = listOf(
        RecommendedLocalModel(
            displayNameRes = R.string.local_model_gemma3_name,
            descriptionRes = R.string.local_model_gemma3_desc,
            fileName = "gemma3-1b-it-int4.litertlm",
            url = "https://images.oneboxable.com/models/gemma3-1b-it-int4.litertlm",
            sizeLabel = "~585MB",
        ),
        RecommendedLocalModel(
            displayNameRes = R.string.local_model_qwen3_name,
            descriptionRes = R.string.local_model_qwen3_desc,
            fileName = "Qwen3-0.6B.litertlm",
            url = "https://images.oneboxable.com/models/Qwen3-0.6B.litertlm",
            sizeLabel = "~615MB",
        ),
        RecommendedLocalModel(
            displayNameRes = R.string.local_model_hy_mt2_name,
            descriptionRes = R.string.local_model_hy_mt2_desc,
            fileName = LocalTranslationModel.FILE_NAME,
            url = LocalTranslationModel.DEFAULT_DOWNLOAD_URL,
            sizeLabel = "~1.83GB",
        ),
    )

    private val _downloadedModels = MutableStateFlow<List<LocalModelFile>>(emptyList())
    val downloadedModels: StateFlow<List<LocalModelFile>> = _downloadedModels.asStateFlow()

    private val _downloadStates = MutableStateFlow<Map<String, ModelDownloadUiState>>(emptyMap())
    val downloadStates: StateFlow<Map<String, ModelDownloadUiState>> = _downloadStates.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    val currentAIEngine = aiEngineManager.currentAIEngine

    sealed interface ImportResult {
        data object Success : ImportResult
        data class AlreadyExists(val fileName: String) : ImportResult
        data object Failed : ImportResult
    }

    init {
        refreshDownloadedModels()
        componentScope.launch {
            localModelRepository.downloads.collect { states ->
                val hasCompletedModel = recommendedModels.any { model ->
                    _downloadStates.value[model.fileName]?.isDownloading == true &&
                        states[model.fileName] is LocalModelDownloadState.Success
                }
                _downloadStates.value = recommendedModels.mapNotNull { model ->
                    val state = when (val download = states[model.fileName]) {
                        is LocalModelDownloadState.Running -> ModelDownloadUiState(
                            isDownloading = true,
                            progress = download.progress,
                        )

                        is LocalModelDownloadState.Failed -> ModelDownloadUiState(isFailed = true)
                        else -> null
                    }
                    state?.let { model.fileName to it }
                }.toMap()
                if (hasCompletedModel) updateDownloadedModels()
            }
        }
    }

    fun refreshDownloadedModels() {
        componentScope.launch(ioDispatcher) { updateDownloadedModels() }
    }

    private suspend fun updateDownloadedModels() {
        _downloadedModels.value = localModelRepository.listInstalledModels().map {
            LocalModelFile(fileName = it.fileName, sizeBytes = it.sizeBytes)
        }
    }

    fun startDownload(model: RecommendedLocalModel) {
        localModelRepository.startDownload(
            fileName = model.fileName,
            defaultUrl = model.url,
            title = context.getString(model.displayNameRes),
        )
    }

    fun cancelDownload(fileName: String) {
        localModelRepository.cancelDownload(fileName)
    }

    fun importModel(uri: Uri, overwrite: Boolean, onComplete: (ImportResult) -> Unit) {
        if (_isImporting.value) return
        componentScope.launch(ioDispatcher) {
            _isImporting.value = true
            val result = try {
                when (val imported = localModelRepository.importModel(uri, overwrite)) {
                    LocalModelImportResult.Success -> {
                        updateDownloadedModels()
                        ImportResult.Success
                    }

                    is LocalModelImportResult.AlreadyExists ->
                        ImportResult.AlreadyExists(imported.fileName)

                    LocalModelImportResult.Failed -> ImportResult.Failed
                }
            } finally {
                _isImporting.value = false
            }
            withContext(uiDispatcher) { onComplete(result) }
        }
    }

    /** onComplete(success, revertedChatModel)。 */
    fun deleteModel(model: LocalModelFile, onComplete: (Boolean, Boolean) -> Unit) {
        componentScope.launch(ioDispatcher) {
            if (!localModelRepository.deleteModel(model.fileName)) {
                withContext(uiDispatcher) { onComplete(false, false) }
                return@launch
            }
            removeLocalModelRecord(model)
            val current = aiEngineManager.getCurrentAiEngine()
            val reverted = current.requestProtocol == AiRequestProtocol.LOCAL_ON_DEVICE &&
                current.model.name.equals(model.modelName, ignoreCase = true)
            if (reverted) {
                val fallbackName = AiEngineConfig.getDefaultSlotConfig(AiWorkingModelSlot.DEFAULT).engineName
                val fallback = aiEngineCatalogManager.getEngineByName(fallbackName) ?: AiEngine.defaultEngine()
                aiEngineManager.switchEngine(fallback)
            }
            updateDownloadedModels()
            withContext(uiDispatcher) { onComplete(true, reverted) }
        }
    }

    fun setAsChatModel(model: LocalModelFile, onComplete: (Boolean) -> Unit) {
        componentScope.launch {
            val engine = ensureLocalEngine()
            val modelRecord = if (engine != null) upsertModelRecord(model) else null
            if (engine == null || modelRecord == null) {
                onComplete(false)
                return@launch
            }
            aiEngineManager.switchModel(engine, modelRecord)
            onComplete(true)
        }
    }

    private suspend fun ensureLocalEngine(): AiEngine? {
        aiEngineCatalogManager.getEngineByNameAndProtocol(
            name = LOCAL_ENGINE_NAME,
            requestProtocol = AiRequestProtocol.LOCAL_ON_DEVICE.name,
        )?.let { return it }

        val saved = suspendCancellableCoroutine<Boolean> { continuation ->
            aiEngineCatalogManager.saveEngineConfigOnly(
                aiEngineCatalogManager.createLocalEngineDraft().copy(
                    name = LOCAL_ENGINE_NAME,
                    title = context.getString(R.string.local_models_engine_title),
                    requestProtocol = AiRequestProtocol.LOCAL_ON_DEVICE,
                    authType = AuthType.NONE,
                    stream = true,
                ),
                onComplete = { success -> continuation.resume(success) },
            )
        }
        if (!saved) return null
        return aiEngineCatalogManager.getEngineByNameAndProtocol(
            name = LOCAL_ENGINE_NAME,
            requestProtocol = AiRequestProtocol.LOCAL_ON_DEVICE.name,
        )
    }

    private suspend fun upsertModelRecord(model: LocalModelFile): AiModel? =
        suspendCancellableCoroutine { continuation ->
            aiEngineCatalogManager.upsertLocalModel(
                aiEngineCatalogManager.createLocalModelDraft(LOCAL_ENGINE_NAME).copy(
                    name = model.modelName,
                    title = model.fileName,
                    provider = AiProvider.Local,
                    supportToolCalls = false,
                    free = true,
                    contextWindowTokens = 4096,
                ),
                onComplete = { success, saved ->
                    continuation.resume(saved.takeIf { success })
                },
            )
        }

    private suspend fun removeLocalModelRecord(model: LocalModelFile) {
        suspendCancellableCoroutine { continuation ->
            aiEngineCatalogManager.deleteLocalModel(
                model = aiEngineCatalogManager.createLocalModelDraft(LOCAL_ENGINE_NAME).copy(
                    name = model.modelName,
                    title = model.fileName,
                ),
                engineRequestProtocol = AiRequestProtocol.LOCAL_ON_DEVICE,
                onComplete = { _, _ -> continuation.resume(Unit) },
            )
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
        ): LocalModelManagementComponent
    }

    private companion object {
        const val LOCAL_ENGINE_NAME = "on-device"
    }
}
