package com.wanbaohe.boardgame.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * 三款棋共用的走子提示节拍。
 *
 * [hint] 候选落点的闪烁(0 暗 → 1 亮),[glow] 刚走过那步外圈的呼吸。
 * 这两个值只能在绘制阶段读(Canvas 的 lambda 里),这样只会重画提示层。
 */
class BoardCueMotion internal constructor(
    private val hintState: State<Float>,
    private val glowState: State<Float>,
) {
    val hint: Float get() = hintState.value
    val glow: Float get() = glowState.value
}

@Composable
fun rememberBoardCueMotion(): BoardCueMotion {
    val transition = rememberInfiniteTransition(label = "boardCue")
    val hint = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "hintBlink",
    )
    val glow = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "moveGlow",
    )
    return remember(hint, glow) { BoardCueMotion(hint, glow) }
}

/**
 * 候选落点:实心点用容器色,外圈用对应的 on 色,缓慢闪。
 * 透明度停在一半左右,颜色在,但不压过棋子。
 */
fun DrawScope.drawPulsingDot(
    center: Offset,
    baseRadius: Float,
    color: Color,
    hint: Float,
    minAlpha: Float = 0.39f,
    maxAlpha: Float = 0.5f,
    edgeColor: Color = Color.Unspecified,
    edgeWidth: Float = 0f,
) {
    val phase = hint.coerceIn(0f, 1f)
    val radius = baseRadius * (0.92f + 0.08f * phase)
    val alpha = minAlpha + (maxAlpha - minAlpha) * phase
    drawCircle(
        color = color.copy(alpha = alpha),
        radius = radius,
        center = center,
    )
    if (edgeColor != Color.Unspecified && edgeWidth > 0f) {
        drawCircle(
            color = edgeColor.copy(alpha = 0.35f + 0.15f * phase),
            radius = radius,
            center = center,
            style = Stroke(width = edgeWidth),
        )
    }
}

/**
 * 贴着棋子外侧的一圈细线,跟着 [glow] 轻微呼吸。
 * 线心在棋子半径之外,不会盖住棋面。
 */
fun DrawScope.drawMoveHalo(
    center: Offset,
    radius: Float,
    color: Color,
    glow: Float,
    strokeWidth: Float,
    strong: Boolean,
) {
    val phase = glow.coerceIn(0f, 1f)
    val stroke = if (strong) strokeWidth else strokeWidth * 0.8f
    val gap = stroke * 0.45f
    drawCircle(
        color = color.copy(alpha = if (strong) 0.32f + 0.18f * phase else 0.18f + 0.1f * phase),
        radius = radius + gap + stroke / 2f,
        center = center,
        style = Stroke(width = stroke),
    )
}
