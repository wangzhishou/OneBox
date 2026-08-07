package com.shifenmiao.model.tts.mimo

data class MimoTTSResponse(
    val choices: List<MimoChoice> = emptyList(),
)

data class MimoChoice(
    val message: MimoMessageWithAudio? = null,
)

data class MimoMessageWithAudio(
    val role: String = "",
    val content: String = "",
    val audio: MimoAudioData? = null,
)

data class MimoAudioData(
    val data: String = "",
    val format: String? = null,
)
