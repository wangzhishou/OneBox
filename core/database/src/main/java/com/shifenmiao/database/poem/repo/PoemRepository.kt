package com.shifenmiao.database.poem.repo

import com.shifenmiao.database.poem.dao.PoemDao
import com.shifenmiao.database.poem.entity.PoemEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PoemRepository @Inject constructor(
    private val poemDao: PoemDao,
) {

    suspend fun upsert(entity: PoemEntity) {
        poemDao.upsert(entity)
    }

    fun observeAll(): Flow<List<PoemEntity>> {
        return poemDao.observeAll()
    }

    fun observeFavorites(): Flow<List<PoemEntity>> {
        return poemDao.observeFavorites()
    }

    suspend fun getById(id: Long): PoemEntity? {
        return poemDao.getById(id)
    }

    fun observeById(id: Long): Flow<PoemEntity?> {
        return poemDao.observeById(id)
    }

    suspend fun updateAiInsight(id: Long, insight: String) {
        poemDao.updateAiInsight(id = id, insight = insight)
    }

    suspend fun updatePinyin(id: Long, pinyin: String) {
        poemDao.updatePinyin(id = id, pinyin = pinyin)
    }

    suspend fun updateTranslation(id: Long, translation: String) {
        poemDao.updateTranslation(id = id, translation = translation)
    }

    suspend fun updateFavorite(id: Long, isFavorite: Boolean) {
        poemDao.updateFavorite(id = id, isFavorite = isFavorite)
    }

    suspend fun deleteById(id: Long) {
        poemDao.deleteById(id)
    }

    suspend fun deleteAllNonFavorites() {
        poemDao.deleteAllNonFavorites()
    }
}
