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
