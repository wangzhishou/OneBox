package com.shifenmiao.database.blessing.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.blessing.entity.BlessingWishEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlessingWishDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: BlessingWishEntity)

    @Query("SELECT * FROM blessing_wish WHERE date = :date ORDER BY updated_at DESC")
    fun observeByDate(date: String): Flow<List<BlessingWishEntity>>

    @Query("SELECT * FROM blessing_wish WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun observeInDateRange(
        startDate: String,
        endDate: String,
    ): Flow<List<BlessingWishEntity>>
}
