package com.shifenmiao.marquee.screen

import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ScreenLockRotation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.base.ui.button.CancelButton
import com.shifenmiao.base.ui.button.ConfirmButton
import com.shifenmiao.core.R
import com.shifenmiao.marquee.screen.components.FireworksBackground
import com.shifenmiao.marquee.screenLogic.MarqueeComponent
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalScreenSize
import com.t8rin.imagetoolbox.core.ui.widget.modifier.alertDialogBorder
import com.t8rin.imagetoolbox.core.ui.widget.other.LockScreenOrientation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineScreenRotation

@Composable
fun FullScreenSubtitles(
    onGoBack: () -> Unit,
    component: MarqueeComponent
) {
    // 进入全屏时强制从本地刷新一次（避免跨 Activity 的 retained 组件 state 不是最新）
    LaunchedEffect(Unit) {
        component.reloadFromLocalStore()
    }

    val marqueeSettingsState by component.marqueeSettingsState.collectAsState()
    val activity = LocalActivity.current
    val window = activity?.window

    // Fullscreen immersive + keep screen on
    ImmersiveFullscreenEffect(window = window, keepScreenOn = true)

    // Default: lock landscape (preferred). If user unlocks, allow sensor-based rotation.
    if (marqueeSettingsState.lockLandscapeInFullscreen) {
        LockScreenOrientation(mode = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
    } else {
        LockScreenOrientation(mode = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
    }

    val derivedFontSizeValue by remember(marqueeSettingsState) {
        derivedStateOf { marqueeSettingsState.marqueeTextSize }
    }

    var sliderFontSizeValue by remember(derivedFontSizeValue) {
        mutableFloatStateOf(derivedFontSizeValue)
    }

    val localScreenSize = LocalScreenSize.current

    // 使用屏幕短边来计算字号范围（横屏时短边是高度）
    val screenMinDimension = remember(localScreenSize) {
        minOf(localScreenSize.width.value, localScreenSize.height.value)
    }

    val minFontSizeSp = remember(screenMinDimension) {
        // 保持可读性，但允许较小字体
        (screenMinDimension * 0.05f).coerceAtLeast(16f)
    }
    val maxFontSizeSp = remember(screenMinDimension) {
        // 允许字体接近短边高度，让单个字符能铺满屏幕
        // 考虑系统 UI 和字体 metrics，使用 0.95 的系数
        (screenMinDimension * 0.95f)
    }

    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    // Tap overlay
    var showControls by rememberSaveable { mutableStateOf(false) }
    var lastTapAtMs by rememberSaveable { mutableStateOf(0L) }
    var tapCount by rememberSaveable { mutableStateOf(0) }

    // We postpone the single-tap action slightly so a double-tap doesn't also toggle controls.
    @Suppress("UNUSED_VARIABLE")
    val scope = rememberCoroutineScope()
    var singleTapJob by remember { mutableStateOf<Job?>(null) }

    val multiTapWindowMs = 300L

    fun toggleRotationLock() {
        component.onLockLandscapeInFullscreenChange(!marqueeSettingsState.lockLandscapeInFullscreen)
        showControls = true
    }

    fun fitTextToScreen() {
        sliderFontSizeValue = maxFontSizeSp
        showControls = true
    }

    // Auto-hide controls after inactivity
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(2500)
            showControls = false
        }
    }

    // 文字闪烁动画
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (marqueeSettingsState.marqueeBlinkEnabled) 0.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1000 / marqueeSettingsState.marqueeBlinkSpeed).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    // 背景闪烁动画
    val backgroundBlinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (marqueeSettingsState.backgroundBlinkEnabled) 0.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1000 / marqueeSettingsState.marqueeBlinkSpeed).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "backgroundBlinkAlpha"
    )

    // 计算背景色（带闪烁效果）
    val backgroundColor = remember(marqueeSettingsState.marqueeBackgroundColor) {
        Color(marqueeSettingsState.marqueeBackgroundColor)
    }
    val animatedBackgroundColor = backgroundColor.copy(
        red = backgroundColor.red * backgroundBlinkAlpha,
        green = backgroundColor.green * backgroundBlinkAlpha,
        blue = backgroundColor.blue * backgroundBlinkAlpha
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedBackgroundColor)
            .combinedClickable(
                onClick = {
                    val now = System.currentTimeMillis()
                    val withinWindow = (now - lastTapAtMs) in 1..multiTapWindowMs
                    if (!withinWindow) {
                        tapCount = 0
                    }
                    tapCount += 1
                    lastTapAtMs = now

                    // Cancel pending single-tap action; we'll decide once we know tapCount.
                    singleTapJob?.cancel()
                    singleTapJob = null

                    when (tapCount) {
                        1 -> {
                            // Delay single-tap so it won't run if user double/triple taps.
                            singleTapJob = scope.launch {
                                delay(multiTapWindowMs)
                                showControls = !showControls
                                tapCount = 0
                            }
                        }

                        2 -> {
                            toggleRotationLock()
                            tapCount = 0
                        }

                        3 -> {
                            fitTextToScreen()
                            tapCount = 0
                        }

                        else -> {
                            // If user taps very fast many times, just reset.
                            tapCount = 0
                        }
                    }
                },
                onLongClick = { showExitDialog = true },
            )
            .pointerInput(Unit) {
                detectTransformGestures(
                    onGesture = { _, _, gestureZoom, _ ->
                        val newSize = sliderFontSizeValue * gestureZoom
                        val finalSize = newSize.coerceIn(minFontSizeSp..maxFontSizeSp)
                        sliderFontSizeValue = finalSize
                    }
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        // 烟花背景效果
        if (marqueeSettingsState.fireworksEnabled) {
            FireworksBackground(
                modifier = Modifier.fillMaxSize(),
                backgroundColor = Color(marqueeSettingsState.marqueeBackgroundColor)
            )
        }

        FullScreenMarqueeRenderer(
            marqueeSettings = marqueeSettingsState,
            fontSizeSp = sliderFontSizeValue,
            blinkAlpha = blinkAlpha,
            modifier = Modifier.fillMaxSize()
        )

        if (showControls) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .wrapContentSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isLocked = marqueeSettingsState.lockLandscapeInFullscreen

                    FilledIconButton(
                        modifier = Modifier.size(44.dp),
                        onClick = { toggleRotationLock() }
                    ) {
                        Icon(
                            imageVector = if (isLocked) Icons.Rounded.ScreenLockRotation else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineScreenRotation,
                            contentDescription = if (isLocked) "已锁定横屏" else "跟随旋转",
                        )
                    }

                    Spacer(modifier = Modifier.size(10.dp))

                    // Small chip-like label to reduce confusion.
                    Surface(
                        color = Color.Black.copy(alpha = 0.35f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            text = if (isLocked) "锁定横屏" else "跟随旋转",
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    if (showExitDialog) {
        ShowExitAlertDialog(
            onGoBack = onGoBack,
            onDismiss = { showExitDialog = false }
        )
    }
    BackHandler {
        onGoBack()
    }
}

@Composable
fun ShowExitAlertDialog(
    onGoBack: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.alertDialogBorder(),
        onDismissRequest = {
        },
        confirmButton = {
            ConfirmButton(onClick = onGoBack)
        },

        dismissButton = {
            CancelButton(onClick = onDismiss)
        },

        title = { Text(stringResource(R.string.marquee_exit_toast)) }
    )
}
