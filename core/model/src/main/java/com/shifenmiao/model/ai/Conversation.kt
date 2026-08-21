package com.shifenmiao.model.ai

import android.os.Parcelable
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.voice.VoiceType
import kotlinx.parcelize.Parcelize
import java.util.Date
import com.shifenmiao.core.R
import kotlinx.serialization.Serializable

@Serializable
enum class AIConversationEntryType {
    ASSISTANT,
    CHAT,
    PROMPT,
    AGENT,
    STREAM_QA,
    QA,
    DUEL
}

@Serializable
enum class AIConversationTitleSource {
    USER_PREFIX,
    AI_SUMMARY,
    MANUAL,
    SYSTEM,
}

@Serializable
@Parcelize
data class Conversation(
    var id: String = Date().time.toString(),
    var entryType: AIConversationEntryType = AIConversationEntryType.CHAT,
    var entryRefId: String? = null,
    var appTitle: String = "",
    var engine: AiEngine = AiEngine.defaultEngine(),
    val showAvatar: Boolean = false,
    val showTokens: Boolean = true,
    /**
     * 外部传递进来的,需要显示上一次的对话记录
     */
    val showLastMessage: Boolean = false,
    val voiceType: VoiceType = VoiceType.FEMALE,
    var title: String = AppContext.getString(R.string.ai_chat_title),
    var titleSource: AIConversationTitleSource = AIConversationTitleSource.SYSTEM,
    var prompt: String = "",
    var placeholder: String = AppContext.getString(R.string.default_placeholder),
    val historyVisible: Boolean = true,
    var template: String? = null,
    val promptId: Int? = null,
    /** 服务端 prompt id；本地创建未推送时为 null。fallback 路径专用，与 promptId 本地表主键解耦。 */
    val promptRemoteId: Int? = null,
    /** 服务端 prompt 的 Strapi v5 documentId；回查 / 刷新远端 prompt 时优先于 [promptRemoteId]。 */
    val promptDocumentId: String? = null,
    val lastMessagePreview: String = "",
    val lastUserMessagePreview: String = "",
    val lastActiveAt: Long = Date().time,
    val messageCount: Int = 0,
    /**
     * 多模态识别的时候图片列表
     */
    val imageUrls: List<String>? = null
) : Parcelable {
    /**
     * 获取应用标题
     */
    fun miniAppTitle(): String {
        return when (entryType) {
            AIConversationEntryType.PROMPT,
            AIConversationEntryType.AGENT -> appTitle.ifBlank { title }

            AIConversationEntryType.DUEL -> appTitle.ifBlank {
                AppContext.getString(R.string.ai_duel_chat_title)
            }

            AIConversationEntryType.CHAT -> {
                title.ifBlank { AppContext.getString(R.string.ai_chat_title) }
            }

            AIConversationEntryType.ASSISTANT -> AppContext.getString(R.string.ai_tab_chat_title)
            else -> {
                AppContext.getString(R.string.ai_stream_answer_title)
            }
        }
    }
}

