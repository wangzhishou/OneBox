package com.wanbaohe.compass.ui.luopan

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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

// ─── 径向布局（半径分数，由外到内）──────────────────────────────────────────────
// 每个文字带 = 两条分环线 + 居中文字；字号 = FONT 分数 × 半径，
// 文字上下各留 (带宽 - 字高) / 2 的空白，不与分环线相碰。
private const val RIM = 0.985f              // 外边框
private const val TICK_OUTER = 0.960f       // 刻度外端
private const val TICK_1 = 0.948f           // 1° 刻度内端
private const val TICK_5 = 0.938f           // 5° 刻度内端
private const val TICK_10 = 0.926f          // 10° 刻度内端
private const val TICK_LINE_IN = 0.920f     // 刻度环内界细线

private const val DEGREE_TEXT = 0.886f      // 周天度数文字中心（带 0.920 ~ 0.852）
private const val DEGREE_FONT = 0.038f

private const val MOUNTAIN_LINE_OUT = 0.852f
private const val MOUNTAIN_TEXT = 0.810f    // 二十四山文字中心（带 0.852 ~ 0.768）
private const val MOUNTAIN_FONT = 0.042f
private const val MOUNTAIN_LINE_IN = 0.768f

private const val MANSION_TEXT = 0.734f     // 二十八宿文字中心（带 0.768 ~ 0.700）
private const val MANSION_FONT = 0.036f
private const val MANSION_LINE_IN = 0.700f

private const val STAR_TEXT = 0.668f        // 九星文字中心（带 0.700 ~ 0.636）
private const val STAR_FONT = 0.032f
private const val STAR_LINE_IN = 0.636f

private const val TRIGRAM_BAR = 0.590f      // 卦象中心（八卦带 0.636 ~ 0.500）
private const val TRIGRAM_BAR_STEP = 0.026f // 三爻径向间距
private const val TRIGRAM_NAME = 0.530f     // 卦名文字中心
private const val TRIGRAM_NAME_FONT = 0.038f
private const val TRIGRAM_LINE_IN = 0.500f

private const val TIANCHI = 0.442f          // 天池外圆
private const val NEEDLE_LEN = 0.300f       // 天池指针半长
private const val POLE_TEXT = 0.372f        // 南/北 字样距中心
private const val POLE_FONT = 0.046f

/**
 * 罗经盘表盘（纯 Canvas 实现，无 View 依赖）
 *
 * 设计语言：仿古罗经分层版式，配色跟随 Material 3 主题（深浅色自适应）：
 * 盘面 surfaceContainerLow、刻度 outline、分环线 outlineVariant、
 * 主文字 onSurface、次要文字 onSurfaceVariant、朱红元素（0°/准线/指针/南）用 error。
 * 物理约定与 [com.wanbaohe.compass.ui.CompassDial] 一致 —— 全部圈层随 [-heading] 旋转，
 * 12 点红色准线即当前朝向读数位置；天池磁针画在盘面坐标系中（与圈层同转），
 * 相对真实方位静止：朱红针尖始终指南，南/北字样按真实方位定位、字形保持正立。
 *
 * 圈层（外→内）：1° 刻度环 → 周天度数 → 二十四山 → 二十八宿 → 九星 → 八卦 → 天池。
 * 圈层数据见 LuopanRings.kt，新增圈层只需在那里加数据并在此加一段绘制。
 *
 * 性能要点：与 CompassDial 相同 —— [heading] 绘制阶段读取只触发重绘；
 * 圈层文字按表盘像素尺寸成比例排版（remember 按尺寸/主题缓存），
 * 指针 Path 同样按尺寸缓存，不做逐帧分配。
 *
 * @param heading 高频平滑方位角 State（[0,360)，0=正北，顺时针增大）
 */
@Composable
fun LuopanDial(
    heading: State<Float>,
    modifier: Modifier = Modifier
) {
    // ── 主题配色（深浅色自适应；盘面微透明，透出底层背景） ─────────────────
    val dialBackground = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.7f)
    val tickColor = MaterialTheme.colorScheme.outline
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val accentColor = MaterialTheme.colorScheme.error

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    // 表盘像素半径：所有圈层字号按半径比例推导，任何屏幕宽度下留白比例一致
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val radiusPx = minOf(canvasSize.width, canvasSize.height) / 2f

    // ── 圈层文字预排版：尺寸未就绪时为空，就绪后随尺寸/密度/主题重排 ─────────
    val degreeLayouts = remember(textMeasurer, radiusPx, density, textColor, accentColor) {
        if (radiusPx <= 0f) return@remember emptyList()
        (0 until 36).map { i ->
            val isZero = i == 0
            textMeasurer.measure(
                text = "${i * 10}",
                style = TextStyle(
                    fontSize = with(density) { (radiusPx * DEGREE_FONT).toSp() },
                    fontWeight = if (isZero) FontWeight.Bold else FontWeight.Normal,
                    color = if (isZero) accentColor else textColor
                )
            )
        }
    }
    val mountainLayouts = remember(textMeasurer, radiusPx, density, textColor) {
        if (radiusPx <= 0f) return@remember emptyList()
        MOUNTAINS.map { name ->
            textMeasurer.measure(
                text = name,
                style = TextStyle(
                    fontSize = with(density) { (radiusPx * MOUNTAIN_FONT).toSp() },
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            )
        }
    }
    val mansionLayouts = remember(textMeasurer, radiusPx, density, subTextColor) {
        if (radiusPx <= 0f) return@remember emptyList()
        MANSIONS.map { name ->
            textMeasurer.measure(
                text = name,
                style = TextStyle(
                    fontSize = with(density) { (radiusPx * MANSION_FONT).toSp() },
                    color = subTextColor
                )
            )
        }
    }
    val starLayouts = remember(textMeasurer, radiusPx, density, subTextColor) {
        if (radiusPx <= 0f) return@remember emptyList()
        TRIGRAMS.map { trigram ->
            textMeasurer.measure(
                text = trigram.star,
                style = TextStyle(
                    fontSize = with(density) { (radiusPx * STAR_FONT).toSp() },
                    color = subTextColor
                )
            )
        }
    }
    val trigramNameLayouts = remember(textMeasurer, radiusPx, density, textColor) {
        if (radiusPx <= 0f) return@remember emptyList()
        TRIGRAMS.map { trigram ->
            textMeasurer.measure(
                text = trigram.name,
                style = TextStyle(
                    fontSize = with(density) { (radiusPx * TRIGRAM_NAME_FONT).toSp() },
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            )
        }
    }
    val poleTextStyle = remember(radiusPx, density) {
        TextStyle(
            fontSize = with(density) { (radiusPx * POLE_FONT).toSp() },
            fontWeight = FontWeight.Bold
        )
    }
    val southLayout = remember(textMeasurer, poleTextStyle, accentColor) {
        if (radiusPx <= 0f) return@remember null
        textMeasurer.measure(text = "南", style = poleTextStyle.copy(color = accentColor))
    }
    val northLayout = remember(textMeasurer, poleTextStyle, textColor) {
        if (radiusPx <= 0f) return@remember null
        textMeasurer.measure(text = "北", style = poleTextStyle.copy(color = textColor))
    }

    // 天池磁针 Path 缓存：盘面坐标系（上 = 0° = 北），仅在表盘像素尺寸变化时重建
    val needleNorthEnd = remember(canvasSize) { buildNeedlePath(canvasSize, pointingUp = true) }
    val needleSouthEnd = remember(canvasSize) { buildNeedlePath(canvasSize, pointingUp = false) }

    Canvas(modifier = modifier.onSizeChanged { canvasSize = it }) {
        // 绘制阶段读取：角度变化仅触发重绘
        val degrees = heading.value
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = minOf(cx, cy)

        // ── 0. 盘面 ──────────────────────────────────────────────────────
        drawCircle(color = dialBackground, radius = radius * RIM, center = Offset(cx, cy))
        drawCircle(
            color = tickColor, radius = radius * RIM, center = Offset(cx, cy),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // ── 1~5. 圈层内容：整体随 -heading 逆向旋转，顶部即当前朝向 ────────
        rotate(degrees = -degrees, pivot = Offset(cx, cy)) {
            drawTickRing(cx, cy, radius, tickColor)
            drawDegreeNumbers(cx, cy, radius, degreeLayouts)
            drawMountainRing(cx, cy, radius, mountainLayouts, lineColor)
            drawMansionRing(cx, cy, radius, mansionLayouts)
            drawStarRing(cx, cy, radius, starLayouts)
            drawTrigramRing(cx, cy, radius, trigramNameLayouts, textColor)
            // 天池磁针：画在盘面坐标系中（随 -heading 同转）= 相对真实方位静止，
            // 朱红针尖始终指向真实南（180°），针尾朝北（0°）
            drawPath(needleSouthEnd, accentColor)
            drawPath(needleNorthEnd, subTextColor)
            // 分环细线
            listOf(
                TICK_LINE_IN, MOUNTAIN_LINE_OUT, MOUNTAIN_LINE_IN,
                MANSION_LINE_IN, STAR_LINE_IN, TRIGRAM_LINE_IN
            ).forEach { r ->
                drawCircle(
                    color = lineColor,
                    radius = radius * r, center = Offset(cx, cy),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        // ── 6. 天池：边界 + 中心点（位置与旋转无关） ───────────────────────
        drawCircle(
            color = lineColor, radius = radius * TIANCHI, center = Offset(cx, cy),
            style = Stroke(width = 1.dp.toPx())
        )
        drawCircle(color = textColor, radius = 4.dp.toPx(), center = Offset(cx, cy))

        // ── 6.1 南/北字样：盘面上 α 方位旋转后出现在屏幕角 (α - heading)，
        //    南(180°)/北(0°) 按此定位到真实方位，字形保持正立 ─────────────
        southLayout?.let { drawPoleText(it, cx, cy, radius * POLE_TEXT, 180f - degrees) }
        northLayout?.let { drawPoleText(it, cx, cy, radius * POLE_TEXT, -degrees) }

        // ── 7. 12 点读数准线（固定） ─────────────────────────────────────
        drawLine(
            color = accentColor,
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

/** 在屏幕钟点角 [screenAngle]（自 12 点顺时针，度）的圆周位置上画文字，字形保持正立 */
private fun DrawScope.drawPoleText(
    layout: TextLayoutResult, cx: Float, cy: Float, offsetR: Float, screenAngle: Float
) {
    val angleRad = Math.toRadians(screenAngle.toDouble())
    val tx = cx + (offsetR * sin(angleRad)).toFloat()
    val ty = cy - (offsetR * cos(angleRad)).toFloat()
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(tx - layout.size.width / 2f, ty - layout.size.height / 2f)
    )
}

/** 刻度环：360×1° 细刻度，5°/10° 逐级加粗加长 */
private fun DrawScope.drawTickRing(cx: Float, cy: Float, radius: Float, color: Color) {
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
            color = color.copy(alpha = alpha),
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

/** 周天度数：每 10° 一个数字，0 加粗高亮；文字径向（顶端朝外） */
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
    cx: Float, cy: Float, radius: Float, layouts: List<TextLayoutResult>, gridColor: Color
) {
    // 格线：每山边界在 i*15 - 7.5°
    for (i in MOUNTAINS.indices) {
        val angleRad = Math.toRadians(i * 15.0 - 7.5)
        drawLine(
            color = gridColor,
            start = Offset(
                cx + (radius * MOUNTAIN_LINE_OUT * sin(angleRad)).toFloat(),
                cy - (radius * MOUNTAIN_LINE_OUT * cos(angleRad)).toFloat()
            ),
            end = Offset(
                cx + (radius * MOUNTAIN_LINE_IN * sin(angleRad)).toFloat(),
                cy - (radius * MOUNTAIN_LINE_IN * cos(angleRad)).toFloat()
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
    cx: Float, cy: Float, radius: Float, nameLayouts: List<TextLayoutResult>, barColor: Color
) {
    if (nameLayouts.isEmpty()) return
    val barLength = radius * 0.13f
    val barThick = 2.dp.toPx()
    val barStep = radius * TRIGRAM_BAR_STEP
    TRIGRAMS.forEachIndexed { i, trigram ->
        rotate(degrees = i * 45f, pivot = Offset(cx, cy)) {
            trigram.lines.forEachIndexed { lineIdx, solid ->
                // 上爻（lines[0]）在最外，初爻在最内
                val barCenterY = cy - radius * TRIGRAM_BAR + (lineIdx - 1) * barStep
                if (solid) {
                    drawRect(
                        color = barColor,
                        topLeft = Offset(cx - barLength / 2f, barCenterY - barThick / 2f),
                        size = Size(barLength, barThick)
                    )
                } else {
                    val segLength = barLength * 0.38f
                    drawRect(
                        color = barColor,
                        topLeft = Offset(cx - barLength / 2f, barCenterY - barThick / 2f),
                        size = Size(segLength, barThick)
                    )
                    drawRect(
                        color = barColor,
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
 * 构建天池菱形指针的一半（pointingUp=true 为北向针）。
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
