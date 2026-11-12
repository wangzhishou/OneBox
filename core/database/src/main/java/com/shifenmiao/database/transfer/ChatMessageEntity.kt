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
