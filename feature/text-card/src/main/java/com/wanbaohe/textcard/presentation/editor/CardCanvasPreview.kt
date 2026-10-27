@file:OptIn(ExperimentalFoundationApi::class)

package com.wanbaohe.textcard.presentation.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.FormatClear
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.components.text.MAX_SELECTION_SIZE_SCALE
import com.shifenmiao.common.components.text.MIN_SELECTION_SIZE_SCALE
import com.shifenmiao.common.components.text.SuppressSystemTextSelectionMenu
import com.shifenmiao.common.components.text.TextSelectionAction
import com.shifenmiao.common.components.text.TextSelectionToolbar
import com.shifenmiao.common.components.text.applySelectionStyle
import com.shifenmiao.common.components.text.copySelection
import com.shifenmiao.common.components.text.pasteAtSelection
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.ContentPaste
import com.t8rin.imagetoolbox.core.settings.presentation.model.toUiFont
import com.t8rin.imagetoolbox.core.utils.toTypeface
import com.wanbaohe.core.ui.widget.color_picker.ColorSelectionRow
import com.wanbaohe.core.ui.widget.editor.AnimatedSelectionBorder
import com.wanbaohe.core.ui.widget.image.Picture
import com.wanbaohe.core.ui.widget.modifier.meshGradient
import com.wanbaohe.core.ui.widget.text.OutlineParams
import com.wanbaohe.core.ui.widget.text.OutlinedText
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.BackgroundSpec
import com.wanbaohe.textcard.domain.model.CardTextAlignment
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.domain.model.TextCardDocument
import com.wanbaohe.textcard.domain.model.TextCardRenderState
import com.wanbaohe.textcard.domain.model.TextCardRole
import com.wanbaohe.textcard.domain.model.TextCardSegment
import com.wanbaohe.textcard.domain.model.buildAnnotatedContent
import com.wanbaohe.textcard.domain.model.effectiveColorAt
import com.wanbaohe.textcard.domain.model.effectiveSizeScaleAt
import com.wanbaohe.textcard.domain.model.isRangeEffectivelyBold
import com.wanbaohe.textcard.domain.model.isRangeEffectivelyItalic
import com.wanbaohe.textcard.domain.render.CardLayout
import com.wanbaohe.textcard.domain.render.MESH_RESOLUTION
import com.wanbaohe.textcard.domain.render.toPointPairs
import com.wanbaohe.textcard.presentation.screenLogic.CanvasTextEditSession
import kotlin.math.roundToInt

@Composable
internal fun CardCanvasPreview(
    state: TextCardRenderState,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    interactive: Boolean = true,
    liveDocument: TextCardDocument? = null,
    selectedRole: TextCardRole? = null,
    editingSession: CanvasTextEditSession? = null,
    editingState: TextFieldState? = null,
    pageIndex: Int = 0,
    onSegmentTap: (TextCardSegment, Int) -> Unit = { _, _ -> },
    onSegmentEdit: (TextCardSegment, Int) -> Unit = { _, _ -> },
    onSegmentDrag: (TextCardSegment, Float, Float) -> Unit = { _, _, _ -> },
    onGestureEnd: () -> Unit = {},
    onCanvasTap: () -> Unit = {},
    onBackgroundDrag: (Float, Float) -> Unit = { _, _ -> },
    onEditingSync: () -> Unit = {},
    onTextEditCommit: () -> Unit = {},
    onOpenStylePanel: () -> Unit = {},
    onClearSelectionStyles: () -> Unit = {},
    selectionToolbarVisible: Boolean = true,
) {
    val segments = state.segments.map { segment ->
        val source = liveDocument?.block(segment.role)
        if (source == null) segment else {
            segment.copy(
                block = segment.block.copy(
                    offsetX = source.offsetX,
                    offsetY = source.offsetY,
                    scale = source.scale,
                    rotation = source.rotation
                )
            )
        }
    }
    val segmentBlocks = segments.associate { it.block.id to it.block }
    val renderState = state.copy(
        background = liveDocument?.background ?: state.background,
        backgroundOpacity = liveDocument?.backgroundOpacity ?: state.backgroundOpacity,
        textBlocks = state.textBlocks.map { segmentBlocks[it.id] ?: it },
        segments = segments
    )
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(state.canvas.aspectRatio)
            .clip(RoundedCornerShape(cornerRadius))
            .clipToBounds()
    ) {
        val canvasWidthPx = constraints.maxWidth.toFloat()
        val canvasHeightPx = constraints.maxHeight.toFloat()
        BackgroundLayerContent(
            state = renderState,
            canvasWidthPx = canvasWidthPx,
            canvasHeightPx = canvasHeightPx,
            interactive = interactive,
            onBackgroundDrag = onBackgroundDrag,
            onGestureEnd = onGestureEnd,
            onCanvasTap = onCanvasTap,
            modifier = Modifier.fillMaxSize()
        )
        renderState.textBlocks.forEach { block ->
            val segment = renderState.segments.firstOrNull { it.block.id == block.id }
            val isEditing = segment != null &&
                editingSession?.block?.id == block.id &&
                editingSession.pageIndex == pageIndex &&
                editingState != null
            if (block.content.isNotBlank() || isEditing) {
                CardTextElement(
                    block = block,
                    segment = segment,
                    isSelected = segment?.role == selectedRole,
                    editingSession = editingSession.takeIf { isEditing },
                    editingState = editingState.takeIf { isEditing },
                    canvasWidthPx = canvasWidthPx,
                    canvasHeightPx = canvasHeightPx,
                    interactive = interactive,
                    onSegmentTap = onSegmentTap,
                    onSegmentEdit = onSegmentEdit,
                    onSegmentDrag = onSegmentDrag,
                    onGestureEnd = onGestureEnd,
                    onEditingSync = onEditingSync,
                    onTextEditCommit = onTextEditCommit,
                    onOpenStylePanel = onOpenStylePanel,
                    onClearSelectionStyles = onClearSelectionStyles,
                    selectionToolbarVisible = selectionToolbarVisible
                )
            }
        }
    }
}

@Composable
private fun BackgroundLayerContent(
    state: TextCardRenderState,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    interactive: Boolean,
    onBackgroundDrag: (Float, Float) -> Unit,
    onGestureEnd: () -> Unit,
    onCanvasTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isImageBackground = state.background is BackgroundSpec.Image
    Box(
        modifier = modifier
            .background(Color(CardLayout.CARD_BASE_COLOR))
            .then(if (interactive) Modifier.clickable(onClick = onCanvasTap) else Modifier)
            .pointerInput(interactive, isImageBackground, canvasWidthPx, canvasHeightPx) {
                if (!interactive || !isImageBackground) return@pointerInput
                detectDragGestures(
                    onDragEnd = onGestureEnd,
                    onDragCancel = onGestureEnd,
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onBackgroundDrag(
                            dragAmount.x / canvasWidthPx,
                            dragAmount.y / canvasHeightPx
                        )
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = state.backgroundOpacity.coerceIn(0f, 1f) }
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
                is BackgroundSpec.Image -> Picture(
                    model = background.uri,
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    showTransparencyChecker = false,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = CardLayout.BACKGROUND_IMAGE_OVERSCAN
                            scaleY = CardLayout.BACKGROUND_IMAGE_OVERSCAN
                            translationX = background.offsetX * canvasWidthPx
                            translationY = background.offsetY * canvasHeightPx
                        }
                )
            }
        }
    }
}

@Composable
private fun CardTextElement(
    block: TextBlock,
    segment: TextCardSegment?,
    isSelected: Boolean,
    editingSession: CanvasTextEditSession?,
    editingState: TextFieldState?,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    interactive: Boolean,
    onSegmentTap: (TextCardSegment, Int) -> Unit,
    onSegmentEdit: (TextCardSegment, Int) -> Unit,
    onSegmentDrag: (TextCardSegment, Float, Float) -> Unit,
    onGestureEnd: () -> Unit,
    onEditingSync: () -> Unit,
    onTextEditCommit: () -> Unit,
    onOpenStylePanel: () -> Unit,
    onClearSelectionStyles: () -> Unit,
    selectionToolbarVisible: Boolean,
) {
    val density = LocalDensity.current
    val paddingPx = canvasWidthPx * CardLayout.CONTENT_PADDING_RATIO
    val leftPx = paddingPx + block.offsetX * canvasWidthPx
    val topPx = canvasWidthPx * block.baseTopRatio + block.offsetY * canvasHeightPx
    val widthDp = with(density) { (canvasWidthPx * block.widthRatio).toDp() }
    var textLayout by remember(block.id) { mutableStateOf<TextLayoutResult?>(null) }
    val isEditing = editingSession != null && editingState != null

    fun cursorFor(position: androidx.compose.ui.geometry.Offset): Int =
        textLayout?.getOffsetForPosition(position) ?: block.content.length

    Box(
        modifier = Modifier
            .offset { IntOffset(leftPx.roundToInt(), topPx.roundToInt()) }
            .width(widthDp)
            .graphicsLayer {
                scaleX = block.scale
                scaleY = block.scale
                rotationZ = block.rotation
            }
            .then(
                if (interactive && segment != null && !isEditing) {
                    Modifier
                        .pointerInput(block.id, isSelected) {
                            detectTapGestures(
                                onDoubleTap = { position ->
                                    onSegmentEdit(segment, cursorFor(position))
                                },
                                onTap = { position ->
                                    onSegmentTap(segment, cursorFor(position))
                                }
                            )
                        }
                        .pointerInput(block.id, isSelected, canvasWidthPx, canvasHeightPx) {
                            if (!isSelected) return@pointerInput
                            detectDragGestures(
                                onDragEnd = onGestureEnd,
                                onDragCancel = onGestureEnd,
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    onSegmentDrag(
                                        segment,
                                        dragAmount.x / canvasWidthPx,
                                        dragAmount.y / canvasHeightPx
                                    )
                                }
                            )
                        }
                } else Modifier
            )
    ) {
        if (isEditing) {
            InPlaceTextEditor(
                block = editingSession.block,
                baseSizePx = canvasWidthPx * editingSession.block.baseSizeRatio,
                state = editingState,
                onSync = onEditingSync,
                onCommit = onTextEditCommit,
                onOpenStylePanel = onOpenStylePanel,
                onClearSelectionStyles = onClearSelectionStyles,
                selectionToolbarVisible = selectionToolbarVisible,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            CardText(
                block = block,
                baseSizePx = canvasWidthPx * block.baseSizeRatio,
                canvasWidthPx = canvasWidthPx,
                onTextLayout = { textLayout = it },
                modifier = Modifier.fillMaxWidth()
            )
        }
        AnimatedSelectionBorder(
            isVisible = isSelected,
            scale = block.scale,
            modifier = Modifier.matchParentSize(),
            cornerRadius = 4.dp,
            alphaScale = if (isEditing) 0.4f else 1f
        )
    }
}

@Composable
private fun InPlaceTextEditor(
    block: TextBlock,
    baseSizePx: Float,
    state: TextFieldState,
    onSync: () -> Unit,
    onCommit: () -> Unit,
    onOpenStylePanel: () -> Unit,
    onClearSelectionStyles: () -> Unit,
    selectionToolbarVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboard = LocalClipboardManager.current
    var anchorBounds by remember { mutableStateOf(Rect.Zero) }
    var showColors by remember { mutableStateOf(false) }
    val selection = state.selection
    val hasSelection = !selection.collapsed

    LaunchedEffect(state) {
        showColors = false
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    LaunchedEffect(hasSelection) {
        if (!hasSelection) showColors = false
    }

    SuppressSystemTextSelectionMenu {
        BasicTextField(
            state = state,
            textStyle = cardTextStyle(block, baseSizePx),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            onKeyboardAction = {
                onSync()
                onCommit()
            },
            modifier = modifier
                .graphicsLayer { alpha = block.alpha.coerceIn(0f, 1f) }
                .focusRequester(focusRequester)
                .onGloballyPositioned { anchorBounds = it.boundsInWindow() }
        )
    }

    if (hasSelection && selectionToolbarVisible) {
        val start = selection.min
        val end = selection.max
        val bold = block.isRangeEffectivelyBold(start, end)
        val italic = block.isRangeEffectivelyItalic(start, end)
        val currentColor = Color(block.effectiveColorAt(start, end))
        val currentSize = block.effectiveSizeScaleAt(start, end)

        val stepSize: (Boolean) -> Unit = { increase ->
            val next = (if (increase) currentSize * 1.15f else currentSize / 1.15f)
                .coerceIn(MIN_SELECTION_SIZE_SCALE, MAX_SELECTION_SIZE_SCALE)
            state.applySelectionStyle(
                style = SpanStyle(fontSize = next.em),
                onChanged = onSync
            )
        }
        val actions = listOf(
            TextSelectionAction(
                key = "bold",
                label = stringResource(R.string.textcard_bold),
                glyph = "B",
                selected = bold,
                onClick = {
                    state.applySelectionStyle(
                        style = SpanStyle(
                            fontWeight = if (bold) FontWeight.Normal else FontWeight.Bold
                        ),
                        onChanged = onSync
                    )
                }
            ),
            TextSelectionAction(
                key = "italic",
                label = stringResource(R.string.textcard_italic),
                glyph = "I",
                selected = italic,
                onClick = {
                    state.applySelectionStyle(
                        style = SpanStyle(
                            fontStyle = if (italic) FontStyle.Normal else FontStyle.Italic
                        ),
                        onChanged = onSync
                    )
                }
            ),
            TextSelectionAction(
                key = "color",
                label = stringResource(R.string.textcard_text_color),
                swatch = currentColor,
                selected = showColors,
                onClick = { showColors = !showColors }
            ),
            TextSelectionAction(
                key = "size_down",
                label = stringResource(R.string.textcard_text_size_decrease),
                glyph = "A-",
                onClick = { stepSize(false) }
            ),
            TextSelectionAction(
                key = "size_up",
                label = stringResource(R.string.textcard_text_size_increase),
                glyph = "A+",
                onClick = { stepSize(true) }
            ),
            TextSelectionAction(
                key = "copy",
                label = stringResource(android.R.string.copy),
                icon = Icons.Rounded.ContentCopy,
                onClick = { state.copySelection(clipboard) }
            ),
            TextSelectionAction(
                key = "paste",
                label = stringResource(android.R.string.paste),
                icon = Icons.Rounded.ContentPaste,
                onClick = { state.pasteAtSelection(clipboard, onSync) }
            ),
            TextSelectionAction(
                key = "clear",
                label = stringResource(R.string.textcard_clear_selection_style),
                icon = MaterialIcons.Outlined.FormatClear,
                onClick = onClearSelectionStyles
            ),
            TextSelectionAction(
                key = "more",
                label = stringResource(R.string.textcard_text_settings),
                icon = MaterialIcons.Outlined.Tune,
                onClick = onOpenStylePanel
            )
        )

        TextSelectionToolbar(
            anchorBounds = anchorBounds,
            actions = actions,
            footer = if (showColors) {
                {
                    ColorSelectionRow(
                        value = currentColor,
                        onValueChange = { color ->
                            state.applySelectionStyle(
                                style = SpanStyle(color = color),
                                onChanged = onSync
                            )
                            showColors = false
                        },
                        allowAlpha = false
                    )
                }
            } else null
        )
    }
}

@Composable
private fun CardText(
    block: TextBlock,
    baseSizePx: Float,
    canvasWidthPx: Float,
    onTextLayout: (TextLayoutResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val outlineParams = if (block.outlineWidthRatio > 0f) {
        OutlineParams(
            color = Color(block.outlineColor),
            stroke = Stroke(
                width = block.outlineWidthRatio * canvasWidthPx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    } else null
    OutlinedText(
        text = block.buildAnnotatedContent(),
        outlineParams = outlineParams,
        style = cardTextStyle(block, baseSizePx),
        onTextLayout = onTextLayout,
        modifier = modifier.graphicsLayer { alpha = block.alpha.coerceIn(0f, 1f) }
    )
}

@Composable
private fun cardTextStyle(
    block: TextBlock,
    baseSizePx: Float,
): TextStyle {
    val density = LocalDensity.current
    val fontSize = with(density) { (baseSizePx * block.sizeScale).toSp() }
    val metrics = android.text.TextPaint().apply {
        textSize = baseSizePx * block.sizeScale
        val base = runCatching { block.font.toTypeface() }.getOrNull()
            ?: android.graphics.Typeface.DEFAULT
        val style = (if (block.isBold) android.graphics.Typeface.BOLD else 0) or
            (if (block.isItalic) android.graphics.Typeface.ITALIC else 0)
        typeface = if (style != 0) {
            android.graphics.Typeface.create(base, style)
        } else base
    }.fontMetrics
    val lineHeight = with(density) {
        ((metrics.descent - metrics.ascent) * block.lineSpacingMultiplier).toSp()
    }
    return TextStyle(
        color = Color(block.color),
        fontSize = fontSize,
        lineHeight = lineHeight,
        letterSpacing = block.letterSpacingEm.em,
        fontFamily = block.font.toUiFont().fontFamily,
        fontWeight = if (block.isBold) FontWeight.Bold else FontWeight.Normal,
        fontStyle = if (block.isItalic) FontStyle.Italic else FontStyle.Normal,
        textDecoration = TextDecoration.combine(
            buildList {
                if (block.isUnderline) add(TextDecoration.Underline)
                if (block.isStrikethrough) add(TextDecoration.LineThrough)
            }
        ),
        background = if (block.backgroundColor != 0L) {
            Color(block.backgroundColor)
        } else Color.Unspecified,
        textAlign = when (block.alignment) {
            CardTextAlignment.Left -> TextAlign.Start
            CardTextAlignment.Center -> TextAlign.Center
            CardTextAlignment.Right -> TextAlign.End
            CardTextAlignment.Justify -> TextAlign.Justify
        }
    )
}
