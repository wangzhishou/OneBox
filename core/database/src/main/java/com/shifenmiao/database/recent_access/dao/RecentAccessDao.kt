package com.shifenmiao.database.recent_access.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.recent_access.entity.RecentAccessEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentAccessDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RecentAccessEntity)

    @Query("SELECT * FROM recent_access ORDER BY accessed_at DESC LIMIT :limit")
    fun observeAll(limit: Int = 50): Flow<List<RecentAccessEntity>>

    @Query("SELECT * FROM recent_access WHERE access_type = :type ORDER BY accessed_at DESC LIMIT :limit")
    fun observeByType(type: String, limit: Int = 50): Flow<List<RecentAccessEntity>>

    @Query("SELECT * FROM recent_access ORDER BY accessed_at DESC LIMIT :limit")
    suspend fun getAll(limit: Int = 50): List<RecentAccessEntity>

    @Query("DELETE FROM recent_access WHERE uri = :uri")
    suspend fun deleteByUri(uri: String)

    @Query("DELETE FROM recent_access WHERE accessed_at < :cutoffTime")
    suspend fun deleteOlderThan(cutoffTime: Long)

    @Query("DELETE FROM recent_access")
    suspend fun clearAll()
}
