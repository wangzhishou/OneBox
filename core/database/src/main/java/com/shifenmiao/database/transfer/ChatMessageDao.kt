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

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 聊天消息数据访问对象
 */
@Dao
interface ChatMessageDao {

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    suspend fun getAllMessages(): List<ChatMessageEntity>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE timestamp >= :startTime ORDER BY timestamp ASC")
    suspend fun getMessagesSince(startTime: Long): List<ChatMessageEntity>

    // ====== channel-scoped ======

    @Query("SELECT * FROM chat_messages WHERE channelId = :channelId ORDER BY timestamp ASC")
    suspend fun getMessagesByChannel(channelId: String): List<ChatMessageEntity>

    @Query("SELECT * FROM chat_messages WHERE channelId = :channelId ORDER BY timestamp ASC")
    fun getMessagesByChannelFlow(channelId: String): Flow<List<ChatMessageEntity>>

    @Query(
        "SELECT channelId FROM chat_messages GROUP BY channelId ORDER BY MAX(timestamp) DESC"
    )
    suspend fun listChannelsByLastMessageDesc(): List<String>

    @Query("DELETE FROM chat_messages WHERE channelId = :channelId")
    suspend fun deleteChannelMessages(channelId: String)

    /**
     * 保留每个频道最近的 [maxCount] 条消息；超过的会被删除（按时间最早优先删除）。
     */
    @Query(
        "DELETE FROM chat_messages WHERE channelId = :channelId AND id NOT IN (" +
            "SELECT id FROM chat_messages WHERE channelId = :channelId ORDER BY timestamp DESC LIMIT :maxCount" +
        ")"
    )
    suspend fun trimChannelToMaxCount(channelId: String, maxCount: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()

    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getMessageCount(): Int

    /**
     * 会话列表（按最后一条消息时间倒序）。
     *
     * 返回字段：channelId, lastTimestamp, lastSender, lastContent, messageCount
     */
    @Query(
        "SELECT m.channelId AS channelId, " +
            "s.deviceName AS deviceName, " +
            "MAX(m.timestamp) AS lastTimestamp, " +
            "(SELECT sender FROM chat_messages WHERE channelId = m.channelId ORDER BY timestamp DESC LIMIT 1) AS lastSender, " +
            "(SELECT content FROM chat_messages WHERE channelId = m.channelId ORDER BY timestamp DESC LIMIT 1) AS lastContent, " +
            "COUNT(*) AS messageCount " +
        "FROM chat_messages m " +
        "LEFT JOIN chat_sessions s ON s.channelId = m.channelId " +
        "GROUP BY m.channelId ORDER BY lastTimestamp DESC"
    )
    suspend fun listSessionSummaries(): List<ChatSessionSummary>

    /**
     * listSessionSummaries() 的 Flow 版本：用于 Compose/UI 订阅数据库变化。
     */
    @Query(
        "SELECT m.channelId AS channelId, " +
            "s.deviceName AS deviceName, " +
            "MAX(m.timestamp) AS lastTimestamp, " +
            "(SELECT sender FROM chat_messages WHERE channelId = m.channelId ORDER BY timestamp DESC LIMIT 1) AS lastSender, " +
            "(SELECT content FROM chat_messages WHERE channelId = m.channelId ORDER BY timestamp DESC LIMIT 1) AS lastContent, " +
            "COUNT(*) AS messageCount " +
        "FROM chat_messages m " +
        "LEFT JOIN chat_sessions s ON s.channelId = m.channelId " +
        "GROUP BY m.channelId ORDER BY lastTimestamp DESC"
    )
    fun listSessionSummariesFlow(): Flow<List<ChatSessionSummary>>
}

/**
 * Room projection for session summary.
 */
data class ChatSessionSummary(
    val channelId: String,
    val deviceName: String?,
    val lastTimestamp: Long,
    val lastSender: String?,
    val lastContent: String?,
    val messageCount: Int
)
