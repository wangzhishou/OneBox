package com.shifenmiao.model.voice

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class VoiceType(val config: VoiceConfig) : Parcelable {
    MALE(VoiceConfig(4106,"男声", "zh-CN-YunjianNeural", 1.0f, "male")),
    FEMALE(VoiceConfig(4100, "女生", "zh-CN-XiaoxiaoNeural", 1.0f, "female"));
}

@Parcelize
data class VoiceConfig(
    val id: Int,
    val name: String,
    val type: String,
    val rate: Float,
    val fileType: String
) : Parcelable

data class VoiceTypeList(
    val voiceTypes: List<VoiceType>
)