package com.wanbaohe.blog.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.shifenmiao.model.blog.BlogItem
import com.shifenmiao.network.api.ApiService
import com.shifenmiao.storage.BlogListStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FeedbackRepository(
    private val apiService: ApiService
) {
    private val pagingSourceFlow = MutableStateFlow<BlogPagingSource?>(null)

    fun getBlogPager(): Pager<Int, BlogItem> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 2,
                enablePlaceholders = false,
                initialLoadSize = 10
            ),
            pagingSourceFactory = {
                BlogPagingSource(apiService).also { pagingSource ->
                    pagingSourceFlow.update { pagingSource }
                }
            }
        )
    }

    fun invalidatePagingSource() {
        pagingSourceFlow.value?.invalidate()
    }

    suspend fun getBlogDetail(blogId: Int, blogType: Int? = null): BlogItem? {
        // Try to get from cache first
        val cachedBlog = BlogListStore.loadBlogDetail(blogId)
        if (cachedBlog != null) {
            return cachedBlog
        }

        // If not in cache, fetch from network
        return try {
            val response = apiService.fetchBlog(blogId, blogType)
            if (response.isSuccessful) {
                val blog = response.body()?.data
                // Save to cache if not null
                blog?.let { BlogListStore.saveBlogDetail(blogId, it) }
                blog
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private class BlogPagingSource(
        private val apiService: ApiService
    ) : PagingSource<Int, BlogItem>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BlogItem> {
            val page = params.key ?: 1
            return try {
                // Try to get from cache first
                val cachedData = BlogListStore.loadBlogs(page, params.loadSize)

                if (cachedData != null) {
                    val publishedBlogs = cachedData.data.filter { it.publishedAt != null }
                    return LoadResult.Page(
                        data = publishedBlogs,
                        prevKey = if (page > 1) page - 1 else null,
                        nextKey = if (page < cachedData.meta.pagination.pageCount) page + 1 else null
                    )
                }

                // If not in cache, fetch from network
                val response = apiService.fetchBlogs(
                    page = page,
                    pageSize = params.loadSize
                )
                if (response.isSuccessful) {
                    response.body()?.let { res ->
                        // Only show officially published blogs
                        val publishedBlogs = res.data.filter { it.publishedAt != null }
                        val pagedRes = res.copy(data = publishedBlogs)
                        // Save to cache
                        BlogListStore.saveBlogs(page, params.loadSize, pagedRes)

                        return LoadResult.Page(
                            data = publishedBlogs,
                            prevKey = if (page > 1) page - 1 else null,
                            nextKey = if (page < res.meta.pagination.pageCount) page + 1 else null
                        )
                    }
                }
                LoadResult.Error(Exception("Failed to fetch blogs"))
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<Int, BlogItem>): Int? {
            return state.anchorPosition?.let { anchorPosition ->
                val anchorPage = state.closestPageToPosition(anchorPosition)
                anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
            }
        }
    }
}