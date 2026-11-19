package com.wanbaohe.markdown

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import com.hrm.markdown.parser.MarkdownParserConfig
import com.hrm.markdown.parser.ast.BlankLine
import com.hrm.markdown.parser.ast.Node
import com.hrm.markdown.renderer.MarkdownLayout

/**
 * 长 Markdown 内容的一次分块结果。
 *
 * 每一个分块都可以独立交给 [OneBoxMarkdown] 渲染：分块边界永远落在**顶层 Markdown 块**
 * （段落 / 标题 / 代码块 / 表格 / 列表 / 公式 / 引用 / 分隔线）之间，不会把围栏代码块、
 * 表格、列表或公式切开，因此单独渲染时语义与整篇渲染一致。
 *
 * 设计意图：聊天列表是 LazyColumn，一条消息原本是一个 item。超长回答（几千字 + 多个代码块 +
 * 公式）会在进入会话的那一帧把整篇内容一次性组合、测量、绘制，实测能卡住 1 秒以上。
 * 按顶层块切成多个 item 交给外层 LazyColumn 虚拟化后，进入会话只渲染可见的 1~2 块，
 * 滚动到哪一块才渲染哪一块，同时仍然是"全文直接展示"，不需要折叠/展开。
 *
 * 已知取舍：每个分块都是一篇独立的 Markdown 文档，**文字选区不能跨分块**（普通长度回答
 * 不受影响）；聊天动作栏的"复制全文"复制的仍是整条回答，可作为兜底。
 *
 * @property index 分块序号，0 表示文档开头，`index == 0` 的块负责补文档开头的间距
 * @property source 该分块的 Markdown 源码（原文档的子串，未做任何改写）
 * @property startOffset 该分块在原始文档中的起始字符偏移（含）
 * @property endOffset 该分块在原始文档中的结束字符偏移（不含）
 * @property blockCount 该分块包含的顶层 Markdown 块数量，便于日志/调试与后续按块做更细策略
 */
@Immutable
data class OneBoxMarkdownChunk(
    val index: Int,
    val source: String,
    val startOffset: Int,
    val endOffset: Int,
    val blockCount: Int,
)

/**
 * 长文分块策略。
 *
 * 数值是"体验 / 成本"的折中，集中放在这里便于按设备档位或场景调整：
 * - 阈值以下不分块：普通回答保持单文档，跨块选区等行为完全不变；
 * - 目标块大小决定 LazyColumn 单 item 的渲染成本，越小进入会话越轻，但 item 越多；
 * - 上限用于兜底极端长文，避免一次生成成百上千个 item。
 *
 * @property minSourceChars 小于该字符数的内容不拆分，整段一次渲染
 * @property targetChunkChars 单个分块的目标字符上限（代码块/表格等不可拆块可能略超）
 * @property minChunkChars 分块的最小字符数，尾块过小时并入前一块，避免出现只装一两行的 item
 * @property maxChunks 最多分块数；超过时自动放宽 [effectiveChunkChars] 而不是丢弃内容
 */
@Immutable
data class OneBoxMarkdownChunkPolicy(
    val minSourceChars: Int = 3_000,
    val targetChunkChars: Int = 1_400,
    val minChunkChars: Int = 400,
    val maxChunks: Int = 64,
) {
    /**
     * 实际生效的单块上限：普通内容用 [targetChunkChars]；极端长文按 [maxChunks] 均摊，
     * 宁可单块大一点，也不要把 item 数撑爆。
     */
    fun effectiveChunkChars(sourceLength: Int): Int {
        if (maxChunks <= 0) return targetChunkChars
        val balanced = (sourceLength + maxChunks - 1) / maxChunks
        return maxOf(targetChunkChars, balanced)
    }

    companion object {
        val Default = OneBoxMarkdownChunkPolicy()
    }
}

/**
 * 分块时补回的分块间距。
 *
 * 单篇 Markdown 内部，块与块之间由引擎的 `MarkdownLayout.blockSpacing` 撑开；拆成多个
 * LazyColumn item 后，item 之间没有这层间距，分块边界看起来会比普通段落更紧。
 * 因此由界面对 `index > 0` 的分块补一个相同的顶部间距，视觉上与未拆分的文档一致。
 */
val OneBoxMarkdownChunkSpacing: Dp = MarkdownLayout().blockSpacing

/**
 * 把长 Markdown 源码切成若干个可独立渲染的块组。
 *
 * 实现要点：
 * 1. **只解析一次**：用新引擎的 [MarkdownParserConfig] 解析整篇，拿到顶层块的 [Node.lineRange]；
 *    块边界来自解析器本身，而不是手写扫描 `\n\n`，因此围栏代码块里的空行、表格、松散列表、
 *    `$$` 公式都不会被切坏。
 * 2. **按行号切片**：`LineRange.endLine` 是开区间，切片严格落在行首，分块可以原样交给
 *    [OneBoxMarkdown] 重新解析；块间的空行归属前一个分块，不影响渲染。
 * 3. **兜底优先于性能**：解析失败、或文档里存在链接引用定义 / 脚注定义 / 缩写定义时，
 *    直接返回整段一个分块——这些定义可能被任意块引用，跨分块后会失效，宁可慢也不能渲染错。
 *
 * 线程注意事项：解析是 CPU 工作（9000 字约几十毫秒），**必须在后台线程调用**；
 * 上层（消息模型构建）本身就在 IO 线程上，可直接使用。
 *
 * @param source 原始 Markdown 文本
 * @param policy 分块策略，默认 [OneBoxMarkdownChunkPolicy.Default]
 * @return 至少一个分块；返回值按文档顺序排列，`index` 从 0 递增
 */
fun splitMarkdownForLazyRendering(
    source: String,
    policy: OneBoxMarkdownChunkPolicy = OneBoxMarkdownChunkPolicy.Default,
): List<OneBoxMarkdownChunk> {
    // 普通回答不分块：保持单文档，选区、脚注等行为与分块前完全一致。
    if (source.length < policy.minSourceChars) {
        return listOf(singleChunk(source))
    }

    val document = runCatching {
        MarkdownParserConfig.Default.newParser().parse(source)
    }.getOrNull() ?: return listOf(singleChunk(source))

    // 引用型定义可以从任意块被引用，跨分块会解析不到定义；这里直接放弃分块。
    if (document.linkDefinitions.isNotEmpty() ||
        document.footnoteDefinitions.isNotEmpty() ||
        document.abbreviationDefinitions.isNotEmpty()
    ) {
        return listOf(singleChunk(source))
    }

    val lineStarts = computeLineStarts(source)
    val blockRanges = document.children
        // 空白行不是可渲染块，切出来只会制造空 item；渲染器同样会过滤它。
        .filterNot { it is BlankLine }
        .mapNotNull { it.toCharRange(lineStarts, source.length) }
        .toList()
    // 只有一个顶层块（例如一整段代码或一个巨大表格）时没有安全切点，保持整段。
    if (blockRanges.size <= 1) {
        return listOf(singleChunk(source))
    }

    val chunkChars = policy.effectiveChunkChars(source.length)
    val groups = groupBlocks(blockRanges, chunkChars)
    val mergedGroups = mergeTinyTail(groups, blockRanges, policy.minChunkChars, source.length)

    return buildChunks(source, blockRanges, mergedGroups)
}

/**
 * 单分块的快速路径：偏移覆盖整篇，`blockCount` 用 1 占位（未解析时无意义，仅用于调试展示）。
 */
private fun singleChunk(source: String) = OneBoxMarkdownChunk(
    index = 0,
    source = source,
    startOffset = 0,
    endOffset = source.length,
    blockCount = 1,
)

/**
 * 预计算每一行的起始字符偏移，下标即行号（0 起）。
 *
 * 与解析器的 `LineRange` 行号一一对应；最后一行没有结尾换行时也能覆盖到。
 */
private fun computeLineStarts(source: String): IntArray {
    val starts = ArrayList<Int>(source.length / 32 + 1)
    starts.add(0)
    for (index in source.indices) {
        if (source[index] == '\n' && index + 1 <= source.length) {
            starts.add(index + 1)
        }
    }
    return starts.toIntArray()
}

/**
 * 顶层块的行号范围 → 字符范围（半开区间）。
 *
 * `LineRange` 缺失或范围非法（空块、越界）时跳过，避免切出非法切片。
 */
private fun Node.toCharRange(lineStarts: IntArray, sourceLength: Int): IntRange? {
    val lines = lineRange ?: return null
    if (lines.startLine !in lineStarts.indices) return null
    val start = lineStarts[lines.startLine]
    // endLine 是开区间；等于总行数时表示一直到文末。
    val end = lineStarts.getOrNull(lines.endLine) ?: sourceLength
    return if (end > start) start until end else null
}

/**
 * 把顶层块按字符预算分组：每组尽量接近但不超过 [chunkChars]。
 *
 * 规则：至少一块一组；单块本身超预算时独占一组（代码块/表格无法再拆）。
 * 返回每组在 [blockRanges] 中的下标区间。
 */
private fun groupBlocks(blockRanges: List<IntRange>, chunkChars: Int): List<IntRange> {
    val groups = ArrayList<IntRange>()
    var groupStartIndex = 0
    var groupStartOffset = blockRanges[0].first
    for (index in blockRanges.indices) {
        val end = blockRanges[index].last + 1
        val exceedsBudget = index > groupStartIndex && end - groupStartOffset > chunkChars
        if (exceedsBudget) {
            // 当前块装不下：结束上一组，从当前块开始新的一组。
            groups.add(groupStartIndex until index)
            groupStartIndex = index
            groupStartOffset = blockRanges[index].first
        }
        if (index == blockRanges.lastIndex) {
            groups.add(groupStartIndex..index)
        }
    }
    return groups
}

/**
 * 尾块过小时并入前一组：避免最后一个 LazyColumn item 只有一两行，滚动时反而更碎。
 *
 * 合并后可能略超字符预算，这里以"少一个碎 item"优先。
 */
private fun mergeTinyTail(
    groups: List<IntRange>,
    blockRanges: List<IntRange>,
    minChunkChars: Int,
    sourceLength: Int,
): List<IntRange> {
    if (groups.size <= 1) return groups
    val last = groups.last()
    val lastStart = blockRanges[last.first].first
    val lastLength = sourceLength - lastStart
    if (lastLength >= minChunkChars) return groups

    val previous = groups[groups.size - 2]
    val merged = previous.first..last.last
    return groups.dropLast(2) + listOf(merged)
}

/**
 * 依据分组结果切片。
 *
 * - 第一块从 0 开始，保留文首的空行；
 * - 非首块从自己的第一个顶层块行首开始，不把上一块的结尾空行带进来；
 * - 每块结束于下一块起点，块间空行归属前一块（重新解析时是空行，不影响渲染）；
 * - 最后一块一直切到文末，保留结尾巴空行。
 */
private fun buildChunks(
    source: String,
    blockRanges: List<IntRange>,
    groups: List<IntRange>,
): List<OneBoxMarkdownChunk> {
    return groups.mapIndexed { index, group ->
        val start = if (index == 0) 0 else blockRanges[group.first].first
        val end = groups.getOrNull(index + 1)?.let { blockRanges[it.first].first } ?: source.length
        OneBoxMarkdownChunk(
            index = index,
            source = source.substring(start, end),
            startOffset = start,
            endOffset = end,
            blockCount = group.last - group.first + 1,
        )
    }
}
