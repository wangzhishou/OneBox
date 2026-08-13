package com.shifenmiao.ai.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.commonmark.CommonMarkdownParseOptions
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.markdown.splitMarkdownBlocks
import com.halilibo.richtext.markwon.MarkdownAstNodeParser
import com.halilibo.richtext.ui.material3.RichText
import com.shifenmiao.ai.component.AIStreamAnswerComponent
import com.shifenmiao.ai.component.AIStreamAnswerStatus
import com.shifenmiao.ai.model.BlockReuseCache
import com.shifenmiao.base.ui.AILoadingRow
import com.shifenmiao.base.ui.ExpandableMarkdownContent
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.model.node.AstNode
import com.shifenmiao.theme.AppTheme
import com.shifenmiao.webview.mermaid.ProvideMermaidRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.t8rin.imagetoolbox.core.resources.icons.Refresh

@Composable
fun AIStreamAnswerScreen(
    appComponent: AppComponent,
    component: AIStreamAnswerComponent,
) {
    val status by component.status.collectAsState()
    val accumulatedText by component.accumulatedText.collectAsState()
    val reasoningText by component.reasoningText.collectAsState()
    val errorMessage by component.errorMessage.collectAsState()
    val engineInfo by component.engineInfo.collectAsState()

    BaseScreen(
        title = component.displayTitle,
        onGoBack = appComponent.onGoBack,
    ) {
        ProvideMermaidRenderer {
            AnimatedContent(
                targetState = status,
                transitionSpec = {
                    fadeIn(tween(300)).togetherWith(fadeOut(tween(150)))
                },
                label = "AIStreamAnswerContent",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { currentStatus ->
                when (currentStatus) {
                    AIStreamAnswerStatus.LOADING -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                        ) {
                            AILoadingRow()
                        }
                    }

                    AIStreamAnswerStatus.STREAMING -> {
                        MarkdownScrollContainer(
                            modifier = Modifier.fillMaxSize(),
                            autoScroll = true,
                            scrollTrigger = accumulatedText.length + reasoningText.length,
                        ) {
                            if (reasoningText.isNotEmpty()) {
                                ExpandableMarkdownContent(reasoningText)
                            }
                            MarkdownBlocksContent(
                                text = accumulatedText,
                                isStreaming = true,
                            )
                        }
                    }

                    AIStreamAnswerStatus.SUCCESS -> {
                        if (component.useStreaming) {
                            // 流式路径：文本已实时显示过，直接渲染分块 Markdown（无 cursor）
                            MarkdownScrollContainer(modifier = Modifier.fillMaxSize()) {
                                if (reasoningText.isNotEmpty()) {
                                    ExpandableMarkdownContent(reasoningText)
                                }
                                MarkdownBlocksContent(
                                    text = accumulatedText,
                                    isStreaming = false,
                                )
                            }
                        } else {
                            // 同步路径：一次性返回，需打字机动画
                            TypewriterMarkdownBlock(
                                targetText = accumulatedText,
                                engineInfo = engineInfo,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }

                    AIStreamAnswerStatus.ERROR -> {
                        StreamAnswerErrorBlock(
                            error = errorMessage,
                            onRetry = component::retry,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

// ── 通用滚动容器 ──────────────────────────────────────────────────────

@Composable
private fun MarkdownScrollContainer(
    modifier: Modifier = Modifier,
    autoScroll: Boolean = false,
    scrollTrigger: Any = Unit,
    content: @Composable () -> Unit,
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(scrollTrigger) {
        if (autoScroll) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        content()
    }
}

// ── 分块 AST 渲染 + 可选 inline cursor ────────────────────────────────

@Composable
private fun MarkdownBlocksContent(
    text: String,
    isStreaming: Boolean,
) {
    val context = LocalContext.current
    val blockReuseCache = remember { BlockReuseCache() }
    var blocks by remember { mutableStateOf<List<AstNode>>(emptyList()) }
    // 流式期间和完成态必须分 key，否则签名跳过 cursor 会导致完成态错误复用带 cursor 的节点
    val cacheKey = if (isStreaming) "stream_cursor" else "stream_final"

    DisposableEffect(Unit) {
        onDispose {
            blockReuseCache.clear(cacheKey)
        }
    }

    LaunchedEffect(text, isStreaming) {
        if (text.isEmpty()) {
            blocks = emptyList()
            blockReuseCache.clear(cacheKey)
            return@LaunchedEffect
        }
        val parsedBlocks = withContext(Dispatchers.IO) {
            try {
                val parser = MarkdownAstNodeParser(context, CommonMarkdownParseOptions.Default)
                val rootNode = parser.parse(text)
                val newBlocks = splitMarkdownBlocks(rootNode)
                blockReuseCache.trim(cacheKey, newBlocks.size)
                newBlocks.mapIndexed { index, newNode ->
                    blockReuseCache.reuseOrPut(cacheKey, index, newNode)
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
        blocks = parsedBlocks
    }

    if (blocks.isEmpty() && text.isNotBlank()) {
        // 解析中或解析失败，fallback 到纯文本
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    } else {
        blocks.forEachIndexed { index, block ->
            val isLast = index == blocks.lastIndex
            RichText(modifier = Modifier.fillMaxWidth()) {
                BasicMarkdown(
                    astNode = block,
                    showCursor = isLast && isStreaming,
                )
            }
        }
    }
}

// ── 完成态：打字机步进动画 → Markdown 渲染（同步路径） ─────────────────

@Composable
private fun TypewriterMarkdownBlock(
    targetText: String,
    engineInfo: String,
    modifier: Modifier = Modifier,
) {
    // displayedLength 追踪当前已"打"出多少字
    var displayedLength by remember { mutableIntStateOf(0) }
    val isFullyTyped by remember(displayedLength) {
        derivedStateOf { displayedLength >= targetText.length }
    }

    // 打字机步进逻辑
    LaunchedEffect(targetText) {
        displayedLength = 0
        while (displayedLength < targetText.length) {
            val step = ((targetText.length - displayedLength) / 10).coerceIn(1, 5)
            displayedLength = (displayedLength + step).coerceAtMost(targetText.length)
            delay(20L)
        }
    }

    // 打字期间光标
    val infiniteTransition = rememberInfiniteTransition(label = "typeCursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "type_cursor_blink",
    )

    val scrollState = rememberScrollState()
    LaunchedEffect(displayedLength) {
        if (!isFullyTyped) scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (!isFullyTyped) {
            // 正在打字 → 纯文本 + inline 闪烁光标
            Text(
                text = buildAnnotatedString {
                    append(targetText.take(displayedLength))
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = cursorAlpha),
                        )
                    ) {
                        append("▎")
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        } else {
            // 打字完成 → 分块渲染 Markdown（无 cursor）
            MarkdownBlocksContent(
                text = targetText,
                isStreaming = false,
            )
        }

        // 引擎信息标签（生成完成后展示）
        if (isFullyTyped && engineInfo.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = engineInfo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            )
        }
    }
}

// ── 错误态：错误信息 + 重试按钮 ─────────────────────────────────────

@Composable
private fun StreamAnswerErrorBlock(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.ai_error),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleMedium,
        )
        if (error.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        FilledTonalButton(
            onClick = onRetry,
            colors = AppTheme.colors.filledTonalButtonColors(),
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = stringResource(R.string.ai_stream_answer_retry))
        }
    }
}
