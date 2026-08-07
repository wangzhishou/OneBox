package com.wanbaohe.xiangqi.router.screenLogic

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.database.chat_prompt.dao.PromptDao
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.tts.TTSConfig
import com.shifenmiao.tts.service.TTSService
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.xiangqi.application.port.outbound.SoundPlayer
import com.wanbaohe.xiangqi.application.usecase.GameQueryUseCase
import com.wanbaohe.xiangqi.application.usecase.SettingsUseCase
import com.wanbaohe.xiangqi.component.XiangqiAnalysisComponent
import com.wanbaohe.xiangqi.component.XiangqiGameComponent
import com.wanbaohe.xiangqi.component.XiangqiLibraryComponent
import com.wanbaohe.xiangqi.data.XiangqiSettings
import com.wanbaohe.xiangqi.data.XiangqiTTSTemplate
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.xiangqi.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

class XiangqiRouterComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val type: Screen.XiangqiRouter.Type?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    libraryFactory: XiangqiLibraryComponent.Factory,
    private val gameFactory: XiangqiGameComponent.Factory,
    private val analysisFactory: XiangqiAnalysisComponent.Factory,
    private val settingsUseCase: SettingsUseCase,
    private val soundPlayer: SoundPlayer,
    private val ttsService: TTSService,
    private val aiEngineManager: AIEngineManager,
    aiEngineCatalogManager: AIEngineCatalogManager,
    private val gameQuery: GameQueryUseCase,
    private val promptDao: PromptDao,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    sealed interface SettingsAction {
        data object PreviewTTSConfig : SettingsAction
        data object PreviewMoveSound : SettingsAction
        data object PreviewCheckSound : SettingsAction
        data object PreviewBackgroundMusic : SettingsAction
        data object StopBackgroundMusic : SettingsAction
        data class GenerateTTS(val tag: String) : SettingsAction
        data class RegenerateTTS(val tag: String) : SettingsAction
        data class PlayTTS(val tag: String) : SettingsAction
    }

    enum class Tab { Play, Analyze, Library, Settings }

    sealed interface Child {
        data object PlayHome : Child
        data object AnalysisHome : Child
        data object Settings : Child
        data class Library(val component: XiangqiLibraryComponent) : Child
        data class Game(val component: XiangqiGameComponent) : Child
        data class Analysis(val component: XiangqiAnalysisComponent) : Child
    }

    @Serializable
    sealed interface Route {
        @Serializable @SerialName("PlayHome") data object PlayHome : Route
        @Serializable @SerialName("AnalysisHome") data object AnalysisHome : Route
        @Serializable @SerialName("Library") data object Library : Route
        @Serializable @SerialName("Settings") data object Settings : Route
        @Serializable @SerialName("Game") data class Game(val gameId: String) : Route
        @Serializable @SerialName("Analysis") data class Analysis(val gameId: String, val initialPly: Int = -1) : Route
    }

    val xiangqiSettings: StateFlow<XiangqiSettings> = settingsUseCase.observe()
        .mapToLegacy()
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), XiangqiSettings())

    val currentAIEngine: StateFlow<AiEngine> = aiEngineManager.fastAIEngine
    val duelEngineA: StateFlow<AiEngine> = aiEngineManager.duelEngineA
    val duelEngineB: StateFlow<AiEngine> = aiEngineManager.duelEngineB

    val allAiEngines: StateFlow<List<AiEngine>> =
        aiEngineCatalogManager.observeAvailableEngines()
            .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val modelsByProvider: StateFlow<Map<String, List<AiModel>>> =
        aiEngineCatalogManager.observeModelsByProvider()
            .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val ttsConfig: Flow<TTSConfig> = ttsService.observeConfig()

    private val _runningSettingsActions = MutableStateFlow<Set<SettingsAction>>(emptySet())
    val runningSettingsActions: StateFlow<Set<SettingsAction>> = _runningSettingsActions.asStateFlow()

    val libraryComponent: XiangqiLibraryComponent = libraryFactory(
        componentContext = componentContext.childContext("xiangqi_library_shared"),
        onGoBack = ::navigateBack,
        onNavigate = ::handleInternalNavigation,
    )

    private val navigation = StackNavigation<Route>()

    val childStack: Value<ChildStack<Route, Child>> = childStack(
        source = navigation,
        serializer = Route.serializer(),
        initialConfiguration = type.toInitialRoute(),
        handleBackButton = false,
        childFactory = ::createChild,
    )

    private var selectedGameId: String? = type.initialGameId()

    var pendingJoinRoomId by mutableStateOf(type.initialJoinRoomId())
        private set

    init {
        maybeOpenRecentGame()
    }

    fun selectTab(tab: Tab) {
        when (tab) {
            Tab.Play -> openPlayTab()
            Tab.Analyze -> openAnalyzeTab()
            Tab.Library -> openLibrary()
            Tab.Settings -> navigation.pushToFront(Route.Settings)
        }
    }

    fun openLibrary() { navigation.pushToFront(Route.Library) }
    fun openGame(gameId: String) { selectedGameId = gameId; navigation.pushNew(Route.Game(gameId)) }
    fun openAnalysis(gameId: String, initialPly: Int = -1) { selectedGameId = gameId; navigation.pushNew(Route.Analysis(gameId, initialPly)) }
    fun joinOnlineRoom(roomId: String) { pendingJoinRoomId = roomId.trim() }
    fun clearPendingJoinRoom() { pendingJoinRoomId = "" }
    fun navigateBack() { navigation.pop() }

    fun navigateBackFrom(route: Route) {
        when {
            route is Route.Game || route is Route.Analysis -> navigation.pop()
            childStack.value.items.size > 1 -> navigation.pop()
            else -> onGoBack()
        }
    }

    fun tabOf(route: Route): Tab = when (route) {
        Route.PlayHome, is Route.Game -> Tab.Play
        Route.AnalysisHome, is Route.Analysis -> Tab.Analyze
        Route.Library -> Tab.Library
        Route.Settings -> Tab.Settings
    }

    fun canPop(route: Route): Boolean = childStack.value.items.size > 1

    fun updateMoveSoundUrl(url: String) { componentScope.launch { settingsUseCase.updateMoveSoundUrl(url) } }
    fun updateBackgroundMusicUrl(url: String) { componentScope.launch { settingsUseCase.updateBackgroundMusicUrl(url) } }
    fun updateCheckSoundUrl(url: String) { componentScope.launch { settingsUseCase.updateCheckSoundUrl(url) } }
    fun updateTTSEnabled(enabled: Boolean) { componentScope.launch { settingsUseCase.updateTTSEnabled(enabled) } }
    fun updateTTSTemplateText(tag: String, text: String) { componentScope.launch { settingsUseCase.updateTTSTemplateText(tag, text) } }

    fun switchAiModel(engine: AiEngine, model: AiModel) { aiEngineManager.switchFastModel(engine, model) }
    fun switchDuelEngineA(engine: AiEngine, model: AiModel) { aiEngineManager.setDuelEngineA(engine.copy(model = model)) }
    fun switchDuelEngineB(engine: AiEngine, model: AiModel) { aiEngineManager.setDuelEngineB(engine.copy(model = model)) }

    fun openAiModelSettings() { onNavigate(Screen.Settings(searchQuery = AppContext.getString(R.string.xiangqi_search_ai_model))) }
    fun openXiangqiPromptSettings() {
        componentScope.launch {
            val prompt = promptDao.getSystemPromptByKey(PromptEntity.SYSTEM_PROMPT_KEY_XIANGQI_MOVE)
            if (prompt != null) {
                onNavigate(Screen.SystemPromptDetail(promptId = prompt.id))
            }
        }
    }
    fun openTTSConfigSettings() { onNavigate(Screen.TTSSettings) }

    fun generateTTS(template: XiangqiTTSTemplate, customText: String) {
        runSettingsAction(SettingsAction.GenerateTTS(template.tag)) {
            val text = customText.ifBlank { template.defaultText }
            ttsService.synthesize(text = text, tag = template.tag)
        }
    }

    fun regenerateTTS(template: XiangqiTTSTemplate, customText: String) {
        runSettingsAction(SettingsAction.RegenerateTTS(template.tag)) {
            val text = customText.ifBlank { template.defaultText }
            ttsService.regenerate(text = text, tag = template.tag)
        }
    }

    fun playTTSAudio(template: XiangqiTTSTemplate, customText: String) {
        runSettingsAction(SettingsAction.PlayTTS(template.tag)) {
            val text = customText.ifBlank { template.defaultText }
            val audio = ttsService.getAudioByTextAndTag(text, template.tag)
            if (audio != null) {
                soundPlayer.playLocalFile(File(audio.filePath))
            } else {
                ttsService.synthesize(text = text, tag = template.tag)
                    .onSuccess { file -> soundPlayer.playLocalFile(file) }
            }
        }
    }

    fun previewTTSConfig() {
        runSettingsAction(SettingsAction.PreviewTTSConfig) {
            ttsService.synthesize(text = AppContext.getString(R.string.xiangqi_tts_test_text), tag = "tts-test")
                .onSuccess { file -> soundPlayer.playLocalFile(file) }
        }
    }

    fun previewMoveSound() = previewSound(
        action = SettingsAction.PreviewMoveSound,
        url = xiangqiSettings.value.moveSoundUrl,
    )

    fun previewCheckSound() = previewSound(
        action = SettingsAction.PreviewCheckSound,
        url = xiangqiSettings.value.checkSoundUrl,
    )

    fun previewBackgroundMusic() {
        runSettingsAction(SettingsAction.PreviewBackgroundMusic) {
            soundPlayer.playBackground(xiangqiSettings.value.backgroundMusicUrl)
        }
    }

    fun stopBackgroundMusicPreview() {
        runSettingsAction(SettingsAction.StopBackgroundMusic) {
            soundPlayer.stopBackground()
        }
    }

    private fun previewSound(action: SettingsAction, url: String) {
        runSettingsAction(action) {
            soundPlayer.playEffect(url)
        }
    }

    private fun runSettingsAction(
        action: SettingsAction,
        block: suspend () -> Unit,
    ) {
        componentScope.launch {
            markSettingsActionRunning(action, true)
            try {
                block()
            } finally {
                markSettingsActionRunning(action, false)
            }
        }
    }

    private fun markSettingsActionRunning(
        action: SettingsAction,
        isRunning: Boolean,
    ) {
        _runningSettingsActions.value = if (isRunning) {
            _runningSettingsActions.value + action
        } else {
            _runningSettingsActions.value - action
        }
    }

    private fun createChild(route: Route, context: ComponentContext): Child = when (route) {
        Route.PlayHome -> Child.PlayHome
        Route.AnalysisHome -> Child.AnalysisHome
        Route.Library -> Child.Library(libraryComponent)
        Route.Settings -> Child.Settings
        is Route.Game -> Child.Game(gameFactory(context, route.gameId, ::navigateBack, ::handleInternalNavigation))
        is Route.Analysis -> Child.Analysis(analysisFactory(context, route.gameId, route.initialPly, ::navigateBack, ::handleInternalNavigation))
    }

    private fun openPlayTab(clearSelectedGame: Boolean = false) {
        if (clearSelectedGame) selectedGameId = null
        val gameId = selectedGameId
        if (gameId == null) navigation.pushToFront(Route.PlayHome)
        else navigation.pushNew(Route.Game(gameId))
    }

    private fun maybeOpenRecentGame() {
        if (type != null) return
        componentScope.launch {
            gameQuery.observeAll()
                .filter { it.isNotEmpty() }
                .first()
                .let { games ->
                    val mostRecent = games.maxByOrNull { it.updatedAt }
                    if (mostRecent != null) {
                        selectedGameId = mostRecent.id
                        navigation.replaceCurrent(Route.Game(mostRecent.id))
                    }
                }
        }
    }

    private fun openAnalyzeTab() {
        val gameId = selectedGameId
        if (gameId == null) navigation.pushToFront(Route.AnalysisHome)
        else navigation.pushNew(Route.Analysis(gameId))
    }

    private fun handleInternalNavigation(screen: Screen) {
        when (screen) {
            is Screen.XiangqiRouter -> when (val target = screen.type) {
                null -> openPlayTab(clearSelectedGame = true)
                Screen.XiangqiRouter.Type.Library -> openLibrary()
                is Screen.XiangqiRouter.Type.Game -> openGame(target.gameId)
                is Screen.XiangqiRouter.Type.Analysis -> openAnalysis(target.gameId, target.initialPly)
                is Screen.XiangqiRouter.Type.JoinOnlineRoom -> joinOnlineRoom(target.roomId)
            }
            else -> onNavigate(screen)
        }
    }

    private fun Screen.XiangqiRouter.Type?.toInitialRoute(): Route = when (this) {
        is Screen.XiangqiRouter.Type.Game -> Route.Game(gameId)
        is Screen.XiangqiRouter.Type.Analysis -> Route.Analysis(gameId, initialPly)
        Screen.XiangqiRouter.Type.Library -> Route.Library
        is Screen.XiangqiRouter.Type.JoinOnlineRoom -> Route.PlayHome
        null -> Route.PlayHome
    }

    private fun Screen.XiangqiRouter.Type?.initialGameId(): String? = when (this) {
        is Screen.XiangqiRouter.Type.Game -> gameId
        is Screen.XiangqiRouter.Type.Analysis -> gameId
        else -> null
    }

    private fun Screen.XiangqiRouter.Type?.initialJoinRoomId(): String = when (this) {
        is Screen.XiangqiRouter.Type.JoinOnlineRoom -> roomId.trim()
        else -> ""
    }

    private fun kotlinx.coroutines.flow.Flow<com.wanbaohe.xiangqi.application.port.outbound.AudioSettings>.mapToLegacy() =
        map { it.toLegacy() }

    private fun com.wanbaohe.xiangqi.application.port.outbound.AudioSettings.toLegacy() = XiangqiSettings(
        moveSoundUrl = moveSoundUrl,
        backgroundMusicUrl = backgroundMusicUrl,
        checkSoundUrl = checkSoundUrl,
        ttsEnabled = ttsEnabled,
        ttsTemplateTexts = ttsTemplateTexts,
    )

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            type: Screen.XiangqiRouter.Type?,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): XiangqiRouterComponent
    }
}
