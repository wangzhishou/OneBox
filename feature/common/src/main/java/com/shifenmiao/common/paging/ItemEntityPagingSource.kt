package com.shifenmiao.common.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.item.entity.ItemWithCategories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItemEntityPagingSource(
    private val appDatabase: AppDatabase,
    private val categoryId: Int
) : PagingSource<Int, ItemWithCategories>() {

    override fun getRefreshKey(state: PagingState<Int, ItemWithCategories>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ItemWithCategories> {
        return withContext(Dispatchers.IO) {
            val page = params.key ?: 1
            val pageSize = params.loadSize
            val items = if (categoryId > 0) {
                appDatabase.itemEntityDao().getItemsListByCategoryId(categoryId, page, pageSize)
            } else {
                appDatabase.itemEntityDao().getAllItemsList(page, pageSize)
            }
            val nextKey = if (items.isEmpty()) {
                null
            } else {
                page + 1
            }
            LoadResult.Page(
                data = items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = nextKey
            )
        }
    }
}