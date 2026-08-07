package com.shifenmiao.common.blog

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.blog.entity.BlogArticleEntity
import com.shifenmiao.model.StrapiImage
import com.shifenmiao.model.blog.Author
import com.shifenmiao.model.blog.BlogItem
import com.shifenmiao.model.blog.Tag
import com.shifenmiao.network.api.ApiService
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BlogRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val appDatabase: AppDatabase,
    private val dispatchersHolder: DispatchersHolder,
    private val gson: Gson = Gson(),
) : BlogRepository {

    override fun pagingFlow(blogType: Int): Flow<PagingData<BlogItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false,
                initialLoadSize = PAGE_SIZE,
            ),
            pagingSourceFactory = {
                appDatabase.blogArticleDao().pagingSourceByType(blogType)
            },
        ).flow.map { pagingData ->
            pagingData.map { entity -> entity.toBlogItem(gson) }
        }
    }

    override suspend fun refresh(blogType: Int): Result<Unit> = withContext(dispatchersHolder.ioDispatcher) {
        runCatching {
            val allEntities = mutableListOf<BlogArticleEntity>()
            var page = 1
            var pageCount = 1
            val syncedAt = System.currentTimeMillis()

            do {
                val response = apiService.fetchBlogs(
                    page = page,
                    pageSize = REFRESH_PAGE_SIZE,
                    blogType = blogType,
                )
                if (!response.isSuccessful) {
                    throw Exception("Failed to fetch blogs: ${response.code()}")
                }
                val body = response.body()
                    ?: throw Exception("Empty blogs response")

                // Only cache officially published blogs (skip drafts)
                allEntities.addAll(
                    body.data
                        .filter { it.publishedAt != null }
                        .map { it.toEntity(blogType, syncedAt, gson) }
                )

                pageCount = body.meta.pagination.pageCount.coerceAtLeast(1)
                page++
            } while (page <= pageCount && page <= MAX_REFRESH_PAGES)

            appDatabase.withTransaction {
                appDatabase.blogArticleDao().deleteByType(blogType)
                appDatabase.blogArticleDao().upsert(allEntities)
            }
        }
    }

    private companion object {
        const val PAGE_SIZE = 10
        const val REFRESH_PAGE_SIZE = 50
        const val MAX_REFRESH_PAGES = 100
    }
}

private fun BlogItem.toEntity(
    blogType: Int,
    syncedAt: Long,
    gson: Gson,
): BlogArticleEntity = BlogArticleEntity(
    remoteId = id,
    blogType = blogType,
    title = title,
    summary = summary,
    content = content,
    authorName = author?.nickname.orEmpty(),
    authorAvatar = author?.avatar.orEmpty(),
    picturesJson = picture?.let { gson.toJson(it) },
    tagsJson = tags.takeIf { it.isNotEmpty() }?.let { gson.toJson(it) },
    fixed = fixed,
    publishedAt = publishedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncedAt = syncedAt,
)

private fun BlogArticleEntity.toBlogItem(gson: Gson): BlogItem {
    val pictureList = picturesJson?.let { json ->
        try {
            gson.fromJson<List<StrapiImage>>(
                json,
                object : TypeToken<List<StrapiImage>>() {}.type
            )
        } catch (_: Exception) {
            null
        }
    }
    val tagList = tagsJson?.let { json ->
        try {
            gson.fromJson<List<Tag>>(
                json,
                object : TypeToken<List<Tag>>() {}.type
            )
        } catch (_: Exception) {
            emptyList()
        }
    } ?: emptyList()

    return BlogItem(
        id = remoteId,
        title = title,
        summary = summary,
        content = content,
        author = Author(
            nickname = authorName,
            avatar = authorAvatar,
        ),
        picture = pictureList,
        tags = tagList,
        fixed = fixed,
        publishedAt = publishedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
