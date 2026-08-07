package com.shifenmiao.database.teleprompter.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.teleprompter.entity.TeleprompterScriptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeleprompterScriptDao {

    @Query("SELECT * FROM teleprompter_script ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<TeleprompterScriptEntity>>

    @Query("SELECT * FROM teleprompter_script WHERE id = :id")
    suspend fun getById(id: String): TeleprompterScriptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TeleprompterScriptEntity)

    @Query("DELETE FROM teleprompter_script WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM teleprompter_script")
    suspend fun count(): Int
}

