package com.shifenmiao.database.transfer

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shifenmiao.model.transfer.ChatMessage
import com.shifenmiao.model.transfer.MessageType

/**
 * 聊天消息数据库实体
 */
@Entity(
    tableName = "chat_messages",
    indices = [
        Index(value = ["channelId", "timestamp"]),
        Index(value = ["timestamp"])
    ]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    /**
     * 浏览器连接/会话对应的频道（用于隔离不同浏览器的聊天记录）。
     */
    val channelId: String,
    val type: String, // "text", "file", "system"
    val content: String,
    val sender: String, // "mobile" or "browser"
    val timestamp: Long,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val filePath: String? = null
) {
    companion object {
        /**
         * 历史兼容：旧版本未分频道时，把消息归到默认频道。
         */
        const val DEFAULT_CHANNEL_ID: String = "default"

        fun fromChatMessage(
            chatMessage: ChatMessage,
            channelId: String = DEFAULT_CHANNEL_ID
        ): ChatMessageEntity {
            return ChatMessageEntity(
                id = chatMessage.id,
                channelId = channelId,
                type = when (chatMessage.type) {
                    MessageType.TEXT -> "text"
                    MessageType.FILE -> "file"
                    MessageType.SYSTEM -> "system"
                },
                content = chatMessage.content,
                sender = chatMessage.sender,
                timestamp = chatMessage.timestamp,
                fileName = chatMessage.fileName,
                fileSize = chatMessage.fileSize,
                filePath = chatMessage.filePath
            )
        }
    }

    fun toChatMessage(): ChatMessage {
        return ChatMessage(
            id = id,
            type = when (type) {
                "file" -> MessageType.FILE
                "system" -> MessageType.SYSTEM
                else -> MessageType.TEXT
            },
            content = content,
            sender = sender,
            timestamp = timestamp,
            fileName = fileName,
            fileSize = fileSize,
            filePath = filePath
        )
    }
}
