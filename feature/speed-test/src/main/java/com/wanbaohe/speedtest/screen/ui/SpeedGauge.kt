package com.wanbaohe.speedtest.screen.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.shifenmiao.theme.AppTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanbaohe.speedtest.R
import com.wanbaohe.speedtest.component.SpeedTestStatus
import kotlin.math.cos
import kotlin.math.sin

/**
 * 测速圆形仪表盘（视觉升级版）
 *
 * - 渐变弧线 + 外发光
 * - 12 条刻度线
 * - IDLE 呼吸动画
 * - 中心大字号速度显示
 */
@Composable
fun SpeedGauge(
    status: SpeedTestStatus,
    liveMbps: Float,
    progress: Float,
    estimatedMb: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gaugeSize: Dp = 220.dp,
    strokeWidth: Dp = 18.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gauge_anim")

    // 测速中：旋转角度
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // IDLE：呼吸 alpha（0.55 ~ 1.0）
    val breathAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    // 弧线 sweep 动画
    val sweepAngle by animateFloatAsState(
        targetValue = when (status) {
            SpeedTestStatus.IDLE -> 360f
            SpeedTestStatus.MEASURING -> (progress * 360f).coerceAtLeast(40f)
            SpeedTestStatus.DONE -> 360f
        },
        animationSpec = tween(400),
        label = "sweep"
    )

    // 颜色体系
    val trackColor = AppTheme.colors.getInactiveContainerColor()
    val primaryColor = AppTheme.colors.getPrimaryColor()
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val centerTextColor = AppTheme.colors.getPrimaryTextColor()
    val subtitleColor = AppTheme.colors.getOnInactiveContainerColor()
    val tickColor = AppTheme.colors.getInactiveContainerColor().copy(alpha = 0.6f)

    // 弧线渐变色（主色 → 辅色 → 主色）
    val arcBrush = Brush.sweepGradient(
        colors = listOf(primaryColor, secondaryColor, primaryColor)
    )

    // 发光颜色（低透明度主色）
    val glowColor = primaryColor.copy(alpha = 0.18f)

    // 弧线当前 alpha
    val arcAlpha = when (status) {
        SpeedTestStatus.IDLE -> breathAlpha
        SpeedTestStatus.MEASURING -> 1f
        SpeedTestStatus.DONE -> 1f
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(gaugeSize)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Canvas(modifier = Modifier.size(gaugeSize)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val inset = strokeWidth.toPx() / 2f
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            val arcOffset = Offset(inset, inset)
            val center = Offset(size.width / 2f, size.height / 2f)
            val arcRadius = (size.width - inset * 2) / 2f

            // ── 1. 轨道背景 ──────────────────────────────────────────────
            drawArc(
                color = trackColor,
                startAngle = -90f, sweepAngle = 360f,
                useCenter = false,
                topLeft = arcOffset, size = arcSize, style = stroke
            )

            // ── 2. 刻度线（12 条，每 30° 一条）──────────────────────────
            val tickCount = 12
            val tickInnerRadius = arcRadius - strokeWidth.toPx() * 0.5f - 4.dp.toPx()
            val tickOuterRadius = arcRadius - strokeWidth.toPx() * 0.5f - 14.dp.toPx()
            for (i in 0 until tickCount) {
                val angleDeg = (i * 30f - 90f) * (Math.PI / 180f)
                val startX = center.x + (tickInnerRadius * cos(angleDeg)).toFloat()
                val startY = center.y + (tickInnerRadius * sin(angleDeg)).toFloat()
                val endX = center.x + (tickOuterRadius * cos(angleDeg)).toFloat()
                val endY = center.y + (tickOuterRadius * sin(angleDeg)).toFloat()
                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // ── 3. 外发光弧线（粗、低透明度）────────────────────────────
            val glowStroke = Stroke(width = strokeWidth.toPx() * 2.2f, cap = StrokeCap.Round)
            val startAngle = if (status == SpeedTestStatus.MEASURING) rotationAngle - 90f else -90f
            drawArc(
                color = glowColor,
                startAngle = startAngle, sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = arcOffset, size = arcSize, style = glowStroke,
                alpha = arcAlpha
            )

            // ── 4. 主弧线（渐变色）──────────────────────────────────────
            rotate(degrees = if (status == SpeedTestStatus.MEASURING) rotationAngle else 0f, pivot = center) {
                drawArc(
                    brush = arcBrush,
                    startAngle = -90f, sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = arcOffset, size = arcSize, style = stroke,
                    alpha = arcAlpha
                )
            }
        }

        // ── 中心文字区域 ──────────────────────────────────────────────────
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when (status) {
                SpeedTestStatus.IDLE -> {
                    Text(
                        text = stringResource(R.string.speed_test_start),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = centerTextColor.copy(alpha = breathAlpha)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.speed_test_estimated_data, estimatedMb),
                        fontSize = 12.sp,
                        color = subtitleColor,
                        textAlign = TextAlign.Center
                    )
                }
                SpeedTestStatus.MEASURING -> {
                    val displayMbps = if (liveMbps > 0f) "%.1f".format(liveMbps) else "..."
                    Text(
                        text = displayMbps,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                    if (liveMbps > 0f) {
                        Text(
                            text = stringResource(R.string.speed_test_unit_mbps),
                            fontSize = 13.sp,
                            color = subtitleColor
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.speed_test_measuring),
                        fontSize = 12.sp,
                        color = subtitleColor
                    )
                }
                SpeedTestStatus.DONE -> {
                    Text(
                        text = "%.1f".format(liveMbps),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                    Text(
                        text = stringResource(R.string.speed_test_unit_mbps),
                        fontSize = 13.sp,
                        color = subtitleColor
                    )
                }
            }
        }
    }
}
