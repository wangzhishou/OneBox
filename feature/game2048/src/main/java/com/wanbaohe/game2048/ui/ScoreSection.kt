package com.wanbaohe.game2048.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.wanbaohe.game2048.R

/**
 * 分数展示区域 —— 当前分数 + 最高分并排显示
 *
 * 使用 [GlassCard] 作为容器，在毛玻璃模式下自动变为透明玻璃风格。
 */
@Composable
fun ScoreSection(
    currentScore: Int,
    bestScore: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── 当前分数 ──
        GlassCard(
            modifier = Modifier.weight(1f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.game_2048_current_score),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 1.5,
                )
                Text(
                    text = formatScore(currentScore),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        // ── 最高分 ──
        GlassCard(
            modifier = Modifier.weight(1f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.game_2048_best_score),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 1.5,
                )
                Text(
                    text = formatScore(bestScore),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

/** 格式化分数：千分位逗号 */
private fun formatScore(score: Int): String {
    return String.format("%,d", score)
}

