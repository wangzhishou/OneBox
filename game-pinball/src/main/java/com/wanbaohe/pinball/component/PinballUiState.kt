package com.wanbaohe.pinball.component

enum class PinballPhase {
    Ready,
    Playing,
    Paused,
    GameOver,
    LevelComplete,
}

data class PinballUiState(
    val score: Int = 0,
    val bestScore: Int = 0,
    val combo: Int = 0,
    val lives: Int = 3,
    val remainingBricks: Int = 48,
    val level: Int = 1,
    val phase: PinballPhase = PinballPhase.Ready,
    val comboProgress: Float = 0f,
)
