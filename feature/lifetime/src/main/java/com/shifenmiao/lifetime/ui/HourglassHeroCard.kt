package com.shifenmiao.lifetime.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.lifetime.R
import com.shifenmiao.lifetime.domain.LifeTimeData
import com.shifenmiao.lifetime.domain.RemainingLifeData
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import kotlin.math.abs
import kotlin.random.Random

/**
 * 时光沙漏主视觉卡。
 *
 * 视觉分层（自下而上）：
 *  1. GlassCard 玻璃底 + 径向 primary 发光
 *  2. Canvas 绘制沙漏轮廓（双层描边）+ 40 颗沙粒下落动画
 *  3. 文本层：标签 → 年 → 天 → 时分秒（每行水平居中）
 *
 * 点击整体区域切换 PAST / REMAINING。使用 GlassCard(onClick=) 重载确保按下态也保持卡片圆角。
 */
@Composable
fun HourglassHeroCard(
    pastTimeData: LifeTimeData,
    remainingLifeData: RemainingLifeData,
    displayMode: TimeDisplayMode,
    onToggleMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val yearsLabel = stringResource(R.string.lifetime_unit_years)
    val daysLabel = stringResource(R.string.lifetime_unit_days)
    val hoursLabel = stringResource(R.string.lifetime_unit_hours)
    val minutesLabel = stringResource(R.string.lifetime_unit_minutes)
    val secondsLabel = stringResource(R.string.lifetime_unit_seconds)
    val pastLabel = stringResource(R.string.lifetime_time_card_label)
    val remainingLabel = stringResource(R.string.lifetime_remaining_time_label)

    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    GlassCard(
        onClick = onToggleMode,
        modifier = modifier,
        shape = OneBoxDesignSystem.sectionCardShape,
        containerAlpha = 0.34f,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
        ) {
            HourglassBackdrop(
                modifier = Modifier.fillMaxSize(),
                primary = primary,
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = OneBoxDesignSystem.cardPadding,
                        vertical = 20.dp,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = displayMode,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(300)) +
                            slideInVertically { it / 3 })
                            .togetherWith(
                                fadeOut(animationSpec = tween(220)) +
                                    slideOutVertically { -it / 3 }
                            )
                    },
                    label = "hourglass_mode"
                ) { mode ->
                    val label = if (mode == TimeDisplayMode.PAST) pastLabel else remainingLabel
                    val years: Long
                    val days: Long
                    val hours: Long
                    val minutes: Long
                    val seconds: Long
                    if (mode == TimeDisplayMode.PAST) {
                        years = pastTimeData.years
                        days = pastTimeData.totalDays
                        hours = pastTimeData.hours % 24
                        minutes = pastTimeData.minutes % 60
                        seconds = pastTimeData.seconds % 60
                    } else {
                        years = remainingLifeData.years
                        days = remainingLifeData.days
                        hours = remainingLifeData.hours % 24
                        minutes = remainingLifeData.minutes % 60
                        seconds = remainingLifeData.seconds % 60
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 2.sp,
                            ),
                            color = onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        StackedTimeValue(
                            value = years,
                            unit = yearsLabel,
                            valueSize = 64.sp,
                            unitSize = 18.sp,
                            color = primary,
                            subColor = onSurfaceVariant,
                        )
                        StackedTimeValue(
                            value = days,
                            unit = daysLabel,
                            valueSize = 36.sp,
                            unitSize = 16.sp,
                            color = primary,
                            subColor = onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TimeInlineRow(
                            hours = hours,
                            hoursLabel = hoursLabel,
                            minutes = minutes,
                            minutesLabel = minutesLabel,
                            seconds = seconds,
                            secondsLabel = secondsLabel,
                            color = onSurface,
                            subColor = onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HourglassBackdrop(
    modifier: Modifier = Modifier,
    primary: Color,
) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val w = widthPx
        val h = heightPx
        val centerX = w / 2f
        val neckY = h * 0.5f
        val neckHalfWidth = w * 0.07f
        val topHalfWidth = w * 0.34f
        val bottomHalfWidth = w * 0.34f

        val particles = remember {
            val rng = Random(0xC0FFEE)
            Array(40) {
                SandParticle(
                    xRatio = 0.3f + rng.nextFloat() * 0.4f,
                    yRatio = rng.nextFloat() * 0.42f,
                    speed = 0.18f + rng.nextFloat() * 0.22f,
                    phase = rng.nextFloat() * 6.28f,
                    size = 1.6f + rng.nextFloat() * 1.8f,
                )
            }
        }
        var frameTick by remember { mutableStateOf(0L) }

        LaunchedEffect(Unit) {
            var lastFrame = 0L
            while (true) {
                withFrameNanos { frame ->
                    if (lastFrame == 0L) lastFrame = frame
                    val dt = ((frame - lastFrame).coerceAtLeast(0L)) / 1_000_000_000f
                    lastFrame = frame
                    if (dt in 0.001f..0.1f) {
                        particles.forEachIndexed { i, p ->
                            val newY = p.yRatio + p.speed * dt
                            val yClamped = if (newY > 1f) newY - 1f else newY
                            val xBias = (yClamped - 0.5f) * 0.6f
                            val newX = (p.xRatio + xBias * dt).coerceIn(0.05f, 0.95f)
                            particles[i] = p.copy(
                                yRatio = yClamped,
                                xRatio = if (newY > 1f) 0.3f + (p.xRatio - 0.3f) * 0.4f else newX,
                                phase = p.phase + dt * 4f,
                            )
                        }
                        frameTick = frame
                    }
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val ringColor = primary.copy(alpha = 0.55f)
            val innerRing = Color.White.copy(alpha = 0.16f)
            val neckColor = primary.copy(alpha = 0.18f)

            val topPath = Path().apply {
                moveTo(centerX - topHalfWidth, 0f)
                lineTo(centerX + topHalfWidth, 0f)
                lineTo(centerX + neckHalfWidth, neckY)
                lineTo(centerX - neckHalfWidth, neckY)
                close()
            }
            val bottomPath = Path().apply {
                moveTo(centerX - neckHalfWidth, neckY)
                lineTo(centerX + neckHalfWidth, neckY)
                lineTo(centerX + bottomHalfWidth, h)
                lineTo(centerX - bottomHalfWidth, h)
                close()
            }

            drawPath(
                path = topPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primary.copy(alpha = 0.22f),
                        primary.copy(alpha = 0.05f),
                    ),
                    startY = 0f,
                    endY = neckY,
                )
            )
            drawPath(
                path = bottomPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primary.copy(alpha = 0.05f),
                        primary.copy(alpha = 0.18f),
                    ),
                    startY = neckY,
                    endY = h,
                )
            )

            val fullSilhouette = Path().apply {
                addPath(topPath)
                addPath(bottomPath)
            }
            drawPath(
                path = fullSilhouette,
                color = ringColor,
                style = Stroke(width = 1.6f),
            )
            val inner = Path().apply {
                val inset = 8f
                val iTopHalf = topHalfWidth - inset
                val iNeck = neckHalfWidth - 2f
                moveTo(centerX - iTopHalf, inset)
                lineTo(centerX + iTopHalf, inset)
                lineTo(centerX + iNeck, neckY)
                lineTo(centerX - iNeck, neckY)
                close()
            }
            val innerBottom = Path().apply {
                val inset = 8f
                val iBottomHalf = bottomHalfWidth - inset
                val iNeck = neckHalfWidth - 2f
                moveTo(centerX - iNeck, neckY)
                lineTo(centerX + iNeck, neckY)
                lineTo(centerX + iBottomHalf, h - inset)
                lineTo(centerX - iBottomHalf, h - inset)
                close()
            }
            drawPath(path = inner, color = innerRing, style = Stroke(width = 0.6f))
            drawPath(path = innerBottom, color = innerRing, style = Stroke(width = 0.6f))

            val neckRect = androidx.compose.ui.geometry.Rect(
                left = centerX - neckHalfWidth - 1f,
                top = neckY - 14f,
                right = centerX + neckHalfWidth + 1f,
                bottom = neckY + 14f,
            )
            drawRoundRect(
                color = neckColor,
                topLeft = Offset(neckRect.left, neckRect.top),
                size = Size(neckRect.width, neckRect.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
            )

            drawSandParticles(
                particles = particles,
                centerX = centerX,
                topHalfWidth = topHalfWidth,
                neckHalfWidth = neckHalfWidth,
                bottomHalfWidth = bottomHalfWidth,
                neckY = neckY,
                height = h,
                primary = primary,
            )

            @Suppress("UNUSED_EXPRESSION")
            frameTick
        }
    }
}

private fun DrawScope.drawSandParticles(
    particles: Array<SandParticle>,
    centerX: Float,
    topHalfWidth: Float,
    neckHalfWidth: Float,
    bottomHalfWidth: Float,
    neckY: Float,
    height: Float,
    primary: Color,
) {
    particles.forEach { p ->
        val t = p.yRatio
        val x: Float
        val maxHalfWidth: Float
        if (t < 0.5f) {
            val localT = t / 0.5f
            maxHalfWidth = lerp(topHalfWidth, neckHalfWidth, localT)
            x = centerX + (p.xRatio - 0.5f) * 2f * maxHalfWidth
        } else {
            val localT = (t - 0.5f) / 0.5f
            maxHalfWidth = lerp(neckHalfWidth, bottomHalfWidth, localT)
            x = centerX + (p.xRatio - 0.5f) * 2f * maxHalfWidth
        }
        val y = t * height
        val yFromNeck = abs(y - neckY)
        val alpha = if (yFromNeck < 6f) 0.35f else 0.92f
        drawCircle(
            color = primary.copy(alpha = alpha),
            radius = p.size,
            center = Offset(x, y),
        )
        drawCircle(
            color = primary.copy(alpha = alpha * 0.45f),
            radius = p.size + 2.4f,
            center = Offset(x, y),
        )
    }
}

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

private data class SandParticle(
    val xRatio: Float,
    val yRatio: Float,
    val speed: Float,
    val phase: Float,
    val size: Float,
)

@Composable
private fun StackedTimeValue(
    value: Long,
    unit: String,
    valueSize: androidx.compose.ui.unit.TextUnit,
    unitSize: androidx.compose.ui.unit.TextUnit,
    color: Color,
    subColor: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = valueSize,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp,
            ),
            color = color,
            textAlign = TextAlign.Center,
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = unitSize,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
            ),
            color = subColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun TimeInlineRow(
    hours: Long,
    hoursLabel: String,
    minutes: Long,
    minutesLabel: String,
    seconds: Long,
    secondsLabel: String,
    color: Color,
    subColor: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        InlineTimeBlock(hours, hoursLabel, color, subColor)
        DividerDot(subColor)
        InlineTimeBlock(minutes, minutesLabel, color, subColor)
        DividerDot(subColor)
        InlineTimeBlock(seconds, secondsLabel, color, subColor)
    }
}

@Composable
private fun InlineTimeBlock(
    value: Long,
    label: String,
    color: Color,
    subColor: Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString().padStart(2, '0'),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            ),
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
            ),
            color = subColor,
        )
    }
}

@Composable
private fun DividerDot(color: Color) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(22.dp)
            .padding(horizontal = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = color.copy(alpha = 0.3f),
                )
            }
        }
    }
}
