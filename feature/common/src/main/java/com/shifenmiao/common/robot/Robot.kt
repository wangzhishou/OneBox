package com.shifenmiao.common.robot

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.shifenmiao.base.ui.shapes.DownArrowBubbleShape
import com.shifenmiao.core.R
import com.shifenmiao.model.state.RobotState
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground

/** 气泡区域固定高度（含下箭头），机器人出现/消失时布局不跳动 */
private val BUBBLE_AREA_HEIGHT = 64.dp

/** 气泡内最多显示的字符数，防止布局溢出 */
private const val BUBBLE_MAX_CHARS = 30

private enum class RobotVisualMode {
    Normal,
    Streaming,
    Sleeping,
}

@Composable
fun FloatingRobot(
    robotState: RobotState,
    onRobotClick: () -> Unit,
    modifier: Modifier = Modifier,
    onExitClick: (() -> Unit)? = null,
    isOutsideChatView: Boolean = false,
    /**
     * 流式输出时传入正在生成的文本片段，气泡将实时滚动显示它，覆盖默认轮播提示。
     * - null：恢复默认提示词/完成提示
     * - 空字符串：显示"AI 回答中…"占位符
     * - 非空字符串：截取末尾最多 [BUBBLE_MAX_CHARS] 字符展示
     */
    streamingMessage: String? = null,
) {
    val isStreaming = streamingMessage != null
    val bubbleTipState = rememberBubbleTipState()
    var wasStreaming by remember { mutableStateOf(false) }
    var visualMode by remember { mutableStateOf(RobotVisualMode.Normal) }

    LaunchedEffect(isStreaming, isOutsideChatView, bubbleTipState) {
        if (isStreaming) {
            wasStreaming = true
            visualMode = RobotVisualMode.Streaming
            return@LaunchedEffect
        }

        if (!isOutsideChatView) {
            visualMode = RobotVisualMode.Normal
        }

        val hasJustFinishedStreaming = wasStreaming && isOutsideChatView
        wasStreaming = false

        if (hasJustFinishedStreaming) {
            bubbleTipState.showCompletedHint()
            visualMode = RobotVisualMode.Sleeping
        } else if (visualMode == RobotVisualMode.Streaming) {
            visualMode = RobotVisualMode.Normal
        }
    }

    // 用 remember 包裹 lambda，使 RobotBody 在流式输出期间能被 Compose 跳过重组
    val stableOnTap = remember(onRobotClick, bubbleTipState) {
        {
            visualMode = RobotVisualMode.Normal
            bubbleTipState.markUserClicked()
            onRobotClick()
        }
    }
    val stableOnDoubleTap = remember(onExitClick, bubbleTipState) {
        {
            visualMode = RobotVisualMode.Normal
            bubbleTipState.hide()
            onExitClick?.invoke()
            Unit
        }
    }

    Box(
        modifier = modifier
            .height(160.dp)
            .width(robotState.size),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // ── ② Lottie 机器人主体：与气泡独立，流式时被 Compose 跳过重组 ────
        RobotBody(
            robotState = robotState,
            visualMode = visualMode,
            onTap = stableOnTap,
            onDoubleTap = stableOnDoubleTap,
        )

        SleepIndicator(
            isVisible = visualMode == RobotVisualMode.Sleeping,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 28.dp, end = 6.dp),
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(BUBBLE_AREA_HEIGHT),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            // ── ① 气泡区域：固定高度，浮在机器人上方 ──────────────────────────
            FloatingRobotBubble(
                streamingMessage = streamingMessage,
                bubbleTipState = bubbleTipState,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 气泡区：固定高度容器 + 滚动文字
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FloatingRobotBubble(
    streamingMessage: String?,
    bubbleTipState: BubbleTipState,
) {
    val isStreaming = streamingMessage != null
    val streamingPlaceholder = stringResource(id = R.string.robot_bubble_streaming)
    val isVisible = if (isStreaming) true else bubbleTipState.isVisible
    val message = when {
        streamingMessage == null -> bubbleTipState.currentMessage
        streamingMessage.isEmpty() -> streamingPlaceholder
        streamingMessage.length <= BUBBLE_MAX_CHARS -> streamingMessage
        else -> "…${streamingMessage.takeLast(BUBBLE_MAX_CHARS - 1)}"
    }

    // 固定高度保证机器人位置不随气泡出现/消失而抖动
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { -it / 2 },
        exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { -it / 2 },
    ) {
        BubbleContainer {
            if (isStreaming) {
                // 流式模式：滚动文字动画（新文本从下方滚入）
                RollingText(text = message)
            } else {
                // 默认轮播：静态文字
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * 滚动文字：每次 [text] 更新时，旧内容向上滑出、新内容从下方滑入，
 * 形成"字母翻页/滚动"视觉效果，适合 AI 流式输出场景。
 */
@Composable
private fun RollingText(text: String) {
    AnimatedContent(
        targetState = text,
        transitionSpec = {
            // 新文本：从下方淡入 + 向上滑入
            // 旧文本：向上淡出
            (fadeIn(tween(120)) + slideInVertically(tween(100)) { it / 2 })
                .togetherWith(fadeOut(tween(80)))
                .using(SizeTransform(clip = true))
        },
        label = "RollingText",
    ) { displayedText ->
        Text(
            text = displayedText,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

/** 气泡外壳：带下箭头的 glass 背景容器 */
@Composable
private fun BubbleContainer(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .glassBackground(
                style = GlassStyle.Thick,
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = DownArrowBubbleShape(8.dp),
            )
            .padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Lottie 机器人主体：独立 composable，流式输出时参数不变 → Compose 跳过重组
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RobotBody(
    robotState: RobotState,
    visualMode: RobotVisualMode,
    onTap: () -> Unit,
    onDoubleTap: () -> Unit,
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.robot))
    val dynamicProperties = rememberRobotDynamicProperties(visualMode = visualMode)

    Box(
        modifier = Modifier
            .size(robotState.size)
            .pointerInput(onTap, onDoubleTap) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = { onDoubleTap() },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            dynamicProperties = dynamicProperties,
        )
    }
}

@Composable
private fun SleepIndicator(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 3 },
        exit = fadeOut(tween(180)),
    ) {
        val transition = rememberInfiniteTransition(label = "robotSleepIndicator")
        val sleepLetter = stringResource(id = R.string.robot_sleep_indicator_letter)

        Box(
            modifier = Modifier
                .width(44.dp)
                .height(36.dp),
        ) {
            repeat(3) { index ->
                val animatedAlpha by transition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(
                            offsetMillis = index * 180,
                            offsetType = StartOffsetType.FastForward,
                        ),
                    ),
                    label = "sleepAlpha$index",
                )
                val translateY by transition.animateFloat(
                    initialValue = 6f,
                    targetValue = -8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(
                            offsetMillis = index * 180,
                            offsetType = StartOffsetType.FastForward,
                        ),
                    ),
                    label = "sleepTranslate$index",
                )

                Text(
                    text = sleepLetter,
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = (18 + index * 2).sp,
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = (index * 10).dp, bottom = (index * 6).dp)
                        .graphicsLayer {
                            alpha = animatedAlpha
                            translationY = translateY
                        },
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 保留给外部可单独调用的简单气泡（非流式场景复用）
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AnimatedBubbleTip(message: String, isVisible: Boolean) {
    AnimatedVisibility(
        modifier = Modifier.padding(top = AppTheme.dimens.paddingNormal),
        visible = isVisible,
        enter = fadeIn() + slideInVertically { -40 },
        exit = fadeOut() + slideOutVertically { -40 },
    ) {
        BubbleContainer {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Lottie 动态属性：眼睛颜色在流式时脉冲，其余属性恒定
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun rememberRobotDynamicProperties(visualMode: RobotVisualMode = RobotVisualMode.Normal) =
    with(MaterialTheme.colorScheme) {
        // 只对眼睛颜色做动画；initial==target 时 InfiniteTransition 不触发额外重组
        val eyeTransition = rememberInfiniteTransition(label = "robotEyePulse")
        val targetEyeColor = when (visualMode) {
            RobotVisualMode.Streaming -> tertiary
            RobotVisualMode.Sleeping -> outlineVariant
            RobotVisualMode.Normal -> primaryContainer
        }
        val pulseDurationMillis = when (visualMode) {
            RobotVisualMode.Sleeping -> 1400
            RobotVisualMode.Streaming -> 500
            RobotVisualMode.Normal -> 500
        }
        val eyeColor by eyeTransition.animateColor(
            initialValue = primaryContainer,
            targetValue = targetEyeColor,
            animationSpec = infiniteRepeatable(
                animation = tween(pulseDurationMillis, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "eyeColor",
        )
        rememberLottieDynamicProperties(
            rememberLottieDynamicProperty(
                property = LottieProperty.COLOR,
                value = primaryContainer.toArgb(),
                keyPath = arrayOf("1 Outlines", "**")
            ),
            rememberLottieDynamicProperty(
                property = LottieProperty.COLOR,
                value = primaryContainer.toArgb(),
                keyPath = arrayOf("2 Outlines", "**")
            ),
            rememberLottieDynamicProperty(
                property = LottieProperty.STROKE_COLOR,
                value = primaryContainer.toArgb(),
                keyPath = arrayOf("3 Outlines", "**")
            ),
            rememberLottieDynamicProperty(
                property = LottieProperty.COLOR,
                value = eyeColor.toArgb(),
                keyPath = arrayOf("**", "RightEyeShape", "**")
            ),
            rememberLottieDynamicProperty(
                property = LottieProperty.COLOR,
                value = eyeColor.toArgb(),
                keyPath = arrayOf("**", "LeftEyeShape", "**")
            ),
            rememberLottieDynamicProperty(
                property = LottieProperty.COLOR,
                value = onSurfaceVariant.toArgb(),
                keyPath = arrayOf("**", "HeadEyeBgRight", "**")
            ),
            rememberLottieDynamicProperty(
                property = LottieProperty.COLOR,
                value = onSurface.toArgb(),
                keyPath = arrayOf("**", "HeadEyeBg", "**")
            ),
            rememberLottieDynamicProperty(
                property = LottieProperty.COLOR,
                value = primaryContainer.toArgb(),
                keyPath = arrayOf("**", "EarLeftShape", "**")
            ),
            rememberLottieDynamicProperty(
                property = LottieProperty.COLOR,
                value = primaryContainer.toArgb(),
                keyPath = arrayOf("**", "EarRightShape", "**")
            ),
            rememberLottieDynamicProperty(
                property = LottieProperty.GRADIENT_COLOR,
                value = arrayOf(
                    surfaceContainerHigh.toArgb(),
                    surfaceContainerHighest.toArgb(),
                    primaryContainer.toArgb()
                ),
                keyPath = arrayOf("**", "HeadBackground", "**")
            ),
            rememberLottieDynamicProperty(
                property = LottieProperty.GRADIENT_COLOR,
                value = arrayOf(
                    surfaceContainerHigh.toArgb(),
                    surfaceContainerHighest.toArgb(),
                    primaryContainer.toArgb()
                ),
                keyPath = arrayOf("**", "ArmLeftShape", "**")
            ),
            rememberLottieDynamicProperty(
                property = LottieProperty.GRADIENT_COLOR,
                value = arrayOf(
                    surfaceContainerHigh.toArgb(),
                    surfaceContainerHighest.toArgb(),
                    primaryContainer.toArgb()
                ),
                keyPath = arrayOf("**", "ArmRightShape", "**")
            ),
            rememberLottieDynamicProperty(
                property = LottieProperty.COLOR,
                value = outlineVariant.toArgb(),
                keyPath = arrayOf("**", "FooterBottom", "**")
            ),
            rememberLottieDynamicProperty(
                property = LottieProperty.COLOR,
                value = outlineVariant.toArgb(),
                keyPath = arrayOf("**", "BodyTop", "**")
            ),
            rememberLottieDynamicProperty(
                property = LottieProperty.STROKE_COLOR,
                value = outlineVariant.toArgb(),
                keyPath = arrayOf("**", "BodyTop", "**")
            ),
            rememberLottieDynamicProperty(
                property = LottieProperty.GRADIENT_COLOR,
                value = arrayOf(
                    surfaceContainerHigh.toArgb(),
                    primaryContainer.toArgb(),
                    surfaceContainerHighest.toArgb()
                ),
                keyPath = arrayOf("**", "Body", "**")
            ),
        )
    }
