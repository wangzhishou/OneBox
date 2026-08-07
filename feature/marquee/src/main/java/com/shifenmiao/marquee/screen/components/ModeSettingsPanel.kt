package com.shifenmiao.marquee.screen.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.model.marquee.MarqueePresentationMode
import com.shifenmiao.model.marquee.MarqueeSettings
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCustomSlider

/**
 * 根据展示模式显示对应的设置面板
 */
@Composable
fun ModeSettingsPanel(
    marqueeSettings: MarqueeSettings,
    onTextSpacingChange: (Float) -> Unit,
    onTextVelocityChange: (Float) -> Unit,
    onTypewriterCharsPerSecondChange: (Float) -> Unit,
    onOneCharHoldMillisChange: (Int) -> Unit,
    onPagedAutoAdvanceEnabledChange: (Boolean) -> Unit,
    onPagedIntervalMillisChange: (Int) -> Unit,
    onClockShowSecondsChange: (Boolean) -> Unit,
    onClockShowDateChange: (Boolean) -> Unit,
    onClockUse24HourChange: (Boolean) -> Unit,
    onCountdownSecondsChange: (Int) -> Unit,
    onCountdownShowMillisChange: (Boolean) -> Unit,
    onBouncingSpeedChange: (Float) -> Unit = {},
    modifier: Modifier = Modifier,
    saveState: () -> Unit = {}
) {
    AnimatedContent(
        targetState = marqueeSettings.presentationMode,
        modifier = modifier,
        transitionSpec = {
            (fadeIn() + slideInVertically { it / 4 }) togetherWith
                    (fadeOut() + slideOutVertically { -it / 4 })
        },
        label = "modeSettings"
    ) { mode ->
        Column(modifier = Modifier.fillMaxWidth()) {
            when (mode) {
                MarqueePresentationMode.ClassicMarquee -> {
                    ClassicMarqueeSettings(
                        spacing = marqueeSettings.marqueeTextSpacing,
                        velocity = marqueeSettings.marqueeTextVelocity,
                        onSpacingChange = onTextSpacingChange,
                        onVelocityChange = onTextVelocityChange,
                        saveState = saveState
                    )
                }

                MarqueePresentationMode.Typewriter -> {
                    TypewriterSettings(
                        charsPerSecond = marqueeSettings.typewriterCharsPerSecond,
                        onCharsPerSecondChange = onTypewriterCharsPerSecondChange
                    )
                }

                MarqueePresentationMode.OneCharPerScreen -> {
                    OneCharPerScreenSettings(
                        holdMillis = marqueeSettings.oneCharHoldMillis,
                        onHoldMillisChange = onOneCharHoldMillisChange
                    )
                }

                MarqueePresentationMode.Paged -> {
                    PagedSettings(
                        autoAdvance = marqueeSettings.pagedAutoAdvanceEnabled,
                        intervalMillis = marqueeSettings.pagedIntervalMillis,
                        onAutoAdvanceChange = onPagedAutoAdvanceEnabledChange,
                        onIntervalChange = onPagedIntervalMillisChange
                    )
                }

                MarqueePresentationMode.Clock -> {
                    ClockSettings(
                        showSeconds = marqueeSettings.clockShowSeconds,
                        showDate = marqueeSettings.clockShowDate,
                        use24Hour = marqueeSettings.clockUse24Hour,
                        onShowSecondsChange = onClockShowSecondsChange,
                        onShowDateChange = onClockShowDateChange,
                        onUse24HourChange = onClockUse24HourChange
                    )
                }

                MarqueePresentationMode.Countdown -> {
                    CountdownSettings(
                        seconds = marqueeSettings.countdownSeconds,
                        showMillis = marqueeSettings.countdownShowMillis,
                        onSecondsChange = onCountdownSecondsChange,
                        onShowMillisChange = onCountdownShowMillisChange
                    )
                }

                MarqueePresentationMode.Bouncing -> {
                    BouncingSettings(
                        speed = marqueeSettings.bouncingSpeed,
                        onSpeedChange = onBouncingSpeedChange
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassicMarqueeSettings(
    spacing: Float,
    velocity: Float,
    onSpacingChange: (Float) -> Unit,
    onVelocityChange: (Float) -> Unit,
    saveState: () -> Unit
) {
    // 弹幕速度
    var velocityValue by remember(velocity) { mutableFloatStateOf(velocity) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.marquee_velocity),
            style = AppTheme.typography.getCardTitle()
        )
        Text(
            text = (velocityValue / 100).toInt().toString(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    GlassCustomSlider(
        value = velocityValue,
        onValueChange = { velocityValue = (it * 100).toInt() / 100f },
        valueRange = 500f..2000f,
        steps = 10,
        onValueChangeFinished = {
            onVelocityChange(velocityValue)
            saveState()
        }
    )

    Spacer(modifier = Modifier.height(16.dp))

    // 字间距
    Text(
        text = stringResource(id = R.string.marquee_text_spacing),
        style = AppTheme.typography.getCardTitle()
    )
    Spacer(modifier = Modifier.height(8.dp))

    var spacingValue by remember(spacing) { mutableFloatStateOf(spacing) }
    GlassCustomSlider(
        value = spacingValue,
        onValueChange = { spacingValue = (it * 100).toInt() / 100f },
        valueRange = 0f..80f,
        steps = 15,
        onValueChangeFinished = {
            onSpacingChange(spacingValue)
            saveState()
        }
    )
}

@Composable
private fun TypewriterSettings(
    charsPerSecond: Float,
    onCharsPerSecondChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.marquee_typewriter_speed),
            style = AppTheme.typography.getCardTitle()
        )
        Text(
            text = String.format(
                java.util.Locale.getDefault(),
                stringResource(id = R.string.marquee_typewriter_speed_format),
                charsPerSecond
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    var cps by remember(charsPerSecond) { mutableFloatStateOf(charsPerSecond) }
    GlassCustomSlider(
        value = cps,
        onValueChange = { cps = it },
        valueRange = 4f..60f,
        steps = 13,
        onValueChangeFinished = { onCharsPerSecondChange(cps) }
    )
}

@Composable
private fun OneCharPerScreenSettings(
    holdMillis: Int,
    onHoldMillisChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.marquee_hold_duration),
            style = AppTheme.typography.getCardTitle()
        )
        Text(
            text = "$holdMillis ms",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    var hold by remember(holdMillis) { mutableFloatStateOf(holdMillis.toFloat()) }
    GlassCustomSlider(
        value = hold,
        onValueChange = { hold = it },
        valueRange = 150f..1500f,
        steps = 9,
        onValueChangeFinished = { onHoldMillisChange(hold.toInt()) }
    )
}

@Composable
private fun PagedSettings(
    autoAdvance: Boolean,
    intervalMillis: Int,
    onAutoAdvanceChange: (Boolean) -> Unit,
    onIntervalChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(id = R.string.marquee_auto_page),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(id = R.string.marquee_auto_page_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        GlassSwitch(
            checked = autoAdvance,
            onCheckedChange = onAutoAdvanceChange,
            colors = AppTheme.colors.switchColors()
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.marquee_page_interval),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "$intervalMillis ms",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    var interval by remember(intervalMillis) { mutableFloatStateOf(intervalMillis.toFloat()) }
    GlassCustomSlider(
        value = interval,
        onValueChange = { interval = it },
        valueRange = 600f..10_000f,
        steps = 9,
        onValueChangeFinished = { onIntervalChange(interval.toInt()) }
    )
}

@Composable
private fun ClockSettings(
    showSeconds: Boolean,
    showDate: Boolean,
    use24Hour: Boolean,
    onShowSecondsChange: (Boolean) -> Unit,
    onShowDateChange: (Boolean) -> Unit,
    onUse24HourChange: (Boolean) -> Unit
) {
    Text(
        text = stringResource(id = R.string.marquee_clock_settings),
        style = AppTheme.typography.getCardTitle()
    )
    Spacer(modifier = Modifier.height(8.dp))

    // 显示秒
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.marquee_show_seconds),
            style = MaterialTheme.typography.bodyMedium
        )
        GlassSwitch(
            checked = showSeconds,
            onCheckedChange = onShowSecondsChange,
            colors = AppTheme.colors.switchColors()
        )
    }

    // 显示日期
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.marquee_show_date),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(id = R.string.marquee_show_date_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        GlassSwitch(
            checked = showDate,
            onCheckedChange = onShowDateChange,
            colors = AppTheme.colors.switchColors()
        )
    }

    // 24小时制
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.marquee_24_hour),
            style = MaterialTheme.typography.bodyMedium
        )
        GlassSwitch(
            checked = use24Hour,
            onCheckedChange = onUse24HourChange,
            colors = AppTheme.colors.switchColors()
        )
    }
}

@Composable
private fun CountdownSettings(
    seconds: Int,
    showMillis: Boolean,
    onSecondsChange: (Int) -> Unit,
    onShowMillisChange: (Boolean) -> Unit
) {
    Text(
        text = stringResource(id = R.string.marquee_countdown_settings),
        style = AppTheme.typography.getCardTitle()
    )
    Spacer(modifier = Modifier.height(8.dp))

    // 倒计时格式化字符串
    val countdownFormatMinSec = stringResource(id = R.string.marquee_countdown_format_min_sec)
    val countdownFormatSec = stringResource(id = R.string.marquee_countdown_format_sec)

    // 倒计时时长
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.marquee_countdown_duration),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = formatCountdownTime(seconds, countdownFormatMinSec, countdownFormatSec),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    var secondsValue by remember(seconds) { mutableIntStateOf(seconds) }
    GlassCustomSlider(
        value = secondsValue.toFloat(),
        onValueChange = { secondsValue = it.toInt() },
        valueRange = 10f..600f,  // 10秒 到 10分钟
        steps = 58,
        onValueChangeFinished = { onSecondsChange(secondsValue) }
    )

    Spacer(modifier = Modifier.height(8.dp))

    // 显示毫秒
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.marquee_show_millis),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(id = R.string.marquee_show_millis_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        GlassSwitch(
            checked = showMillis,
            onCheckedChange = onShowMillisChange,
            colors = AppTheme.colors.switchColors()
        )
    }
}

private fun formatCountdownTime(
    totalSeconds: Int,
    formatMinSec: String,
    formatSec: String
): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) {
        String.format(java.util.Locale.getDefault(), formatMinSec, minutes, seconds)
    } else {
        String.format(java.util.Locale.getDefault(), formatSec, seconds)
    }
}

@Composable
private fun BouncingSettings(
    speed: Float,
    onSpeedChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.marquee_bouncing_speed),
            style = AppTheme.typography.getCardTitle()
        )
        Text(
            text = String.format(java.util.Locale.getDefault(), "%.0f", speed),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    var speedValue by remember(speed) { mutableFloatStateOf(speed) }
    GlassCustomSlider(
        value = speedValue,
        onValueChange = { speedValue = it },
        valueRange = 2f..20f,
        steps = 8,
        onValueChangeFinished = { onSpeedChange(speedValue) }
    )
}

