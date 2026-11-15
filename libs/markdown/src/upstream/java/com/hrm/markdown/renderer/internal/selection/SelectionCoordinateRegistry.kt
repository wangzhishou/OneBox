package com.hrm.markdown.renderer.internal.selection

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.IntSize

internal data class SelectionLayoutSnapshot(
    val positionInWindow: Offset,
    val size: IntSize,
)

/**
 * 选区坐标注册表：记录根坐标系与每个可选 block 的 [LayoutCoordinates]，
 * 以 block 的 stableId 为 key。用于把 overlay 的 window 坐标换算到 block-local 坐标。
 *
 * 只有当前组合（可见）的 block 才在表中；滚动出屏的 block 会在 dispose 时移除，
 * 因此天然支持 LazyColumn 虚拟化。
 */
@Stable
internal class SelectionCoordinateRegistry {
    private val blockCoordinates = mutableStateMapOf<Long, LayoutCoordinates>()
    private val bringIntoViewRequesters = mutableMapOf<Long, BringIntoViewRequester>()
    private val blockSnapshots = mutableStateMapOf<Long, SelectionLayoutSnapshot>()
    var rootCoordinates: LayoutCoordinates? = null
        private set
    var rootSnapshot: SelectionLayoutSnapshot? by mutableStateOf(null)
        private set

    fun setRoot(coordinates: LayoutCoordinates?) {
        rootCoordinates = coordinates
        rootSnapshot = coordinates?.takeIf { it.isAttached }?.toSelectionLayoutSnapshot()
    }

    fun register(stableId: Long, coordinates: LayoutCoordinates, requester: BringIntoViewRequester? = null) {
        blockCoordinates[stableId] = coordinates
        blockSnapshots[stableId] = coordinates.toSelectionLayoutSnapshot()
        if (requester != null) bringIntoViewRequesters[stableId] = requester
        else bringIntoViewRequesters.remove(stableId)
    }

    fun requesterOf(stableId: Long): BringIntoViewRequester? =
        if (coordinatesOf(stableId) != null) bringIntoViewRequesters[stableId] else null

    fun unregister(stableId: Long) {
        blockCoordinates.remove(stableId)
        blockSnapshots.remove(stableId)
        bringIntoViewRequesters.remove(stableId)
    }

    fun coordinatesOf(stableId: Long): LayoutCoordinates? =
        blockCoordinates[stableId]?.takeIf { it.isAttached }

    fun snapshotOf(stableId: Long): SelectionLayoutSnapshot? = blockSnapshots[stableId]

    /** 把 window 坐标换算到指定 block 的 block-local 坐标；block 不可见时返回 null。 */
    fun windowToBlockLocal(stableId: Long, windowOffset: Offset): Offset? {
        val coords = coordinatesOf(stableId) ?: return null
        return coords.windowToLocal(windowOffset)
    }

    /** 当前可见（已注册且 attached）的索引条目，按文档序排列。 */
    fun visibleEntries(index: SelectionModelIndex): List<SelectionBlockEntry> =
        index.entries.filter { coordinatesOf(it.stableId) != null }

    private fun LayoutCoordinates.toSelectionLayoutSnapshot(): SelectionLayoutSnapshot =
        SelectionLayoutSnapshot(
            positionInWindow = localToWindow(Offset.Zero),
            size = size,
        )
}
