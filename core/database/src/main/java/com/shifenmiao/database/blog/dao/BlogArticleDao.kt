package com.shifenmiao.database.blog.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.shifenmiao.database.blog.entity.BlogArticleEntity

@Dao
interface BlogArticleDao {

    @Query(
        """
        SELECT * FROM blog_article
        WHERE blog_type = :blogType
        ORDER BY fixed DESC, published_at DESC, remote_id DESC
        """
    )
    fun pagingSourceByType(blogType: Int): PagingSource<Int, BlogArticleEntity>

    @Upsert
    suspend fun upsert(entities: List<BlogArticleEntity>)

    @Query("DELETE FROM blog_article WHERE blog_type = :blogType")
    suspend fun deleteByType(blogType: Int)

    @Query("SELECT * FROM blog_article WHERE remote_id = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: Int): BlogArticleEntity?

    @Query("SELECT COUNT(*) FROM blog_article WHERE blog_type = :blogType")
    suspend fun countByType(blogType: Int): Int
}
