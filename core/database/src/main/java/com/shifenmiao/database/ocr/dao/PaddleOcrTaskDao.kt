package com.shifenmiao.database.ocr.dao

import androidx.room.*
import com.shifenmiao.database.ocr.entity.PaddleOcrTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaddleOcrTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: PaddleOcrTaskEntity): Long

    @Update
    suspend fun updateTask(task: PaddleOcrTaskEntity)

    @Query("SELECT * FROM ocr_tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<PaddleOcrTaskEntity>>

    @Query("SELECT * FROM ocr_tasks WHERE taskId = :taskId LIMIT 1")
    suspend fun getTaskByTaskId(taskId: String): PaddleOcrTaskEntity?

    @Query("SELECT * FROM ocr_tasks WHERE status IN ('pending', 'processing')")
    suspend fun getActiveTasks(): List<PaddleOcrTaskEntity>

    @Query("UPDATE ocr_tasks SET updatedAt = :updatedAt WHERE taskId = :taskId")
    suspend fun touchUpdatedAt(taskId: String, updatedAt: Long)

    @Query("UPDATE ocr_tasks SET errorMsg = :errorMsg, updatedAt = :updatedAt WHERE taskId = :taskId")
    suspend fun setErrorMsg(taskId: String, errorMsg: String?, updatedAt: Long)

    @Delete
    suspend fun deleteTask(task: PaddleOcrTaskEntity)

    @Query("DELETE FROM ocr_tasks WHERE taskId = :taskId")
    suspend fun deleteTaskByTaskId(taskId: String)
}
