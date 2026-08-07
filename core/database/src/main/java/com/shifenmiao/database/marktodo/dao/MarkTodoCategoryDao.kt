package com.shifenmiao.database.marktodo.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shifenmiao.database.marktodo.entity.MarkTodoCategoryEntity

@Dao
interface MarkTodoCategoryDao {

    @Query("SELECT * FROM marktodo_category ORDER BY sort_order ASC, created_at DESC")
    suspend fun getAll(): List<MarkTodoCategoryEntity>

    @Query("SELECT COUNT(*) FROM marktodo_category")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(category: MarkTodoCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(category: MarkTodoCategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(categories: List<MarkTodoCategoryEntity>)

    @Update
    suspend fun update(category: MarkTodoCategoryEntity)

    @Query("UPDATE marktodo_category SET sort_order = :sortOrder, updated_at = :updatedAt WHERE id = :categoryId")
    suspend fun updateSortOrder(categoryId: String, sortOrder: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM marktodo_category")
    suspend fun deleteAll()

    @Query("DELETE FROM marktodo_category WHERE id = :categoryId")
    suspend fun deleteById(categoryId: String)

    @Query(
        """
        SELECT COUNT(*) FROM marktodo_category
        WHERE lower(trim(title)) = lower(trim(:title))
        """
    )
    suspend fun countByNormalizedTitle(title: String): Int
}
