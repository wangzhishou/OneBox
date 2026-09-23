package com.wanbaohe.gomoku.router.screenLogic

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
import com.wanbaohe.gomoku.application.audio.GomokuAudioDefaults
import com.wanbaohe.gomoku.application.port.outbound.EngineSlot
import com.wanbaohe.gomoku.application.port.outbound.SoundPlayer
import com.wanbaohe.gomoku.application.port.outbound.GomokuAiConfig
import com.wanbaohe.gomoku.application.port.outbound.GomokuAiSource
import com.wanbaohe.gomoku.application.port.outbound.GomokuAiStore
import com.wanbaohe.gomoku.application.usecase.GameQueryUseCase
import com.wanbaohe.gomoku.application.usecase.SettingsUseCase
import com.wanbaohe.gomoku.component.GomokuAnalysisComponent
import com.wanbaohe.gomoku.component.GomokuGameComponent
import com.wanbaohe.gomoku.component.GomokuLibraryComponent
import com.wanbaohe.gomoku.data.GomokuSettings
import com.wanbaohe.gomoku.data.GomokuTTSTemplate
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.gomoku.R
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

class GomokuRouterComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val type: Screen.GomokuRouter.Type?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    libraryFactory: GomokuLibraryComponent.Factory,
    private val gameFactory: GomokuGameComponent.Factory,
    private val analysisFactory: GomokuAnalysisComponent.Factory,
    private val settingsUseCase: SettingsUseCase,
    private val soundPlayer: SoundPlayer,
    private val ttsService: TTSService,
    private val aiEngineManager: AIEngineManager,
    private val gomokuAiStore: GomokuAiStore,
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
        data class Library(val component: GomokuLibraryComponent) : Child
        data class Game(val component: GomokuGameComponent) : Child
        data class Analysis(val component: GomokuAnalysisComponent) : Child
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

    val gomokuSettings: StateFlow<GomokuSettings> = settingsUseCase.observe()
        .mapToLegacy()
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), GomokuSettings())

    val currentAIEngine: StateFlow<AiEngine> = aiEngineManager.fastAIEngine
    val duelEngineA: StateFlow<AiEngine> = aiEngineManager.duelEngineA
    val duelEngineB: StateFlow<AiEngine> = aiEngineManager.duelEngineB

    val gomokuAiConfig: StateFlow<GomokuAiConfig> = gomokuAiStore.observe()
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), GomokuAiConfig())

    val allAiEngines: StateFlow<List<AiEngine>> =
        aiEngineCatalogManager.observeAvailableEngines()
            .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val modelsByProvider: StateFlow<Map<String, List<AiModel>>> =
        aiEngineCatalogManager.observeModelsByProvider()
            .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val ttsConfig: Flow<TTSConfig> = ttsService.observeConfig()

    private val _runningSettingsActions = MutableStateFlow<Set<SettingsAction>>(emptySet())
    val runningSettingsActions: StateFlow<Set<SettingsAction>> = _runningSettingsActions.asStateFlow()

    val libraryComponent: GomokuLibraryComponent = libraryFactory(
        componentContext = componentContext.childContext("gomoku_library_shared"),
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
    fun updateSoundEnabled(enabled: Boolean) {
        componentScope.launch { settingsUseCase.updateSoundEnabled(enabled) }
        if (!enabled) soundPlayer.stopBackground()
    }
    fun updateTTSTemplateText(tag: String, text: String) { componentScope.launch { settingsUseCase.updateTTSTemplateText(tag, text) } }

    fun switchAiModel(engine: AiEngine, model: AiModel) { aiEngineManager.switchFastModel(engine, model) }
    fun switchDuelEngineA(engine: AiEngine, model: AiModel) { aiEngineManager.setDuelEngineA(engine.copy(model = model)) }
    fun switchDuelEngineB(engine: AiEngine, model: AiModel) { aiEngineManager.setDuelEngineB(engine.copy(model = model)) }

    fun switchAiSource(slot: EngineSlot, source: GomokuAiSource) {
        componentScope.launch {
            gomokuAiStore.update(gomokuAiStore.get().withSource(slot, source))
        }
    }

    fun openAiModelSettings() {
        onNavigate(Screen.AISettings(Screen.AISettings.Type.WorkingModel))
    }
    fun openGomokuPromptSettings() {
        componentScope.launch {
            val prompt = promptDao.getSystemPromptByKey(PromptEntity.SYSTEM_PROMPT_KEY_GOMOKU_MOVE)
            if (prompt != null) {
                onNavigate(Screen.SystemPromptDetail(promptId = prompt.id))
            }
        }
    }
    fun openTTSConfigSettings() { onNavigate(Screen.TTSSettings) }

    fun generateTTS(template: GomokuTTSTemplate, customText: String) {
        runSettingsAction(SettingsAction.GenerateTTS(template.tag)) {
            val text = customText.ifBlank { template.defaultText }
            ttsService.synthesize(text = text, tag = template.tag)
        }
    }

    fun regenerateTTS(template: GomokuTTSTemplate, customText: String) {
        runSettingsAction(SettingsAction.RegenerateTTS(template.tag)) {
            val text = customText.ifBlank { template.defaultText }
            ttsService.regenerate(text = text, tag = template.tag)
        }
    }

    fun playTTSAudio(template: GomokuTTSTemplate, customText: String) {
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
            ttsService.synthesize(text = AppContext.getString(R.string.gomoku_tts_test_text), tag = "tts-test")
                .onSuccess { file -> soundPlayer.playLocalFile(file) }
        }
    }

    fun previewMoveSound() = previewSound(
        action = SettingsAction.PreviewMoveSound,
        url = gomokuSettings.value.moveSoundUrl,
        defaultUrl = GomokuAudioDefaults.MOVE,
    )

    fun previewWinSound() = previewSound(
        action = SettingsAction.PreviewCheckSound,
        url = gomokuSettings.value.checkSoundUrl,
        defaultUrl = GomokuAudioDefaults.WIN,
    )

    fun previewBackgroundMusic() {
        runSettingsAction(SettingsAction.PreviewBackgroundMusic) {
            // 没填 URL 就试听内置的默认 BGM，否则点"试听"什么也听不到
            val url = gomokuSettings.value.backgroundMusicUrl
                .ifBlank { GomokuAudioDefaults.BACKGROUND }
            soundPlayer.playBackground(url)
        }
    }

    fun stopBackgroundMusicPreview() {
        runSettingsAction(SettingsAction.StopBackgroundMusic) {
            soundPlayer.stopBackground()
        }
    }

    private fun previewSound(action: SettingsAction, url: String, defaultUrl: String) {
        runSettingsAction(action) {
            // 没填 URL 就试听内置默认音, 否则点"试听"什么也听不到
            soundPlayer.playEffect(url.ifBlank { defaultUrl })
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
            is Screen.GomokuRouter -> when (val target = screen.type) {
                null -> openPlayTab(clearSelectedGame = true)
                Screen.GomokuRouter.Type.Library -> openLibrary()
                is Screen.GomokuRouter.Type.Game -> openGame(target.gameId)
                is Screen.GomokuRouter.Type.Analysis -> openAnalysis(target.gameId, target.initialPly)
                is Screen.GomokuRouter.Type.JoinOnlineRoom -> joinOnlineRoom(target.roomId)
            }
            else -> onNavigate(screen)
        }
    }

    private fun Screen.GomokuRouter.Type?.toInitialRoute(): Route = when (this) {
        is Screen.GomokuRouter.Type.Game -> Route.Game(gameId)
        is Screen.GomokuRouter.Type.Analysis -> Route.Analysis(gameId, initialPly)
        Screen.GomokuRouter.Type.Library -> Route.Library
        is Screen.GomokuRouter.Type.JoinOnlineRoom -> Route.PlayHome
        null -> Route.PlayHome
    }

    private fun Screen.GomokuRouter.Type?.initialGameId(): String? = when (this) {
        is Screen.GomokuRouter.Type.Game -> gameId
        is Screen.GomokuRouter.Type.Analysis -> gameId
        else -> null
    }

    private fun Screen.GomokuRouter.Type?.initialJoinRoomId(): String = when (this) {
        is Screen.GomokuRouter.Type.JoinOnlineRoom -> roomId.trim()
        else -> ""
    }

    private fun kotlinx.coroutines.flow.Flow<com.wanbaohe.gomoku.application.port.outbound.AudioSettings>.mapToLegacy() =
        map { it.toLegacy() }

    private fun com.wanbaohe.gomoku.application.port.outbound.AudioSettings.toLegacy() = GomokuSettings(
        moveSoundUrl = moveSoundUrl,
        backgroundMusicUrl = backgroundMusicUrl,
        checkSoundUrl = checkSoundUrl,
        ttsEnabled = ttsEnabled,
        ttsTemplateTexts = ttsTemplateTexts,
        soundEnabled = soundEnabled,
    )

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            type: Screen.GomokuRouter.Type?,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): GomokuRouterComponent
    }
}
