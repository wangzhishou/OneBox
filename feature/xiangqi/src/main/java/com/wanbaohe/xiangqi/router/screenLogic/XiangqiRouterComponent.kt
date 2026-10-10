package com.wanbaohe.xiangqi.router.screenLogic

import com.arkivanov.decompose.ComponentContext
import com.wanbaohe.xiangqi.data.local.LocalXiangqiEngine
import com.wanbaohe.xiangqi.data.local.XiangqiEngineWeights
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
import com.wanbaohe.xiangqi.application.audio.XiangqiAudioDefaults
import com.wanbaohe.xiangqi.application.port.outbound.EngineSlot
import com.wanbaohe.xiangqi.application.port.outbound.SoundPlayer
import com.wanbaohe.xiangqi.application.port.outbound.XiangqiAiConfig
import com.wanbaohe.xiangqi.application.port.outbound.XiangqiAiSource
import com.wanbaohe.xiangqi.application.port.outbound.XiangqiAiStore
import com.wanbaohe.xiangqi.application.usecase.GameQueryUseCase
import com.wanbaohe.xiangqi.application.usecase.CreateGameUseCase
import com.wanbaohe.xiangqi.application.dto.GameAiPlayerConfig
import com.wanbaohe.xiangqi.application.dto.GamePreparation
import com.wanbaohe.xiangqi.application.dto.engineSlotFor
import com.wanbaohe.xiangqi.component.XiangqiGameUiState
import com.wanbaohe.xiangqi.domain.BoardSetupDraft
import com.wanbaohe.xiangqi.domain.FenCodec
import com.wanbaohe.xiangqi.domain.SetupPositionValidator
import com.wanbaohe.xiangqi.domain.model.GameMode
import com.wanbaohe.xiangqi.domain.model.GameStatus
import com.wanbaohe.xiangqi.domain.model.PlayerType
import com.wanbaohe.xiangqi.domain.model.Side
import com.wanbaohe.xiangqi.presentation.displayNames
import com.shifenmiao.base.utils.ActionUtils
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
    private val xiangqiAiStore: XiangqiAiStore,
    aiEngineCatalogManager: AIEngineCatalogManager,
    private val gameQuery: GameQueryUseCase,
    private val createGame: CreateGameUseCase,
    private val promptDao: PromptDao,
    private val localEngine: LocalXiangqiEngine,
    private val engineWeights: XiangqiEngineWeights,
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

    val xiangqiAiConfig: StateFlow<XiangqiAiConfig> = xiangqiAiStore.observe()
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), XiangqiAiConfig())

    val allAiEngines: StateFlow<List<AiEngine>> =
        aiEngineCatalogManager.observeAvailableEngines()
            .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val modelsByProvider: StateFlow<Map<String, List<AiModel>>> =
        aiEngineCatalogManager.observeModelsByProvider()
            .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val ttsConfig: Flow<TTSConfig> = ttsService.observeConfig()

    private val _runningSettingsActions = MutableStateFlow<Set<SettingsAction>>(emptySet())
    val runningSettingsActions: StateFlow<Set<SettingsAction>> = _runningSettingsActions.asStateFlow()

    /**
     * 端侧象棋引擎(离线引擎)对设置页暴露的状态。
     *
     * [isPackaged] 是编译期事实(离线构建没放 jniLibs 时恒为 false),运行期不变;
     * 安装状态由 [XiangqiEngineWeights] 单例持有,所以退出设置页不会中断下载。
     */
    val isLocalEnginePackaged: Boolean = localEngine.isPackaged()
    val localEngineInstallState: StateFlow<XiangqiEngineWeights.InstallState> = engineWeights.state

    fun downloadLocalEngine() {
        engineWeights.startDownload()
    }

    fun cancelLocalEngineDownload() {
        engineWeights.cancelDownload()
    }

    /** 先回收引擎进程再删权重:进程可能还持有该文件的映射 */
    fun deleteLocalEngine() {
        localEngine.release()
        engineWeights.deleteWeights()
    }

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
        val slot = preparation.setup.mode.engineSlotFor(side)
        val engine = when (slot) {
            EngineSlot.FAST -> aiEngineManager.getFastAiEngine()
            EngineSlot.DUEL_A -> aiEngineManager.getDuelEngineA()
            EngineSlot.DUEL_B -> aiEngineManager.getDuelEngineB()
        }
        return preparation.aiConfigFor(side)
            ?: GameAiPlayerConfig.capture(xiangqiAiConfig.value.sourceFor(slot), engine)
    }

    fun preparationUiState(): XiangqiGameUiState {
        val red = preparationAiConfig(Side.RED).displayNames()
        val black = preparationAiConfig(Side.BLACK).displayNames()
        return XiangqiGameUiState(
            title = preparation.title,
            mode = preparation.setup.mode,
            boardState = FenCodec.parse(preparation.initialFen),
            initialFen = preparation.initialFen,
            status = GameStatus.NOT_STARTED,
            redPlayerType = preparation.setup.playerTypeFor(Side.RED),
            blackPlayerType = preparation.setup.playerTypeFor(Side.BLACK),
            redAiServiceName = red.first,
            redAiModelName = red.second,
            blackAiServiceName = black.first,
            blackAiModelName = black.second,
            isLoaded = !isRestoringRecentGame,
            isUpdating = isStartingPreparation,
            origin = preparation.origin,
        )
    }

    fun switchPreparationAi(side: Side, source: XiangqiAiSource) {
        if (source == preparationAiConfig(side).source) return
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
        val slot = preparation.setup.mode.engineSlotFor(side)
        when (slot) {
            EngineSlot.FAST -> aiEngineManager.switchFastModel(engine, model)
            EngineSlot.DUEL_A -> aiEngineManager.setDuelEngineA(engine.copy(model = model))
            EngineSlot.DUEL_B -> aiEngineManager.setDuelEngineB(engine.copy(model = model))
        }
        preparationTouched = true
        preparation = preparation.withAiConfig(
            side, GameAiPlayerConfig.capture(XiangqiAiSource.WorkingModel, engine.copy(model = model)),
        )
        switchAiSource(slot, XiangqiAiSource.WorkingModel)
    }

    fun beginPreparationSetup() {
        preparationTouched = true
        preparationSetupDraft = BoardSetupDraft(FenCodec.parse(preparation.initialFen))
    }

    fun updatePreparationSetup(draft: BoardSetupDraft) {
        preparationSetupDraft = draft
    }

    fun cancelPreparationSetup() {
        preparationSetupDraft = null
    }

    fun finishPreparationSetup() {
        val draft = preparationSetupDraft ?: return
        if (draft.hasChanges) {
            if (SetupPositionValidator.validate(draft.startPosition()) != null) {
                ActionUtils.showToast(R.string.xiangqi_setup_invalid)
                return
            }
            preparation = preparation.copy(initialFen = FenCodec.encode(draft.startPosition()))
        }
        preparationSetupDraft = null
    }

    fun startPreparation() {
        if (isStartingPreparation || isRestoringRecentGame) return
        val draft = preparation
        if (SetupPositionValidator.validate(FenCodec.parse(draft.initialFen)) != null) {
            ActionUtils.showToast(R.string.xiangqi_setup_invalid)
            return
        }
        preparationTouched = true
        isStartingPreparation = true
        componentScope.launch {
            val resolved = createGame.resolvePreparation(draft)
            val configs = listOfNotNull(resolved.redAiConfig, resolved.blackAiConfig)
            if (configs.any { it.source == XiangqiAiSource.LocalEngine } &&
                (!isLocalEnginePackaged || localEngineInstallState.value !is XiangqiEngineWeights.InstallState.Installed)
            ) {
                isStartingPreparation = false
                ActionUtils.showToast(R.string.xiangqi_setup_local_engine_required)
                return@launch
            }
            val start = {
                componentScope.launch {
                    if (preparation == draft && childStack.value.active.configuration == Route.PlayHome) {
                        val title = resolved.title.ifBlank {
                            AppContext.getString(
                                when {
                                    resolved.origin != null -> R.string.xiangqi_setup_practice
                                    resolved.setup.mode == GameMode.LOCAL_PVP -> R.string.xiangqi_mode_local
                                    resolved.setup.mode == GameMode.LLM_VS_LLM -> R.string.xiangqi_mode_ai_vs_ai
                                    else -> R.string.xiangqi_mode_ai
                                },
                            )
                        }
                        val gameId = createGame.createPrepared(resolved.copy(title = title))
                        openGame(gameId, startImmediately = true)
                    }
                    isStartingPreparation = false
                }
                Unit
            }
            if (configs.any { it.source.requiresLogin }) {
                ActionUtils.ensureLoginAndCheckPoints(
                    source = "xiangqi_preparation",
                    point = configs.maxOf { it.source.startPoints },
                    onLoginFailure = {
                        isStartingPreparation = false
                        ActionUtils.showToast(com.shifenmiao.core.R.string.login_failed)
                    },
                    onPointsFailure = { isStartingPreparation = false },
                    onSuccess = start,
                )
            } else {
                start()
            }
        }
    }

    val libraryComponent: XiangqiLibraryComponent = libraryFactory(
        componentContext = componentContext.childContext("xiangqi_library_shared"),
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
        if (startImmediately) gamesToStart.add(gameId)
        val route = Route.Game(gameId)
        if (childStack.value.active.configuration == route) return
        pauseBeforeLeavingGame {
            if (childStack.value.active.configuration is Route.Game &&
                childStack.value.items.none { it.configuration == route }
            ) {
                navigation.replaceCurrent(route)
            } else {
                navigation.pushToFront(route)
            }
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

    private fun pauseBeforeLeavingGame(onPaused: () -> Unit) {
        val child = childStack.value.active.instance
        if (child is Child.Game) child.component.pauseBeforeNavigating(onPaused) else onPaused()
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

    fun switchAiSource(slot: EngineSlot, source: XiangqiAiSource) {
        componentScope.launch {
            xiangqiAiStore.update(xiangqiAiStore.get().withSource(slot, source))
        }
    }

    fun openAiModelSettings() {
        onNavigate(Screen.AISettings(Screen.AISettings.Type.WorkingModel))
    }
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
        defaultUrl = XiangqiAudioDefaults.MOVE,
    )

    fun previewCheckSound() = previewSound(
        action = SettingsAction.PreviewCheckSound,
        url = xiangqiSettings.value.checkSoundUrl,
        defaultUrl = XiangqiAudioDefaults.CHECK,
    )

    fun previewBackgroundMusic() {
        runSettingsAction(SettingsAction.PreviewBackgroundMusic) {
            // 没填 URL 就试听内置的默认 BGM，否则点"试听"什么也听不到
            val url = xiangqiSettings.value.backgroundMusicUrl
                .ifBlank { XiangqiAudioDefaults.BACKGROUND }
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
            if (recent != null && !preparationTouched && childStack.value.active.configuration == Route.PlayHome) {
                selectedGameId = recent.id
                navigation.replaceCurrent(Route.Game(recent.id))
            }
            isRestoringRecentGame = false
        }
    }

    private fun openAnalyzeTab() {
        val gameId = selectedGameId
        val child = childStack.value.active.instance
        when {
            child is Child.Game && child.component.gameId == gameId -> child.component.openAnalysis()
            gameId == null -> navigation.pushToFront(Route.AnalysisHome)
            else -> openAnalysis(gameId)
        }
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
        soundEnabled = soundEnabled,
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
