package com.shifenmiao.model.prompt

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class AgentJson(
    val id: String,
    val name: String,
    val emoji: String,
    val group: List<String>,
    val prompt: String,
    val description: String,
    val placeholder: String? = null,
) : Parcelable