package com.wanbaohe.speedtest.screen.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.shifenmiao.theme.AppTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
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
 * - 36 条外圈刻度线
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
    gaugeSize: Dp = 280.dp,
    strokeWidth: Dp = 16.dp,
    measuringLatency: Boolean = false
) {
    // Completed results have no perpetual animations.
    val rotationAngle = if (status == SpeedTestStatus.MEASURING) {
        val infiniteTransition = rememberInfiniteTransition(label = "gauge_anim")
        val angle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )
        angle
    } else 0f

    val breathScale = if (status == SpeedTestStatus.IDLE) {
        val infiniteTransition = rememberInfiniteTransition(label = "start_breath")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "breath"
        )
        scale
    } else 1f

    val displayedMbps by animateFloatAsState(
        targetValue = liveMbps,
        animationSpec = tween(if (status == SpeedTestStatus.DONE) 650 else 240),
        label = "speed_value"
    )

    // 弧线 sweep 动画
    val sweepAngle by animateFloatAsState(
        targetValue = when (status) {
            SpeedTestStatus.IDLE -> 0f
            SpeedTestStatus.MEASURING -> progress.coerceIn(0f, 1f) * 360f
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
    val tickColor = primaryColor.copy(alpha = 0.3f)

    // 弧线渐变色（主色 → 辅色 → 主色）
    val arcBrush = Brush.sweepGradient(
        colors = listOf(primaryColor, secondaryColor, primaryColor)
    )

    // 发光颜色（低透明度主色）
    val glowColor = primaryColor.copy(alpha = 0.18f)

    // 开始按钮填充用品牌色（primary → secondary），内容用 onPrimary 保证对比度：
    // getPrimaryColor() 取的是 onPrimaryContainer（文字色），直接当填充会和内容同色而"隐形"。
    val actionGradient = Brush.linearGradient(
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
    )
    val onActionColor = MaterialTheme.colorScheme.onPrimary

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(gaugeSize)
            .semantics {
                if (status == SpeedTestStatus.MEASURING) {
                    progressBarRangeInfo = if (measuringLatency) ProgressBarRangeInfo.Indeterminate
                    else ProgressBarRangeInfo(progress.coerceIn(0f, 1f), 0f..1f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val inset = size.minDimension * (30f / 280f)
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

            // Outer ticks follow the reference's 36 divisions.
            val tickCount = 36
            val tickInnerRadius = arcRadius + strokeWidth.toPx() * 0.5f + 6.dp.toPx()
            val tickOuterRadius = tickInnerRadius + 8.dp.toPx()
            for (i in 0 until tickCount) {
                val angleDeg = (i * 10f - 90f) * (Math.PI / 180f)
                val startX = center.x + (tickInnerRadius * cos(angleDeg)).toFloat()
                val startY = center.y + (tickInnerRadius * sin(angleDeg)).toFloat()
                val endX = center.x + (tickOuterRadius * cos(angleDeg)).toFloat()
                val endY = center.y + (tickOuterRadius * sin(angleDeg)).toFloat()
                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (i % 3 == 0) 2.dp.toPx() else 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // ── 3. 外发光弧线（粗、低透明度）────────────────────────────
            val glowStroke = Stroke(width = strokeWidth.toPx() * 2.2f, cap = StrokeCap.Round)
            val startAngle = -90f
            drawArc(
                color = glowColor,
                startAngle = startAngle, sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = arcOffset, size = arcSize, style = glowStroke,
                alpha = 1f
            )

            // ── 4. 主弧线（渐变色）──────────────────────────────────────
            if (sweepAngle > 0f) {
                drawArc(
                    brush = arcBrush,
                    startAngle = -90f, sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = arcOffset, size = arcSize, style = stroke,
                    alpha = 1f
                )
            }
            if (status == SpeedTestStatus.MEASURING) {
                rotate(rotationAngle, center) {
                    drawArc(
                        brush = Brush.sweepGradient(listOf(primaryColor.copy(alpha = 0f), primaryColor.copy(alpha = 0.65f))),
                        startAngle = 0f, sweepAngle = 300f, useCenter = false,
                        topLeft = Offset(5.dp.toPx(), 5.dp.toPx()),
                        size = Size(size.width - 10.dp.toPx(), size.height - 10.dp.toPx()),
                        style = Stroke(3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }

        // ── 中心文字区域 ──────────────────────────────────────────────────
        AnimatedContent(
            targetState = status,
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
            label = "gauge_center"
        ) { centerStatus ->
            Column(
                modifier = Modifier.widthIn(max = 196.dp).padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (centerStatus) {
                    SpeedTestStatus.IDLE -> {
                        Box(
                            Modifier.size(88.dp)
                                .graphicsLayer { scaleX = breathScale; scaleY = breathScale }
                                .clip(CircleShape)
                                .background(actionGradient)
                                .clickable(role = Role.Button, onClick = onClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                stringResource(R.string.speed_test_start),
                                Modifier.size(36.dp),
                                tint = onActionColor
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.speed_test_start),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = centerTextColor
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.speed_test_estimated_data, estimatedMb),
                            fontSize = 11.sp,
                            color = subtitleColor,
                            textAlign = TextAlign.Center
                        )
                    }
                    SpeedTestStatus.MEASURING, SpeedTestStatus.DONE -> {
                        val speedText = "%.1f".format(displayedMbps)
                        Text(
                            text = speedText,
                            fontSize = (if (speedText.length > 6) 34 else 46).sp,
                            maxLines = 1,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (centerStatus == SpeedTestStatus.DONE) primaryColor else centerTextColor
                        )
                        Text(
                            text = stringResource(R.string.speed_test_unit_mbps),
                            fontSize = 13.sp,
                            color = subtitleColor
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (centerStatus == SpeedTestStatus.DONE) {
                                stringResource(R.string.speed_test_result_mb_per_sec, liveMbps / 8f)
                            } else {
                                stringResource(if (measuringLatency) R.string.speed_test_measuring_latency else R.string.speed_test_measuring)
                            },
                            fontSize = 11.sp,
                            color = if (centerStatus == SpeedTestStatus.DONE) subtitleColor else primaryColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
