package com.shifenmiao.ai.component

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.shifenmiao.model.ModelProvider.AppJson
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
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
import com.shifenmiao.ai.model.DuelPhase
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
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.MessageUIState
import com.shifenmiao.model.ai.RoleType
import com.shifenmiao.model.ai.SearchCitation
import com.shifenmiao.model.ai.SearchResult
import com.shifenmiao.model.ai.Usage
import com.shifenmiao.model.ai.unified.LlmStreamEvent
import com.shifenmiao.model.channel.FlavorType
import com.shifenmiao.storage.AppSharedStorage
import com.shifenmiao.storage.RemoteConfigStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.resource.ResourceManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.cancel
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
        // 2 分钟无任何流式数据才判超时；reasoning 模型首 token 可能很慢，20 秒容易误杀。
        const val STREAM_INACTIVITY_TIMEOUT_MS = 120_000L
        const val TYPEWRITER_FAST_FORWARD_PENDING_CHARS = 4096
        const val TYPEWRITER_FAST_CHUNK_CHARS = 256
        const val TYPEWRITER_FAST_UI_UPDATE_MS = 48L
        const val MAX_CONTEXT_TURNS = 40
        const val MAX_UNLIMITED_TURNS = 120
        const val RESUME_WINDOW_MS = 12 * 60 * 60 * 1000L
        const val SESSION_NEW = "NEW"
        const val SESSION_PAUSED = "PAUSED"
        const val SESSION_BATCH_LIMIT = "BATCH_LIMIT"
        const val SESSION_ENDED = "ENDED"
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
    private var pauseRequested = false
    private var duelSessionToken = 0

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
            val now = System.currentTimeMillis()
            // 最近一场即使还没有消息也要优先恢复：它就是用户刚点的“新建”，
            // 不能被更早但已经有消息的会话抢回去。
            val entity = appDatabase.conversationDao()
                .getLastConversationByType(AIConversationEntryType.DUEL.name)
                ?.takeIf { now - it.lastActiveAt <= RESUME_WINDOW_MS }
                ?: appDatabase.conversationDao()
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

    private fun personaOrDefault(config: AIDuelConfig, speaker: DuelSpeaker): String {
        val persona = if (speaker == DuelSpeaker.A) config.personaA else config.personaB
        return persona.trim().ifBlank {
            applicationContext.getString(
                if (speaker == DuelSpeaker.A) {
                    R.string.ai_duel_default_persona_a
                } else {
                    R.string.ai_duel_default_persona_b
                }
            )
        }
    }

    private fun getSystemInstruction(currentName: String, opponentName: String): String {
        return applicationContext.getString(
            R.string.ai_duel_system_prompt,
            currentName,
            opponentName
        )
    }

    private fun getSeedMessage(): String {
        return applicationContext.getString(R.string.ai_duel_seed_start)
    }

    private fun getWrapUpInstruction(): String {
        return applicationContext.getString(R.string.ai_duel_wrap_up_instruction)
    }

    private var observeMessagesJob: Job? = null

    private fun observeMessages(conversationId: String) {
        observeMessagesJob?.cancel()
        observeMessagesJob = componentScope.launch(ioDispatcher) {
            messageDao.getMessagesFlowPaged(conversationId, limit = 200)
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
                    val turnCount = messageList.count { parseTurn(it) != null }
                    val phase = _duelState.value.phase
                    val shouldRestore = phase == DuelPhase.IDLE &&
                        messageList.isNotEmpty() &&
                        !_conversation.value.showLastMessage
                    val persistedState = _duelConfig.value.sessionState
                    _duelState.value = _duelState.value.copy(
                        totalTokens = messageList.sumOf { it.totalTokens },
                        round = turnCount / 2,
                        phase = if (shouldRestore && persistedState == SESSION_ENDED) {
                            DuelPhase.ENDED
                        } else if (shouldRestore) {
                            DuelPhase.PAUSED
                        } else {
                            phase
                        },
                        batchLimitReached = if (shouldRestore) {
                            persistedState == SESSION_BATCH_LIMIT
                        } else {
                            _duelState.value.batchLimitReached
                        }
                    )
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
        val hasMessages = synchronized(messages) { messages.isNotEmpty() }
        val preserveSession = hasMessages ||
            (_conversation.value.historyVisible && _conversation.value.messageCount > 0)
        _duelConfig.value = config

        config.engineA?.let(aiEngineManager::setDuelEngineA)
        config.engineB?.let(aiEngineManager::setDuelEngineB)

        if (preserveSession) {
            // 已经在聊天中/从历史打开：只更新配置，不能把已落库的会话重新标成“草稿”。
            _conversation.value = _conversation.value.copy(
                entryType = AIConversationEntryType.DUEL,
                appTitle = appTitle,
                prompt = AIDuelConfigCodec.encode(config),
            )
            synchronized(cachedMessageUiModels) { cachedMessageUiModels.clear() }
            renderMessage(forceUpdate = true)
            if (persistDraft && shouldPersistDraftConfig(config)) {
                componentScope.launch(ioDispatcher) {
                    appDatabase.conversationDao()
                        .insertReplace(ConversationEntity.fromConversation(_conversation.value))
                }
            }
            return
        }

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
                // 顺手清掉会话级记忆/技能开关，避免死行堆积
                appDatabase.conversationMemoryPolicyDao().deleteByConversationId(conversationId)
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
            promptNameB.isNotBlank() ||
            scene.isNotBlank() ||
            sessionState.isNotBlank() ||
            maxRounds != 5
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
        componentScope.launch(ioDispatcher) {
            pauseDuelInternal()
            _duelConfig.value = _duelConfig.value.copy(sessionState = SESSION_NEW)
            _conversation.value = _conversation.value.copy(
                id = Date().time.toString(),
                entryType = AIConversationEntryType.DUEL,
                appTitle = applicationContext.getString(R.string.ai_duel_chat_title),
                title = "",
                showLastMessage = false,
                prompt = AIDuelConfigCodec.encode(_duelConfig.value),
                historyVisible = false,
                lastMessagePreview = "",
                lastUserMessagePreview = "",
                lastActiveAt = System.currentTimeMillis(),
                messageCount = 0,
            )
            clearMessages()
            _duelState.value = AIDuelState()
            // 落库：否则离开页面后“新建”会消失，12 小时自动恢复又捞回上一场。
            appDatabase.conversationDao()
                .insertReplace(ConversationEntity.fromConversation(_conversation.value))
            loadMessages()
        }
    }

    fun startDuel(config: AIDuelConfig) {
        componentScope.launch(ioDispatcher) {
            startDuelInternal(config = config, resume = false)
        }
    }

    /** 从当前最后一条发言接着聊；不新建会话、不清空记录。 */
    fun resumeDuel() {
        componentScope.launch(ioDispatcher) {
            startDuelInternal(config = _duelConfig.value, resume = true)
        }
    }

    /** 只重试上一条失败/中断的发言。 */
    fun retryDuel() {
        componentScope.launch(ioDispatcher) {
            startDuelInternal(config = _duelConfig.value, resume = true, maxTurnsOverride = 1)
        }
    }

    /** 让下一位用一句话收尾，然后结束本次会话。 */
    fun wrapUpDuel() {
        componentScope.launch(ioDispatcher) {
            startDuelInternal(
                config = _duelConfig.value,
                resume = true,
                maxTurnsOverride = 1,
                wrapUp = true,
            )
        }
    }

    fun stopDuel() {
        if (_duelState.value.running) {
            // 不立刻砍流：当前这句说完再停，避免屏幕上的半句突然消失。
            pauseRequested = true
            _duelState.value = _duelState.value.copy(pauseRequested = true)
        } else {
            componentScope.launch(ioDispatcher) {
                pauseDuelInternal()
            }
        }
    }

    private suspend fun startDuelInternal(
        config: AIDuelConfig,
        resume: Boolean,
        maxTurnsOverride: Int? = null,
        wrapUp: Boolean = false,
    ) {
        pauseDuelInternal()
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
            engineB = defaultEngineB,
            sessionState = "",
        )
        val validationError = validateDuelConfigForStart(resolvedConfig)
        if (validationError != null) {
            withContext(Dispatchers.Main) {
                ActionUtils.showToast(applicationContext.getString(validationError))
            }
            return
        }

        if (!resume) {
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
                return
            }
        }

        val appTitle = buildConversationTitle(resolvedConfig)
        // 非续聊一律开新会话 ID：复用一个已经有消息的 ID 会让 clearMessages() 后的
        // loadMessages() 又把旧记录读回来。
        val conversationId = if (resume) {
            _conversation.value.id.ifBlank { Date().time.toString() }
        } else {
            Date().time.toString()
        }

        _duelConfig.value = resolvedConfig
        if (resume) {
            _conversation.value = _conversation.value.copy(
                entryType = AIConversationEntryType.DUEL,
                appTitle = appTitle,
                historyVisible = true,
                prompt = AIDuelConfigCodec.encode(resolvedConfig),
                placeholder = applicationContext.getString(R.string.ai_duel_chat_description)
            )
        } else {
            _conversation.value = _conversation.value.copy(
                id = conversationId,
                entryType = AIConversationEntryType.DUEL,
                appTitle = appTitle,
                title = "",
                showLastMessage = false,
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
        }

        appDatabase.conversationDao()
            .insertReplace(ConversationEntity.fromConversation(_conversation.value))

        val turns = synchronized(messages) { messages.toList() }
            .asReversed()
            .mapNotNull(::parseTurn)
        val nextSpeaker = turns.lastOrNull()?.first?.other() ?: DuelSpeaker.A
        val totalTokens = synchronized(messages) { messages.sumOf { it.totalTokens } }
        val maxTurns = maxTurnsOverride ?: batchTurns(resolvedConfig)
        pauseRequested = false
        val sessionToken = ++duelSessionToken

        _duelState.value = AIDuelState(
            phase = DuelPhase.RUNNING,
            round = turns.size / 2,
            speaker = nextSpeaker,
            totalTokens = totalTokens,
            batchRound = 1,
            batchTotal = ((maxTurns + 1) / 2).coerceAtLeast(1),
        )
        duelJob = componentScope.launch(ioDispatcher) {
            try {
                runDuelLoop(
                    maxTurns = maxTurns,
                    wrapUp = wrapUp,
                    sessionToken = sessionToken,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                removeStreamingPlaceholder()
                _duelState.value = _duelState.value.copy(
                    phase = DuelPhase.PAUSED,
                    errorMessage = e.message?.takeIf { it.isNotBlank() }
                        ?: applicationContext.getString(R.string.ai_error_unknown),
                    retryAvailable = true,
                )
            }
        }
    }

    private fun batchTurns(config: AIDuelConfig): Int {
        val rounds = config.maxRounds
        return if (rounds <= 0) {
            MAX_UNLIMITED_TURNS
        } else {
            (rounds * 2).coerceAtMost(MAX_UNLIMITED_TURNS)
        }
    }

    private suspend fun pauseDuelInternal() {
        pauseRequested = false
        duelJob?.cancel()
        duelJob = null
        fetchJob?.cancel()
        fetchJob = null
        removeStreamingPlaceholder()
        if (_duelState.value.phase == DuelPhase.RUNNING) {
            _duelState.value = _duelState.value.copy(
                phase = DuelPhase.PAUSED,
                retryAvailable = _duelState.value.hasError,
                pauseRequested = false,
            )
        }
        _chatUIState.value = _chatUIState.value.copy(chatActive = false)
    }

    private fun removeStreamingPlaceholder() {
        val removed = synchronized(messages) {
            messages.removeIf { it.id <= 0 && it.uId < MessageUIState.NORMAL.value }
        }
        if (removed) {
            renderMessage(forceUpdate = true)
        }
    }

    private fun parseDuelConfigTextField(prompt: String, key: String): String {
        if (prompt.isBlank()) return ""
        return kotlin.runCatching {
            val obj = AppJson.parseToJsonElement(prompt).jsonObject
            (obj[key] as? JsonPrimitive)?.contentOrNull.orEmpty().trim()
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

    private suspend fun runDuelLoop(
        maxTurns: Int,
        wrapUp: Boolean = false,
        sessionToken: Int = duelSessionToken,
    ) {
        val loopConversationId = _conversation.value.id
        var completedTurns = 0

        while (currentCoroutineContext().isActive && completedTurns < maxTurns) {
            if (pauseRequested) {
                pauseRequested = false
                _duelState.value = _duelState.value.copy(
                    phase = DuelPhase.PAUSED,
                    pauseRequested = false,
                    batchLimitReached = false,
                )
                break
            }
            val config = _duelConfig.value
            val speaker = nextSpeaker()
            _duelState.value = _duelState.value.copy(
                phase = DuelPhase.RUNNING,
                round = currentTurnCount() / 2,
                speaker = speaker,
                errorMessage = "",
                retryAvailable = false,
                batchLimitReached = false,
                batchRound = completedTurns / 2 + 1,
                batchTotal = ((maxTurns + 1) / 2).coerceAtLeast(1),
            )

            val speakerEngine = if (speaker == DuelSpeaker.A) config.engineA else config.engineB
            if (speakerEngine == null) {
                val modelNotSelected = applicationContext.getString(R.string.ai_duel_error_model_not_selected)
                _duelState.value = _duelState.value.copy(
                    phase = DuelPhase.PAUSED,
                    errorMessage = modelNotSelected,
                    retryAvailable = false,
                )
                break
            }

            val canProceed = ensureLoginAndCheckPointsForRound(speakerEngine)
            if (!canProceed) {
                _duelState.value = _duelState.value.copy(
                    phase = DuelPhase.PAUSED,
                    errorMessage = applicationContext.getString(R.string.ai_duel_error_cannot_start),
                    retryAvailable = true,
                )
                break
            }

            val requestConversation = _conversation.value.copy(
                prompt = buildRoundPrompt(
                    speaker = speaker,
                    config = config,
                    wrapUp = wrapUp,
                ),
                engine = speakerEngine
            )
            val requestMessages = buildRequestMessages(speaker = speaker)
            val completionId = Date().time.toString()
            val isSpeakerA = speaker == DuelSpeaker.A
            val streaming = MessageEntity(
                completionId = completionId,
                conversationId = _conversation.value.id,
                role = if (isSpeakerA) RoleType.USER.value else RoleType.ASSISTANT.value,
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
            var turnSucceeded = false
            val turnJob = componentScope.launch(ioDispatcher) {
                turnSucceeded = try {
                    streamWithTypewriter(
                        requestConversation = requestConversation,
                        requestMessages = requestMessages,
                        streaming = streaming,
                        speaker = speaker,
                        startTimeMs = roundStartTimeMs
                    )
                } catch (e: CancellationException) {
                    false
                } catch (e: Exception) {
                    removeStreamingPlaceholder()
                    _duelState.value = _duelState.value.copy(
                        phase = DuelPhase.PAUSED,
                        errorMessage = e.message?.takeIf { it.isNotBlank() }
                            ?: applicationContext.getString(R.string.ai_error_unknown),
                        retryAvailable = true,
                    )
                    false
                }
            }
            fetchJob = turnJob
            turnJob.join()
            fetchJob = null

            if (!currentCoroutineContext().isActive) break
            if (!turnSucceeded) {
                _duelState.value = _duelState.value.copy(
                    phase = DuelPhase.PAUSED,
                    retryAvailable = _duelState.value.hasError,
                )
                break
            }

            completedTurns += 1
            _duelState.value = _duelState.value.copy(
                round = currentTurnCount() / 2,
                totalTokens = synchronized(messages) { messages.sumOf { it.totalTokens } },
                batchRound = completedTurns / 2 + 1,
            )
            if (pauseRequested) {
                pauseRequested = false
                _duelState.value = _duelState.value.copy(
                    phase = DuelPhase.PAUSED,
                    pauseRequested = false,
                    batchLimitReached = false,
                )
                break
            }
        }

        if (_duelState.value.phase == DuelPhase.RUNNING) {
            _duelState.value = _duelState.value.copy(
                phase = if (wrapUp && completedTurns > 0) DuelPhase.ENDED else DuelPhase.PAUSED,
                batchLimitReached = !wrapUp &&
                    maxTurns == batchTurns(_duelConfig.value) &&
                    completedTurns >= maxTurns,
                pauseRequested = false,
            )
        }
        val persistedState = when {
            _duelState.value.phase == DuelPhase.ENDED -> SESSION_ENDED
            _duelState.value.batchLimitReached -> SESSION_BATCH_LIMIT
            else -> SESSION_PAUSED
        }
        if (sessionToken == duelSessionToken &&
            _conversation.value.id == loopConversationId
        ) {
            kotlin.runCatching { persistSessionState(persistedState) }
            kotlin.runCatching { insertDuelOperationHistoryIfNeeded() }
        }
    }

    private suspend fun persistSessionState(state: String) {
        val config = _duelConfig.value.copy(sessionState = state)
        _duelConfig.value = config
        _conversation.value = _conversation.value.copy(
            prompt = AIDuelConfigCodec.encode(config)
        )
        kotlin.runCatching {
            appDatabase.conversationDao()
                .insertReplace(ConversationEntity.fromConversation(_conversation.value))
        }
    }

    private fun nextSpeaker(): DuelSpeaker {
        val lastSpeaker = synchronized(messages) { messages.toList() }
            .asReversed()
            .mapNotNull(::parseTurn)
            .lastOrNull()
            ?.first
        return lastSpeaker?.other() ?: DuelSpeaker.A
    }

    private fun currentTurnCount(): Int {
        return synchronized(messages) {
            messages.count { parseTurn(it) != null }
        }
    }

    private suspend fun ensureLoginAndCheckPointsForRound(engine: com.shifenmiao.model.ai.AiEngine): Boolean {
        // 端侧本地引擎不需要 token/代理/登录/积分,直接放行
        if (engine.requestProtocol == com.shifenmiao.model.ai.AiRequestProtocol.LOCAL_ON_DEVICE) {
            return true
        }
        // 海外渠道(google / foss): 自带 token 可直连的引擎免登录免积分直接聊
        if (FlavorType.fromName().isOverseas && engine.canChatDirectly()) {
            return true
        }
        // 海外渠道(google / foss): 无代理路由时登录也无意义(网关无对应代理), 直接失败并提示去设置
        if (FlavorType.fromName().isOverseas && !engine.hasProxyRouteConfigured()) {
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
    ): Boolean = coroutineScope {
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
                    // 流结束后不再按 18ms/字慢慢放，直接快速排空，避免堵住下一位。
                    val fast = streamEnded || pendingChars > TYPEWRITER_FAST_FORWARD_PENDING_CHARS
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
            ).catch { error ->
                if (error is CancellationException) throw error
                errorMsg = error.message
                streamEnded = true
            }.collect { event ->
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

                    // 流异常中断(未收到结束标记连接即关闭): 与 Error 同路径, 走错误提示
                    is LlmStreamEvent.StreamAborted -> {
                        errorMsg = applicationContext.getString(R.string.ai_chat_stream_interrupted)
                        streamEnded = true
                        this.cancel()
                    }
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
            // 错误只作为本次会话的临时状态展示，绝不落成一条“发言”。
            synchronized(messages) {
                messages.removeIf { it.completionId == streaming.completionId }
            }
            renderMessage(forceUpdate = true)
            _duelState.value = _duelState.value.copy(
                phase = DuelPhase.PAUSED,
                errorMessage = finalErrorMsg,
                retryAvailable = true,
            )
            _chatUIState.value = _chatUIState.value.copy(chatActive = false)
            return@coroutineScope false
        }

        delay(TYPE_DELAY)
        val completionText = if (speaker == DuelSpeaker.A) streaming.question else streaming.answer
        if (completionText.isBlank()) {
            synchronized(messages) {
                messages.removeIf { it.completionId == streaming.completionId }
            }
            renderMessage(forceUpdate = true)
            _duelState.value = _duelState.value.copy(
                phase = DuelPhase.PAUSED,
                errorMessage = applicationContext.getString(R.string.ai_error),
                retryAvailable = true,
            )
            _chatUIState.value = _chatUIState.value.copy(chatActive = false)
            return@coroutineScope false
        }

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
        kotlin.runCatching {
            consumePoints(
                engine = requestConversation.engine,
                messageEntity = finalMessage
            )
        }
        true
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
        speaker: DuelSpeaker,
    ): List<MessageEntity> {
        val turnsAsc = synchronized(messages) { messages.toList() }
            .asReversed()
            .filter { it.id > 0 || it.uId == MessageUIState.NORMAL.value }
            .mapNotNull(::parseTurn)
            .takeLast(MAX_CONTEXT_TURNS)

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
                    question = content
                )
            }
        }

        // 严格轮转下最后一条一定是对手的 user 消息；只有历史数据异常时才会走到这里。
        if (result.lastOrNull()?.role != RoleType.USER.value) {
            result += MessageEntity(
                completionId = "u_tail",
                conversationId = _conversation.value.id,
                role = RoleType.USER.value,
                question = getSeedMessage()
            )
        }

        return result
    }

    // 每轮的 system prompt：身份 + 可选人设 + 一条“别替对方说话”的边界。
    // 不再追加“亮观点/别寒暄/回应最有价值的一点”这类导演指令，形式交给双方自己长出来。
    private fun buildRoundPrompt(
        speaker: DuelSpeaker,
        config: AIDuelConfig,
        wrapUp: Boolean = false,
    ): String {
        val currentName = resolveSpeakerDisplayName(config, speaker)
        val opponentName = resolveSpeakerDisplayName(config, speaker.other())
        val persona = personaOrDefault(config, speaker).trim()

        return buildString {
            append(getSystemInstruction(currentName, opponentName))
            if (persona.isNotBlank()) {
                append("\n\n")
                append(persona)
            }
            val scene = config.scene.trim()
            if (scene.isNotBlank()) {
                append("\n\n")
                append(applicationContext.getString(R.string.ai_duel_scene_prompt, scene))
            }
            if (wrapUp) {
                append("\n\n")
                append(getWrapUpInstruction())
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

    private fun buildConversationTitle(config: AIDuelConfig): String {
        val nameA = resolveSpeakerDisplayName(config, DuelSpeaker.A)
        val nameB = resolveSpeakerDisplayName(config, DuelSpeaker.B)
        return applicationContext.getString(R.string.ai_duel_title_format, nameA, nameB)
    }

    private fun validateDuelConfigForStart(config: AIDuelConfig): Int? {
        return when {
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
            if (config.scene.isNotBlank()) {
                add(SensitiveWordCheckField(key = "scene", text = config.scene))
            }
        }
        if (fields.isEmpty()) return null
        return when (val outcome = sensitiveWordChecker.check(scene = scene, fields = fields)) {
            is SensitiveWordCheckOutcome.Hit -> outcome
            else -> null
        }
    }

    private fun parseTurn(message: MessageEntity): Pair<DuelSpeaker, String>? {
        // 旧版本会把网络错误文案落成一条普通消息；重放时不能再当成发言喂给对方。
        if (message.finishReason.startsWith("error:") || message.expired) return null
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
            text.startsWith("A：") -> text.removePrefix("A：").trimStart()
            text.startsWith("B：") -> text.removePrefix("B：").trimStart()
            text.startsWith("A:") -> text.removePrefix("A:").trimStart()
            text.startsWith("B:") -> text.removePrefix("B:").trimStart()
            else -> text
        }
    }

    fun onDestroy() {
        duelJob?.cancel()
        duelJob = null
        fetchJob?.cancel()
        fetchJob = null
    }
}
