package com.halilibo.richtext.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

/**
 * 正文图片一律完整显示：最大宽度是所在栏宽，最大高度是窗口高度，
 * 超出才按更紧的那条边等比缩小，不裁切、不放大。
 *
 * BasicMarkdown(
 *   astNode = ast,
 *   imageConfig = MarkdownImageConfig(
 *     loading = { },
 *     error = { },
 *     onImageClick = { url, title -> }
 *   )
 * )
 */
@Immutable
data class MarkdownImageConfig(
    val onImageClick: ((url: String, title: String?) -> Unit)? = null,
    val onImageLongClick: ((url: String, title: String?) -> Unit)? = null,
    val loading: (@Composable () -> Unit)? = null,
    val error: (@Composable () -> Unit)? = null
)
