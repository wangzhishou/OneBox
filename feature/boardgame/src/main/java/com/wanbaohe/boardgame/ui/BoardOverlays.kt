package com.wanbaohe.boardgame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons as MaterialIcons

/**
 * 盖住棋盘的近白遮罩。吃掉棋子上的点击,中间放开始或继续按钮。
 * 底色用 [androidx.compose.material3.ColorScheme.surfaceContainerLow],略透明,棋盘还认得出来。
 */
@Composable
fun BoxScope.BoardCover(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .matchParentSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            )
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.50f)),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

/**
 * 未开始/暂停时盖住棋盘。遮罩吃掉全部点击,棋子点不到;
 * 只有中间按钮会开始或继续。[startLabel] 由调用方区分「开始」/「继续」。
 */
@Composable
fun BoxScope.BoardStartOverlay(
    startLabel: String,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoardCover(modifier) {
        BoardGamePillButton(
            label = startLabel,
            icon = MaterialIcons.Filled.PlayArrow,
            onClick = onStart,
            primary = true,
        )
    }
}

/**
 * 终局遮罩:棋盘中央浮一张**实底**(surfaceContainerLowest,不透明)圆角卡片
 * (🏆 + 🎉 装饰、结果标题/副标题、竖排按钮组),不再把文字直接压在棋子上。
 * [resultTitle]/[resultSubtitle]/按钮文案均由调用方本地化。
 *
 * 卡片是弹层容器,按仓库约定不走玻璃管线(玻璃的底只铺约 4% 覆盖率,压在棋盘上会透出棋子,
 * 文字糊成一团);这里直接给 M3 实色底,保证任何棋盘底色下都看得清。
 *
 * @param emphasizeResult 胜负类结果用主题强调色;和棋等中性结果传 false
 * @param resultColor 胜方身份色(红方/白方等);传了就以它为准,不传走 [emphasizeResult] 的强调色
 * @param backLabel/onBack 可选第三按钮(text 样式),不传则不显示
 * @param dismissLabel/onDismiss 可选「先看看棋盘」入口;传了 [onDismiss] 即可关闭浮层就地复盘
 */
@Composable
fun BoxScope.BoardGameOverOverlay(
    resultTitle: String,
    restartLabel: String,
    reviewLabel: String,
    onRestart: () -> Unit,
    onReview: () -> Unit,
    modifier: Modifier = Modifier,
    resultSubtitle: String = "",
    emphasizeResult: Boolean = true,
    resultColor: Color = Color.Unspecified,
    backLabel: String = "",
    onBack: (() -> Unit)? = null,
    dismissLabel: String = "",
    onDismiss: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .matchParentSize()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.28f))
            // 吃掉落在卡片外的点击;若允许关闭浮层,点遮罩也关掉以便看棋盘
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onDismiss?.invoke() },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 300.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shadowElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "🎉 🏆 🎉",
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = resultTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = when {
                        resultColor != Color.Unspecified -> resultColor
                        emphasizeResult -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    textAlign = TextAlign.Center,
                )
                if (resultSubtitle.isNotBlank()) {
                    Text(
                        text = resultSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onRestart,
                        modifier = Modifier.fillMaxWidth(),
                        colors = accentFilledButtonColors(),
                    ) {
                        Text(restartLabel)
                    }
                    OutlinedButton(
                        onClick = onReview,
                        modifier = Modifier.fillMaxWidth(),
                        colors = accentOutlinedButtonColors(),
                    ) {
                        Text(reviewLabel)
                    }
                    if (onBack != null) {
                        TextButton(
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth(),
                            colors = accentTextButtonColors(),
                        ) {
                            Text(backLabel)
                        }
                    }
                    if (onDismiss != null) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = accentTextButtonColors(),
                        ) {
                            Text(dismissLabel)
                        }
                    }
                }
            }
        }
    }
}
