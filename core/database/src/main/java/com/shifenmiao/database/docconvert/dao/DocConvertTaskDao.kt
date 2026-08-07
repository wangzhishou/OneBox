package com.shifenmiao.database.docconvert.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shifenmiao.database.docconvert.entity.DocConvertTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocConvertTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DocConvertTaskEntity): Long

    @Update
    suspend fun updateTask(task: DocConvertTaskEntity)

    @Query("SELECT * FROM doc_convert_tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<DocConvertTaskEntity>>

    @Query("SELECT * FROM doc_convert_tasks WHERE taskId = :taskId LIMIT 1")
    suspend fun getTaskByTaskId(taskId: String): DocConvertTaskEntity?

    @Query("SELECT * FROM doc_convert_tasks WHERE status IN ('pending', 'processing')")
    suspend fun getActiveTasks(): List<DocConvertTaskEntity>

    @Query("UPDATE doc_convert_tasks SET updatedAt = :updatedAt WHERE taskId = :taskId")
    suspend fun touchUpdatedAt(taskId: String, updatedAt: Long)

    @Query("UPDATE doc_convert_tasks SET errorMsg = :errorMsg, updatedAt = :updatedAt WHERE taskId = :taskId")
    suspend fun setErrorMsg(taskId: String, errorMsg: String?, updatedAt: Long)

    @Delete
    suspend fun deleteTask(task: DocConvertTaskEntity)

    @Query("DELETE FROM doc_convert_tasks WHERE taskId = :taskId")
    suspend fun deleteTaskByTaskId(taskId: String)
}

