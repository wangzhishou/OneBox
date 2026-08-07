package com.shifenmiao.tts.repository

import com.shifenmiao.database.tts.dao.TTSAudioEntryDao
import com.shifenmiao.database.tts.entity.TTSAudioEntryEntity
import com.shifenmiao.model.tts.TTSAudioEntry
import com.shifenmiao.model.tts.TTSProviderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TTSAudioRepository @Inject constructor(
    private val dao: TTSAudioEntryDao,
) {

    fun getAudiosByTag(tag: String): Flow<List<TTSAudioEntry>> {
        return dao.getAudiosByTag(tag).map { list ->
            list.map { it.toModel() }
        }
    }

    suspend fun getAudioByHash(hash: String): TTSAudioEntry? {
        return dao.getAudioByHash(hash)?.toModel()
    }

    suspend fun getAudioByTextAndTag(text: String, tag: String): TTSAudioEntry? {
        return dao.getAudioByTextAndTag(text, tag)?.toModel()
    }

    suspend fun saveAudio(audio: TTSAudioEntry) {
        dao.insertAudio(audio.toEntity())
    }

    suspend fun deleteAudio(id: String): Boolean {
        return dao.deleteAudioById(id) > 0
    }

    suspend fun deleteAudiosByTag(tag: String): Int {
        return dao.deleteAudiosByTag(tag)
    }

    suspend fun getAudiosByTagList(tag: String): List<TTSAudioEntry> {
        return dao.getAudiosByTagList(tag).map { it.toModel() }
    }

    suspend fun deleteAllAudios(): Int {
        return dao.deleteAllAudios()
    }

    private fun TTSAudioEntryEntity.toModel(): TTSAudioEntry = TTSAudioEntry(
        id = id,
        text = text,
        voice = voice,
        speed = speed,
        filePath = filePath,
        createdAt = createdAt,
        tag = tag,
        providerType = runCatching { TTSProviderType.valueOf(providerType) }.getOrDefault(TTSProviderType.MIMO),
    )

    private fun TTSAudioEntry.toEntity(): TTSAudioEntryEntity = TTSAudioEntryEntity(
        id = id,
        text = text,
        voice = voice,
        speed = speed,
        filePath = filePath,
        createdAt = createdAt,
        tag = tag,
        providerType = providerType.name,
        cacheHash = id,
    )
}
