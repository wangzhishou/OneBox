package com.hrm.markdown.renderer

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect

/**
 * 一个被选中块内的字符区间。块由渲染层稳定 ID 标识，[blockText] 是该块的
 * 可选中纯文本（与选区索引一致），偏移即该文本内的字符下标（半开区间）。
 */
@Immutable
data class MarkdownSelectionBlockRange(
    val blockStableId: Long,
    val blockText: String,
    val startInBlock: Int,
    val endInBlock: Int,
    /**
     * 源块节点携带的属性透传（CommonMark 里来自 `{key=value}` 块属性，
     * HTML 输入场景常用于回传 `data-*` 标识）。库不解释，使用方按约定读取。
     */
    val blockAttributes: Map<String, String> = emptyMap(),
)

/**
 * 一次完成的选区快照：纯文本、窗口坐标包围盒与逐块字符区间。
 * 由自定义选区工具栏（[MarkdownComponents.selectionToolbar]）消费。
 */
@Immutable
data class MarkdownSelectionInfo(
    val text: String,
    val boundsInWindow: Rect,
    val ranges: List<MarkdownSelectionBlockRange>,
)

/** 自定义选区工具栏可调用的宿主动作。 */
class MarkdownSelectionActions(
    private val onCopy: () -> Unit,
    private val onSelectAll: () -> Unit,
    private val onDismiss: () -> Unit,
    private val onSelectRange: (Long, Int, Int) -> Boolean,
) {
    constructor(
        onCopy: () -> Unit,
        onSelectAll: () -> Unit,
        onDismiss: () -> Unit,
    ) : this(onCopy, onSelectAll, onDismiss, { _, _, _ -> false })

    /** 复制选中文本并清除选区（与平台工具栏行为一致）。 */
    fun copySelection() = onCopy()

    fun selectAll() = onSelectAll()

    /** Selects a non-empty half-open UTF-16 range in the current block; invalid input leaves selection unchanged. */
    fun selectRange(blockStableId: Long, start: Int, end: Int): Boolean =
        onSelectRange(blockStableId, start, end)

    /** 清除选区并隐藏自定义工具栏。 */
    fun dismiss() = onDismiss()
}
