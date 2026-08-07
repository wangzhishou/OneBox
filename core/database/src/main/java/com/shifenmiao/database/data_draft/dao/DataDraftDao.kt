package com.shifenmiao.database.data_draft.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shifenmiao.database.data_draft.entity.DataDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DataDraftDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(draft: DataDraftEntity): Long

    @Update
    suspend fun update(draft: DataDraftEntity)

    @Query("SELECT * FROM data_draft WHERE id = :id")
    suspend fun getById(id: Long): DataDraftEntity?

    @Query("SELECT * FROM data_draft WHERE draft_type = :draftType ORDER BY update_time DESC")
    fun observeAllByType(draftType: Int): Flow<List<DataDraftEntity>>

    @Query(
        "SELECT * FROM data_draft WHERE draft_type = :draftType AND related_entity_id = :relatedEntityId ORDER BY update_time DESC LIMIT 1"
    )
    suspend fun getLatestByTypeAndRelatedEntityId(
        draftType: Int,
        relatedEntityId: Int,
    ): DataDraftEntity?

    @Query("SELECT * FROM data_draft ORDER BY update_time DESC")
    fun observeAll(): Flow<List<DataDraftEntity>>

    @Query("DELETE FROM data_draft WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM data_draft WHERE draft_type = :draftType")
    suspend fun deleteAllByType(draftType: Int)

    @Query("DELETE FROM data_draft")
    suspend fun deleteAll()
}

