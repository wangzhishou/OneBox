package com.shifenmiao.marquee.screen.components

import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.core.R
import com.shifenmiao.model.marquee.MarqueePresentationMode
import com.shifenmiao.model.marquee.MarqueeSettings
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

/**
 * 根据不同模式显示不同的预览效果
 */
@Composable
fun MarqueePreview(
    marqueeSettings: MarqueeSettings,
    previewText: String,
    modifier: Modifier = Modifier
) {
    val textColor = Color(marqueeSettings.marqueeTextColor)
    val fontWeight = if (marqueeSettings.marqueeBoldEnabled) FontWeight.Bold else FontWeight.Normal
    val mirrorModifier = Modifier.graphicsLayer {
        scaleX = if (marqueeSettings.marqueeMirrorEnabled) -1f else 1f
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (marqueeSettings.presentationMode) {
            MarqueePresentationMode.ClassicMarquee -> {
                // 经典弹幕：滚动效果
                Text(
                    text = previewText,
                    modifier = mirrorModifier.basicMarquee(
                        iterations = Int.MAX_VALUE,
                        spacing = MarqueeSpacing(marqueeSettings.marqueeTextSpacing.dp),
                        velocity = marqueeSettings.marqueeTextVelocity.dp,
                        repeatDelayMillis = marqueeSettings.marqueeTextRepeatDelayMillis,
                    ),
                    color = textColor,
                    fontSize = 36.sp,
                    fontWeight = fontWeight,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                )
            }

            MarqueePresentationMode.Typewriter -> {
                // 打字机：逐字显示
                TypewriterPreview(
                    text = previewText,
                    color = textColor,
                    fontWeight = fontWeight,
                    charsPerSecond = marqueeSettings.typewriterCharsPerSecond,
                    modifier = mirrorModifier
                )
            }

            MarqueePresentationMode.OneCharPerScreen -> {
                // 一字一屏：单字轮播
                OneCharPreview(
                    text = previewText,
                    color = textColor,
                    fontWeight = fontWeight,
                    holdMillis = marqueeSettings.oneCharHoldMillis,
                    modifier = mirrorModifier
                )
            }

            MarqueePresentationMode.Paged -> {
                // 分屏翻页：静态显示
                Text(
                    text = previewText,
                    modifier = mirrorModifier,
                    color = textColor,
                    fontSize = 36.sp,
                    fontWeight = fontWeight,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                )
            }

            MarqueePresentationMode.Clock -> {
                // 时钟：显示当前时间
                ClockPreview(
                    color = textColor,
                    fontWeight = fontWeight,
                    showSeconds = marqueeSettings.clockShowSeconds,
                    use24Hour = marqueeSettings.clockUse24Hour,
                    modifier = mirrorModifier
                )
            }

            MarqueePresentationMode.Countdown -> {
                // 倒计时：显示倒计时
                CountdownPreview(
                    color = textColor,
                    fontWeight = fontWeight,
                    initialSeconds = marqueeSettings.countdownSeconds,
                    showMillis = marqueeSettings.countdownShowMillis,
                    modifier = mirrorModifier
                )
            }

            else -> {
                // 其他模式暂不支持预览
                Text(
                    text = stringResource(id = R.string.marquee_preview_unavailable),
                    modifier = mirrorModifier,
                    color = textColor,
                    fontSize = 36.sp,
                    fontWeight = fontWeight,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun TypewriterPreview(
    text: String,
    color: Color,
    fontWeight: FontWeight,
    charsPerSecond: Float,
    modifier: Modifier = Modifier
) {
    var index by remember(text) { mutableIntStateOf(0) }
    val cps = charsPerSecond.coerceIn(1f, 120f)

    LaunchedEffect(text, cps) {
        index = 0
        val delayMillis = (1000f / cps).toLong().coerceAtLeast(50L)
        while (true) {
            delay(delayMillis)
            index = if (index >= text.length) 0 else index + 1
        }
    }

    val shown = text.take(index.coerceIn(0, text.length))

    Text(
        text = shown.ifEmpty { "▌" },
        modifier = modifier,
        color = color,
        fontSize = 36.sp,
        fontWeight = fontWeight,
        textAlign = TextAlign.Center,
        maxLines = 1,
        softWrap = false,
    )
}

@Composable
private fun OneCharPreview(
    text: String,
    color: Color,
    fontWeight: FontWeight,
    holdMillis: Int,
    modifier: Modifier = Modifier
) {
    val chars = remember(text) {
        buildList {
            var i = 0
            while (i < text.length) {
                val cp = text.codePointAt(i)
                add(String(Character.toChars(cp)))
                i += Character.charCount(cp)
            }
        }.ifEmpty { listOf("") }
    }

    var idx by remember(chars) { mutableIntStateOf(0) }

    LaunchedEffect(chars, holdMillis) {
        while (true) {
            delay(holdMillis.coerceIn(150, 2000).toLong())
            idx = (idx + 1) % chars.size
        }
    }

    Text(
        text = chars.getOrElse(idx) { "" },
        modifier = modifier,
        color = color,
        fontSize = 48.sp,
        fontWeight = fontWeight,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun ClockPreview(
    color: Color,
    fontWeight: FontWeight,
    showSeconds: Boolean,
    use24Hour: Boolean,
    modifier: Modifier = Modifier
) {
    var timeText by remember { mutableStateOf("") }

    LaunchedEffect(showSeconds, use24Hour) {
        while (true) {
            val cal = Calendar.getInstance()
            val hour = if (use24Hour) {
                cal.get(Calendar.HOUR_OF_DAY)
            } else {
                cal.get(Calendar.HOUR).let { if (it == 0) 12 else it }
            }
            val minute = cal.get(Calendar.MINUTE)
            val second = cal.get(Calendar.SECOND)

            timeText = if (showSeconds) {
                String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            }
            delay(if (showSeconds) 1000L else 10000L)
        }
    }

    Text(
        text = timeText,
        modifier = modifier,
        color = color,
        fontSize = 36.sp,
        fontWeight = fontWeight,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun CountdownPreview(
    color: Color,
    fontWeight: FontWeight,
    initialSeconds: Int,
    showMillis: Boolean,
    modifier: Modifier = Modifier
) {
    var remainingMs by remember(initialSeconds) { mutableStateOf(initialSeconds * 1000L) }

    LaunchedEffect(initialSeconds) {
        val endTime = System.currentTimeMillis() + initialSeconds * 1000L
        while (true) {
            val now = System.currentTimeMillis()
            remainingMs = (endTime - now).coerceAtLeast(0L)
            if (remainingMs <= 0) {
                remainingMs = initialSeconds * 1000L
            }
            delay(if (showMillis) 50L else 100L)
        }
    }

    val totalSeconds = remainingMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val millis = (remainingMs % 1000) / 10

    val countdownText = if (showMillis) {
        String.format(Locale.getDefault(), "%02d:%02d.%02d", minutes, seconds, millis)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    Text(
        text = countdownText,
        modifier = modifier,
        color = color,
        fontSize = 36.sp,
        fontWeight = fontWeight,
        textAlign = TextAlign.Center,
    )
}

