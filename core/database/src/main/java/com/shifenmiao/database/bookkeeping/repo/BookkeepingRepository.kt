package com.shifenmiao.database.bookkeeping.repo

import androidx.room.withTransaction
import com.shifenmiao.database.FeatureDatabase
import com.shifenmiao.database.bookkeeping.dao.BookkeepingCategoryDao
import com.shifenmiao.database.bookkeeping.dao.BookkeepingRecordDao
import com.shifenmiao.database.bookkeeping.entity.BookkeepingCategoryEntity
import com.shifenmiao.database.bookkeeping.entity.BookkeepingRecordEntity
import com.shifenmiao.database.bookkeeping.model.BookkeepingCategorySum
import com.shifenmiao.database.bookkeeping.model.BookkeepingRecordWithCategory
import com.shifenmiao.database.bookkeeping.model.BookkeepingTimeTotal
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookkeepingRepository @Inject constructor(
    private val database: FeatureDatabase,
    private val categoryDao: BookkeepingCategoryDao,
    private val recordDao: BookkeepingRecordDao,
) {

    suspend fun ensureDefaults(categories: List<BookkeepingCategoryEntity>) {
        if (categoryDao.count() > 0) return
        categories.forEach { category ->
            val inserted = categoryDao.insertIgnore(category)
            if (inserted == -1L) {
                categoryDao.update(category)
            }
        }
    }

    fun observeCategories(type: Int): Flow<List<BookkeepingCategoryEntity>> {
        return categoryDao.observeByType(type)
    }

    suspend fun upsertCategory(category: BookkeepingCategoryEntity) {
        val inserted = categoryDao.insertIgnore(category)
        if (inserted == -1L) {
            categoryDao.update(category)
        }
    }

    suspend fun updateCategoryOrder(categoryId: String, order: Int) {
        categoryDao.updateSortOrder(categoryId = categoryId, sortOrder = order)
    }

    suspend fun deleteCustomCategory(categoryId: String) {
        categoryDao.deleteCustomById(categoryId)
    }

    suspend fun upsertRecord(record: BookkeepingRecordEntity) {
        recordDao.upsert(record)
    }

    suspend fun deleteRecord(recordId: String) {
        recordDao.deleteById(recordId)
    }

    suspend fun getAllCategories(): List<BookkeepingCategoryEntity> {
        return categoryDao.getAll()
    }

    suspend fun getAllRecords(): List<BookkeepingRecordEntity> {
        return recordDao.getAll()
    }

    suspend fun replaceAll(
        categories: List<BookkeepingCategoryEntity>,
        records: List<BookkeepingRecordEntity>,
    ) {
        database.withTransaction {
            recordDao.deleteAll()
            categoryDao.deleteAll()
            categories.forEach { category ->
                categoryDao.insertIgnore(category)
            }
            records.forEach { record ->
                recordDao.upsert(record)
            }
        }
    }

    fun observeRecordsInRange(startTime: Long, endTime: Long): Flow<List<BookkeepingRecordWithCategory>> {
        return recordDao.observeRecordsInRange(startTime, endTime)
    }

    fun observeExpenseCategorySumsInRange(startTime: Long, endTime: Long): Flow<List<BookkeepingCategorySum>> {
        return recordDao.observeExpenseCategorySumsInRange(startTime, endTime)
    }

    fun observeIncomeCategorySumsInRange(startTime: Long, endTime: Long): Flow<List<BookkeepingCategorySum>> {
        return recordDao.observeIncomeCategorySumsInRange(startTime, endTime)
    }

    fun observeDailyExpenseTotalsInRange(startTime: Long, endTime: Long): Flow<List<BookkeepingTimeTotal>> {
        return recordDao.observeDailyExpenseTotalsInRange(startTime, endTime)
    }

    fun observeMonthlyExpenseTotalsInRange(startTime: Long, endTime: Long): Flow<List<BookkeepingTimeTotal>> {
        return recordDao.observeMonthlyExpenseTotalsInRange(startTime, endTime)
    }
}
