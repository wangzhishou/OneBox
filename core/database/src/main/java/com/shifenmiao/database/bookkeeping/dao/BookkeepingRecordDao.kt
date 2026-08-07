package com.shifenmiao.database.bookkeeping.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.bookkeeping.entity.BookkeepingRecordEntity
import com.shifenmiao.database.bookkeeping.model.BookkeepingCategorySum
import com.shifenmiao.database.bookkeeping.model.BookkeepingRecordWithCategory
import com.shifenmiao.database.bookkeeping.model.BookkeepingTimeTotal
import kotlinx.coroutines.flow.Flow

@Dao
interface BookkeepingRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: BookkeepingRecordEntity)

    @Query("SELECT * FROM bookkeeping_record ORDER BY happened_at DESC, created_at DESC")
    suspend fun getAll(): List<BookkeepingRecordEntity>

    @Query("DELETE FROM bookkeeping_record WHERE id = :recordId")
    suspend fun deleteById(recordId: String)

    @Query("DELETE FROM bookkeeping_record")
    suspend fun deleteAll()

    @Query(
        """
        SELECT r.id, r.category_id, r.type, r.amount_cents, r.note, r.happened_at, r.exclude_from_stats,
               c.name AS category_name, c.icon_key AS category_icon_key
        FROM bookkeeping_record r
        LEFT JOIN bookkeeping_category c ON c.id = r.category_id
        WHERE r.happened_at BETWEEN :startTime AND :endTime
        ORDER BY r.happened_at DESC
        """
    )
    fun observeRecordsInRange(startTime: Long, endTime: Long): Flow<List<BookkeepingRecordWithCategory>>

    @Query(
        """
        SELECT r.category_id, c.name AS category_name, c.icon_key AS category_icon_key,
               SUM(r.amount_cents) AS total_cents
        FROM bookkeeping_record r
        LEFT JOIN bookkeeping_category c ON c.id = r.category_id
        WHERE r.happened_at BETWEEN :startTime AND :endTime
          AND r.type = 0
          AND r.exclude_from_stats = 0
        GROUP BY r.category_id, c.name, c.icon_key
        ORDER BY total_cents DESC
        """
    )
    fun observeExpenseCategorySumsInRange(startTime: Long, endTime: Long): Flow<List<BookkeepingCategorySum>>

    @Query(
        """
        SELECT r.category_id, c.name AS category_name, c.icon_key AS category_icon_key,
               SUM(r.amount_cents) AS total_cents
        FROM bookkeeping_record r
        LEFT JOIN bookkeeping_category c ON c.id = r.category_id
        WHERE r.happened_at BETWEEN :startTime AND :endTime
          AND r.type = 1
          AND r.exclude_from_stats = 0
        GROUP BY r.category_id, c.name, c.icon_key
        ORDER BY total_cents DESC
        """
    )
    fun observeIncomeCategorySumsInRange(startTime: Long, endTime: Long): Flow<List<BookkeepingCategorySum>>

    @Query(
        """
        SELECT strftime('%Y-%m-%d', r.happened_at / 1000, 'unixepoch', 'localtime') AS time_key,
               SUM(r.amount_cents) AS total_cents
        FROM bookkeeping_record r
        WHERE r.happened_at BETWEEN :startTime AND :endTime
          AND r.type = 0
          AND r.exclude_from_stats = 0
        GROUP BY time_key
        ORDER BY time_key ASC
        """
    )
    fun observeDailyExpenseTotalsInRange(startTime: Long, endTime: Long): Flow<List<BookkeepingTimeTotal>>

    @Query(
        """
        SELECT strftime('%Y-%m', r.happened_at / 1000, 'unixepoch', 'localtime') AS time_key,
               SUM(r.amount_cents) AS total_cents
        FROM bookkeeping_record r
        WHERE r.happened_at BETWEEN :startTime AND :endTime
          AND r.type = 0
          AND r.exclude_from_stats = 0
        GROUP BY time_key
        ORDER BY time_key ASC
        """
    )
    fun observeMonthlyExpenseTotalsInRange(startTime: Long, endTime: Long): Flow<List<BookkeepingTimeTotal>>
}
