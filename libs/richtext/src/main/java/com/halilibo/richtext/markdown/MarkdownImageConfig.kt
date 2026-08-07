package com.halilibo.richtext.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.layout.ContentScale

/**
    BasicMarkdown(
      astNode = ast,
    imageConfig = MarkdownImageConfig(
    loading = {
    // 你的占位 UI，比如 CircularProgressIndicator / Skeleton
    },
    error = {
    // 你的错误 UI，比如“加载失败，点击重试”
    },
    onImageClick = { url, title ->
    // 打开预览
    }
    )
    )
 *
 * */
@Immutable
data class MarkdownImageConfig(
    val contentScale: ContentScale = ContentScale.FillWidth,
    val onImageClick: ((url: String, title: String?) -> Unit)? = null,
    val onImageLongClick: ((url: String, title: String?) -> Unit)? = null,
    val loading: (@Composable () -> Unit)? = null,
    val error: (@Composable () -> Unit)? = null
)
