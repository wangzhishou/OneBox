package com.shifenmiao.ai.component

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.shifenmiao.ai.history.withHistorySnapshot
import com.halilibo.richtext.markwon.MarkdownAstNodeParser
import com.shifenmiao.ai.export.DuelHtmlExporter
import com.shifenmiao.ai.mediator.MessageRemoteMediator
import com.shifenmiao.model.moderation.SensitiveWordCheckField
import com.shifenmiao.network.service.SensitiveWordCheckOutcome
import com.shifenmiao.network.service.SensitiveWordChecker
import com.shifenmiao.ai.model.AIDuelConfig
import com.shifenmiao.ai.model.AIDuelConfigCodec
import com.shifenmiao.ai.model.AIDuelState
import com.shifenmiao.ai.model.DuelSpeaker
import com.shifenmiao.ai.model.MessageUiModel
import com.shifenmiao.ai.prompt.PromptManager
import com.wanbaohe.a2ui.catalog.A2uiRenderProvider
import com.shifenmiao.ai.repository.ConversationRepository
import com.shifenmiao.ai.repository.MessageRepository
import com.shifenmiao.ai.usecase.MessageListUseCase
import com.shifenmiao.ai.utils.AiUtils
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.common.manager.AIEngineSyncManager
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.core.R
import com.shifenmiao.core.constants.Constants.TYPE_DELAY
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.ai.entity.ConversationEntity
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.ChatCompletionChunk
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.MessageUIState
import com.shifenmiao.model.ai.RoleType
import com.shifenmiao.model.ai.SearchCitation
import com.shifenmiao.model.ai.SearchResult
import com.shifenmiao.model.ai.Usage
import com.shifenmiao.model.ai.unified.LlmStreamEvent
import com.shifenmiao.model.channel.FlavorType
import com.shifenmiao.model.state.PageState
import com.shifenmiao.storage.AppSharedStorage
import com.shifenmiao.storage.RemoteConfigStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.resource.ResourceManager
import com.t8rin.logger.makeLog
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Date
import kotlin.coroutines.resume
import kotlin.math.ceil

private data class DuelPromptTemplates(
    @SerializedName("roleNameInstruction") val roleNameInstruction: String = "",
    @SerializedName("responseRules") val responseRules: String = "",
    @SerializedName("seedMessage") val seedMessage: String = "",
    @SerializedName("continuePrompt") val continuePrompt: String = "",
    @SerializedName("systemInstruction") val systemInstruction: String = "",
    @SerializedName("firstTurnInstruction") val firstTurnInstruction: String = "",
    @SerializedName("followUpInstruction") val followUpInstruction: String = "",
    @SerializedName("responseEnvelope") val responseEnvelope: String = "",
    @SerializedName("opponentLineLabel") val opponentLineLabel: String = "",
    // 旧版按模式分 key 的 map，仅用于兼容解析存量数据：扁平字段为空时回退读取
    @SerializedName("seedMessages") val legacySeedMessages: Map<String, String> = emptyMap(),
    @SerializedName("continuePrompts") val legacyContinuePrompts: Map<String, String> = emptyMap(),
    @SerializedName("modeSystemInstructions") val legacySystemInstructions: Map<String, String> = emptyMap(),
    @SerializedName("firstTurnInstructions") val legacyFirstTurnInstructions: Map<String, String> = emptyMap(),
    @SerializedName("followUpInstructions") val legacyFollowUpInstructions: Map<String, String> = emptyMap(),
    @SerializedName("responseEnvelopes") val legacyResponseEnvelopes: Map<String, String> = emptyMap(),
    @SerializedName("opponentLineLabels") val legacyOpponentLineLabels: Map<String, String> = emptyMap()
)

class AIDuelChatComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted conversationParams: Conversation,
    dispatchersHolder: DispatchersHolder,
    resourceManager: ResourceManager,
    private val appDatabase: AppDatabase,
    aiEngineManager: AIEngineManager,
    aiEngineCatalogManager: AIEngineCatalogManager,
    aiEngineSyncManager: AIEngineSyncManager,
    promptManager: PromptManager,
    conversationRepository: ConversationRepository,
    messageRepository: MessageRepository,
    private val messageRemoteMediator: MessageRemoteMediator,
    private val activityLogRecorder: ActivityLogRecorder,
    private val sensitiveWordChecker: SensitiveWordChecker,
    @ApplicationContext applicationContext: Context,
    messageListUseCase: MessageListUseCase,
    a2uiRenderProvider: A2uiRenderProvider,
    markdownAstNodeParserFactory: MarkdownAstNodeParser.Factory,
) : AIChatBaseComponent(
    componentContext,
    conversationParams,
    dispatchersHolder,
    resourceManager,
    aiEngineManager,
    aiEngineCatalogManager,
    aiEngineSyncManager,
    applicationContext,
    messageListUseCase,
    promptManager,
    conversationRepository,
    messageRepository,
    a2uiRenderProvider,
) {
    private companion object {
        const val RENDER_THROTTLE_MS = 16L
        const val TYPEWRITER_CHAR_DELAY_MS = 18L
        const val TYPEWRITER_IDLE_POLL_MS = 6L
        const val STREAM_INACTIVITY_TIMEOUT_MS = 20_000L
        const val TYPEWRITER_FAST_FORWARD_PENDING_CHARS = 4096
        const val TYPEWRITER_FAST_CHUNK_CHARS = 256
        const val TYPEWRITER_FAST_UI_UPDATE_MS = 48L
    }

    private val parser = markdownAstNodeParserFactory.create()
    private val messageDao = appDatabase.messageDao()
    private val chatPromptDao = appDatabase.chatPromptDao()
    private val categoryDao = appDatabase.categoryDao()
    private val defaultCategory = Category(id = 0, name = "全部", canEdit = false)

    private var _messageUiModels = MutableStateFlow<List<MessageUiModel>>(emptyList())
    val messageUiModels: StateFlow<List<MessageUiModel>> = _messageUiModels

    private val cachedMessageUiModels = mutableMapOf<String, List<MessageUiModel>>()
    private var lastRenderTime = 0L

    private var duelPromptTemplates: DuelPromptTemplates? = null

    private val _duelConfig = MutableStateFlow(AIDuelConfig())
    val duelConfig: StateFlow<AIDuelConfig> = _duelConfig

    private val _duelState = MutableStateFlow(AIDuelState())
    val duelState: StateFlow<AIDuelState> = _duelState

    private val _promptCategories = MutableStateFlow(listOf(defaultCategory))
    val promptCategories: StateFlow<List<Category>> = _promptCategories

    private val availableEngines = allEngines

    fun promptListFlow(categoryId: Int) = if (categoryId == 0) {
        chatPromptDao.getAllPromptsFlow()
    } else {
        chatPromptDao.getPromptsByCategoryId(categoryId, com.shifenmiao.model.ListItemType.PROMPT.id)
    }

    private var duelJob: Job? = null

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            conversation: Conversation
        ): AIDuelChatComponent
    }

    init {
        componentContext.lifecycle.doOnDestroy {
            onDestroy()
        }
        componentScope.launch {
            AppSharedStorage.isExpandedReasoningChat.collectLatest {
                synchronized(cachedMessageUiModels) { cachedMessageUiModels.clear() }
                renderMessage(forceUpdate = true)
            }
        }
    }

    override fun initReady() {
        componentScope.launch {
            if (_conversation.value.showLastMessage) {
                loadConversationFromDbIfNeeded(_conversation.value.id)
            } else {
                loadLastDuelDraftIfAvailable()
            }
            ensureDefaultEngines()
            loadPromptCategories()
            loadMessages()
        }
    }

    private suspend fun loadLastDuelDraftIfAvailable() {
        withContext(ioDispatcher) {
            val entity = appDatabase.conversationDao()
                .getLastHiddenConversationByType(AIConversationEntryType.DUEL.name)
            if (entity != null) {
                val conversation = ConversationEntity.toConversation(entity)
                _conversation.value = conversation
                AIDuelConfigCodec.decodeOrNull(conversation.prompt)?.let(_duelConfig::tryEmit)
            } else {
                _conversation.value = _conversation.value.copy(
                    entryType = AIConversationEntryType.DUEL,
                    appTitle = applicationContext.getString(R.string.ai_duel_chat_title),
                    title = "",
                    historyVisible = false,
                )
            }
        }
    }

    private suspend fun loadConversationFromDbIfNeeded(conversationId: String) {
        withContext(ioDispatcher) {
            val entity =
                appDatabase.conversationDao().getConversationByConversationId(conversationId)
            entity?.let {
                val conversation = ConversationEntity.toConversation(it)
                _conversation.value = conversation
                AIDuelConfigCodec.decodeOrNull(conversation.prompt)?.let(_duelConfig::tryEmit)
            }
        }
    }

    private suspend fun ensureDefaultEngines() {
        val engines = availableEngines.value.takeIf { it.isNotEmpty() }
            ?: availableEngines.first { it.isNotEmpty() }
        val duelEngineA = aiEngineManager.getDuelEngineA()
        val duelEngineB = aiEngineManager.getDuelEngineB()
        val engineA = _duelConfig.value.engineA ?: duelEngineA
        val engineB = _duelConfig.value.engineB
            ?: duelEngineB.takeIf { it.name != engineA.name }
            ?: engines.firstOrNull()
            ?: _conversation.value.engine
        updateDraftConfig(
            _duelConfig.value.copy(engineA = engineA, engineB = engineB),
            persistDraft = false
        )
    }

    private fun loadPromptCategories() {
        componentScope.launch(ioDispatcher) {
            categoryDao.getAllCategories().collectLatest { categories ->
                _promptCategories.value = listOf(defaultCategory) + categories
            }
        }
    }

    private suspend fun loadDuelPromptTemplates(): DuelPromptTemplates {
        duelPromptTemplates?.let { return it }
        val entity = withContext(ioDispatcher) {
            chatPromptDao.getSystemPromptByKey(PromptEntity.SYSTEM_PROMPT_KEY_DUEL_TEMPLATES)
        }
        val templates = entity?.prompt?.takeIf { it.isNotBlank() }?.let { json ->
            kotlin.runCatching { Gson().fromJson(json, DuelPromptTemplates::class.java) }
                .onFailure { error -> makeLog { "Failed to parse duel prompt templates: ${error.message}" } }
                .getOrNull()
        }
        val result = templates ?: DuelPromptTemplates()
        duelPromptTemplates = result
        return result
    }

    private fun String.replaceDuelPlaceholders(
        currentName: String = "",
        opponentName: String = ""
    ): String {
        return this
            .replace("{currentName}", currentName)
            .replace("{opponentName}", opponentName)
    }

    private fun getResponseRules(): String = duelPromptTemplates?.responseRules ?: ""

    private fun getRoleNameInstruction(roleName: String): String {
        return duelPromptTemplates?.roleNameInstruction?.replace("{roleName}", roleName.trim()) ?: ""
    }

    // 以下 getter 均带旧模板兼容：扁平字段为空时回退到旧 map 的 DEBATE key，避免模板缺失时提示词全空

    private fun getSeedMessage(): String {
        return duelPromptTemplates?.seedMessage?.takeIf { it.isNotBlank() }
            ?: duelPromptTemplates?.legacySeedMessages?.get("DEBATE").orEmpty()
    }

    private fun getContinuePrompt(): String {
        return duelPromptTemplates?.continuePrompt?.takeIf { it.isNotBlank() }
            ?: duelPromptTemplates?.legacyContinuePrompts?.get("DEBATE").orEmpty()
    }

    private fun getSystemInstruction(
        currentName: String,
        opponentName: String
    ): String {
        val template = duelPromptTemplates?.systemInstruction?.takeIf { it.isNotBlank() }
            ?: duelPromptTemplates?.legacySystemInstructions?.get("DEBATE")
        return template?.replaceDuelPlaceholders(currentName, opponentName) ?: ""
    }

    private fun getFirstTurnInstruction(
        currentName: String,
        opponentName: String
    ): String {
        val template = duelPromptTemplates?.firstTurnInstruction?.takeIf { it.isNotBlank() }
            ?: duelPromptTemplates?.legacyFirstTurnInstructions?.get("DEBATE")
        return template?.replaceDuelPlaceholders(currentName, opponentName) ?: ""
    }

    private fun getFollowUpInstruction(
        currentName: String,
        opponentName: String
    ): String {
        val template = duelPromptTemplates?.followUpInstruction?.takeIf { it.isNotBlank() }
            ?: duelPromptTemplates?.legacyFollowUpInstructions?.get("DEBATE")
        return template?.replaceDuelPlaceholders(currentName, opponentName) ?: ""
    }

    private fun getResponseEnvelope(
        currentName: String,
        opponentName: String
    ): String {
        val template = duelPromptTemplates?.responseEnvelope?.takeIf { it.isNotBlank() }
            ?: duelPromptTemplates?.legacyResponseEnvelopes?.get("DEBATE")
        return template?.replaceDuelPlaceholders(currentName, opponentName) ?: ""
    }

    private fun getOpponentLineLabel(): String {
        return duelPromptTemplates?.opponentLineLabel?.takeIf { it.isNotBlank() }
            ?: duelPromptTemplates?.legacyOpponentLineLabels?.get("DEBATE").orEmpty()
    }

    private var observeMessagesJob: Job? = null

    private fun observeMessages(conversationId: String) {
        observeMessagesJob?.cancel()
        observeMessagesJob = componentScope.launch(ioDispatcher) {
            messageDao.getMessagesFlowPaged(conversationId, limit = 50)
                .catch { e -> showFailureUI(e.message) }
                .collectLatest { messageList ->
                    if (conversationId != _conversation.value.id) return@collectLatest

                    // 快速指纹比较，避免 Room 重复发射时无意义的全量清缓存 + 重解析
                    val oldFingerprint = synchronized(messages) {
                        if (messages.isEmpty()) "" else "${messages.size}_${messages.last().completionId}"
                    }
                    val newFingerprint = if (messageList.isEmpty()) "" else "${messageList.size}_${messageList.last().completionId}"
                    if (oldFingerprint == newFingerprint) return@collectLatest

                    synchronized(messages) {
                        messages.clear()
                        messages.addAll(messageList)
                    }
                    renderMessage(forceUpdate = true)
                    if (messageList.isEmpty()) {
                        markPageReady()
                    } else {
                        showSuccessUI()
                    }
                }
        }
    }

    private fun renderMessage(forceUpdate: Boolean = false) {
        val currentTime = System.currentTimeMillis()
        if (!forceUpdate && currentTime - lastRenderTime < RENDER_THROTTLE_MS) return
        lastRenderTime = currentTime
        doRenderMessage()
    }

    private fun doRenderMessage() {
        lastRenderTime = System.currentTimeMillis()
        val showExpandedReasoning = AppSharedStorage.isExpandedReasoningChat.value

        // 按 (completionId, role) 去重：DB 中可能存在重复 completionId 的记录
        // （如错误后重试时服务端返回相同 chatCompletionChunk.id），会导致 LazyColumn key 冲突。
        val messagesCopy = synchronized(messages) {
            messages.toList()
        }.groupBy { "${it.completionId}_${it.role}" }
            .mapValues { (_, msgs) -> msgs.maxByOrNull { it.id }!! }
            .values
            .sortedByDescending { it.createdAt }
            .toList()

        val newUiModels = messagesCopy.flatMap { message ->
            val cacheKey = "${message.id}_${message.completionId}_${message.uId}_${if (showExpandedReasoning) 1 else 0}"
            val cached = if (message.uId > 0 && message.id > 0) {
                synchronized(cachedMessageUiModels) { cachedMessageUiModels[cacheKey] }
            } else {
                null
            }
            if (cached != null) return@flatMap cached

            val uiModels = MessageUiModel.fromMessage(
                parser = parser,
                message = message,
                conversation = _conversation.value,
                getAIModel = { modelName ->
                    aiEngineCatalogManager.getAiModelTitleByModel(modelName)
                },
                showExpandedReasoning = showExpandedReasoning
            )
            if (message.uId > 0 && message.id > 0) {
                synchronized(cachedMessageUiModels) { cachedMessageUiModels[cacheKey] = uiModels }
            }
            uiModels
        }
        _messageUiModels.value = newUiModels
    }

    override fun loadMessages() {
        if (_conversation.value.id.isNotEmpty()) {
            observeMessages(_conversation.value.id)
        }
    }

    override fun clearMessages() {
        synchronized(messages) { messages.clear() }
        synchronized(cachedMessageUiModels) { cachedMessageUiModels.clear() }
        _messageUiModels.value = emptyList()
    }

    fun updateDraftConfig(config: AIDuelConfig, persistDraft: Boolean = true) {
        if (_duelState.value.running) return
        val appTitle = buildConversationTitle(config)
        _duelConfig.value = config

        config.engineA?.let(aiEngineManager::setDuelEngineA)
        config.engineB?.let(aiEngineManager::setDuelEngineB)

        _conversation.value = _conversation.value.copy(
            entryType = AIConversationEntryType.DUEL,
            appTitle = appTitle,
            title = "",
            prompt = AIDuelConfigCodec.encode(config),
            placeholder = applicationContext.getString(R.string.ai_duel_chat_description),
            historyVisible = false,
        ).withHistorySnapshot(
            defaultTitle = applicationContext.getString(R.string.ai_duel_chat_title),
            assistantMessage = appTitle,
            timestamp = System.currentTimeMillis()
        )
        synchronized(cachedMessageUiModels) { cachedMessageUiModels.clear() }
        renderMessage(forceUpdate = true)

        if (!persistDraft) return

        if (shouldPersistDraftConfig(config)) {
            componentScope.launch(ioDispatcher) {
                appDatabase.conversationDao()
                    .insertReplace(ConversationEntity.fromConversation(_conversation.value))
            }
        } else if (!_conversation.value.historyVisible) {
            val conversationId = _conversation.value.id
            componentScope.launch(ioDispatcher) {
                appDatabase.conversationDao().deleteConversationByConversationId(conversationId)
            }
        }
    }

    private fun shouldPersistDraftConfig(config: AIDuelConfig): Boolean {
        if (config.hasMeaningfulDraftContent()) return true

        val conversation = _conversation.value
        return conversation.messageCount > 0 ||
            conversation.lastMessagePreview.isNotBlank() ||
            conversation.lastUserMessagePreview.isNotBlank() ||
            synchronized(messages) { messages.isNotEmpty() }
    }

    private fun AIDuelConfig.hasMeaningfulDraftContent(): Boolean {
        return personaA.isNotBlank() ||
            personaB.isNotBlank() ||
            roleNameA.isNotBlank() ||
            roleNameB.isNotBlank() ||
            avatarA.isNotBlank() ||
            avatarB.isNotBlank() ||
            promptIdA > 0 ||
            promptIdB > 0 ||
            promptNameA.isNotBlank() ||
            promptNameB.isNotBlank()
    }

    fun applyPromptToRole(role: DuelSpeaker, prompt: PromptEntity) {
        if (_duelState.value.running) return
        // 显示名称为空时，用提示词标题填充
        val current = _duelConfig.value
        val updated = if (role == DuelSpeaker.A) {
            current.copy(
                personaA = prompt.prompt ?: "",
                promptIdA = prompt.id,
                promptNameA = prompt.title ?: "",
                roleNameA = current.roleNameA.ifBlank { prompt.title ?: "" }
            )
        } else {
            current.copy(
                personaB = prompt.prompt ?: "",
                promptIdB = prompt.id,
                promptNameB = prompt.title ?: "",
                roleNameB = current.roleNameB.ifBlank { prompt.title ?: "" }
            )
        }
        updateDraftConfig(updated)
    }

    fun startNewConversation() {
        _conversation.value = _conversation.value.copy(
            id = Date().time.toString(),
            entryType = AIConversationEntryType.DUEL,
            appTitle = applicationContext.getString(R.string.ai_duel_chat_title),
            title = "",
            prompt = AIDuelConfigCodec.encode(_duelConfig.value),
            historyVisible = false,
            lastMessagePreview = "",
            lastUserMessagePreview = "",
            lastActiveAt = System.currentTimeMillis(),
            messageCount = 0,
        )
        clearMessages()
        loadMessages()
    }

    fun startDuel(config: AIDuelConfig) {
        componentScope.launch(ioDispatcher) {
            stopDuelInternal(savePartial = false)
            val engines = availableEngines.value
            val duelEngineA = aiEngineManager.getDuelEngineA()
            val duelEngineB = aiEngineManager.getDuelEngineB()
            val defaultEngineA = config.engineA
                ?: _duelConfig.value.engineA
                ?: duelEngineA
            val defaultEngineB = config.engineB
                ?: _duelConfig.value.engineB
                ?: duelEngineB.takeIf { it.name != defaultEngineA.name }
                ?: engines.firstOrNull { it.name != defaultEngineA.name }
                ?: engines.firstOrNull()
                ?: _conversation.value.engine

            val resolvedConfig = config.copy(
                engineA = defaultEngineA,
                engineB = defaultEngineB
            )
            val validationError = validateDuelConfigForStart(resolvedConfig)
            if (validationError != null) {
                withContext(Dispatchers.Main) {
                    ActionUtils.showToast(applicationContext.getString(validationError))
                }
                return@launch
            }

            val sensitiveHit = checkDuelInputsForSensitiveWords(
                config = resolvedConfig,
                scene = "ai_duel_start"
            )
            if (sensitiveHit != null) {
                val message = sensitiveHit.message.ifBlank {
                    applicationContext.getString(R.string.ai_duel_sensitive_blocked)
                }
                withContext(Dispatchers.Main) {
                    ActionUtils.showToast(message)
                }
                return@launch
            }

            val appTitle = buildConversationTitle(resolvedConfig)
            val conversationId = _conversation.value.id.ifBlank { Date().time.toString() }

            _duelConfig.value = resolvedConfig
            _conversation.value = _conversation.value.copy(
                id = conversationId,
                entryType = AIConversationEntryType.DUEL,
                appTitle = appTitle,
                title = "",
                historyVisible = true,
                lastMessagePreview = "",
                lastUserMessagePreview = "",
                messageCount = 0,
                prompt = AIDuelConfigCodec.encode(resolvedConfig),
                placeholder = applicationContext.getString(R.string.ai_duel_chat_description)
            ).withHistorySnapshot(
                defaultTitle = applicationContext.getString(R.string.ai_duel_chat_title),
                assistantMessage = appTitle,
                timestamp = System.currentTimeMillis()
            )

            clearMessages()
            loadMessages()

            appDatabase.conversationDao()
                .insertReplace(ConversationEntity.fromConversation(_conversation.value))

            _duelState.value = _duelState.value.copy(
                running = true,
                round = 0,
                speaker = DuelSpeaker.A,
                errorMessage = ""
            )
            duelJob = componentScope.launch(ioDispatcher) {
                runDuelLoop()
            }
        }
    }

    private fun parseDuelConfigTextField(prompt: String, key: String): String {
        if (prompt.isBlank()) return ""
        return kotlin.runCatching {
            val obj = JsonParser.parseString(prompt).asJsonObject
            obj.get(key)?.takeIf { it.isJsonPrimitive }?.asString.orEmpty().trim()
        }.getOrNull().orEmpty()
    }

    suspend fun loadPersonaHistory(role: DuelSpeaker, limit: Int = 30): List<String> {
        return withContext(ioDispatcher) {
            val entities = appDatabase.conversationDao()
                .getVisibleConversationsByType(AIConversationEntryType.DUEL.name, limit)

            val dedup = LinkedHashSet<String>()
            for (entity in entities) {
                val prompt = entity.prompt
                if (prompt.isBlank()) continue
                val key = if (role == DuelSpeaker.A) "personaA" else "personaB"
                val persona = parseDuelConfigTextField(prompt, key)
                if (persona.isNotBlank()) dedup.add(persona)
            }
            dedup.toList()
        }
    }

    suspend fun loadRoleNameHistory(role: DuelSpeaker, limit: Int = 30): List<String> {
        return withContext(ioDispatcher) {
            val entities = appDatabase.conversationDao()
                .getVisibleConversationsByType(AIConversationEntryType.DUEL.name, limit)

            val dedup = LinkedHashSet<String>()
            for (entity in entities) {
                val prompt = entity.prompt
                if (prompt.isBlank()) continue
                val key = if (role == DuelSpeaker.A) "roleNameA" else "roleNameB"
                val roleName = parseDuelConfigTextField(prompt, key)
                if (roleName.isNotBlank()) dedup.add(roleName)
            }
            dedup.toList()
        }
    }

    /**
     * 导出 AI 互动记录为专用 HTML。
     *
     * 与通用 [exportChatHistory] 不同，这里会：
     * - 显示每条消息的角色名 + 模型名；
     * - 顶部展示双方人设卡片（可展开）；
     * - AIGC 元数据写入 HTML 注释。
     */
    fun exportDuelChatHistory(exportHtml: (String, String) -> Unit) {
        componentScope.launch {
            val exporter = DuelHtmlExporter()
            exportHtml(
                exporter.exportToHtml(
                    conversation = _conversation.value,
                    messages = synchronized(messages) { messages.toList() },
                    aiEngineCatalogManager = aiEngineCatalogManager,
                ),
                exporter.aIgcInfoString
            )
        }
    }

    fun stopDuel() {
        componentScope.launch(ioDispatcher) {
            stopDuelInternal(savePartial = true)
        }
    }

    private suspend fun stopDuelInternal(savePartial: Boolean) {
        duelJob?.cancel()
        duelJob = null

        val stoppedSuffix = duelStoppedSuffix()

        if (savePartial) {
            val placeholder = synchronized(messages) {
                messages.firstOrNull { it.id <= 0 && it.uId == MessageUIState.STREAMING.value }
            }
            val hasContent = placeholder?.let {
                if (it.role == RoleType.USER.value) it.question.isNotBlank() else it.answer.isNotBlank()
            } == true
            if (placeholder != null && hasContent) {
                kotlin.runCatching {
                    placeholder.uId = MessageUIState.NORMAL.value
                    messageDao.insertReplace(
                        if (placeholder.role == RoleType.USER.value) {
                            placeholder.copy(
                                id = 0,
                                createdAt = Date(),
                                question = placeholder.question + stoppedSuffix,
                                entryType = _conversation.value.entryType,
                                title = _conversation.value.title,
                            )
                        } else {
                            placeholder.copy(
                                id = 0,
                                createdAt = Date(),
                                answer = placeholder.answer + stoppedSuffix,
                                entryType = _conversation.value.entryType,
                                title = _conversation.value.title,
                            )
                        }
                    )
                }
            }
        }

        fetchJob?.cancel()
        fetchJob = null

        _duelState.value = _duelState.value.copy(running = false)
        _chatUIState.value = _chatUIState.value.copy(chatActive = false)
    }

    private suspend fun runDuelLoop() {
        val config = _duelConfig.value
        if (config.personaA.isBlank() || config.personaB.isBlank()) {
            withContext(Dispatchers.Main) {
                ActionUtils.showToast(applicationContext.getString(R.string.ai_error_unknown))
            }
            _duelState.value = _duelState.value.copy(running = false)
            return
        }

        loadDuelPromptTemplates()

        var round = 0

        duelLoop@ while (round < config.maxRounds) {
            if (!currentCoroutineContext().isActive) break

            for (speaker in listOf(DuelSpeaker.A, DuelSpeaker.B)) {
                if (!currentCoroutineContext().isActive) break@duelLoop
                try {
                    _duelState.value =
                        _duelState.value.copy(running = true, round = round, speaker = speaker)
                    val baseSpeakerPrompt = if (speaker == DuelSpeaker.A) config.personaA else config.personaB
                    val roleName = if (speaker == DuelSpeaker.A) config.roleNameA else config.roleNameB
                    val speakerPrompt = appendRoleNameInstruction(baseSpeakerPrompt, roleName)
                    val speakerEngine = if (speaker == DuelSpeaker.A) config.engineA else config.engineB
                    if (speakerEngine == null) {
                        val modelNotSelected = applicationContext.getString(R.string.ai_duel_error_model_not_selected)
                        _duelState.value =
                            _duelState.value.copy(running = false, errorMessage = modelNotSelected)
                        showFailureUI(modelNotSelected)
                        break@duelLoop
                    }

                    val canProceed = ensureLoginAndCheckPointsForRound(speakerEngine)
                    if (!canProceed) {
                        _duelState.value = _duelState.value.copy(running = false)
                        break@duelLoop
                    }
                    val isFirstTurn = synchronized(messages) { messages.none { parseTurn(it) != null } }
                    val requestConversation = _conversation.value.copy(
                        prompt = buildRoundPrompt(
                            speakerPrompt = speakerPrompt,
                            isFirstTurn = isFirstTurn,
                            speaker = speaker,
                            config = config,
                        ),
                        engine = speakerEngine
                    )

                    val requestMessages = buildRequestMessages(config, speaker)
                    val completionId = Date().time.toString()
                    val isSpeakerA = speaker == DuelSpeaker.A
                    val streaming = MessageEntity(
                        completionId = completionId,
                        conversationId = _conversation.value.id,
                        role = if (isSpeakerA) {
                            RoleType.USER.value
                        } else {
                            RoleType.ASSISTANT.value
                        },
                        question = "",
                        answer = "",
                        reasoningContent = "",
                        createdAt = Date(),
                        engine = speakerEngine.name,
                        model = speakerEngine.model.name,
                        entryType = _conversation.value.entryType,
                        title = _conversation.value.title,
                    ).also {
                        it.uId = MessageUIState.LOADING.value
                    }

                    updateStreamingPlaceholder(streaming, forceUpdate = true)
                    _chatUIState.value = _chatUIState.value.copy(chatActive = true)

                    val roundStartTimeMs = System.currentTimeMillis()
                    fetchJob = componentScope.launch(ioDispatcher) {
                        try {
                            streamWithTypewriter(
                                requestConversation = requestConversation,
                                requestMessages = requestMessages,
                                streaming = streaming,
                                speaker = speaker,
                                startTimeMs = roundStartTimeMs
                            )
                        } catch (e: Exception) {
                            if (e is CancellationException) {
                                val hasContent = if (streaming.role == RoleType.USER.value) {
                                    streaming.question.isNotBlank()
                                } else {
                                    streaming.answer.isNotBlank()
                                }
                                if (hasContent && streaming.uId == MessageUIState.STREAMING.value) {
                                    // 持久化前必须置为 NORMAL，否则下一轮会按 STREAMING 状态被误删。
                                    streaming.uId = MessageUIState.NORMAL.value
                                    messageDao.insertReplace(
                                        if (streaming.role == RoleType.USER.value) {
                                            streaming.copy(
                                                id = 0,
                                                createdAt = Date(),
                                                question = streaming.question + duelStoppedSuffix(),
                                                entryType = _conversation.value.entryType,
                                                title = _conversation.value.title,
                                            )
                                        } else {
                                            streaming.copy(
                                                id = 0,
                                                createdAt = Date(),
                                                answer = streaming.answer + duelStoppedSuffix(),
                                                entryType = _conversation.value.entryType,
                                                title = _conversation.value.title,
                                            )
                                        }
                                    )
                                }
                                streaming.uId = MessageUIState.NORMAL.value
                            } else {
                                _duelState.value = _duelState.value.copy(
                                    running = false,
                                    errorMessage = e.message.orEmpty()
                                )
                                showFailureUI(e.message)
                                // 持久化前必须置为 NORMAL，否则错误消息 uId 为 STREAMING，
                                // 后续会被 updateStreamingPlaceholder 误删。
                                streaming.uId = MessageUIState.NORMAL.value
                                messageDao.insertReplace(
                                    if (streaming.role == RoleType.USER.value) {
                                        streaming.copy(
                                            id = 0,
                                            createdAt = Date(),
                                            question = e.message.orEmpty(),
                                            entryType = _conversation.value.entryType,
                                            title = _conversation.value.title,
                                        )
                                    } else {
                                        streaming.copy(
                                            id = 0,
                                            createdAt = Date(),
                                            answer = e.message.orEmpty(),
                                            entryType = _conversation.value.entryType,
                                            title = _conversation.value.title,
                                        )
                                    }
                                )
                            }
                        }
                    }

                    fetchJob?.join()
                    fetchJob = null

                    if (!_duelState.value.running) break@duelLoop
                    makeLog {
                        "next"
                    }
                } catch (e: Exception) {
                    _duelState.value = _duelState.value.copy(
                        running = false,
                        errorMessage = e.message.orEmpty()
                    )
                    showFailureUI(e.message)
                    break@duelLoop
                }
            }

            round += 1
        }

        _duelState.value = _duelState.value.copy(running = false)
        kotlin.runCatching { insertDuelOperationHistoryIfNeeded() }
    }

    private fun appendRoleNameInstruction(prompt: String, roleName: String): String {
        val normalizedRoleName = roleName.trim()
        if (normalizedRoleName.isBlank()) return prompt

        val normalizedPrompt = prompt.trim()

        return buildString {
            append(getRoleNameInstruction(normalizedRoleName))
            if (normalizedPrompt.isNotBlank()) {
                append("\n\n")
                append(normalizedPrompt)
            }
        }
    }

    private suspend fun ensureLoginAndCheckPointsForRound(engine: com.shifenmiao.model.ai.AiEngine): Boolean {
        // Google 渠道: 自带 token 可直连的引擎免登录免积分直接聊
        if (FlavorType.fromName() == FlavorType.GOOGLE && engine.canChatDirectly()) {
            return true
        }
        // Google 渠道: 无代理路由时登录也无意义(网关无对应代理), 直接失败并提示去设置
        if (FlavorType.fromName() == FlavorType.GOOGLE && !engine.hasProxyRouteConfigured()) {
            ActionUtils.showError(R.string.ai_chat_engine_unavailable_toast)
            return false
        }
        val point = ceil(engine.model.basePoints).toInt().coerceAtLeast(0)
        return withTimeoutOrNull(15_000L) {
            withContext(Dispatchers.Main) {
                suspendCancellableCoroutine { cont ->
                    ActionUtils.ensureLoginAndCheckPoints(
                        source = "AI_DUEL",
                        point = point,
                        onLoginFailure = {
                            if (cont.isActive) cont.resume(false)
                        },
                        onPointsFailure = {
                            if (cont.isActive) cont.resume(false)
                        },
                        onSuccess = {
                            if (cont.isActive) cont.resume(true)
                        }
                    )
                }
            }
        } ?: false
    }

    private suspend fun streamWithTypewriter(
        requestConversation: Conversation,
        requestMessages: List<MessageEntity>,
        streaming: MessageEntity,
        speaker: DuelSpeaker,
        startTimeMs: Long
    ) = coroutineScope {
        if (chatUIState.value.pageState == PageState.ERROR) return@coroutineScope

        val bufferMutex = Mutex()
        val contentBuffer = StringBuilder()
        var contentCursor = 0
        val reasoningBuffer = StringBuilder()
        var reasoningCursor = 0
        val questionOut = StringBuilder(streaming.question)
        val answerOut = StringBuilder(streaming.answer)
        val reasoningOut = StringBuilder(streaming.reasoningContent)

        data class TypingBatch(
            val contentChunk: String,
            val reasoningChunk: String,
            val hasPending: Boolean,
            val fastForward: Boolean
        )

        var streamEnded = false
        var errorMsg: String? = null
        var lastChunkAtMs = System.currentTimeMillis()
        var lastUsage: Usage? = null

        val typingJob = launch {
            var lastUiUpdateAtMs = 0L
            while (isActive) {
                val (contentChunk, reasoningChunk, hasPending, fastForward) = bufferMutex.withLock {
                    val pendingChars =
                        (contentBuffer.length - contentCursor) + (reasoningBuffer.length - reasoningCursor)
                    val fast = pendingChars > TYPEWRITER_FAST_FORWARD_PENDING_CHARS
                    val maxChars = if (fast) TYPEWRITER_FAST_CHUNK_CHARS else 1

                    val contentEnd = (contentCursor + maxChars).coerceAtMost(contentBuffer.length)
                    val c = if (contentEnd > contentCursor) {
                        contentBuffer.substring(contentCursor, contentEnd)
                    } else {
                        ""
                    }
                    contentCursor = contentEnd

                    val reasoningEnd = (reasoningCursor + maxChars).coerceAtMost(reasoningBuffer.length)
                    val r = if (reasoningEnd > reasoningCursor) {
                        reasoningBuffer.substring(reasoningCursor, reasoningEnd)
                    } else {
                        ""
                    }
                    reasoningCursor = reasoningEnd

                    if (contentCursor > 2048) {
                        contentBuffer.delete(0, contentCursor)
                        contentCursor = 0
                    }
                    if (reasoningCursor > 2048) {
                        reasoningBuffer.delete(0, reasoningCursor)
                        reasoningCursor = 0
                    }

                    val pending =
                        (contentBuffer.length - contentCursor) + (reasoningBuffer.length - reasoningCursor)
                    val pendingNow = pending > 0
                    TypingBatch(c, r, pendingNow, fast)
                }

                val wroteSomething = contentChunk.isNotEmpty() || reasoningChunk.isNotEmpty()
                if (wroteSomething) {
                    if (streaming.uId == MessageUIState.LOADING.value) {
                        streaming.uId = MessageUIState.STREAMING.value
                    }
                    if (contentChunk.isNotEmpty()) {
                        if (speaker == DuelSpeaker.A) {
                            questionOut.append(contentChunk)
                            streaming.question = questionOut.toString()
                        } else {
                            answerOut.append(contentChunk)
                            streaming.answer = answerOut.toString()
                        }
                    }
                    if (reasoningChunk.isNotEmpty()) {
                        reasoningOut.append(reasoningChunk)
                        streaming.reasoningContent = reasoningOut.toString()
                        streaming.reasoningTime = ((System.currentTimeMillis() - startTimeMs) / 1000L)
                    }

                    val now = System.currentTimeMillis()
                    val shouldUpdateUi =
                        !fastForward || now - lastUiUpdateAtMs >= TYPEWRITER_FAST_UI_UPDATE_MS || (!hasPending && streamEnded)
                    if (shouldUpdateUi) {
                        updateStreamingPlaceholder(streaming)
                        lastUiUpdateAtMs = now
                    }

                    if (!fastForward) {
                        delay(TYPEWRITER_CHAR_DELAY_MS)
                    } else {
                        delay(1L)
                    }
                    continue
                }

                val hasPendingNow = bufferMutex.withLock {
                    contentCursor < contentBuffer.length || reasoningCursor < reasoningBuffer.length
                }
                if (!hasPendingNow && streamEnded) break
                delay(TYPEWRITER_IDLE_POLL_MS)
            }
        }

        val collectJob = launch {
            messageRemoteMediator.fetchAndSaveMessages(
                conversation = requestConversation,
                questionMessageEntityList = requestMessages,
                enableWebSearch = false
            ).collect { event ->
                lastChunkAtMs = System.currentTimeMillis()
                when (event) {
                    is LlmStreamEvent.Error -> {
                        errorMsg = event.errorMessage
                        streamEnded = true
                        this.cancel()
                    }

                    is LlmStreamEvent.SearchResultsEvent -> {
                        processSearchResults(
                            streaming = streaming,
                            searchResults = event.searchResults,
                            searchInfo = event.searchInfo
                        )
                    }

                    is LlmStreamEvent.UsageUpdated -> {
                        lastUsage = event.usage
                    }

                    is LlmStreamEvent.TextDelta -> {
                        bufferMutex.withLock { contentBuffer.append(event.text) }
                    }

                    is LlmStreamEvent.ReasoningDelta -> {
                        bufferMutex.withLock { reasoningBuffer.append(event.text) }
                    }

                    is LlmStreamEvent.Completed -> {
                        streamEnded = true
                        this.cancel()
                    }

                    is LlmStreamEvent.ResponseStarted,
                    is LlmStreamEvent.ToolCallDeltaEvent -> Unit
                }
            }
        }

        val watchdogJob = launch {
            while (isActive && !streamEnded) {
                val idleMs = System.currentTimeMillis() - lastChunkAtMs
                if (idleMs > STREAM_INACTIVITY_TIMEOUT_MS) {
                    errorMsg = applicationContext.getString(R.string.ai_duel_error_stream_timeout)
                    streamEnded = true
                    collectJob.cancel()
                    break
                }
                delay(500L)
            }
        }

        collectJob.join()
        streamEnded = true
        watchdogJob.cancel()
        typingJob.join()

        val finalErrorMsg = errorMsg
        if (!finalErrorMsg.isNullOrBlank()) {
            _duelState.value = _duelState.value.copy(running = false, errorMessage = finalErrorMsg)
            showFailureUI(finalErrorMsg)
            // 持久化前必须置为 NORMAL，否则错误消息 uId 为 STREAMING，
            // 后续会被 updateStreamingPlaceholder 误删。
            streaming.uId = MessageUIState.NORMAL.value
            val finalMessage = if (streaming.role == RoleType.USER.value) {
                streaming.copy(
                    id = 0,
                    createdAt = Date(),
                    question = finalErrorMsg,
                    entryType = _conversation.value.entryType,
                    title = _conversation.value.title,
                )
            } else {
                streaming.copy(
                    id = 0,
                    createdAt = Date(),
                    answer = finalErrorMsg,
                    entryType = _conversation.value.entryType,
                    title = _conversation.value.title,
                )
            }
            messageDao.insertReplace(finalMessage)
            return@coroutineScope
        }

        delay(TYPE_DELAY)
        val completionText = if (speaker == DuelSpeaker.A) streaming.question else streaming.answer
        val finalUsage = calculateUsageIfNull(
            usage = lastUsage,
            questionMessageEntityList = requestMessages,
            completionText = completionText
        )
        // 必须在 copy 前把状态置为 NORMAL，否则持久化到 DB 的消息 uId 为 STREAMING，
        // 下一轮 updateStreamingPlaceholder 会按 `uId <= STREAMING` 规则把上一轮消息误删。
        streaming.uId = MessageUIState.NORMAL.value
        val finalMessage = streaming.copy(
            id = 0,
            createdAt = Date(),
            entryType = _conversation.value.entryType,
            title = _conversation.value.title,
            promptTokens = finalUsage.promptTokens,
            completionTokens = finalUsage.completionTokens,
            totalTokens = finalUsage.totalTokens,
        )
        messageDao.insertReplace(finalMessage)
        consumePoints(
            engine = requestConversation.engine,
            messageEntity = finalMessage
        )
    }

    private fun calculateUsageIfNull(
        usage: Usage?,
        questionMessageEntityList: List<MessageEntity>,
        completionText: String
    ): Usage {
        if (usage != null && usage.totalTokens > 0) return usage
        val promptTokens = StringUtils.calculateTokens(
            AiUtils.concatenateQuestionsAndAnswers(questionMessageEntityList)
        )
        val completionTokens = StringUtils.calculateTokens(completionText)
        return Usage(
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            totalTokens = promptTokens + completionTokens
        )
    }

    private fun consumePoints(engine: com.shifenmiao.model.ai.AiEngine, messageEntity: MessageEntity) {
        if (messageEntity.totalTokens <= 0) return
        val conversation = _conversation.value.copy(engine = engine)
        if (AiUtils.isNotFree(conversation)) {
            BaseUtils.consumePointsByToken(
                messageEntity.totalTokens,
                conversation,
                desc = messageEntity.completionId
            )
        }
    }

    private suspend fun insertDuelOperationHistoryIfNeeded() {
        val conversationId = _conversation.value.id
        if (conversationId.isBlank()) return

        val messagesAsc = withContext(ioDispatcher) {
            messageDao.getMessagesByConversationId(conversationId)
                .filter { it.entryType == AIConversationEntryType.DUEL }
        }
        if (messagesAsc.isEmpty()) return

        val question = messagesAsc.firstOrNull {
            it.role == RoleType.USER.value && it.question.isNotBlank()
        } ?: messagesAsc.first()
        val answer = messagesAsc.last()
        if (question.id <= 0 || answer.id <= 0) return

        // 写入新的 activity_log 表
        activityLogRecorder.recordAiDuel(
            conversationId = conversationId,
            title = question.title.ifBlank { _conversation.value.appTitle },
            appTitle = _conversation.value.appTitle,
            description = question.question,
            questionId = question.id.toString(),
            answerId = answer.id.toString(),
            completionId = answer.completionId,
            timestamp = answer.createdAt
        )
    }

    /**
     * 处理搜索结果，支持多种 API 格式
     */
    private fun processSearchResults(
        streaming: MessageEntity,
        searchResults: List<SearchCitation>?,
        searchInfo: com.shifenmiao.model.ai.SearchInfo?
    ) {
        // 只处理一次搜索结果（通常在流的开始或结束返回）
        if (streaming.searchResults.isNotBlank()) {
            return
        }

        val citations = mutableListOf<SearchCitation>()

        // 方式1: 直接从 search_results 字段获取
        searchResults?.let { results ->
            citations.addAll(results)
        }

        // 方式2: 从百度千帆的 search_info 字段获取
        searchInfo?.searchResults?.let { baiduResults ->
            citations.addAll(baiduResults.map { it.toSearchCitation() })
        }

        // 如果有搜索结果，保存到消息实体
        if (citations.isNotEmpty()) {
            val searchResult = SearchResult(
                query = "",
                citations = citations.mapIndexed { index, citation ->
                    // 确保 index 正确
                    if (citation.index == 0) {
                        citation.copy(index = index + 1)
                    } else {
                        citation
                    }
                }
            )
            streaming.searchResults = searchResult.toJson()
        }
    }

    private fun updateStreamingPlaceholder(message: MessageEntity, forceUpdate: Boolean = false) {
        synchronized(messages) {
            // 只移除未完成的 placeholder（LOADING / STREAMING / ERROR）以及同 completionId 的旧副本。
            // 已流式结束但尚未拿到 DB 自增 id 的 placeholder（uId == NORMAL, id == 0）必须保留，
            // 否则下一轮发言会把上一轮的完整消息误删。
            messages.removeIf { it.uId < MessageUIState.NORMAL.value || it.completionId == message.completionId }
            messages.add(0, message)
        }
        renderMessage(forceUpdate = forceUpdate)
    }

    private fun buildRequestMessages(
        config: AIDuelConfig,
        speaker: DuelSpeaker
    ): List<MessageEntity> {
        val turnsAsc = synchronized(messages) { messages.toList() }
            .asReversed()
            .filter { it.id > 0 || it.uId == MessageUIState.NORMAL.value }
            .mapNotNull(::parseTurn)

        if (turnsAsc.isEmpty()) {
            return listOf(
                MessageEntity(
                    completionId = "seed",
                    conversationId = _conversation.value.id,
                    role = RoleType.USER.value,
                    question = getSeedMessage()
                )
            )
        }

        val result = mutableListOf<MessageEntity>()

        for (i in 0 until turnsAsc.size) {
            val (turnSpeaker, content) = turnsAsc[i]
            result += if (turnSpeaker == speaker) {
                MessageEntity(
                    completionId = "a_$i",
                    conversationId = _conversation.value.id,
                    role = RoleType.ASSISTANT.value,
                    answer = content
                )
            } else {
                MessageEntity(
                    completionId = "u_$i",
                    conversationId = _conversation.value.id,
                    role = RoleType.USER.value,
                    question = buildOpponentUserMessage(
                        opponentSpeaker = turnSpeaker,
                        opponentContent = content,
                        currentSpeaker = speaker,
                        config = config
                    )
                )
            }
        }

        val last = result.lastOrNull()
        if (last != null && last.role != RoleType.USER.value) {
            result += MessageEntity(
                completionId = "u_tail",
                conversationId = _conversation.value.id,
                role = RoleType.USER.value,
                question = getContinuePrompt()
            )
        }

        return result
    }

    private fun buildOpponentUserMessage(
        opponentSpeaker: DuelSpeaker,
        opponentContent: String,
        currentSpeaker: DuelSpeaker,
        config: AIDuelConfig,
    ): String {
        val normalizedContent = opponentContent.trim()
        if (normalizedContent.isBlank()) {
            return applicationContext.getString(R.string.ai_duel_continue)
        }

        val opponentName = resolveSpeakerDisplayName(config, opponentSpeaker)
        val currentName = resolveSpeakerDisplayName(config, currentSpeaker)
        return buildString {
            append(getResponseEnvelope(currentName, opponentName))
            append("\n\n")
            append(opponentName)
            append(getOpponentLineLabel())
            append("：\n")
            append(normalizedContent)
        }
    }

    // 每轮的 system prompt：通用指令 + 发言约束 + 角色 persona（每轮都带，贯穿整个上下文）+ 首轮/后续指令
    private fun buildRoundPrompt(
        speakerPrompt: String,
        isFirstTurn: Boolean,
        speaker: DuelSpeaker,
        config: AIDuelConfig,
    ): String {
        val normalizedPrompt = speakerPrompt.trim()
        val currentName = resolveSpeakerDisplayName(config, speaker)
        val opponentName = resolveSpeakerDisplayName(config, speaker.other())

        return buildString {
            append(getSystemInstruction(currentName, opponentName))
            append("\n")
            append(getResponseRules())
            if (normalizedPrompt.isNotBlank()) {
                append("\n\n")
                append(normalizedPrompt)
            }
            append("\n\n")
            if (isFirstTurn) {
                append(getFirstTurnInstruction(currentName, opponentName))
            } else {
                append(getFollowUpInstruction(currentName, opponentName))
            }
        }
    }

    private fun resolveSpeakerDisplayName(config: AIDuelConfig, speaker: DuelSpeaker): String {
        return when (speaker) {
            DuelSpeaker.A -> {
                config.roleNameA.ifBlank {
                    config.promptNameA.ifBlank { applicationContext.getString(R.string.ai_duel_speaker_a) }
                }
            }

            DuelSpeaker.B -> {
                config.roleNameB.ifBlank {
                    config.promptNameB.ifBlank { applicationContext.getString(R.string.ai_duel_speaker_b) }
                }
            }
        }
    }

    private fun buildConversationTitle(@Suppress("UNUSED_PARAMETER") config: AIDuelConfig): String {
        return applicationContext.getString(R.string.ai_duel_chat_title)
    }

    private fun duelStoppedSuffix(): String {
        return "\n\n[${applicationContext.getString(R.string.ai_duel_message_stopped)}]"
    }

    private fun validateDuelConfigForStart(config: AIDuelConfig): Int? {
        return when {
            config.personaA.isBlank() || config.personaB.isBlank() -> {
                R.string.ai_duel_error_persona_required
            }

            config.engineA == null || config.engineB == null -> {
                R.string.ai_duel_error_model_not_selected
            }

            else -> null
        }
    }

    /**
     * 在启动 duel 前对双方人设、角色名进行敏感词检测。
     * - 远端开关未启用 → 返回 null (放行)
     * - 命中 → 返回 [SensitiveWordCheckOutcome.Hit] (调用方负责拦截)
     * - 网络/接口异常 → 返回 null (fail-open, 放行)
     */
    private suspend fun checkDuelInputsForSensitiveWords(
        config: AIDuelConfig,
        scene: String,
    ): SensitiveWordCheckOutcome.Hit? {
        if (RemoteConfigStorage.getRemoteConfig().enableAgentSensitiveCheck != true) return null
        val fields = buildList {
            if (config.personaA.isNotBlank()) {
                add(SensitiveWordCheckField(key = "personaA", text = config.personaA))
            }
            if (config.personaB.isNotBlank()) {
                add(SensitiveWordCheckField(key = "personaB", text = config.personaB))
            }
            if (config.roleNameA.isNotBlank()) {
                add(SensitiveWordCheckField(key = "roleNameA", text = config.roleNameA))
            }
            if (config.roleNameB.isNotBlank()) {
                add(SensitiveWordCheckField(key = "roleNameB", text = config.roleNameB))
            }
        }
        if (fields.isEmpty()) return null
        return when (val outcome = sensitiveWordChecker.check(scene = scene, fields = fields)) {
            is SensitiveWordCheckOutcome.Hit -> outcome
            else -> null
        }
    }

    private fun parseTurn(message: MessageEntity): Pair<DuelSpeaker, String>? {
        val isSpeakerA = message.role == RoleType.USER.value
        val speaker = if (isSpeakerA) DuelSpeaker.A else DuelSpeaker.B
        val text = if (isSpeakerA) message.question else message.answer
        if (text.isBlank()) return null
        return speaker to stripLegacySpeakerPrefix(text).trim()
    }

    private fun stripLegacySpeakerPrefix(text: String): String {
        return when {
            text.startsWith("【A】") -> text.removePrefix("【A】").trimStart()
            text.startsWith("【B】") -> text.removePrefix("【B】").trimStart()
            else -> text
        }
    }

    fun onDestroy() {
        stopDuel()
    }
}
