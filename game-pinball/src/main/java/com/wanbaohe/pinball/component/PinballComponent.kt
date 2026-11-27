package com.wanbaohe.pinball.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.engine.android.EngineCommands
import com.wanbaohe.engine.android.EngineController
import com.wanbaohe.engine.android.EngineSnapshot
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PinballComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {
    val engineController = EngineController(GAME_ID)

    private val _uiState = MutableStateFlow(PinballUiState())
    val uiState = _uiState.asStateFlow()

    init {
        componentContext.lifecycle.doOnDestroy(engineController::close)
    }

    fun updateSnapshot(snapshot: EngineSnapshot) {
        val phase = PinballPhase.entries.getOrElse(snapshot.integers[4]) { PinballPhase.Ready }
        val score = snapshot.integers[0]
        _uiState.update { current ->
            val updated = current.copy(
                score = score,
                bestScore = maxOf(current.bestScore, score),
                combo = snapshot.integers[1],
                lives = snapshot.integers[2],
                remainingBricks = snapshot.integers[3],
                level = snapshot.integers[5],
                phase = phase,
                comboProgress = snapshot.floats[0].coerceIn(0f, 1f),
                ballCount = snapshot.integers[6].coerceAtLeast(1),
                enlargedBallCount = snapshot.integers[7],
                enlargeSeconds = snapshot.floats[1].coerceAtLeast(0f),
            )
            if (updated == current) current else updated
        }
    }

    fun launch() = engineController.dispatch(EngineCommands.GAME_SPECIFIC)

    fun pause() = engineController.setPaused(true)

    fun resume() = engineController.setPaused(false)

    fun restart() = engineController.dispatch(EngineCommands.RESET)

    fun selectLevel(level: Int) {
        if (_uiState.value.phase != PinballPhase.Paused) return
        val payload = ByteBuffer.allocate(LEVEL_PAYLOAD_BYTES).order(ByteOrder.LITTLE_ENDIAN).apply {
            putInt(level.coerceIn(1, PINBALL_MAX_LEVEL))
        }
        engineController.dispatch(GAME_COMMAND_SELECT_LEVEL, payload.array())
    }

    fun updateTheme(colors: PinballThemeColors) {
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

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
        ): PinballComponent
    }

    private companion object {
        const val GAME_ID = "pinball"
        const val GAME_COMMAND_THEME = 257
        const val GAME_COMMAND_SELECT_LEVEL = GAME_COMMAND_THEME + 1
        const val THEME_PAYLOAD_BYTES = 24
        const val LEVEL_PAYLOAD_BYTES = 4
    }
}
