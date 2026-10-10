package com.wanbaohe.chess.router.screenLogic

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnStop
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.database.chat_prompt.dao.PromptDao
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.tts.TTSConfig
import com.shifenmiao.tts.service.TTSService
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.chess.application.audio.ChessAudioDefaults
import com.wanbaohe.chess.application.port.outbound.EngineSlot
import com.wanbaohe.chess.application.port.outbound.SoundPlayer
import com.wanbaohe.chess.application.port.outbound.ChessAiConfig
import com.wanbaohe.chess.application.port.outbound.ChessAiSource
import com.wanbaohe.chess.application.port.outbound.ChessAiStore
import com.wanbaohe.chess.application.usecase.GameQueryUseCase
import com.wanbaohe.chess.application.usecase.CreateGameUseCase
import com.wanbaohe.chess.application.dto.GameAiPlayerConfig
import com.wanbaohe.chess.application.dto.GamePreparation
import com.wanbaohe.chess.application.dto.engineSlotFor
import com.wanbaohe.chess.component.ChessGameUiState
import com.wanbaohe.chess.domain.BoardSetupDraft
import com.wanbaohe.chess.domain.FenCodec
import com.wanbaohe.chess.domain.SetupPositionValidator
import com.wanbaohe.chess.domain.model.GameMode
import com.wanbaohe.chess.domain.model.GameStatus
import com.wanbaohe.chess.domain.model.PlayerType
import com.wanbaohe.chess.domain.model.Side
import com.wanbaohe.chess.presentation.displayNames
import com.wanbaohe.chess.application.usecase.SettingsUseCase
import com.wanbaohe.chess.component.ChessAnalysisComponent
import com.wanbaohe.chess.component.ChessGameComponent
import com.wanbaohe.chess.component.ChessLibraryComponent
import com.wanbaohe.chess.data.ChessSettings
import com.wanbaohe.chess.data.ChessTTSTemplate
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.chess.R
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
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

class ChessRouterComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val type: Screen.ChessRouter.Type?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    libraryFactory: ChessLibraryComponent.Factory,
    private val gameFactory: ChessGameComponent.Factory,
    private val analysisFactory: ChessAnalysisComponent.Factory,
    private val settingsUseCase: SettingsUseCase,
    private val soundPlayer: SoundPlayer,
    private val ttsService: TTSService,
    private val aiEngineManager: AIEngineManager,
    private val chessAiStore: ChessAiStore,
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
        data class Library(val component: ChessLibraryComponent) : Child
        data class Game(val component: ChessGameComponent) : Child
        data class Analysis(val component: ChessAnalysisComponent) : Child
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

    val chessSettings: StateFlow<ChessSettings> = settingsUseCase.observe()
        .mapToLegacy()
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), ChessSettings())

    val currentAIEngine: StateFlow<AiEngine> = aiEngineManager.fastAIEngine
    val duelEngineA: StateFlow<AiEngine> = aiEngineManager.duelEngineA
    val duelEngineB: StateFlow<AiEngine> = aiEngineManager.duelEngineB

    val chessAiConfig: StateFlow<ChessAiConfig> = chessAiStore.observe()
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), ChessAiConfig())

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
    private var preparationStartAttempt = 0L

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
        val slot = preparation.setup.mode.engineSlotFor(side)
        val engine = when (slot) {
            EngineSlot.FAST -> aiEngineManager.getFastAiEngine()
            EngineSlot.DUEL_A -> aiEngineManager.getDuelEngineA()
            EngineSlot.DUEL_B -> aiEngineManager.getDuelEngineB()
        }
        return preparation.aiConfigFor(side) ?: GameAiPlayerConfig.capture(chessAiConfig.value.sourceFor(slot), engine)
    }

    fun preparationUiState(): ChessGameUiState {
        val white = preparationAiConfig(Side.WHITE).displayNames()
        val black = preparationAiConfig(Side.BLACK).displayNames()
        return ChessGameUiState(
            title = preparation.title,
            mode = preparation.setup.mode,
            boardState = FenCodec.parse(preparation.initialFen),
            initialFen = preparation.initialFen,
            status = GameStatus.NOT_STARTED,
            whitePlayerType = preparation.setup.playerTypeFor(Side.WHITE),
            blackPlayerType = preparation.setup.playerTypeFor(Side.BLACK),
            whiteAiServiceName = white.first,
            whiteAiModelName = white.second,
            blackAiServiceName = black.first,
            blackAiModelName = black.second,
            isLoaded = !isRestoringRecentGame,
            isUpdating = isStartingPreparation,
            origin = preparation.origin,
        )
    }

    fun switchPreparationAi(side: Side, source: ChessAiSource) {
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
        val slot = preparation.setup.mode.engineSlotFor(side)
        when (slot) {
            EngineSlot.FAST -> aiEngineManager.switchFastModel(engine, model)
            EngineSlot.DUEL_A -> aiEngineManager.setDuelEngineA(engine.copy(model = model))
            EngineSlot.DUEL_B -> aiEngineManager.setDuelEngineB(engine.copy(model = model))
        }
        preparationTouched = true
        preparation = preparation.withAiConfig(
            side, GameAiPlayerConfig.capture(ChessAiSource.WorkingModel, engine.copy(model = model)),
        )
        switchAiSource(slot, ChessAiSource.WorkingModel)
    }

    fun beginPreparationSetup() {
        if (isStartingPreparation || isRestoringRecentGame) return
        preparationTouched = true
        preparationSetupDraft = BoardSetupDraft(FenCodec.parse(preparation.initialFen))
    }

    fun updatePreparationSetup(draft: BoardSetupDraft) {
        if (!isStartingPreparation) preparationSetupDraft = draft
    }

    fun cancelPreparationSetup() { preparationSetupDraft = null }

    fun finishPreparationSetup() {
        val draft = preparationSetupDraft ?: return
        if (draft.hasChanges) {
            if (SetupPositionValidator.validate(draft.startPosition()) != null) {
                ActionUtils.showToast(R.string.chess_setup_invalid)
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
            ActionUtils.showToast(R.string.chess_setup_invalid)
            return
        }
        preparationTouched = true
        isStartingPreparation = true
        val attempt = ++preparationStartAttempt
        componentScope.launch {
            try {
                val resolved = createGame.resolvePreparation(draft)
                val configs = listOfNotNull(resolved.whiteAiConfig, resolved.blackAiConfig)
                if (configs.any { !it.isSupported }) {
                    finishPreparationStart(attempt)
                    ActionUtils.showToast(R.string.chess_ai_opponent_unavailable)
                    return@launch
                }
                for (config in configs.filter { it.source == ChessAiSource.WorkingModel }) {
                    if (!isCurrentPreparationStart(attempt, draft)) return@launch
                    val engine = if (config.engineName.isBlank() || config.engineProtocol.isBlank()) null
                        else aiEngineCatalogManager.getEngineByNameAndProtocol(config.engineName, config.engineProtocol)
                    if (!isCurrentPreparationStart(attempt, draft)) return@launch
                    if (config.model?.name.isNullOrBlank() || engine == null ||
                        engine.requestProtocol.name != config.engineProtocol ||
                        engine.requestProtocol == AiRequestProtocol.LOCAL_ON_DEVICE || engine.requestProtocol.isNonChat
                    ) {
                        finishPreparationStart(attempt)
                        ActionUtils.showToast(R.string.chess_ai_opponent_unavailable)
                        return@launch
                    }
                    val models = aiEngineCatalogManager.observeModelsByProvider().first()[engine.name.lowercase()].orEmpty()
                    if (!isCurrentPreparationStart(attempt, draft)) return@launch
                    if (models.none { it.name == config.model?.name }) {
                        finishPreparationStart(attempt)
                        ActionUtils.showToast(R.string.chess_ai_opponent_unavailable)
                        return@launch
                    }
                }
                val start = {
                    componentScope.launch {
                        try {
                            if (isCurrentPreparationStart(attempt, draft)) {
                                val title = resolved.title.ifBlank {
                                    AppContext.getString(when {
                                        resolved.origin != null -> R.string.chess_setup_practice
                                        resolved.setup.mode == GameMode.LOCAL_PVP -> R.string.chess_mode_local
                                        resolved.setup.mode == GameMode.LLM_VS_LLM -> R.string.chess_mode_ai_vs_ai
                                        else -> R.string.chess_mode_ai
                                    })
                                }
                                val gameId = createGame.createPrepared(resolved.copy(title = title))
                                if (isCurrentPreparationStart(attempt, draft)) openGame(gameId, startImmediately = true)
                            }
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (_: Exception) {
                            if (attempt == preparationStartAttempt) ActionUtils.showToast(R.string.chess_setup_start_failed)
                        } finally {
                            finishPreparationStart(attempt)
                        }
                    }
                    Unit
                }
                if (configs.any { it.source.requiresLogin }) {
                    ActionUtils.ensureLoginAndCheckPoints(
                        source = "chess_preparation",
                        point = configs.maxOf { it.source.startPoints },
                        onLoginFailure = {
                            finishPreparationStart(attempt)
                            ActionUtils.showToast(com.shifenmiao.core.R.string.login_failed)
                        },
                        onPointsFailure = { finishPreparationStart(attempt) },
                        onSuccess = start,
                    )
                } else start()
            } catch (cancelled: CancellationException) {
                finishPreparationStart(attempt)
                throw cancelled
            } catch (_: Exception) {
                if (attempt == preparationStartAttempt) ActionUtils.showToast(R.string.chess_setup_start_failed)
                finishPreparationStart(attempt)
            }
        }
    }

    private fun isCurrentPreparationStart(attempt: Long, draft: GamePreparation): Boolean =
        attempt == preparationStartAttempt && isStartingPreparation && preparation == draft &&
            lifecycle.state >= Lifecycle.State.STARTED && childStack.value.active.configuration == Route.PlayHome

    private fun finishPreparationStart(attempt: Long) {
        if (attempt == preparationStartAttempt) isStartingPreparation = false
    }

    private fun cancelPreparationStart() {
        preparationStartAttempt++
        isStartingPreparation = false
    }

    val libraryComponent: ChessLibraryComponent = libraryFactory(
        componentContext = componentContext.childContext("chess_library_shared"),
        onGoBack = ::navigateBack,
        onNavigate = ::handleInternalNavigation,
        onPrepareGame = ::prepareGame,
        onBeforeModal = ::beforeOpeningModal,
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
        lifecycle.doOnStop {
            cancelPreparationStart()
            gamesToStart.clear()
        }
        maybeOpenRecentGame()
    }

    fun selectTab(tab: Tab) {
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
    fun joinOnlineRoom(roomId: String) {
        pauseBeforeLeavingGame { pendingJoinRoomId = roomId.trim() }
    }
    fun clearPendingJoinRoom() { pendingJoinRoomId = "" }
    fun navigateBack() {
        val child = childStack.value.active.instance
        if (child is Child.Game && child.component.setupDraft != null) {
            child.component.cancelSetup()
            return
        }
        if (child == Child.PlayHome && preparationSetupDraft != null) {
            cancelPreparationSetup()
            return
        }
        pauseBeforeLeavingGame {
            if (childStack.value.items.size > 1) navigation.pop() else onGoBack()
        }
    }

    fun navigateBackFrom(route: Route) { navigateBack() }

    private fun pauseBeforeLeavingGame(onPaused: () -> Unit) {
        val child = childStack.value.active.instance
        if (child == Child.PlayHome && isStartingPreparation) cancelPreparationStart()
        if (child is Child.Game) child.component.pauseBeforeNavigating(onPaused) else onPaused()
    }

    private fun beforeOpeningModal(onReady: () -> Unit) {
        preparationTouched = true
        val child = childStack.value.active.instance
        if (child is Child.Game) child.component.pauseForModal(onReady)
        else {
            if (child == Child.PlayHome) cancelPreparationStart()
            onReady()
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

    fun switchAiSource(slot: EngineSlot, source: ChessAiSource) {
        componentScope.launch {
            chessAiStore.update(chessAiStore.get().withSource(slot, source))
        }
    }

    fun openAiModelSettings() {
        pauseBeforeLeavingGame { onNavigate(Screen.AISettings(Screen.AISettings.Type.WorkingModel)) }
    }
    fun openChessPromptSettings() {
        componentScope.launch {
            val prompt = promptDao.getSystemPromptByKey(PromptEntity.SYSTEM_PROMPT_KEY_CHESS_MOVE)
            if (prompt != null) {
                pauseBeforeLeavingGame { onNavigate(Screen.SystemPromptDetail(promptId = prompt.id)) }
            }
        }
    }
    fun openTTSConfigSettings() { pauseBeforeLeavingGame { onNavigate(Screen.TTSSettings) } }

    fun generateTTS(template: ChessTTSTemplate, customText: String) {
        runSettingsAction(SettingsAction.GenerateTTS(template.tag)) {
            val text = customText.ifBlank { template.defaultText }
            ttsService.synthesize(text = text, tag = template.tag)
        }
    }

    fun regenerateTTS(template: ChessTTSTemplate, customText: String) {
        runSettingsAction(SettingsAction.RegenerateTTS(template.tag)) {
            val text = customText.ifBlank { template.defaultText }
            ttsService.regenerate(text = text, tag = template.tag)
        }
    }

    fun playTTSAudio(template: ChessTTSTemplate, customText: String) {
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
            ttsService.synthesize(text = AppContext.getString(R.string.chess_tts_test_text), tag = "tts-test")
                .onSuccess { file -> soundPlayer.playLocalFile(file) }
        }
    }

    fun previewMoveSound() = previewSound(
        action = SettingsAction.PreviewMoveSound,
        url = chessSettings.value.moveSoundUrl,
        defaultUrl = ChessAudioDefaults.MOVE,
    )

    fun previewCheckSound() = previewSound(
        action = SettingsAction.PreviewCheckSound,
        url = chessSettings.value.checkSoundUrl,
        defaultUrl = ChessAudioDefaults.CHECK,
    )

    fun previewBackgroundMusic() {
        runSettingsAction(SettingsAction.PreviewBackgroundMusic) {
            // 没填 URL 就试听内置的默认 BGM，否则点"试听"什么也听不到
            val url = chessSettings.value.backgroundMusicUrl
                .ifBlank { ChessAudioDefaults.BACKGROUND }
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
        if (type != null) return
        componentScope.launch {
            val recent = GameQueryUseCase.mostRecentUnfinishedHumanAiGame(gameQuery.observeAll().first())
            if (recent != null && !preparationTouched && childStack.value.active.configuration == Route.PlayHome &&
                selectedGameId == null
            ) {
                selectedGameId = recent.id
                navigation.replaceCurrent(Route.Game(recent.id))
            }
            isRestoringRecentGame = false
        }
    }

    private fun openAnalyzeTab() {
        val gameId = selectedGameId
        if (gameId == null) pauseBeforeLeavingGame { navigation.pushToFront(Route.AnalysisHome) }
        else openAnalysis(gameId)
    }

    private fun handleInternalNavigation(screen: Screen) {
        when (screen) {
            is Screen.ChessRouter -> when (val target = screen.type) {
                null -> openPlayTab(clearSelectedGame = true)
                Screen.ChessRouter.Type.Library -> openLibrary()
                is Screen.ChessRouter.Type.Game -> openGame(target.gameId)
                is Screen.ChessRouter.Type.Analysis -> openAnalysis(target.gameId, target.initialPly)
                is Screen.ChessRouter.Type.JoinOnlineRoom -> joinOnlineRoom(target.roomId)
            }
            else -> pauseBeforeLeavingGame { onNavigate(screen) }
        }
    }

    private fun Screen.ChessRouter.Type?.toInitialRoute(): Route = when (this) {
        is Screen.ChessRouter.Type.Game -> Route.Game(gameId)
        is Screen.ChessRouter.Type.Analysis -> Route.Analysis(gameId, initialPly)
        Screen.ChessRouter.Type.Library -> Route.Library
        is Screen.ChessRouter.Type.JoinOnlineRoom -> Route.PlayHome
        null -> Route.PlayHome
    }

    private fun Screen.ChessRouter.Type?.initialGameId(): String? = when (this) {
        is Screen.ChessRouter.Type.Game -> gameId
        is Screen.ChessRouter.Type.Analysis -> gameId
        else -> null
    }

    private fun Screen.ChessRouter.Type?.initialJoinRoomId(): String = when (this) {
        is Screen.ChessRouter.Type.JoinOnlineRoom -> roomId.trim()
        else -> ""
    }

    private fun kotlinx.coroutines.flow.Flow<com.wanbaohe.chess.application.port.outbound.AudioSettings>.mapToLegacy() =
        map { it.toLegacy() }

    private fun com.wanbaohe.chess.application.port.outbound.AudioSettings.toLegacy() = ChessSettings(
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
            type: Screen.ChessRouter.Type?,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): ChessRouterComponent
    }
}
