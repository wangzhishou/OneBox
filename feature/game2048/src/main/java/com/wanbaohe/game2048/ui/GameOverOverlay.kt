package com.wanbaohe.game2048.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassButton
import com.wanbaohe.game2048.R

/**
 * 游戏结束 / 胜利覆盖层
 *
 * 半透明遮罩 + 居中提示文本 + 新游戏按钮。
 *
 * @param isGameOver   游戏是否结束
 * @param isWon        是否达成 2048
 * @param hasShownWin  胜利弹窗是否已展示过（继续游戏后不再弹）
 * @param onNewGame    开始新游戏回调
 * @param onContinue   胜利后继续游戏回调
 */
@Composable
fun GameOverOverlay(
    isGameOver: Boolean,
    isWon: Boolean,
    hasShownWin: Boolean,
    onNewGame: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 显示条件：游戏结束 或 首次胜利（未弹过窗）
    val showOverlay = isGameOver || (isWon && !hasShownWin)

    AnimatedVisibility(
        visible = showOverlay,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                    RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp),
            ) {
                // ── 标题 ──
                Text(
                    text = if (isGameOver) {
                        stringResource(R.string.game_2048_game_over)
                    } else {
                        stringResource(R.string.game_2048_you_win)
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isGameOver) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = if (isGameOver) {
                        stringResource(R.string.game_2048_no_moves)
                    } else {
                        stringResource(R.string.game_2048_reached_2048)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(24.dp))

                // ── 新游戏按钮 ──
                GlassButton(
                    onClick = onNewGame,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(48.dp),
                ) {
                    Text(
                        text = stringResource(R.string.game_2048_new_game),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                // ── 胜利时额外显示"继续游戏"按钮 ──
                if (isWon && !hasShownWin) {
                    Spacer(Modifier.height(12.dp))
                    GlassButton(
                        onClick = onContinue,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        borderWidth = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(48.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.game_2048_continue),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

