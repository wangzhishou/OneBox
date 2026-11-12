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

package com.shifenmiao.database.household.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.household.entity.HouseholdItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseholdItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: HouseholdItemEntity)

    @Query("SELECT * FROM household_item ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<HouseholdItemEntity>>

    @Query(
        """
        SELECT * FROM household_item
        WHERE name LIKE '%' || :query || '%'
           OR note LIKE '%' || :query || '%'
           OR category LIKE '%' || :query || '%'
        ORDER BY updated_at DESC
        """
    )
    fun search(query: String): Flow<List<HouseholdItemEntity>>

    @Query("SELECT * FROM household_item WHERE location_id = :locationId ORDER BY updated_at DESC")
    fun observeByLocation(locationId: String): Flow<List<HouseholdItemEntity>>

    @Query("SELECT * FROM household_item WHERE id = :id")
    suspend fun getById(id: String): HouseholdItemEntity?

    @Query("SELECT COUNT(*) FROM household_item WHERE location_id = :locationId")
    suspend fun countByLocation(locationId: String): Int

    @Query("DELETE FROM household_item WHERE id = :id")
    suspend fun delete(id: String)
}
