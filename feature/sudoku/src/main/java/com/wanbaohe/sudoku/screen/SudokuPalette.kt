package com.wanbaohe.sudoku.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.wanbaohe.core.ui.widget.glass.tintedGlassContentColor
import com.wanbaohe.sudoku.logic.SudokuDifficulty

/**
 * 数独配色：与首页「精选」大卡同一套轮换主题（primary / secondary / tertiary / 中性）。
 *
 * 两条不变量：
 * - [SudokuPalette.tint] 只携带色调，绝不 `copy(alpha = …)` —— 玻璃管线会丢弃颜色自身的
 *   alpha，浓淡一律交给 GlassStyle 阶梯表达；
 * - [SudokuPalette.ink] 按玻璃混合后的有效底色自动取深/浅，所以任意主题、任意明暗设置下
 *   彩色卡片上的文字都读得清，不再拿成对的 onXxxContainer 硬凑。
 */
internal enum class SudokuTint { PRIMARY, SECONDARY, TERTIARY, NEUTRAL }

internal data class SudokuPalette(
    /** 卡片 / 徽章的容器色，只带色调。 */
    val tint: Color,
    /** 彩色玻璃上的可读文字色。 */
    val ink: Color,
    /** 与 tint 成对的图标 / 强调色，用于小面积徽章内部。 */
    val icon: Color,
)

@Composable
internal fun sudokuPalette(theme: SudokuTint): SudokuPalette {
    val colorScheme = MaterialTheme.colorScheme
    val tint = when (theme) {
        SudokuTint.PRIMARY -> colorScheme.primaryContainer
        SudokuTint.SECONDARY -> colorScheme.secondaryContainer
        SudokuTint.TERTIARY -> colorScheme.tertiaryContainer
        SudokuTint.NEUTRAL -> colorScheme.surfaceContainer
    }
    val icon = when (theme) {
        SudokuTint.PRIMARY -> colorScheme.onPrimaryContainer
        SudokuTint.SECONDARY -> colorScheme.onSecondaryContainer
        SudokuTint.TERTIARY -> colorScheme.onTertiaryContainer
        SudokuTint.NEUTRAL -> colorScheme.onSurfaceVariant
    }
    return SudokuPalette(
        tint = tint,
        ink = tintedGlassContentColor(tint),
        icon = icon,
    )
}

/** 难度越高越贴近强调色，与精选卡的四色轮换同源。 */
internal fun difficultyTint(difficulty: SudokuDifficulty): SudokuTint = when (difficulty) {
    SudokuDifficulty.EASY -> SudokuTint.TERTIARY
    SudokuDifficulty.MEDIUM -> SudokuTint.SECONDARY
    SudokuDifficulty.HARD -> SudokuTint.PRIMARY
}

