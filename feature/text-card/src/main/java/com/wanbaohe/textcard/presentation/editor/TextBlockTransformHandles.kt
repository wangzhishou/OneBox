package com.wanbaohe.textcard.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RotateRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.domain.render.CardLayout
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun BoxScope.TextBlockTransformHandles(
    block: TextBlock,
    boxSize: IntSize,
    elementCoordinates: LayoutCoordinates?,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    onStart: () -> Unit,
    onChange: (TextBlock) -> Unit,
    onEnd: () -> Unit,
) {
    if (boxSize == IntSize.Zero) return
    val inverseScale = 1f / block.scale.coerceAtLeast(0.01f)
    val counterRotation = -block.rotation
    val center = Offset(boxSize.width / 2f, boxSize.height / 2f)

    listOf(
        Triple(Alignment.TopStart, -1, -1),
        Triple(Alignment.TopEnd, 1, -1),
        Triple(Alignment.BottomStart, -1, 1),
        Triple(Alignment.BottomEnd, 1, 1)
    ).forEach { (alignment, signX, signY) ->
        var startBlock by remember { mutableStateOf(block) }
        var centerRoot by remember { mutableStateOf(Offset.Zero) }
        var startDistance by remember { mutableStateOf(1f) }
        TextBlockTransformHandle(
            description = stringResource(R.string.textcard_scale_text),
            alignment = alignment,
            offsetX = (signX * HANDLE_HALF_DP).dp,
            offsetY = (signY * HANDLE_HALF_DP).dp,
            inverseScale = inverseScale,
            counterRotation = counterRotation,
            onStart = { root ->
                startBlock = block
                centerRoot = elementCoordinates?.localToRoot(center) ?: root
                startDistance = (root - centerRoot).getDistance().coerceAtLeast(1f)
                onStart()
            },
            onDrag = { root ->
                val distance = (root - centerRoot).getDistance()
                onChange(
                    startBlock.copy(
                        scale = (startBlock.scale * distance / startDistance)
                            .coerceIn(0.1f, 10f)
                    )
                )
            },
            onEnd = onEnd
        ) {
            HandleSquare()
        }
    }

    var rotationStartBlock by remember { mutableStateOf(block) }
    var rotationCenterRoot by remember { mutableStateOf(Offset.Zero) }
    var startAngle by remember { mutableStateOf(0f) }
    TextBlockTransformHandle(
        description = stringResource(R.string.textcard_rotate_text),
        alignment = Alignment.TopCenter,
        offsetY = -(HANDLE_HALF_DP.dp + 16.dp * inverseScale),
        inverseScale = inverseScale,
        counterRotation = counterRotation,
        onStart = { root ->
            rotationStartBlock = block
            rotationCenterRoot = elementCoordinates?.localToRoot(center) ?: root
            startAngle = atan2(root.y - rotationCenterRoot.y, root.x - rotationCenterRoot.x)
            onStart()
        },
        onDrag = { root ->
            val angle = atan2(root.y - rotationCenterRoot.y, root.x - rotationCenterRoot.x)
            onChange(
                rotationStartBlock.copy(
                    rotation = rotationStartBlock.rotation +
                        Math.toDegrees((angle - startAngle).toDouble()).toFloat()
                )
            )
        },
        onEnd = onEnd
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                imageVector = Icons.Outlined.RotateRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    listOf(-1, 1).forEach { edge ->
        var startBlock by remember { mutableStateOf(block) }
        var startRoot by remember { mutableStateOf(Offset.Zero) }
        var startSize by remember { mutableStateOf(IntSize.Zero) }
        var axisRoot by remember { mutableStateOf(Offset(1f, 0f)) }
        TextBlockTransformHandle(
            description = stringResource(R.string.textcard_resize_text_width),
            alignment = if (edge < 0) Alignment.CenterStart else Alignment.CenterEnd,
            offsetX = (edge * HANDLE_HALF_DP).dp,
            inverseScale = inverseScale,
            counterRotation = counterRotation,
            onStart = { root ->
                startBlock = block
                startRoot = root
                startSize = boxSize
                axisRoot = elementCoordinates?.let {
                    it.localToRoot(Offset(1f, 0f)) - it.localToRoot(Offset.Zero)
                } ?: Offset(1f, 0f)
                onStart()
            },
            onDrag = { root ->
                val drag = root - startRoot
                val projected = (drag.x * axisRoot.x + drag.y * axisRoot.y) /
                    (axisRoot.x * axisRoot.x + axisRoot.y * axisRoot.y).coerceAtLeast(0.001f)
                val startWidth = startSize.width.toFloat()
                val newWidth = (startWidth + edge * projected)
                    .coerceIn(canvasWidthPx * 0.1f, canvasWidthPx)
                val widthDelta = startWidth - newWidth
                val shift = Offset(-edge * widthDelta / 2f, 0f)
                    .rotateBy(startBlock.rotation) * startBlock.scale
                val startLeft = canvasWidthPx * CardLayout.CONTENT_PADDING_RATIO +
                    startBlock.offsetX * canvasWidthPx
                val startTop = canvasWidthPx * startBlock.baseTopRatio +
                    startBlock.offsetY * canvasHeightPx
                val newLeft = startLeft + widthDelta / 2f + shift.x
                val newTop = startTop + shift.y
                onChange(
                    startBlock.copy(
                        widthRatio = newWidth / canvasWidthPx,
                        widthManuallySet = true,
                        offsetX = (
                            newLeft - canvasWidthPx * CardLayout.CONTENT_PADDING_RATIO
                            ) / canvasWidthPx,
                        offsetY = (
                            newTop - canvasWidthPx * startBlock.baseTopRatio
                            ) / canvasHeightPx
                    )
                )
            },
            onEnd = onEnd
        ) {
            HandleSquare()
        }
    }
}

@Composable
private fun BoxScope.TextBlockTransformHandle(
    description: String,
    alignment: Alignment,
    inverseScale: Float,
    counterRotation: Float,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    onStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onEnd: () -> Unit,
    visual: @Composable BoxScope.() -> Unit,
) {
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val currentOnStart by rememberUpdatedState(onStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnEnd by rememberUpdatedState(onEnd)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .align(alignment)
            .offset(offsetX, offsetY)
            .graphicsLayer {
                scaleX = inverseScale
                scaleY = inverseScale
                rotationZ = counterRotation
            }
            .size(48.dp)
            .onGloballyPositioned { coordinates = it }
            .semantics { contentDescription = description }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (down.isConsumed) return@awaitEachGesture
                    val startRoot = coordinates?.localToRoot(down.position) ?: down.position
                    down.consume()
                    var dragging = false
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.changes.size > 1 || event.changes.any { it.isConsumed }) break
                            val change = event.changes.first()
                            val root = coordinates?.localToRoot(change.position) ?: change.position
                            if (!dragging &&
                                (root - startRoot).getDistance() > viewConfiguration.touchSlop
                            ) {
                                dragging = true
                                currentOnStart(startRoot)
                            }
                            if (dragging && change.positionChange() != Offset.Zero) {
                                currentOnDrag(root)
                                change.consume()
                            }
                            if (!change.pressed) break
                        }
                    } finally {
                        if (dragging) currentOnEnd()
                    }
                }
            },
        content = visual
    )
}

@Composable
private fun HandleSquare() {
    val shape = RoundedCornerShape(2.dp)
    Box(
        modifier = Modifier
            .size(13.5.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.primaryContainer, shape)
            .border(1.dp, Color.White, shape)
    )
}

internal fun Offset.rotateBy(degrees: Float): Offset {
    val radians = Math.toRadians(degrees.toDouble())
    return Offset(
        x = (x * cos(radians) - y * sin(radians)).toFloat(),
        y = (x * sin(radians) + y * cos(radians)).toFloat()
    )
}

private const val HANDLE_HALF_DP = 24
