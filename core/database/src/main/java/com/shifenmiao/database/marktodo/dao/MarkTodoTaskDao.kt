package com.shifenmiao.database.marktodo.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.marktodo.entity.MarkTodoTaskEntity

@Dao
interface MarkTodoTaskDao {

    @Query("SELECT * FROM marktodo_task WHERE category_id = :categoryId ORDER BY sort_order ASC, created_at ASC")
    suspend fun getByCategoryId(categoryId: String): List<MarkTodoTaskEntity>

    @Query("SELECT * FROM marktodo_task ORDER BY updated_at DESC")
    suspend fun getAll(): List<MarkTodoTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: MarkTodoTaskEntity)

    @Query("UPDATE marktodo_task SET is_completed = :isCompleted, updated_at = :updatedAt WHERE id = :taskId")
    suspend fun setCompleted(taskId: String, isCompleted: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE marktodo_task SET is_starred = :isStarred, updated_at = :updatedAt WHERE id = :taskId")
    suspend fun setStarred(taskId: String, isStarred: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE marktodo_task SET sort_order = :sortOrder, updated_at = :updatedAt WHERE id = :taskId")
    suspend fun updateSortOrder(taskId: String, sortOrder: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT MAX(sort_order) FROM marktodo_task WHERE category_id = :categoryId")
    suspend fun getMaxSortOrder(categoryId: String): Int?

    @Query("DELETE FROM marktodo_task WHERE id = :taskId")
    suspend fun deleteById(taskId: String)

    @Query("DELETE FROM marktodo_task")
    suspend fun deleteAll()

    @Query(
        """
        SELECT COUNT(*) FROM marktodo_task
        WHERE category_id = :categoryId
          AND lower(trim(title)) = lower(trim(:title))
        """
    )
    suspend fun countByCategoryIdAndNormalizedTitle(categoryId: String, title: String): Int
}
