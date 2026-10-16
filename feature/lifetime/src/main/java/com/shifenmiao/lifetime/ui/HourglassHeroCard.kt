package com.shifenmiao.lifetime.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign


import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.lifetime.R
import com.shifenmiao.lifetime.domain.LifeTimeData
import com.shifenmiao.lifetime.domain.RemainingLifeData
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/**
 * 设计稿口径：内容区宽 330dp、卡片高 430dp。
 * 字号按内容区宽度相对 330dp 缩放。窄屏（不超过设计宽）高度保持 430dp；
 * 宽屏按同一比例拉高，否则字号变大后年 / 天 / 时分秒会叠在固定高度里。
 */
private val HeroDesignWidth = 330.dp
private val HeroCardHeight = 430.dp

/**
 * 时光沙漏主视觉卡。
 *
 * 视觉分层（自下而上）：
 *  1. GlassCard 玻璃底
 *  2. Canvas 沙漏轮廓（单层细描边 + 极淡渐变填充）+ 偏左 S 形曲线下落的沙流
 *  3. 文本层：按卡片高度比例分段叠放（标签 → 年 → 天 → 时分秒），
 *     标签与年落在上壶内，天与时分秒落在下壶内
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

    val primary = MaterialTheme.colorScheme.onPrimaryContainer
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    GlassCard(
        onClick = onToggleMode,
        modifier = modifier,
        shape = OneBoxDesignSystem.sectionCardShape,
        containerAlpha = 0.34f,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val contentWidth = (maxWidth - OneBoxDesignSystem.cardPadding * 2).coerceAtLeast(1.dp)
            val sizeRatio = contentWidth / HeroDesignWidth
            // 窄屏不缩高度；宽屏跟字号同一比例拉高，沙漏轮廓和文字一起变高
            val heightScale = sizeRatio.coerceAtLeast(1f)
            val cardHeight = HeroCardHeight * heightScale

            Box(modifier = Modifier.fillMaxWidth().height(cardHeight)) {
                HourglassBackdrop(
                    modifier = Modifier.fillMaxSize(),
                    primary = primary,
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = OneBoxDesignSystem.cardPadding),
                ) {
                    // 字号按内容区宽度换算（与沙漏轮廓同口径）；纵向位置按整卡高度比例分层，
                    // 段高用 TextMeasurer 实测，避免行高留白把段落推歪
                    val density = LocalDensity.current
                    val textMeasurer = rememberTextMeasurer()

                    val labelSize = 15.sp * sizeRatio
                    val yearsSize = 58.sp * sizeRatio
                    val unitSize = 19.sp * sizeRatio
                    val daysSize = 46.sp * sizeRatio
                    val daysUnitSize = 18.sp * sizeRatio
                    val clockSize = 36.sp * sizeRatio
                    val clockUnitSize = 16.sp * sizeRatio

                    val labelStyle = MaterialTheme.typography.labelSmall.copy(
                        fontSize = labelSize,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                    )
                    val yearsValueStyle = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = yearsSize,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp,
                    )
                    val yearsUnitStyle = MaterialTheme.typography.labelMedium.copy(
                        fontSize = unitSize,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp,
                    )
                    val daysValueStyle = yearsValueStyle.copy(
                        fontSize = daysSize,
                        letterSpacing = (-0.5).sp,
                    )
                    val daysUnitStyle = yearsUnitStyle.copy(
                        fontSize = daysUnitSize,
                    )
                    val clockValueStyle = MaterialTheme.typography.titleLarge.copy(
                        fontSize = clockSize,
                        fontWeight = FontWeight.Bold,
                    )
                    val clockUnitStyle = MaterialTheme.typography.labelSmall.copy(
                        fontSize = clockUnitSize,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp,
                    )

                    val labelHeight = remember(labelStyle) {
                        with(density) { textMeasurer.measure("Ag", labelStyle).size.height.toDp() }
                    }
                    val yearsValueHeight = remember(yearsValueStyle) {
                        with(density) { textMeasurer.measure("0", yearsValueStyle).size.height.toDp() }
                    }
                    val yearsUnitHeight = remember(yearsUnitStyle) {
                        with(density) { textMeasurer.measure("Ag", yearsUnitStyle).size.height.toDp() }
                    }
                    val daysValueHeight = remember(daysValueStyle) {
                        with(density) { textMeasurer.measure("0", daysValueStyle).size.height.toDp() }
                    }
                    val daysUnitHeight = remember(daysUnitStyle) {
                        with(density) { textMeasurer.measure("Ag", daysUnitStyle).size.height.toDp() }
                    }
                    val clockValueHeight = remember(clockValueStyle) {
                        with(density) { textMeasurer.measure("0", clockValueStyle).size.height.toDp() }
                    }
                    val clockUnitHeight = remember(clockUnitStyle) {
                        with(density) { textMeasurer.measure("Ag", clockUnitStyle).size.height.toDp() }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        AnimatedContent(
                            targetState = displayMode,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(280))
                                    .togetherWith(fadeOut(animationSpec = tween(200)))
                            },
                            label = "hourglass_mode",
                        ) { mode ->
                            val isPast = mode == TimeDisplayMode.PAST
                            val modeLabel = if (isPast) pastLabel else remainingLabel
                            val modeYears = if (isPast) pastTimeData.years else remainingLifeData.years
                            val modeDays = if (isPast) pastTimeData.totalDays else remainingLifeData.days
                            val modeHours = if (isPast) {
                                pastTimeData.hours % 24
                            } else {
                                remainingLifeData.hours % 24
                            }
                            val modeMinutes = if (isPast) {
                                pastTimeData.minutes % 60
                            } else {
                                remainingLifeData.minutes % 60
                            }
                            val modeSeconds = if (isPast) {
                                pastTimeData.seconds % 60
                            } else {
                                remainingLifeData.seconds % 60
                            }

                            Box(modifier = Modifier.fillMaxSize()) {
                                HeroTextBlock(
                                    cardHeight = cardHeight,
                                    contentHeight = labelHeight,
                                    centerFraction = 0.160f,
                                ) {
                                    Text(
                                        text = modeLabel,
                                        style = labelStyle,
                                        color = onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                    )
                                }

                                // 年（大号，独占上壶）
                                HeroTextBlock(
                                    cardHeight = cardHeight,
                                    contentHeight = yearsValueHeight + yearsUnitHeight,
                                    centerFraction = 0.310f,
                                ) {
                                    StackedTimeValue(
                                        value = modeYears,
                                        unit = yearsLabel,
                                        valueStyle = yearsValueStyle,
                                        unitStyle = yearsUnitStyle,
                                        color = primary,
                                        subColor = onSurfaceVariant,
                                    )
                                }

                                // 天（落在下壶、细颈之下，与细颈留出呼吸空隙）
                                HeroTextBlock(
                                    cardHeight = cardHeight,
                                    contentHeight = daysValueHeight + daysUnitHeight,
                                    centerFraction = 0.595f,
                                ) {
                                    StackedTimeValue(
                                        value = modeDays,
                                        unit = daysLabel,
                                        valueStyle = daysValueStyle,
                                        unitStyle = daysUnitStyle,
                                        color = primary,
                                        subColor = onSurfaceVariant,
                                    )
                                }

                                // 时 / 分 / 秒（落在下壶中段，底部留足空间）
                                HeroTextBlock(
                                    cardHeight = cardHeight,
                                    contentHeight = clockValueHeight + clockUnitHeight,
                                    centerFraction = 0.80f,
                                ) {
                                    HeroClockRow(
                                        hours = modeHours,
                                        hoursLabel = hoursLabel,
                                        minutes = modeMinutes,
                                        minutesLabel = minutesLabel,
                                        seconds = modeSeconds,
                                        secondsLabel = secondsLabel,
                                        valueColor = MaterialTheme.colorScheme.onSurface,
                                        subColor = onSurfaceVariant,
                                        valueStyle = clockValueStyle,
                                        unitStyle = clockUnitStyle,
                                        spacing = 24.dp * heightScale,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 文本分段容器：以 [centerFraction]（相对整卡高度）为垂直中心，把内容水平居中放置。
 * [contentHeight] 由 TextMeasurer 实测得到，用来把"中心"换算成 Column 的顶部偏移。
 */
@Composable
private fun HeroTextBlock(
    cardHeight: Dp,
    contentHeight: Dp,
    centerFraction: Float,
    content: @Composable () -> Unit,
) {
    val top = (cardHeight * centerFraction - contentHeight / 2).coerceAtLeast(0.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = top),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        content()
    }
}

/** 时 / 分 / 秒 一行三组。 */
@Composable
private fun HeroClockRow(
    hours: Long,
    hoursLabel: String,
    minutes: Long,
    minutesLabel: String,
    seconds: Long,
    secondsLabel: String,
    valueColor: Color,
    subColor: Color,
    valueStyle: TextStyle,
    unitStyle: TextStyle,
    spacing: Dp,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        ClockBlock(hours, hoursLabel, valueColor, subColor, valueStyle, unitStyle)
        ClockBlock(minutes, minutesLabel, valueColor, subColor, valueStyle, unitStyle)
        ClockBlock(seconds, secondsLabel, valueColor, subColor, valueStyle, unitStyle)
    }
}

@Composable
private fun ClockBlock(
    value: Long,
    label: String,
    color: Color,
    subColor: Color,
    valueStyle: TextStyle,
    unitStyle: TextStyle,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString().padStart(2, '0'),
            style = valueStyle,
            color = color,
            maxLines = 1,
        )
        Text(
            text = label,
            style = unitStyle,
            color = subColor,
            maxLines = 1,
        )
    }
}

/** 大号数值 + 单位标签的竖排组合。 */
@Composable
private fun StackedTimeValue(
    value: Long,
    unit: String,
    valueStyle: TextStyle,
    unitStyle: TextStyle,
    color: Color,
    subColor: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Text(
            text = value.toString(),
            style = valueStyle,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Text(
            text = unit,
            style = unitStyle,
            color = subColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
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

        // 轮廓比例按参考图实测：上下壶镜像对称，端部半宽 ≈ 0.44w，细颈 ≈ 0.03w，颈位 0.46h
        val geometry = remember(w, h) {
            HourglassGeometry(
                width = w,
                height = h,
                neckRatio = 0.46f,
                halfWidthTop = w * 0.44f,
                halfWidthNeck = w * 0.03f,
                halfWidthBottom = w * 0.44f,
            )
        }

        // 沙粒：偏左 S 形曲线的主流 + 上壶里悬停闪烁的静粒
        val particles = remember {
            val rng = Random(0xC0FFEE)
            Array(34) { index ->
                if (index < 8) {
                    SandParticle(
                        xRatio = (0.5f + (rng.nextFloat() - 0.5f) * 1.1f).coerceIn(0.08f, 0.92f),
                        yRatio = rng.nextFloat() * 0.40f,
                        speed = 0f,
                        phase = rng.nextFloat() * 6.28f,
                        size = 1.3f + rng.nextFloat() * 1.2f,
                    )
                } else {
                    SandParticle(
                        xRatio = (0.5f + (rng.nextFloat() - 0.5f) * 0.8f).coerceIn(0.08f, 0.92f),
                        yRatio = rng.nextFloat(),
                        speed = 0.17f + rng.nextFloat() * 0.16f,
                        phase = rng.nextFloat() * 6.28f,
                        size = 1.4f + rng.nextFloat() * 1.5f,
                    )
                }
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
                        particles.forEachIndexed { index, particle ->
                            if (particle.speed <= 0f) {
                                // 上壶静粒只做轻微闪烁
                                particles[index] = particle.copy(phase = particle.phase + dt * 1.1f)
                                return@forEachIndexed
                            }
                            // 越靠近细颈越快，底部略减速，形成落沙节奏
                            val gravity = when {
                                particle.yRatio < 0.36f -> 0.95f
                                particle.yRatio < 0.48f -> 1.55f
                                particle.yRatio < 0.84f -> 1.15f
                                else -> 0.5f
                            }
                            val nextY = particle.yRatio + particle.speed * gravity * dt
                            val phase = particle.phase + dt * 3.6f
                            if (nextY > 0.96f) {
                                // 落到闭合底边后回到上壶顶端，重新汇入沙流
                                particles[index] = particle.copy(
                                    yRatio = nextY - 0.96f,
                                    xRatio = (0.5f + (particle.xRatio - 0.5f) * 0.5f)
                                        .coerceIn(0.12f, 0.88f),
                                    phase = phase,
                                )
                            } else {
                                particles[index] = particle.copy(
                                    yRatio = nextY,
                                    phase = phase,
                                )
                            }
                        }
                        frameTick = frame
                    }
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val ringColor = Color(0xFF9AA6C4).copy(alpha = 0.85f)

            // 壶身填充保持极淡：只托住轮廓，不压住文字
            drawPath(
                path = geometry.topFill,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primary.copy(alpha = 0.065f),
                        primary.copy(alpha = 0.055f),
                        primary.copy(alpha = 0.005f),
                    ),
                    startY = 0f,
                    endY = geometry.neckY,
                )
            )
            drawPath(
                path = geometry.bottomFill,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primary.copy(alpha = 0.005f),
                        primary.copy(alpha = 0.015f),
                        primary.copy(alpha = 0.028f),
                    ),
                    startY = geometry.neckY,
                    endY = h,
                )
            )
            drawPath(
                path = geometry.outline,
                color = ringColor,
                style = Stroke(width = 1.4f),
            )

            drawSandParticles(
                particles = particles,
                geometry = geometry,
            )

            @Suppress("UNUSED_EXPRESSION")
            frameTick
        }
    }
}

/**
 * 缓动：两端导数为 0（轮廓在顶点/细颈处自然收束），
 * 中段比余弦更"直"，接近参考图里保持宽度到最后才收口的弧线。
 */
private fun sharpEase(t: Float, exponent: Float = 3.2f): Float {
    val u = t.coerceIn(0f, 1f)
    val cosine = 0.5f - 0.5f * cos(PI.toFloat() * u)
    val steep = u.pow(exponent)
    return (cosine * 0.35f + steep * 0.65f).coerceIn(0f, 1f)
}

/**
 * 沙漏轮廓：侧壁按缓动采样后用 Catmull-Rom 转三次贝塞尔。
 * 上下壶关于细颈镜像：同一条缓动曲线，从细颈向两端张开到 [halfWidthTop] / [halfWidthBottom]。
 * 顶边延伸到卡片上缘被裁掉；底部在卡内收尾——底边两端圆角下沉、中部微微鼓起闭合。
 */
private class HourglassGeometry(
    val width: Float,
    val height: Float,
    neckRatio: Float,
    val halfWidthTop: Float,
    val halfWidthNeck: Float,
    val halfWidthBottom: Float,
) {
    val centerX: Float = width / 2f
    val neckY: Float = height * neckRatio

    /** 下壶底边高度：比卡片底缘高一点，给底部收口留出可见边距 */
    val bottomY: Float = height * 0.945f

    /** 底边收口下沉的最低点 */
    val capBottomY: Float = bottomY + height * 0.022f

    /** 任意高度处的半宽，与侧壁曲线一致，供沙粒贴壁使用。 */
    fun halfWidthAt(y: Float): Float {
        return if (y <= neckY) {
            val t = (y / neckY.coerceAtLeast(1f)).coerceIn(0f, 1f)
            lerp(halfWidthTop, halfWidthNeck, sharpEase(t))
        } else {
            // 与上壶镜像：按"距底边的剩余比例"套用同一条缓动，张开到底边
            val t = ((bottomY - y) / (bottomY - neckY).coerceAtLeast(1f)).coerceIn(0f, 1f)
            lerp(halfWidthBottom, halfWidthNeck, sharpEase(t))
        }
    }

    private fun leftWallPoints(): List<Offset> {
        val pts = ArrayList<Offset>(20)
        for (i in 0..10) {
            val y = lerp(0f, neckY, i / 10f)
            pts.add(Offset(centerX - halfWidthAt(y), y))
        }
        for (i in 1..10) {
            val y = lerp(neckY, bottomY, i / 10f)
            pts.add(Offset(centerX - halfWidthAt(y), y))
        }
        return pts
    }

    private fun rightWallPoints(): List<Offset> {
        val pts = ArrayList<Offset>(20)
        for (i in 0..10) {
            val y = lerp(0f, neckY, i / 10f)
            pts.add(Offset(centerX + halfWidthAt(y), y))
        }
        for (i in 1..10) {
            val y = lerp(neckY, bottomY, i / 10f)
            pts.add(Offset(centerX + halfWidthAt(y), y))
        }
        return pts
    }

    private fun topBoundaryPoints(): List<Offset> {
        val pts = ArrayList<Offset>(22)
        for (i in 0..10) {
            val y = lerp(0f, neckY, i / 10f)
            pts.add(Offset(centerX - halfWidthAt(y), y))
        }
        for (i in 10 downTo 0) {
            val y = lerp(0f, neckY, i / 10f)
            pts.add(Offset(centerX + halfWidthAt(y), y))
        }
        return pts
    }

    /** 底边收口：两端之间插两个下沉点，Catmull-Rom 拟合出圆角鼓起的闭合底边 */
    private fun bottomCapPoints(): List<Offset> = listOf(
        Offset(centerX - halfWidthBottom * 0.45f, capBottomY),
        Offset(centerX + halfWidthBottom * 0.45f, capBottomY),
    )

    private fun bottomBoundaryPoints(): List<Offset> {
        val pts = ArrayList<Offset>(24)
        for (i in 0..10) {
            val y = lerp(neckY, bottomY, i / 10f)
            pts.add(Offset(centerX - halfWidthAt(y), y))
        }
        pts.addAll(bottomCapPoints())
        for (i in 10 downTo 0) {
            val y = lerp(neckY, bottomY, i / 10f)
            pts.add(Offset(centerX + halfWidthAt(y), y))
        }
        return pts
    }

    /** 上壶填充 */
    val topFill: Path = smoothPath(topBoundaryPoints())

    /** 下壶填充 */
    val bottomFill: Path = smoothPath(bottomBoundaryPoints())

    /** 连续外轮廓：左壁上→下、底部收口、右壁下→上、顶边 */
    val outline: Path = smoothPath(
        leftWallPoints() + bottomCapPoints() + rightWallPoints().reversed()
    )
}

/**
 * Catmull-Rom → 三次贝塞尔：过点采样序列拟合出自然圆滑的曲线。
 */
private fun smoothPath(points: List<Offset>): Path {
    require(points.size >= 2) { "smoothPath needs at least 2 points" }
    return Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 0 until points.size - 1) {
            val p0 = points[(i - 1).coerceAtLeast(0)]
            val p1 = points[i]
            val p2 = points[i + 1]
            val p3 = points[(i + 2).coerceAtMost(points.size - 1)]
            val c1x = p1.x + (p2.x - p0.x) / 6f
            val c1y = p1.y + (p2.y - p0.y) / 6f
            val c2x = p2.x - (p3.x - p1.x) / 6f
            val c2y = p2.y - (p3.y - p1.y) / 6f
            cubicTo(c1x, c1y, c2x, c2y, p2.x, p2.y)
        }
        close()
    }
}

private fun DrawScope.drawSandParticles(
    particles: Array<SandParticle>,
    geometry: HourglassGeometry,
) {
    val sandColor = Color(0xFF8B98B8)
    particles.forEach { particle ->
        val y = particle.yRatio * geometry.height
        // 沙流中轴是偏左的 S 形曲线：上壶偏左起流，过细颈后在下壶缓缓回中
        val streamOffset = (-0.075f + 0.055f * sin(PI.toFloat() * particle.yRatio)) * geometry.width
        // 横向散布按当前高度的半宽收放，但给一个最小宽度，
        // 否则细颈附近半宽趋近 0，整条沙流会被压成一个点
        val halfWidth = geometry.halfWidthAt(y).coerceAtLeast(geometry.halfWidthTop * 0.10f)
        val x = geometry.centerX + streamOffset + (particle.xRatio - 0.5f) * 2f * halfWidth * 0.55f

        val isHovering = particle.speed <= 0f
        val nearNeck = abs(y - geometry.neckY) < 10f
        val alpha = when {
            nearNeck -> 0.42f
            isHovering -> 0.34f + sin(particle.phase) * 0.12f
            else -> 0.62f
        }
        drawCircle(
            color = sandColor.copy(alpha = alpha.coerceIn(0.1f, 0.8f)),
            radius = particle.size,
            center = Offset(x, y),
        )
        drawCircle(
            color = sandColor.copy(alpha = alpha * 0.35f),
            radius = particle.size + 2f,
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
