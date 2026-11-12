/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.sudoku.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.glassBackground
import com.wanbaohe.core.ui.widget.glass.tintedGlassCardColors
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.sudoku.R
import com.wanbaohe.sudoku.component.SudokuComponent
import com.wanbaohe.sudoku.logic.SudokuDifficulty

/**
 * 入口层: 难度选择页。
 * 「继续游戏」卡片(有未完成快照时) + 简单/中等/困难三张难度卡。
 *
 * 配色跟随首页精选大卡: 四色轮换的彩色玻璃容器 + 按混合后底色自动取深浅的文字,
 * 徽章用容器反色, 难度用三段色条表达递进。
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
                    palette = sudokuPalette(SudokuTint.PRIMARY),
                    title = stringResource(R.string.sudoku_continue_title),
                    subtitle = stringResource(R.string.sudoku_continue_subtitle, state.savedFilledCount),
                    filledCells = CONTINUE_GRID_PATTERN,
                    level = 0,
                    onClick = component::continueSavedGame
                )
            }

            SudokuDifficulty.entries.forEachIndexed { index, difficulty ->
                HomeActionCard(
                    palette = sudokuPalette(difficultyTint(difficulty)),
                    title = difficultyLabel(difficulty),
                    subtitle = stringResource(R.string.sudoku_difficulty_desc, difficulty.givens),
                    filledCells = DIFFICULTY_GRID_PATTERNS[index],
                    level = difficulty.ordinal + 1,
                    onClick = { component.startNewGame(difficulty) }
                )
            }

            Spacer(modifier = Modifier.height(OneBoxDesignSystem.blockSpacing))
        }
    }
}

@Composable
internal fun difficultyLabel(difficulty: SudokuDifficulty): String = when (difficulty) {
    SudokuDifficulty.EASY -> stringResource(R.string.sudoku_difficulty_easy)
    SudokuDifficulty.MEDIUM -> stringResource(R.string.sudoku_difficulty_medium)
    SudokuDifficulty.HARD -> stringResource(R.string.sudoku_difficulty_hard)
}

@Composable
private fun HomeActionCard(
    palette: SudokuPalette,
    title: String,
    subtitle: String,
    filledCells: List<Boolean>,
    level: Int,
    onClick: () -> Unit
) {
    GlassCard(
        onClick = onClick,
        shape = OneBoxDesignSystem.sectionCardShape,
        style = GlassStyle.Thick,
        colors = tintedGlassCardColors(palette.tint)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(OneBoxDesignSystem.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)
        ) {
            // 徽章用容器反色: 深色主题下自动翻成浅色块, 始终是卡片里最跳的一块
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .glassBackground(
                        style = GlassStyle.Thick,
                        color = palette.icon,
                        shape = RoundedCornerShape(OneBoxDesignSystem.mediumRadius)
                    ),
                contentAlignment = Alignment.Center
            ) {
                MiniGrid(accent = palette.tint, filledCells = filledCells)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = palette.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (level > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    DifficultyMeter(level = level, palette = palette)
                    Spacer(modifier = Modifier.height(6.dp))
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.ink.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .glassBackground(
                        style = GlassStyle.Thick,
                        color = palette.icon,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LineChevronRight,
                    contentDescription = null,
                    tint = palette.tint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/** 三段色条: 实心段数 = 难度等级, 比文字标签更直观也不占翻译资源。 */
@Composable
private fun DifficultyMeter(
    level: Int,
    palette: SudokuPalette
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(width = 14.dp, height = 4.dp)
                    .clip(RoundedCornerShape(OneBoxDesignSystem.pillRadius))
                    .background(
                        if (index < level) palette.icon else palette.ink.copy(alpha = 0.22f)
                    )
            )
        }
    }
}

/** 徽章里的 3×3 小宫格, 实心格 = 反色, 空格 = 同色低透明度(纯装饰绘制)。 */
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
                            .background(if (filled) accent else accent.copy(alpha = 0.3f))
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
