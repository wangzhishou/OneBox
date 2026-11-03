package com.wanbaohe.ocr.screen

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.components.text.SuppressSystemTextSelectionMenu
import com.shifenmiao.common.components.text.TextSelectionAction
import com.shifenmiao.common.components.text.TextSelectionToolbar
import com.shifenmiao.common.components.text.copySelection
import com.shifenmiao.common.components.text.selectAllText
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.SelectAll
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.editor.AnimatedSelectionBorder
import com.wanbaohe.ocr.R
import com.wanbaohe.ocr.domain.PixelPoint
import com.wanbaohe.ocr.domain.PpOcrSpec
import com.wanbaohe.ocr.domain.RecognizedLine
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

@Composable
internal fun RecognizedImage(
    bitmap: Bitmap,
    lines: List<RecognizedLine>,
    selectedIndex: Int?,
    onSelect: (Int?) -> Unit,
    onCopyLine: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val image = remember(bitmap) { bitmap.asImageBitmap() }
    val plate = MaterialTheme.colorScheme.surfaceContainer
    val plateSelected = MaterialTheme.colorScheme.primaryContainer
    val density = LocalDensity.current
    SuppressSystemTextSelectionMenu {
        BoxWithConstraints(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            val drawWidth = maxWidth
            val drawHeight = drawWidth * bitmap.height / bitmap.width
            val widthPx = with(density) { drawWidth.toPx() }
            val heightPx = with(density) { drawHeight.toPx() }
            val scaleX = widthPx / bitmap.width
            val scaleY = heightPx / bitmap.height
            val placements = remember(lines, scaleX, scaleY) {
                lines.map { line -> textPlacement(line.corners, scaleX, scaleY) }
            }
            Box(
                modifier = Modifier
                    .width(drawWidth)
                    .height(drawHeight)
                    .clip(RoundedCornerShape(IMAGE_CORNER)),
            ) {
                Image(
                    bitmap = image,
                    contentDescription = stringResource(R.string.ocr_image),
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize(),
                )
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(lines, bitmap.width, bitmap.height) {
                            detectTapGestures { tap ->
                                val x = tap.x / size.width * bitmap.width
                                val y = tap.y / size.height * bitmap.height
                                val hit = lines.indexOfLast { line -> contains(line.corners, x, y) }
                                onSelect(hit.takeIf { it >= 0 })
                            }
                        },
                ) {
                    placements.forEachIndexed { index, placement ->
                        val selected = index == selectedIndex
                        if (placement != null) {
                            drawIntoCanvas { canvas ->
                                canvas.save()
                                canvas.translate(placement.origin.x, placement.origin.y)
                                canvas.rotate(placement.angleDegrees)
                                val verticalPadding = placement.height * PLATE_VERTICAL_PADDING
                                drawRoundRect(
                                    color = if (selected) plateSelected else plate,
                                    topLeft = Offset(0f, placement.top - verticalPadding),
                                    size = Size(
                                        placement.width,
                                        placement.height + verticalPadding * 2f,
                                    ),
                                    cornerRadius = CornerRadius(
                                        placement.height * PLATE_RADIUS,
                                        placement.height * PLATE_RADIUS,
                                    ),
                                )
                                canvas.restore()
                            }
                        }
                    }
                }
                lines.forEachIndexed { index, line ->
                    val placement = placements[index] ?: return@forEachIndexed
                    key(index, line.text) {
                        val verticalPadding = placement.height * PLATE_VERTICAL_PADDING
                        AnimatedSelectionBorder(
                            isVisible = index == selectedIndex,
                            scale = 1f,
                            cornerRadius = with(density) { (placement.height * PLATE_RADIUS).toDp() },
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        placement.origin.x.roundToInt(),
                                        placement.origin.y.roundToInt(),
                                    )
                                }
                                .graphicsLayer {
                                    rotationZ = placement.angleDegrees
                                    transformOrigin = TransformOrigin(0f, 0f)
                                }
                                .offset {
                                    IntOffset(
                                        0,
                                        (placement.top - verticalPadding).roundToInt(),
                                    )
                                }
                                .requiredSize(
                                    width = with(density) { placement.width.toDp() },
                                    height = with(density) {
                                        (placement.height + verticalPadding * 2f).toDp()
                                    },
                                ),
                        )
                        SelectableRecognizedLine(
                            line = line,
                            placement = placement,
                            selected = index == selectedIndex,
                            onSelect = { onSelect(index) },
                            onCopyLine = { onCopyLine(line.text) },
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        placement.origin.x.roundToInt(),
                                        placement.origin.y.roundToInt(),
                                    )
                                }
                                .graphicsLayer {
                                    rotationZ = placement.angleDegrees
                                    transformOrigin = TransformOrigin(0f, 0f)
                                }
                                .offset { IntOffset(0, placement.top.roundToInt()) }
                                .requiredSize(
                                    width = with(density) { placement.width.toDp() },
                                    height = with(density) { placement.height.toDp() },
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectableRecognizedLine(
    line: RecognizedLine,
    placement: TextPlacement,
    selected: Boolean,
    onSelect: () -> Unit,
    onCopyLine: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberTextFieldState(line.text)
    val clipboard = LocalClipboardManager.current
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val textColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val textStyle = remember(line.text, textColor, placement.width, placement.height, density) {
        fittedTextStyle(measurer, density, line.text, textColor, placement.width, placement.height)
    }
    val hasSelection = !state.selection.collapsed
    var anchorBounds by remember { mutableStateOf(Rect.Zero) }

    LaunchedEffect(selected) {
        if (!selected && !state.selection.collapsed) {
            val end = state.selection.end
            state.edit { selection = TextRange(end) }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        BasicTextField(
            state = state,
            readOnly = true,
            lineLimits = TextFieldLineLimits.SingleLine,
            textStyle = textStyle,
            cursorBrush = SolidColor(Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = with(density) { (placement.height * TEXT_INSET).toDp() })
                .onFocusChanged { focus ->
                    if (focus.isFocused) onSelect()
                }
                .onGloballyPositioned { coordinates ->
                    anchorBounds = coordinates.boundsInWindow()
                },
        )
    }

    if (selected && anchorBounds != Rect.Zero) {
        TextSelectionToolbar(
            anchorBounds = anchorBounds,
            actions = listOf(
                TextSelectionAction(
                    key = "copy",
                    label = stringResource(
                        if (hasSelection) R.string.ocr_copy_selection else R.string.ocr_copy_line
                    ),
                    icon = Icons.Outlined.ContentCopy,
                    onClick = {
                        if (hasSelection) {
                            if (state.copySelection(clipboard)) {
                                AppToastHost.showToast(R.string.ocr_copied)
                            }
                        } else {
                            onCopyLine()
                        }
                    },
                ),
                TextSelectionAction(
                    key = "select_all",
                    label = stringResource(R.string.ocr_select_line),
                    icon = Icons.Outlined.SelectAll,
                    onClick = state::selectAllText,
                ),
            ),
        )
    }
}

private fun fittedTextStyle(
    measurer: TextMeasurer,
    density: Density,
    text: String,
    color: Color,
    boxWidth: Float,
    boxHeight: Float,
): TextStyle {
    val heightPx = boxHeight * TEXT_HEIGHT_RATIO
    val widthPx = (boxWidth - boxHeight * TEXT_INSET * 2f).coerceAtLeast(1f)
    val fontSize = with(density) { heightPx.toSp() }
    val style = TextStyle(
        fontSize = fontSize,
        lineHeight = fontSize * TEXT_LINE_HEIGHT,
        color = color,
        fontWeight = FontWeight.Medium,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    )
    val first = measurer.measure(
        text = text,
        style = style,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Visible,
    )
    val scale = if (first.size.width > 0) {
        (widthPx / first.size.width).coerceAtMost(1f)
    } else {
        1f
    }
    if (scale >= 1f) return style
    val fittedSize = fontSize * scale
    return style.copy(fontSize = fittedSize, lineHeight = fittedSize * TEXT_LINE_HEIGHT)
}

private data class TextPlacement(
    val origin: Offset,
    val angleDegrees: Float,
    val width: Float,
    val height: Float,
    val top: Float,
)

private fun textPlacement(corners: List<PixelPoint>, scaleX: Float, scaleY: Float): TextPlacement? {
    if (corners.size < 4) return null
    val points = List(4) { index -> Offset(corners[index].x * scaleX, corners[index].y * scaleY) }
    val alongTop = hypot(points[1].x - points[0].x, points[1].y - points[0].y)
    val alongSide = hypot(points[3].x - points[0].x, points[3].y - points[0].y)
    val vertical = alongSide > alongTop * PpOcrSpec.VERTICAL_RATIO
    val origin = points[0]
    val end = if (vertical) points[3] else points[1]
    val across = if (vertical) points[1] else points[3]
    val dx = end.x - origin.x
    val dy = end.y - origin.y
    val width = hypot(dx, dy).coerceAtLeast(1f)
    val angle = atan2(dy, dx) * RADIAN_TO_DEGREE
    val px = -dy / width
    val py = dx / width
    val localY = (across.x - origin.x) * px + (across.y - origin.y) * py
    return TextPlacement(
        origin = origin,
        angleDegrees = angle,
        width = width,
        height = abs(localY).coerceAtLeast(MIN_TEXT_BOX_HEIGHT),
        top = if (localY >= 0f) 0f else localY,
    )
}

private fun contains(corners: List<PixelPoint>, x: Float, y: Float): Boolean {
    if (corners.size < 3) return false
    var inside = false
    var previous = corners.lastIndex
    for (index in corners.indices) {
        val current = corners[index]
        val other = corners[previous]
        val crosses = (current.y > y) != (other.y > y)
        if (crosses) {
            val span = other.y - current.y
            val edgeX = (other.x - current.x) * (y - current.y) / span + current.x
            if (x < edgeX) inside = !inside
        }
        previous = index
    }
    return inside
}

private val IMAGE_CORNER = 20.dp
private const val PLATE_RADIUS = 0.24f
private const val PLATE_VERTICAL_PADDING = 0.14f
private const val TEXT_HEIGHT_RATIO = 0.76f
private const val TEXT_LINE_HEIGHT = 1.08f
private const val TEXT_INSET = 0.06f
private const val MIN_TEXT_BOX_HEIGHT = 1f
private val RADIAN_TO_DEGREE = 180f / kotlin.math.PI.toFloat()
