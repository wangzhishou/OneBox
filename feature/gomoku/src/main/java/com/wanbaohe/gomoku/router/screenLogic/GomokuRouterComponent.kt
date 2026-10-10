package com.wanbaohe.gomoku.router.screenLogic

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
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
import com.wanbaohe.gomoku.application.usecase.CreateGameUseCase
import com.wanbaohe.gomoku.application.dto.GameAiPlayerConfig
import com.wanbaohe.gomoku.application.dto.GamePreparation
import com.wanbaohe.gomoku.application.dto.engineSlotFor
import com.wanbaohe.gomoku.domain.BoardSetupDraft
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.SetupPositionValidator
import com.wanbaohe.gomoku.domain.model.GameMode
import com.wanbaohe.gomoku.domain.model.PlayerType
import com.wanbaohe.gomoku.domain.model.Side
import com.wanbaohe.gomoku.component.GomokuGameUiState
import com.wanbaohe.gomoku.presentation.displayNames
import com.shifenmiao.base.utils.ActionUtils
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
    private val aiEngineCatalogManager: AIEngineCatalogManager,
    private val gameQuery: GameQueryUseCase,
    private val createGame: CreateGameUseCase,
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

    var preparation by mutableStateOf(GamePreparation())
        private set
    var preparationSetupDraft by mutableStateOf<BoardSetupDraft?>(null)
        private set
    var isStartingPreparation by mutableStateOf(false)
        private set
    var isRestoringRecentGame by mutableStateOf(type == null)
        private set
    private var preparationTouched = false

    fun prepareGame(game: GamePreparation) {
        preparationTouched = true
        pauseBeforeLeavingGame {
            preparation = game
            preparationSetupDraft = null
            selectedGameId = null
            navigation.pushToFront(Route.PlayHome)
        }
    }

    fun preparationAiConfig(side: Side): GameAiPlayerConfig {
        preparation.aiConfigFor(side)?.let { return it }
        val slot = preparation.setup.mode.engineSlotFor(side)
        val engine = when (slot) {
            EngineSlot.FAST -> aiEngineManager.getFastAiEngine()
            EngineSlot.DUEL_A -> aiEngineManager.getDuelEngineA()
            EngineSlot.DUEL_B -> aiEngineManager.getDuelEngineB()
        }
        return GameAiPlayerConfig.capture(gomokuAiConfig.value.sourceFor(slot), engine)
    }

    fun preparationUiState(): GomokuGameUiState {
        val black = preparationAiConfig(Side.BLACK).displayNames()
        val white = preparationAiConfig(Side.WHITE).displayNames()
        return GomokuGameUiState(
            title = preparation.title,
            mode = preparation.setup.mode,
            boardState = FenCodec.parse(preparation.initialFen),
            initialFen = preparation.initialFen,
            blackPlayerType = preparation.setup.playerTypeFor(Side.BLACK),
            whitePlayerType = preparation.setup.playerTypeFor(Side.WHITE),
            blackAiServiceName = black.first,
            blackAiModelName = black.second,
            whiteAiServiceName = white.first,
            whiteAiModelName = white.second,
            blackAiConfig = preparationAiConfig(Side.BLACK),
            whiteAiConfig = preparationAiConfig(Side.WHITE),
            isLoaded = !isRestoringRecentGame,
            isUpdating = isStartingPreparation,
            origin = preparation.origin,
        )
    }

    fun switchPreparationAi(side: Side, source: GomokuAiSource) {
        if (isStartingPreparation || source == preparationAiConfig(side).takeIf { it.isSupported }?.source) return
        preparationTouched = true
        val slot = preparation.setup.mode.engineSlotFor(side)
        val engine = when (slot) {
            EngineSlot.FAST -> aiEngineManager.getFastAiEngine()
            EngineSlot.DUEL_A -> aiEngineManager.getDuelEngineA()
            EngineSlot.DUEL_B -> aiEngineManager.getDuelEngineB()
        }
        preparation = preparation.withAiConfig(side, GameAiPlayerConfig.capture(source, engine))
        switchAiSource(slot, source)
    }

    fun switchPreparationModel(side: Side, engine: AiEngine, model: AiModel) {
        if (isStartingPreparation) return
        val config = GameAiPlayerConfig.capture(GomokuAiSource.WorkingModel, engine.copy(model = model))
        if (!config.isSupported) return
        val slot = preparation.setup.mode.engineSlotFor(side)
        when (slot) {
            EngineSlot.FAST -> aiEngineManager.switchFastModel(engine, model)
            EngineSlot.DUEL_A -> aiEngineManager.setDuelEngineA(engine.copy(model = model))
            EngineSlot.DUEL_B -> aiEngineManager.setDuelEngineB(engine.copy(model = model))
        }
        preparationTouched = true
        preparation = preparation.withAiConfig(side, config)
        switchAiSource(slot, GomokuAiSource.WorkingModel)
    }

    fun beginPreparationSetup() {
        if (isStartingPreparation || isRestoringRecentGame) return
        preparationTouched = true
        preparationSetupDraft = BoardSetupDraft(FenCodec.parse(preparation.initialFen))
    }

    fun updatePreparationSetup(draft: BoardSetupDraft) { preparationSetupDraft = draft }
    fun cancelPreparationSetup() { preparationSetupDraft = null }
    fun finishPreparationSetup() {
        val draft = preparationSetupDraft ?: return
        if (draft.hasChanges) {
            if (SetupPositionValidator.validate(draft.startPosition()) != null) {
                ActionUtils.showToast(R.string.gomoku_invalid_fen)
                return
            }
            preparation = preparation.copy(initialFen = FenCodec.encode(draft.startPosition()))
        }
        preparationSetupDraft = null
    }

    fun startPreparation() {
        if (isStartingPreparation || isRestoringRecentGame || preparationSetupDraft != null) return
        val draft = preparation
        if (SetupPositionValidator.validate(FenCodec.parse(draft.initialFen)) != null) {
            ActionUtils.showToast(R.string.gomoku_invalid_fen)
            return
        }
        preparationTouched = true
        isStartingPreparation = true
        componentScope.launch {
            val resolved = createGame.resolvePreparation(draft)
            val configs = Side.entries.filter { resolved.setup.playerTypeFor(it) == PlayerType.LLM }
                .mapNotNull { resolved.aiConfigFor(it) }
            if (configs.any { config ->
                    !config.isSupported || (config.source == GomokuAiSource.WorkingModel &&
                        aiEngineCatalogManager.getEngineByNameAndProtocol(config.engineName, config.engineProtocol)
                            ?.hasAvailableChatRoute() != true)
                }
            ) {
                isStartingPreparation = false
                ActionUtils.showToast(R.string.gomoku_ai_opponent_unavailable)
                return@launch
            }
            val start = {
                componentScope.launch {
                    try {
                        if (preparation == draft && childStack.value.active.configuration == Route.PlayHome &&
                            lifecycle.state >= Lifecycle.State.STARTED
                        ) {
                            val title = resolved.title.ifBlank { AppContext.getString(when {
                                resolved.origin != null -> R.string.gomoku_setup_practice
                                resolved.setup.mode == GameMode.LOCAL_PVP -> R.string.gomoku_mode_local
                                resolved.setup.mode == GameMode.LLM_VS_LLM -> R.string.gomoku_mode_ai_vs_ai
                                else -> R.string.gomoku_mode_ai
                            }) }
                            val gameId = createGame.createPrepared(resolved.copy(title = title))
                            openGame(gameId, startImmediately = true)
                        }
                    } finally {
                        isStartingPreparation = false
                    }
                }
                Unit
            }
            if (configs.any { it.source.requiresLogin }) {
                ActionUtils.ensureLoginAndCheckPoints(
                    source = "gomoku_preparation",
                    point = configs.maxOf { it.source.startPoints },
                    onLoginFailure = { isStartingPreparation = false },
                    onPointsFailure = { isStartingPreparation = false },
                    onSuccess = start,
                )
            } else start()
        }
    }

    val libraryComponent: GomokuLibraryComponent = libraryFactory(
        componentContext = componentContext.childContext("gomoku_library_shared"),
        onGoBack = ::navigateBack,
        onNavigate = ::handleInternalNavigation,
        onPrepareGame = ::prepareGame,
    )

    private val navigation = StackNavigation<Route>()
    private val gamesToStart = mutableSetOf<String>()

    val childStack: Value<ChildStack<Route, Child>> = childStack(
        source = navigation,
        serializer = Route.serializer(),
        initialConfiguration = type.toInitialRoute(),
        handleBackButton = false,
        childFactory = ::createChild,
    )

    private var selectedGameId: String? = childStack.value.items.asReversed().firstNotNullOfOrNull {
        when (val route = it.configuration) {
            is Route.Game -> route.gameId
            is Route.Analysis -> route.gameId
            else -> null
        }
    }

    var pendingJoinRoomId by mutableStateOf(type.initialJoinRoomId())
        private set

    init {
        maybeOpenRecentGame()
    }

    fun selectTab(tab: Tab) {
        when (val route = childStack.value.active.configuration) {
            is Route.Game -> selectedGameId = route.gameId
            is Route.Analysis -> selectedGameId = route.gameId
            else -> Unit
        }
        when (tab) {
            Tab.Play -> openPlayTab()
            Tab.Analyze -> openAnalyzeTab()
            Tab.Library -> openLibrary()
            Tab.Settings -> pauseBeforeLeavingGame { navigation.pushToFront(Route.Settings) }
        }
    }

    fun openLibrary() { pauseBeforeLeavingGame { navigation.pushToFront(Route.Library) } }
    fun openGame(gameId: String, startImmediately: Boolean = false) {
        selectedGameId = gameId
        val route = Route.Game(gameId)
        if (childStack.value.active.configuration == route) return
        if (startImmediately) gamesToStart.add(gameId)
        pauseBeforeLeavingGame {
            if (childStack.value.active.configuration is Route.Game &&
                childStack.value.items.none { it.configuration == route }
            ) navigation.replaceCurrent(route) else navigation.pushToFront(route)
        }
    }
    fun openAnalysis(gameId: String, initialPly: Int = -1) {
        selectedGameId = gameId
        pauseBeforeLeavingGame { navigation.pushToFront(Route.Analysis(gameId, initialPly)) }
    }
    fun joinOnlineRoom(roomId: String) { pendingJoinRoomId = roomId.trim() }
    fun clearPendingJoinRoom() { pendingJoinRoomId = "" }
    fun navigateBack() {
        val child = childStack.value.active.instance
        if (child is Child.Game && child.component.setupDraft != null) {
            child.component.cancelSetup()
            return
        }
        if (preparationSetupDraft != null && child == Child.PlayHome) {
            cancelPreparationSetup()
            return
        }
        pauseBeforeLeavingGame {
            if (childStack.value.items.size > 1) navigation.pop() else onGoBack()
        }
    }

    fun navigateBackFrom(route: Route) {
        navigateBack()
    }

    fun exitModule() { pauseBeforeLeavingGame(onGoBack) }

    private fun pauseBeforeLeavingGame(action: () -> Unit) {
        when (val child = childStack.value.active.instance) {
            is Child.Game -> child.component.pauseBeforeNavigating(action)
            is Child.Analysis -> { child.component.stopAutoPlay(); action() }
            else -> action()
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
        pauseBeforeLeavingGame { onNavigate(Screen.AISettings(Screen.AISettings.Type.WorkingModel)) }
    }
    fun openGomokuPromptSettings() {
        componentScope.launch {
            val prompt = promptDao.getSystemPromptByKey(PromptEntity.SYSTEM_PROMPT_KEY_GOMOKU_MOVE)
            if (prompt != null) {
                pauseBeforeLeavingGame { onNavigate(Screen.SystemPromptDetail(promptId = prompt.id)) }
            }
        }
    }
    fun openTTSConfigSettings() { pauseBeforeLeavingGame { onNavigate(Screen.TTSSettings) } }

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
        is Route.Game -> Child.Game(gameFactory(
            context, route.gameId, ::navigateBack, ::handleInternalNavigation, ::prepareGame, gamesToStart.remove(route.gameId),
        ))
        is Route.Analysis -> Child.Analysis(analysisFactory(
            context, route.gameId, route.initialPly, ::navigateBack, ::handleInternalNavigation, ::prepareGame,
        ))
    }

    private fun openPlayTab(clearSelectedGame: Boolean = false) {
        if (clearSelectedGame) selectedGameId = null
        val gameId = selectedGameId
        val route = if (gameId == null) Route.PlayHome else Route.Game(gameId)
        if (childStack.value.active.configuration == route) return
        pauseBeforeLeavingGame { navigation.pushToFront(route) }
    }

    private fun maybeOpenRecentGame() {
        if (type != null || selectedGameId != null || childStack.value.active.configuration != Route.PlayHome) {
            isRestoringRecentGame = false
            return
        }
        componentScope.launch {
            val recent = GameQueryUseCase.mostRecentUnfinishedHumanAiGame(gameQuery.observeAll().first())
            if (recent != null && !preparationTouched && childStack.value.active.configuration == Route.PlayHome) {
                selectedGameId = recent.id
                navigation.replaceCurrent(Route.Game(recent.id))
            }
            isRestoringRecentGame = false
        }
    }

    private fun openAnalyzeTab() {
        val child = childStack.value.active.instance
        val gameId = when (child) {
            is Child.Game -> child.component.gameId
            is Child.Analysis -> child.component.gameId
            else -> selectedGameId
        }
        when {
            child is Child.Game && child.component.gameId == gameId -> child.component.openAnalysis()
            gameId == null -> pauseBeforeLeavingGame { navigation.pushToFront(Route.AnalysisHome) }
            else -> openAnalysis(gameId)
        }
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
            else -> pauseBeforeLeavingGame { onNavigate(screen) }
        }
    }

    private fun Screen.GomokuRouter.Type?.toInitialRoute(): Route = when (this) {
        is Screen.GomokuRouter.Type.Game -> Route.Game(gameId)
        is Screen.GomokuRouter.Type.Analysis -> Route.Analysis(gameId, initialPly)
        Screen.GomokuRouter.Type.Library -> Route.Library
        is Screen.GomokuRouter.Type.JoinOnlineRoom -> Route.PlayHome
        null -> Route.PlayHome
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
