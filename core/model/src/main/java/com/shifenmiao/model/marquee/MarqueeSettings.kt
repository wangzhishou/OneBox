package com.shifenmiao.model.marquee

import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
enum class MarqueePresentationMode {
    ClassicMarquee,
    Typewriter,
    OneCharPerScreen,
    Paged,
    Clock,       // 时钟模式 - 显示当前时间
    Countdown,   // 倒计时模式
    Bouncing     // 弹跳模式 (DVD Screensaver)
}

@Parcelize
@Serializable
data class MarqueeSettings(
    var marqueeTextColor: Int = Color.White.toArgb(),
    var marqueeBackgroundColor: Int = Color.Black.toArgb(),
    var marqueeText: String = "",
    var marqueeTextSize: Float = 300.dp.value,
    var marqueeTextSpacing: Float = 0.dp.value,
    var marqueeTextVelocity: Float = 1000.dp.value,
    val marqueeTextRepeatDelayMillis: Int = 0,
    // 字符间距 (em)
    var letterSpacing: Float = 0f,
    // 新增功能字段
    var marqueeBlinkEnabled: Boolean = false,      // 文字闪烁效果开关
    var marqueeBlinkSpeed: Float = 2f,             // 闪烁速度(次/秒)
    var backgroundBlinkEnabled: Boolean = false,   // 背景闪烁效果开关
    var marqueeMirrorEnabled: Boolean = false,     // 镜像翻转开关
    var marqueeBoldEnabled: Boolean = false,       // 加粗字体开关

    // 全屏展示样式
    var presentationMode: MarqueePresentationMode = MarqueePresentationMode.ClassicMarquee,

    // Typewriter
    var typewriterCharsPerSecond: Float = 16f,

    // OneCharPerScreen
    var oneCharHoldMillis: Int = 450,

    // Paged
    var pagedAutoAdvanceEnabled: Boolean = true,
    var pagedIntervalMillis: Int = 2500,

    // Clock 时钟模式
    var clockShowSeconds: Boolean = true,          // 是否显示秒
    var clockShowDate: Boolean = false,            // 是否显示日期
    var clockUse24Hour: Boolean = true,            // 使用24小时制

    // Countdown 倒计时模式
    var countdownSeconds: Int = 60,                // 倒计时初始秒数
    var countdownShowMillis: Boolean = false,      // 是否显示毫秒

    // Bouncing 弹跳模式
    var bouncingSpeed: Float = 6f,                 // 弹跳速度 (1-20)

    // 背景效果
    var fireworksEnabled: Boolean = false,         // 烟花背景效果

    // Screen orientation
    // true: lock fullscreen preview to landscape (default)
    // false: allow sensor-based rotation
    var lockLandscapeInFullscreen: Boolean = true,
) : Parcelable {
}
