package com.wanbaohe.compass.ui

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import com.shifenmiao.core.R as CoreR

/**
 * 指南针表盘（纯 Canvas 实现，无 View 依赖）
 *
 * 设计语言：极致扁平，Material 3 配色，本地化方位标签
 *
 * 布局（由外到内）：
 *   1. 外圆边框（含 72 个刻度线，每 5° 一格）
 *   2. 随表盘反向旋转的 8 方位标签环（北/东北/东/…，北红色高亮）
 *   3. 12 点方向固定的读数准线（红色，指示精确读数位置）
 *   4. 中心固定菱形指针（上红=北，下灰=南）+ 中心实心点
 *
 * 性能要点：
 *   - [heading] 在绘制阶段读取，角度变化只触发 Canvas 重绘，不触发重组
 *   - 方位标签排版结果（TextLayoutResult）在语言/主题变化时才重算
 *   - 指针 Path 仅在表盘像素尺寸变化时重建，不做逐帧分配
 *
 * @param heading    高频平滑方位角 State（[0,360)，0=正北，顺时针增大）
 * @param northColor 北向元素（指针、北标签、读数准线）颜色
 * @param southColor 南向指针颜色
 */
@Composable
fun CompassDial(
    heading: State<Float>,
    modifier: Modifier = Modifier,
    northColor: Color = Color(0xFFE53935),   // Material Red
    southColor: Color = Color(0xFFBDBDBD),   // Material Grey
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface = MaterialTheme.colorScheme.surface
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outlineVariant

    // 8 个本地化方位标签
    val dirN = stringResource(CoreR.string.compass_dir_n)
    val dirNE = stringResource(CoreR.string.compass_dir_ne)
    val dirE = stringResource(CoreR.string.compass_dir_e)
    val dirSE = stringResource(CoreR.string.compass_dir_se)
    val dirS = stringResource(CoreR.string.compass_dir_s)
    val dirSW = stringResource(CoreR.string.compass_dir_sw)
    val dirW = stringResource(CoreR.string.compass_dir_w)
    val dirNW = stringResource(CoreR.string.compass_dir_nw)

    // 标签排版缓存：仅在语言或配色变化时重新 measure，避免逐帧创建文本对象
    val textMeasurer = rememberTextMeasurer()
    val labelLayouts = remember(
        dirN, dirNE, dirE, dirSE, dirS, dirSW, dirW, dirNW,
        northColor, primary, onSurface
    ) {
        listOf(dirN, dirNE, dirE, dirSE, dirS, dirSW, dirW, dirNW)
            .mapIndexed { idx, label ->
                val isNorth = idx == 0
                val isMajor = idx % 2 == 0   // 北/东/南/西
                textMeasurer.measure(
                    text = label,
                    style = TextStyle(
                        fontSize = when {
                            isNorth -> 22.sp
                            isMajor -> 16.sp
                            else -> 12.sp
                        },
                        fontWeight = if (isMajor) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isNorth -> northColor
                            isMajor -> primary
                            else -> onSurface.copy(alpha = 0.6f)
                        }
                    )
                )
            }
    }

    // 指针 Path 缓存：仅在表盘像素尺寸变化时重建
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val needleNorth = remember(canvasSize) { buildNeedlePath(canvasSize, pointingUp = true) }
    val needleSouth = remember(canvasSize) { buildNeedlePath(canvasSize, pointingUp = false) }

    Canvas(modifier = modifier.onSizeChanged { canvasSize = it }) {
        // 绘制阶段读取：角度变化仅触发重绘
        val degrees = heading.value
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = minOf(cx, cy)

        // ── 1. 外圆背景 + 边框 ─────────────────────────────────────────
        drawCircle(color = surface, radius = radius * 0.98f, center = Offset(cx, cy))
        drawCircle(
            color = outline,
            radius = radius * 0.98f,
            center = Offset(cx, cy),
            style = Stroke(width = 2.dp.toPx())
        )

        // ── 2. 表盘内容逆向旋转，视觉上"北"跟随设备朝向 ────────────────
        rotate(degrees = -degrees, pivot = Offset(cx, cy)) {
            drawTicks(cx, cy, radius, onSurface)
            drawCardinalLabels(cx, cy, radius, labelLayouts)
        }

        // ── 3. 12 点读数准线（固定不动，与北向指针同色呼应） ───────────
        drawLine(
            color = northColor,
            start = Offset(cx, cy - radius * 0.98f),
            end = Offset(cx, cy - radius * 0.86f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // ── 4. 中心固定指针（不随表盘旋转）+ 中心圆点 ──────────────────
        drawPath(needleNorth, northColor)
        drawPath(needleSouth, southColor)
        drawCircle(color = onSurface, radius = 6.dp.toPx(), center = Offset(cx, cy))
    }
}

// ─── 私有绘制扩展 ─────────────────────────────────────────────────────────────

/** 绘制刻度环：每 5° 一格，每 45° 为长刻度，每 90° 为最长刻度 */
private fun DrawScope.drawTicks(
    cx: Float, cy: Float, radius: Float,
    color: Color
) {
    for (i in 0 until 72) {
        val angleRad = Math.toRadians(i * 5.0)

        val (outerR, innerR, strokeW) = when {
            i % 18 == 0 -> Triple(0.95f, 0.80f, 2.5f)  // 90° 主刻度
            i % 9 == 0 -> Triple(0.95f, 0.85f, 1.8f)   // 45° 副刻度
            else -> Triple(0.95f, 0.90f, 1.2f)         // 5°  小刻度
        }
        val alpha = when {
            i % 18 == 0 -> 0.90f
            i % 9 == 0 -> 0.65f
            else -> 0.35f
        }

        val startX = cx + (radius * outerR * sin(angleRad)).toFloat()
        val startY = cy - (radius * outerR * cos(angleRad)).toFloat()
        val endX = cx + (radius * innerR * sin(angleRad)).toFloat()
        val endY = cy - (radius * innerR * cos(angleRad)).toFloat()

        drawLine(
            color = color.copy(alpha = alpha),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = strokeW.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/** 绘制 8 方位标签环：使用预排版的 TextLayoutResult，零逐帧排版开销 */
private fun DrawScope.drawCardinalLabels(
    cx: Float, cy: Float, radius: Float,
    layouts: List<TextLayoutResult>
) {
    val labelRadius = radius * 0.70f
    layouts.forEachIndexed { idx, layout ->
        val angleRad = Math.toRadians(idx * 45.0)
        val tx = cx + (labelRadius * sin(angleRad)).toFloat()
        val ty = cy - (labelRadius * cos(angleRad)).toFloat()

        drawText(
            textLayoutResult = layout,
            topLeft = Offset(
                x = tx - layout.size.width / 2f,
                y = ty - layout.size.height / 2f
            )
        )
    }
}

/**
 * 构建中心菱形指针的一半（pointingUp=true 为北向）。
 * 坐标依赖表盘像素尺寸，由调用方在尺寸变化时重建。
 */
private fun buildNeedlePath(size: IntSize, pointingUp: Boolean): Path {
    val path = Path()
    if (size.width <= 0 || size.height <= 0) return path

    val cx = size.width / 2f
    val cy = size.height / 2f
    val radius = minOf(cx, cy)
    val length = radius * 0.45f
    val halfWidth = radius * 0.06f
    val direction = if (pointingUp) -1f else 1f

    path.moveTo(cx, cy + direction * length)          // 尖端
    path.lineTo(cx - halfWidth, cy)                    // 左腰
    path.lineTo(cx, cy + direction * length * 0.1f)   // 中心缺口
    path.lineTo(cx + halfWidth, cy)                    // 右腰
    path.close()
    return path
}
