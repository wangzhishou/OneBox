package com.wanbaohe.media.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle

/**
 * 播放进度条。
 *
 * 不用 Material Slider：媒体页要的是"一条细线 + 小圆点"（原型里的样子），
 * M3 滑块默认 48dp 触控区配大圆点，压在视频画面上会糊掉半张脸。
 * 这里自绘 4dp 轨道 + 13dp 圆点；按下事件先 consume，页面在 LazyColumn/滚动容器里时
 * 上下轻微偏移不会把拖动变成滚动。
 */
@Composable
fun MediaSeekBar(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    showTimes: Boolean = true,
    activeColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
    thumbColor: Color = activeColor,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val enabled = durationMs > 0L
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    val progress = if (!enabled) 0f else (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
    val shownProgress = dragFraction ?: progress

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .semantics {
                    progressBarRangeInfo = ProgressBarRangeInfo(shownProgress, 0f..1f)
                    setProgress { target ->
                        if (enabled) {
                            onSeek(target.coerceIn(0f, 1f))
                            true
                        } else {
                            false
                        }
                    }
                }
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    fun fractionOf(x: Float): Float =
                        (x / size.width.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)

                    awaitEachGesture {
                        // 先消费按下事件，父级滚动容器就拿不到这次手势
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        dragFraction = fractionOf(down.position.x)
                        var pressed = true
                        while (pressed) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            pressed = change.pressed
                            if (pressed) {
                                dragFraction = fractionOf(change.position.x)
                                change.consume()
                            }
                        }
                        dragFraction?.let(onSeek)
                        dragFraction = null
                    }
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerY = size.height / 2f
                val trackHeight = 4.dp.toPx()
                val activeEnd = size.width * shownProgress
                drawLine(
                    color = inactiveColor,
                    start = Offset(0f, centerY),
                    end = Offset(size.width, centerY),
                    strokeWidth = trackHeight,
                    cap = StrokeCap.Round,
                )
                if (shownProgress > 0f) {
                    drawLine(
                        color = activeColor,
                        start = Offset(0f, centerY),
                        end = Offset(activeEnd, centerY),
                        strokeWidth = trackHeight,
                        cap = StrokeCap.Round,
                    )
                }
                drawCircle(
                    color = thumbColor,
                    radius = 6.5.dp.toPx(),
                    center = Offset(activeEnd.coerceIn(0f, size.width), centerY),
                )
            }
        }
        if (showTimes) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatMediaDuration(
                        (durationMs * shownProgress).toLong().takeIf { it > 0L } ?: positionMs
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor,
                )
                Text(
                    text = formatMediaDuration(durationMs),
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor,
                )
            }
        }
    }
}

/**
 * 播放页/库页的小节标题（"最近观看""全部视频"）。
 *
 * 不画右侧箭头：这里没有跳转，箭头只会让人以为能点。
 */
@Composable
fun MediaSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * "播放模式 / 播放队列"这类可点信息行。
 *
 * 一行 = 图标 + 主标题 + 副标题 + 箭头，整行可点，交互区域不小于 56dp。
 */
@Composable
fun MediaInfoRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        style = GlassStyle.Regular,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = MediaChevronRightIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
