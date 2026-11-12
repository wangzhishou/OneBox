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

package com.shifenmiao.database.ai.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.shifenmiao.database.ai.converters.AiEngineConverter
import com.shifenmiao.database.ai.converters.VoiceTypeConverter
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.AIConversationTitleSource
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.voice.VoiceType

@Entity(
    tableName = "conversations",
    indices = [
        Index(value = ["conversation_id"], unique = true),
        Index(value = ["entry_type", "entry_ref_id"]),
        Index(value = ["last_active_at"]),
    ]
)
@TypeConverters(AiEngineConverter::class, VoiceTypeConverter::class)
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    @ColumnInfo(name = "entry_type") val entryType: String,
    @ColumnInfo(name = "entry_ref_id") val entryRefId: String?,
    @ColumnInfo(name = "app_title") val appTitle: String,
    @ColumnInfo(name = "engine") val engine: AiEngine,
    @ColumnInfo(name = "show_avatar") val showAvatar: Boolean,
    @ColumnInfo(name = "show_tokens") val showTokens: Boolean,
    @ColumnInfo(name = "voice_type") val voiceType: VoiceType,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "title_source") val titleSource: String,
    @ColumnInfo(name = "prompt") val prompt: String,
    @ColumnInfo(name = "placeholder") val placeholder: String,
    @ColumnInfo(name = "history_visible", defaultValue = "1") val historyVisible: Boolean,
    @ColumnInfo(name = "last_message_preview") val lastMessagePreview: String,
    @ColumnInfo(name = "last_user_message_preview") val lastUserMessagePreview: String,
    @ColumnInfo(name = "last_active_at") val lastActiveAt: Long,
    @ColumnInfo(name = "message_count") val messageCount: Int,
) {
    companion object {
        /**
         * `conversations` 现在承接 AI 会话中心的摘要信息。
         * 聊天明细继续落在 `message`，而历史中心直接消费这里的结构化归属与预览字段。
         */
        fun fromConversation(conversation: Conversation): ConversationEntity {
            return ConversationEntity(
                conversationId = conversation.id,
                entryType = conversation.entryType.name,
                entryRefId = conversation.entryRefId,
                appTitle = conversation.appTitle,
                engine = conversation.engine,
                showAvatar = conversation.showAvatar,
                showTokens = conversation.showTokens,
                voiceType = conversation.voiceType,
                title = conversation.title,
                titleSource = conversation.titleSource.name,
                prompt = conversation.prompt,
                placeholder = conversation.placeholder,
                historyVisible = conversation.historyVisible,
                lastMessagePreview = conversation.lastMessagePreview,
                lastUserMessagePreview = conversation.lastUserMessagePreview,
                lastActiveAt = conversation.lastActiveAt,
                messageCount = conversation.messageCount,
            )
        }

        fun toConversation(entity: ConversationEntity): Conversation {
            val entryType = entity.entryType.toEntryType()
            return Conversation(
                id = entity.conversationId,
                entryType = entryType,
                entryRefId = entity.entryRefId,
                appTitle = entity.appTitle,
                engine = entity.engine,
                showAvatar = entity.showAvatar,
                showTokens = entity.showTokens,
                voiceType = entity.voiceType,
                title = entity.title,
                titleSource = entity.titleSource.toTitleSource(),
                prompt = entity.prompt,
                placeholder = entity.placeholder,
                historyVisible = entity.historyVisible,
                lastMessagePreview = entity.lastMessagePreview,
                lastUserMessagePreview = entity.lastUserMessagePreview,
                lastActiveAt = entity.lastActiveAt,
                messageCount = entity.messageCount,
                promptId = entity.entryRefId?.toIntOrNull()
                    ?.takeIf { entryType == AIConversationEntryType.PROMPT },
            )
        }

        private fun String.toEntryType(): AIConversationEntryType =
            runCatching { enumValueOf<AIConversationEntryType>(this) }
                .getOrDefault(AIConversationEntryType.CHAT)

        private fun String.toTitleSource(): AIConversationTitleSource =
            runCatching { enumValueOf<AIConversationTitleSource>(this) }
                .getOrDefault(AIConversationTitleSource.SYSTEM)
    }
}
