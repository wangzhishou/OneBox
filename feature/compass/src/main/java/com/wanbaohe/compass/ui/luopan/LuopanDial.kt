package com.wanbaohe.compass.ui.luopan

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onSizeChanged
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

// ─── 古典配色（按原型稿固定，不随深浅主题变化）──────────────────────────────────
private val LuopanBackground = Color(0xFFF7F3E9)   // 米白盘面
private val LuopanGold = Color(0xFFA68A5B)         // 金棕：刻度/格线/边框
private val LuopanBrown = Color(0xFF7A5C33)        // 深棕：圈层文字
private val LuopanRed = Color(0xFFC62828)          // 朱红：0°/准线/指针/南
private val LuopanNeedleSouth = Color(0xFF4A4238)  // 指针南半（深灰褐）

// ─── 径向布局（半径分数，由外到内）──────────────────────────────────────────────
private const val RIM = 0.985f          // 外边框
private const val TICK_OUTER = 0.962f   // 刻度外端
private const val TICK_1 = 0.946f       // 1° 刻度内端
private const val TICK_5 = 0.936f       // 5° 刻度内端
private const val TICK_10 = 0.922f      // 10° 刻度内端
private const val DEGREE_TEXT = 0.878f  // 周天度数（每 10°）
private const val MOUNTAIN_OUT = 0.842f // 二十四山环外边界
private const val MOUNTAIN_TEXT = 0.806f
private const val MOUNTAIN_IN = 0.770f  // 二十四山环内边界
private const val MANSION_TEXT = 0.736f // 二十八宿环（边界 MOUNTAIN_IN ~ 0.702）
private const val MANSION_IN = 0.702f
private const val STAR_TEXT = 0.668f    // 九星环（边界 MANSION_IN ~ 0.634）
private const val STAR_IN = 0.634f
private const val TRIGRAM_BAR = 0.590f  // 卦象中心
private const val TRIGRAM_NAME = 0.528f // 卦名
private const val TRIGRAM_IN = 0.496f   // 八卦环内边界
private const val TIANCHI = 0.440f      // 天池外圆
private const val NEEDLE_LEN = 0.300f   // 天池指针半长
private const val POLE_TEXT = 0.370f    // 南/北 字样距中心

/**
 * 罗经盘表盘（纯 Canvas 实现，无 View 依赖）
 *
 * 设计语言：仿古罗经（米白盘面 + 金棕刻度 + 朱红点缀），物理约定与 [com.wanbaohe.compass.ui.CompassDial]
 * 一致 —— 全部圈层随 [-heading] 旋转，12 点红色准线即当前朝向读数位置，天池指针固定指向准线。
 *
 * 圈层（外→内）：1° 刻度环 → 周天度数 → 二十四山 → 二十八宿 → 九星 → 八卦 → 天池。
 * 圈层数据见 [LuopanRings.kt]，新增圈层只需在那里加数据并在此加一段绘制。
 *
 * 性能要点：与 CompassDial 相同 —— [heading] 绘制阶段读取只触发重绘；
 * 全部圈层文字预排版缓存，指针 Path 按尺寸缓存，不做逐帧分配。
 *
 * @param heading 高频平滑方位角 State（[0,360)，0=正北，顺时针增大）
 */
@Composable
fun LuopanDial(
    heading: State<Float>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // ── 圈层文字预排版：样式固定，仅随密度/字体环境变化重算 ──────────────────
    val degreeLayouts = remember(textMeasurer) {
        (0 until 36).map { i ->
            val isZero = i == 0
            textMeasurer.measure(
                text = "${i * 10}",
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = if (isZero) FontWeight.Bold else FontWeight.Normal,
                    color = if (isZero) LuopanRed else LuopanBrown
                )
            )
        }
    }
    val mountainLayouts = remember(textMeasurer) {
        MOUNTAINS.map { name ->
            textMeasurer.measure(
                text = name,
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = LuopanBrown)
            )
        }
    }
    val mansionLayouts = remember(textMeasurer) {
        MANSIONS.map { name ->
            textMeasurer.measure(
                text = name,
                style = TextStyle(fontSize = 10.sp, color = LuopanBrown)
            )
        }
    }
    val starLayouts = remember(textMeasurer) {
        TRIGRAMS.map { trigram ->
            textMeasurer.measure(
                text = trigram.star,
                style = TextStyle(fontSize = 9.sp, color = LuopanGold)
            )
        }
    }
    val trigramNameLayouts = remember(textMeasurer) {
        TRIGRAMS.map { trigram ->
            textMeasurer.measure(
                text = trigram.name,
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = LuopanBrown)
            )
        }
    }
    val southLayout = remember(textMeasurer) {
        textMeasurer.measure(
            text = "南",
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LuopanRed)
        )
    }
    val northLayout = remember(textMeasurer) {
        textMeasurer.measure(
            text = "北",
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LuopanBrown)
        )
    }

    // 天池指针 Path 缓存：仅在表盘像素尺寸变化时重建
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val needleNorth = remember(canvasSize) { buildNeedlePath(canvasSize, pointingUp = true) }
    val needleSouth = remember(canvasSize) { buildNeedlePath(canvasSize, pointingUp = false) }

    Canvas(modifier = modifier.onSizeChanged { canvasSize = it }) {
        // 绘制阶段读取：角度变化仅触发重绘
        val degrees = heading.value
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = minOf(cx, cy)

        // ── 0. 盘面 ──────────────────────────────────────────────────────
        drawCircle(color = LuopanBackground, radius = radius * RIM, center = Offset(cx, cy))
        drawCircle(
            color = LuopanGold, radius = radius * RIM, center = Offset(cx, cy),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // ── 1~5. 圈层内容：整体随 -heading 逆向旋转，顶部即当前朝向 ────────
        rotate(degrees = -degrees, pivot = Offset(cx, cy)) {
            drawTickRing(cx, cy, radius)
            drawDegreeNumbers(cx, cy, radius, degreeLayouts)
            drawMountainRing(cx, cy, radius, mountainLayouts)
            drawMansionRing(cx, cy, radius, mansionLayouts)
            drawStarRing(cx, cy, radius, starLayouts)
            drawTrigramRing(cx, cy, radius, trigramNameLayouts)
            // 分环细线
            listOf(MOUNTAIN_OUT, MOUNTAIN_IN, MANSION_IN, STAR_IN, TRIGRAM_IN).forEach { r ->
                drawCircle(
                    color = LuopanGold.copy(alpha = 0.8f),
                    radius = radius * r, center = Offset(cx, cy),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        // ── 6. 天池（固定不转）：边界 + 指针 + 南/北 + 中心点 ─────────────
        drawCircle(
            color = LuopanGold, radius = radius * TIANCHI, center = Offset(cx, cy),
            style = Stroke(width = 1.dp.toPx())
        )
        drawPath(needleNorth, LuopanRed)
        drawPath(needleSouth, LuopanNeedleSouth)
        drawRadialText(southLayout, cx, cy, radius * POLE_TEXT)
        drawRadialText(northLayout, cx, cy, -radius * POLE_TEXT)
        drawCircle(color = LuopanBrown, radius = 4.dp.toPx(), center = Offset(cx, cy))

        // ── 7. 12 点读数准线（固定，朱红） ────────────────────────────────
        drawLine(
            color = LuopanRed,
            start = Offset(cx, cy - radius * RIM),
            end = Offset(cx, cy - radius * TICK_10),
            strokeWidth = 2.5.dp.toPx()
        )
    }
}

// ─── 私有绘制扩展 ─────────────────────────────────────────────────────────────

/** 在"正上方"位置画文字（offsetR 为距中心距离，负值在下方），文字保持正立 */
private fun DrawScope.drawRadialText(
    layout: TextLayoutResult, cx: Float, cy: Float, offsetR: Float
) {
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(cx - layout.size.width / 2f, cy - offsetR - layout.size.height / 2f)
    )
}

/** 刻度环：360×1° 细刻度，5°/10° 逐级加粗加长 */
private fun DrawScope.drawTickRing(cx: Float, cy: Float, radius: Float) {
    for (deg in 0 until 360) {
        val innerR = when {
            deg % 10 == 0 -> TICK_10
            deg % 5 == 0 -> TICK_5
            else -> TICK_1
        }
        val strokeW = when {
            deg % 10 == 0 -> 1.6f
            deg % 5 == 0 -> 1.1f
            else -> 0.7f
        }
        val alpha = when {
            deg % 10 == 0 -> 0.95f
            deg % 5 == 0 -> 0.7f
            else -> 0.45f
        }
        val angleRad = Math.toRadians(deg.toDouble())
        drawLine(
            color = LuopanGold.copy(alpha = alpha),
            start = Offset(
                cx + (radius * TICK_OUTER * sin(angleRad)).toFloat(),
                cy - (radius * TICK_OUTER * cos(angleRad)).toFloat()
            ),
            end = Offset(
                cx + (radius * innerR * sin(angleRad)).toFloat(),
                cy - (radius * innerR * cos(angleRad)).toFloat()
            ),
            strokeWidth = strokeW.dp.toPx()
        )
    }
}

/** 周天度数：每 10° 一个数字，0 朱红加粗；文字径向（顶端朝外） */
private fun DrawScope.drawDegreeNumbers(
    cx: Float, cy: Float, radius: Float, layouts: List<TextLayoutResult>
) {
    layouts.forEachIndexed { i, layout ->
        rotate(degrees = i * 10f, pivot = Offset(cx, cy)) {
            drawRadialText(layout, cx, cy, radius * DEGREE_TEXT)
        }
    }
}

/** 二十四山环：每山 15° 一格，带格线，山名径向排列（子居 0°±7.5°） */
private fun DrawScope.drawMountainRing(
    cx: Float, cy: Float, radius: Float, layouts: List<TextLayoutResult>
) {
    // 格线：每山边界在 i*15 - 7.5°
    for (i in MOUNTAINS.indices) {
        val angleRad = Math.toRadians(i * 15.0 - 7.5)
        drawLine(
            color = LuopanGold.copy(alpha = 0.7f),
            start = Offset(
                cx + (radius * MOUNTAIN_OUT * sin(angleRad)).toFloat(),
                cy - (radius * MOUNTAIN_OUT * cos(angleRad)).toFloat()
            ),
            end = Offset(
                cx + (radius * MOUNTAIN_IN * sin(angleRad)).toFloat(),
                cy - (radius * MOUNTAIN_IN * cos(angleRad)).toFloat()
            ),
            strokeWidth = 1.dp.toPx()
        )
    }
    layouts.forEachIndexed { i, layout ->
        rotate(degrees = i * 15f, pivot = Offset(cx, cy)) {
            drawRadialText(layout, cx, cy, radius * MOUNTAIN_TEXT)
        }
    }
}

/** 二十八宿环：等分简化，虚宿锚定正北，逆时针推进（见 [mansionCenterAngle]） */
private fun DrawScope.drawMansionRing(
    cx: Float, cy: Float, radius: Float, layouts: List<TextLayoutResult>
) {
    layouts.forEachIndexed { k, layout ->
        rotate(degrees = mansionCenterAngle(k), pivot = Offset(cx, cy)) {
            drawRadialText(layout, cx, cy, radius * MANSION_TEXT)
        }
    }
}

/** 九星环：后天八卦配洛书九星，与八卦同宫（每宫 45°） */
private fun DrawScope.drawStarRing(
    cx: Float, cy: Float, radius: Float, layouts: List<TextLayoutResult>
) {
    layouts.forEachIndexed { i, layout ->
        rotate(degrees = i * 45f, pivot = Offset(cx, cy)) {
            drawRadialText(layout, cx, cy, radius * STAR_TEXT)
        }
    }
}

/**
 * 八卦环：每宫 45°，外卦象（Canvas 画三爻，阳爻实线/阴爻中断线，不依赖字体）+ 内卦名。
 * 三爻自外（上爻）向内（初爻）排列，随宫旋转保持径向。
 */
private fun DrawScope.drawTrigramRing(
    cx: Float, cy: Float, radius: Float, nameLayouts: List<TextLayoutResult>
) {
    val barLength = radius * 0.13f
    val barThick = 2.dp.toPx()
    val barStep = radius * 0.028f
    TRIGRAMS.forEachIndexed { i, trigram ->
        rotate(degrees = i * 45f, pivot = Offset(cx, cy)) {
            trigram.lines.forEachIndexed { lineIdx, solid ->
                // 上爻（lines[0]）在最外，初爻在最内
                val barCenterY = cy - radius * TRIGRAM_BAR + (lineIdx - 1) * barStep
                if (solid) {
                    drawRect(
                        color = LuopanBrown,
                        topLeft = Offset(cx - barLength / 2f, barCenterY - barThick / 2f),
                        size = Size(barLength, barThick)
                    )
                } else {
                    val segLength = barLength * 0.38f
                    drawRect(
                        color = LuopanBrown,
                        topLeft = Offset(cx - barLength / 2f, barCenterY - barThick / 2f),
                        size = Size(segLength, barThick)
                    )
                    drawRect(
                        color = LuopanBrown,
                        topLeft = Offset(cx + barLength / 2f - segLength, barCenterY - barThick / 2f),
                        size = Size(segLength, barThick)
                    )
                }
            }
            drawRadialText(nameLayouts[i], cx, cy, radius * TRIGRAM_NAME)
        }
    }
}

/**
 * 构建天池菱形指针的一半（pointingUp=true 为北向红针）。
 * 坐标依赖表盘像素尺寸，由调用方在尺寸变化时重建。
 */
private fun buildNeedlePath(size: IntSize, pointingUp: Boolean): Path {
    val path = Path()
    if (size.width <= 0 || size.height <= 0) return path

    val cx = size.width / 2f
    val cy = size.height / 2f
    val radius = minOf(cx, cy)
    val length = radius * NEEDLE_LEN
    val halfWidth = radius * 0.045f
    val direction = if (pointingUp) -1f else 1f

    path.moveTo(cx, cy + direction * length)          // 尖端
    path.lineTo(cx - halfWidth, cy)                    // 左腰
    path.lineTo(cx, cy + direction * length * 0.1f)   // 中心缺口
    path.lineTo(cx + halfWidth, cy)                    // 右腰
    path.close()
    return path
}
