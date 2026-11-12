/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
