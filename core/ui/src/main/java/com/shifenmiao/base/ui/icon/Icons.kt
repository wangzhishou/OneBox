package com.shifenmiao.base.ui.icon

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 形状类型枚举
 */
private enum class IconShape {
    CIRCLE,           // 圆形
    ROUNDED_RECT,     // 圆角矩形
    HEXAGON,          // 六边形
    DIAMOND,          // 菱形
    SQUIRCLE,         // 超椭圆（iOS风格）
    OCTAGON           // 八边形
}

/**
 * 缓存的图标样式参数，基于name确定性生成
 */
private data class IconStyleParams(
    val shape: IconShape
)

/**
 * 基于字符串生成确定性样式参数
 */
private fun generateStyleParams(name: String): IconStyleParams {
    val hash = abs(name.hashCode())
    val shapes = IconShape.entries
    return IconStyleParams(
        shape = shapes[hash % shapes.size]
    )
}


/**
 * 缓存字符的墨水边界测量结果，避免重复计算
 */
private data class CharMeasureCache(
    val inkWidth: Float,
    val inkHeight: Float,
    val inkLeft: Float,
    val inkTop: Float
)

private val measureCache = mutableMapOf<String, CharMeasureCache>()
private val styleCache = mutableMapOf<String, IconStyleParams>()

private val cachePaint = android.graphics.Paint().apply {
    isAntiAlias = true
    textSize = 100f // 固定参考尺寸用于计算比例
    typeface = android.graphics.Typeface.create(
        android.graphics.Typeface.DEFAULT,
        android.graphics.Typeface.BOLD
    )
}
private val cacheBounds = android.graphics.Rect()

private fun getMeasureCache(char: String): CharMeasureCache {
    return measureCache.getOrPut(char) {
        synchronized(cachePaint) {
            cachePaint.getTextBounds(char, 0, char.length, cacheBounds)
            CharMeasureCache(
                inkWidth = cacheBounds.width().toFloat(),
                inkHeight = cacheBounds.height().toFloat(),
                inkLeft = cacheBounds.left.toFloat(),
                inkTop = cacheBounds.top.toFloat()
            )
        }
    }
}

private fun getStyleParams(name: String): IconStyleParams {
    return styleCache.getOrPut(name) { generateStyleParams(name) }
}

/**
 * 绘制形状描边（线条）
 */
private fun DrawScope.drawShapeStroke(
    shape: IconShape,
    color: Color,
    centerX: Float,
    centerY: Float,
    radius: Float,
    strokeWidth: Float
) {
    val stroke = Stroke(width = strokeWidth)
    when (shape) {
        IconShape.CIRCLE -> {
            drawCircle(
                color = color,
                radius = radius - strokeWidth / 2,
                center = Offset(centerX, centerY),
                style = stroke
            )
        }

        IconShape.ROUNDED_RECT -> {
            val rectSize = radius * 1.8f - strokeWidth
            val cornerRadius = radius * 0.2f
            drawRoundRect(
                color = color,
                topLeft = Offset(centerX - rectSize / 2, centerY - rectSize / 2),
                size = Size(rectSize, rectSize),
                cornerRadius = CornerRadius(cornerRadius),
                style = stroke
            )
        }

        IconShape.SQUIRCLE -> {
            val rectSize = radius * 1.8f - strokeWidth
            val cornerRadius = radius * 0.4f
            drawRoundRect(
                color = color,
                topLeft = Offset(centerX - rectSize / 2, centerY - rectSize / 2),
                size = Size(rectSize, rectSize),
                cornerRadius = CornerRadius(cornerRadius),
                style = stroke
            )
        }

        IconShape.HEXAGON -> {
            val r = radius - strokeWidth / 2
            val path = Path().apply {
                for (i in 0 until 6) {
                    val angle = Math.toRadians((60.0 * i - 30)).toFloat()
                    val x = centerX + r * cos(angle)
                    val y = centerY + r * sin(angle)
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(path, color, style = stroke)
        }

        IconShape.DIAMOND -> {
            val r = radius - strokeWidth / 2
            val path = Path().apply {
                moveTo(centerX, centerY - r)
                lineTo(centerX + r, centerY)
                lineTo(centerX, centerY + r)
                lineTo(centerX - r, centerY)
                close()
            }
            drawPath(path, color, style = stroke)
        }

        IconShape.OCTAGON -> {
            val r = radius - strokeWidth / 2
            val path = Path().apply {
                for (i in 0 until 8) {
                    val angle = Math.toRadians((45.0 * i - 22.5)).toFloat()
                    val x = centerX + r * cos(angle)
                    val y = centerY + r * sin(angle)
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(path, color, style = stroke)
        }
    }
}


@Composable
fun LetterIcon(
    name: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    showOutline: Boolean = true
) {
    val nameStr = name ?: "?"
    val firstLetter = remember(nameStr) {
        nameStr.firstOrNull()?.let { char ->
            // 汉字不需要 uppercase
            if (char.code in 0x4E00..0x9FFF || char.code in 0x3400..0x4DBF) {
                char.toString()
            } else {
                char.uppercase()
            }
        } ?: "?"
    }

    val cache = remember(firstLetter) { getMeasureCache(firstLetter) }
    val styleParams = remember(nameStr) { getStyleParams(nameStr) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val drawColor = if (tint != Color.Unspecified) tint else primaryColor

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        if (canvasWidth <= 0 || canvasHeight <= 0 || cache.inkWidth <= 0 || cache.inkHeight <= 0) {
            return@Canvas
        }

        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2
        val radius = minOf(canvasWidth, canvasHeight) / 2 * 0.95f

        // 统一线条粗细：基于画布大小计算
        val strokeWidth = minOf(canvasWidth, canvasHeight) * 0.06f

        if (showOutline) {
            drawShapeStroke(
                shape = styleParams.shape,
                color = drawColor,
                centerX = centerX,
                centerY = centerY,
                radius = radius,
                strokeWidth = strokeWidth
            )
        }

        // 计算文字区域（根据形状调整比例，确保文字在形状内）
        val textAreaRatio = if (!showOutline) {
            0.75f
        } else {
            when (styleParams.shape) {
                IconShape.DIAMOND -> 0.45f
                IconShape.HEXAGON -> 0.55f
                IconShape.OCTAGON -> 0.55f
                else -> 0.6f
            }
        }

        val refSize = 100f
        val scaleX = canvasWidth / cache.inkWidth
        val scaleY = canvasHeight / cache.inkHeight
        val scale = minOf(scaleX, scaleY) * textAreaRatio
        val fontSize = refSize * scale

        // 缩放后的墨水边界
        val scaledInkWidth = cache.inkWidth * scale
        val scaledInkHeight = cache.inkHeight * scale
        val scaledInkLeft = cache.inkLeft * scale
        val scaledInkTop = cache.inkTop * scale

        val baselineY = (canvasHeight - scaledInkHeight) / 2f - scaledInkTop
        val drawX = (canvasWidth - scaledInkWidth) / 2f - scaledInkLeft

        // 绘制文字（普通字体，正常填充）
        drawContext.canvas.nativeCanvas.drawText(
            firstLetter,
            drawX,
            baselineY,
            android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = fontSize
                color = drawColor.toArgb()
                typeface = android.graphics.Typeface.DEFAULT  // 普通字体
            }
        )
    }
}


fun resolveOutlinedIcon(name: String): ImageVector? {
    return IconRegistry.resolve(name)
}

@Composable
fun IconOutlinedByName(
    name: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    if (name == null) return
    val icon: ImageVector? = remember(name) { IconRegistry.resolve(name) }
    if (icon != null) {
        Icon(icon, "$name icon", modifier, tint)
    } else {
        LetterIcon(name, modifier, tint)
    }
}

@Composable
fun IconFilledByName(
    name: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    if (name == null) return
    val icon: ImageVector? = remember(name) { IconRegistry.resolve(name) }
    if (icon != null) {
        Icon(icon, "$name icon", modifier, tint)
    } else {
        LetterIcon(name, modifier, tint)
    }
}

@Composable
fun BuildCustomIcon(
    iconName: String? = "null",
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    fallbackName: String? = null,
) {
    IconRegistry.resolve(iconName ?: "")?.let {
        Icon(it, contentDescription = "$iconName icon", modifier = modifier, tint = tint)
    } ?: LetterIcon(
        name = fallbackName?.takeIf { it.isNotEmpty() } ?: iconName,
        modifier = modifier,
        tint = tint,
    )
}

/**
 * 通用图标头像组件:玻璃背景容器 + 居中图标。
 *
 * @param iconName 图标名称;为 null/空时容器仍会渲染但不绘制图标。
 * @param size 容器宽高,默认 48dp。
 * @param shape 容器形状,圆形头像传 [androidx.compose.foundation.shape.CircleShape]。
 * @param iconSizeRatio 图标相对容器尺寸的比例。
 */
@Composable
fun IconAvatar(
    iconName: String?,
    fallbackName: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    tint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    glassStyle: GlassStyle = GlassStyle.Regular,
    iconSizeRatio: Float = 0.5f,
) {
    val effectiveName = iconName?.takeIf { it.isNotEmpty() }
        ?: fallbackName?.takeIf { it.isNotEmpty() }
        ?: ""

    Box(
        modifier = modifier
            .size(size)
            .glassBackground(
                style = glassStyle,
                shape = shape,
                color = containerColor,
            ),
        contentAlignment = Alignment.Center
    ) {
        BuildCustomIcon(
            iconName = effectiveName,
            modifier = Modifier.size(size * iconSizeRatio),
            tint = tint,
            fallbackName = fallbackName,
        )
    }
}

@Composable
fun AnimatedCardIcon(
    modifier: Modifier = Modifier,
    visible: Boolean,
    iconName: String,
    tint: Color = Color.Unspecified,
    size: Dp = 40.dp,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        BuildCustomIcon(
            modifier = modifier
                .size(size),
            iconName = iconName,
            tint = tint
        )
    }
}
