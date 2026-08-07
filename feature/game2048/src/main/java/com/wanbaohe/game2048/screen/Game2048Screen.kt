package com.wanbaohe.game2048.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassButton
import com.wanbaohe.game2048.R
import com.wanbaohe.game2048.component.Game2048Component
import com.wanbaohe.game2048.ui.GameBoard
import com.wanbaohe.game2048.ui.GameOverOverlay
import com.wanbaohe.game2048.ui.ScoreSection
import com.t8rin.imagetoolbox.core.resources.icons.Refresh

/**
 * 2048 游戏主页面
 *
 * 基于 [BaseScreen] 扩展，布局自上而下：
 * 1. 分数展示区（当前分 + 最高分）
 * 2. 4×4 游戏棋盘（含滑动手势 + 游戏结束覆盖层）
 * 3. 新游戏按钮
 */
@Composable
fun Game2048Screen(
    component: Game2048Component,
) {
    val state by component.uiState.collectAsState()

    BaseScreen(
        title = stringResource(R.string.game_2048_title),
        onGoBack = component.onGoBack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── 1. 分数区 ──────────────────────────────────────────────
            ScoreSection(
                currentScore = state.score,
                bestScore = state.bestScore,
                modifier = Modifier.fillMaxWidth(),
            )

            // ── 2. 棋盘区 + 结束覆盖层 ────────────────────────────────
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                GameBoard(
                    grid = state.grid,
                    onSwipe = component::move,
                    modifier = Modifier.fillMaxWidth(),
                )

                // 游戏结束 / 胜利覆盖层
                GameOverOverlay(
                    isGameOver = state.isGameOver,
                    isWon = state.isWon,
                    hasShownWin = state.hasShownWinDialog,
                    onNewGame = component::newGame,
                    onContinue = component::dismissWinDialog,
                    modifier = Modifier.matchParentSize(),
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── 3. 新游戏按钮 ──────────────────────────────────────────
            GlassButton(
                onClick = component::newGame,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                borderWidth = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.game_2048_new_game),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

