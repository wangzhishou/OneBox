package com.halilibo.richtext.markdown

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.size.Scale
import coil3.size.Size
import com.t8rin.imagetoolbox.core.resources.icons.line.LineError

internal val MarkdownImageCornerRadius = 16.dp

/** 尺寸未知时的占位高度。加载完成后换成图片真实显示尺寸。 */
private val MarkdownImagePlaceholderHeight = 160.dp

/**
 * 正文图片。
 *
 * 最大宽度是当前栏宽（正文区域，不会超出屏幕），最大高度是窗口高度。
 * 两条边都没超出时按原图像素显示，不放大；超出时按更紧的那条边等比缩小，整张图都留在框里。
 * 图比栏窄时在栏宽内水平居中。
 * 解码目标也是这个框，避免把原图整张解进内存。
 */
@Composable
internal fun MarkdownImage(
    url: String,
    title: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    loading: (@Composable () -> Unit)? = null,
    error: (@Composable () -> Unit)? = null
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val windowSize = LocalWindowInfo.current.containerSize
    val windowHeightPx = windowSize.height.takeIf { it > 0 }
        ?: with(density) { 800.dp.roundToPx() }
    val windowWidthPx = windowSize.width.takeIf { it > 0 } ?: windowHeightPx

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val maxWidthPx = maxBoundedPx(constraints.maxWidth, windowWidthPx)
        val maxHeightPx = minOf(
            maxBoundedPx(constraints.maxHeight, windowHeightPx),
            windowHeightPx
        )
        val request = remember(url, context, maxWidthPx, maxHeightPx) {
            ImageRequest.Builder(context)
                .data(url)
                .size(Size(maxWidthPx, maxHeightPx))
                .scale(Scale.FIT)
                .build()
        }
        val painter = rememberAsyncImagePainter(
            model = request,
            contentScale = ContentScale.Fit
        )
        val imageState = painter.state.collectAsState().value

        val fitted = if (imageState is AsyncImagePainter.State.Success) {
            val image = imageState.result.image
            fittedImageSize(
                imageWidth = image.width.toFloat(),
                imageHeight = image.height.toFloat(),
                maxWidthPx = maxWidthPx,
                maxHeightPx = maxHeightPx
            )
        } else {
            null
        }

        val frameModifier = if (fitted != null) {
            with(density) {
                Modifier.size(fitted.first.toDp(), fitted.second.toDp())
            }
        } else {
            with(density) {
                Modifier
                    .width(maxWidthPx.toDp())
                    .height(MarkdownImagePlaceholderHeight)
            }
        }

        val clickModifier = if (onClick != null || onLongClick != null) {
            Modifier.combinedClickable(
                onClick = { onClick?.invoke() },
                onLongClick = onLongClick
            )
        } else {
            Modifier
        }

        Box(
            modifier = frameModifier
                .clip(RoundedCornerShape(MarkdownImageCornerRadius))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .then(clickModifier),
            contentAlignment = Alignment.Center
        ) {
            if (imageState is AsyncImagePainter.State.Success) {
                Image(
                    painter = painter,
                    contentDescription = title,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Fit
                )
            }
            when (imageState) {
                is AsyncImagePainter.State.Loading -> {
                    if (loading != null) {
                        loading()
                    } else {
                        DefaultMarkdownImageLoading()
                    }
                }
                is AsyncImagePainter.State.Error -> {
                    if (error != null) {
                        error()
                    } else {
                        DefaultMarkdownImageError()
                    }
                }
                else -> Unit
            }
        }
    }
}

/**
 * 原图不放大。宽或高超出上限时，取更小的那个比例，两条边一起缩。
 * 返回值为显示用的像素尺寸。
 */
private fun fittedImageSize(
    imageWidth: Float,
    imageHeight: Float,
    maxWidthPx: Int,
    maxHeightPx: Int
): Pair<Float, Float>? {
    if (maxWidthPx <= 0 || maxHeightPx <= 0) return null
    if (!imageWidth.isFinite() || !imageHeight.isFinite()) return null
    if (imageWidth <= 0f || imageHeight <= 0f) return null
    val scale = minOf(
        maxWidthPx / imageWidth,
        maxHeightPx / imageHeight,
        1f
    )
    return (imageWidth * scale) to (imageHeight * scale)
}

private fun maxBoundedPx(constraintPx: Int, fallbackPx: Int): Int {
    val bounded = if (constraintPx == Constraints.Infinity || constraintPx <= 0) {
        fallbackPx
    } else {
        constraintPx
    }
    return bounded.coerceAtLeast(1)
}

@Composable
private fun DefaultMarkdownImageLoading() {
    CircularProgressIndicator(
        modifier = Modifier.size(28.dp),
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        strokeWidth = 2.5.dp
    )
}

@Composable
private fun DefaultMarkdownImageError() {
    Box(
        modifier = Modifier.padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineError,
            contentDescription = "Image load failed",
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
