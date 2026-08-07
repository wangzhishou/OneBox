package com.shifenmiao.database.passwordvault.repo

import com.shifenmiao.database.passwordvault.dao.PasswordVaultCategoryDao
import com.shifenmiao.database.passwordvault.dao.PasswordVaultEntryDao
import com.shifenmiao.database.passwordvault.entity.PasswordVaultCategoryEntity
import com.shifenmiao.database.passwordvault.entity.PasswordVaultEntryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasswordVaultRepository @Inject constructor(
    private val entryDao: PasswordVaultEntryDao,
    private val categoryDao: PasswordVaultCategoryDao,
) {

    // ── Entry ─────────────────────────────────────────────

    fun observeAll(): Flow<List<PasswordVaultEntryEntity>> = entryDao.observeAll()

    suspend fun getAll(): List<PasswordVaultEntryEntity> = entryDao.getAll()

    suspend fun getById(entryId: String): PasswordVaultEntryEntity? = entryDao.getById(entryId)

    suspend fun save(entity: PasswordVaultEntryEntity) {
        entryDao.upsert(entity)
    }

    suspend fun delete(entryId: String) {
        entryDao.deleteById(entryId)
    }

    suspend fun deleteAll() {
        entryDao.deleteAll()
    }

    suspend fun hasEntries(): Boolean = entryDao.count() > 0

    // ── Category ──────────────────────────────────────────

    fun observeCategories(): Flow<List<PasswordVaultCategoryEntity>> = categoryDao.observeAll()

    suspend fun getAllCategories(): List<PasswordVaultCategoryEntity> = categoryDao.getAll()

    suspend fun getCategoryById(categoryId: String): PasswordVaultCategoryEntity? =
        categoryDao.getById(categoryId)

    suspend fun upsertCategory(category: PasswordVaultCategoryEntity) {
        categoryDao.upsert(category)
    }

    suspend fun updateCategoryOrder(categoryId: String, order: Int) {
        categoryDao.updateSortOrder(categoryId = categoryId, sortOrder = order)
    }

    suspend fun deleteCustomCategory(categoryId: String) {
        categoryDao.deleteCustomById(categoryId)
    }

    suspend fun ensureDefaultCategories(defaults: List<PasswordVaultCategoryEntity>) {
        if (categoryDao.count() > 0) return
        defaults.forEach { categoryDao.upsert(it) }
    }
}
