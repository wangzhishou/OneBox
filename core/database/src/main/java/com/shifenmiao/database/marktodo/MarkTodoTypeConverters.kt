package com.shifenmiao.database.marktodo

import androidx.room.TypeConverter

/**
 * Type converters for MarkTodo feature tables.
 */
class MarkTodoTypeConverters {

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(separator = "\u001F") // unit separator char, unlikely in tags
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split("\u001F").filter { it.isNotBlank() }
    }
}

