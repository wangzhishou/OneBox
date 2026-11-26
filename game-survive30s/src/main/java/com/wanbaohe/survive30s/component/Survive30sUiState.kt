/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 * Licensed under the Apache License, Version 2.0
 */

package com.wanbaohe.survive30s.component

import androidx.compose.runtime.Immutable

const val SURVIVE_GAME_DURATION = 30f

@Immutable
data class Survive30sUiState(
    val gameState: GameState = GameState.IDLE,
    val elapsedSec: Float = 0f,
    val bestTime: Float = 0f,
    val shieldCount: Int = 0,
    val nearMissCount: Int = 0,
    val nearMissCharge: Int = 0,
    val dangerLevel: Float = 0f,
    val invincibleSec: Float = 0f,
    val phase: SurvivalPhase = SurvivalPhase.Warmup,
    val canvasWidth: Float = 0f,
    val canvasHeight: Float = 0f,
)

enum class SurvivalPhase {
    Warmup,
    Rush,
    Storm,
}

enum class GameState {
    IDLE,
    PLAYING,
    PAUSED,
    GAME_OVER,
    WIN,
}

data class SurviveThemeColors(
    val surface: Int,
    val primary: Int,
    val secondary: Int,
    val tertiary: Int,
    val outline: Int,
    val onSurface: Int,
)
