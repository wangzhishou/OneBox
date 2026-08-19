package com.shifenmiao.database.poem.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.shifenmiao.database.poem.entity.PoemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PoemDao {

    @Upsert
    suspend fun upsert(entity: PoemEntity)

    @Query("SELECT * FROM poem ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PoemEntity>>

    @Query("SELECT * FROM poem WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun observeFavorites(): Flow<List<PoemEntity>>

    @Query("SELECT * FROM poem WHERE id = :id")
    suspend fun getById(id: Long): PoemEntity?

    @Query("SELECT * FROM poem WHERE id = :id")
    fun observeById(id: Long): Flow<PoemEntity?>

    @Query("UPDATE poem SET aiInsight = :insight WHERE id = :id")
    suspend fun updateAiInsight(id: Long, insight: String)

    @Query("UPDATE poem SET pinyin = :pinyin WHERE id = :id")
    suspend fun updatePinyin(id: Long, pinyin: String)

    @Query("UPDATE poem SET translation = :translation WHERE id = :id")
    suspend fun updateTranslation(id: Long, translation: String)

    @Query("UPDATE poem SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM poem WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM poem WHERE isFavorite = 0")
    suspend fun deleteAllNonFavorites()
}
