package com.t8rin.imagetoolbox.core.ui.widget.other

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.com.color.ColorGenerator
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import com.t8rin.imagetoolbox.core.resources.icons.PlayCircle

/**
 * 转盘选项数据
 */
@Immutable
data class DecisionWheelItem(
    val label: String,
    val color: Color
)

/**
 * 转盘 Canvas 绘制组件
 *
 * 采用扁平风格：不绘制扇区边框与外圈边框。
 *
 * @param items 转盘选项列表
 * @param rotation 当前旋转角度（度）
 * @param selectedIndex 当前选中/高亮的扇区索引
 * @param highlightAlpha 高亮叠加层的透明度
 * @param pulse 选中扇区的脉冲缩放值 [0, 1]
 * @param onSegmentClick 扇区点击回调
 */
@Composable
fun DecisionWheelCanvas(
    items: List<DecisionWheelItem>,
    rotation: Float,
    selectedIndex: Int? = null,
    highlightAlpha: Float = 0f,
    pulse: Float = 0f,
    onSegmentClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.pointerInput(items, rotation) {
            detectTapGestures { tapOffset: Offset ->
                if (items.isEmpty()) return@detectTapGestures

                val center = Offset(size.width / 2f, size.height / 2f)
                val dx = tapOffset.x - center.x
                val dy = tapOffset.y - center.y
                val distance = sqrt(dx * dx + dy * dy)
                val radius = min(size.width, size.height) / 2f

                if (distance > radius) return@detectTapGestures

                var angle =
                    Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                if (angle < 0f) angle += 360f

                val adjusted = (angle + 90f - (rotation % 360f) + 360f) % 360f
                val sectorAngle = 360f / items.size
                val idx = (adjusted / sectorAngle).toInt().coerceIn(0, items.lastIndex)
                onSegmentClick(idx)
            }
        }
    ) {
        if (items.isEmpty()) return@Canvas

        val canvasSize = size.minDimension
        val radius = canvasSize / 2
        val center = Offset(size.width / 2, size.height / 2)

        val sectorAngle = 360f / items.size

        rotate(degrees = rotation, pivot = center) {
            items.forEachIndexed { index, item ->
                val startAngle = index * sectorAngle - 90f

                val backgroundColor = item.color
                val contentColor = ColorGenerator.contentColorFor(backgroundColor)

                val isSelected = selectedIndex == index
                val pulseScale = if (isSelected) 1f + 0.04f * pulse else 1f
                val localRadius = radius * pulseScale

                drawArc(
                    color = backgroundColor,
                    startAngle = startAngle,
                    sweepAngle = sectorAngle,
                    useCenter = true,
                    topLeft = Offset(
                        center.x - localRadius,
                        center.y - localRadius
                    ),
                    size = Size(localRadius * 2, localRadius * 2)
                )

                if (isSelected && highlightAlpha > 0f) {
                    drawArc(
                        color = contentColor.copy(alpha = highlightAlpha),
                        startAngle = startAngle,
                        sweepAngle = sectorAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - localRadius, center.y - localRadius),
                        size = Size(localRadius * 2, localRadius * 2)
                    )
                }

                val textAngle = Math.toRadians((startAngle + sectorAngle / 2).toDouble())
                val textRadius = radius * 0.65f
                val textX = center.x + textRadius * cos(textAngle).toFloat()
                val textY = center.y + textRadius * sin(textAngle).toFloat()

                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = contentColor.toArgb()
                        textSize = 40f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawText(item.label, textX, textY + 15f, paint)
                }
            }
        }
    }
}

/**
 * 转盘顶部指针指示器
 *
 * @param color 指针主体颜色
 * @param markerColor 指针顶部标记颜色
 */
@Composable
fun DecisionWheelPointer(
    color: Color,
    markerColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size(40.dp, 60.dp)
    ) {
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width / 2, size.height)
            lineTo(0f, 0f)
            lineTo(size.width, 0f)
            close()
        }
        drawPath(
            path = path,
            color = color
        )

        val r = size.minDimension * 0.14f
        drawCircle(
            color = markerColor,
            radius = r,
            center = Offset(size.width / 2f, r * 1.4f)
        )
    }
}

/**
 * 转盘中心旋转按钮
 *
 * @param onClick 点击回调
 * @param containerColor 按钮背景色
 * @param contentColor 图标/内容色
 */
@Composable
fun DecisionWheelSpinButton(
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle,
            contentDescription = stringResource(R.string.start),
            tint = contentColor,
            modifier = Modifier.size(40.dp)
        )
    }
}

/**
 * 完整的决策转盘组件，内置旋转动画和结果计算。
 *
 * 适合弹窗或即插即用的场景。如需更精细控制旋转过程，
 * 请使用 [DecisionWheelCanvas] + [DecisionWheelPointer] + [DecisionWheelSpinButton] 组合。
 *
 * @param items 转盘选项列表（至少2项）
 * @param onItemSelected 旋转停止后回调，参数为选中的选项
 * @param modifier Modifier
 * @param onSpinningStateChanged 旋转状态变化回调，true=开始旋转，false=旋转结束
 * @param spinDurationMillis 旋转动画时长（毫秒），默认 4000
 * @param spinSpeedMultiplier 旋转速度倍率，默认 1.0（影响圈数）
 * @param pointerColor 顶部指针颜色，默认由第一个选项颜色自动推导
 * @param centerButtonColor 中心按钮颜色，默认由第一个选项颜色自动推导
 * @param enabled 是否启用交互
 */
@Composable
fun DecisionWheel(
    items: List<DecisionWheelItem>,
    onItemSelected: (DecisionWheelItem) -> Unit,
    modifier: Modifier = Modifier,
    onSpinningStateChanged: ((Boolean) -> Unit)? = null,
    spinDurationMillis: Int = 4000,
    spinSpeedMultiplier: Float = 1f,
    pointerColor: Color? = null,
    centerButtonColor: Color? = null,
    enabled: Boolean = true
) {
    if (items.size < 2) return

    var isAnimating by remember { mutableStateOf(false) }
    var targetRotation by remember { mutableFloatStateOf(0f) }

    val customEasing = CubicBezierEasing(0.33f, 0.0f, 0.2f, 1.0f)
    val rotationAnim = remember { Animatable(0f) }
    val latestIsAnimating by rememberUpdatedState(isAnimating)
    val latestItems by rememberUpdatedState(items)

    LaunchedEffect(targetRotation) {
        val from = rotationAnim.value
        if (from == targetRotation) return@LaunchedEffect

        rotationAnim.animateTo(
            targetValue = targetRotation,
            animationSpec = tween(
                durationMillis = spinDurationMillis,
                easing = customEasing
            )
        )

        if (latestIsAnimating) {
            isAnimating = false
            onSpinningStateChanged?.invoke(false)

            val currentItems = latestItems
            if (currentItems.isNotEmpty()) {
                val sectorAngle = 360f / currentItems.size
                val normalizedRotation = ((360f - (targetRotation % 360f)) % 360f)
                val selectedIndex =
                    (normalizedRotation / sectorAngle).toInt() % currentItems.size
                onItemSelected(currentItems[selectedIndex])
            }
        }
    }

    val rotation = rotationAnim.value

    // 颜色推导
    val firstItemColor = items.firstOrNull()?.color ?: MaterialTheme.colorScheme.primary
    val (derivedPointerColor, derivedCenterColor) = remember(firstItemColor) {
        ColorGenerator.splitComplementaryPair(firstItemColor)
    }
    val actualPointerColor = pointerColor ?: derivedPointerColor
    val actualCenterColor = centerButtonColor ?: derivedCenterColor
    val pointerContentColor = remember(actualPointerColor) {
        ColorGenerator.contentColorFor(actualPointerColor)
    }
    val centerContentColor = remember(actualCenterColor) {
        ColorGenerator.contentColorFor(actualCenterColor)
    }

    Box(
        modifier = modifier
            .fillMaxWidth(0.8f)
            .widthIn(max = 420.dp)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        DecisionWheelCanvas(
            items = items,
            rotation = rotation,
            modifier = Modifier.fillMaxSize()
        )

        DecisionWheelPointer(
            color = actualPointerColor,
            markerColor = pointerContentColor,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-8).dp)
        )

        if (!isAnimating && enabled) {
            DecisionWheelSpinButton(
                onClick = {
                    if (!isAnimating) {
                        isAnimating = true
                        onSpinningStateChanged?.invoke(true)
                        val baseRotations =
                            (Random.nextInt(5, 9) * 360f) * spinSpeedMultiplier
                        val randomAngle = Random.nextFloat() * 360f
                        targetRotation = rotation + baseRotations + randomAngle
                    }
                },
                containerColor = actualCenterColor,
                contentColor = centerContentColor,
                modifier = Modifier.size(80.dp)
            )
        }
    }
}
