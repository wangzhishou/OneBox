package com.shifenmiao.database.transfer

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ChatSessionEntity)

    @Query("SELECT * FROM chat_sessions WHERE channelId = :channelId LIMIT 1")
    suspend fun getByChannelId(channelId: String): ChatSessionEntity?

    @Query("SELECT * FROM chat_sessions")
    fun observeAll(): Flow<List<ChatSessionEntity>>
}

