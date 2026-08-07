package com.shifenmiao.database.blog.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 博客/文章列表本地缓存表。
 *
 * 与后台 `blogs` 表对应，按 `blog_type` 区分不同栏目（如反馈=1，玩法=2）。
 * 图片、标签等集合字段以 JSON 字符串存储，由业务层解析。
 */
@Entity(
    tableName = "blog_article",
    indices = [
        Index(value = ["remote_id", "blog_type"], unique = true),
        Index(value = ["blog_type"]),
        Index(value = ["published_at"]),
    ]
)
data class BlogArticleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "remote_id") val remoteId: Int = 0,
    @ColumnInfo(name = "blog_type") val blogType: Int = 0,
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "summary") val summary: String? = null,
    @ColumnInfo(name = "content") val content: String? = null,
    @ColumnInfo(name = "author_name") val authorName: String = "",
    @ColumnInfo(name = "author_avatar") val authorAvatar: String = "",
    @ColumnInfo(name = "pictures_json") val picturesJson: String? = null,
    @ColumnInfo(name = "tags_json") val tagsJson: String? = null,
    @ColumnInfo(name = "fixed", defaultValue = "0") val fixed: Boolean = false,
    @ColumnInfo(name = "published_at") val publishedAt: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long? = null,
    @ColumnInfo(name = "synced_at") val syncedAt: Long = System.currentTimeMillis(),
)
