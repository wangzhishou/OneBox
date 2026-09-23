package com.wanbaohe.boardgame.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import kotlinx.coroutines.delay

/** 状态卡默认高度(AI 失败卡) */
val StatusCardHeight = 140.dp

/** 状态卡默认高度(AI 本地兜底提示卡) */
val FallbackCardHeight = 92.dp

/** 棋盘自适应布局的最小可用高度,低于此回退为整宽棋盘 + 整页滚动 */
val MinAdaptiveBoardHeight = 280.dp

/** 固定高度的状态卡:标题 + 走马灯副标题 + 可选尾部/操作区,棋种无关 */
@Composable
fun StatusCard(
    title: String,
    subtitle: String,
    height: Dp = StatusCardHeight,
    modifier: Modifier = Modifier,
    titleTrailing: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
) {
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 12.dp),
        style = GlassStyle.Medium,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                titleTrailing?.invoke()
            }
            VerticalMarqueeText(
                text = subtitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
            actions?.invoke()
        }
    }
}

/** 固定高度内自下而上循环滚动的纵向走马灯;文字放得下时静止 */
@Composable
fun VerticalMarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val scrollState = rememberScrollState()
    var textHeightPx by remember { mutableIntStateOf(0) }
    val gapPx = with(LocalDensity.current) { 12.dp.roundToPx() }
    val overflowing = scrollState.maxValue > 0

    LaunchedEffect(text, overflowing) {
        if (!overflowing) return@LaunchedEffect
        while (true) {
            scrollState.scrollTo(0)
            delay(1200)
            scrollState.animateScrollTo(textHeightPx + gapPx, tween(2500, easing = LinearEasing))
            delay(1200)
        }
    }

    Box(modifier = modifier.clip(RoundedCornerShape(6.dp))) {
        Column(
            modifier = Modifier.verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = text,
                style = style,
                color = color,
                maxLines = Int.MAX_VALUE,
                onTextLayout = { textHeightPx = it.size.height },
            )
            if (overflowing) {
                Text(
                    text = text,
                    style = style,
                    color = color,
                    maxLines = Int.MAX_VALUE,
                )
            }
        }
    }
}

/**
 * AI 走棋失败状态卡:文案 + 重试/关闭按钮。
 * [secondaryLabel]/[onSecondary] 是可选的第三个入口(如「切换模型」),null 则不显示。
 */
@Composable
fun ErrorStatusCard(
    title: String,
    subtitle: String,
    retryLabel: String,
    onRetry: () -> Unit,
    dismissLabel: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    StatusCard(
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        actions = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GlassTonalButton(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(retryLabel, maxLines = 1)
                }
                if (secondaryLabel != null && onSecondary != null) {
                    GlassTonalButton(
                        onClick = onSecondary,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(secondaryLabel, maxLines = 1)
                    }
                }
                GlassTonalButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(dismissLabel, maxLines = 1)
                }
            }
        },
    )
}

/**
 * AI 本地兜底提示卡(引擎不可用时静默回退本地启发式)。
 * [switchLabel]/[onSwitch] 是可选的「切换模型」入口(标题尾部),null 则不显示。
 */
@Composable
fun FallbackStatusCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    height: Dp = FallbackCardHeight,
    switchLabel: String? = null,
    onSwitch: (() -> Unit)? = null,
) {
    StatusCard(
        title = title,
        subtitle = subtitle,
        height = height,
        modifier = modifier,
        titleTrailing = if (switchLabel != null && onSwitch != null) {
            {
                GlassTonalButton(
                    onClick = onSwitch,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        switchLabel,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                    )
                }
            }
        } else {
            null
        },
    )
}
