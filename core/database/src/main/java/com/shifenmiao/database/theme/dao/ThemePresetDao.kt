package com.shifenmiao.database.theme.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shifenmiao.database.theme.entity.ThemePresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ThemePresetDao {

    @Query("SELECT * FROM theme_preset ORDER BY created_at ASC")
    fun observeAll(): Flow<List<ThemePresetEntity>>

    @Query("SELECT * FROM theme_preset ORDER BY created_at ASC")
    suspend fun getAll(): List<ThemePresetEntity>

    @Query("SELECT * FROM theme_preset WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ThemePresetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ThemePresetEntity)

    @Update
    suspend fun update(entity: ThemePresetEntity)

    @Query("DELETE FROM theme_preset WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM theme_preset")
    suspend fun count(): Int
}

