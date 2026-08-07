package com.shifenmiao.ai.component

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.ai.agent.tool.ToolBindingRepository
import com.shifenmiao.ai.prompt.PromptManager
import com.shifenmiao.ai.repository.ConversationRepository
import com.shifenmiao.ai.repository.MessageRepository
import com.shifenmiao.ai.usecase.MessageListUseCase
import com.shifenmiao.ai.utils.AiUtils
import com.shifenmiao.ai.service.AgentCreationService
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.common.manager.AIEngineSyncManager
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.core.R
import com.shifenmiao.core.constants.Constants
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.utils.DataBaseUtils
import com.shifenmiao.model.AIChatObject
import com.shifenmiao.model.Source
import com.shifenmiao.model.ai.Agent
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.event.MainClickEvent
import com.shifenmiao.model.ai.event.MainClickEventFrom
import com.shifenmiao.model.ai.event.MainShowType
import com.shifenmiao.network.api.ApiService
import com.shifenmiao.network.api.RemoteId
import com.shifenmiao.network.utils.NetworkUtils
import com.shifenmiao.storage.RemoteConfigStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.resource.ResourceManager
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.a2ui.catalog.A2uiRenderProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.shifenmiao.model.event.AppEventBus

class AgentComponent @AssistedInject constructor(
    private val apiService: ApiService,
    @Assisted componentContext: ComponentContext,
    @Assisted conversationParams: Conversation,
    @Assisted agent: Agent,
    dispatchersHolder: DispatchersHolder,
    resourceManager: ResourceManager,
    private val appDatabase: AppDatabase,
    aiEngineManager: AIEngineManager,
    aiEngineCatalogManager: AIEngineCatalogManager,
    aiEngineSyncManager: AIEngineSyncManager,
    promptManager: PromptManager,
    conversationRepository: ConversationRepository,
    messageRepository: MessageRepository,
    private val agentCreationService: AgentCreationService,
    private val toolBindingRepository: ToolBindingRepository,
    @ApplicationContext
    applicationContext: Context,
    messageListUseCase: MessageListUseCase,
    a2uiRenderProvider: A2uiRenderProvider,
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
    enablePromptInitialization = false,
    enableModelRefresh = false,
) {

    private val _agent = MutableStateFlow(agent)
    val agentState: StateFlow<Agent> = _agent

    private var _currentId = MutableStateFlow(_agent.value.id)


    init {
        initData()
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            conversation: Conversation,
            agent: Agent
        ): AgentComponent
    }

    fun initData() {
        if (!_agent.value.dynamicBody.isNullOrBlank()) {
            updateAgentAndConversation(_agent.value)
            showSuccessUI()
            return
        }
        componentScope.launch {
            fetchLocalData()
        }
    }


    private fun fetchData(remoteId: RemoteId, isBackgroundRefresh: Boolean = false) {
        componentScope.launch(Dispatchers.IO) {
            if (!isBackgroundRefresh) {
                showLoadingUI()
            }
            val response = NetworkUtils.safeApiCall {
                apiService.fetchAgent(
                    remoteId.value
                )
            }
            if (response != null) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val localAgentId = _currentId.value
                        val remoteAgent = it.data.copy(source = Source.REMOTE)
                        val updatedAgentId = insertAgentEntity(remoteAgent, localAgentId)
                        updateAgentAndConversation(remoteAgent, localId = updatedAgentId)
                        showSuccessUI()
                    } ?: run {
                        if (!isBackgroundRefresh) showFailureUI()
                    }
                } else {
                    if (!isBackgroundRefresh) {
                        NetworkUtils.handleErrorResponse(
                            response,
                            onFriendlyErrorTip = { message ->
                                showFailureUI(message)
                            }
                        )
                    }
                }
            } else {
                if (!isBackgroundRefresh) showFailureUI()
            }
        }
    }

    /**
     * 将远端拉取到的 agent 内容写入本地资源表。
     * @param localAgentId 本地 agent 资源行主键（item_agent.id），用于更新已有行。
     */
    private suspend fun insertAgentEntity(agent: Agent, localAgentId: Int): Int {
        val itemAgentEntity = DataBaseUtils.agentToAgentEntity(
            agent,
            source = Source.REMOTE,
        ).copy(id = localAgentId)
        return appDatabase.agentDao().upsertRemoteAgent(itemAgentEntity)
    }

    private fun updateAgentAndConversation(agent: Agent, localId: Int = agent.id) {
        _agent.value = agent.copy(id = localId)
        _conversation.value = _conversation.value.copy(
            appTitle = agent.title.orEmpty(),
            prompt = agent.prompt.orEmpty(),
            entryType = AIConversationEntryType.AGENT,
            entryRefId = localId.toString(),
        )
        _currentId.value = localId
    }

    private fun fetchLocalData() {
        componentScope.launch(Dispatchers.IO) {
            val aiAgentUpdateInterval = RemoteConfigStorage.getRemoteConfig().aiAgentUpdateInterval
                ?: Constants.AI_AGENT_UPDATE_INTERVAL
            val localAgentId = _agent.value.id.takeIf { it > 0 }
            val remoteAgentId = RemoteId.of(_agent.value.remoteId)

            if (localAgentId != null) {
                // _agent.value.id 是本地 agent 资源主键，直接查资源行
                val agentEntity = appDatabase.agentDao().getAgentById(localAgentId)
                if (agentEntity != null) {
                    val agent = DataBaseUtils.agentEntityToAgent(agentEntity)
                    if (!agent.dynamicBody.isNullOrBlank()) {
                        updateAgentAndConversation(agent, localId = agentEntity.id)
                        if (agentEntity.source == Source.LOCAL) {
                            showSuccessUI()
                        } else {
                            val entityRemoteId = RemoteId.of(agentEntity.remoteId) ?: remoteAgentId
                            if (entityRemoteId != null &&
                                agentEntity.updatedAt + aiAgentUpdateInterval < System.currentTimeMillis()
                            ) {
                                // 本地数据已可用，后台静默更新，失败不影响展示
                                fetchData(entityRemoteId, isBackgroundRefresh = true)
                            } else {
                                showSuccessUI()
                            }
                        }
                        return@launch
                    }
                    val entityRemoteId = RemoteId.of(agentEntity.remoteId) ?: remoteAgentId
                    if (entityRemoteId != null) {
                        fetchData(entityRemoteId)
                    } else {
                        showFailureUI()
                    }
                    return@launch
                }
            }

            // 本地未命中：用 remoteId 兜底，绝不用 localId 调 API
            if (remoteAgentId != null) {
                fetchData(remoteAgentId)
            } else {
                showFailureUI()
            }
        }
    }

    fun prepareEditDraft(
        onReady: (Long) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        val currentAgent = agentState.value
        if (currentAgent.dynamicBody.isNullOrBlank()) {
            onFailure(getString(R.string.create_ai_agent_edit_unavailable))
            return
        }
        componentScope.launch(Dispatchers.IO) {
            runCatching {
                agentCreationService.ensureEditDraft(
                    agent = currentAgent,
                    fallbackSelectedToolNames = toolBindingRepository
                        .getAgentBoundToolNames(currentAgent.id)
                        .orEmpty()
                        .toSet()
                )
            }.onSuccess { draftId ->
                withContext(Dispatchers.Main) {
                    onReady(draftId)
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    onFailure(getString(R.string.create_ai_agent_edit_unavailable))
                }
            }
        }
    }

    fun pushAgentToRemote(
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {},
    ) {
        val currentAgent = _agent.value
        componentScope.launch(Dispatchers.IO) {
            val localAgentEntity = appDatabase.agentDao().getAgentById(currentAgent.id)
            val requestAgent = currentAgent.copy(
                id = localAgentEntity?.remoteId ?: currentAgent.id
            )
            val response = NetworkUtils.safeApiCall {
                apiService.updateAgent(requestAgent)
            }
            if (response != null) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val localAgentId = currentAgent.id
                        val remoteAgent = it.data.copy(source = Source.REMOTE)
                        val updatedAgentId = insertAgentEntity(remoteAgent, localAgentId)
                        updateAgentAndConversation(remoteAgent, localId = updatedAgentId)
                    }
                    withContext(Dispatchers.Main) { onSuccess() }
                } else {
                    var errorMsg: String? = null
                    NetworkUtils.handleErrorResponse(
                        response,
                        onFriendlyErrorTip = { message ->
                            errorMsg = message
                        }
                    )
                    withContext(Dispatchers.Main) {
                        onFailure(errorMsg ?: getString(R.string.agent_push_failed))
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    onFailure(getString(R.string.agent_push_failed))
                }
            }
        }
    }

    public override fun submitToAIChat(
        onNavigate: (Screen) -> Unit,
        promptText: String?
    ) {
        componentScope.launch(Dispatchers.IO) {
            ActionUtils.userAIChatInputCheck(
                conversation = _conversation.value,
                source = "Agent/submit",
            ) {
                val agent = agentState.value
                val startMessage = promptText?.let {
                    if (it.endsWith(",") || it.endsWith("，")) it.dropLast(1) + "。" else it
                }
                startMessage?.let { message ->
                    if (!AiUtils.isNotFree(_conversation.value) || BaseUtils.canConsumePoints(message)) {
                        navigateToChatScreen(
                            onNavigate, AIChatObject(
                                agentId = agent.id.toString(),
                                message = startMessage,
                                conversation = _conversation.value
                            )
                        )
                    } else {
                        ActionUtils.showToast(R.string.no_points)
                        AppEventBus.emit(
                            MainClickEvent(
                                from = MainClickEventFrom.AI_START_CHAT,
                                type = MainShowType.BUY_COFFEE
                            )
                        )
                        return@userAIChatInputCheck
                    }
                }
            }
        }
    }
}
