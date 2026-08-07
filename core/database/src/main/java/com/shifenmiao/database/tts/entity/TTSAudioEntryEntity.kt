package com.shifenmiao.database.tts.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tts_audio_entries",
    indices = [
        Index(value = ["cache_hash"], unique = true),
        Index(value = ["tag"]),
    ]
)
data class TTSAudioEntryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "voice") val voice: String,
    @ColumnInfo(name = "speed") val speed: Double,
    @ColumnInfo(name = "file_path") val filePath: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "tag") val tag: String,
    @ColumnInfo(name = "provider_type") val providerType: String,
    @ColumnInfo(name = "cache_hash") val cacheHash: String,
)
