package com.wanbaohe.measurement.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.tintedGlassCardColors
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min
import com.shifenmiao.core.R as CoreR

private val HandleRadius = 16.dp
private val TouchRadius = 48.dp
private val MinArmLength = 32.dp
private val ArcRadius = 44.dp
private const val InitialArmRatio = 0.28f
private const val BreathDurationMs = 1600

class AngleMeasureState {
    var pointA by mutableStateOf<Offset?>(null)
    var pointB by mutableStateOf<Offset?>(null)
    var canvasSize by mutableStateOf(IntSize.Zero)

    private val center: Offset
        get() = Offset(canvasSize.width / 2f, canvasSize.height / 2f)

    val angleDegrees: Float?
        get() {
            val a = pointA ?: return null
            val b = pointB ?: return null
            val va = a - center
            val vb = b - center
            val cross = va.x * vb.y - va.y * vb.x
            val dot = va.x * vb.x + va.y * vb.y
            if (abs(cross) < 1e-4f && abs(dot) < 1e-4f) return null
            return abs(Math.toDegrees(atan2(cross, dot).toDouble())).toFloat()
        }

    fun ensureInitialized() {
        if (pointA != null || canvasSize == IntSize.Zero) return
        val arm = min(canvasSize.width, canvasSize.height) * InitialArmRatio
        pointA = center + Offset(0f, -arm)
        pointB = center + Offset(arm, 0f)
    }
}

@Composable
fun rememberAngleMeasureState(): AngleMeasureState = remember { AngleMeasureState() }

@Composable
fun AngleMeasureBackground(state: AngleMeasureState) {
    val container = MaterialTheme.colorScheme.primaryContainer
    val onContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val surface = MaterialTheme.colorScheme.surface

    val density = LocalDensity.current
    val handleRadiusPx = with(density) { HandleRadius.toPx() }
    val touchRadiusPx = with(density) { TouchRadius.toPx() }
    val minArmPx = with(density) { MinArmLength.toPx() }
    val arcRadiusPx = with(density) { ArcRadius.toPx() }
    val lineWidthPx = with(density) { 3.dp.toPx() }

    // 呼吸感:手柄外圈缓慢扩散的脉冲环(拖动时暂停)
    val infiniteTransition = rememberInfiniteTransition(label = "angleHandleBreathing")
    val breath by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = BreathDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "breath"
    )

    // 0 = pointA,1 = pointB,null = 未命中手柄
    var activeHandle by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                state.canvasSize = size
                state.ensureInitialized()
            }
    ) {
        val a = state.pointA ?: return@Box
        val b = state.pointB ?: return@Box

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(touchRadiusPx, minArmPx) {
                    detectDragGestures(
                        onDragStart = { start ->
                            val pa = state.pointA
                            val pb = state.pointB
                            val distA = pa?.let { (start - it).getDistance() } ?: Float.MAX_VALUE
                            val distB = pb?.let { (start - it).getDistance() } ?: Float.MAX_VALUE
                            activeHandle = when {
                                distA <= touchRadiusPx && distA <= distB -> 0
                                distB <= touchRadiusPx -> 1
                                else -> null
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val handle = activeHandle ?: return@detectDragGestures
                            val c = Offset(size.width / 2f, size.height / 2f)
                            val current = (if (handle == 0) state.pointA else state.pointB)
                                ?: return@detectDragGestures
                            var target = current + dragAmount
                            // 保持最小臂长,防止拖进中心点导致角度失效
                            var arm = target - c
                            val armLen = arm.getDistance()
                            if (armLen < minArmPx) {
                                arm = if (armLen < 0.5f) {
                                    Offset(0f, -minArmPx)
                                } else {
                                    arm / armLen * minArmPx
                                }
                                target = c + arm
                            }
                            target = Offset(
                                x = target.x.coerceIn(0f, size.width.toFloat()),
                                y = target.y.coerceIn(0f, size.height.toFloat())
                            )
                            if (handle == 0) state.pointA = target else state.pointB = target
                        },
                        onDragEnd = { activeHandle = null },
                        onDragCancel = { activeHandle = null }
                    )
                }
        ) {
            val c = center

            // 夹角扇形与圆弧
            val angleA = Math.toDegrees(atan2(a.y - c.y, a.x - c.x).toDouble()).toFloat()
            val angleB = Math.toDegrees(atan2(b.y - c.y, b.x - c.x).toDouble()).toFloat()
            val sweepRaw = (angleB - angleA) % 360f
            val sweep = when {
                sweepRaw > 180f -> sweepRaw - 360f
                sweepRaw < -180f -> sweepRaw + 360f
                else -> sweepRaw
            }
            val arcRect = Rect(center = c, radius = arcRadiusPx)
            val sector = Path().apply {
                moveTo(c.x, c.y)
                arcTo(arcRect, angleA, sweep, false)
                close()
            }
            drawPath(path = sector, color = container.copy(alpha = 0.35f))
            drawArc(
                color = container,
                startAngle = angleA,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = arcRect.topLeft,
                size = arcRect.size,
                style = Stroke(width = lineWidthPx * 0.7f, cap = StrokeCap.Round)
            )

            // 中心出发的两条边
            drawLine(
                color = container,
                start = c,
                end = a,
                strokeWidth = lineWidthPx,
                cap = StrokeCap.Round
            )
            drawLine(
                color = container,
                start = c,
                end = b,
                strokeWidth = lineWidthPx,
                cap = StrokeCap.Round
            )

            // 可拖动手柄:白底描边保证相机画面上可见
            listOf(a, b).forEachIndexed { index, p ->
                val isActive = activeHandle == index
                if (!isActive) {
                    drawCircle(
                        color = container.copy(alpha = (1f - breath) * 0.35f),
                        radius = handleRadiusPx * (1f + breath * 1.1f),
                        center = p
                    )
                }
                val radius = handleRadiusPx * if (isActive) 1.15f else 1f
                drawCircle(color = surface, radius = radius + lineWidthPx, center = p)
                drawCircle(color = container, radius = radius, center = p)
                drawCircle(color = onContainer, radius = radius * 0.35f, center = p)
            }

            // 中心圆点
            drawCircle(color = surface, radius = handleRadiusPx * 0.55f + lineWidthPx, center = c)
            drawCircle(color = container, radius = handleRadiusPx * 0.55f, center = c)
        }
    }
}

@Composable
fun AngleMeasureContent(angleDegrees: Float?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        GlassCard(
            modifier = Modifier.padding(top = 48.dp),
            colors = tintedGlassCardColors(MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 40.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = angleDegrees?.let { "%.1f°".format(it) } ?: "--",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(CoreR.string.angle_measure_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}
