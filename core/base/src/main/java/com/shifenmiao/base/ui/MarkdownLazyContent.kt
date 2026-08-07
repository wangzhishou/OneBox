package com.shifenmiao.base.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.commonmark.CommonMarkdownParseOptions
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.markdown.MarkdownImageConfig
import com.halilibo.richtext.markwon.MarkdownAstNodeParser
import com.halilibo.richtext.ui.material3.RichText
import com.t8rin.imagetoolbox.core.ui.widget.image.ImageGalleryViewer
import com.shifenmiao.base.components.CenterErrorBox
import com.shifenmiao.base.ui.loading.EmptyBox
import com.shifenmiao.core.R
import com.shifenmiao.core.ui.loading.DotDanceLoading
import com.shifenmiao.model.node.AstNode
import io.noties.markwon.plugins.codeblock.CodeBlockClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 基于 LazyColumn 的 Markdown 内容展示组件。
 *
 * 将 Markdown 文本解析为 AST，每个顶层 Block 作为 LazyColumn 的独立 Item 进行渲染，
 * 支持 Loading、Error、Empty 三种状态展示。
 *
 * @param message        Markdown 原始文本
 * @param modifier       外层 Modifier
 * @param isLoading      是否处于加载状态（数据尚未就绪时由调用方控制）
 * @param errorMessage   错误信息，非空时展示错误页面
 * @param onRetry        错误页面点击重试的回调
 * @param onGoBack       错误页面点击返回的回调
 * @param paddingValues  LazyColumn 的 contentPadding
 * @param emptyText      空页面提示文字
 * @param lazyListState  外部传入的 LazyListState，可用于联动滚动
 * @param headerContent  在 Markdown 块之前插入的 LazyColumn items（如图片、标题等）
 * @param footerContent  在 Markdown 块之后插入的 LazyColumn items（如作者信息等）
 */
@Composable
fun MarkdownLazyContent(
    message: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    onGoBack: () -> Unit = {},
    paddingValues: PaddingValues = PaddingValues(
        horizontal = 16.dp,
        vertical = 8.dp
    ),
    emptyText: String = stringResource(R.string.load_empty_toast),
    lazyListState: LazyListState = rememberLazyListState(),
    headerContent: (LazyListScope.() -> Unit)? = null,
    footerContent: (LazyListScope.() -> Unit)? = null,
    codeBlockClickListener: CodeBlockClickListener? = null,
    onLinkClick: ((String) -> Unit)? = null
) {

    when {
        // ── 加载中 ──────────────────────────────────────────────
        isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                DotDanceLoading()
            }
        }

        // ── 错误状态 ────────────────────────────────────────────
        errorMessage != null -> {
            CenterErrorBox(
                errorMessage = errorMessage,
                onRetry = onRetry,
                onGoBack = onGoBack
            )
        }

        // ── 空内容 ──────────────────────────────────────────────
        message.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyBox(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    text = emptyText,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── 正常渲染 ────────────────────────────────────────────
        else -> {
            val context = LocalContext.current
            var blocks by remember { mutableStateOf<List<AstNode>>(emptyList()) }
            var isParsing by remember { mutableStateOf(true) }
            var previewImageUrl by remember { mutableStateOf<String?>(null) }

            val imageConfig = remember {
                MarkdownImageConfig(
                    onImageClick = { url, _ ->
                        previewImageUrl = url
                    }
                )
            }

            LaunchedEffect(message) {
                isParsing = true
                withContext(Dispatchers.IO) {
                    try {
                        val parser = MarkdownAstNodeParser(
                            context,
                            CommonMarkdownParseOptions.Default
                        )
                        val rootNode = parser.parse(message)
                        // 提取 Document 根节点的直接子节点（顶层 Block）
                        val children = mutableListOf<AstNode>()
                        var child = rootNode.links.firstChild
                        while (child != null) {
                            children.add(child)
                            child = child.links.next
                        }
                        blocks = children
                    } catch (_: Exception) {
                        blocks = emptyList()
                    }
                }
                isParsing = false
            }

            if (isParsing && blocks.isEmpty()) {
                // Markdown 解析中，显示加载动画
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    DotDanceLoading()
                }
            } else if (blocks.isEmpty()) {
                // 解析完成但无内容
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyBox(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        text = emptyText,
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val lazyColumn: @Composable () -> Unit = {
                    LazyColumn(
                        state = lazyListState,
                        contentPadding = paddingValues,
                        modifier = modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ── header items ──
                        headerContent?.invoke(this)

                        // ── markdown block items ──
                        items(
                            count = blocks.size,
                            key = { index -> "md_block_$index" }
                        ) { index ->
                            RichText(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                BasicMarkdown(
                                    astNode = blocks[index],
                                    codeBlockClickListener = codeBlockClickListener,
                                    imageConfig = imageConfig
                                )
                            }
                        }

                        // ── footer items ──
                        footerContent?.invoke(this)
                    }
                }

                if (onLinkClick != null) {
                    val uriHandler = remember(onLinkClick) {
                        object : UriHandler {
                            override fun openUri(uri: String) {
                                onLinkClick(uri)
                            }
                        }
                    }
                    CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                        lazyColumn()
                    }
                } else {
                    lazyColumn()
                }

                previewImageUrl?.let { url ->
                    ImageGalleryViewer(
                        images = listOf(url),
                        onDismiss = { previewImageUrl = null }
                    )
                }
            }
        }
    }
}

