package com.shifenmiao.database.blessing.repo

import com.shifenmiao.database.blessing.dao.BlessingRecordDao
import com.shifenmiao.database.blessing.dao.BlessingTabConfigDao
import com.shifenmiao.database.blessing.dao.BlessingWishDao
import com.shifenmiao.database.blessing.entity.BlessingRecordEntity
import com.shifenmiao.database.blessing.entity.BlessingTabConfigEntity
import com.shifenmiao.database.blessing.entity.BlessingWishEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlessingRepository @Inject constructor(
    private val recordDao: BlessingRecordDao,
    private val wishDao: BlessingWishDao,
    private val tabConfigDao: BlessingTabConfigDao,
) {

    suspend fun incrementCount(date: String, type: String) {
        recordDao.increment(
            id = UUID.randomUUID().toString(),
            date = date,
            type = type,
            updatedAt = System.currentTimeMillis(),
        )
    }

    fun observeByDate(date: String): Flow<List<BlessingRecordEntity>> {
        return recordDao.observeByDate(date)
    }

    fun observeInDateRange(startDate: String, endDate: String): Flow<List<BlessingRecordEntity>> {
        return recordDao.observeInDateRange(startDate, endDate)
    }

    fun observeWishesByDate(date: String): Flow<List<BlessingWishEntity>> {
        return wishDao.observeByDate(date)
    }

    fun observeWishesInDateRange(
        startDate: String,
        endDate: String,
    ): Flow<List<BlessingWishEntity>> {
        return wishDao.observeInDateRange(startDate = startDate, endDate = endDate)
    }

    suspend fun saveWish(date: String, type: String, content: String) {
        wishDao.save(
            BlessingWishEntity(
                date = date,
                type = type,
                content = content,
                updatedAt = System.currentTimeMillis(),
            )
        )
    }

    fun observeTabConfigs(): Flow<List<BlessingTabConfigEntity>> {
        return tabConfigDao.observeAll()
    }

    suspend fun saveTabConfig(date: String, type: String, title: String, subtitle: String) {
        tabConfigDao.save(
            BlessingTabConfigEntity(
                date = date,
                type = type,
                title = title,
                subtitle = subtitle,
                updatedAt = System.currentTimeMillis(),
            )
        )
    }
}
