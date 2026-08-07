package com.shifenmiao.ai.component

import com.arkivanov.decompose.ComponentContext
import com.google.gson.Gson
import com.shifenmiao.ai.agent.tool.AgentToolRegistry
import com.shifenmiao.ai.mediator.MessageRemoteMediator
import com.shifenmiao.ai.service.CreationMetaService
import com.shifenmiao.ai.service.PromptSavedResult
import com.shifenmiao.ai.service.PromptCreationService
import com.shifenmiao.network.service.SensitiveWordCheckOutcome
import com.shifenmiao.network.service.SensitiveWordChecker
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.core.R
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.shifenmiao.database.data_draft.dao.DataDraftDao
import com.shifenmiao.database.data_draft.entity.DataDraftEntity
import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.ai.ChatPrompt
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.RoleType
import com.shifenmiao.model.ai.tool.ToolCatalogItem
import com.shifenmiao.model.ai.unified.LlmStreamEvent
import com.shifenmiao.model.moderation.SensitiveWordCheckField
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.storage.RemoteConfigStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.resource.ResourceManager
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.code.editor.CodeEditorDataStore
import com.wanbaohe.markdown.edit.EditorDataStore
import com.shifenmiao.model.event.EditorResultEvent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import com.shifenmiao.model.event.AppEventBus
import java.util.Date

/**
 * 创建提示词组件
 *
 * 职责：
 * 1. 组合系统提示词 + 用户描述 → 发送给AI
 * 2. 流式接收AI返回的JSON → 累积并解析为 [ChatPrompt]
 * 3. 草稿箱持久化管理（FeatureDatabase.data_draft）
 * 4. 成功后可保存为 ChatPromptEntity（AppDatabase.prompt）+ ItemEntity
 */
class CreateAIPromptComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val initialDraftId: Long,
    @Assisted val onGoBack: () -> Unit,
    @Assisted("navigate") val onNavigate: (Screen) -> Unit,
    @Assisted("navigateReplacingCurrent") val onNavigateReplacingCurrent: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
    resourceManager: ResourceManager,
    private val appDatabase: AppDatabase,
    private val dataDraftDao: DataDraftDao,
    private val dataDraftHelper: DataDraftHelper,
    val aiEngineManager: AIEngineManager,
    val aiEngineCatalogManager: AIEngineCatalogManager,
    private val agentToolRegistry: AgentToolRegistry,
    private val creationMetaService: CreationMetaService,
    private val promptCreationService: PromptCreationService,
    private val sensitiveWordChecker: SensitiveWordChecker,
    private val messageRemoteMediator: MessageRemoteMediator,
    private val gson: Gson
) : BaseComponent(dispatchersHolder, componentContext), ResourceManager by resourceManager {

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialDraftId: Long,
            onGoBack: () -> Unit,
            @Assisted("navigate") onNavigate: (Screen) -> Unit,
            @Assisted("navigateReplacingCurrent") onNavigateReplacingCurrent: (Screen) -> Unit,
        ): CreateAIPromptComponent
    }

    // ── UI 状态 ────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow(CreateAIChatPromptUiState())
    val uiState: StateFlow<CreateAIChatPromptUiState> = _uiState.asStateFlow()

    private val _drafts = MutableStateFlow<List<DataDraftEntity>>(emptyList())
    val drafts: StateFlow<List<DataDraftEntity>> = _drafts.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _availableTools = MutableStateFlow<List<ToolCatalogItem>>(emptyList())
    val availableTools: StateFlow<List<ToolCatalogItem>> = _availableTools.asStateFlow()

    private val itemDao = appDatabase.itemEntityDao()
    private val categoryDao = appDatabase.categoryDao()
    private val chatPromptDao = appDatabase.chatPromptDao()

    private var generateJob: Job? = null

    // Gson 由 Hilt 注入，无需每次创建实例

    /** 对话历史记录，用于多轮对话（assistant回复 + 用户追加修改） */
    private val conversationHistory = mutableListOf<MessageEntity>()

    /** 用于匹配 CodeEditor / AgentJsonEditor 回传结果的草稿 ID */
    private var editorDraftId: Long = 0L

    private var editorTarget: EditorTarget = EditorTarget.PROMPT_BODY

    private var lastSavedPromptId: Int? = null
    private var lastSavedSignature: String? = null

    init {
        observeDrafts()
        observeCategories()
        observeAvailableTools()
        componentScope.launch {
            AppEventBus.editorResultEvents.collect { event ->
                onEditorResult(event)
            }
        }
        if (initialDraftId > 0L) {
            componentScope.launch(Dispatchers.IO) {
                loadInitialDraft(initialDraftId)
            }
        }
    }

    /**
     * 初次进入页面时加载指定草稿。
     * - 草稿有 data → 直接 [loadDraft]。
     * - 草稿为空但有 relatedEntityId → 从 [com.shifenmiao.database.chat_prompt.entity.PromptEntity] 读取
     *   原始 Prompt 数据,序列化为标准 payload 后回填草稿再加载,确保页面带数据展示。
     */
    private suspend fun loadInitialDraft(draftId: Long) {
        val draft = dataDraftHelper.getById(draftId) ?: return
        if (draft.data.isNotBlank()) {
            withContext(Dispatchers.Main) { loadDraft(draft) }
            return
        }
        val relatedPromptId = draft.relatedEntityId?.takeIf { it > 0 } ?: return
        val promptEntity = chatPromptDao.getPromptById(relatedPromptId) ?: return
        val chatPrompt = ChatPrompt(
            id = promptEntity.id,
            title = promptEntity.title,
            description = promptEntity.description,
            prompt = promptEntity.prompt,
            placeholder = promptEntity.placeholder,
            templates = promptEntity.templates,
        )
        val serialized = promptCreationService.serializePromptPayload(chatPrompt)
        dataDraftHelper.updateDraft(
            draftId = draft.id,
            title = promptEntity.title,
            description = promptEntity.description,
            data = serialized,
            status = DataDraftEntity.STATUS_SUCCESS,
        )
        val refreshed = dataDraftHelper.getById(draft.id) ?: return
        withContext(Dispatchers.Main) { loadDraft(refreshed) }
    }

    // ── 草稿箱 ─────────────────────────────────────────────────────────

    private fun observeDrafts() {
        dataDraftDao.observeAllByType(ListItemType.PROMPT.id)
            .onEach { list -> _drafts.value = list }
            .launchIn(componentScope)
    }

    private fun observeCategories() {
        categoryDao.getAllCategories()
            .onEach { list -> _categories.value = list }
            .launchIn(componentScope)
    }

    private fun observeAvailableTools() {
        componentScope.launch(Dispatchers.IO) {
            _availableTools.value = agentToolRegistry.getVisibleTools()
                .sortedWith(compareBy<ToolCatalogItem> { it.sortOrder }.thenBy { it.title })
        }
    }

    /** 删除草稿 */
    fun deleteDraft(draft: DataDraftEntity) {
        componentScope.launch(Dispatchers.IO) {
            dataDraftDao.deleteById(draft.id)
            if (_uiState.value.currentDraftId == draft.id) {
                resetForNew()
            }
        }
    }

    /** 新建：重置所有状态，开始全新的创建 */
    fun resetForNew() {
        generateJob?.cancel()
        generateJob = null
        conversationHistory.clear()
        lastSavedPromptId = null
        lastSavedSignature = null
        _uiState.update {
            CreateAIChatPromptUiState() // 全部恢复默认值
        }
    }

    /** 加载草稿到编辑区，完整刷新UI（包括预览、输入文本等） */
    fun loadDraft(draft: DataDraftEntity) {
        generateJob?.cancel()
        generateJob = null
        conversationHistory.clear()
        val parsedPrompt = tryParseChatPrompt(draft.data)
        val restoredStatus = when {
            draft.status == DataDraftEntity.STATUS_SUCCESS && parsedPrompt != null -> GenerationStatus.SUCCESS
            draft.data.isNotBlank() && draft.status != DataDraftEntity.STATUS_DRAFT -> GenerationStatus.FAILED
            else -> GenerationStatus.IDLE
        }
        val restoredCategoryIds = DataDraftHelper.decodeCategoryIds(draft.selectedCategoryIds)
        val restoredToolNames = decodeDraftToolNames(draft.url)
        if (restoredStatus == GenerationStatus.SUCCESS && draft.data.isNotBlank()) {
            conversationHistory.add(
                MessageEntity(
                    completionId = "draft_user_${draft.id}",
                    conversationId = "create_prompt_draft",
                    role = RoleType.USER.value,
                    question = draft.description,
                    answer = "",
                    reasoningContent = "",
                    engine = "",
                    model = ""
                )
            )
            conversationHistory.add(
                MessageEntity(
                    completionId = "draft_assistant_${draft.id}",
                    conversationId = "create_prompt_draft",
                    role = RoleType.ASSISTANT.value,
                    question = "",
                    answer = draft.data,
                    reasoningContent = "",
                    engine = "",
                    model = ""
                )
            )
        }
        _uiState.update {
            it.copy(
                inputText = if (restoredStatus == GenerationStatus.SUCCESS) "" else draft.description,
                rawJson = draft.data,
                parsedPrompt = parsedPrompt,
                status = restoredStatus,
                currentDraftId = draft.id,
                errorMessage = null,
                isSaving = false,
                conversationRound = if (restoredStatus == GenerationStatus.SUCCESS) 1 else 0,
                selectedCategoryIds = restoredCategoryIds,
                selectedToolNames = restoredToolNames,
            )
        }
        val savedPromptId = draft.relatedEntityId?.takeIf { it > 0 }
        lastSavedPromptId = savedPromptId
        lastSavedSignature = if (savedPromptId != null) {
            buildCurrentPromptSignature()
        } else {
            null
        }
    }

    // ── 输入 ───────────────────────────────────────────────────────────

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun hasUnsavedChanges(): Boolean {
        val state = _uiState.value
        val currentDraft = _drafts.value.firstOrNull { it.id == state.currentDraftId }
        val linkedPromptId = currentDraft?.relatedEntityId?.takeIf { it > 0 } ?: lastSavedPromptId
        val signature = buildCurrentPromptSignature()
        return when {
            state.isSaving || state.status == GenerationStatus.STREAMING -> true
            state.inputText.isNotBlank() -> true
            signature == null -> false
            linkedPromptId == null -> true
            lastSavedSignature == null -> true
            else -> signature != lastSavedSignature
        }
    }

    fun updateSelectedCategories(selectedCategories: List<Category>) {
        _uiState.update {
            it.copy(selectedCategoryIds = selectedCategories.map(Category::id).toSet())
        }
        persistDraftSelectionSnapshot()
    }

    fun updateSelectedToolNames(selectedToolNames: List<String>) {
        _uiState.update { it.copy(selectedToolNames = selectedToolNames.toSet()) }
        persistDraftSelectionSnapshot()
    }

    // ── 外部编辑器 ─────────────────────────────────────────────────────

    /**
     * 把当前 prompt body 写入 [CodeEditorDataStore] 并跳转到 [Screen.CodeEditor]，
     * 由 [onEditorResult] 在 EditorTarget.PROMPT_BODY 分支中接收回传结果。
     */
    fun editPromptManually() {
        val state = _uiState.value
        val prompt = state.parsedPrompt ?: return
        val promptText = prompt.prompt?.trim().orEmpty()
        if (promptText.isEmpty()) return

        componentScope.launch(Dispatchers.IO) {
            val codeDraftId = CodeEditorDataStore.put(dataDraftHelper, promptText)
            editorDraftId = codeDraftId
            editorTarget = EditorTarget.PROMPT_BODY
            val editTitle = prompt.title?.takeIf { it.isNotBlank() }
                ?: getString(R.string.code_editor_title)
            withContext(Dispatchers.Main) {
                onNavigate(
                    Screen.CodeEditor(
                        editDraftId = codeDraftId,
                        editTitle = editTitle,
                    )
                )
            }
        }
    }

    fun editRawResultInEditor() {
        val rawResult = _uiState.value.rawJson.trim()
        if (rawResult.isEmpty()) return
        componentScope.launch(Dispatchers.IO) {
            val draftId = EditorDataStore.put(dataDraftHelper, rawResult)
            editorDraftId = draftId
            editorTarget = EditorTarget.RAW_RESULT
            withContext(Dispatchers.Main) {
                onNavigate(
                    Screen.AgentJsonEditor(
                        editDraftId = draftId,
                        editTitle = getString(R.string.create_ai_chat_prompt_stream_raw_toggle)
                    )
                )
            }
        }
    }

    /** 接收 AgentJsonEditor 回传的编辑结果 */
    fun onEditorResult(event: EditorResultEvent) {
        if (event.editDraftId != editorDraftId) return
        when (editorTarget) {
            EditorTarget.PROMPT_BODY -> {
                val currentPrompt = _uiState.value.parsedPrompt ?: return
                val updatedPrompt = currentPrompt.copy(prompt = event.text)
                val serialized = promptCreationService.serializePromptPayload(updatedPrompt)
                _uiState.update {
                    it.copy(
                        parsedPrompt = updatedPrompt,
                        rawJson = serialized,
                        status = GenerationStatus.SUCCESS,
                        errorMessage = null,
                    )
                }
                componentScope.launch(Dispatchers.IO) {
                    persistCurrentDraftContent(
                        description = resolveFallbackInputText(),
                        rawJson = serialized,
                        status = GenerationStatus.SUCCESS,
                        generatedTitle = updatedPrompt.title,
                    )
                    CodeEditorDataStore.clear(dataDraftHelper, event.editDraftId)
                }
            }

            EditorTarget.RAW_RESULT -> {
                componentScope.launch(Dispatchers.IO) {
                    val generationResult = promptCreationService.parseGenerationResult(event.text)
                    val prompt = generationResult.payload.prompt
                    val status = if (!prompt?.prompt.isNullOrBlank()) {
                        GenerationStatus.SUCCESS
                    } else {
                        GenerationStatus.FAILED
                    }
                    _uiState.update {
                        it.copy(
                            rawJson = generationResult.cleanedJson,
                            parsedPrompt = prompt ?: it.parsedPrompt,
                            status = status,
                            errorMessage = generationResult.errorMessage,
                        )
                    }
                    if (status == GenerationStatus.SUCCESS) {
                        applySuggestedMeta(
                            inputText = resolveFallbackInputText(),
                            prompt = prompt,
                            aiSuggestedCategoryNames = generationResult.payload.suggestedCategoryNames,
                            aiSuggestedToolNames = generationResult.payload.suggestedToolNames
                        )
                    }
                    persistCurrentDraftContent(
                        description = resolveFallbackInputText(),
                        rawJson = generationResult.cleanedJson,
                        status = status,
                        generatedTitle = prompt?.title,
                    )
                    EditorDataStore.clear(dataDraftHelper, event.editDraftId)
                }
            }
        }
    }

    /** 更新 Prompt 标题 */
    fun updatePromptTitle(title: String) {
        val currentPrompt = _uiState.value.parsedPrompt ?: return
        val updatedPrompt = currentPrompt.copy(title = title)
        _uiState.update {
            it.copy(parsedPrompt = updatedPrompt)
        }
        persistStructuredPromptChanges(updatedPrompt)
    }

    /** 更新 Prompt 描述 */
    fun updatePromptDescription(description: String) {
        val currentPrompt = _uiState.value.parsedPrompt ?: return
        val updatedPrompt = currentPrompt.copy(description = description)
        _uiState.update {
            it.copy(parsedPrompt = updatedPrompt)
        }
        persistStructuredPromptChanges(updatedPrompt)
    }

    // ── 生成 ───────────────────────────────────────────────────────────

    /** 取消正在进行的生成 */
    fun cancelGeneration() {
        generateJob?.cancel()
        generateJob = null
        _uiState.update {
            it.copy(
                status = when {
                    it.parsedPrompt != null -> GenerationStatus.SUCCESS
                    it.rawJson.isNotBlank() -> GenerationStatus.FAILED
                    else -> GenerationStatus.IDLE
                }
            )
        }
    }

    /** 发起AI生成（支持多轮对话：首次创建或在现有结果基础上修改） */
    fun generate() {
        val description = _uiState.value.inputText.trim()
        if (description.isEmpty()) return

        generateJob?.cancel()
        _uiState.update {
            it.copy(
                status = GenerationStatus.STREAMING,
                rawJson = "",
                reasoningText = "",
                errorMessage = null,
            )
        }

        generateJob = componentScope.launch(Dispatchers.IO) {
            try {
                val systemPrompt = buildSystemPrompt()
                val conversation = buildConversation(systemPrompt)

                val currentUserMessage = MessageEntity(
                    completionId = Date().time.toString(),
                    conversationId = conversation.id,
                    role = RoleType.USER.value,
                    question = description,
                    answer = "",
                    reasoningContent = "",
                    engine = conversation.engine.name,
                    model = conversation.engine.model.name
                )
                val allMessages = conversationHistory.toMutableList().apply {
                    add(currentUserMessage)
                }

                val resultFlow: Flow<LlmStreamEvent> =
                    messageRemoteMediator.fetchAndSaveMessages(
                        conversation, allMessages, false
                    )

                val jsonBuffer = StringBuilder()
                var streamCompleted = false
                resultFlow.collect { event ->
                    when (event) {
                        is LlmStreamEvent.Error -> {
                            _uiState.update {
                                it.copy(
                                    status = GenerationStatus.FAILED,
                                    errorMessage = event.errorMessage.ifEmpty { getString(R.string.create_ai_chat_prompt_error_service_unavailable) }
                                )
                            }
                        }

                        is LlmStreamEvent.TextDelta -> {
                            jsonBuffer.append(event.text)
                            _uiState.update { it.copy(rawJson = jsonBuffer.toString()) }
                        }

                        is LlmStreamEvent.ReasoningDelta -> {
                            if (event.text.isNotEmpty()) {
                                _uiState.update {
                                    it.copy(reasoningText = it.reasoningText + event.text)
                                }
                            }
                        }

                        is LlmStreamEvent.Completed -> {
                            streamCompleted = true
                            conversationHistory.add(currentUserMessage)
                            conversationHistory.add(
                                MessageEntity(
                                    completionId = "assistant_${Date().time}",
                                    conversationId = conversation.id,
                                    role = RoleType.ASSISTANT.value,
                                    question = "",
                                    answer = jsonBuffer.toString(),
                                    reasoningContent = "",
                                    engine = conversation.engine.name,
                                    model = conversation.engine.model.name
                                )
                            )
                            onStreamEnd(jsonBuffer.toString(), description)
                        }

                        is LlmStreamEvent.ResponseStarted,
                        is LlmStreamEvent.UsageUpdated,
                        is LlmStreamEvent.SearchResultsEvent,
                        is LlmStreamEvent.ToolCallDeltaEvent -> Unit
                    }
                }

                if (!streamCompleted && _uiState.value.status == GenerationStatus.STREAMING) {
                    conversationHistory.add(currentUserMessage)
                    conversationHistory.add(
                        MessageEntity(
                            completionId = "assistant_${Date().time}",
                            conversationId = conversation.id,
                            role = RoleType.ASSISTANT.value,
                            question = "",
                            answer = jsonBuffer.toString(),
                            reasoningContent = "",
                            engine = conversation.engine.name,
                            model = conversation.engine.model.name
                        )
                    )
                    onStreamEnd(jsonBuffer.toString(), description)
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.update {
                    it.copy(
                        status = GenerationStatus.FAILED,
                        errorMessage = getString(R.string.create_ai_chat_prompt_error_network)
                    )
                }
            }
        }
    }

    /** 流结束后解析JSON并持久化草稿 */
    private suspend fun onStreamEnd(rawJson: String, description: String) {
        val generationResult = promptCreationService.parseGenerationResult(rawJson)
        val cleanedJson = generationResult.cleanedJson
        val promptPayload = generationResult.payload
        val prompt = promptPayload.prompt
        val status = if (prompt?.prompt != null) GenerationStatus.SUCCESS else GenerationStatus.FAILED
        val errorMsg = generationResult.errorMessage

        _uiState.update {
            it.copy(
                rawJson = cleanedJson,
                parsedPrompt = prompt ?: it.parsedPrompt,
                status = status,
                errorMessage = errorMsg,
                conversationRound = if (status == GenerationStatus.SUCCESS) it.conversationRound + 1 else it.conversationRound,
                inputText = if (status == GenerationStatus.SUCCESS) "" else it.inputText,
            )
        }

        if (status == GenerationStatus.SUCCESS) {
            applySuggestedMeta(
                inputText = description,
                prompt = prompt,
                aiSuggestedCategoryNames = promptPayload.suggestedCategoryNames,
                aiSuggestedToolNames = promptPayload.suggestedToolNames
            )
        }

        persistCurrentDraftContent(
            description = description,
            rawJson = cleanedJson,
            status = status,
            generatedTitle = prompt?.title,
        )
    }

    private suspend fun persistCurrentDraftContent(
        description: String,
        rawJson: String,
        status: GenerationStatus,
        generatedTitle: String? = null,
    ) {
        val currentDraftId = _uiState.value.currentDraftId
        val currentState = _uiState.value
        val currentDraft = _drafts.value.firstOrNull { it.id == currentDraftId }
        val newId = promptCreationService.saveDraft(
            draftId = currentDraftId,
            description = description,
            rawJson = rawJson,
            isSuccess = status == GenerationStatus.SUCCESS,
            generatedTitle = generatedTitle,
            selectedCategoryIds = currentState.selectedCategoryIds,
            selectedToolNames = currentState.selectedToolNames,
            itemId = currentDraft?.itemId,
            relatedEntityId = currentDraft?.relatedEntityId,
        )
        if (currentDraftId == null || currentDraftId == 0L) {
            _uiState.update { it.copy(currentDraftId = newId) }
        }
    }

    // ── 敏感词校验 ─────────────────────────────────────────────────────

    /**
     * 在保存前对 ChatPrompt 标题进行敏感词检测。
     * - 远端开关未启用 / 标题为空 → 返回 null (放行)
     * - 命中 → 返回 [SensitiveWordCheckOutcome.Hit] (调用方负责拦截)
     * - 网络/接口异常 → 返回 null (fail-open, 放行)
     */
    private suspend fun checkTitleForSensitiveWords(
        title: String,
        scene: String,
    ): SensitiveWordCheckOutcome.Hit? {
        if (RemoteConfigStorage.getRemoteConfig().enablePromptSensitiveCheck != true) return null
        if (title.isBlank()) return null
        return when (val outcome = sensitiveWordChecker.check(
            scene = scene,
            fields = listOf(SensitiveWordCheckField(key = "title", text = title))
        )) {
            is SensitiveWordCheckOutcome.Hit -> outcome
            else -> null
        }
    }

    // ── 保存到 ChatPromptEntity + ItemEntity ────────────────────────────

    /**
     * 将生成成功的ChatPrompt保存到正式数据库，同时创建对应的ItemEntity并关联分类。
     */
    fun savePrompt(
        onSuccess: (PromptSavedResult) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        if (_uiState.value.isSaving) return
        val chatPrompt = _uiState.value.parsedPrompt
        if (chatPrompt?.prompt.isNullOrBlank()) {
            onFailure(getString(R.string.create_ai_chat_prompt_error_no_prompt))
            return
        }

        val fallbackInputText = resolveFallbackInputText()
        val finalPrompt = chatPrompt.copy(
            id = 0,
            title = if (chatPrompt.title.isNullOrEmpty()) fallbackInputText.take(30) else chatPrompt.title,
            description = if (chatPrompt.description.isNullOrEmpty()) fallbackInputText else chatPrompt.description,
        )

        _uiState.update { it.copy(isSaving = true) }
        componentScope.launch(Dispatchers.IO) {
            try {
                val hit = checkTitleForSensitiveWords(
                    title = finalPrompt.title.orEmpty(),
                    scene = "prompt_create_save",
                )
                if (hit != null) {
                    val message = hit.message.ifBlank { getString(R.string.create_ai_chat_prompt_sensitive_blocked) }
                    withContext(Dispatchers.Main) {
                        onFailure(message)
                    }
                    return@launch
                }
                val savedResult = promptCreationService.savePrompt(
                    parsedPrompt = finalPrompt,
                    fallbackInputText = fallbackInputText,
                    selectedCategoryIds = _uiState.value.selectedCategoryIds,
                    selectedToolNames = _uiState.value.selectedToolNames,
                    draftId = _uiState.value.currentDraftId
                )
                lastSavedPromptId = savedResult.prompt.id
                lastSavedSignature = buildCurrentPromptSignature()

                withContext(Dispatchers.Main) {
                    onSuccess(savedResult)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(getString(R.string.create_ai_chat_prompt_error_save, e.message.orEmpty()))
                }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    /** 新增分类（用户在保存弹窗中创建） */
    fun insertCategoryByName(name: String) {
        if (name.isBlank()) return
        componentScope.launch(Dispatchers.IO) {
            val category = creationMetaService.ensureCategoryByName(name)
            _uiState.update {
                it.copy(selectedCategoryIds = it.selectedCategoryIds + category.id)
            }
            persistDraftSelectionSnapshot()
        }
    }

    private suspend fun applySuggestedMeta(
        inputText: String,
        prompt: ChatPrompt?,
        aiSuggestedCategoryNames: List<String> = emptyList(),
        aiSuggestedToolNames: List<String> = emptyList()
    ) {
        val suggestion = promptCreationService.resolveSuggestedMeta(
            inputText = inputText,
            prompt = prompt,
            aiSuggestedCategoryNames = aiSuggestedCategoryNames,
            aiSuggestedToolNames = aiSuggestedToolNames
        )
        _uiState.update {
            it.copy(
                selectedCategoryIds = if (it.selectedCategoryIds.isEmpty()) {
                    suggestion.categoryIds
                } else {
                    it.selectedCategoryIds
                },
                selectedToolNames = if (it.selectedToolNames.isEmpty()) {
                    suggestion.toolNames
                } else {
                    it.selectedToolNames
                }
            )
        }
    }

    // ── 预览 ───────────────────────────────────────────────────────────

    /**
     * 将ChatPrompt以预览专用source保存到AppDatabase，然后返回带该ID的ChatPrompt对象。
     * AIChatBaseComponent 通过按 source=SOURCE_PREVIEW 查询，这样就能从DB加载到预览数据。
     * 每次预览都会先删除旧预览记录再插入新的（REPLACE策略）。
     */
    fun saveAndPreview(onReady: (ChatPrompt) -> Unit, onBlock: (String) -> Unit = {}) {
        val chatPrompt = _uiState.value.parsedPrompt ?: return
        val fallbackInputText = resolveFallbackInputText()
        val finalPrompt = chatPrompt.copy(
            title = if (chatPrompt.title.isNullOrEmpty()) fallbackInputText.take(30) else chatPrompt.title,
            description = if (chatPrompt.description.isNullOrEmpty()) fallbackInputText else chatPrompt.description,
        )
        componentScope.launch(Dispatchers.IO) {
            val hit = checkTitleForSensitiveWords(
                title = finalPrompt.title.orEmpty(),
                scene = "prompt_preview",
            )
            if (hit != null) {
                val message = hit.message.ifBlank { getString(R.string.create_ai_chat_prompt_sensitive_blocked) }
                withContext(Dispatchers.Main) { onBlock(message) }
                return@launch
            }
            val readyPrompt = promptCreationService.savePreviewPrompt(
                parsedPrompt = finalPrompt,
                fallbackInputText = fallbackInputText,
                selectedToolNames = _uiState.value.selectedToolNames
            )
            withContext(Dispatchers.Main) {
                onReady(readyPrompt)
            }
        }
    }

    // ── JSON解析辅助 ───────────────────────────────────────────────────

    /** 从AI输出中提取JSON部分 (去除markdown代码块包裹等) */
    private fun extractJson(raw: String): String {
        var result = raw.trim()
        if (result.startsWith("```json")) {
            result = result.removePrefix("```json").trimStart()
        } else if (result.startsWith("```")) {
            result = result.removePrefix("```").trimStart()
        }
        if (result.endsWith("```")) {
            result = result.removeSuffix("```").trimEnd()
        }
        return result
    }

    /** 安全解析ChatPrompt JSON，返回ChatPrompt或null */
    private fun tryParseChatPrompt(json: String): ChatPrompt? {
        return promptCreationService.parseGenerationResult(json).payload.prompt
    }

    /** 构建用户友好的错误消息 */
    private fun buildErrorMessage(json: String): String {
        if (json.isBlank()) return getString(R.string.create_ai_chat_prompt_error_no_response)
        return promptCreationService.parseGenerationResult(json).errorMessage
            ?: getString(R.string.create_ai_chat_prompt_error_parse_fallback)
    }

    // ── 构建AI请求 ─────────────────────────────────────────────────────

    private suspend fun buildSystemPrompt(): String {
        return promptCreationService.buildSystemPrompt()
    }

    private fun buildConversation(systemPrompt: String): Conversation {
        val engine = aiEngineManager.currentAIEngine.value
        return Conversation(
            id = "create_prompt_${Date().time}",
            title = getString(R.string.create_ai_chat_prompt_conversation_title),
            prompt = systemPrompt,
            engine = engine,
        )
    }

    private fun persistDraftSelectionSnapshot() {
        val currentDraftId = _uiState.value.currentDraftId ?: return
        val currentState = _uiState.value
        componentScope.launch(Dispatchers.IO) {
            dataDraftHelper.updateDraft(
                draftId = currentDraftId,
                url = gson.toJson(currentState.selectedToolNames.toList()),
                selectedCategoryIds = currentState.selectedCategoryIds,
            )
        }
    }

    private fun persistStructuredPromptChanges(prompt: ChatPrompt) {
        val currentDraftId = _uiState.value.currentDraftId ?: return
        componentScope.launch(Dispatchers.IO) {
            dataDraftHelper.updateDraft(
                draftId = currentDraftId,
                title = prompt.title,
                data = promptCreationService.serializePromptPayload(prompt),
            )
        }
    }

    private fun resolveFallbackInputText(): String {
        val currentState = _uiState.value
        return currentState.inputText.trim()
            .ifBlank {
                _drafts.value.firstOrNull { it.id == currentState.currentDraftId }
                    ?.description
                    .orEmpty()
                    .trim()
            }
            .ifBlank { currentState.parsedPrompt?.title.orEmpty().trim() }
            .ifBlank { currentState.parsedPrompt?.description.orEmpty().trim() }
    }

    private fun decodeDraftToolNames(raw: String): Set<String> = try {
        gson.fromJson(raw, Array<String>::class.java)?.toSet().orEmpty()
    } catch (_: Exception) {
        emptySet()
    }

    private fun buildCurrentPromptSignature(): String? {
        val state = _uiState.value
        val prompt = state.parsedPrompt
        if (
            prompt == null &&
            state.rawJson.isBlank() &&
            state.inputText.isBlank() &&
            state.selectedCategoryIds.isEmpty() &&
            state.selectedToolNames.isEmpty()
        ) {
            return null
        }
        return listOf(
            prompt?.title.orEmpty(),
            prompt?.description.orEmpty(),
            prompt?.prompt.orEmpty(),
            prompt?.placeholder.orEmpty(),
            prompt?.templates.orEmpty(),
            state.selectedCategoryIds.sorted().joinToString(","),
            state.selectedToolNames.sorted().joinToString(",")
        ).joinToString("|")
    }

    companion object {
        /**
         * 兜底系统提示词 — 仅在数据库无系统预置时使用
         */
    }
}

private enum class EditorTarget {
    PROMPT_BODY,
    RAW_RESULT,
}

/** UI状态数据类 */
data class CreateAIChatPromptUiState(
    val inputText: String = "",
    val rawJson: String = "",
    val reasoningText: String = "",
    val parsedPrompt: ChatPrompt? = null,
    val status: GenerationStatus = GenerationStatus.IDLE,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val currentDraftId: Long? = null,
    val selectedCategoryIds: Set<Int> = emptySet(),
    val selectedToolNames: Set<String> = emptySet(),
    /** 当前对话轮次，0=首次创建，>0=二次修改 */
    val conversationRound: Int = 0,
) {
    /** 是否处于多轮修改模式 */
    val isMultiTurn: Boolean get() = conversationRound > 0
}
