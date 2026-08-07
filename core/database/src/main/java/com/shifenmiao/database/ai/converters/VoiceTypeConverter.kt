package com.shifenmiao.database.ai.converters

import androidx.room.TypeConverter
import com.shifenmiao.model.voice.VoiceType

class VoiceTypeConverter {

    @TypeConverter
    fun fromVoiceType(voiceType: VoiceType): String {
        return voiceType.name
    }

    @TypeConverter
    fun toVoiceType(voiceTypeString: String): VoiceType {
        return VoiceType.valueOf(voiceTypeString)
    }
}