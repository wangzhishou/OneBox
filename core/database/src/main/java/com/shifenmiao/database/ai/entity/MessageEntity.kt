package com.shifenmiao.database.ai.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.ContentType
import com.shifenmiao.model.ai.MessageUIState
import com.shifenmiao.model.ai.RoleType
import java.util.Date

@Entity(
    tableName = "message",
    indices = [Index(
        value = ["conversation_id", "completion_id", "role"],
        unique = true
    )]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0,

    @ColumnInfo(name = "completion_id")
    var completionId: String = "",

    @ColumnInfo(name = "conversation_id")
    var conversationId: String = "",

    @ColumnInfo(name = "role")
    var role: String = RoleType.USER.value,

    @ColumnInfo(name = "question")
    var question: String = "",

    @ColumnInfo(name = "answer")
    var answer: String = "",

    @ColumnInfo(name = "reasoning_content")
    var reasoningContent: String = "",

    @ColumnInfo(name = "replay_id")
    var replayId: String = "",

    @ColumnInfo(name = "created_at")
    var createdAt: Date = Date(),

    @ColumnInfo(name = "prompt_tokens")
    var promptTokens: Int = 0,

    @ColumnInfo(name = "completion_tokens")
    var completionTokens: Int = 0,

    @ColumnInfo(name = "total_tokens")
    var totalTokens: Int = 0,

    @ColumnInfo(name = "engine")
    var engine: String = "",

    @ColumnInfo(name = "model")
    var model: String = "",

    /**
     * 记录该消息轮次实际使用的协议，便于后续按协议重放上下文。
     */
    @ColumnInfo(name = "request_protocol", defaultValue = "OPENAI_COMPATIBLE")
    var requestProtocol: String = "OPENAI_COMPATIBLE",

    @ColumnInfo(name = "entry_type")
    var entryType: AIConversationEntryType = AIConversationEntryType.CHAT,

    @ColumnInfo(name = "entry_ref_id")
    var entryRefId: String? = null,

    @ColumnInfo(name = "title")
    var title: String = "",

    @ColumnInfo(name = "expired")
    var expired : Boolean = false,

    @ColumnInfo(name = "reasoning_time")
    var reasoningTime: Long = 0L,

    @ColumnInfo(name = "content_type")
    var contentType: String = ContentType.TEXT.value,

    @ColumnInfo(name = "content_url")
    var contentUrl: String = "",

    /** Provider 侧返回的 response / completion id（Responses API 续聊依赖该字段）。 */
    @ColumnInfo(name = "provider_response_id", defaultValue = "")
    var providerResponseId: String = "",

    /** 当前响应基于哪个 response 继续生成（主要用于 Responses API）。 */
    @ColumnInfo(name = "previous_response_id", defaultValue = "")
    var previousResponseId: String = "",

    @ColumnInfo(name = "search_results")
    var searchResults: String = "",

    @ColumnInfo(name = "tool_calls", defaultValue = "")
    var toolCalls: String = "",

    @ColumnInfo(name = "agent_content", defaultValue = "")
    var agentContent: String = "",

    /** Responses API 的 output items 原始快照，便于调试/恢复/继续对话。 */
    @ColumnInfo(name = "response_items_json", defaultValue = "")
    var responseItemsJson: String = "",

    /** 当前轮次最终 finish reason，供 UI/诊断页展示。 */
    @ColumnInfo(name = "finish_reason", defaultValue = "")
    var finishReason: String = "",

    /** AI 聊天附件序列化结果，JSON 结构为 feature/ai 中定义的 AttachmentPayloadDto 列表。 */
    @ColumnInfo(name = "attachments_json", defaultValue = "")
    var attachmentsJson: String = "",
) {
    @Ignore
    var uId: Int = MessageUIState.NORMAL.value
}

