package com.wanbaohe.setting.ai.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.AuthType
import com.shifenmiao.model.ai.ModelDraft
import com.shifenmiao.model.ai.SubmitModelsRequest
import com.shifenmiao.model.ai.openai.OpenAIModelItem
import com.shifenmiao.network.api.ApiService
import com.shifenmiao.network.api.OpenAICompatibleService
import com.shifenmiao.network.api.OpenAIWithApiKeyService
import com.wanbaohe.cloud.storage.data.CloudStorageRepository
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class AIEngineSettingsDetailComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted("engineName") private val engineName: String,
    @Assisted("requestProtocol") private val requestProtocol: String,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val aiEngineCatalogManager: AIEngineCatalogManager,
    private val openAICompatibleService: OpenAICompatibleService,
    private val openAIWithApiKeyService: OpenAIWithApiKeyService,
    private val apiService: ApiService,
    private val cloudStorageRepository: CloudStorageRepository,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    val cloudConnections: List<CloudStorageConnection>
        get() = cloudStorageRepository.getConnections()

    private var originalEngine: AiEngine? = null

    private val _draftEngine = MutableStateFlow(originalEngine)
    val draftEngine: StateFlow<AiEngine?> = _draftEngine.asStateFlow()
    private val _editingModelDraft = MutableStateFlow<AiModel?>(null)
    val editingModelDraft: StateFlow<AiModel?> = _editingModelDraft.asStateFlow()

    private val _editingModelOriginal = MutableStateFlow<AiModel?>(null)
    val editingModelOriginal: StateFlow<AiModel?> = _editingModelOriginal.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    val allEngines: StateFlow<List<AiEngine>> = aiEngineCatalogManager.observeAvailableEngines()
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val localOwnedEngineKeys: StateFlow<Set<String>> = aiEngineCatalogManager.observeLocalOwnedEngineIdentityKeys()
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val modelsByProvider = aiEngineCatalogManager.observeModelsByProvider()
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val _remoteModels = MutableStateFlow<List<OpenAIModelItem>>(emptyList())
    val remoteModels: StateFlow<List<OpenAIModelItem>> = _remoteModels.asStateFlow()

    private val _isLoadingRemoteModels = MutableStateFlow(false)
    val isLoadingRemoteModels: StateFlow<Boolean> = _isLoadingRemoteModels.asStateFlow()

    private val _remoteModelsError = MutableStateFlow<String?>(null)
    val remoteModelsError: StateFlow<String?> = _remoteModelsError.asStateFlow()

    init {
        componentScope.launch {
            allEngines.collect { engines ->
                val latestEngine = engines.firstOrNull {
                    it.name.equals(engineName, ignoreCase = true) &&
                        it.requestProtocol == AiRequestProtocol.fromValue(requestProtocol)
                }
                if (latestEngine != null) {
                    val currentDraft = _draftEngine.value
                    if (currentDraft == null || !hasDraftChanged()) {
                        originalEngine = latestEngine
                        _draftEngine.value = latestEngine
                    }
                }
            }
        }
    }

    fun hasDraftChanged(): Boolean {
        return _draftEngine.value != null && _draftEngine.value != originalEngine
    }

    fun hasEditingModelChanged(): Boolean {
        val draft = _editingModelDraft.value ?: return false
        val original = _editingModelOriginal.value ?: return true
        return draft != original
    }

    fun updateRequestUrl(value: String) {
        _draftEngine.value = _draftEngine.value?.copy(requestUrl = value, isUrlError = false)
    }

    fun updateUrlValidation(isError: Boolean) {
        _draftEngine.value = _draftEngine.value?.copy(isUrlError = isError)
    }

    fun updateRequestPath(value: String) {
        _draftEngine.value = _draftEngine.value?.copy(requestPath = value, isDetestPassed = false)
    }

    fun updateRequestProtocol(value: AiRequestProtocol) {
        _draftEngine.value = _draftEngine.value?.let { engine ->
            val fallbackPath = when (value) {
                AiRequestProtocol.RESPONSES_COMPATIBLE -> "/v1/responses"
                AiRequestProtocol.ANTHROPIC_COMPATIBLE -> "/v1/messages"
                else -> engine.requestPath.ifBlank { "/v1/chat/completions" }
            }
            val previousDefaultAuthType = AuthType.defaultFor(engine.requestProtocol)
            val nextAuthType = if (engine.authType == previousDefaultAuthType) {
                AuthType.defaultFor(value)
            } else {
                engine.authType
            }
            engine.copy(
                requestProtocol = value,
                authType = nextAuthType,
                requestPath = engine.requestPath.ifBlank { fallbackPath },
                isDetestPassed = false
            )
        }
    }

    fun updateAuthType(value: AuthType) {
        _draftEngine.value = _draftEngine.value?.copy(
            authType = value,
            isDetestPassed = false,
        )
    }

    fun updateAuthorizationCode(value: String) {
        _draftEngine.value = _draftEngine.value?.copy(authorizationCode = value, isDetestPassed = false)
    }

    fun updateStream(enabled: Boolean) {
        _draftEngine.value = _draftEngine.value?.copy(stream = enabled)
    }

    fun updateModel(model: AiModel) {
        _draftEngine.value = _draftEngine.value?.copy(model = model, isDetestPassed = false)
    }

    fun canDeleteCurrentEngine(): Boolean {
        val engine = originalEngine ?: _draftEngine.value ?: return false
        return localOwnedEngineKeys.value.contains(engine.identityKey())
    }

    fun beginAddLocalModel() {
        val engine = _draftEngine.value ?: return
        val draft = aiEngineCatalogManager.createLocalModelDraft(engine.name)
        _editingModelDraft.value = draft
        _editingModelOriginal.value = draft
    }

    fun beginEditModel(model: AiModel) {
        val engine = _draftEngine.value ?: return
        _editingModelDraft.value = model.copy(engineName = engine.name)
        _editingModelOriginal.value = model.copy(engineName = engine.name)
    }

    fun dismissModelEditor() {
        _editingModelDraft.value = null
        _editingModelOriginal.value = null
    }

    fun updateEditingModel(transform: (AiModel) -> AiModel) {
        _editingModelDraft.value = _editingModelDraft.value?.let(transform)
    }

    fun updateTemperature(value: Float) {
        _draftEngine.value = _draftEngine.value?.let { engine ->
            engine.copy(
                model = engine.model.copy(temperature = value.toDouble())
            )
        }
    }

    fun updateTopP(value: Float) {
        _draftEngine.value = _draftEngine.value?.let { engine ->
            engine.copy(
                model = engine.model.copy(topP = value.toDouble())
            )
        }
    }

    fun updateMaxTokens(value: Int) {
        _draftEngine.value = _draftEngine.value?.let { engine ->
            engine.copy(model = engine.model.copy(maxTokens = value))
        }
    }

    fun updateModelCanUploadFile(enabled: Boolean) {
        _draftEngine.value = _draftEngine.value?.let { engine ->
            engine.copy(model = engine.model.copy(canUploadFile = enabled))
        }
    }

    fun updateModelCanNetwork(enabled: Boolean) {
        _draftEngine.value = _draftEngine.value?.let { engine ->
            engine.copy(model = engine.model.copy(canNetwork = enabled))
        }
    }

    fun updateModelCanReasoning(enabled: Boolean) {
        _draftEngine.value = _draftEngine.value?.let { engine ->
            engine.copy(model = engine.model.copy(canReasoning = enabled))
        }
    }

    fun updateModelCanImage(enabled: Boolean) {
        _draftEngine.value = _draftEngine.value?.let { engine ->
            engine.copy(model = engine.model.copy(canImage = enabled))
        }
    }

    fun updateModelIsFast(enabled: Boolean) {
        _draftEngine.value = _draftEngine.value?.let { engine ->
            engine.copy(model = engine.model.copy(isFast = enabled))
        }
    }

    fun updateModelIsCode(enabled: Boolean) {
        _draftEngine.value = _draftEngine.value?.let { engine ->
            engine.copy(model = engine.model.copy(isCode = enabled))
        }
    }

    fun updateModelSupportToolCalls(enabled: Boolean) {
        _draftEngine.value = _draftEngine.value?.let { engine ->
            engine.copy(model = engine.model.copy(supportToolCalls = enabled))
        }
    }

    fun updateFileUploadStrategy(strategy: com.shifenmiao.model.ai.FileUploadStrategy) {
        _draftEngine.value = _draftEngine.value?.copy(fileUploadStrategy = strategy)
    }

    fun updateCloudStorageConnectionId(value: String?) {
        _draftEngine.value = _draftEngine.value?.copy(cloudStorageConnectionId = value)
    }

    fun updateCloudStorageBucket(value: String?) {
        _draftEngine.value = _draftEngine.value?.copy(cloudStorageBucket = value)
    }

    fun updateCloudStoragePrefix(value: String) {
        _draftEngine.value = _draftEngine.value?.copy(cloudStoragePrefix = value)
    }

    fun markTestPassed() {
        _draftEngine.value = _draftEngine.value?.copy(isDetestPassed = true)
    }

    fun restoreDraft() {
        _draftEngine.value = originalEngine
    }

    fun persistDraft(onComplete: (Boolean) -> Unit) {
        val draft = _draftEngine.value ?: return onComplete(false)
        _isSaving.value = true
        aiEngineCatalogManager.saveEngineConfigOnly(draft) { success ->
            _isSaving.value = false
            if (success) {
                originalEngine = draft
            }
            onComplete(success)
        }
    }

    fun persistModelDraft(onComplete: (Boolean) -> Unit) {
        val editing = _editingModelDraft.value ?: return onComplete(false)
        _isSaving.value = true
        aiEngineCatalogManager.upsertLocalModel(editing) { success, savedModel ->
            _isSaving.value = false
            if (success && savedModel != null) {
                _draftEngine.value = _draftEngine.value?.copy(model = savedModel)
                originalEngine = originalEngine?.copy(model = savedModel)
                _editingModelDraft.value = null
                _editingModelOriginal.value = null
            }
            onComplete(success)
        }
    }

    fun deleteLocalModel(model: AiModel, onComplete: (Boolean) -> Unit) {
        _isDeleting.value = true
        aiEngineCatalogManager.deleteLocalModel(
            model = model,
            engineRequestProtocol = AiRequestProtocol.fromValue(requestProtocol),
        ) { success, updatedEngine ->
            _isDeleting.value = false
            if (success) {
                updatedEngine?.let { latest ->
                    originalEngine = latest
                    _draftEngine.value = _draftEngine.value?.copy(model = latest.model) ?: latest
                }
            }
            onComplete(success)
        }
    }

    fun resetRemoteModelOverrides(onComplete: (Boolean) -> Unit) {
        val engine = _draftEngine.value ?: return onComplete(false)
        if (engine.model.canEdit) return onComplete(false)

        _isSaving.value = true
        aiEngineCatalogManager.clearRemoteModelOverrides(
            model = engine.model,
            engineRequestProtocol = AiRequestProtocol.fromValue(requestProtocol),
        ) { success, updatedEngine ->
            _isSaving.value = false
            if (success && updatedEngine != null) {
                originalEngine = updatedEngine
                _draftEngine.value = updatedEngine
            }
            onComplete(success)
        }
    }

    fun deleteLocalEngine(onComplete: (Boolean) -> Unit) {
        val target = originalEngine ?: _draftEngine.value ?: return onComplete(false)
        _isDeleting.value = true
        aiEngineCatalogManager.deleteLocalEngine(target) { success ->
            _isDeleting.value = false
            if (success) {
                originalEngine = null
                _draftEngine.value = null
            }
            onComplete(success)
        }
    }

    fun loadRemoteModels(onComplete: (Boolean) -> Unit = {}) {
        val engine = _draftEngine.value ?: return onComplete(false)
        val baseUrl = engine.requestUrl.trimEnd('/')
        val url = "$baseUrl/models"

        componentScope.launch {
            _isLoadingRemoteModels.value = true
            _remoteModelsError.value = null
            try {
                val response = when (engine.authType) {
                    AuthType.API_KEY -> {
                        openAIWithApiKeyService.listModels(
                            url = url,
                            apiKey = engine.authorizationCode
                        )
                    }
                    else -> {
                        openAICompatibleService.listModels(
                            url = url,
                            authorization = engine.authorizationCode
                                .takeIf { it.isNotBlank() }
                                ?.let { "Bearer $it" }
                        )
                    }
                }
                if (response.isSuccessful) {
                    _remoteModels.value = response.body()?.data ?: emptyList()
                    onComplete(true)
                } else {
                    _remoteModelsError.value = "加载失败: ${response.code()}"
                    onComplete(false)
                }
            } catch (e: Exception) {
                _remoteModelsError.value = e.message
                onComplete(false)
            } finally {
                _isLoadingRemoteModels.value = false
            }
        }
    }

    fun submitSelectedModels(selectedIds: List<String>, onComplete: (Boolean) -> Unit = {}) {
        val engine = _draftEngine.value ?: return onComplete(false)
        val selectedModels = _remoteModels.value
            .filter { it.id in selectedIds }
            .map { ModelDraft(name = it.id, title = it.id) }

        if (selectedModels.isEmpty()) return onComplete(false)

        componentScope.launch {
            try {
                val request = SubmitModelsRequest(engineName = engine.name, models = selectedModels)
                val response = apiService.submitAiModels(request)
                if (response.isSuccessful) {
                    _remoteModels.value = emptyList()
                }
                onComplete(response.isSuccessful)
            } catch (_: Exception) {
                onComplete(false)
            }
        }
    }

    fun clearRemoteModels() {
        _remoteModels.value = emptyList()
        _remoteModelsError.value = null
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            @Assisted("engineName") engineName: String,
            @Assisted("requestProtocol") requestProtocol: String,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): AIEngineSettingsDetailComponent
    }
}
