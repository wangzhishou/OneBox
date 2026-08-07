package com.shifenmiao.database.blessing.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.blessing.entity.BlessingTabConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlessingTabConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: BlessingTabConfigEntity)

    @Query("SELECT * FROM blessing_tab_config ORDER BY date ASC")
    fun observeAll(): Flow<List<BlessingTabConfigEntity>>
}
