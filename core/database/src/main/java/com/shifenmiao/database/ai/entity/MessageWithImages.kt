package com.shifenmiao.database.ai.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.shifenmiao.database.image.entity.ImageEntity

/**
 * 表示消息及其关联的图片列表的关系类
 */
data class MessageWithImages(
    @Embedded val message: MessageEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "message_id"
    )
    val images: List<ImageEntity> = emptyList()
)
