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
    val ballCount: Int = 1,
    val enlargedBallCount: Int = 0,
    val enlargeSeconds: Float = 0f,
)

data class PinballThemeColors(
    val surface: Int,
    val primary: Int,
    val secondary: Int,
    val tertiary: Int,
    val outline: Int,
    val onSurface: Int,
)
