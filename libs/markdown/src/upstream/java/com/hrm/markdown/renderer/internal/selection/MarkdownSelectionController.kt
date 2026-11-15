package com.hrm.markdown.renderer.internal.selection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Constraints
import com.hrm.markdown.renderer.MarkdownSelectionBlockRange
import com.hrm.markdown.renderer.MarkdownSelectionInfo
import com.hrm.markdown.renderer.internal.core.model.CodeBlockModel
import com.hrm.markdown.renderer.internal.core.model.InternalRenderBlockModel
import com.hrm.markdown.renderer.internal.layout.inline.LayoutInlineRunPlacement
import com.hrm.markdown.renderer.internal.layout.inline.runPlacements
import com.hrm.markdown.renderer.internal.layout.inline.textMeasurementStyle
import com.hrm.markdown.renderer.internal.layout.model.InternalLayoutBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutColumnsBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutDefinitionDescriptionGroup
import com.hrm.markdown.renderer.internal.layout.model.LayoutDefinitionListBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutFootnoteBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutInlineBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutListBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutRenderBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutTabBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutTableBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutTextRun
import com.hrm.markdown.renderer.internal.layout.model.LayoutWidgetBlockModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

/**
 * 自研选区控制器：聚合选区状态、坐标注册表与当前文档索引，
 * 把 overlay 的 window 坐标转成不依赖屏幕坐标的逻辑锚点 [SelectionAnchor]。
 *
 * 锚点链：window → block-local（[SelectionCoordinateRegistry.windowToBlockLocal]）
 * → 命中 run（[hitTestRunInBlock]）→ run 内字符偏移（[TextMeasurer]）→ +charStart。
 */
@Stable
internal class MarkdownSelectionController(
    private val coroutineScope: CoroutineScope,
    private val textMeasurer: TextMeasurer,
) {
    val state = MarkdownSelectionState()
    val registry = SelectionCoordinateRegistry()

    internal var index: SelectionModelIndex = SelectionModelIndex(emptyList())
        private set
    private var documentBlocks: List<InternalRenderBlockModel>? = null
    private var searchEnabled = false
    private val incrementalIndexBuilder = IncrementalSelectionIndexBuilder()
    private val visibleLayoutBlocks = mutableMapOf<Long, InternalLayoutBlockModel>()
    private val geometryByStableId = mutableMapOf<Long, SelectionBlockGeometry>()
    private val textLayoutsByBlock = mutableMapOf<Long, MutableMap<VisibleTextLayoutKey, VisibleRunTextLayout>>()
    private var clipboard: Clipboard? = null
    private var startAnchor: SelectionAnchor? = null
    private var draggedHandleAnchor: SelectionAnchor? by mutableStateOf(null)
    private var fixedHandleAnchor: SelectionAnchor? = null

    internal var fallbackTextMeasurementCount: Long = 0
        private set

    val hasSelection: Boolean get() = state.hasSelection

    fun bindClipboard(clipboard: Clipboard) {
        this.clipboard = clipboard
    }

    /** Refresh the lightweight whole-document index without laying out offscreen blocks. */
    fun updateDocument(blocks: List<InternalRenderBlockModel>, includeSearch: Boolean = false) {
        if (documentBlocks === blocks && searchEnabled == includeSearch) return
        documentBlocks = blocks
        searchEnabled = includeSearch
        replaceIndex(incrementalIndexBuilder.build(blocks, includeSearch))
    }

    /** Legacy/test entry point for callers that already own an eager layout tree. */
    fun updateIndex(blocks: List<InternalLayoutBlockModel>) {
        replaceIndex(buildSelectionIndexFromLayout(blocks))
        fun registerTree(block: InternalLayoutBlockModel) {
            registerVisibleBlock(block)
            when (block) {
                is LayoutListBlockModel ->
                    block.items.forEach { item -> item.children.forEach(::registerTree) }
                is LayoutColumnsBlockModel ->
                    block.columns.forEach { column -> column.children.forEach(::registerTree) }
                is LayoutTabBlockModel ->
                    block.tabs.forEach { tab -> tab.children.forEach(::registerTree) }
                is LayoutFootnoteBlockModel -> {
                    block.leadChild?.let(::registerTree)
                    block.trailingChildren.forEach(::registerTree)
                }
                is LayoutDefinitionListBlockModel ->
                    block.items.forEach { item ->
                        if (item is LayoutDefinitionDescriptionGroup) {
                            item.children.forEach(::registerTree)
                        }
                    }
                is LayoutRenderBlockModel ->
                    block.children.forEach(::registerTree)
                else -> Unit
            }
        }
        blocks.forEach(::registerTree)
    }

    fun registerVisibleBlock(block: InternalLayoutBlockModel) {
        val stableId = block.identity.stableId
        if (visibleLayoutBlocks[stableId] === block) return
        val previous = visibleLayoutBlocks.put(stableId, block)
        if (previous != null &&
            (previous.identity.contentRevision != block.identity.contentRevision ||
                previous.identity.layoutRevision != block.identity.layoutRevision)
        ) {
            textLayoutsByBlock.remove(stableId)
        }
        rebuildGeometry(stableId)
    }

    fun unregisterVisibleBlock(stableId: Long) {
        visibleLayoutBlocks.remove(stableId)
        geometryByStableId.remove(stableId)
        textLayoutsByBlock.remove(stableId)
    }

    /** Reuse the TextLayoutResult produced by BasicText instead of measuring selected runs again. */
    fun registerTextLayout(
        blockStableId: Long,
        placements: List<LayoutInlineRunPlacement>,
        result: TextLayoutResult,
    ) {
        if (visibleLayoutBlocks[blockStableId] == null) return
        val textPlacements = placements.filter { it.run is LayoutTextRun }
        val first = textPlacements.firstOrNull() ?: return
        val expectedText = buildString {
            textPlacements.forEach { placement ->
                append((placement.run as LayoutTextRun).text.text)
            }
        }
        if (result.layoutInput.text.text != expectedText) return
        val blockCache = textLayoutsByBlock.getOrPut(blockStableId) { mutableMapOf() }
        var textStart = 0
        for (placement in textPlacements) {
            val run = placement.run as LayoutTextRun
            blockCache[run.visibleTextLayoutKey()] = VisibleRunTextLayout(
                result = result,
                textStart = textStart,
                runX = (placement.x - first.x).toFloat(),
                runY = (placement.y - first.y).toFloat(),
            )
            textStart += run.text.length
        }
    }

    private fun replaceIndex(newIndex: SelectionModelIndex) {
        index = newIndex
        geometryByStableId.clear()
        visibleLayoutBlocks.keys.toList().forEach(::rebuildGeometry)
        val current = state.range ?: return
        val start = index.clampAnchor(current.start)
        val end = index.clampAnchor(current.end)
        if (start == null || end == null) {
            clearSelection()
            return
        }
        startAnchor = startAnchor?.let(index::clampAnchor)
        draggedHandleAnchor = draggedHandleAnchor?.let(index::clampAnchor)
        fixedHandleAnchor = fixedHandleAnchor?.let(index::clampAnchor)
        if (state.isHandleDrag && (draggedHandleAnchor == null || fixedHandleAnchor == null)) {
            clearSelection()
            return
        }
        state.range = index.normalize(start, end)
    }

    private fun rebuildGeometry(stableId: Long) {
        val block = visibleLayoutBlocks[stableId] ?: return
        val entry = index.geometryEntryOf(stableId) ?: return
        geometryByStableId[stableId] = buildSelectionGeometry(block, entry)
    }

    /** 返回某 block 内字符区间对应的 run 切片，供高亮绘制使用。 */
    fun runSlicesFor(stableId: Long, range: SelectionRange? = state.range): List<RunCharSlice> {
        if (range == null) return emptyList()
        val entry = index.geometryEntryOf(stableId) ?: return emptyList()
        val geometry = geometryByStableId[stableId] ?: return emptyList()
        val order = entry.order
        if (order < (index.orderOf(range.start.blockStableId) ?: return emptyList())) return emptyList()
        if (order > (index.orderOf(range.end.blockStableId) ?: return emptyList())) return emptyList()

        val from = if (stableId == range.start.blockStableId) range.start.charInBlock else 0
        val to = if (stableId == range.end.blockStableId) range.end.charInBlock else entry.totalChars
        return runRangeForBlock(geometry, entry.totalChars, from, to)
    }

    /**
     * 返回某 block 内选区高亮矩形（block-local 坐标，原点为 block.frame 左上），
     * 坐标公式与 [PaintInlineLayoutContent] 放置一致：`left = run.frame.left - block.frame.left`。
     */
    fun highlightBoxesFor(
        stableId: Long,
        range: SelectionRange? = state.range,
        textOnly: Boolean = false,
    ): List<Rect> {
        if (range == null) return emptyList()
        val entry = index.geometryEntryOf(stableId) ?: return emptyList()
        val geometry = geometryByStableId[stableId] ?: return emptyList()
        val slices = runSlicesFor(stableId, range)
        if (slices.isEmpty()) {
            return if (!textOnly && geometry.runs.isEmpty()) wholeBlockHighlightBox(entry, geometry, range) else emptyList()
        }
        val block = geometry.block as? LayoutInlineBlockModel
            ?: return if (textOnly) emptyList() else wholeBlockHighlightBox(entry, geometry, range)

        val boxes = ArrayList<Rect>(slices.size)
        for (slice in slices) {
            val run = slice.span.run
            if (textOnly && run !is LayoutTextRun) continue
            val runLeft = run.frame.left - block.frame.left
            val runTop = run.frame.top - block.frame.top
            val text = slice.span.text
            val startX: Float
            val endX: Float
            if (run !is LayoutTextRun || text.isEmpty()) {
                startX = 0f
                endX = run.frame.width
            } else {
                val layout = if (textOnly) {
                    // Search must use painted geometry, never the selection fallback measurement.
                    textLayoutsByBlock[stableId]?.get(run.visibleTextLayoutKey())
                } else {
                    textLayoutFor(geometry, slice.span)
                } ?: continue
                val s = slice.startInRun.coerceIn(0, text.length)
                val e = slice.endInRun.coerceIn(0, text.length)
                if (textOnly) {
                    // 搜索使用真实 glyph 区间；双向文本的两端 caret 可能同侧，不能只减两个 x。
                    val result = layout.result
                    val start = layout.textStart + s
                    val end = layout.textStart + e
                    for (line in result.getLineForOffset(start)..result.getLineForOffset(end - 1)) {
                        val from = maxOf(start, result.getLineStart(line))
                        val to = minOf(end, result.getLineEnd(line))
                        if (to <= from) continue
                        val bounds = result.getPathForRange(from, to).getBounds()
                        boxes += bounds.translate(Offset(runLeft - layout.runX, runTop - layout.runY))
                    }
                    continue
                }
                startX = layout.horizontalPosition(s)
                endX = layout.horizontalPosition(e)
            }
            val left = runLeft + minOf(startX, endX)
            val right = runLeft + maxOf(startX, endX)
            boxes += Rect(left, runTop, right, runTop + run.frame.height)
        }
        return boxes
    }

    private fun wholeBlockHighlightBox(
        entry: SelectionBlockEntry,
        geometry: SelectionBlockGeometry,
        range: SelectionRange,
    ): List<Rect> {
        val order = entry.order
        if (order < (index.orderOf(range.start.blockStableId) ?: return emptyList())) return emptyList()
        if (order > (index.orderOf(range.end.blockStableId) ?: return emptyList())) return emptyList()

        val from = if (entry.stableId == range.start.blockStableId) range.start.charInBlock else 0
        val to = if (entry.stableId == range.end.blockStableId) range.end.charInBlock else entry.totalChars
        if (to <= from) return emptyList()

        val actualSize = registry.snapshotOf(entry.stableId)?.size
        val width = actualSize?.width?.toFloat() ?: geometry.block.frame.width
        val height = actualSize?.height?.toFloat() ?: geometry.block.frame.height
        val top = when (geometry.block) {
            is LayoutTableBlockModel ->
                geometry.block.contentFrame.top - geometry.block.frame.top
            is LayoutWidgetBlockModel ->
                if (geometry.block.block is CodeBlockModel)
                    geometry.block.contentFrame.top - geometry.block.frame.top else 0f
            else -> 0f
        }.coerceIn(0f, height)
        return listOf(Rect(0f, top, width, height))
    }

    fun beginSelectionAt(windowOffset: Offset, requireTextHit: Boolean = false): Boolean {
        val anchor = anchorFromWindow(windowOffset, requireTextHit) ?: return false
        startAnchor = anchor
        draggedHandleAnchor = null
        fixedHandleAnchor = null
        state.activeHandle = SelectionActiveHandle.End
        state.isHandleDrag = false
        state.toolbarRequestKey = 0
        state.range = SelectionRange(anchor, anchor)
        return true
    }

    fun extendSelectionTo(windowOffset: Offset) {
        val start = startAnchor ?: return
        val moving = anchorFromWindow(windowOffset) ?: return
        state.range = index.normalize(start, moving)
    }

    fun finishSelectionGesture() {
        state.activeHandle = SelectionActiveHandle.None
        state.isHandleDrag = false
        draggedHandleAnchor = null
        fixedHandleAnchor = null
        // 原地长按只建立了光标锚点。释放时用已绘制文字的词边界展开，不能把它当作空选择清除。
        // 长按期间 hasSelection 已启用真实 TextLayoutResult 注册；未完成回调时复用现有单 run 测量。
        // 只访问命中的可见块，不为选词或复制物化离屏正文。
        state.range?.takeIf { it.start == it.end }?.let { collapsed ->
            val anchor = collapsed.start
            val entry = index.entryOf(anchor.blockStableId) ?: return@let
            if (entry.totalChars == 0) return@let
            val geometry = geometryByStableId[anchor.blockStableId] ?: return@let
            val (span, offset) = geometry.charToRun(
                anchor.charInBlock.coerceAtMost(entry.totalChars - 1), entry.totalChars,
            ) ?: return@let
            val layout = textLayoutFor(geometry, span) ?: return@let
            val text = layout.result.layoutInput.text
            if (text.isEmpty()) return@let
            val word = layout.result.getWordBoundary((layout.textStart + offset).coerceIn(0, text.lastIndex))
            val from = (span.charStart + word.start - layout.textStart).coerceIn(0, entry.totalChars)
            val to = (span.charStart + word.end - layout.textStart).coerceIn(from, entry.totalChars)
            state.range = SelectionRange(
                anchor.copy(charInBlock = clampToCharBoundary(entry.text, from)),
                anchor.copy(charInBlock = clampToCharBoundary(entry.text, to)),
            )
        }
        if (selectedText.isEmpty()) {
            clearSelection()
        } else {
            state.toolbarRequestKey += 1
        }
    }

    /** 把根容器 local 坐标（来自 pointerInput）转成 window 坐标后开始选区。 */
    fun beginSelectionAtRootLocal(rootLocal: Offset, requireTextHit: Boolean = false): Boolean {
        val root = registry.rootCoordinates?.takeIf { it.isAttached } ?: return false
        return beginSelectionAt(root.localToWindow(rootLocal), requireTextHit)
    }

    /** 把根容器 local 坐标（来自 pointerInput）转成 window 坐标后延展选区。 */
    fun extendSelectionToRootLocal(rootLocal: Offset) {
        val root = registry.rootCoordinates?.takeIf { it.isAttached } ?: return
        extendSelectionTo(root.localToWindow(rootLocal))
    }

    fun clearSelection() {
        startAnchor = null
        draggedHandleAnchor = null
        fixedHandleAnchor = null
        state.clear()
    }

    fun selectRange(blockId: Long, start: Int, end: Int): Boolean {
        val entry = index.entryOf(blockId) ?: return false
        if (start < 0 || start >= end || end > entry.text.length) return false
        val first = index.clampAnchor(SelectionAnchor(blockId, start)) ?: return false
        val last = index.clampAnchor(SelectionAnchor(blockId, end)) ?: return false
        val range = index.normalize(first, last)
        if (range.start == range.end) return false
        startAnchor = range.start
        draggedHandleAnchor = null
        fixedHandleAnchor = null
        state.isHandleDrag = false
        state.range = range
        finishSelectionGesture()
        return true
    }

    fun selectAll() {
        val first = index.firstAnchor ?: return
        val last = index.lastAnchor ?: return
        startAnchor = first
        state.range = index.normalize(first, last)
    }

    fun beginSelectionHandleDrag(handle: SelectionActiveHandle) {
        if (handle == SelectionActiveHandle.None) return
        val range = state.range ?: return
        draggedHandleAnchor = when (handle) {
            SelectionActiveHandle.Start -> range.start
            SelectionActiveHandle.End -> range.end
            SelectionActiveHandle.None -> return
        }
        fixedHandleAnchor = when (handle) {
            SelectionActiveHandle.Start -> range.end
            SelectionActiveHandle.End -> range.start
            SelectionActiveHandle.None -> return
        }
        state.activeHandle = handle
        state.isHandleDrag = true
    }

    fun dragSelectionHandleToRootLocal(rootLocal: Offset) {
        if (!state.isHandleDrag) return
        val root = registry.rootCoordinates?.takeIf { it.isAttached } ?: return
        dragSelectionHandleTo(anchorFromWindow(root.localToWindow(rootLocal)) ?: return)
    }

    internal fun dragSelectionHandleTo(anchor: SelectionAnchor) {
        if (!state.isHandleDrag) return
        val fixed = fixedHandleAnchor ?: return
        val moving = index.clampAnchor(anchor) ?: return
        draggedHandleAnchor = moving
        state.range = index.normalize(fixed, moving)
    }

    fun finishSelectionHandleDrag() {
        if (!state.isHandleDrag) return
        state.activeHandle = SelectionActiveHandle.None
        state.isHandleDrag = false
        draggedHandleAnchor = null
        fixedHandleAnchor = null
        if (selectedText.isEmpty()) {
            clearSelection()
        } else {
            state.toolbarRequestKey += 1
        }
    }

    internal fun selectionAnchorForHandle(handle: SelectionActiveHandle): SelectionAnchor? {
        if (handle == SelectionActiveHandle.None) return null
        if (state.isHandleDrag) {
            return if (handle == state.activeHandle) draggedHandleAnchor else fixedHandleAnchor
        }
        val range = state.range ?: return null
        return when (handle) {
            SelectionActiveHandle.Start -> range.start
            SelectionActiveHandle.End -> range.end
            SelectionActiveHandle.None -> null
        }
    }

    fun selectionHandlePositionInRoot(handle: SelectionActiveHandle): SelectionHandlePosition? {
        val anchor = selectionAnchorForHandle(handle) ?: return null
        val entry = index.entryOf(anchor.blockStableId) ?: return null
        val geometry = geometryByStableId[anchor.blockStableId] ?: return null
        val blockSnapshot = registry.snapshotOf(anchor.blockStableId) ?: return null
        val rootSnapshot = registry.rootSnapshot ?: return null
        val blockCoordinates = registry.coordinatesOf(anchor.blockStableId) ?: return null
        val rootCoordinates = registry.rootCoordinates?.takeIf { it.isAttached } ?: return null
        val inlineBlock = geometry.block as? LayoutInlineBlockModel
        val blockLocalPosition: Offset
        if (inlineBlock == null || geometry.runs.isEmpty()) {
            val atStart = anchor.charInBlock <= 0
            blockLocalPosition = Offset(
                x = if (atStart) 0f else blockSnapshot.size.width.toFloat(),
                y = if (atStart) 0f else blockSnapshot.size.height.toFloat(),
            )
        } else {
            val (span, offsetInRun) = charToRunForHandle(
                geometry = geometry,
                totalChars = entry.totalChars,
                charInBlock = anchor.charInBlock,
                handle = handle,
            )
                ?: return null
            val run = span.run
            val runLeft = run.frame.left - inlineBlock.frame.left
            val runTop = run.frame.top - inlineBlock.frame.top
            val horizontal = textLayoutFor(geometry, span)?.horizontalPosition(
                offsetInRun.coerceIn(0, span.text.length)
            ) ?: if (offsetInRun <= 0) 0f else run.frame.width
            blockLocalPosition = Offset(
                x = runLeft + horizontal,
                y = runTop + run.frame.height,
            )
        }
        val positionInWindow = blockCoordinates.localToWindow(blockLocalPosition)
        val positionInRoot = rootCoordinates.windowToLocal(positionInWindow)
        if (positionInRoot.x < 0f || positionInRoot.x > rootSnapshot.size.width ||
            positionInRoot.y < 0f || positionInRoot.y > rootSnapshot.size.height
        ) {
            return null
        }
        return SelectionHandlePosition(positionInRoot)
    }

    val selectedText: String
        get() = state.range?.let { extractSelectedText(index, it) } ?: ""

    fun copySelection() {
        val text = selectedText
        if (text.isEmpty()) return
        val target = clipboard ?: return
        coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            target.setClipEntry(plainTextClipEntry(text))
        }
    }

    /**
     * 当前选区在 window 坐标系下的并集包围盒，供 [TextToolbar.showMenu]
     * 定位浮动菜单使用。遍历所有可见且参与选区的 block，把其 block-local 高亮矩形经
     * [LayoutCoordinates.localToWindow] 映射后求并集；无可见选区时返回 null。
     */
    fun selectionBoundsInWindow(): Rect? {
        if (state.range == null) return null
        var left = Float.MAX_VALUE
        var top = Float.MAX_VALUE
        var right = -Float.MAX_VALUE
        var bottom = -Float.MAX_VALUE
        var found = false
        for (entry in registry.visibleEntries(index)) {
            if (geometryByStableId[entry.stableId] == null) continue
            val coords = registry.coordinatesOf(entry.stableId) ?: continue
            val boxes = highlightBoxesFor(entry.stableId)
            for (box in boxes) {
                val topLeft = coords.localToWindow(Offset(box.left, box.top))
                val bottomRight = coords.localToWindow(Offset(box.right, box.bottom))
                left = minOf(left, topLeft.x, bottomRight.x)
                top = minOf(top, topLeft.y, bottomRight.y)
                right = maxOf(right, topLeft.x, bottomRight.x)
                bottom = maxOf(bottom, topLeft.y, bottomRight.y)
                found = true
            }
        }
        if (!found) return null
        return Rect(left, top, right, bottom)
    }

    /**
     * 当前选区的公开快照：纯文本、窗口包围盒与逐块字符区间（携带块源属性）。
     * 供自定义选区工具栏消费；无选区或无可见几何时返回 null。
     */
    fun selectionInfo(): MarkdownSelectionInfo? {
        val range = state.range ?: return null
        val bounds = selectionBoundsInWindow() ?: return null
        val startOrder = index.orderOf(range.start.blockStableId)
        val endOrder = index.orderOf(range.end.blockStableId)
        val ranges = ArrayList<MarkdownSelectionBlockRange>()
        for (entry in index.entries) {
            if (startOrder != null && entry.order < startOrder) continue
            if (endOrder != null && entry.order > endOrder) continue
            val from = if (entry.stableId == range.start.blockStableId) range.start.charInBlock else 0
            val to = if (entry.stableId == range.end.blockStableId) range.end.charInBlock else entry.totalChars
            if (to <= from) continue
            ranges += MarkdownSelectionBlockRange(
                blockStableId = entry.stableId,
                blockText = entry.text,
                startInBlock = from,
                endInBlock = to,
                blockAttributes = entry.attributes,
            )
        }
        if (ranges.isEmpty()) return null
        return MarkdownSelectionInfo(
            text = selectedText,
            boundsInWindow = bounds,
            ranges = ranges,
        )
    }

    /**
     * 把 window 坐标解析为逻辑锚点：
     * 选择纵向最接近的可见 block，命中其 run，再用与布局一致的测量口径求字符偏移。
     */
    private fun anchorFromWindow(windowOffset: Offset, requireTextHit: Boolean = false): SelectionAnchor? {
        val visible = registry.visibleEntries(index)
        if (visible.isEmpty()) return null

        var best: SelectionBlockEntry? = null
        var bestLocal: Offset = Offset.Zero
        var bestPenalty = Float.MAX_VALUE
        for (entry in visible) {
            if (geometryByStableId[entry.stableId] == null) continue
            val coords = registry.coordinatesOf(entry.stableId) ?: continue
            val local = coords.windowToLocal(windowOffset)
            val height = coords.size.height.toFloat()
            val penalty = when {
                local.y < 0f -> -local.y
                local.y > height -> local.y - height
                else -> 0f
            }
            if (penalty < bestPenalty) {
                bestPenalty = penalty
                best = entry
                bestLocal = local
            }
            if (penalty == 0f) {
                // Inside this block vertically; prefer it directly.
                best = entry
                bestLocal = local
                break
            }
        }

        val entry = best ?: return null
        val geometry = geometryByStableId[entry.stableId] ?: return null
        val inlineBlock = geometry.block as? LayoutInlineBlockModel
        if (inlineBlock == null) {
            if (requireTextHit) return null
            return SelectionAnchor(entry.stableId, textOnlyBlockOffset(entry, geometry, bestLocal))
        }
        if (requireTextHit) {
            val placement = inlineBlock.runPlacements().firstOrNull {
                bestLocal.x in it.x.toFloat()..(it.x + it.width).toFloat() &&
                    bestLocal.y in it.y.toFloat()..(it.y + it.height).toFloat()
            } ?: return null
            if (placement.run !is LayoutTextRun) return null
        }
        val hit = hitTestRunInBlock(inlineBlock, bestLocal.x, bestLocal.y) ?: run {
            // No runs at all; snap to block boundary by horizontal side.
            return SelectionAnchor(entry.stableId, if (bestLocal.x <= 0f) 0 else entry.totalChars)
        }

        val span = geometry.runs.firstOrNull {
            it.lineIndex == hit.lineIndex && it.runIndex == hit.runIndex
        } ?: return SelectionAnchor(entry.stableId, 0)

        val offsetInRun = charOffsetInRun(geometry, span, hit)
        val charInBlock = (span.charStart + offsetInRun).coerceIn(0, entry.totalChars)
        return SelectionAnchor(entry.stableId, charInBlock)
    }

    private fun textOnlyBlockOffset(
        entry: SelectionBlockEntry,
        geometry: SelectionBlockGeometry,
        local: Offset,
    ): Int {
        if (entry.totalChars == 0) return 0
        val height = (
            registry.snapshotOf(entry.stableId)?.size?.height?.toFloat()
                ?: geometry.block.frame.height
            ).coerceAtLeast(1f)
        return if (local.y < height / 2f) 0 else entry.totalChars
    }

    private fun charOffsetInRun(
        geometry: SelectionBlockGeometry,
        span: SelectionRunSpan,
        hit: RunHit,
    ): Int {
        val text = hit.run.text
        if (text.isEmpty()) return 0
        val layout = textLayoutFor(geometry, span) ?: return 0
        val raw = layout.offsetForPosition(
            x = hit.runLocalX,
            y = hit.runLocalY.coerceAtLeast(0f),
        )
        return clampToCharBoundary(text, raw.coerceIn(0, text.length))
    }

    private fun charToRunForHandle(
        geometry: SelectionBlockGeometry,
        totalChars: Int,
        charInBlock: Int,
        handle: SelectionActiveHandle,
    ): Pair<SelectionRunSpan, Int>? {
        if (handle != SelectionActiveHandle.End || charInBlock <= 0) {
            return geometry.charToRun(charInBlock, totalChars)
        }
        val clamped = charInBlock.coerceIn(0, totalChars)
        val span = geometry.runs.firstOrNull {
            clamped > it.charStart && clamped <= it.charEnd
        } ?: return geometry.charToRun(clamped, totalChars)
        return span to (clamped - span.charStart)
    }

    private fun SelectionBlockGeometry.charToRun(
        charInBlock: Int,
        totalChars: Int,
    ): Pair<SelectionRunSpan, Int>? {
        if (runs.isEmpty()) return null
        val clamped = charInBlock.coerceIn(0, totalChars)
        for (span in runs) {
            if (clamped < span.charEnd) {
                return span to (clamped - span.charStart).coerceIn(0, span.text.length)
            }
        }
        val last = runs.last()
        return last to last.text.length
    }

    private fun textLayoutFor(
        geometry: SelectionBlockGeometry,
        span: SelectionRunSpan,
    ): VisibleRunTextLayout? {
        val block = geometry.block as? LayoutInlineBlockModel ?: return null
        val run = span.run as? LayoutTextRun ?: return null
        val key = run.visibleTextLayoutKey()
        val blockCache = textLayoutsByBlock.getOrPut(geometry.stableId) { mutableMapOf() }
        return blockCache.getOrPut(key) {
            fallbackTextMeasurementCount++
            VisibleRunTextLayout(
                result = textMeasurer.measure(
                    text = run.text,
                    style = textMeasurementStyle(block.style),
                    constraints = Constraints(maxWidth = Int.MAX_VALUE),
                    maxLines = 1,
                    softWrap = false,
                ),
            )
        }
    }
}

private fun LayoutTextRun.visibleTextLayoutKey(): VisibleTextLayoutKey =
    VisibleTextLayoutKey(
        stableId = identity.stableId,
        contentRevision = identity.contentRevision,
        text = text.text,
    )

private data class VisibleRunTextLayout(
    val result: TextLayoutResult,
    val textStart: Int = 0,
    val runX: Float = 0f,
    val runY: Float = 0f,
) {
    fun horizontalPosition(offsetInRun: Int): Float =
        result.getHorizontalPosition(
            offset = (textStart + offsetInRun).coerceIn(0, result.layoutInput.text.length),
            usePrimaryDirection = true,
        ) - runX

    fun offsetForPosition(x: Float, y: Float): Int =
        result.getOffsetForPosition(Offset(x + runX, y + runY)) - textStart
}

private data class VisibleTextLayoutKey(
    val stableId: Long,
    val contentRevision: Long,
    val text: String,
)

internal data class SelectionHandlePosition(
    val positionInRoot: Offset,
)

internal val LocalMarkdownSelectionController =
    staticCompositionLocalOf<MarkdownSelectionController?> { null }

@Composable
internal fun rememberMarkdownSelectionController(
    coroutineScope: CoroutineScope,
    textMeasurer: TextMeasurer,
): MarkdownSelectionController {
    return remember(coroutineScope, textMeasurer) {
        MarkdownSelectionController(coroutineScope, textMeasurer)
    }
}
