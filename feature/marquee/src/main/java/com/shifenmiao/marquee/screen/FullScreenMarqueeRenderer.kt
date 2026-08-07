package com.shifenmiao.marquee.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.model.marquee.MarqueePresentationMode
import com.shifenmiao.model.marquee.MarqueeSettings
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

@Composable
internal fun FullScreenMarqueeRenderer(
    marqueeSettings: MarqueeSettings,
    fontSizeSp: Float,
    blinkAlpha: Float,
    modifier: Modifier = Modifier,
) {
    val text = marqueeSettings.marqueeText

    val textColor = remember(marqueeSettings.marqueeTextColor) {
        Color(marqueeSettings.marqueeTextColor)
    }

    val fontWeight = if (marqueeSettings.marqueeBoldEnabled) {
        FontWeight.Bold
    } else {
        FontWeight.Normal
    }

    // 字符间距 (em 单位转 sp)
    val letterSpacingSp = (marqueeSettings.letterSpacing).sp

    val commonGraphicsModifier = Modifier.graphicsLayer {
        scaleX = if (marqueeSettings.marqueeMirrorEnabled) -1f else 1f
        alpha = if (marqueeSettings.marqueeBlinkEnabled) blinkAlpha else 1f
    }

    // Use full available height; vertical optical centering handles font metrics offsets.
    val safeAreaModifier = Modifier.fillMaxSize()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (marqueeSettings.presentationMode) {
            MarqueePresentationMode.ClassicMarquee -> {
                Box(
                    modifier = commonGraphicsModifier.then(safeAreaModifier),
                    contentAlignment = Alignment.Center
                ) {
                    OpticalCenteredText(
                        text = text,
                        modifier = Modifier
                            .fillMaxSize()
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                spacing = MarqueeSpacing(marqueeSettings.marqueeTextSpacing.dp),
                                velocity = marqueeSettings.marqueeTextVelocity.dp,
                                repeatDelayMillis = marqueeSettings.marqueeTextRepeatDelayMillis,
                            ),
                        color = textColor,
                        fontSize = fontSizeSp.sp,
                        fontWeight = fontWeight,
                        textAlign = TextAlign.Center,
                        softWrap = false,
                        letterSpacing = letterSpacingSp,
                    )
                }
            }

            MarqueePresentationMode.Typewriter -> {
                Box(
                    modifier = commonGraphicsModifier.then(safeAreaModifier),
                    contentAlignment = Alignment.Center
                ) {
                    TypewriterFullScreenText(
                        text = text,
                        modifier = Modifier.fillMaxSize(),
                        color = textColor,
                        fontSizeSp = fontSizeSp,
                        fontWeight = fontWeight,
                        charsPerSecond = marqueeSettings.typewriterCharsPerSecond,
                        letterSpacing = letterSpacingSp,
                    )
                }
            }

            MarqueePresentationMode.OneCharPerScreen -> {
                Box(
                    modifier = commonGraphicsModifier.then(safeAreaModifier),
                    contentAlignment = Alignment.Center
                ) {
                    OneCharPerScreenText(
                        text = text,
                        modifier = Modifier.fillMaxSize(),
                        color = textColor,
                        fontSizeSp = fontSizeSp,
                        fontWeight = fontWeight,
                        holdMillis = marqueeSettings.oneCharHoldMillis,
                        letterSpacing = letterSpacingSp,
                    )
                }
            }

            MarqueePresentationMode.Paged -> {
                Box(
                    modifier = commonGraphicsModifier.then(safeAreaModifier),
                    contentAlignment = Alignment.Center
                ) {
                    PagedFullScreenText(
                        text = text,
                        modifier = Modifier.fillMaxSize(),
                        color = textColor,
                        fontSizeSp = fontSizeSp,
                        fontWeight = fontWeight,
                        autoAdvance = marqueeSettings.pagedAutoAdvanceEnabled,
                        intervalMillis = marqueeSettings.pagedIntervalMillis,
                        letterSpacing = letterSpacingSp,
                    )
                }
            }

            MarqueePresentationMode.Clock -> {
                Box(
                    modifier = commonGraphicsModifier.then(safeAreaModifier),
                    contentAlignment = Alignment.Center
                ) {
                    ClockFullScreenText(
                        modifier = Modifier.fillMaxSize(),
                        color = textColor,
                        fontSizeSp = fontSizeSp,
                        fontWeight = fontWeight,
                        showSeconds = marqueeSettings.clockShowSeconds,
                        showDate = marqueeSettings.clockShowDate,
                        use24Hour = marqueeSettings.clockUse24Hour,
                        letterSpacing = letterSpacingSp,
                    )
                }
            }

            MarqueePresentationMode.Countdown -> {
                Box(
                    modifier = commonGraphicsModifier.then(safeAreaModifier),
                    contentAlignment = Alignment.Center
                ) {
                    CountdownFullScreenText(
                        modifier = Modifier.fillMaxSize(),
                        color = textColor,
                        fontSizeSp = fontSizeSp,
                        fontWeight = fontWeight,
                        initialSeconds = marqueeSettings.countdownSeconds,
                        showMillis = marqueeSettings.countdownShowMillis,
                        letterSpacing = letterSpacingSp,
                    )
                }
            }

            MarqueePresentationMode.Bouncing -> {
                Box(
                    modifier = commonGraphicsModifier.then(safeAreaModifier)
                ) {
                    BouncingFullScreenText(
                        text = text,
                        modifier = Modifier.fillMaxSize(),
                        color = textColor,
                        fontSizeSp = fontSizeSp,
                        fontWeight = fontWeight,
                        speed = marqueeSettings.bouncingSpeed,
                        letterSpacing = letterSpacingSp,
                    )
                }
            }
        }
    }
}@Composable
private fun TypewriterFullScreenText(
    text: String,
    modifier: Modifier,
    color: Color,
    fontSizeSp: Float,
    fontWeight: FontWeight,
    charsPerSecond: Float,
    letterSpacing: TextUnit = TextUnit.Unspecified,
) {
    val cps = charsPerSecond.coerceIn(1f, 120f)

    var index by remember(text, cps) { mutableIntStateOf(0) }

    LaunchedEffect(text, cps) {
        index = 0
        if (text.isEmpty()) return@LaunchedEffect
        val delayMillis = (1000f / cps).toLong().coerceAtLeast(8L)
        while (true) {
            if (index < text.length) {
                index++
            } else {
                // small pause, then loop
                delay(450)
                index = 0
            }
            delay(delayMillis)
        }
    }

    val shown by remember(text, index) {
        derivedStateOf { text.take(index.coerceIn(0, text.length)) }
    }

    OpticalCenteredText(
        text = shown,
        modifier = modifier,
        color = color,
        fontSize = fontSizeSp.sp,
        fontWeight = fontWeight,
        textAlign = TextAlign.Center,
        softWrap = true,
        maxLines = Int.MAX_VALUE,
        letterSpacing = letterSpacing,
    )
}

@Composable
private fun OneCharPerScreenText(
    text: String,
    modifier: Modifier,
    color: Color,
    fontSizeSp: Float,
    fontWeight: FontWeight,
    holdMillis: Int,
    letterSpacing: TextUnit = TextUnit.Unspecified,
) {
    val hold = holdMillis.coerceIn(120, 5000)

    // naive split: keep it safe for Chinese / emoji by iterating code points
    val codePoints = remember(text) {
        buildList {
            var i = 0
            while (i < text.length) {
                val cp = text.codePointAt(i)
                add(String(Character.toChars(cp)))
                i += Character.charCount(cp)
            }
        }.ifEmpty { listOf("") }
    }

    var idx by remember(codePoints, hold) { mutableIntStateOf(0) }

    LaunchedEffect(codePoints, hold) {
        idx = 0
        if (codePoints.isEmpty()) return@LaunchedEffect
        while (true) {
            delay(hold.toLong())
            idx = (idx + 1) % codePoints.size
        }
    }

    val current = codePoints.getOrElse(idx) { "" }

    AnimatedContent(
        targetState = current,
        transitionSpec = {
            (fadeIn(tween(160, easing = LinearEasing)) togetherWith fadeOut(tween(160, easing = LinearEasing)))
        },
        label = "oneChar"
    ) { value ->
        OpticalCenteredText(
            text = value,
            modifier = modifier,
            color = color,
            fontSize = fontSizeSp.sp,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            softWrap = false,
            maxLines = 1,
            letterSpacing = letterSpacing,
        )
    }
}

@Composable
private fun PagedFullScreenText(
    text: String,
    modifier: Modifier,
    color: Color,
    fontSizeSp: Float,
    fontWeight: FontWeight,
    autoAdvance: Boolean,
    intervalMillis: Int,
    letterSpacing: TextUnit = TextUnit.Unspecified,
) {
    val interval = intervalMillis.coerceIn(600, 30_000)

    BoxWithConstraints(modifier = modifier) {
        val measurer = rememberTextMeasurer()

        // Available space for text (we keep it simple here: no extra padding; can add later)
        val maxWidthPx = constraints.maxWidth
        val maxHeightPx = constraints.maxHeight

        val style = remember(color, fontSizeSp, fontWeight, letterSpacing) {
            TextStyle(
                color = color,
                fontSize = fontSizeSp.sp,
                fontWeight = fontWeight,
                textAlign = TextAlign.Center,
                letterSpacing = letterSpacing,
            )
        }

        val pages = remember(text, maxWidthPx, maxHeightPx, style) {
            paginateTextByHeight(
                measurer = measurer,
                text = text,
                style = style,
                maxWidthPx = maxWidthPx,
                maxHeightPx = maxHeightPx
            ).ifEmpty { listOf("") }
        }

        var pageIndex by remember(pages, autoAdvance, interval) { mutableIntStateOf(0) }

        LaunchedEffect(pages, autoAdvance, interval) {
            pageIndex = 0
            if (!autoAdvance) return@LaunchedEffect
            while (true) {
                delay(interval.toLong())
                pageIndex = (pageIndex + 1) % pages.size
            }
        }

        val current = pages.getOrElse(pageIndex) { "" }

        PagedOverlay(
            current = current,
            color = color,
            fontSizeSp = fontSizeSp,
            fontWeight = fontWeight,
            letterSpacing = letterSpacing,
        )
    }
}

@Composable
private fun PagedOverlay(
    current: String,
    color: Color,
    fontSizeSp: Float,
    fontWeight: FontWeight,
    letterSpacing: TextUnit = TextUnit.Unspecified,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedContent(
            targetState = current,
            transitionSpec = {
                (fadeIn(tween(200, easing = LinearEasing)) togetherWith fadeOut(
                    tween(
                        200,
                        easing = LinearEasing
                    )
                ))
            },
            label = "paged"
        ) { value ->
            OpticalCenteredText(
                text = value,
                modifier = Modifier.fillMaxWidth(),
                color = color,
                fontSize = fontSizeSp.sp,
                fontWeight = fontWeight,
                textAlign = TextAlign.Center,
                softWrap = true,
                maxLines = Int.MAX_VALUE,
                overflow = TextOverflow.Clip,
                letterSpacing = letterSpacing,
            )
        }
    }
}

private fun paginateTextByHeight(
    measurer: androidx.compose.ui.text.TextMeasurer,
    text: String,
    style: TextStyle,
    maxWidthPx: Int,
    maxHeightPx: Int,
): List<String> {
    if (text.isBlank() || maxWidthPx <= 0 || maxHeightPx <= 0) return emptyList()

    // Normalize whitespace a bit but keep intentional line breaks.
    val normalized = text.replace("\r\n", "\n").trimEnd()

    // We paginate by growing a substring until it exceeds height.
    // To keep performance acceptable, we use a binary search per page.
    val out = ArrayList<String>()
    var start = 0

    fun fits(endExclusive: Int): Boolean {
        if (endExclusive <= start) return true
        val sub = normalized.substring(start, endExclusive)
        val res = measurer.measure(
            text = sub,
            style = style,
            softWrap = true,
            overflow = TextOverflow.Clip,
            maxLines = Int.MAX_VALUE,
            constraints = androidx.compose.ui.unit.Constraints(
                maxWidth = maxWidthPx,
                maxHeight = Int.MAX_VALUE
            )
        )
        return res.size.height <= maxHeightPx
    }

    while (start < normalized.length) {
        var low = start + 1
        var high = normalized.length
        var best = start

        while (low <= high) {
            val mid = (low + high) ushr 1
            if (fits(mid)) {
                best = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }

        if (best <= start) {
            // Even a single char doesn't fit. Fallback: force progress by one code unit.
            best = (start + 1).coerceAtMost(normalized.length)
        }

        // Try to break at a nicer boundary near the end (newline or space), but don't get stuck.
        var end = best
        val windowStart = (start).coerceAtLeast(0)
        val windowEnd = end.coerceAtMost(normalized.length)
        val window = normalized.substring(windowStart, windowEnd)
        val lastNewLine = window.lastIndexOf('\n')
        val lastSpace = window.lastIndexOf(' ')
        val candidate = maxOf(lastNewLine, lastSpace)
        if (candidate in 1 until window.length - 1) {
            end = windowStart + candidate + 1
            // Ensure the nicer break still fits; otherwise keep best.
            if (!fits(end)) end = best
        }

        val page = normalized.substring(start, end).trim()
        if (page.isNotEmpty()) out.add(page)
        start = end
    }

    return out
}

/**
 * 时钟全屏显示组件
 */
@Composable
private fun ClockFullScreenText(
    modifier: Modifier,
    color: Color,
    fontSizeSp: Float,
    fontWeight: FontWeight,
    showSeconds: Boolean,
    showDate: Boolean,
    use24Hour: Boolean,
    letterSpacing: TextUnit = TextUnit.Unspecified,
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(showSeconds) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(if (showSeconds) 1000L else 60_000L)
        }
    }

    val calendar = remember(currentTime) {
        java.util.Calendar.getInstance().apply { timeInMillis = currentTime }
    }

    val timeText = remember(currentTime, showSeconds, use24Hour) {
        val hour = if (use24Hour) {
            calendar.get(java.util.Calendar.HOUR_OF_DAY)
        } else {
            calendar.get(java.util.Calendar.HOUR).let { if (it == 0) 12 else it }
        }
        val minute = calendar.get(java.util.Calendar.MINUTE)
        val second = calendar.get(java.util.Calendar.SECOND)

        if (showSeconds) {
            String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second)
        } else {
            String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute)
        }
    }

    val dateText = remember(currentTime) {
        if (showDate) {
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH) + 1
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            String.format(java.util.Locale.getDefault(), "%d年%d月%d日", year, month, day)
        } else ""
    }

    val amPmText = remember(currentTime, use24Hour) {
        if (!use24Hour) {
            if (calendar.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM) "AM" else "PM"
        } else ""
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (showDate) {
            Text(
                text = dateText,
                color = color.copy(alpha = 0.7f),
                fontSize = (fontSizeSp * 0.3f).sp,
                fontWeight = fontWeight,
                textAlign = TextAlign.Center,
                letterSpacing = letterSpacing,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = timeText,
            color = color,
            fontSize = fontSizeSp.sp,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            letterSpacing = letterSpacing,
        )

        if (!use24Hour) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amPmText,
                color = color.copy(alpha = 0.6f),
                fontSize = (fontSizeSp * 0.2f).sp,
                fontWeight = fontWeight,
                textAlign = TextAlign.Center,
                letterSpacing = letterSpacing,
            )
        }
    }
}

/**
 * 倒计时全屏显示组件
 */
@Composable
private fun CountdownFullScreenText(
    modifier: Modifier,
    color: Color,
    fontSizeSp: Float,
    fontWeight: FontWeight,
    initialSeconds: Int,
    showMillis: Boolean,
    letterSpacing: TextUnit = TextUnit.Unspecified,
) {
    var remainingMs by remember(initialSeconds) { mutableLongStateOf(initialSeconds * 1000L) }
    var isRunning by remember { mutableStateOf(true) }

    LaunchedEffect(initialSeconds, isRunning) {
        if (!isRunning) return@LaunchedEffect
        val startTime = System.currentTimeMillis()
        val endTime = startTime + initialSeconds * 1000L

        while (isRunning) {
            val now = System.currentTimeMillis()
            remainingMs = (endTime - now).coerceAtLeast(0L)
            if (remainingMs <= 0) {
                isRunning = false
                break
            }
            delay(if (showMillis) 50L else 100L)
        }
    }

    val countdownText = remember(remainingMs, showMillis) {
        val totalSeconds = remainingMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val millis = (remainingMs % 1000) / 10

        if (showMillis) {
            String.format(java.util.Locale.getDefault(), "%02d:%02d.%02d", minutes, seconds, millis)
        } else {
            String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    val isFinished = remainingMs <= 0

    AnimatedContent(
        targetState = isFinished,
        transitionSpec = {
            (fadeIn(tween(300, easing = LinearEasing)) togetherWith fadeOut(tween(300, easing = LinearEasing)))
        },
        label = "countdownFinish"
    ) { finished ->
        if (finished) {
            // 倒计时结束显示
            OpticalCenteredText(
                text = "00:00",
                modifier = modifier,
                color = color,
                fontSize = fontSizeSp.sp,
                fontWeight = fontWeight,
                textAlign = TextAlign.Center,
                softWrap = false,
                maxLines = 1,
                letterSpacing = letterSpacing,
            )
        } else {
            OpticalCenteredText(
                text = countdownText,
                modifier = modifier,
                color = color,
                fontSize = fontSizeSp.sp,
                fontWeight = fontWeight,
                textAlign = TextAlign.Center,
                softWrap = false,
                maxLines = 1,
                letterSpacing = letterSpacing,
            )
        }
    }
}

/**
 * 弹跳模式全屏显示组件 (DVD Screensaver style)
 */
@Composable
private fun BouncingFullScreenText(
    text: String,
    modifier: Modifier,
    color: Color,
    fontSizeSp: Float,
    fontWeight: FontWeight,
    speed: Float = 6f,
    letterSpacing: TextUnit = TextUnit.Unspecified,
) {
    BoxWithConstraints(modifier = modifier) {
        val containerWidth = constraints.maxWidth.toFloat()
        val containerHeight = constraints.maxHeight.toFloat()

        var textWidth by remember { mutableFloatStateOf(0f) }
        var textHeight by remember { mutableFloatStateOf(0f) }

        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }

        // 使用 State 来保存速度，初始值使用传入的 speed 参数
        var velocityX by remember(speed) { mutableFloatStateOf(speed) }
        var velocityY by remember(speed) { mutableFloatStateOf(speed) }

        // 标记是否已初始化位置
        var initialized by remember { mutableStateOf(false) }

        // 动画循环
        LaunchedEffect(Unit) {
            while (true) {
                withFrameNanos { _ ->
                    // 等待文字尺寸测量完成
                    if (textWidth <= 0 || textHeight <= 0) return@withFrameNanos
                    if (containerWidth <= 0 || containerHeight <= 0) return@withFrameNanos

                    // 初始化位置（只执行一次）
                    if (!initialized) {
                        offsetX = (containerWidth - textWidth) / 2f
                        offsetY = (containerHeight - textHeight) / 2f
                        initialized = true
                    }

                    // 更新位置
                    offsetX += velocityX
                    offsetY += velocityY

                    // 计算可移动范围
                    val maxX = (containerWidth - textWidth).coerceAtLeast(0f)
                    val maxY = (containerHeight - textHeight).coerceAtLeast(0f)

                    // Bounce X
                    if (offsetX <= 0) {
                        offsetX = 0f
                        velocityX = kotlin.math.abs(velocityX)
                    } else if (offsetX >= maxX) {
                        offsetX = maxX
                        velocityX = -kotlin.math.abs(velocityX)
                    }

                    // Bounce Y
                    if (offsetY <= 0) {
                        offsetY = 0f
                        velocityY = kotlin.math.abs(velocityY)
                    } else if (offsetY >= maxY) {
                        offsetY = maxY
                        velocityY = -kotlin.math.abs(velocityY)
                    }
                }
            }
        }

        // 使用普通 Text + offset，不使用 OpticalCenteredText（它会强制居中）
        Text(
            text = text,
            modifier = Modifier
                .onSizeChanged {
                    textWidth = it.width.toFloat()
                    textHeight = it.height.toFloat()
                }
                .offset {
                    IntOffset(offsetX.roundToInt(), offsetY.roundToInt())
                },
            color = color,
            fontSize = fontSizeSp.sp,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            softWrap = false,
            maxLines = 1,
            letterSpacing = letterSpacing,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                letterSpacing = letterSpacing,
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both
                )
            )
        )
    }
}
