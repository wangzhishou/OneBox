package com.wanbaohe.blog.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.MarkdownLazyContent
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.core.R
import com.shifenmiao.model.blog.BlogDetailState
import com.shifenmiao.common.components.blog.AuthorInfo
import com.shifenmiao.common.components.common.ImageThumbnailRow
import com.shifenmiao.webview.mermaid.ProvideMermaidRenderer
import com.wanbaohe.blog.logic.BlogComponent

@Composable
fun BlogContent(
    blogComponent: BlogComponent,
    onGoBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val blogDetailState by blogComponent.blogDetailState.collectAsState()
    val lazyListState = rememberLazyListState()
    val navigator = LocalUrlNavigator.current
    val uriHandler = LocalUriHandler.current

    // 根据 BlogDetailState 映射到 MarkdownLazyContent 的参数
    val isLoading = blogDetailState is BlogDetailState.Loading
    val errorMessage = (blogDetailState as? BlogDetailState.Error)?.message
        ?: if (blogDetailState is BlogDetailState.Error) stringResource(R.string.error_message) else null
    val blog = (blogDetailState as? BlogDetailState.Success)?.blog
    val markdownMessage = blog?.content.orEmpty()

    ProvideMermaidRenderer {
        MarkdownLazyContent(
            message = markdownMessage,
            modifier = modifier.fillMaxSize(),
            isLoading = isLoading,
            errorMessage = errorMessage,
            onRetry = { blogComponent.onRetry() },
            onGoBack = onGoBack,
            onLinkClick = { url ->
                if (!navigator.navigate(url)) {
                    uriHandler.openUri(url)
                }
            },
            lazyListState = lazyListState,
            headerContent = {
                // 图片区域
                blog?.picture?.takeIf { it.isNotEmpty() }?.let { images ->
                    item(key = "blog_images") {
                        Spacer(modifier = Modifier.height(8.dp))
                        ImageThumbnailRow(images = images)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            },
            footerContent = {
                // 作者信息
                blog?.author?.let { author ->
                    item(key = "blog_author") {
                        Spacer(modifier = Modifier.height(8.dp))
                        AuthorInfo(
                            authorName = author.nickname.orEmpty(),
                            authorAvatar = author.avatar,
                            publishDate = blog.publishedAt
                        )
                    }
                }
                // 底部留白
                item(key = "blog_bottom_spacer") {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        )
    }
}