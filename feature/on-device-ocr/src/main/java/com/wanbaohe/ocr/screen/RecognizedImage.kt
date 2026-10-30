package com.wanbaohe.ocr.screen

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import com.wanbaohe.ocr.R
import com.wanbaohe.ocr.domain.PixelPoint
import com.wanbaohe.ocr.domain.RecognizedLine

@Composable
internal fun RecognizedImage(
    bitmap: Bitmap,
    lines: List<RecognizedLine>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val image = remember(bitmap) { bitmap.asImageBitmap() }
    val stroke = MaterialTheme.colorScheme.onPrimaryContainer
    val fill = MaterialTheme.colorScheme.primaryContainer
    val density = LocalDensity.current
    BoxWithConstraints(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val availableHeight = if (maxHeight == Dp.Infinity) maxWidth else maxHeight
        val drawWidth = fittedWidth(maxWidth, availableHeight, bitmap.width, bitmap.height)
        val drawHeight = drawWidth * bitmap.height / bitmap.width
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
                            if (hit >= 0) onSelect(hit)
                        }
                    },
            ) {
                val scaleX = size.width / bitmap.width
                val scaleY = size.height / bitmap.height
                lines.forEachIndexed { index, line ->
                    val path = polygon(line.corners, scaleX, scaleY)
                    val selected = index == selectedIndex
                    drawPath(path, color = fill.copy(alpha = if (selected) SELECTED_FILL else IDLE_FILL))
                    drawPath(
                        path,
                        color = stroke,
                        style = Stroke(
                            width = with(density) {
                                (if (selected) SELECTED_STROKE else IDLE_STROKE).toPx()
                            },
                        ),
                    )
                }
            }
        }
    }
}

private fun fittedWidth(maxWidth: Dp, maxHeight: Dp, imageWidth: Int, imageHeight: Int): Dp {
    if (imageWidth <= 0 || imageHeight <= 0) return maxWidth
    val imageRatio = imageWidth.toFloat() / imageHeight
    val boxRatio = maxWidth / maxHeight
    return if (imageRatio > boxRatio) maxWidth else maxHeight * imageRatio
}

private fun polygon(corners: List<PixelPoint>, scaleX: Float, scaleY: Float): Path {
    val path = Path()
    corners.forEachIndexed { index, point ->
        val offset = Offset(point.x * scaleX, point.y * scaleY)
        if (index == 0) path.moveTo(offset.x, offset.y) else path.lineTo(offset.x, offset.y)
    }
    path.close()
    return path
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
private val SELECTED_STROKE = 3.dp
private val IDLE_STROKE = 1.5.dp
private const val SELECTED_FILL = 0.34f
private const val IDLE_FILL = 0.16f
