package com.shifenmiao.database.marktodo.repo

import com.shifenmiao.database.marktodo.dao.MarkTodoCategoryDao
import com.shifenmiao.database.marktodo.dao.MarkTodoDashboardDao
import com.shifenmiao.database.marktodo.dao.MarkTodoTaskDao
import com.shifenmiao.database.marktodo.dao.MarkTodoTagDao
import com.shifenmiao.database.marktodo.entity.MarkTodoCategoryEntity
import com.shifenmiao.database.marktodo.entity.MarkTodoTagEntity
import com.shifenmiao.database.marktodo.entity.MarkTodoTaskEntity
import com.shifenmiao.database.marktodo.model.CategoryWithTasks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarkTodoRepository @Inject constructor(
    private val dashboardDao: MarkTodoDashboardDao,
    private val categoryDao: MarkTodoCategoryDao,
    private val taskDao: MarkTodoTaskDao,
    private val tagDao: MarkTodoTagDao,
) {

    suspend fun isEmpty(): Boolean = categoryDao.count() == 0

    suspend fun seedDefaults(categories: List<MarkTodoCategoryEntity>, tasks: List<MarkTodoTaskEntity>) {
        categoryDao.upsertAll(categories)
        tasks.forEach { taskDao.upsert(it) }
    }

    suspend fun getDashboard(): List<CategoryWithTasks> {
        return dashboardDao.getCategoriesWithTasks().activeTasksOnly()
    }

    fun observeDashboard(): Flow<List<CategoryWithTasks>> {
        return dashboardDao.observeCategoriesWithTasks().map { it.activeTasksOnly() }
    }

    fun observeAll(): Flow<List<MarkTodoTaskEntity>> = taskDao.observeAll()

    suspend fun getInboxTasks(): List<MarkTodoTaskEntity> = taskDao.getByCategoryId(null)

    suspend fun getCategoryWithTasks(categoryId: String): CategoryWithTasks? {
        return getDashboard().find { it.category.id == categoryId }
    }

    suspend fun upsertCategory(category: MarkTodoCategoryEntity) {
        val rowId = categoryDao.insertIgnore(category)
        if (rowId == -1L) {
            categoryDao.update(category)
        }
    }

    suspend fun updateCategoryOrder(categoryId: String, sortOrder: Int) {
        categoryDao.updateSortOrder(categoryId, sortOrder)
    }

    suspend fun deleteCategory(categoryId: String) {
        categoryDao.deleteById(categoryId)
    }

    suspend fun addTask(task: MarkTodoTaskEntity) {
        val maxOrder = taskDao.getMaxSortOrder(task.categoryId) ?: -1
        taskDao.upsert(task.copy(sortOrder = maxOrder + 1))
    }

    suspend fun getTaskById(taskId: String): MarkTodoTaskEntity? = taskDao.getById(taskId)

    suspend fun getTaskByIdIncludingDeleted(taskId: String): MarkTodoTaskEntity? =
        taskDao.getByIdIncludingDeleted(taskId)

    suspend fun updateTask(task: MarkTodoTaskEntity) {
        taskDao.upsert(task)
    }

    suspend fun updateTaskOrder(taskId: String, sortOrder: Int) {
        taskDao.updateSortOrder(taskId, sortOrder)
    }

    suspend fun setTaskCompleted(taskId: String, isCompleted: Boolean) {
        taskDao.setCompleted(taskId, isCompleted)
    }

    suspend fun setTaskStarred(taskId: String, isStarred: Boolean) {
        taskDao.setStarred(taskId, isStarred)
    }

    suspend fun softDeleteTask(taskId: String, deletedAt: Long): Boolean {
        return taskDao.softDeleteById(taskId, deletedAt) == 1
    }

    suspend fun restoreTask(taskId: String, deletedAt: Long): Boolean {
        return taskDao.restoreByIdAndDeletedAt(
            taskId = taskId,
            deletedAt = deletedAt,
            restoredAt = System.currentTimeMillis(),
        ) == 1
    }

    suspend fun purgeTask(taskId: String, deletedAt: Long): Boolean {
        return taskDao.purgeByIdAndDeletedAt(taskId, deletedAt) == 1
    }

    suspend fun getSoftDeletedTasks(): List<MarkTodoTaskEntity> = taskDao.getSoftDeleted()

    suspend fun getAllTasksByCategoryIdIncludingDeleted(categoryId: String): List<MarkTodoTaskEntity> =
        taskDao.getAllByCategoryIdIncludingDeleted(categoryId)

    suspend fun getMaxCategorySortOrder(): Int? = categoryDao.getMaxSortOrder()

    suspend fun existsCategoryTitle(title: String): Boolean {
        return categoryDao.countByNormalizedTitle(title) > 0
    }

    suspend fun existsTaskTitleInCategory(categoryId: String?, title: String): Boolean {
        return taskDao.countByCategoryIdAndNormalizedTitle(categoryId = categoryId, title = title) > 0
    }

    // ── 标签 ─────────────────────────────────────────

    fun observeTags(): Flow<List<MarkTodoTagEntity>> = tagDao.observeAll()

    suspend fun getTags(): List<MarkTodoTagEntity> = tagDao.getAll()

    suspend fun upsertTag(tag: MarkTodoTagEntity) = tagDao.upsert(tag)

    suspend fun deleteTag(tagId: String) = tagDao.deleteById(tagId)

    /**
     * 预置标签兜底种子：表为空时写入（升级老库走这里，新装由 FeatureDatabase onCreate 本地化写入）。
     */
    suspend fun ensurePresetTags(presets: List<MarkTodoTagEntity>) {
        if (tagDao.count() == 0) {
            tagDao.insertIgnore(presets)
        }
    }

    private fun List<CategoryWithTasks>.activeTasksOnly(): List<CategoryWithTasks> =
        map { relation -> relation.copy(tasks = relation.tasks.filter { it.deletedAt == null }) }
}
