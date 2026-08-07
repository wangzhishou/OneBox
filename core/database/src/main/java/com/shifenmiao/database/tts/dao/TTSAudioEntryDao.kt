package com.shifenmiao.database.tts.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.tts.entity.TTSAudioEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TTSAudioEntryDao {

    @Query("SELECT * FROM tts_audio_entries WHERE tag = :tag ORDER BY created_at DESC")
    fun getAudiosByTag(tag: String): Flow<List<TTSAudioEntryEntity>>

    @Query("SELECT * FROM tts_audio_entries WHERE tag = :tag ORDER BY created_at DESC")
    suspend fun getAudiosByTagList(tag: String): List<TTSAudioEntryEntity>

    @Query("SELECT * FROM tts_audio_entries WHERE cache_hash = :hash LIMIT 1")
    suspend fun getAudioByHash(hash: String): TTSAudioEntryEntity?

    @Query("SELECT * FROM tts_audio_entries WHERE text = :text AND tag = :tag ORDER BY created_at DESC LIMIT 1")
    suspend fun getAudioByTextAndTag(text: String, tag: String): TTSAudioEntryEntity?

    @Query("SELECT * FROM tts_audio_entries WHERE id = :id LIMIT 1")
    suspend fun getAudioById(id: String): TTSAudioEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudio(audio: TTSAudioEntryEntity)

    @Query("DELETE FROM tts_audio_entries WHERE id = :id")
    suspend fun deleteAudioById(id: String): Int

    @Query("DELETE FROM tts_audio_entries WHERE tag = :tag")
    suspend fun deleteAudiosByTag(tag: String): Int

    @Query("DELETE FROM tts_audio_entries")
    suspend fun deleteAllAudios(): Int

    @Query("SELECT * FROM tts_audio_entries ORDER BY created_at DESC")
    fun getAllAudios(): Flow<List<TTSAudioEntryEntity>>
}
