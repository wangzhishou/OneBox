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
import com.shifenmiao.database.household.entity.HouseholdLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseholdLocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(location: HouseholdLocationEntity)

    @Query("SELECT * FROM household_location ORDER BY sort_order ASC, created_at ASC")
    fun observeAll(): Flow<List<HouseholdLocationEntity>>

    @Query(
        """
        SELECT * FROM household_location
        WHERE (:parentId IS NULL AND parent_id IS NULL) OR parent_id = :parentId
        ORDER BY sort_order ASC, created_at ASC
        """
    )
    fun observeChildren(parentId: String?): Flow<List<HouseholdLocationEntity>>

    @Query("SELECT * FROM household_location WHERE id = :id")
    suspend fun getById(id: String): HouseholdLocationEntity?

    @Query(
        """
        SELECT * FROM household_location
        WHERE name = :name AND ((:parentId IS NULL AND parent_id IS NULL) OR parent_id = :parentId)
        LIMIT 1
        """
    )
    suspend fun getByNameAndParent(name: String, parentId: String?): HouseholdLocationEntity?

    @Query("SELECT * FROM household_location WHERE name = :name")
    suspend fun getByName(name: String): List<HouseholdLocationEntity>

    @Query("SELECT COUNT(*) FROM household_location WHERE parent_id = :id")
    suspend fun countChildren(id: String): Int

    @Query("SELECT COUNT(*) FROM household_location")
    suspend fun count(): Int

    @Query("DELETE FROM household_location WHERE id = :id")
    suspend fun delete(id: String)
}
