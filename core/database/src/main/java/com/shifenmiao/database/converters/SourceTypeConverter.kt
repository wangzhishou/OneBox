package com.shifenmiao.database.converters

import androidx.room.TypeConverter
import com.shifenmiao.model.Source
import com.shifenmiao.model.ai.AIConversationEntryType

class SourceTypeConverter {
    @TypeConverter
    fun fromSource(source: Source): Int = source.value

    @TypeConverter
    fun toSource(value: Int): Source = Source.fromValue(value)

    @TypeConverter
    fun fromEntryType(type: AIConversationEntryType): String = type.name

    @TypeConverter
    fun toEntryType(string: String): AIConversationEntryType =
        enumValueOf<AIConversationEntryType>(string)
}
