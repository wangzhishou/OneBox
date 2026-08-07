package com.shifenmiao.model.voice

data class VoiceItem(
    val FriendlyName: String,
    val Gender: String,
    val Locale: String,
    val Name: String,
    val ShortName: String,
    val Status: String,
    val SuggestedCodec: String,
    val VoiceTag: VoiceTag
)