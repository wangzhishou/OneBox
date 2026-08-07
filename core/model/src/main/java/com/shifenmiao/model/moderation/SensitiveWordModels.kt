package com.shifenmiao.model.moderation

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SensitiveWordCheckRequest(
    @SerializedName("scene")
    val scene: String,
    @SerializedName("fields")
    val fields: List<SensitiveWordCheckField>
)

@Keep
data class SensitiveWordCheckField(
    @SerializedName("key")
    val key: String,
    @SerializedName("text")
    val text: String
)

@Keep
data class SensitiveWordCheckResponse(
    @SerializedName("hit")
    val hit: Boolean = false,
    @SerializedName("hits")
    val hits: List<SensitiveWordHit> = emptyList(),
    @SerializedName("message")
    val message: String? = null
)

@Keep
data class SensitiveWordHit(
    @SerializedName("key")
    val key: String,
    @SerializedName("words")
    val words: List<String> = emptyList(),
    @SerializedName("reason")
    val reason: String? = null
)
