package com.wanbaohe.markuplayers.presentation.tools.mask

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.button.CancelButton
import com.shifenmiao.base.ui.button.ConfirmButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedChip
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedSlider
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.modifier.transparencyChecker
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.LayerMask
import com.wanbaohe.markuplayers.domain.model.MaskStroke
import com.wanbaohe.markuplayers.domain.model.NormalizedRect
import com.wanbaohe.markuplayers.domain.model.StrokePoint
import com.wanbaohe.markuplayers.presentation.components.EditorTargetSelector
import com.wanbaohe.markuplayers.presentation.components.SheetHeader
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent
import kotlinx.coroutines.CancellationException
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private enum class MaskMode { Rectangle, Brush, Erase }

/** The draft stays local until Apply; one committed mask is one document undo step. */
@Composable
fun MaskToolSheet(
    visible: Boolean,
    component: MarkupLayersComponent,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    val candidateLayerId = remember { component.selectedLayerId }
    val targetId = component.selectedLayerId
    val initialMask = component.targetMask
    var draft by remember(targetId) { mutableStateOf(initialMask ?: LayerMask()) }
    var mode by remember(targetId) {
        mutableStateOf(if (initialMask?.strokes?.isNotEmpty() == true) MaskMode.Brush else MaskMode.Rectangle)
    }
    var brushWidth by remember { mutableStateOf(0.08f) }
    var pendingPoints by remember(targetId) { mutableStateOf<List<StrokePoint>>(emptyList()) }
    var undoMasks by remember(targetId) { mutableStateOf<List<LayerMask>>(emptyList()) }
    var featherDragging by remember(targetId) { mutableStateOf(false) }
    var alphaPreview by remember(targetId) { mutableStateOf<Bitmap?>(null) }
    var previewFailed by remember(targetId) { mutableStateOf(false) }
    var previewRevision by remember { mutableStateOf(0) }
    val bitmap = component.maskTargetBitmap
    val enabled = component.canMaskTarget && !component.isImageLoading
    val effectiveDraft = if (pendingPoints.isEmpty()) draft else draft.copy(
        strokes = draft.strokes + MaskStroke(pendingPoints, brushWidth, mode == MaskMode.Erase)
    )
    val rememberUndo = {
        undoMasks = (undoMasks + draft).takeLast(40)
    }

    LaunchedEffect(targetId) {
        if (component.canMaskTarget) component.prepareMaskTarget()
    }
    LaunchedEffect(targetId, bitmap, effectiveDraft, previewRevision) {
        alphaPreview = null
        previewFailed = false
        if (bitmap != null && component.canMaskTarget) {
            val ratio = min(1f, 384f / max(bitmap.width, bitmap.height))
            try {
                alphaPreview = component.previewTargetMask(
                    mask = effectiveDraft,
                    width = (bitmap.width * ratio).roundToInt().coerceAtLeast(1),
                    height = (bitmap.height * ratio).roundToInt().coerceAtLeast(1)
                )
            } catch (failure: CancellationException) {
                throw failure
            } catch (_: Exception) {
                previewFailed = true
            }
        }
    }
    EnhancedModalBottomSheet(
        visible = true,
        dragHandle = {},
        onDismiss = { onDismiss() },
        sheetContent = {
            SheetHeader(
                title = stringResource(R.string.studio_mask),
                onClose = onDismiss
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                EditorTargetSelector(
                    component = component,
                    candidateLayerId = candidateLayerId,
                    enabled = !component.isImageLoading
                )
                when {
                    !component.canMaskTarget -> Text(
                        text = stringResource(R.string.studio_mask_unavailable),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    component.isImageLoading -> Text(
                        text = stringResource(R.string.studio_mask_loading),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    bitmap == null -> {
                        Text(
                            text = stringResource(R.string.studio_mask_load_failed),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        TextButton(
                            onClick = component::prepareMaskTarget,
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) { Text(stringResource(R.string.studio_retry)) }
                    }
                    else -> {
                        MaskPreview(
                            bitmap = bitmap,
                            alphaPreview = alphaPreview,
                            mask = effectiveDraft,
                            mode = mode,
                            brushWidth = brushWidth,
                            onGestureStart = rememberUndo,
                            onRectChange = { draft = draft.copy(rect = it, strokes = emptyList()) },
                            onPendingPoints = { pendingPoints = it },
                            onStrokeEnd = { points ->
                                if (points.isNotEmpty()) {
                                    draft = draft.copy(
                                        strokes = draft.strokes + MaskStroke(
                                            points = points,
                                            widthRatio = brushWidth,
                                            erase = mode == MaskMode.Erase
                                        )
                                    )
                                }
                                pendingPoints = emptyList()
                            }
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MaskMode.entries.forEach { value ->
                                EnhancedChip(
                                    selected = mode == value,
                                    onClick = { mode = value },
                                    selectedColor = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                                ) {
                                    Text(
                                        text = stringResource(
                                            when (value) {
                                                MaskMode.Rectangle -> R.string.studio_mask_rectangle
                                                MaskMode.Brush -> R.string.studio_mask_brush
                                                MaskMode.Erase -> R.string.studio_mask_erase
                                            }
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        Text(
                            text = stringResource(
                                if (mode == MaskMode.Rectangle) R.string.studio_mask_rect_hint
                                else R.string.studio_mask_brush_hint
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (mode != MaskMode.Rectangle) {
                            MaskSlider(
                                label = stringResource(R.string.studio_mask_brush_size),
                                value = brushWidth,
                                valueRange = 0.005f..0.4f,
                                onValueChange = { brushWidth = it }
                            )
                        }
                        MaskSlider(
                            label = stringResource(R.string.studio_mask_feather),
                            value = draft.feather.coerceIn(0f, LayerMask.MaxFeather),
                            valueRange = 0f..LayerMask.MaxFeather,
                            onValueChange = {
                                if (!featherDragging) {
                                    rememberUndo()
                                    featherDragging = true
                                }
                                draft = draft.copy(feather = it)
                            },
                            onValueChangeFinished = { featherDragging = false }
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.studio_mask_invert),
                                modifier = Modifier.weight(1f)
                            )
                            GlassSwitch(
                                checked = draft.inverted,
                                onCheckedChange = {
                                    rememberUndo()
                                    draft = draft.copy(inverted = it)
                                },
                                modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                        Row {
                            TextButton(
                                enabled = undoMasks.isNotEmpty(),
                                onClick = {
                                    draft = undoMasks.last()
                                    undoMasks = undoMasks.dropLast(1)
                                }
                            ) { Text(stringResource(R.string.markup_undo)) }
                            TextButton(
                                onClick = {
                                    rememberUndo()
                                    draft = LayerMask()
                                }
                            ) { Text(stringResource(R.string.markup_reset)) }
                            Spacer(Modifier.weight(1f))
                            TextButton(
                                enabled = initialMask != null,
                                onClick = {
                                    component.updateTargetMask(null)
                                    onDismiss()
                                }
                            ) { Text(stringResource(R.string.studio_mask_remove)) }
                        }
                        if (previewFailed) {
                            Text(
                                text = stringResource(R.string.studio_mask_load_failed),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            TextButton(onClick = { previewRevision++ }) {
                                Text(stringResource(R.string.studio_retry))
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CancelButton(
                        text = stringResource(R.string.markup_cancel),
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                    )
                    ConfirmButton(
                        text = stringResource(R.string.studio_mask_done),
                        enabled = enabled && bitmap != null && alphaPreview != null && !previewFailed,
                        onClick = {
                            component.updateTargetMask(draft)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                    )
                }
            }
        }
    )
}

@Composable
private fun MaskSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit = {},
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.studio_percent, (value * 100).roundToInt()),
                style = MaterialTheme.typography.labelMedium
            )
        }
        EnhancedSlider(
            value = value,
            valueRange = valueRange,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        )
    }
}

/** All gestures and overlay geometry use the fitted content bounds, not the surrounding sheet. */
@Composable
private fun MaskPreview(
    bitmap: Bitmap,
    alphaPreview: Bitmap?,
    mask: LayerMask,
    mode: MaskMode,
    brushWidth: Float,
    onGestureStart: () -> Unit,
    onRectChange: (NormalizedRect) -> Unit,
    onPendingPoints: (List<StrokePoint>) -> Unit,
    onStrokeEnd: (List<StrokePoint>) -> Unit,
) {
    val density = LocalDensity.current
    val description = stringResource(R.string.studio_mask_preview)
    val outline = MaterialTheme.colorScheme.onPrimaryContainer
    val alphaImage = remember(alphaPreview) { alphaPreview?.asImageBitmap() }
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth().height(260.dp)
    ) {
        val fit = min(constraints.maxWidth / bitmap.width.toFloat(), constraints.maxHeight / bitmap.height.toFloat())
        val width = (bitmap.width * fit).toInt().coerceAtLeast(1)
        val height = (bitmap.height * fit).toInt().coerceAtLeast(1)
        Box(
            modifier = Modifier
                .size(with(density) { width.toDp() }, with(density) { height.toDp() })
                .transparencyChecker()
                .semantics { contentDescription = description }
                .pointerInput(bitmap, mode, brushWidth, width, height) {
                    fun Offset.point() = StrokePoint(
                        x = (x / width).coerceIn(0f, 1f),
                        y = (y / height).coerceIn(0f, 1f)
                    )
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        down.consume()
                        val start = down.position.point()
                        var points = listOf(start)
                        onGestureStart()
                        if (mode == MaskMode.Rectangle) {
                            onRectChange(maskRect(start, start, bitmap.width, bitmap.height))
                        } else onPendingPoints(points)
                        try {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.changes.size > 1 || event.changes.any { it.isConsumed }) break
                                val change = event.changes.first()
                                val point = change.position.point()
                                if (mode == MaskMode.Rectangle) {
                                    onRectChange(maskRect(start, point, bitmap.width, bitmap.height))
                                } else if (change.positionChange() != Offset.Zero) {
                                    points = points + point
                                    onPendingPoints(points)
                                }
                                change.consume()
                                if (!change.pressed) break
                            }
                        } finally {
                            if (mode != MaskMode.Rectangle) onStrokeEnd(points)
                        }
                    }
                }
        ) {
            Picture(
                model = bitmap,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.matchParentSize()
            )
            Canvas(
                modifier = Modifier.matchParentSize().graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
            ) {
                alphaImage?.let {
                    drawRect(Color.Red.copy(alpha = 0.35f))
                    drawImage(
                        image = it,
                        dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                        blendMode = BlendMode.DstOut
                    )
                }
                mask.rect?.let { rect ->
                    drawRect(
                        color = outline,
                        topLeft = Offset(rect.left * size.width, rect.top * size.height),
                        size = Size(rect.width * size.width, rect.height * size.height),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
    }
}

private fun maskRect(
    start: StrokePoint,
    end: StrokePoint,
    width: Int,
    height: Int,
): NormalizedRect {
    val minWidth = 1f / width
    val minHeight = 1f / height
    val left = min(start.x, end.x).coerceIn(0f, 1f - minWidth)
    val top = min(start.y, end.y).coerceIn(0f, 1f - minHeight)
    return NormalizedRect(
        left = left,
        top = top,
        right = max(start.x, end.x).coerceIn(left + minWidth, 1f),
        bottom = max(start.y, end.y).coerceIn(top + minHeight, 1f)
    )
}
