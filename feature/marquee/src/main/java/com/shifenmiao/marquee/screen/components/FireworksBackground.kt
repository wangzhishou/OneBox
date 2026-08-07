package com.shifenmiao.marquee.screen.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 烟花粒子
 */
private data class FireworkParticle(
    val startX: Float,
    val startY: Float,
    val angle: Float,      // 弧度
    val speed: Float,
    val color: Color,
    val size: Float,
    val gravity: Float = 0.12f,
    var progress: Float = 0f
) {
    fun getPosition(progress: Float): Offset {
        val distance = speed * progress
        val gravityEffect = gravity * progress * progress * 600
        return Offset(
            x = startX + cos(angle) * distance,
            y = startY + sin(angle) * distance + gravityEffect
        )
    }

    fun getAlpha(progress: Float): Float {
        // 缓慢淡出
        return (1f - progress * progress).coerceIn(0f, 1f)
    }
}

/**
 * 烟花
 */
private data class Firework(
    val x: Float,
    val y: Float,
    val particles: List<FireworkParticle>,
    val isForeground: Boolean = false,  // 是否为前景烟花
    val progress: Animatable<Float, *> = Animatable(0f)
)

/**
 * 预定义的鲜艳烟花颜色
 */
private val vibrantFireworkColors = listOf(
    // 红色系
    Color(0xFFFF1744),  // 亮红
    Color(0xFFFF5252),  // 红
    Color(0xFFFF4081),  // 粉红
    // 橙黄色系
    Color(0xFFFF9100),  // 橙
    Color(0xFFFFEA00),  // 黄
    Color(0xFFFFD600),  // 金黄
    // 绿色系
    Color(0xFF00E676),  // 亮绿
    Color(0xFF76FF03),  // 黄绿
    Color(0xFF00BFA5),  // 青绿
    // 蓝色系
    Color(0xFF00B0FF),  // 亮蓝
    Color(0xFF2979FF),  // 蓝
    Color(0xFF00E5FF),  // 青
    // 紫色系
    Color(0xFFD500F9),  // 紫
    Color(0xFFE040FB),  // 淡紫
    Color(0xFF7C4DFF),  // 蓝紫
    // 白色系（适合深色背景）
    Color(0xFFFFFFFF),  // 白
    Color(0xFFF5F5F5),  // 亮灰白
)

/**
 * 计算两个颜色的亮度差异
 */
private fun colorLuminanceDiff(c1: Color, c2: Color): Float {
    val l1 = 0.299f * c1.red + 0.587f * c1.green + 0.114f * c1.blue
    val l2 = 0.299f * c2.red + 0.587f * c2.green + 0.114f * c2.blue
    return kotlin.math.abs(l1 - l2)
}

/**
 * 计算两个颜色的色相差异
 */
private fun colorHueDiff(c1: Color, c2: Color): Float {
    val dr = kotlin.math.abs(c1.red - c2.red)
    val dg = kotlin.math.abs(c1.green - c2.green)
    val db = kotlin.math.abs(c1.blue - c2.blue)
    return dr + dg + db
}

/**
 * 基于背景色筛选合适的烟花颜色
 * 确保颜色与背景有足够对比度，同时保持五彩缤纷
 */
private fun generateFireworkColors(backgroundColor: Color): List<Color> {
    // 计算背景亮度
    val bgLuminance = 0.299f * backgroundColor.red + 0.587f * backgroundColor.green + 0.114f * backgroundColor.blue
    val isDarkBackground = bgLuminance < 0.5f

    // 过滤出与背景色有足够对比的颜色
    val suitableColors = vibrantFireworkColors.filter { color ->
        val lumDiff = colorLuminanceDiff(color, backgroundColor)
        val hueDiff = colorHueDiff(color, backgroundColor)

        // 亮度差异要足够大，或者色相差异足够大
        lumDiff > 0.25f || hueDiff > 0.8f
    }

    // 如果过滤后颜色太少，根据背景亮度选择预设组
    return if (suitableColors.size >= 6) {
        suitableColors
    } else if (isDarkBackground) {
        // 深色背景用亮色
        listOf(
            Color(0xFFFF1744),  // 红
            Color(0xFFFFEA00),  // 黄
            Color(0xFF00E676),  // 绿
            Color(0xFF00B0FF),  // 蓝
            Color(0xFFD500F9),  // 紫
            Color(0xFFFF9100),  // 橙
            Color(0xFFFFFFFF),  // 白
            Color(0xFF00E5FF),  // 青
        )
    } else {
        // 浅色背景用深色鲜艳色
        listOf(
            Color(0xFFD50000),  // 深红
            Color(0xFFFF6D00),  // 深橙
            Color(0xFF00C853),  // 深绿
            Color(0xFF2962FF),  // 深蓝
            Color(0xFFAA00FF),  // 深紫
            Color(0xFFDD2C00),  // 朱红
            Color(0xFF00BFA5),  // 深青
            Color(0xFFC51162),  // 深粉
        )
    }
}

/**
 * 创建烟花粒子
 */
private fun createFireworkParticles(
    x: Float,
    y: Float,
    colors: List<Color>,
    isForeground: Boolean = false
): List<FireworkParticle> {
    val color = colors.random()
    // 前景烟花粒子更多更大
    val particleCount = if (isForeground) Random.nextInt(50, 80) else Random.nextInt(40, 60)
    val baseSpeed = if (isForeground) 350f else 250f
    val speedRange = if (isForeground) 200f else 150f
    val baseSize = if (isForeground) 6f else 4f
    val sizeRange = if (isForeground) 6f else 4f

    return List(particleCount) {
        val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
        val speed = Random.nextFloat() * speedRange + baseSpeed
        val size = Random.nextFloat() * sizeRange + baseSize

        FireworkParticle(
            startX = x,
            startY = y,
            angle = angle,
            speed = speed,
            color = color.copy(
                red = (color.red + Random.nextFloat() * 0.15f - 0.075f).coerceIn(0f, 1f),
                green = (color.green + Random.nextFloat() * 0.15f - 0.075f).coerceIn(0f, 1f),
                blue = (color.blue + Random.nextFloat() * 0.15f - 0.075f).coerceIn(0f, 1f)
            ),
            size = size,
            gravity = if (isForeground) 0.08f else 0.12f
        )
    }
}

/**
 * 烟花背景效果
 * @param backgroundColor 背景色，用于生成互补的烟花颜色
 */
@Composable
fun FireworksBackground(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Black
) {
    val fireworks = remember { mutableStateListOf<Firework>() }
    var canvasSize by remember { mutableStateOf(Offset.Zero) }

    // 基于背景色生成烟花颜色
    val fireworkColors = remember(backgroundColor) {
        generateFireworkColors(backgroundColor)
    }

    // 定期生成背景烟花
    LaunchedEffect(fireworkColors) {
        while (true) {
            delay(Random.nextLong(400, 900))
            if (canvasSize != Offset.Zero) {
                val x = Random.nextFloat() * canvasSize.x
                val y = Random.nextFloat() * canvasSize.y * 0.5f + canvasSize.y * 0.15f

                val particles = createFireworkParticles(x, y, fireworkColors, isForeground = false)
                val firework = Firework(x, y, particles, isForeground = false)
                fireworks.add(firework)

                // 启动动画
                launch {
                    firework.progress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = Random.nextInt(2000, 3000),
                            easing = LinearEasing
                        )
                    )
                    fireworks.remove(firework)
                }
            }
        }
    }

    // 定期生成前景烟花（更大更亮）
    LaunchedEffect(fireworkColors) {
        delay(500) // 错开启动时间
        while (true) {
            delay(Random.nextLong(800, 1500))
            if (canvasSize != Offset.Zero) {
                val x = Random.nextFloat() * canvasSize.x * 0.8f + canvasSize.x * 0.1f
                val y = Random.nextFloat() * canvasSize.y * 0.4f + canvasSize.y * 0.2f

                val particles = createFireworkParticles(x, y, fireworkColors, isForeground = true)
                val firework = Firework(x, y, particles, isForeground = true)
                fireworks.add(firework)

                // 启动动画
                launch {
                    firework.progress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = Random.nextInt(2500, 3500),
                            easing = LinearEasing
                        )
                    )
                    fireworks.remove(firework)
                }
            }
        }
    }

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        canvasSize = Offset(size.width, size.height)

        // 先绘制背景烟花
        fireworks.filter { !it.isForeground }.forEach { firework ->
            val progress = firework.progress.value

            firework.particles.forEach { particle ->
                val position = particle.getPosition(progress)
                val alpha = particle.getAlpha(progress) * 0.8f

                if (alpha > 0.01f &&
                    position.x >= -50 && position.x <= size.width + 50 &&
                    position.y >= -50 && position.y <= size.height + 50
                ) {
                    drawCircle(
                        color = particle.color.copy(alpha = alpha),
                        radius = particle.size * (1f - progress * 0.3f),
                        center = position
                    )
                }
            }
        }

        // 再绘制前景烟花
        fireworks.filter { it.isForeground }.forEach { firework ->
            val progress = firework.progress.value

            firework.particles.forEach { particle ->
                val position = particle.getPosition(progress)
                val alpha = particle.getAlpha(progress)

                if (alpha > 0.01f &&
                    position.x >= -100 && position.x <= size.width + 100 &&
                    position.y >= -100 && position.y <= size.height + 100
                ) {
                    drawCircle(
                        color = particle.color.copy(alpha = alpha),
                        radius = particle.size * (1f - progress * 0.3f),
                        center = position
                    )
                }
            }
        }
    }
}