package com.shifenmiao.model.ai

import android.os.Parcelable
import com.shifenmiao.model.Source
import com.shifenmiao.model.common.Meta
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ChatPrompt(
    val id: Int = 0,
    /** 服务端 ID；本地创建未推送时为 null。fallback 路径专用，与 id 本地表主键解耦。 */
    val remoteId: Int? = null,
    /** Strapi v5 文档级 cuid；回查 / 回写服务端定位优先用它（数字 remoteId 重发后会漂移）。 */
    @SerialName("documentId")
    val documentId: String? = null,
    val title: String? = null,
    val type: String? = null,
    val description: String? = null,
    val prompt: String? = null,
    val templates: String? = null,
    val placeholder: String? = null,
    val conversationId: String? = null,
    val source: Source? = null
) : Parcelable

@Parcelize
@Serializable
data class ChatPromptItem(
    val data: ChatPrompt,
    val meta: Meta
) : Parcelable