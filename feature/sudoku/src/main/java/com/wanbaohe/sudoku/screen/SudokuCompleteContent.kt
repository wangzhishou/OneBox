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
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.wanbaohe.sudoku.R
import com.wanbaohe.sudoku.component.SudokuComponent

/**
 * 反馈层: 完成结算页。81 格全部与解一致时替换对局页显示。
 * 不做分享, 主按钮"再来一局"直接按刚完成的难度开新局。
 */
@Composable
fun SudokuCompleteContent(
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(OneBoxDesignSystem.sectionSpacing))

            OneBoxSectionCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TrophyWithConfetti()

                    Spacer(modifier = Modifier.height(OneBoxDesignSystem.blockSpacing))

                    Text(
                        text = stringResource(R.string.sudoku_complete_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.sudoku_complete_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(OneBoxDesignSystem.sectionSpacing))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
                    ) {
                        CompleteStatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.LineTimer,
                            accent = MaterialTheme.colorScheme.onPrimaryContainer,
                            label = stringResource(R.string.sudoku_complete_time),
                            value = formatSudokuTime(state.elapsedSeconds)
                        )
                        CompleteStatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.LineMedal,
                            accent = Color(0xFF43A047),
                            label = stringResource(R.string.sudoku_complete_best),
                            value = state.completedBestSeconds?.let(::formatSudokuTime)
                                ?: stringResource(R.string.sudoku_time_none)
                        )
                    }

                    Spacer(modifier = Modifier.height(OneBoxDesignSystem.sectionSpacing))

                    EnhancedButton(
                        onClick = component::onPlayAgain,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
private fun TrophyWithConfetti() {
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        CONFETTI.forEach { (offsetX, offsetY, color) ->            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = offsetX, y = offsetY)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
        Icon(
            imageVector = Icons.Outlined.LineEmojiEvents,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(72.dp)
        )
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
    icon: ImageVector,
    accent: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(OneBoxDesignSystem.mediumRadius))
            .background(accent.copy(alpha = 0.10f))
            .padding(vertical = OneBoxDesignSystem.blockSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
