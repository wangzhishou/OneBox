/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.blog.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.shifenmiao.model.blog.BlogItem
import com.shifenmiao.model.blog.FeedbackRequest
import com.shifenmiao.network.api.ApiService
import com.shifenmiao.storage.BlogListStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    /**
     * 提交用户反馈(对应 POST api/blogs/feedback)。
     *
     * 该接口走 AnyAuth 鉴权(游客 token 即可),服务端对未登录请求有默认用户兜底,
     * 因此不要求登录态。服务端对 title 有全局非空 + 唯一约束,
     * 这里取正文首行前缀拼接时间戳生成标题,避免重名导致提交失败。
     *
     * @return true 表示提交成功
     */
    suspend fun submitFeedback(content: String): Boolean {
        val request = FeedbackRequest(
            title = buildFeedbackTitle(content),
            content = content,
            pictureIds = emptyList(),
            tagIds = emptyList(),
            blogType = FEEDBACK_BLOG_TYPE
        )
        return try {
            apiService.createFeedback(request).isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    private fun buildFeedbackTitle(content: String): String {
        val prefix = content.lineSequence().firstOrNull().orEmpty().trim().take(TITLE_PREFIX_LENGTH)
        val timestamp = SimpleDateFormat(TITLE_TIME_PATTERN, Locale.US).format(Date())
        return "$prefix-$timestamp"
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

    private companion object {
        /** 与反馈页 CreateFeedbackComponent 默认 blogType 保持一致 */
        const val FEEDBACK_BLOG_TYPE = 1

        const val TITLE_PREFIX_LENGTH = 16

        const val TITLE_TIME_PATTERN = "yyyyMMddHHmmss"
    }
}