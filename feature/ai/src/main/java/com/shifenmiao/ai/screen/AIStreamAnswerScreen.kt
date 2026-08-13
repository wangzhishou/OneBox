package com.shifenmiao.ai.screen

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.node.AstNode
import com.shifenmiao.theme.AppTheme
import com.shifenmiao.webview.mermaid.ProvideMermaidRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen

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

    // 生成中（LOADING/STREAMING）时，退出/跳历史/重新生成都先弹确认
    val isBusy = status == AIStreamAnswerStatus.LOADING || status == AIStreamAnswerStatus.STREAMING
    var showInterruptDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun runWithInterruptConfirm(action: () -> Unit) {
        if (isBusy) {
            pendingAction = action
            showInterruptDialog = true
        } else {
            action()
        }
    }

    // 系统返回手势/按键在生成中同样拦截
    BackHandler(enabled = isBusy) {
        runWithInterruptConfirm {
            component.cancelAnswer()
            appComponent.onGoBack()
        }
    }

    BaseScreen(
        title = component.displayTitle,
        onGoBack = {
            runWithInterruptConfirm {
                component.cancelAnswer()
                appComponent.onGoBack()
            }
        },
        actions = {
            IconButton(
                onClick = { runWithInterruptConfirm { component.retry() } }
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                    contentDescription = stringResource(R.string.ai_stream_answer_regenerate),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = {
                    // 跳历史不中断生成（组件在返回栈中继续跑），只需提示
                    runWithInterruptConfirm {
                        appComponent.onNavigate(
                            Screen.AIHistoryCenter(
                                initialFilter = AIConversationEntryType.STREAM_QA
                            )
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                    contentDescription = stringResource(R.string.history),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
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

    if (showInterruptDialog) {
        AlertDialog(
            onDismissRequest = { showInterruptDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showInterruptDialog = false
                        pendingAction?.invoke()
                        pendingAction = null
                    }
                ) {
                    Text(stringResource(R.string.ai_stream_answer_interrupt_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showInterruptDialog = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            },
            title = { Text(stringResource(R.string.ai_stream_answer_interrupt_title)) },
            text = { Text(stringResource(R.string.ai_stream_answer_interrupt_message)) },
        )
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
        // 按总时长约 1s 反推步长：长内容不再逐字慢跑，短内容仍保留打字感
        val step = (targetText.length / 60).coerceIn(2, 48)
        while (displayedLength < targetText.length) {
            displayedLength = (displayedLength + step).coerceAtMost(targetText.length)
            delay(16L)
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
