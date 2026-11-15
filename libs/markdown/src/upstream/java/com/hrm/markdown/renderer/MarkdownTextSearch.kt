package com.hrm.markdown.renderer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.graphics.Color
import com.hrm.markdown.renderer.internal.selection.MarkdownSelectionController
import com.hrm.markdown.renderer.internal.selection.SelectionActiveHandle
import com.hrm.markdown.renderer.internal.selection.SelectionAnchor
import com.hrm.markdown.renderer.internal.selection.SelectionModelIndex
import com.hrm.markdown.renderer.internal.selection.SelectionRange
import com.hrm.markdown.renderer.internal.selection.clampToCharBoundary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.job

/**
 * 正文中的一次字面匹配。偏移为渲染器逻辑块内的 UTF-16 半开区间，不是 Markdown/HTML 源码下标。
 * 仅可交回产生它的 [MarkdownTextSearchState]；文档替换或修订后旧结果失效。
 */
@Immutable
@ConsistentCopyVisibility
data class MarkdownTextSearchMatch internal constructor(
    val documentRevision: Long,
    val blockStableId: Long,
    val startInBlock: Int,
    val endInBlock: Int,
    val excerpt: String,
    internal val documentToken: Any,
    internal val geometryStableId: Long = blockStableId,
    internal val geometryStart: Int = 0,
) {
    internal val logicalRange: SelectionRange
        get() = SelectionRange(
            SelectionAnchor(geometryStableId, startInBlock - geometryStart),
            SelectionAnchor(geometryStableId, endInBlock - geometryStart),
        )
}

/**
 * 一个 Markdown 实例的文本搜索状态，须在 UI 线程使用。不修改选区，也不触发菜单或手柄。
 * 搜索复用渲染器的可选文本模型，支持段落、标题、嵌套正文及表格单元格的真实文本。
 * 不索引代码块（CodeHigh 尚未公开文本布局）、图片/公式/行内 widget 的替代文本；
 * 不跨块、表格单元格或 widget 拼接，不读取图片或公式的源码作为可见文字。
 * 查询更新不重新解析文档。
 */
@Stable
class MarkdownTextSearchState internal constructor() {
    var query: String by mutableStateOf("")
        private set
    var matches: List<MarkdownTextSearchMatch> by mutableStateOf(emptyList())
        private set
    var activeMatch: MarkdownTextSearchMatch? by mutableStateOf(null)
        private set
    var highlightActiveMatch: Boolean by mutableStateOf(true)
    var highlightColor: Color by mutableStateOf(Color.Yellow.copy(alpha = 0.3f))

    /** 是否绘制所有匹配的背景；当前匹配启用高亮时只使用 [highlightColor]，不叠色。 */
    var highlightAllMatches: Boolean by mutableStateOf(false)
    var allMatchHighlightColor: Color by mutableStateOf(Color.Yellow.copy(alpha = 0.15f))

    // 按实际几何节点分组，避免每个可见块在每帧遍历整篇结果；不测量或保留离屏块。
    internal val matchesByGeometry: Map<Long, List<MarkdownTextSearchMatch>> by derivedStateOf {
        if (highlightAllMatches) matches.groupBy { it.geometryStableId } else emptyMap()
    }

    private var ignoreCase = true
    private var controller: MarkdownSelectionController? = null
    private var markdownState: MarkdownState? = null
    private var documentKey: Any? = null
    private var documentToken: Any = Any()
    private var documentRevision = 0L
    private var pendingReveal: Deferred<Boolean>? = null
    internal var relocation: MarkdownTextSearchRelocation? by mutableStateOf(null)
        private set
    internal val selectionInProgress: Boolean
        get() = controller?.state?.activeHandle?.let { it != SelectionActiveHandle.None } == true

    /** Unicode 空白按 [Char.isWhitespace] 折叠并去首尾；大小写匹配遵循 Kotlin 的 ignoreCase。 */
    fun setQuery(query: String, ignoreCase: Boolean = true) {
        if (this.query == query && this.ignoreCase == ignoreCase) return
        pendingReveal?.cancel()
        relocation = null
        documentToken = Any()
        activeMatch = null
        this.query = query
        this.ignoreCase = ignoreCase
        matches = controller?.index?.findTextMatches(query, ignoreCase, documentRevision, documentToken).orEmpty()
    }

    /** 关闭搜索：清除查询、结果、高亮，并取消尚未完成的定位。 */
    fun clear() {
        pendingReveal?.cancel()
        relocation = null
        documentToken = Any()
        query = ""
        matches = emptyList()
        activeMatch = null
    }

    /**
     * 把匹配的首个实际文本行交给 Compose BringIntoViewRequester，由父滚动容器负责滚动。
     * 支持外部 ScrollState + Markdown(enableScroll = false)，无需调用方计算坐标。
     * 不会为定位切换渲染模式或创建额外 Markdown 实例。
     *
     * 内部 lazy 滚动先通过 [MarkdownState.scrollToBlock] 组合目标块，再定位匹配行。
     * 过期结果、隐藏 tab 等仍未组合的内容及无实际文字几何的内容返回 false。
     * 新查询、文档替换、离开组合或更新的定位请求会取消旧请求并返回 false；调用协程取消仍向上传播。
     * 每次调用都会重新定位，即使 [activeMatch] 没变。
     */
    suspend fun reveal(match: MarkdownTextSearchMatch): Boolean = coroutineScope {
        val target = controller ?: return@coroutineScope false
        if (match.documentToken !== documentToken || match !in matches || selectionInProgress) return@coroutineScope false
        pendingReveal?.cancel()
        relocation = null
        val request = async(start = CoroutineStart.UNDISPATCHED) {
            var centerAfterMaterialization = false
            if (target.registry.coordinatesOf(match.geometryStableId) == null) {
                val entry = target.index.entryOf(match.blockStableId) ?: return@async false
                val navigation = markdownState
                val lazyLayout = navigation?.lazyListState?.layoutInfo
                val itemIndex = entry.topLevelIndex + (navigation?.headerItems ?: 0)
                val itemVisible = lazyLayout?.visibleItemsInfo?.any { it.index == itemIndex } == true
                centerAfterMaterialization = lazyLayout != null && lazyLayout.totalItemsCount > 0 && !itemVisible
                if (!itemVisible) navigation?.scrollToBlock(entry.topLevelIndex)
                withFrameNanos { }
            }
            if (controller !== target || match.documentToken !== documentToken || match !in matches || selectionInProgress) {
                return@async false
            }
            val requester = target.registry.requesterOf(match.geometryStableId) ?: return@async false
            val rect = target.highlightBoxesFor(match.geometryStableId, match.logicalRange, textOnly = true)
                .firstOrNull { it.width > 0f && it.height > 0f } ?: return@async false
            val relocating = MarkdownTextSearchRelocation(currentCoroutineContext().job, centerAfterMaterialization)
            relocation = relocating
            try {
                requester.bringIntoView(rect)
            } finally {
                if (relocation === relocating) relocation = null
            }
            if (controller !== target || match.documentToken !== documentToken || match !in matches) {
                return@async false
            }
            activeMatch = match
            true
        }
        pendingReveal = request
        try {
            request.await()
        } catch (cancelled: CancellationException) {
            currentCoroutineContext().ensureActive()
            false
        } finally {
            if (pendingReveal === request) pendingReveal = null
        }
    }

    internal fun bind(
        controller: MarkdownSelectionController,
        documentKey: Any,
        revision: Long,
        markdownState: MarkdownState,
    ) {
        check(this.controller == null || this.controller === controller) {
            "A MarkdownTextSearchState can only be attached to one Markdown instance"
        }
        this.markdownState = markdownState
        if (this.controller === controller && this.documentKey === documentKey) return
        pendingReveal?.cancel()
        relocation = null
        this.controller = controller
        this.documentKey = documentKey
        documentRevision = revision
        documentToken = Any()
        activeMatch = null
        matches = controller.index.findTextMatches(query, ignoreCase, revision, documentToken)
    }

    internal fun unbind(controller: MarkdownSelectionController) {
        if (this.controller !== controller) return
        pendingReveal?.cancel()
        relocation = null
        this.controller = null
        markdownState = null
        documentKey = null
        documentToken = Any()
        matches = emptyList()
        activeMatch = null
    }
}

@Composable
fun rememberMarkdownTextSearchState(): MarkdownTextSearchState = remember { MarkdownTextSearchState() }

internal val LocalMarkdownTextSearchState = staticCompositionLocalOf<MarkdownTextSearchState?> { null }

/** 在已有逻辑索引上匹配，归一化映射仅用于本次查询，不建立另一份文档索引。 */
internal fun SelectionModelIndex.findTextMatches(
    query: String,
    ignoreCase: Boolean,
    revision: Long,
    documentToken: Any,
): List<MarkdownTextSearchMatch> {
    val needle = normalizeSearchText(query).text
    if (needle.isEmpty()) return emptyList()
    return buildList {
        for (entry in entries) {
            if (entry.searchableRanges.isEmpty()) continue
            val normalized = normalizeSearchText(entry.text)
            var cursor = 0
            var rangeCursor = 0
            while (cursor <= normalized.text.length - needle.length) {
                val at = normalized.text.indexOf(needle, cursor, ignoreCase)
                if (at < 0) break
                val start = normalized.starts[at]
                val end = normalized.ends[at + needle.length - 1]
                var range = entry.searchableRanges.getOrNull(rangeCursor)
                while (range != null && range.chars.last < start) {
                    range = entry.searchableRanges.getOrNull(++rangeCursor)
                }
                if (range != null && (start < range.chars.first || end > range.chars.last + 1)) range = null
                // A rejected widget-boundary candidate must not hide a later overlapping text match.
                val accepted = range != null && clampToCharBoundary(entry.text, start) == start &&
                    clampToCharBoundary(entry.text, end) == end
                if (accepted) {
                    val excerptStart = clampToCharBoundary(entry.text, (start - 40).coerceAtLeast(0))
                    val excerptEnd = clampToCharBoundary(entry.text, (end + 40).coerceAtMost(entry.totalChars))
                    add(MarkdownTextSearchMatch(
                        documentRevision = revision,
                        blockStableId = entry.stableId,
                        startInBlock = start,
                        endInBlock = end,
                        excerpt = (if (excerptStart > 0) "…" else "") +
                            normalizeSearchText(entry.text.substring(excerptStart, excerptEnd)).text +
                            (if (excerptEnd < entry.totalChars) "…" else ""),
                        documentToken = documentToken,
                        geometryStableId = range.geometryStableId,
                        geometryStart = range.geometryStart,
                    ))
                }
                cursor = at + if (accepted) needle.length else 1
            }
        }
    }
}

private class NormalizedSearchText(val text: String, val starts: IntArray, val ends: IntArray)

private fun normalizeSearchText(source: String): NormalizedSearchText {
    val starts = IntArray(source.length)
    val ends = IntArray(source.length)
    val text = StringBuilder(source.length)
    var cursor = 0
    while (cursor < source.length) {
        val start = cursor
        val char = source[cursor++]
        if (char.isWhitespace()) {
            while (cursor < source.length && source[cursor].isWhitespace()) cursor++
            if (text.isEmpty() || cursor == source.length) continue
        }
        starts[text.length] = start
        ends[text.length] = cursor
        text.append(if (char.isWhitespace()) ' ' else char)
    }
    return NormalizedSearchText(text.toString(), starts, ends)
}
