package com.shifenmiao.database.ai.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.shifenmiao.database.image.entity.ImageEntity
import com.shifenmiao.database.search.entity.SearchResultEntity

/**
 * 表示消息及其关联的所有关系数据（图片和搜索结果）
 */
data class MessageWithAllRelations(
    @Embedded val message: MessageEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "message_id"
    )
    val images: List<ImageEntity> = emptyList(),
    
    @Relation(
        parentColumn = "id",
        entityColumn = "message_id"
    )
    val searchResults: List<SearchResultEntity> = emptyList()
)
