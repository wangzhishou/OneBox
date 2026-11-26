/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 * Licensed under the Apache License, Version 2.0
 */

package com.wanbaohe.survive30s.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.shifenmiao.base.audio.NetworkAudioPlayer
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.storage.RemoteConfigStorage
import com.tencent.mmkv.MMKV
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.review.ReviewPromptHost
import com.wanbaohe.core.ui.review.ReviewPromptTrigger
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.engine.android.EngineCommands
import com.wanbaohe.engine.android.EngineController
import com.wanbaohe.engine.android.EngineSnapshot
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Survive30sComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted("onGoBack") val onGoBack: () -> Unit,
    @Assisted("onNavigateToAdWatch") val onNavigateToAdWatch: (() -> Unit)?,
    dispatchersHolder: DispatchersHolder,
    private val audioPlayer: NetworkAudioPlayer,
) : BaseComponent(dispatchersHolder, componentContext) {

    val engineController = EngineController(GAME_ID)

    private val mkv by lazy { MMKV.mmkvWithID("survive30s") }
    private val _uiState = MutableStateFlow(Survive30sUiState(bestTime = loadBestTime()))
    val uiState = _uiState.asStateFlow()
    private var lastSoundSequence = 0

    init {
        componentScope.launch {
            ALL_SOUNDS.map { url -> async { audioPlayer.warmUp(url) } }.awaitAll()
        }
        componentContext.lifecycle.doOnDestroy {
            engineController.close()
            audioPlayer.stopBackground()
            audioPlayer.stopEffect()
        }
    }

    fun updateCanvasSize(width: Float, height: Float) {
        if (width <= 0f || height <= 0f) return
        val state = _uiState.value
        if (state.canvasWidth == width && state.canvasHeight == height) return
        _uiState.update { it.copy(canvasWidth = width, canvasHeight = height) }
    }

    fun startGame() {
        lastSoundSequence = 0
        engineController.dispatch(EngineCommands.RESET)
        playSound(SOUND_BGM, isBackground = true)
    }

    fun pauseGame() {
        if (_uiState.value.gameState != GameState.PLAYING) return
        engineController.setPaused(true)
        audioPlayer.stopBackground()
    }

    fun resumeGame() {
        if (_uiState.value.gameState != GameState.PAUSED) return
        engineController.setPaused(false)
        playSound(SOUND_BGM, isBackground = true)
    }

    fun movePlayerTo(x: Float, y: Float) {
        val state = _uiState.value
        if (state.gameState != GameState.PLAYING) return
        if (state.canvasWidth <= 0f || state.canvasHeight <= 0f) return
        engineController.dispatchPointer(
            type = EngineCommands.POINTER_MOVE,
            normalizedX = (x / state.canvasWidth).coerceIn(0f, 1f),
            normalizedY = (y / state.canvasHeight).coerceIn(0f, 1f),
        )
    }

    fun updateTheme(colors: SurviveThemeColors) {
        val payload = ByteBuffer.allocate(THEME_PAYLOAD_BYTES).order(ByteOrder.LITTLE_ENDIAN).apply {
            putInt(colors.surface)
            putInt(colors.primary)
            putInt(colors.secondary)
            putInt(colors.tertiary)
            putInt(colors.outline)
            putInt(colors.onSurface)
        }
        engineController.dispatch(GAME_COMMAND_THEME, payload.array())
    }

    fun updateSnapshot(snapshot: EngineSnapshot) {
        val previous = _uiState.value
        val gameState = GameState.entries.getOrElse(snapshot.integers[0]) { GameState.IDLE }
        val elapsed = snapshot.floats[0]
        val isNewTerminalState = gameState != previous.gameState
        val bestTime = if (
            isNewTerminalState &&
            (gameState == GameState.GAME_OVER || gameState == GameState.WIN) &&
            elapsed > previous.bestTime
        ) {
            saveBestTime(elapsed)
            elapsed
        } else {
            previous.bestTime
        }
        _uiState.update {
            it.copy(
                gameState = gameState,
                elapsedSec = elapsed,
                bestTime = bestTime,
                shieldCount = snapshot.integers[1],
                nearMissCount = snapshot.integers[2],
                nearMissCharge = snapshot.integers[3],
                dangerLevel = snapshot.floats[1],
                invincibleSec = snapshot.floats[2],
                phase = SurvivalPhase.entries.getOrElse(snapshot.integers[4]) { SurvivalPhase.Warmup },
            )
        }

        val soundSequence = snapshot.integers[5]
        if (soundSequence != lastSoundSequence) {
            lastSoundSequence = soundSequence
            when (snapshot.integers[6]) {
                SOUND_CODE_GAME_OVER -> playSound(SOUND_GAME_OVER)
                SOUND_CODE_SHIELD_BLOCK -> playSound(SOUND_SHIELD_BLOCK)
                SOUND_CODE_NEAR_MISS -> playSound(SOUND_NEAR_MISS)
                SOUND_CODE_SHIELD_GAIN -> playSound(SOUND_SHIELD_GAIN)
                SOUND_CODE_PHASE_CHANGE -> playSound(SOUND_PHASE_CHANGE)
                SOUND_CODE_WIN -> playSound(SOUND_WIN)
            }
        }
        if (!isNewTerminalState) return
        when (gameState) {
            GameState.GAME_OVER -> audioPlayer.stopBackground()
            GameState.WIN -> onWin()
            else -> Unit
        }
    }

    private fun onWin() {
        audioPlayer.stopBackground()
        val winPoints = RemoteConfigStorage.getRemoteConfig().survive30sWinPoints ?: 300
        BaseUtils.rewardPoints(
            points = winPoints,
            desc = "通关躲避30秒游戏奖励",
            source = "survive_30s_game",
            bizId = "",
            showToast = true,
        )
        ReviewPromptHost.notifySuccess(ReviewPromptTrigger.GAME_WIN)
    }

    private fun playSound(url: String, isBackground: Boolean = false) {
        componentScope.launch {
            runCatching {
                if (isBackground) audioPlayer.playBackground(url) else audioPlayer.playEffect(url)
            }
        }
    }

    private fun loadBestTime(): Float = mkv.decodeFloat("best_time", 0f)

    private fun saveBestTime(time: Float) = mkv.encode("best_time", time)

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            @Assisted("onGoBack") onGoBack: () -> Unit,
            @Assisted("onNavigateToAdWatch") onNavigateToAdWatch: (() -> Unit)?,
        ): Survive30sComponent
    }

    companion object {
        private const val GAME_ID = "survive30s"
        private const val GAME_COMMAND_THEME = 257
        private const val THEME_PAYLOAD_BYTES = 24
        private const val SOUND_BASE = "https://images.oneboxable.com/audio/survive30s"
        private const val SOUND_BGM = "$SOUND_BASE/bgm.ogg"
        private const val SOUND_GAME_OVER = "$SOUND_BASE/game_over.ogg"
        private const val SOUND_SHIELD_BLOCK = "$SOUND_BASE/shield_block.ogg"
        private const val SOUND_NEAR_MISS = "$SOUND_BASE/near_miss.ogg"
        private const val SOUND_SHIELD_GAIN = "$SOUND_BASE/shield_gain.ogg"
        private const val SOUND_PHASE_CHANGE = "$SOUND_BASE/phase_change.ogg"
        private const val SOUND_WIN = "$SOUND_BASE/win.ogg"
        private val ALL_SOUNDS = listOf(
            SOUND_BGM,
            SOUND_GAME_OVER,
            SOUND_SHIELD_BLOCK,
            SOUND_NEAR_MISS,
            SOUND_SHIELD_GAIN,
            SOUND_PHASE_CHANGE,
            SOUND_WIN,
        )

        private const val SOUND_CODE_GAME_OVER = 1
        private const val SOUND_CODE_SHIELD_BLOCK = 2
        private const val SOUND_CODE_NEAR_MISS = 3
        private const val SOUND_CODE_SHIELD_GAIN = 4
        private const val SOUND_CODE_PHASE_CHANGE = 5
        private const val SOUND_CODE_WIN = 6
    }
}
