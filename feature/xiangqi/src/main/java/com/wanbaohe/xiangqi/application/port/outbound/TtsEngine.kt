package com.wanbaohe.xiangqi.application.port.outbound

import java.io.File

interface TtsEngine {
    suspend fun synthesize(text: String, tag: String): Result<File>
    suspend fun regenerate(text: String, tag: String): Result<File>
    suspend fun getAudioByTextAndTag(text: String, tag: String): CachedAudio?
    fun isLoggedIn(): Boolean
    fun canPrepareVoice(): Boolean
}

data class CachedAudio(
    val filePath: String,
)
