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
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
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
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.PlayCircle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground

/** 未开始/暂停时盖在棋盘上的开始遮罩;[startLabel] 由调用方区分「开始」/「继续」 */
@Composable
fun BoxScope.BoardStartOverlay(
    startLabel: String,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .matchParentSize()
            .clip(MaterialTheme.shapes.large)
            .glassBackground(
                style = GlassStyle.Dense,
                color = MaterialTheme.colorScheme.primaryContainer.copy(0.5f),
                shape = MaterialTheme.shapes.large
            ),
        contentAlignment = Alignment.Center,
    ) {
        ExtendedFloatingActionButton(
            onClick = onStart,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.PlayCircle,
                    contentDescription = null,
                )
            },
            text = {
                Text(
                    text = startLabel,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

/**
 * 终局遮罩:棋盘中央浮一张不透明圆角卡片(🏆 + 🎉 装饰、结果标题/副标题、竖排按钮组),
 * 不再把文字直接压在棋子上。[resultTitle]/[resultSubtitle]/按钮文案均由调用方本地化。
 *
 * @param emphasizeResult 胜负类结果用主题强调色;和棋等中性结果传 false
 * @param backLabel/onBack 可选第三按钮(text 样式),不传则不显示
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
    backLabel: String = "",
    onBack: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .matchParentSize()
            .clip(MaterialTheme.shapes.large)
            .background(Color.Black.copy(alpha = 0.42f))
            // 吃掉落在卡片外的点击,避免终局后还能点到棋子
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 300.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            ),
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
                    color = if (emphasizeResult) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
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
                    ) {
                        Text(restartLabel)
                    }
                    OutlinedButton(
                        onClick = onReview,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(reviewLabel)
                    }
                    if (onBack != null) {
                        TextButton(
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(backLabel)
                        }
                    }
                }
            }
        }
    }
}
