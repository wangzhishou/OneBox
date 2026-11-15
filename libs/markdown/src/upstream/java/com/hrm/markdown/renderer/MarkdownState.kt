package com.hrm.markdown.renderer

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable

/** Per-view state. Block indexes exclude header/footer and refer to top-level Markdown blocks. */
@Stable
class MarkdownState internal constructor(initialBlockIndex: Int = 0, initialScrollOffset: Int = 0) {
    internal val lazyListState = LazyListState(initialBlockIndex, initialScrollOffset)
    internal val scrollState = ScrollState(0)
    internal var headerItems by mutableStateOf(0)
    internal var footerItems by mutableStateOf(0)
    internal var blockCount by mutableStateOf(0)
    internal var scrollEnabled by mutableStateOf(true)
    internal var clearSelectionAction: (() -> Unit)? = null

    val firstVisibleBlockIndex: Int
        get() = if (scrollEnabled) (lazyListState.firstVisibleItemIndex - headerItems)
            .coerceIn(0, (blockCount - 1).coerceAtLeast(0)) else 0
    val firstVisibleBlockScrollOffset: Int
        get() = if (scrollEnabled && lazyListState.firstVisibleItemIndex in headerItems until headerItems + blockCount)
            lazyListState.firstVisibleItemScrollOffset else 0
    val isScrollInProgress: Boolean get() = scrollEnabled && lazyListState.isScrollInProgress
    val canScrollForward: Boolean get() = scrollEnabled && lazyListState.canScrollForward
    val canScrollBackward: Boolean get() = scrollEnabled && lazyListState.canScrollBackward
    val lastScrolledBackward: Boolean get() = lazyListState.lastScrolledBackward

    /** Returns false when internal scrolling is disabled or the requested block does not exist. */
    suspend fun scrollToBlock(index: Int, scrollOffset: Int = 0): Boolean {
        require(index >= 0 && scrollOffset >= 0)
        if (!scrollEnabled || index >= blockCount) return false
        lazyListState.scrollToItem(index + headerItems, scrollOffset)
        return true
    }

    suspend fun animateScrollToBlock(index: Int, scrollOffset: Int = 0): Boolean {
        require(index >= 0 && scrollOffset >= 0)
        if (!scrollEnabled || index >= blockCount) return false
        lazyListState.animateScrollToItem(index + headerItems, scrollOffset)
        return true
    }

    fun clearSelection() { clearSelectionAction?.invoke() }

    /** Includes the footer and the bottom of a last block taller than the viewport. */
    suspend fun scrollToEnd(): Boolean {
        if (!scrollEnabled || blockCount == 0) return false
        lazyListState.scrollToItem(headerItems + blockCount + footerItems - 1)
        lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.let { lazyListState.scrollBy(it.size.toFloat()) }
        return true
    }

    internal companion object {
        val Saver = listSaver<MarkdownState, Int>(
            save = { listOf(it.lazyListState.firstVisibleItemIndex, it.lazyListState.firstVisibleItemScrollOffset) },
            restore = { MarkdownState(it[0], it[1]) },
        )
    }
}

@Composable
fun rememberMarkdownState(): MarkdownState = rememberSaveable(saver = MarkdownState.Saver) { MarkdownState() }
