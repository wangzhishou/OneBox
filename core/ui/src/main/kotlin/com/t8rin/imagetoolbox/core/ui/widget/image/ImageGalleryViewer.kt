package com.t8rin.imagetoolbox.core.ui.widget.image

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import kotlinx.coroutines.delay
import com.shifenmiao.base.ui.page.PageIndicator
import com.t8rin.imagetoolbox.core.ui.widget.utils.FullscreenPopup
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.toggleScale
import net.engawapg.lib.zoomable.zoomable
import com.t8rin.imagetoolbox.core.resources.icons.line.LineError

/**
 * 通用图片画廊查看器
 *
 * ## 功能概述
 * - 全屏弹窗展示图片列表
 * - 左右滑动切换图片
 * - 双指缩放、双击放大/复位
 * - 加载与错误状态提示
 * - 多图时显示页码指示器
 * - 返回/点击关闭
 *
 * ## 使用示例
 * ```kotlin
 * ImageGalleryViewer(
 *     images = listOf("https://example.com/a.jpg", "https://example.com/b.jpg"),
 *     initialIndex = 0,
 *     onDismiss = { /* 关闭 */ }
 * )
 * ```
 *
 * @param images 图片 URL 列表
 * @param initialIndex 初始显示的图片索引
 * @param onDismiss 关闭回调
 */
private const val ImageGalleryAnimationDurationMs = 300

@Composable
fun ImageGalleryViewer(
    images: List<String>,
    initialIndex: Int = 0,
    onDismiss: () -> Unit
) {
    if (images.isEmpty()) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }

    val safeInitialIndex = initialIndex.coerceIn(0, images.lastIndex)
    val pagerState = rememberPagerState(initialPage = safeInitialIndex) { images.size }
    var isVisible by remember { mutableStateOf(true) }

    val dismissWithAnimation = remember {
        {
            isVisible = false
        }
    }

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            delay(ImageGalleryAnimationDurationMs.toLong())
            onDismiss()
        }
    }

    FullscreenPopup(
        onDismiss = dismissWithAnimation,
        placeAboveAll = true
    ) {
        BackHandler(onBack = dismissWithAnimation)

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(ImageGalleryAnimationDurationMs)),
            exit = fadeOut(tween(ImageGalleryAnimationDurationMs))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .navigationBarsPadding()
                    .statusBarsPadding()
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    key = { images[it] },
                    beyondViewportPageCount = 1
                ) { index ->
                    GalleryImagePage(
                        imageUrl = images[index],
                        onDismiss = dismissWithAnimation
                    )
                }

                if (images.size > 1) {
                    PageIndicator(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp),
                        pageCount = images.size,
                        currentPage = pagerState.currentPage
                    )
                }
            }
        }
    }
}

@Composable
private fun GalleryImagePage(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    val zoomState = rememberZoomState(maxScale = 5f)
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
                    onTap = { onDismiss() },
                    onDoubleTap = { position ->
                        zoomState.toggleScale(targetScale = 2.5f, position = position)
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
