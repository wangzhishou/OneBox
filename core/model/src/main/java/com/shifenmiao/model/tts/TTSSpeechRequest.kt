package com.shifenmiao.model.tts

import com.google.gson.annotations.SerializedName

data class TTSSpeechRequest(
    val model: String,
    val input: String,
    val voice: String,
    @SerializedName("response_format")
    val responseFormat: String = "mp3",
    val speed: Double = 1.0,
)
