package com.shifenmiao.model

import android.os.Parcelable
import com.shifenmiao.model.ai.Conversation
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class AIChatObject(
    val agentId: String = "",
    val showHistory: Boolean = false,
    val conversation: Conversation = Conversation(),
    val message:String? = "",
) : Parcelable