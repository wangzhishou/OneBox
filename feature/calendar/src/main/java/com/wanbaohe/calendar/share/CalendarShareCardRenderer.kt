package com.wanbaohe.calendar.share

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons as MaterialIcons
import com.wanbaohe.icons.Rat
import com.wanbaohe.icons.Ox
import com.wanbaohe.icons.Tiger
import com.wanbaohe.icons.Rabbit
import com.wanbaohe.icons.Dragon
import com.wanbaohe.icons.Snake
import com.wanbaohe.icons.Horse
import com.wanbaohe.icons.Goat
import com.wanbaohe.icons.Monkey
import com.wanbaohe.icons.Rooster
import com.wanbaohe.icons.Dog
import com.wanbaohe.icons.Pig
import com.t8rin.imagetoolbox.core.resources.Icons
import com.wanbaohe.icons.ornament.OrnamentChineseKnot
import com.wanbaohe.icons.ornament.OrnamentCloudRed
import com.wanbaohe.icons.ornament.OrnamentCornerTlGray
import com.wanbaohe.icons.ornament.OrnamentCornerTlRed
import com.wanbaohe.icons.ornament.OrnamentMeanderTile
import com.wanbaohe.icons.ornament.OrnamentSealGreen
import com.wanbaohe.icons.ornament.OrnamentSealRed
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.random.Random

/**
 * 把 [CalendarShareCardData] 画成一张 1080 宽、按内容自适应高度的 PNG。
 *
 * 所有图形都是矢量的：纸页撕口、打孔、纸纤维、祥云、如意印、回纹角、铜钱、
 * 盘长结、田字、生肖马剪影，以及可选的节日/天气/心语信息栏。
 *
 * 纹样替换：祥云、印章、回纹角目前是参数化矢量（圆/圆角矩形并集 + 折线），
 * 若要换成设计稿 SVG，把对应 `buildXxx()` 生成的 Path 换成 `PathParser` 解析的
 * pathData 即可，版面坐标仍是 1080 宽。
 */
object CalendarShareCardRenderer {

    const val WIDTH = 1080

    private const val TEAR_TOP = 58f
    /** 纸页四周留出的透明边，保存成 PNG 后是真正的透明背景。 */
    private const val PAPER_INSET = 24f
    private const val TEAR_STEPS = 90
    private const val HOLE_LEFT_X = 270f
    private const val HOLE_RIGHT_X = 810f
    private const val HOLE_Y = 128f
    private const val HOLE_RADIUS = 27f
    private const val WEATHER_CARD_HEIGHT = 150f
    private const val WEATHER_CARD_GAP = 22f
    private const val FESTIVAL_BLOCK_TWO_ROWS = 96f
    private const val FESTIVAL_BLOCK_ONE_ROW = 70f
    private const val NOTE_BLOCK_HEIGHT = 120f

    fun render(
        data: CalendarShareCardData,
        options: CalendarShareRenderOptions,
        textMeasurer: TextMeasurer,
    ): ImageBitmap {
        val layout = buildLayout(data, options)
        val paper = buildPaperGeometry(layout.height)
        val image = ImageBitmap(
            width = WIDTH,
            height = layout.height.toInt(),
            hasAlpha = true
        )
        val canvas = Canvas(image)

        CanvasDrawScope().draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = canvas,
            size = Size(WIDTH.toFloat(), layout.height)
        ) {
            drawCard(
                data = data,
                options = options,
                textMeasurer = textMeasurer,
                paper = paper,
                layout = layout
            )
        }
        return image
    }

    // ─── 版面规格 ────────────────────────────────────────────────────

    private data class CardLayout(
        val height: Float,
        val weatherTop: Float?,
        val festivalTop: Float?,
        val noteTop: Float?,
        val footerLineY: Float,
        val brandBaseline: Float,
    )

    /**
     * 单张卡片、内容自适应高度：
     * - 没有天气/节日/心语时就是 3:4 左右的经典版式；
     * - 有天气卡、节日栏、心语时依次往下加，页脚跟着下移；
     * - 「有了就显示」，不预留空位。
     */
    private fun buildLayout(
        data: CalendarShareCardData,
        options: CalendarShareRenderOptions,
    ): CardLayout {
        val hasWeather = options.showWeather && data.weather?.hasContent == true
        val hasToday = options.showTodayFestival && data.todayFestivals.isNotEmpty()
        val hasUpcoming = options.showUpcomingFestivals &&
            data.upcomingFestivals.any { it.daysUntil > 0 }
        val hasFestival = hasToday || hasUpcoming
        val hasNote = options.showNote && data.note.isNotBlank()

        var y = 1360f
        val weatherTop = if (hasWeather) {
            val top = y
            y += WEATHER_CARD_HEIGHT + WEATHER_CARD_GAP
            top
        } else {
            null
        }
        val festivalTop = if (hasFestival) {
            val top = y
            y += if (hasToday && hasUpcoming) FESTIVAL_BLOCK_TWO_ROWS else FESTIVAL_BLOCK_ONE_ROW
            top
        } else {
            null
        }
        val noteTop = if (hasNote) {
            val top = y
            y += NOTE_BLOCK_HEIGHT
            top
        } else {
            null
        }

        val footerLine = y + 26f
        val brand = footerLine + 50f
        return CardLayout(
            height = brand + 20f + PAPER_INSET,
            weatherTop = weatherTop,
            festivalTop = festivalTop,
            noteTop = noteTop,
            footerLineY = footerLine,
            brandBaseline = brand,
        )
    }

    // ─── 纸页 ────────────────────────────────────────────────────────

    private class PaperGeometry(
        val path: Path,
        val tornEdge: List<Offset>,
    )

    private fun buildPaperGeometry(height: Float): PaperGeometry {
        val random = Random(20261020)
        val edge = ArrayList<Offset>(TEAR_STEPS + 1)
        val outline = Path()
        val start = Offset(PAPER_INSET, TEAR_TOP + 3f)
        outline.moveTo(start.x, start.y)
        edge += start

        for (i in 1..TEAR_STEPS) {
            val x = PAPER_INSET + i * (WIDTH - PAPER_INSET * 2f) / TEAR_STEPS
            var y = TEAR_TOP + sin(i * 1.9f) * 1.8f + (random.nextFloat() - 0.5f) * 3.6f
            if (i % 11 == 0) y += 5f
            outline.lineTo(x, y)
            edge += Offset(x, y)
        }
        outline.lineTo(WIDTH - PAPER_INSET, height - PAPER_INSET)
        outline.lineTo(PAPER_INSET, height - PAPER_INSET)
        outline.close()

        val holes = Path().apply {
            addOval(Rect(center = Offset(HOLE_LEFT_X, HOLE_Y), radius = HOLE_RADIUS))
            addOval(Rect(center = Offset(HOLE_RIGHT_X, HOLE_Y), radius = HOLE_RADIUS))
        }
        val path = Path()
        val cut = path.op(outline, holes, PathOperation.Difference)

        return PaperGeometry(
            path = if (cut) path else outline,
            tornEdge = edge
        )
    }

    private fun DrawScope.drawCard(
        data: CalendarShareCardData,
        options: CalendarShareRenderOptions,
        textMeasurer: TextMeasurer,
        paper: PaperGeometry,
        layout: CardLayout,
    ) {
        drawPaper(paper, data.palette, layout.height)

        drawHeader(data, textMeasurer)
        drawMainDay(data, textMeasurer)
        drawLunarBanner(data, textMeasurer)
        drawAlmanacBand(data, textMeasurer)

        layout.weatherTop?.let { top ->
            data.weather?.let { weather ->
                drawWeatherCard(
                    weather = weather,
                    top = top,
                    palette = data.palette,
                    labels = data.labels,
                    textMeasurer = textMeasurer
                )
            }
        }
        layout.festivalTop?.let { top ->
            drawFestivalBlock(
                data = data,
                options = options,
                textMeasurer = textMeasurer,
                top = top
            )
        }
        layout.noteTop?.let { top ->
            drawNoteBlock(
                data = data,
                textMeasurer = textMeasurer,
                top = top
            )
        }

        drawFooter(
            data = data,
            textMeasurer = textMeasurer,
            layout = layout
        )

        for (i in 1 until paper.tornEdge.size) {
            drawLine(
                color = data.palette.accent.copy(alpha = 0.16f),
                start = paper.tornEdge[i - 1],
                end = paper.tornEdge[i],
                strokeWidth = 2f
            )
        }
    }

    private fun DrawScope.drawPaper(
        paper: PaperGeometry,
        palette: CalendarSharePalette,
        height: Float,
    ) {
        drawPath(paper.path, palette.paper)

        clipPath(paper.path) {
            drawRect(
                brush = Brush.radialGradient(
                    0f to Color.Transparent,
                    0.58f to Color.Transparent,
                    1f to palette.ink.copy(alpha = 0.05f),
                    center = Offset(WIDTH / 2f, height * 0.43f),
                    radius = height * 0.65f
                ),
                topLeft = Offset.Zero,
                size = Size(WIDTH.toFloat(), height)
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        palette.ink.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    startY = TEAR_TOP,
                    endY = TEAR_TOP + 96f
                ),
                topLeft = Offset(0f, TEAR_TOP),
                size = Size(WIDTH.toFloat(), 96f)
            )
            drawPaperFibers(palette.ink, height)
        }

        listOf(HOLE_LEFT_X, HOLE_RIGHT_X).forEach { cx ->
            drawCircle(
                color = palette.accent.copy(alpha = 0.22f),
                radius = HOLE_RADIUS,
                center = Offset(cx, HOLE_Y),
                style = Stroke(width = 2f)
            )
            drawArc(
                color = palette.ink.copy(alpha = 0.07f),
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(cx - HOLE_RADIUS + 1f, HOLE_Y - HOLE_RADIUS + 1f),
                size = Size((HOLE_RADIUS - 1f) * 2f, (HOLE_RADIUS - 1f) * 2f),
                style = Stroke(width = 3f)
            )
        }
    }

    private fun DrawScope.drawPaperFibers(ink: Color, height: Float) {
        val random = Random(91)
        repeat(280) {
            val x = random.nextFloat() * WIDTH
            val y = TEAR_TOP + 20f + random.nextFloat() * (height - TEAR_TOP - 40f)
            val length = 5f + random.nextFloat() * 26f
            drawLine(
                color = ink.copy(alpha = 0.012f + random.nextFloat() * 0.02f),
                start = Offset(x, y),
                end = Offset(x + length, y + (random.nextFloat() - 0.5f) * 1.6f),
                strokeWidth = 0.8f + random.nextFloat() * 0.5f
            )
        }
        repeat(80) {
            drawCircle(
                color = ink.copy(alpha = 0.012f + random.nextFloat() * 0.012f),
                radius = 0.7f + random.nextFloat() * 1.1f,
                center = Offset(
                    random.nextFloat() * WIDTH,
                    TEAR_TOP + random.nextFloat() * (height - TEAR_TOP)
                )
            )
        }
    }

    // ─── 页眉 ────────────────────────────────────────────────────────

    private fun DrawScope.drawHeader(
        data: CalendarShareCardData,
        textMeasurer: TextMeasurer,
    ) {
        val palette = data.palette
        val accent = palette.accent
        val labels = data.labels
        val style = serif(
            size = 76f,
            color = accent,
            weight = FontWeight.Bold,
            letterSpacing = 3f
        )

        drawCloudOrnament(
            center = Offset(126f, 154f),
            scale = 1.4f,
            color = accent,
            alpha = 0.34f
        )
        drawCloudOrnament(
            center = Offset(954f, 154f),
            scale = 1.4f,
            color = accent,
            alpha = 0.34f,
            mirrored = true
        )

        val year = textMeasurer.oneLine(
            text = "${data.solarYear}${labels.yearSuffix}",
            style = style
        )
        drawLeft(year, x = 88f, baselineY = 252f)

        val month = textMeasurer.oneLine(
            text = "${data.solarMonth}${labels.monthSuffix}",
            style = style
        )
        drawRight(month, right = WIDTH - 88f, baselineY = 252f)

        val ruleColor = accent.copy(alpha = 0.55f)
        drawLine(ruleColor, Offset(88f, 292f), Offset(470f, 292f), 2f)
        drawLine(ruleColor, Offset(610f, 292f), Offset(WIDTH - 88f, 292f), 2f)

        drawCircle(
            color = accent,
            radius = 19f,
            center = Offset(WIDTH / 2f, 292f),
            style = Stroke(width = 3f)
        )
        drawRoundRect(
            color = accent,
            topLeft = Offset(WIDTH / 2f - 7f, 292f - 7f),
            size = Size(14f, 14f),
            cornerRadius = CornerRadius(1.5f),
            style = Stroke(width = 3f)
        )

        drawCloudOrnament(
            center = Offset(492f, 292f),
            scale = 0.55f,
            color = accent,
            alpha = 0.42f
        )
        drawCloudOrnament(
            center = Offset(588f, 292f),
            scale = 0.55f,
            color = accent,
            alpha = 0.42f,
            mirrored = true
        )
    }

    // ─── 日期主体 ────────────────────────────────────────────────────

    private fun DrawScope.drawMainDay(
        data: CalendarShareCardData,
        textMeasurer: TextMeasurer,
    ) {
        val palette = data.palette
        val accent = palette.accent

        drawVerticalRule(x = 246f, top = 326f, bottom = 826f, color = accent)
        drawVerticalRule(x = 834f, top = 326f, bottom = 826f, color = accent)
        drawCornerAsset(
            vector = Icons.Filled.OrnamentCornerTlRed,
            x = 246f, y = 326f, width = 72f, height = 44f,
            xDir = 1f, yDir = 1f, alpha = 0.75f, tint = accent
        )
        drawCornerAsset(
            vector = Icons.Filled.OrnamentCornerTlRed,
            x = 834f, y = 326f, width = 72f, height = 44f,
            xDir = -1f, yDir = 1f, alpha = 0.75f, tint = accent
        )
        drawCornerAsset(
            vector = Icons.Filled.OrnamentCornerTlRed,
            x = 246f, y = 826f, width = 72f, height = 44f,
            xDir = 1f, yDir = -1f, alpha = 0.75f, tint = accent
        )
        drawCornerAsset(
            vector = Icons.Filled.OrnamentCornerTlRed,
            x = 834f, y = 826f, width = 72f, height = 44f,
            xDir = -1f, yDir = -1f, alpha = 0.75f, tint = accent
        )
        drawCloudOrnament(
            center = Offset(195f, 798f),
            scale = 0.7f,
            color = accent,
            alpha = 0.32f
        )
        drawCloudOrnament(
            center = Offset(885f, 798f),
            scale = 0.7f,
            color = accent,
            alpha = 0.32f,
            mirrored = true
        )

        val day = textMeasurer.oneLine(
            text = data.solarDay.toString(),
            style = serif(
                size = 470f,
                color = accent,
                weight = FontWeight.Bold,
                letterSpacing = -10f
            )
        )
        drawCentered(day, centerX = WIDTH / 2f, baselineY = 700f)

        val verticalStyle = serif(
            size = 47f,
            color = accent,
            weight = FontWeight.Bold
        )
        drawVerticalChars(
            text = data.ganZhiYear + data.labels.yearSuffix,
            centerX = 150f,
            firstBaseline = 398f,
            step = 64f,
            textMeasurer = textMeasurer,
            style = verticalStyle
        )
        drawVerticalChars(
            text = data.zodiac + data.labels.yearSuffix,
            centerX = 150f,
            firstBaseline = 642f,
            step = 64f,
            textMeasurer = textMeasurer,
            style = verticalStyle
        )

        val zodiac = zodiacVector(data.zodiac)
        if (zodiac != null) {
            drawVectorAsset(
                vector = zodiac,
                left = 150f - 44f,
                top = 790f - 44f,
                width = 88f,
                height = 88f,
                tint = accent
            )
        } else {
            drawZodiacSeal(
                center = Offset(150f, 790f),
                glyph = data.zodiac,
                palette = palette,
                textMeasurer = textMeasurer
            )
        }

        drawVerticalChars(
            text = data.weekday,
            centerX = 930f,
            firstBaseline = 398f,
            step = 64f,
            textMeasurer = textMeasurer,
            style = verticalStyle
        )
        val weekdayEn = textMeasurer.oneLine(
            text = data.weekdayEn,
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 29.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                letterSpacing = 10.sp
            )
        )
        drawCentered(weekdayEn, centerX = 930f, baselineY = 636f)
        val rightKnotSize = 82f
        drawVectorAsset(
            vector = Icons.Filled.OrnamentChineseKnot,
            left = 930f - rightKnotSize / 2f,
            top = 834f - rightKnotSize,
            width = rightKnotSize,
            height = rightKnotSize,
            alpha = 0.95f,
            tint = accent
        )
    }

    private fun DrawScope.drawVerticalRule(
        x: Float,
        top: Float,
        bottom: Float,
        color: Color,
    ) {
        val stroke = 2f
        val rule = color.copy(alpha = 0.62f)
        drawLine(rule, Offset(x, top), Offset(x, bottom), stroke)
        drawLine(rule, Offset(x - 20f, top), Offset(x + 20f, top), stroke)
        drawLine(rule, Offset(x - 20f, bottom), Offset(x + 20f, bottom), stroke)
    }

    private fun DrawScope.drawVerticalChars(
        text: String,
        centerX: Float,
        firstBaseline: Float,
        step: Float,
        textMeasurer: TextMeasurer,
        style: TextStyle,
    ) {
        text.forEachIndexed { index, char ->
            if (char.isWhitespace()) return@forEachIndexed
            val layout = textMeasurer.oneLine(char.toString(), style)
            drawCentered(
                layout = layout,
                centerX = centerX,
                baselineY = firstBaseline + index * step
            )
        }
    }

    private fun zodiacVector(zodiac: String): ImageVector? = when (zodiac) {
        "鼠" -> MaterialIcons.Filled.Rat
        "牛" -> MaterialIcons.Filled.Ox
        "虎" -> MaterialIcons.Filled.Tiger
        "兔" -> MaterialIcons.Filled.Rabbit
        "龙" -> MaterialIcons.Filled.Dragon
        "蛇" -> MaterialIcons.Filled.Snake
        "马" -> MaterialIcons.Filled.Horse
        "羊" -> MaterialIcons.Filled.Goat
        "猴" -> MaterialIcons.Filled.Monkey
        "鸡" -> MaterialIcons.Filled.Rooster
        "狗" -> MaterialIcons.Filled.Dog
        "猪" -> MaterialIcons.Filled.Pig
        else -> null
    }

    private fun DrawScope.drawZodiacSeal(
        center: Offset,
        glyph: String,
        palette: CalendarSharePalette,
        textMeasurer: TextMeasurer,
    ) {
        drawCircle(color = palette.accent, radius = 48f, center = center)
        drawCircle(
            color = palette.paper.copy(alpha = 0.85f),
            radius = 39f,
            center = center,
            style = Stroke(width = 2f)
        )
        val layout = textMeasurer.oneLine(
            text = glyph,
            style = serif(size = 46f, color = palette.onAccent)
        )
        drawCenteredInBox(layout, center)
    }

    // ─── 农历横幅 ────────────────────────────────────────────────────

    private fun DrawScope.drawLunarBanner(
        data: CalendarShareCardData,
        textMeasurer: TextMeasurer,
    ) {
        val accent = data.palette.accent
        val left = 84f
        val top = 858f
        val right = WIDTH - 84f
        val bottom = 966f

        drawRect(
            color = accent.copy(alpha = 0.035f),
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top)
        )
        drawRect(
            color = accent,
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top),
            style = Stroke(width = 2.5f)
        )
        drawRect(
            color = accent.copy(alpha = 0.6f),
            topLeft = Offset(left + 10f, top + 10f),
            size = Size(right - left - 20f, bottom - top - 20f),
            style = Stroke(width = 1f)
        )

        drawCornerAsset(
            vector = Icons.Filled.OrnamentCornerTlRed,
            x = left,
            y = top,
            width = 78f,
            height = 48f,
            xDir = 1f,
            yDir = 1f,
            alpha = 0.95f,
            tint = accent
        )
        drawCornerAsset(
            vector = Icons.Filled.OrnamentCornerTlRed,
            x = right,
            y = top,
            width = 78f,
            height = 48f,
            xDir = -1f,
            yDir = 1f,
            alpha = 0.95f,
            tint = accent
        )
        drawCornerAsset(
            vector = Icons.Filled.OrnamentCornerTlRed,
            x = left,
            y = bottom,
            width = 78f,
            height = 48f,
            xDir = 1f,
            yDir = -1f,
            alpha = 0.95f,
            tint = accent
        )
        drawCornerAsset(
            vector = Icons.Filled.OrnamentCornerTlRed,
            x = right,
            y = bottom,
            width = 78f,
            height = 48f,
            xDir = -1f,
            yDir = -1f,
            alpha = 0.95f,
            tint = accent
        )

        val text = textMeasurer.oneLine(
            text = buildString {
                append(data.labels.lunarPrefix)
                append(data.lunarMonthName)
                append(data.lunarDayName)
            },
            style = serif(
                size = 62f,
                color = accent,
                weight = FontWeight.Bold,
                letterSpacing = 8f
            )
        )
        drawCloudOrnament(
            center = Offset(205f, 912f),
            scale = 1.2f,
            color = accent,
            alpha = 0.37f
        )
        drawCloudOrnament(
            center = Offset(875f, 912f),
            scale = 1.2f,
            color = accent,
            alpha = 0.37f,
            mirrored = true
        )
        drawCentered(text, centerX = WIDTH / 2f, baselineY = 933f)
    }

    // ─── 三栏黄历：宜 / 今日干支+吉时 / 忌 ────────────────────────

    /**
     * 下方三块共用一条外框、同高同宽：
     * 左「宜」、中「今日干支 + 吉时」、右「忌」，左右两边和上下边严格对齐。
     */
    private fun DrawScope.drawAlmanacBand(
        data: CalendarShareCardData,
        textMeasurer: TextMeasurer,
    ) {
        val palette = data.palette
        val accent = palette.accent
        val left = 84f
        val top = 990f
        val right = WIDTH - 84f
        val bottom = 1338f
        val columnWidth = (right - left) / 3f
        val centers = listOf(
            left + columnWidth / 2f,
            left + columnWidth * 1.5f,
            left + columnWidth * 2.5f
        )

        drawRect(
            color = accent.copy(alpha = 0.018f),
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top)
        )
        drawRect(
            color = accent,
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top),
            style = Stroke(width = 2.5f)
        )
        drawRect(
            color = accent.copy(alpha = 0.5f),
            topLeft = Offset(left + 9f, top + 9f),
            size = Size(right - left - 18f, bottom - top - 18f),
            style = Stroke(width = 1f)
        )

        drawCornerAsset(
            vector = Icons.Filled.OrnamentCornerTlGray,
            x = left + 12f, y = top + 12f, width = 72f, height = 44f,
            xDir = 1f, yDir = 1f, alpha = 0.8f, tint = palette.inkSoft
        )
        drawCornerAsset(
            vector = Icons.Filled.OrnamentCornerTlRed,
            x = right - 12f, y = top + 12f, width = 72f, height = 44f,
            xDir = -1f, yDir = 1f, alpha = 0.8f, tint = accent
        )
        drawCornerAsset(
            vector = Icons.Filled.OrnamentCornerTlGray,
            x = left + 12f, y = bottom - 12f, width = 72f, height = 44f,
            xDir = 1f, yDir = -1f, alpha = 0.8f, tint = palette.inkSoft
        )
        drawCornerAsset(
            vector = Icons.Filled.OrnamentCornerTlRed,
            x = right - 12f, y = bottom - 12f, width = 72f, height = 44f,
            xDir = -1f, yDir = -1f, alpha = 0.8f, tint = accent
        )

        listOf(left + columnWidth, left + columnWidth * 2f).forEach { x ->
            drawLine(
                color = accent.copy(alpha = 0.5f),
                start = Offset(x, top + 18f),
                end = Offset(x, bottom - 18f),
                strokeWidth = 1.5f
            )
            val midY = (top + bottom) / 2f
            val diamond = Path().apply {
                moveTo(x, midY - 5f)
                lineTo(x + 6f, midY)
                lineTo(x, midY + 5f)
                lineTo(x - 6f, midY)
                close()
            }
            drawPath(diamond, accent.copy(alpha = 0.55f))
        }

        drawSeal(
            center = Offset(centers[0], 1050f),
            glyph = data.labels.yiSeal,
            onFill = palette.onYiSeal,
            textMeasurer = textMeasurer,
            vector = Icons.Filled.OrnamentSealGreen,
            tint = palette.yiSeal
        )
        drawSeal(
            center = Offset(centers[2], 1050f),
            glyph = data.labels.jiSeal,
            onFill = palette.onJiSeal,
            textMeasurer = textMeasurer,
            vector = Icons.Filled.OrnamentSealRed,
            tint = palette.jiSeal
        )

        drawAlmanacList(
            items = data.yi,
            centerX = centers[0],
            textMeasurer = textMeasurer,
            palette = palette
        )
        drawAlmanacList(
            items = data.ji,
            centerX = centers[2],
            textMeasurer = textMeasurer,
            palette = palette
        )
        drawAlmanacCenter(
            data = data,
            textMeasurer = textMeasurer,
            centerX = centers[1]
        )
    }

    private fun DrawScope.drawAlmanacList(
        items: List<String>,
        centerX: Float,
        textMeasurer: TextMeasurer,
        palette: CalendarSharePalette,
    ) {
        val shown = items.take(8)
        if (shown.isEmpty()) {
            drawCentered(
                layout = textMeasurer.oneLine(
                    text = "—",
                    style = serif(size = 28f, color = palette.inkSoft)
                ),
                centerX = centerX,
                baselineY = 1180f
            )
            return
        }

        val style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 28.sp,
            color = palette.ink,
            letterSpacing = 1.sp
        )
        val columns = listOf(centerX - 70f, centerX + 70f)
        val paired = (shown.size / 2) * 2

        fun drawItem(item: String, x: Float, baselineY: Float) {
            var layout = textMeasurer.oneLine(item, style)
            if (layout.size.width > 124f) {
                val scale = 124f / layout.size.width
                layout = textMeasurer.oneLine(
                    text = item,
                    style = style.copy(fontSize = style.fontSize * scale)
                )
            }
            drawCentered(layout = layout, centerX = x, baselineY = baselineY)
        }

        for (index in 0 until paired) {
            drawItem(
                item = shown[index],
                x = columns[index % 2],
                baselineY = 1152f + (index / 2) * 40f
            )
        }
        if (shown.size % 2 == 1) {
            // 奇数条时最后一条居中，避免出现一边三行、一边两行的缺口
            drawItem(
                item = shown.last(),
                x = centerX,
                baselineY = 1152f + (paired / 2) * 40f
            )
        }
    }

    private fun DrawScope.drawAlmanacCenter(
        data: CalendarShareCardData,
        textMeasurer: TextMeasurer,
        centerX: Float,
    ) {
        val palette = data.palette
        val accent = palette.accent
        val labels = data.labels

        drawCentered(
            layout = textMeasurer.oneLine(
                text = labels.todayPrefix,
                style = serif(
                    size = 18f,
                    color = accent,
                    weight = FontWeight.SemiBold,
                    letterSpacing = 5f
                )
            ),
            centerX = centerX,
            baselineY = 1030f
        )

        val ganZhi = buildString {
            append(data.ganZhiYear)
            append(labels.yearSuffix)
            if (data.ganZhiMonth.isNotBlank()) {
                append(" ")
                append(data.ganZhiMonth)
                append(labels.monthSuffix)
            }
            if (data.ganZhiDay.isNotBlank()) {
                append(" ")
                append(data.ganZhiDay)
                append(labels.daySuffix)
            }
        }
        drawCentered(
            layout = textMeasurer.oneLine(
                text = ganZhi,
                style = serif(
                    size = 20f,
                    color = palette.ink,
                    letterSpacing = 1f
                )
            ),
            centerX = centerX,
            baselineY = 1068f
        )

        drawLine(
            color = accent.copy(alpha = 0.35f),
            start = Offset(centerX - 90f, 1092f),
            end = Offset(centerX - 12f, 1092f),
            strokeWidth = 1f
        )
        drawLine(
            color = accent.copy(alpha = 0.35f),
            start = Offset(centerX + 12f, 1092f),
            end = Offset(centerX + 90f, 1092f),
            strokeWidth = 1f
        )
        val diamond = Path().apply {
            moveTo(centerX, 1087f)
            lineTo(centerX + 5f, 1092f)
            lineTo(centerX, 1097f)
            lineTo(centerX - 5f, 1092f)
            close()
        }
        drawPath(diamond, accent.copy(alpha = 0.55f))

        drawCentered(
            layout = textMeasurer.oneLine(
                text = labels.auspiciousTitle,
                style = serif(
                    size = 22f,
                    color = accent,
                    weight = FontWeight.SemiBold,
                    letterSpacing = 5f
                )
            ),
            centerX = centerX,
            baselineY = 1128f
        )
        drawLine(
            color = accent.copy(alpha = 0.3f),
            start = Offset(centerX - 100f, 1120f),
            end = Offset(centerX - 45f, 1120f),
            strokeWidth = 1f
        )
        drawLine(
            color = accent.copy(alpha = 0.3f),
            start = Offset(centerX + 45f, 1120f),
            end = Offset(centerX + 100f, 1120f),
            strokeWidth = 1f
        )

        val hours = data.luckyHours.take(3)
        if (hours.isEmpty()) {
            drawCentered(
                layout = textMeasurer.oneLine(
                    text = "—",
                    style = serif(size = 22f, color = palette.inkSoft)
                ),
                centerX = centerX,
                baselineY = 1162f
            )
        }

        hours.forEachIndexed { index, hour ->
            val nameLayout = textMeasurer.oneLine(
                text = hour.name,
                style = serif(
                    size = 21f,
                    color = accent,
                    weight = FontWeight.SemiBold,
                    letterSpacing = 1.5f
                )
            )
            val rangeLayout = textMeasurer.oneLine(
                text = hour.range,
                style = serif(
                    size = 17f,
                    color = palette.inkSoft,
                    letterSpacing = 1f
                )
            )
            val gap = 9f
            val total = nameLayout.size.width + gap + rangeLayout.size.width
            val startX = centerX - total / 2f
            val baseline = 1162f + index * 40f
            drawLeft(nameLayout, x = startX, baselineY = baseline)
            drawLeft(
                layout = rangeLayout,
                x = startX + nameLayout.size.width + gap,
                baselineY = baseline
            )
        }

        val knotSize = 54f
        drawVectorAsset(
            vector = Icons.Filled.OrnamentChineseKnot,
            left = centerX - knotSize / 2f,
            top = 1274f - knotSize / 2f,
            width = knotSize,
            height = knotSize,
            alpha = 0.9f,
            tint = accent
        )
        drawMeanderStrip(
            left = 180f,
            right = 900f,
            top = 1311f,
            height = 13f,
            alpha = 0.6f,
            tint = accent
        )
    }

    private fun DrawScope.drawSeal(
        center: Offset,
        glyph: String,
        onFill: Color,
        textMeasurer: TextMeasurer,
        vector: ImageVector,
        tint: Color,
    ) {
        val width = 86f
        val height = 79f
        drawVectorAsset(
            vector = vector,
            left = center.x - width / 2f,
            top = center.y - height / 2f,
            width = width,
            height = height,
            tint = tint,
            preserveLight = true,
            lightTint = onFill
        )

        val glyphLayout = textMeasurer.oneLine(
            text = glyph,
            style = serif(size = 35f, color = onFill)
        )
        drawCenteredInBox(glyphLayout, center)
    }

    // ─── 天气卡 / 节日栏 / 心语（有则显示）────────────────────────

    private enum class WeatherKind { Sunny, Cloudy, Rain, Snow, Fog }

    private fun CalendarShareWeather.kind(): WeatherKind {
        val code = iconCode.trim()
        val text = text.lowercase()
        return when {
            code.startsWith("3") || text.contains("雨") || text.contains("rain") -> WeatherKind.Rain
            code.startsWith("4") || text.contains("雪") || text.contains("snow") -> WeatherKind.Snow
            text.contains("雾") || text.contains("霾") ||
                text.contains("fog") || text.contains("haze") ||
                code in setOf("501", "502", "509", "510", "514", "515") -> WeatherKind.Fog

            code == "100" || code == "150" ||
                text.contains("晴") || text.contains("sun") || text.contains("clear") -> WeatherKind.Sunny

            else -> WeatherKind.Cloudy
        }
    }

    /**
     * 天气单独成卡，和宜/忌一样有边框、底色和图标，而不是一行小字。
     */
    private fun DrawScope.drawWeatherCard(
        weather: CalendarShareWeather,
        top: Float,
        palette: CalendarSharePalette,
        labels: CalendarShareLabels,
        textMeasurer: TextMeasurer,
    ) {
        val left = 84f
        val right = WIDTH - 84f
        val card = Path().apply {
            addRoundRect(
                RoundRect(
                    left = left,
                    top = top,
                    right = right,
                    bottom = top + WEATHER_CARD_HEIGHT,
                    radiusX = 16f,
                    radiusY = 16f
                )
            )
        }
        drawPath(card, palette.accent.copy(alpha = 0.035f))
        drawPath(card, palette.accent.copy(alpha = 0.75f), style = Stroke(width = 1.5f))

        val innerFrame = Path().apply {
            addRoundRect(
                RoundRect(
                    left = left + 8f,
                    top = top + 8f,
                    right = right - 8f,
                    bottom = top + WEATHER_CARD_HEIGHT - 8f,
                    radiusX = 12f,
                    radiusY = 12f
                )
            )
        }
        drawPath(
            path = innerFrame,
            color = palette.accent.copy(alpha = 0.3f),
            style = Stroke(width = 1f)
        )

        val borderClouds = listOf(
            left + 54f,
            (left + right) / 2f,
            right - 54f
        )
        borderClouds.forEachIndexed { index, x ->
            drawCloudOrnament(
                center = Offset(x, top + 8f),
                scale = 0.45f,
                color = palette.accent,
                alpha = 0.5f,
                mirrored = index == 2
            )
            drawCloudOrnament(
                center = Offset(x, top + WEATHER_CARD_HEIGHT - 8f),
                scale = 0.45f,
                color = palette.accent,
                alpha = 0.5f,
                mirrored = index == 0
            )
        }

        drawLeft(
            layout = textMeasurer.oneLine(
                text = labels.weatherTitle,
                style = serif(20f, palette.accent, FontWeight.SemiBold, letterSpacing = 4f)
            ),
            x = left + 22f,
            baselineY = top + 38f
        )

        drawCloudOrnament(
            center = Offset(left + 12f, top + 75f),
            scale = 0.3f,
            color = palette.accent,
            alpha = 0.45f
        )
        drawCloudOrnament(
            center = Offset(right - 12f, top + 75f),
            scale = 0.3f,
            color = palette.accent,
            alpha = 0.45f,
            mirrored = true
        )

        val iconCenter = Offset(left + 74f, top + 92f)
        drawCircle(palette.accent.copy(alpha = 0.08f), 34f, iconCenter)
        drawCircle(
            color = palette.accent.copy(alpha = 0.22f),
            radius = 34f,
            center = iconCenter,
            style = Stroke(width = 1f)
        )
        drawWeatherIcon(
            weather = weather,
            center = iconCenter,
            size = 22f,
            color = palette.accent
        )

        val city = weather.city.ifBlank { weather.text }
        if (city.isNotBlank()) {
            drawLeft(
                layout = textMeasurer.oneLine(
                    text = city,
                    style = serif(30f, palette.ink, FontWeight.SemiBold, letterSpacing = 1f)
                ),
                x = left + 128f,
                baselineY = top + 80f
            )
        }
        if (weather.city.isNotBlank() && weather.text.isNotBlank()) {
            drawLeft(
                layout = textMeasurer.oneLine(
                    text = weather.text,
                    style = serif(23f, palette.inkSoft, letterSpacing = 1f)
                ),
                x = left + 128f,
                baselineY = top + 114f
            )
        }

        drawRight(
            layout = textMeasurer.oneLine(
                text = (weather.temp.ifBlank { "--" }) + "°",
                style = serif(52f, palette.accent, FontWeight.Bold, letterSpacing = -1f)
            ),
            right = right - 28f,
            baselineY = top + 92f
        )

        val range = when {
            weather.tempMin.isNotBlank() && weather.tempMax.isNotBlank() ->
                "${weather.tempMin}°~${weather.tempMax}°"

            weather.tempMin.isNotBlank() -> "${weather.tempMin}°"
            weather.tempMax.isNotBlank() -> "${weather.tempMax}°"
            else -> ""
        }
        if (range.isNotBlank()) {
            drawRight(
                layout = textMeasurer.oneLine(
                    text = range,
                    style = serif(21f, palette.inkSoft, letterSpacing = 1f)
                ),
                right = right - 28f,
                baselineY = top + 124f
            )
        }
        if (weather.aqiCategory.isNotBlank()) {
            drawRight(
                layout = textMeasurer.oneLine(
                    text = "${labels.airPrefix} ${weather.aqiCategory}",
                    style = serif(19f, palette.accent, letterSpacing = 2f)
                ),
                right = right - 28f,
                baselineY = top + 38f
            )
        }

    }

    private fun DrawScope.drawWeatherIcon(
        weather: CalendarShareWeather,
        center: Offset,
        size: Float,
        color: Color,
    ) {
        when (weather.kind()) {
            WeatherKind.Sunny -> {
                drawCircle(color = color, radius = size * 0.55f, center = center)
                repeat(8) { index ->
                    val angle = index * PI / 4
                    val dx = cos(angle).toFloat()
                    val dy = sin(angle).toFloat()
                    drawLine(
                        color = color,
                        start = Offset(center.x + dx * size * 0.78f, center.y + dy * size * 0.78f),
                        end = Offset(center.x + dx * size * 1.08f, center.y + dy * size * 1.08f),
                        strokeWidth = 2.4f,
                        cap = StrokeCap.Round
                    )
                }
            }

            WeatherKind.Cloudy -> {
                translate(center.x, center.y + size * 0.08f) {
                    scale(size / 30f, size / 30f, pivot = Offset.Zero) {
                        drawPath(buildCloudPath(), color)
                    }
                }
            }

            WeatherKind.Rain -> {
                translate(center.x, center.y - size * 0.18f) {
                    scale(size / 38f, size / 38f, pivot = Offset.Zero) {
                        drawPath(buildCloudPath(), color)
                    }
                }
                listOf(-0.45f, 0f, 0.45f).forEach { factor ->
                    drawLine(
                        color = color,
                        start = Offset(center.x + factor * size - size * 0.12f, center.y + size * 0.35f),
                        end = Offset(center.x + factor * size - size * 0.28f, center.y + size * 0.8f),
                        strokeWidth = 2.2f,
                        cap = StrokeCap.Round
                    )
                }
            }

            WeatherKind.Snow -> {
                repeat(3) { index ->
                    val angle = index * PI / 3
                    val dx = cos(angle).toFloat()
                    val dy = sin(angle).toFloat()
                    drawLine(
                        color = color,
                        start = Offset(center.x - dx * size * 0.8f, center.y - dy * size * 0.8f),
                        end = Offset(center.x + dx * size * 0.8f, center.y + dy * size * 0.8f),
                        strokeWidth = 2.2f,
                        cap = StrokeCap.Round
                    )
                }
            }

            WeatherKind.Fog -> {
                listOf(-0.36f, 0f, 0.36f).forEach { factor ->
                    drawLine(
                        color = color,
                        start = Offset(center.x - size * 0.8f, center.y + factor * size),
                        end = Offset(center.x + size * 0.8f, center.y + factor * size),
                        strokeWidth = 2.4f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }

    private fun DrawScope.drawFestivalBlock(
        data: CalendarShareCardData,
        options: CalendarShareRenderOptions,
        textMeasurer: TextMeasurer,
        top: Float,
    ) {
        val palette = data.palette
        drawSectionDivider(top, palette.accent)

        val todayText = data.todayFestivals
            .take(3)
            .joinToString(" · ") { it.name }
            .takeIf { options.showTodayFestival && it.isNotBlank() }
        val upcomingText = data.upcomingFestivals
            .filter { it.daysUntil > 0 }
            .take(2)
            .joinToString(" · ") { "${it.name} ${it.dateText}" }
            .takeIf { options.showUpcomingFestivals && it.isNotBlank() }

        val both = todayText != null && upcomingText != null
        var baseline = top + if (both) 40f else 48f
        todayText?.let {
            drawInfoLine(
                prefix = data.labels.todayPrefix,
                content = it,
                centerX = WIDTH / 2f,
                baselineY = baseline,
                textMeasurer = textMeasurer,
                palette = palette
            )
            baseline += 36f
        }
        upcomingText?.let {
            drawInfoLine(
                prefix = data.labels.upcomingPrefix,
                content = it,
                centerX = WIDTH / 2f,
                baselineY = baseline,
                textMeasurer = textMeasurer,
                palette = palette
            )
        }
    }

    private fun DrawScope.drawNoteBlock(
        data: CalendarShareCardData,
        textMeasurer: TextMeasurer,
        top: Float,
    ) {
        drawNoteHeading(
            title = data.labels.noteTitle,
            centerX = WIDTH / 2f,
            baselineY = top + 32f,
            textMeasurer = textMeasurer,
            palette = data.palette
        )
        drawNote(
            note = data.note,
            centerX = WIDTH / 2f,
            baselineY = top + 66f,
            maxWidth = 840f,
            fontSize = 30f,
            lineHeight = 32f,
            textMeasurer = textMeasurer,
            palette = data.palette
        )
    }

    private fun DrawScope.drawSectionDivider(top: Float, accent: Color) {
        drawLine(
            color = accent.copy(alpha = 0.32f),
            start = Offset(84f, top),
            end = Offset(WIDTH / 2f - 18f, top),
            strokeWidth = 1.5f
        )
        drawLine(
            color = accent.copy(alpha = 0.32f),
            start = Offset(WIDTH / 2f + 18f, top),
            end = Offset(WIDTH - 84f, top),
            strokeWidth = 1.5f
        )
        val diamond = Path().apply {
            moveTo(WIDTH / 2f, top - 6f)
            lineTo(WIDTH / 2f + 7f, top)
            lineTo(WIDTH / 2f, top + 6f)
            lineTo(WIDTH / 2f - 7f, top)
            close()
        }
        drawPath(diamond, accent.copy(alpha = 0.6f))
    }

    private fun DrawScope.drawInfoLine(
        prefix: String,
        content: String,
        centerX: Float,
        baselineY: Float,
        textMeasurer: TextMeasurer,
        palette: CalendarSharePalette,
    ) {
        var prefixStyle = serif(
            size = 30f,
            color = palette.accent,
            weight = FontWeight.SemiBold,
            letterSpacing = 2f
        )
        var contentStyle = serif(
            size = 30f,
            color = palette.ink,
            letterSpacing = 1f
        )
        var prefixLayout = if (prefix.isBlank()) null else textMeasurer.oneLine(prefix, prefixStyle)
        var contentLayout = textMeasurer.oneLine(content, contentStyle)
        val gap = if (prefixLayout != null) 14f else 0f
        var totalWidth = (prefixLayout?.size?.width ?: 0).toFloat() + gap + contentLayout.size.width

        if (totalWidth > 900f) {
            val fit = 900f / totalWidth
            prefixStyle = prefixStyle.copy(fontSize = prefixStyle.fontSize * fit)
            contentStyle = contentStyle.copy(fontSize = contentStyle.fontSize * fit)
            prefixLayout = if (prefix.isBlank()) null else textMeasurer.oneLine(prefix, prefixStyle)
            contentLayout = textMeasurer.oneLine(content, contentStyle)
            totalWidth = (prefixLayout?.size?.width ?: 0).toFloat() + gap + contentLayout.size.width
        }

        val startX = centerX - totalWidth / 2f
        prefixLayout?.let {
            drawLeft(it, x = startX, baselineY = baselineY)
        }
        drawLeft(
            layout = contentLayout,
            x = startX + (prefixLayout?.size?.width ?: 0).toFloat() + gap,
            baselineY = baselineY
        )
    }

    private fun DrawScope.drawNoteHeading(
        title: String,
        centerX: Float,
        baselineY: Float,
        textMeasurer: TextMeasurer,
        palette: CalendarSharePalette,
    ) {
        val layout = textMeasurer.oneLine(
            text = title,
            style = serif(
                size = 22f,
                color = palette.accent,
                letterSpacing = 6f
            )
        )
        drawCentered(layout, centerX, baselineY)
        val half = layout.size.width / 2f
        val lineY = baselineY - 8f
        drawLine(
            color = palette.accent.copy(alpha = 0.28f),
            start = Offset(84f, lineY),
            end = Offset(centerX - half - 18f, lineY),
            strokeWidth = 1f
        )
        drawLine(
            color = palette.accent.copy(alpha = 0.28f),
            start = Offset(centerX + half + 18f, lineY),
            end = Offset(WIDTH - 84f, lineY),
            strokeWidth = 1f
        )
    }

    private fun DrawScope.drawNote(
        note: String,
        centerX: Float,
        baselineY: Float,
        maxWidth: Float,
        fontSize: Float,
        lineHeight: Float,
        textMeasurer: TextMeasurer,
        palette: CalendarSharePalette,
    ) {
        val style = TextStyle(
            fontFamily = FontFamily.Serif,
            fontSize = fontSize.sp,
            color = palette.ink,
            letterSpacing = 1.5.sp,
            lineHeight = lineHeight.sp
        )
        val layout = textMeasurer.measure(
            text = AnnotatedString(note),
            style = style,
            overflow = TextOverflow.Ellipsis,
            softWrap = true,
            maxLines = 2,
            constraints = Constraints(maxWidth = maxWidth.toInt())
        )
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(
                x = centerX - layout.size.width / 2f,
                y = baselineY - layout.firstBaseline
            )
        )
    }

    // ─── 页脚 ────────────────────────────────────────────────────────

    private fun DrawScope.drawFooter(
        data: CalendarShareCardData,
        textMeasurer: TextMeasurer,
        layout: CardLayout,
    ) {
        val palette = data.palette
        val accent = palette.accent
        val blessing = textMeasurer.oneLine(
            text = data.labels.blessing,
            style = serif(size = 41f, color = accent, letterSpacing = 9f)
        )
        val halfWidth = blessing.size.width / 2f
        val lineY = layout.footerLineY
        val gap = halfWidth + 26f

        val lineStart = 144f
        val lineEnd = 936f
        val leftLineEnd = (WIDTH / 2f - gap).coerceAtLeast(lineStart)
        val rightLineStart = (WIDTH / 2f + gap).coerceAtMost(lineEnd)
        if (leftLineEnd > lineStart) {
            drawLine(accent, Offset(lineStart, lineY), Offset(leftLineEnd, lineY), 2f)
        }
        if (rightLineStart < lineEnd) {
            drawLine(accent, Offset(rightLineStart, lineY), Offset(lineEnd, lineY), 2f)
        }

        drawFieldGlyph(center = Offset(132f, lineY), color = accent)
        drawFieldGlyph(center = Offset(948f, lineY), color = accent)

        drawCentered(blessing, centerX = WIDTH / 2f, baselineY = lineY + 14f)
        drawCloudOrnament(
            center = Offset(126f, layout.brandBaseline - 8f),
            scale = 1.1f,
            color = accent,
            alpha = 0.3f
        )
        drawCloudOrnament(
            center = Offset(954f, layout.brandBaseline - 8f),
            scale = 1.1f,
            color = accent,
            alpha = 0.3f,
            mirrored = true
        )
        drawBrand(data, textMeasurer, layout.brandBaseline)
    }

    private fun DrawScope.drawBrand(
        data: CalendarShareCardData,
        textMeasurer: TextMeasurer,
        baselineY: Float,
    ) {
        val brand = textMeasurer.oneLine(
            text = data.labels.brand,
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 20.sp,
                color = data.palette.inkSoft.copy(alpha = 0.65f),
                letterSpacing = 4.sp
            )
        )
        drawCentered(brand, centerX = WIDTH / 2f, baselineY = baselineY)
    }

    private fun DrawScope.drawFieldGlyph(center: Offset, color: Color) {
        val size = 24f
        drawRect(
            color = color,
            topLeft = Offset(center.x - size / 2f, center.y - size / 2f),
            size = Size(size, size),
            style = Stroke(width = 3f)
        )
        listOf(-4f, 0f, 4f).forEach { offsetY ->
            drawLine(
                color = color,
                start = Offset(center.x - size / 2f + 5f, center.y + offsetY),
                end = Offset(center.x + size / 2f - 5f, center.y + offsetY),
                strokeWidth = 2f
            )
        }
    }

    // ─── 矢量纹样 ────────────────────────────────────────────────────

    /**
     * 如意云头：三个圆 + 圆角底边做并集；两侧再叠一条卷云尾。
     */
    private fun buildCloudPath(): Path {
        var cloud = Path().apply {
            addOval(Rect(center = Offset(-15f, -2f), radius = 9f))
        }
        listOf(
            Triple(0f, -9f, 13f),
            Triple(16f, -1f, 8f)
        ).forEach { (cx, cy, radius) ->
            val circle = Path().apply {
                addOval(Rect(center = Offset(cx, cy), radius = radius))
            }
            cloud = cloud.unionWith(circle)
        }
        val base = Path().apply {
            addRoundRect(
                RoundRect(
                    left = -24f,
                    top = -2f,
                    right = 24f,
                    bottom = 7f,
                    radiusX = 4f,
                    radiusY = 4f
                )
            )
        }
        return cloud.unionWith(base)
    }

    private fun Path.unionWith(other: Path): Path {
        val result = Path()
        return if (result.op(this, other, PathOperation.Union)) result else this
    }

    /** 低饱和度的祥云装饰，使用设计稿导出的对称流云素材。 */
    private fun DrawScope.drawCloudOrnament(
        center: Offset,
        scale: Float,
        color: Color,
        alpha: Float,
        mirrored: Boolean = false,
    ) {
        val width = 80f * scale
        val height = width * (590f / 1380f)
        drawVectorAsset(
            vector = Icons.Filled.OrnamentCloudRed,
            left = center.x - width / 2f,
            top = center.y - height / 2f,
            width = width,
            height = height,
            alpha = alpha,
            tint = color,
            flipX = mirrored
        )
    }

    /** 回纹角花：使用左上朝向素材，其它三角通过翻转复用。 */
    private fun DrawScope.drawCornerAsset(
        vector: ImageVector,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        xDir: Float,
        yDir: Float,
        alpha: Float,
        tint: Color?,
    ) {
        drawVectorAsset(
            vector = vector,
            left = if (xDir < 0f) x - width else x,
            top = if (yDir < 0f) y - height else y,
            width = width,
            height = height,
            alpha = alpha,
            tint = tint,
            flipX = xDir < 0f,
            flipY = yDir < 0f
        )
    }

    /** 回纹无缝平铺：按素材原始比例横向重复，作为三栏底部的细边框。 */
    private fun DrawScope.drawMeanderStrip(
        left: Float,
        right: Float,
        top: Float,
        height: Float,
        alpha: Float,
        tint: Color?,
    ) {
        val tileWidth = height * (233f / 47f)
        clipRect(left = left, top = top, right = right, bottom = top + height) {
            var x = left
            while (x < right) {
                drawVectorAsset(
                    vector = Icons.Filled.OrnamentMeanderTile,
                    left = x,
                    top = top,
                    width = tileWidth,
                    height = height,
                    alpha = alpha,
                    tint = tint
                )
                x += tileWidth
            }
        }
    }

    // ─── ImageVector 矢量素材绘制（不依赖 Compose 组合）────────────

    private fun DrawScope.drawVectorAsset(
        vector: ImageVector,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        alpha: Float = 1f,
        tint: Color? = null,
        preserveLight: Boolean = false,
        lightTint: Color? = null,
        flipX: Boolean = false,
        flipY: Boolean = false,
    ) {
        translate(
            left = left + if (flipX) width else 0f,
            top = top + if (flipY) height else 0f
        ) {
            scale(
                scaleX = (if (flipX) -width else width) / vector.viewportWidth,
                scaleY = (if (flipY) -height else height) / vector.viewportHeight,
                pivot = Offset.Zero
            ) {
                drawVectorGroup(vector.root, alpha = alpha, tint = tint, preserveLight = preserveLight, lightTint = lightTint)
            }
        }
    }

    private fun DrawScope.drawVectorGroup(
        group: VectorGroup,
        alpha: Float,
        tint: Color?,
        preserveLight: Boolean,
        lightTint: Color?,
    ) {
        translate(left = group.translationX, top = group.translationY) {
            rotate(degrees = group.rotation, pivot = Offset(group.pivotX, group.pivotY)) {
                scale(
                    scaleX = group.scaleX,
                    scaleY = group.scaleY,
                    pivot = Offset(group.pivotX, group.pivotY)
                ) {
                    group.forEach { node ->
                        when (node) {
                            is VectorGroup -> drawVectorGroup(node, alpha, tint, preserveLight, lightTint)
                            is VectorPath -> drawVectorPath(node, alpha, tint, preserveLight, lightTint)
                        }
                    }
                }
            }
        }
    }

    private fun DrawScope.drawVectorPath(
        path: VectorPath,
        alpha: Float,
        tint: Color?,
        preserveLight: Boolean,
        lightTint: Color?,
    ) {
        val composePath = path.pathData.toComposePath()
        composePath.fillType = path.pathFillType

        val fill = path.fill
        if (fill is SolidColor) {
            val color = if (tint != null && (!preserveLight || fill.value.luminance() < 0.8f)) {
                tint
            } else if (preserveLight && fill.value.luminance() >= 0.8f && lightTint != null) {
                lightTint
            } else {
                fill.value
            }
            drawPath(
                path = composePath,
                color = color,
                alpha = alpha * path.fillAlpha
            )
        }

        val stroke = path.stroke
        if (stroke is SolidColor) {
            val color = if (tint != null && (!preserveLight || stroke.value.luminance() < 0.8f)) {
                tint
            } else if (preserveLight && stroke.value.luminance() >= 0.8f && lightTint != null) {
                lightTint
            } else {
                stroke.value
            }
            drawPath(
                path = composePath,
                color = color,
                alpha = alpha * path.strokeAlpha,
                style = Stroke(
                    width = path.strokeLineWidth,
                    cap = path.strokeLineCap,
                    join = path.strokeLineJoin,
                    miter = path.strokeLineMiter
                )
            )
        }
    }

    private fun List<PathNode>.toComposePath(): Path {
        val path = Path()
        var currentX = 0f
        var currentY = 0f
        var contourX = 0f
        var contourY = 0f
        var lastCubicCtrlX = 0f
        var lastCubicCtrlY = 0f
        var lastQuadCtrlX = 0f
        var lastQuadCtrlY = 0f

        fun resetControls() {
            lastCubicCtrlX = currentX
            lastCubicCtrlY = currentY
            lastQuadCtrlX = currentX
            lastQuadCtrlY = currentY
        }

        for (node in this) {
            when (node) {
                is PathNode.MoveTo -> {
                    path.moveTo(node.x, node.y)
                    currentX = node.x
                    currentY = node.y
                    contourX = node.x
                    contourY = node.y
                    resetControls()
                }

                is PathNode.RelativeMoveTo -> {
                    path.relativeMoveTo(node.dx, node.dy)
                    currentX += node.dx
                    currentY += node.dy
                    contourX = currentX
                    contourY = currentY
                    resetControls()
                }

                is PathNode.LineTo -> {
                    path.lineTo(node.x, node.y)
                    currentX = node.x
                    currentY = node.y
                    resetControls()
                }

                is PathNode.RelativeLineTo -> {
                    path.relativeLineTo(node.dx, node.dy)
                    currentX += node.dx
                    currentY += node.dy
                    resetControls()
                }

                is PathNode.HorizontalTo -> {
                    path.lineTo(node.x, currentY)
                    currentX = node.x
                    resetControls()
                }

                is PathNode.RelativeHorizontalTo -> {
                    path.relativeLineTo(node.dx, 0f)
                    currentX += node.dx
                    resetControls()
                }

                is PathNode.VerticalTo -> {
                    path.lineTo(currentX, node.y)
                    currentY = node.y
                    resetControls()
                }

                is PathNode.RelativeVerticalTo -> {
                    path.relativeLineTo(0f, node.dy)
                    currentY += node.dy
                    resetControls()
                }

                is PathNode.CurveTo -> {
                    path.cubicTo(
                        node.x1, node.y1, node.x2, node.y2, node.x3, node.y3
                    )
                    currentX = node.x3
                    currentY = node.y3
                    lastCubicCtrlX = node.x2
                    lastCubicCtrlY = node.y2
                    lastQuadCtrlX = currentX
                    lastQuadCtrlY = currentY
                }

                is PathNode.RelativeCurveTo -> {
                    val c2x = currentX + node.dx2
                    val c2y = currentY + node.dy2
                    val endX = currentX + node.dx3
                    val endY = currentY + node.dy3
                    path.relativeCubicTo(
                        node.dx1, node.dy1, node.dx2, node.dy2, node.dx3, node.dy3
                    )
                    currentX = endX
                    currentY = endY
                    lastCubicCtrlX = c2x
                    lastCubicCtrlY = c2y
                    lastQuadCtrlX = currentX
                    lastQuadCtrlY = currentY
                }

                is PathNode.ReflectiveCurveTo -> {
                    val c1x = 2f * currentX - lastCubicCtrlX
                    val c1y = 2f * currentY - lastCubicCtrlY
                    path.cubicTo(c1x, c1y, node.x1, node.y1, node.x2, node.y2)
                    currentX = node.x2
                    currentY = node.y2
                    lastCubicCtrlX = node.x1
                    lastCubicCtrlY = node.y1
                    lastQuadCtrlX = currentX
                    lastQuadCtrlY = currentY
                }

                is PathNode.RelativeReflectiveCurveTo -> {
                    val c1x = 2f * currentX - lastCubicCtrlX
                    val c1y = 2f * currentY - lastCubicCtrlY
                    val c2x = currentX + node.dx1
                    val c2y = currentY + node.dy1
                    val endX = currentX + node.dx2
                    val endY = currentY + node.dy2
                    path.cubicTo(c1x, c1y, c2x, c2y, endX, endY)
                    currentX = endX
                    currentY = endY
                    lastCubicCtrlX = c2x
                    lastCubicCtrlY = c2y
                    lastQuadCtrlX = currentX
                    lastQuadCtrlY = currentY
                }

                is PathNode.QuadTo -> {
                    path.quadraticBezierTo(node.x1, node.y1, node.x2, node.y2)
                    currentX = node.x2
                    currentY = node.y2
                    lastQuadCtrlX = node.x1
                    lastQuadCtrlY = node.y1
                    lastCubicCtrlX = currentX
                    lastCubicCtrlY = currentY
                }

                is PathNode.RelativeQuadTo -> {
                    val cx = currentX + node.dx1
                    val cy = currentY + node.dy1
                    val endX = currentX + node.dx2
                    val endY = currentY + node.dy2
                    path.quadraticBezierTo(cx, cy, endX, endY)
                    currentX = endX
                    currentY = endY
                    lastQuadCtrlX = cx
                    lastQuadCtrlY = cy
                    lastCubicCtrlX = currentX
                    lastCubicCtrlY = currentY
                }

                is PathNode.ReflectiveQuadTo -> {
                    val cx = 2f * currentX - lastQuadCtrlX
                    val cy = 2f * currentY - lastQuadCtrlY
                    path.quadraticBezierTo(cx, cy, node.x, node.y)
                    currentX = node.x
                    currentY = node.y
                    lastQuadCtrlX = cx
                    lastQuadCtrlY = cy
                    lastCubicCtrlX = currentX
                    lastCubicCtrlY = currentY
                }

                is PathNode.RelativeReflectiveQuadTo -> {
                    val cx = 2f * currentX - lastQuadCtrlX
                    val cy = 2f * currentY - lastQuadCtrlY
                    val endX = currentX + node.dx
                    val endY = currentY + node.dy
                    path.quadraticBezierTo(cx, cy, endX, endY)
                    currentX = endX
                    currentY = endY
                    lastQuadCtrlX = cx
                    lastQuadCtrlY = cy
                    lastCubicCtrlX = currentX
                    lastCubicCtrlY = currentY
                }

                is PathNode.ArcTo -> {
                    appendSvgArc(
                        path = path,
                        startX = currentX,
                        startY = currentY,
                        node = node
                    )
                    currentX = node.arcStartX
                    currentY = node.arcStartY
                    resetControls()
                }

                is PathNode.RelativeArcTo -> {
                    val arc = PathNode.ArcTo(
                        horizontalEllipseRadius = node.horizontalEllipseRadius,
                        verticalEllipseRadius = node.verticalEllipseRadius,
                        theta = node.theta,
                        isMoreThanHalf = node.isMoreThanHalf,
                        isPositiveArc = node.isPositiveArc,
                        arcStartX = currentX + node.arcStartDx,
                        arcStartY = currentY + node.arcStartDy
                    )
                    appendSvgArc(
                        path = path,
                        startX = currentX,
                        startY = currentY,
                        node = arc
                    )
                    currentX = arc.arcStartX
                    currentY = arc.arcStartY
                    resetControls()
                }

                PathNode.Close -> {
                    path.close()
                    currentX = contourX
                    currentY = contourY
                    resetControls()
                }

                else -> Unit
            }
        }
        return path
    }

    /**
     * SVG 椭圆弧转三次贝塞尔：Path.arcTo 只支持未旋转椭圆的 rect/角度形式，
     * 盘长结素材里带 x-axis-rotation 的圆弧需要自己求圆心再分段拟合。
     */
    private fun appendSvgArc(
        path: Path,
        startX: Float,
        startY: Float,
        node: PathNode.ArcTo,
    ) {
        val endX = node.arcStartX
        val endY = node.arcStartY
        var rx = abs(node.horizontalEllipseRadius)
        var ry = abs(node.verticalEllipseRadius)
        if (rx == 0f || ry == 0f || (startX == endX && startY == endY)) {
            path.lineTo(endX, endY)
            return
        }

        val phi = node.theta * PI.toFloat() / 180f
        val cosPhi = cos(phi)
        val sinPhi = sin(phi)
        val dx = (startX - endX) / 2f
        val dy = (startY - endY) / 2f
        val x1p = cosPhi * dx + sinPhi * dy
        val y1p = -sinPhi * dx + cosPhi * dy

        val lambda = (x1p * x1p) / (rx * rx) + (y1p * y1p) / (ry * ry)
        if (lambda > 1f) {
            val scale = sqrt(lambda)
            rx *= scale
            ry *= scale
        }

        val sign = if (node.isMoreThanHalf == node.isPositiveArc) -1f else 1f
        val numerator = rx * rx * ry * ry - rx * rx * y1p * y1p - ry * ry * x1p * x1p
        val denominator = rx * rx * y1p * y1p + ry * ry * x1p * x1p
        val coefficient = sign * sqrt(max(0f, numerator / denominator))
        val cxp = coefficient * (rx * y1p / ry)
        val cyp = -coefficient * (ry * x1p / rx)
        val cx = cosPhi * cxp - sinPhi * cyp + (startX + endX) / 2f
        val cy = sinPhi * cxp + cosPhi * cyp + (startY + endY) / 2f

        val ux = (x1p - cxp) / rx
        val uy = (y1p - cyp) / ry
        val vx = (-x1p - cxp) / rx
        val vy = (-y1p - cyp) / ry
        val startAngle = atan2(uy, ux)
        var delta = atan2(ux * vy - uy * vx, ux * vx + uy * vy)
        if (!node.isPositiveArc && delta > 0f) delta -= 2f * PI.toFloat()
        if (node.isPositiveArc && delta < 0f) delta += 2f * PI.toFloat()

        val segments = max(1, ceil(abs(delta) / (PI.toFloat() / 2f)).toInt())
        val step = delta / segments
        var theta1 = startAngle
        repeat(segments) {
            val theta2 = theta1 + step
            val alpha = 4f / 3f * tan((theta2 - theta1) / 4f)
            val p0 = ellipsePoint(cx, cy, rx, ry, cosPhi, sinPhi, theta1)
            val p3 = ellipsePoint(cx, cy, rx, ry, cosPhi, sinPhi, theta2)
            val d0 = ellipseDerivative(rx, ry, cosPhi, sinPhi, theta1)
            val d1 = ellipseDerivative(rx, ry, cosPhi, sinPhi, theta2)
            path.cubicTo(
                p0.x + alpha * d0.x,
                p0.y + alpha * d0.y,
                p3.x - alpha * d1.x,
                p3.y - alpha * d1.y,
                p3.x,
                p3.y
            )
            theta1 = theta2
        }
    }

    private fun ellipsePoint(
        cx: Float,
        cy: Float,
        rx: Float,
        ry: Float,
        cosPhi: Float,
        sinPhi: Float,
        theta: Float,
    ): Offset {
        val x = rx * cos(theta)
        val y = ry * sin(theta)
        return Offset(
            cx + cosPhi * x - sinPhi * y,
            cy + sinPhi * x + cosPhi * y
        )
    }

    private fun ellipseDerivative(
        rx: Float,
        ry: Float,
        cosPhi: Float,
        sinPhi: Float,
        theta: Float,
    ): Offset {
        val x = -rx * sin(theta)
        val y = ry * cos(theta)
        return Offset(
            cosPhi * x - sinPhi * y,
            sinPhi * x + cosPhi * y
        )
    }

    // ─── 文字 ────────────────────────────────────────────────────────

    private fun serif(
        size: Float,
        color: Color,
        weight: FontWeight = FontWeight.Normal,
        letterSpacing: Float = 0f,
    ): TextStyle = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = size.sp,
        fontWeight = weight,
        color = color,
        letterSpacing = letterSpacing.sp
    )

    private fun TextMeasurer.oneLine(
        text: String,
        style: TextStyle,
    ) = measure(
        text = AnnotatedString(text),
        style = style,
        overflow = TextOverflow.Clip,
        softWrap = false,
        maxLines = 1
    )

    private fun DrawScope.drawLeft(
        layout: TextLayoutResult,
        x: Float,
        baselineY: Float,
    ) {
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(x, baselineY - layout.firstBaseline)
        )
    }

    private fun DrawScope.drawRight(
        layout: TextLayoutResult,
        right: Float,
        baselineY: Float,
    ) {
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(
                x = right - layout.size.width,
                y = baselineY - layout.firstBaseline
            )
        )
    }

    private fun DrawScope.drawCentered(
        layout: TextLayoutResult,
        centerX: Float,
        baselineY: Float,
    ) {
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(
                x = centerX - layout.size.width / 2f,
                y = baselineY - layout.firstBaseline
            )
        )
    }

    private fun DrawScope.drawCenteredInBox(
        layout: TextLayoutResult,
        center: Offset,
    ) {
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(
                x = center.x - layout.size.width / 2f,
                y = center.y - layout.size.height / 2f
            )
        )
    }
}
