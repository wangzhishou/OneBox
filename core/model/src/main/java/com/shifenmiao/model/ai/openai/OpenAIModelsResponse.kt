package com.shifenmiao.model.ai.openai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenAIModelsResponse(
    @SerialName("object")
    val obj: String = "",
    val data: List<OpenAIModelItem> = emptyList()
)

@Serializable
data class OpenAIModelItem(
    val id: String = "",
    @SerialName("object")
    val obj: String = "",
    val created: Long = 0,
    @SerialName("owned_by")
    val ownedBy: String = ""
)
