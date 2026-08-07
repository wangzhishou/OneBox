package com.shifenmiao.database.marktodo.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.shifenmiao.database.marktodo.model.CategoryWithTasks
import kotlinx.coroutines.flow.Flow

@Dao
interface MarkTodoDashboardDao {

    @Transaction
    @Query("SELECT * FROM marktodo_category ORDER BY sort_order ASC, created_at DESC")
    suspend fun getCategoriesWithTasks(): List<CategoryWithTasks>

    /**
     * 观察分类及其任务的变化（响应式）
     * 当数据库中的分类或任务发生变化时，会自动触发更新
     */
    @Transaction
    @Query("SELECT * FROM marktodo_category ORDER BY sort_order ASC, created_at DESC")
    fun observeCategoriesWithTasks(): Flow<List<CategoryWithTasks>>
}

