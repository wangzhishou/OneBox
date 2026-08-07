package com.shifenmiao.database.search.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 搜索结果数据表实体类
 * 用于存储与消息相关的搜索结果
 */
@Entity(
    tableName = "search_results",
    indices = [Index(value = ["message_id"])],
    foreignKeys = [
        ForeignKey(
            entity = com.shifenmiao.database.ai.entity.MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["message_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SearchResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 序号
    @ColumnInfo(name = "index")
    val index: Int?,
    
    // 搜索结果URL
    @ColumnInfo(name = "url")
    val url: String?,
    
    // 搜索结果标题
    @ColumnInfo(name = "title")
    val title: String?,
    
    // 关联的消息ID
    @ColumnInfo(name = "message_id")
    val messageId: Int,
    
    // 创建时间
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    // 更新时间
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
