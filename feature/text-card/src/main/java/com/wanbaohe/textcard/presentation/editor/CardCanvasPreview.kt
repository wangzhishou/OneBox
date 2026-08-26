package com.wanbaohe.textcard.presentation.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.resources.emoji.Emoji
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.settings.presentation.model.toUiFont
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.modifier.meshGradient
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.BackgroundSpec
import com.wanbaohe.textcard.domain.model.CardTextAlignment
import com.wanbaohe.textcard.domain.model.DecorationSpec
import com.wanbaohe.textcard.domain.model.ElementLayer
import com.wanbaohe.textcard.domain.model.ElementTransform
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.domain.model.TextCardRenderState
import com.wanbaohe.textcard.domain.render.CardLayout
import com.wanbaohe.textcard.domain.render.MESH_RESOLUTION
import com.wanbaohe.textcard.domain.render.toPointPairs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 卡片画布预览(Compose):与导出渲染器共用 [CardLayout] 几何/颜色常量。
 * 渲染顺序:背景(钉最底,可经 [onBackgroundDrag] 拖动相册图/点按取消选中)
 * → 按元素图层 z 序逐层画文字块/装饰贴纸。
 * 元素手势(参照 markup-layers EditBox):点按=选中([onElementTap],选中态画边框),
 * 选中后单指拖动 + 双指缩放/旋转(detectTransformGestures,
 * pan 乘缩放并按当前旋转角转回画布坐标系后归一化,fit 缩放天然抵消);
 * [onElementTransform] 回调绝对值(offsetX/offsetY/scale/rotation)。
 */
@Composable
fun CardCanvasPreview(
    state: TextCardRenderState,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    onElementTap: (String) -> Unit = {},
    onElementTransform: (String, Float, Float, Float, Float) -> Unit = { _, _, _, _, _ -> },
    onElementDelete: (String) -> Unit = {},
    onTextBoxResize: (String, Float, Float, Float, Float) -> Unit = { _, _, _, _, _ -> },
    selectedElementId: String? = null,
    editingTextBlockId: String? = null,
    onTextChange: (String, String) -> Unit = { _, _ -> },
    onTextEditCommit: () -> Unit = {},
    onCanvasTap: () -> Unit = {},
    onBackgroundDrag: (Float, Float) -> Unit = { _, _ -> },
) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(state.canvas.aspectRatio)
            .clip(RoundedCornerShape(cornerRadius))
            .clipToBounds()
    ) {
        val canvasWidthPx = constraints.maxWidth.toFloat()
        val canvasHeightPx = constraints.maxHeight.toFloat()

        if (state.backgroundVisible) {
            BackgroundLayerContent(
                state = state,
                canvasWidthPx = canvasWidthPx,
                canvasHeightPx = canvasHeightPx,
                onBackgroundDrag = onBackgroundDrag,
                onCanvasTap = onCanvasTap,
                modifier = Modifier.fillMaxSize()
            )
        }

        state.visibleLayers.forEach { layer ->
            when (layer.kind) {
                ElementLayer.Kind.Text -> state.blockOf(layer.elementId)?.let { block ->
                    key(block.id) {
                        CardTextElement(
                            block = block,
                            isSelected = selectedElementId == block.id && !layer.locked,
                            isEditing = editingTextBlockId == block.id,
                            // 文字块至少保留一块,最后一块不出删除按钮
                            canDelete = state.textBlocks.size > 1,
                            canvasWidthPx = canvasWidthPx,
                            canvasHeightPx = canvasHeightPx,
                            onElementTap = onElementTap,
                            onElementTransform = onElementTransform,
                            onElementDelete = onElementDelete,
                            onTextBoxResize = onTextBoxResize,
                            onTextChange = onTextChange,
                            onTextEditCommit = onTextEditCommit
                        )
                    }
                }

                ElementLayer.Kind.Decoration -> state.decorationOf(layer.elementId)?.let {
                    key(it.id) {
                        CardDecorationElement(
                            decoration = it,
                            isSelected = selectedElementId == it.id && !layer.locked,
                            canDelete = true,
                            canvasWidthPx = canvasWidthPx,
                            canvasHeightPx = canvasHeightPx,
                            onElementTap = onElementTap,
                            onElementTransform = onElementTransform,
                            onElementDelete = onElementDelete
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BackgroundLayerContent(
    state: TextCardRenderState,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    onBackgroundDrag: (Float, Float) -> Unit,
    onCanvasTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isImageBackground = state.background is BackgroundSpec.Image
    Box(
        modifier = modifier
            .background(Color(CardLayout.CARD_BASE_COLOR))
            .graphicsLayer { alpha = state.backgroundOpacity.coerceIn(0f, 1f) }
            // 点按空白=取消元素选中;背景为相册图时空白处拖动调整图片位置
            .pointerInput(canvasWidthPx, canvasHeightPx) {
                detectTapGestures { onCanvasTap() }
            }
            .pointerInput(isImageBackground, canvasWidthPx, canvasHeightPx) {
                if (!isImageBackground) return@pointerInput
                detectTransformGestures { _, pan, _, _ ->
                    onBackgroundDrag(pan.x / canvasWidthPx, pan.y / canvasHeightPx)
                }
            }
    ) {
        when (val background = state.background) {
            BackgroundSpec.None -> Unit
            is BackgroundSpec.Paper -> Canvas(modifier = Modifier.fillMaxSize()) {
                drawPaperTexture(background.kind)
            }

            is BackgroundSpec.Gradient -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .meshGradient(
                        points = background.toPointPairs(),
                        resolutionX = MESH_RESOLUTION,
                        resolutionY = MESH_RESOLUTION
                    )
            )

            // 居中裁剪铺满 + 归一化拖动偏移(与导出侧 RectF 平移一致);
            // 透明区域透出卡片底色,不画棋盘格
            is BackgroundSpec.Image -> Picture(
                model = background.uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                showTransparencyChecker = false,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = background.offsetX * canvasWidthPx
                        translationY = background.offsetY * canvasHeightPx
                    }
            )
        }
    }
}

/**
 * 元素容器:offset 定位 + 选中后 transform 手势 + 虚线选中框与拖拽手柄。
 * detectTransformGestures 的 pan 在元素本地(已变换)坐标系,乘新缩放并按
 * 新旋转角转回画布坐标系再归一化(同 markup-layers LayerTransform.applyGesture)。
 * [gesturesEnabled]=false(就地编辑态)时手势与手柄全部关闭,避免与文本选择/光标冲突,
 * 编辑态虚线框弱化显示。
 * [resizeConfig] 非空(文字块)= 8 向框尺寸手柄(改框宽/高,文字重排,字号不变),
 * 删除按钮经 [deleteAtBottom] 挪到底部居中独立位;为空(装饰)= 四角缩放手柄 + 右上删除。
 */
@Composable
private fun ElementBox(
    elementId: String,
    transform: ElementTransform,
    leftPx: Float,
    topPx: Float,
    isSelected: Boolean,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    onElementTap: (String) -> Unit,
    onElementTransform: (String, Float, Float, Float, Float) -> Unit,
    onDelete: (() -> Unit)? = null,
    deleteAtBottom: Boolean = false,
    gesturesEnabled: Boolean = true,
    width: Dp? = null,
    minHeight: Dp? = null,
    resizeConfig: BoxResizeConfig? = null,
    content: @Composable () -> Unit,
) {
    // 手势回调以「最新状态 + 本次增量」算绝对值:pointerInput 协程不随重组重启,
    // 必须经 rememberUpdatedState 取最新 transform,否则每次事件都从旧基准叠加,表现为拖不动
    val currentTransform by rememberUpdatedState(transform)
    val currentLeftPx by rememberUpdatedState(leftPx)
    val currentTopPx by rememberUpdatedState(topPx)
    var boxSizePx by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = Modifier
            .offset { IntOffset(leftPx.roundToInt(), topPx.roundToInt()) }
            .then(if (width != null) Modifier.width(width) else Modifier)
            .then(if (minHeight != null) Modifier.heightIn(min = minHeight) else Modifier)
            .onSizeChanged { boxSizePx = it }
            .graphicsLayer {
                scaleX = transform.scale
                scaleY = transform.scale
                rotationZ = transform.rotation
                transformOrigin = TransformOrigin.Center
            }
            .then(
                if (isSelected) {
                    Modifier.dashedBorder(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary.copy(
                            alpha = if (gesturesEnabled) 1f else 0.4f
                        ),
                        cornerRadius = 4.dp
                    )
                } else Modifier
            )
            // 选中态接管 transform 手势;点按(选中/再点编辑)由独立 tap 检测器承担,
            // 拖动消费位移后 tap 自动取消
            .pointerInput(elementId, isSelected, gesturesEnabled, canvasWidthPx, canvasHeightPx) {
                if (!isSelected || !gesturesEnabled) return@pointerInput
                detectTransformGestures { _, pan, zoom, rotation ->
                    val base = currentTransform
                    val newScale = (base.scale * zoom).coerceIn(0.2f, 5f)
                    val newRotation = base.rotation + rotation
                    val panInCanvas = (pan * newScale).rotateBy(newRotation)
                    onElementTransform(
                        elementId,
                        base.offsetX + panInCanvas.x / canvasWidthPx,
                        base.offsetY + panInCanvas.y / canvasHeightPx,
                        newScale,
                        newRotation
                    )
                }
            }
            .then(
                if (gesturesEnabled) {
                    Modifier.pointerInput(elementId) {
                        detectTapGestures { onElementTap(elementId) }
                    }
                } else Modifier
            )
    ) {
        content()
        // 删除按钮:独立 clickable,事件在子级被消费,不会触发元素的拖动/点选手势。
        // 装饰:选中框外侧右上角 X;文字块:底部居中独立位(避开 8 向尺寸手柄)
        if (isSelected && onDelete != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(if (deleteAtBottom) Alignment.BottomCenter else Alignment.TopEnd)
                    .offset(
                        x = if (deleteAtBottom) 0.dp else 12.dp,
                        y = if (deleteAtBottom) 40.dp else (-12).dp
                    )
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
                    .clickable(onClick = onDelete)
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.textcard_delete_selected),
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        // 选中框手柄:文字块 = 8 向框尺寸手柄;装饰 = 四角圆形缩放手柄。
        // 两者都有顶部中心旋转手柄;手柄随元素一起被 graphicsLayer 变换(旋转/缩放时跟随元素)
        if (isSelected && gesturesEnabled && boxSizePx != IntSize.Zero) {
            if (resizeConfig != null) {
                BoxResizeHandles(
                    sizeProvider = { boxSizePx },
                    leftProvider = { currentLeftPx },
                    topProvider = { currentTopPx },
                    transformProvider = { currentTransform },
                    config = resizeConfig
                )
                RotationHandle(
                    handlePx = with(LocalDensity.current) { HANDLE_SIZE.toPx() },
                    elementCenter = Offset(boxSizePx.width / 2f, boxSizePx.height / 2f),
                    elementId = elementId,
                    transformProvider = { currentTransform },
                    onElementTransform = onElementTransform
                )
            } else {
                SelectionHandles(
                    elementId = elementId,
                    sizePx = boxSizePx,
                    transformProvider = { currentTransform },
                    onElementTransform = onElementTransform
                )
            }
        }
    }
}

/** 文字框尺寸手柄配置:阈值均 px;onResize 回报新框宽高与锚点补偿后的 left/top(均 px) */
private class BoxResizeConfig(
    val minWidthPx: Float,
    val maxWidthPx: Float,
    val minHeightPx: Float,
    val maxHeightPx: Float,
    val onResize: (widthPx: Float, heightPx: Float, leftPx: Float, topPx: Float) -> Unit,
)

private val HANDLE_SIZE = 36.dp

/** 四角缩放手柄 + 顶部中心旋转手柄(全部在元素本地坐标系计算,与当前旋转/缩放无关) */
@Composable
private fun androidx.compose.foundation.layout.BoxScope.SelectionHandles(
    elementId: String,
    sizePx: IntSize,
    transformProvider: () -> ElementTransform,
    onElementTransform: (String, Float, Float, Float, Float) -> Unit,
) {
    val density = LocalDensity.current
    val handlePx = with(density) { HANDLE_SIZE.toPx() }
    val center = Offset(sizePx.width / 2f, sizePx.height / 2f)

    // 四角:handle 中心 = 角点
    val corners = listOf(
        Triple(Alignment.TopStart, Offset(0f, 0f), IntOffset(-1, -1)),
        Triple(Alignment.TopEnd, Offset(sizePx.width.toFloat(), 0f), IntOffset(1, -1)),
        Triple(Alignment.BottomStart, Offset(0f, sizePx.height.toFloat()), IntOffset(-1, 1)),
        Triple(Alignment.BottomEnd, Offset(sizePx.width.toFloat(), sizePx.height.toFloat()), IntOffset(1, 1)),
    )
    corners.forEach { (alignment, corner, offsetSign) ->
        ScaleHandle(
            alignment = alignment,
            cornerInElement = corner,
            offsetSign = offsetSign,
            handlePx = handlePx,
            elementCenter = center,
            elementId = elementId,
            transformProvider = transformProvider,
            onElementTransform = onElementTransform
        )
    }

    // 顶部中心旋转手柄(handle 中心在 (w/2, -H/2),即顶边中点上方)
    RotationHandle(
        handlePx = handlePx,
        elementCenter = center,
        elementId = elementId,
        transformProvider = transformProvider,
        onElementTransform = onElementTransform
    )
}

/** 手柄视觉:白边主色小圆点(热区 36dp,视觉 16dp) */
@Composable
private fun HandleDot() {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .border(2.dp, Color.White, CircleShape)
    )
}

/** 拖角缩放:触点与元素中心的距离比 × 起始缩放 */
@Composable
private fun androidx.compose.foundation.layout.BoxScope.ScaleHandle(
    alignment: Alignment,
    cornerInElement: Offset,
    offsetSign: IntOffset,
    handlePx: Float,
    elementCenter: Offset,
    elementId: String,
    transformProvider: () -> ElementTransform,
    onElementTransform: (String, Float, Float, Float, Float) -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .align(alignment)
            .offset {
                IntOffset(
                    x = (offsetSign.x * handlePx / 2).roundToInt(),
                    y = (offsetSign.y * handlePx / 2).roundToInt()
                )
            }
            .size(HANDLE_SIZE)
            .pointerInput(elementId, cornerInElement, elementCenter) {
                val half = handlePx / 2
                // 手势期起点(每次手势 onDragStart 重置,不跨手势共享)
                var startDist = 1f
                var startScale = 1f
                detectDragGestures(
                    onDragStart = { downOffset ->
                        val startPoint = cornerInElement - Offset(half, half) + downOffset
                        startDist = (startPoint - elementCenter).getDistance().coerceAtLeast(1f)
                        startScale = transformProvider().scale
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val point = cornerInElement - Offset(half, half) + change.position
                        val factor = (point - elementCenter).getDistance() / startDist
                        val t = transformProvider()
                        onElementTransform(
                            elementId,
                            t.offsetX,
                            t.offsetY,
                            (startScale * factor).coerceIn(0.2f, 5f),
                            t.rotation
                        )
                    }
                )
            }
    ) {
        HandleDot()
    }
}

/** 手柄规格:alignment=贴位,offsetSign=中心越边方向,edgeX/edgeY=-1 拖左/顶边、0 该轴不动、1 拖右/底边 */
private data class HandleSpec(
    val alignment: Alignment,
    val offsetSign: IntOffset,
    val edgeX: Int,
    val edgeY: Int,
)

/** 文字框 8 向尺寸手柄(四角 + 四边中点):拖手柄改框宽/高,文字重排,字号不变 */
@Composable
private fun androidx.compose.foundation.layout.BoxScope.BoxResizeHandles(
    sizeProvider: () -> IntSize,
    leftProvider: () -> Float,
    topProvider: () -> Float,
    transformProvider: () -> ElementTransform,
    config: BoxResizeConfig,
) {
    val density = LocalDensity.current
    val handlePx = with(density) { HANDLE_SIZE.toPx() }
    val specs = listOf(
        HandleSpec(Alignment.TopStart, IntOffset(-1, -1), edgeX = -1, edgeY = -1),
        HandleSpec(Alignment.TopEnd, IntOffset(1, -1), edgeX = 1, edgeY = -1),
        HandleSpec(Alignment.BottomStart, IntOffset(-1, 1), edgeX = -1, edgeY = 1),
        HandleSpec(Alignment.BottomEnd, IntOffset(1, 1), edgeX = 1, edgeY = 1),
        HandleSpec(Alignment.CenterStart, IntOffset(-1, 0), edgeX = -1, edgeY = 0),
        HandleSpec(Alignment.CenterEnd, IntOffset(1, 0), edgeX = 1, edgeY = 0),
        HandleSpec(Alignment.TopCenter, IntOffset(0, -1), edgeX = 0, edgeY = -1),
        HandleSpec(Alignment.BottomCenter, IntOffset(0, 1), edgeX = 0, edgeY = 1),
    )
    specs.forEach { spec ->
        ResizeHandle(
            spec = spec,
            handlePx = handlePx,
            sizeProvider = sizeProvider,
            leftProvider = leftProvider,
            topProvider = topProvider,
            transformProvider = transformProvider,
            config = config
        )
    }
}

/**
 * 单方向尺寸手柄:拖动改框宽/高,字号不变;对侧边为视觉锚点,旋转/缩放下
 * 经锚点补偿保持锚点不动。补偿公式:dw=startW−w',dh=startH−h',
 * shift = (dw/2, dh/2) + R(θ)·s·((ax−½)dw, (ay−½)dh),ax/ay 为锚点比例(0/½/1)。
 */
@Composable
private fun androidx.compose.foundation.layout.BoxScope.ResizeHandle(
    spec: HandleSpec,
    handlePx: Float,
    sizeProvider: () -> IntSize,
    leftProvider: () -> Float,
    topProvider: () -> Float,
    transformProvider: () -> ElementTransform,
    config: BoxResizeConfig,
) {
    // 锚点比例:拖右边锚左边(ax=0)、拖左边锚右边(ax=1)、不动轴锚中心(½)
    val anchorX = (1 - spec.edgeX) / 2f
    val anchorY = (1 - spec.edgeY) / 2f
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .align(spec.alignment)
            .offset {
                IntOffset(
                    x = (spec.offsetSign.x * handlePx / 2).roundToInt(),
                    y = (spec.offsetSign.y * handlePx / 2).roundToInt()
                )
            }
            .size(HANDLE_SIZE)
            .pointerInput(spec) {
                // 手势期起点(每次手势 onDragStart 重置,不跨手势共享)
                var drag = Offset.Zero
                var startW = 0f
                var startH = 0f
                var startLeft = 0f
                var startTop = 0f
                var startScale = 1f
                var startRotation = 0f
                detectDragGestures(
                    onDragStart = {
                        drag = Offset.Zero
                        val size = sizeProvider()
                        startW = size.width.toFloat()
                        startH = size.height.toFloat()
                        startLeft = leftProvider()
                        startTop = topProvider()
                        startScale = transformProvider().scale
                        startRotation = transformProvider().rotation
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        drag += dragAmount
                        val newW = (startW + spec.edgeX * drag.x)
                            .coerceIn(config.minWidthPx, config.maxWidthPx)
                        val newH = (startH + spec.edgeY * drag.y)
                            .coerceIn(config.minHeightPx, config.maxHeightPx)
                        val dw = startW - newW
                        val dh = startH - newH
                        val radians = Math.toRadians(startRotation.toDouble())
                        val scaledCos = cos(radians).toFloat() * startScale
                        val scaledSin = sin(radians).toFloat() * startScale
                        val anchorDw = (anchorX - 0.5f) * dw
                        val anchorDh = (anchorY - 0.5f) * dh
                        config.onResize(
                            newW,
                            newH,
                            startLeft + dw / 2 + (anchorDw * scaledCos - anchorDh * scaledSin),
                            startTop + dh / 2 + (anchorDw * scaledSin + anchorDh * scaledCos)
                        )
                    }
                )
            }
    ) {
        HandleDot()
    }
}

/** 顶部中心旋转手柄:触点绕元素中心的角度增量 */@Composable
private fun androidx.compose.foundation.layout.BoxScope.RotationHandle(
    handlePx: Float,
    elementCenter: Offset,
    elementId: String,
    transformProvider: () -> ElementTransform,
    onElementTransform: (String, Float, Float, Float, Float) -> Unit,
) {
    // handle 中心:元素顶边中点正上方半个手柄位
    val handleCenter = Offset(elementCenter.x, -handlePx / 2)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .offset(y = -HANDLE_SIZE)
            .size(HANDLE_SIZE)
            .pointerInput(elementId, elementCenter) {
                val half = handlePx / 2
                // 手势期起点(每次手势 onDragStart 重置,不跨手势共享)
                var startAngle = 0f
                var startRotation = 0f
                detectDragGestures(
                    onDragStart = { downOffset ->
                        val startPoint = handleCenter - Offset(half, half) + downOffset
                        startAngle = atan2(
                            startPoint.y - elementCenter.y,
                            startPoint.x - elementCenter.x
                        )
                        startRotation = transformProvider().rotation
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val point = handleCenter - Offset(half, half) + change.position
                        val angle = atan2(
                            point.y - elementCenter.y,
                            point.x - elementCenter.x
                        )
                        val t = transformProvider()
                        onElementTransform(
                            elementId,
                            t.offsetX,
                            t.offsetY,
                            t.scale,
                            startRotation + Math.toDegrees((angle - startAngle).toDouble()).toFloat()
                        )
                    }
                )
            }
    ) {
        HandleDot()
    }
}

/** 选中态虚线圆角边框 */
private fun Modifier.dashedBorder(
    width: Dp,
    color: Color,
    cornerRadius: Dp,
): Modifier = drawBehind {
    val strokeWidth = width.toPx()
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = Stroke(
            width = strokeWidth,
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(6.dp.toPx(), 4.dp.toPx())
            )
        )
    )
}

/** 文字块元素:编辑态就地渲染 BasicTextField(原位同尺寸同样式),手势关闭。
 * 框宽 = widthRatio·画布宽(文字在框内折行),框高 = max(内容高, heightRatio·画布高);
 * 8 向尺寸手柄经 [onTextBoxResize] 回报新框尺寸与锚点补偿后的位置。 */
@Composable
private fun CardTextElement(
    block: TextBlock,
    isSelected: Boolean,
    isEditing: Boolean,
    canDelete: Boolean,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    onElementTap: (String) -> Unit,
    onElementTransform: (String, Float, Float, Float, Float) -> Unit,
    onElementDelete: (String) -> Unit,
    onTextBoxResize: (String, Float, Float, Float, Float) -> Unit,
    onTextChange: (String, String) -> Unit,
    onTextEditCommit: () -> Unit,
) {
    if (block.content.isBlank()) return
    val density = LocalDensity.current
    val paddingPx = canvasWidthPx * CardLayout.CONTENT_PADDING_RATIO
    val baseTopPx = canvasWidthPx * block.baseTopRatio
    ElementBox(
        elementId = block.id,
        transform = block,
        leftPx = paddingPx + block.offsetX * canvasWidthPx,
        topPx = baseTopPx + block.offsetY * canvasHeightPx,
        isSelected = isSelected,
        canvasWidthPx = canvasWidthPx,
        canvasHeightPx = canvasHeightPx,
        onElementTap = onElementTap,
        onElementTransform = onElementTransform,
        // 编辑态也保留删除(底部居中独立位,打字时不会被误触区覆盖)
        onDelete = if (canDelete) {
            { onElementDelete(block.id) }
        } else null,
        deleteAtBottom = true,
        gesturesEnabled = !isEditing,
        width = with(density) { (canvasWidthPx * block.widthRatio).toDp() },
        minHeight = if (block.heightRatio > 0f) {
            with(density) { (canvasHeightPx * block.heightRatio).toDp() }
        } else null,
        resizeConfig = BoxResizeConfig(
            minWidthPx = canvasWidthPx * CardLayout.MIN_TEXT_WIDTH_RATIO,
            maxWidthPx = canvasWidthPx,
            minHeightPx = canvasHeightPx * CardLayout.MIN_TEXT_HEIGHT_RATIO,
            maxHeightPx = canvasHeightPx,
            onResize = { newWPx, newHPx, newLeftPx, newTopPx ->
                onTextBoxResize(
                    block.id,
                    newWPx / canvasWidthPx,
                    newHPx / canvasHeightPx,
                    (newLeftPx - paddingPx) / canvasWidthPx,
                    (newTopPx - baseTopPx) / canvasHeightPx
                )
            }
        )
    ) {
        if (isEditing) {
            InPlaceTextEditor(
                block = block,
                baseSizePx = canvasWidthPx * block.baseSizeRatio,
                onTextChange = { onTextChange(block.id, it) },
                onCommit = onTextEditCommit,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            CardText(
                block = block,
                baseSizePx = canvasWidthPx * block.baseSizeRatio,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 就地文字编辑:原位同样式 BasicTextField(背景透明无装饰线),
 * 进入时自动聚焦弹键盘、光标落末尾;内容实时经 [onTextChange] 写回组件。
 */
@Composable
private fun InPlaceTextEditor(
    block: TextBlock,
    baseSizePx: Float,
    onTextChange: (String) -> Unit,
    onCommit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var fieldValue by remember(block.id) {
        mutableStateOf(TextFieldValue(block.content, TextRange(block.content.length)))
    }
    LaunchedEffect(block.id) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    BasicTextField(
        value = fieldValue,
        onValueChange = { value ->
            fieldValue = value
            onTextChange(value.text)
        },
        textStyle = cardTextStyle(block, baseSizePx),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onCommit() }),
        modifier = modifier
            .graphicsLayer { alpha = block.alpha.coerceIn(0f, 1f) }
            // 编辑态输入框内留白,避免文字贴着虚线框
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .focusRequester(focusRequester)
    )
}

/** 装饰贴纸元素:透明底,不画棋盘格 */
@Composable
private fun CardDecorationElement(
    decoration: DecorationSpec,
    isSelected: Boolean,
    canDelete: Boolean,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    onElementTap: (String) -> Unit,
    onElementTransform: (String, Float, Float, Float, Float) -> Unit,
    onElementDelete: (String) -> Unit,
) {
    val emojis = Emoji.allIcons()
    val uri = emojis.getOrNull(decoration.emojiIndex) ?: return
    val density = LocalDensity.current
    val sizePx = canvasWidthPx * CardLayout.DECORATION_SIZE_RATIO
    ElementBox(
        elementId = decoration.id,
        transform = decoration,
        leftPx = decoration.offsetX * canvasWidthPx,
        topPx = decoration.offsetY * canvasHeightPx,
        isSelected = isSelected,
        canvasWidthPx = canvasWidthPx,
        canvasHeightPx = canvasHeightPx,
        onElementTap = onElementTap,
        onElementTransform = onElementTransform,
        onDelete = if (canDelete) {
            { onElementDelete(decoration.id) }
        } else null
    ) {
        Picture(
            model = uri,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            showTransparencyChecker = false,
            modifier = Modifier
                .size(with(density) { sizePx.toDp() })
                .graphicsLayer { alpha = decoration.alpha.coerceIn(0f, 1f) }
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

/** 单个文字块预览:字号 px→sp 等比缩放,letterSpacing(em)/行距倍率/对齐/粗斜与导出侧一致 */
@Composable
private fun CardText(
    block: TextBlock,
    baseSizePx: Float,
    modifier: Modifier = Modifier,
) {
    if (block.content.isBlank()) return
    Text(
        text = block.content,
        style = cardTextStyle(block, baseSizePx),
        modifier = modifier.graphicsLayer { alpha = block.alpha.coerceIn(0f, 1f) }
    )
}

/** 文字块统一样式:CardText 与就地编辑器共用,保证编辑态与渲染态观感一致 */
@Composable
private fun cardTextStyle(
    block: TextBlock,
    baseSizePx: Float,
): TextStyle {
    val density = LocalDensity.current
    val fontSize = with(density) { (baseSizePx * block.sizeScale).toSp() }
    return TextStyle(
        color = Color(block.color),
        fontSize = fontSize,
        lineHeight = (fontSize.value * block.lineSpacingMultiplier).sp,
        letterSpacing = block.letterSpacingEm.em,
        fontFamily = block.font.toUiFont().fontFamily,
        fontWeight = if (block.isBold) FontWeight.Bold else FontWeight.Normal,
        fontStyle = if (block.isItalic) FontStyle.Italic else FontStyle.Normal,
        textAlign = when (block.alignment) {
            CardTextAlignment.Left -> TextAlign.Start
            CardTextAlignment.Center -> TextAlign.Center
            CardTextAlignment.Right -> TextAlign.End
            CardTextAlignment.Justify -> TextAlign.Justify
        }
    )
}
