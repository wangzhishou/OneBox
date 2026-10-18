package com.shifenmiao.online.component

import androidx.paging.PagingSource
import androidx.paging.PagingState

/**
 * 包一层 Room 的 [PagingSource]，只改刷新起点。
 *
 * Room 的刷新键会把锚点放在新窗口中间（锚点减去 initialLoadSize 的一半），
 * 保证前后都有条目可滑。同步按页写入时，列表已经画出来，锚点是当时屏幕上的卡片；
 * 后写进来、排序更靠前的条目落在窗口上方，视口就停在中间。
 *
 * 人还没离开头部时，刷新键返回 null，下一次加载从查询的第一行开始（最新的在最前）。
 * 人已经往下翻过之后，交回 Room 原来的锚点，刷新不再把视口拽回顶部。
 */
internal class HeadFollowPagingSource<Value : Any>(
    private val delegate: PagingSource<Int, Value>,
    private val refreshFromHead: () -> Boolean,
) : PagingSource<Int, Value>() {

    init {
        delegate.registerInvalidatedCallback { invalidate() }
        registerInvalidatedCallback { delegate.invalidate() }
    }

    override val jumpingSupported: Boolean
        get() = delegate.jumpingSupported

    override fun getRefreshKey(state: PagingState<Int, Value>): Int? {
        if (refreshFromHead()) return null
        return delegate.getRefreshKey(state)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Value> =
        delegate.load(params)
}
