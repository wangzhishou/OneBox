package com.wanbaohe.imageviewer.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImagePainter
import com.shifenmiao.base.ui.page.PageIndicator
import com.shifenmiao.common.ui.ImageViewerBaseScreen
import com.shifenmiao.common.ui.ViewerActionButton
import com.shifenmiao.common.ui.rememberImmersiveModeState
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.wanbaohe.imageviewer.screenLogic.ImageViewerComponent
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.toggleScale
import net.engawapg.lib.zoomable.zoomable
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDownload
import com.t8rin.imagetoolbox.core.resources.icons.line.LineError

private const val MaxZoomScale = 5f
private const val DoubleTapZoomScale = 2.5f

@Composable
fun ImageViewerScreen(
    imageViewerComponent: ImageViewerComponent
) {
    val images = imageViewerComponent.imageViewerInfo?.images
    val initialIndex = imageViewerComponent.imageViewerInfo?.initialIndex ?: 0

    if (images.isNullOrEmpty()) {
        LaunchedEffect(Unit) {
            imageViewerComponent.exit()
        }
        return
    }

    ImageViewer(
        images = images,
        initialIndex = initialIndex,
        onDownload = imageViewerComponent::downloadImage,
        onDismiss = imageViewerComponent::exit
    )
}

@Composable
fun ImageViewer(
    images: List<String>,
    initialIndex: Int = 0,
    onDownload: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    val isSingleLocalImage = remember(images) {
        images.size == 1 && !isNetworkImage(images.first())
    }

    val immersiveModeState = rememberImmersiveModeState(initialValue = isSingleLocalImage)
    val pagerState = rememberPagerState(initialPage = initialIndex) { images.size }

    ImageViewerBaseScreen(
        onDismiss = onDismiss,
        immersiveModeState = immersiveModeState,
        backDirectlyExit = isSingleLocalImage,
        title = {
            PageIndicator(
                pageCount = images.size,
                currentPage = pagerState.currentPage
            )
        },
        actions = {
            DownloadButton(
                images = images,
                currentPage = pagerState.currentPage,
                onDownload = onDownload
            )
        }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { images[it] },
            beyondViewportPageCount = 1
        ) { index ->
            ZoomableImagePage(
                imageUrl = images[index],
                onTap = {
                    if (isSingleLocalImage) {
                        onDismiss()
                    } else {
                        immersiveModeState.toggle()
                    }
                }
            )
        }
    }
}

@Composable
private fun ZoomableImagePage(
    imageUrl: String,
    onTap: () -> Unit
) {
    val zoomState = rememberZoomState(maxScale = MaxZoomScale)
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Picture(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            shape = RectangleShape,
            showTransparencyChecker = false,
            shimmerEnabled = false,
            crossfadeEnabled = true,
            modifier = Modifier
                .fillMaxSize()
                .zoomable(
                    zoomState = zoomState,
                    onTap = { onTap() },
                    onDoubleTap = { position ->
                        zoomState.toggleScale(
                            targetScale = DoubleTapZoomScale,
                            position = position
                        )
                    }
                ),
            onState = { state ->
                isLoading = state is AsyncImagePainter.State.Loading
                isError = state is AsyncImagePainter.State.Error
            }
        )

        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primaryContainer
            )
        }

        AnimatedVisibility(
            visible = isError,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineError,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

private fun isNetworkImage(url: String): Boolean {
    return url.startsWith("http://") || url.startsWith("https://")
}

@Composable
private fun DownloadButton(
    images: List<String>,
    currentPage: Int,
    onDownload: (String) -> Unit
) {
    val url = images.getOrNull(currentPage) ?: return

    AnimatedVisibility(
        visible = isNetworkImage(url),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ViewerActionButton(onClick = { onDownload(url) }) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDownload,
                contentDescription = "Download"
            )
        }
    }
}
