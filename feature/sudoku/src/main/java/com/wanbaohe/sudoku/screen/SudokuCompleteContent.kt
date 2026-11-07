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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEmojiEvents
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMedal
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTimer
import com.wanbaohe.core.ui.widget.enhanced.EnhancedButton
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.glassBackground
import com.wanbaohe.core.ui.widget.glass.tintedGlassCardColors
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.sudoku.R
import com.wanbaohe.sudoku.component.SudokuComponent

/**
 * 反馈层: 完成结算页。81 格全部与解一致时替换对局页显示。
 * 不做分享, 主按钮"再来一局"直接按刚完成的难度开新局。
 *
 * 整卡用强调色玻璃 + 自动可读文字, 两张数据卡轮换第二/第三主题色,
 * 按钮反色从卡片里跳出来 —— 不再出现写死绿色这种不跟主题走的色块。
 */
@Composable
fun SudokuCompleteContent(
    component: SudokuComponent,
    bottomInset: Dp = 0.dp
) {
    val state by component.uiState.collectAsState()
    val palette = sudokuPalette(SudokuTint.PRIMARY)

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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(OneBoxDesignSystem.sectionSpacing))

            GlassCard(
                shape = OneBoxDesignSystem.sectionCardShape,
                style = GlassStyle.Thick,
                colors = tintedGlassCardColors(palette.tint)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(OneBoxDesignSystem.cardPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TrophyWithConfetti(palette = palette)

                    Spacer(modifier = Modifier.height(OneBoxDesignSystem.blockSpacing))

                    Text(
                        text = stringResource(R.string.sudoku_complete_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.ink
                    )
                    Spacer(modifier = Modifier.height(OneBoxDesignSystem.microSpacing))
                    Text(
                        text = stringResource(R.string.sudoku_complete_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.ink.copy(alpha = 0.72f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(OneBoxDesignSystem.itemSpacing))

                    Box(
                        modifier = Modifier
                            .glassBackground(
                                style = GlassStyle.Thick,
                                color = palette.icon,
                                shape = RoundedCornerShape(OneBoxDesignSystem.pillRadius)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = difficultyLabel(state.difficulty),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.tint
                        )
                    }

                    Spacer(modifier = Modifier.height(OneBoxDesignSystem.sectionSpacing))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
                    ) {
                        CompleteStatCard(
                            modifier = Modifier.weight(1f),
                            palette = sudokuPalette(SudokuTint.SECONDARY),
                            icon = Icons.Outlined.LineTimer,
                            label = stringResource(R.string.sudoku_complete_time),
                            value = formatSudokuTime(state.elapsedSeconds)
                        )
                        CompleteStatCard(
                            modifier = Modifier.weight(1f),
                            palette = sudokuPalette(SudokuTint.TERTIARY),
                            icon = Icons.Outlined.LineMedal,
                            label = stringResource(R.string.sudoku_complete_best),
                            value = state.completedBestSeconds?.let(::formatSudokuTime)
                                ?: stringResource(R.string.sudoku_time_none)
                        )
                    }

                    Spacer(modifier = Modifier.height(OneBoxDesignSystem.sectionSpacing))

                    EnhancedButton(
                        onClick = component::onPlayAgain,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = palette.ink,
                        contentColor = palette.tint
                    ) {
                        Text(
                            text = stringResource(R.string.sudoku_play_again),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(OneBoxDesignSystem.blockSpacing))
        }
    }
}

/** 奖杯 + 周围一圈彩带色点, 点缀色有意不走主题(庆祝彩带就该是五彩的)。 */
@Composable
private fun TrophyWithConfetti(palette: SudokuPalette) {
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        CONFETTI.forEach { (offsetX, offsetY, color) ->
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = offsetX, y = offsetY)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
        Box(
            modifier = Modifier
                .size(76.dp)
                .glassBackground(
                    style = GlassStyle.Thick,
                    color = palette.icon,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.LineEmojiEvents,
                contentDescription = null,
                tint = palette.tint,
                modifier = Modifier.size(44.dp)
            )
        }
    }
}

private val CONFETTI = listOf(
    Triple(72.dp, (-38).dp, Color(0xFFFFCA28)),
    Triple((-76).dp, (-20).dp, Color(0xFF66BB6A)),
    Triple(84.dp, 6.dp, Color(0xFF90CAF9)),
    Triple((-64).dp, 30.dp, Color(0xFF90CAF9)),
    Triple(58.dp, 42.dp, Color(0xFF66BB6A)),
    Triple((-88).dp, 8.dp, Color(0xFFFFCA28))
)

@Composable
private fun CompleteStatCard(
    palette: SudokuPalette,
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .glassBackground(
                style = GlassStyle.Dense,
                color = palette.tint,
                shape = RoundedCornerShape(OneBoxDesignSystem.mediumRadius)
            )
            .padding(vertical = OneBoxDesignSystem.blockSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .glassBackground(
                    style = GlassStyle.Thick,
                    color = palette.icon,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.tint,
                modifier = Modifier.size(17.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = palette.ink.copy(alpha = 0.72f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = palette.ink
        )
    }
}
