package com.wanbaohe.textcard.presentation.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.resources.emoji.Emoji
import com.t8rin.imagetoolbox.core.settings.presentation.model.toUiFont
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.modifier.meshGradient
import com.wanbaohe.textcard.domain.model.BackgroundSpec
import com.wanbaohe.textcard.domain.model.CardTextAlignment
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.domain.model.TextBlockId
import com.wanbaohe.textcard.domain.model.TextCardLayer
import com.wanbaohe.textcard.domain.model.TextCardRenderState
import com.wanbaohe.textcard.domain.render.CardLayout
import com.wanbaohe.textcard.domain.render.MESH_RESOLUTION
import com.wanbaohe.textcard.domain.render.toPointPairs
import kotlin.math.roundToInt

/**
 * 卡片画布预览(Compose):与导出渲染器共用 [CardLayout] 几何/颜色常量,
 * 按图层 z 序叠 背景 → 文字 → 装饰。点按标题/正文回调 [onTextClick] 弹出编辑;
 * 文字块支持单指拖动([onTextDrag],dx/dy 已按预览画布像素归一化,天然处理 fit 缩放);
 * 装饰贴纸同样画布内拖动([onDecorationDrag]);
 * 背景为相册图时空白处拖动调整图片位置([onBackgroundDrag])。
 */
@Composable
fun CardCanvasPreview(
    state: TextCardRenderState,
    onTextClick: (TextBlockId) -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    onTextDrag: (TextBlockId, Float, Float) -> Unit = { _, _, _ -> },
    onBackgroundDrag: (Float, Float) -> Unit = { _, _ -> },
    onDecorationDrag: (Float, Float) -> Unit = { _, _ -> },
) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(state.canvas.aspectRatio)
            .clip(RoundedCornerShape(cornerRadius))
            .clipToBounds()
    ) {
        val canvasWidthPx = constraints.maxWidth.toFloat()
        val canvasHeightPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current
        val emojis = Emoji.allIcons()

        state.visibleLayers.forEach { layer ->
            when (layer) {
                is TextCardLayer.Background -> BackgroundLayerContent(
                    state = state,
                    canvasWidthPx = canvasWidthPx,
                    canvasHeightPx = canvasHeightPx,
                    onBackgroundDrag = onBackgroundDrag,
                    modifier = Modifier.fillMaxSize()
                )

                is TextCardLayer.Text -> TextLayerContent(
                    state = state,
                    canvasWidthPx = canvasWidthPx,
                    canvasHeightPx = canvasHeightPx,
                    onTextClick = onTextClick,
                    onTextDrag = onTextDrag,
                    modifier = Modifier.fillMaxSize()
                )

                is TextCardLayer.Decoration -> {
                    val emojiIndex = state.decoration.emojiIndex
                    val uri = emojis.getOrNull(emojiIndex ?: -1)
                    if (uri != null) {
                        val sizePx = canvasWidthPx * CardLayout.DECORATION_SIZE_RATIO
                        val x = state.decoration.offsetX * canvasWidthPx
                        val y = state.decoration.offsetY * canvasHeightPx
                        Picture(
                            model = uri,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            // 装饰为透明底贴纸,不画棋盘格
                            showTransparencyChecker = false,
                            modifier = Modifier
                                .offset {
                                    IntOffset(x.roundToInt(), y.roundToInt())
                                }
                                .size(with(density) { sizePx.toDp() })
                                // 画布内自由拖动(与文字块同优先级,各自命中区域)
                                .pointerInput(canvasWidthPx, canvasHeightPx) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        onDecorationDrag(
                                            dragAmount.x / canvasWidthPx,
                                            dragAmount.y / canvasHeightPx
                                        )
                                    }
                                }
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
    modifier: Modifier = Modifier,
) {
    val isImageBackground = state.background is BackgroundSpec.Image
    Box(
        modifier = modifier
            .background(Color(CardLayout.CARD_BASE_COLOR))
            .graphicsLayer { alpha = state.backgroundOpacity.coerceIn(0f, 1f) }
            // 背景图拖动:铺满画布的最底层手势;文字块自身手势消费后这里自动取消
            .pointerInput(isImageBackground, canvasWidthPx, canvasHeightPx) {
                if (!isImageBackground) return@pointerInput
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onBackgroundDrag(dragAmount.x / canvasWidthPx, dragAmount.y / canvasHeightPx)
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

/** 渐变 Brush 已移除:渐变背景统一走 meshGradient(见 BackgroundLayerContent) */

@Composable
private fun TextLayerContent(
    state: TextCardRenderState,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    onTextClick: (TextBlockId) -> Unit,
    onTextDrag: (TextBlockId, Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val paddingPx = canvasWidthPx * CardLayout.CONTENT_PADDING_RATIO
    val contentWidthDp = with(density) { (canvasWidthPx - paddingPx * 2).toDp() }

    Box(modifier = modifier) {
        DraggableCardText(
            block = state.title,
            blockId = TextBlockId.Title,
            baseSizePx = canvasWidthPx * CardLayout.TITLE_BASE_SIZE_RATIO,
            leftPx = paddingPx + state.title.offsetX * canvasWidthPx,
            topPx = paddingPx + state.title.offsetY * canvasHeightPx,
            width = contentWidthDp,
            canvasWidthPx = canvasWidthPx,
            canvasHeightPx = canvasHeightPx,
            onTextClick = onTextClick,
            onTextDrag = onTextDrag
        )
        DraggableCardText(
            block = state.body,
            blockId = TextBlockId.Body,
            baseSizePx = canvasWidthPx * CardLayout.BODY_BASE_SIZE_RATIO,
            leftPx = paddingPx + state.body.offsetX * canvasWidthPx,
            topPx = canvasWidthPx * CardLayout.BODY_BASE_TOP_RATIO +
                state.body.offsetY * canvasHeightPx,
            width = contentWidthDp,
            canvasWidthPx = canvasWidthPx,
            canvasHeightPx = canvasHeightPx,
            onTextClick = onTextClick,
            onTextDrag = onTextDrag
        )
    }
}

/**
 * 可拖动的文字块:绝对定位于基准位置 + 归一化偏移(与导出侧一致),
 * 单指拖动回调归一化增量(dx/画布宽、dy/画布高,fit 缩放天然抵消),点按弹出编辑。
 */
@Composable
private fun DraggableCardText(
    block: TextBlock,
    blockId: TextBlockId,
    baseSizePx: Float,
    leftPx: Float,
    topPx: Float,
    width: Dp,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    onTextClick: (TextBlockId) -> Unit,
    onTextDrag: (TextBlockId, Float, Float) -> Unit,
) {
    if (block.content.isBlank()) return
    Box(
        modifier = Modifier
            .offset { IntOffset(leftPx.roundToInt(), topPx.roundToInt()) }
            .width(width)
            .pointerInput(blockId, canvasWidthPx, canvasHeightPx) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onTextDrag(
                        blockId,
                        dragAmount.x / canvasWidthPx,
                        dragAmount.y / canvasHeightPx
                    )
                }
            }
            .clickable(onClick = { onTextClick(blockId) })
    ) {
        CardText(
            block = block,
            baseSizePx = baseSizePx,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** 单个文字块预览:字号 px→sp 等比缩放,letterSpacing(em)/行距倍率/对齐/粗斜与导出侧一致 */
@Composable
private fun CardText(
    block: TextBlock,
    baseSizePx: Float,
    modifier: Modifier = Modifier,
) {
    if (block.content.isBlank()) return
    val density = LocalDensity.current
    val fontSize = with(density) { (baseSizePx * block.sizeScale).toSp() }
    val fontFamily = block.font.toUiFont().fontFamily
    Text(
        text = block.content,
        color = Color(block.color),
        fontSize = fontSize,
        lineHeight = (fontSize.value * block.lineSpacingMultiplier).sp,
        letterSpacing = block.letterSpacingEm.em,
        fontFamily = fontFamily,
        fontWeight = if (block.isBold) FontWeight.Bold else FontWeight.Normal,
        fontStyle = if (block.isItalic) FontStyle.Italic else FontStyle.Normal,
        textAlign = when (block.alignment) {
            CardTextAlignment.Left -> TextAlign.Start
            CardTextAlignment.Center -> TextAlign.Center
            CardTextAlignment.Right -> TextAlign.End
            CardTextAlignment.Justify -> TextAlign.Justify
        },
        modifier = modifier
    )
}
