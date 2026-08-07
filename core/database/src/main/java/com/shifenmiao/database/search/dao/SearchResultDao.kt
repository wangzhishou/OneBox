package com.shifenmiao.database.search.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.search.entity.SearchResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(searchResult: SearchResultEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(searchResults: List<SearchResultEntity>)
    
    @Query("SELECT * FROM search_results WHERE message_id = :messageId ORDER BY `index` ASC")
    fun getSearchResultsByMessageId(messageId: Int): Flow<List<SearchResultEntity>>
    
    @Query("DELETE FROM search_results WHERE message_id = :messageId")
    suspend fun deleteByMessageId(messageId: Int): Int
}
