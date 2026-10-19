package com.wanbaohe.markuplayers.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.ui.widget.editor.AnimatedSelectionBorder
import com.t8rin.imagetoolbox.core.ui.widget.editor.HandleDot
import com.t8rin.imagetoolbox.core.ui.widget.editor.BoxResizeConfig
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.LayerTransform
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.RotateRight

/**
 * One finger moves the selected layer. Explicit handles resize/rotate; multi-touch is never
 * consumed here, so the canvas can take over even in the middle of a layer drag.
 */
@Composable
fun BoxWithConstraintsScope.EditBox(
    transform: LayerTransform,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onTransformEnd: (LayerTransform) -> Unit,
    modifier: Modifier = Modifier,
    onTransformStart: () -> Unit = {},
    onTransformChange: ((LayerTransform) -> Unit)? = null,
    onEditRequest: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    tapSelectable: Boolean = true,
    gesturesEnabled: Boolean = true,
    showHandles: Boolean = true,
    canvasScale: Float = 1f,
    resizeConfig: BoxResizeConfig? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    if (!transform.visible) return
    val canvasWidth = constraints.maxWidth.toFloat()
    val canvasHeight = constraints.maxHeight.toFloat()
    var localTransform by remember { mutableStateOf(transform) }
    var changing by remember { mutableStateOf(false) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var elementCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentOnEdit by rememberUpdatedState(onEditRequest)
    val currentOnStart by rememberUpdatedState(onTransformStart)
    val currentOnChange by rememberUpdatedState(onTransformChange)
    val currentOnEnd by rememberUpdatedState(onTransformEnd)
    val canTransform = gesturesEnabled && isSelected && !transform.locked

    LaunchedEffect(transform) {
        if (!changing) localTransform = transform
    }
    val finish = {
        if (changing) {
            changing = false
            currentOnEnd(localTransform)
        }
    }
    val start = {
        changing = true
        currentOnStart()
    }
    val update: (LayerTransform) -> Unit = {
        localTransform = it
        currentOnChange?.invoke(it)
    }

    Box(
        modifier = modifier
            .align(Alignment.Center)
            .onSizeChanged { boxSize = it }
            .onGloballyPositioned { elementCoordinates = it }
            .graphicsLayer {
                scaleX = localTransform.scale
                scaleY = localTransform.scale
                rotationZ = localTransform.rotation
                translationX = (localTransform.centerX - 0.5f) * canvasWidth
                translationY = (localTransform.centerY - 0.5f) * canvasHeight
            }
            .pointerInput(canTransform, tapSelectable, gesturesEnabled, canvasWidth, canvasHeight) {
                if (!gesturesEnabled || transform.locked || (!canTransform && !tapSelectable)) {
                    return@pointerInput
                }
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (down.isConsumed) return@awaitEachGesture
                    var pan = Offset.Zero
                    var moved = false
                    var canceled = false
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.changes.size > 1 || event.changes.any { it.isConsumed }) {
                                canceled = true
                                break
                            }
                            val change = event.changes.first()
                            val delta = change.positionChange()
                            pan += delta
                            if (canTransform && !moved &&
                                pan.getDistance() * localTransform.scale * canvasScale >
                                viewConfiguration.touchSlop
                            ) {
                                moved = true
                                start()
                            }
                            if (moved && delta != Offset.Zero) {
                                val canvasPan = (delta * localTransform.scale)
                                    .rotateBy(localTransform.rotation)
                                update(
                                    localTransform.copy(
                                        centerX = (localTransform.centerX + canvasPan.x / canvasWidth)
                                            .coerceIn(0f, 1f),
                                        centerY = (localTransform.centerY + canvasPan.y / canvasHeight)
                                            .coerceIn(0f, 1f)
                                    )
                                )
                                change.consume()
                            }
                            if (!change.pressed) {
                                if (!moved && !canceled && tapSelectable &&
                                    pan.getDistance() * localTransform.scale * canvasScale <=
                                    viewConfiguration.touchSlop
                                ) {
                                    change.consume()
                                    if (isSelected) currentOnEdit?.invoke() else currentOnSelect()
                                }
                                break
                            }
                        }
                    } finally {
                        finish()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.graphicsLayer {
                scaleX = if (localTransform.flipHorizontal) -1f else 1f
                scaleY = if (localTransform.flipVertical) -1f else 1f
                alpha = localTransform.alpha
            },
            contentAlignment = Alignment.Center,
            content = content
        )
        val chromeScale = (localTransform.scale * canvasScale).coerceAtLeast(0.01f)
        val inverseScale = 1f / chromeScale
        // 标记外观不随图层变换:缩放由 inverseScale 抵消、旋转由 counterRotation 抵消,
        // 缩放/旋转选中区域时标记大小与朝向恒定。
        // 偏移 = 半个热区(把标记中心压在角/边上,中心锚定对任何缩放都不变)
        // + 外扩距离 × inverseScale(屏幕外扩距离恒定)。
        val counterRotation = -localTransform.rotation
        // 选框动效与图文卡片一致:就地编辑态不隐藏,弱化显示(alphaScale 0.4)
        AnimatedSelectionBorder(
            isVisible = isSelected,
            scale = chromeScale,
            modifier = Modifier.matchParentSize(),
            alphaScale = if (gesturesEnabled) 1f else 0.4f
        )
        if (canTransform && showHandles && boxSize != IntSize.Zero) {
            var startTransform by remember { mutableStateOf(localTransform) }
            var centerRoot by remember { mutableStateOf(Offset.Zero) }
            var startDistance by remember { mutableStateOf(1f) }
            var startAngle by remember { mutableStateOf(0f) }

            // 标准选框:三个角缩放(右上角让给删除),两侧改文字框宽,顶部旋转。
            // 角/边手柄统一用图文卡片同款 HandleDot(白边主色圆点)
            listOf(
                Triple(Alignment.TopStart, -1, -1),
                Triple(Alignment.BottomStart, -1, 1),
                Triple(Alignment.BottomEnd, 1, 1),
            ).forEach { (corner, signX, signY) ->
                LayerTransformHandle(
                    description = stringResource(R.string.studio_layer_scale),
                    alignment = corner,
                    offsetX = (signX * HANDLE_HALF_DP).dp,
                    offsetY = (signY * HANDLE_HALF_DP).dp,
                    inverseScale = inverseScale,
                    counterRotation = counterRotation,
                    onStart = { root ->
                        startTransform = localTransform
                        centerRoot = elementCoordinates?.localToRoot(
                            Offset(boxSize.width / 2f, boxSize.height / 2f)
                        ) ?: root
                        startDistance = (root - centerRoot).getDistance().coerceAtLeast(1f)
                        start()
                    },
                    onDrag = { root ->
                        update(
                            startTransform.copy(
                                scale = (startTransform.scale *
                                    (root - centerRoot).getDistance() / startDistance)
                                    .coerceIn(0.1f, 10f)
                            )
                        )
                    },
                    onEnd = finish
                ) {
                    HandleDot()
                }
            }
            LayerTransformHandle(
                description = stringResource(R.string.studio_layer_rotate),
                alignment = Alignment.TopCenter,
                // 旋转手柄中心恒定在顶边中点上方 16dp(屏幕距离)
                offsetY = -(HANDLE_HALF_DP.dp + 16.dp * inverseScale),
                inverseScale = inverseScale,
                counterRotation = counterRotation,
                onStart = { root ->
                    startTransform = localTransform
                    centerRoot = elementCoordinates?.localToRoot(
                        Offset(boxSize.width / 2f, boxSize.height / 2f)
                    ) ?: root
                    startAngle = atan2(root.y - centerRoot.y, root.x - centerRoot.x)
                    start()
                },
                onDrag = { root ->
                    val angle = atan2(root.y - centerRoot.y, root.x - centerRoot.x)
                    update(
                        startTransform.copy(
                            rotation = startTransform.rotation +
                                Math.toDegrees((angle - startAngle).toDouble()).toFloat()
                        )
                    )
                },
                onEnd = finish
            ) {
                HandleIcon(MaterialIcons.Outlined.RotateRight)
            }
            resizeConfig?.let { config ->
                listOf(-1, 1).forEach { edge ->
                    var startRoot by remember { mutableStateOf(Offset.Zero) }
                    var axisRoot by remember { mutableStateOf(Offset(1f, 0f)) }
                    var startSize by remember { mutableStateOf(IntSize.Zero) }
                    LayerTransformHandle(
                        description = stringResource(R.string.studio_layer_resize_width),
                        alignment = if (edge < 0) Alignment.CenterStart else Alignment.CenterEnd,
                        // 改框宽手柄中心恒定在边中点外 8dp(屏幕距离)
                        offsetX = (edge * (HANDLE_HALF_DP + 8f * inverseScale)).dp,
                        inverseScale = inverseScale,
                        counterRotation = counterRotation,
                        onStart = { root ->
                            startRoot = root
                            startSize = boxSize
                            startTransform = localTransform
                            axisRoot = elementCoordinates?.let {
                                it.localToRoot(Offset(1f, 0f)) - it.localToRoot(Offset.Zero)
                            } ?: Offset(1f, 0f)
                            changing = true
                            config.onGestureStart()
                        },
                        onDrag = { root ->
                            val drag = root - startRoot
                            val projected = (drag.x * axisRoot.x + drag.y * axisRoot.y) /
                                (axisRoot.x * axisRoot.x + axisRoot.y * axisRoot.y).coerceAtLeast(0.001f)
                            val width = (startSize.width + edge * projected)
                                .coerceIn(config.minWidthPx, config.maxWidthPx)
                            val dw = startSize.width - width
                            val shift = Offset(-edge * dw / 2f, 0f)
                                .rotateBy(startTransform.rotation) * startTransform.scale
                            val left = startTransform.centerX * canvasWidth -
                                startSize.width / 2f + dw / 2f + shift.x
                            val top = startTransform.centerY * canvasHeight -
                                startSize.height / 2f + shift.y
                            localTransform = startTransform.copy(
                                centerX = ((left + width / 2f) / canvasWidth).coerceIn(0f, 1f),
                                centerY = ((top + startSize.height / 2f) / canvasHeight).coerceIn(0f, 1f)
                            )
                            config.onResize(width, startSize.height.toFloat(), left, top)
                        },
                        onEnd = finish
                    ) {
                        HandleDot()
                    }
                }
            }
        }
        if (canTransform && showHandles && onDelete != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    // 删除按钮中心压在右上角点上,缩放/旋转下位置恒定
                    .offset(x = HANDLE_HALF_DP.dp, y = (-HANDLE_HALF_DP).dp)
                    .graphicsLayer {
                        scaleX = inverseScale
                        scaleY = inverseScale
                        rotationZ = counterRotation
                    }
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onDelete)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(24.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.markup_delete_layer),
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/** 手柄热区半宽:标记中心压在角/边上时的对齐补偿量 */
private const val HANDLE_HALF_DP = 24

/** Root coordinates avoid feedback from a handle moving as its layer is transformed. */
@Composable
private fun BoxScope.LayerTransformHandle(
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

/** 圆形图标手柄视觉(旋转/改文字框宽):主色圆底 + onPrimaryContainer 图标 */
@Composable
private fun HandleIcon(icon: ImageVector) {
    Box(
        modifier = Modifier.size(24.dp).clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(18.dp)
        )
    }
}

private fun Offset.rotateBy(degrees: Float): Offset {
    val radians = Math.toRadians(degrees.toDouble())
    return Offset(
        x = (x * cos(radians) - y * sin(radians)).toFloat(),
        y = (x * sin(radians) + y * cos(radians)).toFloat()
    )
}
