package com.shifenmiao.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.shifenmiao.base.ui.OptionConfirmDialog
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.robot.FloatingRobot
import com.shifenmiao.core.R
import com.shifenmiao.model.state.RobotState
import com.shifenmiao.storage.AppSharedStorage
import kotlin.math.roundToInt

@Composable
fun FloatingRobotDragController(
    appComponent: AppComponent,
    robotState: RobotState,
    isOutsideChatView: Boolean = false,
    streamingMessage: String? = null,
) {
    val haptic = LocalHapticFeedback.current
    val showExitConfirmState = remember { mutableStateOf(false) }
    OptionConfirmDialog(
        title = stringResource(id = R.string.robot_exit_dialog_title),
        message = stringResource(id = R.string.robot_exit_dialog_message),
        primaryButtonText = stringResource(id = R.string.robot_exit_dialog_this_time),
        secondaryButtonText = stringResource(id = R.string.robot_exit_dialog_forever),
        cancelButtonText = stringResource(id = R.string.robot_exit_dialog_cancel),
        onPrimary = {
            // Hide only this time.
            appComponent.setRobotIsMenuExpanded(false)
            appComponent.setRobotHasBeenDragged(false)
            appComponent.toggleRobotVisibility(false)
        },
        onSecondary = {
            // Disable (persist) and hide.
            appComponent.setDisableRobot(true)
            appComponent.setRobotIsMenuExpanded(false)
            appComponent.toggleRobotVisibility(false)
        },
        onCancel = {
            // no-op
        },
        showDialog = showExitConfirmState,
    )

    val dragState = rememberRobotDragState(
        robotState = robotState,
        onDragStart = {
            appComponent.setRobotIsMenuExpanded(false)
            appComponent.setRobotIsDragging(true)
        },
        onDragEnd = { percentX, percentY ->
            appComponent.setRobotOffsetPercent(percentX, percentY)
            appComponent.setRobotIsDragging(false)
            appComponent.setRobotHasBeenDragged(true)
        },
        onDragCancel = { appComponent.setRobotIsDragging(false) },
        haptic = haptic
    )

    AnimatedVisibility(
        visible = robotState.visible,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            initialOffsetY = { -it } // 从上方滑入
        ),
        exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(
            animationSpec = tween(200),
            targetOffsetY = { -it } // 向上方滑出
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    dragState.updateContainerSize(it)
                }
        ) {
            if (robotState.isInitialized && robotState.hasComputedInitialPosition) {
                val animatedOffset = dragState.animatedOffset(disableAnimation = false)
                val animatedScale = dragState.animatedScale(disableDragScale = false)

                FloatingRobot(
                    robotState = robotState,
                    isOutsideChatView = isOutsideChatView,
                    streamingMessage = streamingMessage,
                    onRobotClick = {
                            appComponent.showAIChat()
                            AppSharedStorage.saveRobotClick()
                            appComponent.setRobotIsDoubleClick(AppSharedStorage.loadIsRobotDoubleClick())
                    },
                    onExitClick = {
                        appComponent.setRobotScale(0.6f)
                        showExitConfirmState.value = true
                    },
                    modifier = Modifier
                        .offset { animatedOffset }
                        .scale(animatedScale)
                        .onSizeChanged { dragState.updateRobotSize(it) }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { dragState.onDragStart() },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragState.onDrag(dragAmount)
                                },
                                onDragEnd = { dragState.onDragEnd() },
                                onDragCancel = { dragState.onDragCancel() }
                            )
                        }
                )
            }
        }
    }
}

@Composable
private fun rememberRobotDragState(
    robotState: RobotState,
    onDragStart: () -> Unit,
    onDragEnd: (Float, Float) -> Unit,
    onDragCancel: () -> Unit,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) = remember {
    RobotDragState(onDragStart, onDragEnd, onDragCancel, haptic)
}.apply {
    LaunchedEffect(robotState) {
        updateRobotState(robotState)
    }
}

private class RobotDragState(
    private val onDragStart: () -> Unit,
    private val onDragEnd: (Float, Float) -> Unit,
    private val onDragCancel: () -> Unit,
    private val haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {

    private var currentRobotState by mutableStateOf(RobotState())

    var containerSize by mutableStateOf(IntSize.Zero)
        private set

    var robotSize by mutableStateOf(IntSize.Zero)
        private set

    private var dragOffsetX by mutableFloatStateOf(0f)
    private var dragOffsetY by mutableFloatStateOf(0f)

    /**
     * When we end a drag we send percentX/Y to state owner, but that update arrives asynchronously.
     * If we clear [dragOffsetX]/[dragOffsetY] immediately, the UI can briefly render the *old*
     * percent-based offset and appear to "flash" back to the original position.
     *
     * Keep showing the drag offset until we observe new percent values.
     */
    private var pendingDragEnd by mutableStateOf(false)

    private var lastOffsetXPercent by mutableFloatStateOf(Float.NaN)
    private var lastOffsetYPercent by mutableFloatStateOf(Float.NaN)

    private val shouldShowDragOffset by derivedStateOf {
        currentRobotState.isDragging || pendingDragEnd
    }

    private val offsetX by derivedStateOf {
        if (containerSize.width > 0 && robotSize.width > 0) {
            val minX = -robotSize.width / 2f
            val maxX = containerSize.width - robotSize.width / 2f
            val range = maxX - minX

            // Map percent to the extended range. We clamp pixels (not percent) to keep it stable
            // even if old persisted values were out of bounds.
            (minX + range * currentRobotState.offsetXPercent)
                .coerceIn(minX, maxX)
        } else 0f
    }

    private val offsetY by derivedStateOf {
        if (containerSize.height > 0 && robotSize.height > 0) {
            val minY = -robotSize.height / 2f
            val maxY = containerSize.height - robotSize.height / 2f
            val range = maxY - minY

            // Map percent to the extended range. We clamp pixels (not percent) to keep it stable
            // even if old persisted values were out of bounds.
            (minY + range * currentRobotState.offsetYPercent)
                .coerceIn(minY, maxY)
        } else 0f
    }


    @Composable
    fun animatedOffset(disableAnimation: Boolean): IntOffset {
        // 拖动中/拖动刚结束(等待percent落地)不做动画：保证跟手且避免松手后闪回原位置
        if (shouldShowDragOffset) {
            return IntOffset(
                (offsetX + dragOffsetX).roundToInt(),
                (offsetY + dragOffsetY).roundToInt()
            )
        }

        if (disableAnimation) {
            return IntOffset(
                offsetX.roundToInt(),
                offsetY.roundToInt()
            )
        }

        // 第一次初始化时不使用动画，避免从(0,0)闪现；拖拽过之后再启用回弹动画
        val shouldAnimate = currentRobotState.hasBeenDragged

        val x by animateFloatAsState(
            targetValue = offsetX,
            animationSpec = if (shouldAnimate) {
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            } else {
                spring(
                    dampingRatio = 1f,
                    stiffness = 5000f
                )
            },
            label = "offsetX"
        )
        val y by animateFloatAsState(
            targetValue = offsetY,
            animationSpec = if (shouldAnimate) {
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            } else {
                spring(
                    dampingRatio = 1f,
                    stiffness = 5000f
                )
            },
            label = "offsetY"
        )
        return IntOffset(x.toInt(), y.toInt())
    }

    @Composable
    fun animatedScale(disableDragScale: Boolean): Float {
        if (currentRobotState.isDragging && !disableDragScale) return 1.1f

        // 第一次初始化时不使用动画，避免闪现；拖拽过之后再启用回弹动画
        val shouldAnimate = currentRobotState.hasBeenDragged

        val scale by animateFloatAsState(
            targetValue = currentRobotState.scale,
            animationSpec = if (shouldAnimate) {
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            } else {
                spring(
                    dampingRatio = 1f,
                    stiffness = 5000f
                )
            },
            label = "scale"
        )
        return scale
    }

    fun updateContainerSize(size: IntSize) {
        containerSize = size
    }

    fun updateRobotSize(size: IntSize) {
        robotSize = size
    }

    fun updateRobotState(state: RobotState) {
        currentRobotState = state

        // If we were waiting for the state owner to apply the new percent, clear the pending flag
        // once we observe a percent change.
        if (pendingDragEnd) {
            val xChanged = lastOffsetXPercent.isNaN() || lastOffsetXPercent != state.offsetXPercent
            val yChanged = lastOffsetYPercent.isNaN() || lastOffsetYPercent != state.offsetYPercent
            if (xChanged || yChanged) {
                pendingDragEnd = false
                dragOffsetX = 0f
                dragOffsetY = 0f
            }
        }
        lastOffsetXPercent = state.offsetXPercent
        lastOffsetYPercent = state.offsetYPercent
    }

    fun onDragStart() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onDragStart.invoke()
        pendingDragEnd = false
        dragOffsetX = 0f
        dragOffsetY = 0f
    }

    fun onDrag(dragAmount: androidx.compose.ui.geometry.Offset) {
        dragOffsetX += dragAmount.x
        dragOffsetY += dragAmount.y
    }

    fun onDragEnd() {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

        val minX = -robotSize.width / 2f
        val maxX = containerSize.width - robotSize.width / 2f
        val xRange = (maxX - minX).takeIf { it > 0f } ?: 1f

        val minY = -robotSize.height / 2f
        val maxY = containerSize.height - robotSize.height / 2f
        val yRange = (maxY - minY).takeIf { it > 0f } ?: 1f

        val finalX = (offsetX + dragOffsetX).coerceIn(minX, maxX)
        val finalY = (offsetY + dragOffsetY).coerceIn(minY, maxY)

        // Percent is now based on the extended horizontal range.
        val percentX = (finalX - minX) / xRange

        // Percent is now based on the extended vertical range.
        val percentY = (finalY - minY) / yRange

        pendingDragEnd = true
        // Keep X/Y percent aligned to the extended contract. Clamp to [0..1] for safety.
        onDragEnd(percentX.coerceIn(0f, 1f), percentY.coerceIn(0f, 1f))
    }

    fun currentCenterPx(): androidx.compose.ui.geometry.Offset {
        val x = offsetX + if (shouldShowDragOffset) dragOffsetX else 0f
        val y = offsetY + if (shouldShowDragOffset) dragOffsetY else 0f
        return androidx.compose.ui.geometry.Offset(
            x = x + robotSize.width / 2f,
            y = y + robotSize.height / 2f
        )
    }

    fun currentRadiusPx(): Float {
        if (robotSize.width <= 0 || robotSize.height <= 0) return 0f
        return minOf(robotSize.width, robotSize.height) * 0.28f
    }


    fun onDragCancel() {
        onDragCancel.invoke()
        pendingDragEnd = false
        dragOffsetX = 0f
        dragOffsetY = 0f
    }
}
