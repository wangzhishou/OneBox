package com.wanbaohe.diceroller.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanbaohe.diceroller.component.DiceResult
import com.wanbaohe.diceroller.component.DiceType
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 单颗骰子 Composable
 *
 * - D6：Canvas 手绘圆角正方形 + 传统点阵
 * - 其余骰型：Canvas 手绘对应多边形 + Text 数字叠加显示
 * - isRolling=true 时触发 3D 弹跳翻滚动效（graphicsLayer rotationX/Y）
 */
@Composable
fun DiceView(
    result: DiceResult?,
    isRolling: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    faceColor: Color = MaterialTheme.colorScheme.primaryContainer,
    dotColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    val rotationX = remember { Animatable(0f) }
    val rotationY = remember { Animatable(0f) }
    val scale    = remember { Animatable(1f) }

    LaunchedEffect(isRolling) {
        if (isRolling) {
            launch {
                rotationX.animateTo(
                    targetValue = rotationX.value + (3..5).random() * 360f,
                    animationSpec = tween(durationMillis = 800)
                )
            }
            launch {
                rotationY.animateTo(
                    targetValue = rotationY.value + (2..4).random() * 360f,
                    animationSpec = tween(durationMillis = 800)
                )
            }
            launch {
                scale.animateTo(0.75f, tween(200))
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
    }

    val type  = result?.type  ?: DiceType.D6
    val value = result?.value

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .graphicsLayer {
                this.rotationX  = rotationX.value % 360f
                this.rotationY  = rotationY.value % 360f
                this.scaleX     = scale.value
                this.scaleY     = scale.value
                cameraDistance  = 12f * density
            }
    ) {
        // ── 骰面形状 ──────────────────────────────────────────────────────
        Canvas(modifier = Modifier.size(size)) {
            drawDiceShape(type = type, faceColor = faceColor, dotColor = dotColor, value = value)
        }

        // ── 数字叠加（D6 用点阵，其余用 Text）──────────────────────────────
        if (type != DiceType.D6 && value != null) {
            val fontSize = when (size) {
                in 0.dp..60.dp -> 14.sp
                in 60.dp..90.dp -> 18.sp
                else -> 22.sp
            }
            Text(
                text = value.toString(),
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                color = dotColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─── Canvas：只负责绘制外形 + D6 点阵 ─────────────────────────────────────────

private fun DrawScope.drawDiceShape(
    type: DiceType,
    faceColor: Color,
    dotColor: Color,
    value: Int?,
) {
    val w  = size.width
    val h  = size.height
    val cx = w / 2f
    val cy = h / 2f
    val r  = minOf(w, h) / 2f * 0.88f

    when (type) {
        DiceType.D4 -> {
            // 等边三角形（尖朝上）
            drawPath(regularPolygon(cx, cy, r, 3, startAngle = -PI.toFloat() / 2f), faceColor)
        }

        DiceType.D6 -> {
            // 圆角正方形
            drawRoundRect(
                color = faceColor,
                topLeft = androidx.compose.ui.geometry.Offset(cx - r, cy - r),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.2f)
            )
            // D6 专用传统点阵
            if (value != null) drawDotsD6(cx, cy, r, value, dotColor)
        }

        DiceType.D8 -> {
            // 菱形（正方形旋转 45°）
            drawPath(regularPolygon(cx, cy, r, 4, startAngle = 0f), faceColor)
        }

        DiceType.D10 -> {
            // 五边形
            drawPath(regularPolygon(cx, cy, r, 5, startAngle = -PI.toFloat() / 2f), faceColor)
        }

        DiceType.D12 -> {
            // 六边形（扁平顶部）
            drawPath(regularPolygon(cx, cy, r, 6, startAngle = 0f), faceColor)
        }

        DiceType.D20 -> {
            // 六边形（尖顶）—— 视觉近似二十面体
            drawPath(regularPolygon(cx, cy, r, 6, startAngle = -PI.toFloat() / 6f), faceColor)
        }
    }
}

// ─── 正多边形路径 ──────────────────────────────────────────────────────────────

private fun regularPolygon(
    cx: Float, cy: Float, r: Float,
    sides: Int,
    startAngle: Float = -PI.toFloat() / 2f,
): Path {
    val path = Path()
    for (i in 0 until sides) {
        val angle = startAngle + i * (2f * PI.toFloat() / sides)
        val x = cx + r * cos(angle)
        val y = cy + r * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

// ─── D6 传统六面点阵 ──────────────────────────────────────────────────────────

private fun DrawScope.drawDotsD6(
    cx: Float, cy: Float, r: Float, value: Int, dotColor: Color,
) {
    val dotR = r * 0.12f
    val off  = r * 0.52f

    val tl = Offset(cx - off, cy - off)
    val ml = Offset(cx - off, cy)
    val bl = Offset(cx - off, cy + off)
    val tr = Offset(cx + off, cy - off)
    val mr = Offset(cx + off, cy)
    val br = Offset(cx + off, cy + off)
    val cc = Offset(cx,       cy)

    val positions = when (value) {
        1 -> listOf(cc)
        2 -> listOf(tl, br)
        3 -> listOf(tl, cc, br)
        4 -> listOf(tl, tr, bl, br)
        5 -> listOf(tl, tr, cc, bl, br)
        6 -> listOf(tl, ml, bl, tr, mr, br)
        else -> emptyList()
    }
    positions.forEach { drawCircle(dotColor, dotR, it) }
}
