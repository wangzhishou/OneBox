package com.wanbaohe.base.ui

import android.os.SystemClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.commonmark.CommonMarkdownParseOptions
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.markwon.MarkdownAstNodeParser
import com.halilibo.richtext.ui.material3.RichText
import com.shifenmiao.model.node.AstFencedCodeBlock
import com.shifenmiao.model.node.AstHeading
import com.shifenmiao.model.node.AstHtmlBlock
import com.shifenmiao.model.node.AstHtmlInline
import com.shifenmiao.model.node.AstIndentedCodeBlock
import com.shifenmiao.model.node.AstJLatexBlockMath
import com.shifenmiao.model.node.AstJLatexNodeMath
import com.shifenmiao.model.node.AstNode
import com.shifenmiao.model.node.AstText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * 流式友好的 Markdown 渲染组件(AI 解读类场景通用:易经/诗词/八字等)。
 *
 * 直接渲染 Markdown,流式期间也不退化为纯文本。性能方案与 AI 助手聊天页一致
 * (见 feature/ai 的 BlockReuseCache + StreamContentProcessor):
 *
 * 1. 解析在 [Dispatchers.Default],渲染按内容长度分级节流(48~180ms,与
 *    feature/ai 的 StreamContentProcessor 分段一致)——delta 密集到达时中间版本
 *    自然合并,UI 以稳定帧率推进;打字机的节奏感正来自这个稳定刷新节奏,
 *    而不是逐字 delay(feature/ai 注释:逐字 delay 会串行卡住整条 SSE 流);
 * 2. 整段解析后拆成顶层 block,按"块内容签名"与上一版对齐比对,未变的块
 *    **复用旧 AstNode 引用**。下游 `BasicMarkdown`/`RichText` 内部的
 *    `remember(astNode)` 因此真正命中,逐 delta 增长时只有最后一个块重组,
 *    避免整篇在主线程重算(不这样做的话 AstNode 引用比较永远不等,长文会卡);
 * 3. 首版 AST 未就绪时短暂回退纯文本,避免空白帧。
 *
 * 非流式静态内容同样走该管线(内容不变则只解析一次),调用方无需区分状态。
 */
@Composable
fun StreamingMarkdownContent(
    content: String,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    blockSpacing: Dp = 8.dp,
) {
    val context = LocalContext.current
    val parser = remember { MarkdownAstNodeParser(context, CommonMarkdownParseOptions.Default) }
    var blocks by remember { mutableStateOf<List<AstNode>>(emptyList()) }
    // 上一版块的 (签名, 节点),用于跨版本复用未变块的 AstNode 引用
    val reuseCache = remember { mutableListOf<Pair<String, AstNode>>() }
    // content 是普通 String 参数,必须经 rememberUpdatedState 转成快照状态,
    // 否则 snapshotFlow 只会读到首次组合时的值,流式后续 delta 永远不触发重解析
    val currentContent by rememberUpdatedState(content)

    LaunchedEffect(parser) {
        var lastRendered: String? = null
        var lastRenderAt = 0L
        // 渲染节流:不逐 delta 解析,按固定节奏取"最新快照"渲染。collect 挂起期间
        // snapshotFlow 的中间版本自然合并,唤醒后读到的就是最新内容
        snapshotFlow { currentContent }.collect {
            val interval = renderIntervalMs(currentContent.length)
            val elapsed = SystemClock.uptimeMillis() - lastRenderAt
            if (elapsed < interval) delay(interval - elapsed)
            val text = currentContent
            if (text == lastRendered) return@collect
            lastRenderAt = SystemClock.uptimeMillis()
            if (text.isBlank()) {
                reuseCache.clear()
                lastRendered = text
                blocks = emptyList()
                return@collect
            }
            val parsed = withContext(Dispatchers.Default) {
                runCatching {
                    val root = parser.parse(text)
                    val topLevel = mutableListOf<AstNode>()
                    var child = root.links.firstChild
                    while (child != null) {
                        topLevel.add(child)
                        child = child.links.next
                    }
                    topLevel
                }.getOrNull()
            }
            if (parsed == null) {
                lastRendered = null
                return@collect
            }
            lastRendered = text

            // 按 index 对齐做签名复用;新版本块数更少时截断旧缓存
            val reused = parsed.mapIndexed { index, node ->
                val signature = node.contentSignature()
                val cached = reuseCache.getOrNull(index)
                if (cached != null && cached.first == signature) cached.second
                else node.also { reuseCache.setOrAdd(index, signature to it) }
            }
            while (reuseCache.size > reused.size) reuseCache.removeAt(reuseCache.size - 1)
            blocks = reused
        }
    }

    if (blocks.isEmpty()) {
        // AST 未就绪(首帧/解析失败)时纯文本兜底,避免空白
        Text(
            text = content,
            style = textStyle,
            lineHeight = 24.sp,
            color = contentColor,
            modifier = modifier.fillMaxWidth(),
        )
    } else {
        Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(blockSpacing)) {
            blocks.forEachIndexed { index, node ->
                // node 引用在内容未变时跨重组保持稳定,RichText 内部 remember 命中跳过重组
                androidx.compose.runtime.key(index) {
                    RichText(contentColor = contentColor, textStyle = textStyle) {
                        BasicMarkdown(astNode = node)
                    }
                }
            }
        }
    }
}

private fun MutableList<Pair<String, AstNode>>.setOrAdd(index: Int, slot: Pair<String, AstNode>) {
    if (index < size) this[index] = slot else add(slot)
}

/** 内容越长渲染间隔越大,分段与 feature/ai 的 StreamContentProcessor 一致 */
private fun renderIntervalMs(length: Int): Long = when {
    length < 1_000 -> 48L
    length < 3_000 -> 84L
    length < 8_000 -> 120L
    else -> 180L
}

/**
 * 块字面内容的稳定签名:只收集实际渲染的可见文本(文本/代码块/literal/latex/标题级别),
 * 忽略引用类字段(links/parent),使"内容未变的新解析树"与旧树签名一致。
 * 与 feature/ai 的 BlockReuseCache.computeSignature 同源。
 */
private fun AstNode.contentSignature(): String {
    val sb = StringBuilder(64)
    appendSignature(sb)
    return sb.toString()
}

private fun AstNode.appendSignature(sb: StringBuilder) {
    when (val t = type) {
        is AstText -> sb.append("|t:").append(t.literal)
        is AstFencedCodeBlock -> sb.append("|fc:").append(t.info).append(":").append(t.literal)
        is AstIndentedCodeBlock -> sb.append("|ic:").append(t.literal)
        is AstHtmlBlock -> sb.append("|hb:").append(t.literal)
        is AstHtmlInline -> sb.append("|hi:").append(t.literal)
        is AstJLatexBlockMath -> sb.append("|lb:").append(t.latex.orEmpty())
        is AstJLatexNodeMath -> sb.append("|li:").append(t.latex.orEmpty())
        is AstHeading -> sb.append("|h:").append(t.level)
        else -> sb.append("|n:").append(t::class.java.simpleName)
    }
    var child = links.firstChild
    sb.append('[')
    while (child != null) {
        child.appendSignature(sb)
        child = child.links.next
    }
    sb.append(']')
}
