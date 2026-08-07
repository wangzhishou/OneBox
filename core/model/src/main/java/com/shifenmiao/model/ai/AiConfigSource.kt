package com.shifenmiao.model.ai

import kotlinx.serialization.Serializable

@Serializable
enum class AiConfigSource {
    REMOTE,
    LOCAL;

    companion object {
        fun fromValue(value: String?): AiConfigSource {
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true)
            } ?: REMOTE
        }
    }
}

