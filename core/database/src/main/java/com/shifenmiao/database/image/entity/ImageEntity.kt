package com.shifenmiao.database.image.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "images",
    indices = [
        Index(value = ["uri"], unique = true),
        Index(value = ["message_id"]),
        Index(value = ["conversation_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = com.shifenmiao.database.ai.entity.MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["message_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uri")
    val uri: String,

    @ColumnInfo(name = "format")
    val format: String,

    @ColumnInfo(name = "base64_data")
    val base64Data: String,

    @ColumnInfo(name = "message_id")
    val messageId: Int,

    @ColumnInfo(name = "conversation_id")
    val conversationId: String = "",

    @ColumnInfo(name = "local_path")
    val localPath: String? = null,

    @ColumnInfo(name = "thumbnail_base64")
    val thumbnailBase64: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
