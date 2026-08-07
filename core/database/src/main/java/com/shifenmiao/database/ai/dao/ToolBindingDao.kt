package com.shifenmiao.database.ai.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.ai.entity.ToolBindingEntity

@Dao
interface ToolBindingDao {

    @Query(
        """
        SELECT tool_name
        FROM tool_binding
        WHERE owner_type = :ownerType AND owner_id = :ownerId
        ORDER BY sort_order ASC, tool_name ASC
        """
    )
    suspend fun getToolNames(ownerType: String, ownerId: Int): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bindings: List<ToolBindingEntity>)

    @Query("DELETE FROM tool_binding WHERE owner_type = :ownerType AND owner_id = :ownerId")
    suspend fun deleteByOwner(ownerType: String, ownerId: Int)
}
