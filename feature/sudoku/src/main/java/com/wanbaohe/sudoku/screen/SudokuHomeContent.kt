package com.wanbaohe.sudoku.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.wanbaohe.sudoku.R
import com.wanbaohe.sudoku.component.SudokuComponent
import com.wanbaohe.sudoku.logic.SudokuDifficulty

/**
 * 入口层: 难度选择页。
 * 「继续游戏」卡片(有未完成快照时) + 简单/中等/困难三张难度卡。
 */
@Composable
fun SudokuHomeContent(
    component: SudokuComponent,
    bottomInset: Dp = 0.dp
) {
    val state by component.uiState.collectAsState()

    BaseScreen(
        modifier = Modifier.padding(bottom = bottomInset),
        title = stringResource(R.string.sudoku_title),
        onGoBack = component::onTabBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = OneBoxDesignSystem.screenPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing)
        ) {
            Spacer(modifier = Modifier.height(OneBoxDesignSystem.screenTopSpacing))

            if (state.hasSavedGame) {
                HomeActionCard(
                    accent = MaterialTheme.colorScheme.onPrimaryContainer,
                    title = stringResource(R.string.sudoku_continue_title),
                    subtitle = stringResource(R.string.sudoku_continue_subtitle, state.savedFilledCount),
                    filledCells = CONTINUE_GRID_PATTERN,
                    onClick = component::continueSavedGame
                )
            }

            SudokuDifficulty.entries.forEachIndexed { index, difficulty ->
                HomeActionCard(
                    accent = difficultyAccent(difficulty),
                    title = difficultyLabel(difficulty),
                    subtitle = stringResource(R.string.sudoku_difficulty_desc, difficulty.givens),
                    filledCells = DIFFICULTY_GRID_PATTERNS[index],
                    onClick = { component.startNewGame(difficulty) }
                )
            }

            Spacer(modifier = Modifier.height(OneBoxDesignSystem.blockSpacing))
        }
    }
}

/** 难度卡配色: 主题三色, 难度越高越贴近强调色(onPrimaryContainer), 界面整体跟随主题变化。 */
@Composable
private fun difficultyAccent(difficulty: SudokuDifficulty): Color = when (difficulty) {
    SudokuDifficulty.EASY -> MaterialTheme.colorScheme.tertiary
    SudokuDifficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
    SudokuDifficulty.HARD -> MaterialTheme.colorScheme.onPrimaryContainer
}

@Composable
internal fun difficultyLabel(difficulty: SudokuDifficulty): String = when (difficulty) {
    SudokuDifficulty.EASY -> stringResource(R.string.sudoku_difficulty_easy)
    SudokuDifficulty.MEDIUM -> stringResource(R.string.sudoku_difficulty_medium)
    SudokuDifficulty.HARD -> stringResource(R.string.sudoku_difficulty_hard)
}

@Composable
private fun HomeActionCard(
    accent: Color,
    title: String,
    subtitle: String,
    filledCells: List<Boolean>,
    onClick: () -> Unit
) {
    OneBoxSectionCard(
        onClick = onClick,
        containerColor = accent.copy(alpha = 0.10f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing)
        ) {
            MiniGrid(accent = accent, filledCells = filledCells)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LineChevronRight,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/** 卡片左侧的 3×3 小宫格点缀, 深色格 = 主题点缀色, 浅色格 = 同色低透明度。 */
@Composable
private fun MiniGrid(
    accent: Color,
    filledCells: List<Boolean>
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        filledCells.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                row.forEach { filled ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (filled) accent else accent.copy(alpha = 0.25f))
                    )
                }
            }
        }
    }
}

private val CONTINUE_GRID_PATTERN = listOf(
    true, false, false,
    false, true, false,
    false, false, true
)

private val DIFFICULTY_GRID_PATTERNS = listOf(
    listOf(
        true, false, true,
        true, true, false,
        false, true, true
    ),
    listOf(
        false, true, false,
        true, true, true,
        true, false, true
    ),
    listOf(
        true, true, true,
        false, true, false,
        true, true, true
    )
)
