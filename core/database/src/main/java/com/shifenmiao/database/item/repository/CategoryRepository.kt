package com.shifenmiao.database.item.repository

import com.shifenmiao.database.item.dao.CategoryDao
import com.shifenmiao.database.item.dao.ItemEntityDao
import com.shifenmiao.database.item.entity.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val itemDao: ItemEntityDao
) {
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun insertOrUpdateCategory(category: Category) {
        categoryDao.insertOrUpdateCategory(category)
    }

    suspend fun deleteCategory(id: Int) {
        itemDao.deleteItemCategoryCrossRefsByCategoryId(id)
        categoryDao.deleteCategory(id)
    }

    suspend fun updateCategoryName(id: Int, name: String) {
        categoryDao.updateCategoryName(id, name)
    }
}
