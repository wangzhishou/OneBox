package com.shifenmiao.ai.component

import com.arkivanov.decompose.ComponentContext
import com.google.gson.Gson
import com.shifenmiao.ai.mediator.MessageRemoteMediator
import com.shifenmiao.ai.agent.tool.AgentToolRegistry
import com.shifenmiao.ai.service.AgentCreationService
import com.shifenmiao.ai.service.AgentSavedResult
import com.shifenmiao.ai.service.CreationMetaService
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
import com.shifenmiao.model.ai.Agent
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
import com.wanbaohe.a2ui.catalog.A2uiRenderProvider
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
 * 创建AI应用组件
 *
 * 职责：
 * 1. 组合系统提示词 + 用户描述 → 发送给AI
 * 2. 流式接收AI返回的JSON → 累积并解析为 [Agent]
 * 3. 草稿箱持久化管理（FeatureDatabase.data_draft）
 * 4. 成功后可保存为 AgentEntity（AppDatabase.agent）
 */
class CreateAIAgentComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val editDraftId: Long?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted("navigate") val onNavigate: (Screen) -> Unit,
    @Assisted("navigateReplacingCurrent") val onNavigateReplacingCurrent: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
    resourceManager: ResourceManager,
    appDatabase: AppDatabase,
    private val dataDraftDao: DataDraftDao,
    private val dataDraftHelper: DataDraftHelper,
    val aiEngineManager: AIEngineManager,
    val aiEngineCatalogManager: AIEngineCatalogManager,
    private val agentToolRegistry: AgentToolRegistry,
    private val creationMetaService: CreationMetaService,
    private val agentCreationService: AgentCreationService,
    private val sensitiveWordChecker: SensitiveWordChecker,
    private val messageRemoteMediator: MessageRemoteMediator,
    private val gson: Gson,
    val a2uiRenderProvider: A2uiRenderProvider
) : BaseComponent(dispatchersHolder, componentContext), ResourceManager by resourceManager {

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            editDraftId: Long?,
            onGoBack: () -> Unit,
            @Assisted("navigate") onNavigate: (Screen) -> Unit,
            @Assisted("navigateReplacingCurrent") onNavigateReplacingCurrent: (Screen) -> Unit,
        ): CreateAIAgentComponent
    }

    // ── UI 状态 ────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow(CreateAIAgentUiState())
    val uiState: StateFlow<CreateAIAgentUiState> = _uiState.asStateFlow()

    private val _drafts = MutableStateFlow<List<DataDraftEntity>>(emptyList())
    val drafts: StateFlow<List<DataDraftEntity>> = _drafts.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _availableTools = MutableStateFlow<List<ToolCatalogItem>>(emptyList())
    val availableTools: StateFlow<List<ToolCatalogItem>> = _availableTools.asStateFlow()

    private val categoryDao = appDatabase.categoryDao()

    private var generateJob: Job? = null

    // Gson 由 Hilt 注入，无需每次创建实例

    /** 对话历史记录，用于多轮对话（assistant回复 + 用户追加修改） */
    private val conversationHistory = mutableListOf<MessageEntity>()

    /** 用于匹配 CodeEditor 回传结果的草稿 ID */
    private var editorDraftId: Long = 0L

    private var editorTarget: AgentEditorTarget = AgentEditorTarget.AGENT_BODY

    private var lastSavedAgentId: Int? = null
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
        restoreInitialDraftIfNeeded()
    }

    private fun restoreInitialDraftIfNeeded() {
        val targetDraftId = editDraftId ?: return
        componentScope.launch(Dispatchers.IO) {
            dataDraftHelper.getById(targetDraftId)?.let(::loadDraft)
        }
    }

    // ── 草稿箱 ─────────────────────────────────────────────────────────

    private fun observeDrafts() {
        dataDraftDao.observeAllByType(ListItemType.AGENT.id)
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
        lastSavedAgentId = null
        lastSavedSignature = null
        _uiState.update {
            CreateAIAgentUiState() // 全部恢复默认值
        }
    }

    /** 加载草稿到编辑区，完整刷新UI（包括预览、输入文本等） */
    fun loadDraft(draft: DataDraftEntity) {
        generateJob?.cancel()
        generateJob = null
        conversationHistory.clear()
        val parsedAgent = tryParseAgent(draft.data)
        val restoredStatus = when {
            draft.status == DataDraftEntity.STATUS_SUCCESS && parsedAgent != null -> GenerationStatus.SUCCESS
            draft.data.isNotBlank() && draft.status != DataDraftEntity.STATUS_DRAFT -> GenerationStatus.FAILED
            else -> GenerationStatus.IDLE
        }
        val restoredCategoryIds = DataDraftHelper.decodeCategoryIds(draft.selectedCategoryIds)
        val restoredToolNames = decodeDraftToolNames(draft.url)
        if (restoredStatus == GenerationStatus.SUCCESS && draft.data.isNotBlank()) {
            conversationHistory.add(
                MessageEntity(
                    completionId = "draft_user_${draft.id}",
                    conversationId = "create_agent_draft",
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
                    conversationId = "create_agent_draft",
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
                parsedAgent = parsedAgent,
                status = restoredStatus,
                currentDraftId = draft.id,
                errorMessage = null,
                isSaving = false,
                conversationRound = if (restoredStatus == GenerationStatus.SUCCESS) 1 else 0,
                selectedCategoryIds = restoredCategoryIds,
                selectedToolNames = restoredToolNames,
            )
        }
        val savedAgentId = draft.relatedEntityId?.takeIf { it > 0 }
        lastSavedAgentId = savedAgentId
        lastSavedSignature = if (savedAgentId != null) {
            buildCurrentAgentSignature()
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
        val linkedAgentId = currentDraft?.relatedEntityId?.takeIf { it > 0 } ?: lastSavedAgentId
        val signature = buildCurrentAgentSignature()
        return when {
            state.isSaving || state.status == GenerationStatus.STREAMING -> true
            state.inputText.isNotBlank() -> true
            signature == null -> false
            linkedAgentId == null -> true
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

    /** 将当前 Agent JSON 存入 EditorDataStore，并跳转到 AgentJsonEditor */
    fun editAgentInCodeEditor() {
        val agent = _uiState.value.parsedAgent ?: return
        val json = agent.dynamicBody ?: return
        componentScope.launch(Dispatchers.IO) {
            val draftId = EditorDataStore.put(dataDraftHelper, json)
            editorDraftId = draftId
            editorTarget = AgentEditorTarget.AGENT_BODY
            withContext(Dispatchers.Main) {
                onNavigate(
                    Screen.AgentJsonEditor(
                        editDraftId = draftId,
                        editTitle = agent.title ?: getString(R.string.create_ai_agent_edit)
                    )
                )
            }
        }
    }

    fun editRawResultInCodeEditor() {
        val rawResult = _uiState.value.rawJson.trim()
        if (rawResult.isEmpty()) return
        componentScope.launch(Dispatchers.IO) {
            val draftId = EditorDataStore.put(dataDraftHelper, rawResult)
            editorDraftId = draftId
            editorTarget = AgentEditorTarget.RAW_RESULT
            withContext(Dispatchers.Main) {
                onNavigate(
                    Screen.AgentJsonEditor(
                        editDraftId = draftId,
                        editTitle = getString(R.string.create_ai_agent_stream_raw_toggle)
                    )
                )
            }
        }
    }

    /** 接收 AgentJsonEditor 回传的编辑结果 */
    fun onEditorResult(event: EditorResultEvent) {
        if (event.editDraftId != editorDraftId) return
        when (editorTarget) {
            AgentEditorTarget.AGENT_BODY -> {
                val currentAgent = _uiState.value.parsedAgent ?: return
                val updatedAgent = currentAgent.copy(dynamicBody = event.text)
                val serialized = agentCreationService.serializeAgentPayload(updatedAgent)
                _uiState.update {
                    it.copy(
                        parsedAgent = updatedAgent,
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
                        generatedTitle = updatedAgent.title,
                    )
                    EditorDataStore.clear(dataDraftHelper, event.editDraftId)
                }
            }

            AgentEditorTarget.RAW_RESULT -> {
                componentScope.launch(Dispatchers.IO) {
                    val generationResult = agentCreationService.parseGenerationResult(
                        rawJson = event.text,
                        inputText = resolveFallbackInputText()
                    )
                    val agent = generationResult.payload.agent
                    val status = if (!agent?.dynamicBody.isNullOrBlank()) {
                        GenerationStatus.SUCCESS
                    } else {
                        GenerationStatus.FAILED
                    }
                    _uiState.update {
                        it.copy(
                            rawJson = generationResult.cleanedJson,
                            parsedAgent = agent ?: it.parsedAgent,
                            status = status,
                            errorMessage = generationResult.errorMessage,
                        )
                    }
                    if (status == GenerationStatus.SUCCESS) {
                        applySuggestedMeta(
                            inputText = resolveFallbackInputText(),
                            agent = agent,
                            aiSuggestedCategoryNames = generationResult.payload.suggestedCategoryNames,
                            aiSuggestedToolNames = generationResult.payload.suggestedToolNames
                        )
                    }
                    persistCurrentDraftContent(
                        description = resolveFallbackInputText(),
                        rawJson = generationResult.cleanedJson,
                        status = status,
                        generatedTitle = agent?.title,
                    )
                    EditorDataStore.clear(dataDraftHelper, event.editDraftId)
                }
            }
        }
    }

    /** 更新 Agent 标题 */
    fun updateAgentTitle(title: String) {
        val currentAgent = _uiState.value.parsedAgent ?: return
        val updatedAgent = currentAgent.copy(title = title)
        _uiState.update {
            it.copy(parsedAgent = updatedAgent)
        }
        persistStructuredAgentChanges(updatedAgent)
    }

    /** 更新 Agent 描述 */
    fun updateAgentDescription(description: String) {
        val currentAgent = _uiState.value.parsedAgent ?: return
        val updatedAgent = currentAgent.copy(description = description)
        _uiState.update {
            it.copy(parsedAgent = updatedAgent)
        }
        persistStructuredAgentChanges(updatedAgent)
    }

    // ── 生成 ───────────────────────────────────────────────────────────

    /** 取消正在进行的生成 */
    fun cancelGeneration() {
        generateJob?.cancel()
        generateJob = null
        _uiState.update {
            it.copy(
                status = when {
                    it.parsedAgent != null -> GenerationStatus.SUCCESS
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

                // 构建多轮消息列表：历史对话 + 本次用户输入
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
                                    errorMessage = event.errorMessage.ifEmpty { getString(R.string.create_ai_agent_error_service_unavailable) }
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

                // 如果flow正常结束但isEnd没标记
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
                        errorMessage = getString(R.string.create_ai_agent_error_network)
                    )
                }
            }
        }
    }

    /** 流结束后解析JSON并持久化草稿 */
    private suspend fun onStreamEnd(rawJson: String, description: String) {
        val generationResult = agentCreationService.parseGenerationResult(
            rawJson = rawJson,
            inputText = description
        )
        val cleanedJson = generationResult.cleanedJson
        val agentPayload = generationResult.payload
        val agent = agentPayload.agent
        val status = if (!agent?.dynamicBody.isNullOrBlank()) {
            GenerationStatus.SUCCESS
        } else {
            GenerationStatus.FAILED
        }
        val errorMsg = generationResult.errorMessage

        _uiState.update {
            it.copy(
                rawJson = cleanedJson,
                parsedAgent = agent ?: it.parsedAgent,
                status = status,
                errorMessage = errorMsg,
                conversationRound = if (status == GenerationStatus.SUCCESS) it.conversationRound + 1 else it.conversationRound,
                inputText = if (status == GenerationStatus.SUCCESS) "" else it.inputText,
            )
        }

        if (status == GenerationStatus.SUCCESS) {
            applySuggestedMeta(
                inputText = description,
                agent = agent,
                aiSuggestedCategoryNames = agentPayload.suggestedCategoryNames,
                aiSuggestedToolNames = agentPayload.suggestedToolNames
            )
        }

        persistCurrentDraftContent(
            description = description,
            rawJson = cleanedJson,
            status = status,
            generatedTitle = agent?.title,
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
        val newId = agentCreationService.saveDraft(
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
     * 在保存前对 Agent 标题进行敏感词检测。
     * - 远端开关未启用 / 标题为空 → 返回 null (放行)
     * - 命中 → 返回 [SensitiveWordCheckOutcome.Hit] (调用方负责拦截)
     * - 网络/接口异常 → 返回 null (fail-open, 放行)
     */
    private suspend fun checkTitleForSensitiveWords(
        title: String,
        scene: String,
    ): SensitiveWordCheckOutcome.Hit? {
        if (RemoteConfigStorage.getRemoteConfig().enableAgentSensitiveCheck != true) return null
        if (title.isBlank()) return null
        return when (val outcome = sensitiveWordChecker.check(
            scene = scene,
            fields = listOf(SensitiveWordCheckField(key = "title", text = title))
        )) {
            is SensitiveWordCheckOutcome.Hit -> outcome
            else -> null
        }
    }

    // ── 保存到 AgentEntity + ItemEntity ────────────────────────────────

    /**
     * 将生成成功的Agent保存到正式数据库，同时创建对应的ItemEntity并关联分类。
     */
    fun saveAgent(
        onSuccess: (AgentSavedResult) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        if (_uiState.value.isSaving) return
        val agent = _uiState.value.parsedAgent
        if (agent?.dynamicBody.isNullOrBlank()) {
            onFailure(getString(R.string.create_ai_agent_error_no_agent))
            return
        }
        val fallbackInputText = resolveFallbackInputText()
        val finalAgent = agent.copy(
            id = 0,
            title = if (agent.title.isNullOrEmpty()) fallbackInputText.take(30) else agent.title,
            description = if (agent.description.isNullOrEmpty()) fallbackInputText else agent.description,
        )

        _uiState.update { it.copy(isSaving = true) }
        componentScope.launch(Dispatchers.IO) {
            try {
                val hit = checkTitleForSensitiveWords(
                    title = finalAgent.title.orEmpty(),
                    scene = "agent_create_save",
                )
                if (hit != null) {
                    val message = hit.message.ifBlank { getString(R.string.create_ai_agent_sensitive_blocked) }
                    withContext(Dispatchers.Main) {
                        onFailure(message)
                    }
                    return@launch
                }
                val savedResult = agentCreationService.saveAgent(
                    parsedAgent = finalAgent,
                    fallbackInputText = fallbackInputText,
                    selectedCategoryIds = _uiState.value.selectedCategoryIds,
                    selectedToolNames = _uiState.value.selectedToolNames,
                    draftId = _uiState.value.currentDraftId
                )
                lastSavedAgentId = savedResult.agent.id
                lastSavedSignature = buildCurrentAgentSignature()

                withContext(Dispatchers.Main) {
                    onSuccess(savedResult)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(getString(R.string.create_ai_agent_error_save, e.message.orEmpty()))
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
        agent: Agent?,
        aiSuggestedCategoryNames: List<String> = emptyList(),
        aiSuggestedToolNames: List<String> = emptyList()
    ) {
        val suggestion = agentCreationService.resolveSuggestedMeta(
            inputText = inputText,
            agent = agent,
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
     * 对 Agent 标题做敏感词校验，校验通过后将预览数据以 [Source.PREVIEW] 写入数据库，
     * 再返回带真实本地 ID 的 Agent 对象用于预览导航。
     * 使用独立的 PREVIEW 来源，避免与 Source.LOCAL 的真实 Agent 记录冲突。
     */
    fun saveAndPreview(
        onReady: (Agent) -> Unit,
        onBlock: (String) -> Unit = {},
        onFailure: (String) -> Unit = {},
    ) {
        val agent = _uiState.value.parsedAgent ?: return
        val fallbackInputText = resolveFallbackInputText()
        val finalAgent = agent.copy(
            title = if (agent.title.isNullOrEmpty()) fallbackInputText.take(30) else agent.title,
            description = if (agent.description.isNullOrEmpty()) fallbackInputText else agent.description,
        )
        componentScope.launch(Dispatchers.IO) {
            try {
                val hit = checkTitleForSensitiveWords(
                    title = finalAgent.title.orEmpty(),
                    scene = "agent_preview",
                )
                if (hit != null) {
                    val message = hit.message.ifBlank { getString(R.string.create_ai_agent_sensitive_blocked) }
                    withContext(Dispatchers.Main) { onBlock(message) }
                    return@launch
                }
                val readyAgent = agentCreationService.savePreviewAgent(
                    parsedAgent = finalAgent,
                    fallbackInputText = fallbackInputText,
                    selectedToolNames = _uiState.value.selectedToolNames
                )
                withContext(Dispatchers.Main) {
                    onReady(readyAgent)
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                withContext(Dispatchers.Main) {
                    onFailure(
                        getString(R.string.create_ai_agent_error_preview, e.message.orEmpty())
                    )
                }
            }
        }
    }

    // ── JSON解析辅助 ───────────────────────────────────────────────────

    private fun tryParseAgent(json: String): Agent? {
        return agentCreationService.parseGenerationResult(
            rawJson = json,
            inputText = _uiState.value.inputText
        ).payload.agent
    }
    // ── 构建AI请求 ─────────────────────────────────────────────────────

    /** 系统提示词：告知AI生成符合规范的JSON */
    private suspend fun buildSystemPrompt(): String {
        return agentCreationService.buildSystemPrompt()
    }

    private fun buildConversation(systemPrompt: String): Conversation {
        val engine = aiEngineManager.currentAIEngine.value
        return Conversation(
            id = "create_agent_${Date().time}",
            title = getString(R.string.create_ai_agent_conversation_title),
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

    private fun persistStructuredAgentChanges(agent: Agent) {
        val currentDraftId = _uiState.value.currentDraftId ?: return
        componentScope.launch(Dispatchers.IO) {
            dataDraftHelper.updateDraft(
                draftId = currentDraftId,
                title = agent.title,
                data = agentCreationService.serializeAgentPayload(agent),
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
            .ifBlank { currentState.parsedAgent?.title.orEmpty().trim() }
            .ifBlank { currentState.parsedAgent?.description.orEmpty().trim() }
    }

    private fun decodeDraftToolNames(raw: String): Set<String> = try {
        gson.fromJson(raw, Array<String>::class.java)?.toSet().orEmpty()
    } catch (_: Exception) {
        emptySet()
    }

    private fun buildCurrentAgentSignature(): String? {
        val state = _uiState.value
        val agent = state.parsedAgent
        if (
            agent == null &&
            state.rawJson.isBlank() &&
            state.inputText.isBlank() &&
            state.selectedCategoryIds.isEmpty() &&
            state.selectedToolNames.isEmpty()
        ) {
            return null
        }
        return listOf(
            agent?.title.orEmpty(),
            agent?.description.orEmpty(),
            agent?.prompt.orEmpty(),
            agent?.dynamicBody.orEmpty(),
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

private enum class AgentEditorTarget {
    AGENT_BODY,
    RAW_RESULT,
}

/** 生成状态 */
enum class GenerationStatus {
    IDLE,       // 空闲
    STREAMING,  // 流式生成中
    SUCCESS,    // 生成成功
    FAILED,     // 生成失败
}

/** UI状态数据类 */
data class CreateAIAgentUiState(
    val inputText: String = "",
    val rawJson: String = "",
    val reasoningText: String = "",
    val parsedAgent: Agent? = null,
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
