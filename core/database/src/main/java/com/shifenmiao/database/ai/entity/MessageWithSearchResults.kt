package com.shifenmiao.database.ai.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.shifenmiao.database.search.entity.SearchResultEntity

/**
 * 表示消息及其关联的搜索结果列表的关系类
 */
data class MessageWithSearchResults(
    @Embedded val message: MessageEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "message_id"
    )
    val searchResults: List<SearchResultEntity> = emptyList()
)
