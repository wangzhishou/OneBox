package com.wanbaohe.gomoku.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.shifenmiao.base.audio.NetworkAudioPlayer
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.gomoku.application.audio.GomokuAudioDefaults
import com.wanbaohe.gomoku.application.port.outbound.AudioSettings
import com.wanbaohe.gomoku.application.port.outbound.SoundPlayer
import com.wanbaohe.gomoku.application.usecase.AudioFeedbackUseCase
import com.wanbaohe.gomoku.application.usecase.ExportGameUseCase
import com.wanbaohe.gomoku.application.usecase.GameQueryUseCase
import com.wanbaohe.gomoku.application.usecase.SettingsUseCase
import com.wanbaohe.gomoku.data.GomokuPlyRecord
import com.wanbaohe.gomoku.data.TextExportLabels
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.model.BoardState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class GomokuAnalysisUiState(
    val title: String = "",
    val boardState: BoardState = FenCodec.parse(FenCodec.INITIAL_FEN),
    val currentPly: Int = 0,
    val maxPly: Int = 0,
    val plies: List<GomokuPlyRecord> = emptyList(),
    val exportContent: String = "",
    /**
     * 落库的结果码（[com.wanbaohe.gomoku.domain.GameResultCode]），与回放进度无关，整局恒定。
     *
     * 结果的唯一来源。**不要**改用当前回放局面重算的状态：认输局在任何一手的盘面上
     * 都推不出 RESIGNED，用它会静默显示"未结束"。
     */
    val resultText: String = "",
    val isAutoPlaying: Boolean = false,
    /** 声音总开关：关掉之后音效和背景音乐一起静音 */
    val isSoundOn: Boolean = true,
)

class GomokuAnalysisComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val gameId: String,
    @Assisted initialPly: Int,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val gameQuery: GameQueryUseCase,
    private val exportGame: ExportGameUseCase,
    private val audioFeedback: AudioFeedbackUseCase,
    private val settingsUseCase: SettingsUseCase,
    private val soundPlayer: SoundPlayer,
    private val audioPlayer: NetworkAudioPlayer,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    var uiState by mutableStateOf(GomokuAnalysisUiState())
        private set

    private var targetInitialPly by mutableIntStateOf(initialPly)
    private var autoPlayJob: Job? = null

    /**
     * 声音开关（复盘页顶上的小喇叭）。
     *
     * 关掉时 BGM 和在放的音效一起停；再打开时 BGM 重新起。
     * 状态写进设置，下次进复盘页保持关闭。
     */
    fun toggleSound() {
        val next = !uiState.isSoundOn
        uiState = uiState.copy(isSoundOn = next)
        componentScope.launch {
            settingsUseCase.updateSoundEnabled(next)
            if (next) {
                startBackgroundMusic(settingsUseCase.current())
            } else {
                audioPlayer.stopBackground()
                audioPlayer.stopEffect()
            }
        }
    }

    private suspend fun startBackgroundMusic(settings: AudioSettings) {
        if (!settings.soundEnabled) return
        // 用户没填就用内置的 24s 循环 BGM，别让复盘页干着
        val url = settings.backgroundMusicUrl.ifBlank { GomokuAudioDefaults.BACKGROUND }
        runCatching { soundPlayer.playBackground(url) }
    }

    fun toggleAutoPlay() {
        if (uiState.isAutoPlaying) {
            stopAutoPlay()
        } else {
            startAutoPlay()
        }
    }

    fun startAutoPlay() {
        stopAutoPlay()
        if (uiState.currentPly >= uiState.maxPly) {
            goToStart()
        }
        uiState = uiState.copy(isAutoPlaying = true)
        autoPlayJob = componentScope.launch {
            while (uiState.isAutoPlaying) {
                delay(800)
                if (!uiState.isAutoPlaying) break
                if (uiState.currentPly >= uiState.maxPly) {
                    stopAutoPlay()
                    break
                }
                goNext()
            }
        }
    }

    fun stopAutoPlay() {
        autoPlayJob?.cancel()
        autoPlayJob = null
        uiState = uiState.copy(isAutoPlaying = false)
    }

    init {
        // 先落盘缓存，等真落第一子的时候再下载就来不及了（自动播放 800ms 一步）
        componentScope.launch {
            GomokuAudioDefaults.ALL.forEach { audioPlayer.warmUp(it) }
        }
        componentScope.launch {
            val settings = settingsUseCase.current()
            uiState = uiState.copy(isSoundOn = settings.soundEnabled)
            startBackgroundMusic(settings)
        }
        componentContext.lifecycle.doOnDestroy {
            audioPlayer.stopBackground()
            audioPlayer.stopEffect()
        }
        componentScope.launch {
            gameQuery.observeById(gameId).collect { detail ->
                detail ?: return@collect
                val plies = detail.plies
                val maxPly = plies.maxOfOrNull { it.ply } ?: 0
                val target = if (targetInitialPly >= 0) {
                    targetInitialPly.coerceIn(0, maxPly).also { targetInitialPly = -1 }
                } else {
                    detail.currentPly.coerceIn(0, maxPly)
                }
                // 结果与回放进度无关，整局恒定；在这里写一次，updatePly 只刷新盘面状态
                uiState = uiState.copy(
                    resultText = detail.resultText,
                    title = detail.title,
                )
                updatePly(
                    plies,
                    detail.initialFen,
                    detail.title,
                    target,
                    play = false,
                )
            }
        }
    }

    fun goToStart() {
        stopAutoPlay()
        updatePly(uiState.plies, uiState.plies.firstOrNull()?.beforeFen ?: FenCodec.INITIAL_FEN, uiState.title, 0)
    }

    fun goPrev() {
        stopAutoPlay()
        val target = (uiState.currentPly - 1).coerceAtLeast(0)
        updatePly(uiState.plies, uiState.plies.firstOrNull()?.beforeFen ?: FenCodec.INITIAL_FEN, uiState.title, target, play = target > 0)
    }

    fun goNext() {
        val target = (uiState.currentPly + 1).coerceAtMost(uiState.maxPly)
        updatePly(uiState.plies, uiState.plies.firstOrNull()?.beforeFen ?: FenCodec.INITIAL_FEN, uiState.title, target, play = target > uiState.currentPly)
    }

    fun goToEnd() {
        stopAutoPlay()
        updatePly(uiState.plies, uiState.plies.firstOrNull()?.beforeFen ?: FenCodec.INITIAL_FEN, uiState.title, uiState.maxPly)
    }

    /**
     * 跳到指定手（着法表点击）。
     *
     * 打谱的核心动线：80 回合的局想看第 50 手，不应该连点 50 次 ▶。
     */
    fun goToPly(ply: Int) {
        stopAutoPlay()
        val target = ply.coerceIn(0, uiState.maxPly)
        updatePly(
            uiState.plies,
            uiState.plies.firstOrNull()?.beforeFen ?: FenCodec.INITIAL_FEN,
            uiState.title,
            target,
            play = target > 0,
        )
    }

    fun openCurrentGame() {
        onNavigate(Screen.GomokuRouter(Screen.GomokuRouter.Type.Game(gameId)))
    }

    fun exportFen() {
        uiState = uiState.copy(exportContent = FenCodec.encode(uiState.boardState))
    }

    fun exportJson() {
        componentScope.launch {
            uiState = uiState.copy(exportContent = exportGame.asJson(gameId))
        }
    }

    fun exportText(labels: TextExportLabels, resultText: String) {
        componentScope.launch {
            uiState = uiState.copy(
                exportContent = exportGame.asText(gameId, labels, resultText),
            )
        }
    }

    fun dismissExport() {
        uiState = uiState.copy(exportContent = "")
    }

    private fun updatePly(
        plies: List<GomokuPlyRecord>,
        initialFen: String,
        title: String,
        targetPly: Int,
        play: Boolean = false,
    ) {
        val fen = when {
            targetPly <= 0 || plies.isEmpty() -> initialFen
            else -> plies.firstOrNull { it.ply == targetPly }?.afterFen
                ?: plies.lastOrNull()?.afterFen
                ?: initialFen
        }
        val boardState = FenCodec.parse(fen)
        val maxPly = plies.maxOfOrNull { it.ply } ?: 0
        val safeTargetPly = when {
            plies.isEmpty() -> 0
            targetPly <= 0 -> 0
            targetPly > maxPly -> maxPly
            else -> targetPly
        }
        uiState = uiState.copy(
            title = title,
            boardState = boardState,
            currentPly = safeTargetPly,
            maxPly = maxPly,
            plies = plies,
        )
        if (play && safeTargetPly > 0) {
            playReplaySound(plies, safeTargetPly)
        }
    }

    private fun playReplaySound(plies: List<GomokuPlyRecord>, targetPly: Int) {
        val record = plies.firstOrNull { it.ply == targetPly } ?: return
        componentScope.launch {
            val settings = settingsUseCase.current()
            audioFeedback.playForMove(record.afterFen, settings)
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            gameId: String,
            initialPly: Int,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): GomokuAnalysisComponent
    }
}
