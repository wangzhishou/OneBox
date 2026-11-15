package com.hrm.markdown.renderer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hrm.markdown.parser.MarkdownParserConfig
import com.hrm.markdown.parser.ast.Document
import com.hrm.markdown.runtime.MarkdownDirectivePipeline
import com.hrm.markdown.runtime.MarkdownSourceMap
import com.hrm.markdown.runtime.MarkdownTransformResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * 流式文档解析状态。
 * - 流式模式：增量追加解析，避免闪烁
 * - 非流式模式：全量异步解析
 */
@Composable
internal fun rememberStreamingDocument(
    markdown: String,
    isStreaming: Boolean,
    parserConfig: MarkdownParserConfig = MarkdownParserConfig.Default,
    runtimePipeline: MarkdownDirectivePipeline = MarkdownDirectivePipeline.Empty,
): StreamingMarkdownSnapshot? {
    val parser = remember(parserConfig) { parserConfig.newParser() }
    var snapshotState by remember(parser, runtimePipeline) {
        mutableStateOf(StreamingMarkdownState())
    }
    val transformMutex = remember(runtimePipeline) { Mutex() }

    LaunchedEffect(markdown, isStreaming, parser, runtimePipeline) {
        val transformed = if (runtimePipeline.hasTransformers) {
            withContext(Dispatchers.Default) {
                transformMutex.withLock { runtimePipeline.transform(markdown) }
            }
        } else {
            MarkdownTransformResult(markdown)
        }
        val documentState = updateStreamingDocumentState(
            markdown = transformed.markdown,
            isStreaming = isStreaming,
            state = snapshotState.documentState,
            beginStream = parser::beginStream,
            append = parser::append,
            endStream = parser::endStream,
            parse = { value ->
                withContext(Dispatchers.Default) {
                    // 每次全量解析使用独立解析器。取消后同步解析可能仍会运行，
                    // 但不会再与持有状态的流式解析器争用同一会话。
                    parserConfig.newParser().parse(value)
                }
            }
        )
        // 文档与源码映射对应同一输入版本，必须同时发布；分别写入 Compose 状态会短暂混用版本。
        snapshotState = StreamingMarkdownState(documentState, transformed.sourceMap)
    }

    return snapshotState.documentState.document?.let { document ->
        StreamingMarkdownSnapshot(document, snapshotState.sourceMap)
    }
}

private data class StreamingMarkdownState(
    val documentState: StreamingDocumentState<Document> = StreamingDocumentState(),
    val sourceMap: MarkdownSourceMap = MarkdownSourceMap.Identity,
)

internal data class StreamingMarkdownSnapshot(
    val document: Document,
    val sourceMap: MarkdownSourceMap,
)

internal data class StreamingDocumentState<T>(
    /** 已提交给流式解析器的 Markdown，用于验证后续输入是否仅在尾部追加。 */
    val streamedMarkdown: String = "",
    val document: T? = null,
    val wasStreaming: Boolean = false,
    /** null 表示尚未解析过非流式版本；空 Markdown 也是有效版本。 */
    val lastNonStreamingMarkdown: String? = null,
)

internal suspend fun <T> updateStreamingDocumentState(
    markdown: String,
    isStreaming: Boolean,
    state: StreamingDocumentState<T>,
    beginStream: () -> Unit,
    append: (String) -> T,
    endStream: () -> T,
    parse: suspend (String) -> T?,
): StreamingDocumentState<T> {
    var nextState = state

    if (isStreaming && !nextState.wasStreaming) {
        beginStream()
        nextState = nextState.copy(
            streamedMarkdown = "",
            document = null,
            wasStreaming = true,
        )
    }

    if (isStreaming) {
        if (!markdown.startsWith(nextState.streamedMarkdown)) {
            // 重试、清空或替换并非纯追加；原会话已不可复用，需要整体重启。
            beginStream()
            nextState = nextState.copy(
                streamedMarkdown = "",
                document = null,
                wasStreaming = true,
            )
        }
        val chunk = markdown.substring(nextState.streamedMarkdown.length)
        if (chunk.isNotEmpty()) {
            nextState = nextState.copy(document = append(chunk))
        }
        return nextState.copy(
            streamedMarkdown = markdown,
            wasStreaming = true,
        )
    }

    if (nextState.wasStreaming) {
        if (!markdown.startsWith(nextState.streamedMarkdown)) {
            // 流结束时上游可能已经用重试结果替换全文；直接全量解析该最终真相。
            return nextState.copy(
                streamedMarkdown = "",
                document = parse(markdown),
                wasStreaming = false,
                lastNonStreamingMarkdown = markdown,
            )
        }
        val chunk = markdown.substring(nextState.streamedMarkdown.length)
        if (chunk.isNotEmpty()) {
            nextState = nextState.copy(document = append(chunk))
        }
        return nextState.copy(
            streamedMarkdown = "",
            document = endStream(),
            wasStreaming = false,
            lastNonStreamingMarkdown = markdown,
        )
    }

    if (markdown == nextState.lastNonStreamingMarkdown) {
        return nextState.copy(wasStreaming = false)
    }

    return nextState.copy(
        streamedMarkdown = "",
        document = parse(markdown),
        wasStreaming = false,
        lastNonStreamingMarkdown = markdown,
    )
}
