package com.wanbaohe.setting.ai.component

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.PersistableBundle
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.AuthType
import com.shifenmiao.model.ai.openai.OpenAIModelItem
import com.wanbaohe.cloud.storage.data.CloudStorageRepository
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
    private val aiEngineCatalogManager: AIEngineCatalogManager,
    private val aiEngineManager: AIEngineManager,
    private val modelsFetcher: AiEngineModelsFetcher,
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

    fun updateTitle(value: String) {
        _draftEngine.value = _draftEngine.value?.copy(title = value)
    }

    fun updateRequestUrl(value: String) {
        // 改 URL 时连同已验证标记一并复位(F10), 与 updateRequestPath 等字段行为对齐,
        // 否则改了地址仍是"已验证", 静默复查又以 isDetestPassed 为跳过条件, 新 URL 永远不会被检查
        _draftEngine.value = _draftEngine.value?.copy(
            requestUrl = value,
            isUrlError = false,
            isDetestPassed = false,
        )
    }

    fun updateUrlValidation(isError: Boolean) {
        _draftEngine.value = _draftEngine.value?.copy(isUrlError = isError)
    }

    fun updateRequestPath(value: String) {
        _draftEngine.value = _draftEngine.value?.copy(requestPath = value, isDetestPassed = false)
    }

    fun updateRequestProtocol(value: AiRequestProtocol) {
        // 与新增页共用同一套切换/预填规则(F4 收敛), Jev/Pikafish 的预填行为两边一致
        _draftEngine.value = _draftEngine.value?.switchedRequestProtocol(value)
    }

    fun updateAuthType(value: AuthType) {
        _draftEngine.value = _draftEngine.value?.copy(
            authType = value,
            isDetestPassed = false,
        )
    }

    fun updateAuthorizationCode(value: String) {
        val cleaned = com.shifenmiao.base.utils.StringUtils.sanitizeApiToken(value)
        _draftEngine.value = _draftEngine.value?.copy(authorizationCode = cleaned, isDetestPassed = false)
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
        _editingModelDraft.value = model.copy(
            engineName = engine.name,
            title = model.title.ifBlank { prettifyModelTitle(model.name) },
        )
        _editingModelOriginal.value = _editingModelDraft.value
    }

    fun dismissModelEditor() {
        _editingModelDraft.value = null
        _editingModelOriginal.value = null
    }

    fun updateEditingModel(transform: (AiModel) -> AiModel) {
        _editingModelDraft.value = _editingModelDraft.value?.let(transform)
    }

    /**
     * 改模型标识时同步维护显示名：用户还没手改过显示名就自动跟一版「首字母大写」驼峰，
     * 避免新增弹窗里三个字段都要填、还得自己想展示名。
     */
    fun updateEditingModelName(value: String) {
        _editingModelDraft.value = _editingModelDraft.value?.let { model ->
            val autoTitle = prettifyModelTitle(value)
            val titleWasAuto = model.title.isBlank() || model.title == prettifyModelTitle(model.name)
            model.copy(
                name = value,
                title = if (titleWasAuto) autoTitle else model.title,
            )
        }
    }

    companion object {
        private const val CLIP_LABEL = "ai_engine_config"

        /** deepseek-flash → Deepseek-Flash；每个 -/_/空格 后的首字母大写 */
        internal fun prettifyModelTitle(raw: String): String {
            if (raw.isBlank()) return raw
            return raw.mapIndexed { index, c ->
                val isSegmentStart = index == 0 || !raw[index - 1].isLetterOrDigit()
                if (isSegmentStart && c.isLetter()) c.uppercaseChar() else c
            }.joinToString("")
        }
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
        // 测试通过同时清掉 URL 错误标记(静默复查失败留下的), 否则保存后徽章仍显示不可用
        _draftEngine.value = _draftEngine.value?.copy(isDetestPassed = true, isUrlError = false)
    }

    /**
     * 导出当前引擎配置到系统剪贴板(JSON, 含鉴权 Token)。
     *
     * 标记为敏感内容: Android 13+ 的剪贴板预览会因此隐藏具体内容。
     * 导出入口只对本地自有引擎开放, 由页面侧判断。
     */
    fun exportConfigToClipboard(): Boolean {
        val engine = _draftEngine.value ?: return false
        return copySensitiveToClipboard(AiEngineConfigCodec.encode(engine))
    }

    /** 导出为导入深链(onebox://action/import_engine?config=...), 同样走敏感剪贴板(F12) */
    fun exportImportLinkToClipboard(): Boolean {
        val engine = _draftEngine.value ?: return false
        return copySensitiveToClipboard(AiEngineConfigCodec.encodeAsImportLink(engine))
    }

    private fun copySensitiveToClipboard(content: String): Boolean {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return false
        val clip = ClipData.newPlainText(CLIP_LABEL, content)
        // 旧系统不认识这个 extra 会安全忽略
        clip.description.extras = PersistableBundle().apply {
            putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
        }
        return runCatching { clipboard.setPrimaryClip(clip) }.isSuccess
    }

    /** 该引擎是否已是当前对话引擎(同名即视为同一引擎, 与 AIEngineManager 的同名重绑语义一致) */
    fun isDraftCurrentDefault(): Boolean {
        val draft = _draftEngine.value ?: return true
        val current = aiEngineManager.getCurrentAiEngine()
        return current.identityKey() == draft.identityKey() ||
            current.name.equals(draft.name, ignoreCase = true)
    }

    /** 把当前草稿设为当前对话引擎; 失败返回 false(不阻塞退出) */
    fun setDraftAsCurrentDefault(): Boolean {
        val draft = _draftEngine.value ?: return false
        // setDefaultEngine 返回 Unit、失败路径静默 return, 必须回读确认真正生效(F9)
        return runCatching {
            aiEngineManager.setDefaultEngine(draft)
            val current = aiEngineManager.getCurrentAiEngine()
            current.identityKey() == draft.identityKey() ||
                current.name.equals(draft.name, ignoreCase = true)
        }.getOrDefault(false)
    }

    fun restoreDraft() {
        _draftEngine.value = originalEngine
    }

    fun persistDraft(onComplete: (Boolean) -> Unit) {
        val draft = _draftEngine.value ?: return onComplete(false)
        // 保存时 originalEngine 仍是被编辑的原行快照(草稿有改动时 init 收集器不会覆盖它),
        // 其 name/requestProtocol 就是定位原行所需的 (previousName, originalProtocol)。
        val original = originalEngine
        _isSaving.value = true
        aiEngineCatalogManager.saveEditedLocalEngine(
            engine = draft,
            originalName = original?.name,
            originalProtocol = original?.requestProtocol?.name,
        ) { success ->
            _isSaving.value = false
            if (success) {
                original?.let { aiEngineManager.onLocalEngineEdited(oldEngine = it, newEngine = draft) }
                originalEngine = draft
                // 保存后的静默连通性复查统一由 AIEngineCatalogManager 的保存路径触发(F8),
                // 新增/编辑两条入口都会走到, 这里不再单独挂
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

        componentScope.launch {
            _isLoadingRemoteModels.value = true
            _remoteModelsError.value = null
            modelsFetcher.fetch(engine)
                .onSuccess { models ->
                    _remoteModels.value = models
                    onComplete(true)
                }
                .onFailure { error ->
                    _remoteModelsError.value = error.message
                    onComplete(false)
                }
            _isLoadingRemoteModels.value = false
        }
    }

    /**
     * 把服务商列表里的模型加成本地模型（模型管理只走本地，不再提交到服务端）。
     *
     * 已经存在的同名本地模型直接选中，避免重复添加把用户改过的参数覆盖掉。
     */
    fun addProviderModelLocally(model: OpenAIModelItem, onComplete: (Boolean) -> Unit = {}) {
        val engine = _draftEngine.value ?: return onComplete(false)
        if (model.id.isBlank()) return onComplete(false)

        val existing = modelsByProvider.value[engine.name.lowercase()]
            ?.firstOrNull { it.name == model.id }
        if (existing != null) {
            _draftEngine.value = _draftEngine.value?.copy(model = existing)
            originalEngine = originalEngine?.copy(model = existing)
            onComplete(true)
            return
        }

        val localModel = engine.model.copy(
            id = 0,
            name = model.id,
            title = AIEngineSettingsDetailComponent.prettifyModelTitle(model.id),
            engineName = engine.name,
            canEdit = true,
            hasLocalOverrides = false,
        )
        aiEngineCatalogManager.upsertLocalModel(localModel) { success, savedModel ->
            if (success && savedModel != null) {
                _draftEngine.value = _draftEngine.value?.copy(model = savedModel)
                originalEngine = originalEngine?.copy(model = savedModel)
            }
            onComplete(success)
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
