package com.wanbaohe.chess.application.port.outbound

import java.io.File

interface TtsEngine {
    suspend fun synthesize(text: String, tag: String): Result<File>
    suspend fun regenerate(text: String, tag: String): Result<File>
    suspend fun getAudioByTextAndTag(text: String, tag: String): CachedAudio?
}

data class CachedAudio(
    val filePath: String,
)
