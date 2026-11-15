package com.hrm.diagram.render.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.hrm.diagram.core.DiagramApi
import com.hrm.diagram.core.draw.Rect
import com.hrm.diagram.render.streaming.DiagramSnapshot
import kotlin.math.max

/**
 * Host-facing presentation policy for diagram composition.
 *
 * - [Auto] chooses a sensible default from the parent's constraints.
 * - [Embedded] is for inline/block embedding such as Markdown cards or list rows.
 * - [Viewport] is for full preview panes and editors that should consume the whole canvas.
 */
@DiagramApi
@Immutable
enum class DiagramPresentationMode {
    Auto,
    Embedded,
    Viewport,
}

/**
 * High-level Compose surface for displaying an existing [DiagramRenderState].
 *
 * It translates the render snapshot into a host-aware presentation layout, then delegates the
 * actual painting and pan/zoom interaction to [DiagramCanvas].
 */
@Composable
@DiagramApi
fun DiagramSurface(
    state: DiagramRenderState,
    modifier: Modifier = Modifier,
    zoomEnabled: Boolean = false,
    presentationMode: DiagramPresentationMode = DiagramPresentationMode.Auto,
) {
    DiagramSurface(
        snapshot = state.snapshot,
        modifier = modifier,
        zoomEnabled = zoomEnabled,
        presentationMode = presentationMode,
    )
}

@Composable
internal fun DiagramSurface(
    snapshot: DiagramSnapshot,
    modifier: Modifier = Modifier,
    zoomEnabled: Boolean = false,
    presentationMode: DiagramPresentationMode = DiagramPresentationMode.Auto,
) {
    val viewportState = rememberDiagramViewportState()
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val density = LocalDensity.current
        val resolvedMode = remember(
            presentationMode,
            constraints.maxWidth,
            constraints.maxHeight,
        ) {
            resolveDiagramPresentationMode(
                requested = presentationMode,
                hasBoundedWidth = constraints.hasBoundedWidth,
                hasBoundedHeight = constraints.hasBoundedHeight,
            )
        }
        val layout = remember(
            snapshot.laidOut?.bounds,
            resolvedMode,
            constraints.maxWidth,
            constraints.maxHeight,
        ) {
            resolveDiagramPresentationLayout(
                bounds = snapshot.laidOut?.bounds,
                mode = resolvedMode,
                maxWidth = constraints.maxWidth.takeIf { constraints.hasBoundedWidth }?.toFloat(),
                maxHeight = constraints.maxHeight.takeIf { constraints.hasBoundedHeight }?.toFloat(),
            )
        }

        val canvasModifier = with(density) {
            Modifier
                .width(layout.width.toDp())
                .height(layout.height.toDp())
        }

        Box(contentAlignment = Alignment.Center) {
            DiagramCanvas(
                snapshot = snapshot,
                modifier = canvasModifier,
                viewportState = viewportState,
                panZoomEnabled = zoomEnabled,
            )
        }
    }
}

@Immutable
internal data class DiagramPresentationLayout(
    val width: Float,
    val height: Float,
)

internal fun resolveDiagramPresentationMode(
    requested: DiagramPresentationMode,
    hasBoundedWidth: Boolean,
    hasBoundedHeight: Boolean,
): DiagramPresentationMode = when (requested) {
    DiagramPresentationMode.Auto -> {
        if (hasBoundedWidth && hasBoundedHeight) DiagramPresentationMode.Viewport
        else DiagramPresentationMode.Embedded
    }
    else -> requested
}

internal fun resolveDiagramPresentationLayout(
    bounds: Rect?,
    mode: DiagramPresentationMode,
    maxWidth: Float? = null,
    maxHeight: Float? = null,
    padding: Float = DEFAULT_PRESENTATION_PADDING,
    fallbackWidth: Float = DEFAULT_FALLBACK_WIDTH,
    fallbackHeight: Float = DEFAULT_FALLBACK_HEIGHT,
): DiagramPresentationLayout = when (mode) {
    DiagramPresentationMode.Viewport -> resolveViewportPresentationLayout(
        bounds = bounds,
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        padding = padding,
        fallbackWidth = fallbackWidth,
        fallbackHeight = fallbackHeight,
    )
    DiagramPresentationMode.Embedded,
    DiagramPresentationMode.Auto,
    -> resolveEmbeddedPresentationLayout(
        bounds = bounds,
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        padding = padding,
        fallbackWidth = fallbackWidth,
        fallbackHeight = fallbackHeight,
    )
}

private fun resolveEmbeddedPresentationLayout(
    bounds: Rect?,
    maxWidth: Float?,
    maxHeight: Float?,
    padding: Float,
    fallbackWidth: Float,
    fallbackHeight: Float,
): DiagramPresentationLayout {
    val boundsWidth = bounds?.size?.width
    val boundsHeight = bounds?.size?.height

    if (boundsWidth == null || boundsHeight == null || boundsWidth <= 0f || boundsHeight <= 0f) {
        return DiagramPresentationLayout(
            width = clampToMax(maxWidth ?: fallbackWidth, maxWidth, fallbackWidth),
            height = clampToMax(fallbackHeight, maxHeight, fallbackHeight),
        )
    }

    var width = boundsWidth + padding
    var height = boundsHeight + padding

    if (maxWidth != null && maxWidth > 0f && width > maxWidth) {
        val scale = maxWidth / width
        width = maxWidth
        height *= scale
    }

    if (maxHeight != null && maxHeight > 0f && height > maxHeight) {
        val scale = maxHeight / height
        width *= scale
        height = maxHeight
    }

    return DiagramPresentationLayout(
        width = max(width, MIN_PRESENTATION_SIZE),
        height = max(height, MIN_PRESENTATION_SIZE),
    )
}

private fun resolveViewportPresentationLayout(
    bounds: Rect?,
    maxWidth: Float?,
    maxHeight: Float?,
    padding: Float,
    fallbackWidth: Float,
    fallbackHeight: Float,
): DiagramPresentationLayout {
    if (maxWidth != null && maxWidth > 0f && maxHeight != null && maxHeight > 0f) {
        return DiagramPresentationLayout(
            width = maxWidth,
            height = maxHeight,
        )
    }
    return resolveEmbeddedPresentationLayout(
        bounds = bounds,
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        padding = padding,
        fallbackWidth = fallbackWidth,
        fallbackHeight = fallbackHeight,
    )
}

private fun clampToMax(
    value: Float,
    maxValue: Float?,
    fallback: Float,
): Float {
    if (maxValue == null || maxValue <= 0f) return max(value, MIN_PRESENTATION_SIZE)
    return max(minOf(value, maxValue), minOf(maxValue, fallback).coerceAtLeast(MIN_PRESENTATION_SIZE))
}

private const val DEFAULT_PRESENTATION_PADDING = 24f
private const val DEFAULT_FALLBACK_WIDTH = 320f
private const val DEFAULT_FALLBACK_HEIGHT = 220f
private const val MIN_PRESENTATION_SIZE = 1f
