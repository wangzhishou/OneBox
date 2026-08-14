package com.wanbaohe.markuplayers.presentation.tools.crop

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFlip
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRotateLeft
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRotateRight
import com.shifenmiao.base.ui.button.CancelButton
import com.shifenmiao.base.ui.button.ConfirmButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTopAppBar
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTopAppBarType
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.text.marquee
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.NormalizedRect
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent
import com.wanbaohe.markuplayers.presentation.tools.adjust.toColorMatrixValues
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import androidx.compose.ui.graphics.ColorMatrix as ComposeColorMatrix

/**
 * 裁剪旋转全屏工具页(设计稿「裁剪与旋转」):
 * 顶栏(取消/标题/重置+确认) + 自绘裁剪框画布 + 旋转刻度盘 + 底部比例/翻转面板。
 *
 * 会话状态只在页内 remember:预览图经 graphicsLayer 呈现 pending 旋转/翻转,
 * 裁剪框记录为相对「旋转后包围盒」的归一化矩形;确认时把 自由旋转+90° 步进+翻转+裁剪
 * 合成一次变换交给 [MarkupLayersComponent.applyBaseTransform] 作用于全分辨率原图。
 * 取消直接退出,不改任何状态。
 */
@Composable
fun CropToolScreen(component: MarkupLayersComponent) {
    val session = remember { CropSessionState() }
    val close = { component.setActiveTool(null) }
    val confirm = {
        if (!session.isUntouched) {
            component.applyBaseTransform(
                rotationSteps = session.rotationSteps,
                freeRotation = session.freeRotation,
                flipHorizontal = session.flipHorizontal,
                flipVertical = session.flipVertical,
                cropRect = session.cropRect
            )
        }
        close()
    }
    BackHandler(onBack = close)

    Column(modifier = Modifier.fillMaxSize()) {
        CropTopBar(onCancel = close, onReset = session::reset, onConfirm = confirm)
        CropCanvasArea(
            component = component,
            session = session,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        RotationDial(
            value = session.freeRotation,
            onValueChange = { session.freeRotation = it }
        )
        CropControlPanel(
            session = session,
            bitmapWidth = component.bitmap?.width ?: 1,
            bitmapHeight = component.bitmap?.height ?: 1
        )
    }
}

/** 裁剪会话状态:均为 pending 值,确认前不触碰 component */
private class CropSessionState {
    var rotationSteps by mutableStateOf(0)
    var freeRotation by mutableStateOf(0f)
    var flipHorizontal by mutableStateOf(false)
    var flipVertical by mutableStateOf(false)
    var aspectMode by mutableStateOf(CropAspectMode.Free)
    var cropRect by mutableStateOf(NormalizedRect.Full)

    val totalDegrees: Float get() = rotationSteps * 90f + freeRotation

    val isUntouched: Boolean
        get() = rotationSteps == 0 && freeRotation == 0f &&
            !flipHorizontal && !flipVertical &&
            aspectMode == CropAspectMode.Free && cropRect == NormalizedRect.Full

    fun reset() {
        rotationSteps = 0
        freeRotation = 0f
        flipHorizontal = false
        flipVertical = false
        aspectMode = CropAspectMode.Free
        cropRect = NormalizedRect.Full
    }
}

/** 裁剪比例选项,ratio 为宽:高;Free 为自由比例 */
private enum class CropAspectMode(val ratio: Float?) {
    OneToOne(1f),
    FourToThree(4f / 3f),
    SixteenToNine(16f / 9f),
    Free(null)
}

@Composable
private fun CropTopBar(
    onCancel: () -> Unit,
    onReset: () -> Unit,
    onConfirm: () -> Unit,
) {
    EnhancedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.markup_crop_title),
                modifier = Modifier.marquee()
            )
        },
        type = EnhancedTopAppBarType.Center,
        navigationIcon = {
            CancelButton(
                text = stringResource(R.string.markup_cancel),
                onClick = onCancel
            )
        },
        actions = {
            // 「重置」为次级操作,保留文字按钮
            TextButton(onClick = onReset) {
                Text(stringResource(R.string.markup_reset))
            }
            ConfirmButton(
                text = stringResource(R.string.markup_confirm),
                onClick = onConfirm
            )
        }
    )
}

/**
 * 画布区:外层 Box 尺寸 = 旋转后包围盒(适配可用空间),内层 Picture 以原图尺寸
 * 居中并经 graphicsLayer 旋转/翻转,视觉恰好填满包围盒;裁剪框在包围盒坐标系内交互。
 */
@Composable
private fun CropCanvasArea(
    component: MarkupLayersComponent,
    session: CropSessionState,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .clipToBounds()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = component.bitmap ?: return@BoxWithConstraints
        val display = component.displayBitmap ?: bitmap
        val density = LocalDensity.current
        val degrees = session.totalDegrees
        val radians = Math.toRadians(degrees.toDouble())
        val cos = abs(cos(radians)).toFloat()
        val sin = abs(sin(radians)).toFloat()
        val rotatedWidth = bitmap.width * cos + bitmap.height * sin
        val rotatedHeight = bitmap.width * sin + bitmap.height * cos
        val fitScale = min(
            constraints.maxWidth / rotatedWidth,
            constraints.maxHeight / rotatedHeight
        )
        val boxWidth = rotatedWidth * fitScale
        val boxHeight = rotatedHeight * fitScale

        Box(
            modifier = Modifier.size(
                width = with(density) { boxWidth.toDp() },
                height = with(density) { boxHeight.toDp() }
            )
        ) {
            val imageBitmap = remember(display) { display.asImageBitmap() }
            val adjustments = component.baseAdjustments
            val colorFilter = remember(adjustments) {
                if (adjustments.isNeutral) null
                else ColorFilter.colorMatrix(ComposeColorMatrix(adjustments.toColorMatrixValues()))
            }
            Picture(
                model = imageBitmap,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                colorFilter = colorFilter,
                showTransparencyChecker = false,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(
                        width = with(density) { (bitmap.width * fitScale).toDp() },
                        height = with(density) { (bitmap.height * fitScale).toDp() }
                    )
                    .graphicsLayer {
                        rotationZ = degrees
                        scaleX = if (session.flipHorizontal) -1f else 1f
                        scaleY = if (session.flipVertical) -1f else 1f
                    }
            )
            CropOverlay(
                session = session,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

// ---------------- 裁剪框(手柄/九宫格/压暗 + 拖动手势) ----------------

private enum class CropHandle {
    TopLeft, TopRight, BottomLeft, BottomRight,
    Top, Bottom, Left, Right,
    Move
}

@Composable
private fun CropOverlay(
    session: CropSessionState,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val touchSlopPx = with(density) { 28.dp.toPx() }
    val minSizePx = with(density) { 48.dp.toPx() }
    val scrimColor = Color.Black.copy(alpha = 0.5f)
    val frameColor = Color.White
    val gridColor = Color.White.copy(alpha = 0.6f)
    val borderStroke = with(density) { 1.5.dp.toPx() }
    val gridStroke = with(density) { 0.5.dp.toPx() }
    val handleStroke = with(density) { 3.5.dp.toPx() }
    val handleLength = with(density) { 20.dp.toPx() }

    Canvas(
        modifier = modifier.cropGestures(session, touchSlopPx, minSizePx)
    ) {
        val rect = session.cropRect
        val left = rect.left * size.width
        val top = rect.top * size.height
        val right = rect.right * size.width
        val bottom = rect.bottom * size.height

        // 框外压暗(上/下/左/右四条带)
        drawRect(scrimColor, size = Size(size.width, top))
        drawRect(
            scrimColor,
            topLeft = Offset(0f, bottom),
            size = Size(size.width, size.height - bottom)
        )
        drawRect(scrimColor, topLeft = Offset(0f, top), size = Size(left, bottom - top))
        drawRect(
            scrimColor,
            topLeft = Offset(right, top),
            size = Size(size.width - right, bottom - top)
        )

        // 九宫格辅助线
        for (i in 1..2) {
            val x = left + (right - left) * i / 3f
            drawLine(gridColor, Offset(x, top), Offset(x, bottom), gridStroke)
            val y = top + (bottom - top) * i / 3f
            drawLine(gridColor, Offset(left, y), Offset(right, y), gridStroke)
        }

        // 边框
        drawRect(
            frameColor,
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top),
            style = Stroke(width = borderStroke)
        )

        // 四角 L 形手柄
        drawCornerHandle(Offset(left, top), 1f, 1f, handleLength, handleStroke, frameColor)
        drawCornerHandle(Offset(right, top), -1f, 1f, handleLength, handleStroke, frameColor)
        drawCornerHandle(Offset(left, bottom), 1f, -1f, handleLength, handleStroke, frameColor)
        drawCornerHandle(Offset(right, bottom), -1f, -1f, handleLength, handleStroke, frameColor)

        // 四边中点手柄(短杠)
        val centerX = (left + right) / 2f
        val centerY = (top + bottom) / 2f
        val bar = handleLength / 2f
        drawLine(
            frameColor, Offset(centerX - bar, top), Offset(centerX + bar, top), handleStroke
        )
        drawLine(
            frameColor, Offset(centerX - bar, bottom), Offset(centerX + bar, bottom), handleStroke
        )
        drawLine(
            frameColor, Offset(left, centerY - bar), Offset(left, centerY + bar), handleStroke
        )
        drawLine(
            frameColor, Offset(right, centerY - bar), Offset(right, centerY + bar), handleStroke
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCornerHandle(
    corner: Offset,
    dirX: Float,
    dirY: Float,
    length: Float,
    strokeWidth: Float,
    color: Color,
) {
    drawLine(
        color,
        start = corner,
        end = Offset(corner.x + dirX * length, corner.y),
        strokeWidth = strokeWidth
    )
    drawLine(
        color,
        start = corner,
        end = Offset(corner.x, corner.y + dirY * length),
        strokeWidth = strokeWidth
    )
}

/** 裁剪框手势:角/边手柄缩放(锁比例时等比),框内拖动整体平移 */
private fun Modifier.cropGestures(
    session: CropSessionState,
    touchSlopPx: Float,
    minSizePx: Float,
): Modifier = pointerInput(touchSlopPx, minSizePx) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val handle = hitTestHandle(
            position = down.position,
            rect = session.cropRect,
            boxSize = size,
            slop = touchSlopPx
        ) ?: return@awaitEachGesture
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) break
            if (change.positionChange() != Offset.Zero) {
                session.cropRect = session.cropRect.draggedBy(
                    handle = handle,
                    position = change.position,
                    delta = change.positionChange(),
                    boxSize = size,
                    minSizePx = minSizePx,
                    aspect = session.aspectMode.ratio
                )
                change.consume()
            }
        }
    }
}

private fun hitTestHandle(
    position: Offset,
    rect: NormalizedRect,
    boxSize: IntSize,
    slop: Float,
): CropHandle? {
    val left = rect.left * boxSize.width
    val top = rect.top * boxSize.height
    val right = rect.right * boxSize.width
    val bottom = rect.bottom * boxSize.height
    val corners = listOf(
        CropHandle.TopLeft to Offset(left, top),
        CropHandle.TopRight to Offset(right, top),
        CropHandle.BottomLeft to Offset(left, bottom),
        CropHandle.BottomRight to Offset(right, bottom)
    )
    corners.forEach { (handle, corner) ->
        if ((position - corner).getDistance() <= slop) return handle
    }
    val edges = listOf(
        CropHandle.Top to Offset((left + right) / 2f, top),
        CropHandle.Bottom to Offset((left + right) / 2f, bottom),
        CropHandle.Left to Offset(left, (top + bottom) / 2f),
        CropHandle.Right to Offset(right, (top + bottom) / 2f)
    )
    edges.forEach { (handle, edge) ->
        if ((position - edge).getDistance() <= slop) return handle
    }
    if (position.x in left..right && position.y in top..bottom) return CropHandle.Move
    return null
}

/** 下限优先的收敛:上限不足下限时退化为下限(避免 coerceIn 区间倒置崩溃) */
private fun Float.clampLower(lower: Float, upper: Float): Float =
    coerceIn(lower, upper.coerceAtLeast(lower))

/** 上限优先的收敛:下限超过上限时退化为上限 */
private fun Float.clampUpper(lower: Float, upper: Float): Float =
    coerceIn(lower.coerceAtMost(upper), upper)

/** 在包围盒像素空间完成一次拖动计算,结果换回归一化矩形 */
private fun NormalizedRect.draggedBy(
    handle: CropHandle,
    position: Offset,
    delta: Offset,
    boxSize: IntSize,
    minSizePx: Float,
    aspect: Float?,
): NormalizedRect {
    val boxWidth = boxSize.width.toFloat()
    val boxHeight = boxSize.height.toFloat()
    var left = this.left * boxWidth
    var top = this.top * boxHeight
    var right = this.right * boxWidth
    var bottom = this.bottom * boxHeight
    val minWidth = min(minSizePx, boxWidth)
    val minHeight = min(minSizePx, boxHeight)

    if (handle == CropHandle.Move) {
        val dx = delta.x.coerceIn(-left, boxWidth - right)
        val dy = delta.y.coerceIn(-top, boxHeight - bottom)
        left += dx
        right += dx
        top += dy
        bottom += dy
    } else if (aspect == null) {
        when (handle) {
            CropHandle.TopLeft -> {
                left = position.x.clampLower(0f, right - minWidth)
                top = position.y.clampLower(0f, bottom - minHeight)
            }

            CropHandle.TopRight -> {
                right = position.x.clampUpper(left + minWidth, boxWidth)
                top = position.y.clampLower(0f, bottom - minHeight)
            }

            CropHandle.BottomLeft -> {
                left = position.x.clampLower(0f, right - minWidth)
                bottom = position.y.clampUpper(top + minHeight, boxHeight)
            }

            CropHandle.BottomRight -> {
                right = position.x.clampUpper(left + minWidth, boxWidth)
                bottom = position.y.clampUpper(top + minHeight, boxHeight)
            }

            CropHandle.Top -> top = position.y.clampLower(0f, bottom - minHeight)
            CropHandle.Bottom -> bottom = position.y.clampUpper(top + minHeight, boxHeight)
            CropHandle.Left -> left = position.x.clampLower(0f, right - minWidth)
            CropHandle.Right -> right = position.x.clampUpper(left + minWidth, boxWidth)
            CropHandle.Move -> Unit
        }
    } else {
        val resized = resizeLocked(
            handle = handle,
            position = position,
            left = left,
            top = top,
            right = right,
            bottom = bottom,
            boxWidth = boxWidth,
            boxHeight = boxHeight,
            minWidth = minWidth,
            aspect = aspect
        )
        left = resized[0]
        top = resized[1]
        right = resized[2]
        bottom = resized[3]
    }
    return NormalizedRect(
        left = left / boxWidth,
        top = top / boxHeight,
        right = right / boxWidth,
        bottom = bottom / boxHeight
    )
}

/**
 * 锁定比例的缩放:角手柄以对角为锚点,边手柄以框中心为锚点等比缩放。
 * 返回 [left, top, right, bottom](包围盒像素),已收敛在盒内。
 */
private fun resizeLocked(
    handle: CropHandle,
    position: Offset,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    boxWidth: Float,
    boxHeight: Float,
    minWidth: Float,
    aspect: Float,
): FloatArray {
    when (handle) {
        CropHandle.TopLeft, CropHandle.TopRight,
        CropHandle.BottomLeft, CropHandle.BottomRight -> {
            val anchorX = if (handle == CropHandle.TopLeft || handle == CropHandle.BottomLeft) {
                right
            } else left
            val anchorY = if (handle == CropHandle.TopLeft || handle == CropHandle.TopRight) {
                bottom
            } else top
            val rawWidth = abs(position.x - anchorX)
            val rawHeight = abs(position.y - anchorY)
            val maxWidth = if (anchorX == right) anchorX else boxWidth - anchorX
            val maxHeight = if (anchorY == bottom) anchorY else boxHeight - anchorY
            val upper = min(maxWidth, maxHeight * aspect).coerceAtLeast(0f)
            val newWidth = max(rawWidth, rawHeight * aspect)
                .clampLower(minWidth, upper)
            val newHeight = newWidth / aspect
            val newLeft = if (handle == CropHandle.TopLeft || handle == CropHandle.BottomLeft) {
                anchorX - newWidth
            } else anchorX
            val newTop = if (handle == CropHandle.TopLeft || handle == CropHandle.TopRight) {
                anchorY - newHeight
            } else anchorY
            return floatArrayOf(newLeft, newTop, newLeft + newWidth, newTop + newHeight)
        }

        else -> {
            val centerX = (left + right) / 2f
            val centerY = (top + bottom) / 2f
            val maxWidth = 2f * min(centerX, boxWidth - centerX)
            val maxHeight = 2f * min(centerY, boxHeight - centerY)
            val rawWidth: Float
            val rawHeight: Float
            when (handle) {
                CropHandle.Left -> {
                    rawWidth = right - position.x
                    rawHeight = rawWidth / aspect
                }

                CropHandle.Right -> {
                    rawWidth = position.x - left
                    rawHeight = rawWidth / aspect
                }

                CropHandle.Top -> {
                    rawHeight = bottom - position.y
                    rawWidth = rawHeight * aspect
                }

                CropHandle.Bottom -> {
                    rawHeight = position.y - top
                    rawWidth = rawHeight * aspect
                }

                else -> return floatArrayOf(left, top, right, bottom)
            }
            val newWidth = rawWidth.clampLower(minWidth, min(maxWidth, maxHeight * aspect))
            val newHeight = newWidth / aspect
            return floatArrayOf(
                centerX - newWidth / 2f,
                centerY - newHeight / 2f,
                centerX + newWidth / 2f,
                centerY + newHeight / 2f
            )
        }
    }
}

// ---------------- 旋转刻度盘 ----------------

/** 横向刻度盘:-45°~45°,0.1° 步进;刻度随手拖动,中心指针固定,下方显示当前角度 */
@Composable
private fun RotationDial(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val pxPerDegree = with(density) { 7.dp.toPx() }
    // 拖动回调里读最新值,避免 pointerInput 闭包捕获旧角度
    val currentValue by rememberUpdatedState(value)
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant
    val pointerColor = MaterialTheme.colorScheme.primary
    val labelPaint = remember(tickColor, density) {
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            textSize = with(density) { 10.sp.toPx() }
            color = tickColor.toArgb()
            textAlign = android.graphics.Paint.Align.CENTER
        }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .pointerInput(pxPerDegree) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        val next = (currentValue - dragAmount / pxPerDegree).coerceIn(-45f, 45f)
                        onValueChange((next * 10).roundToInt() / 10f)
                    }
                }
        ) {
            val centerX = size.width / 2f
            val halfWindow = size.width / 2f / pxPerDegree
            val baseline = size.height - 6.dp.toPx()
            val firstTick = floor((value - halfWindow) / 5f).toInt() * 5
            val lastTick = ceil((value + halfWindow) / 5f).toInt() * 5
            for (degree in firstTick..lastTick step 5) {
                val x = centerX + (degree - value) * pxPerDegree
                val length = when {
                    degree % 30 == 0 -> 16.dp.toPx()
                    degree % 10 == 0 -> 11.dp.toPx()
                    else -> 7.dp.toPx()
                }
                drawLine(
                    color = tickColor,
                    start = Offset(x, baseline),
                    end = Offset(x, baseline - length),
                    strokeWidth = 1.5.dp.toPx()
                )
                if (degree % 30 == 0) {
                    drawContext.canvas.nativeCanvas.drawText(
                        "$degree°", x, baseline - length - 4.dp.toPx(), labelPaint
                    )
                }
            }
            // 中心指针
            drawLine(
                color = pointerColor,
                start = Offset(centerX, baseline + 2.dp.toPx()),
                end = Offset(centerX, baseline - 22.dp.toPx()),
                strokeWidth = 3.dp.toPx()
            )
        }
        val displayValue = if (value == 0f) 0f else value
        Text(
            text = String.format(Locale.US, "%.1f°", displayValue),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ---------------- 底部比例 / 旋转翻转面板 ----------------

@Composable
private fun CropControlPanel(
    session: CropSessionState,
    bitmapWidth: Int,
    bitmapHeight: Int,
) {
    // 旋转后包围盒宽高比(随 pending 角度实时变化)
    fun boxAspect(): Float {
        val radians = Math.toRadians(session.totalDegrees.toDouble())
        val cos = abs(cos(radians)).toFloat()
        val sin = abs(sin(radians)).toFloat()
        val rotatedWidth = bitmapWidth * cos + bitmapHeight * sin
        val rotatedHeight = bitmapWidth * sin + bitmapHeight * cos
        return if (rotatedHeight == 0f) 1f else rotatedWidth / rotatedHeight
    }
    val rotateBy: (Int) -> Unit = { delta ->
        session.rotationSteps = ((session.rotationSteps + delta) % 4 + 4) % 4
        // 锁定比例时按新的包围盒重新居中适配,保证输出比例不变
        session.aspectMode.ratio?.let { ratio ->
            session.cropRect = centeredRect(boxAspect(), ratio)
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        PanelLabel(text = stringResource(R.string.markup_crop_ratio))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CropAspectMode.entries.forEach { mode ->
                RatioButton(
                    mode = mode,
                    selected = session.aspectMode == mode,
                    onClick = {
                        session.aspectMode = mode
                        mode.ratio?.let { ratio ->
                            session.cropRect = centeredRect(boxAspect(), ratio)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        PanelLabel(text = stringResource(R.string.markup_crop_rotate_flip))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PanelButton(
                icon = Icons.Outlined.LineRotateLeft,
                labelRes = R.string.markup_crop_rotate_left,
                onClick = { rotateBy(-1) },
                modifier = Modifier.weight(1f)
            )
            PanelButton(
                icon = Icons.Outlined.LineRotateRight,
                labelRes = R.string.markup_crop_rotate_right,
                onClick = { rotateBy(1) },
                modifier = Modifier.weight(1f)
            )
            PanelButton(
                icon = Icons.Outlined.LineFlip,
                labelRes = R.string.markup_crop_flip_horizontal,
                onClick = { session.flipHorizontal = !session.flipHorizontal },
                modifier = Modifier.weight(1f)
            )
            PanelButton(
                icon = Icons.Outlined.LineFlip,
                labelRes = R.string.markup_crop_flip_vertical,
                onClick = { session.flipVertical = !session.flipVertical },
                modifier = Modifier.weight(1f),
                iconModifier = Modifier.graphicsLayer { rotationZ = 90f }
            )
        }
        Spacer(Modifier.height(4.dp))
    }
}

/** 在给定包围盒宽高比内,算出指定宽高比的最大居中归一化矩形 */
private fun centeredRect(boxAspect: Float, ratio: Float): NormalizedRect {
    val fractionWidth: Float
    val fractionHeight: Float
    if (ratio <= boxAspect) {
        fractionHeight = 1f
        fractionWidth = ratio / boxAspect
    } else {
        fractionWidth = 1f
        fractionHeight = boxAspect / ratio
    }
    return NormalizedRect(
        left = (1f - fractionWidth) / 2f,
        top = (1f - fractionHeight) / 2f,
        right = (1f + fractionWidth) / 2f,
        bottom = (1f + fractionHeight) / 2f
    )
}

@Composable
private fun PanelLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** 比例选项卡:按宽高比画一个小矩形示意(自由 = 虚线框),下方文字 */
@Composable
private fun RatioButton(
    mode: CropAspectMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else MaterialTheme.colorScheme.onSurfaceVariant
    val label = when (mode) {
        CropAspectMode.OneToOne -> "1:1"
        CropAspectMode.FourToThree -> "4:3"
        CropAspectMode.SixteenToNine -> "16:9"
        CropAspectMode.Free -> stringResource(R.string.markup_crop_ratio_free)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(ShapeDefaults.default)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                } else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .then(
                if (selected) {
                    Modifier.border(1.dp, contentColor, ShapeDefaults.default)
                } else Modifier
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        val ratio = mode.ratio
        if (ratio == null) {
            val dashColor = contentColor
            Box(
                modifier = Modifier
                    .size(22.dp, 16.dp)
                    .drawBehind {
                        drawRect(
                            color = dashColor,
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(4.dp.toPx(), 3.dp.toPx())
                                )
                            )
                        )
                    }
            )
        } else {
            Box(
                modifier = Modifier
                    .size(width = (16.dp * ratio).coerceAtMost(30.dp), height = 16.dp)
                    .border(1.5.dp, contentColor, RoundedCornerShape(2.dp))
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}

@Composable
private fun PanelButton(
    icon: ImageVector,
    labelRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(ShapeDefaults.default)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(labelRes),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = iconModifier.size(20.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}
