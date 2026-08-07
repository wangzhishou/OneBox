package com.shifenmiao.model.transfer

import com.google.gson.annotations.SerializedName

/**
 * 聊天消息模型
 */
data class ChatMessage(
    @SerializedName("id")
    val id: String,

    @SerializedName("type")
    val type: MessageType,

    @SerializedName("content")
    val content: String,

    @SerializedName("sender")
    val sender: String, // "mobile" or "browser"

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("fileName")
    val fileName: String? = null,

    @SerializedName("fileSize")
    val fileSize: Long? = null,

    @SerializedName("filePath")
    val filePath: String? = null
)

enum class MessageType {
    @SerializedName("text")
    TEXT,

    @SerializedName("file")
    FILE,

    @SerializedName("system")
    SYSTEM
}

