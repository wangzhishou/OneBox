package com.shifenmiao.database.blessing.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.shifenmiao.database.blessing.entity.BlessingRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class BlessingRecordDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertIfAbsent(entity: BlessingRecordEntity)

    @Query(
        """
        UPDATE blessing_record
        SET count = count + 1, updated_at = :updatedAt
        WHERE date = :date AND type = :type
        """
    )
    protected abstract suspend fun incrementExisting(
        date: String,
        type: String,
        updatedAt: Long,
    )

    @Transaction
    open suspend fun increment(
        id: String,
        date: String,
        type: String,
        updatedAt: Long,
    ) {
        insertIfAbsent(
            BlessingRecordEntity(
                id = id,
                date = date,
                type = type,
                count = 0,
                updatedAt = updatedAt,
            )
        )
        incrementExisting(date = date, type = type, updatedAt = updatedAt)
    }

    @Query("SELECT * FROM blessing_record WHERE date = :date")
    abstract suspend fun getByDate(date: String): List<BlessingRecordEntity>

    @Query("SELECT * FROM blessing_record WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    abstract fun observeInDateRange(
        startDate: String,
        endDate: String,
    ): Flow<List<BlessingRecordEntity>>

    @Query("SELECT * FROM blessing_record WHERE date = :date")
    abstract fun observeByDate(date: String): Flow<List<BlessingRecordEntity>>
}
