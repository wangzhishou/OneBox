package com.shifenmiao.common.blog

import androidx.paging.PagingData
import com.shifenmiao.model.blog.BlogItem
import kotlinx.coroutines.flow.Flow

/**
 * 博客/文章列表仓库。
 *
 * 列表从本地 Room 分页读取；[refresh] 负责从 `/api/blogs` 全量拉取并持久化。
 */
interface BlogRepository {

    /**
     * 返回指定 [blogType] 的文章分页流。
     */
    fun pagingFlow(blogType: Int): Flow<PagingData<BlogItem>>

    /**
     * 强制刷新指定 [blogType] 的数据，成功后替换本地缓存。
     */
    suspend fun refresh(blogType: Int): Result<Unit>
}
