package com.wanbaohe.diceroller.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassButton
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.diceroller.R
import com.wanbaohe.diceroller.component.DiceRollerComponent
import com.wanbaohe.diceroller.component.DiceRollerUiState
import com.wanbaohe.diceroller.component.DiceType
import com.wanbaohe.diceroller.ui.DiceCountSelector
import com.wanbaohe.diceroller.ui.DiceHistorySheet
import com.wanbaohe.diceroller.ui.DiceTypeSelector
import com.wanbaohe.diceroller.ui.DiceView
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCasino

/**
 * 投骰子主页面
 *
 * 基于 [BaseScreen] 扩展；主体内容分三区：
 *   1. 骰子类型 + 数量选择器
 *   2. 骰子展示区（含 3D 翻滚动效）
 *   3. 总点数 + 摇一摇按钮
 */
@Composable
fun DiceRollerScreen(
    component: DiceRollerComponent,
) {
    val state by component.uiState.collectAsState()

    BaseScreen(
        title = stringResource(R.string.dice_roller_title),
        onGoBack = component.onGoBack,
        actions = {
            // 历史记录按钮
            IconButton(
                onClick = component::toggleHistory,
                colors = AppTheme.colors.iconButtonColors()
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                    contentDescription = stringResource(R.string.dice_roller_history)
                )
            }
        }
    ) {
        DiceRollerContent(
            state = state,
            onRoll = component::roll,
            onTypeSelect = component::setDiceType,
            onCountChange = component::setDiceCount,
        )
    }

    // 历史面板（BottomSheet）
    DiceHistorySheet(
        state = state.showHistory,
        records = state.history,
        onDismiss = component::toggleHistory,
        onClear = component::clearHistory
    )
}

// ─── 主内容区 ─────────────────────────────────────────────────────────────────

@Composable
private fun DiceRollerContent(
    state: DiceRollerUiState,
    onRoll: () -> Unit,
    onTypeSelect: (DiceType) -> Unit,
    onCountChange: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── 1. 骰子类型选择器 ──────────────────────────────────────────────
        SectionLabel(stringResource(R.string.dice_roller_type))
        DiceTypeSelector(
            selected = state.diceType,
            onSelect = onTypeSelect
        )

        // ── 2. 骰子数量选择器 ──────────────────────────────────────────────
        SectionLabel(stringResource(R.string.dice_roller_count))
        DiceCountSelector(
            count = state.diceCount,
            maxCount = DiceRollerUiState.MAX_DICE,
            onCountChange = onCountChange
        )

        Spacer(Modifier.height(8.dp))

        // ── 3. 骰子展示区 ──────────────────────────────────────────────────
        DiceGrid(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(8.dp))

        // ── 4. 总点数显示 ─────────────────────────────────────────────────
        AnimatedVisibility(
            visible = state.currentRoll.isNotEmpty() && !state.isRolling,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            TotalDisplay(total = state.total, diceType = state.diceType)
        }

        // ── 5. 摇晃提示 ──────────────────────────────────────────────────
        ShakeHintText()

        // ── 6. 摇一摇按钮 ─────────────────────────────────────────────────
        GlassButton(
            onClick = onRoll,
            enabled = !state.isRolling,
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            borderWidth = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(52.dp)
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCasino,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.dice_roller_roll),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ─── 骰子网格 ─────────────────────────────────────────────────────────────────

/**
 * 根据骰子数量自适应排列 —— 最多每行 3 颗，保持视觉均衡
 */
@Composable
private fun DiceGrid(
    state: DiceRollerUiState,
    modifier: Modifier = Modifier,
) {
    val count = state.diceCount
    val results = state.currentRoll

    // 计算每行多少颗：1-3颗一行，4-6颗两行
    val columns = when {
        count <= 3 -> count
        else -> 3
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val rows = (count + columns - 1) / columns
        for (row in 0 until rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val startIdx = row * columns
                val endIdx = minOf(startIdx + columns, count)
                for (idx in startIdx until endIdx) {
                    val result = results.getOrNull(idx)
                    DiceView(
                        result = result ?: run {
                            // 没有结果时展示空白骰子（使用默认骰型的初始面）
                            null
                        },
                        isRolling = state.isRolling,
                        size = when {
                            count == 1 -> 112.dp
                            count <= 3 -> 88.dp
                            else -> 72.dp
                        },
                        faceColor = MaterialTheme.colorScheme.primaryContainer,
                        dotColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

// ─── 总点数展示 ───────────────────────────────────────────────────────────────

@Composable
private fun TotalDisplay(total: Int, diceType: DiceType) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.dice_roller_total),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            text = total.toString(),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    }
}

// ─── 摇晃提示（呼吸动效） ─────────────────────────────────────────────────────

@Composable
private fun ShakeHintText() {
    val infiniteTransition = rememberInfiniteTransition(label = "hint_anim")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hint_alpha"
    )

    Text(
        text = stringResource(R.string.dice_roller_shake_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.alpha(alpha),
        textAlign = TextAlign.Center
    )
}

// ─── 分区标签 ─────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

