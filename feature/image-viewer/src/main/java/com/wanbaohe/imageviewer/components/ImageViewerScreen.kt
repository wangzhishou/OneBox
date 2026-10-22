package com.wanbaohe.imageviewer.components

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import coil3.compose.AsyncImagePainter
import com.wanbaohe.base.ui.page.PageIndicator
import com.shifenmiao.common.ui.ImageViewerBaseScreen
import com.shifenmiao.common.ui.ViewerActionButton
import com.shifenmiao.common.ui.rememberImmersiveModeState
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.ImageEdit
import com.wanbaohe.core.ui.utils.content.FileAccessGuidance
import com.wanbaohe.core.ui.utils.content.rememberFileAccessController
import com.wanbaohe.core.ui.widget.image.Picture
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
        onEdit = imageViewerComponent::editImage,
        onDismiss = imageViewerComponent::exit
    )
}

@Composable
fun ImageViewer(
    images: List<String>,
    initialIndex: Int = 0,
    onDownload: (String) -> Unit = {},
    onEdit: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    val immersiveModeState = rememberImmersiveModeState()
    val pagerState = rememberPagerState(initialPage = initialIndex) { images.size }

    ImageViewerBaseScreen(
        onDismiss = onDismiss,
        immersiveModeState = immersiveModeState,
        title = {
            PageIndicator(
                pageCount = images.size,
                currentPage = pagerState.currentPage
            )
        },
        actions = {
            EditButton(
                images = images,
                currentPage = pagerState.currentPage,
                onEdit = onEdit
            )
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
                onTap = { immersiveModeState.toggle() }
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

    // file:// 直开无权限时走统一授权引导(媒体权限 / SAF 文件夹,含已授权静默恢复)
    val accessController = rememberFileAccessController(
        uriString = imageUrl,
        isLoadFailed = isError,
        mediaReadPermission = Manifest.permission.READ_MEDIA_IMAGES
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        key(accessController.retrySignal) {
            Picture(
                model = accessController.effectiveUriString,
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
        }

        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        AnimatedVisibility(
            visible = isError,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            when {
                // 静默检查已持久化的授权,期间不闪错误图标
                accessController.isCheckingPersistedGrants -> Unit
                accessController.isGuidanceVisible -> FileAccessGuidance(accessController)
                else -> {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineError,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

private fun isNetworkImage(url: String): Boolean {
    return url.startsWith("http://") || url.startsWith("https://")
}

@Composable
private fun EditButton(
    images: List<String>,
    currentPage: Int,
    onEdit: (String) -> Unit
) {
    val url = images.getOrNull(currentPage) ?: return

    AnimatedVisibility(
        visible = !isNetworkImage(url),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ViewerActionButton(onClick = { onEdit(url) }) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ImageEdit,
                contentDescription = stringResource(R.string.edit)
            )
        }
    }
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
