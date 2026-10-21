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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
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

    /** 项目内置的实心生肖马（Apache-2.0，见 core/resources/.../Horse.kt），24×24。 */
    private const val HORSE_PATH =
        "M22 6 v3.5 l-1.5 .5 l-1.54 -2.46 c-.13 -.21 -.46 -.12 -.46 .13 v3.58 " +
            "c0 .98 -.39 1.86 -1 2.53 V21 H15 v-6 h-.25 c-.21 0 -.42 -.03 -.62 -.06 " +
            "l-4.44 -.74 l-1.12 2.01 l.96 4.79 H7 l-1 -4.75 c-.03 -.3 0 -.6 .16 -.86 " +
            "l1.02 -1.81 a3.27 3.27 0 0 1 -1.68 -2.77 c-.04 .15 -.06 .37 -.03 .69 " +
            "c.03 .44 .14 1.09 .07 1.81 c-.04 .72 -.37 1.46 -.79 1.95 " +
            "c-.43 .49 -.9 .83 -1.4 1.09 l-.7 -.7 c.19 -.47 .38 -.89 .42 -1.28 " +
            "c.06 -.37 -.01 -.67 -.12 -.94 l-.53 -1.13 c-.21 -.51 -.47 -1.25 -.42 -2.12 " +
            "c.03 -.85 .5 -1.96 1.39 -2.57 c.9 -.61 1.87 -.69 2.66 -.53 " +
            "c.5 .1 1.01 .34 1.45 .68 c.37 -.17 .8 -.26 1.25 -.26 h5.75 V7 " +
            "c0 -2.21 1.79 -4 4 -4 H22 l-.89 1.34 c.54 .36 .89 .97 .89 1.66"

    private val horsePath: Path? by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        runCatching { PathParser().parsePathString(HORSE_PATH).toPath() }.getOrNull()
    }

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
            alpha = 0.28f
        )
        drawCloudOrnament(
            center = Offset(954f, 154f),
            scale = 1.4f,
            color = accent,
            alpha = 0.28f,
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
            alpha = 0.3f
        )
        drawCloudOrnament(
            center = Offset(588f, 292f),
            scale = 0.55f,
            color = accent,
            alpha = 0.3f,
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
        drawCornerBracket(246f, 326f, 1f, 1f, 25f, accent.copy(alpha = 0.62f))
        drawCornerBracket(834f, 326f, -1f, 1f, 25f, accent.copy(alpha = 0.62f))
        drawCornerBracket(246f, 826f, 1f, -1f, 25f, accent.copy(alpha = 0.62f))
        drawCornerBracket(834f, 826f, -1f, -1f, 25f, accent.copy(alpha = 0.62f))
        drawCloudOrnament(
            center = Offset(195f, 798f),
            scale = 0.7f,
            color = accent,
            alpha = 0.24f
        )
        drawCloudOrnament(
            center = Offset(885f, 798f),
            scale = 0.7f,
            color = accent,
            alpha = 0.24f,
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

        val verticalStyle = serif(size = 47f, color = accent)
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

        val horse = horsePath
        if (horse != null) {
            drawHorse(
                path = horse,
                center = Offset(150f, 790f),
                size = 88f,
                color = accent
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
                color = accent,
                letterSpacing = 10.sp
            )
        )
        drawCentered(weekdayEn, centerX = 930f, baselineY = 636f)
        drawKnot(center = Offset(930f, 762f), color = accent)
    }

    private fun DrawScope.drawHorse(
        path: Path,
        center: Offset,
        size: Float,
        color: Color,
    ) {
        val scale = size / 24f
        translate(center.x - size / 2f, center.y - size / 2f) {
            scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero) {
                drawPath(path, color)
            }
        }
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

    private fun DrawScope.drawKnot(center: Offset, color: Color) {
        rotate(degrees = 45f, pivot = center) {
            drawRoundRect(
                color = color,
                topLeft = Offset(center.x - 34f, center.y - 34f),
                size = Size(68f, 68f),
                cornerRadius = CornerRadius(6f),
                style = Stroke(width = 3f)
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(center.x - 18f, center.y - 18f),
                size = Size(36f, 36f),
                cornerRadius = CornerRadius(4f),
                style = Stroke(width = 3f)
            )
        }
        drawCircle(color = color, radius = 5f, center = center)
        drawLine(
            color = color,
            start = Offset(center.x, center.y + 50f),
            end = Offset(center.x, center.y + 72f),
            strokeWidth = 3f
        )
        val tassel = Path().apply {
            moveTo(center.x - 7f, center.y + 72f)
            lineTo(center.x, center.y + 84f)
            lineTo(center.x + 7f, center.y + 72f)
            close()
        }
        drawPath(tassel, color)
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

        drawLunarBracket(left, top, xDir = 1f, yDir = 1f, color = accent)
        drawLunarBracket(right, top, xDir = -1f, yDir = 1f, color = accent)
        drawLunarBracket(left, bottom, xDir = 1f, yDir = -1f, color = accent)
        drawLunarBracket(right, bottom, xDir = -1f, yDir = -1f, color = accent)

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
            center = Offset(178f, 912f),
            scale = 1.2f,
            color = accent,
            alpha = 0.3f
        )
        drawCloudOrnament(
            center = Offset(902f, 912f),
            scale = 1.2f,
            color = accent,
            alpha = 0.3f,
            mirrored = true
        )
        drawCentered(text, centerX = WIDTH / 2f, baselineY = 933f)
    }

    /**
     * 回纹式折角：外层 L + 内层 L + 端部短钩，比一条简单折线更像传统角花。
     */
    private fun DrawScope.drawLunarBracket(
        x: Float,
        y: Float,
        xDir: Float,
        yDir: Float,
        color: Color,
    ) {
        val stroke = 3.5f
        drawLine(
            color = color,
            start = Offset(x, y + yDir * 46f),
            end = Offset(x, y),
            strokeWidth = stroke,
            cap = StrokeCap.Square
        )
        drawLine(
            color = color,
            start = Offset(x, y),
            end = Offset(x + xDir * 46f, y),
            strokeWidth = stroke,
            cap = StrokeCap.Square
        )

        val inner = 13f
        drawLine(
            color = color,
            start = Offset(x + xDir * inner, y + yDir * 34f),
            end = Offset(x + xDir * inner, y + yDir * inner),
            strokeWidth = stroke * 0.7f,
            cap = StrokeCap.Square
        )
        drawLine(
            color = color,
            start = Offset(x + xDir * inner, y + yDir * inner),
            end = Offset(x + xDir * 34f, y + yDir * inner),
            strokeWidth = stroke * 0.7f,
            cap = StrokeCap.Square
        )

        drawLine(
            color = color,
            start = Offset(x, y + yDir * 56f),
            end = Offset(x, y + yDir * 46f),
            strokeWidth = stroke * 0.7f,
            cap = StrokeCap.Square
        )
        drawLine(
            color = color,
            start = Offset(x + xDir * 56f, y),
            end = Offset(x + xDir * 46f, y),
            strokeWidth = stroke * 0.7f,
            cap = StrokeCap.Square
        )
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

        drawCornerBracket(left + 12f, top + 12f, 1f, 1f, 26f, accent.copy(alpha = 0.6f))
        drawCornerBracket(right - 12f, top + 12f, -1f, 1f, 26f, accent.copy(alpha = 0.6f))
        drawCornerBracket(left + 12f, bottom - 12f, 1f, -1f, 26f, accent.copy(alpha = 0.6f))
        drawCornerBracket(right - 12f, bottom - 12f, -1f, -1f, 26f, accent.copy(alpha = 0.6f))

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
            fill = palette.yiSeal,
            onFill = palette.onYiSeal,
            palette = palette,
            textMeasurer = textMeasurer
        )
        drawSeal(
            center = Offset(centers[2], 1050f),
            glyph = data.labels.jiSeal,
            fill = palette.jiSeal,
            onFill = palette.onJiSeal,
            palette = palette,
            textMeasurer = textMeasurer
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
                baselineY = 1170f
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
                baselineY = 1132f + (index / 2) * 40f
            )
        }
        if (shown.size % 2 == 1) {
            // 奇数条时最后一条居中，避免出现一边三行、一边两行的缺口
            drawItem(
                item = shown.last(),
                x = centerX,
                baselineY = 1132f + (paired / 2) * 40f
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

        val coinCenter = Offset(centerX, 1284f)
        drawCircle(
            color = accent.copy(alpha = 0.42f),
            radius = 18f,
            center = coinCenter,
            style = Stroke(width = 1.5f)
        )
        drawCircle(
            color = accent.copy(alpha = 0.18f),
            radius = 12f,
            center = coinCenter,
            style = Stroke(width = 1f)
        )
        drawRect(
            color = accent.copy(alpha = 0.42f),
            topLeft = Offset(centerX - 5f, 1279f),
            size = Size(10f, 10f),
            style = Stroke(width = 1.2f)
        )
    }

    private fun DrawScope.drawSeal(
        center: Offset,
        glyph: String,
        fill: Color,
        onFill: Color,
        palette: CalendarSharePalette,
        textMeasurer: TextMeasurer,
    ) {
        val badge = buildScallopedBadge(radius = 46f, lobes = 12, lobeRadius = 10f)

        translate(center.x, center.y) {
            drawPath(badge, fill)
            drawCircle(
                color = palette.paper,
                radius = 37f,
                center = Offset.Zero,
                style = Stroke(width = 2f)
            )
        }

        val glyphLayout = textMeasurer.oneLine(
            text = glyph,
            style = serif(size = 52f, color = onFill)
        )
        drawCenteredInBox(glyphLayout, center)

        val cloud = buildCloudPath()
        drawSealCloud(
            center = Offset(center.x - 66f, center.y + 6f),
            cloud = cloud,
            fill = palette.paper,
            outline = fill,
            mirrored = false
        )
        drawSealCloud(
            center = Offset(center.x + 66f, center.y + 6f),
            cloud = cloud,
            fill = palette.paper,
            outline = fill,
            mirrored = true
        )
    }

    private fun DrawScope.drawSealCloud(
        center: Offset,
        cloud: Path,
        fill: Color,
        outline: Color,
        mirrored: Boolean,
    ) {
        translate(center.x, center.y) {
            scale(
                scaleX = if (mirrored) -1f else 1f,
                scaleY = 1f,
                pivot = Offset.Zero
            ) {
                drawPath(cloud, fill)
                drawPath(cloud, outline, style = Stroke(width = 2.6f))
                drawPath(
                    path = cloudCurlPath(),
                    color = outline,
                    style = Stroke(width = 2.2f, cap = StrokeCap.Round)
                )
            }
        }
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

        drawLeft(
            layout = textMeasurer.oneLine(
                text = labels.weatherTitle,
                style = serif(20f, palette.accent, FontWeight.SemiBold, letterSpacing = 4f)
            ),
            x = left + 22f,
            baselineY = top + 34f
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

        drawCloudOrnament(
            center = Offset(left + 28f, top + WEATHER_CARD_HEIGHT - 14f),
            scale = 0.6f,
            color = palette.accent,
            alpha = 0.22f
        )
        drawCloudOrnament(
            center = Offset(right - 28f, top + WEATHER_CARD_HEIGHT - 14f),
            scale = 0.6f,
            color = palette.accent,
            alpha = 0.22f,
            mirrored = true
        )
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
            alpha = 0.22f
        )
        drawCloudOrnament(
            center = Offset(954f, layout.brandBaseline - 8f),
            scale = 1.1f,
            color = accent,
            alpha = 0.22f,
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

    private fun cloudCurlPath(): Path = Path().apply {
        moveTo(-24f, 5f)
        cubicTo(-34f, 5f, -38f, -2f, -34f, -8f)
        cubicTo(-30f, -15f, -20f, -12f, -22f, -4f)
        cubicTo(-23f, 1f, -29f, 2f, -30f, -2f)
    }

    /** 低饱和度的祥云装饰：淡填充 + 线稿 + 卷云尾。 */
    private fun DrawScope.drawCloudOrnament(
        center: Offset,
        scale: Float,
        color: Color,
        alpha: Float,
        mirrored: Boolean = false,
    ) {
        val cloud = buildCloudPath()
        translate(center.x, center.y) {
            scale(
                scaleX = if (mirrored) -scale else scale,
                scaleY = scale,
                pivot = Offset.Zero
            ) {
                drawPath(cloud, color.copy(alpha = alpha * 0.16f))
                drawPath(
                    path = cloud,
                    color = color.copy(alpha = alpha),
                    style = Stroke(width = 1.6f)
                )
                drawPath(
                    path = cloudCurlPath(),
                    color = color.copy(alpha = alpha),
                    style = Stroke(width = 1.4f, cap = StrokeCap.Round)
                )
            }
        }
    }

    /** 小回纹角花：外 L + 内 L，用在主日期框和三栏黄历的四角。 */
    private fun DrawScope.drawCornerBracket(
        x: Float,
        y: Float,
        xDir: Float,
        yDir: Float,
        size: Float,
        color: Color,
    ) {
        drawLine(
            color = color,
            start = Offset(x + xDir * size, y),
            end = Offset(x, y),
            strokeWidth = 2f,
            cap = StrokeCap.Square
        )
        drawLine(
            color = color,
            start = Offset(x, y),
            end = Offset(x, y + yDir * size),
            strokeWidth = 2f,
            cap = StrokeCap.Square
        )
        val inset = size * 0.32f
        val inner = size * 0.72f
        drawLine(
            color = color,
            start = Offset(x + xDir * inner, y + yDir * inset),
            end = Offset(x + xDir * inset, y + yDir * inset),
            strokeWidth = 1.1f,
            cap = StrokeCap.Square
        )
        drawLine(
            color = color,
            start = Offset(x + xDir * inset, y + yDir * inset),
            end = Offset(x + xDir * inset, y + yDir * inner),
            strokeWidth = 1.1f,
            cap = StrokeCap.Square
        )
    }

    private fun Path.unionWith(other: Path): Path {
        val result = Path()
        return if (result.op(this, other, PathOperation.Union)) result else this
    }

    /**
     * 花瓣边印章：中心圆 + 12 个环绕小圆并集。
     */
    private fun buildScallopedBadge(
        radius: Float,
        lobes: Int,
        lobeRadius: Float,
    ): Path {
        var badge = Path().apply {
            addOval(Rect(center = Offset.Zero, radius = radius))
        }
        repeat(lobes) { index ->
            val angle = index * 2.0 * PI / lobes
            val lobe = Path().apply {
                addOval(
                    Rect(
                        center = Offset(
                            (cos(angle) * radius).toFloat(),
                            (sin(angle) * radius).toFloat()
                        ),
                        radius = lobeRadius
                    )
                )
            }
            badge = badge.unionWith(lobe)
        }
        return badge
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
