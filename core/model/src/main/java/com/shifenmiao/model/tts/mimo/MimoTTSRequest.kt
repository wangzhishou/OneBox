package com.shifenmiao.model.tts.mimo

data class MimoTTSRequest(
    val model: String,
    val messages: List<MimoMessage>,
    val audio: MimoAudioConfig,
    val stream: Boolean = false,
)

data class MimoMessage(
    val role: String,
    val content: String,
)

data class MimoAudioConfig(
    val format: String,
    val voice: String,
)
