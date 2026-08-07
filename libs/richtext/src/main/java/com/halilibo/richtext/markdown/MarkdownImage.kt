package com.halilibo.richtext.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImageScope
import com.t8rin.imagetoolbox.core.resources.icons.line.LineError

internal val MarkdownImageCornerRadius = 16.dp
internal val MarkdownImageMaxHeight = 320.dp

/**
 * Markdown 图片的统一包装组件。
 *
 * 提供：
 * - 最大高度限制
 * - surfaceContainer 背景 + 16.dp 圆角
 * - 图片按宽度铺满、等比缩放
 * - 默认加载/错误占位
 *
 * 注意：宽度由调用方通过 [modifier] 控制（默认使用 [Modifier.fillMaxWidth()]）。
 *
 * @param url 图片地址
 * @param title 图片描述
 * @param modifier 外部传入的 Modifier，用于控制宽度等
 * @param contentScale 图片缩放模式
 * @param onClick 点击回调
 * @param onLongClick 长按回调
 * @param loading 加载占位，null 时使用默认
 * @param error 错误占位，null 时使用默认
 */
@Composable
internal fun MarkdownImage(
    url: String,
    title: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillWidth,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    loading: (@Composable SubcomposeAsyncImageScope.(AsyncImagePainter.State.Loading) -> Unit)? = null,
    error: (@Composable SubcomposeAsyncImageScope.(AsyncImagePainter.State.Error) -> Unit)? = null
) {
    Box(
        modifier = modifier
            .heightIn(max = MarkdownImageMaxHeight)
            .clip(RoundedCornerShape(MarkdownImageCornerRadius))
            .background(MaterialTheme.colorScheme.surfaceContainer),
        contentAlignment = Alignment.Center
    ) {
        RemoteImage(
            url = url,
            contentDescription = title,
            modifier = Modifier.fillMaxWidth(),
            contentScale = contentScale,
            onClick = onClick,
            onLongClick = onLongClick,
            loading = loading ?: { DefaultMarkdownImageLoading() },
            error = error ?: { DefaultMarkdownImageError() }
        )
    }
}

@Composable
private fun SubcomposeAsyncImageScope.DefaultMarkdownImageLoading() {
    CircularProgressIndicator(
        modifier = Modifier.size(28.dp),
        color = MaterialTheme.colorScheme.primary,
        strokeWidth = 2.5.dp
    )
}

@Composable
private fun SubcomposeAsyncImageScope.DefaultMarkdownImageError() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
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
