package com.shifenmiao.common.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil3.imageLoader
import coil3.request.ImageRequest

/**
 * 图片分页浏览状态
 */
class ImagePagerState(
    val pagerState: PagerState,
    private val scales: MutableList<Float>
) {
    /**
     * 当前页是否可以滑动到其他页
     * 当前页缩放倍数大于 1 时不可滑动
     */
    val canPagerScroll: Boolean
        get() {
            val currentPageScale = scales.getOrElse(pagerState.currentPage) { 1f }
            return currentPageScale <= 1.01f
        }

    /**
     * 更新指定页的缩放倍数
     */
    fun updateScale(index: Int, scale: Float) {
        if (index < scales.size) {
            scales[index] = scale
        }
    }

    /**
     * 获取指定页的缩放倍数
     */
    fun getScale(index: Int): Float = scales.getOrElse(index) { 1f }

    /**
     * 当前页码
     */
    val currentPage: Int get() = pagerState.currentPage
}

/**
 * 记住图片分页浏览状态
 *
 * @param imageCount 图片数量
 * @param initialPage 初始页码
 */
@Composable
fun rememberImagePagerState(
    imageCount: Int,
    initialPage: Int = 0
): ImagePagerState {
    val pagerState = rememberPagerState(initialPage = initialPage) { imageCount }
    val scales = remember {
        mutableStateListOf<Float>().also { list ->
            repeat(imageCount) { list.add(1f) }
        }
    }
    return remember(pagerState, scales) {
        ImagePagerState(pagerState, scales)
    }
}

/**
 * 图片分页浏览组件
 *
 * ## 模块位置
 * - **模块**: `feature/common`
 * - **包路径**: `com.shifenmiao.common.ui`
 * - **文件**: `ImagePager.kt`
 *
 * ## 功能概述
 * 提供图片分页浏览功能：
 * - 水平滑动切换图片
 * - 缩放时禁止滑动
 * - 预加载相邻图片
 * - 可自定义图片项内容
 *
 * ## 使用示例
 * ```kotlin
 * val imagePagerState = rememberImagePagerState(
 *     imageCount = images.size,
 *     initialPage = 0
 * )
 *
 * ImagePager(
 *     images = images,
 *     state = imagePagerState,
 *     onTap = { onDismiss() }
 * )
 * ```
 *
 * @param images 图片 URL 列表
 * @param state 分页状态
 * @param onTap 单击回调
 * @param modifier Modifier
 * @param enablePreload 是否启用预加载
 */
@Composable
fun ImagePager(
    images: List<String>,
    state: ImagePagerState,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    enablePreload: Boolean = true
) {
    val context = LocalContext.current

    // 是否可以滑动
    val canPagerScroll by remember {
        derivedStateOf { state.canPagerScroll }
    }

    // 加载和错误状态
    val isLoading = remember { mutableStateOf(true) }
    val isError = remember { mutableStateOf(false) }

    HorizontalPager(
        state = state.pagerState,
        modifier = modifier.fillMaxSize(),
        key = { images[it] },
        userScrollEnabled = canPagerScroll,
        flingBehavior = PagerDefaults.flingBehavior(state = state.pagerState)
    ) { index ->
        val imageUrl = images[index]
        ZoomableAsyncImage(
            imageUrl = imageUrl,
            onTap = onTap,
            onScaleChanged = { newScale ->
                state.updateScale(index, newScale)
            },
            onLoadingChanged = { isLoading.value = it },
            onErrorChanged = { isError.value = it },
            initialScale = state.getScale(index),
            modifier = Modifier.fillMaxSize()
        )
    }

    // 预加载相邻图片
    if (enablePreload) {
        LaunchedEffect(state.currentPage) {
            val currentPage = state.currentPage
            // 预加载下一页
            if (currentPage < images.size - 1) {
                val nextRequest = ImageRequest.Builder(context)
                    .data(images[currentPage + 1])
                    .build()
                context.imageLoader.enqueue(nextRequest)
            }
            // 预加载前一页
            if (currentPage > 0) {
                val prevRequest = ImageRequest.Builder(context)
                    .data(images[currentPage - 1])
                    .build()
                context.imageLoader.enqueue(prevRequest)
            }
        }
    }
}

/**
 * 带自定义内容的图片分页浏览组件
 *
 * @param images 图片 URL 列表
 * @param state 分页状态
 * @param modifier Modifier
 * @param enablePreload 是否启用预加载
 * @param itemContent 自定义图片项内容
 */
@Composable
fun ImagePager(
    images: List<String>,
    state: ImagePagerState,
    modifier: Modifier = Modifier,
    enablePreload: Boolean = true,
    itemContent: @Composable (index: Int, imageUrl: String) -> Unit
) {
    val context = LocalContext.current

    // 是否可以滑动
    val canPagerScroll by remember {
        derivedStateOf { state.canPagerScroll }
    }

    HorizontalPager(
        state = state.pagerState,
        modifier = modifier.fillMaxSize(),
        key = { images[it] },
        userScrollEnabled = canPagerScroll,
        flingBehavior = PagerDefaults.flingBehavior(state = state.pagerState)
    ) { index ->
        itemContent(index, images[index])
    }

    // 预加载相邻图片
    if (enablePreload) {
        LaunchedEffect(state.currentPage) {
            val currentPage = state.currentPage
            if (currentPage < images.size - 1) {
                val nextRequest = ImageRequest.Builder(context)
                    .data(images[currentPage + 1])
                    .build()
                context.imageLoader.enqueue(nextRequest)
            }
            if (currentPage > 0) {
                val prevRequest = ImageRequest.Builder(context)
                    .data(images[currentPage - 1])
                    .build()
                context.imageLoader.enqueue(prevRequest)
            }
        }
    }
}

