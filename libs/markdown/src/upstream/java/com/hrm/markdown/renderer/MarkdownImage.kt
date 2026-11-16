package com.hrm.markdown.renderer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter

/**
 * 图片渲染所需的数据模型。
 *
 * @param url 图片 URL
 * @param altText 替代文本（无障碍）
 * @param title 图片标题（tooltip）
 * @param width 指定宽度（像素），null 表示自适应
 * @param height 指定高度（像素），null 表示自适应
 * @param attributes 额外属性（class, id, loading, align 等）
 */
@Immutable
data class MarkdownImageData(
    val url: String,
    val altText: String,
    val title: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val attributes: Map<String, String> = emptyMap(),
) {
    /** 获取对齐方式 */
    val align: String?
        get() = attributes["align"]

    /** 获取 loading 模式（lazy/eager） */
    val loading: String?
        get() = attributes["loading"]

    /** 获取 CSS class 列表 */
    val cssClasses: List<String>
        get() = attributes["class"]?.split(" ")?.filter { it.isNotEmpty() } ?: emptyList()

    /** 获取 CSS ID */
    val cssId: String?
        get() = attributes["id"]
}

/**
 * 图片渲染器的类型别名。
 * 外部可以通过提供自定义实现来替换默认的图片加载逻辑。
 */
typealias MarkdownImageRenderer = @Composable (data: MarkdownImageData, modifier: Modifier) -> Unit

/**
 * 用于在组件树中传递自定义图片渲染器的 CompositionLocal。
 *
 * 默认值为 null，表示使用内置 Coil 渲染器。
 * 外部调用者可以通过 [Markdown] 的 `components.image` 插槽提供自定义实现，
 * 例如使用 Coil、Kamel、Ktor 等图片加载库。
 *
 * 使用示例：
 * ```kotlin
 * Markdown(
 *     markdown = "![alt](https://example.com/image.png =200x300)",
 *     components = MarkdownComponents(image = { data, modifier ->
 *         // 使用 Coil 或其他图片加载库
 *         AsyncImage(
 *             model = data.url,
 *             contentDescription = data.altText,
 *             modifier = modifier,
 *         )
 *     }),
 * )
 * ```
 */
internal val LocalImageRenderer = compositionLocalOf<MarkdownImageRenderer?> { null }

/**
 * 默认的图片渲染组件。
 *
 * 使用 Coil3 的 [AsyncImage] 从网络或本地加载图片。
 * 加载过程中显示 loading 指示器，加载失败时显示替代文本。
 *
 * 外部使用者也可通过 [Markdown] 的 `components.image` 插槽传入自定义图片加载实现来覆盖此行为。
 */
@Composable
internal fun DefaultMarkdownImage(
    data: MarkdownImageData,
    modifier: Modifier = Modifier,
) {
    var painterState by remember(data.url) {
        mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty)
    }
    val loaded = painterState is AsyncImagePainter.State.Success
    val failed = painterState is AsyncImagePainter.State.Error
    val imageAlpha by animateFloatAsState(
        targetValue = if (loaded) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "markdown-image-alpha",
    )
    val placeholderShape = RoundedCornerShape(10.dp)

    Box(
        modifier = modifier
            .applyImageSize(data.width, data.height)
            // 未拿到真实高度时先占一块 16:9 左右的空间，避免图片加载完成后整页突然跳一下。
            .then(if (!loaded && data.height == null) Modifier.height(180.dp) else Modifier)
            .padding(vertical = 4.dp)
            .clip(placeholderShape)
            .background(MaterialTheme.colorScheme.surfaceContainer),
        contentAlignment = Alignment.Center,
    ) {
        when {
            !loaded && !failed -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            failed -> {
                Text(
                    text = data.altText.ifBlank { data.title.orEmpty() },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        AsyncImage(
            model = data.url,
            contentDescription = data.altText.ifEmpty { data.title },
            modifier = Modifier
                .applyImageSize(data.width, data.height)
                .graphicsLayer { alpha = imageAlpha },
            contentScale = if (data.width != null || data.height != null) {
                ContentScale.Fit
            } else {
                ContentScale.FillWidth
            },
            onState = { painterState = it },
        )
    }
}

/**
 * 根据图片数据的宽高约束应用 Modifier。
 */
internal fun Modifier.applyImageSize(width: Int?, height: Int?): Modifier {
    var mod = this
    if (width != null && height != null) {
        mod = mod.size(width.dp, height.dp)
    } else if (width != null) {
        mod = mod.widthIn(max = width.dp)
    } else if (height != null) {
        mod = mod.heightIn(max = height.dp)
    } else {
        mod = mod.fillMaxWidth()
    }
    return mod
}
